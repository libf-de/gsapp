package de.xorg.gsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            findViewById(R.id.WebView_Root).setBackgroundColor(Color.WHITE);
            findViewById(R.id.WebView_Loader).setVisibility(View.VISIBLE);
            findViewById(R.id.WebView).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            findViewById(R.id.WebView_Root).setBackgroundResource(R.drawable.background_splash);
            findViewById(R.id.WebView_Loader).setVisibility(View.GONE);
            findViewById(R.id.WebView).setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError (WebView view, int errorCode,
                                     String description, String failingUrl) {
            findViewById(R.id.WebView_Root).setBackgroundColor(Color.WHITE);
            findViewById(R.id.WebView_Loader).setVisibility(View.GONE);
            findViewById(R.id.WebView).setVisibility(View.VISIBLE);
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
