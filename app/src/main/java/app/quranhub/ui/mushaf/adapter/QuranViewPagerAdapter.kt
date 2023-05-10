package app.quranhub.ui.mushaf.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import app.quranhub.data.Constants
import app.quranhub.ui.mushaf.fragments.QuranPageFragment.Companion.getInstance

class QuranViewPagerAdapter(
    fm: FragmentManager?,
    private val imagesUrl: List<String>,
    private var nightMode: Boolean,
    private var initSelectedAyaId: Int
) : FragmentStatePagerAdapter(
    fm!!
) {

    override fun getItem(position: Int): Fragment {
        val pageFragment = getInstance(
            imagesUrl[position],
            Constants.Quran.NUM_OF_PAGES - position,
            initSelectedAyaId,
            nightMode
        )
        initSelectedAyaId =
            -1 // reset it back to default (no selection) after returning the first item
        return pageFragment
    }

    override fun getCount(): Int {
        return imagesUrl.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE // required for QuranViewPagerAdapter#notifyDataSetChanged to work.
    }

    fun setNightMode(nightMode: Boolean) {
        this.nightMode = nightMode
        notifyDataSetChanged()
    }
}