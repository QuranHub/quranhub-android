package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import app.quranhub.R;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment;
import app.quranhub.util.DialogUtils;

/**
 * A dialog that displays translation books for a language & allows the user to download & select one.
 * The target fragment must implement the interface {@code TranslationsDialogFragment#TranslationSelectionListener}
 */
public class TranslationsDialogFragment extends DialogFragment implements TranslationsDataFragment.TranslationSelectionListener {

    private static final String TAG = TranslationsDialogFragment.class.getSimpleName();

    private static final String ARG_LANGUAGE_CODE = "ARG_LANGUAGE_CODE";

    @Nullable
    private String languageCode;

    private TranslationsDataFragment.TranslationSelectionListener listener;


    public TranslationsDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this dialog fragment using the provided parameters.
     *
     * @param languageCode
     * @param targetFragment
     * @return A new instance of fragment TranslationsDialogFragment.
     */
    public static TranslationsDialogFragment newInstance(String languageCode
            , @NonNull Fragment targetFragment) {
        TranslationsDialogFragment fragment = new TranslationsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE_CODE, languageCode);
        fragment.setArguments(args);
        fragment.setTargetFragment(targetFragment, 0);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getTargetFragment() instanceof TranslationsDataFragment.TranslationSelectionListener) {
            listener = (TranslationsDataFragment.TranslationSelectionListener) getTargetFragment();
        } else {
            throw new RuntimeException(getTargetFragment().getClass().getSimpleName()
                    + " must implement TranslationsDialogFragment#TranslationSelectionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            languageCode = getArguments().getString(ARG_LANGUAGE_CODE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_translations, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            TranslationsDataFragment translationsDataFragment = TranslationsDataFragment.newInstance(
                    languageCode);
            getChildFragmentManager().beginTransaction()
                    .add(R.id.container_translations_data, translationsDataFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        DialogUtils.adjustDialogSize(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onTranslationSelected(TranslationBook translationBook) {
        dismiss();
        listener.onTranslationSelected(translationBook);
    }

}
