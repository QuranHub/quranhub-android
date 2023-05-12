package app.quranhub.ui.downloads_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.quranhub.databinding.FragmentDownloadsQuranImagesBinding

class DownloadsQuranImagesFragment : Fragment() {

    private var _binding: FragmentDownloadsQuranImagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsQuranImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = DownloadsQuranImagesFragment()
    }
}