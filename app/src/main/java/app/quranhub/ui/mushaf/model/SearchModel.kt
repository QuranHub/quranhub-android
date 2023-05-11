package app.quranhub.ui.mushaf.model;

import androidx.room.Ignore;

public class SearchModel {

    int id;
    int sura;
    String pure_text;
    String text;
    int page;
    int sura_aya;
    int juz;
    @Ignore
    int hezb;
    @Ignore
    int quarter;


    public int getHezb() {
        return hezb;
    }

    public void setHezb(int hezb) {
        this.hezb = hezb;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSura() {
        return sura;
    }

    public void setSura(int sura) {
        this.sura = sura;
    }

    public String getPure_text() {
        return pure_text;
    }

    public void setPure_text(String pure_text) {
        this.pure_text = pure_text;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSura_aya() {
        return sura_aya;
    }

    public void setSura_aya(int sura_aya) {
        this.sura_aya = sura_aya;
    }

    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }
}
