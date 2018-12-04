package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class FirstRunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setThemeUI(this);
        setContentView(R.layout.activity_first_run);

        Spinner kla = (Spinner) findViewById(R.id.spinner);
        List<String> adp = new ArrayList<String>();
        adp.addAll(Arrays.asList("keine", "5.1", "5.2", "5.3", "5.4", "5.5", "6.1", "6.2", "6.3", "6.4", "6.5", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "9.4", "9.5", "10.1", "10.2", "10.3", "10.4", "10.5"));
        adp.add("A" + (Calendar.getInstance().get(Calendar.YEAR) % 100));
        adp.add("A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 1));
        adp.add("A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 2));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, adp);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kla.setAdapter(adapter);
        kla.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Spinner sp = (Spinner) findViewById(R.id.spinner);
                    String Name = (String) sp.getSelectedItem();
                    sp.setTag(Name.replace("keine", ""));
                } catch (Exception e) {
                    Spinner sp = (Spinner) findViewById(R.id.spinner);
                    sp.setTag("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Spinner sp = (Spinner) findViewById(R.id.spinner);
                sp.setTag("");
            }
        });
    }

    public void showTerms(View v) {
        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(true);
        ad.setTitle("Nutzungshinweise");
        ad.setMessage("Da die meisten Nutzer der App jugendlich sind, sind diese Hinweise auch so formuliert, das jeder sie versteht ;)\n\nIch (Autor der App) kann nicht garantieren, das die Vertretungen (und Speisepläne), die die App anzeigt, auch stimmen - diese Daten stammen von der Homepage unserer Schule bzw. von der Essen-Bestellseite. Du kannst also nicht die Ausrede verwenden, die Vertretung hätte nicht in der App gestanden. Weiterhin sind mir deine Interessen, wie lange du die App benutzt, was du sonst noch so auf deinem Handy hast, etc. (deine privaten Daten) vollkommen egal - die bleiben zu 100% bei dir. Trotz sorgfältiger Tests kann es durchaus vorkommen, das die App abstürzt (oder du einen Fehler meldest). In einem solchen Fall kannst du einen Bericht an mich senden, damit ich weiß, warum die App abgestürzt ist (und ich den Fehler beheben kann). Alles was von der App dabei übermittelt wird, kann ich nicht verwenden, um herauszufinden, wer du bist oder um dir zu schreiben. Privatsphäre ist wichtig! Weiterhin ist die App nicht zu 100% von mir selbst geschrieben, sie verwendet sog. Programmbibliotheken. Diese kannst du auf der Projekthomepage einsehen - falls du dich auskennst. Das wars, viel Spaß mit der App :)\n\n\n- Datenschutzerklärung -\n" + getResources().getString(R.string.privacy_disclaimer));
        ad.setButton(AlertDialog.BUTTON_NEUTRAL, "Gelesen und akzeptiert", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void complete(View v) {
        int checkVal = 0;
        if( ((RadioButton) findViewById(R.id.acheck_never)).isChecked() ) {
            checkVal = 0;
        } else if( ((RadioButton) findViewById(R.id.acheck_me)).isChecked() ) {
            checkVal = 1;
        } else if( ((RadioButton) findViewById(R.id.acheck_all)).isChecked() ) {
            checkVal = 2;
        }

        FirebaseService.changePush(this, (checkVal == 1 || checkVal == 2));

        Spinner sp = (Spinner) findViewById(R.id.spinner);


        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("klasse", sp.getTag().toString());
        editor.putInt(Util.Preferences.PUSH_MODE, checkVal);
        editor.putInt("configVer", 2);
        editor.putBoolean(Util.Preferences.FIRST_RUN2, false);
        editor.commit();

        //Intent intent = new Intent(this, MainActivity2.class);
        //startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        complete(null);

        //super.onBackPressed();
        return;
    }
}
