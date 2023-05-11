package app.quranhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.quranhub.data.remote.model.BookContent
import app.quranhub.ui.mushaf.adapter.BookAdapter

@Entity
data class Book(
    @PrimaryKey
    val id: Int = 0,
    val downloadStatus: Int = 0,
    val downloadId: Long = 0
) {

    constructor(content: BookContent) : this(
        content.id,
        BookAdapter.TRANSLATION_NOT_DOWNLOADED,
        -1
    )
}