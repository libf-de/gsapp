package de.xorg.gsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import android.support.design.widget.FloatingActionButton;
import android.widget.Toast;

/**
 * Created by xorg on 04.11.15.
 */
public class PermissionManager extends Activity {

    public int MaterialGreen = Color.parseColor("#4caf50");
    public int RippleGreen = Color.parseColor("#2e7d32");
    public int MaterialRed = Color.parseColor("#f44336");
    public int RippleRed = Color.parseColor("#c62828");
    FloatingActionButton permissionGrant = null;
    FloatingActionButton accept = null;
    FloatingActionButton deny = null;
    ColorStateList positive = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, new int[]{-android.R.attr.state_enabled}, new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_pressed}}, new int[]{MaterialGreen, MaterialGreen, MaterialGreen, RippleGreen});
    ColorStateList negative = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, new int[]{-android.R.attr.state_enabled}, new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_pressed}}, new int[]{MaterialRed, MaterialRed, MaterialRed, RippleRed});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if (!i.hasExtra("reason")) {
            drawError();

        } else {
            if (i.getStringExtra("reason").equals("debug")) {
                drawDebugScreen();
            } else if (i.getStringExtra("reason").equals("call")) {
                drawCallScreen();
            } else if (i.getStringExtra("reason").equals("hello")) {
                drawHello(i.getStringExtra("text"));
            } else {
                drawError();
            }
        }
    }

    public void drawError() {
        LinearLayout root = new LinearLayout(this);
        // specifying vertical orientation
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        // creating LayoutParams
        ViewGroup.LayoutParams linLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams defaultParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams marginParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams readmeParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        marginParam.setMargins(0, 15, 0, 0);
        readmeParam.setMargins(25, 0, 25, 0);
        // set LinearLayout as a root element of the screen
        setContentView(root);

        setTitle("GBus Permission Handler (Marshmallow)");

        TextView disclaimer = new TextView(this);
        disclaimer.setLayoutParams(readmeParam);
        disclaimer.setText("Der Aktivität wurden keine Daten übergeben! Starten sie diese Aktivität nicht direkt!");
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer);

        LinearLayout bl = new LinearLayout(this);
        bl.setOrientation(LinearLayout.HORIZONTAL);
        bl.setGravity(Gravity.CENTER);
        bl.setLayoutParams(marginParam);

        final FloatingActionButton accept = new FloatingActionButton(this);
        accept.setLayoutParams(marginParam);
        accept.setBackgroundTintList(positive);
        accept.setRippleColor(RippleGreen);
        accept.setImageResource(R.drawable.ic_tick);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        root.addView(accept);

        root.addView(bl);
    }

    public void drawHello(String text) {
        LinearLayout root = new LinearLayout(this);
        // specifying vertical orientation
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        // creating LayoutParams
        ViewGroup.LayoutParams linLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams defaultParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams marginParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams readmeParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        marginParam.setMargins(0, 15, 0, 0);
        readmeParam.setMargins(25, 0, 25, 0);
        // set LinearLayout as a root element of the screen
        setContentView(root);

        setTitle("GBus Permission Handler (Marshmallow)");

        TextView disclaimer = new TextView(this);
        disclaimer.setLayoutParams(readmeParam);
        disclaimer.setText("Handshake success!\n\n" + text);
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer);

        LinearLayout bl = new LinearLayout(this);
        bl.setOrientation(LinearLayout.HORIZONTAL);
        bl.setGravity(Gravity.CENTER);
        bl.setLayoutParams(marginParam);

        final FloatingActionButton accept = new FloatingActionButton(this);
        accept.setLayoutParams(marginParam);
        accept.setBackgroundTintList(positive);
        accept.setRippleColor(RippleGreen);
        accept.setImageResource(R.drawable.ic_tick);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        root.addView(accept);

        root.addView(bl);
    }

    @SuppressLint("RestrictedApi")
    public void drawDebugScreen() {
        // creating LinearLayout
        LinearLayout root = new LinearLayout(this);
        // specifying vertical orientation
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        // creating LayoutParams
        ViewGroup.LayoutParams linLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams defaultParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams marginParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams readmeParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginParam.setMargins(5, 15, 5, 5);
        readmeParam.setMargins(25, 0, 25, 0);
        // set LinearLayout as a root element of the screen
        setContentView(root);


        TextView disclaimer = new TextView(this);
        disclaimer.setLayoutParams(defaultParam);
        disclaimer.setText("Du verwendest Android 6.0, hier hat sich das Rechte-System verändert. Bitte drücke auf den roten Knopf mit dem Schloss, um das Recht für den Zugriff auf den Internen Speicher zu erlangen. Der Knopf wird dann grün. Drücke auf das rote »X«, wenn du es doch nicht tun willst. Sind die erforderlichen Rechte vorhanden, taucht neben dem roten »X« ein grüner Haken auf!");
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer);

        permissionGrant = new FloatingActionButton(this);
        permissionGrant.setLayoutParams(defaultParam);
        permissionGrant.setTag("NO");
        permissionGrant.setBackgroundTintList(negative);
        permissionGrant.setRippleColor(RippleRed);
        permissionGrant.setImageResource(R.drawable.ic_lock);
        permissionGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(PermissionManager.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionManager.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(PermissionManager.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Util.PERMISSION_DEBUG);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
            }
        });

        root.addView(permissionGrant);

        LinearLayout bl = new LinearLayout(this);
        bl.setOrientation(LinearLayout.HORIZONTAL);
        bl.setGravity(Gravity.CENTER);
        bl.setLayoutParams(marginParam);

        LinearLayout.LayoutParams leftBtn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams rightBtn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftBtn.setMargins(0, 0, 15, 0);
        rightBtn.setMargins(15, 0, 0, 0);

        accept = new FloatingActionButton(this);
        accept.setLayoutParams(leftBtn);
        accept.setBackgroundTintList(positive);
        accept.setRippleColor(RippleGreen);
        accept.setImageResource(R.drawable.ic_tick);
        accept.setVisibility(View.INVISIBLE);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PermissionManager.this).edit();
                editor.putBoolean("debug", true);
                editor.commit();
                finish();
            }
        });
        bl.addView(accept);

        deny = new FloatingActionButton(this);
        deny.setLayoutParams(rightBtn);
        deny.setBackgroundTintList(negative);
        deny.setRippleColor(RippleRed);
        deny.setImageResource(R.drawable.ic_cross);
        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        bl.addView(deny);

        root.addView(bl);
    }

    @SuppressLint("RestrictedApi")
    public void drawCallScreen() {
        // creating LinearLayout
        LinearLayout root = new LinearLayout(this);
        // specifying vertical orientation
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        // creating LayoutParams
        ViewGroup.LayoutParams linLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams defaultParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams marginParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams readmeParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginParam.setMargins(5, 15, 5, 5);
        readmeParam.setMargins(25, 0, 25, 0);
        // set LinearLayout as a root element of the screen
        setContentView(root);


        TextView disclaimer = new TextView(this);
        disclaimer.setLayoutParams(defaultParam);
        disclaimer.setText("Du verwendest Android 6.0, hier hat sich das Rechte-System verändert. Bitte drücke auf den roten Knopf mit dem Schloss, um das Recht für den Zugriff auf direkte Anrufe zu erlangen. Der Knopf wird dann grün. Drücke auf das rote »X«, wenn du es doch nicht tun willst. Sind die erforderlichen Rechte vorhanden, taucht neben dem roten »X« ein grüner Haken auf!");
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer);

        permissionGrant = new FloatingActionButton(this);
        permissionGrant.setLayoutParams(defaultParam);
        permissionGrant.setTag("NO");
        permissionGrant.setBackgroundTintList(negative);
        permissionGrant.setRippleColor(RippleRed);
        permissionGrant.setImageResource(R.drawable.ic_lock);
        permissionGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(PermissionManager.this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionManager.this,
                            Manifest.permission.CALL_PHONE)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(PermissionManager.this,
                                new String[]{Manifest.permission.CALL_PHONE}, Util.PERMISSION_CALL);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
            }
        });

        root.addView(permissionGrant);

        LinearLayout bl = new LinearLayout(this);
        bl.setOrientation(LinearLayout.HORIZONTAL);
        bl.setGravity(Gravity.CENTER);
        bl.setLayoutParams(marginParam);

        LinearLayout.LayoutParams leftBtn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams rightBtn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftBtn.setMargins(5, 5, 15, 5);
        rightBtn.setMargins(15, 5, 5, 5);

        accept = new FloatingActionButton(this);
        accept.setLayoutParams(leftBtn);
        accept.setBackgroundTintList(positive);
        accept.setRippleColor(RippleGreen);
        accept.setImageResource(R.drawable.ic_tick);
        accept.setVisibility(View.INVISIBLE);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PermissionManager.this).edit();
                editor.putString("ruf", "call");
                editor.commit();
                finish();
            }
        });
        bl.addView(accept);

        deny = new FloatingActionButton(this);
        deny.setLayoutParams(rightBtn);
        deny.setBackgroundTintList(negative);
        deny.setRippleColor(RippleRed);
        deny.setImageResource(R.drawable.ic_cross);
        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        bl.addView(deny);

        root.addView(bl);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Util.PERMISSION_DEBUG: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    permissionGrant.setBackgroundTintList(positive);
                    permissionGrant.setRippleColor(RippleGreen);
                    permissionGrant.setImageResource(R.drawable.ic_unlock);
                    accept.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Recht freigegeben!", Toast.LENGTH_SHORT).show();
                } else {
                    permissionGrant.setBackgroundTintList(negative);
                    permissionGrant.setRippleColor(RippleRed);
                    permissionGrant.setImageResource(R.drawable.ic_lock);
                    accept.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Antrag auf Recht abgelehnt!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case Util.PERMISSION_CALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    permissionGrant.setBackgroundTintList(positive);
                    permissionGrant.setRippleColor(RippleGreen);
                    permissionGrant.setImageResource(R.drawable.ic_unlock);
                    accept.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Anrufe freigegeben!", Toast.LENGTH_SHORT).show();
                } else {
                    permissionGrant.setBackgroundTintList(negative);
                    permissionGrant.setRippleColor(RippleRed);
                    permissionGrant.setImageResource(R.drawable.ic_lock);
                    accept.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Direkt Anrufen abgelehnt!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


}
