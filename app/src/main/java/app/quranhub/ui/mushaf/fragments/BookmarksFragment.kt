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
import app.quranhub.R
import app.quranhub.databinding.FragmentBookmarksBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.listener.BookmarksListListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.presenter.BookmarksPresenter
import app.quranhub.ui.mushaf.presenter.BookmarksPresenterImp
import app.quranhub.ui.mushaf.view.BookmarksView
import app.quranhub.util.ScreenUtils.dismissKeyboard

class BookmarksFragment : Fragment(), BookmarksView, BookmarksListListener,
    QuranNavigationCallbacks {

    private var binding: FragmentBookmarksBinding? = null

    private var presenter: BookmarksPresenter<BookmarksView>? = null
    private var navDrawerListener: ToolbarActionsListener? = null
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var isListEditable = true
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
        presenter = BookmarksPresenterImp()
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                (presenter as BookmarksPresenterImp).searchList(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookmarksListFragment = BookmarksListFragment.newInstance()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.list_container, bookmarksListFragment!!)
        transaction.commit()
        presenter!!.onAttach(this)
        attachListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter!!.onDetach()
        binding = null
    }

    private fun attachListeners() {
        binding!!.hamburgerIv.setOnClickListener { v: View? -> onNavHamburgerClick() }
        binding!!.editBtn.setOnClickListener { v: View? -> edit() }
        binding!!.filterBtn.setOnClickListener { v: View? -> filter() }
        binding!!.ibFinishEdit.setOnClickListener { v: View? -> finishEdit() }
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    private fun edit() {
        if (isListEditable) {
            presenter!!.enableEditList()
        } else {
            showMessage(getString(R.string.msg_no_bookmarks))
        }
    }

    private fun filter() {
        presenter!!.filterList()
    }

    private fun finishEdit() {
        presenter!!.finishEditList()
    }

    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() { /* there's no need for this here */
    }

    override fun hideLoading() { /* there's no need for this here */
    }

    override fun enableEditList() {
        binding!!.editBtn.visibility = View.INVISIBLE
        binding!!.ibFinishEdit.visibility = View.VISIBLE
        binding!!.filterBtn.visibility = View.INVISIBLE
        bookmarksListFragment!!.setEditBookmarks(true)
    }

    override fun finishEditList() {
        binding!!.editBtn.visibility = View.VISIBLE
        binding!!.ibFinishEdit.visibility = View.INVISIBLE
        binding!!.filterBtn.visibility = View.VISIBLE
        bookmarksListFragment!!.setEditBookmarks(false)
    }

    override fun filterList() {
        bookmarksListFragment!!.showFilterDialog()
    }

    override fun searchList(text: String) {
        bookmarksListFragment!!.searchBookmarks(text)
    }

    override fun onEditabilityChange(isEditable: Boolean) {
        isListEditable = isEditable

        // disable/enable the edit image button
        if (isEditable) {
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

    override fun gotoQuranPage(pageNumber: Int) {
        quranNavigationCallbacks!!.gotoQuranPage(pageNumber)
    }

    override fun gotoQuranPageAya(pageNumber: Int, ayaId: Int, addToBackStack: Boolean) {
        dismissKeyboard(requireActivity(), binding!!.etSearch)
        quranNavigationCallbacks!!.gotoQuranPageAya(pageNumber, ayaId, false)
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