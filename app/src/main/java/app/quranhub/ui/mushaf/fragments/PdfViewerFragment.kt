package app.quranhub.ui.mushaf.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;

import app.quranhub.data.Constants;
import app.quranhub.databinding.FragmentPdfViewerBinding;


public class PdfViewerFragment extends Fragment {

    private FragmentPdfViewerBinding binding;

    private String fileName;
    private Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPdfViewerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPdfView();
    }

    private void setPdfView() {
        OnLoadCompleteListener completeListener = nbPages -> binding.progressBar.setVisibility(View.GONE);
        OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
            @Override
            public void onPageChanged(int page, int pageCount) {
                Log.d("TAG", "onPageChanged: " + page);
            }
        };
        fileName = getArguments().getString("file_name");
        File file = new File(Environment.getExternalStorageDirectory() + File.separator
                + Constants.Directory.LIBRARY_PUBLIC, fileName);

        binding.pdfView.fromFile(file)
                .enableDoubletap(true)
                .enableSwipe(true)
                .onLoad(completeListener)
                .onPageChange(pageChangeListener)
                .load();

    }

}
