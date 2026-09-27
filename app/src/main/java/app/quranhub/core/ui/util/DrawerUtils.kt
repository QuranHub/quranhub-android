package app.quranhub.core.ui.util

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import app.quranhub.R
import app.quranhub.core.common.util.InsetsUtils
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView

object DrawerUtils {
    private val TAG = DrawerUtils::class.java.simpleName

    const val IDENTIFIER_MUSHAF = 0
    const val IDENTIFIER_INDEX = 1
    const val IDENTIFIER_TOPICS = 2
    const val IDENTIFIER_LIBRARY = 3
    const val IDENTIFIER_BOOKMARKS = 4
    const val IDENTIFIER_MY_NOTES = 5
    const val IDENTIFIER_SETTINGS = 6
    const val IDENTIFIER_DOWNLOADS_MANAGER = 7

    /**
     * Fills the host-owned [MaterialDrawerSliderView].
     *
     * The activity layout must already contain the slider inside a `DrawerLayout`
     * (gravity start, no `fitsSystemWindows`).
     *
     * @param activity activity that hosts the drawer.
     * Must implement [Mus7afDrawerItemClickListener].
     * @throws IllegalArgumentException if the passed activity doesn't implement
     * [Mus7afDrawerItemClickListener].
     */
    @JvmStatic
    fun initDrawer(
        activity: Activity,
        slider: MaterialDrawerSliderView,
        savedInstanceState: Bundle?,
    ) {
        val clickListener =
            activity as? Mus7afDrawerItemClickListener
                ?: throw IllegalArgumentException(
                    "The passed activity argument must implement " +
                        "DrawerUtils.Mus7afDrawerItemClickListener",
                )
        val kufi = Typeface.createFromAsset(activity.assets, "fonts/droid_arabic_kufi.ttf")
        val textColors =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(activity, R.color.drawer_selected_tint),
                    ContextCompat.getColor(activity, R.color.drawer_text_color),
                ),
            )

        fun item(
            name: String,
            icon: Int,
            selectedIcon: Int,
            id: Int,
            selectable: Boolean = true,
        ) = PrimaryDrawerItem().apply {
            this.name = StringHolder(name)
            typeface = kufi
            this.icon = ImageHolder(icon)
            identifier = id.toLong()
            this.selectedIcon = ImageHolder(selectedIcon)
            textColor = textColors
            isSelectable = selectable
        }

        val header =
            LayoutInflater
                .from(activity)
                .inflate(R.layout.nav_drawer_header, slider, false)
        InsetsUtils.padTopForStatusBar(header)
        InsetsUtils.padBottomForNavigationBar(slider)

        slider.hasStableIds = true
        slider.headerDivider = true
        slider.headerView = header
        slider.itemAdapter.add(
            item(
                activity.getString(R.string.mushaf),
                R.drawable.read_quran_sidemenu_green,
                R.drawable.read_quran_sidemenu_orange,
                IDENTIFIER_MUSHAF,
            ),
            item(
                activity.getString(R.string.fehris_menu),
                R.drawable.index_green_sidemenu_ic,
                R.drawable.index_gold_sidemenu_ic,
                IDENTIFIER_INDEX,
            ),
            item(
                activity.getString(R.string.topics_menu),
                R.drawable.topics_green_sidemenu_ic,
                R.drawable.topics_gold_sidemenu_ic,
                IDENTIFIER_TOPICS,
            ),
            item(
                activity.getString(R.string.library_menu),
                R.drawable.library_green_sidemenu_ic,
                R.drawable.library_gold_sidemenu_ic,
                IDENTIFIER_LIBRARY,
            ),
            item(
                activity.getString(R.string.fwasil_menu),
                R.drawable.bookmark_green_sidemenu_ic,
                R.drawable.bookmark_gold_sidemenu_ic,
                IDENTIFIER_BOOKMARKS,
            ),
            item(
                activity.getString(R.string.notes_menu),
                R.drawable.notes_green_sidemenu_ic,
                R.drawable.notes_gold_sidemenu_ic,
                IDENTIFIER_MY_NOTES,
            ),
            DividerDrawerItem(),
            item(
                activity.getString(R.string.settings_menu),
                R.drawable.settings_green_sidemenu_ic,
                R.drawable.settings_gold_sidemenu_ic,
                IDENTIFIER_SETTINGS,
                selectable = false,
            ),
            item(
                activity.getString(R.string.downloads_menu),
                R.drawable.downloads_green_sidemenu_ic,
                R.drawable.downloads_gold_sidemenu_ic,
                IDENTIFIER_DOWNLOADS_MANAGER,
                selectable = false,
            ),
        )
        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            when (drawerItem.identifier.toInt()) {
                IDENTIFIER_MUSHAF -> {
                    Log.d(TAG, "Item 0 clicked: mushaf")
                    clickListener.openMushaf()
                }

                IDENTIFIER_INDEX -> {
                    Log.d(TAG, "Item 1 clicked: index")
                    clickListener.openIndex(0 /* SuraGuz2IndexFragment.SURA_INDEX_TAB; inlined so core does not depend on feature */)
                }

                IDENTIFIER_TOPICS -> {
                    Log.d(TAG, "Item 2 clicked: topics")
                    clickListener.openTopics()
                }

                IDENTIFIER_LIBRARY -> {
                    Log.d(TAG, "Item 3 clicked: library")
                    clickListener.openLibrary()
                }

                IDENTIFIER_BOOKMARKS -> {
                    Log.d(TAG, "Item 4 clicked: bookmarks")
                    clickListener.openBookmarks()
                }

                IDENTIFIER_MY_NOTES -> {
                    Log.d(TAG, "Item 5 clicked: my notes")
                    clickListener.openMyNotes()
                }

                IDENTIFIER_SETTINGS -> {
                    Log.d(TAG, "Item 6 clicked: settings")
                    clickListener.openSettings()
                }

                IDENTIFIER_DOWNLOADS_MANAGER -> {
                    Log.d(TAG, "Item 7 clicked: download manager")
                    clickListener.openDownloadsManager()
                }
            }
            false
        }
        if (savedInstanceState != null) {
            slider.setSavedInstance(savedInstanceState)
        } else {
            slider.setSelection(IDENTIFIER_MUSHAF.toLong(), false)
        }
    }

    interface Mus7afDrawerItemClickListener {
        fun openIndex(indexTab: Int)

        fun openTopics()

        fun openLibrary()

        fun openBookmarks()

        fun openMyNotes()

        fun openSettings()

        fun openDownloadsManager()

        fun openMushaf()
    }
}
