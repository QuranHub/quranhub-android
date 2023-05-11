package app.quranhub.ui.mushaf.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class HizbQuarterDataModel {

    @ColumnInfo(name = "sura_number")
    private int suraNumber;
    @ColumnInfo(name = "aya_number")
    private int ayaNumber;
    @ColumnInfo(name = "aya_text")
    private String ayaText;
    @ColumnInfo(name = "start_page")
    private int startPage;
    @ColumnInfo(name = "end_page")
    private int endPage;
    private int juz;
    private int hizb;
    private int quarter;

    public HizbQuarterDataModel(int suraNumber, int ayaNumber, String ayaText, int startPage, int endPage, int juz, int hizb, int quarter) {
        this.suraNumber = suraNumber;
        this.ayaNumber = ayaNumber;
        this.ayaText = ayaText;
        this.startPage = startPage;
        this.endPage = endPage;
        this.juz = juz;
        this.hizb = hizb;
        this.quarter = quarter;
    }

    public int getSuraNumber() {
        return suraNumber;
    }

    public void setSuraNumber(int suraNumber) {
        this.suraNumber = suraNumber;
    }

    public int getAyaNumber() {
        return ayaNumber;
    }

    public void setAyaNumber(int ayaNumber) {
        this.ayaNumber = ayaNumber;
    }

    public String getAyaText() {
        return ayaText;
    }

    public void setAyaText(String ayaText) {
        this.ayaText = ayaText;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }

    public int getHizb() {
        return hizb;
    }

    public void setHizb(int hizb) {
        this.hizb = hizb;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    @NonNull
    @Override
    public String toString() {
        return "HizbQuarterDataModel{" +
                "suraNumber=" + suraNumber +
                ", ayaNumber=" + ayaNumber +
                ", ayaText='" + ayaText + '\'' +
                ", startPage=" + startPage +
                ", endPage=" + endPage +
                ", juz=" + juz +
                ", hizb=" + hizb +
                ", quarter=" + quarter +
                '}';
    }
}
