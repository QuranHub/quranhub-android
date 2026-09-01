package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.databinding.FragmentBookmarksListBinding
import app.quranhub.ui.mushaf.adapter.BookmarksAdapter
import app.quranhub.ui.mushaf.adapter.BookmarksAdapter.BookmarkActionListener
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog.BookmarkFilterListener
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog.Companion.getInstance
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.DisplayableBookmark
import app.quranhub.ui.mushaf.viewmodel.BookmarksViewModel
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of user saved bookmarked Quran ayas.
 * Use the [BookmarksListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookmarksListFragment : Fragment(), BookmarkActionListener, BookmarkFilterListener {

    private var bookMarksViewModel: BookmarksViewModel? = null
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var editedBookmarkId = -1
    private var binding: FragmentBookmarksListBinding? = null
    private var adapter: BookmarksAdapter? = null
    private var lastAppliedSearchQuery: String? = null
    private var lastAppliedFilterType = -1
    private var lastAppliedEditMode: Boolean? = null
    private var lastRenderedBookmarks: List<DisplayableBookmark> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is QuranNavigationCallbacks) {
            quranNavigationCallbacks = parentFragment as QuranNavigationCallbacks?
        } else {
            error(
                "${requireParentFragment().javaClass.simpleName} must implement QuranNavigationCallbacks interface."
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarksListBinding.inflate(inflater, container, false)
        setupBookmarksRecyclerView()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
    }

    private fun bindViewModel() {
        // Share the host screen's ViewModel (dialogs are UI shells over the same one)
        bookMarksViewModel = ViewModelProvider(requireParentFragment())[
            BookmarksViewModel::class.java
        ]
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    bookMarksViewModel!!.uiState.collect { uiState ->
                        binding!!.loadingIndicator.visibility =
                            if (uiState.loading) View.VISIBLE else View.GONE
                        val bookmarksChanged = uiState.bookmarks != lastRenderedBookmarks
                        renderBookmarks(uiState.bookmarks)
                        lastRenderedBookmarks = uiState.bookmarks
                        applyListState(uiState.searchQuery, uiState.filterType, bookmarksChanged)
                        applyEditMode(uiState.isEditMode)
                        binding!!.tvEmptyListMsg.visibility =
                            if (uiState.bookmarks.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun renderBookmarks(displayableBookmarks: List<DisplayableBookmark>) {
        adapter!!.setBookmarks(displayableBookmarks.toMutableList())
    }

    private fun applyListState(searchQuery: String, filterType: Int, listContentChanged: Boolean) {
        if (listContentChanged || searchQuery != lastAppliedSearchQuery) {
            lastAppliedSearchQuery = searchQuery
            lastAppliedFilterType = filterType
            adapter!!.filter.filter(searchQuery)
            if (searchQuery.isEmpty() && filterType != BookmarkEditDialog.ALL_BOOKMARK_FILTER) {
                adapter!!.filterBookmarks(filterType)
            }
        } else if (filterType != lastAppliedFilterType) {
            lastAppliedFilterType = filterType
            adapter!!.filterBookmarks(filterType)
        }
    }

    private fun applyEditMode(isEditMode: Boolean) {
        if (isEditMode != lastAppliedEditMode) {
            lastAppliedEditMode = isEditMode
            adapter!!.setEditable(isEditMode)
        }
    }

    private fun setupBookmarksRecyclerView() {
        binding!!.bookmarksRv.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding!!.bookmarksRv.setHasFixedSize(true)
        binding!!.bookmarksRv.layoutManager = LinearLayoutManager(context)
        adapter = BookmarksAdapter(requireContext(), this)
        binding!!.bookmarksRv.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onSelectItem(displayableBookmark: DisplayableBookmark?) {
        displayableBookmark?.let {
            quranNavigationCallbacks!!.gotoQuranPageAya(
                it.pageNumber,
                it.ayaId,
                false
            )
        }

    }

    fun showFilterDialog() {
        val bookmarkTypes: List<BookmarkType> = bookMarksViewModel!!.uiState.value.bookmarkTypes
        if (bookmarkTypes.isNotEmpty()) {
            val dialog = getInstance(
                bookmarkTypes, bookMarksViewModel!!.uiState.value.filterType, false
            )
            dialog.show(childFragmentManager, "BookmarkEditDialog")
        }
    }

    override fun deleteBookmark(displayableBookmark: DisplayableBookmark) {
        bookMarksViewModel!!.deleteBookmark(displayableBookmark.bookmarkId)
    }

    override fun updateBookmarkType(bookmarkId: Int) {
        val bookmarkTypes: List<BookmarkType> = bookMarksViewModel!!.uiState.value.bookmarkTypes
        if (bookmarkTypes.isNotEmpty()) {
            editedBookmarkId = bookmarkId
            val dialog = getInstance(bookmarkTypes, bookmarkId, true)
            dialog.show(childFragmentManager, "BookmarkEditDialog")
        }
    }

    override fun onBookmarkFilter(bookmarkType: Int, colorIndex: Int) {
        if (editedBookmarkId == -1) {
            bookMarksViewModel!!.onFilterTypeSelected(bookmarkType)
        } else {
            bookMarksViewModel!!.changeBookmarkType(editedBookmarkId, bookmarkType)
            editedBookmarkId = -1
        }
    }

    companion object {
        private val TAG = BookmarksListFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment [BookmarksListFragment].
         */
        fun newInstance(): BookmarksListFragment {
            return BookmarksListFragment()
        }
    }
}
