package app.quranhub.ui.downloads_manager.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.model.ReciterModel
import app.quranhub.databinding.DialogQuranRecitersBinding
import app.quranhub.ui.common.dialogs.OptionsListAdapter
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import app.quranhub.ui.downloads_manager.viewmodel.ReciterPickerViewModel
import app.quranhub.util.DialogUtils.adjustDialogSize
import kotlinx.coroutines.launch

/**
 * A `DialogFragment` that displays the available Quran reciters for the user to choose from.
 *
 *
 * Activities or parent fragments that shows this DialogFragment must implement the
 * [ReciterSelectionListener] interface to handle interaction events.
 * Use the [QuranRecitersDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuranRecitersDialogFragment : DialogFragment(), OptionsListAdapter.ItemClickListener {

    private var recitationId = 0
    private var selectedReciterId: String? = null
    private var binding: DialogQuranRecitersBinding? = null
    private var adapter: OptionsListAdapter? = null
    private var reciterSelectionListener: ReciterSelectionListener? = null

    private val viewModel: ReciterPickerViewModel by viewModels {
        viewModelFactory {
            initializer {
                ReciterPickerViewModel(
                    requireActivity().application, recitationId, selectedReciterId
                )
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        reciterSelectionListener = if (context is ReciterSelectionListener) {
            context
        } else if (parentFragment is ReciterSelectionListener) {
            parentFragment as ReciterSelectionListener?
        } else {
            throw RuntimeException(
                "Activities or parent fragments that shows this DialogFragment"
                        + " must implement ReciterSelectionListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
            selectedReciterId = it.getString(ARG_SELECTED_RECITER_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogQuranRecitersBinding.inflate(inflater, container, false)
        initDialogView()
        return binding!!.root
    }

    private fun initDialogView() {
        binding!!.tvMsgDownloadedRecitersOnly.visibility = View.GONE
        binding!!.tvMsgInternetConnectionFailed.visibility = View.GONE
        binding!!.btnSelect.isEnabled = false
        binding!!.btnSelect.setOnClickListener { onSelectClick() }
        binding!!.btnBack.setOnClickListener { onBackClick() }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        render(state)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is ReciterPickerViewModel.ReciterPickerEvent.NoReciters ->
                                Toast.makeText(
                                    requireContext(), R.string.no_reciters,
                                    Toast.LENGTH_SHORT
                                ).show()
                            is ReciterPickerViewModel.ReciterPickerEvent.ReciterSelected -> {
                                reciterSelectionListener!!.onReciterSelected(
                                    event.recitationId, event.reciterModel
                                )
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(state: ReciterPickerViewModel.ReciterPickerUiState) {
        val binding = binding ?: return

        binding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.tvMsgDownloadedRecitersOnly.visibility =
            if (state.showDownloadedOnlyMessage) View.VISIBLE else View.GONE
        binding.tvMsgInternetConnectionFailed.visibility =
            if (state.showNoInternetMessage) View.VISIBLE else View.GONE
        binding.btnSelect.isEnabled = state.isSelectEnabled

        if (binding.rvReciters.adapter == null && state.reciterNames.isNotEmpty()) {
            binding.rvReciters.setHasFixedSize(true)
            binding.rvReciters.layoutManager = LinearLayoutManager(
                context, RecyclerView.VERTICAL, false
            )
            binding.rvReciters.addItemDecoration(
                DividerItemDecoration(
                    context, DividerItemDecoration.VERTICAL
                )
            )
            adapter = OptionsListAdapter(state.reciterNames, state.selectedReciterIndex, this)
            binding.rvReciters.adapter = adapter
        } else {
            adapter?.setSelectedOptionIndex(state.selectedReciterIndex)
        }
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(this)
    }

    override fun onItemClick(clickedItemIndex: Int) {
        viewModel.onReciterClicked(clickedItemIndex)
    }

    private fun onBackClick() {
        dismiss()
    }

    private fun onSelectClick() {
        viewModel.onSelectClicked()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
    }

    override fun onDetach() {
        super.onDetach()
        reciterSelectionListener = null
    }

    /**
     * This interface must be implemented by activities or parent fragments that contain this
     * dialog fragment to allow an interaction in this fragment to be communicated
     * to the activity or parent fragment.
     */
    interface ReciterSelectionListener {
        fun onReciterSelected(recitationId: Int, reciterModel: ReciterModel)
    }

    companion object {

        private const val ARG_RECITATION_ID = "ARG_RECITATION_ID"
        private const val ARG_SELECTED_RECITER_ID = "ARG_SELECTED_RECITER_ID"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param recitationId      A recitation ID as in [Constants.Recitation].
         * @param selectedReciterId The current selected reciter ID.
         * @return A new instance of fragment QuranRecitersDialogFragment.
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            recitationId: Int,
            selectedReciterId: String? = null
        ): QuranRecitersDialogFragment {
            val recitersDialogFragment = QuranRecitersDialogFragment()
            val args = Bundle()
            args.putInt(ARG_RECITATION_ID, recitationId)
            args.putString(ARG_SELECTED_RECITER_ID, selectedReciterId)
            recitersDialogFragment.arguments = args
            return recitersDialogFragment
        }
    }
}
