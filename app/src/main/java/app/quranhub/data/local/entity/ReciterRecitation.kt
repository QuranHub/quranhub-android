package app.quranhub.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Recitation::class,
        parentColumns = ["id"],
        childColumns = ["recitation_id"],
        onDelete = CASCADE,
        onUpdate = CASCADE
    ), ForeignKey(
        entity = Reciter::class,
        parentColumns = ["id"],
        childColumns = ["reciter_id"],
        onDelete = CASCADE,
        onUpdate = CASCADE
    )]
)
data class ReciterRecitation(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "recitation_id") var recitationId: Int,
    @ColumnInfo(name = "reciter_id") var reciterId: String
)