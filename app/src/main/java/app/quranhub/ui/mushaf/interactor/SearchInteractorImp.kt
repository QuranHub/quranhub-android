package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.entity.HizbQuarter
import app.quranhub.ui.mushaf.model.SearchModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SearchInteractorImp(
    context: Context,
    private val listener: SearchInteractor.TopicListener
) : SearchInteractor {

    private val context: Context = context
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)

    override fun searchAya(inputQuery: String?) {
        val searchModels = mushafDatabase.ayaDao
            .getSimpleSearchResult(inputQuery)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        getData(searchModels)
    }

    @SuppressLint("CheckResult")
    private fun getData(searchModels: Single<List<SearchModel>>) {
        val hizbQuarterData = mushafDatabase.hizbQuarterDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        Single.zip(searchModels, hizbQuarterData, io.reactivex.functions.BiFunction { searchModels1: List<SearchModel>, hizbQuarters: List<HizbQuarter> ->
            val ayaHezbQuarterIndex = IntArray(6237)
            for (hizbQuarter in hizbQuarters) {
                for (i in hizbQuarter.ayaFrom..hizbQuarter.ayaTo) {
                    ayaHezbQuarterIndex[i] = hizbQuarter.id
                }
            }
            for (i in searchModels1.indices) {
                val hezbQuarterData = ayaHezbQuarterIndex[searchModels1[i].id]
                val hezb = (hezbQuarterData - 1) / 4 % 2 + 1
                val quarter = (hezbQuarterData - 1) % 4 + 1
                searchModels1[i].hezb = hezb
                searchModels1[i].quarter = quarter
            }
            searchModels1 as List<SearchModel>
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                listener.onGetTopics(result)
            }, {
                Log.d("Error", "Error")
            })
    }

    override fun searchAyaInGuz(inputQuery: String?, guzNumber: Int) {
        val searchModels = mushafDatabase.ayaDao
            .getJuzSearchResult(inputQuery, guzNumber)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        getData(searchModels)
    }

    override fun searchAyaInSura(inputQuery: String?, suraNumber: Int) {
        val searchModels = mushafDatabase.ayaDao
            .getSuraSearchResult(inputQuery, suraNumber)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        getData(searchModels)
    }

    override fun getSurasInChapter(chapter: Int): LiveData<List<Int>> {
        @Suppress("UNCHECKED_CAST")
        return mushafDatabase.ayaDao.getSurasInChapter(chapter) as LiveData<List<Int>>
    }

    override fun searchWithSuraAndJuz(inputSearch: String?, selectedSura: Int, selectedJuz: Int) {
        val searchModels = mushafDatabase.ayaDao
            .getSuraJuzSearchResult(inputSearch, selectedSura, selectedJuz)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        getData(searchModels)
    }

    override fun searchWithSuraAndJuzAndHizb(
        inputSearch: String?,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int
    ) {
        var startHezbInterval = (selectedJuz - 1) * 8 + 1
        if (selectedHezb == 2) {
            startHezbInterval += 4
        }
        val endHezbInterval = startHezbInterval + 3
        val searchModels: Single<List<SearchModel>>
        if (selectedSura == 0) {
            searchModels = mushafDatabase.ayaDao
                .getJuzHezbSearchResult(inputSearch, selectedJuz, startHezbInterval, endHezbInterval)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            searchModels = mushafDatabase.ayaDao
                .getSuraJuzHezbSearchResult(inputSearch, selectedSura, selectedJuz, startHezbInterval, endHezbInterval)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
        getData(searchModels)
    }

    override fun searchWithSuraAndJuzAndHizbQuarter(
        inputSearch: String?,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int,
        selectedQuarter: Int
    ) {
        var startHezbInterval = (selectedJuz - 1) * 8 + 1 + (selectedQuarter - 1)
        if (selectedHezb == 2) {
            startHezbInterval += 4
        }
        val searchModels: Single<List<SearchModel>>
        if (selectedSura == 0) {
            searchModels = mushafDatabase.ayaDao
                .getJuzHezbSearchResult(inputSearch, selectedJuz, startHezbInterval, startHezbInterval)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            searchModels = mushafDatabase.ayaDao
                .getSuraJuzHezbSearchResult(inputSearch, selectedSura, selectedJuz, startHezbInterval, startHezbInterval)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
        getData(searchModels)
    }
}
