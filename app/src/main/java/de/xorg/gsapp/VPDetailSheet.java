package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import de.xorg.cardsuilib.objects.Card;
import de.xorg.cardsuilib.objects.CardStack;
import de.xorg.cardsuilib.views.CardUI;

import static de.xorg.gsapp.Util.LongName;
import static de.xorg.gsapp.Util.getFachColor;

public class VPDetailSheet extends BottomSheetDialogFragment {

    CardUI display;
    String dateD;
    String hinweisD;
    ArrayList<Eintrag> input = new ArrayList<>();
    String klasse;
    View rootView;

    boolean istDark = false;
    boolean cardMarquee;

    public VPDetailSheet() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.vpdetail_sheet, container, false);
        display = rootView.findViewById(R.id.cv_dialog);
        cardMarquee = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Util.Preferences.MARQUEE, false);
        dateD = getArguments().getString("date");
        hinweisD = getArguments().getString("hinweis");
        istDark = getArguments().getString("theme").equals(Util.AppTheme.DARK);
        input = (ArrayList) getArguments().getSerializable("input");
        klasse = getArguments().getString("klasse");

        CardStack dateHead = new CardStack(istDark);
        dateHead.setTypeface(Util.getTKFont(this.getContext(), false));
        dateHead.setTitle("Für " + dateD);
        display.addStack(dateHead);

        if(hinweisD.length() > 6) {
            MyPlayCard card = new MyPlayCard(istDark,"Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false, false, cardMarquee);
            card.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(getContext()).create();
                    ad.setCancelable(true);
                    ad.setTitle("Hinweis");
                    ad.setMessage(hinweisD);

                    ad.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }

            });
            display.addCard(card);
        }

        CardStack stackPlay = new CardStack(istDark);
        stackPlay.setTitle("Vertretungen für Klasse " + klasse);
        stackPlay.setTypeface(Util.getTKFont(getContext(), false));
        display.addStack(stackPlay);

        for(final Eintrag single : input) {
            MyPlayCard card;
            String note = "";
            if(single.getKlasse().contains("STK")) {
                String Stammkurs = "";
                try {
                    Stammkurs = single.getKlasse().split("STK")[1].trim();
                } catch(Exception e) { }

                note = " (STK" + Stammkurs + ")";
            }

            if(single.getBemerkung().equals("Ausfall")) {
                card = new MyPlayCard(istDark,single.getStunde() + ". Stunde - Ausfall!" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
            } else if(single.getBemerkung().equals("Stillbesch.")) {
                card = new MyPlayCard(istDark,single.getStunde() + ". Stunde - Stillbesch.!" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
            } else if(single.getBemerkung().equals("AA")) {
                card = new MyPlayCard(istDark,single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
            } else {
                card = new MyPlayCard(istDark,single.getStunde() + ". Stunde" + note, "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + Util.getTeacherName(single.getVertretung(), true) + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, single.getNeu(), cardMarquee);
            }
            card.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //displayMoreInformation(single);
                }

            });
            display.addCard(card);
        }

        display.refresh();
        display.getScrollView().computeScrollY();
        return rootView;
    }

    public void addStack(CardStack c) {
        CardUI cu = rootView.findViewById(R.id.cv_dialog);
        cu.addStack(c);
    }

    public void addCard(Card c) {
        CardUI cu = rootView.findViewById(R.id.cv_dialog);
        cu.addCard(c);
    }

    public void refreshUI() {
        CardUI cu = rootView.findViewById(R.id.cv_dialog);
        cu.refresh();
    }

    public void computeUI() {
        CardUI cu = rootView.findViewById(R.id.cv_dialog);
        cu.getScrollView().computeScrollY();
    }

    public CardUI getDisplay() {
        return display;
    }
}
