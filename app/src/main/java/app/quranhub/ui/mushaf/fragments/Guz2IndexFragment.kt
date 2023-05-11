package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import app.quranhub.databinding.FragmentGuz2IndexBinding;
import app.quranhub.ui.mushaf.adapter.Guz2IndexAdapter;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel;
import app.quranhub.ui.mushaf.viewmodel.Guz2IndexViewModel;

/**
 * Fragment that displays a list containing Juz' index with its Hizb & Hizb Quarters.
 */
public class Guz2IndexFragment extends Fragment implements Guz2IndexAdapter.IndexItemClickListener {

    private static final String TAG = Guz2IndexFragment.class.getSimpleName();

    private static final String ARG_FILTER_GUZ2 = "ARG_FILTER_GUZ2";
    private static final String STATE_FILTER_GUZ2 = "STATE_FILTER_GUZ2";

    private FragmentGuz2IndexBinding binding;

    private QuranNavigationCallbacks quranNavigationCallbacks;

    private Guz2IndexViewModel guz2IndexViewModel;

    private Guz2IndexAdapter adapter;

    private int filterGuz2 = Guz2IndexAdapter.FILTER_GUZ2_ALL;


    public Guz2IndexFragment() {
        // Required empty public constructor
    }

    public static Guz2IndexFragment newInstance(int filterGuz2) {
        Guz2IndexFragment fragment = new Guz2IndexFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FILTER_GUZ2, filterGuz2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof QuranNavigationCallbacks) {
            quranNavigationCallbacks = (QuranNavigationCallbacks) context;
        } else {
            throw new RuntimeException(
                    "The containing Activity must implement QuranNavigationCallbacks interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterGuz2 = getArguments().getInt(ARG_FILTER_GUZ2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGuz2IndexBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            filterGuz2 = savedInstanceState.getInt(STATE_FILTER_GUZ2, Guz2IndexAdapter.FILTER_GUZ2_ALL);
        }

        initGuz2IndexRecyclerView();

        guz2IndexViewModel = new ViewModelProvider(this).get(Guz2IndexViewModel.class);
        guz2IndexViewModel.getHizbQuarterDataModelsLiveData().observe(getViewLifecycleOwner(), hizbQuarterDataModels -> {
            Log.d(TAG, "hizbQuarterDataModels = " + hizbQuarterDataModels);

            if (binding.guz2IndexProgressBar.getVisibility() == View.VISIBLE) {
                binding.guz2IndexProgressBar.setVisibility(View.GONE);
            }
            adapter.setHizbQuarterDataModels(hizbQuarterDataModels);
        });
        guz2IndexViewModel.indexItemClickEvent().observe(getViewLifecycleOwner(),
                indexItemClickEvent -> quranNavigationCallbacks.gotoQuranPage(indexItemClickEvent.page));
    }

    private void initGuz2IndexRecyclerView() {
        binding.rvGuz2Index.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvGuz2Index.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        binding.rvGuz2Index.addItemDecoration(dividerItemDecoration);

        adapter = new Guz2IndexAdapter(null, filterGuz2, this);
        binding.rvGuz2Index.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_FILTER_GUZ2, filterGuz2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onIndexItemClick(HizbQuarterDataModel model, int clickedItemIndex) {
        guz2IndexViewModel.notifyIndexItemClick(clickedItemIndex);
    }

    public void filterForGuz2(int guz2) {
        filterGuz2 = guz2;
        adapter.getFilter().filter(Integer.toString(guz2));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        quranNavigationCallbacks = null;
    }
}
