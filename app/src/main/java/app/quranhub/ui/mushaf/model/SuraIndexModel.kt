package app.quranhub.ui.mushaf.model

import androidx.room.Ignore

data class SuraIndexModel(
    var id: Int = 0,
    var sura: Int = 0,
    var juz: Int = 0,
    var page: Int = 0,
    var ayas: Int = 0,
    var type: String? = null,
    @Ignore var sura_hezb: Int = 0, // ignore them to be not included in join query of sura index
    @Ignore var sura_rob3: Int = 0
)