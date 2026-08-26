package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.room.EmptyResultSetException
import app.quranhub.R
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class QuranPageInteractorImp(
    context: Context,
    private val resultListener: QuranPageInteractor.ResultListener
) : QuranPageInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)
    private val userDatabase: UserDatabase = UserDatabase.getInstance(context)
    private val context: Context = context

    @SuppressLint("CheckResult")
    override fun getPageAyaWithPrevious(pageNumber: Int, ayaId: Int) {
        Single.fromCallable {
            val ayas = mutableListOf<Aya>()
            val currentAya = mushafDatabase.ayaDao.getPageAya(pageNumber, ayaId)
            val previousAya = mushafDatabase.ayaDao.getPageAya(pageNumber, ayaId - 1)
            if (currentAya != null) ayas.add(currentAya)
            if (previousAya != null) ayas.add(previousAya)
            ayas
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ pageAyas ->
                Log.d(TAG, "getPageAyaWithPrevious - pageAyas.size=${pageAyas.size}")
                if (pageAyas.size == 2) {
                    resultListener.onGetAyaWithPrevious(pageAyas[0], pageAyas[1])
                }
            }, { error ->
                Log.e(TAG, "getPageAyaWithPrevious error: $error")
            })
    }

    @SuppressLint("CheckResult")
    override fun getPageAyas(page: Int) {
        mushafDatabase.ayaDao.getAllInPage(page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                if (result != null) {
                    resultListener.onGetPageAyas(result)
                } else {
                    resultListener.showMessage(context.getString(R.string.page_info_failed))
                }
            }, { error ->
                Log.d(TAG, "getPageAyas: $error")
                resultListener.showMessage(context.getString(R.string.page_info_failed))
            })
    }

    @SuppressLint("CheckResult")
    override fun getBookmarkType(ayaId: Int) {
        userDatabase.bookmarkDao.getBookmarkType(ayaId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ type ->
                resultListener.onGetBookmarkType(type)
            }, { error ->
                if (error is EmptyResultSetException) {
                    Log.d(TAG, "getBookmarkType: No bookmark for this aya")
                } else {
                    Log.e(TAG, "getBookmarkType: Error", error)
                }
            })
    }

    @SuppressLint("CheckResult")
    override fun insertAyaBookmark(ayaBookmark: AyaBookmark) {
        Completable.fromAction { userDatabase.bookmarkDao.insertAyaBookmark(ayaBookmark) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                resultListener.showMessage(context.getString(R.string.success_insert_bookmark))
            }, {
                resultListener.showMessage(context.getString(R.string.insert_bookmark_failed))
            })
    }

    @SuppressLint("CheckResult")
    override fun removeBookmark(ayaId: Int) {
        Completable.fromAction { userDatabase.bookmarkDao.deleteAyaBookmark(ayaId) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                resultListener.onSuccessRemoveBookmark()
            }, {
                resultListener.showMessage(context.getString(R.string.bookmark_failed_removed))
            })
    }

    override fun addNote(note: Note) {
        Completable.fromAction { userDatabase.noteDao.insertNote(note) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                resultListener.onSuccessAddNote()
            }, {
                Log.e(TAG, "onError: ")
            })
    }

    @SuppressLint("CheckResult")
    override fun checkAyaNote(ayaId: Int) {
        userDatabase.noteDao.getAyaNote(ayaId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                resultListener.onGetAyaNote(result)
            }, { error ->
                if (error is EmptyResultSetException) {
                    Log.d(TAG, "checkAyaNote: No note for this aya")
                } else {
                    Log.e(TAG, "checkAyaNote: Error", error)
                }
            })
    }

    @SuppressLint("CheckResult")
    override fun getBookmarkTypes() {
        userDatabase.bookmarkDao.getBookmarkTypes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                resultListener.onGetBookmarkTypes(result)
            }, {
                Log.e(TAG, "onError: getBookmarkTypes")
            })
    }

    override fun insertCustomBookmark(currentAya: Aya, type: BookmarkType) {
        Completable.fromAction { userDatabase.bookmarkDao.insertBookmarkType(type) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                insertAyaBookmark(AyaBookmark(currentAya.id, type.typeId, currentAya))
            }, {
                resultListener.showMessage(context.getString(R.string.insert_bookmark_failed))
            })
    }

    companion object {
        private const val TAG = "QuranPageInteractorImp"
    }
}
