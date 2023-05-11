package app.quranhub.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class BookmarkType(
    @PrimaryKey
    var typeId: Int,
    var bookmarkTypeName: String?,
    var colorIndex: Int = 0
) : Parcelable
