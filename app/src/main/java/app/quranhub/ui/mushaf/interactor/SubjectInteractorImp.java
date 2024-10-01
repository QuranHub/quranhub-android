package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.entity.QuranSubject;
import app.quranhub.data.local.entity.QuranSubjectCategory;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.mushaf.model.TopicModel;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SubjectInteractorImp implements SubjectInteractor {

    private final Context context;
    @NonNull
    private final MushafDatabase mushafDatabase;
    private final SubjectInteractor.SubjectListener listener;

    public SubjectInteractorImp(@NonNull Context context, SubjectInteractor.SubjectListener listener) {
        this.context = context;
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
        this.listener = listener;
    }

    @SuppressLint("CheckResult")
    @Override
    public void getSubjects(List<String> subjects, List<String> subjectsCategory) {

        //      List<String> subjects = Arrays.asList(context.getResources().getStringArray(R.array.subject_name));
        //     List<String> subjectsCategory = Arrays.asList(context.getResources().getStringArray(R.array.subject_category_name));

        Single<List<QuranSubject>> quranSubjects = mushafDatabase.getQuranSubjectDao().getAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Single<List<QuranSubjectCategory>> quranSubjectsCategory = mushafDatabase.getQuranSubjectCategoryDao().getAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Single.zip(quranSubjects, quranSubjectsCategory, (quranSubjects1, quranSubjectCategories) -> {
                    List<TopicModel> results = new ArrayList<>();
                    List<TopicCategory> topicCategories = new ArrayList<>();
                    int topicIndex = 0;
                    for (int i = 0; i < quranSubjects1.size(); i++) {

                        if (i > 0 && quranSubjects1.get(i).getCategory() != quranSubjects1.get(i - 1).getCategory()) {
                            results.add(new TopicModel(subjectsCategory.get(topicIndex), topicCategories));
                            topicIndex++;
                            topicCategories = new ArrayList<>();
                        }
                        topicCategories.add(new TopicCategory(subjects.get(i), quranSubjects1.get(i).getAyaCount(), quranSubjects1.get(i).getId()));
                    }
                    results.add(new TopicModel(subjectsCategory.get(topicIndex), topicCategories));
                    return results;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    listener.onGetSubjects(result);
                }, error -> {
                    Log.d("Error", "Error");
                });

    }
}
