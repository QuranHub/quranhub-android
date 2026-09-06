package app.quranhub.ui.downloads_manager

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.quranhub.R
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.Companion.newInstance
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.ui.downloads_manager.viewmodel.DownloadsRecitationsViewModel

class DownloadsRecitationsFragment : BaseDownloadsFragment(), DeleteConfirmationCallbacks {

    override val viewModel: DownloadsRecitationsViewModel by viewModels {
        viewModelFactory {
            initializer {
                DownloadsRecitationsViewModel(requireActivity().application)
            }
        }
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
        viewModel.deleteRecitation(deletePosition)
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
