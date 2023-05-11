package app.quranhub.ui.mushaf.model;

public class MyNoteModel {

    private int sura;
    private int sura_aya;
    private String pure_text;
    private String text;
    private int page;

    public int getSura() {
        return sura;
    }

    public void setSura(int sura) {
        this.sura = sura;
    }

    public int getSura_aya() {
        return sura_aya;
    }

    public void setSura_aya(int sura_aya) {
        this.sura_aya = sura_aya;
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
