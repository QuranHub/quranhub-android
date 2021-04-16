package app.quranhub.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mikepenz.materialdrawer.Drawer;

import app.quranhub.data.Constants;
import app.quranhub.R;
import app.quranhub.ui.mushaf.audio_manager.AyaAudioService;
import app.quranhub.ui.base.BaseActivity;
import app.quranhub.data.repository.RecitationsRepository;
import app.quranhub.ui.downloads_manager.DownloadsManagerActivity;
import app.quranhub.ui.first_wizard.FirstTimeWizardActivity;
import app.quranhub.ui.mushaf.fragments.BookmarksFragment;
import app.quranhub.ui.mushaf.fragments.BooksLibraryFragment;
import app.quranhub.ui.mushaf.fragments.MushafFragment;
import app.quranhub.ui.mushaf.fragments.MyNotesFragment;
import app.quranhub.ui.mushaf.fragments.PdfViewerFragment;
import app.quranhub.ui.mushaf.fragments.QuranTopicsFragment;
import app.quranhub.ui.mushaf.fragments.SearchFragment;
import app.quranhub.ui.mushaf.fragments.SuraGuz2IndexFragment;
import app.quranhub.ui.mushaf.fragments.TafseerFragment;
import app.quranhub.ui.mushaf.fragments.TopicAyasFragment;
import app.quranhub.ui.mushaf.listener.QuranNavigationCallbacks;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.settings.SettingsActivity;
import app.quranhub.util.DrawerUtils;
import app.quranhub.util.SharedPrefsUtils;
import app.quranhub.util.UserPreferencesUtils;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;

