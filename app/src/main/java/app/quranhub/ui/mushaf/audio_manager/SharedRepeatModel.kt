package app.quranhub.ui.mushaf.audio_manager

import app.quranhub.ui.mushaf.model.RepeatModel

object SharedRepeatModel {

    @JvmStatic
    var repeatModel: RepeatModel? = null

    @JvmField
    var isRepeatModelChanged = false
}