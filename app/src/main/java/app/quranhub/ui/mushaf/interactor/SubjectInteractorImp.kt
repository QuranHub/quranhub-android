package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.entity.QuranSubject
import app.quranhub.ui.mushaf.model.TopicCategory
import app.quranhub.ui.mushaf.model.TopicModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class SubjectInteractorImp(context: Context) : SubjectInteractor {

    private val mushafDatabase: MushafDatabase =
        MushafDatabase.getInstance(context.applicationContext)

    override suspend fun getSubjects(
        subjects: List<String>,
        subjectsCategory: List<String>
    ): List<TopicModel> = coroutineScope {
        val quranSubjects = async { mushafDatabase.quranSubjectDao.getAll() }
        buildTopicModels(quranSubjects.await(), subjects, subjectsCategory)
    }

    private suspend fun buildTopicModels(
        quranSubjects: List<QuranSubject>,
        subjects: List<String>,
        subjectsCategory: List<String>
    ): List<TopicModel> = withContext(Dispatchers.Default) {
        val results = mutableListOf<TopicModel>()
        var topicCategories = mutableListOf<TopicCategory>()
        var topicIndex = 0
        for (i in quranSubjects.indices) {
            if (i > 0 && quranSubjects[i].category != quranSubjects[i - 1].category) {
                if (topicIndex < subjectsCategory.size) {
                    results.add(TopicModel(subjectsCategory[topicIndex], topicCategories))
                }
                topicIndex++
                topicCategories = mutableListOf()
            }
            topicCategories.add(
                TopicCategory(
                    subjects.getOrNull(i),
                    quranSubjects[i].ayaCount,
                    quranSubjects[i].id
                )
            )
        }
        if (topicIndex < subjectsCategory.size) {
            results.add(TopicModel(subjectsCategory[topicIndex], topicCategories))
        }
        results
    }
}
