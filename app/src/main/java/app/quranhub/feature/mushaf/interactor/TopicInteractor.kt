package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.model.SearchModel

interface TopicInteractor {
    suspend fun getAyas(categoryId: Int): List<SearchModel>
}
