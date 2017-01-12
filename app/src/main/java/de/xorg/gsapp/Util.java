package de.xorg.gsapp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Calendar;

public class Util {

    public static final int PERMISSION_DEBUG = 22021996;
    public static final int PERMISSION_CALL = 17082000;

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
        return "GSApp " + getVersion(c) + " on " + About.getDeviceName() + " (Android " + Build.VERSION.RELEASE.toString() + ") " + MORE;
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
			// TODO Auto-generated catch block
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
                    a.setTheme(R.style.PAppTheme);
                }
                break;
            case "classic":
                a.setTheme(R.style.ClassicTheme);
                break;
            case "holo":
                a.setTheme(R.style.HoloTheme);
                break;
            case "holodark":
                a.setTheme(R.style.HoloDarkTheme);
                break;
            case "material":
                a.setTheme(R.style.MaterialTheme);
                break;
            case "materialdark":
                a.setTheme(R.style.MaterialDarkTheme);
                break;
            default:
                Toast.makeText(a, "WARNUNG: UNGÜLTIGE ENTWICKEREINSTELLUNG UITHEMEMODE!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("NewApi")
    public static boolean hasSoftNavigation(Context context)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
        return false;
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
