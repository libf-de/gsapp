package de.xorg.gsapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import de.xorg.cardsuilib.objects.RecyclableCard;

public class AdCard extends RecyclableCard {

	Context context;

	public AdCard(Context c, Boolean hasOverflow, Boolean isClickable) {
		super("", "", "#FFFFFF", "#FFFFFF", hasOverflow,
				isClickable);
		context = c;
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.ad_card;
	}

	@Override
	protected void applyTo(View convertView) {
		MobileAds.initialize(context, "ca-app-pub-6538125936915221~2281967739");
		AdView mAdView = convertView.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().addTestDevice("F42D4035C5B8ABF685658DE77BCB840A").build();
		mAdView.loadAd(adRequest);

		if (isClickable == true)
			convertView.findViewById(R.id.backLayout)
					.setBackgroundResource(R.drawable.selectable_background_cardbank);

		if (hasOverflow == true)
			convertView.findViewById(R.id.overflow)
					.setVisibility(View.VISIBLE);
		else
			convertView.findViewById(R.id.overflow)
					.setVisibility(View.GONE);
	}
}
