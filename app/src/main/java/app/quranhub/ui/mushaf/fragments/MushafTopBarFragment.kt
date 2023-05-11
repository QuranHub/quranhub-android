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
import androidx.lifecycle.MutableLiveData
import app.quranhub.R
import app.quranhub.databinding.FragmentMushafTopBarBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener

class MushafTopBarFragment : Fragment() {

    private var binding: FragmentMushafTopBarBinding? = null

    private var toolbarActionsListener: ToolbarActionsListener? = null
    private var pageDirLiveData: MutableLiveData<Int>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarActionsListener) {
            toolbarActionsListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageDirLiveData = MutableLiveData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMushafTopBarBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        pageDirLiveData!!.observe(viewLifecycleOwner) { pageDir: Int? ->
            when (pageDir) {
                PAGE_DIR_RIGHT -> binding!!.ivPageDir.setImageResource(R.drawable.ic_quran_page_right)
                PAGE_DIR_LEFT -> binding!!.ivPageDir.setImageResource(R.drawable.ic_quran_page_left)
                else -> throw IllegalArgumentException("Invalid page dir")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        TooltipCompat.setTooltipText(binding!!.ivPageDir, getText(R.string.tooltip_page_dir))
        binding!!.llRoot.setOnTouchListener { v: View?, event: MotionEvent? ->
            true // To prevent event bubbling to the views below this one
        }
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.ivMenu.setOnClickListener { onNavHamburgerClick() }
        binding!!.btnPageGuz2.setOnClickListener { onGuz2Click() }
        binding!!.btnPageSura.setOnClickListener { onSuraClick() }
    }

    private fun onNavHamburgerClick() {
        toolbarActionsListener!!.onNavDrawerClick()
    }

    private fun onGuz2Click() {
        toolbarActionsListener!!.onGuz2Click()
    }

    fun onSuraClick() {
        toolbarActionsListener!!.onSuraClick()
    }

    fun setSuraText(suraName: String?) {
        binding!!.btnPageSura.text = suraName
    }

    fun setGuz2Text(currentGuz2: String?) {
        binding!!.btnPageGuz2.text = currentGuz2
    }

    /**
     * Control page dir icon.
     *
     * @param pageDir either [.PAGE_DIR_LEFT] or [.PAGE_DIR_RIGHT].
     */
    fun setPageDir(pageDir: Int) {
        pageDirLiveData!!.value = pageDir
    }

    companion object {

        private val TAG = MushafTopBarFragment::class.java.simpleName

        const val PAGE_DIR_RIGHT = 0
        const val PAGE_DIR_LEFT = 1
    }
}