package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata for a downloaded translation/tafseer book.
 */
@Entity
data class TranslationBook(
    @PrimaryKey var id: String,
    var name: String,
    var author: String,
    var language: String,
    var fileDownloadPath: String,
    var databaseName: String,
    var downloadStatus: Int,
    var downloadLevelPercentage: Int
)