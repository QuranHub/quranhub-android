package app.quranhub.ui.mushaf.model

import android.content.Context
import app.quranhub.R
import app.quranhub.util.LocaleUtils.formatNumber

/**
 * Mapper class to use columns as strings to convert number to arabic
 */
data class SuraIndexModelMapper(
    var id: String? = null,
    var name: String? = null,
    var guz: String? = null,
    var page: String? = null,
    var suraType: String? = null,
    var numOfAya: String? = null,
    var suraHezb: String? = null,
    var suraRob3: String? = null
) {

    companion object {
        @JvmStatic
        fun mapToString(model: SuraIndexModel, context: Context): SuraIndexModelMapper {
            val indexModelMapper = SuraIndexModelMapper()
            val suraName =
                " ${context.resources.getStringArray(R.array.sura_name)[model.sura - 1]} "
            indexModelMapper.guz = formatNumber(model.juz)
            indexModelMapper.id = formatNumber(model.id)
            indexModelMapper.page = formatNumber(model.page)
            indexModelMapper.numOfAya = formatNumber(model.ayas)
            indexModelMapper.suraHezb = formatNumber(model.sura_hezb)
            indexModelMapper.suraRob3 = formatNumber(model.sura_rob3)
            indexModelMapper.name = suraName
            indexModelMapper.suraType = model.type
            return indexModelMapper
        }
    }
}