package de.xorg.gsapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KontaktAnzeige extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
        Util.setThemeUI(this);
		setContentView(R.layout.kontaktzeug);
		
		Button AnrufLohau = (Button) findViewById(R.id.lohauAnruf);
		Button AnrufDamm = (Button) findViewById(R.id.dammAnruf);
		
		Button MailLohau = (Button) findViewById(R.id.lohauMail);
		Button MailDamm = (Button) findViewById(R.id.dammMail);
		Button MailXorg = (Button) findViewById(R.id.xorgMail);

        Util.setOrientation(this);
		
		setTitle("Kontakt");
		
		SetBean(BeanUI);
		
		AnrufLohau.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				call("03675702977");
			}
		});
		
        AnrufDamm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				call("03675468890");
			}
		});
        
        MailLohau.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sekretariat-lohau@gymson.de"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Bitte Betreff eintragen");
				i.putExtra(Intent.EXTRA_TEXT   , "");
				try {
				    startActivity(Intent.createChooser(i, "Mail senden"));
				} catch (ActivityNotFoundException ex) {
				    toaster("Fehler: Es sind keine E-Mail-Apps installiert!");
				}
			}
		});
        
        MailDamm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sekretariat@gymson.de"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Bitte Betreff eintragen");
				i.putExtra(Intent.EXTRA_TEXT   , "");
				try {
				    startActivity(Intent.createChooser(i, "Mail senden"));
				} catch (ActivityNotFoundException ex) {
				    toaster("Fehler: Es sind keine E-Mail-Apps installiert!");
				}
			}
		});
        
        MailXorg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"xorgmc@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "GSApp");
				i.putExtra(Intent.EXTRA_TEXT   , "");
				try {
				    startActivity(Intent.createChooser(i, "Mail senden"));
				} catch (ActivityNotFoundException ex) {
				    toaster("Fehler: Es sind keine E-Mail-Apps installiert!");
				}
			}
		});
        
        
		
		
	}
	
	public void SetBean(Boolean UseDarkBean) {
		TextView T1 = (TextView) findViewById(R.id.textView1);
		TextView T2 = (TextView) findViewById(R.id.versionText);
		TextView T3 = (TextView) findViewById(R.id.textView3);
		TextView T4 = (TextView) findViewById(R.id.textView4);
		Button B1 = (Button) findViewById(R.id.lohauAnruf);
		Button B2 = (Button) findViewById(R.id.lohauMail);
		Button B3 = (Button) findViewById(R.id.dammAnruf);
		Button B4 = (Button) findViewById(R.id.dammMail);
		Button B5 = (Button) findViewById(R.id.xorgMail);
		RelativeLayout widerrist = (RelativeLayout) findViewById(R.id.withers);
		if(UseDarkBean) {
			widerrist.setBackgroundColor(Color.BLACK);
			B1.setTextColor(Color.WHITE);
			T1.setTextColor(Color.WHITE);
			T2.setTextColor(Color.WHITE);
			T3.setTextColor(Color.WHITE);
			T4.setTextColor(Color.WHITE);
			B2.setTextColor(Color.WHITE);
			B3.setTextColor(Color.WHITE);
			B4.setTextColor(Color.WHITE);
			B5.setTextColor(Color.WHITE);
		} else {
			widerrist.setBackgroundColor(Color.parseColor("#fed21b"));
			B1.setTextColor(Color.BLACK);
			B2.setTextColor(Color.BLACK);
			B3.setTextColor(Color.BLACK);
			B4.setTextColor(Color.BLACK);
			B5.setTextColor(Color.BLACK);
			T1.setTextColor(Color.BLACK);
			T2.setTextColor(Color.BLACK);
			T3.setTextColor(Color.BLACK);
			T4.setTextColor(Color.BLACK);
		}
		
	}
	
	public void toaster(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	public void call(String tel) {
		String ttyp = PreferenceManager.getDefaultSharedPreferences(this).getString("ruf", "dial");
		
		if(ttyp.equals("call")) {
			try {
	            Intent callIntent = new Intent(Intent.ACTION_CALL);
	            callIntent.setData(Uri.parse("tel:" + tel));
	            startActivity(callIntent);
	        } catch (ActivityNotFoundException activityException) {
	            activityException.printStackTrace();
	            Log.e("Kontakt Anruf", "Anruf fehlgeschlagen");
	            toaster("Fehler: Beim Anrufen ist ein Fehler aufgetreten!\n"+activityException.getMessage());
	        }
		} else {
			try {
	            Intent callIntent = new Intent(Intent.ACTION_DIAL);
	            callIntent.setData(Uri.parse("tel:" + tel));
	            startActivity(callIntent);
	        } catch (ActivityNotFoundException activityException) {
	            activityException.printStackTrace();
	            Log.e("Kontakt Anruf", "Anruf fehlgeschlagen");
	            toaster("Fehler: Beim Anrufen ist ein Fehler aufgetreten!\n"+activityException.getMessage());
	        }
		}
		
	}
	
}
