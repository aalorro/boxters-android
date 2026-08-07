package com.artmondo.boxters.ui.game

sealed class GameEvent {
    data class PlayTone(val letter: Char) : GameEvent()
    data class PlayWordChord(val word: String) : GameEvent()
    data object PlayError : GameEvent()
    data object PlayVictoryFanfare : GameEvent()
    data object PlayUltimateVictory : GameEvent()
    data object PlayFail : GameEvent()
    data class ShowFeedback(val message: String) : GameEvent()
    data class CopyToClipboard(val text: String) : GameEvent()
}
