package app.quranhub.ui.downloads_manager.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import app.quranhub.R
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.ReciterRecitation
import app.quranhub.data.service.QuranAudioDownloaderService.Companion.downloadQuran
import app.quranhub.data.service.QuranAudioDownloaderService.Companion.downloadSura
import app.quranhub.databinding.DialogAudioDownloadAmountBinding
import app.quranhub.util.DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE
import app.quranhub.util.DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT
import app.quranhub.util.DialogUtils.adjustDialogSize
import app.quranhub.util.NetworkUtil.isNetworkAvailable

/**
 * A `DialogFragment` that allows the user to choose the Quran audio amount he wants to download.
 * Use the [AudioDownloadAmountDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AudioDownloadAmountDialogFragment : DialogFragment() {

    private var recitationId = 0
    private var reciterId: String? = null
    private var suraId = 0 // [optional, defaults to 1]
    private var selectedOption = 0
    private var binding: DialogAudioDownloadAmountBinding? = null
    private var listener: AudioDownloadListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is AudioDownloadListener) {
            context
        } else if (parentFragment is AudioDownloadListener) {
            parentFragment as AudioDownloadListener?
        } else {
            throw RuntimeException(
                "The containing fragment or activity must implement" +
                        " AudioDownloadAmountDialogFragment#AudioDownloadListener interface"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
            reciterId = it.getString(ARG_RECITER_ID)
            suraId = it.getInt(ARG_SURA_ID, 1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DialogAudioDownloadAmountBinding.inflate(inflater, container, false)
        initDialogView()
        return binding!!.root
    }

    private fun initDialogView() {
        setSelectedOption(OPTION_DOWNLOAD_SURA)

        // init surasSpinner
        val suras = resources.getStringArray(R.array.sura_name)
        val dataAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, suras
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding!!.spinnerSuras.adapter = dataAdapter
        binding!!.spinnerSuras.setSelection(suraId - 1)
        binding!!.spinnerSuras.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (parent?.getChildAt(0) != null) {
                        (parent.getChildAt(0) as TextView).setTextColor(
                            resources.getColor(R.color.white_color)
                        )
                    }
                    setSelectedOption(OPTION_DOWNLOAD_SURA)
                    suraId = position + 1
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        attachListeners()
    }

    private fun setSelectedOption(option: Int) {
        selectedOption = option
        if (selectedOption == OPTION_DOWNLOAD_SURA) {
            binding!!.ivCheckOptionSuraDownload.visibility = View.VISIBLE
            binding!!.ivCheckOptionDownloadAll.visibility = View.INVISIBLE
        } else if (selectedOption == OPTION_DOWNLOAD_ALL) {
            binding!!.ivCheckOptionSuraDownload.visibility = View.INVISIBLE
            binding!!.ivCheckOptionDownloadAll.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(
            this, DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT, 0.4f,
            DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE, 0.7f
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun attachListeners() {
        binding!!.clOptionSuraDownload.setOnClickListener { v: View? -> onSuraDownloadOptionClick() }
        binding!!.clOptionDownloadAll.setOnClickListener { v: View? -> onDownloadAllOptionClick() }
        binding!!.btnCancel.setOnClickListener { v: View? -> onCancelButtonClick() }
        binding!!.btnDownload.setOnClickListener { v: View? -> onDownloadButtonClick() }
    }

    private fun onSuraDownloadOptionClick() {
        setSelectedOption(OPTION_DOWNLOAD_SURA)
    }

    private fun onDownloadAllOptionClick() {
        setSelectedOption(OPTION_DOWNLOAD_ALL)
    }

    private fun onCancelButtonClick() {
        dismiss()
    }

    @SuppressLint("StaticFieldLeak")
    private fun onDownloadButtonClick() {
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(activity, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
            return
        }
        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg voids: Void?): Void? {
                // Store SheikhRecitation for the download recitation & reciter in DB
                val userDatabase = UserDatabase.getInstance(requireContext())
                if (userDatabase.reciterRecitationDao[recitationId, reciterId] == null) {
                    userDatabase.reciterRecitationDao
                        .insert(
                            ReciterRecitation(
                                recitationId = recitationId,
                                reciterId = reciterId!!
                            )
                        )
                }
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                if (selectedOption == OPTION_DOWNLOAD_SURA) {
                    downloadSura(requireContext(), recitationId, reciterId, suraId)
                } else if (selectedOption == OPTION_DOWNLOAD_ALL) {
                    downloadQuran(requireContext(), recitationId, reciterId)
                }
                Toast.makeText(
                    requireContext(), R.string.msg_quran_audio_download_started,
                    Toast.LENGTH_SHORT
                ).show()
                listener!!.onClickDownload()
                dismiss()
            }
        }.execute()
    }

    interface AudioDownloadListener {
        fun onClickDownload()
    }

    companion object {
        private val TAG = AudioDownloadAmountDialogFragment::class.java.simpleName

        private const val ARG_RECITATION_ID = "ARG_RECITATION_ID"
        private const val ARG_RECITER_ID = "ARG_RECITER_ID"
        private const val ARG_SURA_ID = "ARG_SURA_ID" // [optional]

        private const val OPTION_DOWNLOAD_SURA = 0
        private const val OPTION_DOWNLOAD_ALL = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param recitationId Recitation ID as in [Constants.Recitation]
         * @param reciterId    A reciter ID.
         * @param suraId       A sura ID to be selected when opening the dialog.
         * @return A new instance of fragment AudioDownloadAmountDialogFragment.
         */
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param recitationId Recitation ID as in [Constants.Recitation]
         * @param reciterId    A reciter ID.
         * @return A new instance of fragment AudioDownloadAmountDialogFragment.
         */
        @JvmOverloads
        fun newInstance(
            recitationId: Int, reciterId: String, suraId: Int = 1
        ): AudioDownloadAmountDialogFragment {
            val dialogFragment = AudioDownloadAmountDialogFragment()
            val args = Bundle()
            args.putInt(ARG_RECITATION_ID, recitationId)
            args.putString(ARG_RECITER_ID, reciterId)
            args.putInt(ARG_SURA_ID, suraId)
            dialogFragment.arguments = args
            return dialogFragment
        }
    }
}