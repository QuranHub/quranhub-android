package app.quranhub.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TopicCategory(
    var categoryName: String?,
    var ayaCount: Int,
    var categoryId: Int
) : Parcelable