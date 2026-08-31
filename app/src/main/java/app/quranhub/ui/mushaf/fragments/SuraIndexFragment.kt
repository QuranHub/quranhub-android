package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.databinding.FragmentSuraIndexBinding
import app.quranhub.ui.mushaf.adapter.SuraIndexAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.viewmodel.SuraIndexViewModel
import app.quranhub.util.ScreenUtils.dismissKeyboard
import kotlinx.coroutines.launch

class SuraIndexFragment : Fragment(), ItemSelectionListener<Int> {

    private var binding: FragmentSuraIndexBinding? = null

    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var viewModel: SuraIndexViewModel? = null
    private var adapter: SuraIndexAdapter? = null
    private var currentQuery: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        quranNavigationCallbacks = if (context is QuranNavigationCallbacks) {
            context
        } else {
            error(
                "The containing Activity must implement QuranNavigationCallbacks interface"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        quranNavigationCallbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuraIndexBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        currentQuery = (parentFragment as? SuraGuz2IndexFragment)?.currentSearchQuery
        viewModel = ViewModelProvider(this)[SuraIndexViewModel::class.java]
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel!!.uiState.collect { uiState ->
                        binding!!.suraIndexProgressBar.visibility =
                            if (uiState.loading) View.VISIBLE else View.GONE
                        if (!uiState.loading) {
                            adapter!!.setSuraIndexModelList(uiState.items)
                            binding!!.suraIndexRv.recycledViewPool.clear()
                            currentQuery?.let { adapter!!.filter(it) }
                        }
                    }
                }
                launch {
                    viewModel!!.suraIndexEvents.collect { event ->
                        when (event) {
                            is SuraIndexViewModel.SuraIndexEvent.NavigateToSura -> {
                                dismissKeyboard(requireActivity(), binding!!.root)
                                quranNavigationCallbacks!!.gotoQuranPage(event.page)
                            }

                            is SuraIndexViewModel.SuraIndexEvent.ShowError ->
                                Toast.makeText(activity, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun initRecycler() {
        binding!!.suraIndexRv.layoutManager = LinearLayoutManager(activity)
        adapter = SuraIndexAdapter(requireActivity(), this)
        binding!!.suraIndexRv.adapter = adapter
    }

    fun onSearchSura(inputQuery: String?) {
        currentQuery = inputQuery
        adapter!!.filter(inputQuery!!)
    }

    override fun onSelectItem(suraPage: Int) {
        viewModel!!.onSuraItemClick(suraPage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
