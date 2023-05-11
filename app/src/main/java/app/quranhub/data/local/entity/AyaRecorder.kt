package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AyaRecorder(
    @PrimaryKey
    var ayaId: Int = 0,
    var recitation: Int = 0,
    var recorderPath: String? = null
)