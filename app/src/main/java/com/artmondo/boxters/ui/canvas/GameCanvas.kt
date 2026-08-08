package com.artmondo.boxters.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artmondo.boxters.data.model.*
import com.artmondo.boxters.domain.hex.HexMath
import com.artmondo.boxters.particles.ParticleSystem
import com.artmondo.boxters.ui.game.*
import com.artmondo.boxters.ui.theme.*
import com.artmondo.boxters.util.Combo
import kotlin.math.*

@Composable
fun GameCanvas(
    uiState: GameUiState,
    particleSystem: ParticleSystem,
    onTraceStart: (HexCoord) -> Unit,
    onTraceMove: (HexCoord) -> Unit,
    onTraceEnd: () -> Unit,
    onVictoryTapped: () -> Unit,
    onDefeatContinue: () -> Unit,
    onCooldownTapped: () -> Unit,
    onToggleSound: () -> Unit,
    onLogout: () -> Unit,
    onShare: () -> Unit,
    onNavigateLevel: (Int) -> Unit,
    onSolutionWordSelected: (List<HexCoord>) -> Unit,
    onFrame: (Float) -> Unit
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var boardCenter by remember { mutableStateOf(Offset.Zero) }
    var hexSize by remember { mutableFloatStateOf(61f) }
    var time by remember { mutableFloatStateOf(0f) }

    // Animation loop
    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (true) {
            androidx.compose.ui.platform.LocalView
            kotlinx.coroutines.delay(16) // ~60fps
            val now = System.nanoTime()
            val dt = if (lastFrameTime == 0L) 0.016f
            else ((now - lastFrameTime) / 1_000_000_000f).coerceAtMost(0.1f)
            lastFrameTime = now
            time += dt
            onFrame(dt)
        }
    }

    // Calculate hex size and board center when cells change
    LaunchedEffect(uiState.cells.size, canvasSize) {
        if (canvasSize.width > 0 && uiState.cells.isNotEmpty()) {
            with(density) {
                hexSize = HexMath.calculateHexSize(
                    uiState.cells.size,
                    canvasSize.width / density.density,
                    canvasSize.height / density.density
                ) * density.density
            }
            boardCenter = Offset(canvasSize.width / 2f, canvasSize.height * 0.43f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.gameState) {
                    if (uiState.gameState == GameState.PLAYING) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()

                            // Check button hits first
                            val pos = down.position
                            val buttonRadius = 20.dp.toPx()

                            // Sound button (top-right area)
                            val soundBtnPos = Offset(canvasSize.width - 50.dp.toPx(), 50.dp.toPx())
                            if ((pos - soundBtnPos).getDistance() < buttonRadius) {
                                onToggleSound()
                                return@awaitEachGesture
                            }

                            // Logout button
                            val logoutPos = Offset(canvasSize.width - 20.dp.toPx(), 50.dp.toPx())
                            if ((pos - logoutPos).getDistance() < buttonRadius) {
                                onLogout()
                                return@awaitEachGesture
                            }

                            // Share button
                            val sharePos = Offset(canvasSize.width - 30.dp.toPx(), canvasSize.height - 108.dp.toPx())
                            if ((pos - sharePos).getDistance() < buttonRadius) {
                                onShare()
                                return@awaitEachGesture
                            }

                            // Back arrow (left side)
                            val navY = canvasSize.height - 108.dp.toPx()
                            val backPos = Offset(30.dp.toPx(), navY)
                            if ((pos - backPos).getDistance() < buttonRadius) {
                                onNavigateLevel(-1)
                                return@awaitEachGesture
                            }

                            // Forward arrow (right of back)
                            val fwdPos = Offset(70.dp.toPx(), navY)
                            if ((pos - fwdPos).getDistance() < buttonRadius) {
                                onNavigateLevel(1)
                                return@awaitEachGesture
                            }

                            // Start trace
                            val relX = pos.x - boardCenter.x
                            val relY = pos.y - boardCenter.y
                            val hex = HexMath.pixelToAxial(relX, relY, hexSize)
                            onTraceStart(hex)

                            // Track drag
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                change.consume()
                                val dragPos = change.position
                                val dRelX = dragPos.x - boardCenter.x
                                val dRelY = dragPos.y - boardCenter.y
                                val dragHex = HexMath.pixelToAxial(dRelX, dRelY, hexSize)
                                onTraceMove(dragHex)
                            } while (event.changes.any { it.pressed })

                            onTraceEnd()
                        }
                    } else if (uiState.gameState == GameState.VICTORY) {
                        awaitEachGesture {
                            awaitFirstDown().consume()
                            onVictoryTapped()
                        }
                    } else if (uiState.gameState == GameState.COOLDOWN) {
                        awaitEachGesture {
                            awaitFirstDown().consume()
                            onCooldownTapped()
                        }
                    }
                }
        ) {
            canvasSize = size

            // Background gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(GameColors.backgroundMid, GameColors.backgroundDark),
                    center = Offset(size.width / 2f, size.height / 3f),
                    radius = size.height * 0.8f
                )
            )

            // Background stars
            for (star in particleSystem.stars) {
                val brightness = 0.3f + 0.7f * (0.5f + 0.5f * sin(time * star.speed + star.phase))
                drawCircle(
                    color = star.color.copy(alpha = brightness * 0.8f),
                    radius = star.size * density.density,
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }

            if (uiState.cells.isNotEmpty()) {
                // Draw hex cells
                val traceKeys = uiState.tracePath.map { it.key }.toSet()
                val solutionPathKeys = uiState.highlightedSolutionPath
                    ?.map { it.key }?.toSet() ?: emptySet()
                for (cell in uiState.cells) {
                    if (cell.isCleared || !cell.isActive) continue

                    val pixel = HexMath.axialToPixel(cell.q, cell.r, hexSize)
                    val cx = boardCenter.x + pixel.x
                    val cy = boardCenter.y + pixel.y

                    val isInTrace = cell.key in traceKeys ||
                        cell.key in solutionPathKeys
                    val isLastWordCell = cell.key in uiState.lastWordCellKeys

                    drawHexCell(
                        cx = cx, cy = cy, size = hexSize,
                        cell = cell, isInTrace = isInTrace,
                        isLastWordCell = isLastWordCell,
                        mode = uiState.mode, time = time,
                        textMeasurer = textMeasurer,
                        density = density.density
                    )
                }

                // Draw trace line
                if (uiState.tracePath.isNotEmpty()) {
                    drawTraceLine(
                        path = uiState.tracePath,
                        hexSize = hexSize,
                        boardCenter = boardCenter,
                        validation = uiState.traceValidation,
                        time = time,
                        density = density.density
                    )
                }

                // Draw solution path highlight
                if (solutionPathKeys.isNotEmpty()) {
                    drawTraceLine(
                        path = uiState.highlightedSolutionPath!!,
                        hexSize = hexSize,
                        boardCenter = boardCenter,
                        validation = TraceValidation.VALID,
                        time = time,
                        density = density.density
                    )
                }
            }

            // Draw particles
            for (p in particleSystem.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.4f),
                    radius = p.size * density.density * 2f,
                    center = Offset(p.x, p.y),
                    blendMode = BlendMode.Plus
                )
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size * density.density,
                    center = Offset(p.x, p.y)
                )
            }

            // Draw HUD text elements
            drawHud(uiState, textMeasurer, density.density, time)

            // Draw overlays
            when (uiState.gameState) {
                GameState.VICTORY -> drawVictoryOverlay(uiState, textMeasurer, density.density, time)
                GameState.DEFEAT, GameState.GAME_OVER -> drawDefeatOverlay(uiState, textMeasurer, density.density)
                GameState.COOLDOWN -> drawCooldownOverlay(uiState, textMeasurer, density.density)
                else -> {}
            }

            // Feedback message
            if (uiState.feedbackMessage != null) {
                val feedbackAlpha = (uiState.feedbackTimer / 2f).coerceIn(0f, 1f)
                drawFeedbackMessage(uiState.feedbackMessage, textMeasurer, density.density, feedbackAlpha)
            }
        }

        // Compose UI overlays for word tray, objectives, and words formed
        if (uiState.cells.isNotEmpty() && uiState.gameState == GameState.PLAYING) {
            // Word tray
            if (uiState.traceWord.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 200.dp)
                        .background(
                            GameColors.uiPanel,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val borderColor = when (uiState.traceValidation) {
                        TraceValidation.VALID -> GameColors.traceValid
                        TraceValidation.PREFIX -> GameColors.tracePrefix
                        TraceValidation.INVALID -> GameColors.traceInvalid
                        TraceValidation.NONE -> GameColors.uiPanelBorder
                    }
                    Text(
                        text = uiState.traceWord,
                        style = GameTypography.wordTray.copy(color = borderColor)
                    )
                }
            }

            // Objectives panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp, start = 16.dp, end = 16.dp)
                    .background(GameColors.uiPanel, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                for (obj in uiState.objectives) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = if (obj.completed) "✓" else "○",
                            color = if (obj.completed) GameColors.uiSuccess else GameColors.uiTextDim,
                            fontSize = 18.sp,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = obj.description,
                            style = GameTypography.objective.copy(
                                textDecoration = if (obj.completed) TextDecoration.LineThrough else null,
                                color = if (obj.completed) GameColors.uiTextDim else GameColors.uiText
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (!obj.completed && obj.target > 1) {
                            Text(
                                text = "${obj.progress}/${obj.target}",
                                style = GameTypography.objective.copy(color = GameColors.uiAccent),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Words formed list
            if (uiState.wordsFormed.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.wordsFormed.withIndex().toList()) { (index, word) ->
                        Text(
                            text = "${index + 1}.$word",
                            style = GameTypography.objective.copy(
                                color = GameColors.uiAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier
                                .background(
                                    GameColors.uiPanel,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Solution modal for defeat/game over
        if (uiState.gameState == GameState.DEFEAT || uiState.gameState == GameState.GAME_OVER) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(GameColors.uiPanel, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Words You Could Have Played",
                    style = GameTypography.levelName.copy(fontSize = 21.sp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Solution word chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.solutionWords) { sw ->
                        Text(
                            text = sw.word,
                            style = GameTypography.objective.copy(
                                color = GameColors.uiAccent,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .background(GameColors.backgroundLight, RoundedCornerShape(4.dp))
                                .clickable { onSolutionWordSelected(sw.path) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Continue / Retry button
                androidx.compose.material3.Button(
                    onClick = onDefeatContinue,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = GameColors.uiAccent,
                        contentColor = GameColors.backgroundDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (uiState.gameState == GameState.GAME_OVER)
                            "Retry Level" else "Continue (${uiState.lives} lives left)",
                        style = GameTypography.button
                    )
                }
            }
        }
    }
}

// --- Drawing Extension Functions ---

private fun DrawScope.drawHexCell(
    cx: Float, cy: Float, size: Float,
    cell: HexCellState, isInTrace: Boolean,
    isLastWordCell: Boolean, mode: GameMode,
    time: Float, textMeasurer: TextMeasurer,
    density: Float
) {
    val corners = HexMath.hexCorners(cx, cy, size)
    val path = Path().apply {
        moveTo(corners[0].x, corners[0].y)
        for (i in 1 until 6) lineTo(corners[i].x, corners[i].y)
        close()
    }

    // Glow effect
    if (cell.glowIntensity > 0f || isInTrace) {
        val glowAlpha = if (isInTrace) 0.3f else cell.glowIntensity * 0.3f
        drawCircle(
            color = GameColors.boardGlow.copy(alpha = glowAlpha),
            radius = size * 1.3f,
            center = Offset(cx, cy),
            blendMode = BlendMode.Plus
        )
    }

    // Illumination overlay
    if (cell.isIlluminated && !isInTrace) {
        drawPath(path, GameColors.illuminateLit)
    }

    // Chain mode last-word pulse
    if (isLastWordCell && mode == GameMode.CHAIN) {
        val pulseAlpha = 0.15f + 0.1f * sin(time * 4f)
        drawPath(path, Color(0xFF60A5FA).copy(alpha = pulseAlpha))
    }

    // Fill
    val fillColor = when {
        isInTrace -> GameColors.boardFill.copy(alpha = 0.15f)
        cell.isAnchor -> GameColors.anchorFill
        else -> GameColors.boardFill.copy(alpha = 0.06f)
    }
    drawPath(path, fillColor)

    // Border
    val borderWidth = if (isInTrace) 3f * density else 2.5f * density
    val borderColor = when {
        isInTrace -> GameColors.boardStroke
        cell.isAnchor -> GameColors.anchorRim
        else -> GameColors.boardStroke.copy(alpha = 0.4f)
    }
    drawPath(path, borderColor, style = Stroke(width = borderWidth))

    // Anchor indicator (small diamond below cell)
    if (cell.isAnchor) {
        val diamondSize = 4f * density
        val diamondY = cy + size + 6f * density
        val diamondPath = Path().apply {
            moveTo(cx, diamondY - diamondSize)
            lineTo(cx + diamondSize, diamondY)
            lineTo(cx, diamondY + diamondSize)
            lineTo(cx - diamondSize, diamondY)
            close()
        }
        drawPath(diamondPath, GameColors.anchorRim)
    }

    // Letter
    cell.letter?.let { letter ->
        val fontSize = (size * 0.675f / density).sp
        val textColor = if (isInTrace) Color.White else GameColors.boardText
        val textResult = textMeasurer.measure(
            AnnotatedString(letter.toString()),
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                color = textColor
            )
        )
        drawText(
            textResult,
            topLeft = Offset(
                cx - textResult.size.width / 2f,
                cy - textResult.size.height / 2f
            )
        )
    }
}

private fun DrawScope.drawTraceLine(
    path: List<HexCoord>,
    hexSize: Float,
    boardCenter: Offset,
    validation: TraceValidation,
    time: Float,
    density: Float
) {
    if (path.size < 2) return

    val color = when (validation) {
        TraceValidation.VALID -> GameColors.traceValid
        TraceValidation.PREFIX -> GameColors.tracePrefix
        TraceValidation.INVALID -> GameColors.traceInvalid
        TraceValidation.NONE -> GameColors.tracePrefix
    }

    val points = path.map { coord ->
        val pixel = HexMath.axialToPixel(coord.q, coord.r, hexSize)
        Offset(boardCenter.x + pixel.x, boardCenter.y + pixel.y)
    }

    // Outer glow line
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color.copy(alpha = 0.35f),
            start = points[i],
            end = points[i + 1],
            strokeWidth = 10f * density
        )
    }

    // Inner core line
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = 5f * density
        )
    }

    // Node dots
    for (point in points) {
        drawCircle(color, radius = 6f * density, center = point)
        drawCircle(Color.White, radius = 2.5f * density, center = point)
    }

    // Animated highlight dot
    val totalLength = (0 until points.size - 1).sumOf {
        (points[it + 1] - points[it]).getDistance().toDouble()
    }.toFloat()
    if (totalLength > 0f) {
        val progress = (time * 2f % 1f)
        val targetDist = progress * totalLength
        var accumulated = 0f
        for (i in 0 until points.size - 1) {
            val segLen = (points[i + 1] - points[i]).getDistance()
            if (accumulated + segLen >= targetDist) {
                val segProgress = (targetDist - accumulated) / segLen
                val dotPos = points[i] + (points[i + 1] - points[i]) * segProgress
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 4f * density, center = dotPos)
                break
            }
            accumulated += segLen
        }
    }
}

private fun DrawScope.drawHud(
    uiState: GameUiState,
    textMeasurer: TextMeasurer,
    density: Float,
    time: Float
) {
    val topPadding = 50f * density
    val leftPadding = 16f * density

    // Level name
    val levelName = uiState.levelData?.name ?: ""
    val levelText = textMeasurer.measure(
        AnnotatedString(levelName),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            color = GameColors.uiAccent
        )
    )
    drawText(levelText, topLeft = Offset(leftPadding, topPadding))

    // Mode badge + level number
    val modeBadge = "${uiState.mode.displayName} ${uiState.modeLevelNum}"
    val modeAccent = when (uiState.mode) {
        GameMode.SIMPLE -> GameColors.simpleAccent
        GameMode.CLEAR -> GameColors.clearAccent
        GameMode.CHAIN -> GameColors.chainAccent
        GameMode.ILLUMINATE -> GameColors.illuminateAccent
    }
    val badgeText = textMeasurer.measure(
        AnnotatedString(modeBadge),
        style = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = modeAccent
        )
    )
    drawText(badgeText, topLeft = Offset(leftPadding, topPadding + levelText.size.height + 4 * density))

    // Lives, Moves, Score row
    val statsY = topPadding + levelText.size.height + badgeText.size.height + 12 * density
    val centerX = size.width / 2f

    // Lives (hearts)
    val hearts = buildString {
        repeat(uiState.lives) { append("♥") }
        repeat(3 - uiState.lives) { append("♡") }
    }
    val heartsText = textMeasurer.measure(
        AnnotatedString(hearts),
        style = TextStyle(fontSize = 21.sp, color = GameColors.uiError)
    )
    drawText(heartsText, topLeft = Offset(centerX - 120 * density, statsY))

    // Moves
    val movesText = textMeasurer.measure(
        AnnotatedString("${uiState.movesRemaining}"),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = if (uiState.movesRemaining <= 2) GameColors.uiWarning else GameColors.uiText
        )
    )
    drawText(movesText, topLeft = Offset(centerX - movesText.size.width / 2f, statsY))

    val movesLabel = textMeasurer.measure(
        AnnotatedString("moves"),
        style = TextStyle(fontSize = 13.sp, color = GameColors.uiTextDim)
    )
    drawText(movesLabel, topLeft = Offset(centerX - movesLabel.size.width / 2f, statsY + movesText.size.height))

    // Level Score
    val scoreX = centerX + 60 * density
    val scoreText = textMeasurer.measure(
        AnnotatedString("${uiState.score}"),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = GameColors.uiAccent
        )
    )
    drawText(scoreText, topLeft = Offset(scoreX - scoreText.size.width / 2f, statsY))

    val scoreLabelText = textMeasurer.measure(
        AnnotatedString("score"),
        style = TextStyle(fontSize = 13.sp, color = GameColors.uiTextDim)
    )
    drawText(scoreLabelText, topLeft = Offset(scoreX - scoreLabelText.size.width / 2f, statsY + scoreText.size.height))

    // Total Score
    val totalX = centerX + 140 * density
    val totalText = textMeasurer.measure(
        AnnotatedString("${uiState.totalScore}"),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            color = GameColors.uiTextDim
        )
    )
    drawText(totalText, topLeft = Offset(totalX - totalText.size.width / 2f, statsY + 2 * density))

    val totalLabelText = textMeasurer.measure(
        AnnotatedString("total"),
        style = TextStyle(fontSize = 13.sp, color = GameColors.uiTextDim.copy(alpha = 0.6f))
    )
    drawText(totalLabelText, topLeft = Offset(totalX - totalLabelText.size.width / 2f, statsY + totalText.size.height + 2 * density))

    // Mode-specific HUD
    val modeHudY = statsY + 42 * density
    when (uiState.mode) {
        GameMode.CHAIN -> {
            if (uiState.comboCount > 0) {
                val comboText = textMeasurer.measure(
                    AnnotatedString("${uiState.comboMultiplier}x COMBO"),
                    style = TextStyle(
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        color = GameColors.chainCombo
                    )
                )
                drawText(comboText, topLeft = Offset(centerX - comboText.size.width / 2f, modeHudY))
            }
        }
        GameMode.ILLUMINATE -> {
            val litText = textMeasurer.measure(
                AnnotatedString("${uiState.percentLit}% Lit"),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = GameColors.illuminateAccent
                )
            )
            drawText(litText, topLeft = Offset(centerX - litText.size.width / 2f, modeHudY))
        }
        GameMode.CLEAR -> {
            val remainText = textMeasurer.measure(
                AnnotatedString("${uiState.cellsRemaining} cells"),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = GameColors.clearAccent
                )
            )
            drawText(remainText, topLeft = Offset(centerX - remainText.size.width / 2f, modeHudY))
        }
        else -> {}
    }

    // Tutorial message
    uiState.tutorialMessage?.let { msg ->
        if (uiState.wordsFormed.isEmpty()) {
            val tutY = modeHudY + 30 * density
            val tutText = textMeasurer.measure(
                AnnotatedString(msg),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontSize = 17.sp,
                    color = GameColors.uiText.copy(alpha = 0.8f)
                ),
                constraints = androidx.compose.ui.unit.Constraints(
                    maxWidth = (size.width * 0.85f).toInt()
                )
            )
            // Background pill
            drawRoundRect(
                color = GameColors.uiPanel,
                topLeft = Offset(centerX - tutText.size.width / 2f - 12 * density, tutY - 4 * density),
                size = Size(
                    tutText.size.width + 24 * density,
                    tutText.size.height + 8 * density
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8 * density)
            )
            drawText(tutText, topLeft = Offset(centerX - tutText.size.width / 2f, tutY))
        }
    }

    // Canvas buttons (sound toggle, logout)
    val btnRadius = 18f * density
    val rightPadding = 20f * density

    // Sound toggle
    val soundX = size.width - rightPadding - btnRadius * 3
    val soundY = topPadding + 4 * density
    drawCircle(GameColors.uiPanel, radius = btnRadius, center = Offset(soundX, soundY))
    val soundText = textMeasurer.measure(
        AnnotatedString(if (uiState.audioEnabled) "♪" else "✕"),
        style = TextStyle(fontSize = 16.sp, color = GameColors.uiText)
    )
    drawText(soundText, topLeft = Offset(soundX - soundText.size.width / 2f, soundY - soundText.size.height / 2f))

    // Logout
    val logoutX = size.width - rightPadding
    val logoutY = topPadding + 4 * density
    drawCircle(GameColors.uiPanel, radius = btnRadius, center = Offset(logoutX, logoutY))
    val logoutText = textMeasurer.measure(
        AnnotatedString("✕"),
        style = TextStyle(fontSize = 18.sp, color = GameColors.uiText)
    )
    drawText(logoutText, topLeft = Offset(logoutX - logoutText.size.width / 2f, logoutY - logoutText.size.height / 2f))

    // Bottom buttons: navigation arrows + share
    val bottomY = size.height - 108 * density

    // Back arrow
    if (uiState.canGoBack) {
        val backX = 30f * density
        drawCircle(GameColors.uiPanel, radius = btnRadius, center = Offset(backX, bottomY))
        val backText = textMeasurer.measure(
            AnnotatedString("◀"),
            style = TextStyle(fontSize = 16.sp, color = GameColors.uiText)
        )
        drawText(backText, topLeft = Offset(backX - backText.size.width / 2f, bottomY - backText.size.height / 2f))
    }

    // Forward arrow
    if (uiState.canGoForward) {
        val fwdX = 70f * density
        drawCircle(GameColors.uiPanel, radius = btnRadius, center = Offset(fwdX, bottomY))
        val fwdText = textMeasurer.measure(
            AnnotatedString("▶"),
            style = TextStyle(fontSize = 16.sp, color = GameColors.uiText)
        )
        drawText(fwdText, topLeft = Offset(fwdX - fwdText.size.width / 2f, bottomY - fwdText.size.height / 2f))
    }

    // Share button
    val shareX = size.width - 30f * density
    drawCircle(GameColors.uiPanel, radius = btnRadius, center = Offset(shareX, bottomY))
    val shareText = textMeasurer.measure(
        AnnotatedString("↗"),
        style = TextStyle(fontSize = 18.sp, color = GameColors.uiAccent)
    )
    drawText(shareText, topLeft = Offset(shareX - shareText.size.width / 2f, bottomY - shareText.size.height / 2f))
}

