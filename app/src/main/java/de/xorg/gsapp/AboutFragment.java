package de.xorg.gsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;


public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView VERSION_LABEL = ((TextView) getView().findViewById(R.id.versionText));
        Button XBUTTON = ((Button) getView().findViewById(R.id.xorgButton));

        ((Button) getView().findViewById(R.id.homepageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xorg.ga/"));
                startActivity(browserIntent);
            }
        });

        XBUTTON.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*Toast.makeText(AboutFragment.this.getContext(), "Entwickereinstellungen werden geöffnet...", Toast.LENGTH_SHORT).show();
                Intent intentes = new Intent(AboutFragment.this.getContext(), DeveloperSettings.class);
                startActivity(intentes);*/

                TextView showText = new TextView(AboutFragment.this.getContext());
                try {
                    File dlog = new File(AboutFragment.this.getContext().getFilesDir(), "/timber.log");
                    if(dlog.exists())
                        showText.setText(Files.toString(dlog, Charsets.UTF_8));
                    else
                        showText.setText("Keine Log-Datei gefunden!");
                } catch(IOException io) {
                    showText.setText("IOException beim Lesen der Log-Datei!");
                    io.printStackTrace();
                }

                showText.setTextIsSelectable(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutFragment.this.getContext());
                // Build the Dialog
                builder.setView(showText)
                        .setTitle("Log-Datei anzeigen")
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .show();
                return false;
            }
        });

        VERSION_LABEL.setText(Util.getVersion(this.getContext()));
        VERSION_LABEL.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    StringBuilder NOTE = new StringBuilder();

                    NOTE.append("--> Note\n");
                    NOTE.append("Diese Informationen werden nicht an irgendjemanden\n");
                    NOTE.append("weiterverkauft oder an einen Server gesendet! Sie\n");
                    NOTE.append("sind nur zur Fehleranalyse gedacht und sollen, wenn\n");
                    NOTE.append("vom Entwickler angefordert, von ihnen weitergeleitet\n");
                    NOTE.append("werden. Wenn sie die Informationen weiterleiten möchten,\n");
                    NOTE.append("tippen sie hier auf SENDEN. Danke");


                    AlertDialog.Builder alert = new AlertDialog.Builder(AboutFragment.this.getContext());

                    alert.setTitle("Debug-Informationen");
                    alert.setMessage("==[ GSApp 5.x »Merlin« ]==\n" + Util.dataLoader(AboutFragment.this.getActivity()) + "\n" + NOTE.toString());

                    alert.setCancelable(false);

                    alert.setPositiveButton("SENDEN", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String Sharer = Util.dataLoader(AboutFragment.this.getActivity());
                            Toast.makeText(AboutFragment.this.getContext(), "Senden sie diese Informationen per Mail an xorgmc@gmail.com", Toast.LENGTH_LONG).show();
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "GSAPP DEBUG INFORMATION\n" + Sharer);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                        }
                    });

                    alert.setNegativeButton("SCHLIESSEN", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });

                    alert.show();
                } catch(Exception e) {
                    Toast.makeText(AboutFragment.this.getContext(), "Fehler beim Anzeigen der Debug-Informationen", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String dataLoader(Activity ct) {
        final StringBuilder RITT = new StringBuilder();

        String klasse = PreferenceManager.getDefaultSharedPreferences(ct).getString("klasse", "");
        String klasseD;
        if(klasse.isEmpty()) { klasseD = "NONE"; } else { klasseD = klasse; }

        RITT.append("--> DEBUG <--\n");
        RITT.append("\n");
        RITT.append("--> Config\n");
        RITT.append("CONFIG.KLASSE=" + klasseD + "\n");
        RITT.append("CONFIG.SERVICE=" + String.valueOf(PreferenceManager.getDefaultSharedPreferences(ct).getInt("check", 0)) + "\n");
        RITT.append("CONFIG.ASYNC=" + Util.bolToStr(PreferenceManager.getDefaultSharedPreferences(ct).getBoolean("loadAsync", false)) + "\n");
        RITT.append("--> Build\n");
        RITT.append("BUILD.DEBUG=" + String.valueOf(BuildConfig.DEBUG) + "\n");
        RITT.append("BUILD.VERSIONCODE=" + Util.getVersionID(ct) + "\n");
        RITT.append("BUILD.VERSIONNAME=" + Util.getVersionCode(ct) + "\n");
        RITT.append("BUILD.VERSION=" + Util.getVersion(ct).replace(" ", "_") + "\n");
        RITT.append("BUILD.BUILD=" + ct.getString(R.string.build) + "\n");
        RITT.append("BUILD.DEBUG=" + String.valueOf(BuildConfig.DEBUG) + "\n");
        RITT.append("\n");
        RITT.append("--> Device\n");


        // Bildschirmgroesse
        DisplayMetrics dm = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / (double) dens;
        double hi = (double) height / (double) dens;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double si = Math.sqrt(x + y);
        si = Math.round(si * 100);
        si = si / 100;
        String BILDSCHIRMGROESSE = String.valueOf(si);

        // DPI
        DisplayMetrics metrics = ct.getResources().getDisplayMetrics();
        int densityDpi = (int) (metrics.density * 160f);
        String DPI = String.valueOf(densityDpi);

        // Bildschirmaufloesung
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int heightA = displaymetrics.heightPixels;
        int widthA = displaymetrics.widthPixels;
        String BILDSCHIRMAUFLOESUNG = String.valueOf(widthA) + "x" + String.valueOf(heightA);

        // Android-Version
        String ANDROIDVERSION = Build.VERSION.RELEASE;
        String ANDROIDSDK = String.valueOf(Build.VERSION.SDK_INT);

        // Handydaten
        String HERSTELLER = Build.MANUFACTURER;
        String HANDYTYP = getDeviceName();

        RITT.append("DEVICE.SCREEN.SIZE=" + BILDSCHIRMGROESSE + "\n");
        RITT.append("DEVICE.SCREEN.DPI=" + DPI + "\n");
        RITT.append("DEVICE.SCREEN.RESOLUTION=" + BILDSCHIRMAUFLOESUNG + "\n");
        RITT.append("DEVICE.ANDROID.VERSION=" + ANDROIDVERSION + "\n");
        RITT.append("DEVICE.ANDROID.SDK=" + ANDROIDSDK + "\n");
        RITT.append("DEVICE.MANUFACTURER=" + HERSTELLER + "\n");
        RITT.append("DEVICE.DESCRIPTOR=" + HANDYTYP + "\n");

        return RITT.toString();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
