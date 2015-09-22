package de.xorg.gsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InternetViewer extends ActionBarActivity {
	public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
	
	@SuppressWarnings("unused")
	private Context c;
	private boolean isConnected = true;
	private boolean isAktuelles = false;
	private boolean isVertretung = false;
	private boolean isEgg = false;
	private boolean isNtf = false;
	private boolean isRST = false;
	private int PostID = 207;
	public int lastID = 209;
	
	public String UrlToLoad; 
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setThemeUI(this);
		setContentView(R.layout.webviewer);

        Util.setOrientation(this);
	    
	    RelativeLayout widerrist = (RelativeLayout) findViewById(R.id.withers);
		widerrist.setBackgroundColor(Color.parseColor("#fed21b"));
	    
		Intent intent = getIntent();	    
		String titleD = intent.getStringExtra(MainActivity.EXTRA_NAME);
	    final String title = intent.getStringExtra(MainActivity.EXTRA_NAME).replace(".", "").replace(",", "").replace("*", "");
	    
	    if(titleD.startsWith(".")) {
	    	isAktuelles = true;
	    }
	    
	    if(titleD.startsWith(",")) {
	    	isNtf = true;
	    }
	    
	    if(titleD.startsWith("*")) {
	    	isRST = true;
	    }
	    
	    if(title.contains("Vertretungsplan")) {
	    	isVertretung = true;
	    }
	    
	    if(titleD.equalsIgnoreCase("EasterEgg")) {
	    	isEgg = true;
	    }
	    
	    if(isAktuelles) {
	    	
	    }
	    
	    c = this;
	    
	    isConnected = Util.hasInternet(getApplicationContext());
		
		WebView Firefox = (WebView) findViewById(R.id.WebView);
		final Activity MyActivity = this;
		
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		Firefox.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)   
            {
                MyActivity.setTitle(title);
                MyActivity.setProgress(progress * 100);
            }
         });
		
		if(isNtf) {
			NotificationManager mNotificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(0);
		}

 		
		Firefox.setWebViewClient(new MyWebViewClient() );
		
		Firefox.getSettings().setJavaScriptEnabled(false);
		Firefox.getSettings().setBuiltInZoomControls(true);

			Firefox.setBackgroundColor(Color.parseColor("#fed21b"));
        		
		try {
			if(!(PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "").equals(""))) {
				Firefox.setInitialScale(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "")));
			}
		} catch (Exception ex) {

		}
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			Firefox.getSettings().setDisplayZoomControls(false);
		}
	    
	    String message = intent.getStringExtra(MainActivity.EXTRA_URL);
	    
	    if(isEgg) {
	    	openUrl("http://tslink.tk/supersecret");
	    }
		
	    if(!(message.equals(""))) {
	    	if(isAktuelles) {
	    		
	    		new GetLastPost().execute();
	    		
	    		UrlToLoad = message;
	    		Firefox.loadUrl(message + PostID);
	    	} else if(isVertretung) {
	    		if(isConnected) {
	    	    	new GetDate2(this).execute();
	    	    } else {
	    	    	Toast.makeText(this, "Automatische Vertretungsplan-Überprüfung wurde unterbrochen\nweil keine Internetverbindung besteht.\nDas Datum des schon angesehenen Vertretungsplans wird nicht aktualisiert!", Toast.LENGTH_LONG).show();
	    	    }
	    		UrlToLoad = message;
			    Firefox.loadUrl(message);
	    	} else {
	    		UrlToLoad = message;
			    Firefox.loadUrl(message);
	    	}
	    } else {
	    	
	    }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		if(isAktuelles) {
			getMenuInflater().inflate(R.menu.aktuelles, menu);
		} else {
			getMenuInflater().inflate(R.menu.webviewer, menu);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(isAktuelles) {
			switch (item.getItemId()) {
		    case R.id.refresh:
		         openUrl(UrlToLoad + PostID);
		         return true;
		    case R.id.action_kback:
		    	 if(PostID == 2) {
		    		 Toast.makeText(this, "Fehler: Dies ist der erste Post!", Toast.LENGTH_LONG).show();
		    	 } else {
		    		 PostID = PostID - 1;
		    		 openUrl(UrlToLoad + PostID);
		    	 }
		    	 return true;
		    case R.id.action_kfwd:
		    	 if(PostID == lastID) {
		    		 Toast.makeText(this, "Fehler: Dies ist der letzte Post!", Toast.LENGTH_LONG).show();
		    	 } else {
		    		 PostID = PostID + 1;
		    		 openUrl(UrlToLoad + PostID);
		    	 }
		    	 return true;
		    default:
		         return super.onOptionsItemSelected(item);
		    }
		} else {
			switch (item.getItemId()) {
		    case R.id.refresh:
		         openUrl(UrlToLoad);
		         return true;
		    default:
		         return super.onOptionsItemSelected(item);
		    }
		}
	}
	
	@Override
	public void onBackPressed() {
		if(isRST) {
			Intent mStartActivity = new Intent(this, MainActivity.class);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
			System.exit(0);
		}
        
        super.onBackPressed();
        return;
	}
	
	public void openUrl(String url) {
		WebView Firefox = (WebView) findViewById(R.id.WebView);
		Firefox.loadUrl(url);
	}
	
    private class MyWebViewClient extends WebViewClient {
    	final String OFFLINE = "<html><head></head><body bgcolor='#fed21b'><h2>Sie sind offline</h2><br><b>Es ist keine Internetverbindung verf&uuml;gbar! :(</b></body></html>";
    	final String TIMEOUT = "<html><head></head><body bgcolor='#fed21b'><h2>TIMEOUT</h2><br><b>Der Server braucht zu lange,<br>um eine Antwort zu senden :(<br><br><small>URL: $URL</b></body></html>";
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
                view.loadData(TIMEOUT.replace("$URL", failingUrl), "text/html", "utf-8");
            } else {
            	view.stopLoading();
                view.loadData(GENERIC, "text/html", "utf-8");
            }
        }
    }
	
	private class GetLastPost extends AsyncTask<String, Void, String> {
		GetLastPost(){
		    
		}
		
		private ProgressDialog progressDialog;
		
		@Override  
        protected void onPreExecute()  
        {  
            //Create a new progress dialog  
            progressDialog = new ProgressDialog(InternetViewer.this);  
            //Set the progress dialog to display a horizontal progress bar  
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
            //Set the dialog title to 'Loading...'  
            progressDialog.setTitle("GSApp 4.x »Gino«");
            //Set the dialog message to 'Loading application View, please wait...'  
            progressDialog.setMessage("Lade Daten...");  
            //This dialog can't be canceled by pressing the back key  
            progressDialog.setCancelable(false);  
            //This dialog isn't indeterminate  
            progressDialog.setIndeterminate(true);  
            //Display the progress dialog  
            progressDialog.show();  
        }  

		 protected String doInBackground(String... message) {
		    HttpClient httpclient;
		    HttpGet request;
		    HttpResponse response = null;
		    String result = "";
		    
		    try {
		        httpclient = new DefaultHttpClient();
		        // HTTP parameters stores header etc.
		        HttpParams params = new BasicHttpParams();
		        params.setParameter("http.protocol.handle-redirects",false);
		        request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/aktuelles.php5");
		        request.setParams(params);
		        response = httpclient.execute(request);
		    }

		    catch (Exception e) {
		        result = "E";
		    }

		    try {
		        BufferedReader rd = new BufferedReader(new InputStreamReader(
		                response.getEntity().getContent()));
		        String line = "";
		        while ((line = rd.readLine()) != null) {
		            result = result + line ;
		        }
		    } catch (Exception e) {
		        result = "error";
		    }
		    return result;
		}

		protected void onPostExecute(String result) {
			progressDialog.dismiss(); 
			if(result != "E") {
				String gStr = result.split("<div id=\"InhaltAkt\">")[1];
				Log.d("GSApp-Gino", "+" + gStr + "+");
				String[] rawC = gStr.split("\n");
				String GLINE = rawC[0].replace("  <iframe src=\"", "").replace("\" id=\"inhframeAkt\" name=\"frmakt\" frameborder=\"0\" scrolling=\"no\"></iframe>", "").replace("Aktuell/ausgeben.php5?id=", "").split("</div>")[0];
				lastID = Integer.parseInt(GLINE);
				PostID = Integer.parseInt(GLINE);
				openUrl(UrlToLoad + GLINE);
			} else {
				Toast.makeText(InternetViewer.this, "Warnung: Datenabruf fehlgeschlagen - Applet funktioniert nicht korrekt!", Toast.LENGTH_SHORT).show();
			}
			//char gf = (char) 34;
			//String doof = "<script type=" + gf + "text/javascript" + gf + " src=" + gf + "http://stats.hosting24.com/count.php" + gf + "></script>";
			//String[] results = result.split("\n");
			//String Wresult = results[0].replace("$NL", "\n").replace("<!-- Hosting24 Analytics Code -->", "").replace(doof, "").replace("<!-- End Of Analytics Code -->", "");
			
			
		}

		}	
	
}