public class MainActivity extends BaseActivity
        implements ToolbarActionsListener, DrawerUtils.Mus7afDrawerItemClickListener, QuranNavigationCallbacks {

    private Drawer drawer;
    private String currentFragment;
    private Drawer.OnDrawerListener onDrawerListener;
    private boolean isDismissAllow = true;
    public static boolean isActivityActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        // Redirect to `FirstTimeWizardActivity` if not already done
        if (!UserPreferencesUtils.isFirstTimeWizardDone(this)) {
            Intent intent = new Intent(this, FirstTimeWizardActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);
        observeOnDrawerOpen();
        drawer = DrawerUtils.initDrawer(this, savedInstanceState, onDrawerListener);
        if (savedInstanceState == null) {
            launchMushafFragment();
        } else {
            setCurrentFragmentData(savedInstanceState.getString("fragment"));
        }

        // TODO remove this
        RecitationsRepository recitationsRepository = new RecitationsRepository();
        recitationsRepository.getRecitersForRecitation("hafs");
    }

    private void observeOnDrawerOpen() {

        onDrawerListener = new Drawer.OnDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                isDismissAllow = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isDismissAllow = true;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                dismissAudioPopup();
            }
        };
    }

    private void dismissAudioPopup() {
        if (isDismissAllow && currentFragment.equals("mushaf")) {
            isDismissAllow = false;
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("Mushaf");
            if (fragment instanceof MushafFragment) {
                ((MushafFragment) fragment).dismissAudioPopup();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState = drawer.saveInstanceState(outState);
        outState.putString("fragment", currentFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityActive = false;
    }

    private void setCurrentFragmentData(String fragmentName) {
        currentFragment = fragmentName;
    }

    private void launchMushafFragment() {
        currentFragment = "mushaf";
        MushafFragment mushafFragment;

        // get current aya id if Main activity is launched from audio notification OR launch app with audio notification app
        if ((getIntent().getExtras() != null && getIntent().getExtras().getBoolean(AyaAudioService.FROM_NOTIFICATION)) || SharedPrefsUtils.getBoolean(this, AyaAudioService.SERVICE_RUNNING, false)) {
            int ayaId = SharedPrefsUtils.getInteger(this, AyaAudioService.AYA_ID_KEY, 1);
            mushafFragment = MushafFragment.newNotificationInstance(ayaId);
        } else if (UserPreferencesUtils.getLastReadPageSetting(this)) {
            int pageNumber = Constants.Quran.NUM_OF_PAGES - SharedPrefsUtils.getInteger(this
                    , "last_open_page", Constants.Quran.NUM_OF_PAGES - 1);
            mushafFragment = MushafFragment.newInstance(pageNumber);
        } else {
            mushafFragment = new MushafFragment();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mushafFragment, "Mushaf");
        transaction.commit();


    }

    // handle new intent get in stack single-top when click on audio notification
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((getIntent().getExtras() != null && getIntent().getExtras().getBoolean(AyaAudioService.FROM_NOTIFICATION)) || SharedPrefsUtils.getBoolean(this, AyaAudioService.SERVICE_RUNNING, false)) {
            int ayaId = SharedPrefsUtils.getInteger(this, AyaAudioService.AYA_ID_KEY, 1);
            MushafFragment mushafFragment = MushafFragment.newNotificationInstance(ayaId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, mushafFragment, "Mushaf");
            transaction.commit();
        }
    }

    @Override
    public void onNavDrawerClick() {
        dismissAudioPopup();
        drawer.openDrawer();
    }

    @Override
    public void onSuraClick() {
        openIndex(SuraGuz2IndexFragment.SURA_INDEX_TAB);
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_INDEX, false);
    }

    @Override
    public void onGuz2Click() {
        openIndex(SuraGuz2IndexFragment.GUZ2_INDEX_TAB);
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_INDEX, false);
    }

    @Override
    public void onBookmarkClick() {
        openBookmarks();
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_BOOKMARKS, false);
    }

    @Override
    public void selectNavDrawerItem(long itemIdentifier, boolean fireOnClick) {
        if (drawer.getCurrentSelection() == itemIdentifier)
            return;
        drawer.setSelection(itemIdentifier, fireOnClick);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (currentFragment.equals("pdf_viewer")) {
            super.onBackPressed();
            currentFragment = "translation";
        } else if (!currentFragment.equals("mushaf")) {
            backToMushaf();
        } else {
            super.onBackPressed();
        }
    }


    private void backToMushaf() {
        int lastOpenedPage = Constants.Quran.NUM_OF_PAGES - SharedPrefsUtils.getInteger(this
                , "last_open_page", Constants.Quran.NUM_OF_PAGES - 1);
        currentFragment = "mushaf";
        gotoQuranPage(lastOpenedPage);
    }


    @Override
    public void openIndex(int indexTab) {
        checkPrevFragment();
        drawer.closeDrawer();
        SuraGuz2IndexFragment suraGuz2IndexFragment =
                SuraGuz2IndexFragment.newInstance(indexTab);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, suraGuz2IndexFragment, "index");
        transaction.commit();
        currentFragment = "index";
    }

    @Override
    public void openTopics() {
        checkPrevFragment();
        QuranTopicsFragment quranTopicsFragment = new QuranTopicsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, quranTopicsFragment);
        transaction.commit();
        currentFragment = "subjects";
    }

    @Override
    public void openLibrary() {
        checkPrevFragment();
        BooksLibraryFragment fragment = new BooksLibraryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        currentFragment = "library";
    }

    @Override
    public void openBookmarks() {
        checkPrevFragment();
        drawer.closeDrawer();
        BookmarksFragment bookmarksFragment =
                BookmarksFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, bookmarksFragment);
        transaction.commit();
        currentFragment = "bookmark";
    }

    private void checkPrevFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (currentFragment.equals("pdf_viewer")) {
            fragmentManager.popBackStack();
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void openMyNotes() {
        checkPrevFragment();
        MyNotesFragment fragment = new MyNotesFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        currentFragment = "notes";
    }

    public void openPdfFragment(String fileName) {
        checkPrevFragment();
        Bundle bundle = new Bundle();
        bundle.putString("file_name", fileName);
        PdfViewerFragment pdfViewerFragment = new PdfViewerFragment();
        pdfViewerFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, pdfViewerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = "pdf_viewer";
    }

    @Override
    public void openSettings() {
        checkPrevFragment();
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void openDownloadsManager() {
        checkPrevFragment();
        startActivity(new Intent(this, DownloadsManagerActivity.class));
    }

    @Override
    public void openMushaf() {
        drawer.closeDrawer();
        if (!currentFragment.equals("mushaf")) {
            checkPrevFragment();
            backToMushaf();
        }
    }

    public void openTafseerScreen(String suraName, int suraNumber, String bookDbName, String bookName, int ayaNumber) {
        checkPrevFragment();
        currentFragment = "tafseer";
        TafseerFragment tafseerFragment = TafseerFragment.newInstance(suraName, suraNumber, bookDbName, bookName, ayaNumber);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, tafseerFragment);
        transaction.commit();
    }

    public void openSearchFragment() {
        checkPrevFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        currentFragment = "search";
        SearchFragment fragment = new SearchFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();

    }

    public void openTopicAyasFragment(TopicCategory category) {
        checkPrevFragment();
        currentFragment = "tafseer";
        TopicAyasFragment fragment = TopicAyasFragment.getInstance(category);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void openTafseerScreen(String bookDbName, String bookName) {
        openTafseerScreen(getResources().getStringArray(R.array.sura_name)[0]
                , 1, bookDbName, bookName, 1);
    }

    @Override
    public void gotoQuranPage(int pageNumber) {
        currentFragment = "mushaf";
        MushafFragment mushafFragment = MushafFragment.newInstance(pageNumber);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mushafFragment, "Mushaf");
        transaction.commit();
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_MUSHAF, false);
    }

    @Override
    public void gotoQuranPageAya(int pageNumber, int ayaId, boolean addToStack) {

        MushafFragment mushafFragment = MushafFragment.newInstance(pageNumber, ayaId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mushafFragment, "Mushaf");
        if (addToStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_MUSHAF, false);
        currentFragment = "mushaf";
    }

}
