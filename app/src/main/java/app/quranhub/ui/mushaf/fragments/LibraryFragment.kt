package app.quranhub.ui.mushaf.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.quranhub.R;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.databinding.FragmentLibraryBinding;
import app.quranhub.ui.main.MainActivity;

// TODO completely refactor LibraryFragment
public class LibraryFragment extends Fragment implements TranslationsDataFragment.TranslationSelectionListener {

    private static final String TAG = LibraryFragment.class.getSimpleName();

    private FragmentLibraryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            TranslationsDataFragment translationsDataFragment = TranslationsDataFragment.newInstance(
                    AppPreferencesManager.getQuranTranslationLanguage(getContext()));
            getChildFragmentManager().beginTransaction()
                    .add(R.id.container_data_fragment, translationsDataFragment, "TransDataFragment")
                    .commit();
        }
    }

    public void search(String input) {
        TranslationsDataFragment translationsDataFragment = (TranslationsDataFragment) getChildFragmentManager()
                .findFragmentByTag("TransDataFragment");
        if (translationsDataFragment != null) {
            translationsDataFragment.search(input);
        }
    }

    @Override
    public void onTranslationSelected(TranslationBook translationBook) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.openTafseerScreen(translationBook.getDatabaseName(), translationBook.getName());
        }
    }
}
