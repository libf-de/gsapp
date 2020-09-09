package de.xorg.gsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EssenbestellungFragment extends Fragment {

    private String URI;
    private boolean isConnected = true;
    private String themeId;


    public EssenbestellungFragment() { }

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
        if (getArguments() != null && getArguments().containsKey("theme")) {
            themeId = getArguments().getString("theme");
        }


        //Variablen
        WebView Speisen = getView().findViewById(R.id.WebView);
        RelativeLayout FragFrm = getView().findViewById(R.id.withers);

        switch(themeId) {
            case Util.AppTheme.DARK:
                FragFrm.setBackgroundResource(R.color.background_dark);
                Speisen.setBackgroundResource(R.color.background_dark);
                break;
            case Util.AppTheme.LIGHT:
                FragFrm.setBackgroundResource(R.color.background_white);
                Speisen.setBackgroundResource(R.color.background_white);
                break;
            case Util.AppTheme.YELLOW:
                FragFrm.setBackgroundResource(R.color.background_yellow);
                Speisen.setBackgroundResource(R.color.background_yellow);
                break;
        }

        Speisen.setWebViewClient(new MyWebViewClient() );

        Speisen.getSettings().setJavaScriptEnabled(true);
        Speisen.getSettings().setBuiltInZoomControls(false);

        isConnected = Util.hasInternet(this.getContext());

        URI = "https://www.schulkueche-bestellung.de/";

        String ALOGUSER = Datenspeicher.getUser(this.getContext());
        String ALOGPASS = Datenspeicher.getPassword(this.getContext());

        if (ALOGPASS.startsWith("error")) {
            Toast.makeText(this.getContext(), "Fehler in autom. Anmeldung: Entschlüsseln des Passworts fehlgeschlagen", Toast.LENGTH_SHORT).show();
            Speisen.loadUrl(URI);
        } else {
            if (ALOGUSER.equals("")) {
                Speisen.loadUrl(URI);
            } else {
                try {
                    Speisen.postUrl("https://www.schulkueche-bestellung.de/index.php?ear_a=akt_login", ("Login_Name=" + URLEncoder.encode(ALOGUSER, "UTF-8") + "&Login_Passwort=" + URLEncoder.encode(ALOGPASS, "UTF-8")).getBytes()); //TODO: Funktioniert das?
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(this.getContext(), "Automatische Anmeldung fehlgeschlagen (UnsupportedEncoding)", Toast.LENGTH_SHORT).show();
                    Speisen.loadUrl(URI);
                    e.printStackTrace();
                }
            }
        }
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
                URI = "https://www.schulkueche-bestellung.de/index.php?m=2;0";
                openUrl(URI);
                return true;
            case R.id.eb_bestellen:
                URI = "https://www.schulkueche-bestellung.de/index.php?m=2;2";
                openUrl(URI);
                return true;
            case R.id.eb_plan:
                URI = "https://www.schulkueche-bestellung.de/index.php?m=2;1";
                openUrl(URI);
                return true;
            case R.id.eb_account:
                URI = "https://www.schulkueche-bestellung.de/index.php?m=2;5";
                openUrl(URI);
                return true;
            case R.id.eb_abmelden:
                URI = "https://www.schulkueche-bestellung.de/?a=login/logout";
                openUrl(URI);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Util.prepareMenu(menu, Util.NavFragments.BESTELLUNG);
        super.onPrepareOptionsMenu(menu);

    }

    private void openUrl(String url) {
        WebView Speisen = getView().findViewById(R.id.WebView);
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
