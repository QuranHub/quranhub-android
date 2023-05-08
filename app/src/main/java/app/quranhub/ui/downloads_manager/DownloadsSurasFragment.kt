package app.quranhub.ui.downloads_manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.ReciterRecitation;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.data.service.QuranAudioDownloaderService;
import app.quranhub.ui.downloads_manager.dialogs.DeleteConfirmationDialogFragment;
import app.quranhub.ui.downloads_manager.model.DisplayableDownload;
import app.quranhub.util.QuranAudioDeleteUtils;

public class DownloadsSurasFragment extends BaseDownloadsFragment
        implements DeleteConfirmationDialogFragment.DeleteConfirmationCallbacks {

    private static final String TAG = DownloadsSurasFragment.class.getSimpleName();

    private static final String ARG_RECITATION_ID = "ARG_RECITATION_ID";
    private static final String ARG_RECITER_ID = "ARG_RECITER_ID";
    private static final String ARG_RECITER_NAME = "ARG_RECITER_NAME";

    private int recitationId;
    private String reciterId;
    private String reciterName;

    public static DownloadsSurasFragment newInstance(@NonNull Context context, int recitationId
            , @NonNull String reciterId, @NonNull String reciterName) {
        return newInstance(context, recitationId, reciterId, reciterName, false);
    }

    public static DownloadsSurasFragment newInstance(@NonNull Context context, int recitationId
            , @NonNull String reciterId, @NonNull String reciterName, boolean isEditable) {
        DownloadsSurasFragment surasFragment = new DownloadsSurasFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECITATION_ID, recitationId);
        args.putString(ARG_RECITER_ID, reciterId);
        args.putString(ARG_RECITER_NAME, reciterName);
        args.putString(ARG_DESCRIPTION, context.getString(R.string.description_manage_suras_downloads));
        args.putBoolean(ARG_EDITABLE, isEditable);
        surasFragment.setArguments(args);
        return surasFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            recitationId = getArguments().getInt(ARG_RECITATION_ID);
            reciterId = getArguments().getString(ARG_RECITER_ID);
            reciterName = getArguments().getString(ARG_RECITER_NAME);
        }
    }

    @NonNull
    @Override
    protected List<DisplayableDownload> provideDisplayableDownloads() {
        List<DisplayableDownload> displayableDownloadList = new ArrayList<>();

        String[] suras = getResources().getStringArray(R.array.sura_name);

        for (int i = 0; i < suras.length; i++) {
            String suraName = suras[i];
            DisplayableDownload displayableDownload = new DisplayableDownload(suraName);

            int suraId = i + 1;

            boolean isDownloadable = UserDatabase.getInstance(requireContext())
                    .getQuranAudioDao()
                    .getForSura(recitationId, reciterId, suraId)
                    .isEmpty();
            displayableDownload.setDownloadable(isDownloadable);
            displayableDownload.setDeletable(!isDownloadable);

            displayableDownloadList.add(displayableDownload);
        }

        return displayableDownloadList;
    }

    @Override
    public void onClickItem(DisplayableDownload displayableDownload, int position) {
    }

    @Override
    public void onDeleteItem(DisplayableDownload displayableDownload, int position) {
        DeleteConfirmationDialogFragment confirmationDialog = DeleteConfirmationDialogFragment.newInstance(
                getString(R.string.confirm_delete_title),
                getString(R.string.confirm_delete_description_suras), position);
        confirmationDialog.show(getChildFragmentManager(), "DeleteConfirmationDialogFragment");
    }

    @Override
    public void onConfirmDelete(int deletePosition) {
        int suraId = deletePosition + 1;
        QuranAudioDeleteUtils.deleteSuraAudio(requireContext(), recitationId, reciterId, suraId, this::refresh);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onDownloadItem(DisplayableDownload displayableDownload, int position) {

        int suraId = position + 1;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                UserDatabase userDatabase = UserDatabase.getInstance(requireContext());
                if (userDatabase.getReciterDao().getById(reciterId) == null) {
//                    userDatabase.getReciterDao()
//                            .insert(new Reciter(reciterId, reciterName));
                }
                if (userDatabase.getReciterRecitationDao()
                        .get(recitationId, reciterId) == null) {
                    userDatabase.getReciterRecitationDao()
                            .insert(new ReciterRecitation(recitationId, reciterId));
                }

                int recitationIdPreference = AppPreferencesManager.getRecitationSetting(requireContext());
                if (recitationIdPreference == recitationId) {
                    AppPreferencesManager.persistReciterSheikhSetting(requireContext(), reciterId);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                QuranAudioDownloaderService.downloadSura(requireContext(), recitationId, reciterId, suraId);
                Toast.makeText(requireContext(), R.string.msg_quran_audio_download_started,
                        Toast.LENGTH_SHORT).show();
            }
        }.execute();

    }
}
