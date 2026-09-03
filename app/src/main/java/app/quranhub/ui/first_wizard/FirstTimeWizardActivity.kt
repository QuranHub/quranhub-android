package app.quranhub.ui.first_wizard

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.viewpager.widget.ViewPager
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.databinding.ActivityFirstTimeWizardBinding
import app.quranhub.ui.base.BaseActivity
import app.quranhub.ui.first_wizard.OptionsListFragment.OnOptionClickListener
import app.quranhub.ui.main.MainActivity
import app.quranhub.util.InsetsUtils
import app.quranhub.util.LocaleUtils.setAppLanguage
import kotlinx.coroutines.launch

class FirstTimeWizardActivity : BaseActivity(), OnOptionClickListener {

    private var binding: ActivityFirstTimeWizardBinding? = null
    private var wizardStepPagerAdapter: WizardStepPagerAdapter? = null
    private var layoutDir = View.LAYOUT_DIRECTION_LTR
    private var appliedSearchQuery: String? = null

    private val viewModel: FirstTimeWizardViewModel by viewModels {
        viewModelFactory {
            initializer {
                FirstTimeWizardViewModel(application)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstTimeWizardBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        InsetsUtils.padTopForStatusBar(binding!!.appBar)
        InsetsUtils.padBottomForNavigationBar(binding!!.clBottomBar)
        layoutDir = resources.configuration.layoutDirection
        wizardStepPagerAdapter = WizardStepPagerAdapter(supportFragmentManager)
        binding!!.pagerSteps.adapter = wizardStepPagerAdapter
        binding!!.pagerSteps.currentItem = pagerIndexOfStep(viewModel.currentStep.value)
        updateViews(viewModel.currentStep.value)
        attachListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentStep.collect { step ->
                        val pagerIndex = pagerIndexOfStep(step)
                        if (binding!!.pagerSteps.currentItem != pagerIndex) {
                            binding!!.pagerSteps.currentItem = pagerIndex
                        }
                        updateViews(step)
                    }
                }
                launch {
                    viewModel.searchQuery.collect { query ->
                        // keep the search box in sync; each fragment applies the
                        // query to its own options list
                        if (query != appliedSearchQuery) {
                            appliedSearchQuery = query
                            if (binding!!.etSearch.text.toString() != query) {
                                if (query.isEmpty()) {
                                    binding!!.etSearch.text.clear()
                                    binding!!.etSearch.clearFocus()
                                } else {
                                    binding!!.etSearch.setText(query)
                                }
                            }
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is FirstTimeWizardViewModel.WizardEvent.AppLanguageChanged -> {
                                // change app language & restart to apply it
                                setAppLanguage(this@FirstTimeWizardActivity, event.langCode)
                                restart()
                            }

                            is FirstTimeWizardViewModel.WizardEvent.WizardFinished -> {
                                startActivity(
                                    Intent(
                                        this@FirstTimeWizardActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun attachListeners() {
        binding!!.pagerSteps.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected() callback called")
                viewModel.onStepSelected(stepOfPagerIndex(position))
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding!!.btnBack.setOnClickListener { backButtonClicked() }
        binding!!.btnNext.setOnClickListener { nextButtonClicked() }
    }

    /**
     * Maps a logical wizard step (0: first, 2: last) to its index in the view pager,
     * which is reversed in RTL to allow correct swiping.
     */
    private fun pagerIndexOfStep(step: Int): Int =
        if (layoutDir == View.LAYOUT_DIRECTION_RTL) NUM_PAGES - 1 - step else step

    /**
     * Inverse of [pagerIndexOfStep].
     */
    private fun stepOfPagerIndex(pagerIndex: Int): Int =
        if (layoutDir == View.LAYOUT_DIRECTION_RTL) NUM_PAGES - 1 - pagerIndex else pagerIndex

    override fun onBackPressed() {
        if (viewModel.currentStep.value == FirstTimeWizardViewModel.FIRST_STEP) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewModel.onBackClicked()
        }
    }

    private fun backButtonClicked() {
        viewModel.onBackClicked()
    }

    private fun nextButtonClicked() {
        if (viewModel.currentStep.value == FirstTimeWizardViewModel.LAST_STEP) {
            // disable the button while finishing to avoid duplicate clicks
            binding!!.btnNext.isEnabled = false
        }
        viewModel.onNextClicked()
    }

    private fun updateViews(currentStep: Int) {

        // update the step hint
        when (currentStep) {
            FirstTimeWizardViewModel.FIRST_STEP -> {
                title = getString(R.string.first_wizard_title_app_language_step)
                binding!!.tvStepHint.setText(R.string.first_wizard_hint_app_langauge)
            }

            FirstTimeWizardViewModel.TRANSLATIONS_STEP -> {
                title = getString(R.string.first_wizard_title_translation_languages_step)
                binding!!.tvStepHint.text = getString(R.string.first_wizard_hint_translation_languages)
            }

            FirstTimeWizardViewModel.LAST_STEP -> {
                title = getString(R.string.first_wizard_title_recitations_step)
                binding!!.tvStepHint.text = getString(R.string.first_wizard_hint_recitations)
            }
        }

        // update progress
        showStepProgress(currentStep)

        // update buttons
        if (currentStep == FirstTimeWizardViewModel.LAST_STEP) {
            // on last page
            binding!!.btnNext.setText(R.string.finish)
            binding!!.btnBack.isEnabled = true
        } else if (currentStep == FirstTimeWizardViewModel.FIRST_STEP) {
            // on first page
            binding!!.btnNext.setText(R.string.next)
            binding!!.btnBack.isEnabled = false
        } else {
            // default
            binding!!.btnNext.setText(R.string.next)
            binding!!.btnBack.isEnabled = true
        }
    }

    private fun showStepProgress(currentStep: Int) {
        when (currentStep) {
            FirstTimeWizardViewModel.FIRST_STEP -> {
                // first step
                binding!!.ivProgressPage1.setImageResource(R.drawable.check_gold_ic)
                binding!!.ivProgressPage1.setBackgroundResource(R.drawable.progress_circle_checked)
                binding!!.separatorPages12.setBackgroundResource(R.color.color_control_highlight)
                binding!!.ivProgressPage2.setImageDrawable(null)
                binding!!.ivProgressPage2.setBackgroundResource(R.drawable.progress_circle_unchecked)
                binding!!.separatorPages23.setBackgroundResource(R.color.color_control_highlight)
                binding!!.ivProgressPage3.setImageDrawable(null)
                binding!!.ivProgressPage3.setBackgroundResource(R.drawable.progress_circle_unchecked)
            }

            FirstTimeWizardViewModel.TRANSLATIONS_STEP -> {
                // second step
                binding!!.ivProgressPage1.setImageResource(R.drawable.check_gold_ic)
                binding!!.ivProgressPage1.setBackgroundResource(R.drawable.progress_circle_checked)
                binding!!.separatorPages12.setBackgroundResource(R.color.color_primary)
                binding!!.ivProgressPage2.setImageResource(R.drawable.check_gold_ic)
                binding!!.ivProgressPage2.setBackgroundResource(R.drawable.progress_circle_checked)
                binding!!.separatorPages23.setBackgroundResource(R.color.color_control_highlight)
                binding!!.ivProgressPage3.setImageDrawable(null)
                binding!!.ivProgressPage3.setBackgroundResource(R.drawable.progress_circle_unchecked)
            }

            FirstTimeWizardViewModel.LAST_STEP -> {
                // last (third) step
                binding!!.ivProgressPage1.setImageResource(R.drawable.check_gold_ic)
                binding!!.ivProgressPage1.setBackgroundResource(R.drawable.progress_circle_checked)
                binding!!.separatorPages12.setBackgroundResource(R.color.color_primary)
                binding!!.ivProgressPage2.setImageResource(R.drawable.check_gold_ic)
                binding!!.ivProgressPage2.setBackgroundResource(R.drawable.progress_circle_checked)
                binding!!.separatorPages23.setBackgroundResource(R.color.color_primary)
                binding!!.ivProgressPage3.setImageResource(R.drawable.check_gold_ic)
                binding!!.ivProgressPage3.setBackgroundResource(R.drawable.progress_circle_checked)
            }
        }
    }

    override fun onOptionClicked(requestCode: Int, option: String, position: Int) {
        when (requestCode) {
            RC_APP_LANGUAGES_STEP -> viewModel.onAppLanguageSelected(position)

            RC_TRANSLATION_LANGUAGES_STEP -> viewModel.onTranslationLanguageSelected(position)

            RC_RECITATIONS_STEP -> viewModel.onRecitationSelected(position)
        }
    }

    private inner class WizardStepPagerAdapter(fm: FragmentManager?) :
        FragmentStatePagerAdapter(
            fm!!
        ) {
        var currentFragment: Fragment? = null
            private set

        override fun getItem(position: Int): Fragment {
            return when (stepOfPagerIndex(position)) {
                FirstTimeWizardViewModel.FIRST_STEP -> {
                    OptionsListFragment.newInstance(
                        this@FirstTimeWizardActivity,
                        Constants.Language.NAMES_STR_IDS,
                        Constants.Language.FLAGS_DRAWABLE_IDS,
                        viewModel.uiState.value.appLangIndex,
                        RC_APP_LANGUAGES_STEP
                    )
                }

                FirstTimeWizardViewModel.TRANSLATIONS_STEP -> {
                    OptionsListFragment.newInstance(
                        this@FirstTimeWizardActivity,
                        Constants.Language.NAMES_STR_IDS,
                        Constants.Language.FLAGS_DRAWABLE_IDS,
                        viewModel.uiState.value.translationLangIndex,
                        RC_TRANSLATION_LANGUAGES_STEP
                    )
                }

                FirstTimeWizardViewModel.LAST_STEP -> {
                    OptionsListFragment.newInstance(
                        this@FirstTimeWizardActivity,
                        Constants.Recitation.NAMES_STR_IDS,
                        viewModel.uiState.value.recitationIndex,
                        RC_RECITATIONS_STEP
                    )
                }

                else -> error("Invalid position: $position")
            }
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if (currentFragment !== `object`) {
                currentFragment = `object` as Fragment
            }
            super.setPrimaryItem(container, position, `object`)
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    companion object {
        private val TAG = FirstTimeWizardActivity::class.java.simpleName

        const val RC_APP_LANGUAGES_STEP = 0
        const val RC_TRANSLATION_LANGUAGES_STEP = 1
        const val RC_RECITATIONS_STEP = 2

        private const val NUM_PAGES = 3
    }
}
