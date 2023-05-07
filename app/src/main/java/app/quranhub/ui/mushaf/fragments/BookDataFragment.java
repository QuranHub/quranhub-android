package app.quranhub.ui.mushaf.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.entity.Book;
import app.quranhub.data.remote.BookDownloadManager;
import app.quranhub.data.remote.model.BookContent;
import app.quranhub.databinding.FragmentBookDataBinding;
import app.quranhub.ui.common.interfaces.Searchable;
import app.quranhub.ui.main.MainActivity;
import app.quranhub.ui.mushaf.adapter.BookAdapter;
import app.quranhub.ui.mushaf.dialogs.OpenFileDialog;
import app.quranhub.ui.mushaf.viewmodel.BooksViewModel;
import app.quranhub.util.FragmentUtils;
import app.quranhub.util.NetworkUtil;
import pub.devrel.easypermissions.EasyPermissions;

// todo add edit button to delete downloaded translation
public class BookDataFragment extends Fragment implements Searchable, EasyPermissions.PermissionCallbacks
        , BookAdapter.TranslationActionsListener, OpenFileDialog.OpenFileListener {

    private static final String TAG = BookDataFragment.class.getSimpleName();

    private static final String ARG_ALLOW_OPEN_FILES = "ARG_ALLOW_OPEN_FILES";

    private boolean allowOpenFiles = true; // whether to allow the user to open downloaded file on click or not

    private static final int STORAGE_REQUEST_CODE = 100;
    private String inputSearch = "";
    private BookAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private BookContent downloadBook, selectedBook;
    private BooksViewModel viewModel;
    private BookDownloadManager bookDownloadManager;
    private boolean firstTime = true, internetConnection = true, isEditable = false;
    private List<BookContent> bookContents;
    private List<Book> books;

    private FragmentBookDataBinding binding;

    public static BookDataFragment getInstance(boolean allowOpenFiles) {
        BookDataFragment fragment = new BookDataFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ALLOW_OPEN_FILES, allowOpenFiles);
        fragment.setArguments(args);
        return fragment;
    }

    // Listen on downloads status changed (Canceled - Downlaoded)
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            int status = bookDownloadManager.queryOnFinishedDownloads(downloadId);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                viewModel.updateFinishedDownload(downloadId, BookAdapter.TRANSLATION_DOWNLOADED);
            } else {
                viewModel.updateFinishedDownload(downloadId, BookAdapter.TRANSLATION_NOT_DOWNLOADED);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            allowOpenFiles = getArguments().getBoolean(ARG_ALLOW_OPEN_FILES, true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookDataBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecycler();
        if (savedInstanceState != null) {
            getPrevState(savedInstanceState);
        }
        bindViewModel(savedInstanceState != null);
        bookDownloadManager = new BookDownloadManager(requireActivity());
        checkInternetConnection();
    }

    private void getPrevState(Bundle savedInstanceState) {
        inputSearch = savedInstanceState.getString("input_search");
        isEditable = savedInstanceState.getBoolean("is_editable");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_editable", isEditable);
        outState.putString("input_search", inputSearch);
    }

    private void checkInternetConnection() {
        if (!NetworkUtil.isNetworkAvailable(getActivity())) {
            internetConnection = false;
        }
    }

    // return empty space in local storage in MB
    @SuppressLint("NewApi")
    private long getEmptySpaceSize() {

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long availableSize = availableBlocks * blockSize / (1024 * 1024);
        return availableSize;

    }

    private void bindViewModel(boolean isInstanceSaved) {

        viewModel = new ViewModelProvider(this).get(BooksViewModel.class);

        viewModel.getRemoteTranslationsLiveData().observe(getViewLifecycleOwner(), translationModels -> {
            if (translationModels == null) {
                return;
            }

            if (!internetConnection) {
                binding.progressBar.setVisibility(View.GONE);
                binding.partialLoadFailedView.getRoot().setVisibility(View.VISIBLE);
                return;
            } else if (translationModels.size() > 0) {
                this.bookContents = translationModels;
                binding.progressBar.setVisibility(View.GONE);
                binding.partialLoadFailedView.getRoot().setVisibility(View.GONE);
            }

            // if the data is in editable state (get only downloads translations) with remove download
            if (isEditable) {
                if (isInstanceSaved) {
                    adapter.setBookList(translationModels);
                }
                adapter.setDownloadTranslations();
                // if the data in normal state (get all translations)
            } else {
                adapter.setBookList(translationModels);
            }
            // get prev state if user input search after config changes
            if (inputSearch != null && !TextUtils.isEmpty(inputSearch.trim())) {
                adapter.filter(inputSearch);
            }

            if (books != null) {
                setBooksContentStatus(books);
                getUpdatedDownloadedStatus(bookContents);
                adapter.updateBooksDownloadStatus(books);
                firstTime = false;
            }
        });

        viewModel.getLocalTranslationsLiveData().observe(getViewLifecycleOwner(), models -> {
            this.books = models;

            if (bookContents != null) {
                setBooksContentStatus(models);
                adapter.updateBooksDownloadStatus(models);
            }
            if (!isEditable && firstTime && bookContents != null) {
                getUpdatedDownloadedStatus(bookContents);
                firstTime = false;
            }
        });
    }

    private void setBooksContentStatus(List<Book> models) {
        for (Book book : models) {
            for (BookContent content : bookContents) {
                if (book.getId() == content.getId()) {
                    content.setDownloadId(book.getDownloadId());
                    content.setDownloadStatus(book.getDownloadStatus());
                    break;
                }
            }
        }
    }

    /**
     * @param books get status of (in progress downloading) files to update their last status if it changed to ended or cancelled
     */
    private void getUpdatedDownloadedStatus(List<BookContent> books) {

        List<Long> inProgressDownloadedIds = new ArrayList<>();
        for (BookContent model : books) {
            if (model.getDownloadStatus() == BookAdapter.TRANSLATION_DOWNLOADED_IN_PROGRESS) {
                inProgressDownloadedIds.add(model.getDownloadId());
            }
        }

        if (!inProgressDownloadedIds.isEmpty()) {
            List<Integer> statusList = bookDownloadManager.queryOnFinishedDownloads(inProgressDownloadedIds);

            // downloads are canceled from cancel action in notifications
            if (statusList.isEmpty()) {
                for (Long downloadId : inProgressDownloadedIds) {
                    viewModel.updateFinishedDownload(downloadId, BookAdapter.TRANSLATION_NOT_DOWNLOADED);
                    bookDownloadManager.cancelDownload(downloadId);
                }
            } else { // check if downloads are changed its status to complete
                for (int i = 0; i < statusList.size(); i++) {
                    if (inProgressDownloadedIds.size() <= i)
                        break;
                    if (statusList.get(i) != DownloadManager.STATUS_RUNNING && statusList.get(i) != DownloadManager.STATUS_PENDING) {
                        if (statusList.get(i) == DownloadManager.STATUS_SUCCESSFUL)
                            viewModel.updateFinishedDownload(inProgressDownloadedIds.get(i), BookAdapter.TRANSLATION_DOWNLOADED);
                    }
                }
            }
        }
    }

    private void initRecycler() {
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new BookAdapter(this);
        binding.translationRv.setLayoutManager(layoutManager);
        binding.translationRv.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        binding.translationRv.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onDownloadTranslation(BookContent model) {
        downloadBook = model;
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            downloadFile(model);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.storage_perm), STORAGE_REQUEST_CODE, perms);
        }
    }

    @SuppressLint("NewApi")
    private boolean hasAvailbableSpace(long fileSize) {
        return (getEmptySpaceSize() > fileSize) || (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2);
    }

    private void downloadFile(BookContent model) {
        if (hasAvailbableSpace(model.getSize())) {

            deleteExistFile(model.getName());        // delete file if exist (in case of in downloading status then cancel it => in some cases not deleted)

            long downloadId = bookDownloadManager.downloadFile(model);
            bookDownloadManager.queryOnFinishedDownloads(downloadId);
            viewModel.updateTranslationType(model.getId(), BookAdapter.TRANSLATION_DOWNLOADED_IN_PROGRESS, downloadId);
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_space), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onCancelDownload(BookContent model) {
        bookDownloadManager.cancelDownload(model.getDownloadId());
        viewModel.updateTranslationType(model.getId(), BookAdapter.TRANSLATION_NOT_DOWNLOADED, -1);
        deleteExistFile(model.getName());
    }

    private void deleteExistFile(String pdfName) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + BookDownloadManager.FILE_PATH, pdfName + ".pdf");
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void onDeleteTranslation(BookContent model) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + BookDownloadManager.FILE_PATH, model.getName() + ".pdf");
        if (file.exists()) {
            file.delete();
        } else {
            Toast.makeText(getActivity(), getString(R.string.file_not_exist), Toast.LENGTH_LONG).show();
        }
        viewModel.updateTranslationType(model.getId(), BookAdapter.TRANSLATION_NOT_DOWNLOADED, -1);
        adapter.removeDeletedFile(model.getId());
    }

    @Override
    public void onSelectItem(BookContent model) {
        if (allowOpenFiles) {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            selectedBook = model;
            if (EasyPermissions.hasPermissions(getActivity(), perms)) {
                openChooserDialog();
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.storage_perm), STORAGE_REQUEST_CODE, perms);
            }
        }
    }

    private void openChooserDialog() {
        DialogFragment dialogFragment = new OpenFileDialog();
        dialogFragment.show(getChildFragmentManager(), "OpenFileDialog");
    }

    private void openPdfFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri uri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? FileProvider.getUriForFile(
                requireContext(), requireActivity().getPackageName() + ".provider", file)
                : Uri.fromFile(file);

        intent.setDataAndType(uri, "application/pdf");

        PackageManager pm = requireActivity().getPackageManager();
        if (pm != null && intent.resolveActivity(pm) != null) {
            startActivity(Intent.createChooser(intent, "open with"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }
        if (isGranted && selectedBook != null) {
            openChooserDialog();
        } else if (isGranted && downloadBook != null) {
            downloadFile(downloadBook);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: ");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(getActivity(), getString(R.string.accept_perm), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FragmentUtils.isSafeFragment(this)) {
            getActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (FragmentUtils.isSafeFragment(this))
            getActivity().unregisterReceiver(onDownloadComplete);
        binding = null;
    }

    @Override
    public void onOpefFile(int openType) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator
                + Constants.Directory.LIBRARY_PUBLIC, selectedBook.getName() + ".pdf");

        if (!file.exists()) {
            Toast.makeText(getActivity(), getString(R.string.file_not_exist), Toast.LENGTH_LONG).show();
            viewModel.updateTranslationType(selectedBook.getId(), BookAdapter.TRANSLATION_NOT_DOWNLOADED, -1);
            return;
        } else if (openType == OpenFileDialog.IN_APP) {
            ((MainActivity) getActivity()).openPdfFragment(selectedBook.getName() + ".pdf");
        } else {
            openPdfFile(file);
        }
    }

    @Override
    public void search(String text) {
        adapter.filter(text);
        this.inputSearch = text;
    }

    public void toggleEditAction() {
        isEditable = true;
        adapter.setDownloadTranslations();
    }

    public void toggleNormalMode() {
        isEditable = false;
        adapter.setAllTranslation();
    }
}
