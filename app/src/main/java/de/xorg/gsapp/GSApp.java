package de.xorg.gsapp;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

public class GSApp extends MultiDexApplication {
    private static GSApp instance;

    public static GSApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        if(instance.getApplicationContext() == null)
            throw new RuntimeException("No Application Context");
        return instance.getApplicationContext();
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}