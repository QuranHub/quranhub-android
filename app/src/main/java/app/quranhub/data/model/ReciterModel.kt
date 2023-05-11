package app.quranhub.data.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.Map;

import app.quranhub.data.Constants;
import app.quranhub.data.local.prefs.AppPreferencesManager;

public class ReciterModel {

    @NonNull
    @DocumentId
    private String id;

    @Nullable
    private Map<String, String> name;

    @Exclude
    @Nullable
    private String localizedName;

    @Nullable
    private Map<String, String> nationality;

    @Exclude
    @Nullable
    private String localizedNationality;

    @NonNull
    private String audioBaseUrl;

    public ReciterModel() {
        // No arg constructor
    }

    public ReciterModel(@NonNull String id, @NonNull Map<String, String> name,
                        @NonNull Map<String, String> nationality, @NonNull String audioBaseUrl) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.audioBaseUrl = audioBaseUrl;
    }

    public ReciterModel(@NonNull String id, @NonNull String localizedName,
                        @NonNull String localizedNationality, @NonNull String audioBaseUrl) {
        this.id = id;
        this.localizedName = localizedName;
        this.localizedNationality = localizedNationality;
        this.audioBaseUrl = audioBaseUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Nullable
    public Map<String, String> getName() {
        return name;
    }

    public void setName(@NonNull Map<String, String> name) {
        this.name = name;
    }

    @NonNull
    public String getLocalizedName(@NonNull Context context) {
        if (localizedName != null) return localizedName;

        final String appLangCode = AppPreferencesManager.getAppLangSetting(context);
        if (name.containsKey(appLangCode))
            return name.get(appLangCode);
        else return name.get(Constants.Language.DEFAULT_APP_LANGUAGE);
    }

    @Nullable
    public Map<String, String> getNationality() {
        return nationality;
    }

    public void setNationality(@NonNull Map<String, String> nationality) {
        this.nationality = nationality;
    }

    @NonNull
    public String getLocalizedNationality(@NonNull Context context) {
        if (localizedNationality != null) return localizedNationality;

        final String appLangCode = AppPreferencesManager.getAppLangSetting(context);
        if (nationality.containsKey(appLangCode))
            return nationality.get(appLangCode);
        else return nationality.get(Constants.Language.DEFAULT_APP_LANGUAGE);
    }

    @NonNull
    @PropertyName("audio_base_url")
    public String getAudioBaseUrl() {
        return audioBaseUrl;
    }

    @PropertyName("audio_base_url")
    public void setAudioBaseUrl(@NonNull String audioBaseUrl) {
        this.audioBaseUrl = audioBaseUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "Reciter{" +
                "reciterId='" + id + '\'' +
                ", name=" + name +
                ", nationality=" + nationality +
                ", audioBaseUrl='" + audioBaseUrl + '\'' +
                '}';
    }
}
