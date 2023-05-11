package app.quranhub.ui.mushaf.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import app.quranhub.data.local.db.MushafDatabase.Companion.getInstance
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.ui.mushaf.interactor.BookmarksInteractor
import app.quranhub.ui.mushaf.interactor.BookmarksInteractorImp
import app.quranhub.ui.mushaf.model.DisplayableBookmark

class BookmarksListViewModel(private val application: Application) : AndroidViewModel(application) {
    private val bookmarksInteractor: BookmarksInteractor
    private val _bookmarks: MediatorLiveData<List<DisplayableBookmark>>
    private val _bookmarkLiveData: LiveData<List<DisplayableBookmark>>
    private val _bookmarksTypes: MediatorLiveData<List<BookmarkType>>
    private var bookmarkTypeLiveData: LiveData<List<BookmarkType>>? = null

    init {
        bookmarksInteractor = BookmarksInteractorImp(application.applicationContext)
        _bookmarkLiveData = bookmarksInteractor.allBookmarks
        _bookmarksTypes = MediatorLiveData()
        _bookmarks = MediatorLiveData()
        _bookmarks.addSource(_bookmarkLiveData) { ayaBookmarks: List<DisplayableBookmark> ->
            _bookmarks.setValue(
                ayaBookmarks
            )
        }
    }

    fun getBookmarks(): LiveData<List<DisplayableBookmark>> {
        return _bookmarks
    }

    fun getBookmarksTypes(): LiveData<List<BookmarkType>> {
        return _bookmarksTypes
    }

    /**
     * Maps a List of AyaBookmark to a List of DisplayableBookmark.
     *
     * @param ayaBookmarks
     * @param listener
     */
    @SuppressLint("StaticFieldLeak")
    fun bookmarksMapper(
        ayaBookmarks: List<DisplayableBookmark>, listener: BookmarkMapperListener
    ) {

        // TODO refactor AsyncTask
        object : AsyncTask<DisplayableBookmark?, Void?, List<DisplayableBookmark>>() {
            protected override fun doInBackground(vararg bookmarks: DisplayableBookmark?): List<DisplayableBookmark> {
                // TODO simplify the DB queries
                val dao = getInstance(application)
                    .hizbQuarterDao
                val result: MutableList<DisplayableBookmark> = ArrayList()
                for (bookmark in bookmarks) {
                    bookmark?.let {
                        val hizbQuarterDataModel = dao.getHizbQuarterDataModelForAya(bookmark.ayaId)
                        bookmark.hizbNumber = hizbQuarterDataModel!!.hizb
                        bookmark.rub3Number = hizbQuarterDataModel.quarter
                        result.add(bookmark)
                    }
                }
                return result
            }

            override fun onPostExecute(displayableBookmarks: List<DisplayableBookmark>) {
                listener.onDisplayableBookmarksReady(displayableBookmarks)
            }
        }.execute(*ayaBookmarks.toTypedArray())
    }

    fun deleteBookmark(bookmarkId: Int) {
        bookmarksInteractor.deleteBookmark(bookmarkId)
    }

    fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int) {
        bookmarksInteractor.changeBookmarkType(bookmarkId, bookmarkTypeId)
    }

    val bookmarkTypes: Unit
        get() {
            bookmarkTypeLiveData = bookmarksInteractor.bookmarkTypes
            _bookmarksTypes.addSource(
                bookmarkTypeLiveData!!
            ) { types: List<BookmarkType> -> _bookmarksTypes.setValue(types) }
        }

    interface BookmarkMapperListener {
        fun onDisplayableBookmarksReady(displayableBookmarks: List<DisplayableBookmark>?)
    }
}