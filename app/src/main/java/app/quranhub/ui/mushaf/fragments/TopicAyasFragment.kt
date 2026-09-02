package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.databinding.FragmentTopicAyasBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.adapter.SearchAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.SearchModel
import app.quranhub.ui.mushaf.model.TopicCategory
import app.quranhub.ui.mushaf.viewmodel.TopicViewModel
import app.quranhub.util.ScreenUtils.dismissKeyboard
import kotlinx.coroutines.launch

class TopicAyasFragment : Fragment(), ItemSelectionListener<SearchModel> {

    private var binding: FragmentTopicAyasBinding? = null

    private var inputSearch: String? = ""
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var navDrawerListener: ToolbarActionsListener? = null
    private var adapter: SearchAdapter? = null
    private var viewModel: TopicViewModel? = null
    private var category: TopicCategory? = null

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
        binding = FragmentTopicAyasBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        getPrevState(savedInstanceState)
        intiRecycler()
        bindViewModel()
        attachListeners()
    }

    private fun attachListeners() {
        observeOnInputSearch()
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
    }

    private fun getPrevState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            inputSearch = savedInstanceState.getString("input_search")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("input_search", inputSearch)
    }

    private fun setViews() {
        category = requireArguments().getParcelable(CATEGORY_ARGS)
        binding!!.topicTv.text = category?.categoryName
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                inputSearch = s.toString()
                adapter!!.filter(inputSearch!!)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun bindViewModel() {
        viewModel = ViewModelProvider(this)[TopicViewModel::class.java]
        viewModel!!.getAyas(category!!.categoryId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel!!.uiState.collect { state ->
                    binding!!.progreesBar.visibility =
                        if (state.loading) View.VISIBLE else View.GONE
                    state.ayahs?.let { ayahs ->
                        adapter!!.setSearchModels(ayahs)
                        if (inputSearch != null && !TextUtils.isEmpty(inputSearch!!.trim { it <= ' ' })) {
                            adapter!!.filter(inputSearch!!)
                        }
                    }
                }
            }
        }
    }

    private fun intiRecycler() {
        binding!!.topicsRv.layoutManager = LinearLayoutManager(activity)
        adapter = SearchAdapter(requireActivity(), this)
        binding!!.topicsRv.adapter = adapter
    }

    override fun onSelectItem(item: SearchModel) {
        dismissKeyboard(requireContext(), binding!!.etSearch)
        quranNavigationCallbacks!!.gotoQuranPageAya(item.page, item.id, false)
    }

    fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    companion object {
        private const val CATEGORY_ARGS = "CATEGORY_ARGS"
        fun getInstance(category: TopicCategory?): TopicAyasFragment {
            val bundle = Bundle()
            bundle.putParcelable(CATEGORY_ARGS, category)
            val fragment = TopicAyasFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}