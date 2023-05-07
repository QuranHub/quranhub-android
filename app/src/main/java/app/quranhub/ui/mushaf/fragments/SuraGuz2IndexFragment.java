package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
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

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.FragmentSuraGuz2IndexBinding;
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.adapter.Guz2IndexAdapter;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper;
import app.quranhub.ui.mushaf.presenter.SuraGuz2IndexPresenter;
import app.quranhub.ui.mushaf.presenter.SuraGuz2IndexPresenterImp;
import app.quranhub.ui.mushaf.view.SuraGuz2IndexView;
import app.quranhub.util.ScreenUtils;

public class SuraGuz2IndexFragment extends Fragment implements SuraGuz2IndexView, OptionsListDialogFragment.ItemSelectionListener {

    private static final String TAG = SuraGuz2IndexFragment.class.getSimpleName();

    private static final String ARG_SELECTED_TAB = "ARG_SELECTED_TAB";

    private static final String STATE_SELECTED_TAB = "STATE_SELECTED_TAB";
    private static final String STATE_INPUT_SEARCH = "STATE_INPUT_SEARCH";
    private static final String STATE_SELECTED_GUZ2_FILTER = "STATE_SELECTED_GUZ2_FILTER";

    private static final String FRAGMENT_SURA_INDEX = "FRAGMENT_SURA_INDEX";
    private static final String FRAGMENT_GUZ2_INDEX = "FRAGMENT_GUZ2_INDEX";

    public static final int SURA_INDEX_TAB = 0;
    public static final int GUZ2_INDEX_TAB = 1;
    private static final int RC_GUZ2_FILTER = 0;

    private ToolbarActionsListener toolbarActionsListener;
    private QuranNavigationCallbacks quranNavigationCallbacks;

    private int selectedTab = SURA_INDEX_TAB;
    private SuraIndexFragment suraIndexFragment;
    private Guz2IndexFragment guz2IndexFragment;
    private SuraGuz2IndexPresenter presenter;
    private String inputSearch = "";
    private int selectedGUZ2Filter = Guz2IndexAdapter.FILTER_GUZ2_ALL;

    private FragmentSuraGuz2IndexBinding binding;

    public static SuraGuz2IndexFragment newInstance(int selectedTab) {
        SuraGuz2IndexFragment fragment = new SuraGuz2IndexFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTED_TAB, selectedTab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener && context instanceof QuranNavigationCallbacks) {
            toolbarActionsListener = (ToolbarActionsListener) context;
            quranNavigationCallbacks = (QuranNavigationCallbacks) context;
        } else {
            throw new RuntimeException("The parent activity must implement ToolbarActionsListener" +
                    " & QuranNavigationCallbacks interfaces.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedTab = getArguments().getInt(ARG_SELECTED_TAB);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSuraGuz2IndexBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restoreSavedInstanceState(savedInstanceState);
        initPresenter();
        addIndexFragment(selectedTab);

        attachListeners();
    }

    private void attachListeners() {
        listenOnSelectedTab();
        observeOnInputSearch();
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
        binding.filterBtn.setOnClickListener(v -> onFilterButtonClick());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        toolbarActionsListener = null;
        quranNavigationCallbacks = null;
    }

    private void initPresenter() {
        presenter = new SuraGuz2IndexPresenterImp<SuraGuz2IndexView>(getActivity());
        presenter.onAttach(this);
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_SELECTED_TAB);
            inputSearch = savedInstanceState.getString(STATE_INPUT_SEARCH);
            selectedGUZ2Filter = savedInstanceState.getInt(STATE_SELECTED_GUZ2_FILTER);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        outState.putString(STATE_INPUT_SEARCH, inputSearch);
        outState.putInt(STATE_SELECTED_GUZ2_FILTER, selectedGUZ2Filter);
    }

    private void observeOnInputSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.tabLayout.getSelectedTabPosition() == SURA_INDEX_TAB && suraIndexFragment != null) {
                    inputSearch = s.toString();
                    suraIndexFragment.onSearchSura(inputSearch);
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
                addIndexFragment(binding.tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void addIndexFragment(int tab) {
        selectedTab = tab;
        binding.tabLayout.getTabAt(selectedTab).select();
        if (tab == SURA_INDEX_TAB) {
            suraIndexFragment = (SuraIndexFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_SURA_INDEX);
            if (suraIndexFragment == null) {
                suraIndexFragment = new SuraIndexFragment();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.index_container, suraIndexFragment, FRAGMENT_SURA_INDEX)
                        .commit();
            }
            binding.filterBtn.setVisibility(View.INVISIBLE);
            binding.etSearch.setVisibility(View.VISIBLE);
            presenter.getSuraIndex();
        } else if (tab == GUZ2_INDEX_TAB) {
            guz2IndexFragment = (Guz2IndexFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_GUZ2_INDEX);
            if (guz2IndexFragment == null) {
                guz2IndexFragment = Guz2IndexFragment.newInstance(selectedGUZ2Filter);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.index_container, guz2IndexFragment, FRAGMENT_GUZ2_INDEX)
                        .commit();
            }
            binding.filterBtn.setVisibility(View.VISIBLE);
            binding.etSearch.getText().clear();
            inputSearch = "";
            binding.etSearch.setVisibility(View.GONE);
        }
    }

    private void onNavHamburgerClick() {
        toolbarActionsListener.onNavDrawerClick();
    }

    private void onFilterButtonClick() {
        if (binding.tabLayout.getSelectedTabPosition() == GUZ2_INDEX_TAB && guz2IndexFragment != null) {
            List<String> guz2Options = new ArrayList<>();
            guz2Options.add(getString(R.string.all_guz2));
            guz2Options.addAll(Arrays.asList(getResources().getStringArray(R.array.agza2_name)));
            OptionsListDialogFragment guz2Dialog = OptionsListDialogFragment.getInstance(
                    getString(R.string.title_options_dialog_filter_guz2_index),
                    guz2Options, selectedGUZ2Filter, this, RC_GUZ2_FILTER);
            guz2Dialog.show(getFragmentManager(), "guz2Dialog");
        }
    }

    public void navigateToSelectedSura(int suraPage) {
        ScreenUtils.dismissKeyboard(getActivity(), binding.etSearch);
        quranNavigationCallbacks.gotoQuranPage(suraPage);
    }

    @Override
    public void onGetIndex(List<SuraIndexModelMapper> indexList) {
        suraIndexFragment.setAdapterData(indexList);
        if (!TextUtils.isEmpty(inputSearch)) {
            suraIndexFragment.onSearchSura(inputSearch);
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoading() {
        binding.progreesBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progreesBar.setVisibility(View.GONE);
    }

    @Override
    public void onItemSelected(int requestCode, int itemIndex) { // filter dialog callback
        if (requestCode == RC_GUZ2_FILTER && guz2IndexFragment != null) {
            selectedGUZ2Filter = itemIndex;
            guz2IndexFragment.filterForGuz2(selectedGUZ2Filter);
        }
    }
}
