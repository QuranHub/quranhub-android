package app.quranhub.ui.settings.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import app.quranhub.R

/**
 * A Setting item that can display the setting name & a toggle switch.
 */
class MushafSettingSwitch(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    Checkable {

    /**
     * Name of the setting; mandatory
     */
    private var name: String? = null

    /**
     * Whether the Switch is checked or not; optional (default false)
     */
    private var checked: Boolean
    private val nameTextView: TextView
    private val settingSwitch: Switch
    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {

        // first, read the attributes
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.MushafSettingSwitch, 0, 0
        )
        name = if (typedArray.hasValue(R.styleable.MushafSettingSwitch_switchSettingName)) {
            typedArray.getString(R.styleable.MushafSettingSwitch_switchSettingName)
        } else {
            throw RuntimeException(
                "Attribute 'switchSettingName' is not defined or could not be coerced to a string."
            )
        }
        checked = typedArray.getBoolean(R.styleable.MushafSettingSwitch_switchChecked, false)
        typedArray.recycle()

        // initialize the View
        val outValue = TypedValue()
        getContext().theme.resolveAttribute(
            android.R.attr.selectableItemBackground, outValue, true
        )
        setBackgroundResource(outValue.resourceId)
        isClickable = true
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_mushaf_setting_switch, this, true)
        nameTextView = findViewById(R.id.tv_name)
        settingSwitch = findViewById(R.id.switch_setting)
        nameTextView.text = name
        settingSwitch.isChecked = checked
        settingSwitch.isClickable = false
        setOnClickListener { v: View? ->
            toggle()
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener!!.onCheckedChanged(this, checked)
            }
        }
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
        nameTextView.text = this.name
    }

    override fun setChecked(checked: Boolean) {
        this.checked = checked
        settingSwitch.isChecked = this.checked
    }

    override fun isChecked(): Boolean {
        return checked
    }

    override fun toggle() {
        isChecked = !checked
    }

    /**
     * Register a callback to be invoked when the checked state of this MushafSettingSwitch changes.
     *
     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    fun removeOnCheckedListener() {
        onCheckedChangeListener = null
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(settingSwitch: MushafSettingSwitch, checked: Boolean)
    }

    companion object {
        private val TAG = MushafSettingSwitch::class.java.simpleName
    }
}