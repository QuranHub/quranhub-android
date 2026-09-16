package app.quranhub.feature.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.quranhub.R
import app.quranhub.core.data.Constants
import app.quranhub.core.data.local.prefs.AppPreferencesManager
import app.quranhub.core.common.base.BaseActivity
import app.quranhub.core.ui.interfaces.ToolbarActionsListener
import app.quranhub.feature.downloads.DownloadsManagerActivity
import app.quranhub.feature.onboarding.FirstTimeWizardActivity
import app.quranhub.feature.mushaf.audio_manager.AyaAudioService
import app.quranhub.feature.mushaf.fragments.BookmarksFragment
import app.quranhub.feature.mushaf.fragments.MushafFragment
import app.quranhub.feature.mushaf.fragments.MyNotesFragment
import app.quranhub.feature.mushaf.fragments.QuranTopicsFragment
import app.quranhub.feature.mushaf.fragments.SearchFragment
import app.quranhub.feature.mushaf.fragments.SuraGuz2IndexFragment
import app.quranhub.feature.mushaf.fragments.TafseerFragment
import app.quranhub.feature.mushaf.fragments.TopicAyasFragment
import app.quranhub.feature.mushaf.fragments.TranslationsLibraryFragment
import app.quranhub.feature.mushaf.listener.QuranNavigationCallbacks
import app.quranhub.core.data.model.TopicCategory
import app.quranhub.feature.settings.SettingsActivity
import app.quranhub.core.ui.util.DrawerUtils
import app.quranhub.core.ui.util.DrawerUtils.Mus7afDrawerItemClickListener
import app.quranhub.core.ui.util.DrawerUtils.initDrawer
import app.quranhub.core.data.util.SharedPrefsUtils.getBoolean
import app.quranhub.core.data.util.SharedPrefsUtils.getInteger
import app.quranhub.core.common.util.addCrashlyticsCustomKeys
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.Drawer.OnDrawerListener
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(), ToolbarActionsListener, Mus7afDrawerItemClickListener,
    QuranNavigationCallbacks {

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                MainViewModel(application)
            }
        }
    }

    private var drawer: Drawer? = null
    private var currentFragment: String? = null
    private var onDrawerListener: OnDrawerListener? = null
    private var isDismissAllow = true

    private lateinit var notificationPermissionDelegate: NotificationPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        addCrashlyticsCustomKeys(this)

        notificationPermissionDelegate = NotificationPermissionDelegate(this)

        // Redirect to `FirstTimeWizardActivity` if not already done
        if (!AppPreferencesManager.isFirstTimeWizardDone(this)) {
            val intent = Intent(this, FirstTimeWizardActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContentView(R.layout.activity_main)
        observeOnDrawerOpen()
        drawer = initDrawer(this, savedInstanceState, onDrawerListener!!)
        if (savedInstanceState == null) {
            viewModel.computeLaunchDestination(
                intent.extras?.getBoolean(AyaAudioService.FROM_NOTIFICATION) == true
            )
            observeLaunchDestination()
        } else {
            setCurrentFragmentData(savedInstanceState.getString("fragment"))
        }
    }

    private fun observeLaunchDestination() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.launchEvents.collect { destination ->
                    when (destination) {
                        is MainViewModel.LaunchDestination.Notification ->
                            showMushafFragment(MushafFragment.newNotificationInstance(destination.ayaId))

                        is MainViewModel.LaunchDestination.LastReadPage ->
                            showMushafFragment(MushafFragment.newInstance(destination.pageNumber))

                        MainViewModel.LaunchDestination.Mushaf ->
                            showMushafFragment(MushafFragment())
                    }
                }
            }
        }
    }

    private fun showMushafFragment(fragment: MushafFragment) {
        currentFragment = "mushaf"
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, "Mushaf")
        transaction.commit()
    }

    private fun observeOnDrawerOpen() {
        onDrawerListener = object : OnDrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                isDismissAllow = true
            }

            override fun onDrawerClosed(drawerView: View) {
                isDismissAllow = true
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                dismissAudioPopup()
            }
        }
    }

    private fun dismissAudioPopup() {
        if (isDismissAllow && currentFragment == "mushaf") {
            isDismissAllow = false
            val fragment = supportFragmentManager.findFragmentByTag("Mushaf")
            if (fragment is MushafFragment) {
                fragment.dismissAudioPopup()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer?.saveInstanceState(outState)
        outState.putString("fragment", currentFragment)
    }

    override fun onStart() {
        super.onStart()
        isActivityActive = true

        notificationPermissionDelegate.askNotificationPermission()
    }

    override fun onStop() {
        super.onStop()
        isActivityActive = false
    }

    private fun setCurrentFragmentData(fragmentName: String?) {
        currentFragment = fragmentName
    }

    // handle new intent get in stack single-top when click on audio notification
    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (getIntent().extras != null && getIntent().extras!!.getBoolean(AyaAudioService.FROM_NOTIFICATION)
            || getBoolean(this, AyaAudioService.SERVICE_RUNNING, false)
        ) {
            val ayaId = getInteger(this, AyaAudioService.AYA_ID_KEY, 1)
            val mushafFragment = MushafFragment.newNotificationInstance(ayaId)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, mushafFragment, "Mushaf")
            transaction.commit()
        }
    }

    override fun onNavDrawerClick() {
        dismissAudioPopup()
        drawer!!.openDrawer()
    }

    override fun onSuraClick() {
        openIndex(SuraGuz2IndexFragment.SURA_INDEX_TAB)
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_INDEX.toLong(), false)
    }

    override fun onGuz2Click() {
        openIndex(SuraGuz2IndexFragment.GUZ2_INDEX_TAB)
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_INDEX.toLong(), false)
    }

    override fun onBookmarkClick() {
        openBookmarks()
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_BOOKMARKS.toLong(), false)
    }

    override fun selectNavDrawerItem(itemIdentifier: Long, fireOnClick: Boolean) {
        if (drawer!!.currentSelection == itemIdentifier) return
        drawer!!.setSelection(itemIdentifier, fireOnClick)
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen) {
            drawer!!.closeDrawer()
        } else if (currentFragment == "pdf_viewer") {
            super.onBackPressed()
            currentFragment = "translation"
        } else if (currentFragment != "mushaf") {
            backToMushaf()
        } else {
            super.onBackPressed()
        }
    }

    private fun backToMushaf() {
        val lastOpenedPage = Constants.Quran.NUM_OF_PAGES - getInteger(
            this, "last_open_page", Constants.Quran.NUM_OF_PAGES - 1
        )
        currentFragment = "mushaf"
        gotoQuranPage(lastOpenedPage)
    }

    override fun openIndex(indexTab: Int) {
        checkPrevFragment()
        drawer!!.closeDrawer()
        val suraGuz2IndexFragment = SuraGuz2IndexFragment.newInstance(indexTab)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, suraGuz2IndexFragment, "index")
        transaction.commit()
        currentFragment = "index"
    }

    override fun openTopics() {
        checkPrevFragment()
        val quranTopicsFragment = QuranTopicsFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, quranTopicsFragment)
        transaction.commit()
        currentFragment = "subjects"
    }

    override fun openLibrary() {
        checkPrevFragment()
        val fragment = TranslationsLibraryFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
        currentFragment = "library"
    }

    override fun openBookmarks() {
        checkPrevFragment()
        drawer!!.closeDrawer()
        val bookmarksFragment = BookmarksFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, bookmarksFragment)
        transaction.commit()
        currentFragment = "bookmark"
    }

    private fun checkPrevFragment() {
        val fragmentManager = supportFragmentManager
        if (currentFragment == "pdf_viewer") {
            fragmentManager.popBackStack()
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun openMyNotes() {
        checkPrevFragment()
        val fragment = MyNotesFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
        currentFragment = "notes"
    }

    override fun openSettings() {
        checkPrevFragment()
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun openDownloadsManager() {
        checkPrevFragment()
        startActivity(Intent(this, DownloadsManagerActivity::class.java))
    }

    override fun openMushaf() {
        drawer!!.closeDrawer()
        if (currentFragment != "mushaf") {
            checkPrevFragment()
            backToMushaf()
        }
    }

    fun openTafseerScreen(
        suraName: String?,
        suraNumber: Int,
        bookDbName: String?,
        bookName: String?,
        ayaNumber: Int
    ) {
        checkPrevFragment()
        currentFragment = "tafseer"
        val tafseerFragment =
            TafseerFragment.newInstance(suraName, suraNumber, bookDbName, bookName, ayaNumber)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, tafseerFragment)
        transaction.commit()
    }

    fun openSearchFragment() {
        checkPrevFragment()
        val fragmentManager = supportFragmentManager
        currentFragment = "search"
        val fragment = SearchFragment()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    fun openTopicAyasFragment(category: TopicCategory?) {
        checkPrevFragment()
        currentFragment = "tafseer"
        val fragment = TopicAyasFragment.getInstance(category)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun openTafseerScreen(bookDbName: String?, bookName: String?) {
        openTafseerScreen(
            resources.getStringArray(R.array.sura_name)[0], 1, bookDbName, bookName, 1
        )
    }

    override fun gotoQuranPage(pageNumber: Int) {
        currentFragment = "mushaf"
        val mushafFragment = MushafFragment.newInstance(pageNumber)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, mushafFragment, "Mushaf")
        transaction.commit()
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_MUSHAF.toLong(), false)
    }

    override fun gotoQuranPageAya(pageNumber: Int, ayaId: Int, addToStack: Boolean) {
        val mushafFragment = MushafFragment.newInstance(pageNumber, ayaId)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, mushafFragment, "Mushaf")
        if (addToStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        selectNavDrawerItem(DrawerUtils.IDENTIFIER_MUSHAF.toLong(), false)
        currentFragment = "mushaf"
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        var isActivityActive = false
    }
}