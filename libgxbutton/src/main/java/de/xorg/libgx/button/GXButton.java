package de.xorg.libgx.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by xorg on 19.09.15.
 */
public class GXButton extends LinearLayout {

    private CardView cv;
    private TextView tv;
    public static int materialButtonLight = Color.parseColor("#d6d7d7");
    public static int materialButtonDark = Color.parseColor("#5a595b");

    public GXButton(Context context, AttributeSet attrs) {
        super(context, attrs);


        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Options, 0, 0);
        String btnText = a.getString(R.styleable.Options_btnText);
        boolean dark = a.getBoolean(R.styleable.Options_btnUseDarkTheme, false);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(!dark) {
            inflater.inflate(R.layout.gxbutton, this, true);
        } else {
            inflater.inflate(R.layout.gxbuttondark, this, true);
        }

        tv = (TextView) findViewById(R.id.btnText);
        if(btnText != null) { tv.setText(btnText); }
    }

    public GXButton(Context context) {
        this(context, null);
    }

    public void setText(String Text) {
        tv.setText(Text);
    }

    public void setTypeface(Typeface tf) {
        tv.setTypeface(tf);
    }

}

