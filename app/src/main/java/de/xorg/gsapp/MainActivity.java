package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
	public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
	public final static String EXTRA_NAME = "de.xorg.gsapp.MESSAGENAME";
	public static MainActivity IBLAH;
	@SuppressWarnings("unused")
	private Context c;
	@SuppressWarnings("unused")
	private boolean isConnected = false;
	private CheckService alarm;
	private Boolean isNexus = false;

    private GALog l;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		//App-Thema einstellen
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
        Util.setThemeUI(this);
		setContentView(R.layout.activity_main);

        l = new GALog(this);
	
		IBLAH = this;
		
		alarm = new CheckService();
		
		int fetchMode = PreferenceManager.getDefaultSharedPreferences(this).getInt("check", 0);
		if(!(fetchMode == 0)) {
			startRepeatingTimer();
		} else {
			cancelRepeatingTimer();
		}
		
		c = this;

		isNexus = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("devMode", false);
		
	    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isfirstrun", true)) {
            Intent intent = new Intent(this, FirstRunActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
		}

		if (!PreferenceManager.getDefaultSharedPreferences(this).contains("isSamsung")) {
			boolean isSamsung = Build.MANUFACTURER.equals("samsung");
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putBoolean("isSamsung", isSamsung);
			if (isSamsung) {
				editor.putBoolean("loadAsync", false);
			} else {
				editor.putBoolean("loadAsync", true);
			}
			editor.commit();
			Toast.makeText(this, "Sollte der Vertretungsplan lange zum Anzeigen benötigen, melde dies bitte dem Entwickler!", Toast.LENGTH_LONG).show();
		}
		setTitle("GSApp MRLNRW0");

		new ServerMessageHandler(this).execute("");
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(isNexus) {
			getMenuInflater().inflate(R.menu.maind, menu);
		} else {
			getMenuInflater().inflate(R.menu.main, menu);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(isNexus) {
			switch (item.getItemId()) {
		    case R.id.action_settings:
		    	 Intent intent = new Intent(this, Settings.class);
			     startActivity(intent);
		         return true;
		    case R.id.action_about:
		    	 Intent intent2 = new Intent(this, About.class);
			     startActivity(intent2);
		         return true;
		    case R.id.action_share:
		    	 shareAPP();
		    	 return true;
		    case R.id.action_fcheck:
		    	 ForceOT();
		    	 Toast.makeText(this, "Erzwinge Überprüfung..", Toast.LENGTH_SHORT).show();
		    	 return true;
            case R.id.action_devsetting:
                 Intent intentes = new Intent(this, DeveloperSettings.class);
                 startActivity(intentes);
                 return true;
		    default:
		         return super.onOptionsItemSelected(item);
		    }
		} else {
			switch (item.getItemId()) {
				case R.id.action_settings:
					Intent intent = new Intent(this, Settings.class);
					startActivity(intent);
					return true;
				case R.id.action_about:
					Intent intent2 = new Intent(this, About.class);
					startActivity(intent2);
					return true;
				case R.id.action_share:
					shareAPP();
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		}
	}
	
	public void ForceOT() {
		Context context = this.getApplicationContext();
	     if(alarm != null){
	        	 alarm.ForceCheck(context);
	     }else{
	    	 Toast.makeText(context, "Es besteht keine Verbindung zur Überprüfungs-Programmklasse (Code: VPC-FCN)", Toast.LENGTH_SHORT).show();
	     }
	}
		
	public void startRepeatingTimer() {
	     Context context = this.getApplicationContext();
	     if(alarm != null){
	         if(!(alarm.CheckAlarm(this))) {
	        	 alarm.SetAlarm(context);
	         }
	     }else{
	    	 Toast.makeText(context, "Es besteht keine Verbindung zur Überprüfungs-Programmklasse (Code: SRT-AIN)", Toast.LENGTH_SHORT).show();
	     }
	  }
	
	public void cancelRepeatingTimer(){
	     Context context = this.getApplicationContext();
	     if(alarm != null){
	       if(alarm.CheckAlarm(this)) {
	    	   alarm.CancelAlarm(context);
	       }
	     }else{
	       Toast.makeText(context, "Es besteht keine Verbindung zur Überprüfungs-Programmklasse (Code: CRT-AIN)", Toast.LENGTH_SHORT).show();
	     }
	 }
	
	public void zeigeVP(View view) {
		Intent intent = new Intent(this, VPlanViewer.class);
	    startActivity(intent);
	}
	
	public void zeigeKT(View view) {
		Intent intent = new Intent(this, KontaktAnzeige.class);
	    startActivity(intent);
	}
	
	public void zeigeEP(View view) {
		Intent intent = new Intent(this, SpeiseplanActivity.class);
	    startActivity(intent);
	}
	
	public void zeigeEB(View view) {
		Intent intent = new Intent(this, Essensbestellung.class);
	    startActivity(intent);
	}
	
	public void zeigeTM(View view) {
		Intent intent = new Intent(this, InternetViewer.class);
		intent.putExtra(EXTRA_URL, "http://www.gymnasium-sonneberg.de/Informationen/Term/ausgebenK.php5");
		intent.putExtra(EXTRA_NAME, "Termine");
	    startActivity(intent);		
	}
	
	public void zeigeAK(View view) {
		Intent intent = new Intent(this, InternetViewer.class);
		intent.putExtra(EXTRA_URL, "http://www.gymnasium-sonneberg.de/Informationen/Aktuell/ausgeben.php5?id=");
		intent.putExtra(EXTRA_NAME, ".Aktuelles");
	    startActivity(intent);		
	}
	
	public void shareAPP() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, ich empfehle dir die GSApp, die App des Gymnasiums Sonneberg! http://gsapp.xorg.ga/");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	public void reportProblem(View v) {
		AlertDialog.Builder KKDialog = new AlertDialog.Builder(this);

		KKDialog.setTitle("Problem melden");
		KKDialog.setMessage("Dies ist eine Vor-Freigabeversion (RC). Sollten noch Probleme / Fehler auftreten, melden sie bitte diese dem Entwickler.\n\nDie App wird nun abstürzen, dies ist normal. Danach können sie ihre Nachricht an den Entwickler verfassen!\n\nVielen Dank!");

		KKDialog.setCancelable(false);

		KKDialog.setNeutralButton("Melden", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                throw new RuntimeException("This is a known error");
            }
        });

        KKDialog.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

		KKDialog.show();
	}
}