package app.quranhub.ui.downloads_manager

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.databinding.FragmentDownloadsQuranImagesBinding
import app.quranhub.util.NetworkUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.util.Locale

class DownloadsQuranImagesFragment : Fragment() {

    private var _binding: FragmentDownloadsQuranImagesBinding? = null
    private val binding get() = _binding!!

    private var recitationId = Constants.Recitation.HAFS_ID

    private var totalPagesDownloaded = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsQuranImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        attachUiListeners()
    }

    private fun initUi() {
        val recitationIdSetting = AppPreferencesManager.getRecitationSetting(requireContext())
        selectRecitationOption(recitationIdSetting)
    }

    private fun selectRecitationOption(recitationId: Int) {
        this.recitationId = recitationId
        when (recitationId) {
            Constants.Recitation.HAFS_ID -> selectHafsOption()
            Constants.Recitation.WARSH_ID -> selectWarshOption()
            else -> error("Cannot identify recitation")
        }
    }

    private fun selectHafsOption() {
        binding.tvHafs.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.check_gold_ic,
            0
        )
        binding.tvWarsh.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            0,
            0
        )
    }

    private fun selectWarshOption() {
        binding.tvWarsh.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.check_gold_ic,
            0
        )
        binding.tvHafs.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            0,
            0
        )
    }

    private fun attachUiListeners() {
        binding.tvHafs.setOnClickListener {
            selectRecitationOption(Constants.Recitation.HAFS_ID)
        }
        binding.tvWarsh.setOnClickListener {
            selectRecitationOption(Constants.Recitation.WARSH_ID)
        }
        binding.btnDownload.setOnClickListener {
            startDownload()
        }
    }

    private fun startDownload() {
        if (!NetworkUtil.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), R.string.no_internet, Toast.LENGTH_SHORT).show()
            return
        }

        totalPagesDownloaded = 0

        binding.root.keepScreenOn = true
        binding.btnDownload.startAnimation()
        binding.groupDownloadInfo.isVisible = true

        val quranImageBaseUrl: String = when (recitationId) {
            Constants.Recitation.HAFS_ID -> Constants.Quran.HAFS_IMG_BASE_URL
            Constants.Recitation.WARSH_ID -> Constants.Quran.WARSH_IMG_BASE_URL
            else -> throw RuntimeException("Cannot identify recitation")
        }

        for (i in 1..Constants.Quran.NUM_OF_PAGES) {
            val imageName = when (recitationId) {
                Constants.Recitation.HAFS_ID -> String.format(Locale.US, "%d.jpg", i)
                Constants.Recitation.WARSH_ID -> String.format(Locale.US, "%d.png", i)
                else -> error("Cannot identify recitation")
            }
            val imageUrl = quranImageBaseUrl + imageName

            Glide.with(requireContext())
                .load(imageUrl)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        onImageLoaded(i, false)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        onImageLoaded(i)
                        return false
                    }
                })
                .preload()
        }
    }

    private fun onImageLoaded(pageNum: Int, isSuccessful: Boolean = true) {
        Log.d("TAG", "onImageLoaded: $pageNum , success: $isSuccessful")

        if (_binding == null) return

        totalPagesDownloaded += 1

        binding.tvDownloadProgress.text = String.format(
            Locale.US,
            "%d/%d",
            totalPagesDownloaded,
            Constants.Quran.NUM_OF_PAGES
        )

        if (totalPagesDownloaded == Constants.Quran.NUM_OF_PAGES) {
            // Download complete
            binding.btnDownload.revertAnimation()
            binding.groupDownloadInfo.isVisible = false
            binding.tvDownloadProgress.text = ""
            binding.root.keepScreenOn = false
            Toast.makeText(requireContext(), R.string.download_complete, Toast.LENGTH_SHORT).show()
            totalPagesDownloaded = 0
        }
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