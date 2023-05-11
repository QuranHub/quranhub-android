package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.content.pm.ActivityInfo
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

class MyNotesFragment : Fragment(), NoteCallback, AddNoteListener, ItemSelectionListener<Int?> {

    private var binding: FragmentMyNotesBinding? = null

    private var inputSearch: String? = ""
    private var navDrawerListener: ToolbarActionsListener? = null
    private var quranNavigationCallbacks: QuranNavigationCallbacks? = null
    private var adapter: NotesAdapter? = null
    private var viewModel: NotesViewModel? = null
    private var selectedFilterNoteType = 0
    private var openDialog = false
    private var selectedAyaNote: DisplayedNote? = null

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
        inputSearch = savedInstanceState.getString("input_search")
        selectedFilterNoteType = savedInstanceState.getInt("filter_type")
        openDialog = savedInstanceState.getBoolean("open_dialog")
        if (openDialog) {
            openDialog = false
            selectedAyaNote = savedInstanceState.getParcelable("selected_note")
            openAddNoteDialog(selectedAyaNote)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("input_search", inputSearch)
        outState.putInt("filter_type", selectedFilterNoteType)
        outState.putBoolean("open_dialog", openDialog)
        outState.putParcelable("selected_note", selectedAyaNote)
    }

    private fun bindViewModel() {
        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        viewModel!!.allNotes
        viewModel!!.notes.observe(viewLifecycleOwner) { displayedNotes: List<DisplayedNote> ->
            binding!!.progreesBar.visibility = View.GONE
            if (displayedNotes.isNotEmpty()) {
                adapter!!.setNoteList(displayedNotes.toMutableList())
                if (inputSearch != null && !TextUtils.isEmpty(inputSearch!!.trim { it <= ' ' })) {
                    adapter!!.filter(inputSearch!!)
                }
                if (selectedFilterNoteType != 0) {
                    adapter!!.setFilteredNotes(selectedFilterNoteType - 1)
                }
            } else {
                binding!!.noNotesTv.visibility = View.VISIBLE
            }
        }
    }

    private fun observeSearchInput() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter!!.filter(s.toString())
                inputSearch = s.toString()
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
        adapter!!.updateNoteType(note!!)
        viewModel!!.updateNote(note)
    }

    override fun onDismissDialog() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun onNoteEdit() {
        adapter!!.setEditable(true)
        binding!!.ibFinishEdit.visibility = View.VISIBLE
        binding!!.editBtn.visibility = View.GONE
        binding!!.filterBtn.visibility = View.GONE
    }

    private fun onClickFilter() {
        val filterDialog = NotesFilterDialog.getInstance(selectedFilterNoteType)
        filterDialog.show(childFragmentManager, "NotesFilterDialog")
    }

    private fun onFinishEdit() {
        adapter!!.setEditable(false)
        binding!!.ibFinishEdit.visibility = View.GONE
        binding!!.editBtn.visibility = View.VISIBLE
        binding!!.filterBtn.visibility = View.VISIBLE
    }

    override fun onSelectItem(noteFilterType: Int?) {
        noteFilterType?.let {
            selectedFilterNoteType = it
            if (it == 0) {
                adapter!!.setAllNotes()
            } else {
                adapter!!.setFilteredNotes(selectedFilterNoteType - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}