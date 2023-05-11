package app.quranhub.data.remote.model

import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.util.NetworkUtil
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class TranslationsResponse(var translations: List<TranslationData>?) {

    val translationBooks: List<TranslationBook>?
        get() = getTranslationBooksForLanguage(null)

    fun getTranslationBooksForLanguage(languageCode: String?): List<TranslationBook>? {
        var translationBooks: MutableList<TranslationBook>? = null
        if (translations != null) {
            translationBooks = ArrayList()
            for (t in translations!!) {
                if (languageCode == null || t.language.lowercase(Locale.getDefault()) == languageCode.lowercase(
                        Locale.getDefault()
                    )
                ) {
                    val book = TranslationBook(
                        t.id,
                        t.name,
                        t.author,
                        t.language,
                        t.path,
                        t.path.replace('/', '_'),
                        NetworkUtil.STATUS_NOT_DOWNLOADED,
                        0
                    )
                    translationBooks.add(book)
                }
            }
        }
        return translationBooks
    }

    data class TranslationData(
        var id: String,
        var name: String,
        var path: String,
        var author: String,
        var language: String,
        @field:SerializedName("created_at") var createdAt: Long,
        @field:SerializedName("updated_at") var updatedAt: Long
    )
}