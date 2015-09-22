package de.xorg.gsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class BootChecker extends BroadcastReceiver {

	private CheckService alarm;

    private GALog l;
	
	//Vertretungsplan-Check-Service beim Booten starten
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
        	alarm = new CheckService();
        	l = new GALog(context);

        	//Check aktiviert?
        	int Mode = getMode(context);
    		if(!(Mode == 0)) {
    			//Ja, Check aktiviert
    		     if(alarm != null){
    		        alarm.SetAlarm(context);
                    l.debug("BootService: Broadcast received - starting background service");
    	            Toast.makeText(context, "GSApp: Automatische Vertretungsplan-Überprüfung aktiviert! (BS)", Toast.LENGTH_LONG).show();
    		     }else{
    		    	l.error("BootService: Cannot start service: var ALARM is NULL");
    		     }
    		} else {
    			l.debug("Not starting service - disabled by user");
    		}
        }
    }

    public static int getMode(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt("check", 0);
    }
}