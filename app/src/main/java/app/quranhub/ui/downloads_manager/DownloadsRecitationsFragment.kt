package app.quranhub.ui.downloads_manager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment;
import app.quranhub.ui.downloads_manager.model.DisplayableDownload;
import app.quranhub.util.QuranAudioDeleteUtils;

public class DownloadsRecitationsFragment extends BaseDownloadsFragment
        implements DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks {

    private static final String TAG = DownloadsRecitationsFragment.class.getSimpleName();

    public static DownloadsRecitationsFragment newInstance(@NonNull Context context) {
        return newInstance(context, false);
    }

    public static DownloadsRecitationsFragment newInstance(@NonNull Context context, boolean isEditable) {
        DownloadsRecitationsFragment recitationsFragment = new DownloadsRecitationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DESCRIPTION, context.getString(R.string.description_manage_recitations_downloads));
        args.putBoolean(ARG_EDITABLE, isEditable);
        recitationsFragment.setArguments(args);
        return recitationsFragment;
    }

    @NonNull
    @Override
    protected List<DisplayableDownload> provideDisplayableDownloads() {
        List<DisplayableDownload> downloads = new ArrayList<>();
        for (int recitationStringResId : Constants.Recitation.NAMES_STR_IDS) {
            downloads.add(new DisplayableDownload(getString(recitationStringResId)));
        }

        // check if recitations are downloadable and/or deletable & the number of downloaded reciters each.
        for (int i = 0; i < downloads.size(); i++) {
            DisplayableDownload displayableDownload = downloads.get(i);

            int numOfDownloadedReciters = UserDatabase.getInstance(requireContext())
                    .getReciterRecitationDao()
                    .getNumOfRecitersWithDownloads(i);

            displayableDownload.setDownloadedAmount(
                    getString(R.string.downloaded_reciters_num, numOfDownloadedReciters));
            displayableDownload.setDeletable(numOfDownloadedReciters > 0);
            displayableDownload.setDownloadable(true); // TODO check if it's not downloadable
        }

        return downloads;
    }

    @Override
    public void onClickItem(DisplayableDownload displayableDownload, int position) {
        int recitationId = position;
        navigationCallbacks.gotoDownloadsReciters(recitationId);
    }

    @Override
    public void onDeleteItem(DisplayableDownload displayableDownload, int position) {
        DeleteConfirmationDialogFragment confirmationDialog = DeleteConfirmationDialogFragment.newInstance(
                getString(R.string.confirm_delete_title),
                getString(R.string.confirm_delete_description_recitations), position);
        confirmationDialog.show(getChildFragmentManager(), "DeleteConfirmationDialogFragment");
    }

    @Override
    public void onConfirmDelete(int deletePosition) {
        int recitationId = deletePosition;
        QuranAudioDeleteUtils.deleteRecitationAudio(requireContext(), recitationId
                , this::refresh);
    }

    @Override
    public void onDownloadItem(DisplayableDownload displayableDownload, int position) {
        int recitationId = position;
        navigationCallbacks.openRecitersDialog(recitationId);
    }
}
