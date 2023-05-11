package app.quranhub.ui.mushaf.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SuraVersesNumber(
    var id: Int = 0,
    var ayas: Int = 0
) : Parcelable