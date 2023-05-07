package app.quranhub.ui.settings.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import app.quranhub.R;

/**
 * Settings category ViewGroup that displays a given category title at the top.
 * Extension of LinearLayout, use it the same way you would use a linear layout.
 */
public class MushafSettingsCategory extends LinearLayout {

    private static final String TAG = MushafSettingsCategory.class.getSimpleName();

    /**
     * Title for the category; mandatory.
     */
    private String categoryTitle;

    private TextView titleTextView;


    public MushafSettingsCategory(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // first, read the attributes
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.MushafSettingsCategory, 0, 0);
        if (typedArray.hasValue(R.styleable.MushafSettingsCategory_categoryTitle)) {
            categoryTitle = typedArray.getString(R.styleable.MushafSettingsCategory_categoryTitle);
        } else {
            throw new RuntimeException(
                    "Attribute 'categoryTitle' is not defined or could not be coerced to a string.");
        }
        typedArray.recycle();

        // initialize the View
        setOrientation(LinearLayout.VERTICAL);
        setDividerDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.dark_grey)));
        setDividerPadding((int) context.getResources().getDimension(com.intuit.sdp.R.dimen._10sdp));
        setShowDividers(SHOW_DIVIDER_MIDDLE);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_mushaf_settings_category, this, true);

        titleTextView = (TextView) getChildAt(0);
        titleTextView.setText(categoryTitle);

    }

    public void setCategoryTitle(@NonNull String title) {
        categoryTitle = title;
        titleTextView.setText(categoryTitle);
    }

    @NonNull
    public String getCategoryTitle() {
        return categoryTitle;
    }

}
