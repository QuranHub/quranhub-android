package app.quranhub.feature.mushaf.listener

interface QuranNavigationCallbacks {
    fun gotoQuranPage(pageNumber: Int)
    fun gotoQuranPageAya(pageNumber: Int, ayaId: Int, addToStack: Boolean)
}