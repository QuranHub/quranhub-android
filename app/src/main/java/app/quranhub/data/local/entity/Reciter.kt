package app.quranhub.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reciter(
    @PrimaryKey
    var id: String,
    var name: String,
    var nationality: String,
    @ColumnInfo(name = "audio_base_url")
    var audioBaseUrl: String
)