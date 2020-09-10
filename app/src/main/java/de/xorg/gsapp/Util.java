package de.xorg.gsapp;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import timber.log.Timber;

public class Util {

    static final int FIRSTRUN_ACTIVITY = 392;

    static final String CHANGELOG = ""
            + "App für Android 10 angepasst\n"
            + "Speiseplan behoben\n";

    static final String NTF_CHANNEL_ID = "gsapp_notifications";

    final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
    final static String EXTRA_NAME = "de.xorg.gsapp.MESSAGENAME";

    /**
     * Source: https://stackoverflow.com/questions/1181969/java-get-last-element-after-split
     * <p>Gets the last element from array</p>
     * @param array The array
     * @return Last element
     * @throws NullPointerException If array is empty
     */
    public static <T> T last(T[] array) {
        if (array.length < 1) throw new NullPointerException("Array is empty");
        return array[array.length - 1];
    }

    static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    /**
     * Source: http://www.java2s.com/Code/Java/Data-Type/Checksifacalendardateisaftertodayandwithinanumberofdaysinthefuture.htm
     * <p>Checks if the first calendar date is before the second calendar date ignoring time.</p>
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is before cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are <code>null</code>
     */
    static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return true;
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return false;
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true;
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false;
        return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
    }

    static String getTeacherName(String sht, boolean appendN) {
        if(TEACHERS.containsKey(sht.toUpperCase())) {
            if(appendN)
                return TEACHERS.get(sht.toUpperCase()).replace("Hr.", "Herrn").replace("Fr.", "Frau");
            else
                return TEACHERS.get(sht.toUpperCase()).replace("Hr.", "Herr").replace("Fr.", "Frau");
        } else {
            return sht;
        }
    }

    static LinkedHashMap<String, String> TEACHERS = new LinkedHashMap<String, String>() {{
        put("AMB", "Hr. Amberg");
        put("BAR", "Fr. Barnikol-Oettler");
        put("BAA", "Fr. Bauer");
        put("BAY", "Fr. Bayer");
        put("BEY", "Fr. Beyer");
        put("BÖA", "Fr. Böhlein");
        put("BUF", "Fr. Buff");
        put("BÜT", "Fr. Büttner");
        put("DGE", "Fr. Degner-Engelhardt");
        put("DEI", "Hr. Deibert");
        put("DIE", "Fr. Dietrich");
        put("EHA", "Fr. Ehrhardt");
        put("END", "Hr. End");
        put("ENG", "Fr. Engelbrecht");
        put("FEI", "Fr. Feick");
        put("FCH", "Fr. Fichtner");
        put("FRA", "Fr. Franke");
        put("FRB", "Fr. Friebe");
        put("FRD", "Fr. Friedel");
        put("FUC", "Hr. Fuchs");
        put("GÄR", "Fr. Gärtlein");
        put("GEB", "Fr. Gebhardt");
        put("GIE", "Fr. Giernoth");
        put("GLÄ", "Hr. Gläser");
        put("GÖH", "Fr. Göhring");
        put("GRE", "Fr. Greiner");
        put("GRH", "Fr. Greiner-Hiero");
        put("GRL", "Fr. Grell");
        put("HAM", "Hr. Hammerschmidt");
        put("HAR", "Hr. Hartwig");
        put("HAU", "Hr. Hausdörfer");
        put("HER", "Fr. Herold");
        put("HESS", "Fr. Heß");
        put("HEẞ", "Fr. Heß");
        put("HOC", "Fr. Hocevar");
        put("JAA", "Fr. Janusch");
        put("KEI", "Fr. Keiderling");
        put("KLSS", "Fr. Kloß");
        put("KLẞ", "Fr. Kloß");
        put("KOC", "Fr. Koch");
        put("KÖT", "Hr. Köthe");
        put("KRO", "Fr. Kropp");
        put("LEI", "Fr. Leipold");
        put("LEY", "Hr. Leyh");
        put("LIN", "Fr. Linß");
        put("LUT", "Fr. Luthardt");
        put("MAE", "Fr. Maier");
        put("MAG", "Hr. Maier");
        put("OBE", "Hr. Oberender");
        put("PAS", "Fr. Pasztori");
        put("PET", "Fr. Petzold");
        put("RAU", "Hr. Rauch");
        put("ROSS", "Fr. Roß");
        put("ROẞ", "Fr. Roß");
        put("RTB", "Hr. Roterberg");
        put("RUS", "Fr. Rust");
        put("SAB", "Fr. Sauerbrey");
        put("SAT", "Fr. Sauerteig");
        put("SLI", "Hr. Schliewe");
        put("SCN", "Hr. Schönau");
        put("SCB", "Fr. Schott");
        put("SCG", "Hr. Schott");
        put("SRÖ", "Fr. Schrön");
        put("SEA", "Fr. Seliger");
        put("SEL", "Hr. Seliger");
        put("SEM", "Fr. Sesselmann, M.");
        put("SEN", "Fr. Sesselmann, U.");
        put("STB", "Fr. Steiner");
        put("VOI", "Fr. Voigt");
        put("WAG", "Fr. Wagner");
        put("WAL", "Fr. Walther, E.");
        put("WAS", "Fr. Walther, S.");
        put("WEL", "Hr. Welsch");
        put("ZETC", "Fr. Zettler");
        put("ZETR", "Hr. Zettler");
    }};

    public interface AppTheme {
        String AUTO = "AUTO";
        String LIGHT = "LIGHT";
        String DARK = "DARK";
        String YELLOW = "YELLOW";
        static String getAutoTheme() {
            int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hr > 17 || hr < 7)
                return AppTheme.DARK;
            else
                return AppTheme.LIGHT;
        }
    }

    public interface AppThemeRes {
        int LIGHT = R.style.AppThemeLight;
        int DARK = R.style.AppThemeDark;
        int YELLOW = R.style.AppThemeYellow;
    }

    public interface PushTopics {
        String VERTRETUNG = "vertretung";
        String VERTRETUNG_DEBUG = "vertretung_debug";
        static String get(boolean isDebug) {
            return isDebug ? VERTRETUNG_DEBUG : VERTRETUNG;
        }
    }

    public interface NavFragments {
        int VERTRETUNGSPLAN = 1;
        int SPEISEPLAN = 2;
        int BESTELLUNG = 3;
        int AKTUELLES = 4;
        int TERMINE = 5;
        int KONTAKT = 6;
        int SETTINGS = 7;
        int ABOUT = 8;
        int KLAUSUREN = 9;
        static int getIdentifier(Fragment f) {
            if(f instanceof VPlanFragment)
                return VERTRETUNGSPLAN;
            else if(f instanceof SpeiseplanFragment)
                return SPEISEPLAN;
            else if(f instanceof EssenbestellungFragment)
                return BESTELLUNG;
            else if(f instanceof AktuellesFragment)
                return AKTUELLES;
            else if(f instanceof TermineFragment)
                return TERMINE;
            else if(f instanceof KontaktFragment)
                return KONTAKT;
            else if(f instanceof Settings2Fragment)
                return SETTINGS;
            else if(f instanceof AboutFragment)
                return ABOUT;
            else if(f instanceof KlausurenFragment)
                return KLAUSUREN;
            else if(f == null)
                return 0;
            else
                return VERTRETUNGSPLAN;
        }
    }

    public interface Preferences {
        String DEVICE_ID = "device-id";
        String HAS_REGISTERED = "fcm-has-registered";
        String PUSH_MODE = "pref_push";
        String FIRST_RUN3 = "first-run-v3";
        String KLASSE = "pref_klasse";
        String THEME = "pref_theme";
        String FERIEN_FETCHED = "ferien_fetched";
        String MARQUEE = "pref_marquee";
        String LEHRER = "pref_teacher";
        String IS_LEHRER = "pref_teacher_mode";
        String KLAUSUR_PLAN = "pref_klausur_selected";
        String KLAUSUR_TIP = "pref_klausur_tip";
        String LAST_VERSION = "pref_last_version";
    }

    public interface PushMode {
        String DISABLED = "DISABLED";
        String PRIVATE = "PRIVATE";
        String PUBLIC = "PUBLIC";
    }

    static Typeface getTKFont(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return ResourcesCompat.getFont(c, R.font.google_sans);
        else
            return Typeface.createFromAsset(c.getAssets(), "google_sans_regular.ttf");
    }

    static Typeface getTKFont(Context c, boolean bold) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return (bold ? ResourcesCompat.getFont(c, R.font.google_sans_bold) : ResourcesCompat.getFont(c, R.font.google_sans_regular));
        else
            return (bold ? Typeface.createFromAsset(c.getAssets(), "google_sans_bold.ttf") : Typeface.createFromAsset(c.getAssets(), "google_sans_regular.ttf"));
    }

    static Drawable getThemedDrawable(Context c, int resId, boolean isDark) {
        Drawable ic = c.getResources().getDrawable(resId);
        if(isDark)
            ic.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        return ic;
    }

    static Drawable getThemedDrawable(Context c, int resId, String applicationTheme) {
        Drawable ic = c.getResources().getDrawable(resId);
        if(applicationTheme.equals(Util.AppTheme.DARK))
            ic.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        return ic;
    }

    static int getFachColor(String fach) {
        Resources r = GSApp.getContext().getResources();
        switch (fach.toLowerCase()) {
            case "de": return r.getColor(R.color.de);
            case "ma": return r.getColor(R.color.ma);
            case "mu": return r.getColor(R.color.mu);
            case "ku": return r.getColor(R.color.ku);
            case "gg": return r.getColor(R.color.gg);
            case "re": return r.getColor(R.color.etre);
            case "et": return r.getColor(R.color.etre);
            case "mnt": return r.getColor(R.color.mnt);
            case "en": return r.getColor(R.color.en);
            case "sp": return r.getColor(R.color.sp);
            case "spj": return r.getColor(R.color.sp);
            case "spm": return r.getColor(R.color.sp);
            case "bi": return r.getColor(R.color.mnt);
            case "ch": return r.getColor(R.color.ch);
            case "ph": return r.getColor(R.color.ph);
            case "sk": return r.getColor(R.color.sk);
            case "if": return r.getColor(R.color.inf);
            case "wr": return r.getColor(R.color.wr);
            case "ge": return r.getColor(R.color.ge);
            case "ru": return r.getColor(R.color.frl);
            case "la": return r.getColor(R.color.frl);
            case "fr": return r.getColor(R.color.frl);
            case "sn": return r.getColor(R.color.frl);
            case "gewi": return r.getColor(R.color.sk);
            case "dg": return r.getColor(R.color.sk);
            default: return r.getColor(R.color.sp);
        }
    }

    static String LongName(String fach) {
        switch (fach.toLowerCase()) {
            case "de":
                return "Deutsch";
            case "ma":
                return "Mathe";
            case "mu":
                return "Musik";
            case "ku":
                return "Kunst";
            case "gg":
                return "Geografie";
            case "re":
                return "Religion";
            case "et":
                return "Ethik";
            case "mnt":
                return "MNT";
            case "en":
                return "Englisch";
            case "sp":
                return "Sport";
            case "spj":
                return "Sport Jungen";
            case "spm":
                return "Sport Mädchen";
            case "bi":
                return "Biologie";
            case "ch":
                return "Chemie";
            case "ph":
                return "Physik";
            case "sk":
                return "Sozialkunde";
            case "if":
                return "Informatik";
            case "wr":
                return "Wirtschaft/Recht";
            case "ge":
                return "Geschichte";
            case "fr":
                return "Französisch";
            case "ru":
                return "Russisch";
            case "la":
                return "Latein";
            case "gewi":
                return "Gesellschaftsw.";
            case "dg":
                return "Darstellen/Gestalten";
            case "sn":
                return "Spanisch";
            case "&nbsp;":
                return "keine Angabe";
            default:
                return fach;

        }
    }


    static boolean isFiltered(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(Preferences.IS_LEHRER, false) ? PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.LEHRER, "").length() > 2 : PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.KLASSE, "").length() > 2;
    }

    static boolean isLehrerModus(Context c) { //Wahr, wenn Lehrermodus aktiviert ist und eingegebener Lehrer mehr als 2 Zeichen enthält, sonst falsch
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(Preferences.IS_LEHRER, false) && PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.LEHRER, "").length() > 2;
    }

    static String getLehrer(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.LEHRER, "");
    }

    static String getKlasse(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.KLASSE, "");
    }

    static boolean isNumeric(@Nullable String str)
    {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    static boolean hasInternet(Context _context){
        if(_context == null) return false;
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    static String getVersion(Context context) {
        return context.getResources().getString(R.string.version);
    }

    static boolean applyFilter(Context c, String[] dataSet) {
        Timber.d("applyFilter start");
        if(isLehrerModus(c))
            return dataSet[3].toLowerCase().contains(PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.LEHRER, "").toLowerCase());

        String Filter = PreferenceManager.getDefaultSharedPreferences(c).getString(Preferences.KLASSE, "");

        Matcher matcher = Pattern.compile("\\d+").matcher(dataSet[0]);
        matcher.find();
        String skl = Integer.valueOf(matcher.group()).toString();
        Timber.d( "SKL is " + skl + ", data0 is " + dataSet[0]);
        //String skl = data[0].split(".")[0];
        String SUCL = dataSet[0].replace("/2", " " + skl + ".2");
        SUCL = SUCL.replace("/3", " " + skl + ".3");
        SUCL = SUCL.replace("/4", " " + skl + ".4");
        SUCL = SUCL.replace("/5", " " + skl + ".5");

        if(Filter.matches("(^[0-9]{1}\\.[0-9])+")) {
            //9.2, 8.1 etc
            if (SUCL.length() == 1) {
                return Filter.startsWith(SUCL);
            } else {
                return SUCL.contains(Filter);
            }
        } else if(Filter.matches("([0-9]{2}\\.[0-9])+")) {
            if (SUCL.length() == 2) {
                return Filter.startsWith(SUCL);
            } else {
                return SUCL.contains(Filter);
            }
            //10.2 10.3 etc
        } else if(Filter.matches("([A-Z][0-9]{2})+")) {
            //A18, A19 etc
            if(dataSet[0].matches("([A-Z][0-9]{2})+")) {
                return dataSet[0].endsWith(Filter.replaceAll("[^\\d.]", ""));
            } else {
                return dataSet[0].startsWith(Filter.replaceAll("[^\\d.]", ""));
            }
        } else {
            Timber.d("Komischer filter * %s *", Filter);
        }
        return false;
    }

    static void prepareMenu(Menu menu, int fragId) {
        switch (fragId) {
            case NavFragments.BESTELLUNG:
                menu.findItem(R.id.eb_abmelden).setVisible(true);
                menu.findItem(R.id.eb_account).setVisible(true);
                menu.findItem(R.id.eb_bestellen).setVisible(true);
                menu.findItem(R.id.eb_gotoo).setVisible(true);
                menu.findItem(R.id.eb_plan).setVisible(true);
                menu.findItem(R.id.eb_refresh).setVisible(true);
                menu.findItem(R.id.eb_startseite).setVisible(true);
                menu.findItem(R.id.web_refresh).setVisible(false);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(false);
                menu.findItem(R.id.br_fwd).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                break;
            case NavFragments.TERMINE:
                menu.findItem(R.id.eb_abmelden).setVisible(false);
                menu.findItem(R.id.eb_account).setVisible(false);
                menu.findItem(R.id.eb_bestellen).setVisible(false);
                menu.findItem(R.id.eb_gotoo).setVisible(false);
                menu.findItem(R.id.eb_plan).setVisible(false);
                menu.findItem(R.id.eb_refresh).setVisible(false);
                menu.findItem(R.id.eb_startseite).setVisible(false);
                menu.findItem(R.id.web_refresh).setVisible(true);
                menu.findItem(R.id.web_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(false);
                menu.findItem(R.id.br_fwd).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                break;
            case NavFragments.AKTUELLES:
                menu.findItem(R.id.eb_abmelden).setVisible(false);
                menu.findItem(R.id.eb_account).setVisible(false);
                menu.findItem(R.id.eb_bestellen).setVisible(false);
                menu.findItem(R.id.eb_gotoo).setVisible(false);
                menu.findItem(R.id.eb_plan).setVisible(false);
                menu.findItem(R.id.eb_refresh).setVisible(false);
                menu.findItem(R.id.eb_startseite).setVisible(false);
                menu.findItem(R.id.web_refresh).setVisible(false);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(true);
                menu.findItem(R.id.br_fwd).setVisible(true);
                menu.findItem(R.id.br_back).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.br_fwd).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                break;
            case NavFragments.KONTAKT: //TODO: Entfernen
                menu.findItem(R.id.eb_abmelden).setVisible(false);
                menu.findItem(R.id.eb_account).setVisible(false);
                menu.findItem(R.id.eb_bestellen).setVisible(false);
                menu.findItem(R.id.eb_gotoo).setVisible(false);
                menu.findItem(R.id.eb_plan).setVisible(false);
                menu.findItem(R.id.eb_refresh).setVisible(false);
                menu.findItem(R.id.eb_startseite).setVisible(false);
                menu.findItem(R.id.web_refresh).setVisible(false);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(false);
                menu.findItem(R.id.br_fwd).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                break;
            case NavFragments.KLAUSUREN:
                menu.findItem(R.id.eb_abmelden).setVisible(false);
                menu.findItem(R.id.eb_account).setVisible(false);
                menu.findItem(R.id.eb_bestellen).setVisible(false);
                menu.findItem(R.id.eb_gotoo).setVisible(false);
                menu.findItem(R.id.eb_plan).setVisible(false);
                menu.findItem(R.id.eb_refresh).setVisible(false);
                menu.findItem(R.id.eb_startseite).setVisible(false);
                menu.findItem(R.id.web_refresh).setVisible(false);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(false);
                menu.findItem(R.id.br_fwd).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                break;
            case NavFragments.VERTRETUNGSPLAN:
                menu.findItem(R.id.eb_abmelden).setVisible(false);
                menu.findItem(R.id.eb_account).setVisible(false);
                menu.findItem(R.id.eb_bestellen).setVisible(false);
                menu.findItem(R.id.eb_gotoo).setVisible(false);
                menu.findItem(R.id.eb_plan).setVisible(false);
                menu.findItem(R.id.eb_refresh).setVisible(false);
                menu.findItem(R.id.eb_startseite).setVisible(false);
                menu.findItem(R.id.web_refresh).setVisible(false);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(false);
                menu.findItem(R.id.br_fwd).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                //menu.findItem(R.id.show_all).setVisible(true);
                //menu.findItem(R.id.show_all).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                break;
            default:
                menu.findItem(R.id.eb_abmelden).setVisible(false);
                menu.findItem(R.id.eb_account).setVisible(false);
                menu.findItem(R.id.eb_bestellen).setVisible(false);
                menu.findItem(R.id.eb_gotoo).setVisible(false);
                menu.findItem(R.id.eb_plan).setVisible(false);
                menu.findItem(R.id.eb_refresh).setVisible(false);
                menu.findItem(R.id.eb_startseite).setVisible(false);
                menu.findItem(R.id.web_refresh).setVisible(false);
                menu.findItem(R.id.main_about).setVisible(false);
                menu.findItem(R.id.main_settings).setVisible(false);
                menu.findItem(R.id.br_back).setVisible(false);
                menu.findItem(R.id.br_fwd).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(false);
                menu.findItem(R.id.action_klausur_toggle).setVisible(false);
                break;
        }
    }


}

