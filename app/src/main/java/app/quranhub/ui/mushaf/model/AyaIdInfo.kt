package app.quranhub.ui.mushaf.model;

public class AyaIdInfo {

    int ayaNumInSura;
    int suraNum;

    public AyaIdInfo(int ayaNumInSura, int suraNum) {
        this.ayaNumInSura = ayaNumInSura;
        this.suraNum = suraNum;
    }

    public int getAyaNumInSura() {
        return ayaNumInSura;
    }

    public void setAyaNumInSura(int ayaNumInSura) {
        this.ayaNumInSura = ayaNumInSura;
    }

    public int getSuraNum() {
        return suraNum;
    }

    public void setSuraNum(int suraNum) {
        this.suraNum = suraNum;
    }
}
