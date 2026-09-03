package app.quranhub.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.quranhub.BuildConfig
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.model.ReciterModel
import app.quranhub.databinding.FragmentSettingsBinding
import app.quranhub.ui.base.BaseActivity
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment
import app.quranhub.ui.downloads_manager.DownloadsManagerActivity
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import app.quranhub.ui.settings.custom.MushafSettingSwitch
import app.quranhub.util.LocaleUtils.setAppLanguage
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(), OptionsListDialogFragment.ItemSelectionListener,
    ReciterSelectionListener {

    private var binding: FragmentSettingsBinding? = null

    private val viewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer {
                SettingsViewModel(requireActivity().application)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSettingsViewsListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { render(it) }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is SettingsViewModel.SettingsEvent.AppLanguageChanged -> {
                                // change app language
                                setAppLanguage(requireContext(), event.langCode)
                                (requireActivity() as BaseActivity).restart()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(state: SettingsViewModel.SettingsUiState) {
        val b = binding ?: return
        b.settingAppLang.currentValue = state.appLangName
        b.settingTranslationLang.currentValue = state.translationLangName
        b.settingScreenReadingBacklight.isChecked = state.screenReadingBacklight
        b.settingLastReadPage.isChecked = state.lastReadPage
        b.settingRecitation.currentValue = state.recitationName
        b.settingQuranReader.currentValue = state.reciterName
    }

    private fun setSettingsViewsListeners() {
        binding!!.settingAppLang.setOnClickListener {
            val appLangDialog = OptionsListDialogFragment.getInstance(
                getString(R.string.app_lang_setting_dialog_title),
                Constants.Language.NAMES_STR_IDS,
                Constants.Language.FLAGS_DRAWABLE_IDS,
                viewModel.uiState.value.appLangIndex, this, RC_APP_LANG_SETTING
            )
            appLangDialog.show(
                requireActivity().supportFragmentManager, "AppLangDialog"
            )
        }
        binding!!.settingTranslationLang.setOnClickListener {
            val translationLangDialog = OptionsListDialogFragment.getInstance(
                getString(R.string.translation_lang_setting_dialog_title),
                Constants.Language.NAMES_STR_IDS,
                Constants.Language.FLAGS_DRAWABLE_IDS,
                viewModel.uiState.value.translationLangIndex, this, RC_TRANS_LANG_SETTING
            )
            translationLangDialog.show(
                requireActivity().supportFragmentManager, "TransLangDialog"
            )
        }
        binding!!.settingScreenReadingBacklight.setOnCheckedChangeListener(object :
            MushafSettingSwitch.OnCheckedChangeListener {
            override fun onCheckedChanged(settingSwitch: MushafSettingSwitch, checked: Boolean) {
                viewModel.onScreenReadingBacklightChanged(checked)
            }
        })
        binding!!.settingLastReadPage.setOnCheckedChangeListener(object :
            MushafSettingSwitch.OnCheckedChangeListener {
            override fun onCheckedChanged(settingSwitch: MushafSettingSwitch, checked: Boolean) {
                viewModel.onLastReadPageChanged(checked)
            }
        })
        binding!!.settingRecitation.setOnClickListener {
            val recitationDialog = OptionsListDialogFragment.getInstance(
                resources.getString(R.string.recitation_setting_dialog_title),
                Constants.Recitation.NAMES_STR_IDS,
                viewModel.uiState.value.recitationIndex,
                this,
                RC_RECITATION_SETTING
            )
            recitationDialog.show(requireActivity().supportFragmentManager, "RecitationDialog")
        }
        binding!!.settingQuranReader.setOnClickListener {
            val recitationId = viewModel.uiState.value.recitationIndex
            val reciterId = viewModel.uiState.value.reciterId
            val recitersDialog = QuranRecitersDialogFragment
                .newInstance(recitationId, reciterId)
            recitersDialog.show(childFragmentManager, "QuranRecitersDialogFragment")
        }
        binding!!.settingAudioDownloadManager.setOnClickListener {
            startActivity(Intent(requireContext(), DownloadsManagerActivity::class.java))
        }
        binding!!.settingAboutAppVersion.setOnClickListener {
            // TODO aboutAppVersionSetting click listener
            Toast.makeText(
                requireContext(), "v" + BuildConfig.VERSION_NAME,
                Toast.LENGTH_SHORT
            ).show()
        }
        binding!!.settingShareApp.setOnClickListener {
            // TODO shareAppSetting click listener
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=app.quranhub"
            )
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_title)))
        }
    }

    override fun onItemSelected(requestCode: Int, itemIndex: Int) {
        when (requestCode) {
            RC_APP_LANG_SETTING -> viewModel.onAppLanguageSelected(itemIndex)

            RC_TRANS_LANG_SETTING -> viewModel.onTranslationLanguageSelected(itemIndex)

            RC_RECITATION_SETTING -> viewModel.onRecitationSelected(itemIndex)

            else -> Log.e(TAG, "onItemSelected() - unknown requestCode: $requestCode")
        }
    }

    override fun onReciterSelected(recitationId: Int, reciter: ReciterModel) {
        Log.d(TAG, "onReciterSelected: recitationId=" + recitationId + " , reciterId=" + reciter.id)
        viewModel.onReciterSelected(reciter)
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName

        private const val RC_APP_LANG_SETTING = 1
        private const val RC_TRANS_LANG_SETTING = 2
        private const val RC_RECITATION_SETTING = 3
    }
}
