package de.xorg.cardsuilib;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

public class Utils {
	
	public static void setDrawableBackground(Context mContext, Drawable background, int colorRes) {
		if (background instanceof ShapeDrawable) {
			((ShapeDrawable)background).getPaint().setColor(ContextCompat.getColor(mContext,colorRes));
		} else if (background instanceof GradientDrawable) {
			((GradientDrawable)background).setColor(ContextCompat.getColor(mContext,colorRes));
		} else if (background instanceof ColorDrawable) {
			((ColorDrawable)background).setColor(ContextCompat.getColor(mContext,colorRes));
		}
	}


	/**
	 * This method converts device specific pixels to device independent pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param ctx
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent db equivalent to px value
	 */
	public float convertPixelsToDp(Context ctx, float px) {
		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;

	}

	public static int convertDpToPixelInt(Context context, float dp) {

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int px = (int) (dp * (metrics.densityDpi / 160f));
		return px;
	}
	
	public static float convertDpToPixel(Context context, float dp) {

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float px = (float) (dp * (metrics.densityDpi / 160f));
		return px;
	}

}
