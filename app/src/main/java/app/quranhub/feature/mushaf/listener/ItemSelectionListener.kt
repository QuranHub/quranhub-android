package app.quranhub.feature.mushaf.listener

interface ItemSelectionListener<T> {
    fun onSelectItem(item: T)
}