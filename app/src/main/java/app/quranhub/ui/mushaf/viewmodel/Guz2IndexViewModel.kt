package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.quranhub.ui.mushaf.interactor.Guz2IndexInteractor
import app.quranhub.ui.mushaf.interactor.Guz2IndexInteractorImp
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel

class Guz2IndexViewModel(application: Application) : AndroidViewModel(application) {

    private val guz2IndexInteractor: Guz2IndexInteractor
    private var _hizbQuarterDataModelsLiveData: LiveData<List<HizbQuarterDataModel>>? = null
    private val indexItemClickEvent = MutableLiveData<IndexItemClickEvent>()

    init {
        guz2IndexInteractor = Guz2IndexInteractorImp(application)
    }

    fun getHizbQuarterDataModelsLiveData(): LiveData<List<HizbQuarterDataModel>> {
        if (_hizbQuarterDataModelsLiveData == null) {
            _hizbQuarterDataModelsLiveData = guz2IndexInteractor.allHizbQuarterDataModel
        }
        return _hizbQuarterDataModelsLiveData!!
    }

    fun indexItemClickEvent(): LiveData<IndexItemClickEvent> {
        return indexItemClickEvent
    }

    fun notifyIndexItemClick(clickedItemIndex: Int) {
        val (_, _, _, startPage) = _hizbQuarterDataModelsLiveData!!.value!![clickedItemIndex]
        indexItemClickEvent.value = IndexItemClickEvent(startPage)
    }

    class IndexItemClickEvent(var page: Int)
}