package app.quranhub.ui.downloads_manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import app.quranhub.R
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Reciter
import app.quranhub.data.local.entity.ReciterRecitation
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.Companion.newInstance
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.util.QuranAudioDeleteUtils.DeleteFinishListener
import app.quranhub.util.QuranAudioDeleteUtils.deleteReciterAudio

class DownloadsRecitersFragment : BaseDownloadsFragment(), DeleteConfirmationCallbacks {

    private var recitationId = 0
    private val reciters: List<Reciter>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
        }
    }

    override fun provideDisplayableDownloads(): List<DisplayableDownload> {
        val displayableDownloadsList: MutableList<DisplayableDownload> = ArrayList()

//        RecitersApi recitersApi = ApiClient.getClient().create(RecitersApi.class);
//        Call<RecitersResponse> recitersCall = recitersApi.getQuranReciters(recitationId);
//        try {
//            Response<RecitersResponse> response = recitersCall.execute();
//            RecitersResponse recitersResponse = response.body();
//            if (recitersResponse != null) {
//                reciters = recitersResponse.getReciters();
//            } else {
//                Log.e(TAG, "recitersResponse is null!");
//                reciters = retrieveLocalReciters();
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to retrieve reciters from server.");
//            reciters = retrieveLocalReciters();
//        }

        // process reciters list
        for (r in reciters!!) {
            val userDatabase = UserDatabase.getInstance(requireContext())
            val displayableDownload = DisplayableDownload(
                r.name
            )
            val downloadedSurasIds = userDatabase.reciterRecitationDao
                .getSurasIdsForReciterInRecitation(recitationId, r.id)
            displayableDownload.downloadedAmount =
                getString(R.string.downloaded_amount_suras, downloadedSurasIds.size)
            displayableDownload.isDownloadable = downloadedSurasIds.size < 114
            displayableDownload.isDeletable = downloadedSurasIds.size > 0
            displayableDownloadsList.add(displayableDownload)
        }
        return displayableDownloadsList
    }

    private fun retrieveLocalReciters(): List<Reciter> {
        return UserDatabase.getInstance(requireContext())
            .reciterDao.getAllForRecitation(recitationId)
    }

    override fun onClickItem(displayableDownload: DisplayableDownload?, position: Int) {
        val reciter = reciters!![position]
        navigationCallbacks!!.gotoDownloadsSuras(recitationId, reciter.id, reciter.name)
    }

    override fun onDeleteItem(displayableDownload: DisplayableDownload?, position: Int) {
        val confirmationDialog = newInstance(
            getString(R.string.confirm_delete_title),
            getString(R.string.confirm_delete_description_reciters), position
        )
        confirmationDialog.show(childFragmentManager, "DeleteConfirmationDialogFragment")
    }

    override fun onConfirmDelete(deletePosition: Int) {
        deleteReciterAudio(requireContext(),
            recitationId,
            reciters!![deletePosition].id,
            object : DeleteFinishListener {
                override fun onDeleteFinish() {
                    refresh()
                }
            })
    }

    @SuppressLint("StaticFieldLeak")
    override fun onDownloadItem(displayableDownload: DisplayableDownload?, position: Int) {
        val reciter = reciters!![position]
        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg voids: Void?): Void? {
                val userDatabase = UserDatabase.getInstance(requireContext())
                if (userDatabase.reciterDao.getById(reciter.id) == null) {
                    userDatabase.reciterDao.insert(reciter)
                }
                if (userDatabase.reciterRecitationDao[recitationId, reciter.id] == null) {
                    userDatabase.reciterRecitationDao.insert(
                        ReciterRecitation(recitationId, reciter.id)
                    )
                }
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                navigationCallbacks!!.openAudioDownloadAmountDialog(recitationId, reciter.id)
            }
        }.execute()
    }

    companion object {

        private val TAG = DownloadsRecitersFragment::class.java.simpleName

        private const val ARG_RECITATION_ID = "ARG_RECITATION_ID"

        @JvmOverloads
        fun newInstance(
            context: Context, recitationId: Int, isEditable: Boolean = false
        ): DownloadsRecitersFragment {
            val recitersFragment = DownloadsRecitersFragment()
            val args = Bundle()
            args.putInt(ARG_RECITATION_ID, recitationId)
            args.putString(
                ARG_DESCRIPTION,
                context.getString(R.string.description_manage_reciters_downloads)
            )
            args.putBoolean(ARG_EDITABLE, isEditable)
            recitersFragment.arguments = args
            return recitersFragment
        }
    }
}