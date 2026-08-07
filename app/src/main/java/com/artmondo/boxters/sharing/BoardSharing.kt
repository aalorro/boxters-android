package com.artmondo.boxters.sharing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.artmondo.boxters.data.model.Board
import com.artmondo.boxters.data.model.HexCoord
import com.artmondo.boxters.data.model.LevelData
import com.artmondo.boxters.domain.hex.HexMath

object BoardSharing {

    fun encodeBoard(board: Board, levelIndex: Int, levelData: LevelData): String {
        val radius = HexMath.shapeToRadius(levelData.layout.shape)
        val spiralCoords = HexMath.hexSpiral(HexCoord(0, 0), radius)

        val letters = StringBuilder()
        val anchorIndices = mutableListOf<Int>()

        for ((i, coord) in spiralCoords.withIndex()) {
            val cell = board.field.getCell(coord.q, coord.r)
            letters.append(cell?.letter ?: 'A')
            if (cell?.isAnchor == true) {
                anchorIndices.add(i)
            }
        }

        val params = mutableListOf("l=$levelIndex", "b=$letters")
        if (anchorIndices.isNotEmpty()) {
            params.add("a=${anchorIndices.joinToString(",")}")
        }

        return "https://www.boxters.com/?${params.joinToString("&")}"
    }

    data class SharedBoardData(
        val levelIndex: Int,
        val letters: String,
        val anchorIndices: List<Int>
    )

    fun decodeBoard(url: String): SharedBoardData? {
        try {
            val uri = android.net.Uri.parse(url)
            val levelIndex = uri.getQueryParameter("l")?.toIntOrNull() ?: return null
            val letters = uri.getQueryParameter("b") ?: return null
            val anchors = uri.getQueryParameter("a")
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?: emptyList()

            return SharedBoardData(levelIndex, letters, anchors)
        } catch (e: Exception) {
            return null
        }
    }

    fun shareBoard(context: Context, url: String) {
        // Copy to clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Boxters Board", url))

        // Also show share sheet
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out my Boxters board! $url")
        }
        context.startActivity(Intent.createChooser(intent, "Share Board"))
    }
}
