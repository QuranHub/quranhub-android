package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuranSubjectCategory(
    @PrimaryKey var id: Int,
    var name: String
)