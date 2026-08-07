package com.artmondo.boxters.data.model

enum class GameMode(val id: String, val displayName: String) {
    SIMPLE("simple", "Simple"),
    CLEAR("clear", "Clear"),
    CHAIN("chain", "Chain"),
    ILLUMINATE("illuminate", "Illuminate");

    companion object {
        fun fromId(id: String): GameMode = entries.first { it.id == id }
    }
}
