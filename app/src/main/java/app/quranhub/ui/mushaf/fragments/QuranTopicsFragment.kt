package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
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
import app.quranhub.R
import app.quranhub.databinding.FragmentQuranTopicsBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.main.MainActivity
import app.quranhub.ui.mushaf.adapter.SubjectsAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.model.TopicCategory
import app.quranhub.ui.mushaf.model.TopicModel
import app.quranhub.ui.mushaf.viewmodel.SubjectsViewModel
import java.util.Locale
import kotlinx.coroutines.launch

class QuranTopicsFragment : Fragment(), ItemSelectionListener<TopicCategory> {

    private var binding: FragmentQuranTopicsBinding? = null

    private var adapter: SubjectsAdapter? = null
    private var viewModel: SubjectsViewModel? = null
    private var navDrawerListener: ToolbarActionsListener? = null
    private var topicModels: List<TopicModel?>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarActionsListener) {
            navDrawerListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuranTopicsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiRecycler()
        bindViewModel()
        attachListeners()
    }

    private fun attachListeners() {
        observeOnInputSearch()
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun filter(inputQuery: String) {
        if (inputQuery.isEmpty()) {
            adapter = SubjectsAdapter(topicModels, this)
            binding!!.topicsRv.adapter = adapter
        } else {
            val filteredList: MutableList<TopicModel?> = ArrayList()
            for (row in topicModels!!) {
                if (row!!.topicName.lowercase(Locale.getDefault()).contains(
                        inputQuery.lowercase(
                            Locale.getDefault()
                        )
                    )
                ) {
                    filteredList.add(row)
                }
            }
            adapter = SubjectsAdapter(filteredList, this)
            binding!!.topicsRv.adapter = adapter
        }
    }

    private fun bindViewModel() {
        val subjects =
            listOf(*requireActivity().resources.getStringArray(R.array.subject_name))
        val subjectsCategory =
            listOf(*requireActivity().resources.getStringArray(R.array.subject_category_name))
        viewModel = ViewModelProvider(this)[SubjectsViewModel::class.java]
        viewModel!!.getSubjects(subjects, subjectsCategory)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel!!.uiState.collect { state ->
                    binding!!.progreesBar.visibility =
                        if (state.loading) View.VISIBLE else View.GONE
                    state.subjects?.let { topicModels ->
                        this@QuranTopicsFragment.topicModels = topicModels
                        adapter = SubjectsAdapter(topicModels, this@QuranTopicsFragment)
                        binding!!.topicsRv.adapter = adapter
                    }
                }
            }
        }
    }

    private fun intiRecycler() {
        topicModels = ArrayList()
        binding!!.topicsRv.layoutManager = LinearLayoutManager(activity)
    }

    override fun onSelectItem(category: TopicCategory) {
        val activity = activity as? MainActivity
        activity?.openTopicAyasFragment(category)
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }
}