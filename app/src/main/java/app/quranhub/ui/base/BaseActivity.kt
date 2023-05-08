package app.quranhub.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import app.quranhub.util.LocaleUtils;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleUtils.initAppLanguage(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.initAppLanguage(newBase));
    }

    public void restart() {
        if (getIntent() != null) {
            startActivity(getIntent());
            finish();
        } else {
            Log.e(TAG, "Couldn't restart the activity!");
        }
    }

}