private fun DrawScope.drawVictoryOverlay(
    uiState: GameUiState,
    textMeasurer: TextMeasurer,
    density: Float,
    time: Float
) {
    // Semi-transparent overlay
    drawRect(Color.Black.copy(alpha = 0.6f))

    val centerX = size.width / 2f
    val centerY = size.height * 0.4f

    // Title
    val titleSize = if (uiState.isUltimateVictory) 42.sp else 36.sp
    val titleColor = if (uiState.isUltimateVictory) {
        GameColors.uiAccent.copy(alpha = 0.8f + 0.2f * sin(time * 3f))
    } else GameColors.uiAccent
    val titleText = textMeasurer.measure(
        AnnotatedString(uiState.victoryTitle),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = titleSize,
            color = titleColor
        )
    )
    drawText(titleText, topLeft = Offset(centerX - titleText.size.width / 2f, centerY))

    // Stars
    val starsY = centerY + titleText.size.height + 20 * density
    val starStr = "★".repeat(uiState.stars) + "☆".repeat(3 - uiState.stars)
    val starsText = textMeasurer.measure(
        AnnotatedString(starStr),
        style = TextStyle(fontSize = 42.sp, color = GameColors.uiAccent)
    )
    drawText(starsText, topLeft = Offset(centerX - starsText.size.width / 2f, starsY))

    // Score
    val scoreY = starsY + starsText.size.height + 16 * density
    val scoreText = textMeasurer.measure(
        AnnotatedString("Score: ${uiState.score}"),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = GameColors.uiText
        )
    )
    drawText(scoreText, topLeft = Offset(centerX - scoreText.size.width / 2f, scoreY))

    // Subtitle
    if (uiState.victorySubtitle.isNotEmpty()) {
        val subY = scoreY + scoreText.size.height + 12 * density
        val subText = textMeasurer.measure(
            AnnotatedString(uiState.victorySubtitle),
            style = TextStyle(fontSize = 18.sp, color = GameColors.uiTextDim)
        )
        drawText(subText, topLeft = Offset(centerX - subText.size.width / 2f, subY))
    }

    // Tap to continue
    val tapY = size.height * 0.75f
    val tapAlpha = 0.5f + 0.5f * sin(time * 2f)
    val tapText = textMeasurer.measure(
        AnnotatedString("Tap anywhere to continue"),
        style = TextStyle(fontSize = 18.sp, color = GameColors.uiText.copy(alpha = tapAlpha))
    )
    drawText(tapText, topLeft = Offset(centerX - tapText.size.width / 2f, tapY))
}

