package app.quranhub.ui.downloads_manager

import android.content.Context
import android.os.Bundle
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.Companion.newInstance
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.util.QuranAudioDeleteUtils.DeleteFinishListener
import app.quranhub.util.QuranAudioDeleteUtils.deleteRecitationAudio

class DownloadsRecitationsFragment : BaseDownloadsFragment(), DeleteConfirmationCallbacks {

    override fun provideDisplayableDownloads(): List<DisplayableDownload> {

        val downloads: MutableList<DisplayableDownload> = ArrayList()

        for (recitationStringResId in Constants.Recitation.NAMES_STR_IDS) {
            downloads.add(DisplayableDownload(getString(recitationStringResId)))
        }

        // check if recitations are downloadable and/or deletable & the number of downloaded reciters each.
        for (i in downloads.indices) {
            val displayableDownload = downloads[i]
            val numOfDownloadedReciters = UserDatabase.getInstance(requireContext())
                .reciterRecitationDao
                .getNumOfRecitersWithDownloads(i)
            displayableDownload.downloadedAmount =
                getString(R.string.downloaded_reciters_num, numOfDownloadedReciters)
            displayableDownload.isDeletable = numOfDownloadedReciters > 0
            displayableDownload.isDownloadable = true // TODO check if it's not downloadable
        }
        return downloads
    }

    override fun onClickItem(displayableDownload: DisplayableDownload?, position: Int) {
        navigationCallbacks!!.gotoDownloadsReciters(position)
    }

    override fun onDeleteItem(displayableDownload: DisplayableDownload?, position: Int) {
        val confirmationDialog = newInstance(
            getString(R.string.confirm_delete_title),
            getString(R.string.confirm_delete_description_recitations), position
        )
        confirmationDialog.show(childFragmentManager, "DeleteConfirmationDialogFragment")
    }

    override fun onConfirmDelete(deletePosition: Int) {
        deleteRecitationAudio(
            requireContext(),
            deletePosition,
            object : DeleteFinishListener {
                override fun onDeleteFinish() {
                    refresh()
                }
            })
    }

    override fun onDownloadItem(displayableDownload: DisplayableDownload?, position: Int) {
        navigationCallbacks!!.openRecitersDialog(position)
    }

    companion object {
        private val TAG = DownloadsRecitationsFragment::class.java.simpleName

        @JvmOverloads
        fun newInstance(
            context: Context,
            isEditable: Boolean = false
        ): DownloadsRecitationsFragment {
            val recitationsFragment = DownloadsRecitationsFragment()
            val args = Bundle()
            args.putString(
                ARG_DESCRIPTION,
                context.getString(R.string.description_manage_recitations_downloads)
            )
            args.putBoolean(ARG_EDITABLE, isEditable)
            recitationsFragment.arguments = args
            return recitationsFragment
        }
    }
}