package app.quranhub.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import app.quranhub.data.model.ReciterModel;
import io.reactivex.Single;

public class RecitationsRepository {

    private static final String TAG = RecitationsRepository.class.getSimpleName();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @NonNull
    public Single<List<ReciterModel>> getRecitersForRecitation(@NonNull String recitationKey) {
        return Single.create(emitter -> {
            db.collection("recitations")
                    .document(recitationKey)
                    .collection("reciters")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            final List<ReciterModel> reciterModels = task.getResult().toObjects(ReciterModel.class);
                            emitter.onSuccess(reciterModels);
                        } else {
                            emitter.onError(task.getException());
                        }
                    });
        });
    }

}
