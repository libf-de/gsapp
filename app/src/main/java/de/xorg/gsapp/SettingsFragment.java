package de.xorg.gsapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class SettingsFragment extends Fragment {

    Spinner klasse;
    RadioButton phCall;
    RadioButton phDial;
    CheckBox VPSync;
    RadioButton SVNever;
    RadioButton SVUser;
    RadioButton SVAlways;
    Button EBAutologin;
    Button ShowPlayStore;

    boolean initComplete = false;
    boolean permEnable = false;

    String selectedKlasse;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 6375: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (phCall != null && phDial != null) {
                        phCall.setChecked(true);
                        phDial.setChecked(false);
                    }

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                    editor.putBoolean("direct_call", true);
                    editor.commit();

                    permEnable = false;

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Toast.makeText(SettingsFragment.this.getContext(), "OK", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        klasse = (Spinner) getView().findViewById(R.id.klasseEinst);
        phCall = (RadioButton) getView().findViewById(R.id.phoneCall);
        phDial = (RadioButton) getView().findViewById(R.id.phoneDial);
        VPSync = (CheckBox) getView().findViewById(R.id.vpAsync);
        SVNever = (RadioButton) getView().findViewById(R.id.ntf_never);
        SVUser = (RadioButton) getView().findViewById(R.id.ntf_user);
        SVAlways = (RadioButton) getView().findViewById(R.id.ntf_always);
        EBAutologin = (Button) getView().findViewById(R.id.AutologinConfig);
        ShowPlayStore = (Button) getView().findViewById(R.id.StoreButton);


        List<String> adp = new ArrayList<String>();
        adp.addAll(Arrays.asList("keine", "5.1", "5.2", "5.3", "5.4", "5.5", "6.1", "6.2", "6.3", "6.4", "6.5", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "9.4", "9.5", "10.1", "10.2", "10.3", "10.4", "10.5"));
        adp.add("A" + (Calendar.getInstance().get(Calendar.YEAR) % 100));
        adp.add("A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 1));
        adp.add("A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 2));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this.getContext(), android.R.layout.simple_spinner_item, adp);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        klasse.setAdapter(adapter);
        selectedKlasse = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("klasse", "");
        klasse.setSelection(adapter.getPosition(selectedKlasse));

        klasse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String Klasse = ((String) adapterView.getSelectedItem()).replace("keine", "");
                if(Klasse.equals(selectedKlasse)) { return; }
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                editor.putString("klasse", Klasse);
                editor.commit();
                Toast.makeText(SettingsFragment.this.getContext(), "Gespeichert", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        VPSync.setChecked(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("loadAsync", false));

        if (PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("direct_call", false)) {
            phCall.setChecked(true);
            phDial.setChecked(false);
        } else {
            phCall.setChecked(false);
            phDial.setChecked(true);
        }

        int VPChecker = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getInt("check", 0);
        switch (VPChecker) {
            case 0:
                SVAlways.setChecked(false);
                SVUser.setChecked(false);
                SVNever.setChecked(true);
                break;
            case 1:
                SVAlways.setChecked(false);
                SVUser.setChecked(true);
                SVNever.setChecked(false);
                break;
            case 2:
                SVAlways.setChecked(true);
                SVUser.setChecked(false);
                SVNever.setChecked(false);
                break;
            default:
                SVNever.setChecked(true);
                SVUser.setChecked(false);
                SVAlways.setChecked(false);
                break;
        }

        phCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (ContextCompat.checkSelfPermission(SettingsFragment.this.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        phCall.setOnCheckedChangeListener(null);
                        phCall.setChecked(false);
                        phCall.setOnCheckedChangeListener(this);


                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        phDial.setChecked(true);
                                        if (!ActivityCompat.shouldShowRequestPermissionRationale(SettingsFragment.this.getActivity(),
                                                Manifest.permission.CALL_PHONE)) {
                                            ActivityCompat.requestPermissions(SettingsFragment.this.getActivity(),
                                                    new String[]{Manifest.permission.CALL_PHONE}, 6375);
                                        }

                                        Toast.makeText(SettingsFragment.this.getContext(), "Bitte danach erneut \"Direkt anrufen\" wählen.", Toast.LENGTH_SHORT).show();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        phDial.setChecked(true);
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsFragment.this.getContext());
                        builder.setMessage("Um Telefonnummern direkt anzurufen wird ab Android 6.0 eine Berechtigung benötigt. Soll die Berechtigung aktiviert werden? Sie wird nur für die Funktion \"Kontakt\" verwendet.").setPositiveButton("Ja", dialogClickListener).setNegativeButton("Nein", dialogClickListener).show();
                    } else {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                        editor.putBoolean("direct_call", true);
                        editor.commit();
                    }
                } else {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                    editor.putBoolean("direct_call", false);
                    editor.commit();
                }
            }
        });

        SVNever.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                    editor.putInt("check", 0);
                    editor.commit();

                    /*CheckService CS = new CheckService();
                    if (CS.CheckAlarm(SettingsFragment.this.getContext())) {
                        CS.CancelAlarm(SettingsFragment.this.getContext());
                    }*/

                    Toast.makeText(SettingsFragment.this.getContext(), "Benachrichtigung deaktiviert", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SVUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                    editor.putInt("check", 1);
                    editor.commit();

                    /*CheckService CS = new CheckService();
                    if (!CS.CheckAlarm(SettingsFragment.this.getContext())) {
                        CS.SetAlarm(SettingsFragment.this.getContext());
                    }*/

                    Toast.makeText(SettingsFragment.this.getContext(), "Benachrichtigung aktiviert", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SVAlways.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                    editor.putInt("check", 2);
                    editor.commit();

                    /*CheckService CS = new CheckService();
                    if (!CS.CheckAlarm(SettingsFragment.this.getContext())) {
                        CS.SetAlarm(SettingsFragment.this.getContext());
                    }*/

                    Toast.makeText(SettingsFragment.this.getContext(), "Benachrichtigung aktiviert", Toast.LENGTH_SHORT).show();
                }
            }
        });


        EBAutologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // custom dialog
                final Dialog dialog = new Dialog(SettingsFragment.this.getContext());
                dialog.setContentView(R.layout.anmeldung);
                dialog.setTitle("Automatische Anmeldung");

                // set the custom dialog components - text, image and button
                final TextView User = (TextView) dialog.findViewById(R.id.username);
                final CheckBox Use = (CheckBox) dialog.findViewById(R.id.autologinEnable);
                String Username = Datenspeicher.getUser(SettingsFragment.this.getContext());
                if (Username.equalsIgnoreCase("")) {
                    Use.setChecked(false);
                    ((TextView) dialog.findViewById(R.id.username)).setText("");
                    ((TextView) dialog.findViewById(R.id.password)).setText("");
                } else {
                    Use.setChecked(true);
                    ((TextView) dialog.findViewById(R.id.username)).setText(Datenspeicher.getUser(SettingsFragment.this.getContext()));
                    try {
                        String PASSWORD_STATE = Datenspeicher.getPassword(SettingsFragment.this.getContext());
                        if (PASSWORD_STATE.startsWith("error")) {
                            if (PASSWORD_STATE.contains("nocb")) {
                                Toast.makeText(SettingsFragment.this.getContext(), "Fehler beim Laden des Passworts: Kontrollbyte fehlt!", Toast.LENGTH_SHORT).show();
                                ((TextView) dialog.findViewById(R.id.password)).setText("");
                            } else {
                                Toast.makeText(SettingsFragment.this.getContext(), "Fehler beim Laden des Passworts!", Toast.LENGTH_SHORT).show();
                                ((TextView) dialog.findViewById(R.id.password)).setText("");
                            }
                        } else {
                            ((TextView) dialog.findViewById(R.id.password)).setText(PASSWORD_STATE);
                        }
                    } catch (Exception e) {
                        ((TextView) dialog.findViewById(R.id.password)).setText("");
                        e.printStackTrace();
                    }
                }

                Button saveButton = (Button) dialog.findViewById(R.id.serverSave);
                Button cancelButton = (Button) dialog.findViewById(R.id.serverCancel);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Use.isChecked()) {
                            Datenspeicher.saveUser(User.getText().toString(), SettingsFragment.this.getContext());
                            if (!Datenspeicher.savePassword(((TextView) dialog.findViewById(R.id.password)).getText().toString(), SettingsFragment.this.getContext())) {
                                Toast.makeText(SettingsFragment.this.getContext(), "Fehler beim Verschlüsseln des Passworts", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Datenspeicher.savePassword("", SettingsFragment.this.getContext());
                            Datenspeicher.saveUser("", SettingsFragment.this.getContext());
                        }
                        dialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        VPSync.setChecked(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("loadAsync", false));
        VPSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getContext()).edit();
                editor.putBoolean("loadAsync", b);
                editor.commit();
            }
        });
        VPSync.setLongClickable(true);
        VPSync.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(SettingsFragment.this.getContext()).create();
                ad.setCancelable(true); // This blocks the 'BACK' button
                ad.setTitle("Kurzhilfe");
                ad.setMessage("Synchron laden ist scheller als asynchrones Laden, allerdings kann es (insbesondere bei langsamen Internet) zu Instabilität führen. Wenn die App beim Laden des Vertretungsplans abstürtzt, aktivieren sie Asynchrones laden. Insbesondere für Samsung Galaxy-Handys wird Synchrones laden empfohlen, da das Asynchrone laden hier um die 2-3 Minuten dauert.\n\nSynchron laden = nicht angehakt, asynchron laden = angehakt");
                ad.setButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
                return false;
            }

        });

        ShowPlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=de.xorg.gsapp"));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(SettingsFragment.this.getContext(), "Auf diesem Gerät ist scheinbar kein Google Play Store installiert!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getActivity().setTitle("GSApp - Einstellungen");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
