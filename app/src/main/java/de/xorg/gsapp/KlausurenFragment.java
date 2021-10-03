package de.xorg.gsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.io.Files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/*
 * Fragment Klausurenplan
 */

public class KlausurenFragment extends Fragment {

    private ProgressDialog progressDialog;
    private final List<Klausur> klausurs = new ArrayList<>();
    private FloatingActionButton fab;
    private KlausurenAdapter mAdapter;
    private int shownPage = 0;
    private SwipeRefreshLayout swipeContainer;
    private KlausurPlans kP;

    public KlausurenFragment() { }

    private static class KlasseState {
        static final int UNCHANGED = 0;
        static final int CHANGED = 1;
        static final int ERROR = 2;
    }

    /*
    "ADT" für Klausurpläne der Klassen 11 und 12
     */
    private static class KlausurPlans {
        List<Klausur> kl11;
        List<Klausur> kl12;
        String headr11;
        String headr12;

        static class JSONKeys { //JSON-Schlüssel-Namen
            static final String KLAUSUR_11 = "klausuren-11";
            static final String KLAUSUR_12 = "klausuren-12";
            static final String HEADER_11 = "header-11";
            static final String HEADER_12 = "header-12";
        }

        KlausurPlans() { //Leer initialisieren
            this.kl11 = new ArrayList<>();
            this.kl12 = new ArrayList<>();
            this.headr11 = "";
            this.headr12 = "";
        }

        KlausurPlans(String error_message) { //Fügt nur eine Fehlermeldung ein
            this.kl11 = new ArrayList<>(Collections.singletonList(new Klausur(String.format("Fehler: %s", error_message), new Date())));
            this.kl12 = new ArrayList<>(Collections.singletonList(new Klausur(String.format("Fehler: %s", error_message), new Date())));
            this.headr11 = "";
            this.headr12 = "";
        }

        KlausurPlans(JSONObject root) throws JSONException, ParseException { //Füllt aus JSON-Cache
            if(this.kl11 != null) this.kl11.clear(); else this.kl11 = new ArrayList<>();
            if(this.kl12 != null) this.kl12.clear(); else this.kl12 = new ArrayList<>();

            JSONArray k11 = root.getJSONArray(JSONKeys.KLAUSUR_11);
            JSONArray k12 = root.getJSONArray(JSONKeys.KLAUSUR_12);
            for (int i = 0; i < k11.length(); i++) { this.kl11.add(new Klausur(k11.getJSONObject(i))); }
            for (int i = 0; i < k12.length(); i++) { this.kl12.add(new Klausur(k12.getJSONObject(i))); }

            this.headr11 = root.getString(JSONKeys.HEADER_11);
            this.headr12 = root.getString(JSONKeys.HEADER_12);
        }

        //Gibt Klausurplan bzw Header für entspr. Klasse zurück
        List<Klausur> getKl11() { if(this.kl11.size() > 0 ) return this.kl11; else return new ArrayList<>(Collections.singletonList(new Klausur("Fehler: Keine Daten!", new Date()))); }
        List<Klausur> getKl12() { if(this.kl12.size() > 0 ) return this.kl12; else return new ArrayList<>(Collections.singletonList(new Klausur("Fehler: Keine Daten!", new Date()))); }
        String getHeader11() { return this.headr11; }
        String getHeader12() { return this.headr12; }

        void setKl11(List<Klausur> i, String h) { this.kl11 = i; this.headr11 = h; }
        void setKl12(List<Klausur> i, String h) { this.kl12 = i; this.headr12 = h; }

        //Wandelt die Daten in JSON um (Für Cache)
        JSONObject toJSON() throws JSONException {
            JSONObject root = new JSONObject();
            JSONArray k11 = new JSONArray();
            for (int i = 0; i < this.kl11.size(); i++) {  k11.put(this.kl11.get(i).toJSON()); }
            root.put(JSONKeys.KLAUSUR_11, k11);
            JSONArray k12 = new JSONArray();
            for (int i = 0; i < this.kl12.size(); i++) {  k12.put(this.kl12.get(i).toJSON()); }
            root.put(JSONKeys.KLAUSUR_12, k12);
            root.put(JSONKeys.HEADER_11, this.headr11);
            root.put(JSONKeys.HEADER_12, this.headr12);
            return root;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_klausuren, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(false);
    }

