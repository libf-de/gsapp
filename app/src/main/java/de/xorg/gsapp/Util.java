package de.xorg.gsapp;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class Util {

    public static final int PERMISSION_DEBUG = 2226;
    public static final int PERMISSION_CALL = 2202;

    public static final int FIRSTRUN_ACTIVITY = 392;

    public static final String NTF_CHANNEL_ID = "gsapp_notifications";

    public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
    public final static String EXTRA_NAME = "de.xorg.gsapp.MESSAGENAME";

    public static String cDeutsch = "#3f51b5";
    public static String cMathe = "#f44336";
    public static String cMusik = "#9e9e9e";
    public static String cKunst = "#673ab7";
    public static String cGeografie = "#9e9d24";
    public static String cReligion = "#ff8f00";
    public static String cEthik = "#ff8f00";
    public static String cMNT = "#4caf50";
    public static String cEnglisch = "#ff9800";
    public static String cSport = "#607d8b";
    public static String cBiologie = "#4caf50";
    public static String cChemie = "#e91e63";
    public static String cPhysik = "#009688";
    public static String cSozialkunde = "#795548";
    public static String cInformatik = "#03a9f4";
    public static String cWirtschaftRecht = "#ff5722";
    public static String cGeschichte = "#9c27b0";
    public static String cFRL = "#558b2f";


    /*public static String getLastDate(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString("readDate", "ERR");
    }*/

    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    /**
     * Source: http://www.java2s.com/Code/Java/Data-Type/Checksifacalendardateisaftertodayandwithinanumberofdaysinthefuture.htm
     */

    /**
     * <p>Checks if the first calendar date is before the second calendar date ignoring time.</p>
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is before cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are <code>null</code>
     */
    public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return true;
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return false;
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true;
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false;
        return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
    }

    /* Returns true if the provided reference is null otherwise returns false.*/

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static String getTeacherName(String sht) {
        final String HR = "Herrn ";
        final String FR = "Frau ";
        switch(sht) {
            case "BÖA":
                return FR + "Böhlein";
            case "AMB":
                return HR + "Amberg";
            case "JAA":
                return "???JAA???";
            case "GLÄ":
                return "???GLÄ???";
            case "BAR":
                return "???BAR???";
            case "LUT":
                return "???LUT???";
            case "RUS":
                return FR + "Rust";
            case "WAL":
                return "???WAL???";
            case "ZETC":
                return FR + "Zettler";
            case "ROß":
                return FR + "Roß";
            default:
                return sht;
        }
    }

    public static interface AppTheme {
        public static final String AUTO = "AUTO";
        public static final String LIGHT = "LIGHT";
        public static final String DARK = "DARK";
        public static final String YELLOW = "YELLOW";
        public static String getAutoTheme() {
            int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hr > 17 || hr < 7)
                return AppTheme.DARK;
            else
                return AppTheme.LIGHT;
        }
    }

    public static interface AppThemeRes {
        public static final int LIGHT = R.style.AppThemeLight;
        public static final int DARK = R.style.AppThemeDark;
        public static final int YELLOW = R.style.AppThemeYellow;
    }



    public static interface NavFragments {
        public static final int VERTRETUNGSPLAN = 1;
        public static final int SPEISEPLAN = 2;
        public static final int BESTELLUNG = 3;
        public static final int AKTUELLES = 4;
        public static final int TERMINE = 5;
        public static final int KONTAKT = 6;
        public static final int SETTINGS = 7;
        public static final int ABOUT = 8;
        public static final int KLAUSUREN = 9;
    }

    public static interface Preferences {
        public static final String DEVICE_ID = "device-id";
        public static final String HAS_REGISTERED = "fcm-has-registered";
        public static final String PUSH_MODE = "pref_push";
        public static final String FIRST_RUN2 = "first-run-v2";
        public static final String KLASSE = "klasse";
    }

    public static interface PushMode {
        public static final String DISABLED = "DISABLED";
        public static final String PRIVATE = "PRIVATE";
        public static final String PUBLIC = "PUBLIC";
    }

    public static int getAppVersion(Context c) {
        int vers = -1;
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            vers = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return vers;
    }

    public static String dataLoader(Activity ct) {
        final StringBuilder RITT = new StringBuilder();

        RITT.append("--> DEBUG <--\n");
        RITT.append("\n");
        RITT.append("--> Build\n");
        RITT.append("BUILD.DEBUG=" + String.valueOf(BuildConfig.DEBUG) + "\n");
        RITT.append("BUILD.VERSIONCODE=" + Util.getVersionID(ct) + "\n");
        RITT.append("BUILD.VERSIONNAME=" + Util.getVersionCode(ct) + "\n");
        RITT.append("BUILD.VERSION=" + Util.getVersion(ct).replace(" ", "_") + "\n");
        RITT.append("BUILD.BUILD=" + ct.getString(R.string.build) + "\n");
        RITT.append("BUILD.DEBUG=" + String.valueOf(BuildConfig.DEBUG) + "\n");
        RITT.append("\n");
        RITT.append("--> Device\n");

        // Bildschirmgroesse
        DisplayMetrics dm = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / (double) dens;
        double hi = (double) height / (double) dens;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double si = Math.sqrt(x + y);
        si = Math.round(si * 100);
        si = si / 100;
        String BILDSCHIRMGROESSE = String.valueOf(si);

        // DPI
        DisplayMetrics metrics = ct.getResources().getDisplayMetrics();
        int densityDpi = (int) (metrics.density * 160f);
        String DPI = String.valueOf(densityDpi);

        // Bildschirmaufloesung
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int heightA = displaymetrics.heightPixels;
        int widthA = displaymetrics.widthPixels;
        String BILDSCHIRMAUFLOESUNG = String.valueOf(widthA) + "x" + String.valueOf(heightA);

        // Android-Version
        String ANDROIDVERSION = Build.VERSION.RELEASE;
        String ANDROIDSDK = String.valueOf(Build.VERSION.SDK_INT);

        // Handydaten
        String HERSTELLER = Build.MANUFACTURER;
        String HANDYTYP = getDeviceName();

        // Einmalige ID-Nummer
        String PI = PreferenceManager.getDefaultSharedPreferences(ct).getString("id", "X0X0X0X0X");

        Boolean asyncb = PreferenceManager.getDefaultSharedPreferences(ct).getBoolean("loadAsync", false);
        String ASYNC = "";
        if (asyncb) {
            ASYNC = "TRUE";
        } else {
            ASYNC = "FALSE";
        }

        RITT.append("DEVICE.SCREEN.SIZE=" + BILDSCHIRMGROESSE + "\n");
        RITT.append("DEVICE.SCREEN.DPI=" + DPI + "\n");
        RITT.append("DEVICE.SCREEN.RESOLUTION=" + BILDSCHIRMAUFLOESUNG + "\n");
        RITT.append("DEVICE.ANDROID.VERSION=" + ANDROIDVERSION + "\n");
        RITT.append("DEVICE.ANDROID.SDK=" + ANDROIDSDK + "\n");
        RITT.append("DEVICE.MANUFACTURER=" + HERSTELLER + "\n");
        RITT.append("DEVICE.DESCRIPTOR=" + HANDYTYP + "\n");
        RITT.append("DEVICE.IDENTIFIER=" + PI + "\n");
        RITT.append("DEVICE.LOADSASYNC=" + ASYNC + "\n");

        return RITT.toString();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void initNotifications(Context c) { //TODO: Eigenes Schema
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = "GSApp Vertretungsplan";
        // The user-visible description of the channel.
        String description = "Benachrichtigungen bei neuem Vertretungsplan";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(NTF_CHANNEL_ID, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public static int zähleVorkommen(String strings, String suchwort) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = strings.indexOf(suchwort, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += suchwort.length();
            }
        }

        return count;
    }

    public static String getFachColor(String fach) {
        switch (fach.toLowerCase()) {
            case "de":
                return cDeutsch;
            case "ma":
                return cMathe;
            case "mu":
                return cMusik;
            case "ku":
                return cKunst;
            case "gg":
                return cGeografie;
            case "re":
                return cReligion;
            case "et":
                return cEthik;
            case "mnt":
                return cMNT;
            case "en":
                return cEnglisch;
            case "sp":
                return cSport;
            case "spj":
                return cSport;
            case "spm":
                return cSport;
            case "bi":
                return cBiologie;
            case "ch":
                return cChemie;
            case "ph":
                return cPhysik;
            case "sk":
                return cSozialkunde;
            case "if":
                return cInformatik;
            case "wr":
                return cWirtschaftRecht;
            case "ge":
                return cGeschichte;
            case "ru":
                return cFRL;
            case "la":
                return cFRL;
            case "fr":
                return cFRL;
            case "sn":
                return cFRL;
            case "gewi":
                return cSozialkunde;
            case "dg":
                return cSozialkunde;
            default:
                return cSport;
        }
    }

    public static String LongName(String fach) {
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


    public static String bolToStr(boolean value) {
        if(value) {
            return "TRUE";
        } else {
            return "FALSE";
        }
    }

    public static boolean StrToBol(String value) {
        if(value.toLowerCase().equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static int getConfigVersion(Context c) {
        return Integer.parseInt(c.getString(R.string.ConfigVer));
    }

    public static String getUserAgentString(Context c, boolean isSync) {
        String MORE;
        if(isSync) {
            MORE = "syncLoad";
        } else {
            MORE = "asyncLoad";
        }
        return "GSApp " + getVersion(c) + " on " + getDeviceName() + " (Android " + Build.VERSION.RELEASE.toString() + ") " + MORE;
    }

    public static boolean hasInternet(Context _context){
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }
 
    public static String getVersionID(Context context){
        String ID = context.getString(R.string.version);
        String[] UID = ID.split(" ");
        return UID[0];
    }

    public static String getVersion(Context context) {
        String VER = context.getString(R.string.version);
        return VER;
        //String[] VRS = VER.split(" ");
        //if(BuildConfig.DEBUG && !VER.startsWith("The")) {
        //    return VRS[0] + "D " + VRS[1];
        //} else {
        //    return VER;
        //}
    }
    
    public static int getVersionCode(Context context) {
    	PackageInfo pinfo;
		try {
			pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pinfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
    }
    
    public static String getReleaseName(Context context) {
    	String ID = context.getString(R.string.version);
        String[] UID = ID.split(" ");
        char gf = (char) 34;
        String GFF = String.valueOf(gf);
        
        String CODENAME = ID.replace(UID[0], "").replace(GFF, "");
        
        return CODENAME;
    }

    public static void setThemeUI(Activity a) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = a.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(a.getResources().getColor(R.color.gsgelbdark));
            window.setNavigationBarColor(a.getResources().getColor(R.color.gsgelbdark));
        }
        switch (PreferenceManager.getDefaultSharedPreferences(a).getString("themeMode", "android")) {
            case "android":
                if(hasSoftNavigation(a)) {
                    a.setTheme(R.style.AppTheme);
                } else {
                    a.setTheme(R.style.PAppThemeLight); //TODO: In neues Themen-System einbauen?
                }
                break;
            default:
                Toast.makeText(a, "WARNUNG: UNGÜLTIGE ENTWICKEREINSTELLUNG UITHEMEMODE!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("NewApi")
    public static boolean hasSoftNavigation(Context context)
    {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                return !ViewConfiguration.get(context).hasPermanentMenuKey();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static String strArrayToString(String[] strings) {
        StringBuilder sb = new StringBuilder();
        Timber.d("len: " + strings.length);
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]).append(";");
        }
        return sb.toString();
    }

    public static String[] strToArrayString(String strings) {
        return strings.split(";");
    }

    public static boolean applyFilter(Context c, String[] dataSet) {
        String Filter = PreferenceManager.getDefaultSharedPreferences(c).getString("klasse", "");
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
                if (Filter.startsWith(SUCL)) {
                    return true;
                }
            } else {
                if (SUCL.contains(Filter)) {
                    return true;
                }
            }
        } else if(Filter.matches("([0-9]{2}\\.[0-9])+")) {
            if (SUCL.length() == 2) {
                if (Filter.startsWith(SUCL)) {
                    return true;
                }
            } else {
                if (SUCL.contains(Filter)) {
                    return true;
                }
            }
            //10.2 10.3 etc
        } else if(Filter.matches("([A-Z][0-9]{2})+")) {
            //A18, A19 etc
            if(dataSet[0].matches("([A-Z][0-9]{2})+")) {
                if(dataSet[0].endsWith(Filter.replaceAll("[^\\d.]", ""))) {
                    return true;
                }
            } else {
                if(dataSet[0].startsWith(Filter.replaceAll("[^\\d.]", ""))) {
                    return true;
                }
            }
        }



        return false;
    }

    public static Elements getVertretungenElements(Document doc) {
        Elements vpEnts = doc.select("tr[id=Svertretungen], tr[id=Svertretungen] ~ tr");
        if(vpEnts.size() < 1) {
            Timber.d( "Vertretungsplan: Keine Eintraege gefunden, versuche Behelfsmethode...");
            Element par = doc.select("td[class*=vpTextZentriert]").first().parent();
            vpEnts = doc.select(par.cssSelector() + ", " + par.cssSelector() + " ~ tr");
        }
        return vpEnts;
    }

    public static String DoubleToString(Double input) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(12); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

        return df.format(input);
    }

    public static void setOrientation(Activity a) {
        switch (PreferenceManager.getDefaultSharedPreferences(a).getInt("rotateMode", 1)) {
            case 3:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
                break;
            case 10:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                break;
            case 13:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                break;
            case 0:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 14:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                break;
            case 5:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                break;
            case 1:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 8:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case 9:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case 4:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            case 6:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case 7:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case -1:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case 2:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
            case 11:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                break;
            case 12:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                break;
            default:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(a, "WARNUNG: UNGÜLTIGE ENTWICKEREINSTELLUNG UIROTATEMODEGLOBAL!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static void prepareMenu(Menu menu, int fragId) {
        switch (fragId) {
            case R.id.nav_essenbest:
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
            case R.id.nav_termine:
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
            case R.id.nav_aktuelles:
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
            case R.id.nav_kontakt:
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
            case R.id.nav_klausuren:
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
            case R.id.nav_vplan:
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
    public void draw(Canvas canvas) {
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

/*class GetDate2 extends AsyncTask<String, Void, String> {
    Context cont;
    GetDate2(Context ct){
        this.cont = ct;
    }

    protected String doInBackground(String... message) {
        HttpClient httpclient;
        HttpGet request;
        HttpResponse response = null;
        String result = "";

        // Verbindung zum Server mit der "Apache HttpClient Library" aufbauen
        try {
            httpclient = new DefaultHttpClient();
            //request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
            request = new HttpGet("https://xorg.ga/vpt.html");
            response = httpclient.execute(request);
        }

        catch (Exception e) {
            // Code um Fehler zu behandeln
            result = "error";
        }

        // Serverantwort auswerten
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {

                // Serverantwort auslesen
                result = result + line ;
            }
        } catch (Exception e) {
            // Code um Fehler zu behandeln
            result = "error";
        }
        return result;
    }

    protected void onPostExecute(String result) {
        try {
            //Serverantwort (Datum des verfügbarem Vertretungsplan)
            char gf = (char) 34;
            GALog loc = new GALog(cont);
            Double revision = 0.0;
            if(result != "E") {
                String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
                String gDate = gPart.replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "")/*.split(".");
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat RC = new SimpleDateFormat("yyyyMMdd");
                Date dt = df.parse(gDate.replaceAll("[^\\d.]", "").trim());
                String GRC = RC.format(dt);

                revision = Util.zähleVorkommen(gDate.toLowerCase(), "neu") * 0.1;

                loc.debug("IntView/GetDate2: Datum von Server erhalten: +" + (Float.parseFloat(GRC)  + revision.floatValue()) + "+");
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cont).edit();
                editor.putFloat("lastDate", (Float.parseFloat(GRC)  + revision.floatValue()));
                editor.putString("readDate", GRC);
                editor.commit();
            }
        } catch(Exception ex) {
            //Allgemeiner Fehler beim Auswerten
            new GALog(cont).error("IntView/GetDate2: Fehler beim Auswerten: " + ex.getMessage());
        }
    }

}*/

class GetDate extends AsyncTask<String, Void, String> {
    Context cont;

    GetDate(Context ct) {
        this.cont = ct;
    }

    protected String doInBackground(String... message) {
        HttpClient httpclient;
        HttpGet request;
        HttpResponse response = null;
        String result = "";

        try {
            httpclient = new DefaultHttpClient();
            request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.php5");
            response = httpclient.execute(request);
        } catch (Exception e) {
            result = "error";
        }

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result = result + line + "\n";
            }
        } catch (Exception e) {
            result = "error";
        }
        return result;
    }


    protected void onPostExecute(String result) {
        try {
            if (result.equals("error")) {
                Timber.d("CheckService: RESULT ERROR");
                return;
            }
            Document doc = Jsoup.parse(result); //td.rundeEckenOben.vpUeberschr
            String gDate = doc.select("td[class*=vpUeberschr]").first().text().replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "");
            String serverDate = new SimpleDateFormat("yyyyMMdd").format(new SimpleDateFormat("dd.MM.yyyy").parse(gDate.replaceAll("[^0-9.]", "")));

            Double serverDateI = Double.parseDouble(serverDate);
            Timber.d( "gDate: *" + gDate.toLowerCase() + "*");
            Double revision = Util.zähleVorkommen(gDate.toLowerCase(), "neu") * 0.1;
            Double VPDateId = serverDateI + revision;

            Timber.d("CheckService/GetDate: Datum von Server erhalten: +" + Util.DoubleToString(VPDateId) + "+");
            Double readDate = CheckService.getLastDate();
            Timber.d("CheckService/GetDate: Gespeichertes Datum: -" + Util.DoubleToString(readDate) + "-");
            if (readDate.equals("ERR")) {
                //CheckService.setRiddenDate(serverDate);
                CheckService.setVPDate(VPDateId);
                CheckService.PostNotification("Es ist ein neuer Vertretungsplan verf\u00fcgbar!", doc);
            } else {
                if (result.equals("error")) {
                    Timber.e("CheckService/GetDate: Serverfehler (RD:" + readDate + ")");
                } else {
                    //Double rideDate = Double.parseDouble(readDate);
                    //int rideDate = Integer.parseInt(readDate);
                    //int newDate = Integer.parseInt(serverDate);
                    //if (newDate > rideDate) {
                    if(VPDateId > readDate) {
                        Timber.d("CheckService/GetDate: Neuer Plan verf\u00fcgbar");
                        CheckService.setVPDate(VPDateId);
                        //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).edit();
                        //editor.putString("readDate", serverDate);
                        //editor.putString("readDate", String.valueOf(VPDateId));
                        //editor.commit();
                        if (CheckService.getMode() != 1) {
                            CheckService.PostNotification("Es ist ein neuer Vertretungsplan verf\u00fcgbar!", doc);
                        } else if (CheckService.checkClass(doc, CheckService.getKlasse())) {
                            CheckService.PostNotification("Du hast morgen Vertretung!", doc);
                        }
                        Timber.d("CheckService/GetDate: Neuer Plan verfügbar-POST NTF");
                    } else {
                        Timber.d("CheckService/GetDate: Kein neuer Plan verfügbar");
                    }
                }
            }
            CheckService.RemoveWakelock();
        } catch (Exception ex) {
            ex.printStackTrace();
            CheckService.RemoveWakelock();
        }
    }


    /*@Deprecated
    protected void onPostExecute_legacy(String result) {
        try {
            char gf = (char) 34;
            GALog loc = new GALog(CheckService.MC);

            if (result != "E") {
                String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
                String gDate = gPart.replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "")/*.split(".");
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat RC = new SimpleDateFormat("yyyyMMdd");
                Date dt = df.parse(gDate);
                String GRC = RC.format(dt);
                loc.debug("CheckService/GetDate: Datum von Server erhalten: +" + GRC + "+");

                String readDate = getLastDate(CheckService.MC);
                loc.debug("CheckService/GetDate: Gespeichertes Datum: -" + readDate + "-");

                if (readDate.equals("ERR")) {
                    CheckService.setRiddenDate(GRC);
                    //CheckService.PostNotification("Es ist ein neuer Vertretungsplan verfügbar!", GRC, result);
                } else if (result.equals("error")) {
                    loc.error("CheckService/GetDate: Serverfehler (RD:" + readDate + ")");
                } else {
                    int rideDate = Integer.parseInt(readDate);
                    int newDate = Integer.parseInt(GRC);

                    if (newDate > rideDate) {
                        loc.debug("CheckService/GetDate: Neuer Plan verfügbar");
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).edit();
                        editor.putString("readDate", GRC);
                        editor.commit();
                        if (CheckService.getMode() == 1) {
                            if (CheckService.checkClass_legacy(result, CheckService.getKlasse())) {
                                //CheckService.PostNotification("Du hast morgen Vertretung!", String.valueOf(newDate), result);
                            }
                        } else {
                            //CheckService.PostNotification("Es ist ein neuer Vertretungsplan verfügbar!", String.valueOf(newDate), result);
                        }
                        loc.debug("CheckService/GetDate: Neuer Plan verfügbar-POST NTF");
                    } else {
                        loc.debug("CheckService/GetDate: Kein neuer Plan verfügbar");
                    }
                }
            }
            CheckService.RemoveWakelock();
        } catch (Exception ex) {
            ex.printStackTrace();
            CheckService.RemoveWakelock();
        }
    }*/

    /**
     * Datum des zuletzt angesehenem Vertretungsplan auslesen
     *
     * @return Datum in YYYYMMDD
     */

}

class SpMenu {
    int id;
    String Montag;
    String Dienstag;
    String Mittwoch;
    String Donnerstag;
    String Freitag;
    String KW;
    String Datum;
    SpMenu(int idn) {
        id = idn;
    }

    public int getID() {
        return id;
    }
    public String getMontag() { return Montag; }
    public String getDienstag() { return Dienstag; }
    public String getMittwoch() { return Mittwoch; }
    public String getDonnerstag() { return Donnerstag; }
    public String getFreitag() { return Freitag; }

    public String getKW() { return KW; }
    public void setKW(String value) { KW = value; }

    public String getDatum() { return Datum; }
    public void setDatum(String Value) { Datum = Value; }

    public void setMontag(String value) { Montag = value; }
    public void setDienstag(String value) { Dienstag = value; }
    public void setMittwoch(String value) { Mittwoch = value; }
    public void setDonnerstag(String value) { Donnerstag = value; }
    public void setFreitag(String value) { Freitag = value; }

    public String getToday(int day) {
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

    public JSONObject toSaveJSON() throws JSONException {
        JSONObject jk = new JSONObject();
        jk.put("Montag", Montag);
        jk.put("Dienstag", Dienstag);
        jk.put("Mittwoch", Mittwoch);
        jk.put("Donnerstag", Donnerstag);
        jk.put("Freitag", Freitag);
        jk.put("Datum", Datum);
        jk.put("KW", KW);
        jk.put("id", id);
        return jk;
    }

    public void fromSaveJSON(JSONObject input, String KW) throws JSONException {
        JSONObject jo = input;
        Montag = jo.getString("Montag");
        Dienstag = jo.getString("Dienstag");
        Mittwoch = jo.getString("Mittwoch");
        Donnerstag = jo.getString("Donnerstag");
        Freitag = jo.getString("Freitag");
        Datum = jo.getString("Datum");
        KW = jo.getString("KW");
        id = jo.getInt("id");
    }
}

/**
 * Datentyp für einen Eintrag im Vertretungsplan
 */
class Eintrag implements Serializable  {
    String Klasse;
    String Stunde;
    String Fachnormal;
    String Vertretung;
    String Raum;
    String Fachvertret;
    String Bemerkung;
    boolean neu;

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
        Klasse = klasse;
        Stunde = stunde;
        Fachnormal = fachnormal;
        Vertretung = vertretung;
        Raum = raum;
        Fachvertret = fachvertret;
        Bemerkung = bemerkung;
        neu = false;
    }

    Eintrag(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung, boolean isNeu) {
        Klasse = klasse;
        Stunde = stunde;
        Fachnormal = fachnormal;
        Vertretung = vertretung;
        Raum = raum;
        Fachvertret = fachvertret;
        Bemerkung = bemerkung;
        neu = isNeu;
    }

    Eintrag(JSONObject input) throws Exception {
        Klasse = input.getString("Klasse");
        Stunde = input.getString("Stunde");
        Fachnormal = input.getString("Fachnormal");
        Vertretung = input.getString("Vertretung");
        Raum = input.getString("Raum");
        Fachvertret = input.getString("Fachvertret");
        Bemerkung = input.getString("Bemerkung");

        if (input.has("Neu")) {
            neu = input.getBoolean("Neu");
        } else {
            neu = false;
        }
    }

    public String getKlasse() {
        try {
            return Klasse.trim();
        } catch(Exception e) {
            return Klasse;
        }

    }

    public void setKlasse(String value) {
        Klasse = value;
    }

    public String getStunde() {
        try {
            return Stunde.trim();
        } catch(Exception e) {
            return Stunde;
        }

    }

    public void setStunde(String value) {
        Stunde = value;
    }

    public String getFachNormal() {
        try {
            return Fachnormal.trim();
        } catch(Exception e) {
            return Fachnormal;
        }

    }

    public void setFachNormal(String value) {
        Fachnormal = value;
    }

    public String getVertretung() {
        if (Vertretung.equals("##") || Vertretung.equals("&nbsp;")) {
            return "niemandem";
        } else {
            try {
                return Vertretung.trim();
            } catch(Exception e) {
                return Vertretung;
            }
        }
    }

    public void setVertretung(String value) {
        Vertretung = value;
    }

    public String getRaum() {
        if (Raum.equals("##")) {
            return "k.A.";
        } else {
            try {
                return Raum.trim();
            } catch(Exception e) {
                return Raum;
            }
        }
    }

    public void setRaum(String value) {
        Raum = value;
    }

    public String getFachVertretung() {
        if (Fachvertret.equals("##") || Vertretung.equals("&nbsp;")) {
            return "nichts";
        } else {
            try {
                return Fachvertret.trim();
            } catch(Exception e) {
                return Fachvertret;
            }
        }
    }

    public void setFachVertretung(String value) {
        Fachvertret = value.trim();
    }

    public String getBemerkung() {
        if (!Bemerkung.matches(".*[a-zA-Z]+.*")) {
            return "";
        } else {
            return Bemerkung;
        }
    }

    public String getBemerkungForCard() {
        if (!Bemerkung.matches(".*[a-zA-Z]+.*")) {
            return "";
        } else {
            return "\n\n" + Bemerkung;
        }
    }

    public void setBemerkung(String value) {
        Bemerkung = value;
    }

    public boolean getNeu() {
        return neu;
    }

    public void setNeu(boolean value) {
        neu = value;
    }

    public Boolean isKlasse(String input) {
        return Klasse == input;
    }

    public String[] toSaveString() {
        String[] dieser = new String[8];
        dieser[0] = Klasse;
        dieser[1] = Stunde;
        dieser[2] = Fachnormal;
        dieser[3] = Vertretung;
        dieser[4] = Raum;
        dieser[5] = Fachvertret;
        dieser[6] = Bemerkung;
        dieser[7] = Util.bolToStr(neu);
        //Set<String> dieserSet = new HashSet<String>(Arrays.asList(dieser));
        return dieser;
    }

}

class Eintrage extends ArrayList<Eintrag> {
    public ArrayList<Eintrag> getKlasseGruppe(String klasse, Boolean reverse) throws KeineEintrageException {
        ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
        for (Eintrag single : this) {
            //Log.d("GSApp", "ET: " + single.getKlasse() + " vs SUCH: " + klasse);
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

    public ArrayList<Eintrag> getKlasseGruppeS(String klasse) throws KeineEintrageException {
        ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
        for (Eintrag single : this) {
            //Log.d("GSApp", "ET: " + single.getKlasse() + " vs SUCH: " + klasse);
            if (single.getKlasse().equals(klasse)) {
                outp.add(single);
            }
        }

        if (outp.size() < 1) {
            throw new KeineEintrageException();
        } else {
            return outp;
        }
    }

    public ArrayList<String> getKlassen() throws KeineKlassenException {
        String liste = "";
        for (Eintrag single : this) {
            if (!liste.contains(single.getKlasse())) {
                liste = liste + single.getKlasse() + ",";
            }
        }

        if (liste.equals("")) {
            throw new KeineKlassenException();
        } else {
            liste = method(liste);
            ArrayList<String> outp = new ArrayList<String>();
            Collections.addAll(outp, liste.split(","));
            return outp;
        }
    }

    public String method(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}

class KeineEintrageException extends Exception {

}

class KeineKlassenException extends Exception {

}

class FileLogTree extends Timber.DebugTree {
    File log;

    public FileLogTree(Context context) {
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


/*
public void appendLog(String text)
{
   File logFile = new File("sdcard/log.file");
   if (!logFile.exists())
   {
      try
      {
         logFile.createNewFile();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   try
   {
      //BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
      buf.append(text);
      buf.newLine();
      buf.close();
   }
   catch (IOException e)
   {
      // TODO Auto-generated catch block
      e.printStackTrace();
   }
}
 */