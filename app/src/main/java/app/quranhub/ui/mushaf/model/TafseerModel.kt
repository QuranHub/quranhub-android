package app.quranhub.ui.mushaf.model;

import androidx.room.Ignore;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.data.local.entity.Translation;

public class TafseerModel {

    private String text;
    private String tafseer;
    private String pure_text;
    @Ignore
    private boolean isExpandable;

    public TafseerModel(String text, String tafseer, String pure_text) {
        this.text = text;
        this.tafseer = tafseer;
        this.pure_text = pure_text;
        this.isExpandable = false;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public String getPure_text() {
        return pure_text;
    }

    public void setPure_text(String pure_text) {
        this.pure_text = pure_text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTafseer() {
        return tafseer;
    }

    public void setTafseer(String tafseer) {
        this.tafseer = tafseer;
    }

    public static List<TafseerModel> map(List<Translation> translations, List<TafseerModel> ayasTafseer) {
        List<TafseerModel> tafseerModels = new ArrayList<>();
        for (int i = 0; i < translations.size(); i++) {
            tafseerModels.add(new TafseerModel(ayasTafseer.get(i).getText(), translations.get(i).getText(), ayasTafseer.get(i).getPure_text()));
        }
        return tafseerModels;
    }
}
