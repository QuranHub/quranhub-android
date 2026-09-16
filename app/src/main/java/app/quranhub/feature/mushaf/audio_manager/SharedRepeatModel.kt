package app.quranhub.feature.mushaf.audio_manager

import app.quranhub.feature.mushaf.model.RepeatModel

object SharedRepeatModel {

    @JvmStatic
    var repeatModel: RepeatModel? = null

    @JvmField
    var isRepeatModelChanged = false
}