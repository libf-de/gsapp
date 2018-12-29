package de.xorg.gsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
    int today = 1;
    boolean istWeekend = false;
    boolean showWeekend = true;
    boolean didLoad = false;
    Button prevDay;
    Button nextDay;
    private CardUI mCardView;
    private SpMenu m1;
    private SpMenu m2;
    private SpMenu m3;
    private ProgressDialog progressDialog;
    boolean istDark = false;

    public SpeiseplanFragment() { }

    public void fetchSpeiseplan() {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(20, TimeUnit.SECONDS);
        b.connectTimeout(20, TimeUnit.SECONDS);

        OkHttpClient client = b.build();

        System.setProperty("http.keepAlive", "false");
        SpeiseplanFragment.this.progressDialog = new ProgressDialog(SpeiseplanFragment.this.getContext());
        SpeiseplanFragment.this.progressDialog.setProgressStyle(0);
        SpeiseplanFragment.this.progressDialog.setTitle("GSApp");
        SpeiseplanFragment.this.progressDialog.setMessage("Lade Daten...");
        SpeiseplanFragment.this.progressDialog.setCancelable(false);
        SpeiseplanFragment.this.progressDialog.setIndeterminate(true);
        SpeiseplanFragment.this.progressDialog.show();

        Request request = new Request.Builder()
                .url("https://www.schulkueche-bestellung.de/index.php?m=2;1")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e(e);
                if(SpeiseplanFragment.this.getActivity() == null)
                    return;

                SpeiseplanFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(e.getMessage().contains("timeout")) {
                            Toast.makeText(SpeiseplanFragment.this.getContext(), "Der Speiseplan konnte nicht neu geladen werden, da die Verbindung zum Server zu lang gedauert hat!", Toast.LENGTH_SHORT).show(); //TODO
                        } else {
                            Toast.makeText(SpeiseplanFragment.this.getContext(), "Der Speiseplan konnte nicht neu geladen werden!", Toast.LENGTH_SHORT).show();
                        }

                        //TODO: Besser catchen

                        if (SpeiseplanFragment.this.progressDialog != null) {
                            SpeiseplanFragment.this.progressDialog.dismiss();
                        }
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

                SpeiseplanFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseTable(result);
                        loadCallback();

                        if (SpeiseplanFragment.this.progressDialog != null) {
                            SpeiseplanFragment.this.progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speiseplan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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

        Calendar calendar = Calendar.getInstance();
        int cKW = calendar.get(Calendar.WEEK_OF_YEAR);
        String currentKW = String.valueOf(cKW);
        today = calendar.get(Calendar.DAY_OF_WEEK);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String savedKW = sp.getString("cacheKW", "0");

        getView().findViewById(R.id.prevDay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayed = displayed - 1;
                drawCardsForDay(displayed, istWeekend);
            }
        });
        getView().findViewById(R.id.nextDay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayed = displayed + 1;
                drawCardsForDay(displayed, istWeekend);
            }
        });

        Timber.d("cur: *" + currentKW + "*, sav: *" + savedKW + "*");

        if (currentKW.equals(savedKW)) {
            Toast.makeText(this.getContext(), "Cache load! (KW Match)", Toast.LENGTH_SHORT).show();
            try {
                File cacheDir = this.getContext().getCacheDir(); // context being the Activity pointer
                File cacheFile = new File(cacheDir, "speiseplan.gxcache");

                if (cacheFile.exists() && cacheFile.canRead()) {
                    JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));

                    JSONObject M1 = jo.getJSONObject("Meal1");
                    JSONObject M2 = jo.getJSONObject("Meal2");
                    JSONObject M3 = jo.getJSONObject("Meal3");
                    String CKW = jo.getString("KW");

                    m1.fromSaveJSON(M1, CKW);
                    m2.fromSaveJSON(M2, CKW);
                    m3.fromSaveJSON(M3, CKW);
                    didLoad = true;
                    Timber.d("Cache loaded");

                }
            } catch (Exception e) {
                Timber.e("Konnte nicht aus dem Cache laden: " + e.getMessage());
                e.printStackTrace();
                fetchSpeiseplan();
            }
        } else if (Util.isNumeric(savedKW) && cKW < Integer.parseInt(savedKW)) {
            Toast.makeText(this.getContext(), "Cache load! (KW New)", Toast.LENGTH_SHORT).show();
            try {
                File cacheDir = this.getContext().getCacheDir(); // context being the Activity pointer
                File cacheFile = new File(cacheDir, "speiseplan.gxcache");

                if (cacheFile.exists() && cacheFile.canRead()) {
                    JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));

                    JSONObject M1 = jo.getJSONObject("Meal1");
                    JSONObject M2 = jo.getJSONObject("Meal2");
                    JSONObject M3 = jo.getJSONObject("Meal3");
                    String CKW = jo.getString("KW");

                    m1.fromSaveJSON(M1, CKW);
                    m2.fromSaveJSON(M2, CKW);
                    m3.fromSaveJSON(M3, CKW);
                    didLoad = true;
                }
            } catch (Exception e) {
                Timber.e("Konnte nicht aus dem Cache laden: " + e.getMessage());
                e.printStackTrace();
                fetchSpeiseplan();
            }
        } else {
            if (!Util.hasInternet(this.getContext())) {
                try {
                    File cacheDir = this.getContext().getCacheDir(); // context being the Activity pointer
                    File cacheFile = new File(cacheDir, "speiseplan.gxcache");

                    if (cacheFile.exists() && cacheFile.canRead()) {
                        JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));

                        JSONObject M1 = jo.getJSONObject("Meal1");
                        JSONObject M2 = jo.getJSONObject("Meal2");
                        JSONObject M3 = jo.getJSONObject("Meal3");
                        String CKW = jo.getString("KW");

                        m1.fromSaveJSON(M1, CKW);
                        m2.fromSaveJSON(M2, CKW);
                        m3.fromSaveJSON(M3, CKW);

                        Toast.makeText(this.getContext(), "Kein Internet - gespeicherter Plan ist veraltet!", Toast.LENGTH_SHORT).show();

                        didLoad = true;
                    }
                } catch (Exception e) {
                    Timber.e("Konnte nicht aus dem Cache laden: " + e.getMessage());
                    e.printStackTrace();
                    fetchSpeiseplan();
                }
            }
            Timber.d("Cache existiert nicht oder ist veraltet!");
            fetchSpeiseplan();
        }

        prevDay = (Button) getView().findViewById(R.id.prevDay);
        nextDay = (Button) getView().findViewById(R.id.nextDay);
        loadCallback();
    }

    public void loadCallback() {
        if (didLoad) {
            if (today == Calendar.SATURDAY || today == Calendar.SUNDAY) {
                displayed = Calendar.MONDAY;
                istWeekend = true;
                drawCardsForDay(Calendar.MONDAY, istWeekend);
            } else {
                displayed = today;
                istWeekend = false;
                drawCardsForDay(today, istWeekend);
            }
        } else {
            mCardView.clearCards();
            MyPlayCard cardW = new MyPlayCard(istDark,"Fehler", "Es besteht keine Internetverbindung und der Zwischenspeicher existiert nicht! Stellen sie eine Internetverbindung her und tippen sie hier!", "#FFFFFF", "#FF0000", true, false, false);
            cardW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Util.hasInternet(SpeiseplanFragment.this.getContext())) {
                        fetchSpeiseplan();
                    } else {
                        Toast.makeText(SpeiseplanFragment.this.getContext(), "Es besteht keine Internetverbindung!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mCardView.addCard(cardW);
        }
    }

    public void nextDay(View v) {

    }

    public void prevDay(View v) {

    }

    public void parseTable(String data) {
        String output = "";
        try {
            Document doc = Jsoup.parse(data);

            Elements date = doc.select("select option[selected]");
            String KW = date.text().split(" ")[1];
            Timber.d( "Got KW " + KW);
            String Datum = date.text().split("\\|\\|")[1];
            Timber.d("Datum is +" + Datum + "+");

            m1.setKW(KW);
            m2.setKW(KW);
            m3.setKW(KW);

            m1.setDatum(Datum);
            m2.setDatum(Datum);
            m3.setDatum(Datum);

            Elements tableElements = doc.select("table[class=splanauflistung]");

            Elements tableHeaderEles = tableElements.select("thead tr th");
            output += "headers\n";
            for (int i = 0; i < tableHeaderEles.size(); i++) {
                output += tableHeaderEles.get(i).text() + "\n";
            }
            output += "\n";

            Elements tableRowElements = tableElements.select(":not(thead) tr");

            for (int i = 0; i < tableRowElements.size(); i++) {
                Element row = tableRowElements.get(i);
                output += "row" + i + "\n";
                Elements rowItems = row.select("td");
                if (rowItems.size() == 6) {
                    for (int j = 0; j < rowItems.size(); j++) {
                        Element clear = rowItems.get(j);
                        clear.getElementsByClass("zusatzstoff").remove();
                        String meal = clear.text().replaceAll(" ,", ",");
                        switch (j) {
                            case 0:
                                break;
                            case 1:
                                if (i == 1) {
                                    m1.setMontag(meal);
                                } else if (i == 2) {
                                    m2.setMontag(meal);
                                } else if (i == 3) {
                                    m3.setMontag(meal);
                                }
                                break;
                            case 2:
                                if (i == 1) {
                                    m1.setDienstag(meal);
                                } else if (i == 2) {
                                    m2.setDienstag(meal);
                                } else if (i == 3) {
                                    m3.setDienstag(meal);
                                }
                                break;
                            case 3:
                                if (i == 1) {
                                    m1.setMittwoch(meal);
                                } else if (i == 2) {
                                    m2.setMittwoch(meal);
                                } else if (i == 3) {
                                    m3.setMittwoch(meal);
                                }
                                break;
                            case 4:
                                if (i == 1) {
                                    m1.setDonnerstag(meal);
                                } else if (i == 2) {
                                    m2.setDonnerstag(meal);
                                } else if (i == 3) {
                                    m3.setDonnerstag(meal);
                                }
                                break;
                            case 5:
                                if (i == 1) {
                                    m1.setFreitag(meal);
                                } else if (i == 2) {
                                    m2.setFreitag(meal);
                                } else if (i == 3) {
                                    m3.setFreitag(meal);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                output += "\n";
            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
            editor.putString("cacheKW", KW);
            editor.commit();

            SpeiseplanFragment.this.didLoad = true;

            File outputDir = this.getContext().getCacheDir(); // context being the Activity pointer
            File outputFile = new File(outputDir, "speiseplan.gxcache");

            //if (outputFile.canWrite()) {
                JSONObject root = new JSONObject();

                JSONObject Meal1 = m1.toSaveJSON();
                JSONObject Meal2 = m2.toSaveJSON();
                JSONObject Meal3 = m3.toSaveJSON();

                root.put("KW", KW);

                root.put("Meal1", Meal1);
                root.put("Meal2", Meal2);
                root.put("Meal3", Meal3);

                Files.write(root.toString(), outputFile, Charsets.UTF_8);
                Timber.d("Cache erstellt!");
            //} else {
            //    l.critical("Kann nicht in Cache schreiben!");
            //}
        } catch (Exception e) {
            Timber.e("Konnte Serverantwort nicht verarbeiten: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void drawCardsForDay(final int day, final boolean isWeekend) {
        mCardView.clearCards();
        CardStack dateHead = new CardStack(istDark);
        dateHead.setTypeface(Util.getTKFont(getContext(), false));
        //dateHead.setTitle("Kalenderwoche " + m1.getKW());
        String Datem = m1.getDatum();
        String FromDate = "??";
        String TillDate = "??";
        try { FromDate = Datem.split("-")[0].trim(); //TODO: Fehler behandeln
        TillDate = Datem.split("-")[1].trim(); } catch (Exception e) {
            e.printStackTrace();
        }
        dateHead.setTitle("Gültig von " + FromDate + " bis " + TillDate);
        mCardView.addStack(dateHead);

        if (isWeekend && showWeekend) {
            final MyPlayCard cardW = new MyPlayCard(istDark,"Wochenende!", "Heute gibt es keine Schulspeisung!", "#FFFFFF", "#FF0000", true, false, false);
            cardW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showWeekend = false;
                    drawCardsForDay(day, isWeekend);
                }
            });
            mCardView.addCard(cardW);
        }

        TextView dispDay = (TextView) getView().findViewById(R.id.curDay);
        dispDay.setText(getDayName(day));

        if (day == Calendar.MONDAY) {
            prevDay.setEnabled(false);
            prevDay.setText("← Mo");
        } else {
            prevDay.setEnabled(true);
            prevDay.setText("← " + getDayNameShort(day - 1));
        }

        if (day == Calendar.FRIDAY) {
            nextDay.setEnabled(false);
            nextDay.setText("Fr →");
        } else {
            nextDay.setEnabled(true);
            nextDay.setText(getDayNameShort(day + 1) + " →");
        }

        MyPlayCard card1 = new MyPlayCard(istDark,"Menü 1", m1.getToday(day), "#f44336", "#f44336", true, true, false);
        card1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
                ad.setCancelable(true);
                ad.setTitle("Menü 1 am " + getDayName(day));
                ad.setMessage(m1.getToday(day));
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }

        });
        mCardView.addCard(card1);

        MyPlayCard card2 = new MyPlayCard(istDark,"Menü 2", m2.getToday(day), "#ff9800", "#ff9800", true, true, false);
        card2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
                ad.setCancelable(true);
                ad.setTitle("Menü 2 am " + getDayName(day));
                ad.setMessage(m2.getToday(day));
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }

        });
        mCardView.addCard(card2);

        MyPlayCard card3 = new MyPlayCard(istDark,"Menü 3", m3.getToday(day), "#4caf50", "#4caf50", true, true, false);
        card3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
                ad.setCancelable(true);
                ad.setTitle("Menü 3 am " + getDayName(day));
                ad.setMessage(m3.getToday(day));
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }

        });
        mCardView.addCard(card3);

        MyPlayCard salad = new MyPlayCard(istDark,"Salat", getSalatForDay(day), "#2196F3", "#2196F3", true, true, false);
        salad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanFragment.this.getContext()).create();
                ad.setCancelable(true);
                ad.setTitle("Salat am " + getDayName(day));
                ad.setMessage(getSalatForDay(day) + "\n\nSalat nur für Schüler auf Bestellung während der Schulzeit!");
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        });
        mCardView.addCard(salad);

        mCardView.refresh();
    }

    public String getSalatForDay(int day) {
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

    public String getDayName(int day) {
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
    }
}