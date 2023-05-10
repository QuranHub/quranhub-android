package app.quranhub.ui.mushaf.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import app.quranhub.R
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.databinding.FragmentMushafBottomBarBinding
import app.quranhub.ui.mushaf.presenter.QuranFooterPresenter
import app.quranhub.ui.mushaf.presenter.QuranFooterPresenterImp
import app.quranhub.ui.mushaf.view.QuranFooterView

class MushafBottomBarFragment : Fragment(), QuranFooterView {

    private var binding: FragmentMushafBottomBarBinding? = null

    private var presenter: QuranFooterPresenter? = null
    private var nightMode = false
    private var quranPageZoomScaleFactor = 1f
    private var footerCallbacks: QuranFooterCallbacks? = null
    private var pageNumTextLiveData: MutableLiveData<String>? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumTextLiveData = MutableLiveData()
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
        initViews()
        presenter!!.onAttach(this)
        pageNumTextLiveData!!.observe(viewLifecycleOwner) { pageNumText: String? ->
            binding!!.quranPageTv.text = pageNumText
        }
    }

    private fun initViews() {
        setupButtonsTooltips()
        presenter = QuranFooterPresenterImp()
        nightMode = AppPreferencesManager.getNightModeSetting(requireActivity())
        setupNightModeButton()
        quranPageZoomScaleFactor =
            AppPreferencesManager.getQuranPageZoomScaleSetting(requireActivity())
        setupZoomButtons()
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
        presenter!!.onDetach()
    }

    private fun onQuranSearchClick() {
        presenter!!.displaySearchDialog()
    }

    private fun onQuranNightModeClick() {
        presenter!!.toggleNightMode()
    }

    private fun setupNightModeButton() {
        binding!!.quranNightModeIb.setImageResource(
            if (nightMode) R.drawable.ic_nightmode_on else R.drawable.ic_nightmode_off
        )
    }

    fun setCurrentPage(pageNumText: String) {
        pageNumTextLiveData!!.value = pageNumText
    }

    override fun displaySearchDialog() {
        footerCallbacks!!.openSearchFragment()
    }

    override fun toggleNightMode() {
        nightMode = footerCallbacks!!.toggleNightMode()
        setupNightModeButton()
    }

    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {}
    override fun hideLoading() {}

    interface QuranFooterCallbacks {
        fun openSearchFragment()
        fun toggleNightMode(): Boolean
        fun updateQuranPageZoomScale(zoomScaleFactor: Float)
    }

    companion object {
        private val TAG = MushafBottomBarFragment::class.java.simpleName

        private const val ZOOM_SCALE_INCREMENT = 0.05f
    }
}