package app.quranhub.ui.mushaf.model;


import android.content.Context;

import app.quranhub.R;
import app.quranhub.util.LocaleUtils;

/**
 * Mapper class to use columns as strings to convert number to arabic
 */
public class SuraIndexModelMapper {

    private String id;
    private String name;
    private String guz;
    private String page;
    private String sura_type;
    private String num_of_aya;
    private String sura_hezb;
    private String sura_rob3;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuz() {
        return guz;
    }

    public void setGuz(String guz) {
        this.guz = guz;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getSura_type() {
        return sura_type;
    }

    public void setSura_type(String sura_type) {
        this.sura_type = sura_type;
    }

    public String getNum_of_aya() {
        return num_of_aya;
    }

    public void setNum_of_aya(String num_of_aya) {
        this.num_of_aya = num_of_aya;
    }

    public String getSura_hezb() {
        return sura_hezb;
    }

    public void setSura_hezb(String sura_hezb) {
        this.sura_hezb = sura_hezb;
    }

    public String getSura_rob3() {
        return sura_rob3;
    }

    public void setSura_rob3(String sura_rob3) {
        this.sura_rob3 = sura_rob3;
    }

    public static SuraIndexModelMapper mapToString(SuraIndexModel model, Context context) {
        SuraIndexModelMapper indexModelMapper = new SuraIndexModelMapper();
        String suraName = " " + context.getResources().getStringArray(R.array.sura_name)[model.getSura() - 1] + " ";

        indexModelMapper.setGuz(LocaleUtils.formatNumber(model.getJuz()));
        indexModelMapper.setId(LocaleUtils.formatNumber(model.getId()));
        indexModelMapper.setPage(LocaleUtils.formatNumber(model.getPage()));
        indexModelMapper.setNum_of_aya(LocaleUtils.formatNumber(model.getAyas()));
        indexModelMapper.setSura_hezb(LocaleUtils.formatNumber(model.getSura_hezb()));
        indexModelMapper.setSura_rob3(LocaleUtils.formatNumber(model.getSura_rob3()));
        indexModelMapper.setName(suraName);
        indexModelMapper.setSura_type(model.getType());


        return indexModelMapper;
    }

}
