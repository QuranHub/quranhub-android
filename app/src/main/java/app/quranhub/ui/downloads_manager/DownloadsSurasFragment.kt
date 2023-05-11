package app.quranhub.ui.downloads_manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import app.quranhub.R
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.ReciterRecitation
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.service.QuranAudioDownloaderService.Companion.downloadSura
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.Companion.newInstance
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.util.QuranAudioDeleteUtils.DeleteFinishListener
import app.quranhub.util.QuranAudioDeleteUtils.deleteSuraAudio

class DownloadsSurasFragment : BaseDownloadsFragment(), DeleteConfirmationCallbacks {

    private var recitationId = 0
    private var reciterId: String? = null
    private var reciterName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
            reciterId = it.getString(ARG_RECITER_ID)
            reciterName = it.getString(ARG_RECITER_NAME)
        }
    }

    override fun provideDisplayableDownloads(): List<DisplayableDownload> {
        val displayableDownloadList: MutableList<DisplayableDownload> = ArrayList()
        val suras = resources.getStringArray(R.array.sura_name)
        for (i in suras.indices) {
            val suraName = suras[i]
            val displayableDownload = DisplayableDownload(suraName)
            val suraId = i + 1
            val isDownloadable = UserDatabase.getInstance(requireContext())
                .quranAudioDao
                .getForSura(recitationId, reciterId, suraId)
                .isEmpty()
            displayableDownload.isDownloadable = isDownloadable
            displayableDownload.isDeletable = !isDownloadable
            displayableDownloadList.add(displayableDownload)
        }
        return displayableDownloadList
    }

    override fun onClickItem(displayableDownload: DisplayableDownload?, position: Int) {}

    override fun onDeleteItem(displayableDownload: DisplayableDownload?, position: Int) {
        val confirmationDialog = newInstance(
            getString(R.string.confirm_delete_title),
            getString(R.string.confirm_delete_description_suras), position
        )
        confirmationDialog.show(childFragmentManager, "DeleteConfirmationDialogFragment")
    }

    override fun onConfirmDelete(deletePosition: Int) {
        val suraId = deletePosition + 1
        deleteSuraAudio(
            requireContext(),
            recitationId,
            reciterId!!,
            suraId,
            object : DeleteFinishListener {
                override fun onDeleteFinish() {
                    refresh()
                }
            })
    }

    @SuppressLint("StaticFieldLeak")
    override fun onDownloadItem(displayableDownload: DisplayableDownload?, position: Int) {
        val suraId = position + 1
        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg voids: Void?): Void? {
                val userDatabase = UserDatabase.getInstance(requireContext())
                if (userDatabase.reciterDao.getById(reciterId) == null) {
//                    userDatabase.getReciterDao()
//                            .insert(new Reciter(reciterId, reciterName));
                }
                if (userDatabase.reciterRecitationDao[recitationId, reciterId] == null) {
                    userDatabase.reciterRecitationDao
                        .insert(
                            ReciterRecitation(
                                recitationId = recitationId,
                                reciterId = reciterId!!
                            )
                        )
                }
                val recitationIdPreference =
                    AppPreferencesManager.getRecitationSetting(requireContext())
                if (recitationIdPreference == recitationId) {
                    AppPreferencesManager.persistReciterSheikhSetting(requireContext(), reciterId)
                }
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                downloadSura(requireContext(), recitationId, reciterId, suraId)
                Toast.makeText(
                    requireContext(), R.string.msg_quran_audio_download_started,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.execute()
    }

    companion object {

        private val TAG = DownloadsSurasFragment::class.java.simpleName

        private const val ARG_RECITATION_ID = "ARG_RECITATION_ID"
        private const val ARG_RECITER_ID = "ARG_RECITER_ID"
        private const val ARG_RECITER_NAME = "ARG_RECITER_NAME"

        @JvmOverloads
        fun newInstance(
            context: Context,
            recitationId: Int,
            reciterId: String,
            reciterName: String,
            isEditable: Boolean = false
        ): DownloadsSurasFragment {
            val surasFragment = DownloadsSurasFragment()
            val args = Bundle()
            args.putInt(ARG_RECITATION_ID, recitationId)
            args.putString(ARG_RECITER_ID, reciterId)
            args.putString(ARG_RECITER_NAME, reciterName)
            args.putString(
                ARG_DESCRIPTION,
                context.getString(R.string.description_manage_suras_downloads)
            )
            args.putBoolean(ARG_EDITABLE, isEditable)
            surasFragment.arguments = args
            return surasFragment
        }
    }
}