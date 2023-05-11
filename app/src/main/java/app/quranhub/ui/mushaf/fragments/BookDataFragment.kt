package app.quranhub.ui.mushaf.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.Book
import app.quranhub.data.remote.BookDownloadManager
import app.quranhub.data.remote.model.BookContent
import app.quranhub.databinding.FragmentBookDataBinding
import app.quranhub.ui.common.interfaces.Searchable
import app.quranhub.ui.main.MainActivity
import app.quranhub.ui.mushaf.adapter.BookAdapter
import app.quranhub.ui.mushaf.adapter.BookAdapter.TranslationActionsListener
import app.quranhub.ui.mushaf.dialogs.OpenFileDialog
import app.quranhub.ui.mushaf.dialogs.OpenFileDialog.OpenFileListener
import app.quranhub.ui.mushaf.viewmodel.BooksViewModel
import app.quranhub.util.FragmentUtils.isSafeFragment
import app.quranhub.util.NetworkUtil.isNetworkAvailable
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.File

// todo add edit button to delete downloaded translation
class BookDataFragment : Fragment(), Searchable, PermissionCallbacks, TranslationActionsListener,
    OpenFileListener {

    private var allowOpenFiles =
        true // whether to allow the user to open downloaded file on click or not
    private var inputSearch: String? = ""
    private var adapter: BookAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var downloadBook: BookContent? = null
    private var selectedBook: BookContent? = null
    private var viewModel: BooksViewModel? = null
    private var bookDownloadManager: BookDownloadManager? = null
    private var firstTime = true
    private var internetConnection = true
    private var isEditable = false
    private var bookContents: List<BookContent>? = null
    private var books: List<Book>? = null

    private var binding: FragmentBookDataBinding? = null

    // Listen on downloads status changed (Canceled - Downlaoded)
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val status = bookDownloadManager!!.queryOnFinishedDownloads(downloadId)
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                viewModel!!.updateFinishedDownload(downloadId, BookAdapter.TRANSLATION_DOWNLOADED)
            } else {
                viewModel!!.updateFinishedDownload(
                    downloadId,
                    BookAdapter.TRANSLATION_NOT_DOWNLOADED
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            allowOpenFiles = requireArguments().getBoolean(ARG_ALLOW_OPEN_FILES, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookDataBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        savedInstanceState?.let { getPrevState(it) }
        bindViewModel(savedInstanceState != null)
        bookDownloadManager = BookDownloadManager(requireActivity())
        checkInternetConnection()
    }

    private fun getPrevState(savedInstanceState: Bundle) {
        inputSearch = savedInstanceState.getString("input_search")
        isEditable = savedInstanceState.getBoolean("is_editable")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_editable", isEditable)
        outState.putString("input_search", inputSearch)
    }

    private fun checkInternetConnection() {
        if (!isNetworkAvailable(requireActivity())) {
            internetConnection = false
        }
    }

    // return empty space in local storage in MB
    @get:SuppressLint("NewApi")
    private val emptySpaceSize: Long
        get() {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            return availableBlocks * blockSize / (1024 * 1024)
        }

    private fun bindViewModel(isInstanceSaved: Boolean) {
        viewModel = ViewModelProvider(this)[BooksViewModel::class.java]
        viewModel!!.remoteTranslationsLiveData.observe(viewLifecycleOwner) { translationModels: List<BookContent>? ->
            if (translationModels == null) {
                return@observe
            }
            if (!internetConnection) {
                binding!!.progressBar.visibility = View.GONE
                binding!!.partialLoadFailedView.root.visibility = View.VISIBLE
                return@observe
            } else if (translationModels.isNotEmpty()) {
                bookContents = translationModels
                binding!!.progressBar.visibility = View.GONE
                binding!!.partialLoadFailedView.root.visibility = View.GONE
            }

            // if the data is in editable state (get only downloads translations) with remove download
            if (isEditable) {
                if (isInstanceSaved) {
                    adapter!!.setBookList(translationModels.toMutableList())
                }
                adapter!!.setDownloadTranslations()
                // if the data in normal state (get all translations)
            } else {
                adapter!!.setBookList(translationModels.toMutableList())
            }
            // get prev state if user input search after config changes
            if (inputSearch != null && !TextUtils.isEmpty(inputSearch!!.trim { it <= ' ' })) {
                adapter!!.filter(inputSearch!!)
            }
            if (books != null) {
                setBooksContentStatus(books!!)
                getUpdatedDownloadedStatus(bookContents)
                adapter!!.updateBooksDownloadStatus(books!!)
                firstTime = false
            }
        }
        viewModel!!.localTranslationsLiveData.observe(viewLifecycleOwner) { models: List<Book> ->
            books = models
            if (bookContents != null) {
                setBooksContentStatus(models)
                adapter!!.updateBooksDownloadStatus(models)
            }
            if (!isEditable && firstTime && bookContents != null) {
                getUpdatedDownloadedStatus(bookContents)
                firstTime = false
            }
        }
    }

    private fun setBooksContentStatus(models: List<Book>) {
        for ((id, downloadStatus, downloadId) in models) {
            for (content in bookContents!!) {
                if (id == content.id) {
                    content.downloadId = downloadId
                    content.downloadStatus = downloadStatus
                    break
                }
            }
        }
    }

    /**
     * @param books get status of (in progress downloading) files to update their last status if it changed to ended or cancelled
     */
    private fun getUpdatedDownloadedStatus(books: List<BookContent>?) {
        val inProgressDownloadedIds: MutableList<Long> = ArrayList()
        for ((_, _, _, _, _, _, _, downloadStatus, downloadId) in books!!) {
            if (downloadStatus == BookAdapter.TRANSLATION_DOWNLOADED_IN_PROGRESS) {
                inProgressDownloadedIds.add(downloadId)
            }
        }
        if (inProgressDownloadedIds.isNotEmpty()) {
            val statusList = bookDownloadManager!!.queryOnFinishedDownloads(inProgressDownloadedIds)

            // downloads are canceled from cancel action in notifications
            if (statusList.isEmpty()) {
                for (downloadId in inProgressDownloadedIds) {
                    viewModel!!.updateFinishedDownload(
                        downloadId,
                        BookAdapter.TRANSLATION_NOT_DOWNLOADED
                    )
                    bookDownloadManager!!.cancelDownload(downloadId)
                }
            } else { // check if downloads are changed its status to complete
                for (i in statusList.indices) {
                    if (inProgressDownloadedIds.size <= i) break
                    if (statusList[i] != DownloadManager.STATUS_RUNNING && statusList[i] != DownloadManager.STATUS_PENDING) {
                        if (statusList[i] == DownloadManager.STATUS_SUCCESSFUL) viewModel!!.updateFinishedDownload(
                            inProgressDownloadedIds[i], BookAdapter.TRANSLATION_DOWNLOADED
                        )
                    }
                }
            }
        }
    }

    private fun initRecycler() {
        layoutManager = LinearLayoutManager(activity)
        adapter = BookAdapter(this)
        binding!!.translationRv.layoutManager = layoutManager
        binding!!.translationRv.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        binding!!.translationRv.addItemDecoration(dividerItemDecoration)
    }

    override fun onDownloadTranslation(model: BookContent?) {
        downloadBook = model
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (EasyPermissions.hasPermissions(requireActivity(), *perms)) {
            downloadFile(model)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.storage_perm),
                STORAGE_REQUEST_CODE,
                *perms
            )
        }
    }

    private fun hasAvailableSpace(fileSize: Long): Boolean {
        return emptySpaceSize > fileSize
    }

    private fun downloadFile(model: BookContent?) {
        if (hasAvailableSpace(model!!.size.toLong())) {
            deleteExistFile(model.name) // delete file if exist (in case of in downloading status then cancel it => in some cases not deleted)
            val downloadId = bookDownloadManager!!.downloadFile(model)
            bookDownloadManager!!.queryOnFinishedDownloads(downloadId)
            viewModel!!.updateTranslationType(
                model.id,
                BookAdapter.TRANSLATION_DOWNLOADED_IN_PROGRESS,
                downloadId
            )
        } else {
            Toast.makeText(activity, getString(R.string.no_space), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCancelDownload(model: BookContent?) {
        bookDownloadManager!!.cancelDownload(model!!.downloadId)
        viewModel!!.updateTranslationType(model.id, BookAdapter.TRANSLATION_NOT_DOWNLOADED, -1)
        deleteExistFile(model.name)
    }

    private fun deleteExistFile(pdfName: String?) {
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + BookDownloadManager.FILE_PATH, "$pdfName.pdf"
        )
        if (file.exists()) {
            file.delete()
        }
    }

    override fun onDeleteTranslation(model: BookContent?) {
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + BookDownloadManager.FILE_PATH, model!!.name + ".pdf"
        )
        if (file.exists()) {
            file.delete()
        } else {
            Toast.makeText(activity, getString(R.string.file_not_exist), Toast.LENGTH_LONG).show()
        }
        viewModel!!.updateTranslationType(model.id, BookAdapter.TRANSLATION_NOT_DOWNLOADED, -1)
        adapter!!.removeDeletedFile(model.id)
    }

    override fun onSelectItem(model: BookContent?) {
        if (allowOpenFiles) {
            val perms = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            selectedBook = model
            if (EasyPermissions.hasPermissions(requireActivity(), *perms)) {
                openChooserDialog()
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.storage_perm),
                    STORAGE_REQUEST_CODE,
                    *perms
                )
            }
        }
    }

    private fun openChooserDialog() {
        val dialogFragment: DialogFragment = OpenFileDialog()
        dialogFragment.show(childFragmentManager, "OpenFileDialog")
    }

    private fun openPdfFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) FileProvider.getUriForFile(
            requireContext(), requireActivity().packageName + ".provider", file
        ) else Uri.fromFile(file)
        intent.setDataAndType(uri, "application/pdf")
        val pm = requireActivity().packageManager
        if (pm != null && intent.resolveActivity(pm) != null) {
            startActivity(Intent.createChooser(intent, "open with"))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        if (isGranted && selectedBook != null) {
            openChooserDialog()
        } else if (isGranted && downloadBook != null) {
            downloadFile(downloadBook)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted: ")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(activity, getString(R.string.accept_perm), Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        if (isSafeFragment(this)) {
            requireActivity().registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isSafeFragment(this)) requireActivity().unregisterReceiver(onDownloadComplete)
        binding = null
    }

    override fun onOpenFile(openType: Int) {
        val file = File(
            Environment.getExternalStorageDirectory().toString() + File.separator
                    + Constants.Directory.LIBRARY_PUBLIC, selectedBook!!.name + ".pdf"
        )
        if (!file.exists()) {
            Toast.makeText(activity, getString(R.string.file_not_exist), Toast.LENGTH_LONG).show()
            viewModel!!.updateTranslationType(
                selectedBook!!.id,
                BookAdapter.TRANSLATION_NOT_DOWNLOADED,
                -1
            )
            return
        } else if (openType == OpenFileDialog.IN_APP) {
            (activity as MainActivity?)!!.openPdfFragment(selectedBook!!.name + ".pdf")
        } else {
            openPdfFile(file)
        }
    }

    override fun search(text: String?) {
        adapter!!.filter(text!!)
        inputSearch = text
    }

    fun toggleEditAction() {
        isEditable = true
        adapter!!.setDownloadTranslations()
    }

    fun toggleNormalMode() {
        isEditable = false
        adapter!!.setAllTranslation()
    }

    companion object {
        private val TAG = BookDataFragment::class.java.simpleName
        private const val ARG_ALLOW_OPEN_FILES = "ARG_ALLOW_OPEN_FILES"
        private const val STORAGE_REQUEST_CODE = 100

        @JvmStatic
        fun getInstance(allowOpenFiles: Boolean): BookDataFragment {
            val fragment = BookDataFragment()
            val args = Bundle()
            args.putBoolean(ARG_ALLOW_OPEN_FILES, allowOpenFiles)
            fragment.arguments = args
            return fragment
        }
    }
}