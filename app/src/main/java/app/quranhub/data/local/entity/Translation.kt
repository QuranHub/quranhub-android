package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation")
data class Translation(
    @PrimaryKey
    var index: Int = 0,
    var sura: Int,
    var aya: Int,
    var text: String
)