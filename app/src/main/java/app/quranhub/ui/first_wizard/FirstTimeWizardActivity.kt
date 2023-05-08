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
import androidx.viewpager.widget.ViewPager
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.RoomAsset
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.databinding.ActivityFirstTimeWizardBinding
import app.quranhub.ui.base.BaseActivity
import app.quranhub.ui.common.interfaces.Searchable
import app.quranhub.ui.first_wizard.OptionsListFragment.OnOptionClickListener
import app.quranhub.ui.main.MainActivity
import app.quranhub.util.LocaleUtils.setAppLanguage

class FirstTimeWizardActivity : BaseActivity(), OnOptionClickListener {

    private var appLanguagesStepPosition = 0 // first step
    private var translationLanguagesStepPosition = 1 // second step
    private var recitationsStepPosition = 2 // third & last step
    private var currentStepPosition = appLanguagesStepPosition
    private var binding: ActivityFirstTimeWizardBinding? = null
    private var wizardStepPagerAdapter: WizardStepPagerAdapter? = null
    private val searchString = ""
    var layoutDir = View.LAYOUT_DIRECTION_LTR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstTimeWizardBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        layoutDir = resources.configuration.layoutDirection
        if (layoutDir == View.LAYOUT_DIRECTION_RTL) {
            initPagesPositionsForRtl()
        }
        if (savedInstanceState != null) {
            currentStepPosition = savedInstanceState.getInt(STATE_CURRENT_STEP_POSITION)
        }
        wizardStepPagerAdapter = WizardStepPagerAdapter(supportFragmentManager)
        binding!!.pagerSteps.adapter = wizardStepPagerAdapter
        binding!!.pagerSteps.currentItem = appLanguagesStepPosition
        updateViews(appLanguagesStepPosition)

        // Initialize Mus'haf metadata DB
        RoomAsset.initializeDatabase(
            this, MushafDatabase.DATABASE_NAME, MushafDatabase.ASSET_DB_VERSION
        )
        attachListeners()
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
                // TODO apply MVP or MVVM
                Log.d(TAG, "onPageSelected() callback called")
                updateViews(position)
                if (position != currentStepPosition) {
                    // This is not a configuration change; you don't want to reset the search on config changes.
                    resetSearch()
                    currentStepPosition = position
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // TODO apply MVP or MVVM
                searchOptions(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding!!.btnBack.setOnClickListener { v: View? -> backButtonClicked() }
        binding!!.btnNext.setOnClickListener { v: View? -> nextButtonClicked() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_CURRENT_STEP_POSITION, currentStepPosition)
    }

    /**
     * Modify pages position to allow correct RTL swiping.
     */
    private fun initPagesPositionsForRtl() {
        appLanguagesStepPosition = 2 // first page for us, last page for the viewpager
        translationLanguagesStepPosition = 1 // second page for us & the viewpager
        recitationsStepPosition = 0 // third & last page for us, first page for the viewpager
        currentStepPosition = appLanguagesStepPosition
    }

    private fun searchOptions(str: String) {
        val searchableFragment = wizardStepPagerAdapter?.currentFragment as? Searchable?
        searchableFragment?.search(str)
            ?: Log.e(
                TAG,
                "Couldn't search the options list as the current view pager fragment is null"
            )
    }

    override fun onBackPressed() {
        if (binding!!.pagerSteps.currentItem == appLanguagesStepPosition) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            openPreviousStepPage()
        }
    }

    private fun backButtonClicked() {
        // TODO apply MVP or MVVM
        openPreviousStepPage()
    }

    private fun nextButtonClicked() {
        // TODO ally MVP or MVVM
        openNextStepPage()
    }

    private fun openNextStepPage() {
        var currentPageIndex = binding!!.pagerSteps.currentItem
        if (layoutDir == View.LAYOUT_DIRECTION_LTR && currentPageIndex < NUM_PAGES - 1) {
            // navigate to the next page
            binding!!.pagerSteps.currentItem = ++currentPageIndex
        } else if (layoutDir == View.LAYOUT_DIRECTION_RTL && currentPageIndex > 0) {
            // navigate to the next page
            binding!!.pagerSteps.currentItem = --currentPageIndex
        } else {
            finishWizard()
        }
    }

    private fun openPreviousStepPage() {
        var currentPageIndex = binding!!.pagerSteps.currentItem
        if (layoutDir == View.LAYOUT_DIRECTION_LTR && currentPageIndex > 0) {
            // navigate to the previous page
            binding!!.pagerSteps.currentItem = --currentPageIndex
        } else if (layoutDir == View.LAYOUT_DIRECTION_RTL && currentPageIndex < NUM_PAGES - 1) {
            // navigate to the previous page
            binding!!.pagerSteps.currentItem = ++currentPageIndex
        }
    }

