package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.quranhub.R
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Sura
import app.quranhub.ui.mushaf.model.DisplayableBookmark
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class BookmarksInteractorImp(context: Context) : BookmarksInteractor {

    private val userDatabase: UserDatabase = UserDatabase.getInstance(context.applicationContext)
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)
    private val context: Context = context

    override fun getSura(suraId: Int): Sura {
        return mushafDatabase.suraDao.findById(suraId)!!
    }

    override val allBookmarks: LiveData<List<DisplayableBookmark>>
        @SuppressLint("CheckResult")
        get() {
        val listLiveData = MutableLiveData<List<DisplayableBookmark>>()
        val bookmarks: Single<List<AyaBookmark>> = userDatabase.bookmarkDao.getAllBookmarks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        val bookmarkTypes: Single<List<BookmarkType>> = userDatabase.bookmarkDao.getBookmarkTypes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        Single.zip(bookmarks, bookmarkTypes, BiFunction { ayaBookmarks: List<AyaBookmark>, types: List<BookmarkType> ->
            val displayableBookmarks = mutableListOf<DisplayableBookmark>()
            for (i in ayaBookmarks.indices) {
                val displayableBookmark = DisplayableBookmark()
                val suraName = context.resources.getStringArray(R.array.sura_name)[ayaBookmarks[i].aya.sura - 1]
                displayableBookmark.suraName = suraName
                displayableBookmark.bookmarkId = ayaBookmarks[i].bookmarkId
                displayableBookmark.bookmarkType = ayaBookmarks[i].bookmarkTypeId
                displayableBookmark.ayaContent = ayaBookmarks[i].aya.pureText
                displayableBookmark.ayaId = ayaBookmarks[i].aya.id
                displayableBookmark.suraAyaNumber = ayaBookmarks[i].aya.suraAya
                displayableBookmark.guz2Number = ayaBookmarks[i].aya.juz
                displayableBookmark.pageNumber = ayaBookmarks[i].aya.page
                for (j in types.indices) {
                    if (types[j].typeId == ayaBookmarks[i].bookmarkTypeId) {
                        displayableBookmark.colorIndex = types[j].colorIndex
                        break
                    }
                }
                displayableBookmarks.add(displayableBookmark)
            }
            displayableBookmarks
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                listLiveData.value = result
            }, {
                Log.d("Error", "Error")
            })
        return listLiveData
    }

    @SuppressLint("CheckResult")
    override fun deleteBookmark(bookmarkId: Int) {
        Completable.fromAction { userDatabase.bookmarkDao.deleteAyaBookmark(bookmarkId) }
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
    }

    override fun filterBookmarks(filterType: Int): LiveData<List<AyaBookmark>> {
        @Suppress("UNCHECKED_CAST")
        return userDatabase.bookmarkDao.getFilterBookmaks(filterType) as LiveData<List<AyaBookmark>>
    }

    @SuppressLint("CheckResult")
    override fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int) {
        Completable.fromAction { userDatabase.bookmarkDao.changeAyaBookmarkType(bookmarkId, bookmarkTypeId) }
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
    }

    override val bookmarkTypes: LiveData<List<BookmarkType>>
        get() {
            @Suppress("UNCHECKED_CAST")
            return userDatabase.bookmarkDao.getBookmarkTypesLiveData() as LiveData<List<BookmarkType>>
        }
}
