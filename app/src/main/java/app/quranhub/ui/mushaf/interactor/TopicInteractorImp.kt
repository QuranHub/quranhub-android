package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.SearchModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class TopicInteractorImp(
    context: Context,
    private val listener: TopicInteractor.TopicListener
) : TopicInteractor {

    private val context: Context = context
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)

    @SuppressLint("CheckResult")
    override fun getAyas(categoryId: Int) {
        val topicAyas = mushafDatabase.ayaDao.getCategoryAyas(categoryId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        val hizbQuarterData = mushafDatabase.hizbQuarterDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        Single.zip(topicAyas, hizbQuarterData, BiFunction { searchModels: List<SearchModel>, hizbQuarters: List<app.quranhub.data.local.entity.HizbQuarter> ->
            val ayaHezbQuarterIndex = IntArray(6237)
            for (hizbQuarter in hizbQuarters) {
                for (i in hizbQuarter.ayaFrom..hizbQuarter.ayaTo) {
                    ayaHezbQuarterIndex[i] = hizbQuarter.id
                }
            }
            for (i in searchModels.indices) {
                val hezbQuarterData = ayaHezbQuarterIndex[searchModels[i].id]
                val hezb = (hezbQuarterData - 1) / 4 % 2 + 1
                val quarter = (hezbQuarterData - 1) % 4 + 1
                searchModels[i].hezb = hezb
                searchModels[i].quarter = quarter
            }
            searchModels
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                listener.onGetTopics(result)
            }, {
                Log.d("Error", "Error")
            })
    }
}
