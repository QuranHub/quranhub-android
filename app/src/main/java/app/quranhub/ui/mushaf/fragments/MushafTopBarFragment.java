package app.quranhub.ui.mushaf.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import app.quranhub.R;
import app.quranhub.databinding.FragmentMushafTopBarBinding;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;

public class MushafTopBarFragment extends Fragment {

    private static final String TAG = MushafTopBarFragment.class.getSimpleName();

    public static final int PAGE_DIR_RIGHT = 0;
    public static final int PAGE_DIR_LEFT = 1;

    private FragmentMushafTopBarBinding binding;

    private ToolbarActionsListener toolbarActionsListener;

    private MutableLiveData<Integer> pageDirLiveData;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener) {
            toolbarActionsListener = (ToolbarActionsListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageDirLiveData = new MutableLiveData<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMushafTopBarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pageDirLiveData.observe(getViewLifecycleOwner(), pageDir -> {
            switch (pageDir) {
                case PAGE_DIR_RIGHT:
                    binding.ivPageDir.setImageResource(R.drawable.ic_quran_page_right);
                    break;
                case PAGE_DIR_LEFT:
                    binding.ivPageDir.setImageResource(R.drawable.ic_quran_page_left);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid page dir");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        TooltipCompat.setTooltipText(binding.ivPageDir, getText(R.string.tooltip_page_dir));

        binding.llRoot.setOnTouchListener((v, event) -> {
            return true; // To prevent event bubbling to the views below this one
        });

        attachListeners();
    }

    private void attachListeners() {
        binding.ivMenu.setOnClickListener(v -> onNavHamburgerClick());
        binding.btnPageGuz2.setOnClickListener(v -> onGuz2Click());
        binding.btnPageSura.setOnClickListener(v -> onSuraClick());
    }

    private void onNavHamburgerClick() {
        toolbarActionsListener.onNavDrawerClick();
    }

    private void onGuz2Click() {
        toolbarActionsListener.onGuz2Click();
    }

    void onSuraClick() {
        toolbarActionsListener.onSuraClick();
    }

    public void setSuraText(String suraName) {
        binding.btnPageSura.setText(suraName);
    }

    public void setGuz2Text(String currentGuz2) {
        binding.btnPageGuz2.setText(currentGuz2);
    }

    /**
     * Control page dir icon.
     *
     * @param pageDir either {@link #PAGE_DIR_LEFT} or {@link #PAGE_DIR_RIGHT}.
     */
    public void setPageDir(int pageDir) {
        pageDirLiveData.setValue(pageDir);
    }

}