    /*
    Also, wenn es a20 ist, dann machen die 2020 Abi und haben 2 Jahre vorher in der 11. angefangen.
    Wenn also 2018 August bis Dezember oder 2019 Januar bis Juli ist, dann sind die in der 11.
    Rest ist klar.
     */
    private int getPageFromKlasse(String inp) {
        try {
            if(!inp.toUpperCase().startsWith("A")) //Keine Abiturklasse / Nicht 11./12. Klasse
                return 0;

            Calendar now = Calendar.getInstance();
            int abiJahr = Integer.parseInt(inp.replaceAll("\\D+",""));

            if( (abiJahr - 2) == (now.get(Calendar.YEAR) % 100) ) //Zweites Jahr vor Abi -> 11. Klasse
                //11. Klasse
                return 0;
            else if( ((abiJahr - 1) == (now.get(Calendar.YEAR) % 100)) && now.get(Calendar.MONTH) <= 7 ) //Erstes Jahr vor Abi und spätestens Juli -> 11. Klasse
                return 0;
            else
                return 1;
        } catch(Exception e) {
            e.printStackTrace();
            return 0;
        }

    }


    //Erstellt eine Grafik mit Text (durchsichtiger Text auf Weiß)
    //Für Klassen-Umschalter
    private static Bitmap textIconic(Context c, String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize((float) 120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Util.getTKFont(c, true));
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setStyle(Paint.Style.FILL);
        bg.setColor(c.getResources().getColor(R.color.text_dark));

        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        int dpRd = Util.convertToPixels(c, 2);
        int dpRd2 = Util.convertToPixels(c, 4);
        Bitmap image = Bitmap.createBitmap(height + (2*dpRd), height + (2*dpRd), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawRoundRect(new RectF(0, 0, height + (2*dpRd), height + (2*dpRd)), dpRd2, dpRd2, bg);
        canvas.drawText(text, (height-width)/2, baseline + dpRd, paint);
        return image;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() != null && getActivity() instanceof MainActivity2) ((MainActivity2) getActivity()).setBarTitle("Klausurenplan");

        shownPage = PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).getInt(Util.Preferences.KLAUSUR_PLAN, getPageFromKlasse(PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).getString(Util.Preferences.KLASSE, "KEINE")));
        RecyclerView recyclerView = requireView().findViewById(R.id.rv);
        swipeContainer = requireView().findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(() -> {
            fetch(null, 0, true, null); //Erzwingt Aktualisierung
        });
        swipeContainer.setColorSchemeResources(R.color.md_cyan_A200, R.color.md_light_green_A400, R.color.md_amber_300, R.color.md_red_A400); //Setzt Farben für Ladekreis

