package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import app.quranhub.data.local.entity.Translation
import app.quranhub.ui.mushaf.interactor.TafseerInteractor
import app.quranhub.ui.mushaf.interactor.TafseerInteractorImp
import app.quranhub.ui.mushaf.model.TafseerModel

class TafseerViewModel(application: Application) : AndroidViewModel(application) {

    private val interactor: TafseerInteractor = TafseerInteractorImp(application)
    val tafseers: MediatorLiveData<List<TafseerModel>> = MediatorLiveData()
    val bookTafseers: MediatorLiveData<List<Translation>> = MediatorLiveData()
    val ayahs: MediatorLiveData<List<TafseerModel>> = MediatorLiveData()
    private var ayasLiveData: LiveData<List<TafseerModel>>? = null
    private var bookTafseersLiveData: LiveData<List<Translation>>? = null
    private var tafseerLiveData: LiveData<List<TafseerModel>>? = null

    fun getSuraTafseers(suraNumber: Int) {
        tafseerLiveData = interactor.getSuraTafseers(suraNumber)
        tafseers.addSource(tafseerLiveData!!) { tafseerModels: List<TafseerModel> ->
            tafseers.value = tafseerModels
            tafseers.removeSource(tafseerLiveData!!)
        }
    }

    fun getSuraTafseers(bookDbName: String?, suraNumber: Int) {
        interactor.initTranslationDB(bookDbName)
        bookTafseersLiveData = interactor.getSuraBookTafseers(suraNumber)
        bookTafseers.addSource(bookTafseersLiveData!!) { tafseerModels: List<Translation> ->
            bookTafseers.value = tafseerModels
            bookTafseers.removeSource(bookTafseersLiveData!!)
        }
    }

    fun getSuraAyahs(suraNumber: Int) {
        ayasLiveData = interactor.getSuraTafseers(suraNumber)
        ayahs.addSource(ayasLiveData!!) { tafseerModels: List<TafseerModel> ->
            ayahs.value = tafseerModels
            ayahs.removeSource(ayasLiveData!!)
        }
    }
}