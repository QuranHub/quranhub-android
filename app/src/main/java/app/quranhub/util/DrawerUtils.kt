package app.quranhub.util

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import app.quranhub.R
import app.quranhub.ui.mushaf.fragments.SuraGuz2IndexFragment
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.Drawer.OnDrawerListener
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

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
     * Creates and adds the side nav drawer to the passed activity.
     *
     * @param activity           activity to add nav drawer to.
     * Must implement [DrawerUtils.Mus7afDrawerItemClickListener].
     * @param savedInstanceState The passed activity's `savedInstanceState` bundle.
     * @return the drawer instance.
     * @throws IllegalArgumentException if the passed activity doesn't implement
     * [DrawerUtils.Mus7afDrawerItemClickListener].
     */
    @JvmStatic
    fun initDrawer(
        activity: Activity, savedInstanceState: Bundle?, onDrawerListener: OnDrawerListener
    ): Drawer {
        require(activity is Mus7afDrawerItemClickListener) {
            "The passed activity argument must implement " +
                    "DrawerUtils.Mus7afDrawerItemClickListener"
        }
        val clickListener =
            activity as Mus7afDrawerItemClickListener
        return DrawerBuilder()
            .withActivity(activity)
            .withHasStableIds(true)
            .withHeader(R.layout.nav_drawer_header)
            .withHeaderDivider(true)
            .withActionBarDrawerToggle(false)
            .withOnDrawerListener(onDrawerListener)
            .withSavedInstance(savedInstanceState)
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withName(activity.getString(R.string.mushaf))
                    .withTypeface(
                        Typeface.createFromAsset(
                            activity.assets,
                            "fonts/droid_arabic_kufi.ttf"
                        )
                    )
                    .withIcon(R.drawable.read_quran_sidemenu_green)
                    .withIdentifier(IDENTIFIER_MUSHAF.toLong())
                    .withTextColorRes(R.color.drawer_text_color)
                    .withSelectedIcon(R.drawable.read_quran_sidemenu_orange)
                    .withSelectedTextColorRes(R.color.drawer_selected_tint),
                PrimaryDrawerItem()
                    .withName(activity.getString(R.string.fehris_menu))
                    .withTypeface(
                        Typeface.createFromAsset(
                            activity.assets,
                            "fonts/droid_arabic_kufi.ttf"
                        )
                    )
                    .withIcon(R.drawable.index_green_sidemenu_ic)
                    .withIdentifier(IDENTIFIER_INDEX.toLong())
                    .withTextColorRes(R.color.drawer_text_color)
                    .withSelectedIcon(R.drawable.index_gold_sidemenu_ic)
                    .withSelectedTextColorRes(R.color.drawer_selected_tint),
                PrimaryDrawerItem()
                    .withName(activity.getString(R.string.topics_menu))
                    .withTypeface(
                        Typeface.createFromAsset(
                            activity.assets,
                            "fonts/droid_arabic_kufi.ttf"
                        )
                    )
                    .withIcon(R.drawable.topics_green_sidemenu_ic)
                    .withIdentifier(IDENTIFIER_TOPICS.toLong())
                    .withTextColorRes(R.color.drawer_text_color)
                    .withSelectedIcon(R.drawable.topics_gold_sidemenu_ic)
                    .withSelectedTextColorRes(R.color.drawer_selected_tint),  //                        new PrimaryDrawerItem()
                //                                .withName(activity.getString(R.string.library_menu))
                //                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                //                                .withIcon(R.drawable.library_green_sidemenu_ic)
                //                                .withIdentifier(IDENTIFIER_LIBRARY)
                //                                .withTextColorRes(R.color.drawer_text_color)
                //                                .withSelectedIcon(R.drawable.library_gold_sidemenu_ic)
                //                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
                PrimaryDrawerItem()
                    .withName(activity.getString(R.string.fwasil_menu))
                    .withTypeface(
                        Typeface.createFromAsset(
                            activity.assets,
                            "fonts/droid_arabic_kufi.ttf"
                        )
                    )
                    .withIcon(R.drawable.bookmark_green_sidemenu_ic)
                    .withIdentifier(IDENTIFIER_BOOKMARKS.toLong())
                    .withTextColorRes(R.color.drawer_text_color)
                    .withSelectedIcon(R.drawable.bookmark_gold_sidemenu_ic)
                    .withSelectedTextColorRes(R.color.drawer_selected_tint),
                PrimaryDrawerItem()
                    .withName(activity.getString(R.string.notes_menu))
                    .withTypeface(
                        Typeface.createFromAsset(
                            activity.assets,
                            "fonts/droid_arabic_kufi.ttf"
                        )
                    )
                    .withIcon(R.drawable.notes_green_sidemenu_ic)
                    .withIdentifier(IDENTIFIER_MY_NOTES.toLong())
                    .withTextColorRes(R.color.drawer_text_color)
                    .withSelectedIcon(R.drawable.notes_gold_sidemenu_ic)
                    .withSelectedTextColorRes(R.color.drawer_selected_tint),
                DividerDrawerItem(),
                PrimaryDrawerItem()
                    .withName(activity.getString(R.string.settings_menu))
                    .withTypeface(
                        Typeface.createFromAsset(
                            activity.assets,
                            "fonts/droid_arabic_kufi.ttf"
                        )
                    )
                    .withIcon(R.drawable.settings_green_sidemenu_ic)
                    .withIdentifier(IDENTIFIER_SETTINGS.toLong())
                    .withTextColorRes(R.color.drawer_text_color)
                    .withSelectedIcon(R.drawable.settings_gold_sidemenu_ic)
                    .withSelectedTextColorRes(R.color.drawer_selected_tint)
                    .withSelectable(false) //                        , new PrimaryDrawerItem()
                //                                .withName(activity.getString(R.string.downloads_menu))
                //                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                //                                .withIcon(R.drawable.downloads_green_sidemenu_ic)
                //                                .withIdentifier(IDENTIFIER_DOWNLOADS_MANAGER)
                //                                .withTextColorRes(R.color.drawer_text_color)
                //                                .withSelectedIcon(R.drawable.downloads_gold_sidemenu_ic)
                //                                .withSelectedTextColorRes(R.color.drawer_selected_tint)
                //                                .withSelectable(false)
            )
            .withOnDrawerItemClickListener { view: View?, position: Int, drawerItem: IDrawerItem<*, *>? ->
                if (drawerItem != null) {
                    when (drawerItem.identifier.toInt()) {
                        IDENTIFIER_MUSHAF -> {
                            Log.d(TAG, "Item 0 clicked: mushaf")
                            clickListener.openMushaf()
                        }

                        IDENTIFIER_INDEX -> {
                            Log.d(TAG, "Item 1 clicked: index")
                            clickListener.openIndex(SuraGuz2IndexFragment.SURA_INDEX_TAB)
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
                }
                false
            }
            .build()
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