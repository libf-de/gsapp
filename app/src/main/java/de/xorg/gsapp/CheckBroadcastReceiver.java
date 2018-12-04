package de.xorg.gsapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.core.app.NotificationCompat;
import timber.log.Timber;

public class CheckBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Gotcha!");
        String[] klassen = intent.getStringArrayExtra("containedClasses");
        Date forDate = intent.getParcelableExtra("forDate");


    }


}
