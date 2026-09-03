package app.quranhub.ui.mushaf.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.quranhub.R
import app.quranhub.databinding.FragmentMushafBottomBarBinding
import app.quranhub.ui.mushaf.viewmodel.MushafViewModel
import app.quranhub.util.InsetsUtils
import kotlinx.coroutines.launch

class MushafBottomBarFragment : Fragment() {

    private var binding: FragmentMushafBottomBarBinding? = null

    // Shared host ViewModel: the footer wires directly to the mushaf ViewModel
    private val viewModel: MushafViewModel by viewModels({ requireParentFragment() })

    private var quranPageZoomScaleFactor = 1f
    private var footerCallbacks: QuranFooterCallbacks? = null
    private var pageNumText: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        footerCallbacks = if (parentFragment is QuranFooterCallbacks) {
            parentFragment as QuranFooterCallbacks?
        } else {
            throw ClassCastException(
                "Cannot cast the parent fragment to QuranFooterCallbacks instance."
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMushafBottomBarBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        InsetsUtils.padBottomForNavigationBar(binding!!.llRoot)
        initViews()
        observeViewModel()
        binding!!.quranPageTv.text = pageNumText
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    setupNightModeButton(state.nightMode)
                }
            }
        }
    }

    private fun initViews() {
        setupButtonsTooltips()
        quranPageZoomScaleFactor = viewModel.quranPageZoomScaleFactor
        setupZoomButtons()
        setupNightModeButton(viewModel.uiState.value.nightMode)
        attachListeners()
    }

    private fun setupZoomButtons() {
        binding!!.ibZoomOut.isEnabled = quranPageZoomScaleFactor > 1f
        binding!!.ibZoomIn.isEnabled = quranPageZoomScaleFactor < 1.5f
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachListeners() {
        binding!!.llRoot.setOnTouchListener { _: View?, _: MotionEvent? ->
            true // To prevent event bubbling to the views below this one
        }
        binding!!.quranSearchIb.setOnClickListener { onQuranSearchClick() }
        binding!!.quranNightModeIb.setOnClickListener { onQuranNightModeClick() }
        binding!!.ibZoomIn.setOnClickListener { zoomIn() }
        binding!!.ibZoomOut.setOnClickListener { zoomOut() }
    }

    private fun zoomIn() {
        quranPageZoomScaleFactor += ZOOM_SCALE_INCREMENT
        setupZoomButtons()
        footerCallbacks!!.updateQuranPageZoomScale(quranPageZoomScaleFactor)
    }

    private fun zoomOut() {
        quranPageZoomScaleFactor -= ZOOM_SCALE_INCREMENT
        setupZoomButtons()
        footerCallbacks!!.updateQuranPageZoomScale(quranPageZoomScaleFactor)
    }

    private fun setupButtonsTooltips() {
        TooltipCompat.setTooltipText(
            binding!!.ibZoomIn,
            getString(R.string.tooltip_zoom_in_quran_page)
        )
        TooltipCompat.setTooltipText(
            binding!!.ibZoomOut,
            getString(R.string.tooltip_zoom_out_quran_page)
        )
        TooltipCompat.setTooltipText(
            binding!!.quranNightModeIb,
            getString(R.string.tooltip_quran_night_mode)
        )
        TooltipCompat.setTooltipText(
            binding!!.quranSearchIb,
            getString(R.string.tooltip_quran_search)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onQuranSearchClick() {
        footerCallbacks!!.openSearchFragment()
    }

    private fun onQuranNightModeClick() {
        viewModel.toggleNightMode()
    }

    private fun setupNightModeButton(nightMode: Boolean) {
        binding!!.quranNightModeIb.setImageResource(
            if (nightMode) R.drawable.ic_nightmode_on else R.drawable.ic_nightmode_off
        )
    }

    fun setCurrentPage(pageNumText: String) {
        this.pageNumText = pageNumText
        binding?.quranPageTv?.text = pageNumText
    }

    interface QuranFooterCallbacks {
        fun openSearchFragment()
        fun updateQuranPageZoomScale(zoomScaleFactor: Float)
    }

    companion object {
        private val TAG = MushafBottomBarFragment::class.java.simpleName

        private const val ZOOM_SCALE_INCREMENT = 0.05f
    }
}
