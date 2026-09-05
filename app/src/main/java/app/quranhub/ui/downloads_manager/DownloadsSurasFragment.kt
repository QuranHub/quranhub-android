package app.quranhub.ui.downloads_manager

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import app.quranhub.ui.downloads_manager.viewmodel.DownloadsSurasViewModel
import kotlinx.coroutines.launch

class DownloadsSurasFragment : BaseDownloadsFragment(), DeleteConfirmationCallbacks {

    private var recitationId = 0
    private var reciterId: String? = null
    private var reciterName: String? = null

    override val viewModel: DownloadsSurasViewModel by viewModels {
        viewModelFactory {
            initializer {
                DownloadsSurasViewModel(
                    requireActivity().application, recitationId, reciterId!!
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
            reciterId = it.getString(ARG_RECITER_ID)
            reciterName = it.getString(ARG_RECITER_NAME)
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
                        is BaseDownloadsViewModel.DownloadsEvent.DownloadStarted ->
                            Toast.makeText(
                                requireContext(), R.string.msg_quran_audio_download_started,
                                Toast.LENGTH_SHORT
                            ).show()
                        is BaseDownloadsViewModel.DownloadsEvent.OpenAudioDownloadAmountDialog -> {}
                    }
                }
            }
        }
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
        viewModel.deleteSura(deletePosition)
    }

    override fun onDownloadItem(displayableDownload: DisplayableDownload?, position: Int) {
        viewModel.downloadSura(position)
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
