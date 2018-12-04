package de.xorg.gsapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
//import android.support.v4.app.Fragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class TermineFragment extends Fragment {

    String URI;
    private boolean isConnected = true;


    public TermineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Util.setOrientation(this.getActivity());

        //Variablen
        WebView Termine = (WebView) getView().findViewById(R.id.WebView);
        RelativeLayout FragFrm = (RelativeLayout) getView().findViewById(R.id.withers);

        FragFrm.setBackgroundColor(Color.parseColor("#fed21b"));
        Termine.setBackgroundColor(Color.parseColor("#fed21b"));

        Termine.setWebViewClient(new MyWebViewClient() );

        Termine.getSettings().setJavaScriptEnabled(true);
        Termine.getSettings().setBuiltInZoomControls(false);

        isConnected = Util.hasInternet(this.getContext());

        Termine.loadUrl("http://www.gymnasium-sonneberg.de/Informationen/Term/ausgebenK.php5");

        getActivity().setTitle("GSApp - Termine");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eb_refresh:
                openUrl(URI);
                return true;
            case R.id.eb_gotoo:
                return true;
            case R.id.eb_startseite:
                URI = "http://www.schulkueche-bestellung.de/index.php?m=2;0";
                openUrl(URI);
                return true;
            case R.id.eb_bestellen:
                URI = "http://www.schulkueche-bestellung.de/index.php?m=2;2";
                openUrl(URI);
                return true;
            case R.id.eb_plan:
                URI = "http://www.schulkueche-bestellung.de/index.php?m=2;1";
                openUrl(URI);
                return true;
            case R.id.eb_account:
                URI = "http://www.schulkueche-bestellung.de/index.php?m=2;5";
                openUrl(URI);
                return true;
            case R.id.eb_abmelden:
                URI = "http://www.schulkueche-bestellung.de/?a=login/logout";
                openUrl(URI);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Util.prepareMenu(menu, R.id.nav_termine);
        super.onPrepareOptionsMenu(menu);

    }

    public void openUrl(String url) {
        WebView Speisen = (WebView) getView().findViewById(R.id.WebView);
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
