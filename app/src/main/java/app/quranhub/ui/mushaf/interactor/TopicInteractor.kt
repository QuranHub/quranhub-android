package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.SearchModel

interface TopicInteractor {
    suspend fun getAyas(categoryId: Int): List<SearchModel>
}
