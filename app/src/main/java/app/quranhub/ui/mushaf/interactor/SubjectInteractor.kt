package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.TopicModel

interface SubjectInteractor {
    suspend fun getSubjects(subjects: List<String>, subjectsCategory: List<String>): List<TopicModel>
}
