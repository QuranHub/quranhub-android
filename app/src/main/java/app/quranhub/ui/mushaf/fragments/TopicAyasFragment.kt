package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Objects;

import app.quranhub.databinding.FragmentTopicAyasBinding;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.adapter.SearchAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.SearchModel;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.mushaf.viewmodel.TopicViewModel;
import app.quranhub.util.ScreenUtils;

public class TopicAyasFragment extends Fragment implements ItemSelectionListener<SearchModel> {

    private FragmentTopicAyasBinding binding;

    private String inputSearch = "";
    private QuranNavigationCallbacks quranNavigationCallbacks;
    private ToolbarActionsListener navDrawerListener;
    private SearchAdapter adapter;
    private TopicViewModel viewModel;
    private TopicCategory category;
    private static final String CATEGORY_ARGS = "CATEGORY_ARGS";

    public static TopicAyasFragment getInstance(TopicCategory category) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CATEGORY_ARGS, category);
        TopicAyasFragment fragment = new TopicAyasFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopicAyasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViews();
        getPrevState(savedInstanceState);
        intiRecycler();
        bindViewModel();

        attachListeners();
    }

    private void attachListeners() {
        observeOnInputSearch();
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
    }

    private void getPrevState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            inputSearch = savedInstanceState.getString("input_search");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input_search", inputSearch);
    }

    private void setViews() {
        category = getArguments().getParcelable(CATEGORY_ARGS);
        binding.topicTv.setText(Objects.requireNonNull(category).getCategoryName());
    }

    private void observeOnInputSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputSearch = s.toString();
                adapter.filter(inputSearch);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void bindViewModel() {
        viewModel = new ViewModelProvider(this).get(TopicViewModel.class);
        viewModel.getAyas(category.getCategoryId());
        viewModel.getAyahs().observe(getViewLifecycleOwner(), searchModels -> {
            binding.progreesBar.setVisibility(View.GONE);
            adapter.setSearchModels(searchModels);
            if (inputSearch != null && !TextUtils.isEmpty(inputSearch.trim())) {
                adapter.filter(inputSearch);
            }
        });
    }

    private void intiRecycler() {
        binding.topicsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SearchAdapter(getActivity(), this);
        binding.topicsRv.setAdapter(adapter);
    }

    @Override
    public void onSelectItem(SearchModel item) {
        ScreenUtils.dismissKeyboard(requireContext(), binding.etSearch);
        quranNavigationCallbacks.gotoQuranPageAya(item.getPage(), item.getId(), false);
    }

    public void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

}
