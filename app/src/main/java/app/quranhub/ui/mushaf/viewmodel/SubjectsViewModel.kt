package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import app.quranhub.ui.mushaf.interactor.SubjectInteractor
import app.quranhub.ui.mushaf.interactor.SubjectInteractor.SubjectListener
import app.quranhub.ui.mushaf.interactor.SubjectInteractorImp
import app.quranhub.ui.mushaf.model.TopicModel

class SubjectsViewModel(application: Application) : AndroidViewModel(application), SubjectListener {

    val subjectsLiveData: MediatorLiveData<List<TopicModel>> = MediatorLiveData()
    private val interactor: SubjectInteractor = SubjectInteractorImp(application, this)

    fun getSubjects(subjects: List<String?>?, subjectsCategory: List<String?>?) {
        interactor.getSubjects(subjects, subjectsCategory)
    }

    override fun onGetSubjects(topicModels: List<TopicModel>) {
        subjectsLiveData.value = topicModels
    }
}