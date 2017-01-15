package de.xorg.gsapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class CheckService extends BroadcastReceiver {
    final public static String ONE_TIME = "onetime";

    public static Context MC;
    public static NotificationManager NM;

    public static PowerManager pm;
    public static PowerManager.WakeLock wl;

    public static Boolean isRunning = false;

    private GALog l;

    // Wird alle 30 Minuten ausgeführt wenn Service läuft

    /**
     * GSApp Merlin-Rewrite: CheckService.java
     *
     * Geschrieben von Fabian Schillig 2017
     *
     * Neu geschrieben am 12.01.2017 22:30
     *
     */


    /**
     * Erstellt die Benachrichtigung über einen neuen Vertretungsplan
     *
     * @param text  - Inhalt der Benachrichtigung
     * @param datum - Neues Datum
     * @param doc   - JSOUP-Document des Vertretungsplan um Vorschau anzuzeigen
     */

    public static void PostNotification(String text, String datum, Document doc) {
        GALog loc = new GALog(CheckService.MC);
        Intent intent = new Intent(CheckService.MC, VPlanViewer.class);
        PendingIntent pIntent = PendingIntent.getActivity(CheckService.MC, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(CheckService.MC)
                .setSmallIcon(R.drawable.vertretung)
                .setContentTitle("Neuer Vertretungsplan!")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setContentText(text);

        mBuilder.setContentIntent(pIntent);
        try {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            String[] events = parsePage(doc).split("\n");
            // Sets a title for the Inbox in expanded layout
            inboxStyle.setBigContentTitle("Vertretungsplan:");
            // Moves events into the expanded layout
            for (int i = 0; i < events.length; i++) {

                inboxStyle.addLine(events[i]);
            }
            // Moves the expanded layout object into the notification object.
            mBuilder.setStyle(inboxStyle);
        } catch (Exception ex) {
            loc.debug("Error creating expanded notification: " + ex.getMessage());
        }

        NotificationManager mNotificationManager = (NotificationManager) CheckService.MC.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     * VERALTET: Entfernt Leerzeichen und andere unerwünschte Zeichen aus HTML-Code der VP-Seite
     * Wird nicht mehr benötigt, HTML wird per JSOUP verarbeitet
     *
     * @param inpud - HTML-Eingabe
     * @return Gefilterte HTML-Ausgabe
     */

    @Deprecated
    public static String clearUp(String[] inpud) {
        char gf = (char) 34;
        String me = "";
        for (String ln : inpud) {
            ln = ln.replaceAll("\\<.*?>", "");
            ln = ln.replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß");
            ln = ln.replace("                        ", "");
            //ln = ln.replace("        ", "");
            ln = ln.trim();
            ln = ln.replace("	", "");

            if (ln.equals("      ")) {
            } else if (ln.equals("var hoehe = parent.document.getElementById('inhframe').style.height;")) {
            } else if (ln.equals("setFrameHeight();")) {
            } else if (ln.equals("var pageTracker = _gat._getTracker(" + gf + "UA-5496889-1" + gf + ");")) {
            } else if (ln.equals("pageTracker._trackPageview();")) {
            } else if (ln.equals("    ")) {
            } else if (ln.equals("	")) {
            } else if (ln.equals("  ")) {
            } else if (ln.startsWith("var")) {
            } else if (ln.startsWith("document.write")) {
            } else if (ln.equals("")) {
                //} else if(ln.endsWith("&nbsp;")) {
                //	me = me + "XXXX\n";
            } else {
                if (ln.matches(".*\\w.*")) {
                    me = me + ln + "\n";
                } else if (ln.contains("##")) {
                    me = me + ln + "\n";
                }
            }
        }

        return me;
    }

    /**
     * Überprüft ob inputKlasse im Vertretungsplan vorkommt
     *
     * @param doc         - JSOUP-Dokument der VP-Seite
     * @param inputKlasse - Klasse nach der gesucht werden soll
     * @return TRUE wenn Klasse vorkommt
     */

    public static boolean checkClass(Document doc, String inputKlasse) {
        GALog loc = new GALog(CheckService.MC);
        boolean found = false;
        if (inputKlasse.equals("")) {
            return true;
        }
        Iterator it = doc.select("tr[id=Svertretungen],tr[id=Svertretungen] ~ tr").iterator();
        while (it.hasNext()) {
            String[] data = new String[7];
            int dID = 0;
            Iterator it2 = ((Element) it.next()).children().iterator();
            while (it2.hasNext()) {
                data[dID] = ((Element) it2.next()).text();
                dID++;
            }
            String skl = String.valueOf(data[0].charAt(0));
            String SUCL = data[0].replace("/2", " " + skl + ".2").replace("/3", " " + skl + ".3").replace("/4", " " + skl + ".4").replace("/5", " " + skl + ".5");
            Log.d("GSApp", "SUCL=" + SUCL);
            if (SUCL.length() == 1 || SUCL.length() == 2) {
                if (inputKlasse.startsWith(SUCL)) {
                    found = true;
                    break;
                }
            } else if (SUCL.contains(inputKlasse)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Überprüft ob inputKlasse im Vertretungsplan vorkommt
     *
     * @deprecated use {@link #checkClass(Document, String)} ()} instead.
     */
    @Deprecated
    public static boolean checkClass_legacy(String pageBody, String inputKlasse) {
        char gf = (char) 34;
        GALog loc = new GALog(CheckService.MC);

        boolean found = false;

        if (inputKlasse.equals("")) {
            return true;
        }

        if (pageBody != "E") {
            String gPart = pageBody.split("<tr id=\"Svertretungen\">")[1];
            String[] rawC = gPart.split("\n");
            String[] newC = clearUp(rawC).split("\n");
            int counter = 1;
            String klasse = "";

            for (String cnt : newC) {
                if (counter == 1) {
                    klasse = cnt;
                    counter = counter + 1;
                } else if (counter == 2) {
                    counter = counter + 1;
                } else if (counter == 3) {
                    counter = counter + 1;
                } else if (counter == 4) {
                    counter = counter + 1;
                } else if (counter == 5) {
                    counter = counter + 1;
                } else if (counter == 6) {
                    counter = counter + 1;
                } else if (counter == 7) {
                    counter = 1;
                    String skl = String.valueOf(klasse.charAt(0));
                    String SUCL = klasse.replace("/2", " " + skl + ".2");
                    SUCL = SUCL.replace("/3", " " + skl + ".3");
                    SUCL = SUCL.replace("/4", " " + skl + ".4");
                    SUCL = SUCL.replace("/5", " " + skl + ".5");

                    if (SUCL.contains(inputKlasse)) {
                        found = true;
                        break;
                    }
                }
            }

            return found;
        } else {
            return true;
        }
    }

    /**
     * Erstellt Vorschau des Vertretungsplan, jede Vertretung in einer Zeile
     *
     * @param doc - JSOUP-Dokument der VP-Seite
     * @return Vorschau-Text
     */

    public static String parsePage(Document doc) {
        GALog loc = new GALog(CheckService.MC);
        String Klasse = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("klasse", "");
        String outpString = "";
        Iterator it = doc.select("tr[id=Svertretungen],tr[id=Svertretungen] ~ tr").iterator();
        while (it.hasNext()) {
            String[] data = new String[7];
            int dID = 0;
            Iterator it2 = ((Element) it.next()).children().iterator();
            while (it2.hasNext()) {
                data[dID] = ((Element) it2.next()).text();
                dID++;
            }
            String skl = String.valueOf(data[0].charAt(0));
            StringBuilder append = new StringBuilder().append(" ");
            String str = " " + skl + ".3";
            str = " " + skl + ".4";
            String SUCL = data[0].replace("/2", " " + skl + ".2").replace("/3", " " + skl + ".3").replace("/4", " " + skl + ".4").replace("/5", " " + skl + ".5");
            Log.d("GSApp", "SUCL=" + SUCL);
            if (Klasse.equals(" ")) {
                outpString = outpString + formatLine(data, true) + "\n";
            } else if (SUCL.length() == 1 || SUCL.length() == 2) {
                if (Klasse.startsWith(SUCL)) {
                    outpString = outpString + formatLine(data, false) + "\n";
                }
            } else if (SUCL.contains(Klasse)) {
                String oneEntry = data[1] + ". - " + data[2] + " => " + data[5];
                outpString = outpString + formatLine(data, false) + "\n";
            }
        }
        return outpString;
    }

    /**
     * Formatiert eine Vertretung nach Bemerkung (Ausfall, etc.)
     *
     * @param data       - Daten-Eingabe
     * @param showKlasse - Klasse anzeigen ja/nein
     * @return Gefilterte Vertretung
     */
    public static String formatLine(String[] data, boolean showKlasse) {
        String prefix = "";
        if (showKlasse) {
            prefix = data[0] + ": ";
        }
        if (data[6].equals("Ausfall")) {
            return prefix + data[1] + ". - " + data[2] + " => Ausfall";
        }
        if (data[6].equals("Stillbesch.")) {
            return prefix + data[1] + ". - " + data[2] + " => Stillb.";
        }
        if (data[6].equals("AA")) {
            return prefix + data[1] + ". - " + data[2] + " => Arbeitsa.";
        }
        if (data[2].equals(data[5])) {
            return prefix + data[1] + ". - " + data[2];
        }
        return prefix + data[1] + ". - " + data[2] + " => " + data[5];
    }

    @Deprecated
    public static String parsePage_legacy(String pageBody) {
        char gf = (char) 34;
        GALog loc = new GALog(CheckService.MC);

        String Klasse = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("klasse", "");

        String outpString = "";

        //loc.debug("CheckService/Parse/PageDebug: +" + pageBody.replace("\n", "*BR*") + "+");

        if (pageBody != "E") {
            String gPart = pageBody.split("<tr id=\"Svertretungen\">")[1];
            //loc.debug("CheckService/Parse/PartDebug: +" + gPart.replace("\n", "*BR") + "+");

            String[] rawC = gPart.split("\n");

            String[] newC = clearUp(rawC).split("\n");

            int counter = 1;
            int va = 0;
            String klasse = "";
            String stunde = "";
            String orgfach = "";
            String vertret = "";
            String raum = "";
            String verfach = "";
            String bemerkung = "";

            for (String cnt : newC) {
                if (counter == 1) {
                    klasse = cnt;
                    counter = counter + 1;
                } else if (counter == 2) {
                    stunde = cnt;
                    counter = counter + 1;
                } else if (counter == 3) {
                    orgfach = cnt;
                    counter = counter + 1;
                } else if (counter == 4) {
                    vertret = cnt;
                    counter = counter + 1;
                } else if (counter == 5) {
                    raum = cnt;
                    counter = counter + 1;
                } else if (counter == 6) {
                    verfach = cnt;
                    counter = counter + 1;
                } else if (counter == 7) {
                    bemerkung = cnt;
                    counter = 1;

                    if (Klasse.equals("")) {
                        String oneEntry = klasse + ": " + stunde + ". - " + orgfach + " => " + verfach;
                        outpString += oneEntry + "\n";
                        va = va + 1;
                        klasse = "";
                        stunde = "";
                        orgfach = "";
                        vertret = "";
                        raum = "";
                        verfach = "";
                        bemerkung = "";
                    } else {
                        String skl = String.valueOf(klasse.charAt(0));
                        String SUCL = klasse.replace("/2", " " + skl + ".2");
                        SUCL = SUCL.replace("/3", " " + skl + ".3");
                        SUCL = SUCL.replace("/4", " " + skl + ".4");
                        SUCL = SUCL.replace("/5", " " + skl + ".5");

                        if (SUCL.contains(Klasse)) {
                            String oneEntry = stunde + ". - " + orgfach + " => " + verfach;
                            outpString += oneEntry + "\n";
                            va = va + 1;
                        }
                    }
                }
            }
            return outpString;
        } else {
            loc.debug("CheckServce/Parse: Error parsing (LOAD=E) page");
            return "null";
        }
    }

    /**
     * Datum des zuletzt angesehenem Vertretungsplan auslesen
     *
     * @return Datum in YYYYMMDD
     */
    public static String getRiddenDate() {
        String DATUM = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("readDate", "ERR");
        return DATUM;
    }

    /**
     * Setzt das Datum des zuletzt angesehenem Vertretungsplan
     *
     * @param when Datum in YYYYMMDD
     */

    public static void setRiddenDate(String when) {
        SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).edit();
        ed.putString("readDate", when);
        ed.commit();
    }

    /**
     * Liest die gespeicherte Klasse aus
     *
     * @return Gespeicherte Klasse
     */
    public static String getKlasse() {
        String CLASS = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("klasse", "");
        return CLASS;
    }

    /**
     * Überprüfungsmodus auslesen
     *
     * @return Überprüfungsmodus
     */
    public static int getMode() {
        return PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getInt("check", 0);
    }

    /**
     * Entfernt den Wakelock
     */

    public static void RemoveWakelock() {
        try {
            if (wl != null) {
                wl.release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Hauptfunktion
     *
     * @param context
     * @param intent
     */
    @SuppressLint("Wakelock")
    @Override
    public void onReceive(Context context, Intent intent) {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GSApp Check");

        l = new GALog(context);

        //WakeLock aktivieren
        if (wl != null) {
            wl.acquire();
        }

        MC = context;
        NM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Util.hasInternet(context)) {
            new GetDate(context).execute();
        } else {
            wl.release();
        }
    }

    /**
     * Startet den CheckService
     *
     * @param context
     */
    public void SetAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CheckService.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        isRunning = true;

        if (l != null) {
            l.debug("CheckService gestartet");
        } else {
            Log.d("fallbacklog", "CheckService gestartet");
        }

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * PreferenceManager.getDefaultSharedPreferences(context).getInt("checkInt", 1800), pi);
    }

    /**
     * Beendet den CheckService
     *
     * @param context
     */
    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, CheckService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        isRunning = false;

        if (l != null) {
            l.debug("CheckService angehalten");
        }

        alarmManager.cancel(sender);
    }


    /**
     * Erzwingt die Suche nach einem neuen Vertretungsplan einmalig (Entwickler)
     *
     * @param c
     */
    public void ForceCheck(Context c) {
        MC = c;
        NM = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        Toast.makeText(c, "Erzwinge Überprüfung", Toast.LENGTH_SHORT).show();

        if (Util.hasInternet(c)) {
            Toast.makeText(c, "Internetverbindung besteht...", Toast.LENGTH_SHORT).show();
            new GetDate(c).execute();
        }
    }

    /**
     * Überprüft ob der CheckService läuft
     *
     * @param context
     * @return TRUE wenn der CheckService läuft
     */
    public Boolean CheckAlarm(Context context) {
        return isRunning;
    }
}

class GetDate extends AsyncTask<String, Void, String> {
    Context cont;

    GetDate(Context ct) {
        this.cont = ct;
    }

    protected String doInBackground(String... message) {
        HttpClient httpclient;
        HttpGet request;
        HttpResponse response = null;
        String result = "";

        try {
            httpclient = new DefaultHttpClient();
            request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
            response = httpclient.execute(request);
        } catch (Exception e) {
            result = "error";
        }

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result = result + line + "\n";
            }
        } catch (Exception e) {
            result = "error";
        }
        return result;
    }


    protected void onPostExecute(String result) {
        try {
            GALog loc = new GALog(CheckService.MC);
            if (result.equals("error")) {
                loc.debug("CheckService: RESULT ERROR");
                return;
            }
            Document doc = Jsoup.parse(result); //td.rundeEckenOben.vpUeberschr
            String gDate = doc.select("td[class*=vpUeberschr]").first().text().replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "").replaceAll("[^0-9.]", "");
            String serverDate = new SimpleDateFormat("yyyyMMdd").format(new SimpleDateFormat("dd.MM.yyyy").parse(gDate));
            loc.debug("CheckService/GetDate: Datum von Server erhalten: +" + serverDate + "+");
            String riddenDate = CheckService.getRiddenDate();
            loc.debug("CheckService/GetDate: Gespeichertes Datum: -" + riddenDate + "-");
            if (riddenDate.equals("ERR")) {
                CheckService.setRiddenDate(serverDate);
                CheckService.PostNotification("Es ist ein neuer Vertretungsplan verf\u00fcgbar!", serverDate, doc);
            } else {
                if (result.equals("error")) {
                    loc.error("CheckService/GetDate: Serverfehler (RD:" + riddenDate + ")");
                } else {
                    int rideDate = Integer.parseInt(riddenDate);
                    int newDate = Integer.parseInt(serverDate);
                    if (newDate > rideDate) {
                        loc.debug("CheckService/GetDate: Neuer Plan verf\u00fcgbar");
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).edit();
                        editor.putString("readDate", serverDate);
                        editor.commit();
                        if (CheckService.getMode() != 1) {
                            CheckService.PostNotification("Es ist ein neuer Vertretungsplan verf\u00fcgbar!", String.valueOf(newDate), doc);
                        } else if (CheckService.checkClass(doc, CheckService.getKlasse())) {
                            CheckService.PostNotification("Du hast morgen Vertretung!", String.valueOf(newDate), doc);
                        }
                        loc.debug("CheckService/GetDate: Neuer Plan verfügbar-POST NTF");
                    } else {
                        loc.debug("CheckService/GetDate: Kein neuer Plan verfügbar");
                    }
                }
            }
            CheckService.RemoveWakelock();
        } catch (Exception ex) {
            ex.printStackTrace();
            CheckService.RemoveWakelock();
        }
    }


    @Deprecated
    protected void onPostExecute_legacy(String result) {
        try {
            char gf = (char) 34;
            GALog loc = new GALog(CheckService.MC);

            if (result != "E") {
                String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
                String gDate = gPart.replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "")/*.split(".")*/;
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat RC = new SimpleDateFormat("yyyyMMdd");
                Date dt = df.parse(gDate);
                String GRC = RC.format(dt);
                loc.debug("CheckService/GetDate: Datum von Server erhalten: +" + GRC + "+");

                String riddenDate = CheckService.getRiddenDate();
                loc.debug("CheckService/GetDate: Gespeichertes Datum: -" + riddenDate + "-");

                if (riddenDate.equals("ERR")) {
                    CheckService.setRiddenDate(GRC);
                    //CheckService.PostNotification("Es ist ein neuer Vertretungsplan verfügbar!", GRC, result);
                } else if (result.equals("error")) {
                    loc.error("CheckService/GetDate: Serverfehler (RD:" + riddenDate + ")");
                } else {
                    int rideDate = Integer.parseInt(riddenDate);
                    int newDate = Integer.parseInt(GRC);

                    if (newDate > rideDate) {
                        loc.debug("CheckService/GetDate: Neuer Plan verfügbar");
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).edit();
                        editor.putString("readDate", GRC);
                        editor.commit();
                        if (CheckService.getMode() == 1) {
                            if (CheckService.checkClass_legacy(result, CheckService.getKlasse())) {
                                //CheckService.PostNotification("Du hast morgen Vertretung!", String.valueOf(newDate), result);
                            }
                        } else {
                            //CheckService.PostNotification("Es ist ein neuer Vertretungsplan verfügbar!", String.valueOf(newDate), result);
                        }
                        loc.debug("CheckService/GetDate: Neuer Plan verfügbar-POST NTF");
                    } else {
                        loc.debug("CheckService/GetDate: Kein neuer Plan verfügbar");
                    }
                }
            }
            CheckService.RemoveWakelock();
        } catch (Exception ex) {
            ex.printStackTrace();
            CheckService.RemoveWakelock();
        }
    }

}