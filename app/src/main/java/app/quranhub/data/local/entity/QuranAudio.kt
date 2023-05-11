package app.quranhub.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = ReciterRecitation::class,
        parentColumns = ["id"],
        childColumns = ["sheikh_recitation_id"],
        onDelete = CASCADE,
        onUpdate = CASCADE
    )]
)
data class QuranAudio(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var page: Int,
    var sura: Int,
    var aya: Int,
    @ColumnInfo(name = "aya_id") var ayaId: Int,
    @ColumnInfo(name = "file_path") var filePath: String,
    @ColumnInfo(name = "sheikh_recitation_id") var sheikhRecitationId: Int
)