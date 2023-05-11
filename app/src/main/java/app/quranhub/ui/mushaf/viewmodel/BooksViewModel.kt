package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import app.quranhub.data.local.entity.Book
import app.quranhub.data.remote.model.BookContent
import app.quranhub.ui.mushaf.interactor.BooksInteractor
import app.quranhub.ui.mushaf.interactor.BooksInteractor.TranslationsListener
import app.quranhub.ui.mushaf.interactor.BooksInteractorImp

class BooksViewModel(application: Application) : AndroidViewModel(application),
    TranslationsListener {

    private val booksInteractor: BooksInteractor
    private val result: LiveData<List<Book>>
    val localTranslationsLiveData: MediatorLiveData<List<Book>?>
    val remoteTranslationsLiveData: MediatorLiveData<List<BookContent>>

    init {
        booksInteractor = BooksInteractorImp(application.applicationContext, this)
        localTranslationsLiveData = MediatorLiveData()
        remoteTranslationsLiveData = MediatorLiveData()
        booksInteractor.getAllTranslations()
        result = booksInteractor.locallyTranslations
        localTranslationsLiveData.addSource(result) { translationModels: List<Book>? ->
            localTranslationsLiveData.setValue(
                translationModels
            )
        }
    }

    override fun onError() {
        localTranslationsLiveData.value = null
    }

    override fun onGetAllTranslation(contents: List<BookContent>) {
        remoteTranslationsLiveData.value = contents
    }

    fun updateTranslationType(id: Int, type: Int, downloadId: Long) {
        booksInteractor.updateTranslationDownload(id, type, downloadId)
    }

    fun updateFinishedDownload(downloadId: Long, type: Int) {
        booksInteractor.updateFinishedDownload(downloadId, type)
    }
}