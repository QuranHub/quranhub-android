package app.quranhub.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    foreignKeys = [ForeignKey(
        entity = Sura::class,
        parentColumns = ["id"],
        childColumns = ["sura"]
    ), ForeignKey(
        entity = Juz::class,
        parentColumns = ["id"],
        childColumns = ["juz"]
    )]
)
@Parcelize
data class Aya(
    @PrimaryKey
    var id: Int,
    var sura: Int, // sura number in Quran
    @ColumnInfo(name = "sura_aya")
    var suraAya: Int, // num of this aya in its sura
    var text: String,
    @ColumnInfo(name = "pure_text")
    var pureText: String,
    var page: Int,
    var amount: Double,
    var juz: Int,
    var x: Int,
    var y: Int,
    var xw: Int,
    var yw: Int,
    var tafseer: String
) : Parcelable