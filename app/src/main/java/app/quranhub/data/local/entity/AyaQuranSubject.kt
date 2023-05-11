package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Aya::class,
        parentColumns = ["id"],
        childColumns = ["aya"]
    ), ForeignKey(entity = QuranSubject::class, parentColumns = ["id"], childColumns = ["subject"])]
)
data class AyaQuranSubject(
    @PrimaryKey var id: Int,
    var subject: Int, var aya: Int
)