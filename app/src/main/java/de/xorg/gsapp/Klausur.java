package de.xorg.gsapp;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Klausur {
    private String title;
    private Date datum;

    public Klausur() {
    }

    public Klausur(String title, Date datum) {
        this.title = title;
        this.datum = datum;
    }

    public Klausur(JSONObject inp) throws JSONException, ParseException {
        this.title = inp.getString("title");
        this.datum = new SimpleDateFormat("dd/MM/yyyy").parse(inp.getString("date"));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject outp = new JSONObject();
        outp.put("title", title);
        outp.put("date", new SimpleDateFormat("dd/MM/yyyy").format(datum));
        return outp;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Date getDate() {
        return this.datum;
    }

    public void setDate(Date datum) {
        this.datum = datum;
    }

    public String getDateString() { return new SimpleDateFormat("dd.MM.yyyy").format(this.datum); }

    //https://stackoverflow.com/questions/23323792/android-days-between-two-dates
    public String getRemainingTime() {
        Calendar klausurTag = Calendar.getInstance();
        klausurTag.setTime(this.datum);
        long msDiff = klausurTag.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
        long weeksDiff = daysDiff / 7;
        if (daysDiff == 0)
            return "heute";
        else
            if (daysDiff > 7) {
                if (weeksDiff == 1) {
                    return "in 1 Woche";
                } else {
                    return "in " + weeksDiff + " Wochen";
                }
            } else if (daysDiff == 1) {
                return "morgen";
            } else {
                return "in " + daysDiff + " Tagen";
            }
    }

    public String getKursNummer() {
        if(Pattern.matches("^.+?[0-9]$", this.title)) {
            Matcher kursNr = Pattern.compile("[0-9]").matcher(this.title);
            return kursNr.find() ? kursNr.group() : "";
        } else return "";
    }

    //Kursarbeit = eA = GROSSGESCHRIEBEN
    public boolean isKursarbeit() {
        return Pattern.matches("([A-Z]+(?!.*[0-9]))", this.title);
    }

    public String getDesc() {
        return getLongName() + " am " + getDateString();
    }

    public String getLongName() {
        if(this.title.equals("Keine Klausuren mehr!"))  return "Keine Klausuren mehr!";
        if(this.title.startsWith("Fehler:")) return this.title;
        String fachName = Util.LongName(getFachShort());
        if(Pattern.matches("[a-z].+N[0-9]", this.title)) fachName += " Neu";
        return isKursarbeit() ? String.format("%s Kursarbeit", fachName) : String.format("%s %s Klausur", fachName, getKursNummer());
    }

    public String getIconText() {
        if(this.title.equals("Keine Klausuren mehr!"))  return "\uD83D\uDE0A"; else if (this.title.startsWith("Fehler:")) return "\uD83D\uDE1E"; else return this.title; //😊/😞/Titel
    }

    public String getFachShort() { //TODO: Crash-Safe?
        if(this.title.equals("Keine Klausuren mehr!"))  return "et"; //gelb
        if(this.title.startsWith("Fehler:"))  return "ma"; //rot
        if(Pattern.matches("[a-z].+N[0-9]", this.title)) { //frN1
            Matcher kursNeu = Pattern.compile(".+?(?=N)").matcher(this.title);
            return kursNeu.find() ? kursNeu.group() : this.title;
        } else if (Pattern.matches("([a-z]+(?!N)[0-9])", this.title)) { //ge1
            Matcher kursNr = Pattern.compile("([a-z]+)").matcher(this.title);
            return kursNr.find() ? kursNr.group() : this.title;
        } else if (Pattern.matches("([A-Z]+[0-9])", this.title)) { //GE1 #evtl
            Matcher lkursNr = Pattern.compile("([A-Z]+)").matcher(this.title);
            return lkursNr.find() ? lkursNr.group() : this.title;
        } else if (Pattern.matches("([A-Z]+(?!.*[0-9]))", this.title)) { //EN
            Matcher lkurs = Pattern.compile("[A-Z]+").matcher(this.title);
            return lkurs.find() ? lkurs.group() : this.title;
        } else {
            Matcher fallback = Pattern.compile("([A-Za-z]+)").matcher(this.title);
            return fallback.find() ? fallback.group() : this.title;
        }
    }
}