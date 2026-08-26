package app.quranhub.ui.mushaf.presenter

import app.quranhub.ui.base.BasePresenterImp
import app.quranhub.ui.mushaf.view.BookmarksView

class BookmarksPresenterImp : BasePresenterImp<BookmarksView>(), BookmarksPresenter<BookmarksView> {

    override fun enableEditList() {
        baseView!!.enableEditList()
    }

    override fun finishEditList() {
        baseView!!.finishEditList()
    }

    override fun filterList() {
        baseView!!.filterList()
    }

    override fun searchList(text: String) {
        baseView!!.searchList(text)
    }
}
