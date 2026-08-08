package com.artmondo.boxters.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.artmondo.boxters.audio.GameAudioManager
import com.artmondo.boxters.data.model.*
import com.artmondo.boxters.data.repository.DictionaryRepository
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
    private var sessionSeed = (System.currentTimeMillis() xor (Math.random() * 0xFFFFFFFFL).toLong()).toInt()

    private var feedbackTimer = 0f
    private var victoryDelayTimer = 0f
    private var isVictoryClickable = false
    private var confettiTimer = 0f

    init {
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

            delay(Timing.LOADING_MIN_DISPLAY_MS.toLong())

            // Transition to menu
            gameState = if (profile != null) GameState.MENU else GameState.MENU
            val p = profile
            _uiState.value = _uiState.value.copy(
                gameState = gameState,
                dictionaryLoaded = true,
                isRegistered = p != null,
                playerName = p?.name ?: "",
                playerStats = p?.let {
                    PlayerStats(it.gamesPlayed, it.levelsCompleted, it.bestScore)
                },
                unlockedModes = p?.unlockedModes?.map { GameMode.fromId(it) }
                    ?: listOf(GameMode.SIMPLE),
                selectedMode = p?.let { GameMode.fromId(it.lastMode) } ?: GameMode.SIMPLE,
                audioEnabled = audioManager.enabled
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

    private fun startLevel(levelIndex: Int) {
        val absoluteIndex = LevelRepository.getFirstLevelForMode(currentMode.id) + levelIndex
        val levelData = LevelRepository.getLevel(absoluteIndex) ?: return

        currentLevelIndex = levelIndex
        levelScore = 0

        // Try to restore saved board state
        if (tryRestoreBoard()) return

        // Generate board
        val newBoard = BoardGenerator.generateBoard(levelData, dictionary, sessionSeed)
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

        // Check minimum word length from objectives
        val minLenFromObj = objectiveTracker?.allObjectives
            ?.filter { (it.type == "formWord" || it.type == "formWordLength") && !it.completed }
            ?.mapNotNull { it.params["minLength"] }
            ?.minOrNull()
        // Don't enforce objective min length as a hard requirement — any 3+ letter word is valid

        // Check anchor constraint
        val hasAnchors = b.field.getAllCells().any { it.isAnchor }
        if (hasAnchors) {
            val pathTouchesAnchor = result.path.any { it.isAnchor }
            if (!pathTouchesAnchor) {
                showFeedback("Must touch an anchor!")
                return
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

        // Auto-clear isolated clusters in Clear mode
        if (currentMode == GameMode.CLEAR) {
            val autoCleared = BoardGenerator.autoClearIsolated(b)
            for (coord in autoCleared) {
                val pixel = HexMath.axialToPixel(coord.q, coord.r, hexSize)
                particleSystem.emitClearEffect(center.x + pixel.x, center.y + pixel.y)
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

        p.totalScore += finalScore
        if (finalScore > p.bestScore) p.bestScore = finalScore
        p.levelsCompleted++
        p.gamesPlayed++

        // Advance level
        val nextLevel = currentLevelIndex + 1
        val isUltimate = nextLevel >= Gameplay.LEVELS_PER_MODE

        if (isUltimate) {
            // Mode completion
            val modeIndex = MODE_ORDER.indexOf(currentMode.id)
            if (modeIndex < MODE_ORDER.size - 1) {
                val nextMode = MODE_ORDER[modeIndex + 1]
                if (nextMode !in p.unlockedModes) {
                    p.unlockedModes.add(nextMode)
                }
            }
            p.currentLevels[currentMode.id] = 0
            val isCelebrated = currentMode.id in p.celebratedModes
            if (!isCelebrated) {
                p.celebratedModes.add(currentMode.id)
            }
        } else {
            p.currentLevels[currentMode.id] = nextLevel
        }

        val highest = p.highestLevels[currentMode.id] ?: 0
        if (nextLevel > highest) {
            p.highestLevels[currentMode.id] = nextLevel
        }

        p.lastMode = currentMode.id
        playerRepository.saveProfile(p)
        playerRepository.clearBoardSnapshot(currentMode.id)

        val isCelebrateUltimate = isUltimate && currentMode.id !in (profile?.celebratedModes ?: emptyList()).dropLast(1)

        gameState = GameState.VICTORY
        victoryDelayTimer = 0f
        isVictoryClickable = false

        val victoryWord = when (currentMode) {
            GameMode.SIMPLE -> "COMPLETE"
            GameMode.CLEAR -> "CLEARED"
            GameMode.CHAIN -> "CHAINED"
            GameMode.ILLUMINATE -> "ILLUMINATED"
        }
        val ultimateTitle = when (currentMode) {
            GameMode.SIMPLE -> "WORD MASTER"
            GameMode.CLEAR -> "BOARD SWEEPER"
            GameMode.CHAIN -> "COMBO KING"
            GameMode.ILLUMINATE -> "LIGHT BRINGER"
        }

        _uiState.value = _uiState.value.copy(
            gameState = GameState.VICTORY,
            score = finalScore,
            totalScore = p.totalScore,
            stars = stars,
            isUltimateVictory = isUltimate,
            victoryTitle = if (isUltimate) ultimateTitle else victoryWord,
            victorySubtitle = if (isUltimate) "You've mastered ${currentMode.displayName} mode!" else ""
        )

        // Particle burst
        val cx = _uiState.value.boardCenter.x
        val cy = _uiState.value.boardCenter.y
        particleSystem.emitVictoryBurst(cx, cy)

        // Audio
        viewModelScope.launch {
            if (isUltimate) {
                _events.send(GameEvent.PlayUltimateVictory)
            } else {
                _events.send(GameEvent.PlayVictoryFanfare)
            }
        }

        // Enable clicking after delay
        viewModelScope.launch {
            delay(Timing.VICTORY_TRANSITION_DELAY_MS.toLong())
            isVictoryClickable = true
        }
    }

    private fun onDefeat() {
        val b = board ?: return
        val p = profile ?: return

        // Find solution words
        val solutions = b.field.findWordsWithPaths(dictionary, Gameplay.MAX_SOLUTION_WORDS)
        val solutionWords = solutions.map { SolutionWord(it.word, it.path) }

        p.lives--
        p.gamesPlayed++

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

        val nextLevel = currentLevelIndex + 1
        if (nextLevel >= Gameplay.LEVELS_PER_MODE) {
            // Return to menu
            returnToMenu()
        } else {
            startLevel(nextLevel)
        }
    }

    fun onDefeatContinue() {
        val p = profile ?: return

        if (p.lives <= 0) {
            // Start cooldown
            gameState = GameState.COOLDOWN
            _uiState.value = _uiState.value.copy(
                gameState = GameState.COOLDOWN,
                cooldownUntil = p.cooldownUntil
            )
        } else {
            // Retry the same level
            startLevel(currentLevelIndex)
        }
    }

    fun onCooldownTapped() {
        val p = profile ?: return
        if (System.currentTimeMillis() < p.cooldownUntil) return

        // Restore lives
        p.lives = Gameplay.MAX_LIVES
        p.cooldownUntil = 0
        playerRepository.saveProfile(p)

        // Restart level
        startLevel(currentLevelIndex)
    }

    fun onSolutionWordSelected(path: List<HexCoord>) {
        _uiState.value = _uiState.value.copy(highlightedSolutionPath = path)
    }

    fun onToggleSound() {
        audioManager.toggle()
        _uiState.value = _uiState.value.copy(audioEnabled = audioManager.enabled)
    }

    fun onLogout() {
        returnToMenu()
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
            startLevel(newLevel)
        }
    }

    fun handleDeepLink(uri: String?) {
        if (uri == null) return
        val data = BoardSharing.decodeBoard(uri) ?: return

        val levelData = LevelRepository.getLevel(data.levelIndex) ?: return
        currentMode = GameMode.fromId(levelData.mode)
        currentLevelIndex = data.levelIndex - LevelRepository.getFirstLevelForMode(currentMode.id)

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

        // Update confetti during ultimate victory
        if (gameState == GameState.VICTORY && _uiState.value.isUltimateVictory) {
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
        // Clear board snapshot when returning to menu
        playerRepository.clearBoardSnapshot(currentMode.id)
        gameState = GameState.MENU
        board = null
        tracer = null
        objectiveTracker = null

        _uiState.value = _uiState.value.copy(
            gameState = GameState.MENU,
            playerName = p.name,
            playerStats = PlayerStats(p.gamesPlayed, p.levelsCompleted, p.bestScore),
            unlockedModes = p.unlockedModes.map { GameMode.fromId(it) },
            selectedMode = GameMode.fromId(p.lastMode),
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
            hasMovesInProgress = b.moveHistory.isNotEmpty(),
            canGoBack = currentLevelIndex > 0 && b.moveHistory.isEmpty(),
            canGoForward = run {
                val highestReached = profile?.highestLevels?.get(currentMode.id) ?: 0
                currentLevelIndex < highestReached && b.moveHistory.isEmpty()
            }
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

    override fun onCleared() {
        super.onCleared()
        saveBoardOnPause()
        audioManager.release()
    }
}
