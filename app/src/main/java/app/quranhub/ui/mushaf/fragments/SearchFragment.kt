package app.quranhub.ui.mushaf.fragments;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.FragmentSearchBinding;
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.adapter.SearchAdapter;
import app.quranhub.ui.mushaf.dialogs.OptionDialog;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.SearchModel;
import app.quranhub.ui.mushaf.viewmodel.SearchViewModel;
import app.quranhub.util.ScreenUtils;

public class SearchFragment extends Fragment implements ItemSelectionListener<SearchModel>, OptionDialog.ItemClickListener, OptionsListDialogFragment.ItemSelectionListener {

    private FragmentSearchBinding binding;

    public static final int SURA_FILTER_CODE = 1;
    public static final int JUZ_FILTER_CODE = 2;
    public static final int HEZB_FILTER_CODE = 3;
    public static final int QUARTER_FILTER_CODE = 4;
    private boolean isOriented = false, isFilterOptionsShow = false;
    private QuranNavigationCallbacks quranNavigationCallbacks;
    private String inputSearch = "";
    private ToolbarActionsListener navDrawerListener;
    private SearchAdapter searchAdapter;
    private SearchViewModel searchViewModel;
    private int selectedSura = 0;
    private int selectedJuz = 0;
    private int selectedHezb = 0;
    private int selectedQuarter = 0;
    private String option;
    private List<String> suraOptions;
    private List<String> juzOptions;
    private List<String> hezbOptions;
    private List<String> quarterOptions;
    private List<Integer> juzSuraNumbers;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener) {
            navDrawerListener = (ToolbarActionsListener) context;
        }
        if (context instanceof QuranNavigationCallbacks) {
            quranNavigationCallbacks = (QuranNavigationCallbacks) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            isOriented = true;
            getPrevState(savedInstanceState);
        }

        initRecycler();
        bindViewModel();
        setViewsFromBackStack();

        attachListeners();
    }

    private void attachListeners() {
        observeOnInputSearch();

        binding.ibClearSearch.setOnClickListener(v -> clearSearch());
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
        binding.filterContainer.partContainer.setOnClickListener(v -> onClickPartFilter());
        binding.filterContainer.suraContainer.setOnClickListener(v -> onClickSuraFilter());
        binding.filterContainer.hezbContainer.setOnClickListener(v -> onClickHezbFilter());
        binding.filterContainer.rob3Container.setOnClickListener(v -> onClickQuraterFilter());
        binding.moreIv.setOnClickListener(v -> onGetMoreFilterOptions());
    }

    private void setViewsFromBackStack() {
        if (isFilterOptionsShow) {
            binding.filterContainer.getRoot().setVisibility(View.VISIBLE);
        }
        if (selectedSura != 0) {
            binding.filterContainer.suraTv.setText(getActivity().getResources().getStringArray(R.array.sura_name)[selectedSura - 1]);
        }
        if (selectedJuz != 0) {
            binding.filterContainer.chapterTv.setText(refactorOptionText(getActivity().getResources().getStringArray(R.array.agza2_name)[selectedJuz - 1]));
        }
        if (selectedHezb != 0) {
            binding.filterContainer.hezbTv.setText(hezbOptions.get(selectedHezb));
        }
        if (selectedQuarter != 0) {
            binding.filterContainer.rob3Tv.setText(quarterOptions.get(selectedQuarter));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input_search", inputSearch);
        outState.putInt("selected_juz", selectedJuz);
        outState.putInt("selected_sura", selectedSura);
        outState.putInt("input_hezb", selectedHezb);
        outState.putInt("input_qurater", selectedQuarter);
    }

    @SuppressLint("CheckResult")
    private void observeOnInputSearch() {

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputSearch = s.toString();
                if (!isOriented) {
                    binding.progreesBar.setVisibility(View.VISIBLE);
                    searchAya();
                } else {
                    isOriented = false;
                }

                // show or hide clear button in search field
                if (TextUtils.isEmpty(s)) {
                    binding.ibClearSearch.setVisibility(View.INVISIBLE);
                } else {
                    binding.ibClearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void clearSearch() {
        binding.etSearch.getText().clear();
    }

    private void searchAya() {
        if (inputSearch.trim().isEmpty()) {
            clearResult();
        } else if (selectedJuz != 0 && selectedHezb != 0 && selectedQuarter != 0) {
            searchViewModel.searchWithSuraAndJuzAndHizbQuarter(inputSearch, selectedSura, selectedJuz, selectedHezb, selectedQuarter);
        } else if (selectedJuz != 0 && selectedHezb != 0) {
            searchViewModel.searchWithSuraAndJuzAndHizb(inputSearch, selectedSura, selectedJuz, selectedHezb);
        } else if (selectedSura != 0 && selectedJuz != 0) {
            searchViewModel.searchWithSuraAndJuz(inputSearch, selectedSura, selectedJuz);
        } else if (selectedSura != 0) {
            searchViewModel.searchWithSura(inputSearch, selectedSura);
        } else if (selectedJuz != 0) {
            searchViewModel.searchWithJuz(inputSearch, selectedJuz);
        } else {
            searchViewModel.simpleSearch(inputSearch);
        }
    }

    private void clearResult() {
        searchAdapter.setSearchModels(new ArrayList<>());
        binding.noresultTv.setVisibility(View.VISIBLE);
        binding.progreesBar.setVisibility(View.GONE);
    }

    private void bindViewModel() {
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        searchViewModel.getSearch().observe(getViewLifecycleOwner(), searchModels -> {
            binding.progreesBar.setVisibility(View.GONE);
            if (searchModels == null) {
                Toast.makeText(getActivity(), getString(R.string.search_failed), Toast.LENGTH_LONG).show();
            } else if (searchModels.isEmpty()) {
                clearResult();
            } else if (!inputSearch.trim().isEmpty()) {
                binding.noresultTv.setVisibility(View.GONE);
                searchAdapter.setSearchModels(searchModels);
            }
        });

        searchViewModel.getSura().observe(getViewLifecycleOwner(), results -> {
            suraOptions = new ArrayList<>();
            juzSuraNumbers = results;
            getJuzSuras();
        });
    }

    private void getJuzSuras() {
        List<String> surahs = Arrays.asList(getResources().getStringArray(R.array.sura_name));
        suraOptions = new ArrayList<>();
        for (int index : juzSuraNumbers) {
            suraOptions.add(surahs.get(index - 1));
        }
    }

    private void initRecycler() {
        searchAdapter = new SearchAdapter(requireContext(), this);
        binding.searchRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.searchRv.setAdapter(searchAdapter);
    }

    private void getPrevState(Bundle savedInstanceState) {
        inputSearch = savedInstanceState.getString("input_search");
        selectedJuz = savedInstanceState.getInt("selected_juz");
        selectedSura = savedInstanceState.getInt("selected_sura");
        selectedHezb = savedInstanceState.getInt("input_hezb");
        selectedQuarter = savedInstanceState.getInt("input_qurater");
        if (selectedJuz != 0) {
            binding.filterContainer.chapterTv.setText(refactorOptionText(getActivity().getResources().getStringArray(R.array.agza2_name)[selectedJuz - 1]));
        }
        if (selectedSura != 0) {
            binding.filterContainer.suraTv.setText(getActivity().getResources().getStringArray(R.array.sura_name)[selectedSura - 1]);
        }
        if (selectedHezb != 0) {
            binding.filterContainer.hezbTv.setText(getActivity().getResources().getStringArray(R.array.hezb_name)[selectedHezb - 1]);
        }
        if (selectedQuarter != 0) {
            binding.filterContainer.rob3Tv.setText(getActivity().getResources().getStringArray(R.array.quarter_name)[selectedQuarter - 1]);
        }
    }

    private void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

    private void onClickPartFilter() {
        if (juzOptions == null) {
            List<String> options = Arrays.asList(getResources().getStringArray(R.array.agza2_name));
            juzOptions = new ArrayList<>();
            juzOptions.add(getString(R.string.all_guz2));
            juzOptions.addAll(options);
        }
        if (selectedJuz == 0) {
            option = getString(R.string.all_guz2);
        } else {
            option = requireActivity().getResources().getStringArray(R.array.agza2_name)[selectedJuz - 1];
        }
        DialogFragment dialog = OptionDialog.getInstance(juzOptions, option, JUZ_FILTER_CODE, getString(R.string.chapters));
        dialog.show(getChildFragmentManager(), "JuzDialog");
    }

    private void setSuraDialog() {
        if (selectedSura == 0) {
            option = getString(R.string.all_sura);
        } else {
            option = requireActivity().getResources().getStringArray(R.array.sura_name)[selectedSura - 1];
        }
        DialogFragment dialog = OptionDialog.getInstance(suraOptions, option, SURA_FILTER_CODE, getString(R.string.suras));
        dialog.show(getChildFragmentManager(), "OptionDialog");
    }

    private void onClickSuraFilter() {
        if (selectedJuz == 0) {
            List<String> options = Arrays.asList(getResources().getStringArray(R.array.sura_name));
            suraOptions = new ArrayList<>();
            suraOptions.add(getString(R.string.all_sura));
            suraOptions.addAll(options);
        }
        setSuraDialog();
    }

    private void onClickHezbFilter() {
        if (selectedJuz == 0) {
            Toast.makeText(getActivity(), R.string.select_juz_first, Toast.LENGTH_LONG).show();
            return;
        }
        if (hezbOptions == null) {
            List<String> options = Arrays.asList(getResources().getStringArray(R.array.hezb_name));
            hezbOptions = new ArrayList<>();
            hezbOptions.add(getString(R.string.all_hezb));
            hezbOptions.addAll(options);
        }
        OptionsListDialogFragment fragment = OptionsListDialogFragment.getInstance(getString(R.string.hizb), hezbOptions, selectedHezb, this, HEZB_FILTER_CODE);
        fragment.show(getActivity().getSupportFragmentManager(), "HizbFilterDialog");
    }

    private void onClickQuraterFilter() {
        if (selectedHezb == 0) {
            Toast.makeText(getActivity(), R.string.select_hezb_first, Toast.LENGTH_LONG).show();
            return;
        }
        if (quarterOptions == null) {
            List<String> options = Arrays.asList(getResources().getStringArray(R.array.quarter_name));
            quarterOptions = new ArrayList<>();
            quarterOptions.add(getString(R.string.all_quarters));
            quarterOptions.addAll(options);
        }
        OptionsListDialogFragment fragment = OptionsListDialogFragment.getInstance(getString(R.string.rub3), quarterOptions, selectedQuarter, this, QUARTER_FILTER_CODE);
        fragment.show(getActivity().getSupportFragmentManager(), "QuarterFilterDialog");
    }

    @Override
    public void onSelectItem(SearchModel item) {
        ScreenUtils.dismissKeyboard(getContext(), binding.etSearch);
        quranNavigationCallbacks.gotoQuranPageAya(item.getPage(), item.getId(), true);
    }

    @Override
    public void onItemClick(String optionName, int optionIndex, int requestCode) {
        if (requestCode == SURA_FILTER_CODE) {
            binding.filterContainer.suraTv.setText(optionName);
            selectedSura = selectedJuz == 0 ? optionIndex : juzSuraNumbers.get(optionIndex);
            searchAya();
        } else if (requestCode == JUZ_FILTER_CODE) {
            binding.filterContainer.chapterTv.setText(optionIndex == 0 ? optionName : refactorOptionText(optionName));
            binding.filterContainer.suraTv.setText(getString(R.string.sura));
            selectedSura = 0;
            if (optionIndex == 0) {
                selectedQuarter = 0;
                selectedHezb = 0;
                binding.filterContainer.rob3Tv.setText(getString(R.string.rub3));
                binding.filterContainer.hezbTv.setText(getString(R.string.hizb));
            } else {
                searchViewModel.getChapterSuras(optionIndex);
            }
            if (optionIndex != selectedJuz) {
                selectedJuz = optionIndex;
                searchAya();
            }
        }
    }

    private String refactorOptionText(String text) {
        return text.substring(text.indexOf(' ') + 1);
    }


    @Override
    public void onItemSelected(int requestCode, int itemIndex) {
        if (requestCode == HEZB_FILTER_CODE) {
            selectedHezb = itemIndex;
            binding.filterContainer.hezbTv.setText(hezbOptions.get(itemIndex));
            if (selectedHezb == 0) {
                selectedQuarter = 0;
                binding.filterContainer.rob3Tv.setText(getString(R.string.rub3));
            }
            searchAya();
        } else if (requestCode == QUARTER_FILTER_CODE) {
            selectedQuarter = itemIndex;
            binding.filterContainer.rob3Tv.setText(quarterOptions.get(itemIndex));
            searchAya();
        }
    }

    private void onGetMoreFilterOptions() {
        if (isFilterOptionsShow) {
            binding.filterContainer.getRoot().setVisibility(View.GONE);
        } else {
            binding.filterContainer.getRoot().setVisibility(View.VISIBLE);
        }

        isFilterOptionsShow = !isFilterOptionsShow;
    }
}
