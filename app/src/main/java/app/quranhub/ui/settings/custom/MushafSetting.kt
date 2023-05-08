package app.quranhub.ui.settings.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import app.quranhub.R

/**
 * A Setting item that can display the setting name & current setting value.
 */
class MushafSetting(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val nameTextView: TextView
    private val currentValueTextView: TextView
    private val arrowImageView: ImageView

    /**
     * Name of the setting; mandatory.
     */
    var name: String? = null
        set(value) {
            field = value
            nameTextView.text = value
        }

    /**
     * Current value of the setting; optional (default empty string).
     */
    var currentValue: String? = null
        set(value) {
            field = value ?: ""
            currentValueTextView.text = value
        }

    init {

        // initialize the View
        val outValue = TypedValue()
        getContext().theme.resolveAttribute(
            android.R.attr.selectableItemBackground, outValue, true
        )
        setBackgroundResource(outValue.resourceId)

        isClickable = true

        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_mushaf_setting, this, true)

        nameTextView = findViewById(R.id.tv_name)
        currentValueTextView = findViewById(R.id.tv_current_value)
        arrowImageView = findViewById(R.id.iv_arrow)

        if (resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
            arrowImageView.setImageResource(R.drawable.arrow_backward_gray_ic)
        }

        // read the attributes
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.MushafSetting, 0, 0
        )
        name = if (typedArray.hasValue(R.styleable.MushafSetting_settingName)) {
            typedArray.getString(R.styleable.MushafSetting_settingName)
        } else {
            error("Attribute 'settingName' is not defined or could not be coerced to a string.")
        }
        currentValue = if (typedArray.hasValue(R.styleable.MushafSetting_currentValue)) {
            typedArray.getString(R.styleable.MushafSetting_currentValue)
        } else ""
        typedArray.recycle()
    }

    companion object {
        private val TAG = MushafSetting::class.java.simpleName
    }
}