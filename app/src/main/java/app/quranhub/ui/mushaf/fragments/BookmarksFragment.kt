package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.quranhub.R
import app.quranhub.databinding.FragmentBookmarksBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.viewmodel.BookmarksViewModel
import app.quranhub.util.InsetsUtils
import app.quranhub.util.ScreenUtils.dismissKeyboard
import kotlinx.coroutines.launch

class BookmarksFragment : Fragment(), QuranNavigationCallbacks {

    private var binding: FragmentBookmarksBinding? = null

    private var viewModel: BookmarksViewModel? = null
    private var navDrawerListener: ToolbarActionsListener? = null
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var bookmarksListFragment: BookmarksListFragment? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navDrawerListener = if (context is ToolbarActionsListener) {
            context
        } else {
            error(
                "The parent of this fragment must implement ToolbarActionsListener interface."
            )
        }
        quranNavigationCallbacks = if (context is QuranNavigationCallbacks) {
            context
        } else {
            error(
                "The parent of this fragment must implement QuranNavigationCallbacks interface."
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[BookmarksViewModel::class.java]
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel!!.onSearchQueryChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        InsetsUtils.padTopForStatusBar(binding!!.toolbarLayout)
        bookmarksListFragment = BookmarksListFragment.newInstance()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.list_container, bookmarksListFragment!!)
        transaction.commit()
        attachListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel!!.uiState.collect { uiState ->
                        if (uiState.isEditMode) {
                            binding!!.editBtn.visibility = View.INVISIBLE
                            binding!!.ibFinishEdit.visibility = View.VISIBLE
                            binding!!.filterBtn.visibility = View.INVISIBLE
                        } else {
                            binding!!.editBtn.visibility = View.VISIBLE
                            binding!!.ibFinishEdit.visibility = View.INVISIBLE
                            binding!!.filterBtn.visibility = View.VISIBLE
                        }
                        if (uiState.isListEditable) {
                            binding!!.editBtn.setImageResource(R.drawable.edit_gold_ic)
                        } else {
                            binding!!.editBtn.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.dark_grey
                                )
                            )
                        }
                    }
                }
                launch {
                    viewModel!!.bookmarksEvents.collect { event ->
                        when (event) {
                            is BookmarksViewModel.BookmarksEvent.ListNotEditable ->
                                showMessage(getString(R.string.msg_no_bookmarks))

                            is BookmarksViewModel.BookmarksEvent.ShowError ->
                                showMessage(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun attachListeners() {
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
        binding!!.editBtn.setOnClickListener { edit() }
        binding!!.filterBtn.setOnClickListener { filter() }
        binding!!.ibFinishEdit.setOnClickListener { finishEdit() }
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    private fun edit() {
        viewModel!!.onEditClicked()
    }

    private fun filter() {
        bookmarksListFragment!!.showFilterDialog()
    }

    private fun finishEdit() {
        viewModel!!.onFinishEditClicked()
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun gotoQuranPage(pageNumber: Int) {
        quranNavigationCallbacks!!.gotoQuranPage(pageNumber)
    }

    override fun gotoQuranPageAya(pageNumber: Int, ayaId: Int, addToBackStack: Boolean) {
        dismissKeyboard(requireActivity(), binding!!.etSearch)
        quranNavigationCallbacks!!.gotoQuranPageAya(pageNumber, ayaId, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private val TAG = BookmarksFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BookmarksFragment.
         */
        fun newInstance(): BookmarksFragment {
            return BookmarksFragment()
        }
    }
}
