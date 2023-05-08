package app.quranhub.ui.base;

import android.app.Service;
import android.content.Context;

import app.quranhub.util.LocaleUtils;

public abstract class BaseService extends Service {

    @Override
    public void onCreate() {
        LocaleUtils.initAppLanguage(this);
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.initAppLanguage(newBase));
    }


}
