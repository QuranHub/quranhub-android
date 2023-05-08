package app.quranhub.ui.first_wizard;

import static android.view.View.LAYOUT_DIRECTION_LTR;
import static android.view.View.LAYOUT_DIRECTION_RTL;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.RoomAsset;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.databinding.ActivityFirstTimeWizardBinding;
import app.quranhub.ui.base.BaseActivity;
import app.quranhub.ui.common.interfaces.Searchable;
import app.quranhub.ui.main.MainActivity;
import app.quranhub.util.LocaleUtils;

public class FirstTimeWizardActivity extends BaseActivity implements OptionsListFragment.OnOptionClickListener {

    private static final String TAG = FirstTimeWizardActivity.class.getSimpleName();

    private static final String STATE_CURRENT_STEP_POSITION = "STATE_CURRENT_STEP_POSITION";

    private static final int RC_APP_LANGUAGES_STEP = 0;
    private static final int RC_TRANSLATION_LANGUAGES_STEP = 1;
    private static final int RC_RECITATIONS_STEP = 2;

    private static final int NUM_PAGES = 3;

    private int appLanguagesStepPosition = 0; // first step
    private int translationLanguagesStepPosition = 1; // second step
    private int recitationsStepPosition = 2; // third & last step

    private int currentStepPosition = appLanguagesStepPosition;

    private ActivityFirstTimeWizardBinding binding;

    private WizardStepPagerAdapter wizardStepPagerAdapter;

    private String searchString = "";

    int layoutDir = LAYOUT_DIRECTION_LTR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstTimeWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        layoutDir = getResources().getConfiguration().getLayoutDirection();
        if (layoutDir == LAYOUT_DIRECTION_RTL) {
            initPagesPositionsForRtl();
        }

        if (savedInstanceState != null) {
            currentStepPosition = savedInstanceState.getInt(STATE_CURRENT_STEP_POSITION);
        }

        wizardStepPagerAdapter = new WizardStepPagerAdapter(getSupportFragmentManager());
        binding.pagerSteps.setAdapter(wizardStepPagerAdapter);
        binding.pagerSteps.setCurrentItem(appLanguagesStepPosition);
        updateViews(appLanguagesStepPosition);

        // Initialize Mus'haf metadata DB
        RoomAsset.initializeDatabase(this, MushafDatabase.DATABASE_NAME
                , MushafDatabase.ASSET_DB_VERSION);

