package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.local.entity.Note
import app.quranhub.databinding.FragmentMyNotesBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.adapter.NotesAdapter
import app.quranhub.ui.mushaf.adapter.NotesAdapter.NoteCallback
import app.quranhub.ui.mushaf.dialogs.AddNoteDialog.AddNoteListener
import app.quranhub.ui.mushaf.dialogs.AddNoteDialog.Companion.getInstance
import app.quranhub.ui.mushaf.dialogs.NotesFilterDialog
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.ui.mushaf.model.DisplayedNote
import app.quranhub.ui.mushaf.viewmodel.NotesViewModel
import app.quranhub.util.ScreenUtils
import app.quranhub.util.ScreenUtils.dismissKeyboard
import app.quranhub.util.ScreenUtils.getOrientationState
import kotlinx.coroutines.launch

class MyNotesFragment : Fragment(), NoteCallback, AddNoteListener, ItemSelectionListener<Int?> {

    private var binding: FragmentMyNotesBinding? = null

    private var navDrawerListener: ToolbarActionsListener? = null
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var adapter: NotesAdapter? = null
    private var viewModel: NotesViewModel? = null
    private var openDialog = false
    private var selectedAyaNote: DisplayedNote? = null
    private var lastAppliedSearchQuery: String? = null
    private var lastAppliedFilterType = -1
    private var lastAppliedEditMode: Boolean? = null
    private var lastRenderedNotes: List<DisplayedNote> = emptyList()

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyNotesBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let { getPrevState(it) }
        initViews()
        bindViewModel()
        observeSearchInput()
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
        binding!!.editBtn.setOnClickListener { onNoteEdit() }
        binding!!.filterBtn.setOnClickListener { onClickFilter() }
        binding!!.ibFinishEdit.setOnClickListener { onFinishEdit() }
    }

    private fun getPrevState(savedInstanceState: Bundle) {
        openDialog = savedInstanceState.getBoolean("open_dialog")
        if (openDialog) {
            openDialog = false
            selectedAyaNote = savedInstanceState.getParcelable("selected_note")
            openAddNoteDialog(selectedAyaNote)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("open_dialog", openDialog)
        outState.putParcelable("selected_note", selectedAyaNote)
    }

    private fun bindViewModel() {
        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel!!.uiState.collect { uiState ->
                        binding!!.progreesBar.visibility =
                            if (uiState.loading) View.VISIBLE else View.GONE
                        val notesChanged = uiState.notes != lastRenderedNotes
                        renderNotes(uiState.notes)
                        lastRenderedNotes = uiState.notes
                        applyListState(uiState.searchQuery, uiState.filterType, notesChanged)
                        applyEditMode(uiState.isEditMode)
                        binding!!.noNotesTv.visibility =
                            if (uiState.notes.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel!!.notesEvents.collect { event ->
                        when (event) {
                            is NotesViewModel.NotesEvent.ShowError ->
                                Toast.makeText(
                                    activity, event.message, Toast.LENGTH_LONG
                                ).show()
                        }
                    }
                }
            }
        }
    }

    private fun renderNotes(displayedNotes: List<DisplayedNote>) {
        adapter!!.setNoteList(displayedNotes.toMutableList())
    }

    private fun applyListState(searchQuery: String, filterType: Int, listContentChanged: Boolean) {
        if (listContentChanged || searchQuery != lastAppliedSearchQuery) {
            lastAppliedSearchQuery = searchQuery
            lastAppliedFilterType = filterType
            if (searchQuery.isNotEmpty()) {
                adapter!!.filter(searchQuery)
            } else if (filterType == 0) {
                adapter!!.setAllNotes()
            } else {
                adapter!!.setFilteredNotes(filterType - 1)
            }
        } else if (filterType != lastAppliedFilterType) {
            lastAppliedFilterType = filterType
            if (filterType == 0) {
                adapter!!.setAllNotes()
            } else {
                adapter!!.setFilteredNotes(filterType - 1)
            }
        }
    }

    private fun applyEditMode(isEditMode: Boolean) {
        if (isEditMode != lastAppliedEditMode) {
            lastAppliedEditMode = isEditMode
            adapter!!.setEditable(isEditMode)
            if (isEditMode) {
                binding!!.ibFinishEdit.visibility = View.VISIBLE
                binding!!.editBtn.visibility = View.GONE
                binding!!.filterBtn.visibility = View.GONE
            } else {
                binding!!.ibFinishEdit.visibility = View.GONE
                binding!!.editBtn.visibility = View.VISIBLE
                binding!!.filterBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun observeSearchInput() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel!!.onSearchQueryChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun initViews() {
        adapter = NotesAdapter(requireContext(), this)
        binding!!.notesRv.layoutManager = LinearLayoutManager(activity)
        binding!!.notesRv.adapter = adapter
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    override fun onNavigateToAya(ayaId: Int, pageNum: Int) {
        dismissKeyboard(requireContext(), binding!!.etSearch)
        quranNavigationCallbacks!!.gotoQuranPageAya(pageNum, ayaId, false)
    }

    /*
     Get displayedNote => if = null so this aya not has note before, else the aya has note before
     */
    override fun onGetNoteDetails(displayedNote: DisplayedNote?) {
        if (getOrientationState(requireActivity()) === ScreenUtils.PORTRAIT_STATE) {
            openAddNoteDialog(displayedNote)
        } else {
            openDialog = true
            selectedAyaNote = displayedNote
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun openAddNoteDialog(displayedNote: DisplayedNote?) {
        val note = Note(
            displayedNote!!.ayaId,
            displayedNote.noteType,
            displayedNote.noteText,
            displayedNote.noteRecorderPath
        )
        val dialog = getInstance(note)
        dialog.show(childFragmentManager, "AddNoteDialog")
    }

    override fun onDeleteNote(ayaId: Int) {
        viewModel!!.deleteNote(ayaId)
    }

    override fun onAddNote(note: Note?, isEditable: Boolean) {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        Toast.makeText(activity, getString(R.string.note_edited), Toast.LENGTH_LONG).show()
        viewModel!!.updateNote(note!!)
    }

    override fun onDismissDialog() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun onNoteEdit() {
        viewModel!!.onNoteEditClicked()
    }

    private fun onClickFilter() {
        val filterDialog = NotesFilterDialog.getInstance(viewModel!!.uiState.value.filterType)
        filterDialog.show(childFragmentManager, "NotesFilterDialog")
    }

    private fun onFinishEdit() {
        viewModel!!.onFinishEditClicked()
    }

    override fun onSelectItem(noteFilterType: Int?) {
        noteFilterType?.let {
            viewModel!!.onFilterTypeSelected(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
