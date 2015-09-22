package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Calendar;

public class SpeiseplanActivity extends AppCompatActivity {

    private CardUI mCardView;
    private SpMenu m1;
    private SpMenu m2;
    private SpMenu m3;
    int displayed = 0;
    boolean istWeekend = false;
    boolean showWeekend = true;
    boolean didLoad = false;
    Button prevDay;
    Button nextDay;
    private GALog l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        Util.setThemeUI(this);
        setContentView(R.layout.activity_speiseplan);

        l = new GALog(this);

        // CardView initialisieren
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide);
        mCardView = (CardUI) findViewById(R.id.cardsview);
        mCardView.setAnimation(anim);
        mCardView.setSwipeable(false);

        // Menüs initialisieren
        m1 = new SpMenu(1);
        m2 = new SpMenu(2);
        m3 = new SpMenu(3);

        Calendar calendar = Calendar.getInstance();
        int cKW = calendar.get(Calendar.WEEK_OF_YEAR);
        String currentKW = String.valueOf(cKW);
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String savedKW = sp.getString("MealKW", "0");

        if(currentKW == savedKW) {
            try {
                File cacheDir = getCacheDir(); // context being the Activity pointer
                File cacheFile = new File(cacheDir, "speiseplan.gxcache");

                if(cacheFile.exists() && cacheFile.canRead()) {
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
                l.error("Konnte nicht aus dem Cache laden: " + e.getMessage());
                e.printStackTrace();
                loadSynced();
            }
        } else if(Util.isNumeric(savedKW) && cKW < Integer.parseInt(savedKW)) {
            try {
                File cacheDir = getCacheDir(); // context being the Activity pointer
                File cacheFile = new File(cacheDir, "speiseplan.gxcache");

                if(cacheFile.exists() && cacheFile.canRead()) {
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
                l.error("Konnte nicht aus dem Cache laden: " + e.getMessage());
                e.printStackTrace();
                loadSynced();
            }
        } else {
            if(!Util.hasInternet(this)) {
                try {
                    File cacheDir = getCacheDir(); // context being the Activity pointer
                    File cacheFile = new File(cacheDir, "speiseplan.gxcache");

                    if(cacheFile.exists() && cacheFile.canRead()) {
                        JSONObject jo = new JSONObject(Files.toString(cacheFile, Charsets.UTF_8));

                        JSONObject M1 = jo.getJSONObject("Meal1");
                        JSONObject M2 = jo.getJSONObject("Meal2");
                        JSONObject M3 = jo.getJSONObject("Meal3");
                        String CKW = jo.getString("KW");

                        m1.fromSaveJSON(M1, CKW);
                        m2.fromSaveJSON(M2, CKW);
                        m3.fromSaveJSON(M3, CKW);

                        Toast.makeText(this, "Kein Internet - gespeicherter Plan ist veraltet!", Toast.LENGTH_SHORT).show();

                        didLoad = true;
                    }
                } catch (Exception e) {
                    l.error("Konnte nicht aus dem Cache laden: " + e.getMessage());
                    e.printStackTrace();
                    loadSynced();
                }
            }
            l.debug("Cache existiert nicht oder ist veraltet!");
            loadSynced();
        }

        prevDay = (Button) findViewById(R.id.prevDay);
        nextDay = (Button) findViewById(R.id.nextDay);


        if(didLoad) {
            if(today == Calendar.SATURDAY || today == Calendar.SUNDAY) {
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
            MyPlayCard cardW = new MyPlayCard("Fehler", "Es besteht keine Internetverbindung und der Zwischenspeicher existiert nicht! Stellen sie eine Internetverbindung her und tippen sie hier!", "#FFFFFF", "#FF0000", true, false);
            cardW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Util.hasInternet(SpeiseplanActivity.this)) {
                        loadSynced();
                    } else {
                        Toast.makeText(SpeiseplanActivity.this, "Es besteht keine Internetverbindung!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mCardView.addCard(cardW);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vcach, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            loadSynced();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void nextDay(View v) {
        displayed = displayed + 1;
        drawCardsForDay(displayed, istWeekend);
    }

    public void prevDay(View v) {
        displayed = displayed - 1;
        drawCardsForDay(displayed, istWeekend);
    }

    public void loadSynced() {
        String result = "";
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://www.schulkueche-bestellung.de/index.php?m=2;1");
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            request.setHeader("Accept-Charset", "utf-8");
            request.setHeader("User-Agent", Util.getUserAgentString(this, true));
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result = result + line + "\n";
            }

            try {
                if (result != "E") {
                    parseTable(result);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                l.critical("Konnte Serverantwort nicht verarbeiten: " + ex.getMessage());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseTable(String data) {
        String output = "";
        try {
            Document doc = Jsoup.parse(data);

            Elements date = doc.select("select option[selected]");
            String KW = date.text().split(" ")[1];
            String Datum = date.text().split("\\|\\|")[1];

            m1.setKW(KW);
            m2.setKW(KW);
            m3.setKW(KW);

            m1.setDatum(Datum);

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
                if(rowItems.size() == 6) {
                    for (int j = 0; j < rowItems.size(); j++) {
                        Element clear = rowItems.get(j);
                        clear.getElementsByClass("zusatzstoff").remove();
                        String meal = clear.text().replaceAll(" ,", ",");
                        switch (j) {
                            case 0:
                                break;
                            case 1:
                                if(i==1) { m1.setMontag(meal); } else if (i==2) { m2.setMontag(meal); } else if (i==3) { m3.setMontag(meal); }
                                break;
                            case 2:
                                if(i==1) { m1.setDienstag(meal); } else if (i==2) { m2.setDienstag(meal); } else if (i==3) { m3.setDienstag(meal); }
                                break;
                            case 3:
                                if(i==1) { m1.setMittwoch(meal); } else if (i==2) { m2.setMittwoch(meal); } else if(i==3) { m3.setMittwoch(meal); }
                                break;
                            case 4:
                                if(i==1) { m1.setDonnerstag(meal); } else if(i==2) { m2.setDonnerstag(meal); } else if(i==3) { m3.setDonnerstag(meal); }
                                break;
                            case 5:
                                if(i==1) { m1.setFreitag(meal); } else if(i==2) { m2.setFreitag(meal); } else if(i==3) { m3.setFreitag(meal); }
                                break;
                            default:
                                break;
                        }
                    }
                }
                output += "\n";
            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString("cacheKW", KW);
            editor.commit();

            didLoad = true;

            File outputDir = getCacheDir(); // context being the Activity pointer
            File outputFile = new File(outputDir, "speiseplan.gxcache");

            if(outputFile.canWrite()) {
                JSONObject root = new JSONObject();

                JSONObject Meal1 = m1.toSaveJSON();
                JSONObject Meal2 = m2.toSaveJSON();
                JSONObject Meal3 = m3.toSaveJSON();

                root.put("KW", KW);

                root.put("Meal1", Meal1);
                root.put("Meal2", Meal2);
                root.put("Meal3", Meal3);

                Files.write(root.toString(), outputFile, Charsets.UTF_8);
                l.debug("Cache erstellt!");
            } else {
                l.critical("Kann nicht in Cache schreiben!");
            }
        } catch (Exception e) {
            l.error("Konnte Serverantwort nicht verarbeiten: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void drawCardsForDay(final int day, final boolean isWeekend) {
        mCardView.clearCards();
        CardStack dateHead = new CardStack();
        //dateHead.setTitle("Kalenderwoche " + m1.getKW());
        String Datem = m1.getDatum();
        String FromDate = Datem.split("-")[0].trim();
        String TillDate = Datem.split("-")[1].trim();
        dateHead.setTitle("Gültig von " + FromDate + " bis " + TillDate);
        mCardView.addStack(dateHead);

        if(isWeekend && showWeekend) {
            final MyPlayCard cardW = new MyPlayCard("Wochenende!", "Heute gibt es keine Schulspeisung!", "#FFFFFF", "#FF0000", true, false);
            cardW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showWeekend = false;
                    drawCardsForDay(day, isWeekend);
                }
            });
            mCardView.addCard(cardW);
        }

        TextView dispDay = (TextView) findViewById(R.id.curDay);
        dispDay.setText(getDayName(day));

        if(day == Calendar.MONDAY) {
            prevDay.setEnabled(false);
            prevDay.setText("← Mo");
        } else {
            prevDay.setEnabled(true);
            prevDay.setText("← " + getDayNameShort(day - 1));
        }

        if(day == Calendar.FRIDAY) {
            nextDay.setEnabled(false);
            nextDay.setText("Fr →");
        } else {
            nextDay.setEnabled(true);
            nextDay.setText(getDayNameShort(day + 1) + " →");
        }

        MyPlayCard card1 = new MyPlayCard("Menü 1", m1.getToday(day), "#f44336", "#f44336", true, true);
        card1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanActivity.this).create();
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

        MyPlayCard card2 = new MyPlayCard("Menü 2", m2.getToday(day), "#ff9800", "#ff9800", true, true);
        card2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanActivity.this).create();
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

        MyPlayCard card3 = new MyPlayCard("Menü 3", m3.getToday(day), "#4caf50", "#4caf50", true, true);
        card3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanActivity.this).create();
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

        MyPlayCard salad = new MyPlayCard("Salat", getSalatForDay(day), "#3f51b5", "#3f51b5", true, true);
        salad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad = new AlertDialog.Builder(SpeiseplanActivity.this).create();
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

class SpMenu {
    int id;
    String Montag;
    String Dienstag;
    String Mittwoch;
    String Donnerstag;
    String Freitag;
    String KW;
    String Datum;
    SpMenu(int idn) {
        id = idn;
    }

    public int getID() {
        return id;
    }
    public String getMontag() { return Montag; }
    public String getDienstag() { return Dienstag; }
    public String getMittwoch() { return Mittwoch; }
    public String getDonnerstag() { return Donnerstag; }
    public String getFreitag() { return Freitag; }

    public String getKW() { return KW; }
    public void setKW(String value) { KW = value; }

    public String getDatum() { return Datum; }
    public void setDatum(String Value) { Datum = Value; }

    public void setMontag(String value) { Montag = value; }
    public void setDienstag(String value) { Dienstag = value; }
    public void setMittwoch(String value) { Mittwoch = value; }
    public void setDonnerstag(String value) { Donnerstag = value; }
    public void setFreitag(String value) { Freitag = value; }

    public String getToday(int day) {
        String Meal;
        switch (day) {
            case Calendar.MONDAY:
                Meal = Montag;
                break;
            case Calendar.TUESDAY:
                Meal = Dienstag;
                break;
            case Calendar.WEDNESDAY:
                Meal = Mittwoch;
                break;
            case Calendar.THURSDAY:
                Meal = Donnerstag;
                break;
            case Calendar.FRIDAY:
                Meal = Freitag;
                break;
            default:
                Meal = "SNERROR" + day;
                break;
        }

        return Meal;
    }

    public JSONObject toSaveJSON() throws JSONException{
        JSONObject jk = new JSONObject();
        jk.put("Montag", Montag);
        jk.put("Dienstag", Dienstag);
        jk.put("Mittwoch", Mittwoch);
        jk.put("Donnerstag", Donnerstag);
        jk.put("Freitag", Freitag);
        jk.put("id", id);
        return jk;
    }

    public void fromSaveJSON(JSONObject input, String KW) throws JSONException {
        JSONObject jo = input;
        Montag = jo.getString("Montag");
        Dienstag = jo.getString("Dienstag");
        Mittwoch = jo.getString("Mittwoch");
        Donnerstag = jo.getString("Donnerstag");
        Freitag = jo.getString("Freitag");
        id = jo.getInt("id");
    }
}