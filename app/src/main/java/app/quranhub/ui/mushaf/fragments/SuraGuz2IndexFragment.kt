package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.databinding.FragmentSuraGuz2IndexBinding
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment.Companion.getInstance
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.adapter.Guz2IndexAdapter
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper
import app.quranhub.ui.mushaf.presenter.SuraGuz2IndexPresenter
import app.quranhub.ui.mushaf.presenter.SuraGuz2IndexPresenterImp
import app.quranhub.ui.mushaf.view.SuraGuz2IndexView
import app.quranhub.util.ScreenUtils.dismissKeyboard
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import java.util.Arrays

class SuraGuz2IndexFragment : Fragment(), SuraGuz2IndexView,
    OptionsListDialogFragment.ItemSelectionListener {

    private var toolbarActionsListener: ToolbarActionsListener? = null
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var selectedTab = SURA_INDEX_TAB
    private var suraIndexFragment: SuraIndexFragment? = null
    private var guz2IndexFragment: Guz2IndexFragment? = null
    private var presenter: SuraGuz2IndexPresenter<*>? = null
    private var inputSearch: String? = ""
    private var selectedGUZ2Filter = Guz2IndexAdapter.FILTER_GUZ2_ALL

    private var binding: FragmentSuraGuz2IndexBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarActionsListener && context is QuranNavigationCallbacks) {
            toolbarActionsListener = context
            quranNavigationCallbacks = context
        } else {
            error(
                "The parent activity must implement ToolbarActionsListener" +
                        " & QuranNavigationCallbacks interfaces."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            selectedTab = requireArguments().getInt(ARG_SELECTED_TAB)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuraGuz2IndexBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreSavedInstanceState(savedInstanceState)
        initPresenter()
        addIndexFragment(selectedTab)
        attachListeners()
    }

    private fun attachListeners() {
        listenOnSelectedTab()
        observeOnInputSearch()
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
        binding!!.filterBtn.setOnClickListener { onFilterButtonClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        toolbarActionsListener = null
        quranNavigationCallbacks = null
    }

    private fun initPresenter() {
        presenter = SuraGuz2IndexPresenterImp<SuraGuz2IndexView>(requireContext())
        (presenter as? SuraGuz2IndexPresenterImp<SuraGuz2IndexView>)?.onAttach(this)
    }

    private fun restoreSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_SELECTED_TAB)
            inputSearch = savedInstanceState.getString(STATE_INPUT_SEARCH)
            selectedGUZ2Filter = savedInstanceState.getInt(STATE_SELECTED_GUZ2_FILTER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_TAB, selectedTab)
        outState.putString(STATE_INPUT_SEARCH, inputSearch)
        outState.putInt(STATE_SELECTED_GUZ2_FILTER, selectedGUZ2Filter)
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding!!.tabLayout.selectedTabPosition == SURA_INDEX_TAB && suraIndexFragment != null) {
                    inputSearch = s.toString()
                    suraIndexFragment!!.onSearchSura(inputSearch)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun listenOnSelectedTab() {
        binding!!.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                addIndexFragment(binding!!.tabLayout.selectedTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun addIndexFragment(tab: Int) {
        selectedTab = tab
        binding!!.tabLayout.getTabAt(selectedTab)!!.select()
        if (tab == SURA_INDEX_TAB) {
            suraIndexFragment =
                childFragmentManager.findFragmentByTag(FRAGMENT_SURA_INDEX) as SuraIndexFragment?
            if (suraIndexFragment == null) {
                suraIndexFragment = SuraIndexFragment()
                childFragmentManager.beginTransaction()
                    .replace(R.id.index_container, suraIndexFragment!!, FRAGMENT_SURA_INDEX)
                    .commit()
            }
            binding!!.filterBtn.visibility = View.INVISIBLE
            binding!!.etSearch.visibility = View.VISIBLE
            presenter!!.getSuraIndex()
        } else if (tab == GUZ2_INDEX_TAB) {
            guz2IndexFragment =
                childFragmentManager.findFragmentByTag(FRAGMENT_GUZ2_INDEX) as Guz2IndexFragment?
            if (guz2IndexFragment == null) {
                guz2IndexFragment = Guz2IndexFragment.newInstance(selectedGUZ2Filter)
                childFragmentManager.beginTransaction()
                    .replace(R.id.index_container, guz2IndexFragment!!, FRAGMENT_GUZ2_INDEX)
                    .commit()
            }
            binding!!.filterBtn.visibility = View.VISIBLE
            binding!!.etSearch.text.clear()
            inputSearch = ""
            binding!!.etSearch.visibility = View.GONE
        }
    }

    private fun onNavHamburgerClick() {
        toolbarActionsListener!!.onNavDrawerClick()
    }

    private fun onFilterButtonClick() {
        if (binding!!.tabLayout.selectedTabPosition == GUZ2_INDEX_TAB && guz2IndexFragment != null) {
            val guz2Options: MutableList<String?> = ArrayList()
            guz2Options.add(getString(R.string.all_guz2))
            guz2Options.addAll(Arrays.asList(*resources.getStringArray(R.array.agza2_name)))
            val guz2Dialog = getInstance(
                getString(R.string.title_options_dialog_filter_guz2_index),
                guz2Options, selectedGUZ2Filter, this, RC_GUZ2_FILTER
            )
            guz2Dialog.show(parentFragmentManager, "guz2Dialog")
        }
    }

    fun navigateToSelectedSura(suraPage: Int) {
        dismissKeyboard(requireActivity(), binding!!.etSearch)
        quranNavigationCallbacks!!.gotoQuranPage(suraPage)
    }

    override fun onGetIndex(indexList: List<SuraIndexModelMapper>) {
        suraIndexFragment!!.setAdapterData(indexList)
        if (!TextUtils.isEmpty(inputSearch)) {
            suraIndexFragment!!.onSearchSura(inputSearch)
        }
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    override fun showLoading() {
        binding!!.progreesBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding!!.progreesBar.visibility = View.GONE
    }

    override fun onItemSelected(requestCode: Int, itemIndex: Int) { // filter dialog callback
        if (requestCode == RC_GUZ2_FILTER && guz2IndexFragment != null) {
            selectedGUZ2Filter = itemIndex
            guz2IndexFragment!!.filterForGuz2(selectedGUZ2Filter)
        }
    }

    companion object {
        private val TAG = SuraGuz2IndexFragment::class.java.simpleName

        private const val ARG_SELECTED_TAB = "ARG_SELECTED_TAB"
        private const val STATE_SELECTED_TAB = "STATE_SELECTED_TAB"
        private const val STATE_INPUT_SEARCH = "STATE_INPUT_SEARCH"
        private const val STATE_SELECTED_GUZ2_FILTER = "STATE_SELECTED_GUZ2_FILTER"
        private const val FRAGMENT_SURA_INDEX = "FRAGMENT_SURA_INDEX"
        private const val FRAGMENT_GUZ2_INDEX = "FRAGMENT_GUZ2_INDEX"

        const val SURA_INDEX_TAB = 0
        const val GUZ2_INDEX_TAB = 1
        private const val RC_GUZ2_FILTER = 0

        fun newInstance(selectedTab: Int): SuraGuz2IndexFragment {
            val fragment = SuraGuz2IndexFragment()
            val args = Bundle()
            args.putInt(ARG_SELECTED_TAB, selectedTab)
            fragment.arguments = args
            return fragment
        }
    }
}