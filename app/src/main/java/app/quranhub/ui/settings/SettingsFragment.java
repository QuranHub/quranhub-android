package app.quranhub.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.Reciter;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.data.model.ReciterModel;
import app.quranhub.databinding.FragmentSettingsBinding;
import app.quranhub.ui.base.BaseActivity;
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment;
import app.quranhub.ui.downloads_manager.DownloadsManagerActivity;
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment;
import app.quranhub.util.LocaleUtils;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends Fragment implements OptionsListDialogFragment.ItemSelectionListener
        , QuranRecitersDialogFragment.ReciterSelectionListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final int RC_APP_LANG_SETTING = 1;
    private static final int RC_TRANS_LANG_SETTING = 2;
    private static final int RC_RECITATION_SETTING = 3;

    private FragmentSettingsBinding binding;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public SettingsFragment() {
        // required private constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSettingsViews();
        setSettingsViewsListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        compositeDisposable.dispose();
    }

    private void initSettingsViews() {
        // appLangSetting
        int currentAppLanguageIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getAppLangSetting(requireContext()));
        binding.settingAppLang.setCurrentValue(
                getString(Constants.Language.NAMES_STR_IDS[currentAppLanguageIndex]));

        // translationLangSetting
        int currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getQuranTranslationLanguage(requireContext()));
        binding.settingTranslationLang.setCurrentValue(
                getString(Constants.Language.NAMES_STR_IDS[currentTranslationLanguageIndex]));

        // screenReadingBacklightSettingSwitch
        binding.settingScreenReadingBacklight.setChecked(
                AppPreferencesManager.getScreenReadingBacklightSetting(requireContext()));

        // lastReadPageSettingSwitch
        binding.settingLastReadPage.setChecked(
                AppPreferencesManager.getLastReadPageSetting(requireContext()));

        // recitationSetting
        int selectedRecitationIndex = AppPreferencesManager.getRecitationSetting(requireContext());
        binding.settingRecitation.setCurrentValue(getString(Constants.Recitation.NAMES_STR_IDS[selectedRecitationIndex]));

        // quranReaderSetting
        String reciterId = AppPreferencesManager.getReciterSheikhSetting(requireContext());
        if (reciterId != null) {
            final Disposable disposable = Single.create(
                            (SingleOnSubscribe<String>) emitter -> {
                                final Reciter reciter = UserDatabase.getInstance(requireContext())
                                        .getReciterDao()
                                        .getById(reciterId);
                                emitter.onSuccess(reciter.getName());
                            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(reciterName -> {
                        binding.settingQuranReader.setCurrentValue(reciterName);
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void setSettingsViewsListeners() {
        binding.settingAppLang.setOnClickListener(v -> {
            // TODO apply MVP or MVVM
            int currentAppLanguageIndex = Constants.Language.CODES.indexOf(
                    AppPreferencesManager.getAppLangSetting(requireContext()));
            OptionsListDialogFragment appLangDialog = OptionsListDialogFragment.getInstance(
                    getString(R.string.app_lang_setting_dialog_title),
                    Constants.Language.NAMES_STR_IDS,
                    Constants.Language.FLAGS_DRAWABLE_IDS,
                    currentAppLanguageIndex, this, RC_APP_LANG_SETTING);
            appLangDialog.show(getActivity().getSupportFragmentManager()
                    , "AppLangDialog");
        });

        binding.settingTranslationLang.setOnClickListener(v -> {
            // TODO apply MVP or MVVM
            int currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
                    AppPreferencesManager.getQuranTranslationLanguage(requireContext()));
            OptionsListDialogFragment translationLangDialog = OptionsListDialogFragment.getInstance(
                    getString(R.string.translation_lang_setting_dialog_title),
                    Constants.Language.NAMES_STR_IDS,
                    Constants.Language.FLAGS_DRAWABLE_IDS,
                    currentTranslationLanguageIndex, this, RC_TRANS_LANG_SETTING);
            translationLangDialog.show(getActivity().getSupportFragmentManager()
                    , "TransLangDialog");
        });

        binding.settingScreenReadingBacklight.setOnCheckedChangeListener((settingSwitch, checked) -> {
            // TODO apply MVP or MVVM
            AppPreferencesManager.persistScreenReadingBacklightSetting(requireContext(), checked);
        });

        binding.settingLastReadPage.setOnCheckedChangeListener((settingSwitch, checked) -> {
            // TODO apply MVP or MVVM
            AppPreferencesManager.persistLastReadPageSetting(requireContext(), checked);
        });

        binding.settingRecitation.setOnClickListener(v -> {
            // TODO apply MVP or MVVM
            int selectedRecitationSettingIndex = AppPreferencesManager.getRecitationSetting(requireContext());
            OptionsListDialogFragment recitationDialog = OptionsListDialogFragment.getInstance(
                    getResources().getString(R.string.recitation_setting_dialog_title),
                    Constants.Recitation.NAMES_STR_IDS, selectedRecitationSettingIndex
                    , this, RC_RECITATION_SETTING);
            recitationDialog.show(getActivity().getSupportFragmentManager(), "RecitationDialog");
        });

        binding.settingQuranReader.setOnClickListener(v -> {
            // TODO apply MVP or MVVM
            int recitationId = AppPreferencesManager.getRecitationSetting(requireContext());
            String reciterId = AppPreferencesManager.getReciterSheikhSetting(requireContext());
            QuranRecitersDialogFragment recitersDialog = QuranRecitersDialogFragment
                    .newInstance(recitationId, reciterId);
            recitersDialog.show(getChildFragmentManager(), "QuranRecitersDialogFragment");
        });

        binding.settingAudioDownloadManager.setOnClickListener(v -> {
            // TODO apply MVP or MVVM
            startActivity(new Intent(requireContext(), DownloadsManagerActivity.class));
        });

        binding.settingHelp.setOnClickListener(v -> {
            // TODO helpSetting click listener
        });

        binding.settingAboutAppVersion.setOnClickListener(v -> {
            // TODO aboutAppVersionSetting click listener
//            Toast.makeText(requireContext(), "v" + BuildConfig.VERSION_NAME,
//                    Toast.LENGTH_SHORT).show();
        });

        binding.settingShareApp.setOnClickListener(v -> {
            // TODO shareAppSetting click listener
        });
    }

    @Override
    public void onItemSelected(int requestCode, int itemIndex) {
        switch (requestCode) {
            case RC_APP_LANG_SETTING:
                int currentAppLanguageIndex = Constants.Language.CODES.indexOf(
                        AppPreferencesManager.getAppLangSetting(requireContext()));
                if (itemIndex != currentAppLanguageIndex) {
                    // save user setting & change app language
                    String langCode = Constants.Language.CODES.get(itemIndex);
                    AppPreferencesManager.persistAppLangSetting(requireContext(), langCode);
                    LocaleUtils.setAppLanguage(requireContext(), langCode);

                    ((BaseActivity) requireActivity()).restart();
                }
                break;
            case RC_TRANS_LANG_SETTING:
                // save user setting & change translation language
                String langCode = Constants.Language.CODES.get(itemIndex);
                AppPreferencesManager.persistQuranTranslationLanguage(requireContext(), langCode);
                binding.settingTranslationLang.setCurrentValue(
                        getString(Constants.Language.NAMES_STR_IDS[itemIndex]));
                break;
            case RC_RECITATION_SETTING:
                int selectedRecitationId = itemIndex;
                boolean isChanged = AppPreferencesManager.persistRecitationSetting(requireContext(), selectedRecitationId);
                if (isChanged) {
                    // update the current recitation setting & reset quran reader setting
                    binding.settingRecitation.setCurrentValue(
                            getString(Constants.Recitation.NAMES_STR_IDS[selectedRecitationId]));
                    binding.settingQuranReader.setCurrentValue(null);
                }
                break;
            default:
                Log.e(TAG, "onItemSelected() - unknown requestCode: " + requestCode);
        }

    }

    @Override
    public void onReciterSelected(int recitationId, @NonNull ReciterModel reciter) {
        Log.d(TAG, "onReciterSelected: recitationId=" + recitationId + " , reciterId=" + reciter.getId());

        AppPreferencesManager.persistReciterSheikhSetting(requireContext(), reciter.getId());
        binding.settingQuranReader.setCurrentValue(reciter.getLocalizedName(requireContext()));
    }
}
