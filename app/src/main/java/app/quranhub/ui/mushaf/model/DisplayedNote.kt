package app.quranhub.ui.mushaf.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DisplayedNote(
    var ayaId: Int,
    var noteType: Int,
    var noteText: String?,
    var noteRecorderPath: String?,
    var sura: Int,
    var suraAya: Int,
    var pureText: String?,
    var text: String?,
    var page: Int,
) : Parcelable