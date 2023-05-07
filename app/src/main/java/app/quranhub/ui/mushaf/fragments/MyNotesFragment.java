package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import app.quranhub.R;
import app.quranhub.data.local.entity.Note;
import app.quranhub.databinding.FragmentMyNotesBinding;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.adapter.NotesAdapter;
import app.quranhub.ui.mushaf.dialogs.AddNoteDialog;
import app.quranhub.ui.mushaf.dialogs.NotesFilterDialog;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.DisplayedNote;
import app.quranhub.ui.mushaf.viewmodel.NotesViewModel;
import app.quranhub.util.ScreenUtils;

public class MyNotesFragment extends Fragment implements NotesAdapter.NoteCallback, AddNoteDialog.AddNoteListener, ItemSelectionListener<Integer> {

    private FragmentMyNotesBinding binding;

    private String inputSearch = "";
    private ToolbarActionsListener navDrawerListener;
    private QuranNavigationCallbacks quranNavigationCallbacks;
    private NotesAdapter adapter;
    private NotesViewModel viewModel;
    private int selectedFilterNoteType = 0;
    private boolean openDialog = false;
    private DisplayedNote selectedAyaNote;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener) {
            navDrawerListener = (ToolbarActionsListener) context;
        }
        if (context instanceof QuranNavigationCallbacks) {
            quranNavigationCallbacks = (QuranNavigationCallbacks) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState
    ) {
        binding = FragmentMyNotesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            getPrevState(savedInstanceState);
        }

        initViews();
        bindViewModel();
        observeSearchInput();
        attachListeners();
    }

    private void attachListeners() {
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
        binding.editBtn.setOnClickListener(v -> onNoteEdit());
        binding.filterBtn.setOnClickListener(v -> onClickFilter());
        binding.ibFinishEdit.setOnClickListener(v -> onFinishEdit());
    }

    private void getPrevState(Bundle savedInstanceState) {
        inputSearch = savedInstanceState.getString("input_search");
        selectedFilterNoteType = savedInstanceState.getInt("filter_type");
        openDialog = savedInstanceState.getBoolean("open_dialog");
        if (openDialog) {
            openDialog = false;
            selectedAyaNote = savedInstanceState.getParcelable("selected_note");
            openAddNoteDialog(selectedAyaNote);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input_search", inputSearch);
        outState.putInt("filter_type", selectedFilterNoteType);
        outState.putBoolean("open_dialog", openDialog);
        outState.putParcelable("selected_note", selectedAyaNote);
    }

    private void bindViewModel() {
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        viewModel.getAllNotes();
        viewModel.getNotes().observe(getViewLifecycleOwner(), displayedNotes -> {
            binding.progreesBar.setVisibility(View.GONE);
            if (displayedNotes != null && displayedNotes.size() > 0) {
                adapter.setNoteList(displayedNotes);
                if (inputSearch != null && !TextUtils.isEmpty(inputSearch.trim())) {
                    adapter.filter(inputSearch);
                }
                if (selectedFilterNoteType != 0) {
                    adapter.setFilteredNotes(selectedFilterNoteType - 1);
                }
            } else {
                binding.noNotesTv.setVisibility(View.VISIBLE);
            }
        });
    }

    private void observeSearchInput() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                inputSearch = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initViews() {
        adapter = new NotesAdapter(requireContext(), this);
        binding.notesRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.notesRv.setAdapter(adapter);
    }

    private void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

    @Override
    public void onNavigateToAya(int ayaId, int pageNum) {
        ScreenUtils.dismissKeyboard(requireContext(), binding.etSearch);
        quranNavigationCallbacks.gotoQuranPageAya(pageNum, ayaId, false);
    }

    /*
     Get displayedNote => if = null so this aya not has note before, else the aya has note before
     */
    @Override
    public void onGetNoteDetails(DisplayedNote displayedNote) {
        if (ScreenUtils.getOrientationState(requireActivity()) == ScreenUtils.PORTRAIT_STATE) {
            openAddNoteDialog(displayedNote);
        } else {
            openDialog = true;
            selectedAyaNote = displayedNote;
        }
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void openAddNoteDialog(DisplayedNote displayedNote) {
        Note note = new Note(displayedNote.getAyaId(), displayedNote.getNoteType(), displayedNote.getNoteText(), displayedNote.getNoteRecorderPath());
        AddNoteDialog dialog = AddNoteDialog.getInstance(note);
        dialog.show(getChildFragmentManager(), "AddNoteDialog");
    }

    @Override
    public void onDeleteNote(int ayaId) {
        viewModel.deleteNote(ayaId);
    }

    @Override
    public void onAddNote(Note note, boolean isEditable) {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Toast.makeText(getActivity(), getString(R.string.note_edited), Toast.LENGTH_LONG).show();
        adapter.updateNoteType(note);
        viewModel.updateNote(note);
    }

    @Override
    public void onDismissDialog() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void onNoteEdit() {
        adapter.setEditable(true);
        binding.ibFinishEdit.setVisibility(View.VISIBLE);
        binding.editBtn.setVisibility(View.GONE);
        binding.filterBtn.setVisibility(View.GONE);
    }

    private void onClickFilter() {
        NotesFilterDialog filterDialog = NotesFilterDialog.getInstance(selectedFilterNoteType);
        filterDialog.show(getChildFragmentManager(), "NotesFilterDialog");
    }

    private void onFinishEdit() {
        adapter.setEditable(false);
        binding.ibFinishEdit.setVisibility(View.GONE);
        binding.editBtn.setVisibility(View.VISIBLE);
        binding.filterBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSelectItem(Integer noteFilterType) {
        selectedFilterNoteType = noteFilterType;
        if (noteFilterType == 0) {
            adapter.setAllNotes();
        } else {
            adapter.setFilteredNotes(selectedFilterNoteType - 1);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
