package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.databinding.FragmentBooksLibraryBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener

class BooksLibraryFragment : Fragment() {

    private var libraryFragment: LibraryFragment? = null
    private var inputSearch: String? = ""
    private var navDrawerListener: ToolbarActionsListener? = null
    private var isEditable = false

    private var binding: FragmentBooksLibraryBinding? = null

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
        binding = FragmentBooksLibraryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreSavedInstanceState(savedInstanceState)
        addFragment()
        attachListeners()
    }

    private fun attachListeners() {
        observeOnInputSearch()
        binding!!.hamburgerIv.setOnClickListener { v: View? -> onNavHamburgerClick() }
        binding!!.editBtn.setOnClickListener { v: View? -> onEditClick() }
    }

    private fun restoreSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            inputSearch = savedInstanceState.getString(STATE_INPUT_SEARCH)
            isEditable = savedInstanceState.getBoolean(STATE_EDITABLE)
            if (isEditable) {
                binding!!.editBtn.setImageResource(R.drawable.check_gold_ic)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_INPUT_SEARCH, inputSearch)
        outState.putBoolean(STATE_EDITABLE, isEditable)
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                inputSearch = s.toString()
                libraryFragment!!.search(inputSearch)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun addFragment() {
        binding!!.etSearch.text.clear()
        inputSearch = ""
        binding!!.editBtn.visibility = View.INVISIBLE
        libraryFragment =
            childFragmentManager.findFragmentByTag(FRAGMENT_LIBRARY) as LibraryFragment?
        if (libraryFragment == null) {
            libraryFragment = LibraryFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.data_container, libraryFragment!!, FRAGMENT_LIBRARY)
                .commit()
        }
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    private fun onEditClick() {
        if (isEditable) {
            binding!!.editBtn.setImageResource(R.drawable.edit_gold_ic)
        } else {
            binding!!.editBtn.setImageResource(R.drawable.check_gold_ic)
        }
        isEditable = !isEditable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val FRAGMENT_LIBRARY = "FRAGMENT_LIBRARY"
        private const val STATE_INPUT_SEARCH = "STATE_INPUT_SEARCH"
        private const val STATE_EDITABLE = "STATE_EDITABLE"
    }
}