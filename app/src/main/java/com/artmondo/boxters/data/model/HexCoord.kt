package com.artmondo.boxters.data.model

data class HexCoord(val q: Int, val r: Int) {
    val key: String get() = "$q,$r"

    companion object {
        fun fromKey(key: String): HexCoord {
            val (q, r) = key.split(",").map { it.toInt() }
            return HexCoord(q, r)
        }
    }
}
