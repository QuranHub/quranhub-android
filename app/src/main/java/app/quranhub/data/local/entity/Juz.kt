package app.quranhub.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Sura::class,
        parentColumns = ["id"],
        childColumns = ["sura"]
    )]
)
data class Juz(
    @PrimaryKey var id: Int,
    var sura: Int,
    @ColumnInfo(name = "sura_aya") var suraAya: Int
)