package com.artmondo.boxters.data.model

data class LevelData(
    val id: String,
    val name: String,
    val mode: String,
    val tier: String,
    val maxMoves: Int,
    val scoreMult: Float = 1.0f,
    val twoStarTarget: Int,
    val threeStarTarget: Int,
    val layout: LayoutDef,
    val objectives: List<ObjectiveDef>,
    val tutorial: TutorialDef? = null
)

data class LayoutDef(
    val shape: String,
    val anchors: Int = 0,
    val wordCount: Int = 3,
    val minWordLen: Int = 3,
    val maxWordLen: Int = 6
)

data class ObjectiveDef(
    val type: String,
    val description: String,
    val target: Int,
    val params: Map<String, Int> = emptyMap(),
    val isPrimary: Boolean = true
)

data class TutorialDef(
    val message: String
)
