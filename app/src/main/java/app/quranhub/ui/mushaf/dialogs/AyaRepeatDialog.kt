package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import app.quranhub.R
import app.quranhub.data.local.entity.Aya
import app.quranhub.databinding.DialogAyaRepeatBinding
import app.quranhub.ui.mushaf.model.RepeatModel
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import app.quranhub.util.DialogUtils.adjustDialogSize

class AyaRepeatDialog : DialogFragment() {

    private var dialog: Dialog? = null
    private var listener: AyaRepeatListener? = null
    private var suraVersesNumberArrayList: ArrayList<SuraVersesNumber>? = null
    private var selectedAya: Aya? = null
    private var lastAyaInPage = 0
    private var maxFromAyaNumber = 0
    private var maxToAyaNumber = 0
    private var fromSuraNumber = 0
    private var toSuraNumber = 0
    private var fromUser = false
    private var binding: DialogAyaRepeatBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as AyaRepeatListener?
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAyaRepeatBinding.inflate(layoutInflater)
        initializeDialog()
        readArgs()
        setFromToViews()
        observeSpinnerSelection()
        observeOnInputEditText()
        return dialog!!
    }

    private fun observeOnInputEditText() {
        binding!!.fromEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) return
                if (s.toString().toInt() > maxFromAyaNumber) {
                    binding!!.fromEt.error = getString(R.string.enter_valid_aya)
                } else {
                    binding!!.fromEt.error = null
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding!!.toEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) return
                if (s.toString().toInt() > maxToAyaNumber) {
                    binding!!.toEt.error = getString(R.string.enter_valid_aya)
                } else {
                    binding!!.toEt.error = null
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun observeSpinnerSelection() {
        binding!!.fromAyaSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent?.getChildAt(0) != null) (parent.getChildAt(0) as TextView).setTextColor(
                    requireActivity().resources.getColor(R.color.white_color)
                )
                maxFromAyaNumber = suraVersesNumberArrayList!![position].ayas
                if (fromUser) {
                    binding!!.fromEt.setText("1")
                    fromSuraNumber = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding!!.toAyaSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent?.getChildAt(0) != null) (parent.getChildAt(0) as TextView).setTextColor(
                    requireActivity().resources.getColor(R.color.white_color)
                )
                maxToAyaNumber = suraVersesNumberArrayList!![position].ayas
                if (fromUser) {
                    binding!!.toEt.setText(maxToAyaNumber.toString())
                    toSuraNumber = position
                } else {
                    fromUser = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setFromToViews() {
        val surahs = resources.getStringArray(R.array.sura_name)
        val dataAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, surahs
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding!!.fromAyaSp.adapter = dataAdapter
        binding!!.toAyaSp.adapter = dataAdapter
        if (selectedAya != null) {
            lastAyaInPage = suraVersesNumberArrayList!![selectedAya!!.sura - 1].ayas
            binding!!.fromEt.setText(selectedAya!!.suraAya.toString())
            binding!!.toEt.setText(lastAyaInPage.toString())
            binding!!.fromAyaSp.setSelection(selectedAya!!.sura - 1)
            binding!!.toAyaSp.setSelection(selectedAya!!.sura - 1)
            maxFromAyaNumber = suraVersesNumberArrayList!![selectedAya!!.sura - 1].ayas
            maxToAyaNumber = maxFromAyaNumber
            fromSuraNumber = selectedAya!!.sura - 1
            toSuraNumber = fromSuraNumber
        } else {
            binding!!.fromEt.setText("1")
            binding!!.toEt.setText("1")
            binding!!.fromAyaSp.setSelection(0)
            binding!!.toAyaSp.setSelection(0)
            maxFromAyaNumber = suraVersesNumberArrayList!![0].ayas
            maxToAyaNumber = maxFromAyaNumber
            fromSuraNumber = 0
            toSuraNumber = fromSuraNumber
        }
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(
            this, 0.8f,
            0.8f, 0.7f,
            0.9f
        )
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.setCanceledOnTouchOutside(false)
        attachListeners()
    }

    private fun readArgs() {
        arguments?.let {
            selectedAya = it.getParcelable(ARG_SELECTED_SURA)
            suraVersesNumberArrayList = it.getParcelableArrayList(ARG_SURA_VERSES_NUMBER)
        }
    }

    private fun attachListeners() {
        binding!!.repeatBtn.setOnClickListener { v: View? -> onClickRepeat() }
        binding!!.btnBack.setOnClickListener { v: View? -> onClickBack() }
    }

    private fun onClickRepeat() {
        if (binding!!.fromEt.error != null || binding!!.toEt.error != null) {
            Toast.makeText(activity, getString(R.string.enter_valid_aya), Toast.LENGTH_LONG).show()
        } else if (binding!!.fromEt.text.toString().isEmpty() || binding!!.toEt.text.toString()
                .isEmpty()
        ) {
            Toast.makeText(activity, getString(R.string.enter_repeat_interval), Toast.LENGTH_LONG)
                .show()
        } else if (fromSuraNumber > toSuraNumber) {
            Toast.makeText(activity, getString(R.string.invalid_repeat_interval), Toast.LENGTH_LONG)
                .show()
        } else if (fromSuraNumber == toSuraNumber && binding!!.fromEt.text.toString()
                .toInt() > binding!!.toEt.text.toString().toInt()
        ) {
            Toast.makeText(activity, getString(R.string.enter_valid_aya), Toast.LENGTH_LONG).show()
        } else {
            val repeatModel = RepeatModel()
            repeatModel.fromSura = fromSuraNumber + 1
            repeatModel.fromAyaId =
                getFromAyaId(binding!!.fromEt.text.toString().toInt(), fromSuraNumber + 1)
            repeatModel.fromAya = binding!!.fromEt.text.toString().toInt()
            repeatModel.toSura = toSuraNumber + 1
            repeatModel.toAyaId =
                getToAyaId(binding!!.toEt.text.toString().toInt(), toSuraNumber + 1)
            repeatModel.toAya = binding!!.toEt.text.toString().toInt()
            repeatModel.groupRepeatNum =
                if (binding!!.ayaGroupNumberEt.text.toString().trim { it <= ' ' }
                        .isEmpty() || binding!!.ayaGroupNumberEt.text.toString()
                        .toInt() == 0
                ) 1 else binding!!.ayaGroupNumberEt.text.toString().trim { it <= ' ' }.toInt()
            repeatModel.ayaRepeatNum =
                if (binding!!.ayaGroupNumberEt.text.toString().trim { it <= ' ' }
                        .isEmpty() || binding!!.ayaGroupNumberEt.text.toString()
                        .toInt() == 0
                ) 1 else binding!!.ayaGroupNumberEt.text.toString().trim { it <= ' ' }.toInt()
            repeatModel.delayTime = if (binding!!.delayEt.text.toString().trim { it <= ' ' }
                    .isEmpty()) 1 else binding!!.delayEt.text.toString().trim { it <= ' ' }.toInt()
            listener!!.onAyasRepeat(repeatModel)
            dismiss()
        }
    }

    private fun getFromAyaId(fromAya: Int, fromSura: Int): Int {
        var fromAyaId = fromAya
        for (i in 1 until fromSura) {
            fromAyaId += suraVersesNumberArrayList!![i - 1].ayas
        }
        return fromAyaId
    }

    private fun getToAyaId(toAya: Int, toSura: Int): Int {
        var toAyaId = toAya
        for (i in 1 until toSura) {
            toAyaId += suraVersesNumberArrayList!![i - 1].ayas
        }
        return toAyaId
    }

    private fun onClickBack() {
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    interface AyaRepeatListener {
        fun onAyasRepeat(repeatModel: RepeatModel?)
    }

    companion object {

        private const val ARG_SURA_VERSES_NUMBER = "ARG_SURA_VERSES_NUMBER"
        private const val ARG_SELECTED_SURA = "ARG_SELECTED_SURA"

        @JvmStatic
        fun getInstance(
            suraVersesNumberArrayList: ArrayList<SuraVersesNumber?>?,
            selectedAya: Aya?
        ): AyaRepeatDialog {
            val bundle = Bundle()
            bundle.putParcelable(ARG_SELECTED_SURA, selectedAya)
            bundle.putParcelableArrayList(ARG_SURA_VERSES_NUMBER, suraVersesNumberArrayList)
            val dialog = AyaRepeatDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}