package app.quranhub.feature.mushaf.interactor

import app.quranhub.feature.mushaf.model.TopicModel

interface SubjectInteractor {
    suspend fun getSubjects(subjects: List<String>, subjectsCategory: List<String>): List<TopicModel>
}
