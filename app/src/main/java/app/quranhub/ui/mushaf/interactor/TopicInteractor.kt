package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.SearchModel

interface TopicInteractor {
    fun getAyas(categoryId: Int)

    interface TopicListener {
        fun onGetTopics(searchModels: List<SearchModel>)
    }
}
