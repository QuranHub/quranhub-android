package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.TranslationBook
import kotlinx.coroutines.flow.Flow

interface TranslationsInteractor {

    fun getBooksForLanguage(langCode: String?): Flow<List<TranslationBook>>

    suspend fun saveBook(book: TranslationBook)

    suspend fun deleteBook(book: TranslationBook)
}
