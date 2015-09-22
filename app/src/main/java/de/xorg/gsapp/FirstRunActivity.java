package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;


public class FirstRunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        Util.setThemeUI(this);
        setContentView(R.layout.activity_first_run);

        Spinner kla = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klassen_array, android.R.layout.simple_spinner_item);

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
        ad.setMessage("Da die meisten Nutzer der App jugendlich sind, sind diese Hinweise auch so formuliert, das jeder sie versteht ;)\n\nIch (Autor der App) kann nicht garantieren, das die Vertretungen (und Speisepläne), die die App anzeigt, auch stimmen - diese Daten stammen von der Homepage unserer Schule bzw. von der Essen-Bestellseite. Du kannst also nicht die Ausrede verwenden, die Vertretung hätte nicht in der App gestanden. Weiterhin sind mir deine Interessen, wie lange du die App benutzt, was du sonst noch so auf deinem Handy hast, etc. (deine privaten Daten) vollkommen egal - die bleiben zu 100% bei dir. Trotz sorgfältiger Tests kann es durchaus vorkommen, das die App abstürzt (oder du einen Fehler meldest). In einem solchen Fall kannst du einen Bericht an mich senden, damit ich weiß, warum die App abgestürzt ist (und ich den Fehler beheben kann). Alles was von der App dabei übermittelt wird, kann ich nicht verwenden, um herauszufinden, wer du bist oder um dir zu schreiben. Privatsphäre ist wichtig! Weiterhin ist die App nicht zu 100% von mir selbst geschrieben, sie verwendet sog. Programmbibliotheken. Diese kannst du auf der Projekthomepage einsehen - falls du dich auskennst. Das wars, viel Spaß mit der App :)");
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

        CheckService CS = new CheckService();
        if(checkVal == 1 || checkVal == 2) {
            if(!CS.CheckAlarm(this)) {
                CS.SetAlarm(this);
            }
        } else {
            if(CS.CheckAlarm(this)) {
                CS.CancelAlarm(this);
            }
        }

        Spinner sp = (Spinner) findViewById(R.id.spinner);

        Toast.makeText(this, sp.getTag().toString(), Toast.LENGTH_SHORT).show();


        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("klasse", sp.getTag().toString());
        editor.putInt("check", checkVal);
        editor.putInt("configVer", 2);
        editor.putBoolean("isfirstrun", false);
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        complete(null);

        //super.onBackPressed();
        return;
    }
}
