package de.xorg.gsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
//import android.support.v4.app.Fragment;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.xorg.cardsuilib.objects.CardStack;
import de.xorg.cardsuilib.views.CardUI;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class VPlanFragment extends Fragment {
    public static final String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
    private String Filter = null;
    //private Context c;
    public Activity actv = null;
    public String cBiologie = "#4caf50";
    public String cChemie = "#e91e63";
    public String cDeutsch = "#2196F3";
    public String cEnglisch = "#ff9800";
    public String cEthik = "#ff8f00";
    public String cFRL = "#558b2f";
    public String cGeografie = "#9e9d24";
    public String cGeschichte = "#9c27b0";
    public String cInformatik = "#03a9f4";
    public String cKunst = "#673ab7";
    public String cMNT = "#4caf50";
    public String cMathe = "#f44336";
    public String cMusik = "#9e9e9e";
    public String cPhysik = "#009688";
    public String cReligion = "#ff8f00";
    public String cSozialkunde = "#795548";
    public String cSport = "#607d8b";
    public String cWirtschaftRecht = "#ff5722";
    private String dateD = "unbekannt";
    long debugStart;
    long appStart;
    public boolean fallback = false;
    private String hinweisD = "kein Hinweis";
    private boolean isFiltered = false;
    private CardUI mCardView;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeContainer;
    public Eintrage vplane;
    private String htmlSource = "NULL";
    public boolean showingAll = false;
    public boolean istDunkel = false;
    String themeId = Util.AppTheme.LIGHT;
    boolean cardMarquee;




    //TODO: Keine Internetverbindung behandeln!
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_svplan, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appStart = System.currentTimeMillis();
        if (getArguments() != null && getArguments().containsKey("theme")) {
            themeId = getArguments().getString("theme");
            istDunkel = (themeId.equals(Util.AppTheme.DARK));
        }

        swipeContainer = (SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                VPlanFragment.this.vplane.clear();
                VPlanFragment.this.magic(true);
            }
        });
        swipeContainer.setColorSchemeResources(new int[]{R.color.md_cyan_A200, R.color.md_light_green_A400, R.color.md_amber_300, R.color.md_red_A400});
        Filter = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("klasse", "");
        cardMarquee = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Util.Preferences.MARQUEE, false);


        //Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide);
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
        mCardView = (CardUI) getView().findViewById(R.id.cardsview);
        mCardView.setAnimation(anim);
        mCardView.setSwipeable(false);
        mCardView.getScrollView().setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(mCardView.getScrollView().scrollYIsComputed()) {
                    int CSY = 0;
                    try {
                        CSY = mCardView.getScrollView().getComputedScrollY();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    if(CSY == 0) {
                        VPlanFragment.this.swipeContainer.setEnabled(true);
                    } else {
                        VPlanFragment.this.swipeContainer.setEnabled(false);
                    }
                } else {
                    VPlanFragment.this.swipeContainer.setEnabled(false);
                }
            }
        });
        this.vplane = new Eintrage();
        magic(false);

    }

    public void loadOk(boolean showDialog, boolean checkCache) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(20, TimeUnit.SECONDS);
        b.connectTimeout(20, TimeUnit.SECONDS);

        OkHttpClient client = b.build();

        System.setProperty("http.keepAlive", "false");
        if (showDialog) {
            VPlanFragment.this.progressDialog = new ProgressDialog(VPlanFragment.this.getContext());
            VPlanFragment.this.progressDialog.setProgressStyle(0);
            VPlanFragment.this.progressDialog.setTitle("GSApp");
            VPlanFragment.this.progressDialog.setMessage("Lade Daten...");
            VPlanFragment.this.progressDialog.setCancelable(false);
            VPlanFragment.this.progressDialog.setIndeterminate(true);
            VPlanFragment.this.progressDialog.show();
        }

        Request request = new Request.Builder()
                .url(VPlanFragment.this.getURL())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e(e);
                if(VPlanFragment.this.getActivity() == null)
                    return;

                VPlanFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(e.getMessage().contains("timeout")) {
                            Toast.makeText(VPlanFragment.this.getContext(), "Der Vertretungsplan konnte nicht neu geladen werden, da die Verbindung zum Server zu lang gedauert hat!", Toast.LENGTH_SHORT).show(); //TODO
                        } else {
                            Toast.makeText(VPlanFragment.this.getContext(), "Der Vertretungsplan konnte nicht neu geladen werden!", Toast.LENGTH_SHORT).show();
                        }


                        if (showDialog && VPlanFragment.this.progressDialog != null) {
                            VPlanFragment.this.progressDialog.dismiss();
                        }
                        if (swipeContainer != null && swipeContainer.isRefreshing()) {
                            swipeContainer.setRefreshing(false);
                        }

                        if(mCardView.getChildCount() < 2) {
                            mCardView.clearCards();
                            mCardView.addCard(new MyPlayCard(istDunkel,"Interner Fehler", "Es ist ein Fehler beim Herunterladen des Vertretungsplans aufgetreten!", "#FF0000", "#FF0000", true, false, false, cardMarquee));
                            Toast.makeText(getContext(), "Vertretungsplan konnte nicht angezeigt werden, zeige im Browser..", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), WebViewActivity.class);
                            intent.putExtra(EXTRA_URL, "https://www.gymnasium-sonneberg.de/Informationen/vp.php5");
                            intent.putExtra(Util.EXTRA_NAME, "[!] Vertretungsplan [!]");
                            getContext().startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful())
                    Timber.e("onResponse FAILED (" + response.code() + ")");
                final String result = response.body().string();

                if(VPlanFragment.this.getActivity() == null)
                    return;

                VPlanFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Timber.d("Got response after " + (System.currentTimeMillis() - appStart) + "ms");
                        if(checkCache) {
                            if(!hasCacheChanged(result)) {
                                Timber.d( "Cache has not changed");
                                return;
                            } else {
                                Timber.d("Cache HAS changed...");
                            }
                        } else {
                            Timber.d("Not checking cache...");
                        }


                        VPlanFragment.this.vplane.clear();
                        saveToHtmlCache(result);

                        try {
                            VPlanFragment.this.parseResponse(result);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            try {
                                VPlanFragment.this.fallbackLoad(result);
                            } catch(Exception exo) {
                                Timber.d( "Failed on fallbackLoad: " + exo.getMessage());
                                Timber.e(exo);
                                exo.printStackTrace();
                            }

                        } catch(Exception e) {
                            Timber.d( "Failed on ParseResponse: " + e.getMessage());
                            Timber.e(e);
                            e.printStackTrace();
                        }


                        if (showDialog && VPlanFragment.this.progressDialog != null) {
                            VPlanFragment.this.progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    public void doRefresh() {
        Toast.makeText(VPlanFragment.this.getContext(), "Refresh!", Toast.LENGTH_SHORT).show();
        if (swipeContainer != null)
            swipeContainer.setRefreshing(true);
        magic(true);
    }

    //toobar

    public void magic(boolean isRefresh) {
        Timber.d("Loading start after " + (System.currentTimeMillis() - appStart) + "ms");
        if (Util.hasInternet(getContext())) {
            //this.mCardView.clearCards();
            Filter = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Util.Preferences.KLASSE, "");
            //if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("loadAsync", false)) {
                if (!isRefresh) {
                    boolean cacheState = loadFromHtmlCache();
                    Timber.d("Cache loaded after " + (System.currentTimeMillis() - appStart) + "ms");
                    loadOk(!cacheState, cacheState);
                } else {
                    loadOk(false, false);
                }
            //} else {
            //    loadSynced(false);
            //}
        } else {
            if(!loadFromHtmlCache()) {
                this.mCardView.clearCards();
                this.mCardView.addCard(new MyPlayCard(istDunkel, "Fehler", "Es besteht keine Internetverbindung und es wurde noch kein Vertretungsplan zwischengespeichert!", "#FF0000", "#FF0000", true, false, false, cardMarquee));
                this.mCardView.refresh();
            } else {
                Toast.makeText(getContext(), "Vertretungsplan aus dem Zwischenspeicher geladen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all:
                /*if(this.showingAll) {
                    this.showingAll = false;
                    VPlanFragment.this.vplane.clear();
                    loadFromHtmlCache();
                } else {
                    this.showingAll = true;
                    VPlanFragment.this.vplane.clear();
                    loadFromHtmlCache();
                }*/

                return true;
            /*case R.id.show_src:
                AlertDialog.Builder alert = new AlertDialog.Builder(VPlanFragment.this.getContext());

                alert.setTitle("HTML-Code");
                alert.setMessage(this.htmlSource);

                alert.setCancelable(true);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Util.prepareMenu(menu, Util.NavFragments.VERTRETUNGSPLAN);
        //if(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("klasse", "").isEmpty())
        //menu.findItem(R.id.show_all).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    public void parseResponse(String result) throws ArrayIndexOutOfBoundsException {
        this.isFiltered = !this.Filter.isEmpty();
        if(this.showingAll)
            this.isFiltered = false;
        this.htmlSource = result;
        Document doc = Jsoup.parse(result);
        Element date = doc.select("td[class*=vpUeberschr]").first();
        Element bemerk = doc.select("td[class=vpTextLinks]").first();
        String bemerkText;
        String dateText;

        try {
            if(bemerk.hasText()) { bemerkText = bemerk.text().replace("Hinweis:", "").trim(); } else { Timber.d("ParseResponse: bemerk has no text"); bemerkText = ""; }
        } catch(Exception e) {
            bemerkText = "";
            e.printStackTrace();
        }

        try {
            if(date.hasText()) { dateText = date.text(); } else { Timber.d( "ParseResponse: date has no text"); dateText = "Fehler, den 01.01.2000"; }
        } catch(Exception e) {
            dateText = "CFehler, den 01.01.2000";
        }


        if (dateText.equals("Beschilderung beachten!")) {
            Toast.makeText(getContext(), "Es sind Ferien!", Toast.LENGTH_SHORT).show();
            return;
        }
        this.dateD = dateText;
        this.hinweisD = bemerkText.replace("Hinweis: ", "");


        Elements vpEnts = doc.select("tr[id=Svertretungen], tr[id=Svertretungen] ~ tr");
        if(vpEnts.size() < 1) {
            Timber.d( "ParseResponse: No entries found, trying fallback method...");
            Element par = null;
            try {
                par = doc.select("td[class*=vpTextZentriert]").first().parent();
            } catch(NullPointerException npe) {
                Timber.e("ParseResponse: Fallback failed, html is shown below: ");
                Timber.e(result);
            }
            vpEnts = doc.select(par.cssSelector() + ", " + par.cssSelector() + " ~ tr");
        }



        Timber.d( "vpEnts size is " + vpEnts.size());
        Iterator it;
        Elements d;
        String[] data;
        boolean isNew;
        int dID;
        Iterator it2;
        if (this.isFiltered) {
            it = vpEnts.iterator();
            while (it.hasNext()) {
                d = ((Element) it.next()).children();
                data = new String[7];
                isNew = false;
                dID = 0;
                it2 = d.iterator();
                while (it2.hasNext()) {
                    data[dID] = ((Element) it2.next()).text();
                    dID++;
                }
                if (d.html().contains("<strong>")) {
                    isNew = true;
                }
                if (Util.applyFilter(getContext(), data)) {
                    vplane.add(new Eintrag(data[0], data[1], data[2], data[3], data[4], data[5], data[6], isNew));
                }
            }
        } else {
            it = vpEnts.iterator();
            while (it.hasNext()) {
                d = ((Element) it.next()).children();
                data = new String[7];
                isNew = false;
                dID = 0;
                it2 = d.iterator();
                while (it2.hasNext()) {
                    data[dID] = ((Element) it2.next()).text();
                    dID++;
                }
                if (d.html().contains("<strong>")) {
                    isNew = true;
                }
                this.vplane.add(new Eintrag(data[0], data[1], data[2], data[3], data[4], data[5], data[6], isNew));
            }
        }
        displayAll();
    }

    private void fallbackLoad(String result) {
        String Klasse = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Util.Preferences.KLASSE, "");
        if(this.showingAll)
            Klasse = "";
        try {
            if (result != "E") {
                dateD = result.split("<td colspan=\"7\" class=\"rundeEckenOben vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
                hinweisD = "[!] " + result.split("<tr id=\"Shinweis\">")[1].split("</tr>")[0].replace("Hinweis: <br />","").replace("<br />", "· ").replaceAll("\\<.*?>", "").replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß").replaceAll("[\\\r\\\n]+", "").trim();
            }
        } catch (Exception e) {
            this.hinweisD = "[!] kein Hinweis";
            Timber.d( "Fehler beim Auswerten der Informationen");
        }
        if (result != "E") {
            String[] newC = clearUp(result.split("<td class=\"vpTextZentriert\">", 2)[1].split("\n")).split("\n");
            int counter = 1;
            int va = 0;
            String klasse = "";
            String stunde = "";
            String orgfach = "";
            String vertret = "";
            String raum = "";
            String verfach = "";
            String str = "";
            for (String cnt : newC) {
                if (counter == 1) {
                    klasse = cnt;
                    counter++;
                } else if (counter == 2) {
                    stunde = cnt;
                    counter++;
                } else if (counter == 3) {
                    orgfach = cnt;
                    counter++;
                } else if (counter == 4) {
                    vertret = cnt;
                    counter++;
                } else if (counter == 5) {
                    raum = cnt;
                    counter++;
                } else if (counter == 6) {
                    verfach = cnt;
                    counter++;
                } else if (counter == 7) {
                    str = cnt;
                    counter = 1;
                    if (Klasse.equals("")) {
                        isFiltered = false;
                        displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, str);
                        va++;
                        klasse = "";
                        stunde = "";
                        orgfach = "";
                        vertret = "";
                        raum = "";
                        verfach = "";
                        str = "";
                    } else {
                        isFiltered = true;
                        String skl = String.valueOf(klasse.charAt(0));
                        String SUCL = klasse.replace("/2", " " + skl + ".2");
                        SUCL = SUCL.replace("/3", " " + skl + ".3");
                        SUCL = SUCL.replace("/4", " " + skl + ".4");
                        SUCL = SUCL.replace("/5", " " + skl + ".5");
                        if (SUCL.length() == 1) {
                            if (Klasse.startsWith(SUCL)) {
                                displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, str);
                                va++;
                            }
                        } else if (SUCL.contains(Klasse)) {
                            displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, str);
                            va++;
                        }
                    }
                }
            }
            displayAll();
            return;
        }
        mkMsg("Space error :(");
    }

    private void displayStuff(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {

        vplane.add(new Eintrag(klasse, stunde, fachnormal, vertretung, raum, fachvertret, bemerkung));
    }

    private String getURL() {
        int mode = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("debugSrc", 0);
        String URL = null;
        switch (mode) {
            case 0:
                URL = "https://www.gymnasium-sonneberg.de/Informationen/vp.php5";
                break;
            case 1:
                URL = "https://gsapp.xorg.ga/debug/vp.html";
                break;
            case 2:
                URL = "https://gsapp.xorg.ga/debug/vp2.html";
                break;
            case 3:
                URL = "https://gsapp.xorg.ga/debug/vp3.html";
                break;
            default:
                URL = "https://www.gymnasium-sonneberg.de/Informationen/vp.php5";
                break;
        }

        return URL;
    }

    private void displayAll() {
        mCardView.clearCards();
        CardStack dateHead = new CardStack(istDunkel);
        dateHead.setTypeface(Util.getTKFont(this.getContext(), false));
        dateHead.setTitle("Für " + dateD);
        mCardView.addStack(dateHead);

        //if(!hinweisD.equals("Hinweis:")) {
        Timber.d("Hinweis= +" + hinweisD + "+");
        if(!hinweisD.isEmpty()) {
            MyPlayCard card = new MyPlayCard(istDunkel,"Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false, false, cardMarquee);
            card.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(VPlanFragment.this.getContext()).create();
                    ad.setCancelable(true);
                    ad.setTitle("Hinweis");
                    ad.setMessage(hinweisD);

                    ad.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }

            });
            mCardView.addCard(card);
        }

        try {
            if(isFiltered) {
                CardStack stacky = new CardStack(istDunkel);
                stacky.setTypeface(Util.getTKFont(this.getContext(), false));
                stacky.setTitle("Vertretungen für Klasse " + Filter);
                mCardView.addStack(stacky);
            }
            for(String klassee : vplane.getKlassen()) {
                if(!isFiltered) {
                    CardStack stacky = new CardStack(istDunkel);
                    stacky.setTypeface(Util.getTKFont(this.getContext(), false));
                    stacky.setTitle("Klasse " + klassee);
                    mCardView.addStack(stacky);
                }
                final ArrayList<Eintrag> tc = vplane.getKlasseGruppe(klassee, !isFiltered);
                for(final Eintrag single : tc) {
                    MyPlayCard card;
                    String note = "";
                    if(single.getKlasse().contains("STK")) {
                        String Stammkurs = "";
                        try {
                            Stammkurs = single.getKlasse().split("STK")[1].trim();
                        } catch(Exception e) { }

                        note = " (STK" + Stammkurs + ")";
                    }

                    if(single.getKlasse().matches("([0-9]{2}[A-Z]+[0-9]{1})+")) {
                        note = " (" + single.getKlasse().replaceAll("([0-9]{2})+", "") + ")";
                    }

                    if(single.getBemerkung().equals("Ausfall")) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde - Ausfall!" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else if(single.getBemerkung().equals("Stillbesch.") || single.getBemerkung().equals("Stillbeschäftigung")) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde - Stillbesch.!" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else if(single.getBemerkung().equals("AA") || single.getBemerkung().equals("Arbeitsauftrag")) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else if(single.getFachNormal().equals(single.getFachVertretung())) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde" + note, "Du hast " + LongName(single.getFachNormal()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + "." + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + "." + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    }
                    if(isFiltered) {
                        card.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                displayMoreInformation(single);
                            }

                        });
                        mCardView.addCard(card);
                    } else {
                        card.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                if(tc.size() > 1) {
                                    displaySingleClass(single.getKlasse());
                                } else {
                                    displayMoreInformation(single);
                                }

                            }

                        });
                        mCardView.addCardToLastStack(card);
                    }
                }
            }
        } catch (KeineKlassenException e) {
            mCardView.addCard(new MyPlayCard(istDunkel,"Keine Vertretungen", "", cMNT, cMNT, false, false, false, cardMarquee));
            e.printStackTrace();
        } catch (KeineEintrageException e) {
            mCardView.addCard(new MyPlayCard(istDunkel,"ERROR", "KeineEinträgeException", "#FF0000", "#FF0000", false, false, false, cardMarquee));
            e.printStackTrace();
        }

        CardStack adStack = new CardStack(istDunkel);
        adStack.setTypeface(Util.getTKFont(this.getContext(), false));
        adStack.setTitle("Werbung zur Serverfinanzierung");
        mCardView.addStack(adStack);
        mCardView.addCardToLastStack(new AdCard(this.getContext(), false, false));
        mCardView.refresh();
        mCardView.getScrollView().computeScrollY();

        Timber.d("gcc: " + mCardView.getChildCount());

        if (swipeContainer != null && swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }

        saveToCache();

        Timber.d("Displayed after " + (System.currentTimeMillis() - appStart) + "ms");
    }

    private void displaySingleClass(String klasse) {
        //final Dialog scv = new Dialog(getContext(), R.style.FragDialog);
        //scv.setContentView(R.layout.cards_dialog);
        //CardUI dcv = (CardUI) scv.findViewById(R.id.cv_dialog);
        //LayoutParams lp = new LayoutParams();
        //lp.copyFrom(scv.getWindow().getAttributes());
        //lp.width = -1;
        //lp.height = -1;
        VPDetailSheet vds = new VPDetailSheet();
        ArrayList<Eintrag> inp = null;
        try {
            inp = vplane.getKlasseGruppeS(klasse);
        } catch (KeineEintrageException e) {
            e.printStackTrace();
            return;
        }

        Bundle bundl = new Bundle();
        bundl.putString("date", dateD);
        bundl.putString("hinweis", hinweisD);
        bundl.putString("theme", themeId);
        bundl.putSerializable("input", inp);
        bundl.putString("klasse", klasse);

        vds.setArguments(bundl);

        vds.show(getFragmentManager(), vds.getTag());

        return;

        /*CardStack dateHead = new CardStack();
        dateHead.setTitle("Für " + dateD);
        //dcv.addStack(dateHead);
        vds.addStack(dateHead);

        if(!hinweisD.equals("Hinweis:")) {
            MyPlayCard card = new MyPlayCard("Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false);
            card.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(VPlanFragment.this.getContext()).create();
                    ad.setCancelable(true);
                    ad.setTitle("Hinweis");
                    ad.setMessage(hinweisD);

                    ad.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }

            });
            mCardView.addCard(card);
        }

        try {
            CardStack stackPlay = new CardStack();
            stackPlay.setTitle("Vertretungen für Klasse " + klasse);
            vds.addStack(stackPlay);

            for(final Eintrag single : vplane.getKlasseGruppeS(klasse)) {
                MyPlayCard card;
                String note = "";
                if(single.getKlasse().contains("STK")) {
                    String Stammkurs = "";
                    try {
                        Stammkurs = single.getKlasse().split("STK")[1].trim();
                    } catch(Exception e) { }

                    note = " (STK" + Stammkurs + ")";
                }

                if(single.getBemerkung().equals("Ausfall")) {
                    card = new MyPlayCard(single.getStunde() + ". Stunde - Ausfall!" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu());
                } else if(single.getBemerkung().equals("Stillbesch.")) {
                    card = new MyPlayCard(single.getStunde() + ". Stunde - Stillbesch.!" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu());
                } else if(single.getBemerkung().equals("AA")) {
                    card = new MyPlayCard(single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu());
                } else {
                    card = new MyPlayCard(single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu());
                }
                card.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        displayMoreInformation(single);
                    }

                });
                vds.addCard(card);
            }

            vds.refreshUI();
            vds.computeUI();
        } catch (KeineEintrageException e) {
            e.printStackTrace();
        }*/
    }

    private void displayMoreInformation(Eintrag eintrag) {
        AlertDialog ad = new AlertDialog.Builder(getContext()).create();
        ad.setCancelable(true); // This blocks the 'BACK' button
        ad.setTitle("Information");
        String note = "";
        if(eintrag.getKlasse().contains("STK")) {
            String Stammkurs = "";
            try {
                Stammkurs = eintrag.getKlasse().split("STK")[1].trim();
            } catch(Exception e) { }

            note = "\n\nBetrifft nur Stammkurs " + Stammkurs + "!";
        }


        if(eintrag.getBemerkung().equals("Ausfall")) {
            ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Ausfall (Raum " + eintrag.getRaum() + ")" + note);
        } else if(eintrag.getBemerkung().equals("Stillbesch.")) {
            ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + eintrag.getRaum() + note);
        } else if(eintrag.getBemerkung().equals("AA")) {
            ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + eintrag.getRaum() + note);
        } else {
            ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du " + LongName(eintrag.getFachVertretung()) + " bei " + eintrag.getVertretung() + " im Raum " + eintrag.getRaum() + "\n\nBemerkung: " + eintrag.getBemerkung() + note);
        }

        ad.setButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    private String getFachColor(String fach) {
        String col = cSport;
        switch(fach.toLowerCase()) {
            case "de":
                col= cDeutsch;
                break;
            case "ma":
                col = cMathe;
                break;
            case "mu":
                col = cMusik;
                break;
            case "ku":
                col = cKunst;
                break;
            case "gg":
                col = cGeografie;
                break;
            case "re":
                col = cReligion;
                break;
            case "et":
                col = cEthik;
                break;
            case "mnt":
                col = cMNT;
                break;
            case "en":
                col = cEnglisch;
                break;
            case "sp":
                col = cSport;
                break;
            case "spj":
                col = cSport;
                break;
            case "spm":
                col = cSport;
                break;
            case "bi":
                col = cBiologie;
                break;
            case "ch":
                col = cChemie;
                break;
            case "ph":
                col = cPhysik;
                break;
            case "sk":
                col = cSozialkunde;
                break;
            case "if":
                col = cInformatik;
                break;
            case "wr":
                col = cWirtschaftRecht;
                break;
            case "ge":
                col = cGeschichte;
                break;
            case "ru":
                col = cFRL;
                break;
            case "la":
                col = cFRL;
                break;
            case "sn":
                col = cFRL;
                break;
            case "fr":
                col = cFRL;
                break;
            default:
                col = cSport;
                break;
        }
        return col;
    }

    private String LongName(String fach) {
        switch(fach.toLowerCase()) {
            case "de":
                return "Deutsch";
            case "ma":
                return "Mathe";
            case "mu":
                return "Musik";
            case "ku":
                return "Kunst";
            case "gg":
                return "Geografie";
            case "re":
                return "Religion";
            case "et":
                return "Ethik";
            case "mnt":
                return "MNT";
            case "en":
                return "Englisch";
            case "sp":
                return "Sport";
            case "spj":
                return "Sport Jungen";
            case "spm":
                return "Sport Mädchen";
            case "bi":
                return "Biologie";
            case "ch":
                return "Chemie";
            case "ph":
                return "Physik";
            case "sk":
                return "Sozialkunde";
            case "if":
                return "Informatik";
            case "wr":
                return "Wirtschaft/Recht";
            case "ge":
                return "Geschichte";
            case "fr":
                return "Französisch";
            case "ru":
                return "Russisch";
            case "la":
                return "Latein";
            case "sn":
                return "Spanisch";
            case "gewi":
                return "Gesellschaftswissenschaften";
            case "as":
                return "Astronomie";
            case "&nbsp;":
                return "keine Angabe";
            default:
                return fach;

        }
    }

    private String clearUp(String[] inpud) {
        char gf = (char) 34;
        String me = "";
        for(String ln : inpud) {
            ln = ln.replaceAll("\\<.*?>","");
            ln = ln.replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß");
            ln = ln.replace("                        ", "");
            ln = ln.trim();
            ln = ln.replace("	", "");

            if(ln.equals("      ")) {
            } else if(ln.equals("var hoehe = parent.document.getElementById('inhframe').style.height;")) {
            } else if(ln.equals("setFrameHeight();")) {
            } else if(ln.equals("var pageTracker = _gat._getTracker(" + gf + "UA-5496889-1" + gf + ");")) {
            } else if(ln.equals("pageTracker._trackPageview();")) {
            } else if(ln.equals("    ")) {
            } else if(ln.equals("	")) {
            } else if(ln.equals("  ")) {
            } else if(ln.startsWith("var")) {
            } else if(ln.startsWith("document.write")) {
            } else if(ln.equals("")) {
                //} else if(ln.endsWith("&nbsp;")) {
                //	me = me + "XXXX\n";
            } else {
                if (ln.matches(".*\\w.*")) {
                    me = me + ln + "\n";
                } else if(ln.contains("##")) {
                    me = me + ln + "\n";
                }
            }
        }

        return me;
    }

    public void mkMsg(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        Timber.d(msg);
    }

    public void saveToCache() {
        try {
            File outputFile = new File(getContext().getCacheDir(), "vertretung.gxcache");
            JSONObject jk = new JSONObject();
            JSONArray jo = new JSONArray();
            for (int i = 0; i < this.vplane.size(); i++) {
                Eintrag single = (Eintrag) this.vplane.get(i);
                JSONObject so = new JSONObject();
                so.put("Klasse", single.getKlasse());
                so.put("Stunde", single.getStunde());
                so.put("Fachnormal", single.getFachNormal());
                so.put("Vertretung", single.getVertretung());
                so.put("Raum", single.getRaum());
                so.put("Fachvertret", single.getFachVertretung());
                so.put("Bemerkung", single.getBemerkung());
                jo.put(so);
            }
            jk.put("et", jo);
            jk.put("cDate", this.dateD);
            Files.write(jk.toString(), outputFile, Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasCacheChanged(String html) {
        try {
            File hcache = new File(getContext().getCacheDir(), "vpl.fbcache");

            if(!hcache.exists()) {
                Timber.d( "No HTML Cache found!");
                return true;
            }

            if(Files.toString(hcache, Charsets.UTF_8).toLowerCase().equals(html.toLowerCase())) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public void saveToHtmlCache(String html) {
        try {
            File outputFile = new File(getContext().getCacheDir(), "vpl.fbcache");
            Files.write(html, outputFile, Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean loadFromHtmlCache() {
        try {
            File hcache = new File(getContext().getCacheDir(), "vpl.fbcache");

            if(!hcache.exists()) {
                Timber.d( "No HTML Cache found!");
                return false;
            }

            String html = Files.toString(hcache, Charsets.UTF_8);
            try {
                Timber.d("Loading from cache (M1)");
                VPlanFragment.this.parseResponse(html);
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                try {
                    Timber.d("Loading from cache (M2)");
                    VPlanFragment.this.fallbackLoad(html);
                    return true;
                } catch(Exception exo) {
                    Timber.d( "Failed on fallbackLoad: " + exo.getMessage());
                    Timber.e(exo);
                    exo.printStackTrace();
                    return false;
                }

            } catch(Exception e) {
                Timber.d( "Failed on ParseResponse: " + e.getMessage());
                Timber.e(e);
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Timber.e(e);
            e.printStackTrace();
            return false;
        }
    }

    public void readFromCache() {
        try {
            this.vplane.clear();
            JSONObject jo = new JSONObject(Files.toString(new File(getContext().getCacheDir(), "vertretung.gxcache"), Charsets.UTF_8));
            JSONArray singles = jo.getJSONArray("et");
            for (int i = 0; i < singles.length(); i++) {
                this.vplane.add(new Eintrag(singles.getJSONObject(i)));
            }
            this.dateD = jo.getString("cDate") + " (zwischengesp.)";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
