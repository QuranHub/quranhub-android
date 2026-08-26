package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.TopicCategory
import app.quranhub.ui.mushaf.model.TopicModel
import app.quranhub.data.local.entity.QuranSubject
import app.quranhub.data.local.entity.QuranSubjectCategory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class SubjectInteractorImp(
    context: Context,
    private val listener: SubjectInteractor.SubjectListener
) : SubjectInteractor {

    private val context: Context = context
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)

    @SuppressLint("CheckResult")
    override fun getSubjects(subjects: List<String?>?, subjectsCategory: List<String?>?) {
        val quranSubjects = mushafDatabase.quranSubjectDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        val quranSubjectsCategory = mushafDatabase.quranSubjectCategoryDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        Single.zip(quranSubjects, quranSubjectsCategory, BiFunction { quranSubjects1: List<QuranSubject>, quranSubjectCategories: List<QuranSubjectCategory> ->
            val results = mutableListOf<TopicModel>()
            var topicCategories = mutableListOf<TopicCategory>()
            var topicIndex = 0
            for (i in quranSubjects1.indices) {
                if (i > 0 && quranSubjects1[i].category != quranSubjects1[i - 1].category) {
                    if (subjectsCategory != null && topicIndex < subjectsCategory.size) {
                        results.add(TopicModel(subjectsCategory[topicIndex] ?: "", topicCategories))
                    }
                    topicIndex++
                    topicCategories = mutableListOf()
                }
                if (subjects != null && topicIndex <= subjects.size) {
                    val name = if (topicIndex - 1 in subjects.indices) subjects[topicIndex - 1] else null
                    topicCategories.add(TopicCategory(name, quranSubjects1[i].ayaCount, quranSubjects1[i].id))
                }
            }
            if (subjectsCategory != null && topicIndex < subjectsCategory.size) {
                results.add(TopicModel(subjectsCategory[topicIndex] ?: "", topicCategories))
            }
            results
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                listener.onGetSubjects(result)
            }, {
                Log.d("Error", "Error")
            })
    }
}
