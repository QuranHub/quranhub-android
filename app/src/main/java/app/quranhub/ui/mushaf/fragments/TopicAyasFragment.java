package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import app.quranhub.R;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.adapter.SearchAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.SearchModel;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.mushaf.viewmodel.TopicViewModel;
import app.quranhub.util.ScreenUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopicAyasFragment extends Fragment implements ItemSelectionListener<SearchModel> {

    @BindView(R.id.topics_rv)
    RecyclerView topicsRv;
    @BindView(R.id.topic_tv)
    TextView topicTv;
    @BindView(R.id.et_search)
    EditText searchEt;
    @BindView(R.id.progrees_bar)
    ProgressBar progressBar;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_ayas, container, false);
        ButterKnife.bind(this, view);
        setViews();
        getPrevState(savedInstanceState);
        intiRecycler();
        bindViewModel();
        observeOnInputSearch();
        return view;
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
        topicTv.setText(Objects.requireNonNull(category).getCategoryName());
    }

    private void observeOnInputSearch() {
        searchEt.addTextChangedListener(new TextWatcher() {
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
            progressBar.setVisibility(View.GONE);
            adapter.setSearchModels(searchModels);
            if (inputSearch != null && !TextUtils.isEmpty(inputSearch.trim())) {
                adapter.filter(inputSearch);
            }
        });
    }

    private void intiRecycler() {
        topicsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SearchAdapter(getActivity(), this);
        topicsRv.setAdapter(adapter);
    }

    @Override
    public void onSelectItem(SearchModel item) {
        ScreenUtils.dismissKeyboard(requireContext(), searchEt);
        quranNavigationCallbacks.gotoQuranPageAya(item.getPage(), item.getId(), false);
    }

    @OnClick(R.id.hamburger_iv)
    public void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

}
