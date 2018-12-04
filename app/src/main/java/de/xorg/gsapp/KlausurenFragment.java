package de.xorg.gsapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

//import android.support.v4.app.Fragment;


public class KlausurenFragment extends Fragment {

    private ProgressDialog progressDialog;
    private List<Klausur> klausurs = new ArrayList<>();
    private KlausurenAdapter mAdapter;
    private int shownPage = 0;
    private SwipeRefreshLayout swipeContainer;

    public KlausurenFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_klausuren, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.rv);
        swipeContainer = (SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                Toast.makeText(getContext(), "Yo!", Toast.LENGTH_SHORT).show();
            }
        });
        swipeContainer.setColorSchemeResources(new int[]{R.color.md_cyan_A200, R.color.md_light_green_A400, R.color.md_amber_300, R.color.md_red_A400});


        mAdapter = new KlausurenAdapter(this.getContext(), klausurs);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        KlausurenDecoration klausurenDecoration = new KlausurenDecoration(Util.convertToPixels(this.getContext(), 20),
                false, new KlausurenDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return position == 0
                        || klausurs.get(position)
                        .getDate().compareTo(klausurs.get(position - 1).getDate()) != 0;
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return klausurs.get(position).getDateString();
            }
        });
        recyclerView.addItemDecoration(klausurenDecoration);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.HORIZONTAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        getActivity().setTitle("GSApp - Klausuren");

        //prepareMovieData();

        loadOk(true, false);
    }

    public void loadOk(boolean showDialog, boolean checkCache) {
        //OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(20, TimeUnit.SECONDS);
        b.connectTimeout(20, TimeUnit.SECONDS);

        OkHttpClient client = b.build();

        System.setProperty("http.keepAlive", "false");
        if (showDialog) {
            progressDialog = new ProgressDialog(this.getContext());
            progressDialog.setProgressStyle(0);
            progressDialog.setTitle("GSApp");
            progressDialog.setMessage("Lade Daten...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        Request request = new Request.Builder()
                .url("https://www.gymnasium-sonneberg.de/Schueler/KursArb/ka.php5?seite=" + this.shownPage)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e(e);
                if(getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(e.getMessage().contains("timeout")) {
                            Toast.makeText(getContext(), "Der Klausurenplan konnte nicht geladen werden, da die Verbindung zum Server zu lang gedauert hat!", Toast.LENGTH_SHORT).show(); //TODO
                        } else {
                            Toast.makeText(getContext(), "Der Klausurenplan konnte nicht geladen werden!", Toast.LENGTH_SHORT).show();
                        }


                        if (showDialog && progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        if (swipeContainer != null && swipeContainer.isRefreshing()) {
                            swipeContainer.setRefreshing(false);
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful())
                    Timber.e("onResponse FAILED (" + response.code() + ")");
                final String result = response.body().string();

                if(getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(checkCache) {
                            /*if(!hasCacheChanged(result)) {
                                Timber.d( "Cache has not changed");
                                return;
                            } else {
                                Timber.d("Cache HAS changed...");
                            }*/
                        } else {
                            Timber.d("Not checking cache...");
                        }


                        /*VPlanFragment.this.vplane.clear();
                        saveToHtmlCache(result);*/

                        try {
                            parseResponse(result);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            Timber.e("ArrayOutOfBounds Klausuren");
                        } catch(Exception e) {
                            Timber.d( "Failed on ParseResponse: " + e.getMessage());
                            Timber.e(e);
                            e.printStackTrace();
                        }


                        if (showDialog && progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                });
            }

        });
    }

    public void parseResponse(String result) throws ArrayIndexOutOfBoundsException, ParseException {
        //this.isFiltered = !this.Filter.isEmpty();
        //if(this.showingAll)
        ///    this.isFiltered = false;
        //this.htmlSource = result;
        Document doc = Jsoup.parse(result);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        List<Date> daten = new ArrayList<>(); //Enthält die jeweils ersten Tage der Wochen

        Element ksHeader = doc.select("td[class*=ueberschr]").first(); //Überschrift-Element finden
        String[] jahre;
        Matcher jahreMatcher = Pattern.compile("[0-9]{4}\\/[0-9]{4}").matcher(ksHeader.html()); //Regex-Suche nach Jahreszahlen (XXXX/XXXX)
        if(jahreMatcher.matches())
            jahre = jahreMatcher.group(0).split("/");
        else
            jahre = ksHeader.html().split("<br>")[1].replaceAll("[^\\d/]", "" ).split("/"); //Fallback-Methode: Überschrift bei Linebreak teilen, dann Jahre trennen

        Elements vpEnts = doc.select("td[class=kopf] ~ td");
        Iterator it = vpEnts.iterator();
        while (it.hasNext()) {
            //Matcher datums = Pattern.compile("[0-9]{2}\\.[0-9]{2}\\.").matcher(((Element) it.next()).html());
            String weekStart = ((Element) it.next()).html().split("<br>")[0]; //TODO: Doof.
            Calendar cal = Calendar.getInstance();
            cal.setTime(format.parse(weekStart + "2000"));
            Log.d("Klausuren", "WS:" + (weekStart + "2000"));
            //int month = Integer.parseInt(Pattern.compile("([0-9]+)(?!.*[0-9])").matcher(weekStart).group(0));
            if(8 <= cal.get(Calendar.MONTH) && cal.get(Calendar.MONTH) <= 12) //Wenn Monat zwischen 8 und 12 -> erstes Jahr, sonst 2. Jahr
                cal.set(Calendar.YEAR, Integer.parseInt(jahre[0])); //1. Jahr setzen
            else
                cal.set(Calendar.YEAR, Integer.parseInt(jahre[1])); //2. Jahr setzen

            daten.add(cal.getTime()); //Wochenstart-Datum hinzufügen
        }



        for (int i = 0; i < daten.size(); i++) {
            Log.d("KlausurenDaten", "At " + i + " there is " + format.format(daten.get(i)));
        }

        //Elements curRow = ;


        //while (true) {
        //    Elements tablRow = curRow.children();
        //    Log.d("Klausuren", tablRow.html());
        //}

        //Elements klausurs = doc.select("td[class=kopf] ~ td").first().parent().siblingElements();
        int numFirstDateRow = doc.select("td[class=kopf]").first().parent().children().size() - 1;
        Log.d("Klausuren", "nFDR=" + numFirstDateRow);

        Element kartoffel = doc.select("td[class=kopf] ~ td").first().parent().nextElementSibling();
        int day = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        while(kartoffel != kartoffel.lastElementSibling()) {
            Elements klausuren = kartoffel.select("td:not(.tag,.kopf)");
            Iterator klausurenIt = klausuren.iterator();
            while (klausurenIt.hasNext()) {
                Element klausur = ((Element) klausurenIt.next());
                if(!klausur.html().equals("&nbsp;")) {
                    Klausur thisKlausur;
                    //Klausur movie = new Klausur(klausur.text(), new Date());
                    //klausurs.add(movie);
                    Calendar c = Calendar.getInstance();
                    int thisDay = day;
                    int datePos = klausur.elementSiblingIndex();
                    if(thisDay > 5) {
                        datePos += numFirstDateRow;
                        thisDay -= 6;
                    }

                    c.setTime(daten.get(datePos - 1));
                    c.add(Calendar.DATE, thisDay);


                    if (Util.isBeforeDay(Calendar.getInstance(), c))
                        continue;
                    else if (Pattern.compile("[^a-zA-Z0-9\\s]").matcher(klausur.text()).find() || klausur.text().equals("Ferien"))
                        continue;
                    else if (klausur.text().equals("Abgabe SF")) {
                        klausurs.add(new Klausur("Abgabe SF", c.getTime()));
                    } else {
                        for(String singleKlausur : klausur.text().split(" ")) {
                            klausurs.add(new Klausur(singleKlausur, c.getTime()));
                        }
                    }

                    Log.d("Klausuren", "pos (day,week)=" + thisDay + ", " + klausur.elementSiblingIndex() + "-RALF-" + klausur.text());

                    Log.d("Klausuren", klausur.text() + " ON " + sdf.format(c.getTime()));
                }
                //Log.d("Klausuren", klausur.html());
            }
            day++;
            kartoffel = kartoffel.nextElementSibling();
        }


        //Iterator its = klausurs.iterator();
        //while (its.hasNext()) {
        //    Log.d("Klausuren", ((Element) it.next()).html());
        //}
        //Element date = doc.select("td[class*=vpUeberschr]").first();
        //Element bemerk = doc.select("td[class=vpTextLinks]").first();
        /*String bemerkText;
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
        displayAll();*/

        Collections.sort(klausurs, (k1, k2) -> k2.getDate().compareTo(k1.getDate()));
        mAdapter.notifyDataSetChanged();
    }

    private void prepareMovieData() {
        Klausur movie = new Klausur("DE", new Date());
        klausurs.add(movie);

        movie = new Klausur("MA", new Date());
        klausurs.add(movie);

        movie = new Klausur("IF", new Date());
        klausurs.add(movie);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Util.prepareMenu(menu, R.id.nav_klausuren);
        super.onPrepareOptionsMenu(menu);
    }
}
