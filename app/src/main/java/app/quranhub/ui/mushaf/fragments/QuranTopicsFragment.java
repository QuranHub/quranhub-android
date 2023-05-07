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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.FragmentQuranTopicsBinding;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.main.MainActivity;
import app.quranhub.ui.mushaf.adapter.SubjectsAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.mushaf.model.TopicModel;
import app.quranhub.ui.mushaf.viewmodel.SubjectsViewModel;

public class QuranTopicsFragment extends Fragment implements ItemSelectionListener<TopicCategory> {

    private FragmentQuranTopicsBinding binding;

    private SubjectsAdapter adapter;
    private SubjectsViewModel viewModel;
    private ToolbarActionsListener navDrawerListener;
    private List<TopicModel> topicModels;

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
        binding = FragmentQuranTopicsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        intiRecycler();
        bindViewModel();
        attachListeners();
    }

    private void attachListeners() {
        observeOnInputSearch();
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
    }

    private void observeOnInputSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filter(String inputQuery) {
        if (inputQuery.isEmpty()) {
            adapter = new SubjectsAdapter(topicModels, this);
            binding.topicsRv.setAdapter(adapter);
        } else {
            List<TopicModel> filteredList = new ArrayList<>();
            for (TopicModel row : topicModels) {
                if (row.getTopicName().toLowerCase().contains(inputQuery.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            adapter = new SubjectsAdapter(filteredList, this);
            binding.topicsRv.setAdapter(adapter);
        }
    }

    private void bindViewModel() {
        List<String> subjects = Arrays.asList(requireActivity().getResources().getStringArray(R.array.subject_name));
        List<String> subjectsCategory = Arrays.asList(requireActivity().getResources().getStringArray(R.array.subject_category_name));
        viewModel = new ViewModelProvider(this).get(SubjectsViewModel.class);
        viewModel.getSubjects(subjects, subjectsCategory);
        viewModel.getSubjectsLiveData().observe(getViewLifecycleOwner(), topicModels -> {
            binding.progreesBar.setVisibility(View.GONE);
            this.topicModels = topicModels;
            adapter = new SubjectsAdapter(topicModels, this);
            binding.topicsRv.setAdapter(adapter);
        });
    }

    private void intiRecycler() {
        topicModels = new ArrayList<>();
        binding.topicsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onSelectItem(TopicCategory category) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.openTopicAyasFragment(category);
        }
    }

    private void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }
}
