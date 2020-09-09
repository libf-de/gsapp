package de.xorg.gsapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

public class SettingsHeader extends PreferenceCategory {
    public SettingsHeader(Context context) {
        super(context);
    }

    public SettingsHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsHeader(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(this.getContext().getResources().getColor(R.color.gsgelb));
    }
}