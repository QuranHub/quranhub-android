package app.quranhub.data.repository

import app.quranhub.data.model.ReciterModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Single
import io.reactivex.SingleEmitter

class RecitationsRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getRecitersForRecitation(recitationKey: String): Single<List<ReciterModel>?> {

        return Single.create { emitter: SingleEmitter<List<ReciterModel>?> ->
            db.collection("recitations")
                .document(recitationKey)
                .collection("reciters")
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        val reciterModels = task.result.toObjects(
                            ReciterModel::class.java
                        )
                        emitter.onSuccess(reciterModels)
                    } else {
                        emitter.onError(task.exception!!)
                    }
                }
        }
    }

    companion object {
        private val TAG = RecitationsRepository::class.java.simpleName
    }
}