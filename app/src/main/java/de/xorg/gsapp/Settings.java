package de.xorg.gsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends ActionBarActivity implements OnItemSelectedListener {
	
	String EinstellungKlasse = "";
	Boolean TabletMode = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		setTitle("Einstellungen");
		
		// Variablen
		Boolean VPASync = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("loadAsync", false);
		int VPChecker = PreferenceManager.getDefaultSharedPreferences(this).getInt("check", 0);
		Spinner kla = (Spinner) findViewById(R.id.klasseEinst);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klassen_array, android.R.layout.simple_spinner_item);
        final RadioButton phoneCall = (RadioButton) findViewById(R.id.phoneCall);
        final RadioButton phoneDial = (RadioButton) findViewById(R.id.phoneDial);
		final CheckBox VASync = (CheckBox) findViewById(R.id.vpAsync);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		kla.setAdapter(adapter);
		kla.setOnItemSelectedListener(this);
		kla.setSelection(adapter.getPosition(PreferenceManager.getDefaultSharedPreferences(this).getString("klasse", "")));
		
		VASync.setChecked(VPASync);
		VASync.setLongClickable(true);
		VASync.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
			    AlertDialog ad = new AlertDialog.Builder(Settings.this).create();  
			    ad.setCancelable(true); // This blocks the 'BACK' button
			    ad.setTitle("Kurzhilfe");
			    ad.setMessage("Synchron laden ist scheller als asynchrones Laden, allerdings kann es (insbesondere bei langsamen Internet) zu Instabilität führen. Wenn die App beim Laden des Vertretungsplans abstürtzt, aktivieren sie Asynchrones laden. Insbesondere für Samsung Galaxy-Handys wird Synchrones laden empfohlen, da das Asynchrone laden hier um die 2-3 Minuten dauert.\n\nSynchron laden = nicht angehakt, asynchron laden = angehakt");  
			    ad.setButton("OK", new DialogInterface.OnClickListener() {  
			        @Override  
			        public void onClick(DialogInterface dialog, int which) {  
			            dialog.dismiss();                      
			        }  
			    });  
			    ad.show();  
				return false;
			}
			
		});

        switch (VPChecker) {
            case 0:
                ((RadioButton) findViewById(R.id.ntf_never)).setChecked(true);
                break;
            case 1:
                ((RadioButton) findViewById(R.id.ntf_user)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.ntf_always)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.ntf_never)).setChecked(true);
                break;
        }

        String ttyp = PreferenceManager.getDefaultSharedPreferences(this).getString("ruf", "dial");

        if(ttyp.equals("call")) {
            phoneCall.setChecked(true);
            phoneDial.setChecked(false);
        } else {
            phoneCall.setChecked(false);
            phoneDial.setChecked(true);
        }
		
		Toast.makeText(this, "Drücken sie den Zurück-Knopf um die Einstellungen zu speichern!", Toast.LENGTH_SHORT).show();
	}
	
	public void showInPlay(View v) {
        try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=de.xorg.gsapp"));
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Auf diesem Gerät ist scheinbar kein Google Play Store installiert!", Toast.LENGTH_SHORT).show();
        }
    }
	
	public void Autologin(View v) {
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.anmeldung);
		dialog.setTitle("Automatische Anmeldung");
		 
		// set the custom dialog components - text, image and button
		final TextView User = (TextView) dialog.findViewById(R.id.username);
		final CheckBox Use = (CheckBox) dialog.findViewById(R.id.autologinEnable);
		String Username = Datenspeicher.getUser(this);
	    if(Username.equalsIgnoreCase("")) {
	    	Use.setChecked(false);
	    	((TextView) dialog.findViewById(R.id.username)).setText("");
	    	((TextView) dialog.findViewById(R.id.password)).setText("");
	    } else {
	    	Use.setChecked(true);
	    	((TextView) dialog.findViewById(R.id.username)).setText(Datenspeicher.getUser(this));
	    	try {
	    		String PASSWORD_STATE = Datenspeicher.getPassword(this);
	    		if(PASSWORD_STATE.startsWith("error")) {
	    			if(PASSWORD_STATE.contains("nocb")) {
	    				Toast.makeText(this, "Fehler beim Laden des Passworts: Kontrollbyte fehlt!", Toast.LENGTH_SHORT).show();
	    				((TextView) dialog.findViewById(R.id.password)).setText("");
	    			} else {
	    				Toast.makeText(this, "Fehler beim Laden des Passworts!", Toast.LENGTH_SHORT).show();
	    				((TextView) dialog.findViewById(R.id.password)).setText("");
	    			}
	    		} else {
	    			((TextView) dialog.findViewById(R.id.password)).setText(PASSWORD_STATE);
	    		}
			} catch (Exception e) {
				((TextView) dialog.findViewById(R.id.password)).setText("");
				e.printStackTrace();
			}
	    }
		 
		Button saveButton = (Button) dialog.findViewById(R.id.serverSave);
		Button cancelButton = (Button) dialog.findViewById(R.id.serverCancel);
		// if button is clicked, close the custom dialog
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Use.isChecked()) {
					Datenspeicher.saveUser(User.getText().toString(), Settings.this);
					if(!Datenspeicher.savePassword(((TextView) dialog.findViewById(R.id.password)).getText().toString(), Settings.this)) {
						Toast.makeText(Settings.this, "Fehler beim Verschlüsseln des Passworts", Toast.LENGTH_SHORT).show();
					}
				} else {
					Datenspeicher.savePassword("", Settings.this);
					Datenspeicher.saveUser("", Settings.this);
				}
				dialog.dismiss();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Spinner sp = (Spinner) findViewById(R.id.klasseEinst);
		String Name = (String) sp.getSelectedItem();
		EinstellungKlasse = Name.replace("keine", "");
    }

    public void onNothingSelected(AdapterView<?> parent) {
    	EinstellungKlasse = "";
    	Toast.makeText(this, "Hier läuft irgendwas falsch...", Toast.LENGTH_LONG).show();
    }
	
	@Override
	public void onBackPressed() {
		
		saveAll();
		
		Log.d("Settings", "Back pressed");
		
		//Intent mStartActivity = new Intent(this, MainActivity.class);
		//int mPendingIntentId = 123456;
		//PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		//AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		//mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		//System.exit(0);
        
        super.onBackPressed();
        return;
	}
	
	@Override
	public void onDestroy() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		super.onDestroy();
	}
	
	public void saveAll() {
		
		String ttyp = "dial";
		if(!(TabletMode)) {
			RadioButton call = (RadioButton) findViewById(R.id.phoneCall);
			
			if(call.isChecked()) {
				ttyp = "call";
			} else {
				ttyp = "dial";
			}
		}

        int checkVal = 0;
        if( ((RadioButton) findViewById(R.id.ntf_never)).isChecked() ) {
            checkVal = 0;
        } else if( ((RadioButton) findViewById(R.id.ntf_user)).isChecked() ) {
            checkVal = 1;
        } else if( ((RadioButton) findViewById(R.id.ntf_always)).isChecked() ) {
            checkVal = 2;
        }

        CheckService CS = new CheckService();
        if(checkVal == 1 || checkVal == 2) {
            if(!CS.CheckAlarm(this)) {
                CS.SetAlarm(this);
            }
        } else {
            if(CS.CheckAlarm(this)) {
                CS.CancelAlarm(this);
            }
        }

		CheckBox VALoad = (CheckBox) findViewById(R.id.vpAsync);
		
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString("klasse", EinstellungKlasse);
        editor.putString("ruf", ttyp);
        editor.putBoolean("loadAsync", VALoad.isChecked());
        editor.putInt("check", checkVal);
        editor.commit();
	}
	
	public void toaster(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
}



