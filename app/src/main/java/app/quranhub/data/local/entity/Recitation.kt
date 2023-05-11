package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recitation(
    @PrimaryKey
    var id: Int = 0,
    var name: String = ""
)