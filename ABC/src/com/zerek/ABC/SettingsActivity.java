package com.zerek.ABC;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: mode
 * Date: 6/27/13
 * Time: 4:01 PM
 */


public class SettingsActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static int I_TILE_PER_ROW = 0;

    private static final String C_TILE_PER_ROW = "tile_cnt";
    private static final String C_LANG = "lang";
    private static final String C_COMP_NAME = "comp_name";
    private static final String C_THEME = "theme";
    private static final String C_WEB_N_DATE = "web_ndate";

    private static final int[] themes = new int[]{R.style.Theme_Sherlock_Light, R.style.Theme_Sherlock, R.style.Theme_Sherlock_Light_DarkActionBar};

    // For refresh activity
    private static HashMap<String, Integer> refreshArr = new HashMap<String, Integer>();
    private static Integer refreshPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.prepWindow(this, true, R.string.mn_pref);
        // Don't change automatically
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        super.onCreate(savedInstanceState);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.prefs);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Language
    public static String getLang(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Configuration config = context.getResources().getConfiguration();
        return sharedPreferences.getString(C_LANG, config.locale.getLanguage());
    }

    public static boolean setLang(Context context) {
        String sOptLang = PreferenceManager.getDefaultSharedPreferences(context).getString(C_LANG, "");
        if (sOptLang.equals(""))
            return false;

        Configuration config = context.getResources().getConfiguration();
        if (!config.locale.getLanguage().equals(sOptLang)) {
            Locale locale = new Locale(sOptLang);
            Locale.setDefault(locale);

//            config = new Configuration();
            config.locale = locale;
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return true;
        }

        return false;
    }

    // Company name
    public static String getCompName(Context context, boolean bAddCo) {
        String sRes = PreferenceManager.getDefaultSharedPreferences(context).getString(C_COMP_NAME, "NIS");
        if (bAddCo)
            sRes = "©" + sRes;
        return sRes;
    }

    public static void setCompName(Context context, String compName) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(C_COMP_NAME, compName);
        editor.commit();
    }

    public static boolean getMusic(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("music", true);
    }

    public static boolean getAwake(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("awake", true);
    }

    public static boolean getBackMusic(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("back_music", true);
    }

    public static boolean getImgSound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("img_sound", true);
    }

    public static Class getCardMode() {
//        int ind = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("show_mode", "0"));
//
//        switch (ind) {
//            case 0:
        return Game04.class;

//            case 1:
//                return Game03.class;
//        }
//        return null;
    }

    public static int getSlideInterval(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("slide_int", "3"));
    }

    private static int getThemeInd(SharedPreferences sharedPreferences) {
        int ind = Integer.parseInt(sharedPreferences.getString(C_THEME, "0"));
        return themes[ind];
    }

    public static int getTheme(Context context) {
        return getThemeInd(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static void saveRefreshPos(Activity act) {
        String sKey = act.getClass().toString();
        refreshArr.remove(sKey);
        refreshArr.put(sKey, refreshPos);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(C_THEME) || key.equals(C_LANG)) {
            // New pos
            refreshPos++;

            // Update strings
            reShow(SettingsActivity.this);
        }

        if (key.equals(C_TILE_PER_ROW))
            refreshPos++;
    }

    public static void reShow(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }

    public static boolean checkRefreshPos(Activity act, boolean bReShow) {
        if (SettingsActivity.getAwake(act))
            act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        boolean bRes = !refreshArr.get(act.getClass().toString()).equals(refreshPos);
        if (bRes && bReShow)
            reShow(act);

        return bRes;
    }

    // Web notif date
    public static int getLastEvDate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(C_WEB_N_DATE, 20130919);
    }

    public static void setLastEvDate(Context context, int date) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(C_WEB_N_DATE, date);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public static void setTilePerRow(Context context) {
        I_TILE_PER_ROW = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(C_TILE_PER_ROW, "2"));
    }


    // Any opt
    public static String getOpt(Context context, String sId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(sId, "");
    }

    public static void setOpt(Context context, String sId, String sText) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(sId, sText);
        editor.commit();
    }
}
