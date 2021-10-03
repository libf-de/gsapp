package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class KlausurenAdapter extends RecyclerView.Adapter<KlausurenAdapter.ViewHolder> {

    private List<Klausur> klausurs;
    private LayoutInflater layout;
    private Context mContext;
    private boolean cardMarquee;


    KlausurenAdapter(Context c, List<Klausur> l){
        this.klausurs = l;
        layout = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cardMarquee = PreferenceManager.getDefaultSharedPreferences(c).getBoolean(Util.Preferences.MARQUEE, false);
        mContext = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = layout.inflate(R.layout.list_item,parent,false);
        return new ViewHolder(v);
    }

    private int calculateTitleMargin(TextView date) {
        return (int) date.getPaint().measureText(date.getText().toString()) + Util.convertToPixels(mContext, 16);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.iconView.setText(klausurs.get(position).getIconText());
        holder.iconView.setTypeface(Util.getTKFont(mContext, true));
        holder.iconView.setSolidColor(Util.getFachColor(klausurs.get(position).getFachShort()));
        holder.titleView.setText(klausurs.get(position).getLongName());
        holder.titleView.setTypeface(Util.getTKFont(mContext, false));
        holder.titleView.setSingleLine(true);
        if(cardMarquee) {
            holder.titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holder.titleView.setMarqueeRepeatLimit(-1);
            holder.titleView.setSelected(true);
        } else holder.titleView.setEllipsize(TextUtils.TruncateAt.END);

        holder.timeView.setText(klausurs.get(position).getRemainingTime());
        holder.timeView.setTypeface(Util.getTKFont(mContext, false));
        holder.timeView.requestLayout();

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) holder.titleView.getLayoutParams();
        p.setMargins(p.leftMargin, p.topMargin, calculateTitleMargin(holder.timeView), p.bottomMargin);
        holder.titleView.setLayoutParams(p);
        holder.titleView.requestLayout();

        holder.itemView.setOnClickListener(view -> {
            AlertDialog ad = new AlertDialog.Builder(mContext).create();
            ad.setCancelable(true);
            ad.setMessage(klausurs.get(position).getDesc());

            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "Zum Kalender hinzufg.", (dialog, which) -> {
                Calendar kc = Calendar.getInstance();
                kc.setTime(klausurs.get(position).getDate());
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, kc.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, kc.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                        .putExtra(CalendarContract.Events.TITLE, klausurs.get(position).getLongName());
                mContext.startActivity(intent);
            });
            ad.show();
        });
    }

    @Override
    public int getItemCount() {
        return this.klausurs.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, timeView;
        protected CircularTextView iconView;
        //protected ImageView iconView;

        ViewHolder(View itemView) {
            super(itemView);
            this.titleView = itemView.findViewById(R.id.title);
            this.timeView = itemView.findViewById(R.id.timespan);
            this.iconView = itemView.findViewById(R.id.iconView);
        }


    }


}