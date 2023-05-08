package app.quranhub.ui.mushaf.dialogs

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import app.quranhub.R
import app.quranhub.databinding.AyaAudioViewBinding
import app.quranhub.util.LocaleUtils.appLanguage

class AyaAudioPopup(private val context: Context, private val listener: AyaAudioListener) {

    private var popupWindow: PopupWindow? = null
    private var binding: AyaAudioViewBinding? = null

    init {
        setWindowView()
    }

    private fun setWindowView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AyaAudioViewBinding.inflate(inflater)
        popupWindow = PopupWindow(
            binding!!.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        popupWindow!!.isOutsideTouchable = false
        popupWindow!!.elevation = 24f
        setViewDirections()
        attachListeners()
    }

    private fun setViewDirections() {
        if (appLanguage == "ar") {
            binding!!.prevAyaIv.setImageResource(R.drawable.player_fast_forward_white_ic)
            binding!!.nextAyaIv.setImageResource(R.drawable.player_fast_rewind_white_ic)
        }
    }

    fun showPopup(anchorView: View?) {
        if (!popupWindow!!.isShowing) {
            popupWindow!!.showAtLocation(
                anchorView,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                0,
                50
            )
        }
    }

    private fun attachListeners() {
        binding!!.playIv.setOnClickListener { v: View? -> onPlayAudio() }
        binding!!.recordIv.setOnClickListener { v: View? -> onClickRecord() }
        binding!!.nextAyaIv.setOnClickListener { v: View? -> playNextAya() }
        binding!!.prevAyaIv.setOnClickListener { v: View? -> playPrevAya() }
        binding!!.repeatIv.setOnClickListener { v: View? -> onClickRepeat() }
        binding!!.reciterIv.setOnClickListener { v: View? -> onClickReciter() }
        binding!!.stopIv.setOnClickListener { v: View? -> onClickStop() }
    }

    private fun onPlayAudio() {
        listener.checkPlayPauseState()
    }

    private fun onClickRecord() {
        listener.onPressRecord()
    }

    private fun playNextAya() {
        listener.onPlayNextAya()
    }

    private fun playPrevAya() {
        listener.onPlayPrevAya()
    }

    private fun onClickRepeat() {
        listener.onClickRepeat()
    }

    private fun onClickReciter() {
        listener.onClickReciter()
    }

    private fun onClickStop() {
        listener.onClickStop()
    }

    fun dismissPopup() {
        if (popupWindow!!.isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun setRecordState(hasRecorder: Boolean) {
        if (hasRecorder) {
            binding!!.recordIv.setImageResource(R.drawable.play_record)
        } else {
            binding!!.recordIv.setImageResource(R.drawable.player_record_white_ic)
        }
    }

    fun setPlayState() {
        binding!!.playIv.setImageResource(R.drawable.ic_pause)
    }

    fun setPauseState() {
        binding!!.playIv.setImageResource(R.drawable.player_play_white_ic)
    }

    interface AyaAudioListener {
        fun onPlayNextAya()
        fun onPlayPrevAya()
        fun onPressRecord()
        fun checkPlayPauseState()
        fun onClickRepeat()
        fun onClickReciter()
        fun onClickStop()
    }
}