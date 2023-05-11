package app.quranhub.ui.mushaf.model

import androidx.room.ColumnInfo

data class HizbQuarterDataModel(
    @ColumnInfo(name = "sura_number") var suraNumber: Int,
    @ColumnInfo(name = "aya_number") var ayaNumber: Int,
    @ColumnInfo(name = "aya_text") var ayaText: String,
    @ColumnInfo(name = "start_page") var startPage: Int,
    @ColumnInfo(name = "end_page") var endPage: Int,
    var juz: Int,
    var hizb: Int,
    var quarter: Int
)