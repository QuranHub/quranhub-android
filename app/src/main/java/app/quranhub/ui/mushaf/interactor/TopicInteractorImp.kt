package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.SearchModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TopicInteractorImp(context: Context) : TopicInteractor {

    private val mushafDatabase: MushafDatabase =
        MushafDatabase.getInstance(context.applicationContext)

    override suspend fun getAyas(categoryId: Int): List<SearchModel> = coroutineScope {
        val topicAyas = async { mushafDatabase.ayaDao.getCategoryAyas(categoryId) }
        val hizbQuarters = async { mushafDatabase.hizbQuarterDao.getAll() }
        HizbQuarterSearchMapper.attachHizbQuarters(topicAyas.await(), hizbQuarters.await())
    }
}