        //Klassen-Umschalter
        //TODO: Mit in den Einstellungen gewählter Klasse starten
        fab = requireView().findViewById(R.id.fab_class);
        fab.setImageBitmap(textIconic(getContext(),"11")); //Icon setzen
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) fab.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
        fab.setLayoutParams(lp);
        fab.setOnClickListener(v -> {
            if(shownPage == 0) {
                //shownPage = 1;
                showKlasse(1);
                fab.setImageBitmap(textIconic(getContext(),"12"));
            } else {
                //shownPage = 0;
                showKlasse(0);
                fab.setImageBitmap(textIconic(getContext(),"11"));
            }
        });

        klausurs.add(new Klausur("Fehler: (Noch) keine Daten..", new Date()));

        mAdapter = new KlausurenAdapter(requireContext(), klausurs);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //Zwischen-Überschriften: Wenn Datum einer Klausur nicht mit dem der vorherigen übereinstimmt
        //füge eine Zwischenüberschrift mit diesem Datum ein
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
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        load();
        considerTip();
    }

    /*
    Lade-Prozedur:
    Cache vorhanden? -(ja)-> Zeige Plan aus Cache -> Lade Plan aus Internet -> Geändert? -(ja)-> schreibe Cache -> neuen Anzeigen
                     |                                                                  -(nein)-> tue nichts
                     -(nein)-> Zeige Ladebildschirm -> Lade Plan aus Internet -> schreibe Cache -> Ladeb. schließen -> neuen Anzeigen
     */
    private void load() {
        File klausurCache = new File(GSApp.getContext().getCacheDir(), "klausur.json");
        if(klausurCache.exists()) {
            try {
                kP = new KlausurPlans(new JSONObject(new String(Files.toByteArray(klausurCache), Charset.forName("UTF-8"))));
            } catch(IOException e) {
                kP = new KlausurPlans("E/A-Fehler");
            } catch(ParseException p) {
                kP = new KlausurPlans("Verarbeitungsfehler");
            } catch(JSONException j) {
                kP = new KlausurPlans("JSON-Fehler");
            }
            showKlasse(shownPage);
        }

        fetch(klausurCache.exists() ? null : new ProgressDialog(getContext()), 0, false, null);

    }

    /*
    Lädt aktuellen Plan aus Internet
    ---
    showDialog ist ein ProgressDialog wenn Ladebildschirm gezeigt werden soll, sonst null
    page ist ein Zähler, ob die Seite für 11. oder 12. Klasse geladen wird (NUR für Selbstaufruf, muss von extern immer 0 sein)
    force erzwingt eine Aktualisierung (=Wenn Nutzer das anfordert)
    passData enthält den Quelltext der vorherigen Seite (11. Klasse)
     */
    public void fetch(ProgressDialog showDialog, int page, boolean force, String passData) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(20, TimeUnit.SECONDS);
        b.connectTimeout(20, TimeUnit.SECONDS);

        OkHttpClient client = b.build();

        System.setProperty("http.keepAlive", "false");
        if (showDialog != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                showDialog.setProgressStyle(0);
                showDialog.setTitle("GSApp");
                showDialog.setMessage("Lade Daten...");
                showDialog.setCancelable(false);
                showDialog.setIndeterminate(true);
                showDialog.show();
                    });

        } else {
            if(swipeContainer != null) swipeContainer.setRefreshing(true);
        }

        Request request = new Request.Builder()
                .url("https://www.gymnasium-sonneberg.de/Schueler/KursArb/ka.php5?seite=" + page)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Timber.e(e);
                if(getActivity() == null)
                    return;

                getActivity().runOnUiThread(() -> {

                    try {
                        if(Objects.requireNonNull(e.getMessage()).contains("timeout")) {
                            Toast.makeText(getContext(), "Der Klausurenplan konnte nicht geladen werden, da die Verbindung zum Server zu lang gedauert hat!", Toast.LENGTH_SHORT).show(); //TODO
                        } else {
                            Toast.makeText(getContext(), "Der Klausurenplan konnte nicht geladen werden!", Toast.LENGTH_SHORT).show();
                        }
                    } catch(NullPointerException npe) {
                        npe.printStackTrace();
                    }


                    if (showDialog != null) {
                        showDialog.dismiss();
                    }
                    if (swipeContainer != null && swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful())
                    Timber.e("onResponse FAILED (" + response.code() + ")");
                final String result = Objects.requireNonNull(response.body()).string();

                    if(page == 0) {
                        fetch(showDialog, 1, force, result);
                        return;
                    }

                    if(1 == page && passData != null) {
                        if(getActivity() != null)
                            getActivity().runOnUiThread(() -> {
                                if (showDialog != null)
                                    showDialog.dismiss();
                                if (swipeContainer != null && swipeContainer.isRefreshing())
                                    swipeContainer.setRefreshing(false);
                            });
                        parseCache(passData, result, force);
                    }


            }

        });
    }


    /*
    Verarbeitet die aktuellen Pläne (in den Cache und ggf. in die aktuelle Ansicht
    page11 ist der Quelltext des Plans für Klasse 11
    page12 ist der Quelltext des Plans für Klasse 12
    force = Erzwingen?
     */
    private void parseCache(String page11, String page12, boolean force) {
        if(kP == null)
            kP = new KlausurPlans();

        Document d11 = Jsoup.parse(page11);
        Document d12 = Jsoup.parse(page12);

        int state11 = KlasseState.UNCHANGED;
        int state12 = KlasseState.UNCHANGED;

        String header11 = parseHeader(d11);
        String header12 = parseHeader(d12);

        if(!header11.equals(kP.getHeader11()) || force) {
            try {
                kP.setKl11(parse(d11, header11), header11);
                state11 = KlasseState.CHANGED;
            } catch (ParseException e) {
                kP.setKl11(new ArrayList<>(Collections.singletonList(new Klausur("Fehler: Verarbeitungsfehler 11", new Date()))), "");
                state11 = KlasseState.ERROR;
            }
        }

        if(!header12.equals(kP.getHeader12()) || force) {
            try {
                kP.setKl12(parse(d12, header12), header12);
                state12 = KlasseState.CHANGED;
            } catch (ParseException e) {
                kP.setKl12(new ArrayList<>(Collections.singletonList(new Klausur("Fehler: Verarbeitungsfehler 12", new Date()))), "");
                state12 = KlasseState.ERROR;
            }
        }

        //try { Log.d("GSApp", kP.toJSON().toString()); } catch(Exception e) { Log.d("GSApp", "ich bin dumm"); }

        if(getContext() == null)
            Timber.d("*** CONTEXT WOULD BE NULL!");

        try {
            Files.write(kP.toJSON().toString().getBytes(Charset.forName("UTF-8")), new File(GSApp.getContext().getCacheDir(), "klausur.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if(getActivity() == null || (state11 == KlasseState.UNCHANGED && state12 == KlasseState.UNCHANGED))
            return;

        final int s11 = state11;
        final int s12 = state12;

        getActivity().runOnUiThread(() -> {
            if (shownPage == 0 && s11 != KlasseState.UNCHANGED) { showKlasse(0); Toast.makeText(getContext(), "Neuer Plan gefunden!", Toast.LENGTH_SHORT).show(); }
            if (shownPage == 1 && s12 != KlasseState.UNCHANGED) { showKlasse(1); Toast.makeText(getContext(), "Neuer Plan gefunden!", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void considerTip() {
        if(PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).getBoolean(Util.Preferences.KLAUSUR_TIP, true)) {
            Toast t = Toast.makeText(getContext(), "Klicke auf den Button um zwischen 11. und 12. Klasse zu wechseln \uD83D\uDC49", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.BOTTOM|Gravity.START, 0, 0);
            t.show();
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).edit();
            edit.putBoolean(Util.Preferences.KLAUSUR_TIP, false);
            edit.apply();
        }

    }

    /*
    Zeigt die gewünschte Klasse im Plan an
    Die Pläne müssen zuvor in kP (klausurPlans) geladen worden sein
     */
    private void showKlasse(int klasse) {
        List<Klausur> al = (klasse == 0 ? kP.getKl11() : kP.getKl12());
        this.klausurs.clear();
        for(int i = 0; i < al.size(); i++) {
            Calendar c = Calendar.getInstance();
            c.setTime(al.get(i).getDate());
            if(!Util.isBeforeDay(c, Calendar.getInstance())) {
                this.klausurs.add(al.get(i));
            }
        }

        if(this.klausurs.size() < 1) {
            this.klausurs.add(new Klausur("Keine Klausuren mehr!", new Date()));
        }

        shownPage = klasse;
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).edit();
        edit.putInt(Util.Preferences.KLAUSUR_PLAN, klasse);
        edit.apply();

        Collections.sort(klausurs, (k1, k2) -> k1.getDate().compareTo(k2.getDate()));
        mAdapter.notifyDataSetChanged();
    }

    /*
    Verarbeitet die Kopfzeile (für Jahreszahl und Änderungs-Überprüfung)
    doc ist das HTML-Dokument
     */
    private String parseHeader(Document doc) {
        return doc.select("td[class*=ueberschr]").first().html(); //Überschrift-Element finden
    }

    /*
    Verarbeitet einen Klausurplan
    doc ist das HTML-Dokument
    ksHeader ist die Kopfzeile aus parseHeader
    excludeOld schließt vergangene Klausuren aus (eigentlich nicht benutzt wegen Cache - könnte aber eigentlich gemacht werden?)
     */
    private List<Klausur> parse(Document doc, String ksHeader) throws ArrayIndexOutOfBoundsException, ParseException {
        List<Klausur> o = new ArrayList<>();
        //Document doc = Jsoup.parse(result);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        List<Date> daten = new ArrayList<>(); //Enthält die jeweils ersten Tage der Wochen

        String[] jahre;
        Matcher jahreMatcher = Pattern.compile("[0-9]{4}/[0-9]{4}").matcher(ksHeader); //Regex-Suche nach Jahreszahlen (XXXX/XXXX)
        if(jahreMatcher.matches())
            jahre = Objects.requireNonNull(jahreMatcher.group(0)).split("/");
        else
            jahre = ksHeader.split("<br>")[1].replaceAll("[^\\d/]", "" ).split("/"); //Fallback-Methode: Überschrift bei Linebreak teilen, dann Jahre trennen

        Elements vpEnts = doc.select("td[class=kopf] ~ td");
        for (Element vpEnt : vpEnts) {
            //Matcher datums = Pattern.compile("[0-9]{2}\\.[0-9]{2}\\.").matcher(((Element) it.next()).html());
            String weekStart = vpEnt.html().split("<br>")[0]; //TODO: Doof.
            Calendar cal = Calendar.getInstance();
            cal.setTime(Objects.requireNonNull(format.parse(weekStart + "2000")));
            //int month = Integer.parseInt(Pattern.compile("([0-9]+)(?!.*[0-9])").matcher(weekStart).group(0));
            if (8 <= cal.get(Calendar.MONTH) && cal.get(Calendar.MONTH) <= 12) //Wenn Monat zwischen 8 und 12 -> erstes Jahr, sonst 2. Jahr
                cal.set(Calendar.YEAR, Integer.parseInt(jahre[0])); //1. Jahr setzen
            else
                cal.set(Calendar.YEAR, Integer.parseInt(jahre[1])); //2. Jahr setzen

            daten.add(cal.getTime()); //Wochenstart-Datum hinzufügen
        }

        int numFirstDateRow = doc.select("td[class=kopf]").first().parent().children().size() - 1;
        Element kartoffel = doc.select("td[class=kopf] ~ td").first().parent().nextElementSibling();
        int day = 0;
        //while(kartoffel != kartoffel.lastElementSibling()) {
        while(day < 20) {
            //Timber.d(kartoffel.html());
            Elements klausuren = kartoffel.select("td:not(.tag,.kopf)");
            for (Element klausur : klausuren) {
                if (!klausur.html().equals("&nbsp;")) {
                    Calendar c = Calendar.getInstance();
                    int thisDay = day;
                    int datePos = klausur.elementSiblingIndex();
                    if (thisDay > 5) {
                        datePos += numFirstDateRow;
                        thisDay -= 6;
                    }

                    c.setTime(daten.get(datePos - 1));
                    c.add(Calendar.DATE, thisDay);

                    if (Pattern.compile("[^a-zA-Z0-9\\s]").matcher(klausur.text()).find() || klausur.text().equals("Ferien"))
                        continue;
                    else if (klausur.text().equals("Abgabe SF")) {
                        o.add(new Klausur("Abgabe SF", c.getTime()));
                    } else {
                        for (String singleKlausur : klausur.text().split(" ")) {
                            if (singleKlausur.length() < 6)
                                o.add(new Klausur(singleKlausur, c.getTime()));
                        }
                    }
                }
            }
            day++;
            if(kartoffel != kartoffel.lastElementSibling())
                kartoffel = kartoffel.nextElementSibling();
            else
                break;
        }

        if(o.size() < 1) {
            o.add(new Klausur("Keine Klausuren mehr!", new Date()));
        }

        Collections.sort(o, (k1, k2) -> k1.getDate().compareTo(k2.getDate()));
        return o;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        Util.prepareMenu(menu, Util.NavFragments.KLAUSUREN);
        super.onPrepareOptionsMenu(menu);
    }
}
