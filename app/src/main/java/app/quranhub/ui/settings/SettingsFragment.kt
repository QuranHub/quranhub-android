package app.quranhub.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.quranhub.BuildConfig
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.model.ReciterModel
import app.quranhub.databinding.FragmentSettingsBinding
import app.quranhub.ui.base.BaseActivity
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment
import app.quranhub.ui.downloads_manager.DownloadsManagerActivity
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import app.quranhub.ui.settings.custom.MushafSettingSwitch
import app.quranhub.util.LocaleUtils.setAppLanguage
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SettingsFragment : Fragment(), OptionsListDialogFragment.ItemSelectionListener,
    ReciterSelectionListener {

    private var binding: FragmentSettingsBinding? = null

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettingsViews()
        setSettingsViewsListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        compositeDisposable.dispose()
    }

    private fun initSettingsViews() {
        // appLangSetting
        val currentAppLanguageIndex = Constants.Language.CODES.indexOf(
            AppPreferencesManager.getAppLangSetting(requireContext())
        )
        binding!!.settingAppLang.currentValue =
            getString(Constants.Language.NAMES_STR_IDS[currentAppLanguageIndex])

        // translationLangSetting
        val currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
            AppPreferencesManager.getQuranTranslationLanguage(requireContext())
        )
        binding!!.settingTranslationLang.currentValue =
            getString(Constants.Language.NAMES_STR_IDS[currentTranslationLanguageIndex])

        // screenReadingBacklightSettingSwitch
        binding!!.settingScreenReadingBacklight.isChecked =
            AppPreferencesManager.getScreenReadingBacklightSetting(requireContext())

        // lastReadPageSettingSwitch
        binding!!.settingLastReadPage.isChecked =
            AppPreferencesManager.getLastReadPageSetting(requireContext())

        // recitationSetting
        val selectedRecitationIndex = AppPreferencesManager.getRecitationSetting(requireContext())
        binding!!.settingRecitation.currentValue =
            getString(Constants.Recitation.NAMES_STR_IDS[selectedRecitationIndex])

        // quranReaderSetting
        val reciterId = AppPreferencesManager.getReciterSheikhSetting(requireContext())
        if (reciterId != null) {
            val disposable = Single.create(
                SingleOnSubscribe { emitter: SingleEmitter<String?> ->
                    val reciter = UserDatabase.getInstance(requireContext())
                        .reciterDao
                        .getById(reciterId)
                    reciter?.name?.let { emitter.onSuccess(it) }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { reciterName: String? ->
                    binding!!.settingQuranReader.currentValue = reciterName
                }
            compositeDisposable.add(disposable)
        }
    }

    private fun setSettingsViewsListeners() {
        binding!!.settingAppLang.setOnClickListener { v: View? ->
            // TODO apply MVP or MVVM
            val currentAppLanguageIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getAppLangSetting(requireContext())
            )
            val appLangDialog = OptionsListDialogFragment.getInstance(
                getString(R.string.app_lang_setting_dialog_title),
                Constants.Language.NAMES_STR_IDS,
                Constants.Language.FLAGS_DRAWABLE_IDS,
                currentAppLanguageIndex, this, RC_APP_LANG_SETTING
            )
            appLangDialog.show(
                requireActivity().supportFragmentManager, "AppLangDialog"
            )
        }
        binding!!.settingTranslationLang.setOnClickListener { v: View? ->
            // TODO apply MVP or MVVM
            val currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getQuranTranslationLanguage(requireContext())
            )
            val translationLangDialog = OptionsListDialogFragment.getInstance(
                getString(R.string.translation_lang_setting_dialog_title),
                Constants.Language.NAMES_STR_IDS,
                Constants.Language.FLAGS_DRAWABLE_IDS,
                currentTranslationLanguageIndex, this, RC_TRANS_LANG_SETTING
            )
            translationLangDialog.show(
                requireActivity().supportFragmentManager, "TransLangDialog"
            )
        }
        binding!!.settingScreenReadingBacklight.setOnCheckedChangeListener(object :
            MushafSettingSwitch.OnCheckedChangeListener {
            override fun onCheckedChanged(settingSwitch: MushafSettingSwitch, checked: Boolean) {
                // TODO apply MVP or MVVM
                AppPreferencesManager.persistScreenReadingBacklightSetting(
                    requireContext(),
                    checked
                )
            }
        })
        binding!!.settingLastReadPage.setOnCheckedChangeListener(object :
            MushafSettingSwitch.OnCheckedChangeListener {
            override fun onCheckedChanged(settingSwitch: MushafSettingSwitch, checked: Boolean) {
                // TODO apply MVP or MVVM
                AppPreferencesManager.persistLastReadPageSetting(requireContext(), checked)
            }
        })
        binding!!.settingRecitation.setOnClickListener { v: View? ->
            // TODO apply MVP or MVVM
            val selectedRecitationSettingIndex =
                AppPreferencesManager.getRecitationSetting(requireContext())
            val recitationDialog = OptionsListDialogFragment.getInstance(
                resources.getString(R.string.recitation_setting_dialog_title),
                Constants.Recitation.NAMES_STR_IDS,
                selectedRecitationSettingIndex,
                this,
                RC_RECITATION_SETTING
            )
            recitationDialog.show(requireActivity().supportFragmentManager, "RecitationDialog")
        }
        binding!!.settingQuranReader.setOnClickListener { v: View? ->
            // TODO apply MVP or MVVM
            val recitationId = AppPreferencesManager.getRecitationSetting(requireContext())
            val reciterId = AppPreferencesManager.getReciterSheikhSetting(requireContext())
            val recitersDialog = QuranRecitersDialogFragment
                .newInstance(recitationId, reciterId)
            recitersDialog.show(childFragmentManager, "QuranRecitersDialogFragment")
        }
        binding!!.settingAudioDownloadManager.setOnClickListener { v: View? ->
            // TODO apply MVP or MVVM
            startActivity(Intent(requireContext(), DownloadsManagerActivity::class.java))
        }
        binding!!.settingHelp.setOnClickListener { v: View? -> }
        binding!!.settingAboutAppVersion.setOnClickListener { v: View? ->
            // TODO aboutAppVersionSetting click listener
            Toast.makeText(
                requireContext(), "v" + BuildConfig.VERSION_NAME,
                Toast.LENGTH_SHORT
            ).show()
        }
        binding!!.settingShareApp.setOnClickListener { v: View? -> }
    }

    override fun onItemSelected(requestCode: Int, itemIndex: Int) {
        when (requestCode) {
            RC_APP_LANG_SETTING -> {
                val currentAppLanguageIndex = Constants.Language.CODES.indexOf(
                    AppPreferencesManager.getAppLangSetting(requireContext())
                )
                if (itemIndex != currentAppLanguageIndex) {
                    // save user setting & change app language
                    val langCode = Constants.Language.CODES[itemIndex]
                    AppPreferencesManager.persistAppLangSetting(requireContext(), langCode)
                    setAppLanguage(requireContext(), langCode)
                    (requireActivity() as BaseActivity).restart()
                }
            }

            RC_TRANS_LANG_SETTING -> {
                // save user setting & change translation language
                val langCode = Constants.Language.CODES[itemIndex]
                AppPreferencesManager.persistQuranTranslationLanguage(requireContext(), langCode)
                binding!!.settingTranslationLang.currentValue =
                    getString(Constants.Language.NAMES_STR_IDS[itemIndex])
            }

            RC_RECITATION_SETTING -> {
                val isChanged = AppPreferencesManager.persistRecitationSetting(
                    requireContext(),
                    itemIndex
                )
                if (isChanged) {
                    // update the current recitation setting & reset quran reader setting
                    binding!!.settingRecitation.currentValue =
                        getString(Constants.Recitation.NAMES_STR_IDS[itemIndex])
                    binding!!.settingQuranReader.currentValue = null
                }
            }

            else -> Log.e(TAG, "onItemSelected() - unknown requestCode: $requestCode")
        }
    }

    override fun onReciterSelected(recitationId: Int, reciter: ReciterModel) {
        Log.d(TAG, "onReciterSelected: recitationId=" + recitationId + " , reciterId=" + reciter.id)
        AppPreferencesManager.persistReciterSheikhSetting(requireContext(), reciter.id)
        binding!!.settingQuranReader.currentValue = reciter.getLocalizedName(requireContext())
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName

        private const val RC_APP_LANG_SETTING = 1
        private const val RC_TRANS_LANG_SETTING = 2
        private const val RC_RECITATION_SETTING = 3
    }
}