        attachListeners();
    }

    private void attachListeners() {
        binding.pagerSteps.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // TODO apply MVP or MVVM
                Log.d(TAG, "onPageSelected() callback called");
                updateViews(position);
                if (position != currentStepPosition) {
                    // This is not a configuration change; you don't want to reset the search on config changes.
                    resetSearch();
                    currentStepPosition = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO apply MVP or MVVM
                searchOptions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnBack.setOnClickListener(v -> backButtonClicked());

        binding.btnNext.setOnClickListener(v -> nextButtonClicked());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_CURRENT_STEP_POSITION, currentStepPosition);
    }

    /**
     * Modify pages position to allow correct RTL swiping.
     */
    private void initPagesPositionsForRtl() {
        appLanguagesStepPosition = 2;  // first page for us, last page for the viewpager
        translationLanguagesStepPosition = 1; // second page for us & the viewpager
        recitationsStepPosition = 0; // third & last page for us, first page for the viewpager

        currentStepPosition = appLanguagesStepPosition;
    }

    private void searchOptions(@NonNull String str) {
        Searchable searchableFragment = (Searchable) wizardStepPagerAdapter.getCurrentFragment();
        if (searchableFragment != null) {
            searchableFragment.search(str);
        } else {
            Log.e(TAG, "Couldn't search the options list as the current view pager fragment is null");
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.pagerSteps.getCurrentItem() == appLanguagesStepPosition) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            openPreviousStepPage();
        }
    }

    private void backButtonClicked() {
        // TODO apply MVP or MVVM
        openPreviousStepPage();
    }

    private void nextButtonClicked() {
        // TODO ally MVP or MVVM
        openNextStepPage();
    }

    private void openNextStepPage() {
        int currentPageIndex = binding.pagerSteps.getCurrentItem();
        if (layoutDir == LAYOUT_DIRECTION_LTR && currentPageIndex < NUM_PAGES - 1) {
            // navigate to the next page
            binding.pagerSteps.setCurrentItem(++currentPageIndex);
        } else if (layoutDir == LAYOUT_DIRECTION_RTL && currentPageIndex > 0) {
            // navigate to the next page
            binding.pagerSteps.setCurrentItem(--currentPageIndex);
        } else {
            finishWizard();
        }
    }

    private void openPreviousStepPage() {
        int currentPageIndex = binding.pagerSteps.getCurrentItem();
        if (layoutDir == LAYOUT_DIRECTION_LTR && currentPageIndex > 0) {
            // navigate to the previous page
            binding.pagerSteps.setCurrentItem(--currentPageIndex);
        } else if (layoutDir == LAYOUT_DIRECTION_RTL && currentPageIndex < NUM_PAGES - 1) {
            // navigate to the previous page
            binding.pagerSteps.setCurrentItem(++currentPageIndex);
        }
    }

    /**
     * Navigate to main activity and mark wizard as done
     */
    private void finishWizard() {
        binding.btnNext.setEnabled(false);
        AppPreferencesManager.markFirstTimeWizardDone(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void updateViews(int currentStepPageIndex) {

        // update the step hint
        if (currentStepPageIndex == appLanguagesStepPosition) {
            setTitle(getString(R.string.first_wizard_title_app_language_step));
            binding.tvStepHint.setText(R.string.first_wizard_hint_app_langauge);
        } else if (currentStepPageIndex == translationLanguagesStepPosition) {
            setTitle(getString(R.string.first_wizard_title_translation_languages_step));
            binding.tvStepHint.setText(getString(R.string.first_wizard_hint_translation_languages));
        } else if (currentStepPageIndex == recitationsStepPosition) {
            setTitle(getString(R.string.first_wizard_title_recitations_step));
            binding.tvStepHint.setText(getString(R.string.first_wizard_hint_recitations));
        }

        // update progress
        showStepProgress(currentStepPageIndex);

        // update buttons
        if (currentStepPageIndex == recitationsStepPosition) {
            // on last page
            binding.btnNext.setText(R.string.finish);
            binding.btnBack.setEnabled(true);
        } else if (currentStepPageIndex == appLanguagesStepPosition) {
            // on first page
            binding.btnNext.setText(R.string.next);
            binding.btnBack.setEnabled(false);
        } else {
            // default
            binding.btnNext.setText(R.string.next);
            binding.btnBack.setEnabled(true);
        }
    }

    private void resetSearch() {
        binding.etSearch.getText().clear();
        binding.etSearch.clearFocus();
        searchOptions("");
    }

    private void showStepProgress(int currentStepPageIndex) {
        if (currentStepPageIndex == appLanguagesStepPosition) {
            // first step
            binding.ivProgressPage1.setImageResource(R.drawable.check_gold_ic);
            binding.ivProgressPage1.setBackgroundResource(R.drawable.progress_circle_checked);

            binding.separatorPages12.setBackgroundResource(R.color.color_control_highlight);

            binding.ivProgressPage2.setImageDrawable(null);
            binding.ivProgressPage2.setBackgroundResource(R.drawable.progress_circle_unchecked);

            binding.separatorPages23.setBackgroundResource(R.color.color_control_highlight);

            binding.ivProgressPage3.setImageDrawable(null);
            binding.ivProgressPage3.setBackgroundResource(R.drawable.progress_circle_unchecked);
        } else if (currentStepPageIndex == translationLanguagesStepPosition) {
            // second step
            binding.ivProgressPage1.setImageResource(R.drawable.check_gold_ic);
            binding.ivProgressPage1.setBackgroundResource(R.drawable.progress_circle_checked);

            binding.separatorPages12.setBackgroundResource(R.color.color_primary);

            binding.ivProgressPage2.setImageResource(R.drawable.check_gold_ic);
            binding.ivProgressPage2.setBackgroundResource(R.drawable.progress_circle_checked);

            binding.separatorPages23.setBackgroundResource(R.color.color_control_highlight);

            binding.ivProgressPage3.setImageDrawable(null);
            binding.ivProgressPage3.setBackgroundResource(R.drawable.progress_circle_unchecked);
        } else if (currentStepPageIndex == recitationsStepPosition) {
            // last (third) step
            binding.ivProgressPage1.setImageResource(R.drawable.check_gold_ic);
            binding.ivProgressPage1.setBackgroundResource(R.drawable.progress_circle_checked);

            binding.separatorPages12.setBackgroundResource(R.color.color_primary);

            binding.ivProgressPage2.setImageResource(R.drawable.check_gold_ic);
            binding.ivProgressPage2.setBackgroundResource(R.drawable.progress_circle_checked);

            binding.separatorPages23.setBackgroundResource(R.color.color_primary);

            binding.ivProgressPage3.setImageResource(R.drawable.check_gold_ic);
            binding.ivProgressPage3.setBackgroundResource(R.drawable.progress_circle_checked);
        }
    }

    @Override
    public void onOptionClicked(int requestCode, @NonNull String option, int position) {
        // TODO apply MVP or MVVM
        switch (requestCode) {
            case RC_APP_LANGUAGES_STEP:
                String selectedLangCode = Constants.Language.CODES.get(position);
                if (!selectedLangCode.equals(AppPreferencesManager.getAppLangSetting(this))) {
                    AppPreferencesManager.persistAppLangSetting(this, selectedLangCode);
                    LocaleUtils.setAppLanguage(this, selectedLangCode);
                    restart();
                }
                break;
            case RC_TRANSLATION_LANGUAGES_STEP:
                String selectedTransLangCode = Constants.Language.CODES.get(position);
                AppPreferencesManager.persistQuranTranslationLanguage(this, selectedTransLangCode);
                break;
            case RC_RECITATIONS_STEP:
                int selectedRecitationId = position;
                AppPreferencesManager.persistRecitationSetting(this, selectedRecitationId);
                break;
        }
    }


    private class WizardStepPagerAdapter extends FragmentStatePagerAdapter {

        private Fragment currentFragment;

        WizardStepPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == appLanguagesStepPosition) {
                int selectedAppLanguageIndex =
                        Constants.Language.CODES.indexOf(
                                AppPreferencesManager.getAppLangSetting(FirstTimeWizardActivity.this));
                return OptionsListFragment.newInstance(
                        FirstTimeWizardActivity.this,
                        Constants.Language.NAMES_STR_IDS,
                        Constants.Language.FLAGS_DRAWABLE_IDS,
                        selectedAppLanguageIndex,
                        RC_APP_LANGUAGES_STEP);
            } else if (position == translationLanguagesStepPosition) {
                int selectedTranslationLanguageIndex =
                        Constants.Language.CODES.indexOf(
                                AppPreferencesManager.getQuranTranslationLanguage(FirstTimeWizardActivity.this));
                return OptionsListFragment.newInstance(
                        FirstTimeWizardActivity.this,
                        Constants.Language.NAMES_STR_IDS,
                        Constants.Language.FLAGS_DRAWABLE_IDS,
                        selectedTranslationLanguageIndex,
                        RC_TRANSLATION_LANGUAGES_STEP);
            } else if (position == recitationsStepPosition) {
                int selectedRecitationIndex =
                        AppPreferencesManager.getRecitationSetting(FirstTimeWizardActivity.this);
                return OptionsListFragment.newInstance(
                        FirstTimeWizardActivity.this,
                        Constants.Recitation.NAMES_STR_IDS,
                        selectedRecitationIndex,
                        RC_RECITATIONS_STEP);
            } else {
                return null;
            }

        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (currentFragment != object) {
                currentFragment = (Fragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        public Fragment getCurrentFragment() {
            return currentFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
