package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.databinding.FragmentBookmarksListBinding
import app.quranhub.ui.mushaf.adapter.BookmarksAdapter
import app.quranhub.ui.mushaf.adapter.BookmarksAdapter.BookmarkActionListener
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog.BookmarkFilterListener
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog.Companion.getInstance
import app.quranhub.ui.mushaf.listener.BookmarksListListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.DisplayableBookmark
import app.quranhub.ui.mushaf.viewmodel.BookmarksListViewModel

/**
 * A fragment representing a list of user saved bookmarked Quran ayas.
 * Use the [BookmarksListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookmarksListFragment : Fragment(), BookmarkActionListener, BookmarkFilterListener {

    private var bookMarksViewModel: BookmarksListViewModel? = null
    private var bookmarkFilterDialog: DialogFragment? = null
    private var bookmarksListener: BookmarksListListener? = null
    private var bookmarkTypes: List<BookmarkType?>? = null
    private var editedBookmarkId = -1
    private var binding: FragmentBookmarksListBinding? = null
    private var adapter: BookmarksAdapter? = null
    private var selectedFilterType = BookmarkEditDialog.ALL_BOOKMARK_FILTER
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is QuranNavigationCallbacks
            && parentFragment is BookmarksListListener
        ) {
            quranNavigationCallbacks = parentFragment as QuranNavigationCallbacks?
            bookmarksListener = parentFragment as BookmarksListListener?
        } else {
            error(
                "${requireParentFragment().javaClass.simpleName} must implement QuranNavigationCallbacks & BookmarksListener interfaces."
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarksListBinding.inflate(inflater, container, false)
        setupBookmarksRecyclerView()
        bookmarkFilterDialog = BookmarkEditDialog()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
    }

    private fun bindViewModel() {
        bookMarksViewModel = ViewModelProvider(this).get(
            BookmarksListViewModel::class.java
        )
        bookMarksViewModel!!.getBookmarkTypes()
        bookMarksViewModel!!.bookmarks.observe(viewLifecycleOwner) { ayaBookmarks: List<DisplayableBookmark?> ->
            bookmarksListener!!.onEditabilityChange(ayaBookmarks.isNotEmpty())
            bookMarksViewModel!!.bookmarksMapper(ayaBookmarks) { displayableBookmarks: List<DisplayableBookmark> ->
                if (binding!!.loadingIndicator.visibility == View.VISIBLE) {
                    binding!!.loadingIndicator.visibility = View.GONE
                }
                adapter!!.setBookmarks(displayableBookmarks.toMutableList())
                if (displayableBookmarks.isNotEmpty()) {
                    binding!!.tvEmptyListMsg.visibility = View.GONE
                } else {
                    binding!!.tvEmptyListMsg.visibility = View.VISIBLE
                }
            }
        }
        bookMarksViewModel!!.bookmarksTypes.observe(viewLifecycleOwner) { bookmarkTypes: List<BookmarkType?>? ->
            binding!!.loadingIndicator.visibility = View.GONE
            this.bookmarkTypes = bookmarkTypes
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

    fun setEditBookmarks(isEditable: Boolean) {
        adapter!!.setEditable(isEditable)
    }

    fun showFilterDialog() {
        if (bookmarkTypes != null) {
            val dialog = getInstance(bookmarkTypes, selectedFilterType, false)
            dialog.show(childFragmentManager, "BookmarkEditDialog")
        }
    }

    fun searchBookmarks(text: String?) {
        adapter!!.filter.filter(text)
    }

    override fun deleteBookmark(displayableBookmark: DisplayableBookmark) {
        bookMarksViewModel!!.deleteBookmark(displayableBookmark.bookmarkId)
        adapter!!.deleteBookmark(displayableBookmark.ayaId)
    }

    override fun updateBookmarkType(bookmarkId: Int) {
        if (bookmarkTypes != null) {
            editedBookmarkId = bookmarkId
            val dialog = getInstance(bookmarkTypes, bookmarkId, true)
            dialog.show(childFragmentManager, "BookmarkEditDialog")
        }
    }

    override fun onBookmarkFilter(bookmarkType: Int, colorIndex: Int) {
        if (editedBookmarkId == -1) {
            selectedFilterType = bookmarkType
            adapter!!.filterBookmarks(bookmarkType)
        } else {
            bookMarksViewModel!!.changeBookmarkType(editedBookmarkId, bookmarkType)
            adapter!!.editBookmark(editedBookmarkId, bookmarkType, colorIndex)
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