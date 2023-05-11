package app.quranhub.ui.mushaf.fragments

import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.databinding.FragmentSearchBinding
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment.Companion.getInstance
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.adapter.SearchAdapter
import app.quranhub.ui.mushaf.dialogs.OptionDialog
import app.quranhub.ui.mushaf.dialogs.OptionDialog.Companion.getInstance
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.SearchModel
import app.quranhub.ui.mushaf.viewmodel.SearchViewModel
import app.quranhub.util.ScreenUtils.dismissKeyboard

class SearchFragment : Fragment(), ItemSelectionListener<SearchModel>,
    OptionDialog.ItemClickListener, OptionsListDialogFragment.ItemSelectionListener {

    private var binding: FragmentSearchBinding? = null

    private var isOriented = false
    private var isFilterOptionsShow = false
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var inputSearch: String? = ""
    private var navDrawerListener: ToolbarActionsListener? = null
    private var searchAdapter: SearchAdapter? = null
    private var searchViewModel: SearchViewModel? = null
    private var selectedSura = 0
    private var selectedJuz = 0
    private var selectedHezb = 0
    private var selectedQuarter = 0
    private var option: String? = null
    private var suraOptions: MutableList<String>? = null
    private var juzOptions: MutableList<String?>? = null
    private var hezbOptions: MutableList<String?>? = null
    private var quarterOptions: MutableList<String?>? = null
    private var juzSuraNumbers: List<Int>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarActionsListener) {
            navDrawerListener = context
        }
        if (context is QuranNavigationCallbacks) {
            quranNavigationCallbacks = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isOriented = true
            getPrevState(savedInstanceState)
        }
        initRecycler()
        bindViewModel()
        setViewsFromBackStack()
        attachListeners()
    }

    private fun attachListeners() {
        observeOnInputSearch()
        binding!!.ibClearSearch.setOnClickListener { clearSearch() }
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
        binding!!.filterContainer.partContainer.setOnClickListener { onClickPartFilter() }
        binding!!.filterContainer.suraContainer.setOnClickListener { onClickSuraFilter() }
        binding!!.filterContainer.hezbContainer.setOnClickListener { onClickHezbFilter() }
        binding!!.filterContainer.rob3Container.setOnClickListener { onClickQuarterFilter() }
        binding!!.moreIv.setOnClickListener { onGetMoreFilterOptions() }
    }

    private fun setViewsFromBackStack() {
        if (isFilterOptionsShow) {
            binding!!.filterContainer.root.visibility = View.VISIBLE
        }
        if (selectedSura != 0) {
            binding!!.filterContainer.suraTv.text =
                requireActivity().resources.getStringArray(R.array.sura_name)[selectedSura - 1]
        }
        if (selectedJuz != 0) {
            binding!!.filterContainer.chapterTv.text = refactorOptionText(
                requireActivity().resources.getStringArray(R.array.agza2_name)[selectedJuz - 1]
            )
        }
        if (selectedHezb != 0) {
            binding!!.filterContainer.hezbTv.text = hezbOptions!![selectedHezb]
        }
        if (selectedQuarter != 0) {
            binding!!.filterContainer.rob3Tv.text = quarterOptions!![selectedQuarter]
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("input_search", inputSearch)
        outState.putInt("selected_juz", selectedJuz)
        outState.putInt("selected_sura", selectedSura)
        outState.putInt("input_hezb", selectedHezb)
        outState.putInt("input_qurater", selectedQuarter)
    }

    @SuppressLint("CheckResult")
    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                inputSearch = s.toString()
                if (!isOriented) {
                    binding!!.progreesBar.visibility = View.VISIBLE
                    searchAya()
                } else {
                    isOriented = false
                }

                // show or hide clear button in search field
                if (TextUtils.isEmpty(s)) {
                    binding!!.ibClearSearch.visibility = View.INVISIBLE
                } else {
                    binding!!.ibClearSearch.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun clearSearch() {
        binding!!.etSearch.text.clear()
    }

    private fun searchAya() {
        if (inputSearch!!.trim { it <= ' ' }.isEmpty()) {
            clearResult()
        } else if (selectedJuz != 0 && selectedHezb != 0 && selectedQuarter != 0) {
            searchViewModel!!.searchWithSuraAndJuzAndHizbQuarter(
                inputSearch,
                selectedSura,
                selectedJuz,
                selectedHezb,
                selectedQuarter
            )
        } else if (selectedJuz != 0 && selectedHezb != 0) {
            searchViewModel!!.searchWithSuraAndJuzAndHizb(
                inputSearch,
                selectedSura,
                selectedJuz,
                selectedHezb
            )
        } else if (selectedSura != 0 && selectedJuz != 0) {
            searchViewModel!!.searchWithSuraAndJuz(inputSearch, selectedSura, selectedJuz)
        } else if (selectedSura != 0) {
            searchViewModel!!.searchWithSura(inputSearch, selectedSura)
        } else if (selectedJuz != 0) {
            searchViewModel!!.searchWithJuz(inputSearch, selectedJuz)
        } else {
            searchViewModel!!.simpleSearch(inputSearch)
        }
    }

    private fun clearResult() {
        searchAdapter!!.setSearchModels(ArrayList())
        binding!!.noresultTv.visibility = View.VISIBLE
        binding!!.progreesBar.visibility = View.GONE
    }

    private fun bindViewModel() {
        searchViewModel = ViewModelProvider(this).get(
            SearchViewModel::class.java
        )
        searchViewModel!!.search.observe(viewLifecycleOwner) { searchModels: List<SearchModel>? ->
            binding!!.progreesBar.visibility = View.GONE
            if (searchModels == null) {
                Toast.makeText(activity, getString(R.string.search_failed), Toast.LENGTH_LONG)
                    .show()
            } else if (searchModels.isEmpty()) {
                clearResult()
            } else if (inputSearch!!.trim().isNotEmpty()) {
                binding!!.noresultTv.visibility = View.GONE
                searchAdapter!!.setSearchModels(searchModels)
            }
        }
        searchViewModel!!.sura.observe(viewLifecycleOwner) { results: List<Int>? ->
            suraOptions = ArrayList()
            juzSuraNumbers = results
            juzSuras
        }
    }

    private val juzSuras: Unit
        get() {
            val surahs = listOf(*resources.getStringArray(R.array.sura_name))
            suraOptions = mutableListOf()
            for (index in juzSuraNumbers!!) {
                suraOptions!!.add(surahs[index - 1])
            }
        }

    private fun initRecycler() {
        searchAdapter = SearchAdapter(requireContext(), this)
        binding!!.searchRv.layoutManager = LinearLayoutManager(activity)
        binding!!.searchRv.adapter = searchAdapter
    }

    private fun getPrevState(savedInstanceState: Bundle) {
        inputSearch = savedInstanceState.getString("input_search")
        selectedJuz = savedInstanceState.getInt("selected_juz")
        selectedSura = savedInstanceState.getInt("selected_sura")
        selectedHezb = savedInstanceState.getInt("input_hezb")
        selectedQuarter = savedInstanceState.getInt("input_qurater")
        if (selectedJuz != 0) {
            binding!!.filterContainer.chapterTv.text = refactorOptionText(
                requireActivity().resources.getStringArray(R.array.agza2_name)[selectedJuz - 1]
            )
        }
        if (selectedSura != 0) {
            binding!!.filterContainer.suraTv.text =
                requireActivity().resources.getStringArray(R.array.sura_name)[selectedSura - 1]
        }
        if (selectedHezb != 0) {
            binding!!.filterContainer.hezbTv.text =
                requireActivity().resources.getStringArray(R.array.hezb_name)[selectedHezb - 1]
        }
        if (selectedQuarter != 0) {
            binding!!.filterContainer.rob3Tv.text =
                requireActivity().resources.getStringArray(R.array.quarter_name)[selectedQuarter - 1]
        }
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    private fun onClickPartFilter() {
        if (juzOptions == null) {
            val options = listOf(*resources.getStringArray(R.array.agza2_name))
            juzOptions = mutableListOf()
            juzOptions!!.add(getString(R.string.all_guz2))
            juzOptions!!.addAll(options)
        }
        option = if (selectedJuz == 0) {
            getString(R.string.all_guz2)
        } else {
            requireActivity().resources.getStringArray(R.array.agza2_name)[selectedJuz - 1]
        }
        val dialog = getInstance(juzOptions, option, JUZ_FILTER_CODE, getString(R.string.chapters))
        dialog.show(childFragmentManager, "JuzDialog")
    }

    private fun setSuraDialog() {
        option = if (selectedSura == 0) {
            getString(R.string.all_sura)
        } else {
            requireActivity().resources.getStringArray(R.array.sura_name)[selectedSura - 1]
        }
        val dialog = getInstance(suraOptions, option, SURA_FILTER_CODE, getString(R.string.suras))
        dialog.show(childFragmentManager, "OptionDialog")
    }

    private fun onClickSuraFilter() {
        if (selectedJuz == 0) {
            val options = listOf(*resources.getStringArray(R.array.sura_name))
            suraOptions = mutableListOf()
            suraOptions!!.add(getString(R.string.all_sura))
            suraOptions!!.addAll(options)
        }
        setSuraDialog()
    }

    private fun onClickHezbFilter() {
        if (selectedJuz == 0) {
            Toast.makeText(activity, R.string.select_juz_first, Toast.LENGTH_LONG).show()
            return
        }
        if (hezbOptions == null) {
            val options = listOf(*resources.getStringArray(R.array.hezb_name))
            hezbOptions = mutableListOf()
            hezbOptions!!.add(getString(R.string.all_hezb))
            hezbOptions!!.addAll(options)
        }
        val fragment = getInstance(
            getString(R.string.hizb),
            hezbOptions!!,
            selectedHezb,
            this,
            HEZB_FILTER_CODE
        )
        fragment.show(requireActivity().supportFragmentManager, "HizbFilterDialog")
    }

    private fun onClickQuarterFilter() {
        if (selectedHezb == 0) {
            Toast.makeText(activity, R.string.select_hezb_first, Toast.LENGTH_LONG).show()
            return
        }
        if (quarterOptions == null) {
            val options = listOf(*resources.getStringArray(R.array.quarter_name))
            quarterOptions = mutableListOf()
            quarterOptions!!.add(getString(R.string.all_quarters))
            quarterOptions!!.addAll(options)
        }
        val fragment = getInstance(
            getString(R.string.rub3),
            quarterOptions!!,
            selectedQuarter,
            this,
            QUARTER_FILTER_CODE
        )
        fragment.show(requireActivity().supportFragmentManager, "QuarterFilterDialog")
    }

    override fun onSelectItem(item: SearchModel) {
        dismissKeyboard(requireContext(), binding!!.etSearch)
        quranNavigationCallbacks!!.gotoQuranPageAya(item.page, item.id, true)
    }

    override fun onItemClick(optionName: String?, optionIndex: Int, requestCode: Int) {
        if (requestCode == SURA_FILTER_CODE) {
            binding!!.filterContainer.suraTv.text = optionName
            selectedSura = if (selectedJuz == 0) optionIndex else juzSuraNumbers!![optionIndex]
            searchAya()
        } else if (requestCode == JUZ_FILTER_CODE) {
            binding!!.filterContainer.chapterTv.text =
                if (optionIndex == 0) optionName else refactorOptionText(optionName)
            binding!!.filterContainer.suraTv.text = getString(R.string.sura)
            selectedSura = 0
            if (optionIndex == 0) {
                selectedQuarter = 0
                selectedHezb = 0
                binding!!.filterContainer.rob3Tv.text = getString(R.string.rub3)
                binding!!.filterContainer.hezbTv.text = getString(R.string.hizb)
            } else {
                searchViewModel!!.getChapterSuras(optionIndex)
            }
            if (optionIndex != selectedJuz) {
                selectedJuz = optionIndex
                searchAya()
            }
        }
    }

    private fun refactorOptionText(text: String?): String {
        return text!!.substring(text.indexOf(' ') + 1)
    }

    override fun onItemSelected(requestCode: Int, itemIndex: Int) {
        if (requestCode == HEZB_FILTER_CODE) {
            selectedHezb = itemIndex
            binding!!.filterContainer.hezbTv.text = hezbOptions!![itemIndex]
            if (selectedHezb == 0) {
                selectedQuarter = 0
                binding!!.filterContainer.rob3Tv.text = getString(R.string.rub3)
            }
            searchAya()
        } else if (requestCode == QUARTER_FILTER_CODE) {
            selectedQuarter = itemIndex
            binding!!.filterContainer.rob3Tv.text = quarterOptions!![itemIndex]
            searchAya()
        }
    }

    private fun onGetMoreFilterOptions() {
        if (isFilterOptionsShow) {
            binding!!.filterContainer.root.visibility = View.GONE
        } else {
            binding!!.filterContainer.root.visibility = View.VISIBLE
        }
        isFilterOptionsShow = !isFilterOptionsShow
    }

    companion object {
        const val SURA_FILTER_CODE = 1
        const val JUZ_FILTER_CODE = 2
        const val HEZB_FILTER_CODE = 3
        const val QUARTER_FILTER_CODE = 4
    }
}