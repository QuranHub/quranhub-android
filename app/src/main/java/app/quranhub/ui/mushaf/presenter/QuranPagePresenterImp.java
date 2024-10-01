package app.quranhub.ui.mushaf.presenter;

import android.content.Context;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.AyaBookmark;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.data.local.entity.Note;
import app.quranhub.ui.base.BasePresenterImp;
import app.quranhub.ui.mushaf.events.QuranPageClickEvent;
import app.quranhub.ui.mushaf.interactor.QuranPageInteractor;
import app.quranhub.ui.mushaf.interactor.QuranPageInteractorImp;
import app.quranhub.ui.mushaf.model.BookmarkModel;
import app.quranhub.ui.mushaf.view.QuranPageView;

public class QuranPagePresenterImp extends BasePresenterImp<QuranPageView> implements QuranPagePresenter<QuranPageView>, QuranPageInteractor.ResultListener {


    private static final String TAG = QuranPagePresenterImp.class.getSimpleName();

    private final Context context;
    private final QuranPageInteractor interactor;

    public QuranPagePresenterImp(Context context) {
        this.interactor = new QuranPageInteractorImp(context, this);
        this.context = context;
    }

    @Override
    public void getPageAyas(int page) {
        interactor.getPageAyas(page);
    }

    @Override
    public void insertAyaBookmark(AyaBookmark ayaBookmark) {
        baseView.showLoading();
        interactor.insertAyaBookmark(ayaBookmark);
    }

    @Override
    public void getAyaBookmarkType(int ayaId) {
        interactor.getBookmarkType(ayaId);
    }

    @Override
    public void drawInitAyaShadow(int pageNumber, int ayaId) {
        interactor.getPageAyaWithPrevious(pageNumber, ayaId);
    }

    @Override
    public void handleQuranPageClick() {
        EventBus.getDefault().post(new QuranPageClickEvent());
    }

    @Override
    public void addNote(Note note) {
        interactor.addNote(note);
    }

    @Override
    public void checkAyaHasNote(int ayaId) {
        if (isViewAttached()) {
            interactor.checkAyaNote(ayaId);
        }
    }


    @Override
    public void getBookmarkTypes() {
        if (isViewAttached()) {
            interactor.getBookmarkTypes();
        }
    }

    @Override
    public void insertCustomBookmark(Aya currentAya, BookmarkType type) {
        if (isViewAttached()) {
            interactor.insertCustomBookmark(currentAya, type);
        }
    }


    @Override
    public void onGetAyaWithPrevious(@Nullable Aya aya, @Nullable Aya previousAya) {
        if (aya != null) {
            baseView.drawInitAyaShadow(aya, previousAya);
        }
    }

    @Override
    public void onGetPageAyas(List<Aya> ayaList) {
        if (isViewAttached()) {
            baseView.onGetPageAya(ayaList);
        }
    }

    @Override
    public void onGetBookmarkType(BookmarkModel bookmarkModel) {
        if (isViewAttached()) {
            baseView.onGetAyaBookmarkType(bookmarkModel);
        }
    }


    @Override
    public void onSuccessRemoveBookmark() {
        if (isViewAttached()) {
            baseView.onSuccessRemoveBookmark();
        }
    }

    @Override
    public void removeBookmark(int ayaId) {
        interactor.removeBookmark(ayaId);
    }


    @Override
    public void showMessage(String message) {
        if (isViewAttached()) {
            baseView.hideLoading();
            baseView.showMessage(message);
        }
    }

    @Override
    public void onSuccessAddNote() {
        if (isViewAttached()) {
            baseView.hideLoading();
        }
    }

    @Override
    public void onGetAyaNote(Note note) {
        if (isViewAttached()) {
            baseView.hideLoading();
            baseView.onAyaHasNote(note);
        }
    }


    @Override
    public void onGetBookmarkTypes(List<BookmarkType> bookmarkTypes) {
        if (isViewAttached()) {
            baseView.onGetAyaBookmarkTypes(bookmarkTypes);
        }
    }


}
