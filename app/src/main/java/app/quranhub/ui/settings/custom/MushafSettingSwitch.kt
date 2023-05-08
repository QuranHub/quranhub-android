package app.quranhub.ui.settings.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.quranhub.R;

/**
 * A Setting item that can display the setting name & a toggle switch.
 */
public class MushafSettingSwitch extends FrameLayout implements Checkable {

    private static final String TAG = MushafSettingSwitch.class.getSimpleName();

    private String name;  // Name of the setting; mandatory

    private Boolean checked; // Whether the Switch is checked or not; optional (default false)

    private TextView nameTextView;
    private Switch settingSwitch;

    @Nullable
    private OnCheckedChangeListener onCheckedChangeListener;


    public MushafSettingSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // first, read the attributes
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.MushafSettingSwitch, 0, 0);
        if (typedArray.hasValue(R.styleable.MushafSettingSwitch_switchSettingName)) {
            name = typedArray.getString(R.styleable.MushafSettingSwitch_switchSettingName);
        } else {
            throw new RuntimeException(
                    "Attribute 'switchSettingName' is not defined or could not be coerced to a string.");
        }
        checked = typedArray.getBoolean(R.styleable.MushafSettingSwitch_switchChecked, false);
        typedArray.recycle();

        // initialize the View
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue
                , true);
        setBackgroundResource(outValue.resourceId);
        setClickable(true);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_mushaf_setting_switch, this, true);

        nameTextView = findViewById(R.id.tv_name);
        settingSwitch = findViewById(R.id.switch_setting);

        nameTextView.setText(name);
        settingSwitch.setChecked(checked);
        settingSwitch.setClickable(false);
        this.setOnClickListener(v -> {
            toggle();
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, checked);
            }
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        nameTextView.setText(this.name);
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        settingSwitch.setChecked(this.checked);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    /**
     * Register a callback to be invoked when the checked state of this MushafSettingSwitch changes.
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    public void removeOnCheckedListener() {
        onCheckedChangeListener = null;
    }


    public interface OnCheckedChangeListener {
        void onCheckedChanged(@NonNull MushafSettingSwitch settingSwitch, boolean checked);
    }

}
