package app.quranhub.ui.settings.custom

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.quranhub.R

/**
 * Settings category ViewGroup that displays a given category title at the top.
 * Extension of LinearLayout, use it the same way you would use a linear layout.
 */
class MushafSettingsCategory(context: Context, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    /**
     * Title for the category; mandatory.
     */
    private var categoryTitle: String? = null
    private val titleTextView: TextView

    init {

        // first, read the attributes
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.MushafSettingsCategory, 0, 0
        )
        categoryTitle = if (typedArray.hasValue(R.styleable.MushafSettingsCategory_categoryTitle)) {
            typedArray.getString(R.styleable.MushafSettingsCategory_categoryTitle)
        } else {
            throw RuntimeException(
                "Attribute 'categoryTitle' is not defined or could not be coerced to a string."
            )
        }
        typedArray.recycle()

        // initialize the View
        orientation = VERTICAL
        dividerDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.dark_grey))
        dividerPadding = context.resources.getDimension(com.intuit.sdp.R.dimen._10sdp).toInt()
        showDividers = SHOW_DIVIDER_MIDDLE
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_mushaf_settings_category, this, true)
        titleTextView = getChildAt(0) as TextView
        titleTextView.text = categoryTitle
    }

    fun setCategoryTitle(title: String) {
        categoryTitle = title
        titleTextView.text = categoryTitle
    }

    fun getCategoryTitle(): String {
        return categoryTitle!!
    }

    companion object {
        private val TAG = MushafSettingsCategory::class.java.simpleName
    }
}