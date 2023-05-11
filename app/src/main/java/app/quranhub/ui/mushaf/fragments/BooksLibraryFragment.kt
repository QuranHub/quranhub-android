package app.quranhub.ui.mushaf.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import app.quranhub.R;
import app.quranhub.databinding.FragmentBooksLibraryBinding;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;

public class BooksLibraryFragment extends Fragment {

    private static final String FRAGMENT_BOOKS = "FRAGMENT_BOOKS";
    private static final String FRAGMENT_LIBRARY = "FRAGMENT_LIBRARY";
    private static final String STATE_SELECTED_TAB = "STATE_SELECTED_TAB";
    private static final String STATE_INPUT_SEARCH = "STATE_INPUT_SEARCH";
    private static final String STATE_EDITABLE = "STATE_EDITABLE";

    private static final int LIBRARY_TAB = 0;
    private static final int BOOKS_TAB = 1;

    private int selectedTab = LIBRARY_TAB;
    private BookDataFragment bookDataFragment;
    private LibraryFragment libraryFragment;
    private String inputSearch = "";
    private ToolbarActionsListener navDrawerListener;
    private boolean isEditable = false;

    private FragmentBooksLibraryBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener) {
            navDrawerListener = (ToolbarActionsListener) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBooksLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restoreSavedInstanceState(savedInstanceState);
        addFragment(selectedTab);

        attachListeners();
    }

    private void attachListeners() {
        listenOnSelectedTab();
        observeOnInputSearch();

        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
        binding.editBtn.setOnClickListener(v -> onEditClick());
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_SELECTED_TAB);
            inputSearch = savedInstanceState.getString(STATE_INPUT_SEARCH);
            isEditable = savedInstanceState.getBoolean(STATE_EDITABLE);
            if (isEditable) {
                binding.editBtn.setImageResource(R.drawable.check_gold_ic);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        outState.putString(STATE_INPUT_SEARCH, inputSearch);
        outState.putBoolean(STATE_EDITABLE, isEditable);
    }

    private void observeOnInputSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputSearch = s.toString();
                if (binding.tabLayout.getSelectedTabPosition() == LIBRARY_TAB && libraryFragment != null) {
                    libraryFragment.search(inputSearch);
                } else if (binding.tabLayout.getSelectedTabPosition() == BOOKS_TAB && bookDataFragment != null) {
                    bookDataFragment.search(inputSearch);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void listenOnSelectedTab() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                addFragment(binding.tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void addFragment(int tab) {
        selectedTab = tab;
        binding.tabLayout.getTabAt(selectedTab).select();
        binding.etSearch.getText().clear();
        inputSearch = "";
        if (tab == LIBRARY_TAB) {
            binding.editBtn.setVisibility(View.INVISIBLE);
            libraryFragment = (LibraryFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_LIBRARY);
            if (libraryFragment == null) {
                libraryFragment = new LibraryFragment();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.data_container, libraryFragment, FRAGMENT_LIBRARY)
                        .commit();
            }
        } else if (tab == BOOKS_TAB) {
            binding.editBtn.setVisibility(View.VISIBLE);
            bookDataFragment = (BookDataFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_BOOKS);
            if (bookDataFragment == null) {
                bookDataFragment = BookDataFragment.getInstance(true);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.data_container, bookDataFragment, FRAGMENT_BOOKS)
                        .commit();
            }
        }
    }

    private void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

    private void onEditClick() {
        if (isEditable) {
            binding.editBtn.setImageResource(R.drawable.edit_gold_ic);
            bookDataFragment.toggleNormalMode();
        } else {
            binding.editBtn.setImageResource(R.drawable.check_gold_ic);
            bookDataFragment.toggleEditAction();
        }
        isEditable = !isEditable;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
