package app.quranhub.util;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import app.quranhub.R;
import app.quranhub.ui.mushaf.fragments.SuraGuz2IndexFragment;

public final class DrawerUtils {

    private static final String TAG = DrawerUtils.class.getSimpleName();

    public static final int IDENTIFIER_MUSHAF = 0;
    public static final int IDENTIFIER_INDEX = 1;
    public static final int IDENTIFIER_TOPICS = 2;
    public static final int IDENTIFIER_LIBRARY = 3;
    public static final int IDENTIFIER_BOOKMARKS = 4;
    public static final int IDENTIFIER_MY_NOTES = 5;
    public static final int IDENTIFIER_SETTINGS = 6;
    public static final int IDENTIFIER_DOWNLOADS_MANAGER = 7;

    private DrawerUtils() { /* prevent instantiation */}

    /**
     * Creates and adds the side nav drawer to the passed activity.
     *
     * @param activity           activity to add nav drawer to.
     *                           Must implement {@link DrawerUtils.Mus7afDrawerItemClickListener}.
     * @param savedInstanceState The passed activity's {@code savedInstanceState} bundle.
     * @return the drawer instance.
     * @throws IllegalArgumentException if the passed activity doesn't implement
     *                                  {@link DrawerUtils.Mus7afDrawerItemClickListener}.
     */
    @NonNull
    public static Drawer initDrawer(@NonNull Activity activity, Bundle savedInstanceState
            , @NonNull OnDrawerListener onDrawerListener) {

        if (!(activity instanceof Mus7afDrawerItemClickListener)) {
            throw new IllegalArgumentException("The passed activity argument must implement " +
                    "DrawerUtils.Mus7afDrawerItemClickListener");
        }

        Mus7afDrawerItemClickListener clickListener = (Mus7afDrawerItemClickListener) activity;

        Drawer drawer = new DrawerBuilder()
                .withActivity(activity)
                .withHasStableIds(true)
                .withHeader(R.layout.nav_drawer_header)
                .withHeaderDivider(true)
                .withActionBarDrawerToggle(false)
                .withOnDrawerListener(onDrawerListener)
                .withSavedInstance(savedInstanceState)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(activity.getString(R.string.mushaf))
                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                                .withIcon(R.drawable.read_quran_sidemenu_green).withIdentifier(IDENTIFIER_MUSHAF)
                                .withTextColorRes(R.color.drawer_text_color)
                                .withSelectedIcon(R.drawable.read_quran_sidemenu_orange)
                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
                        new PrimaryDrawerItem()
                                .withName(activity.getString(R.string.fehris_menu))
                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                                .withIcon(R.drawable.index_green_sidemenu_ic)
                                .withIdentifier(IDENTIFIER_INDEX)
                                .withTextColorRes(R.color.drawer_text_color)
                                .withSelectedIcon(R.drawable.index_gold_sidemenu_ic)
                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
                        new PrimaryDrawerItem()
                                .withName(activity.getString(R.string.topics_menu))
                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                                .withIcon(R.drawable.topics_green_sidemenu_ic)
                                .withIdentifier(IDENTIFIER_TOPICS)
                                .withTextColorRes(R.color.drawer_text_color)
                                .withSelectedIcon(R.drawable.topics_gold_sidemenu_ic)
                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
//                        new PrimaryDrawerItem()
//                                .withName(activity.getString(R.string.library_menu))
//                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
//                                .withIcon(R.drawable.library_green_sidemenu_ic)
//                                .withIdentifier(IDENTIFIER_LIBRARY)
//                                .withTextColorRes(R.color.drawer_text_color)
//                                .withSelectedIcon(R.drawable.library_gold_sidemenu_ic)
//                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
                        new PrimaryDrawerItem()
                                .withName(activity.getString(R.string.fwasil_menu))
                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                                .withIcon(R.drawable.bookmark_green_sidemenu_ic)
                                .withIdentifier(IDENTIFIER_BOOKMARKS)
                                .withTextColorRes(R.color.drawer_text_color)
                                .withSelectedIcon(R.drawable.bookmark_gold_sidemenu_ic)
                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
                        new PrimaryDrawerItem()
                                .withName(activity.getString(R.string.notes_menu))
                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                                .withIcon(R.drawable.notes_green_sidemenu_ic)
                                .withIdentifier(IDENTIFIER_MY_NOTES)
                                .withTextColorRes(R.color.drawer_text_color)
                                .withSelectedIcon(R.drawable.notes_gold_sidemenu_ic)
                                .withSelectedTextColorRes(R.color.drawer_selected_tint),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(activity.getString(R.string.settings_menu))
                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
                                .withIcon(R.drawable.settings_green_sidemenu_ic)
                                .withIdentifier(IDENTIFIER_SETTINGS)
                                .withTextColorRes(R.color.drawer_text_color)
                                .withSelectedIcon(R.drawable.settings_gold_sidemenu_ic)
                                .withSelectedTextColorRes(R.color.drawer_selected_tint)
                                .withSelectable(false)
//                        , new PrimaryDrawerItem()
//                                .withName(activity.getString(R.string.downloads_menu))
//                                .withTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/droid_arabic_kufi.ttf"))
//                                .withIcon(R.drawable.downloads_green_sidemenu_ic)
//                                .withIdentifier(IDENTIFIER_DOWNLOADS_MANAGER)
//                                .withTextColorRes(R.color.drawer_text_color)
//                                .withSelectedIcon(R.drawable.downloads_gold_sidemenu_ic)
//                                .withSelectedTextColorRes(R.color.drawer_selected_tint)
//                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem != null) {
                        switch ((int) drawerItem.getIdentifier()) {
                            case IDENTIFIER_MUSHAF:
                                Log.d(TAG, "Item 0 clicked: mushaf");
                                clickListener.openMushaf();
                                break;
                            case IDENTIFIER_INDEX:
                                Log.d(TAG, "Item 1 clicked: index");
                                clickListener.openIndex(SuraGuz2IndexFragment.SURA_INDEX_TAB);
                                break;
                            case IDENTIFIER_TOPICS:
                                Log.d(TAG, "Item 2 clicked: topics");
                                clickListener.openTopics();
                                break;
                            case IDENTIFIER_LIBRARY:
                                Log.d(TAG, "Item 3 clicked: library");
                                clickListener.openLibrary();
                                break;
                            case IDENTIFIER_BOOKMARKS:
                                Log.d(TAG, "Item 4 clicked: bookmarks");
                                clickListener.openBookmarks();
                                break;
                            case IDENTIFIER_MY_NOTES:
                                Log.d(TAG, "Item 5 clicked: my notes");
                                clickListener.openMyNotes();
                                break;
                            case IDENTIFIER_SETTINGS:
                                Log.d(TAG, "Item 6 clicked: settings");
                                clickListener.openSettings();
                                break;
                            case IDENTIFIER_DOWNLOADS_MANAGER:
                                Log.d(TAG, "Item 7 clicked: download manager");
                                clickListener.openDownloadsManager();
                                break;

                        }
                    }
                    return false;
                })
                .build();
        return drawer;
    }

    public interface Mus7afDrawerItemClickListener {

        void openIndex(int indexTab);

        void openTopics();

        void openLibrary();

        void openBookmarks();

        void openMyNotes();

        void openSettings();

        void openDownloadsManager();

        void openMushaf();
    }

}
