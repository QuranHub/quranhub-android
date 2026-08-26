package app.quranhub.data.repository

import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.model.TranslationModel
import app.quranhub.util.NetworkUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.map

class TranslationsRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getTranslationsForLanguage(languageCode: String) = db.collection("tafseers")
        .dataObjects<TranslationModel>()
        .map { translations ->
            translations.map {
                TranslationBook(
                    it.id,
                    it.name,
                    it.author,
                    it.language,
                    it.downloadUrl,
                    it.downloadUrl.substringAfterLast("/"),
                    NetworkUtil.STATUS_NOT_DOWNLOADED,
                    0
                )
            }.filter {
                it.language == languageCode
            }
        }
}
