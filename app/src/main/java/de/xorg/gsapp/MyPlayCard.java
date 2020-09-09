package de.xorg.gsapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import de.xorg.cardsuilib.objects.RecyclableCard;

public class MyPlayCard extends RecyclableCard {

	private boolean isNew;
	private boolean isDark;
	boolean marquee = false;

	MyPlayCard(Boolean istDark, String titlePlay, String description, String color,
			   String titleColor, Boolean hasOverflow, Boolean isClickable, Boolean istNew) {
		super(titlePlay, description, color, titleColor, hasOverflow,
				isClickable, istDark);
		this.isDark = istDark;
		isNew = istNew;
	}

    MyPlayCard(Boolean istDark, String titlePlay, String description, int color,
               int titleColor, Boolean hasOverflow, Boolean isClickable, Boolean istNew, Boolean doMarquee) {
        super(titlePlay, description, String.format("#%06X", (0xFFFFFF & color)), String.format("#%06X", (0xFFFFFF & titleColor)), hasOverflow,
                isClickable, istDark);
        this.isDark = istDark;
        this.marquee = doMarquee;
        isNew = istNew;
    }



	MyPlayCard(Boolean istDark, String titlePlay, String description, String color,
			   String titleColor, Boolean hasOverflow, Boolean isClickable, Boolean istNew, Boolean doMarquee) {
		super(titlePlay, description, color, titleColor, hasOverflow,
				isClickable, istDark);
		this.isDark = istDark;
		this.marquee = doMarquee;
		isNew = istNew;
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.playc;
	}

	@Override
	protected void applyTo(View convertView) {
		TextView title = convertView.findViewById(R.id.title);
		title.setText(Html.fromHtml(titlePlay));
		title.setTypeface(Util.getTKFont(convertView.getContext(), isNew));
		title.setSingleLine(true);
		if(this.marquee) {
			title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			title.setMarqueeRepeatLimit(-1);
			title.setSelected(true);
		} else title.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        //((TextView) convertView.findViewById(R.id.title)).setMaxLines(1);

		//if(isNew) { ((TextView) convertView.findViewById(R.id.title)).setTypeface(((TextView) convertView.findViewById(R.id.title)).getTypeface(), Typeface.BOLD); }
		title.setTextColor(Color.parseColor(titleColor));
		TextView desc = convertView.findViewById(R.id.description);
		desc.setText(description.trim());
		desc.setTextColor( (isDark ? convertView.getResources().getColor(android.R.color.primary_text_dark) : convertView.getResources().getColor(android.R.color.primary_text_light)) );
		desc.setTypeface(Util.getTKFont(convertView.getContext(), false));
        GradientDrawable stripebg = (GradientDrawable) convertView.findViewById(R.id.stripe).getBackground();
        stripebg.setColor(Color.parseColor(color));

		if (isClickable)
			convertView.findViewById(R.id.backLayout)
					.setBackgroundResource(R.drawable.selectable_background_cardbank);

		if (hasOverflow)
			convertView.findViewById(R.id.overflow)
					.setVisibility(View.VISIBLE);
		else
			convertView.findViewById(R.id.overflow)
					.setVisibility(View.GONE);
	}
}