private fun DrawScope.drawDefeatOverlay(
    uiState: GameUiState,
    textMeasurer: TextMeasurer,
    density: Float
) {
    val alpha = if (uiState.gameState == GameState.GAME_OVER) 0.5f else 0.45f
    drawRect(Color.Black.copy(alpha = alpha))

    val centerX = size.width / 2f
    val titleY = 50f * density
    val titleText = textMeasurer.measure(
        AnnotatedString(if (uiState.gameState == GameState.GAME_OVER) "Game Over" else "Out of Moves"),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 31.sp,
            color = GameColors.uiError
        )
    )
    drawText(titleText, topLeft = Offset(centerX - titleText.size.width / 2f, titleY))
}

private fun DrawScope.drawCooldownOverlay(
    uiState: GameUiState,
    textMeasurer: TextMeasurer,
    density: Float
) {
    drawRect(Color.Black.copy(alpha = 0.92f))

    val centerX = size.width / 2f
    val centerY = size.height * 0.4f

    val titleText = textMeasurer.measure(
        AnnotatedString("Out of Lives"),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            color = GameColors.uiError
        )
    )
    drawText(titleText, topLeft = Offset(centerX - titleText.size.width / 2f, centerY))

    val timerText = textMeasurer.measure(
        AnnotatedString(uiState.cooldownRemaining),
        style = TextStyle(
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 62.sp,
            color = GameColors.uiAccent
        )
    )
    drawText(timerText, topLeft = Offset(
        centerX - timerText.size.width / 2f,
        centerY + titleText.size.height + 24 * density
    ))

    val remaining = uiState.cooldownUntil - System.currentTimeMillis()
    if (remaining <= 0) {
        val tapText = textMeasurer.measure(
            AnnotatedString("Tap anywhere to continue"),
            style = TextStyle(fontSize = 18.sp, color = GameColors.uiText)
        )
        drawText(tapText, topLeft = Offset(
            centerX - tapText.size.width / 2f,
            size.height * 0.7f
        ))
    }
}

private fun DrawScope.drawFeedbackMessage(
    message: String,
    textMeasurer: TextMeasurer,
    density: Float,
    alpha: Float
) {
    val centerX = size.width / 2f
    val y = size.height * 0.28f

    val text = textMeasurer.measure(
        AnnotatedString(message),
        style = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            color = Color.White.copy(alpha = alpha)
        )
    )

    drawRoundRect(
        color = Color(0xCC000000).copy(alpha = alpha * 0.8f),
        topLeft = Offset(centerX - text.size.width / 2f - 16 * density, y - 4 * density),
        size = Size(text.size.width + 32 * density, text.size.height + 8 * density),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20 * density)
    )
    drawText(text, topLeft = Offset(centerX - text.size.width / 2f, y))
}
