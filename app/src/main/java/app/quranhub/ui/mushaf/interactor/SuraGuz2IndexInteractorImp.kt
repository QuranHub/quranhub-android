package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import app.quranhub.R
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.SuraIndexModel
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SuraGuz2IndexInteractorImp(
    private val listener: SuraGuz2IndexInteractor.GetIndexListener,
    context: Context
) : SuraGuz2IndexInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)
    private val context: Context = context

    @SuppressLint("CheckResult")
    override fun getSuraIndex() {
        mushafDatabase.suraDao.getSuraIndexInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { suraIndexModels ->
                val suraIndexModelMapperList = mutableListOf<SuraIndexModelMapper>()
                for (model in suraIndexModels) {
                    if (model.type == "Medinan") {
                        model.type = context.getString(R.string.sura_madnya)
                    } else if (model.type == "Meccan") {
                        model.type = context.getString(R.string.sura_makya)
                    }
                    suraIndexModelMapperList.add(SuraIndexModelMapper.mapToString(model, context))
                }
                suraIndexModelMapperList
            }
            .subscribe({ result ->
                listener.onGetIndex(result)
            }, {
                listener.onGetIndexFailed(context.getString(R.string.sura_index_failed))
            })
    }
}
