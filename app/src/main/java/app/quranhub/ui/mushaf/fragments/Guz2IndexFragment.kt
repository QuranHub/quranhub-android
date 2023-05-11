package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.databinding.FragmentGuz2IndexBinding
import app.quranhub.ui.mushaf.adapter.Guz2IndexAdapter
import app.quranhub.ui.mushaf.adapter.Guz2IndexAdapter.IndexItemClickListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel
import app.quranhub.ui.mushaf.viewmodel.Guz2IndexViewModel
import app.quranhub.ui.mushaf.viewmodel.Guz2IndexViewModel.IndexItemClickEvent

/**
 * Fragment that displays a list containing Juz' index with its Hizb & Hizb Quarters.
 */
class Guz2IndexFragment : Fragment(), IndexItemClickListener {

    private var binding: FragmentGuz2IndexBinding? = null

    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var guz2IndexViewModel: Guz2IndexViewModel? = null
    private var adapter: Guz2IndexAdapter? = null
    private var filterGuz2 = Guz2IndexAdapter.FILTER_GUZ2_ALL

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            filterGuz2 = requireArguments().getInt(ARG_FILTER_GUZ2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuz2IndexBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            filterGuz2 =
                savedInstanceState.getInt(STATE_FILTER_GUZ2, Guz2IndexAdapter.FILTER_GUZ2_ALL)
        }
        initGuz2IndexRecyclerView()
        guz2IndexViewModel = ViewModelProvider(this).get(
            Guz2IndexViewModel::class.java
        )
        guz2IndexViewModel!!.hizbQuarterDataModelsLiveData.observe(viewLifecycleOwner) { hizbQuarterDataModels: List<HizbQuarterDataModel> ->
            Log.d(TAG, "hizbQuarterDataModels = $hizbQuarterDataModels")
            if (binding!!.guz2IndexProgressBar.visibility == View.VISIBLE) {
                binding!!.guz2IndexProgressBar.visibility = View.GONE
            }
            adapter!!.setHizbQuarterDataModels(hizbQuarterDataModels.toMutableList())
        }
        guz2IndexViewModel!!.indexItemClickEvent().observe(
            viewLifecycleOwner
        ) { indexItemClickEvent: IndexItemClickEvent ->
            quranNavigationCallbacks!!.gotoQuranPage(
                indexItemClickEvent.page
            )
        }
    }

    private fun initGuz2IndexRecyclerView() {
        binding!!.rvGuz2Index.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding!!.rvGuz2Index.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            layoutManager.orientation
        )
        binding!!.rvGuz2Index.addItemDecoration(dividerItemDecoration)
        adapter = Guz2IndexAdapter(null, filterGuz2, this)
        binding!!.rvGuz2Index.adapter = adapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_FILTER_GUZ2, filterGuz2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onIndexItemClick(model: HizbQuarterDataModel?, clickedItemIndex: Int) {
        guz2IndexViewModel!!.notifyIndexItemClick(clickedItemIndex)
    }

    fun filterForGuz2(guz2: Int) {
        filterGuz2 = guz2
        adapter!!.filter.filter(guz2.toString())
    }

    override fun onDetach() {
        super.onDetach()
        quranNavigationCallbacks = null
    }

    companion object {
        private val TAG = Guz2IndexFragment::class.java.simpleName
        private const val ARG_FILTER_GUZ2 = "ARG_FILTER_GUZ2"
        private const val STATE_FILTER_GUZ2 = "STATE_FILTER_GUZ2"

        @JvmStatic
        fun newInstance(filterGuz2: Int): Guz2IndexFragment {
            val fragment = Guz2IndexFragment()
            val args = Bundle()
            args.putInt(ARG_FILTER_GUZ2, filterGuz2)
            fragment.arguments = args
            return fragment
        }
    }
}