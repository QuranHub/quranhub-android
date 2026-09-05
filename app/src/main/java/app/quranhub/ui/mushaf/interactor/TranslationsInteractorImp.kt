package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.TranslationBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TranslationsInteractorImp(context: Context) : TranslationsInteractor {

    private val userDatabase: UserDatabase = UserDatabase.getInstance(context.applicationContext)

    override fun getBooksForLanguage(langCode: String?): Flow<List<TranslationBook>> {
        return userDatabase.translationBookDao.getByLanguage(langCode)
    }

    override suspend fun saveBook(book: TranslationBook) {
        withContext(Dispatchers.IO) {
            userDatabase.translationBookDao.insert(book)
        }
    }

    override suspend fun deleteBook(book: TranslationBook) {
        withContext(Dispatchers.IO) {
            userDatabase.translationBookDao.delete(book)
        }
    }
}
