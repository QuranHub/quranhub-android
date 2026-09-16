package app.quranhub.core.data.model

import androidx.room.ColumnInfo

class MyNoteModel {
    var id = 0
    var sura = 0
    @ColumnInfo("sura_aya")
    var suraAya = 0
    @ColumnInfo("pure_text")
    var pureText: String? = null
    var text: String? = null
    var page = 0
}