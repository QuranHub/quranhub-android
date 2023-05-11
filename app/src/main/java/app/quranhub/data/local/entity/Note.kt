package app.quranhub.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Note(
    @PrimaryKey var ayaId: Int,
    var noteType: Int,
    var noteText: String?,
    var noteRecorderPath: String?,
) : Parcelable