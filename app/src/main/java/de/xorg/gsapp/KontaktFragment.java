package de.xorg.gsapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
//import android.support.v4.app.Fragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import timber.log.Timber;


public class KontaktFragment extends Fragment {


    public KontaktFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kontakt, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles

        Button AnrufLohau = (Button) getView().findViewById(R.id.lohauAnruf);
        Button AnrufDamm = (Button) getView().findViewById(R.id.dammAnruf);

        Button MailLohau = (Button) getView().findViewById(R.id.lohauMail);
        Button MailDamm = (Button) getView().findViewById(R.id.dammMail);
        Button MailXorg = (Button) getView().findViewById(R.id.xorgMail);

        AnrufLohau.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                call("03675702977");
            }
        });

        AnrufDamm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                call("03675468890");
            }
        });

        MailLohau.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sekretariat-lohau@gymson.de"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Bitte Betreff eintragen");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, "Mail senden"));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(KontaktFragment.this.getContext(), "Fehler: Es sind keine E-Mail-Apps installiert!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        MailDamm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sekretariat@gymson.de"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Bitte Betreff eintragen");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, "Mail senden"));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(KontaktFragment.this.getContext(), "Fehler: Es sind keine E-Mail-Apps installiert!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        MailXorg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"xorgmc@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "GSApp");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, "Mail senden"));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(KontaktFragment.this.getContext(), "Fehler: Es sind keine E-Mail-Apps installiert!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Util.prepareMenu(menu, Util.NavFragments.KONTAKT);
        super.onPrepareOptionsMenu(menu);
    }

    public void call(String tel) {
        String ttyp = PreferenceManager.getDefaultSharedPreferences(KontaktFragment.this.getContext()).getString("ruf", "dial");

        if(ttyp.equals("call")) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + tel));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                activityException.printStackTrace();
                Timber.e( "Anruf fehlgeschlagen");
                Toast.makeText(KontaktFragment.this.getContext(), "Fehler: Beim Anrufen ist ein Fehler aufgetreten!\n"+activityException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + tel));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                activityException.printStackTrace();
                Timber.e( "Anruf fehlgeschlagen");
                Toast.makeText(KontaktFragment.this.getContext(), "Fehler: Beim Anrufen ist ein Fehler aufgetreten!\n"+activityException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
