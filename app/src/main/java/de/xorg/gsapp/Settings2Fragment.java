package de.xorg.gsapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

public class Settings2Fragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.tk_prefs);

        if (getArguments() != null && getArguments().containsKey("theme") && (getArguments().getString("theme").equals(Util.AppTheme.DARK))) {
            tintIcons(getPreferenceScreen());
        }

        final ListPreference listPreference = (ListPreference) findPreference("pref_klasse");

        // THIS IS REQUIRED IF YOU DON'T HAVE 'entries' and 'entryValues' in your XML
        setListPreferenceData(listPreference);

        listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setListPreferenceData(listPreference);
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.WHITE);

        return view;
    }*/

    /*
    https://stackoverflow.com/questions/6474707/how-to-fill-listpreference-dynamically-when-onpreferenceclick-is-triggered
     */

    protected static void setListPreferenceData(ListPreference lp) {
        CharSequence[] entries = { "keine", "5.1", "5.2", "5.3", "5.4", "5.5", "6.1", "6.2", "6.3", "6.4", "6.5", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "9.4", "9.5", "10.1", "10.2", "10.3", "10.4", "10.5", "A" + (Calendar.getInstance().get(Calendar.YEAR) % 100), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 1), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 2) };
        CharSequence[] entryValues = { "", "5.1", "5.2", "5.3", "5.4", "5.5", "6.1", "6.2", "6.3", "6.4", "6.5", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "9.4", "9.5", "10.1", "10.2", "10.3", "10.4", "10.5", "A" + (Calendar.getInstance().get(Calendar.YEAR) % 100), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 1), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 2) };
        lp.setEntries(entries);
        lp.setDefaultValue("");
        lp.setEntryValues(entryValues);
    }

    private static void tintIcons(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup g = (PreferenceGroup) p;
            for (int i = 0; i < g.getPreferenceCount(); i++) {
                tintIcons(g.getPreference(i));
            }
        } else {
            Drawable i = p.getIcon();
            if (i != null)
                i.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Util.Preferences.PUSH_MODE)) {
            String val = sharedPreferences.getString(key, Util.PushMode.DISABLED);
            Toast.makeText(this.getContext(), val, Toast.LENGTH_SHORT).show();
            FirebaseService.changePush(this.getContext(), (val.equals(Util.PushMode.PRIVATE) || val.equals(Util.PushMode.PUBLIC)));
        } else if (key.equals(Util.Preferences.THEME)) {
            ((MainActivity2) getActivity()).changeTheme();
        }
    }
}
