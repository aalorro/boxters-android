package com.artmondo.boxters.data.model

class HexCell(
    val q: Int,
    val r: Int,
    var letter: Char? = null,
    var cellType: CellType = CellType.PLAIN,
    var isActive: Boolean = true,
    var isCleared: Boolean = false,
    var isIlluminated: Boolean = false,
    var energy: Float = 0f,
    var usedInMove: Boolean = false,
    // Visual state
    var glowIntensity: Float = 0f,
    var shakeOffsetX: Float = 0f,
    var shakeOffsetY: Float = 0f,
    var scale: Float = 1f
) {
    val key: String get() = "$q,$r"
    val isAnchor: Boolean get() = cellType == CellType.ANCHOR
    val coord: HexCoord get() = HexCoord(q, r)
}
