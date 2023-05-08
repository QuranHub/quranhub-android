package app.quranhub.ui.settings.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.quranhub.R;

/**
 * A Setting item that can display the setting name & current setting value.
 */
public class MushafSetting extends FrameLayout {

    private static final String TAG = MushafSetting.class.getSimpleName();

    /**
     * Name of the setting; mandatory.
     */
    private String name;

    /**
     * Current value of the setting; optional (default empty string).
     */
    private String currentValue;

    private TextView nameTextView;
    private TextView currentValueTextView;
    private ImageView arrowImageView;


    public MushafSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // first, read the attributes
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.MushafSetting, 0, 0);
        if (typedArray.hasValue(R.styleable.MushafSetting_settingName)) {
            name = typedArray.getString(R.styleable.MushafSetting_settingName);
        } else {
            throw new RuntimeException(
                    "Attribute 'settingName' is not defined or could not be coerced to a string.");
        }
        if (typedArray.hasValue(R.styleable.MushafSetting_currentValue)) {
            currentValue = typedArray.getString(R.styleable.MushafSetting_currentValue);
        } else {
            currentValue = "";
        }
        typedArray.recycle();

        // initialize the View
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue
                , true);
        setBackgroundResource(outValue.resourceId);
        setClickable(true);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_mushaf_setting, this, true);

        nameTextView = findViewById(R.id.tv_name);
        currentValueTextView = findViewById(R.id.tv_current_value);
        arrowImageView = findViewById(R.id.iv_arrow);

        nameTextView.setText(name);
        currentValueTextView.setText(currentValue);
        if (getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
            arrowImageView.setImageResource(R.drawable.arrow_backward_gray_ic);
        }
    }

    public void setName(@NonNull String name) {
        this.name = name;
        nameTextView.setText(this.name);
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setCurrentValue(@Nullable String currentValue) {
        if (currentValue == null) {
            this.currentValue = "";
        } else {
            this.currentValue = currentValue;
        }
        currentValueTextView.setText(this.currentValue);
    }

    @Nullable
    public String getCurrentValue() {
        return currentValue;
    }

}
