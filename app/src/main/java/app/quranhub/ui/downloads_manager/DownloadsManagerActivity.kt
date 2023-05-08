package app.quranhub.ui.downloads_manager

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import app.quranhub.R
import app.quranhub.data.model.ReciterModel
import app.quranhub.ui.base.BaseActivity
import app.quranhub.ui.downloads_manager.BaseDownloadsFragment.DownloadsManagerNavigationCallbacks
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment.AudioDownloadListener
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener

class DownloadsManagerActivity : BaseActivity(), DownloadsManagerNavigationCallbacks,
    ReciterSelectionListener, AudioDownloadListener {

    private var editable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads_manager)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null) {
            // restore saved instance state, if any,
            editable = savedInstanceState.getBoolean(STATE_EDITABLE)
        } else {
            // activity is being created for the first time
            val recitationsFragment = DownloadsRecitationsFragment
                .newInstance(this, editable)
            supportFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                recitationsFragment
            ).commit()
        }

        /* sync the activity's action bar with the BaseDownloadsFragment 'editable' state
           when user's back navigating */supportFragmentManager.addOnBackStackChangedListener {
            val downloadsFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container) as BaseDownloadsFragment?
            editable = downloadsFragment!!.getEditable()
            invalidateOptionsMenu()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_EDITABLE, editable)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_downloads_manager, menu)
        val editMenuItem = menu.findItem(R.id.action_edit)
        updateActionBar(editMenuItem)
        return true
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
    }
}