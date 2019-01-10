package de.xorg.gsapp;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

public class Settings2Fragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.tk_prefs);

        if (getArguments() != null && getArguments().containsKey("theme") && (getArguments().getString("theme").equals(Util.AppTheme.DARK))) {
            tintIcons(getPreferenceScreen());
        }

        if(!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Util.Preferences.IS_LEHRER, false)) {
            getPreferenceScreen().removePreference(findPreference("sec_lehrer"));

            final ListPreference listPreference = (ListPreference) findPreference("pref_klasse");

            setListPreferenceData(listPreference);

            listPreference.setOnPreferenceClickListener(preference -> {
                setListPreferenceData(listPreference);
                return false;
            });

        } else { getPreferenceScreen().removePreference(findPreference("sec_klasse")); findPreference(Util.Preferences.LEHRER).setSummary(Util.getTeacherName(getPreferenceManager().getSharedPreferences().getString(Util.Preferences.LEHRER, ""), false)); }



        Preference myPref = findPreference("pref_login");
        myPref.setOnPreferenceClickListener(preference -> {
            showEBLogin(); //TODO: Legacy-Mist!!
            return true;
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

    /*
    https://stackoverflow.com/questions/6474707/how-to-fill-listpreference-dynamically-when-onpreferenceclick-is-triggered
     */

    private static void setListPreferenceData(ListPreference lp) {
        CharSequence[] entries = { "keine", "5.1", "5.2", "5.3", "5.4", "5.5", "6.1", "6.2", "6.3", "6.4", "6.5", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "9.4", "9.5", "10.1", "10.2", "10.3", "10.4", "10.5", "A" + (Calendar.getInstance().get(Calendar.YEAR) % 100), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 1), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 2) };
        CharSequence[] entryValues = { "", "5.1", "5.2", "5.3", "5.4", "5.5", "6.1", "6.2", "6.3", "6.4", "6.5", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "9.4", "9.5", "10.1", "10.2", "10.3", "10.4", "10.5", "A" + (Calendar.getInstance().get(Calendar.YEAR) % 100), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 1), "A" + ((Calendar.getInstance().get(Calendar.YEAR) % 100) + 2) };
        lp.setEntries(entries);
        lp.setDefaultValue("");
        lp.setEntryValues(entryValues);
    }



    private void tintIcons(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup g = (PreferenceGroup) p;
            for (int i = 0; i < g.getPreferenceCount(); i++) {
                tintIcons(g.getPreference(i));
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) convertPref(p);
            Drawable i = p.getIcon();
            if (i != null)
                i.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() instanceof MainActivity2) ((MainActivity2) getActivity()).setBarTitle("Einstellungen");

        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setDividerHeight(0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Util.Preferences.PUSH_MODE: {
                String val = sharedPreferences.getString(key, Util.PushMode.DISABLED);
                FirebaseService.changePush(this.getContext(), (val.equals(Util.PushMode.PRIVATE) || val.equals(Util.PushMode.PUBLIC)));
                break;
            }
            case Util.Preferences.THEME:
                ((MainActivity2) getActivity()).changeTheme();
                break;
            case Util.Preferences.LEHRER: {
                String val = sharedPreferences.getString(key, "");
                findPreference(key).setSummary(Util.getTeacherName(val, false));
                if (val.length() < 3) {
                    Toast.makeText(getContext(), "Lehrerkürzel ungültig, da kürzer als 3 Zeichen! Filter deaktiviert!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    void toggleLehrer() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isEnabled = sp.getBoolean(Util.Preferences.IS_LEHRER, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(isEnabled ? R.string.teacher_disable_msg : R.string.teacher_enable_msg).setTitle(R.string.teacher_title)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean(Util.Preferences.IS_LEHRER, !isEnabled);
                    if(isEnabled) editor.remove(Util.Preferences.LEHRER);
                    editor.apply();
                    Toast.makeText(getContext(), isEnabled ? "Lehrermodus deaktiviert" : "Lehrermodus aktiviert", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    getActivity().recreate();
                }).setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss()).show();
    }



    //Legacy Stuff
    private void convertPref(Preference somePreference) {
        CustomTypefaceSpan customTypefaceSpan = new CustomTypefaceSpan("", Util.getTKFont(this.getContext(), false));

        SpannableStringBuilder ss;
        if (somePreference.getTitle() != null) {
            ss = new SpannableStringBuilder(somePreference.getTitle().toString());
            ss.setSpan(customTypefaceSpan, 0, ss.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            somePreference.setTitle(ss);
        }

        if (somePreference.getSummary() != null) {
            ss = new SpannableStringBuilder(somePreference.getSummary().toString());
            ss.setSpan(customTypefaceSpan, 0, ss.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            somePreference.setSummary(ss);
        }
    }

    static private class CustomTypefaceSpan extends TypefaceSpan {

        private final Typeface newType;

        CustomTypefaceSpan(String family, Typeface type) {
            super(family);
            newType = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeFace(ds, newType);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            applyCustomTypeFace(paint, newType);
        }

        private static void applyCustomTypeFace(Paint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(tf);
        }
    }


    private void showEBLogin() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.anmeldung);
        dialog.setTitle("Automatische Anmeldung");

        final TextView User = dialog.findViewById(R.id.username);
        final CheckBox Use = dialog.findViewById(R.id.autologinEnable);
        String Username = Datenspeicher.getUser(getContext());
        if (Username.equalsIgnoreCase("")) {
            Use.setChecked(false);
            ((TextView) dialog.findViewById(R.id.username)).setText("");
            ((TextView) dialog.findViewById(R.id.password)).setText("");
        } else {
            Use.setChecked(true);
            ((TextView) dialog.findViewById(R.id.username)).setText(Datenspeicher.getUser(getContext()));
            try {
                String PASSWORD_STATE = Datenspeicher.getPassword(getContext());
                if (PASSWORD_STATE.startsWith("error")) {
                    if (PASSWORD_STATE.contains("nocb")) {
                        Toast.makeText(getContext(), "Fehler beim Laden des Passworts: Kontrollbyte fehlt!", Toast.LENGTH_SHORT).show();
                        ((TextView) dialog.findViewById(R.id.password)).setText("");
                    } else {
                        Toast.makeText(getContext(), "Fehler beim Laden des Passworts!", Toast.LENGTH_SHORT).show();
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

        Button saveButton = dialog.findViewById(R.id.serverSave);
        Button cancelButton = dialog.findViewById(R.id.serverCancel);
        saveButton.setOnClickListener(v -> {
            if (Use.isChecked()) {
                Datenspeicher.saveUser(User.getText().toString(), getContext());
                if (!Datenspeicher.savePassword(((TextView) dialog.findViewById(R.id.password)).getText().toString(), getContext())) {
                    Toast.makeText(getContext(), "Fehler beim Verschlüsseln des Passworts", Toast.LENGTH_SHORT).show();
                }
            } else {
                Datenspeicher.savePassword("", getContext());
                Datenspeicher.saveUser("", getContext());
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
