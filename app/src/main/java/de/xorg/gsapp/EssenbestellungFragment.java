package de.xorg.gsapp;

import android.app.ProgressDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EssenbestellungFragment extends Fragment {

    private String URI;
    private boolean isConnected = true;
    private String themeId;
    private WebView Speisen;
    private ProgressDialog progressDialog;
    private String ALOGUSER;
    private String ALOGPASS;
    private boolean autoLogin;


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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() instanceof MainActivity2) ((MainActivity2) getActivity()).setBarTitle("Essenbestellung");

        if (getArguments() != null && getArguments().containsKey("theme")) {
            themeId = getArguments().getString("theme");
        }

        autoLogin = false;


        //Variablen
        Speisen = requireView().findViewById(R.id.WebView);
        RelativeLayout FragFrm = requireView().findViewById(R.id.withers);

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

        ALOGUSER = Datenspeicher.getUser(this.getContext());
        ALOGPASS = Datenspeicher.getPassword(this.getContext());

        autoLogin = (!ALOGUSER.equals("") && !ALOGPASS.startsWith("error"));

        if(ALOGPASS.startsWith("error")) Toast.makeText(this.getContext(), "Fehler in autom. Anmeldung: Entschlüsseln des Passworts fehlgeschlagen", Toast.LENGTH_SHORT).show();

        Speisen.loadUrl("https://www.schulkueche-bestellung.de/de/content/");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.eb_home) {//if(EssenbestellungFragment.this.Speisen != null) Speisen.loadUrl("https://www.schulkueche-bestellung.de/de/content/");
            Speisen.loadUrl("javascript:(function() { document.getElementById('login').value = 'franz'; ;})()");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        Util.prepareMenu(menu, Util.NavFragments.BESTELLUNG);
        super.onPrepareOptionsMenu(menu);

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

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(url.equals("https://www.schulkueche-bestellung.de/de/content/") && autoLogin) view.loadUrl("javascript:(function(){document.getElementById(\"login\").value=\"" + ALOGUSER + "\";document.getElementById(\"password\").value=\"" + ALOGPASS + "\"})();");
        }
    }
}