    /**
     * Navigate to main activity and mark wizard as done
     */
    private fun finishWizard() {
        binding!!.btnNext.isEnabled = false
        AppPreferencesManager.markFirstTimeWizardDone(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun updateViews(currentStepPageIndex: Int) {

        // update the step hint
        if (currentStepPageIndex == appLanguagesStepPosition) {
            title = getString(R.string.first_wizard_title_app_language_step)
            binding!!.tvStepHint.setText(R.string.first_wizard_hint_app_langauge)
        } else if (currentStepPageIndex == translationLanguagesStepPosition) {
            title = getString(R.string.first_wizard_title_translation_languages_step)
            binding!!.tvStepHint.text = getString(R.string.first_wizard_hint_translation_languages)
        } else if (currentStepPageIndex == recitationsStepPosition) {
            title = getString(R.string.first_wizard_title_recitations_step)
            binding!!.tvStepHint.text = getString(R.string.first_wizard_hint_recitations)
        }

        // update progress
        showStepProgress(currentStepPageIndex)

        // update buttons
        if (currentStepPageIndex == recitationsStepPosition) {
            // on last page
            binding!!.btnNext.setText(R.string.finish)
            binding!!.btnBack.isEnabled = true
        } else if (currentStepPageIndex == appLanguagesStepPosition) {
            // on first page
            binding!!.btnNext.setText(R.string.next)
            binding!!.btnBack.isEnabled = false
        } else {
            // default
            binding!!.btnNext.setText(R.string.next)
            binding!!.btnBack.isEnabled = true
        }
    }

    private fun resetSearch() {
        binding!!.etSearch.text.clear()
        binding!!.etSearch.clearFocus()
        searchOptions("")
    }

    private fun showStepProgress(currentStepPageIndex: Int) {
        when (currentStepPageIndex) {
            appLanguagesStepPosition -> {
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

            translationLanguagesStepPosition -> {
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

            recitationsStepPosition -> {
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
        // TODO apply MVP or MVVM
        when (requestCode) {
            RC_APP_LANGUAGES_STEP -> {
                val selectedLangCode = Constants.Language.CODES[position]
                if (selectedLangCode != AppPreferencesManager.getAppLangSetting(this)) {
                    AppPreferencesManager.persistAppLangSetting(this, selectedLangCode)
                    setAppLanguage(this, selectedLangCode)
                    restart()
                }
            }

            RC_TRANSLATION_LANGUAGES_STEP -> {
                val selectedTransLangCode = Constants.Language.CODES[position]
                AppPreferencesManager.persistQuranTranslationLanguage(this, selectedTransLangCode)
            }

            RC_RECITATIONS_STEP -> {
                AppPreferencesManager.persistRecitationSetting(this, position)
            }
        }
    }

    private inner class WizardStepPagerAdapter internal constructor(fm: FragmentManager?) :
        FragmentStatePagerAdapter(
            fm!!
        ) {
        var currentFragment: Fragment? = null
            private set

        override fun getItem(position: Int): Fragment {
            return when (position) {
                appLanguagesStepPosition -> {
                    val selectedAppLanguageIndex = Constants.Language.CODES.indexOf(
                        AppPreferencesManager.getAppLangSetting(this@FirstTimeWizardActivity)
                    )
                    OptionsListFragment.newInstance(
                        this@FirstTimeWizardActivity,
                        Constants.Language.NAMES_STR_IDS,
                        Constants.Language.FLAGS_DRAWABLE_IDS,
                        selectedAppLanguageIndex,
                        RC_APP_LANGUAGES_STEP
                    )
                }

                translationLanguagesStepPosition -> {
                    val selectedTranslationLanguageIndex = Constants.Language.CODES.indexOf(
                        AppPreferencesManager.getQuranTranslationLanguage(this@FirstTimeWizardActivity)
                    )
                    OptionsListFragment.newInstance(
                        this@FirstTimeWizardActivity,
                        Constants.Language.NAMES_STR_IDS,
                        Constants.Language.FLAGS_DRAWABLE_IDS,
                        selectedTranslationLanguageIndex,
                        RC_TRANSLATION_LANGUAGES_STEP
                    )
                }

                recitationsStepPosition -> {
                    val selectedRecitationIndex =
                        AppPreferencesManager.getRecitationSetting(this@FirstTimeWizardActivity)
                    OptionsListFragment.newInstance(
                        this@FirstTimeWizardActivity,
                        Constants.Recitation.NAMES_STR_IDS,
                        selectedRecitationIndex,
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

        private const val STATE_CURRENT_STEP_POSITION = "STATE_CURRENT_STEP_POSITION"

        private const val RC_APP_LANGUAGES_STEP = 0
        private const val RC_TRANSLATION_LANGUAGES_STEP = 1
        private const val RC_RECITATIONS_STEP = 2

        private const val NUM_PAGES = 3
    }
}