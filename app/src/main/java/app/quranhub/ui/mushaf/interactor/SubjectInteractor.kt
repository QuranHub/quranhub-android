package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.TopicModel

interface SubjectInteractor {
    fun getSubjects(subjects: List<String?>?, subjectsCategory: List<String?>?)

    interface SubjectListener {
        fun onGetSubjects(topicModels: List<TopicModel>)
    }
}
