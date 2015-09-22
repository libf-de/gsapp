package de.xorg.gsapp;

/**
 * Created by xorg on 23.12.14.
 *
 * Template zu Datenspeicher
 *
 * Um die App zu compilieren, benennen sie diese Klasse in "Datenspeicher" um
 * und ergänzen sie die Funktionen durch ihre eigenen Funktionalen.
 * Um die Sicherheit der Nutzer zu gewährleisten, sind die genauen Funktionen
 * nicht im Quellcode öffentlich sichtbar. Bitte haben sie dafür Verständnis.
 *
 * Alle Stellen, an denen Code entfernt wurde, sind mit [...] und Kurzbeschreibung versehen
 *
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// [...] Imports für Verschlüsselung

public class DatenspeicherTemplate {

    /*
    Speichert das Passwort verschlüsselt in die App-Einstellungen
     */

    public static boolean savePassword(String pass, Context ct) {
        try {
            if(pass.equals("")) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ct).edit();
                editor.putString("EssenPassword", "");
                editor.commit();
            } else {
                // TODO: Verschlüsselt das Passwort aus der Variable »pass« in die Variable »encryptedValue«, welche in die Einstellungen geschrieben wird
                String encrypedValue = null;

                // [...] (Verschlüsselung und Überprüfung)

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ct).edit();
                editor.putString("EssenPassword", encrypedValue);
                editor.commit();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    Liest und entschlüsselt das Passwort aus den App-Einstellungen
     */

    public static String getPassword(Context ct) {
        try {
            //TODO: Entschlüsselt das Passwort aus den Einstellungen (Variable »epass«) und gibt das entschlüsselte Passwort per »return« zurück
            // [...] (Entschlüsseln)
            String epass = PreferenceManager.getDefaultSharedPreferences(ct).getString("EssenPassword", "");

            if(epass.equals("")) {
                return "";
            } else {
                // [...] (Überprüfung des entschlüsselten Strings)
            }
            // [...] (Fehlerbehandlung der Verschlüsselung)
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public static void saveUser(String user, Context ct) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ct).edit();
        editor.putString("EssenUsername", user);
        editor.commit();
    }

    public static String getUser(Context ct) {
        return PreferenceManager.getDefaultSharedPreferences(ct).getString("EssenUsername", "");
    }


}

