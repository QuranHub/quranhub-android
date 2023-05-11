package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import app.quranhub.ui.mushaf.interactor.TopicInteractor
import app.quranhub.ui.mushaf.interactor.TopicInteractorImp
import app.quranhub.ui.mushaf.model.SearchModel

class TopicViewModel(application: Application) : AndroidViewModel(application),
    TopicInteractor.TopicListener {

    val ayahs: MediatorLiveData<List<SearchModel>> = MediatorLiveData()
    private val interactor: TopicInteractor = TopicInteractorImp(application, this)

    fun getAyas(categoryId: Int) {
        interactor.getAyas(categoryId)
    }

    override fun onGetTopics(searchModels: List<SearchModel>) {
        ayahs.value = searchModels
    }
}