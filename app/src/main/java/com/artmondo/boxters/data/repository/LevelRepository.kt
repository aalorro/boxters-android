package com.artmondo.boxters.data.repository

import com.artmondo.boxters.data.model.LayoutDef
import com.artmondo.boxters.data.model.LevelData
import com.artmondo.boxters.data.model.ObjectiveDef
import com.artmondo.boxters.data.model.TutorialDef

object LevelRepository {

    val LEVEL_DATA: List<LevelData> = listOf(

        // ── SIMPLE MODE (Levels 0-13) ──

        LevelData(
            id = "simple_01",
            name = "First Light",
            mode = "simple",
            tier = "simple",
            maxMoves = 3,
            scoreMult = 0.4f,
            twoStarTarget = 60,
            threeStarTarget = 80,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form a valid word (3+ letters)", target = 1, params = mapOf("minLength" to 3))
            ),
            tutorial = TutorialDef("Trace adjacent cells to spell a word. Drag through letters to form a path.")
        ),

        LevelData(
            id = "simple_02",
            name = "Gathering Words",
            mode = "simple",
            tier = "simple",
            maxMoves = 5,
            scoreMult = 0.5f,
            twoStarTarget = 75,
            threeStarTarget = 100,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 2 words", target = 2, params = mapOf("minLength" to 3))
            ),
            tutorial = TutorialDef("Longer words score more points. Try to find 4+ letter words!")
        ),

        LevelData(
            id = "simple_03",
            name = "Stretch",
            mode = "simple",
            tier = "simple",
            maxMoves = 6,
            scoreMult = 0.6f,
            twoStarTarget = 100,
            threeStarTarget = 155,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form a 4+ letter word", target = 1, params = mapOf("minLength" to 4))
            ),
            tutorial = TutorialDef("Look for common prefixes and suffixes like RE-, UN-, -ING, or -TION to build longer words.")
        ),

        LevelData(
            id = "simple_04",
            name = "Anchor Point",
            mode = "simple",
            tier = "simple",
            maxMoves = 6,
            scoreMult = 0.7f,
            twoStarTarget = 110,
            threeStarTarget = 170,
            layout = LayoutDef(shape = "hex2", anchors = 1),
            objectives = listOf(
                ObjectiveDef(type = "illuminateAnchors", description = "Must use the Anchor cell (each word)", target = 1)
            ),
            tutorial = TutorialDef("Anchor cells (red rim) must be used in your words!")
        ),

        LevelData(
            id = "simple_05",
            name = "Bigger Board",
            mode = "simple",
            tier = "simple",
            maxMoves = 7,
            scoreMult = 0.75f,
            twoStarTarget = 120,
            threeStarTarget = 185,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 2 words of 4+ letters", target = 2, params = mapOf("minLength" to 4))
            ),
            tutorial = TutorialDef("A larger board means more letters and longer words!")
        ),

        LevelData(
            id = "simple_06",
            name = "Wordsmith",
            mode = "simple",
            tier = "simple",
            maxMoves = 8,
            scoreMult = 0.8f,
            twoStarTarget = 135,
            threeStarTarget = 200,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 3 words of 4+ letters", target = 3, params = mapOf("minLength" to 4))
            ),
            tutorial = TutorialDef("Scan the whole board before tracing. The best word isn't always the most obvious one.")
        ),

        LevelData(
            id = "simple_07",
            name = "Twin Anchors",
            mode = "simple",
            tier = "simple",
            maxMoves = 8,
            scoreMult = 0.85f,
            twoStarTarget = 145,
            threeStarTarget = 220,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "illuminateAnchors", description = "Must use both Anchors (use each in a word)", target = 2)
            ),
            tutorial = TutorialDef("With two anchors, every word must pass through at least one. Plan paths that connect them.")
        ),

        LevelData(
            id = "simple_08",
            name = "Deep Boxters",
            mode = "simple",
            tier = "simple",
            maxMoves = 8,
            scoreMult = 0.9f,
            twoStarTarget = 160,
            threeStarTarget = 240,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 2 words of 5+ letters", target = 2, params = mapOf("minLength" to 5))
            ),
            tutorial = TutorialDef("5-letter words score big. Look for paths that zigzag through the center of the board.")
        ),

        LevelData(
            id = "simple_09",
            name = "Under Pressure",
            mode = "simple",
            tier = "simple",
            maxMoves = 8,
            scoreMult = 0.95f,
            twoStarTarget = 175,
            threeStarTarget = 260,
            layout = LayoutDef(shape = "hex3", anchors = 1),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 3 words of 4+ letters", target = 3, params = mapOf("minLength" to 4)),
                ObjectiveDef(type = "illuminateAnchors", description = "Must use the Anchor cell (each word)", target = 1)
            ),
            tutorial = TutorialDef("Every word must use the anchor. Start or end your path at the red-rimmed tile.")
        ),

        LevelData(
            id = "simple_10",
            name = "Sharp Focus",
            mode = "simple",
            tier = "simple",
            maxMoves = 7,
            twoStarTarget = 190,
            threeStarTarget = 280,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 3 words of 5+ letters", target = 3, params = mapOf("minLength" to 5)),
                ObjectiveDef(type = "illuminateAnchors", description = "Must use both Anchors (use each in a word)", target = 2)
            ),
            tutorial = TutorialDef("Don't waste moves on guesses. Invalid words still cost a move!")
        ),

        LevelData(
            id = "simple_11",
            name = "Proving Ground",
            mode = "simple",
            tier = "simple",
            maxMoves = 6,
            twoStarTarget = 200,
            threeStarTarget = 300,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 3 words of 5+ letters", target = 3, params = mapOf("minLength" to 5)),
                ObjectiveDef(type = "illuminateAnchors", description = "Must use both Anchors (use each in a word)", target = 2)
            ),
            tutorial = TutorialDef("Fewer moves means every word counts. Aim for 5+ letters on each move.")
        ),

        LevelData(
            id = "simple_12",
            name = "Final Exam",
            mode = "simple",
            tier = "simple",
            maxMoves = 7,
            twoStarTarget = 210,
            threeStarTarget = 320,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 2 words of 5+ letters", target = 2, params = mapOf("minLength" to 5)),
                ObjectiveDef(type = "formWord", description = "Form 1 word of 6+ letters", target = 1, params = mapOf("minLength" to 6)),
                ObjectiveDef(type = "illuminateAnchors", description = "Must use both Anchors (use each in a word)", target = 2)
            ),
            tutorial = TutorialDef("Need a 6-letter word? Try extending a shorter word with a prefix or suffix.")
        ),

        LevelData(
            id = "simple_13",
            name = "Endurance",
            mode = "simple",
            tier = "simple",
            maxMoves = 6,
            twoStarTarget = 220,
            threeStarTarget = 340,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 4 words of 4+ letters", target = 4, params = mapOf("minLength" to 4)),
                ObjectiveDef(type = "illuminateAnchors", description = "Must use both Anchors (use each in a word)", target = 2)
            ),
            tutorial = TutorialDef("Four words in six moves \u2014 no room for errors. Plan all your paths before you start.")
        ),

        LevelData(
            id = "simple_14",
            name = "Masterclass",
            mode = "simple",
            tier = "simple",
            maxMoves = 6,
            twoStarTarget = 240,
            threeStarTarget = 360,
            layout = LayoutDef(shape = "hex3", anchors = 3),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 4 words of 5+ letters", target = 4, params = mapOf("minLength" to 5)),
                ObjectiveDef(type = "illuminateAnchors", description = "Must use all 3 Anchors (use each in a word)", target = 3)
            ),
            tutorial = TutorialDef("The final challenge. Three anchors, four 5-letter words, six moves. You've got this.")
        ),

        // ── CLEAR MODE (Levels 14-27) ──

        LevelData(
            id = "clear_01",
            name = "Clean Sweep",
            mode = "clear",
            tier = "clear",
            maxMoves = 4,
            scoreMult = 0.4f,
            twoStarTarget = 80,
            threeStarTarget = 110,
            layout = LayoutDef(shape = "hex1"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 7 cells", target = 1)
            ),
            tutorial = TutorialDef("Clear mode! Letters you use get REMOVED. Clear all cells to win!")
        ),

        LevelData(
            id = "clear_02",
            name = "Tidy Up",
            mode = "clear",
            tier = "clear",
            maxMoves = 5,
            scoreMult = 0.5f,
            twoStarTarget = 90,
            threeStarTarget = 125,
            layout = LayoutDef(shape = "hex1"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 7 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 4+ letter word", target = 1, params = mapOf("minLength" to 4), isPrimary = false)
            )
        ),

        LevelData(
            id = "clear_03",
            name = "Growing Pains",
            mode = "clear",
            tier = "clear",
            maxMoves = 10,
            scoreMult = 0.6f,
            twoStarTarget = 150,
            threeStarTarget = 240,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 19 cells", target = 1)
            ),
            tutorial = TutorialDef("Bigger board now \u2014 plan your paths to cover every cell!")
        ),

        LevelData(
            id = "clear_04",
            name = "Shattered Ring",
            mode = "clear",
            tier = "clear",
            maxMoves = 12,
            scoreMult = 0.7f,
            twoStarTarget = 170,
            threeStarTarget = 270,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 19 cells", target = 1)
            ),
            tutorial = TutorialDef("Tip: If only 1 or 2 tiles remain in a cluster with no valid word, they get auto-cleared for you!")
        ),

        LevelData(
            id = "clear_05",
            name = "Wrecking Ball",
            mode = "clear",
            tier = "clear",
            maxMoves = 10,
            scoreMult = 0.75f,
            twoStarTarget = 190,
            threeStarTarget = 300,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 19 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 4+ letter word", target = 1, params = mapOf("minLength" to 4), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: If only 1 or 2 tiles remain in a cluster with no valid word, they get auto-cleared for you!")
        ),

        LevelData(
            id = "clear_06",
            name = "Precision Strike",
            mode = "clear",
            tier = "clear",
            maxMoves = 10,
            scoreMult = 0.8f,
            twoStarTarget = 210,
            threeStarTarget = 330,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 19 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 4+ letter word", target = 1, params = mapOf("minLength" to 4), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: If only 1 or 2 tiles remain in a cluster with no valid word, they get auto-cleared for you!")
        ),

        LevelData(
            id = "clear_07",
            name = "Demolition",
            mode = "clear",
            tier = "clear",
            maxMoves = 16,
            scoreMult = 0.85f,
            twoStarTarget = 240,
            threeStarTarget = 370,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1)
            ),
            tutorial = TutorialDef("Bigger board! Use long words to clear more tiles per move. Small leftover clusters get auto-cleared.")
        ),

        LevelData(
            id = "clear_08",
            name = "Precision",
            mode = "clear",
            tier = "clear",
            maxMoves = 14,
            scoreMult = 0.9f,
            twoStarTarget = 265,
            threeStarTarget = 400,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1)
            ),
            tutorial = TutorialDef("Fewer moves now. Plan your paths to avoid leaving isolated tiles in the middle of the board.")
        ),

        LevelData(
            id = "clear_09",
            name = "Scorched Earth",
            mode = "clear",
            tier = "clear",
            maxMoves = 12,
            scoreMult = 0.95f,
            twoStarTarget = 290,
            threeStarTarget = 440,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 5+ letter word", target = 1, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("Try clearing from the edges inward. A 5+ letter word earns bonus points and clears a big chunk!")
        ),

        LevelData(
            id = "clear_10",
            name = "Total Erasure",
            mode = "clear",
            tier = "clear",
            maxMoves = 10,
            twoStarTarget = 320,
            threeStarTarget = 480,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 2 words of 5+ letters", target = 2, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("Only 10 moves for 37 tiles. Aim for long words that snake across the board to maximize coverage.")
        ),

        LevelData(
            id = "clear_11",
            name = "Methodical",
            mode = "clear",
            tier = "clear",
            maxMoves = 12,
            twoStarTarget = 340,
            threeStarTarget = 500,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 5+ letter word", target = 1, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("Work section by section. Clear one area completely before moving to the next to avoid scattered leftovers.")
        ),

        LevelData(
            id = "clear_12",
            name = "Obliterate",
            mode = "clear",
            tier = "clear",
            maxMoves = 10,
            twoStarTarget = 360,
            threeStarTarget = 540,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 2 words of 5+ letters", target = 2, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("Every move counts. Look for 5+ letter words before settling for shorter ones.")
        ),

        LevelData(
            id = "clear_13",
            name = "Surgical",
            mode = "clear",
            tier = "clear",
            maxMoves = 9,
            twoStarTarget = 380,
            threeStarTarget = 570,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 3 words of 4+ letters", target = 3, params = mapOf("minLength" to 4), isPrimary = false)
            ),
            tutorial = TutorialDef("With just 9 moves, you need an average of 4+ tiles per word. Think big!")
        ),

        LevelData(
            id = "clear_14",
            name = "Annihilation",
            mode = "clear",
            tier = "clear",
            maxMoves = 8,
            twoStarTarget = 400,
            threeStarTarget = 600,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "clearAllCells", description = "Clear all 37 cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 3 words of 5+ letters", target = 3, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("8 moves, 37 tiles. You need 5+ letter words almost every turn. Scan the whole board before each move!")
        ),

        // ── CHAIN MODE (Levels 28-41) ──

        LevelData(
            id = "chain_01",
            name = "First Link",
            mode = "chain",
            tier = "chain",
            maxMoves = 5,
            scoreMult = 0.4f,
            twoStarTarget = 100,
            threeStarTarget = 140,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 3 words (3+ letters each)", target = 3, params = mapOf("minLength" to 3))
            ),
            tutorial = TutorialDef("Welcome to Chain mode! Trace a word, then those tiles get fresh letters. Form 3 words to complete this level.")
        ),

        LevelData(
            id = "chain_02",
            name = "Overlap",
            mode = "chain",
            tier = "chain",
            maxMoves = 6,
            scoreMult = 0.5f,
            twoStarTarget = 120,
            threeStarTarget = 170,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 4 words (3+ letters each) \u2014 try using a blue tile in your next word for bonus points", target = 4, params = mapOf("minLength" to 3))
            ),
            tutorial = TutorialDef("After each word, the tiles you used get new letters and pulse blue. Try using one of those blue tiles in your next word to start a combo!")
        ),

        LevelData(
            id = "chain_03",
            name = "Linked Up",
            mode = "chain",
            tier = "chain",
            maxMoves = 10,
            scoreMult = 0.6f,
            twoStarTarget = 260,
            threeStarTarget = 420,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "achieveCombo", description = "Build a 2x combo \u2014 use one of the blue pulsing tiles in your next word", target = 2)
            ),
            tutorial = TutorialDef("After each word, those tiles get new letters and pulse blue. Use one of those blue tiles in your next word to build a combo!")
        ),

        LevelData(
            id = "chain_04",
            name = "Double Down",
            mode = "chain",
            tier = "chain",
            maxMoves = 10,
            scoreMult = 0.7f,
            twoStarTarget = 300,
            threeStarTarget = 480,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "achieveCombo", description = "Build a 3x combo \u2014 overlap 3 consecutive words through shared tile positions", target = 3)
            ),
            tutorial = TutorialDef("Keep the chain going! Overlap 3 words in a row through the same tile positions to reach 3x. Look for the blue pulsing tiles.")
        ),

        LevelData(
            id = "chain_05",
            name = "Cascade",
            mode = "chain",
            tier = "chain",
            maxMoves = 10,
            scoreMult = 0.75f,
            twoStarTarget = 340,
            threeStarTarget = 540,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 5 words (3+ letters each)", target = 5, params = mapOf("minLength" to 3)),
                ObjectiveDef(type = "achieveCombo", description = "Build a 2x combo \u2014 overlap consecutive words", target = 2, isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: You can play a word anywhere if the blue tiles don't form a useful word \u2014 you'll lose the combo but can start a new chain.")
        ),

        LevelData(
            id = "chain_06",
            name = "Long Links",
            mode = "chain",
            tier = "chain",
            maxMoves = 9,
            scoreMult = 0.8f,
            twoStarTarget = 380,
            threeStarTarget = 600,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "achieveCombo", description = "Build a 3x combo \u2014 chain 3 words through shared tiles", target = 3),
                ObjectiveDef(type = "formWord", description = "Form 2 words with 4+ letters", target = 2, params = mapOf("minLength" to 4))
            ),
            tutorial = TutorialDef("Tip: Longer words refresh more tiles at once, giving you better letters to chain with next.")
        ),

        LevelData(
            id = "chain_07",
            name = "Relentless",
            mode = "chain",
            tier = "chain",
            maxMoves = 9,
            scoreMult = 0.85f,
            twoStarTarget = 420,
            threeStarTarget = 660,
            layout = LayoutDef(shape = "hex3", anchors = 1),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 6 words (3+ letters each)", target = 6, params = mapOf("minLength" to 3)),
                ObjectiveDef(type = "illuminateAnchors", description = "Use the red Anchor tile in a word", target = 1)
            ),
            tutorial = TutorialDef("Anchor tile ahead! At least one of your words must pass through the red-rimmed tile.")
        ),

        LevelData(
            id = "chain_08",
            name = "Momentum",
            mode = "chain",
            tier = "chain",
            maxMoves = 8,
            scoreMult = 0.9f,
            twoStarTarget = 460,
            threeStarTarget = 720,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "achieveCombo", description = "Hit max combo \u2014 chain 4 words through shared tiles", target = 4),
                ObjectiveDef(type = "formWord", description = "Form 2 words with 4+ letters", target = 2, params = mapOf("minLength" to 4), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: To hit max combo (4x), chain 4 words in a row through shared tile positions. Each consecutive overlap increases the multiplier.")
        ),

        LevelData(
            id = "chain_09",
            name = "Overdrive",
            mode = "chain",
            tier = "chain",
            maxMoves = 7,
            scoreMult = 0.95f,
            twoStarTarget = 500,
            threeStarTarget = 780,
            layout = LayoutDef(shape = "hex3", anchors = 1),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 7 words (3+ letters each)", target = 7, params = mapOf("minLength" to 3)),
                ObjectiveDef(type = "achieveCombo", description = "Build a 3x combo \u2014 chain 3 words through shared tiles", target = 3),
                ObjectiveDef(type = "illuminateAnchors", description = "Use the red Anchor tile in a word", target = 1)
            ),
            tutorial = TutorialDef("Tip: With an Anchor and a combo target, try to include the Anchor in your chain path so every word satisfies both goals.")
        ),

        LevelData(
            id = "chain_10",
            name = "Perpetual Motion",
            mode = "chain",
            tier = "chain",
            maxMoves = 7,
            twoStarTarget = 540,
            threeStarTarget = 840,
            layout = LayoutDef(shape = "hex3", anchors = 1),
            objectives = listOf(
                ObjectiveDef(type = "achieveCombo", description = "Hit max combo \u2014 chain 4 words through shared tiles", target = 4),
                ObjectiveDef(type = "formWord", description = "Form 3 words with 4+ letters", target = 3, params = mapOf("minLength" to 4)),
                ObjectiveDef(type = "illuminateAnchors", description = "Use the red Anchor tile in a word", target = 1)
            ),
            tutorial = TutorialDef("Tip: Start your chain near the Anchor so the blue tiles stay close to it. This makes it easier to thread every word through both.")
        ),

        LevelData(
            id = "chain_11",
            name = "Runaway",
            mode = "chain",
            tier = "chain",
            maxMoves = 8,
            twoStarTarget = 560,
            threeStarTarget = 860,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 6 words (3+ letters each)", target = 6, params = mapOf("minLength" to 3)),
                ObjectiveDef(type = "achieveCombo", description = "Build a 3x combo \u2014 chain 3 words through shared tiles", target = 3)
            ),
            tutorial = TutorialDef("Tip: If you break a chain, don't panic. Use the next move to set up a new chain in a different area of the board.")
        ),

        LevelData(
            id = "chain_12",
            name = "Chain Reaction",
            mode = "chain",
            tier = "chain",
            maxMoves = 7,
            twoStarTarget = 580,
            threeStarTarget = 900,
            layout = LayoutDef(shape = "hex3", anchors = 1),
            objectives = listOf(
                ObjectiveDef(type = "achieveCombo", description = "Hit max combo \u2014 chain 4 words through shared tiles", target = 4),
                ObjectiveDef(type = "formWord", description = "Form 3 words with 4+ letters", target = 3, params = mapOf("minLength" to 4)),
                ObjectiveDef(type = "illuminateAnchors", description = "Use the red Anchor tile in a word", target = 1)
            ),
            tutorial = TutorialDef("Tip: With only 7 moves, every word counts. Prioritize keeping your chain alive over finding longer words.")
        ),

        LevelData(
            id = "chain_13",
            name = "Unstoppable",
            mode = "chain",
            tier = "chain",
            maxMoves = 7,
            twoStarTarget = 600,
            threeStarTarget = 940,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 7 words (3+ letters each)", target = 7, params = mapOf("minLength" to 3)),
                ObjectiveDef(type = "achieveCombo", description = "Hit max combo \u2014 chain 4 words through shared tiles", target = 4),
                ObjectiveDef(type = "illuminateAnchors", description = "Use both red Anchor tiles in your words", target = 2)
            ),
            tutorial = TutorialDef("Two Anchors! Your words must pass through both. Try to chain through the area between them.")
        ),

        LevelData(
            id = "chain_14",
            name = "Infinite Loop",
            mode = "chain",
            tier = "chain",
            maxMoves = 6,
            twoStarTarget = 620,
            threeStarTarget = 980,
            layout = LayoutDef(shape = "hex3", anchors = 2),
            objectives = listOf(
                ObjectiveDef(type = "formWord", description = "Form 6 words with 4+ letters", target = 6, params = mapOf("minLength" to 4)),
                ObjectiveDef(type = "achieveCombo", description = "Hit max combo \u2014 chain 4 words through shared tiles", target = 4),
                ObjectiveDef(type = "illuminateAnchors", description = "Use both red Anchor tiles in your words", target = 2)
            ),
            tutorial = TutorialDef("The ultimate chain challenge. 6 moves, 2 Anchors, max combo, all 4+ letter words. Scan the whole board before each move!")
        ),

        // ── ILLUMINATE MODE (Levels 42-55) ──

        LevelData(
            id = "illuminate_01",
            name = "Dawn",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 12,
            twoStarTarget = 200,
            threeStarTarget = 320,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1)
            ),
            tutorial = TutorialDef("Illuminate mode! Cells you use glow permanently. Light up EVERY cell to win! You can re-use already lit tiles in new words.")
        ),

        LevelData(
            id = "illuminate_02",
            name = "Kindling",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 10,
            twoStarTarget = 220,
            threeStarTarget = 350,
            layout = LayoutDef(shape = "hex2"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 4+ letter word", target = 1, params = mapOf("minLength" to 4), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: Longer words light up more tiles per move. You can re-use lit tiles to reach unlit ones!")
        ),

        LevelData(
            id = "illuminate_03",
            name = "Spreading Light",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 12,
            twoStarTarget = 250,
            threeStarTarget = 400,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "illuminatePercent", description = "Illuminate 70% of cells", target = 70)
            ),
            tutorial = TutorialDef("Bigger board! You only need 70% coverage \u2014 focus on dense clusters rather than chasing every edge tile.")
        ),

        LevelData(
            id = "illuminate_04",
            name = "Brightening",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 11,
            scoreMult = 0.7f,
            twoStarTarget = 200,
            threeStarTarget = 310,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "illuminatePercent", description = "Illuminate 85% of cells", target = 85)
            ),
            tutorial = TutorialDef("Tip: 85% coverage needed. Prioritize unlit tiles at the edges \u2014 they're harder to reach later.")
        ),

        LevelData(
            id = "illuminate_05",
            name = "Full Radiance",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 15,
            scoreMult = 0.75f,
            twoStarTarget = 260,
            threeStarTarget = 400,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1)
            ),
            tutorial = TutorialDef("Full coverage required! Plan words that snake through unlit areas. Re-use lit tiles as stepping stones to reach dark corners.")
        ),

        LevelData(
            id = "illuminate_06",
            name = "Guided Light",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 15,
            scoreMult = 0.8f,
            twoStarTarget = 320,
            threeStarTarget = 500,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 5+ letter word", target = 1, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: A 5+ letter word covers more tiles per move. Look for long paths through dark areas.")
        ),

        LevelData(
            id = "illuminate_07",
            name = "Beacon",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 14,
            scoreMult = 0.85f,
            twoStarTarget = 380,
            threeStarTarget = 590,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Use a 6+ letter word", target = 1, params = mapOf("minLength" to 6), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: A 6+ letter word is a big coverage boost. Trace through unlit edges to light them up.")
        ),

        LevelData(
            id = "illuminate_08",
            name = "Burning Bright",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 14,
            scoreMult = 0.9f,
            twoStarTarget = 440,
            threeStarTarget = 680,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 2 words of 5+ letters", target = 2, params = mapOf("minLength" to 5), isPrimary = false)
            ),
            tutorial = TutorialDef("Tip: Start from the edges and work inward. Edge tiles are harder to reach later.")
        ),

        LevelData(
            id = "illuminate_09",
            name = "Solar Flare",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 13,
            scoreMult = 0.95f,
            twoStarTarget = 500,
            threeStarTarget = 780,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 2 words of 6+ letters", target = 2, params = mapOf("minLength" to 6))
            ),
            tutorial = TutorialDef("Tip: Mix long words for coverage with shorter words to mop up isolated tiles.")
        ),

        LevelData(
            id = "illuminate_10",
            name = "Supernova",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 13,
            twoStarTarget = 560,
            threeStarTarget = 860,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 3 words of 5+ letters", target = 3, params = mapOf("minLength" to 5))
            ),
            tutorial = TutorialDef("Moves are getting tight. Aim for long words that zigzag through dark areas. Every tile covered counts.")
        ),

        LevelData(
            id = "illuminate_11",
            name = "Afterglow",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 12,
            twoStarTarget = 600,
            threeStarTarget = 920,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 3 words of 6+ letters", target = 3, params = mapOf("minLength" to 6))
            ),
            tutorial = TutorialDef("Tip: Look for words that bridge lit and unlit areas to maximize coverage per move.")
        ),

        LevelData(
            id = "illuminate_12",
            name = "Blinding",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 12,
            twoStarTarget = 640,
            threeStarTarget = 980,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 4 words of 5+ letters", target = 4, params = mapOf("minLength" to 5))
            ),
            tutorial = TutorialDef("You need long words almost every turn. Scan the whole board before each move.")
        ),

        LevelData(
            id = "illuminate_13",
            name = "White Dwarf",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 11,
            twoStarTarget = 680,
            threeStarTarget = 1040,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 4 words of 6+ letters", target = 4, params = mapOf("minLength" to 6))
            ),
            tutorial = TutorialDef("Tip: Almost every move must be a long word. Don't waste moves on short words.")
        ),

        LevelData(
            id = "illuminate_14",
            name = "Eternal Light",
            mode = "illuminate",
            tier = "illuminate",
            maxMoves = 11,
            twoStarTarget = 720,
            threeStarTarget = 1100,
            layout = LayoutDef(shape = "hex3"),
            objectives = listOf(
                ObjectiveDef(type = "useAllCells", description = "Illuminate all cells", target = 1),
                ObjectiveDef(type = "formWord", description = "Form 5 words of 5+ letters", target = 5, params = mapOf("minLength" to 5))
            ),
            tutorial = TutorialDef("The ultimate illuminate challenge. Every move counts \u2014 plan your path carefully before you trace!")
        )
    )

    /**
     * Returns all levels that belong to the given mode.
     * Each returned LevelData is unchanged; the index in LEVEL_DATA can be
     * recovered via [LEVEL_DATA.indexOf].
     */
    fun getLevelsForMode(mode: String): List<LevelData> {
        return LEVEL_DATA.filter { it.mode == mode }
    }

    /**
     * Returns the LevelData at the given index, wrapping around if the
     * index exceeds the total number of levels.
     */
    fun getLevel(index: Int): LevelData {
        val safeIndex = if (LEVEL_DATA.isEmpty()) 0 else index % LEVEL_DATA.size
        return LEVEL_DATA[safeIndex]
    }

    /**
     * Returns the index of the first level whose mode matches [mode],
     * or 0 if no match is found.
     */
    fun getFirstLevelForMode(mode: String): Int {
        val idx = LEVEL_DATA.indexOfFirst { it.mode == mode }
        return if (idx >= 0) idx else 0
    }

    /**
     * Returns the total number of levels.
     */
    fun getLevelCount(): Int = LEVEL_DATA.size
}
