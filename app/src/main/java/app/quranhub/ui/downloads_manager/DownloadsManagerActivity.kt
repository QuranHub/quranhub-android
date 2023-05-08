package app.quranhub.ui.downloads_manager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import app.quranhub.R;
import app.quranhub.data.model.ReciterModel;
import app.quranhub.ui.base.BaseActivity;
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment;
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment;

public class DownloadsManagerActivity extends BaseActivity implements
        BaseDownloadsFragment.DownloadsManagerNavigationCallbacks,
        QuranRecitersDialogFragment.ReciterSelectionListener, AudioDownloadAmountDialogFragment.AudioDownloadListener {

    private static final String TAG = DownloadsManagerActivity.class.getSimpleName();

    private static final String STATE_EDITABLE = "STATE_EDITABLE";

    private boolean editable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_manager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            // restore saved instance state, if any,
            editable = savedInstanceState.getBoolean(STATE_EDITABLE);

        } else {
            // activity is being created for the first time
            DownloadsRecitationsFragment recitationsFragment = DownloadsRecitationsFragment
                    .newInstance(this, editable);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    recitationsFragment).commit();
        }

        /* sync the activity's action bar with the BaseDownloadsFragment 'editable' state
           when user's back navigating */
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            BaseDownloadsFragment downloadsFragment = (BaseDownloadsFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            editable = downloadsFragment.getEditable();
            invalidateOptionsMenu();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_EDITABLE, editable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_downloads_manager, menu);
        MenuItem editMenuItem = menu.findItem(R.id.action_edit);
        updateActionBar(editMenuItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            editable = !editable;
            updateActionBar(item);

            // notify child fragment
            try {
                Editable editableFragment = (Editable) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (editableFragment != null) {
                    editableFragment.setEditable(editable);
                }
            } catch (ClassCastException e) {
                Log.e(TAG, "Cannot cast the child fragment to Editable." +
                        " Did you implement the Editable interface?");
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateActionBar(MenuItem editMenuItem) {
        // update the menu item icon & the activity title
        if (editable) {
            editMenuItem.setIcon(R.drawable.check_gold_ic);
            setTitle(R.string.title_activity_downloads_manager_edit_enabled);
        } else {
            editMenuItem.setIcon(R.drawable.edit_gold_ic);
            setTitle(R.string.title_activity_downloads_manager);
        }
    }

    @Override
    public void gotoDownloadsRecitations() {
        DownloadsRecitationsFragment recitationsFragment = DownloadsRecitationsFragment
                .newInstance(this, editable);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                recitationsFragment).addToBackStack(null).commit();
    }

    @Override
    public void gotoDownloadsReciters(int recitationId) {
        DownloadsRecitersFragment recitersFragment = DownloadsRecitersFragment
                .newInstance(this, recitationId, editable);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                recitersFragment).addToBackStack(null).commit();
    }

    @Override
    public void gotoDownloadsSuras(int recitationId, @NonNull String reciterId, @NonNull String reciterName) {
        DownloadsSurasFragment downloadsSurasFragment = DownloadsSurasFragment
                .newInstance(this, recitationId, reciterId, reciterName);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                downloadsSurasFragment).addToBackStack(null).commit();
    }

    @Override
    public void openRecitersDialog(int recitationId) {
        QuranRecitersDialogFragment recitersDialogFragment =
                QuranRecitersDialogFragment.newInstance(recitationId);
        recitersDialogFragment.show(getSupportFragmentManager(), "QuranRecitersDialogFragment");
    }

    @Override
    public void openAudioDownloadAmountDialog(int recitationId, @NonNull String reciterId) {
        AudioDownloadAmountDialogFragment downloadAmountDialogFragment =
                AudioDownloadAmountDialogFragment.newInstance(recitationId, reciterId);
        downloadAmountDialogFragment.show(getSupportFragmentManager(), "AudioDownloadAmountDialogFragment");
    }

    @Override
    public void onReciterSelected(int recitationId, @NonNull ReciterModel reciter) {
        Log.d(TAG, "onReciterSelected - recitationId=" + recitationId +
                " , reciter=" + reciter);

        openAudioDownloadAmountDialog(recitationId, reciter.getId());
    }

    @Override
    public void onClickDownload() {
    }
}
