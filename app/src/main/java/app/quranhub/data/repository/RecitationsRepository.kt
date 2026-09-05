package app.quranhub.data.repository

import app.quranhub.data.model.ReciterModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RecitationsRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getRecitersForRecitation(recitationKey: String): List<ReciterModel> {
        return suspendCancellableCoroutine { continuation ->
            db.collection("recitations")
                .document(recitationKey)
                .collection("reciters")
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        continuation.resume(
                            task.result.toObjects(ReciterModel::class.java)
                        )
                    } else {
                        continuation.resumeWithException(
                            task.exception
                                ?: IllegalStateException("Firestore task failed without exception")
                        )
                    }
                }
        }
    }
}
