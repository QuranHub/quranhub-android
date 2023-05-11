package app.quranhub.ui.mushaf.model

import androidx.room.ColumnInfo

class MyNoteModel {
    var sura = 0
    @ColumnInfo("sura_aya")
    var suraAya = 0
    @ColumnInfo("pure_text")
    var pureText: String? = null
    var text: String? = null
    var page = 0
}