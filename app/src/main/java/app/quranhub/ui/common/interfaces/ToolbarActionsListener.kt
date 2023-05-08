package app.quranhub.ui.common.interfaces

interface ToolbarActionsListener {

    fun onNavDrawerClick()

    fun onSuraClick()

    fun onGuz2Click()

    fun onBookmarkClick()

    fun selectNavDrawerItem(itemIdentifier: Long, fireOnClick: Boolean)
}