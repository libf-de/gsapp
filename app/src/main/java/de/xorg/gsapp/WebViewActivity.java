package de.xorg.gsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    WebView wv;
    boolean isConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        wv = findViewById(R.id.WebView);
        isConnected = Util.hasInternet(getApplicationContext());

        wv.setWebViewClient( new MyWebViewClient() );
        wv.getSettings().setJavaScriptEnabled(false);
        wv.getSettings().setBuiltInZoomControls(true);

        Intent intent = getIntent();
        String title = intent.getStringExtra(Util.EXTRA_NAME);
        String url = intent.getStringExtra(Util.EXTRA_URL);

        setTitle(title);
        wv.loadUrl(url);
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
}