class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;

    public TextDrawable(String text) {

        this.text = text;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(22f);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setShadowLayer(6f, 0, 0, Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawText(text, 0, 0, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


}

class SpMenu {
    int id;
    private String Montag;
    private String Dienstag;
    private String Mittwoch;
    private String Donnerstag;
    private String Freitag;
    private String KW;
    SpMenu(int idn) {
        id = idn;
    }

    void setKW(String value) { KW = value; }

    void setMeals(String[] meals) {
        Montag = meals[0];
        Dienstag = meals[1];
        Mittwoch = meals[2];
        Donnerstag = meals[3];
        Freitag = meals[4];
    }

    void setMeals(List<String> meals) {
        Montag = meals.get(0);
        Dienstag = meals.get(1);
        Mittwoch = meals.get(2);
        Donnerstag = meals.get(3);
        Freitag = meals.get(4);
    }

    void setMeal(String meal, int day) {
        switch (day) {
            case 0:
                Montag = meal;
                break;
            case 1:
                Dienstag = meal;
                break;
            case 2:
                Mittwoch = meal;
                break;
            case 3:
                Donnerstag = meal;
                break;
            case 4:
                Freitag = meal;
                break;
        }
    }

    void setMontag(String value) { Montag = value; }
    void setDienstag(String value) { Dienstag = value; }
    void setMittwoch(String value) { Mittwoch = value; }
    void setDonnerstag(String value) { Donnerstag = value; }
    void setFreitag(String value) { Freitag = value; }

    String getToday(int day) {
        String Meal;
        switch (day) {
            case Calendar.MONDAY:
                Meal = Montag;
                break;
            case Calendar.TUESDAY:
                Meal = Dienstag;
                break;
            case Calendar.WEDNESDAY:
                Meal = Mittwoch;
                break;
            case Calendar.THURSDAY:
                Meal = Donnerstag;
                break;
            case Calendar.FRIDAY:
                Meal = Freitag;
                break;
            default:
                Meal = "SNERROR" + day;
                break;
        }

        return Meal;
    }

    JSONObject toSaveJSON() throws JSONException {
        JSONObject jk = new JSONObject();
        jk.put("Montag", Montag);
        jk.put("Dienstag", Dienstag);
        jk.put("Mittwoch", Mittwoch);
        jk.put("Donnerstag", Donnerstag);
        jk.put("Freitag", Freitag);
        jk.put("KW", KW);
        jk.put("id", id);
        return jk;
    }

    void fromSaveJSON(JSONObject input, String KW) throws JSONException {
        JSONObject jo = input;
        Montag = jo.getString("Montag");
        Dienstag = jo.getString("Dienstag");
        Mittwoch = jo.getString("Mittwoch");
        Donnerstag = jo.getString("Donnerstag");
        Freitag = jo.getString("Freitag");
        KW = jo.getString("KW");
        id = jo.getInt("id");
    }
}

/**
 * Datentyp für einen Eintrag im Vertretungsplan
 */
class Eintrag implements Serializable  {
    private String Klasse;
    private String Stunde;
    private String Fachnormal;
    private String Vertretung;
    private String Raum;
    private String Fachvertret;
    private String Bemerkung;
    private boolean neu;

    /**
     * Ein Eintrag im Vertretungsplan (klassisch, ohne neue Vertretung)
     * @param klasse Klasse, die Vertretung hat
     * @param stunde Stunde, die Vertreten wird
     * @param fachnormal Fach, welches normalerweise stattfinden würde
     * @param vertretung Lehrer, der vertritt
     * @param raum Raum, in dem vertreten wird
     * @param fachvertret Fach, das vertreten wird
     * @param bemerkung Bemerkung
     */
    Eintrag(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {
        this.Klasse = klasse;
        this.Stunde = stunde;
        this.Fachnormal = fachnormal;
        this.Vertretung = vertretung;
        this.Raum = raum;
        this.Fachvertret = fachvertret;
        this.Bemerkung = bemerkung;
        this.neu = false;
    }

    /**
     * Ein Eintrag im Vertretungsplan
     * @param klasse Klasse, die Vertretung hat
     * @param stunde Stunde, die Vertreten wird
     * @param fachnormal Fach, welches normalerweise stattfinden würde
     * @param vertretung Lehrer, der vertritt
     * @param raum Raum, in dem vertreten wird
     * @param fachvertret Fach, das vertreten wird
     * @param bemerkung Bemerkung
     * @param isNeu Ist Vertretung neu
     */
    Eintrag(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung, boolean isNeu) {
        this.Klasse = klasse;
        this.Stunde = stunde;
        this.Fachnormal = fachnormal;
        this.Vertretung = vertretung;
        this.Raum = raum;
        this.Fachvertret = fachvertret;
        this.Bemerkung = bemerkung;
        this.neu = isNeu;
    }

    /**
     * Ein Eintrag im Vertretungsplan (Aus JSON-Cache)
     * @param input JSON-Daten
     */
    Eintrag(JSONObject input) throws Exception {
        this.Klasse = input.getString("Klasse");
        this.Stunde = input.getString("Stunde");
        this.Fachnormal = input.getString("Fachnormal");
        this.Vertretung = input.getString("Vertretung");
        this.Raum = input.getString("Raum");
        this.Fachvertret = input.getString("Fachvertret");
        this.Bemerkung = input.getString("Bemerkung");

        if (input.has("Neu"))
            this.neu = input.getBoolean("Neu");
        else
            this.neu = false;
    }

    /**
     * Gibt die Klasse einer Vertretung zurück
     * @return Klasse einer Vertretung
     */
    String getKlasse() {
        try {
            return this.Klasse.trim();
        } catch(Exception e) {
            return this.Klasse;
        }

    }

    /**
     * Gibt die Stunde einer Vertretung zurück
     * @return Stunde einer Vertretung
     */
    String getStunde() {
        try {
            return this.Stunde.trim();
        } catch(Exception e) {
            return this.Stunde;
        }

    }

    /**
     * Gibt das eigentliche Fach zurück
     * @return Eigentliches Fach
     */
    String getFachNormal() {
        try {
            return this.Fachnormal.trim();
        } catch(Exception e) {
            return this.Fachnormal;
        }
    }


    /**
     * Gibt den Vertretungslehrer zurück
     * @return Vertretungslehrer oder "niemandem"
     */
    String getVertretung() {
        if ((!this.Vertretung.matches(".*[a-zA-Z]+.*")) || this.Vertretung.equals("&nbsp;")) { //Enthält keine Buchstaben oder ist &nbsp;
            return "niemandem";
        } else {
            try {
                return this.Vertretung.trim();
            } catch(Exception e) {
                return this.Vertretung;
            }
        }
    }

    /**
     * Gibt den Raum einer Vertretung zurück
     * @return Raum
     */
    String getRaum() {
        if ((!this.Raum.matches(".*[a-zA-Z]+.*")) || this.Raum.equals("&nbsp;")) { //Keine Angabe
            return "k.A.";
        } else {
            try {
                return this.Raum.trim();
            } catch(Exception e) {
                return this.Raum;
            }
        }
    }


    /**
     * Gibt das Fach zurück, das vertreten wird
     * @return Vertretenes Fach
     */
    String getFachVertretung() {
        if (!(this.Fachvertret.matches(".*[a-zA-Z]+.*") || this.Vertretung.equals("&nbsp;"))) {
            return "nichts";
        } else {
            try {
                return this.Fachvertret.trim();
            } catch(Exception e) {
                return this.Fachvertret;
            }
        }
    }


    /**
     * Gibt die Bemerkung oder einen leeren String zurück
     * @return Bemerkung
     */
    String getBemerkung() {
        if (!this.Bemerkung.matches(".*[a-zA-Z]+.*")) {
            return "";
        } else {
            return this.Bemerkung;
        }
    }

    /**
     * Gibt die Bemerkung für Vertretungsplan-Karten zurück (nichts oder 2 Leerzeilen + Bemerkung)
     * @return Bemerkung
     */
    String getBemerkungForCard() {
        if (!this.Bemerkung.matches(".*[a-zA-Z]+.*")) {
            return "";
        } else {
            return "\n\n" + this.Bemerkung;
        }
    }

    /**
     * Gibt zurück, ob die Vertretung neu ist
     * @return Ist Vertretung neu
     */
    boolean getNeu() {
        return neu;
    }
}

/**
 * Datentyp für mehrere Einträge im Vertretungsplan
 * (ArrayList)
 */
class Eintrage extends ArrayList<Eintrag> {

    /**
     * Gibt die Vertretungen einer Klasse zurück
     * @param klasse Klasse
     * @param reverse Reihenfolge umkehren (für Einzelansicht im Vertretungsplan)
     * @return Liste an Einträgen
     * @throws KeineEintrageException Klasse hat keine Vertretungen
     */
    ArrayList<Eintrag> getKlasseGruppe(String klasse, Boolean reverse) throws KeineEintrageException {
        ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
        for (Eintrag single : this) {
            if (single.getKlasse().equals(klasse)) {
                outp.add(single);
            }
        }

        if (outp.size() < 1) {
            throw new KeineEintrageException();
        } else {
            if (reverse) {
                Collections.reverse(outp);
            }
            return outp;
        }
    }

    /**
     * Gibt Vertretungen pro Stunde zurück
     * @param stunde Stunde
     * @param reverse Reihenfolge umkehren
     * @return Liste an Vertretungen
     * @throws KeineEintrageException Keine Vertretungen in dieser Stunde
     */
    ArrayList<Eintrag> getStundeGruppe(String stunde, Boolean reverse) throws KeineEintrageException {
        ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
        for (Eintrag single : this) {
            if (single.getStunde().equals(stunde)) {
                outp.add(single);
            }
        }

        if (outp.size() < 1) {
            throw new KeineEintrageException();
        } else {
            if (reverse) {
                Collections.reverse(outp);
            }
            return outp;
        }
    }

    /**
     * Gibt alle Klassen im Vertretungsplan zurück
     * @return Klassen im Plan
     * @throws KeineKlassenException Plan enthält keine Klassen
     */
    ArrayList<String> getKlassen() throws KeineKlassenException {
        ArrayList<String> outp = new ArrayList<String>();
        for (Eintrag single : this) {
            if (!outp.contains(single.getKlasse()))
                outp.add(single.getKlasse());
        }

        if (outp.isEmpty())
            throw new KeineKlassenException();

        return outp;
    }


    /**
     * Gibt Stunden zurück in denen vertreten wird
     * @return Stunden
     * @throws KeineKlassenException
     */
    ArrayList<String> getStunden() throws KeineKlassenException { //TODO: Neu schreiben mit weniger sinnlosen Zählschleifen
        ArrayList<String> outp = new ArrayList<String>();
        for (Eintrag single : this) {
            if (!outp.contains(single.getStunde()))
                outp.add(single.getStunde());
        }

        if (outp.isEmpty())
            throw new KeineKlassenException();

        Collections.sort(outp, String::compareToIgnoreCase);

        return outp;
    }
}

/**
 * Eigene Exception wenn keine Vertretungen existieren
 */
class KeineEintrageException extends Exception { }

/**
 * Eigene Exception wenn keine Klassen existieren
 */
class KeineKlassenException extends Exception { }

class FileLogTree extends Timber.DebugTree {
    File log;

    FileLogTree(Context context) {
        log = new File(context.getFilesDir(), "/timber.log");
    }

    @Override protected void log(int priority, String tag, String message, Throwable t) {
        if(log == null)
            return;
        if (!log.exists())
        {
            try
            {
                log.createNewFile();
                BufferedWriter buf = new BufferedWriter(new FileWriter(log, true));
                buf.append("### GSApp Log ###");
                buf.newLine();
                buf.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String TYPE = "DEBUG";
        switch(priority) {
            case Log.DEBUG:
                TYPE = "DEBUG";
                break;
            case Log.ERROR:
                TYPE = "ERROR";
                break;
            case Log.WARN:
                TYPE = "WARN";
                break;
            case Log.INFO:
                TYPE = "INFO";
                break;
            case Log.VERBOSE:
                TYPE = "VERB";
                break;
            default:
                TYPE = "DEBUG";
                break;
        }

        try
        {
            //BufferedWriter for performance, true to set append to file flag
            SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            BufferedWriter buf = new BufferedWriter(new FileWriter(log, true));
            buf.append("[" + s.format(new Date()) + "] [" + TYPE + "] [" + tag + "]: " + message);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}