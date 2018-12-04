package de.xorg.gsapp;

/**
 * Created by xorg on 08.01.15.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DeveloperSettings extends AppCompatActivity {

    EditText checkInt;
    EditText rotateMode;
    ToggleButton showDeveloper;
    EditText themeMode;
    ToggleButton debugMode;
    EditText debugsrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setThemeUI(this);
        setContentView(R.layout.devsetting);

        Button help = (Button) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelp();
            }
        });

        checkInt = (EditText) findViewById(R.id.checktime);
        rotateMode = (EditText) findViewById(R.id.rotate);
        showDeveloper = (ToggleButton) findViewById(R.id.developer);
        themeMode = (EditText) findViewById(R.id.thememode);
        debugMode = (ToggleButton) findViewById(R.id.writelog);
        debugsrc = (EditText) findViewById(R.id.debugsrc);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        checkInt.setText(String.valueOf(pref.getInt("checkInt", 1800)));
        rotateMode.setText(String.valueOf(pref.getInt("rotateMode", 1)));
        showDeveloper.setChecked(pref.getBoolean("devMode", false));
        themeMode.setText(pref.getString("themeMode", "android"));
        debugMode.setChecked(pref.getBoolean("debug", false));
        debugsrc.setText(String.valueOf(pref.getInt("debugSrc", 0)));

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(DeveloperSettings.this).edit();
                if(checkInt.getText().toString() != null) { editor.putInt("checkInt", Integer.parseInt(checkInt.getText().toString())); }
                if(rotateMode.getText().toString() != null) { editor.putInt("rotateMode", Integer.parseInt(rotateMode.getText().toString())); }
                editor.putBoolean("devMode", showDeveloper.isChecked());
                if(themeMode.getText().toString() != null) { editor.putString("themeMode", themeMode.getText().toString()); }
                if(debugsrc.getText().toString() != null) { editor.putInt("debugSrc", Integer.parseInt(debugsrc.getText().toString())); }
                if (!PreferenceManager.getDefaultSharedPreferences(DeveloperSettings.this).getBoolean("debug", false)) {
                    if (debugMode.isChecked() && ContextCompat.checkSelfPermission(DeveloperSettings.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        editor.commit();
                        Intent intent = new Intent(DeveloperSettings.this, PermissionManager.class);
                        intent.putExtra("reason", "debug");
                        finish();
                        startActivity(intent);
                    } else {
                        editor.putBoolean("debug", debugMode.isChecked());
                    }
                }
                editor.commit();
                Toast.makeText(DeveloperSettings.this, "Einstellungen gespeichert", Toast.LENGTH_SHORT).show();
                DeveloperSettings.this.finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Bitte drücke SAVE zum Speichern und zurückkehren!", Toast.LENGTH_SHORT).show();
        //super.onBackPressed();
        return;
    }

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringBuilder msg = new StringBuilder();

        msg.append("# GSApp Entwickeroptionen #\n");
        msg.append("\n");
        msg.append("MIT VORSICHT VERWENDEN!!!\n");
        msg.append("ANHANG LESEN!!\n");
        msg.append("\n");
        msg.append("\n");
        msg.append("- CheckIntervalTime -\n");
        msg.append("Intervall, in welchem nach neuem Vertretungsplan gesucht wird. Angabe in Sekunden.\nStandard ist 1800 (30min)\n\n");
        msg.append("- UIRotateModeGlobal -\n");
        msg.append("Gibt an, wie die Bildschirmausrichtung ist. 0 = Landscape, 1 = Portrait, 4 = Automatisch\nStandard ist 1 (Portrait)\n\n");
        msg.append("- UIShowDeveloper -\n");
        msg.append("Entwickeroptionen im Hauptmenü anzeigen.\nStandard ist FALSE\n\n");
        msg.append("- UIThemeMode -\n");
        msg.append("Gibt an, welches Thema verwendet wird. Mögliche Werte:\nandroid - Standard dieser Androidversion\nclassic - Froyo-Thema erzwingen\nholo - Holo-Thema erzwingen\nholodark - Holo-Thema dunkel erzwingen\nmaterial - Material-Thema erzwingen\nMöglicherweise sind nicht alle Themen auf allen Geräten verfügbar\nStandard ist android\n\n");
        msg.append("- GALogWriteLog -\n");
        msg.append("Gibt an, ob Logs in Datei geschrieben werden. Aktiviert Debug-Modus.\nStandard ist FALSE\n\n");
        msg.append("- VPlanSourceDebug -\n");
        msg.append("Erlaubt die Fehlersuche im Vertretungsplan. Aktiviert Debug-URLs. Mögliche Werte:\n0 - richtige URL (standard)\n1 - VP ohne Markierungen\n2 - VP mit allen Klassen\n3 - VP mit Superklassen\n\n");
        msg.append("-- Anhang --\n");
        msg.append("WARNUNG: Entwickereinstellungen werden NICHT auf Richtigkeit überprüft. Ungültige Werte können zu Abstürzen führen. Sollte die App nicht mehr starten, löschen sie die Daten der App in den Android-Einstellungen.");

        builder.setMessage(msg.toString())
                .setTitle("GSApp 5.X »Merlin Rewrite«");

        builder.setPositiveButton("Schließen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
