package com.artmondo.boxters.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.artmondo.boxters.audio.GameAudioManager
import com.artmondo.boxters.data.model.*
import com.artmondo.boxters.data.repository.DictionaryRepository
import com.artmondo.boxters.data.repository.FirebaseRepository
import com.artmondo.boxters.data.repository.LevelRepository
import com.artmondo.boxters.data.repository.PlayerRepository
import com.artmondo.boxters.domain.board.BoardGenerator
import com.artmondo.boxters.domain.board.SeededRNG
import com.artmondo.boxters.domain.dictionary.Dictionary
import com.artmondo.boxters.domain.hex.HexMath
import com.artmondo.boxters.domain.objectives.ObjectiveTracker
import com.artmondo.boxters.domain.scoring.ScoringEngine
import com.artmondo.boxters.domain.tracer.TraceResult
import com.artmondo.boxters.domain.tracer.WordTracer
import com.artmondo.boxters.particles.ParticleSystem
import com.artmondo.boxters.sharing.BoardSharing
import com.artmondo.boxters.ui.theme.GameColors
import com.artmondo.boxters.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val playerRepository = PlayerRepository(application)
    private val firebaseRepo = FirebaseRepository(application)
    val audioManager = GameAudioManager(application)
    val particleSystem = ParticleSystem()

    private lateinit var dictionary: Dictionary
    private var board: Board? = null
    private var tracer: WordTracer? = null
    private var objectiveTracker: ObjectiveTracker? = null

    private var profile: PlayerProfile? = null
    private var gameState = GameState.LOADING
    private var currentMode = GameMode.SIMPLE
    private var currentLevelIndex = 0
    private var levelScore = 0
    private var feedbackTimer = 0f
    private var victoryDelayTimer = 0f
    private var isVictoryClickable = false
    private var confettiTimer = 0f
    private var isNewBest = false
    private var prevLevelBest = 0
    private var isInGauntlet = false
    private var isInRandomMode = false
    private var apexJustUnlocked = false

    init {
        // Load theme preference before anything renders
        val savedDark = playerRepository.loadDarkMode()
        GameColors.isDark = savedDark
        _uiState.value = _uiState.value.copy(isDarkMode = savedDark)

        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            // Load dictionary
            dictionary = DictionaryRepository.loadDictionary(getApplication())

            // Init audio
            audioManager.init()

            // Load profile
            profile = playerRepository.loadProfile()

            // Migrate existing profiles to per-level scoring
            profile?.let { p ->
                if (p.playerId.isEmpty()) {
                    p.playerId = java.util.UUID.randomUUID().toString()
                }
                // Migration: if levelScores is empty but totalScore > 0, set baseScore
                if (p.levelScores.isEmpty() && p.totalScore > 0) {
                    p.baseScore = p.totalScore
                }
                // Anti-tamper: recompute totalScore from levelScores + baseScore
                val recomputed = p.baseScore + p.levelScores.values.sum()
                if (recomputed != p.totalScore) {
                    p.totalScore = recomputed
                }
                if (p.levelScores.isNotEmpty()) {
                    p.bestScore = p.levelScores.values.max()
                }
                // Restore gauntlet state
                isInGauntlet = p.isInGauntlet
                playerRepository.saveProfile(p)
            }

            delay(Timing.LOADING_MIN_DISPLAY_MS.toLong())

            // Transition to menu
            gameState = if (profile != null) GameState.MENU else GameState.MENU
            val p = profile
            _uiState.value = _uiState.value.copy(
                gameState = gameState,
                dictionaryLoaded = true,
                isRegistered = p != null,
                playerName = p?.name ?: "",
                playerId = p?.playerId ?: "",
                totalScore = p?.totalScore ?: 0,
                playerStats = p?.let {
                    PlayerStats(it.gamesPlayed, it.levelsCompleted, it.bestScore)
                },
                unlockedModes = p?.unlockedModes?.map { GameMode.fromId(it) }
                    ?: listOf(GameMode.SIMPLE),
                selectedMode = p?.let { GameMode.fromId(it.lastMode) } ?: GameMode.SIMPLE,
                audioEnabled = audioManager.enabled,
                gender = p?.gender ?: "",
                ageGroup = p?.ageGroup ?: "",
                hasCompletedApex = p?.celebratedModes?.contains("apex") == true ||
                    (p?.highestLevels?.get("apex") ?: 0) >= Gameplay.LEVELS_PER_MODE - 1
            )

            // Check cooldown
            if (p != null && p.cooldownUntil > System.currentTimeMillis()) {
                gameState = GameState.COOLDOWN
                _uiState.value = _uiState.value.copy(
                    gameState = GameState.COOLDOWN,
                    cooldownUntil = p.cooldownUntil,
                    lives = p.lives
                )
            }

            // Sync profile with Firebase (fire-and-forget)
            if (p != null) {
                launch {
                    try {
                        val changed = firebaseRepo.syncProfile(p)
                        if (changed) {
                            playerRepository.saveProfile(p)
                            _uiState.value = _uiState.value.copy(
                                totalScore = p.totalScore,
                                playerStats = PlayerStats(p.gamesPlayed, p.levelsCompleted, p.bestScore)
                            )
                        }
                    } catch (_: Exception) { /* sync failed silently */ }
                }
            }
        }
    }

    fun onRegister(name: String) {
        val trimmed = name.trim().take(20)
        if (trimmed.isEmpty()) return

        val newProfile = PlayerProfile(name = trimmed)
        profile = newProfile
        playerRepository.saveProfile(newProfile)

        gameState = GameState.MENU
        _uiState.value = _uiState.value.copy(
            gameState = GameState.MENU,
            isRegistered = true,
            playerName = trimmed,
            playerStats = PlayerStats(0, 0, 0),
            unlockedModes = listOf(GameMode.SIMPLE),
            selectedMode = GameMode.SIMPLE
        )
    }

    fun onModeSelected(mode: GameMode) {
        val p = profile ?: return
        if (mode.id !in p.unlockedModes) return
        currentMode = mode
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun onPlayClicked() {
        val p = profile ?: return
        currentMode = _uiState.value.selectedMode
        currentLevelIndex = p.currentLevels[currentMode.id] ?: 0

        // Check cooldown
        if (p.cooldownUntil > System.currentTimeMillis()) {
            gameState = GameState.COOLDOWN
            _uiState.value = _uiState.value.copy(
                gameState = GameState.COOLDOWN,
                cooldownUntil = p.cooldownUntil,
                lives = p.lives
            )
            return
        }

        startLevel(currentLevelIndex)
    }

    fun onRandomClicked() {
        val p = profile ?: return

        // Check cooldown
        if (p.cooldownUntil > System.currentTimeMillis()) {
            gameState = GameState.COOLDOWN
            _uiState.value = _uiState.value.copy(
                gameState = GameState.COOLDOWN,
                cooldownUntil = p.cooldownUntil,
                lives = p.lives
            )
            return
        }

        isInRandomMode = true
        loadRandomChallenge()
    }

    private fun startLevel(levelIndex: Int, forceNew: Boolean = false) {
        val absoluteIndex = LevelRepository.getFirstLevelForMode(currentMode.id) + levelIndex
        val levelData = LevelRepository.getLevel(absoluteIndex) ?: return

        currentLevelIndex = levelIndex
        levelScore = 0

        // Try to restore saved board state (only when resuming a session)
        if (!forceNew && tryRestoreBoard()) return

        // Generate board with a unique seed each time
        val levelSeed = SeededRNG.seedForLevel(
            (System.currentTimeMillis() xor (Math.random() * 0xFFFFFFFFL).toLong()).toInt(),
            levelIndex
        )
        val newBoard = BoardGenerator.generateBoard(levelData, dictionary, levelSeed)
        newBoard.apexWeights = levelData.apexWeights
        board = newBoard

        // Init tracer
        tracer = WordTracer(newBoard.field, dictionary)

        // Init objectives
        objectiveTracker = ObjectiveTracker(levelData.objectives)

        gameState = GameState.PLAYING
        victoryDelayTimer = 0f
        isVictoryClickable = false

        updateUiState(levelData)
    }

    fun onTraceStart(coord: HexCoord) {
        if (gameState != GameState.PLAYING) return
        val t = tracer ?: return

        if (t.start(coord)) {
            val validation = when {
                t.isValidWord -> TraceValidation.VALID
                t.isValidPrefix -> TraceValidation.PREFIX
                else -> TraceValidation.INVALID
            }
            _uiState.value = _uiState.value.copy(
                tracePath = t.path.map { it.coord },
                traceWord = t.currentWord,
                traceValidation = validation
            )

            val letter = t.path.lastOrNull()?.letter
            if (letter != null) {
                viewModelScope.launch { _events.send(GameEvent.PlayTone(letter)) }
            }
        }
    }

    fun onTraceMove(coord: HexCoord) {
        if (gameState != GameState.PLAYING) return
        val t = tracer ?: return

        val result = t.extend(coord)
        if (result != TraceResult.NONE) {
            val validation = when {
                t.isValidWord -> TraceValidation.VALID
                t.isValidPrefix -> TraceValidation.PREFIX
                t.currentWord.length >= 3 -> TraceValidation.INVALID
                else -> TraceValidation.PREFIX
            }
            _uiState.value = _uiState.value.copy(
                tracePath = t.path.map { it.coord },
                traceWord = t.currentWord,
                traceValidation = validation
            )

            if (result == TraceResult.EXTENDED) {
                val letter = t.path.lastOrNull()?.letter
                if (letter != null) {
                    viewModelScope.launch { _events.send(GameEvent.PlayTone(letter)) }
                }
            }
        }
    }

    fun onTraceEnd() {
        if (gameState != GameState.PLAYING) return
        val t = tracer ?: return
        val b = board ?: return

        val result = t.end()
        _uiState.value = _uiState.value.copy(
            tracePath = emptyList(),
            traceWord = "",
            traceValidation = TraceValidation.NONE
        )

        if (result.word.length < Gameplay.MIN_WORD_LENGTH) {
            if (result.word.isNotEmpty()) {
                showFeedback("Words must be at least 3 letters")
            }
            return
        }

        if (!result.isValid) {
            // Invalid word — costs 1 move
            b.movesRemaining--
            showFeedback("Not a word!")
            viewModelScope.launch { _events.send(GameEvent.PlayError) }
            checkDefeat()
            return
        }

        val word = result.word

        // Check if already used
        if (b.field.wordsFormed.contains(word)) {
            showFeedback("Already used!")
            return
        }

        // Enforce minimum word length from active objectives
        val minLenFromObj = objectiveTracker?.allObjectives
            ?.filter { (it.type == "formWord" || it.type == "formWordLength") && !it.completed }
            ?.mapNotNull { it.params["minLength"] }
            ?.minOrNull()
        if (minLenFromObj != null && word.length < minLenFromObj) {
            showFeedback("Words must be at least $minLenFromObj letters!")
            return
        }

        // Check anchor constraint (in Apex mode, relaxed after anchor objective met)
        val hasAnchors = b.field.getAllCells().any { it.isAnchor }
        if (hasAnchors) {
            val anchorObjectiveMet = b.mode == "apex" &&
                objectiveTracker?.allObjectives
                    ?.filter { it.type == "illuminateAnchors" }
                    ?.all { it.completed } == true
            if (!anchorObjectiveMet) {
                val pathTouchesAnchor = result.path.any { it.isAnchor }
                if (!pathTouchesAnchor) {
                    showFeedback("Must touch an anchor!")
                    return
                }
            }
        }

        // Valid word — submit it
        submitWord(word, result.path)
    }

    private fun submitWord(word: String, path: List<HexCell>) {
        val b = board ?: return
        val levelData = _uiState.value.levelData ?: return

        // Execute move on board
        val moveResult = b.executeMove(word, path)
        b.field.wordsFormed.add(word)

        // Calculate move score
        val moveScore = ScoringEngine.calculateMoveScore(word, currentMode.id, b.comboCount)
        levelScore += moveScore

        // Update objectives
        val lastMove = Move(word, path.map { it.coord })
        objectiveTracker?.checkAll(b, lastMove)

        // Particle effects
        val hexSize = _uiState.value.hexSize
        val center = _uiState.value.boardCenter
        for (coord in moveResult.cleared) {
            val pixel = HexMath.axialToPixel(coord.q, coord.r, hexSize)
            particleSystem.emitClearEffect(center.x + pixel.x, center.y + pixel.y)
        }
        for ((coord, _) in moveResult.replaced) {
            val pixel = HexMath.axialToPixel(coord.q, coord.r, hexSize)
            particleSystem.emitChainReplace(center.x + pixel.x, center.y + pixel.y)
        }
        for (coord in moveResult.illuminated) {
            val pixel = HexMath.axialToPixel(coord.q, coord.r, hexSize)
            particleSystem.emitIlluminate(center.x + pixel.x, center.y + pixel.y)
        }

        // Auto-clear isolated clusters in Clear mode or Apex clear effect
        if (currentMode == GameMode.CLEAR || (currentMode == GameMode.APEX && b.lastApexEffect == "clear")) {
            val autoCleared = BoardGenerator.autoClearIsolated(b)
            for (coord in autoCleared) {
                val pixel = HexMath.axialToPixel(coord.q, coord.r, hexSize)
                particleSystem.emitClearEffect(center.x + pixel.x, center.y + pixel.y)
            }
            // Recheck objectives after auto-clear may have cleared remaining cells
            if (autoCleared.isNotEmpty()) {
                objectiveTracker?.checkAll(b, lastMove)
            }
        }

        // Show score feedback
        showFeedback("+$moveScore pts")

        // Play word chord
        viewModelScope.launch { _events.send(GameEvent.PlayWordChord(word)) }

        // Check victory
        if (objectiveTracker?.allPrimaryComplete == true) {
            onVictory()
            return
        }

        // Check defeat
        checkDefeat()
        if (gameState != GameState.PLAYING) return

        // Save board state after each word
        saveBoardState()

        // Save state
        updateUiState(levelData)
    }

    private fun checkDefeat() {
        val b = board ?: return
        if (b.movesRemaining <= 0 && objectiveTracker?.allPrimaryComplete != true) {
            onDefeat()
        } else {
            val levelData = _uiState.value.levelData ?: return
            updateUiState(levelData)
        }
    }

    private fun onVictory() {
        val b = board ?: return
        val levelData = _uiState.value.levelData ?: return
        val p = profile ?: return

        val finalScore = ScoringEngine.calculateLevelScore(b, currentMode.id, objectiveTracker!!, levelData)
        val stars = ScoringEngine.getStars(finalScore, levelData)

        // Per-level best score tracking
        val levelId = levelData.id
        prevLevelBest = p.levelScores[levelId] ?: 0
        val cappedScore = minOf(finalScore, Gameplay.MAX_LEVEL_SCORE)
        isNewBest = cappedScore > prevLevelBest
        p.levelScores[levelId] = maxOf(prevLevelBest, cappedScore)

        // Recompute totalScore from levelScores + baseScore (anti-tamper)
        p.totalScore = p.baseScore + p.levelScores.values.sum()
        p.bestScore = p.levelScores.values.maxOrNull() ?: 0

        p.levelsCompleted++
        p.gamesPlayed++

        // Restore lives only if playing at the same level or higher than where lives were lost
        var livesRestored = false
        if (p.lives < Gameplay.MAX_LIVES) {
            val currentAbsolute = absoluteLevel(currentMode.id, currentLevelIndex)
            if (p.livesLostAtLevel < 0 || currentAbsolute >= p.livesLostAtLevel) {
                p.lives = Gameplay.MAX_LIVES
                p.livesLostAtLevel = -1
                livesRestored = true
            }
        }

        // Advance level
        val nextLevel = currentLevelIndex + 1
        val isUltimate = nextLevel >= Gameplay.LEVELS_PER_MODE

        val isFirstModeCompletion = isUltimate && currentMode.id !in p.celebratedModes
        var enterGauntlet = false

        if (isUltimate) {
            // Mode completion
            val modeIndex = MODE_ORDER.indexOf(currentMode.id)
            if (modeIndex < MODE_ORDER.size - 1) {
                val nextMode = MODE_ORDER[modeIndex + 1]
                // Apex unlock requires score threshold
                if (nextMode == "apex") {
                    if (p.totalScore >= Gameplay.APEX_UNLOCK_SCORE) {
                        if ("apex" !in p.unlockedModes) {
                            p.unlockedModes.add("apex")
                        }
                    } else {
                        enterGauntlet = true
                    }
                } else if (nextMode !in p.unlockedModes) {
                    p.unlockedModes.add(nextMode)
                }
            }
            p.currentLevels[currentMode.id] = 0
            if (isFirstModeCompletion) {
                p.celebratedModes.add(currentMode.id)
            }
        } else {
            p.currentLevels[currentMode.id] = nextLevel
        }

        val highest = p.highestLevels[currentMode.id] ?: 0
        if (nextLevel > highest) {
            p.highestLevels[currentMode.id] = nextLevel
        }

        // General Apex unlock check — works during gauntlet, replays, or any victory
        apexJustUnlocked = false
        if ("apex" !in p.unlockedModes &&
            "illuminate" in p.celebratedModes &&
            p.totalScore >= Gameplay.APEX_UNLOCK_SCORE) {
            p.unlockedModes.add("apex")
            if (isInGauntlet) {
                isInGauntlet = false
                p.isInGauntlet = false
            }
            apexJustUnlocked = true
        }

        // Handle gauntlet entry
        if (enterGauntlet && !apexJustUnlocked) {
            isInGauntlet = true
            p.isInGauntlet = true
        }

        p.lastMode = currentMode.id
        playerRepository.saveProfile(p)
        playerRepository.clearBoardSnapshot(currentMode.id)

        // Submit score to Firebase (fire-and-forget)
        viewModelScope.launch {
            try { firebaseRepo.submitScore(p) } catch (_: Exception) {}
        }

        gameState = GameState.VICTORY
        victoryDelayTimer = 0f
        isVictoryClickable = false

        val victoryWord = when (currentMode) {
            GameMode.SIMPLE -> "COMPLETE"
            GameMode.CLEAR -> "CLEARED"
            GameMode.CHAIN -> "CHAINED"
            GameMode.ILLUMINATE -> "ILLUMINATED"
            GameMode.APEX -> "CONQUERED"
        }
        val ultimateTitle = when (currentMode) {
            GameMode.SIMPLE -> "WORD MASTER"
            GameMode.CLEAR -> "BOARD SWEEPER"
            GameMode.CHAIN -> "CHAIN LEGEND"
            GameMode.ILLUMINATE -> "MASTER OF LIGHT"
            GameMode.APEX -> "APEX LEGEND"
        }

        // Apex completion check — show game complete screen
        val isApexComplete = currentMode == GameMode.APEX && isUltimate && isFirstModeCompletion

        val effectiveUltimate = isFirstModeCompletion || apexJustUnlocked

        _uiState.value = _uiState.value.copy(
            gameState = GameState.VICTORY,
            score = finalScore,
            totalScore = p.totalScore,
            stars = stars,
            isUltimateVictory = effectiveUltimate,
            isNewBest = isNewBest,
            prevLevelBest = prevLevelBest,
            isApexComplete = isApexComplete,
            apexJustUnlocked = apexJustUnlocked,
            isInGauntlet = isInGauntlet,
            victoryTitle = when {
                apexJustUnlocked -> "APEX UNLOCKED"
                isFirstModeCompletion -> ultimateTitle
                else -> victoryWord
            },
            livesRestored = livesRestored,
            victorySubtitle = when {
                apexJustUnlocked -> "You've earned the right to play Apex mode!"
                isApexComplete -> "You've mastered all five modes."
                isFirstModeCompletion -> "You've mastered ${currentMode.displayName} mode!"
                else -> ""
            }
        )

        // Particle burst
        val cx = _uiState.value.boardCenter.x
        val cy = _uiState.value.boardCenter.y
        particleSystem.emitVictoryBurst(cx, cy)

        // Audio
        viewModelScope.launch {
            if (effectiveUltimate) {
                _events.send(GameEvent.PlayUltimateVictory)
            } else {
                _events.send(GameEvent.PlayVictoryFanfare)
            }
        }

        // Confetti for celebrations
        if (effectiveUltimate) {
            confettiTimer = 0f
        }

        // For Apex completion, transition to GAME_COMPLETE after delay
        if (isApexComplete) {
            viewModelScope.launch {
                delay(Timing.VICTORY_TRANSITION_DELAY_MS.toLong())
                isVictoryClickable = true
                gameState = GameState.GAME_COMPLETE
                _uiState.value = _uiState.value.copy(gameState = GameState.GAME_COMPLETE)
                _events.send(GameEvent.PlayPuzzleLoop)
            }
        } else {
            // Enable clicking after delay
            viewModelScope.launch {
                delay(Timing.VICTORY_TRANSITION_DELAY_MS.toLong())
                isVictoryClickable = true
            }
        }
    }

    private fun onDefeat() {
        val b = board ?: return
        val p = profile ?: return

        // Clear saved board — defeat means this board is done
        playerRepository.clearBoardSnapshot(currentMode.id)

        // Find solution words (more for illuminate/apex)
        val maxSolutions = if (currentMode == GameMode.ILLUMINATE || currentMode == GameMode.APEX) 12 else Gameplay.MAX_SOLUTION_WORDS
        val solutions = b.field.findWordsWithPaths(dictionary, maxSolutions)
        val solutionWords = solutions.map { SolutionWord(it.word, it.path) }

        p.lives--
        p.gamesPlayed++
        p.lives = maxOf(p.lives, 0)

        // Track the level where the first life was lost (for life-restore gating)
        if (p.livesLostAtLevel < 0) {
            p.livesLostAtLevel = absoluteLevel(currentMode.id, currentLevelIndex)
        }

        if (p.lives <= 0) {
            // Game over
            gameState = GameState.GAME_OVER
            p.cooldownUntil = System.currentTimeMillis() + Timing.COOLDOWN_DURATION_MS
            playerRepository.saveProfile(p)

            _uiState.value = _uiState.value.copy(
                gameState = GameState.GAME_OVER,
                lives = 0,
                solutionWords = solutionWords
            )

            viewModelScope.launch { _events.send(GameEvent.PlayFail) }
        } else {
            gameState = GameState.DEFEAT
            playerRepository.saveProfile(p)

            _uiState.value = _uiState.value.copy(
                gameState = GameState.DEFEAT,
                lives = p.lives,
                solutionWords = solutionWords
            )

            viewModelScope.launch { _events.send(GameEvent.PlayFail) }
        }
    }

    fun onVictoryTapped() {
        if (!isVictoryClickable) return

        // Apex unlock celebration → go to menu with Apex available
        if (apexJustUnlocked) {
            apexJustUnlocked = false
            returnToMenu()
            return
        }

        // Gauntlet mode — load next random gauntlet level
        if (isInGauntlet) {
            loadGauntletLevel()
            return
        }

        // Random mode — load next random challenge
        if (isInRandomMode) {
            loadRandomChallenge()
            return
        }

        val nextLevel = currentLevelIndex + 1
        if (nextLevel >= Gameplay.LEVELS_PER_MODE) {
            // Mode completed — advance to next mode if available
            val modeIndex = MODE_ORDER.indexOf(currentMode.id)
            if (modeIndex < MODE_ORDER.size - 1) {
                val nextModeId = MODE_ORDER[modeIndex + 1]
                if (nextModeId == "apex" && "apex" !in (profile?.unlockedModes ?: emptyList())) {
                    // Apex not yet unlocked, gauntlet should handle this
                    if (isInGauntlet) {
                        loadGauntletLevel()
                    } else {
                        returnToMenu()
                    }
                } else {
                    currentMode = GameMode.fromId(nextModeId)
                    startLevel(0, forceNew = true)
                }
            } else {
                // Last mode completed — return to menu
                returnToMenu()
            }
        } else {
            startLevel(nextLevel, forceNew = true)
        }
    }

    fun onGameCompleteAction(action: String) {
        // Stop puzzle music
        viewModelScope.launch { _events.send(GameEvent.StopPuzzleLoop) }

        when (action) {
            "play_any" -> returnToMenu()
            "random" -> loadRandomChallenge()
        }
    }

    private fun loadGauntletLevel() {
        val modes = listOf("simple", "clear", "chain", "illuminate")
        val randomMode = modes.random()
        val modeLevels = LevelRepository.getLevelsForMode(randomMode)
        // Pick levels 7-14 (indices 6-13) within the mode
        val highLevels = modeLevels.filterIndexed { i, _ -> i >= 6 }
        if (highLevels.isEmpty()) return
        val level = highLevels.random()
        val absoluteIndex = LevelRepository.LEVEL_DATA.indexOf(level)
        if (absoluteIndex < 0) return
        currentMode = GameMode.fromId(randomMode)
        currentLevelIndex = absoluteIndex - LevelRepository.getFirstLevelForMode(randomMode)
        startLevel(currentLevelIndex, forceNew = true)
    }

    private fun loadRandomChallenge() {
        val modes = listOf("simple", "clear", "chain", "illuminate", "apex")
        val randomMode = modes.random()
        val modeLevels = LevelRepository.getLevelsForMode(randomMode)
        // Pick levels 6+ (indices 5+) from any mode
        val highLevels = modeLevels.filterIndexed { i, _ -> i >= 5 }
        if (highLevels.isEmpty()) return
        val level = highLevels.random()
        val absoluteIndex = LevelRepository.LEVEL_DATA.indexOf(level)
        if (absoluteIndex < 0) return
        currentMode = GameMode.fromId(randomMode)
        currentLevelIndex = absoluteIndex - LevelRepository.getFirstLevelForMode(randomMode)
        startLevel(currentLevelIndex, forceNew = true)
    }

    fun onDefeatContinue() {
        val p = profile ?: return

        // Clear saved board so retry gets a fresh board
        playerRepository.clearBoardSnapshot(currentMode.id)

        if (p.lives <= 0) {
            // Start cooldown
            gameState = GameState.COOLDOWN
            _uiState.value = _uiState.value.copy(
                gameState = GameState.COOLDOWN,
                cooldownUntil = p.cooldownUntil
            )
        } else if (isInRandomMode) {
            // Random mode — load a new random challenge instead of retrying
            loadRandomChallenge()
        } else {
            // Retry the same level
            startLevel(currentLevelIndex, forceNew = true)
        }
    }

    fun onCooldownTapped() {
        val p = profile ?: return
        if (System.currentTimeMillis() < p.cooldownUntil) return

        // Restore lives (cooldown penalty served)
        p.lives = Gameplay.MAX_LIVES
        p.livesLostAtLevel = -1
        p.cooldownUntil = 0
        playerRepository.saveProfile(p)

        // If in gauntlet, load gauntlet level instead of restarting
        if (isInGauntlet) {
            loadGauntletLevel()
        } else if (isInRandomMode) {
            loadRandomChallenge()
        } else {
            startLevel(currentLevelIndex, forceNew = true)
        }
    }

    fun onSolutionWordSelected(path: List<HexCoord>) {
        _uiState.value = _uiState.value.copy(
            highlightedSolutionPath = path.ifEmpty { null }
        )
    }

    fun onToggleSound() {
        audioManager.toggle()
        _uiState.value = _uiState.value.copy(audioEnabled = audioManager.enabled)
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(leaderboardLoading = true)
            try {
                val allEntries = firebaseRepo.fetchLeaderboard()
                val playerId = profile?.playerId ?: ""
                val rank = firebaseRepo.findPlayerRank(allEntries, playerId)
                val playerEntry = if (rank > 30) allEntries.getOrNull(rank - 1) else null
                _uiState.value = _uiState.value.copy(
                    leaderboard = allEntries.take(30),
                    playerRank = rank,
                    playerLeaderboardEntry = playerEntry,
                    leaderboardLoading = false
                )
            } catch (_: Exception) {
                val cached = firebaseRepo.getCachedLeaderboard()
                val playerId = profile?.playerId ?: ""
                val rank = firebaseRepo.findPlayerRank(cached, playerId)
                val playerEntry = if (rank > 30) cached.getOrNull(rank - 1) else null
                _uiState.value = _uiState.value.copy(
                    leaderboard = cached.take(30),
                    playerRank = rank,
                    playerLeaderboardEntry = playerEntry,
                    leaderboardLoading = false
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        val trimmed = name.trim().take(20)
        if (trimmed.isEmpty()) return
        val p = profile ?: return
        p.name = trimmed
        playerRepository.saveProfile(p)
        _uiState.value = _uiState.value.copy(playerName = trimmed)
        // Sync updated name with Firebase
        viewModelScope.launch {
            try { firebaseRepo.submitScore(p) } catch (_: Exception) {}
        }
    }

    fun onGenderChanged(gender: String) {
        val p = profile ?: return
        p.gender = gender
        playerRepository.saveProfile(p)
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun onAgeGroupChanged(ageGroup: String) {
        val p = profile ?: return
        p.ageGroup = ageGroup
        playerRepository.saveProfile(p)
        _uiState.value = _uiState.value.copy(ageGroup = ageGroup)
    }

    fun onExportProfile(): String? {
        val p = profile ?: return null
        return try {
            val json = kotlinx.serialization.json.Json.encodeToString(PlayerProfile.serializer(), p)
            val salt = "bxp_k7m2q9w4"
            val hash = java.security.MessageDigest.getInstance("SHA-256")
                .digest("$json$salt".toByteArray())
                .joinToString("") { "%02x".format(it) }
            val payload = "$json|$hash"
            android.util.Base64.encodeToString(payload.toByteArray(), android.util.Base64.NO_WRAP)
        } catch (_: Exception) { null }
    }

    fun onImportProfile(encoded: String): Boolean {
        return try {
            val decoded = String(android.util.Base64.decode(encoded.trim(), android.util.Base64.NO_WRAP))
            val parts = decoded.split("|")
            if (parts.size != 2) return false
            val json = parts[0]
            val hash = parts[1]
            val salt = "bxp_k7m2q9w4"
            val expectedHash = java.security.MessageDigest.getInstance("SHA-256")
                .digest("$json$salt".toByteArray())
                .joinToString("") { "%02x".format(it) }
            if (hash != expectedHash) return false
            val imported = kotlinx.serialization.json.Json.decodeFromString(PlayerProfile.serializer(), json)
            profile = imported
            playerRepository.saveProfile(imported)
            isInGauntlet = imported.isInGauntlet
            returnToMenu()
            true
        } catch (_: Exception) { false }
    }

    fun onToggleTheme() {
        GameColors.isDark = !GameColors.isDark
        playerRepository.saveDarkMode(GameColors.isDark)
        _uiState.value = _uiState.value.copy(isDarkMode = GameColors.isDark)
    }

    fun onLogout() {
        returnToMenu()
    }

    fun onLoadBoard(clipboardText: String?) {
        if (clipboardText.isNullOrBlank()) {
            showFeedback("Clipboard is empty")
            return
        }
        val data = BoardSharing.decodeBoard(clipboardText)
        if (data == null) {
            showFeedback("No valid board link found")
            return
        }
        val levelData = LevelRepository.getLevel(data.levelIndex)
        if (levelData == null) {
            showFeedback("Invalid level data")
            return
        }
        currentMode = GameMode.fromId(levelData.mode)
        currentLevelIndex = data.levelIndex - LevelRepository.getFirstLevelForMode(currentMode.id)

        // Unlock mode and level so the shared board is playable
        val p = profile
        if (p != null) {
            if (currentMode.id !in p.unlockedModes) {
                p.unlockedModes.add(currentMode.id)
            }
            val highest = p.highestLevels[currentMode.id] ?: 0
            if (currentLevelIndex > highest) {
                p.highestLevels[currentMode.id] = currentLevelIndex
            }
            playerRepository.saveProfile(p)
        }

        val newBoard = BoardGenerator.generateBoardFromLetters(levelData, data.letters, data.anchorIndices)
        board = newBoard
        tracer = WordTracer(newBoard.field, dictionary)
        objectiveTracker = ObjectiveTracker(levelData.objectives)
        gameState = GameState.PLAYING
        levelScore = 0

        updateUiState(levelData)
        showFeedback("Board loaded!")
    }

    fun onShareBoard() {
        val b = board ?: return
        val levelData = _uiState.value.levelData ?: return
        val absoluteIndex = LevelRepository.getFirstLevelForMode(currentMode.id) + currentLevelIndex
        val url = BoardSharing.encodeBoard(b, absoluteIndex, levelData)
        viewModelScope.launch {
            _events.send(GameEvent.CopyToClipboard(url))
        }
        showFeedback("Board link copied!")
    }

    fun onNavigateLevel(delta: Int) {
        if (_uiState.value.hasMovesInProgress) return
        val highestReached = profile?.highestLevels?.get(currentMode.id) ?: 0
        val maxAllowed = minOf(highestReached, Gameplay.LEVELS_PER_MODE - 1)
        val newLevel = (currentLevelIndex + delta).coerceIn(0, maxAllowed)
        if (newLevel != currentLevelIndex) {
            startLevel(newLevel, forceNew = true)
        }
    }

    fun handleDeepLink(uri: String?) {
        if (uri == null) return
        val data = BoardSharing.decodeBoard(uri) ?: return

        val levelData = LevelRepository.getLevel(data.levelIndex) ?: return
        currentMode = GameMode.fromId(levelData.mode)
        currentLevelIndex = data.levelIndex - LevelRepository.getFirstLevelForMode(currentMode.id)

        // Unlock mode and level so the shared board is playable
        val p = profile
        if (p != null) {
            if (currentMode.id !in p.unlockedModes) {
                p.unlockedModes.add(currentMode.id)
            }
            val highest = p.highestLevels[currentMode.id] ?: 0
            if (currentLevelIndex > highest) {
                p.highestLevels[currentMode.id] = currentLevelIndex
            }
            playerRepository.saveProfile(p)
        }

        val newBoard = BoardGenerator.generateBoardFromLetters(levelData, data.letters, data.anchorIndices)
        board = newBoard
        tracer = WordTracer(newBoard.field, dictionary)
        objectiveTracker = ObjectiveTracker(levelData.objectives)
        gameState = GameState.PLAYING
        levelScore = 0

        updateUiState(levelData)
    }

    fun onFrame(dt: Float) {
        // Update particles
        particleSystem.update(dt)

        // Decay cell glow
        board?.field?.getAllCells()?.forEach { cell ->
            if (cell.glowIntensity > 0f) {
                cell.glowIntensity = maxOf(0f, cell.glowIntensity - 2f * dt)
            }
        }

        // Update feedback timer
        if (feedbackTimer > 0f) {
            feedbackTimer -= dt
            if (feedbackTimer <= 0f) {
                _uiState.value = _uiState.value.copy(feedbackMessage = null, feedbackTimer = 0f)
            }
        }

        // Update cooldown display
        if (gameState == GameState.COOLDOWN) {
            val p = profile ?: return
            val remaining = p.cooldownUntil - System.currentTimeMillis()
            if (remaining <= 0) {
                _uiState.value = _uiState.value.copy(cooldownRemaining = "0:00")
            } else {
                val mins = remaining / 60000
                val secs = (remaining % 60000) / 1000
                _uiState.value = _uiState.value.copy(
                    cooldownRemaining = "$mins:${secs.toString().padStart(2, '0')}"
                )
            }
        }

        // Update confetti during ultimate victory or game complete
        if ((gameState == GameState.VICTORY && _uiState.value.isUltimateVictory) ||
            gameState == GameState.GAME_COMPLETE) {
            confettiTimer += dt
            if (confettiTimer >= Timing.CONFETTI_INTERVAL_MS / 1000f) {
                confettiTimer = 0f
                val width = _uiState.value.boardCenter.x * 2f
                val height = _uiState.value.boardCenter.y * 2f
                if (width > 0 && height > 0) {
                    particleSystem.emitConfetti(width, height)
                }
            }
        }
    }

    private fun returnToMenu() {
        val p = profile ?: return
        // Save board state so player can resume where they left off
        if (gameState == GameState.PLAYING && board != null && board!!.moveHistory.isNotEmpty()) {
            saveBoardState()
        }
        gameState = GameState.MENU
        board = null
        tracer = null
        objectiveTracker = null
        isInRandomMode = false

        _uiState.value = _uiState.value.copy(
            gameState = GameState.MENU,
            playerName = p.name,
            playerStats = PlayerStats(p.gamesPlayed, p.levelsCompleted, p.bestScore),
            unlockedModes = p.unlockedModes.map { GameMode.fromId(it) },
            selectedMode = GameMode.fromId(p.lastMode),
            hasCompletedApex = "apex" in p.celebratedModes ||
                (p.highestLevels["apex"] ?: 0) >= Gameplay.LEVELS_PER_MODE - 1,
            isInRandomMode = false,
            cells = emptyList(),
            tracePath = emptyList(),
            wordsFormed = emptyList(),
            solutionWords = emptyList(),
            highlightedSolutionPath = null
        )
    }

    private fun showFeedback(message: String) {
        feedbackTimer = Timing.FEEDBACK_DISPLAY_MS / 1000f
        _uiState.value = _uiState.value.copy(
            feedbackMessage = message,
            feedbackTimer = feedbackTimer
        )
    }

    private fun updateUiState(levelData: LevelData) {
        val b = board ?: return
        val modeLevelNum = currentLevelIndex + 1

        val cells = b.field.getAllCells().map { cell ->
            HexCellState(
                q = cell.q, r = cell.r, key = cell.key,
                letter = cell.letter, cellType = cell.cellType,
                isActive = cell.isActive, isCleared = cell.isCleared,
                isIlluminated = cell.isIlluminated, isAnchor = cell.isAnchor,
                glowIntensity = cell.glowIntensity, usedInMove = cell.usedInMove
            )
        }

        val objectives = objectiveTracker?.allObjectives?.map {
            ObjectiveState(it.type, it.description, it.target, it.progress, it.completed, it.isPrimary)
        } ?: emptyList()

        val allCells = b.field.getAllCells()
        val percentLit = if (allCells.isNotEmpty()) {
            (allCells.count { it.isIlluminated }.toFloat() / allCells.size * 100).toInt()
        } else 0
        val cellsRemaining = allCells.count { it.isActive && !it.isCleared }

        // Per-level best for HUD
        val levelBest = profile?.levelScores?.get(levelData.id) ?: 0

        _uiState.value = _uiState.value.copy(
            gameState = gameState,
            cells = cells,
            mode = currentMode,
            levelData = levelData,
            modeLevelNum = modeLevelNum,
            movesRemaining = b.movesRemaining,
            maxMoves = b.maxMoves,
            score = levelScore,
            totalScore = profile?.totalScore ?: 0,
            lives = profile?.lives ?: Gameplay.MAX_LIVES,
            objectives = objectives,
            wordsFormed = b.field.wordsFormed.toList(),
            tutorialMessage = levelData.tutorial?.message,
            comboCount = b.comboCount,
            comboMultiplier = Combo.MULTIPLIERS[minOf(b.comboCount, Combo.MAX_COMBO)],
            percentLit = percentLit,
            cellsRemaining = cellsRemaining,
            lastWordCellKeys = b.lastWordCellKeys.toSet(),
            lastApexEffect = b.lastApexEffect,
            levelBest = levelBest,
            isInGauntlet = isInGauntlet,
            isInRandomMode = isInRandomMode,
            hasMovesInProgress = b.moveHistory.isNotEmpty(),
            canGoBack = currentLevelIndex > 0 && b.moveHistory.isEmpty(),
            canGoForward = run {
                val highestReached = profile?.highestLevels?.get(currentMode.id) ?: 0
                currentLevelIndex < highestReached && b.moveHistory.isEmpty()
            },
            tracePath = emptyList(),
            traceWord = "",
            traceValidation = TraceValidation.NONE,
            solutionWords = emptyList(),
            highlightedSolutionPath = null
        )
    }

    private fun saveBoardState() {
        val b = board ?: return
        val snapshot = BoardSnapshot(
            levelIndex = currentLevelIndex,
            cells = b.field.getAllCells().map { cell ->
                CellSnapshot(
                    q = cell.q, r = cell.r,
                    letter = cell.letter,
                    cellType = cell.cellType.name,
                    isActive = cell.isActive,
                    isCleared = cell.isCleared,
                    isIlluminated = cell.isIlluminated,
                    usedInMove = cell.usedInMove
                )
            },
            moveHistory = b.moveHistory.map { MoveSnapshot(it.word, it.path.map { c -> c.key }) },
            movesRemaining = b.movesRemaining,
            score = levelScore,
            wordsUsed = b.field.wordsFormed.toList(),
            objectivesProgress = objectiveTracker?.allObjectives?.map {
                ObjectiveSnapshot(it.type, it.progress, it.completed)
            } ?: emptyList(),
            comboCount = b.comboCount,
            lastWordCellKeys = b.lastWordCellKeys.toList(),
            lives = profile?.lives ?: Gameplay.MAX_LIVES
        )
        playerRepository.saveBoardSnapshot(currentMode.id, snapshot)
    }

    private fun tryRestoreBoard(): Boolean {
        val snapshot = playerRepository.loadBoardSnapshot(currentMode.id) ?: return false
        if (snapshot.levelIndex != currentLevelIndex) return false
        if (snapshot.moveHistory.isEmpty()) return false

        val absoluteIndex = LevelRepository.getFirstLevelForMode(currentMode.id) + currentLevelIndex
        val levelData = LevelRepository.getLevel(absoluteIndex) ?: return false

        val newBoard = Board()
        newBoard.mode = currentMode.id
        newBoard.maxMoves = levelData.maxMoves

        val radius = com.artmondo.boxters.domain.hex.HexMath.shapeToRadius(levelData.layout.shape)
        val coords = com.artmondo.boxters.domain.hex.HexMath.hexSpiral(HexCoord(0, 0), radius)

        // Create cells from snapshot
        for (cs in snapshot.cells) {
            val cell = HexCell(q = cs.q, r = cs.r)
            cell.letter = cs.letter
            cell.cellType = CellType.valueOf(cs.cellType)
            cell.isActive = cs.isActive
            cell.isCleared = cs.isCleared
            cell.isIlluminated = cs.isIlluminated
            cell.usedInMove = cs.usedInMove
            newBoard.field.addCell(cell)
        }

        newBoard.movesRemaining = snapshot.movesRemaining
        newBoard.comboCount = snapshot.comboCount
        newBoard.lastWordCellKeys.addAll(snapshot.lastWordCellKeys)
        for (ms in snapshot.moveHistory) {
            newBoard.moveHistory.add(Move(ms.word, ms.pathKeys.map { HexCoord.fromKey(it) }))
        }
        newBoard.field.wordsFormed.addAll(snapshot.wordsUsed)

        board = newBoard
        tracer = com.artmondo.boxters.domain.tracer.WordTracer(newBoard.field, dictionary)
        objectiveTracker = ObjectiveTracker(levelData.objectives)

        // Restore objective progress
        val objs = objectiveTracker?.allObjectives ?: emptyList()
        for ((i, objSnap) in snapshot.objectivesProgress.withIndex()) {
            if (i < objs.size) {
                objs[i].progress = objSnap.progress
                objs[i].completed = objSnap.completed
            }
        }

        levelScore = snapshot.score
        gameState = GameState.PLAYING
        victoryDelayTimer = 0f
        isVictoryClickable = false

        updateUiState(levelData)
        return true
    }

    fun saveBoardOnPause() {
        if (gameState == GameState.PLAYING && board != null) {
            saveBoardState()
        }
    }

    /** Compute an absolute level index across all modes for difficulty comparison. */
    private fun absoluteLevel(modeId: String, levelIndex: Int): Int {
        val modeIndex = MODE_ORDER.indexOf(modeId).coerceAtLeast(0)
        return modeIndex * Gameplay.LEVELS_PER_MODE + levelIndex
    }

    override fun onCleared() {
        super.onCleared()
        saveBoardOnPause()
        audioManager.release()
    }
}
