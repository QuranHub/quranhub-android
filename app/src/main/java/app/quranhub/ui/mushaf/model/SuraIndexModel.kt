package app.quranhub.ui.mushaf.model;

import androidx.room.Ignore;

public class SuraIndexModel {


    private int id;
    private int sura;
    private int juz;
    private int page;
    private int ayas;
    private String type;

    // ignore them to be not included in join query of sura index

    @Ignore
    private int sura_hezb;
    @Ignore
    private int sura_rob3;

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

    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAyas() {
        return ayas;
    }

    public void setAyas(int ayas) {
        this.ayas = ayas;
    }

    public int getSura_hezb() {
        return sura_hezb;
    }

    public void setSura_hezb(int sura_hezb) {
        this.sura_hezb = sura_hezb;
    }

    public int getSura_rob3() {
        return sura_rob3;
    }

    public void setSura_rob3(int sura_rob3) {
        this.sura_rob3 = sura_rob3;
    }
}
