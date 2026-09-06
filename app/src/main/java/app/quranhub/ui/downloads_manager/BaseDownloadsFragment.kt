package app.quranhub.ui.downloads_manager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.data.service.DownloadFinishedHolder
import app.quranhub.databinding.FragmentDownloadsBinding
import app.quranhub.ui.downloads_manager.BaseDownloadsFragment.DownloadsManagerNavigationCallbacks
import app.quranhub.ui.downloads_manager.adapters.DownloadsAdapter
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.ui.downloads_manager.viewmodel.BaseDownloadsViewModel
import kotlinx.coroutines.launch

/**
 * Base for downloads screen fragments.
 *
 *
 * Args (optional):  (ARG_DESCRIPTION -> String), (ARG_EDITABLE -> boolean)
 *
 *
 *
 * Activities or parent fragments containing a subclass of [BaseDownloadsFragment]
 * must implement [DownloadsManagerNavigationCallbacks] interface.
 *
 */
abstract class BaseDownloadsFragment : Fragment(), Editable, DownloadsAdapter.ItemClickListener {

    /** The screen's ViewModel, providing the [DisplayableDownload] listing state. */
    protected abstract val viewModel: BaseDownloadsViewModel

    private var binding: FragmentDownloadsBinding? = null

    protected var downloadsAdapter: DownloadsAdapter? = null
        private set
    private var description: String? = null
    private var editable = false

    protected var navigationCallbacks: DownloadsManagerNavigationCallbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationCallbacks = if (context is DownloadsManagerNavigationCallbacks) {
            context
        } else if (parentFragment is DownloadsManagerNavigationCallbacks) {
            parentFragment as DownloadsManagerNavigationCallbacks?
        } else {
            throw RuntimeException(
                "Activities or parent fragments containing a subclass of " +
                        "BaseDownloadsFragment must implement DownloadsManagerNavigationCallbacks interface."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            description = it.getString(ARG_DESCRIPTION)
            editable = it.getBoolean(ARG_EDITABLE, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            editable = savedInstanceState.getBoolean(STATE_EDITABLE)
        }
        setupDescription()
        setupDownloadsRecyclerView()
        observeViewModel()
    }

    private fun setupDescription() {
        if (description != null) {
            binding!!.tvDescription.text = description
        } else {
            binding!!.tvDescription.visibility = View.GONE
        }
    }

    private fun setupDownloadsRecyclerView() {
        binding!!.rvDownloads.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvDownloads.layoutManager = layoutManager

        // add dividers between RecyclerView items
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            layoutManager.orientation
        )
        binding!!.rvDownloads.addItemDecoration(dividerItemDecoration)
        downloadsAdapter = DownloadsAdapter(emptyList<DisplayableDownload>(), this, editable)
        binding!!.rvDownloads.adapter = downloadsAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding!!.progressBar.visibility =
                            if (state.loading) View.VISIBLE else View.GONE
                        if (!state.loading) {
                            Log.d(TAG, "Provided displayableDownloads=${state.downloads}")
                            downloadsAdapter!!.setDisplayableDownloads(state.downloads)
                        }
                    }
                }
                launch {
                    // refresh whenever a downloader service finishes its downloads
                    DownloadFinishedHolder.finished.collect { viewModel.refresh() }
                }
            }
        }
    }

    fun getEditable(): Boolean {
        return editable
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_EDITABLE, editable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override var isEditable: Boolean
        get() = editable
        set(value) {
            editable = value
            downloadsAdapter?.setEdit(editable)
        }

    /**
     * Activities or parent fragments containing a subclass of [BaseDownloadsFragment]
     * must implement this interface.
     */
    interface DownloadsManagerNavigationCallbacks {
        fun gotoDownloadsRecitations()
        fun gotoDownloadsReciters(recitationId: Int)
        fun gotoDownloadsSuras(recitationId: Int, reciterId: String, reciterName: String)
        fun openRecitersDialog(recitationId: Int)
        fun openAudioDownloadAmountDialog(recitationId: Int, reciterId: String)
    }

    companion object {
        private val TAG = BaseDownloadsFragment::class.java.simpleName

        const val ARG_DESCRIPTION = "ARG_DESCRIPTION"
        const val ARG_EDITABLE = "ARG_EDITABLE"

        private const val STATE_EDITABLE = "STATE_EDITABLE"
    }
}
