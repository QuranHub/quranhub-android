package app.quranhub.ui.mushaf.model

data class DisplayableBookmark(
    var bookmarkId: Int = 0,
    var bookmarkType: Int = 0,
    var ayaContent: String? = null,
    var ayaId: Int = 0,
    var suraAyaNumber: Int = 0,
    var guz2Number: Int = 0,
    var hizbNumber: Int = 0,
    var rub3Number: Int = 0,
    var suraName: String? = null,
    var pageNumber: Int = 0,
    var colorIndex: Int = 0
)