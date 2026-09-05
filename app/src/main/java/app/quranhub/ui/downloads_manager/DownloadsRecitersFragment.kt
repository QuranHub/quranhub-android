package app.quranhub.ui.downloads_manager

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.quranhub.R
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.Companion.newInstance
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.ui.downloads_manager.viewmodel.BaseDownloadsViewModel
import app.quranhub.ui.downloads_manager.viewmodel.DownloadsRecitersViewModel
import kotlinx.coroutines.launch

class DownloadsRecitersFragment : BaseDownloadsFragment(), DeleteConfirmationCallbacks {

    private var recitationId = 0

    override val viewModel: DownloadsRecitersViewModel by viewModels {
        viewModelFactory {
            initializer {
                DownloadsRecitersViewModel(requireActivity().application, recitationId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEvents()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is BaseDownloadsViewModel.DownloadsEvent.OpenAudioDownloadAmountDialog ->
                            navigationCallbacks!!.openAudioDownloadAmountDialog(
                                event.recitationId, event.reciterId
                            )
                        is BaseDownloadsViewModel.DownloadsEvent.DownloadStarted -> {}
                    }
                }
            }
        }
    }

    override fun onClickItem(displayableDownload: DisplayableDownload?, position: Int) {
        val reciter = viewModel.reciterAt(position)
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
        viewModel.deleteReciter(deletePosition)
    }

    override fun onDownloadItem(displayableDownload: DisplayableDownload?, position: Int) {
        viewModel.onDownloadItem(position)
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
