package app.quranhub.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Aya::class,
        parentColumns = ["id"],
        childColumns = ["aya_from"]
    ), ForeignKey(entity = Aya::class, parentColumns = ["id"], childColumns = ["aya_to"])]
)
data class HizbQuarter(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "aya_from") var ayaFrom: Int,
    @ColumnInfo(
        name = "aya_to"
    ) var ayaTo: Int
)