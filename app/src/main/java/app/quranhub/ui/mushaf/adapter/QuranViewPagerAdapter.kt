package app.quranhub.ui.mushaf.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import app.quranhub.data.Constants;
import app.quranhub.ui.mushaf.fragments.QuranPageFragment;

public class QuranViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> imagesUrl;
    private int initSelectedAyaId;
    private boolean nightMode;

    public QuranViewPagerAdapter(FragmentManager fm, @NonNull List<String> imagesUrl
            , boolean nightMode, int initSelectedAyaId) {
        super(fm);
        this.imagesUrl = imagesUrl;
        this.nightMode = nightMode;
        this.initSelectedAyaId = initSelectedAyaId;
    }

    @Override
    public Fragment getItem(int position) {
        QuranPageFragment pageFragment = QuranPageFragment.getInstance(imagesUrl.get(position)
                , Constants.Quran.NUM_OF_PAGES - position, initSelectedAyaId, nightMode);
        initSelectedAyaId = -1; // reset it back to default (no selection) after returning the first item
        return pageFragment;
    }

    @Override
    public int getCount() {

        return imagesUrl.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE; // required for QuranViewPagerAdapter#notifyDataSetChanged to work.
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
        notifyDataSetChanged();
    }
}
