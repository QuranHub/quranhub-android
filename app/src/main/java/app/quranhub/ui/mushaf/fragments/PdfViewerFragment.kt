package app.quranhub.ui.mushaf.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.quranhub.data.Constants
import app.quranhub.databinding.FragmentPdfViewerBinding
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import java.io.File

class PdfViewerFragment : Fragment() {

    private var binding: FragmentPdfViewerBinding? = null

    private var fileName: String? = null
    private val uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPdfView()
    }

    private fun setPdfView() {
        val completeListener =
            OnLoadCompleteListener { _: Int -> binding!!.progressBar.visibility = View.GONE }
        val pageChangeListener =
            OnPageChangeListener { page, _ -> Log.d("TAG", "onPageChanged: $page") }
        fileName = requireArguments().getString("file_name")
        val file = File(
            Environment.getExternalStorageDirectory().toString() + File.separator
                    + Constants.Directory.LIBRARY_PUBLIC, fileName!!
        )
        binding!!.pdfView.fromFile(file)
            .enableDoubletap(true)
            .enableSwipe(true)
            .onLoad(completeListener)
            .onPageChange(pageChangeListener)
            .load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}