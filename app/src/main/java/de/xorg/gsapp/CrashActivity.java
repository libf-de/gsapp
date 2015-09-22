package de.xorg.gsapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class CrashActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        final String STACKTRACE = getIntent().getStringExtra("error");

        //TextView error = (TextView) findViewById(R.id.errorCode);
        //error.setText(STACKTRACE);

        setTitle("): GSApp ist abgestürzt");

        Button sent = (Button) findViewById(R.id.send);
        sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CrashActivity.this, "Sende Fehlerbericht...", Toast.LENGTH_SHORT).show();
                reportError(STACKTRACE, ((EditText) findViewById(R.id.description)).getText().toString());

                Intent mStartActivity = new Intent(CrashActivity.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(CrashActivity.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) CrashActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });

        Button canc = (Button) findViewById(R.id.cancel);
        canc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mStartActivity = new Intent(CrashActivity.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(CrashActivity.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) CrashActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });

        Button sTrace = (Button) findViewById(R.id.showStackTrace);
        try {
            if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("devMode", false)) {
                sTrace.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            sTrace.setVisibility(View.GONE);
            e.printStackTrace();
        }
        sTrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad = new AlertDialog.Builder(CrashActivity.this).create();
                ad.setCancelable(true); // This blocks the 'BACK' button
                ad.setTitle("Stack Trace");
                ad.setMessage(STACKTRACE);
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                ad.show();
            }
        });
    }

    public void reportError(String trace, String user) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost("http://gsapp.xorg.ga/bugs/report.php");
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("crashData", trace));
            nameValuePairs.add(new BasicNameValuePair("userText", user));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(request);
            Toast.makeText(this, "Fehlerbericht gesendet! Danke", Toast.LENGTH_SHORT).show();
        }catch (Exception ex) {
            Toast.makeText(this, "Fehlerbericht konnte nicht gesendet werden (" + ex.getMessage() + ")!", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}
