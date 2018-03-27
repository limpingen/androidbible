package org.sabda.alkitabquotegenerator.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sabda.alkitabquotegenerator.App;
import org.sabda.alkitabquotegenerator.BuildConfig;
import org.sabda.alkitabquotegenerator.R;
import org.sabda.opoc.util.AppSettingsBase;

public class AppSettings extends AppSettingsBase {
    private static final int MAX_FAVS = 50;
    private static boolean PACKAGE_CHECKED = false;

    //#####################
    //## Methods
    //#####################
    public AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        AppSettings appSettings = new AppSettings(App.get());


        /*
         * Check if a MemeTastic package ID was used to build the app.
         * If you release something based on MemeTastic you will want to remove the lines below.
         * In any case: You MUST release the full source code.
         *
         * If you publish an app based on MemeTastic you MUST
         *   Comply with the terms of GPLv3 - See https://www.gnu.org/licenses/gpl-3.0.html
         *   Keep existing copyright notices in the app and publish full source code
         *   Show that the app is `based on MemeTastic by MemeTastic developers and contributors`. Include a link to https://github.com/gsantner/memetastic
         *   Show that the app is not MemeTastic but an modified/custom version, and the original app developers or contributors are not responsible for modified versions
         *   Not use MemeTastic as app name
         *
         *  See more details at
         *  https://github.com/gsantner/memetastic/blob/master/README.md#licensing
         */

        return appSettings;
    }

    // Adds a String to a String array and cuts of the last values to match a maximal size
    private static String[] insertAndMaximize(String[] values, String value, int maxSize) {
        List<String> list;
        if (values == null)
            list = new ArrayList<>();
        else
            list = new ArrayList<>(Arrays.asList(values));
        list.add(0, value);
        while (list.size() > maxSize) {
            list.remove(maxSize - 1);
        }
        return list.toArray(new String[list.size()]);
    }

    public int getRenderQualityReal() {
        int val = getInt(prefApp, R.string.pref_key__render_quality__percent, 24);
        return (int) (400 + (2100.0 * (val / 100.0)));
    }

    public void setLastSelectedFont(int value) {
        setInt(prefApp, R.string.pref_key__last_selected_font, value);
    }

    public int getLastSelectedFont() {
        return getInt(prefApp, R.string.pref_key__last_selected_font, 0);
    }

    public void setFavoriteMemes(String[] value) {
        setStringArray(prefApp, R.string.pref_key__meme_favourites, value);
    }

    public String[] getFavoriteMemes() {
        return getStringArray(prefApp, R.string.pref_key__meme_favourites);
    }

    public void appendFavoriteMeme(String meme) {
        String[] memes = insertAndMaximize(getFavoriteMemes(), meme, MAX_FAVS);
        setFavoriteMemes(memes);
    }

    public boolean isFavorite(String name) {
        if (getFavoriteMemes() == null)
            return false;
        for (String s : getFavoriteMemes()) {
            if (s.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public boolean toggleFavorite(String name) {
        if (!isFavorite(name)) {
            appendFavoriteMeme(name);
            return true;
        }
        removeFavorite(name);
        return false;
    }

    public void removeFavorite(String name) {
        String[] favs = getFavoriteMemes();
        ArrayList<String> newFavs = new ArrayList<String>();

        for (String fav : favs) {
            if (!fav.equalsIgnoreCase(name))
                newFavs.add(fav);
        }
        setFavoriteMemes(newFavs.toArray(new String[newFavs.size()]));
    }

    public void setLastSelectedCategory(int value) {
        setInt(prefApp, R.string.pref_key__last_selected_category, value);
    }

    public int getLastSelectedCategory() {
        return getInt(prefApp, R.string.pref_key__last_selected_category, 0);
    }

    public int getGridColumnCountPortrait() {
        int count = getInt(prefApp, R.string.pref_key__grid_column_count_portrait, -1);
        if (count == -1) {
            Helpers myhelper = new Helpers(getContext());
            count = 3 + (int) Math.max(0, 0.5 * (myhelper.getEstimatedScreenSizeInches() - 5.0));
            setGridColumnCountPortrait(count);
        }
        return count;
    }

    public void setGridColumnCountPortrait(int value) {
        setInt(prefApp, R.string.pref_key__grid_column_count_portrait, value);
    }

    public int getGridColumnCountLandscape() {
        int count = getInt(prefApp, R.string.pref_key__grid_column_count_landscape, -1);
        if (count == -1) {
            count = (int) (getGridColumnCountPortrait() * 1.8);
            setGridColumnCountLandscape(count);
        }
        return count;
    }

    public void setGridColumnCountLandscape(int value) {
        setInt(prefApp, R.string.pref_key__grid_column_count_landscape, value);
    }

    public boolean isAppFirstStart(boolean doSet) {
        boolean value = getBool(prefApp, R.string.pref_key__app_first_start, true);
        if (doSet) {
            setBool(prefApp, R.string.pref_key__app_first_start, false);
        }
        return value;
    }

    public boolean isAppCurrentVersionFirstStart() {
        int value = getInt(prefApp, R.string.pref_key__app_first_start_current_version, -1);
        setInt(prefApp, R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
        return value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
    }

    public boolean isAutoSaveMeme() {
        return getBool(R.string.pref_key__auto_save_meme, false);
    }

    public int getDefaultMainMode() {
        return getIntOfStringPref(R.string.pref_key__default_main_mode, 0);
    }
}