class GetDate2 extends AsyncTask<String, Void, String> {
	Context cont;
	GetDate2(Context ct){
	    this.cont = ct;
	}

	 protected String doInBackground(String... message) {
	    HttpClient httpclient;
	    HttpGet request;
	    HttpResponse response = null;
	    String result = "";

	    // Verbindung zum Server mit der "Apache HttpClient Library" aufbauen
	    try {
	        httpclient = new DefaultHttpClient();
	        request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
	        response = httpclient.execute(request);
	    }

	    catch (Exception e) {
	        // Code um Fehler zu behandeln
	        result = "error";
	    }

	    // Serverantwort auswerten
	    try {
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
	                response.getEntity().getContent()));
	        String line = "";
	        while ((line = rd.readLine()) != null) {

	            // Serverantwort auslesen
	            result = result + line ;
	        }
	    } catch (Exception e) {
	        // Code um Fehler zu behandeln
	        result = "error";
	    }
	    return result;
	}

	protected void onPostExecute(String result) {
		try {
			//Serverantwort (Datum des verfügbarem Vertretungsplan)
			char gf = (char) 34;
            GALog loc = new GALog(cont);
			if(result != "E") {
				String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
				String gDate = gPart.replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "")/*.split(".")*/;
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
				DateFormat RC = new SimpleDateFormat("yyyyMMdd");
				Date dt = df.parse(gDate);
				String GRC = RC.format(dt);
                loc.debug("IntView/GetDate2: Datum von Server erhalten: +" + GRC + "+");
				Editor editor = PreferenceManager.getDefaultSharedPreferences(cont).edit();
			        editor.putString("readDate", GRC);
			        editor.commit();
			}
		} catch(Exception ex) {
			//Allgemeiner Fehler beim Auswerten
            new GALog(cont).error("IntView/GetDate2: Fehler beim Auswerten: " + ex.getMessage());
		}
	}

}