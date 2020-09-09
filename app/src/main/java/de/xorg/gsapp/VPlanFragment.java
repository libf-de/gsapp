package de.xorg.gsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.xorg.cardsuilib.objects.CardStack;
import de.xorg.cardsuilib.views.CardUI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class VPlanFragment extends Fragment {
    private static final String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
    private String cMNT = "#4caf50";
    private String dateD = "unbekannt";
    private long appStart;
    private String hinweisD = "kein Hinweis";
    private boolean isFiltered = false;
    private CardUI mCardView;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeContainer;
    private Eintrage vplane;
    private boolean showingAll = false;
    private boolean istDunkel = false;
    private String themeId = Util.AppTheme.LIGHT;
    private boolean cardMarquee;




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

        //MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544/6300978111");
        MobileAds.initialize(getContext(), "ca-app-pub-6538125936915221~2281967739");
        AdView mAdView = getView().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("F42D4035C5B8ABF685658DE77BCB840A")
                .addTestDevice("DD84F3C5FBEDC399E0A6707561EC7323")
                .addTestDevice("ED9E21C114D9DE1A8C0695C4607CD141")
                .build();
        mAdView.loadAd(adRequest);

        swipeContainer = getView().findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(() -> {
            VPlanFragment.this.vplane.clear();
            new Thread(() -> loadData(true)).start();
        });
        swipeContainer.setColorSchemeResources(R.color.md_cyan_A200, R.color.md_light_green_A400, R.color.md_amber_300, R.color.md_red_A400);
        cardMarquee = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Util.Preferences.MARQUEE, false);


        //Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide);
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
        mCardView = getView().findViewById(R.id.cardsview);
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
        new Thread(() -> loadData(false)).start();
        if(getActivity() != null && getActivity() instanceof MainActivity2) ((MainActivity2) getActivity()).setBarTitle("Vertretungsplan");
    }

    /**
     * Zeigt den Lade-Dialog an
     */
    private void showLdDialog() {
        if(VPlanFragment.this.getContext() == null)
            return;
        VPlanFragment.this.progressDialog = new ProgressDialog(VPlanFragment.this.getContext());
        VPlanFragment.this.progressDialog.setProgressStyle(0);
        VPlanFragment.this.progressDialog.setTitle("GSApp");
        VPlanFragment.this.progressDialog.setMessage("Lade Daten...");
        VPlanFragment.this.progressDialog.setCancelable(false);
        VPlanFragment.this.progressDialog.setIndeterminate(true);
        VPlanFragment.this.progressDialog.show();
    }

    private void loadOk(boolean showDialog, boolean checkCache) {
        if(showDialog)
            if(Looper.getMainLooper().getThread() != Thread.currentThread()) {
                if(VPlanFragment.this.getActivity() != null)
                    VPlanFragment.this.getActivity().runOnUiThread(this::showLdDialog);
                else
                    Timber.d("Vertretungsplan getActivity is null");
            } else {
                Timber.w("loadOk sollte nicht im Hauptthread ausgeführt werden!");
                showLdDialog();
            }
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(20, TimeUnit.SECONDS);
        b.connectTimeout(20, TimeUnit.SECONDS);

        OkHttpClient client = b.build();

        System.setProperty("http.keepAlive", "false");

        Request request = new Request.Builder()
                .url(VPlanFragment.this.getURL())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e(e);
                if(VPlanFragment.this.getActivity() == null)
                    return;

                VPlanFragment.this.getActivity().runOnUiThread(() -> {
                    if(e instanceof SocketTimeoutException)
                        Toast.makeText(VPlanFragment.this.getContext(), "Timeout exception", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(VPlanFragment.this.getContext(), "Der Vertretungsplan konnte nicht neu geladen werden!", Toast.LENGTH_SHORT).show();


                    if (showDialog && VPlanFragment.this.progressDialog != null) {
                        VPlanFragment.this.progressDialog.dismiss();
                    }

                    if(mCardView.getChildCount() < 2) {
                        if(VPlanFragment.this.swipeContainer != null && VPlanFragment.this.swipeContainer.isRefreshing()) VPlanFragment.this.swipeContainer.setRefreshing(false);
                        mCardView.clearCards();
                        mCardView.addCard(new MyPlayCard(istDunkel,"Interner Fehler", "Es ist ein Fehler beim Herunterladen des Vertretungsplans aufgetreten!", "#FF0000", "#FF0000", true, false, false, cardMarquee));
                        Toast.makeText(getContext(), "Vertretungsplan konnte nicht angezeigt werden, zeige im Browser..", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra(EXTRA_URL, "https://www.gymnasium-sonneberg.de/Informationen/vp.php5");
                        intent.putExtra(Util.EXTRA_NAME, "[!] Vertretungsplan [!]");
                        getContext().startActivity(intent);
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

                VPlanFragment.this.getActivity().runOnUiThread(() -> {
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
                });
            }
        });
    }

    /*public void doRefresh() {
        Toast.makeText(VPlanFragment.this.getContext(), "Refresh!", Toast.LENGTH_SHORT).show();

        magic(true);
    }*/

    void loadData(boolean isRefresh) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread())
            if(VPlanFragment.this.getActivity() != null)
                VPlanFragment.this.getActivity().runOnUiThread(() -> {
                    if (swipeContainer != null) swipeContainer.setRefreshing(true);
                });

        if(Util.hasInternet(getContext())) {
            if (!isRefresh) {
                boolean cacheState = loadFromHtmlCache();
                Timber.d("Cache loaded after " + (System.currentTimeMillis() - appStart) + "ms");
                loadOk(!cacheState, cacheState);
            } else {
                loadOk(false, false);
            }
        } else {
            boolean cs = loadFromHtmlCache();
            if (Thread.currentThread() != Looper.getMainLooper().getThread())
                if(getActivity() != null)
                    VPlanFragment.this.getActivity().runOnUiThread(() -> {
                        if(!cs) {
                            this.mCardView.clearCards();
                            this.mCardView.addCard(new MyPlayCard(istDunkel, "Fehler", "Es besteht keine Internetverbindung und es wurde noch kein Vertretungsplan zwischengespeichert!", "#FF0000", "#FF0000", true, false, false, cardMarquee));
                            this.mCardView.refresh();
                        } else Toast.makeText(getContext(), "Vertretungsplan aus dem Zwischenspeicher geladen!", Toast.LENGTH_SHORT).show();

                    });
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

    private void parseResponse(String result) throws ArrayIndexOutOfBoundsException {
        //this.isFiltered = !this.Filter.isEmpty();
        this.isFiltered = Util.isFiltered(GSApp.getContext());
        if(this.showingAll)
            this.isFiltered = false;
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
                vpEnts = doc.select(par.cssSelector() + ", " + par.cssSelector() + " ~ tr");
            } catch(NullPointerException npe) {
                Timber.e("ParseResponse: Fallback failed, html is shown below: ");
                Timber.e(result);
            }
        }



        Timber.d( "vpEnts size is %s", vpEnts.size());
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
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            if (getActivity() != null)
                VPlanFragment.this.getActivity().runOnUiThread(() -> {
                    if (VPlanFragment.this.isFiltered && Util.isLehrerModus(getContext()))
                        displayAllTeacher();
                    else
                        displayAll();
                });
            else
                Timber.d("Vertretungsplan Activity is null parseResponse");
        } else
            if (VPlanFragment.this.isFiltered && Util.isLehrerModus(getContext()))
                displayAllTeacher();
            else
                displayAll();
    }

    private void fallbackLoad(String result) {
        String Klasse = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Util.Preferences.KLASSE, "");
        if(this.showingAll)
            Klasse = "";
        try {
            if (!result.equals("E")) {
                dateD = result.split("<td colspan=\"7\" class=\"rundeEckenOben vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
                hinweisD = "[!] " + result.split("<tr id=\"Shinweis\">")[1].split("</tr>")[0].replace("Hinweis: <br />","").replace("<br />", "· ").replaceAll("<.*?>", "").replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß").replaceAll("[\r\n]+", "").trim();
            }
        } catch (Exception e) {
            this.hinweisD = "[!] kein Hinweis";
            Timber.d( "Fehler beim Auswerten der Informationen");
        }
        if (!result.equals("E")) {
            String[] newC = clearUp(result.split("<td class=\"vpTextZentriert\">", 2)[1].split("\n")).split("\n");
            int counter = 1;
            String klasse = "";
            String stunde = "";
            String orgfach = "";
            String vertret = "";
            String raum = "";
            String verfach = "";
            String str;
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
                        klasse = "";
                        stunde = "";
                        orgfach = "";
                        vertret = "";
                        raum = "";
                        verfach = "";
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
                            }
                        } else if (SUCL.contains(Klasse)) {
                            displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, str);
                        }
                    }
                }
            }
            displayAll();
        }
    }

    private void displayStuff(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {
        vplane.add(new Eintrag(klasse, stunde, fachnormal, vertretung, raum, fachvertret, bemerkung));
    }

    private String getURL() {
        int mode = PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).getInt("debugSrc", 0);
        String URL;
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
        Timber.d("Display start after " + (System.currentTimeMillis() - appStart) + "ms");
        mCardView.clearCards();
        CardStack dateHead = new CardStack(istDunkel);
        dateHead.setTypeface(Util.getTKFont(this.getContext(), false));
        dateHead.setTitle("Für " + dateD);
        mCardView.addStack(dateHead);

        //if(!hinweisD.equals("Hinweis:")) {
        Timber.d("Hinweis= +" + hinweisD + "+");
        if(!hinweisD.isEmpty()) {
            MyPlayCard card = new MyPlayCard(istDunkel,"Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\r\n]+","").trim(), "#00FF00", "#00FF00", true, false, false, cardMarquee);
            card.setOnClickListener(v -> {
                AlertDialog ad = new AlertDialog.Builder(VPlanFragment.this.getContext()).create();
                ad.setCancelable(true);
                ad.setTitle("Hinweis");
                ad.setMessage(hinweisD);

                ad.setButton("OK", (dialog, which) -> dialog.dismiss());
                ad.show();
            });
            mCardView.addCard(card);
        }
        try {
            if(this.isFiltered) {
                CardStack stacky = new CardStack(istDunkel);
                stacky.setTypeface(Util.getTKFont(this.getContext(), false));
                stacky.setTitle("Vertretungen für Klasse " + Util.getKlasse(getContext()) );
                mCardView.addStack(stacky);
            }
            for(String klassee : vplane.getKlassen()) {
                if(!this.isFiltered) {
                    CardStack stacky = new CardStack(istDunkel);
                    stacky.setTypeface(Util.getTKFont(this.getContext(), false));
                    stacky.setTitle("Klasse " + klassee);
                    mCardView.addStack(stacky);
                }
                final ArrayList<Eintrag> tc = vplane.getKlasseGruppe(klassee, !this.isFiltered);
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
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde" + note, "Du hast " + LongName(single.getFachNormal()) + " bei " + Util.getTeacherName(single.getVertretung(), true) + " in Raum " + single.getRaum() + "." + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + Util.getTeacherName(single.getVertretung(), true) + " in Raum " + single.getRaum() + "." + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    }
                    if(this.isFiltered) {
                        card.setOnClickListener(arg0 -> displayMoreInformation(single));
                        mCardView.addCard(card);
                    } else {
                        card.setOnClickListener(arg0 -> {
                            if(tc.size() > 1) {
                                displaySingleClass(single.getKlasse());
                            } else {
                                displayMoreInformation(single);
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

        mCardView.refresh();
        mCardView.getScrollView().computeScrollY();

        Timber.d("gcc: %s", mCardView.getChildCount());

        if (swipeContainer != null && swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }

        //saveToCache();

        Timber.d("Displayed after " + (System.currentTimeMillis() - appStart) + "ms");
    }

    private void displayAllTeacher() {
        mCardView.clearCards();
        CardStack dateHead = new CardStack(istDunkel);
        dateHead.setTypeface(Util.getTKFont(this.getContext(), false));
        dateHead.setTitle("Für " + dateD);
        mCardView.addStack(dateHead);

        Timber.d("Hinweis= +" + hinweisD + "+");
        if(!hinweisD.isEmpty()) {
            MyPlayCard card = new MyPlayCard(istDunkel,"Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\r\n]+","").trim(), "#00FF00", "#00FF00", true, false, false, cardMarquee);
            card.setOnClickListener(v -> {
                AlertDialog ad = new AlertDialog.Builder(VPlanFragment.this.getContext()).create();
                ad.setCancelable(true);
                ad.setTitle("Hinweis");
                ad.setMessage(hinweisD);

                ad.setButton("OK", (dialog, which) -> dialog.dismiss());
                ad.show();
            });
            mCardView.addCard(card);
        }

        try {
            if(this.isFiltered) {
                CardStack stacky = new CardStack(istDunkel);
                stacky.setTypeface(Util.getTKFont(this.getContext(), false));
                stacky.setTitle("Vertretungen für " + Util.getTeacherName(Util.getLehrer(getContext()) ,true));
                mCardView.addStack(stacky);
            }

            for(String stundee : vplane.getStunden()) {
                String prevStd = "";
                final ArrayList<Eintrag> tc = vplane.getStundeGruppe(stundee, !this.isFiltered);
                for(final Eintrag single : tc) {
                    MyPlayCard card;
                    String note = "";

                    if(single.getKlasse().matches("([0-9]{2}[A-Z]+[0-9]{1})+")) {
                        note = " (" + single.getKlasse().replaceAll("([0-9]{2})+", "") + ")";
                    }

                    if(single.getBemerkung().equals("Ausfall")) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Std. - Kl. " + single.getKlasse() + " - Ausfall!" + note, LongName(single.getFachNormal()) + " fällt aus (Raum " + single.getRaum() + ")" + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else if(single.getBemerkung().equals("Stillbesch.") || single.getBemerkung().equals("Stillbeschäftigung")) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Std. - Kl. " + single.getKlasse() + " - Stillb.!" + note, "Sie sind zuständig für Stillbeschäftigung in Raum " + single.getRaum() + " (statt " + LongName(single.getFachNormal()) + ")" + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else if(single.getBemerkung().equals("AA") || single.getBemerkung().equals("Arbeitsauftrag")) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Std. - Kl. " + single.getKlasse() + note, "Sie erteilen einen Arbeitsauftrag in Raum " + single.getRaum() + " (statt " + LongName(single.getFachNormal()) + ")" + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else if(single.getFachNormal().equals(single.getFachVertretung())) {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Std. - Kl. " + single.getKlasse() + note, "Sie vertreten " + LongName(single.getFachVertretung()) + " in Raum " + single.getRaum() + "." + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    } else {
                        card = new MyPlayCard(istDunkel,single.getStunde() + ". Std. - Kl. " + single.getKlasse() + note, "Sie vertreten " + LongName(single.getFachVertretung()) + " für " + LongName(single.getFachNormal()) + " in Raum " + single.getRaum() + "." + single.getBemerkungForCard(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
                    }
                    card.setOnClickListener(arg0 -> {
                        if(tc.size() > 1) {
                            displaySingleClass(single.getKlasse());
                        } else {
                            displayMoreInformation(single);
                        }

                    });
                    if(!prevStd.equals(single.getStunde())) {
                        prevStd = single.getStunde();
                        mCardView.addCard(card);
                    } else {
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

        mCardView.refresh();
        mCardView.getScrollView().computeScrollY();

        if (swipeContainer != null && swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }

        Timber.d("Displayed after " + (System.currentTimeMillis() - appStart) + "ms");
    }

    private void displaySingleClass(String klasse) {
        if(getFragmentManager() == null)
            return;
        VPDetailSheet vds = new VPDetailSheet();
        ArrayList<Eintrag> inp;
        try {
            inp = vplane.getKlasseGruppe(klasse, false);
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


        switch (eintrag.getBemerkung()) {
            case "Ausfall":
                ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Ausfall (Raum " + eintrag.getRaum() + ")" + note);
                break;
            case "Stillbesch.":
                ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + eintrag.getRaum() + note);
                break;
            case "AA":
                ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + eintrag.getRaum() + note);
                break;
            default:
                ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du " + LongName(eintrag.getFachVertretung()) + " bei " + eintrag.getVertretung() + " im Raum " + eintrag.getRaum() + "\n\nBemerkung: " + eintrag.getBemerkung() + note);
                break;
        }

        ad.setButton("OK", (dialog, which) -> dialog.dismiss());
        ad.show();
    }

    private String getFachColor(String fach) {
        String cSport = "#607d8b";
        String col;
        String cBiologie = "#4caf50";
        String cChemie = "#e91e63";
        String cDeutsch = "#2196F3";
        String cEnglisch = "#ff9800";
        String cEthik = "#ff8f00";
        String cFRL = "#558b2f";
        String cGeografie = "#9e9d24";
        String cGeschichte = "#9c27b0";
        String cInformatik = "#03a9f4";
        String cKunst = "#673ab7";
        String cMathe = "#f44336";
        String cMusik = "#9e9e9e";
        String cPhysik = "#009688";
        String cReligion = "#ff8f00";
        String cSozialkunde = "#795548";
        String cWirtschaftRecht = "#ff5722";
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
        StringBuilder me = new StringBuilder();
        for(String ln : inpud) {
            ln = ln.replaceAll("<.*?>","");
            ln = ln.replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß");
            ln = ln.replace("                        ", "");
            ln = ln.trim();
            ln = ln.replace("	", "");

            //} else if(ln.endsWith("&nbsp;")) {
            //	me = me + "XXXX\n";
            if (!ln.equals("      ") && !ln.equals("var hoehe = parent.document.getElementById('inhframe').style.height;") && !ln.equals("setFrameHeight();") && !ln.equals("var pageTracker = _gat._getTracker(" + gf + "UA-5496889-1" + gf + ");") && !ln.equals("pageTracker._trackPageview();") && !ln.equals("    ") && !ln.equals("	") && !ln.equals("  ") && !ln.startsWith("var") && !ln.startsWith("document.write") && !ln.equals("")) {
                if (ln.matches(".*\\w.*")) {
                    me.append(ln).append("\n");
                } else if (ln.contains("##")) {
                    me.append(ln).append("\n");
                }
            }
        }

        return me.toString();
    }

    private boolean hasCacheChanged(String html) {
        try {
            File hcache = new File(GSApp.getContext().getCacheDir(), "vpl.fbcache");

            if(!hcache.exists()) {
                Timber.d( "No HTML Cache found!");
                return true;
            }

            return !Files.toString(hcache, Charsets.UTF_8).toLowerCase().equals(html.toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void saveToHtmlCache(String html) {
        try {
            File outputFile = new File(GSApp.getContext().getCacheDir(), "vpl.fbcache");
            Files.asCharSink(outputFile, Charsets.UTF_8).write(html);
            //Files.write(html, outputFile, Charsets.UTF_8); <-- Deprecated
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean loadFromHtmlCache() {
        Timber.d("loadHtml begin after " + (System.currentTimeMillis() - appStart) + "ms");
        try {
            File hcache = new File(GSApp.getContext().getCacheDir(), "vpl.fbcache");

            if(!hcache.exists()) {
                Timber.d( "No HTML Cache found!");
                return false;
            }

            //String html = Files.toString(hcache, Charsets.UTF_8);
            String html = Files.asCharSource(hcache, Charsets.UTF_8).read();
            try {
                Timber.d("Loading from cache (M1)");
                VPlanFragment.this.parseResponse(html);
                Timber.d("Parsed after " + (System.currentTimeMillis() - appStart) + "ms");
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                try {
                    Timber.d("Loading from cache (M2)");
                    if(Looper.getMainLooper().getThread() != Thread.currentThread()) {
                        if (VPlanFragment.this.getActivity() != null)
                            VPlanFragment.this.getActivity().runOnUiThread(() -> VPlanFragment.this.fallbackLoad(html));
                        else
                            Timber.d("Vertretungsplan htmlCache activityIsNull");
                    }
                    return true;
                } catch(Exception exo) {
                    Timber.d( "Failed on fallbackLoad: %s", exo.getMessage());
                    Timber.e(exo);
                    exo.printStackTrace();
                    return false;
                }

            } catch(Exception e) {
                Timber.d( "Failed on ParseResponse: %s", e.getMessage());
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

}
