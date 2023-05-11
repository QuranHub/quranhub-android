package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sura(
    @PrimaryKey var id: Int,
    var name: String,
    var tname: String,
    var ename: String,
    var type: String,
    var order: Int,
    var ayas: Int
)