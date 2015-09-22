package de.xorg.gsapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements
        Thread.UncaughtExceptionHandler {
    private final Activity myContext;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Activity context) {
        myContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ STACK TRACE ************\n");
        errorReport.append(stackTrace.toString());
        errorReport.append("\n************ OTHER DETAIL ************\n");
        errorReport.append("Class name: ");
        errorReport.append(myContext.getLocalClassName());
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Thread name: ");
        errorReport.append(thread.getName());
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("App version: ");
        errorReport.append(Util.getVersion(myContext));
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Build version: ");
        errorReport.append(Util.getVersionCode(myContext));
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Debug build: ");
        errorReport.append(String.valueOf(BuildConfig.DEBUG));
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("UID: ");
        errorReport.append(PreferenceManager.getDefaultSharedPreferences(myContext).getString("id", "X0X0X0X0X"));
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("AsyncLoad: ");
        errorReport.append(String.valueOf(PreferenceManager.getDefaultSharedPreferences(myContext).getBoolean("loadAsync", false)));
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Screen size: ");
        errorReport.append(getScreenSize());
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Screen DPI: ");
        errorReport.append(getDPI());
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Screen Res: ");
        errorReport.append(getScreenRes());
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

        Intent intent = new Intent(myContext, CrashActivity.class);
        intent.putExtra("error", errorReport.toString());
        myContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private String getScreenSize() {
        // Bildschirmgroesse
        DisplayMetrics dm = new DisplayMetrics();
        myContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        int dens=dm.densityDpi;
        double wi=(double)width/(double)dens;
        double hi=(double)height/(double)dens;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double si = Math.sqrt(x+y);
        si = Math.round(si * 100);
        si = si/100;
        return String.valueOf(si);
    }

    private String getDPI() {
        // DPI
        DisplayMetrics metrics = myContext.getResources().getDisplayMetrics();
        int densityDpi = (int)(metrics.density * 160f);
        return String.valueOf(densityDpi);
    }

    private String getScreenRes() {
        // Bildschirmaufloesung
        DisplayMetrics displaymetrics = new DisplayMetrics();
        myContext.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int heightA = displaymetrics.heightPixels;
        int widthA = displaymetrics.widthPixels;
        return String.valueOf(widthA) + "x" + String.valueOf(heightA);
    }

}