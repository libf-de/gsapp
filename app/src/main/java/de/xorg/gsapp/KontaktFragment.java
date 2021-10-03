package de.xorg.gsapp;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;


public class KontaktFragment extends Fragment {

    public KontaktFragment() {  }

    ListView listView;
    private boolean istDunkel = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kontakt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Titelzeile setzen
        if(getActivity() != null && getActivity() instanceof MainActivity2) ((MainActivity2) getActivity()).setBarTitle("Kontakt");

        if (getArguments() != null && getArguments().containsKey("theme")) {
            istDunkel = (getArguments().getString("theme").equals(Util.AppTheme.DARK));
        }

        listView = requireView().findViewById(R.id.contactList);


        if (istDunkel) {
            RelativeLayout hdv = requireView().findViewById(R.id.profile_layout);
            int pad = Util.convertToPixels(GSApp.getContext(), 6);
            hdv.setBackgroundResource(R.color.background_dark);
            hdv.setPadding(0, pad, 0, pad);
            requireView().findViewById(R.id.layout).setBackgroundResource(R.color.background_dark);
            listView.setBackgroundResource(R.color.background_dark);
        }

        ArrayList<KontaktData> kontaktModels = new ArrayList<KontaktData>() {{
            add(new KontaktData("Sekretariat Lohau"));
            add(new KontaktData("Telefon", "03675702977", KontaktTypes.PHONE));
            add(new KontaktData("E-Mail", "sekretariat-lohau@gymson.de", KontaktTypes.EMAIL));
            add(new KontaktData("Sekretariat Dammstraße"));
            add(new KontaktData("Telefon", "03675468890", KontaktTypes.PHONE));
            add(new KontaktData("E-Mail", "sekretariat@gymson.de", KontaktTypes.EMAIL));
            add(new KontaktData("App-Entwickler"));
            add(new KontaktData("E-Mail", "xorgmc+gsapp@gmail.com", KontaktTypes.EMAIL));
            add(new KontaktData("Whats-App", "015902615854", KontaktTypes.WHATSAPP));
        }};

        KontaktAdapter adapter = new KontaktAdapter(requireContext(), kontaktModels, istDunkel);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {

            KontaktData kT = kontaktModels.get(position);
            if(kT.getIsHeader())
                return;

            switch(kT.getType()) {
                case 0:
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    //i.setType("text/plain");
                    i.setData(Uri.parse("mailto:" + kT.getTarget()));
                    try {
                        startActivity(Intent.createChooser(i, "Mail senden"));
                    } catch (ActivityNotFoundException ex) {
                        Snackbar.make(view1, "Es wurden keine E-Mail-Apps gefunden!", Snackbar.LENGTH_LONG)
                                .setAction("Adr. kopieren", v -> {
                                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Mailadresse", kT.getTarget());
                                    clipboard.setPrimaryClip(clip);
                                }).show();
                    }
                    break;
                case 1:
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + kT.getTarget()));
                        startActivity(callIntent);
                    } catch (ActivityNotFoundException activityException) {
                        activityException.printStackTrace();
                        Snackbar.make(view1, "Es wurde keine Telefon-App gefunden!", Snackbar.LENGTH_LONG)
                                .setAction("Tel-Nr. kopieren", v -> {
                                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Telefonnummer", kT.getTarget());
                                    clipboard.setPrimaryClip(clip);
                                }).show();
                    }
                    break;
                case 2:
                    try {
                        Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=49" + kT.getTarget().substring(1));
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(sendIntent);
                    } catch (ActivityNotFoundException activityException) {
                        activityException.printStackTrace();
                        Snackbar.make(view1, "Whats-App wurde nicht gefunden!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                default:
                    Timber.d("Unknown type!");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    static class KontaktTypes {
        static int EMAIL = 0;
        static int PHONE = 1;
        static int WHATSAPP = 2;
    }

    public static class KontaktData {
        String title;
        String target;
        int type;
        boolean isHeader;

        KontaktData(String title, String target, int type) {
            this.title = title;
            this.target = target;
            this.type = type;
            this.isHeader = false;
        }

        KontaktData(String title) {
            this.title = title;
            this.target = null;
            this.type = KontaktTypes.EMAIL;
            this.isHeader = true;
        }

        public String getTitle() { return this.title; }

        public String getTarget() { return this.target; }

        public int getType() { return this.type; }

        boolean getIsHeader() { return this.isHeader; }

        public int getIcon() {
            switch(this.type) {
                case 0:
                    return R.drawable.ic_mail;
                case 1:
                    return R.drawable.phone_in_talk;
                case 2:
                    return R.drawable.ic_whats_app;
                default:
                    return R.drawable.ic_mail;
            }
        }
    }

    public static class KontaktAdapter extends ArrayAdapter<KontaktData> {

        private final Context mContext;
        private final List<KontaktData> kontaktList;
        private final boolean istDunkel;

        KontaktAdapter(@NonNull Context context, ArrayList<KontaktData> list, boolean istDunkel) {
            super(context, 0 , list);
            this.mContext = context;
            this.kontaktList = list;
            this.istDunkel = istDunkel;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            KontaktData tK = Objects.requireNonNull(kontaktList.get(position));

            if(tK.getIsHeader()) {
                if(listItem == null)
                    listItem = LayoutInflater.from(mContext).inflate(R.layout.contact_header, parent,false);

                TextView title = listItem.findViewById(android.R.id.title);
                title.setTypeface(Util.getTKFont(getContext(), false));
                title.setText(tK.getTitle());
                if(this.istDunkel) title.setTextColor(mContext.getResources().getColor(R.color.gsgelb));

                listItem.setBackgroundResource(istDunkel ? R.color.background_dark : R.color.background_white);
            } else {
                if(listItem == null)
                    listItem = LayoutInflater.from(mContext).inflate(R.layout.contact_item, parent,false);

                Typeface tkFont = Util.getTKFont(getContext(), false);

                listItem.setBackgroundResource(istDunkel ? R.color.background_dark : R.color.background_white);

                ImageView icn = listItem.findViewById(android.R.id.icon);
                icn.setImageResource(tK.getIcon());
                if(this.istDunkel) icn.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                TextView title = listItem.findViewById(android.R.id.title);
                title.setTypeface(tkFont);
                title.setText(tK.getTitle());

                TextView target = listItem.findViewById(android.R.id.summary);
                target.setTypeface(tkFont);
                target.setText(tK.getTarget());
            }

            return listItem;
        }
    }
}
