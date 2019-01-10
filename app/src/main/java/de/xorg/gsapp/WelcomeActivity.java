package de.xorg.gsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.github.paolorotolo.appintro.model.SliderPage;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class WelcomeActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int gsgelb = Color.parseColor("#fed21b");

        SliderPage sp1 = new SliderPage();
        sp1.setTitle("Willkommen");
        sp1.setDescription("Wir stellen vor: die neue GSApp!");
        sp1.setImageDrawable(R.mipmap.ic_launcher_round);
        sp1.setBgColor(gsgelb); //schöne farbe?
        addSlide(AppIntro2Fragment.newInstance(sp1));

        SliderPage sp2 = new SliderPage();
        sp2.setTitle("Neues Design");
        sp2.setDescription("Das Aussehen wurde dem neuen Material Design angepasst");
        sp2.setImageDrawable(R.drawable.intro1);
        sp2.setBgColor(gsgelb);
        addSlide(AppIntro2Fragment.newInstance(sp2));

        SliderPage sp3 = new SliderPage();
        sp3.setTitle("Schwarzes Design");
        sp3.setDescription("Außerdem wurde ein schwarzes Design für OLED-Bildschirme hinzugefügt");
        sp3.setImageDrawable(R.drawable.intro2);
        sp3.setBgColor(gsgelb);
        addSlide(AppIntro2Fragment.newInstance(sp3));

        SliderPage sp4 = new SliderPage();
        sp4.setTitle("Push-Benachrichtigungen");
        sp4.setDescription("Neue Vertretungspläne kommen jetzt sofort an - dank Push-Benachrichtigung");
        sp4.setImageDrawable(R.drawable.intro3);
        sp4.setBgColor(gsgelb);
        addSlide(AppIntro2Fragment.newInstance(sp4));

        SliderPage sp5 = new SliderPage();
        sp5.setTitle("Klausurenplan");
        sp5.setDescription("Der Klausurenplan der 11. und 12. Klassen kann nun in der App eingesehen werden");
        sp5.setImageDrawable(R.drawable.intro4);
        sp5.setBgColor(gsgelb);
        addSlide(AppIntro2Fragment.newInstance(sp5));

        SliderPage sp6 = new SliderPage();
        sp6.setTitle("Lehrermodus");
        sp6.setDescription("Auch Lehrer bekommen nun Benachrichtigungen :-)");
        sp6.setImageDrawable(R.drawable.intro5);
        sp6.setBgColor(gsgelb);
        addSlide(AppIntro2Fragment.newInstance(sp6));

        addSlide(PrivacySlide.newInstance(R.layout.privacy_slide));

        showSkipButton(false);

        SliderPage sp8 = new SliderPage();
        sp8.setTitle("Vielen Dank!");
        sp8.setDescription("Du kannst die App nun verwenden!");
        sp8.setBgColor(gsgelb);
        addSlide(AppIntro2Fragment.newInstance(sp8));

        setFadeAnimation();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
        edit.putBoolean(Util.Preferences.FIRST_RUN3, Boolean.TRUE);
        edit.commit();
        finish();
    }

    public final static class PrivacySlide extends Fragment implements ISlidePolicy {
        private static final String ARG_LAYOUT_RES_ID = "layoutResId";
        private int layoutResId;

        private CheckBox accepted;
        private Button showPolicy;

        public static PrivacySlide newInstance(int layoutResId) {
            PrivacySlide prvSlide = new PrivacySlide();

            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
            prvSlide.setArguments(args);

            return prvSlide;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
                layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {

            View v = inflater.inflate(layoutResId, container, false);

            accepted = v.findViewById(R.id.acceptBox);

            showPolicy = v.findViewById(R.id.showBtn);
            showPolicy.setOnClickListener(v1 -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xorg.ga/gsapp/privacy.html"));
                startActivity(browserIntent);
            });

            return v;
        }

        @Override
        public boolean isPolicyRespected() {
            return accepted.isChecked();
        }

        @Override
        public void onUserIllegallyRequestedNextPage() {
            Toast.makeText(getContext(), "Bitte akzeptiere die Datenschutzerklärung um fortzufahren!", Toast.LENGTH_SHORT).show();
        }
    }
}
