package de.xorg.gsapp;

/**
 * Created by xorg on 23.12.14.
 */

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class GALog {

    public Context ct;
    public boolean DEBUG = BuildConfig.DEBUG;

    GALog(Context c) {
        ct = c;
        DEBUG = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("debug", true);
        File debug = new File("/sdcard/galog.log");
        if(!debug.exists()) {
            try {
                FileWriter f = new FileWriter("/sdcard/galog.log", true);
                f.write("### GALOG v. 1.1pub ###\nwritten by Xorg\nwritten for iMute\ndebug is currently " + String.valueOf(DEBUG) + "\n\n - log start -\n");
                f.flush();
                f.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void debug(String message) {
        if(DEBUG) {
            try {

                FileWriter f = new FileWriter("/sdcard/galog.log", true);

                Calendar c = Calendar.getInstance();

                f.write("[DEBUG][" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + " " + c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR) + "] " + message + "\n");
                f.flush();
                f.close();
                Log.d("GALog", "GALog:" + message);
            } catch (Exception e) {
                Log.d("GALog", "EGALog:" + message);
            }
        }
    }

    public void error(String message) {
        if(DEBUG) {
            try {

                FileWriter f = new FileWriter("/sdcard/galog.log", true);

                Calendar c = Calendar.getInstance();

                f.write("[ERROR][" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + " " + c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR) + "] " + message + "\n");
                f.flush();
                f.close();
                Log.w("GALog", "GALog:" + message);
            } catch (Exception e) {
                Log.w("GALog", "EGALog:" + message);
            }
        }
    }

    public void critical(String message) {
        if(DEBUG) {
            try {

                FileWriter f = new FileWriter("/sdcard/galog.log", true);

                Calendar c = Calendar.getInstance();

                f.write("[CRITICAL][" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + " " + c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR) + "] " + message + "\n");
                f.flush();
                f.close();
                Log.e("GALog", "GALog:" + message);
            } catch (Exception e) {
                Log.d("GALog", "EGALog:" + message);
            }
        }
    }

    public void info(String message) {
        if(DEBUG) {
            try {

                FileWriter f = new FileWriter("/sdcard/galog.log", true);

                Calendar c = Calendar.getInstance();

                f.write("[INFO][" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + " " + c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR) + "] " + message + "\n");
                f.flush();
                f.close();
                Log.i("GALog", "GALog:" + message);
            } catch (Exception e) {
                Log.i("GALog", "EGALog:" + message);
            }
        }
    }
}
