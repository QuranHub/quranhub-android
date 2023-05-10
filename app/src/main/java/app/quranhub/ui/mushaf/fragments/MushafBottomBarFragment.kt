package app.quranhub.ui.mushaf.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import app.quranhub.R;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.databinding.FragmentMushafBottomBarBinding;
import app.quranhub.ui.mushaf.presenter.QuranFooterPresenter;
import app.quranhub.ui.mushaf.presenter.QuranFooterPresenterImp;
import app.quranhub.ui.mushaf.view.QuranFooterView;

public class MushafBottomBarFragment extends Fragment implements QuranFooterView {

    private static final String TAG = MushafBottomBarFragment.class.getSimpleName();

    private QuranFooterPresenter presenter;

    private boolean nightMode;

    private FragmentMushafBottomBarBinding binding;

    private QuranFooterCallbacks footerCallbacks;

    private MutableLiveData<String> pageNumTextLiveData;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof QuranFooterCallbacks) {
            footerCallbacks = (QuranFooterCallbacks) getParentFragment();
        } else {
            throw new ClassCastException(
                    "Cannot cast the parent fragment to QuranFooterCallbacks instance.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageNumTextLiveData = new MutableLiveData<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMushafBottomBarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        presenter.onAttach(this);

        pageNumTextLiveData.observe(getViewLifecycleOwner(), pageNumText ->
                binding.quranPageTv.setText(pageNumText));
    }

    private void initViews() {
        setupButtonsTooltips();

        presenter = new QuranFooterPresenterImp();

        nightMode = AppPreferencesManager.getNightModeSetting(requireActivity());
        setupNightModeButton();

        attachListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void attachListeners() {
        binding.llRoot.setOnTouchListener((v, event) -> {
            return true; // To prevent event bubbling to the views below this one
        });

        binding.quranSearchIb.setOnClickListener(v -> onQuranSearchClick());
        binding.quranNightModeIb.setOnClickListener(v -> onQuranNightModeClick());
    }

    private void setupButtonsTooltips() {
        TooltipCompat.setTooltipText(binding.quranNightModeIb, getString(R.string.tooltip_quran_night_mode));
        TooltipCompat.setTooltipText(binding.quranSearchIb, getString(R.string.tooltip_quran_search));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        presenter.onDetach();
    }

    private void onQuranSearchClick() {
        presenter.displaySearchDialog();
    }

    private void onQuranNightModeClick() {
        presenter.toggleNightMode();
    }

    private void setupNightModeButton() {
        binding.quranNightModeIb.setImageResource(
                nightMode ? R.drawable.ic_nightmode_on : R.drawable.ic_nightmode_off);
    }

    public void setCurrentPage(String pageNumText) {
        pageNumTextLiveData.setValue(pageNumText);
    }

    @Override
    public void displaySearchDialog() {
        footerCallbacks.openSearchFragment();
    }

    @Override
    public void toggleNightMode() {
        nightMode = footerCallbacks.toggleNightMode();
        setupNightModeButton();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }


    public interface QuranFooterCallbacks {
        void openSearchFragment();

        boolean toggleNightMode();
    }

}
