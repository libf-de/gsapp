package de.xorg.gsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class About extends AppCompatActivity {
	
	// Intent-Extras' -Bezeichner für Name und URL
	public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
	public final static String EXTRA_NAME = "de.xorg.gsapp.MESSAGENAME";
	// Zähler für Easter-Egg
	public static int EggPressed = 0;
	private GALog l;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        Util.setThemeUI(this);
		setContentView(R.layout.about);

		Util.setOrientation(this);

        l = new GALog(this);

		Button HPButton = (Button) findViewById(R.id.homepageButton);
		TextView ver = (TextView) findViewById(R.id.versionText);
		Button B2 = (Button) findViewById(R.id.xorgButton);

		ver.setText(Util.getVersion(this));
		ver.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://gsapp.xorg.ga/"));
				startActivity(browserIntent);
			}
		});

		ver.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				try {
				    StringBuilder NOTE = new StringBuilder();

				    NOTE.append("--> Note\n");
				    NOTE.append("Diese Informationen werden nicht an irgendjemanden\n");
				    NOTE.append("weiterverkauft oder an einen Server gesendet! Sie\n");
				    NOTE.append("sind nur zur Fehleranalyse gedacht und sollen, wenn\n");
				    NOTE.append("vom Entwickler angefordert, von ihnen weitergeleitet\n");
				    NOTE.append("werden. Wenn sie die Informationen weiterleiten möchten,\n");
				    NOTE.append("tippen sie hier auf SENDEN. Danke");


					AlertDialog.Builder alert = new AlertDialog.Builder(About.this);

					alert.setTitle("Debug-Informationen");
					alert.setMessage("==[ GSApp 5.x »Merlin« ]==\n" + About.dataLoader(About.this) + "\n" + NOTE.toString());

					alert.setCancelable(false);

					alert.setPositiveButton("SENDEN", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String Sharer = About.dataLoader(About.this);
						Toast.makeText(About.this, "Senden sie diese Informationen per Mail an xorgmc@gmail.com", Toast.LENGTH_LONG).show();
						Intent sendIntent = new Intent();
						sendIntent.setAction(Intent.ACTION_SEND);
						sendIntent.putExtra(Intent.EXTRA_TEXT, "GSAPP DEBUG INFORMATION\n" + Sharer);
						sendIntent.setType("text/plain");
						startActivity(sendIntent);
					  }
					});

					alert.setNegativeButton("SCHLIESSEN", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					  }
					});

					alert.show();
				} catch(Exception e) {
					Toast.makeText(About.this, "Fehler beim Anzeigen der Debug-Informationen", Toast.LENGTH_SHORT).show();
                    l.error("at About/DebugInfo: Fehler beim Anzeigen der Debug-Infos: " + e.getMessage());
				}
				return false;
			}
		});

		//Easter-Egg-Bytecode :D
		B2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (EggPressed == 5) {
					zeigeEGG();
			        return;
			    }

			    EggPressed = EggPressed + 1;

			    new Handler().postDelayed(new Runnable() {

			        @Override
			        public void run() {
						EggPressed = 0;
					}
				}, 2000);
			}
		});

        B2.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(About.this, "Entwickereinstellungen werden geöffnet...", Toast.LENGTH_SHORT).show();
                Intent intentes = new Intent(About.this, DeveloperSettings.class);
                startActivity(intentes);
                return false;
            }
        });

		HPButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://xorg.ga/"));
				startActivity(browserIntent);
			}
		});
	}

	@Override
	public void onDestroy() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		super.onDestroy();
	}

	//EasterEgg zeigen
	public void zeigeEGG() {
		if(Util.hasInternet(getApplicationContext())) {
			Intent intent = new Intent(this, InternetViewer.class);
			intent.putExtra(EXTRA_URL, "http://gsapp.xorg.ga/uimserv.php");
			intent.putExtra(EXTRA_NAME, "EasterEgg");
			startActivity(intent);
		}
	}
}