package app.quranhub.ui.mushaf.presenter

import android.content.Context
import org.greenrobot.eventbus.EventBus
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.base.BasePresenterImp
import app.quranhub.ui.mushaf.events.QuranPageClickEvent
import app.quranhub.ui.mushaf.interactor.QuranPageInteractor
import app.quranhub.ui.mushaf.interactor.QuranPageInteractorImp
import app.quranhub.ui.mushaf.model.BookmarkModel
import app.quranhub.ui.mushaf.view.QuranPageView

class QuranPagePresenterImp(context: Context) : BasePresenterImp<QuranPageView>(),
    QuranPagePresenter<QuranPageView>, QuranPageInteractor.ResultListener {

    private val context: Context = context
    private val interactor: QuranPageInteractor = QuranPageInteractorImp(context, this)

    override fun getPageAyas(page: Int) {
        interactor.getPageAyas(page)
    }

    override fun insertAyaBookmark(ayaBookmark: AyaBookmark) {
        baseView!!.showLoading()
        interactor.insertAyaBookmark(ayaBookmark)
    }

    override fun getAyaBookmarkType(ayaId: Int) {
        interactor.getBookmarkType(ayaId)
    }

    override fun drawInitAyaShadow(pageNumber: Int, ayaId: Int) {
        interactor.getPageAyaWithPrevious(pageNumber, ayaId)
    }

    override fun handleQuranPageClick() {
        EventBus.getDefault().post(QuranPageClickEvent())
    }

    override fun addNote(note: Note) {
        interactor.addNote(note)
    }

    override fun checkAyaHasNote(ayaId: Int) {
        if (isViewAttached) {
            interactor.checkAyaNote(ayaId)
        }
    }

    override fun getBookmarkTypes() {
        if (isViewAttached) {
            interactor.getBookmarkTypes()
        }
    }

    override fun insertCustomBookmark(currentAya: Aya, type: BookmarkType) {
        if (isViewAttached) {
            interactor.insertCustomBookmark(currentAya, type)
        }
    }

    override fun onGetAyaWithPrevious(aya: Aya?, previousAya: Aya?) {
        if (aya != null) {
            baseView!!.drawInitAyaShadow(aya, previousAya)
        }
    }

    override fun onGetPageAyas(ayaList: List<Aya>) {
        if (isViewAttached) {
            baseView!!.onGetPageAya(ayaList)
        }
    }

    override fun onGetBookmarkType(bookmarkModel: BookmarkModel) {
        if (isViewAttached) {
            baseView!!.onGetAyaBookmarkType(bookmarkModel)
        }
    }

    override fun onSuccessRemoveBookmark() {
        if (isViewAttached) {
            baseView!!.onSuccessRemoveBookmark()
        }
    }

    override fun removeBookmark(ayaId: Int) {
        interactor.removeBookmark(ayaId)
    }

    override fun showMessage(message: String) {
        if (isViewAttached) {
            baseView!!.hideLoading()
            baseView!!.showMessage(message)
        }
    }

    override fun onSuccessAddNote() {
        if (isViewAttached) {
            baseView!!.hideLoading()
        }
    }

    override fun onGetAyaNote(note: Note) {
        if (isViewAttached) {
            baseView!!.hideLoading()
            baseView!!.onAyaHasNote(note)
        }
    }

    override fun onGetBookmarkTypes(bookmarkTypes: List<BookmarkType>) {
        if (isViewAttached) {
            baseView!!.onGetAyaBookmarkTypes(bookmarkTypes)
        }
    }
}
