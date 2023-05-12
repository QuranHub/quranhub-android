package app.quranhub.ui.downloads_manager

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import app.quranhub.R
import app.quranhub.data.model.ReciterModel
import app.quranhub.databinding.ActivityDownloadsManagerBinding
import app.quranhub.ui.base.BaseActivity
import app.quranhub.ui.downloads_manager.BaseDownloadsFragment.DownloadsManagerNavigationCallbacks
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment.AudioDownloadListener
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import com.google.android.material.tabs.TabLayout

class DownloadsManagerActivity : BaseActivity(), DownloadsManagerNavigationCallbacks,
    ReciterSelectionListener, AudioDownloadListener {

    private var hasMenu = false
    private var editable = false

    private var selectedTabIndex = 0

    private lateinit var binding: ActivityDownloadsManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadsManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTabIndex = tab?.position ?: 0
                when (tab?.position) {
                    0 -> showQuranImagesDownloadsTab()
                    1 -> showAudioDownloadsTab()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        if (savedInstanceState != null) {
            // restore saved instance state, if any,
            editable = savedInstanceState.getBoolean(STATE_EDITABLE)
            selectedTabIndex = savedInstanceState.getInt(STATE_SELECTED_TAB_INDEX, 0)
        } else {
            showQuranImagesDownloadsTab()
        }

        binding.tabLayout.getTabAt(selectedTabIndex)?.select()

        /* sync the activity's action bar with the BaseDownloadsFragment 'editable' state
           when user's back navigating */
        supportFragmentManager.addOnBackStackChangedListener {
            val downloadsFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container) as? BaseDownloadsFragment
            downloadsFragment?.getEditable()?.let {
                editable = it
                invalidateOptionsMenu()
            }
        }
    }

    private fun showQuranImagesDownloadsTab() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        val quranImagesFragment = DownloadsQuranImagesFragment.newInstance()
        supportFragmentManager.commit {
            replace(R.id.fragment_container, quranImagesFragment)
        }
        if (hasMenu) {
            // remove the edit menu item
            hasMenu = false
            invalidateOptionsMenu()
        }
    }

    private fun showAudioDownloadsTab() {
        editable = false
        val recitationsFragment = DownloadsRecitationsFragment
            .newInstance(this@DownloadsManagerActivity, editable)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, recitationsFragment)
        }
        if (!hasMenu) {
            // add the edit menu item
            hasMenu = true
            invalidateOptionsMenu()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_EDITABLE, editable)
        outState.putInt(STATE_SELECTED_TAB_INDEX, selectedTabIndex)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return if (hasMenu) {
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_downloads_manager, menu)
            val editMenuItem = menu.findItem(R.id.action_edit)
            updateActionBar(editMenuItem)
            true
        } else {
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_edit) {
            editable = !editable
            updateActionBar(item)

            // notify child fragment
            try {
                val editableFragment = supportFragmentManager
                    .findFragmentById(R.id.fragment_container) as Editable?
                if (editableFragment != null) {
                    editableFragment.isEditable = editable
                }
            } catch (e: ClassCastException) {
                Log.e(
                    TAG, "Cannot cast the child fragment to Editable." +
                            " Did you implement the Editable interface?"
                )
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateActionBar(editMenuItem: MenuItem) {
        // update the menu item icon & the activity title
        if (editable) {
            editMenuItem.setIcon(R.drawable.check_gold_ic)
            setTitle(R.string.title_activity_downloads_manager_edit_enabled)
        } else {
            editMenuItem.setIcon(R.drawable.edit_gold_ic)
            setTitle(R.string.title_activity_downloads_manager)
        }
    }

    override fun gotoDownloadsRecitations() {
        val recitationsFragment = DownloadsRecitationsFragment
            .newInstance(this, editable)
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            recitationsFragment
        ).addToBackStack(null).commit()
    }

    override fun gotoDownloadsReciters(recitationId: Int) {
        val recitersFragment = DownloadsRecitersFragment
            .newInstance(this, recitationId, editable)
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            recitersFragment
        ).addToBackStack(null).commit()
    }

    override fun gotoDownloadsSuras(recitationId: Int, reciterId: String, reciterName: String) {
        val downloadsSurasFragment = DownloadsSurasFragment
            .newInstance(this, recitationId, reciterId, reciterName)
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            downloadsSurasFragment
        ).addToBackStack(null).commit()
    }

    override fun openRecitersDialog(recitationId: Int) {
        val recitersDialogFragment = QuranRecitersDialogFragment.newInstance(recitationId)
        recitersDialogFragment.show(supportFragmentManager, "QuranRecitersDialogFragment")
    }

    override fun openAudioDownloadAmountDialog(recitationId: Int, reciterId: String) {
        val downloadAmountDialogFragment =
            AudioDownloadAmountDialogFragment.newInstance(recitationId, reciterId)
        downloadAmountDialogFragment.show(
            supportFragmentManager,
            "AudioDownloadAmountDialogFragment"
        )
    }

    override fun onReciterSelected(recitationId: Int, reciterModel: ReciterModel) {
        Log.d(
            TAG, "onReciterSelected - recitationId=" + recitationId +
                    " , reciter=" + reciterModel
        )
        openAudioDownloadAmountDialog(recitationId, reciterModel.id)
    }

    override fun onClickDownload() {}

    companion object {
        private val TAG = DownloadsManagerActivity::class.java.simpleName

        private const val STATE_EDITABLE = "STATE_EDITABLE"
        private const val STATE_SELECTED_TAB_INDEX = "STATE_SELECTED_TAB_INDEX"
    }
}