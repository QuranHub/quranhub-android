package app.quranhub.ui.mushaf.model

import androidx.room.ColumnInfo
import androidx.room.Ignore

data class SearchModel(
    var id: Int = 0,
    var sura: Int = 0,
    @ColumnInfo("pure_text") var pureText: String? = null,
    var text: String? = null,
    var page: Int = 0,
    @ColumnInfo("sura_aya") var suraAya: Int = 0,
    var juz: Int = 0,
    @Ignore var hezb: Int = 0,
    @Ignore var quarter: Int = 0
)