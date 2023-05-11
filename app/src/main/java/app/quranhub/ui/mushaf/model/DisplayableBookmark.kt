package app.quranhub.ui.mushaf.model;

public class DisplayableBookmark {

    private int bookmarkId;
    private int bookmarkType;
    private String ayaContent;
    private int ayaId;
    private int suraAyaNumber;
    private int guz2Number;
    private int hizbNumber;
    private int rub3Number;
    private String suraName;
    private int pageNumber;
    private int colorIndex;

    public DisplayableBookmark() {
    }

    public DisplayableBookmark(int bookmarkId, int bookmarkType, String ayaContent, int ayaId, int suraAyaNumber
            , int guz2Number, int hizbNumber, int rub3Number, String suraName, int pageNumber) {
        this.bookmarkId = bookmarkId;
        this.bookmarkType = bookmarkType;
        this.ayaContent = ayaContent;
        this.ayaId = ayaId;
        this.suraAyaNumber = suraAyaNumber;
        this.guz2Number = guz2Number;
        this.hizbNumber = hizbNumber;
        this.rub3Number = rub3Number;
        this.suraName = suraName;
        this.pageNumber = pageNumber;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public int getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(int bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public int getBookmarkType() {
        return bookmarkType;
    }

    public void setBookmarkType(int bookmarkType) {
        this.bookmarkType = bookmarkType;
    }

    public String getAyaContent() {
        return ayaContent;
    }

    public void setAyaContent(String ayaContent) {
        this.ayaContent = ayaContent;
    }

    public int getAyaId() {
        return ayaId;
    }

    public void setAyaId(int ayaId) {
        this.ayaId = ayaId;
    }

    public int getSuraAyaNumber() {
        return suraAyaNumber;
    }

    public void setSuraAyaNumber(int suraAyaNumber) {
        this.suraAyaNumber = suraAyaNumber;
    }

    public int getGuz2Number() {
        return guz2Number;
    }

    public void setGuz2Number(int guz2Number) {
        this.guz2Number = guz2Number;
    }

    public int getHizbNumber() {
        return hizbNumber;
    }

    public void setHizbNumber(int hizbNumber) {
        this.hizbNumber = hizbNumber;
    }

    public int getRub3Number() {
        return rub3Number;
    }

    public void setRub3Number(int rub3Number) {
        this.rub3Number = rub3Number;
    }

    public String getSuraName() {
        return suraName;
    }

    public void setSuraName(String suraName) {
        this.suraName = suraName;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public String toString() {
        return "DisplayableBookmark{" +
                "bookmarkId=" + bookmarkId +
                ", bookmarkType=" + bookmarkType +
                ", ayaContent='" + ayaContent + '\'' +
                ", ayaId=" + ayaId +
                ", suraAyaNumber=" + suraAyaNumber +
                ", guz2Number=" + guz2Number +
                ", hizbNumber=" + hizbNumber +
                ", rub3Number=" + rub3Number +
                ", suraName='" + suraName + '\'' +
                ", pageNumber=" + pageNumber +
                '}';
    }
}
