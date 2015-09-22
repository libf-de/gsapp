package de.xorg.gsapp;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;

public class MyPlayCard extends RecyclableCard {
	
	public MyPlayCard(String titlePlay, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(titlePlay, description, color, titleColor, hasOverflow,
				isClickable);
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.playc;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));
		((TextView) convertView.findViewById(R.id.description))
				.setText(description);
		convertView.findViewById(R.id.stripe)
				.setBackgroundColor(Color.parseColor(color));

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
