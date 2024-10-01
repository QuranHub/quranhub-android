package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import app.quranhub.data.Constants;
import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.TranslationDatabase;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.AyaRecorder;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.ui.mushaf.model.SuraVersesNumber;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class Mus7fInteractorImp implements Mus7fInteractor {


    private final MushafDatabase mushafDatabase;
    private final UserDatabase userDatabase;
    private TranslationDatabase translationDatabase;
    private final Mus7fInteractor.ResultListener resultListener;
    private final String TAG = "Mus7fInteractorImp";
    private final Context context;
    private final int chosenRecitation;
    private String chosenSheikh;


    public Mus7fInteractorImp(Mus7fInteractor.ResultListener resultListener, Context context) {
        mushafDatabase = MushafDatabase.getInstance(context);
        userDatabase = UserDatabase.getInstance(context);
        this.resultListener = resultListener;
        this.context = context;
        this.chosenRecitation = -1;
    }

    @Override
    public void initTranslationDB(String dbName) {
        translationDatabase = TranslationDatabase.newInstance(context, dbName);
    }

    @SuppressLint("CheckResult")
    @Override
    public void getPageSuras() {
        mushafDatabase.getAyaDao().getSuraPage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(pageSuras -> {
                    ArrayList<Integer> surasInPage = new ArrayList<>();
                    ArrayList<ArrayList<Integer>> results = new ArrayList<>();
                    for (int i = 0; i < pageSuras.size(); i++) {
                        if (i == 0) {
                            surasInPage.add(pageSuras.get(i).getSura());
                            results.add(surasInPage);
                            surasInPage.clear();
                        }
                        if (i == pageSuras.size() - 1) {
                            surasInPage.add(pageSuras.get(i).getSura());
                            results.add(surasInPage);
                        } else if (pageSuras.get(i).getPage() != pageSuras.get(i + 1).getPage()) {
                            surasInPage.add(pageSuras.get(i).getSura());
                            results.add(surasInPage);
                            surasInPage = new ArrayList<>();
                        } else {
                            surasInPage.add(pageSuras.get(i).getSura());
                        }
                    }
                    results.add(surasInPage);
                    return results;
                })
                .subscribe(result -> {
                    resultListener.onGetSuraPage(result);
                }, error -> {

                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void getSuraNumofVerses() {
        mushafDatabase.getSuraDao().getSuraVersesNumber()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    resultListener.onGetSuraVersesNumber((ArrayList<SuraVersesNumber>) res);
                }, error -> {
                    Log.d(TAG, "Failed getSuraNumofVerses: ");
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void getFromAyaPage(int fromAya) {
        mushafDatabase.getAyaDao().getAyaPage(fromAya)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> resultListener.onGetAyaPage(res), error -> {
                    Log.d(TAG, "Failed getFromAyaPage: ");
                });
    }


    @SuppressLint("CheckResult")
    @Override
    public void getPageInfo(int curentPage) {
        mushafDatabase.getSuraDao().getQuranPageInfo(curentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        resultListener.onGetPageInfo(result);
                    } else {
                        resultListener.onErrorOccurred();
                    }
                }, error -> {
                    Log.d(TAG, "getPageInfo: " + error.toString());
                    resultListener.onErrorOccurred();
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void getTafseerBook(String currentTafsserId) {
        userDatabase.getTranslationBookDao().findById(currentTafsserId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    resultListener.onGetTafsserBook(res);
                }, error -> {
                    resultListener.onNoBooks();
                });
    }


    @SuppressLint("CheckResult")
    @Override
    public void getAyaTafseer(int ayaId) {


        if (translationDatabase != null) {
            translationDatabase.getTranslationDao()
                    .findByIndex(ayaId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(res -> {
                        resultListener.onGetAyaTafseer(res);
                    }, error -> {
                        resultListener.onErrorOccurred();
                    });

        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void checkAyaHasRecorder(int id) {
        int recitation = AppPreferencesManager.getRecitationSetting(context);
        userDatabase.getQuranAudioDao().getAyaRecorderPath(id, recitation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(path -> {
                    resultListener.onAyaHasRecorder(path);
                }, error -> {
                    Log.e(TAG, "checkAyaHasRecorder: No recorder exist");
                });
    }

    @Override
    public void saveRecorderPath(int ayaId, String recorderPath) {
        int recitation = AppPreferencesManager.getRecitationSetting(context);
        AyaRecorder ayaRecorder = new AyaRecorder(ayaId, recitation, recorderPath);

        Completable.fromAction(() ->
                        userDatabase.getQuranAudioDao().insertAyaRecorder(ayaRecorder))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void getAya(int currentAyaId) {
        mushafDatabase.getAyaDao().findById(currentAyaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    resultListener.onGetAya(result);
                }, error -> {
                    Log.e(TAG, "onError: getAya");
                });
    }

    @Override
    public void deleteAyaVoiceRecorder(int ayaId) {
        int recitation = AppPreferencesManager.getRecitationSetting(context);

        Completable.fromAction(() ->
                        userDatabase.getQuranAudioDao().deleteAyaVoiceRecorder(ayaId, recitation))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        deleteRecorderLocally(ayaId, recitation);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    private void deleteRecorderLocally(int ayaId, int recitation) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), Constants.Directory.AYA_VOICE_RECORDER
                + File.separator + recitation + File.separator
                + ayaId + ".3gp");
        if (file.exists()) {
            file.delete();
        }
    }


}
