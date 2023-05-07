package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import app.quranhub.R;
import app.quranhub.databinding.FragmentBookmarksBinding;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.listener.BookmarksListListener;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.presenter.BookmarksPresenter;
import app.quranhub.ui.mushaf.presenter.BookmarksPresenterImp;
import app.quranhub.ui.mushaf.view.BookmarksView;
import app.quranhub.util.ScreenUtils;

public class BookmarksFragment extends Fragment
        implements BookmarksView
        , BookmarksListListener, QuranNavigationCallbacks {

    private static final String TAG = BookmarksFragment.class.getSimpleName();

    private BookmarksPresenter presenter;

    private ToolbarActionsListener navDrawerListener;
    private QuranNavigationCallbacks quranNavigationCallbacks;
    private boolean isListEditable = true;

    private FragmentBookmarksBinding binding;

    private BookmarksListFragment bookmarksListFragment;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BookmarksFragment.
     */
    public static BookmarksFragment newInstance() {
        BookmarksFragment fragment = new BookmarksFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener) {
            navDrawerListener = (ToolbarActionsListener) context;
        } else {
            throw new RuntimeException(
                    "The parent of this fragment must implement ToolbarActionsListener interface.");
        }

        if (context instanceof QuranNavigationCallbacks) {
            quranNavigationCallbacks = (QuranNavigationCallbacks) context;
        } else {
            throw new RuntimeException(
                    "The parent of this fragment must implement QuranNavigationCallbacks interface.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentBookmarksBinding.inflate(inflater, container, false);

        presenter = new BookmarksPresenterImp();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.searchList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookmarksListFragment = BookmarksListFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.list_container, bookmarksListFragment);
        transaction.commit();

        presenter.onAttach(this);

        attachListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDetach();
        binding = null;
    }

    private void attachListeners() {
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
        binding.editBtn.setOnClickListener(v -> edit());
        binding.filterBtn.setOnClickListener(v -> filter());
        binding.ibFinishEdit.setOnClickListener(v -> finishEdit());

    }

    private void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

    private void edit() {
        if (isListEditable) {
            presenter.enableEditList();
        } else {
            showMessage(getString(R.string.msg_no_bookmarks));
        }
    }

    private void filter() {
        presenter.filterList();
    }

    private void finishEdit() {
        presenter.finishEditList();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() { /* there's no need for this here */ }

    @Override
    public void hideLoading() { /* there's no need for this here */ }

    @Override
    public void enableEditList() {
        binding.editBtn.setVisibility(View.INVISIBLE);
        binding.ibFinishEdit.setVisibility(View.VISIBLE);
        binding.filterBtn.setVisibility(View.INVISIBLE);
        bookmarksListFragment.setEditBookmarks(true);
    }

    @Override
    public void finishEditList() {
        binding.editBtn.setVisibility(View.VISIBLE);
        binding.ibFinishEdit.setVisibility(View.INVISIBLE);
        binding.filterBtn.setVisibility(View.VISIBLE);
        bookmarksListFragment.setEditBookmarks(false);
    }

    @Override
    public void filterList() {
        bookmarksListFragment.showFilterDialog();
    }

    @Override
    public void searchList(@NonNull String text) {
        bookmarksListFragment.searchBookmarks(text);
    }


    @Override
    public void onEditabilityChange(boolean isEditable) {
        isListEditable = isEditable;

        // disable/enable the edit image button
        if (isEditable) {
            binding.editBtn.setImageResource(R.drawable.edit_gold_ic);
        } else {
            binding.editBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_grey));
        }

    }

    @Override
    public void gotoQuranPage(int pageNumber) {
        quranNavigationCallbacks.gotoQuranPage(pageNumber);
    }

    @Override
    public void gotoQuranPageAya(int pageNumber, int ayaId, boolean addToBackStack) {
        ScreenUtils.dismissKeyboard(requireActivity(), binding.etSearch);
        quranNavigationCallbacks.gotoQuranPageAya(pageNumber, ayaId, false);
    }

}
