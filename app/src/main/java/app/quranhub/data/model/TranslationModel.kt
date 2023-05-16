package app.quranhub.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class TranslationModel(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var language: String = "",
    var author: String = "",
    @set:PropertyName("download_url")
    @get:PropertyName("download_url")
    var downloadUrl: String = ""
)