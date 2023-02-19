package app.quranhub.ui.downloads_manager.dialogs;


import static app.quranhub.util.DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE;
import static app.quranhub.util.DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.ReciterRecitation;
import app.quranhub.data.service.QuranAudioDownloaderService;
import app.quranhub.util.DialogUtils;
import app.quranhub.util.NetworkUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A {@code DialogFragment} that allows the user to choose the Quran audio amount he wants to download.
 * Use the {@link AudioDownloadAmountDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioDownloadAmountDialogFragment extends DialogFragment {

    private static final String TAG = AudioDownloadAmountDialogFragment.class.getSimpleName();

    private static final String ARG_RECITATION_ID = "ARG_RECITATION_ID";
    private static final String ARG_RECITER_ID = "ARG_RECITER_ID";
    private static final String ARG_SURA_ID = "ARG_SURA_ID";  // [optional]

    private static final int OPTION_DOWNLOAD_SURA = 0;
    private static final int OPTION_DOWNLOAD_ALL = 1;

    private int recitationId;
    private String reciterId;
    private int suraId;  // [optional, defaults to 1]
    private int selectedOption;

    @BindView(R.id.spinner_suras)
    Spinner surasSpinner;
    @BindView(R.id.iv_check_option_sura_download)
    ImageView suraDownloadOptionCheckImageView;
    @BindView(R.id.iv_check_option_download_all)
    ImageView downloadAlOptionCheckImageView;

    private Unbinder butterknifeUnbinder;

    private AudioDownloadListener listener;

    public AudioDownloadAmountDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param recitationId Recitation ID as in {@link Constants.Recitation}
     * @param reciterId    A reciter ID.
     * @return A new instance of fragment AudioDownloadAmountDialogFragment.
     */
    public static AudioDownloadAmountDialogFragment newInstance(int recitationId, @NonNull String reciterId) {
        return newInstance(recitationId, reciterId, 1);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param recitationId Recitation ID as in {@link Constants.Recitation}
     * @param reciterId    A reciter ID.
     * @param suraId       A sura ID to be selected when opening the dialog.
     * @return A new instance of fragment AudioDownloadAmountDialogFragment.
     */
    public static AudioDownloadAmountDialogFragment newInstance(int recitationId
            , @NonNull String reciterId, int suraId) {
        AudioDownloadAmountDialogFragment dialogFragment = new AudioDownloadAmountDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECITATION_ID, recitationId);
        args.putString(ARG_RECITER_ID, reciterId);
        args.putInt(ARG_SURA_ID, suraId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AudioDownloadListener) {
            listener = (AudioDownloadListener) context;
        } else if (getParentFragment() instanceof AudioDownloadListener) {
            listener = (AudioDownloadListener) getParentFragment();
        } else {
            throw new RuntimeException("The containing fragment or activity must implement" +
                    " AudioDownloadAmountDialogFragment#AudioDownloadListener interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recitationId = getArguments().getInt(ARG_RECITATION_ID);
            reciterId = getArguments().getString(ARG_RECITER_ID);
            suraId = getArguments().getInt(ARG_SURA_ID, 1);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View dialogView = inflater.inflate(R.layout.dialog_audio_download_amount, container, false);
        butterknifeUnbinder = ButterKnife.bind(this, dialogView);
        initDialogView();
        return dialogView;
    }

    private void initDialogView() {
        setSelectedOption(OPTION_DOWNLOAD_SURA);

        // init surasSpinner
        String[] suras = getResources().getStringArray(R.array.sura_name);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, suras);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        surasSpinner.setAdapter(dataAdapter);
        surasSpinner.setSelection(suraId - 1);
        surasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent != null && parent.getChildAt(0) != null) {
                    ((TextView) parent.getChildAt(0)).setTextColor(
                            getResources().getColor(R.color.white_color));
                }
                setSelectedOption(OPTION_DOWNLOAD_SURA);
                suraId = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setSelectedOption(int option) {
        selectedOption = option;
        if (selectedOption == OPTION_DOWNLOAD_SURA) {
            suraDownloadOptionCheckImageView.setVisibility(View.VISIBLE);
            downloadAlOptionCheckImageView.setVisibility(View.INVISIBLE);
        } else if (selectedOption == OPTION_DOWNLOAD_ALL) {
            suraDownloadOptionCheckImageView.setVisibility(View.INVISIBLE);
            downloadAlOptionCheckImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        DialogUtils.adjustDialogSize(this, DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT, 0.4f,
                DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE, 0.7f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        butterknifeUnbinder.unbind();
    }

    @OnClick(R.id.cl_option_sura_download)
    void onSuraDownloadOptionClick() {
        setSelectedOption(OPTION_DOWNLOAD_SURA);
    }

    @OnClick(R.id.cl_option_download_all)
    void onDownloadAllOptionClick() {
        setSelectedOption(OPTION_DOWNLOAD_ALL);
    }

    @OnClick(R.id.btn_cancel)
    void onCancelButtonClick() {
        dismiss();
    }

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_download)
    void onDownloadButtonClick() {
        if (!NetworkUtil.isNetworkAvailable(requireContext())) {
            Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
            return;
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                // Store SheikhRecitation for the download recitation & reciter in DB
                UserDatabase userDatabase = UserDatabase.getInstance(requireContext());
                if (userDatabase.getReciterRecitationDao()
                        .get(recitationId, reciterId) == null) {
                    userDatabase.getReciterRecitationDao()
                            .insert(new ReciterRecitation(recitationId, reciterId));
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (selectedOption == OPTION_DOWNLOAD_SURA) {
                    QuranAudioDownloaderService.downloadSura(requireContext(), recitationId, reciterId, suraId);
                } else if (selectedOption == OPTION_DOWNLOAD_ALL) {
                    QuranAudioDownloaderService.downloadQuran(requireContext(), recitationId, reciterId);
                }

                Toast.makeText(requireContext(), R.string.msg_quran_audio_download_started,
                        Toast.LENGTH_SHORT).show();
                listener.onClickDownload();
                dismiss();
            }
        }.execute();
    }

    public interface AudioDownloadListener {
        void onClickDownload();
    }
}
