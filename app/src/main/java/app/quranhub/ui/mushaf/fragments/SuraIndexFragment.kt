package app.quranhub.ui.mushaf.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import app.quranhub.databinding.FragmentSuraIndexBinding;
import app.quranhub.ui.mushaf.adapter.SuraIndexAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper;


public class SuraIndexFragment extends Fragment implements ItemSelectionListener<Integer> {

    private FragmentSuraIndexBinding binding;

    private SuraIndexAdapter adapter;

    private ViewPropertyAnimator fastScrollerAnimator;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSuraIndexBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecycler();
    }

    private void initRecycler() {
        binding.suraIndexRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SuraIndexAdapter(getActivity(), this);
        binding.suraIndexRv.setAdapter(adapter);
    }

    public void onSearchSura(String inputQuery) {
        adapter.filter(inputQuery);
    }

    @Override
    public void onSelectItem(Integer suraPage) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SuraGuz2IndexFragment) {
            ((SuraGuz2IndexFragment) parentFragment).navigateToSelectedSura(suraPage);
        }
    }

    public void setAdapterData(List<SuraIndexModelMapper> indexList) {
        adapter.setSuraIndexModelList(indexList);
        binding.suraIndexRv.getRecycledViewPool().clear();
    }

}
