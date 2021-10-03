package de.xorg.gsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import de.xorg.cardsuilib.objects.CardStack;
import de.xorg.cardsuilib.views.CardUI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

//import android.support.v4.app.Fragment;

public class SpeiseplanFragment extends Fragment {


    int displayed = 0;
    private int today = 1;
    private boolean istWeekend = false;
    private boolean showWeekend = true;
    private boolean didLoad = false;
    private boolean useInsecureSSL = false;
    Button prevDay;
    Button nextDay;
    private CardUI mCardView;
    private SpMenu m1;
    private SpMenu m2;
    private SpMenu m3;
    private SpMenu m4;
    private String dateFrom;
    private String dateTill;
    private ProgressDialog progressDialog;
    private boolean istDark = false;

    public SpeiseplanFragment() { }

    private void fetchSpeiseplan(){
        OkHttpClient client;
        if(this.useInsecureSSL)
            client = Util.getUnsafeOkHttpClient();
        else {
            OkHttpClient.Builder b = new OkHttpClient.Builder();
            b.readTimeout(20, TimeUnit.SECONDS);
            b.connectTimeout(20, TimeUnit.SECONDS);
            client = b.build();
        }
        //OkHttpClient client = Util.getUnsafeOkHttpClient();

        System.setProperty("http.keepAlive", "false");

        if(SpeiseplanFragment.this.getContext() != null) {
            SpeiseplanFragment.this.progressDialog = new ProgressDialog(SpeiseplanFragment.this.getContext());
            SpeiseplanFragment.this.progressDialog.setProgressStyle(0);
            SpeiseplanFragment.this.progressDialog.setTitle("GSApp");
            SpeiseplanFragment.this.progressDialog.setMessage("Lade Daten...");
            SpeiseplanFragment.this.progressDialog.setCancelable(false);
            SpeiseplanFragment.this.progressDialog.setIndeterminate(true);
            SpeiseplanFragment.this.progressDialog.show();
        }

        Request request = new Request.Builder()
                //.url("https://www.schulkueche-bestellung.de/index.php?m=2;1")
                .url("https://www.schulkueche-bestellung.de/de/menu/14")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e(e);
                if(SpeiseplanFragment.this.getActivity() == null)
                    return;

                SpeiseplanFragment.this.getActivity().runOnUiThread(() -> {
                    if(e.getMessage().contains("timeout")) {
                        Toast.makeText(SpeiseplanFragment.this.getContext(), "Der Speiseplan konnte nicht neu geladen werden, da die Verbindung zum Server zu lang gedauert hat!", Toast.LENGTH_SHORT).show(); //TODO
                    } else if(e instanceof SSLHandshakeException) {
                        SpeiseplanFragment.this.useInsecureSSL = true;
                        if (SpeiseplanFragment.this.progressDialog != null) {
                            SpeiseplanFragment.this.progressDialog.dismiss();
                        }
                        Toast.makeText(SpeiseplanFragment.this.getContext(), "SSL-Fehler - stimmt die Systemzeit?", Toast.LENGTH_LONG).show();
                        fetchSpeiseplan();
                        return;
                    } else {
                        Toast.makeText(SpeiseplanFragment.this.getContext(), "Der Speiseplan konnte nicht neu geladen werden!", Toast.LENGTH_SHORT).show();
                    }

                    if (SpeiseplanFragment.this.progressDialog != null) {
                        SpeiseplanFragment.this.progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful())
                    Timber.e("onResponse FAILED (" + response.code() + ")");
                final String result = response.body().string();

                if(SpeiseplanFragment.this.getActivity() == null)
                    return;

                SpeiseplanFragment.this.getActivity().runOnUiThread(() -> {
                    parseTable(result);
                    loadCallback();

                    if (SpeiseplanFragment.this.progressDialog != null) {
                        SpeiseplanFragment.this.progressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speiseplan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() instanceof MainActivity2) ((MainActivity2) getActivity()).setBarTitle("Speiseplan");

        if (getArguments() != null && getArguments().containsKey("theme")) {
            istDark = (getArguments().getString("theme").equals(Util.AppTheme.DARK));
        }

        // CardView initialisieren
        final Animation anim = AnimationUtils.loadAnimation(this.getContext(), R.anim.slide);
        mCardView = getView().findViewById(R.id.cardsview);
        mCardView.setAnimation(anim);
        mCardView.setSwipeable(false);

        // Menüs initialisieren
        m1 = new SpMenu(1);
        m2 = new SpMenu(2);
        m3 = new SpMenu(3);
        m4 = new SpMenu(4);

        Calendar calendar = Calendar.getInstance();
        int cKW = calendar.get(Calendar.WEEK_OF_YEAR);
        String currentKW = String.valueOf(cKW);
        //String currentKW = "0";
        today = calendar.get(Calendar.DAY_OF_WEEK);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GSApp.getContext());
        String savedKW = sp.getString("cacheKW", "0");

        getView().findViewById(R.id.prevDay).setOnClickListener(view1 -> {
            displayed = displayed - 1;
            drawCardsForDay(displayed, istWeekend);
        });
        getView().findViewById(R.id.nextDay).setOnClickListener(view12 -> {
            displayed = displayed + 1;
            drawCardsForDay(displayed, istWeekend);
        });

        Timber.d("cur: *" + currentKW + "*, sav: *" + savedKW + "*");

        if (currentKW.equals(savedKW)) {
            try {
                File cacheFile = new File(GSApp.getContext().getCacheDir(), "sp2.gxcache");

                if (cacheFile.exists() && cacheFile.canRead()) {
                    //JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));
                    JSONObject jo = new JSONObject(Files.asCharSource(cacheFile, Charsets.UTF_8).read());

                    JSONObject M1 = jo.getJSONObject("Meal1");
                    JSONObject M2 = jo.getJSONObject("Meal2");
                    JSONObject M3 = jo.getJSONObject("Meal3");
                    JSONObject M4 = jo.getJSONObject("Meal4");
                    String CKW = jo.getString("KW");

                    if(!CKW.equals(savedKW)) {
                        Timber.w("Stored KW is not equal to KW in cache - reloading..");
                        cacheFile.delete();
                        fetchSpeiseplan();
                        return;
                    }


                    this.dateFrom = jo.getString("DateFrom");
                    this.dateTill = jo.getString("DateTill");

                    m1.fromSaveJSON(M1, CKW);
                    m2.fromSaveJSON(M2, CKW);
                    m3.fromSaveJSON(M3, CKW);
                    m4.fromSaveJSON(M4, CKW);
                    didLoad = true;
                    Timber.d("Cache loaded");

                }
            } catch (Exception e) {
                Timber.e("Konnte nicht aus dem Cache laden: %s", e.getMessage());
                e.printStackTrace();
                fetchSpeiseplan();
            }
        } else if (Util.isNumeric(savedKW) && cKW < Integer.parseInt(savedKW)) {
            Toast.makeText(this.getContext(), "Cache load! (KW New)", Toast.LENGTH_SHORT).show();
            try {
                File cacheFile = new File(GSApp.getContext().getCacheDir(), "sp2.gxcache");

                if (cacheFile.exists() && cacheFile.canRead()) {
                    //JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));
                    JSONObject jo = new JSONObject(Files.asCharSource(cacheFile, Charsets.UTF_8).read());

                    JSONObject M1 = jo.getJSONObject("Meal1");
                    JSONObject M2 = jo.getJSONObject("Meal2");
                    JSONObject M3 = jo.getJSONObject("Meal3");
                    JSONObject M4 = jo.getJSONObject("Meal4");
                    String CKW = jo.getString("KW");
                    this.dateFrom = jo.getString("DateFrom");
                    this.dateTill = jo.getString("DateTill");

                    m1.fromSaveJSON(M1, CKW);
                    m2.fromSaveJSON(M2, CKW);
                    m3.fromSaveJSON(M3, CKW);
                    m4.fromSaveJSON(M4, CKW);
                    didLoad = true;
                }
            } catch (Exception e) {
                Timber.e("Konnte nicht aus dem Cache laden: %s", e.getMessage());
                e.printStackTrace();
                fetchSpeiseplan();
            }
        } else {
            if (!Util.hasInternet(getContext())) {
                try {
                    File cacheFile = new File(GSApp.getContext().getCacheDir(), "sp2.gxcache");

                    if (cacheFile.exists() && cacheFile.canRead()) {
                        //JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));
                        JSONObject jo = new JSONObject(Files.asCharSource(cacheFile, Charsets.UTF_8).read());

                        JSONObject M1 = jo.getJSONObject("Meal1");
                        JSONObject M2 = jo.getJSONObject("Meal2");
                        JSONObject M3 = jo.getJSONObject("Meal3");
                        JSONObject M4 = jo.getJSONObject("Meal4");
                        String CKW = jo.getString("KW");
                        this.dateFrom = jo.getString("DateFrom");
                        this.dateTill = jo.getString("DateTill");

                        m1.fromSaveJSON(M1, CKW);
                        m2.fromSaveJSON(M2, CKW);
                        m3.fromSaveJSON(M3, CKW);
                        m4.fromSaveJSON(M4, CKW);

                        Toast.makeText(this.getContext(), "Kein Internet - gespeicherter Plan ist veraltet!", Toast.LENGTH_SHORT).show();

                        didLoad = true;
                    }
                } catch (Exception e) {
                    Timber.e("Konnte nicht aus dem Cache laden: %s", e.getMessage());
                    e.printStackTrace();
                    fetchSpeiseplan();
                }
            }
            Timber.d("Cache existiert nicht oder ist veraltet!");
            fetchSpeiseplan();
        }

        prevDay = getView().findViewById(R.id.prevDay);
        nextDay = getView().findViewById(R.id.nextDay);
        loadCallback();
    }

    private void loadCallback() {
        if (this.didLoad) {
            if ((this.today == Calendar.SATURDAY) || (this.today == Calendar.SUNDAY)) {
                this.displayed = Calendar.MONDAY;
                this.istWeekend = true;
                drawCardsForDay(Calendar.MONDAY, true);
            } else {
                this.displayed = this.today;
                this.istWeekend = false;
                drawCardsForDay(this.today, false);
            }
        } else {
            this.mCardView.clearCards();
            MyPlayCard cardW = new MyPlayCard(this.istDark,"Fehler", "Es besteht keine Internetverbindung und der Zwischenspeicher existiert nicht! Stellen sie eine Internetverbindung her und tippen sie hier!", "#FFFFFF", "#FF0000", true, false, false);
            cardW.setOnClickListener(view -> {
                if (Util.hasInternet(SpeiseplanFragment.this.getContext())) {
                    fetchSpeiseplan();
                } else {
                    Toast.makeText(SpeiseplanFragment.this.getContext(), "Es besteht keine Internetverbindung!", Toast.LENGTH_SHORT).show();
                }
            });
            mCardView.addCard(cardW);
        }
    }

    private void parseTable(String data) {
        try {
            Document doc = Jsoup.parse(data);

            doc.select("sup").remove();

            Elements currentDates = doc.select("button#time-selector-dropdown");
            int kw = Integer.parseInt(currentDates.text().split("\\|\\|")[0].replaceAll("[\\D]", ""));
            if ((kw < 0) || (kw > 53)) {
                Timber.w("Parsed KW \""+ kw +"\" is probably invalid");
            }
            Timber.d("NewEP::KW=%s", kw);
            this.dateFrom = currentDates.text().split("\\|\\|")[1].split("-")[0].trim() + Util.last(currentDates.text().split("\\|\\|")[1].split("\\."));
            this.dateTill = currentDates.text().split("\\|\\|")[1].split("-")[1].trim();

            m1.setKW(String.valueOf(kw));
            m2.setKW(String.valueOf(kw));
            m3.setKW(String.valueOf(kw));
            m4.setKW(String.valueOf(kw));

            Elements tableElements = doc.select("table#menu-table_KW");
            m1.setMeals(tableElements.select("td[mealId=01] mealtxt").eachText());
            m2.setMeals(tableElements.select("td[mealId=02] mealtxt").eachText());
            m3.setMeals(tableElements.select("td[mealId=03] mealtxt").eachText());
            m4.setMeals(tableElements.select("td[mealId=07] mealtxt").eachText()); //TODO: Immer id=07?

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GSApp.getContext()).edit();
            editor.putString("cacheKW", String.valueOf(kw));
            editor.apply();

            SpeiseplanFragment.this.didLoad = true;

            File outputFile = new File(GSApp.getContext().getCacheDir(), "sp2.gxcache");
            try {
                JSONObject root = new JSONObject();

                JSONObject Meal1 = m1.toSaveJSON();
                JSONObject Meal2 = m2.toSaveJSON();
                JSONObject Meal3 = m3.toSaveJSON();
                JSONObject Meal4 = m4.toSaveJSON();

                root.put("KW", String.valueOf(kw));
                root.put("DateFrom", this.dateFrom);
                root.put("DateTill", this.dateTill);
                root.put("Meal1", Meal1);
                root.put("Meal2", Meal2);
                root.put("Meal3", Meal3);
                root.put("Meal4", Meal4);

                //Files.write(root.toString(), outputFile, Charsets.UTF_8);
                Files.asCharSink(outputFile, Charsets.UTF_8).write(root.toString());
                Timber.d("Cache erstellt!");
            } catch (Exception e) {
                if(getContext() != null) Toast.makeText(getContext(), "Speiseplan konnte nicht zwischengespeichert werden!", Toast.LENGTH_SHORT).show();
                Timber.w("Could not create cache - file not writeable");
            }

        } catch (Exception e) {
            Timber.e("Konnte Serverantwort nicht verarbeiten: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void drawCardsForDay(final int day, final boolean isWeekend) {
        mCardView.clearCards();
        CardStack dateHead = new CardStack(istDark);
        dateHead.setTypeface(Util.getTKFont(getContext(), false));
        dateHead.setTitle("Gültig von " + this.dateFrom + " bis " + this.dateTill);
        mCardView.addStack(dateHead);

        if (isWeekend && showWeekend) {
            final MyPlayCard cardW = new MyPlayCard(istDark,"Wochenende!", "Heute gibt es keine Schulspeisung!", "#FFFFFF", "#FF0000", true, false, false);
            cardW.setOnClickListener(view -> {
                showWeekend = false;
                drawCardsForDay(day, true);
            });
            mCardView.addCard(cardW);
        }

        TextView dispDay;
        try {
            dispDay = getView().findViewById(R.id.curDay);
            dispDay.setText(getResources().getString(getDayRes(day)));
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        if (day == Calendar.MONDAY) {
            prevDay.setEnabled(false);
            prevDay.setText(String.format("← %s", getResources().getString(R.string.day_mo)));
        } else {
            prevDay.setEnabled(true);
            prevDay.setText(String.format("← %s", getResources().getString(getDayResShort(day - 1))));
        }

        if (day == Calendar.FRIDAY) {
            nextDay.setEnabled(false);
            nextDay.setText(String.format("%s →", getResources().getString(R.string.day_fr)));
        } else {
            nextDay.setEnabled(true);
            nextDay.setText(String.format("%s →", getResources().getString(getDayResShort(day + 1))));
        }

        MyPlayCard card1 = new MyPlayCard(istDark,"Menü 1", m1.getToday(day), "#f44336", "#f44336", true, true, false);
        card1.setOnClickListener(v -> {
            AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
            ad.setCancelable(true);
            ad.setTitle(String.format(getResources().getString(R.string.menu_on), 1, getResources().getString(getDayRes(day))));
            ad.setMessage(m1.getToday(day));
            ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
            ad.show();
        });
        mCardView.addCard(card1);

        MyPlayCard card2 = new MyPlayCard(istDark,"Menü 2", m2.getToday(day), "#ff9800", "#ff9800", true, true, false);
        card2.setOnClickListener(v -> {
            AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
            ad.setCancelable(true);
            ad.setTitle(String.format(getResources().getString(R.string.menu_on), 2, getResources().getString(getDayRes(day))));
            ad.setMessage(m2.getToday(day));
            ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
            ad.show();
        });
        mCardView.addCard(card2);

        MyPlayCard card3 = new MyPlayCard(istDark,"Menü 3", m3.getToday(day), "#4caf50", "#4caf50", true, true, false);
        card3.setOnClickListener(v -> {
            AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
            ad.setCancelable(true);
            ad.setTitle(String.format(getResources().getString(R.string.menu_on), 3, getResources().getString(getDayRes(day))));
            ad.setMessage(m3.getToday(day));
            ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
            ad.show();
        });
        mCardView.addCard(card3);

        MyPlayCard salad = new MyPlayCard(istDark,"Salat", m4.getToday(day), "#2196F3", "#2196F3", true, true, false);
        salad.setOnClickListener(view -> {
            AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
            ad.setCancelable(true);
            ad.setTitle(String.format(getResources().getString(R.string.salad_on), getResources().getString(getDayRes(day))));
            ad.setMessage(m4.getToday(day));
            ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
            ad.show();
        });
        mCardView.addCard(salad);

        mCardView.refresh();
    }

    private int getDayResShort(int day) {
        switch(day) {
            case Calendar.MONDAY: return R.string.day_mo;
            case Calendar.TUESDAY: return R.string.day_tu;
            case Calendar.WEDNESDAY: return R.string.day_we;
            case Calendar.THURSDAY: return R.string.day_th;
            case Calendar.FRIDAY: return R.string.day_fr;
            default: return R.string.day_fr;
        }
    }

    private int getDayRes(int day) {
        switch(day) {
            case Calendar.MONDAY: return R.string.day_monday;
            case Calendar.TUESDAY: return R.string.day_tuesday;
            case Calendar.WEDNESDAY: return R.string.day_wednesday;
            case Calendar.THURSDAY: return R.string.day_thursday;
            case Calendar.FRIDAY: return R.string.day_friday;
            default: return R.string.day_friday;
        }
    }

    private String getSalatForDay(int day) {
        String Salat;
        switch (day) {
            case Calendar.MONDAY:
                Salat = "Salat mit Käse und Kochschinken mit French Dressing";
                break;
            case Calendar.TUESDAY:
                Salat = "Salat mit gebr. Geflügelfilet und Joghurt Dressing";
                break;
            case Calendar.WEDNESDAY:
                Salat = "Salat mit Thunfisch und American Dressing";
                break;
            case Calendar.THURSDAY:
                Salat = "Salat nach griechischer Art mit Joghurt Dressing";
                break;
            case Calendar.FRIDAY:
                Salat = "Salat mit Tomaten und Mozzarella mit Ital.-Kräuter Dressing";
                break;
            default:
                Salat = "null";
                break;
        }
        return Salat;
    }

    /*public String getDayName(int day) {
        String name;
        switch (day) {
            case Calendar.MONDAY:
                name = "Montag";
                break;
            case Calendar.TUESDAY:
                name = "Dienstag";
                break;
            case Calendar.WEDNESDAY:
                name = "Mittwoch";
                break;
            case Calendar.THURSDAY:
                name = "Donnerstag";
                break;
            case Calendar.FRIDAY:
                name = "Freitag";
                break;
            case Calendar.SATURDAY:
                name = "Samstag";
                break;
            case Calendar.SUNDAY:
                name = "Sonntag";
                break;
            default:
                name = "SNERROR" + day;
                break;
        }
        return name;
    }

    public String getDayNameShort(int day) {
        String name;
        switch (day) {
            case Calendar.MONDAY:
                name = "Mo";
                break;
            case Calendar.TUESDAY:
                name = "Di";
                break;
            case Calendar.WEDNESDAY:
                name = "Mi";
                break;
            case Calendar.THURSDAY:
                name = "Do";
                break;
            case Calendar.FRIDAY:
                name = "Fr";
                break;
            case Calendar.SATURDAY:
                name = "Sa";
                break;
            case Calendar.SUNDAY:
                name = "So";
                break;
            default:
                name = "SNERROR" + day;
                break;
        }
        return name;
    }*/
}