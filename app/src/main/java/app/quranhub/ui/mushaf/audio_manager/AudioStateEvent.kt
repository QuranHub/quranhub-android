package app.quranhub.ui.mushaf.audio_manager

class AudioStateEvent(var audioState: Int) {

    interface State {
        companion object {
            const val PLAYING = 0
            const val PAUSED = 1
            const val RESUME = 2
            const val STOP = 3
            const val COMPLETED = 4
            const val PLAY_NEXT = 5
            const val PLAY_PREV = 6
            const val NOT_DOWNLOADED = 7
            const val GROUP_REPEAT_COMPLETED = 8
        }
    }
}