package app.quranhub.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = BookmarkType::class,
        parentColumns = ["typeId"],
        childColumns = ["bookmarkTypeId"]
    )]
)
data class AyaBookmark(
    @PrimaryKey var bookmarkId: Int,
    var bookmarkTypeId: Int,
    @Embedded var aya: Aya
)