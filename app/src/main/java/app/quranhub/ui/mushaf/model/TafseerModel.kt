package app.quranhub.ui.mushaf.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import app.quranhub.data.local.entity.Translation

data class TafseerModel(
    var text: String,
    var tafseer: String,
    @ColumnInfo("pure_text") var pureText: String
) {

    @Ignore
    var isExpandable = false

    companion object {
        @JvmStatic
        fun map(
            translations: List<Translation>,
            ayasTafseer: List<TafseerModel>
        ): List<TafseerModel> {
            val tafseerModels: MutableList<TafseerModel> = ArrayList()
            val mappedCount = minOf(translations.size, ayasTafseer.size)
            for (i in 0 until mappedCount) {
                tafseerModels.add(
                    TafseerModel(
                        ayasTafseer[i].text,
                        translations[i].text,
                        ayasTafseer[i].pureText
                    )
                )
            }
            return tafseerModels
        }
    }
}