package app.quranhub.ui.mushaf.fragments;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.data.remote.ApiClient;
import app.quranhub.data.remote.TranslationDownloader;
import app.quranhub.data.remote.api.TranslationsApi;
import app.quranhub.data.remote.model.TranslationsResponse;
import app.quranhub.databinding.FragmentTranslationsDataBinding;
import app.quranhub.ui.common.interfaces.Searchable;
import app.quranhub.ui.mushaf.adapter.TranslationsAdapter;
import app.quranhub.ui.mushaf.model.DisplayableTranslation;
import app.quranhub.util.FragmentUtils;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslationsDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// TODO apply MVVM
public class TranslationsDataFragment extends Fragment implements Searchable, TranslationsAdapter.ItemClickListener
        , EasyPermissions.PermissionCallbacks, TranslationDownloader.TranslationDownloadCallback {

    private static final String TAG = TranslationsDataFragment.class.getSimpleName();

    private static final String ARG_LANGUAGE_CODE = "ARG_LANGUAGE_CODE";
    private static final int WRITE_REQUEST_CODE = 0;

    private static final String STATE_SEARCH_TEXT = "STATE_SEARCH_TEXT";
    private String searchText = "";

    @Nullable
    private String languageCode;

    private TranslationSelectionListener listener;

    private FragmentTranslationsDataBinding binding;

    private List<DisplayableTranslation> displayableTranslations;
    private TranslationsAdapter adapter;

    List<TranslationBook> remoteTranslationBooks;
    LiveData<List<TranslationBook>> translationBooksLiveData;

    List<TranslationDownloader> translationDownloaders;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param languageCode
     * @return A new instance of fragment TranslationsDataFragment.
     */
    public static TranslationsDataFragment newInstance(String languageCode) {
        TranslationsDataFragment fragment = new TranslationsDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE_CODE, languageCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof TranslationSelectionListener) {
            listener = (TranslationSelectionListener) getParentFragment();
        } else {
            throw new RuntimeException(getParentFragment().getClass().getSimpleName()
                    + " must implement TranslationsDataFragment#TranslationSelectionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            languageCode = getArguments().getString(ARG_LANGUAGE_CODE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTranslationsDataBinding.inflate(inflater, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        // setup translationsRecyclerView
        binding.rvTranslations.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvTranslations.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        binding.rvTranslations.addItemDecoration(dividerItemDecoration);
        displayableTranslations = new ArrayList<>();
        adapter = new TranslationsAdapter(displayableTranslations, AppPreferencesManager.getQuranTranslationBook(requireContext()), this);
        binding.rvTranslations.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        translationBooksLiveData = UserDatabase.getInstance(requireContext())
                .getTranslationBookDao()
                .getByLanguage(languageCode);
        setupTranslationBooksLiveDataObserver();

        fetchTranslationBooks();
        translationDownloaders = new ArrayList<>();

        if (savedInstanceState != null) {
            search(savedInstanceState.getString(STATE_SEARCH_TEXT));
        }
    }

    private void fetchTranslationBooks() {
        TranslationsApi translationsApi = ApiClient.getClient().create(TranslationsApi.class);
        Call<TranslationsResponse> translationsCall = translationsApi.getAllTranslations();
        translationsCall.enqueue(new Callback<TranslationsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TranslationsResponse> call, @NonNull Response<TranslationsResponse> response) {

                if (FragmentUtils.isSafeFragment(TranslationsDataFragment.this)) {

                    TranslationsResponse translationsResponse = response.body();

                    if (translationsResponse != null) {

                        remoteTranslationBooks = translationsResponse.getTranslationBooksForLanguage(languageCode);

                        if (remoteTranslationBooks != null) {

                            for (TranslationBook book : remoteTranslationBooks) {
                                DisplayableTranslation d = new DisplayableTranslation(book);
                                if (!displayableTranslations.contains(d)) {
                                    // only add if it's not there
                                    displayableTranslations.add(d);
                                }
                            }

                            binding.progressTranslation.setVisibility(View.GONE);
                            adapter.setTranslations(displayableTranslations);
                        }
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslationsResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure - cause:  " + t.getMessage());

                if (FragmentUtils.isSafeFragment(TranslationsDataFragment.this)) {
                    //progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.error_translations_web_service)
                            , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupTranslationBooksLiveDataObserver() {
        translationBooksLiveData.observe(getViewLifecycleOwner(), localTranslationBooks -> {

            Log.d(TAG, "translationBooksLiveData: localTranslationBooks = " + localTranslationBooks);

            if (displayableTranslations.size() > 0) {
                // there's a change in localTranslationBooks
                // merge objects in remoteTranslationBooks & localTranslationBooks
                displayableTranslations.clear();
                for (TranslationBook book : localTranslationBooks) {
                    displayableTranslations.add(new DisplayableTranslation(book));
                }
                if (remoteTranslationBooks != null) {
                    for (TranslationBook book : remoteTranslationBooks) {
                        DisplayableTranslation d = new DisplayableTranslation(book);
                        if (!displayableTranslations.contains(d)) {
                            // only add if it's not there
                            displayableTranslations.add(d);
                        }
                    }
                }

            } else {
                // displayableTranslations is empty
                // copy objects from localTranslationBooks
                for (TranslationBook book : localTranslationBooks) {
                    displayableTranslations.add(new DisplayableTranslation(book));
                }
            }

            Log.d(TAG, "translationBooksLiveData: displayableTranslations = " + displayableTranslations);

            adapter.setTranslations(displayableTranslations);

        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_SEARCH_TEXT, searchText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //Download the file once permission is granted
        translationDownloaders.get(translationDownloaders.size() - 1).download();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permission has been denied");
    }

    @Override
    public void onTranslationClick(TranslationBook translationBook, int clickedItemIndex) {
        Log.d(TAG, "Clicked translation book: " + translationBook);

        AppPreferencesManager.persistQuranTranslationBook(getContext(), translationBook.getId());
        AppPreferencesManager.persistBookDbName(getActivity(), translationBook.getDatabaseName());
        AppPreferencesManager.persistBookName(getActivity(), translationBook.getName());
        listener.onTranslationSelected(translationBook);
    }

    @Override
    public void onDownloadTranslationClick(TranslationBook translationBook, int clickedItemIndex) {
        Log.d(TAG, "onDownloadTranslationClick: translationBook = " + translationBook);

        TranslationDownloader downloader = new TranslationDownloader(translationBook, getContext(), this);
        translationDownloaders.add(downloader);

        // check if app has permission to write to the external storage.
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            downloader.download();
        } else {
            // if permission is not present request for the same.
            EasyPermissions.requestPermissions(this, getString(R.string.translation_dowload_permission_rationale)
                    , WRITE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onCancelDownloadTranslationClick(TranslationBook translationBook, int clickedItemIndex) {
        Log.d(TAG, "onCancelDownloadTranslationClick: translationBook = " + translationBook);

        for (TranslationDownloader downloader : translationDownloaders) {
            if (downloader.getTranslationBook().getId().equals(translationBook.getId())) {
                Log.d(TAG, "Download canceled for : " + translationBook.getId());
                downloader.cancel();
                translationDownloaders.remove(downloader);
                break;
            }
        }
    }

    @Override
    public void onDownloadStarted() {
    }

    @Override
    public void onDownloadFinished() {
    }

    @Override
    public void onDownloadCancelled() {
    }

    @Override
    public void onDownloadFailed() {
        if (FragmentUtils.isSafeFragment(this)) {
            Toast.makeText(getContext(), getString(R.string.error_download_translation), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void search(String text) {
        adapter.getFilter().filter(text);
    }


    /**
     * The target fragment must implement this interface.
     */
    public interface TranslationSelectionListener {
        void onTranslationSelected(TranslationBook translationBook);
    }
}
