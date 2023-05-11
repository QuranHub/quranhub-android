package app.quranhub.ui.mushaf.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RepeatModel(
    var fromSura: Int = 0,
    var fromAya: Int = 0,
    var fromAyaId: Int = 0,
    var toSura: Int = 0,
    var toAya: Int = 0,
    var toAyaId: Int = 0,
    var groupRepeatNum: Int = 0,
    var ayaRepeatNum: Int = 0,
    var delayTime: Int = 0
) : Parcelable