package app.quranhub.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = QuranSubjectCategory::class,
        parentColumns = ["id"],
        childColumns = ["category"]
    )]
)
data class QuranSubject(
    @PrimaryKey var id: Int,
    var name: String,
    var category: Int,
    @ColumnInfo(name = "aya_count") var ayaCount: Int
)