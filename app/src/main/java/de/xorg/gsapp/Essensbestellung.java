package de.xorg.gsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;

public class Essensbestellung extends ActionBarActivity {
	
	
	//Variable für URL
	String URI;
	
	//Variable, ob mit dem Internet verbunden ist
	private boolean isConnected = true;

    private GALog l;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		Util.setThemeUI(this);
		setContentView(R.layout.webviewer);

        l = new GALog(this);

        Util.setOrientation(this);
		
		//Variablen
		WebView Speisen = (WebView) findViewById(R.id.WebView);
		RelativeLayout widerrist = (RelativeLayout) findViewById(R.id.withers);
		final Activity MyActivity = this;
		
		//Titel einstellen
		setTitle("Essenbestellung - Startseite");

		widerrist.setBackgroundColor(Color.parseColor("#fed21b"));
 		
		Speisen.setWebViewClient(new MyWebViewClient() );
		
		Speisen.getSettings().setJavaScriptEnabled(false);
		Speisen.getSettings().setBuiltInZoomControls(true);
		
	    isConnected = Util.hasInternet(getApplicationContext());
		
		URI = "http://www.schulkueche-bestellung.de/";
		
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		Speisen.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)   
            {
            	String realtitle = MyActivity.getTitle().toString();
                MyActivity.setTitle(realtitle);
                MyActivity.setProgress(progress * 100);

                if(progress == 100)
                    MyActivity.setTitle(realtitle);
                }
            });
		
		if(!(PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "").equals(""))) {
			try {
				Speisen.setInitialScale(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "")) - 50);
			} catch(NumberFormatException ex) {
				Speisen.setInitialScale(100);
			} catch(Exception ex) {
				Speisen.setInitialScale(100);
			}
		} else {
			Speisen.setInitialScale(100);
		}
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			//Funktioniert erst ab Honeycomb (3.X)
			Speisen.getSettings().setDisplayZoomControls(false);
		}
		
		String ALOGUSER = Datenspeicher.getUser(this);
		String ALOGPASS = Datenspeicher.getPassword(this);
		
		if(ALOGPASS.startsWith("error")) {
			Toast.makeText(this, "Fehler in autom. Anmeldung: Entschlüsseln des Passworts fehlgeschlagen", Toast.LENGTH_SHORT).show();
			Speisen.loadUrl(URI);
		} else {
			if(ALOGUSER.equals("")) {
				Speisen.loadUrl(URI);
			} else {
				Speisen.postUrl("http://www.schulkueche-bestellung.de/index.php?ear_a=akt_login", EncodingUtils.getBytes("Login_Name=" + ALOGUSER + "&Login_Passwort=" + ALOGPASS, "UTF-8"));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.bestellung, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.refresh:
	         openUrl(URI);
	         return true;
	    case R.id.gotoo:
	    	 return true;
	    case R.id.startseite:
	    	 URI = "http://www.schulkueche-bestellung.de/index.php?m=2;0";
	    	 setTitle("Essenbestellung - Startseite");
	    	 openUrl(URI);
	    	 return true;
	    case R.id.bestellen:
	    	 URI = "http://www.schulkueche-bestellung.de/index.php?m=2;2";
	    	 setTitle("Essenbestellung - Bestellung");
	    	 openUrl(URI);
	    	 return true;
	    case R.id.plan:
	    	 URI = "http://www.schulkueche-bestellung.de/index.php?m=2;1";
	    	 setTitle("Essenbestellung - Speiseplan");
	    	 openUrl(URI);
	    	 return true;
	    case R.id.account:
	    	 URI = "http://www.schulkueche-bestellung.de/index.php?m=2;5";
	    	 setTitle("Essenbestellung - Mein Account");
	    	 openUrl(URI);
	    	 return true;
	    case R.id.abmelden:
	    	 URI = "http://www.schulkueche-bestellung.de/?a=login/logout";
	    	 setTitle("Essenbestellung - Abmelden");
	    	 openUrl(URI);
	    	 return true;
	    default:
	         return super.onOptionsItemSelected(item);
	    }
	}
		
	public void openUrl(String url) {
		WebView Speisen = (WebView) findViewById(R.id.WebView);
		Speisen.loadUrl(url);
	}
	
    private class MyWebViewClient extends WebViewClient {
    	
    	final String OFFLINE = "<html><head></head><body bgcolor='#fed21b'><h2>Sie sind offline</h2><br><b>Es ist keine Internetverbindung verf&uuml;gbar! :(</b></body></html>";
    	final String TIMEOUT = "<html><head></head><body bgcolor='#fed21b'><h2>TIMEOUT</h2><br><b>Der Server braucht zu lange,<br>um eine Antwort zu senden :(<br>Versuchen sie, einen<br>anderen Server in den<br>Einstellungen zu w&auml;hlen!<br></b></body></html>";
    	final String GENERIC = "<html><head></head><body bgcolor='#fed21b'><h2>Fehler</h2><br><b>Bei der Verbindung zum Server<br>ist ein allgemeiner Fehler aufgetr. :(<br><br></b></body></html>";
    	
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	if (isConnected) {
        		view.loadUrl(url);
                return true;
            } else {
                view.loadData(OFFLINE, "text/html", "utf-8");
                return true;
            }
        }
        
        @Override
        public void onReceivedError (WebView view, int errorCode, 
            String description, String failingUrl) {
            if (errorCode == ERROR_TIMEOUT) {
            	view.stopLoading();
                view.loadData(TIMEOUT, "text/html", "utf-8");
            } else {
            	view.stopLoading();
                view.loadData(GENERIC, "text/html", "utf-8");
            }
        }
    }

}

