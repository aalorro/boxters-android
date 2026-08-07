package com.artmondo.boxters.domain.hex

import androidx.compose.ui.geometry.Offset
import com.artmondo.boxters.data.model.HexCoord
import kotlin.math.*

object HexMath {
    val SQRT3 = sqrt(3.0).toFloat()

    val DIRECTIONS = listOf(
        HexCoord(1, 0),    // E
        HexCoord(1, -1),   // NE
        HexCoord(0, -1),   // NW
        HexCoord(-1, 0),   // W
        HexCoord(-1, 1),   // SW
        HexCoord(0, 1)     // SE
    )

    fun axialToPixel(q: Int, r: Int, size: Float): Offset {
        val x = size * (SQRT3 * q + SQRT3 / 2f * r)
        val y = size * (3f / 2f * r)
        return Offset(x, y)
    }

    fun pixelToAxial(x: Float, y: Float, size: Float): HexCoord {
        val q = (SQRT3 / 3f * x - 1f / 3f * y) / size
        val r = (2f / 3f * y) / size
        return axialRound(q, r)
    }

    fun axialRound(q: Float, r: Float): HexCoord {
        val s = -q - r
        var rq = round(q)
        var rr = round(r)
        val rs = round(s)

        val dq = abs(rq - q)
        val dr = abs(rr - r)
        val ds = abs(rs - s)

        if (dq > dr && dq > ds) {
            rq = -rr - rs
        } else if (dr > ds) {
            rr = -rq - rs
        }
        return HexCoord(rq.toInt(), rr.toInt())
    }

    fun getNeighbors(q: Int, r: Int): List<HexCoord> {
        return DIRECTIONS.map { HexCoord(q + it.q, r + it.r) }
    }

    fun areAdjacent(a: HexCoord, b: HexCoord): Boolean {
        val dq = b.q - a.q
        val dr = b.r - a.r
        return DIRECTIONS.any { it.q == dq && it.r == dr }
    }

    fun hexDistance(a: HexCoord, b: HexCoord): Int {
        return (abs(a.q - b.q) + abs(a.q + a.r - b.q - b.r) + abs(a.r - b.r)) / 2
    }

    fun hexRing(center: HexCoord, radius: Int): List<HexCoord> {
        if (radius == 0) return listOf(center)
        val results = mutableListOf<HexCoord>()
        var hex = HexCoord(center.q - radius, center.r + radius) // Start SW
        for (i in 0 until 6) {
            for (j in 0 until radius) {
                results.add(hex)
                hex = HexCoord(hex.q + DIRECTIONS[i].q, hex.r + DIRECTIONS[i].r)
            }
        }
        return results
    }

    fun hexSpiral(center: HexCoord, radius: Int): List<HexCoord> {
        val results = mutableListOf<HexCoord>()
        for (r in 0..radius) {
            results.addAll(hexRing(center, r))
        }
        return results
    }

    fun hexCorners(cx: Float, cy: Float, size: Float): List<Offset> {
        return (0 until 6).map { i ->
            val angle = Math.toRadians((60.0 * i - 30.0))
            Offset(
                cx + size * cos(angle).toFloat(),
                cy + size * sin(angle).toFloat()
            )
        }
    }

    fun calculateHexSize(cellCount: Int, screenWidth: Float, screenHeight: Float): Float {
        val radius = ceil(sqrt(cellCount / 3.0)).toInt()
        val boardHeight = screenHeight * 0.55f
        val boardWidth = screenWidth * 0.9f
        val heightBasedSize = boardHeight / ((2 * radius + 1) * 1.75f)
        val widthBasedSize = boardWidth / ((2 * radius + 1) * SQRT3)
        return min(heightBasedSize, widthBasedSize).coerceIn(
            com.artmondo.boxters.util.HexGeometry.MIN_SIZE_DP,
            com.artmondo.boxters.util.HexGeometry.MAX_SIZE_DP
        )
    }

    fun shapeToRadius(shape: String): Int = when (shape) {
        "hex1" -> 1
        "hex2" -> 2
        "hex3" -> 3
        else -> 2
    }

    fun cellCountForRadius(radius: Int): Int = 3 * radius * radius + 3 * radius + 1
}
