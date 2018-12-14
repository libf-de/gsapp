package de.xorg.gsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
//import android.support.design.widget.NavigationView;
import androidx.core.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.ViewGroup;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.crossfader.Crossfader;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialize.util.UIUtils;

import timber.log.Timber;

public class MainActivity2 extends AppCompatActivity {

    Fragment fragm;
    Toolbar toolbar;
    int shownFragment = -1;
    int defaultFragment = Util.NavFragments.VERTRETUNGSPLAN;
    Drawer result;
    String applicationTheme;
    boolean doubleBackToExitPressedOnce = false;
    long first;
    Typeface tkFont;

    TextView toolbarTextView;

    @Override
    protected void onNewIntent(Intent i) {
        Timber.d("Got Intent...");
        if(i.hasExtra("FRAG_SHOW") && (i.getIntExtra("FRAG_SHOW", Util.NavFragments.VERTRETUNGSPLAN) == Util.NavFragments.VERTRETUNGSPLAN)) {
            Timber.d("To VPlan...");
//            if(i.hasExtra("FROM_NTF")) {
//                Timber.d("From Notification...");
                if (fragm instanceof VPlanFragment) {
                    Timber.d("Showing VPlan...");
                    VPlanFragment vpf = (VPlanFragment) fragm;
                    // Pass intent or its data to the fragment's method
                    vpf.doRefresh();
                } else {
                    Timber.d("Opening VPlan...");
                    showFragment(Util.NavFragments.VERTRETUNGSPLAN);
                    //openFragment(R.id.nav_vplan);
                }
//            }
        } else if(i.hasExtra("FRAG_SHOW")) {
            showFragment(i.getIntExtra("FRAG_SHOW", Util.NavFragments.VERTRETUNGSPLAN));
        }
    }

    public String applyTheme(boolean shallRedraw) {
        String appTheme = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_theme", Util.AppTheme.AUTO); //TODO: not gud"!
        if (appTheme.equals(Util.AppTheme.AUTO))
            appTheme = Util.AppTheme.getAutoTheme();
        switch (appTheme) {
            case Util.AppTheme.DARK:
                setTheme(Util.AppThemeRes.DARK);
                break;
            case Util.AppTheme.LIGHT:
                setTheme(Util.AppThemeRes.LIGHT);
                break;
            case Util.AppTheme.YELLOW:
                setTheme(Util.AppThemeRes.YELLOW);
                break;
            default:
                Timber.i("Got invalid theme %s in MainActivity2:applyTheme, assuming LIGHT", appTheme);
                setTheme(Util.AppThemeRes.LIGHT);
                break;
        }
        return appTheme;
        /*if (shallRedraw) {
            ViewGroup vg = findViewById (R.id.rootLayout);
            vg.invalidate();
        }*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        first = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        applicationTheme = applyTheme(false);
        setContentView(R.layout.activity_main2);

        tkFont = Util.getTKFont(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (applicationTheme.equals(Util.AppTheme.DARK)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) toolbar.setElevation(0.0f);
            toolbar.setBackgroundColor(Color.BLACK);
        } else
            toolbar.setBackgroundResource(R.color.gsgelb);

        toolbarTextView = (TextView) findViewById(R.id.toolbar_title);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            toolbarTextView.setTypeface(Util.getTKFont(this, false));

        toolbarTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(fragm instanceof Settings2Fragment) {
                    ((Settings2Fragment) fragm).toggleLehrer();
                    return true;
                }
                return false;
            }
        });


        if(Timber.treeCount() < 1) {
            //if(BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
                Timber.plant(new FileLogTree(this));
            //}
            Timber.tag("GSApp");
        }

        Timber.d("HELLO");

        Toast.makeText(this, PreferenceManager.getDefaultSharedPreferences(this).getString("pref_klasse", "caput"), Toast.LENGTH_SHORT).show();


        /*if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.Preferences.FIRST_RUN2, true)) {
            Intent intent = new Intent(this, FirstRunActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, Util.FIRSTRUN_ACTIVITY);
        }

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains("isSamsung")) {
            boolean isSamsung = Build.MANUFACTURER.equals("samsung");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean("isSamsung", isSamsung);
            if (isSamsung) {
                editor.putBoolean("loadAsync", false);
            } else {
                editor.putBoolean("loadAsync", true);
            }
            editor.commit();
            Toast.makeText(this, "Sollte der Vertretungsplan lange zum Anzeigen benötigen, melde dies bitte dem Entwickler!", Toast.LENGTH_LONG).show();
        }*/

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/
        /*final ImageView logo = ((ImageView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.imageViewBar));
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RotateAnimation ra = new RotateAnimation(0f, 360f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setRepeatCount(Animation.INFINITE);
                ra.setInterpolator(new LinearInterpolator());
                ra.setDuration(500);
                logo.startAnimation(ra);


            }
        });*/

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        Intent i = getIntent();


        // recovering the instance state
        if (savedInstanceState != null)
            shownFragment = savedInstanceState.getInt("GSAPP_MRLN_FRAGMENT");
        else
            if(i.hasExtra("FRAG_SHOW"))
                shownFragment = i.getIntExtra("FRAG_SHOW", Util.NavFragments.VERTRETUNGSPLAN);
            else
                shownFragment = Util.NavFragments.VERTRETUNGSPLAN;

        setupDrawer(shownFragment);
        showFragment(shownFragment);

        Timber.d("onCreate finished after " + (System.currentTimeMillis() - first) + "ms");

        //if(shownFragment == -1) {
        //    if(i.hasExtra("FRAG_SHOW")) {
        //        showFragment();
        //    } else {
        //        showFragment(defaultFragment);
        //    }
        //} else {
        //    showFragment(shownFragment);
        //}


    }



    public void changeTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Fade());
        }
        //overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.recreate();
    }

    public void setBarTitle(String str) {
        if(this.toolbarTextView == null)
            return;
        this.toolbarTextView.setText(str);
        setTitle("");
    }

    public void setupDrawer(long selectedItem) {
        //FirebaseService.checkAppSignature(this);
        //FirebaseService.sendToken(this);
        ViewGroup fot = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ferien_footer, null);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_background)
                .withSelectionListEnabled(false)
                .withProfileImagesClickable(false)
                .addProfiles(
                        new ProfileDrawerItem().withName("GSApp").withEmail("TeKoop Release Candidate 1").withIcon(getResources().getDrawable(R.drawable.icon_tree))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        PrimaryDrawerItem vp = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.VERTRETUNGSPLAN).withName("Vertretungsplan").withIcon(Util.getThemedDrawable(this, R.drawable.school, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem sp = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.SPEISEPLAN).withName("Speiseplan").withIcon(Util.getThemedDrawable(this,R.drawable.food_fork_drink, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem eb = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.BESTELLUNG).withName("Essenbestellung").withIcon(Util.getThemedDrawable(this,R.drawable.shopping, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem ak = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.AKTUELLES).withName("Aktuelles").withIcon(Util.getThemedDrawable(this,R.drawable.newspaper, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem tm = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.TERMINE).withName("Termine").withIcon(Util.getThemedDrawable(this,R.drawable.calendar_clock, applicationTheme)).withTypeface(tkFont);
        //PrimaryDrawerItem kt = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.KONTAKT).withName("Kontakt").withIcon(Util.getThemedDrawable(this,R.drawable.phone_in_talk, applicationTheme));
        PrimaryDrawerItem ks = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.KLAUSUREN).withName("Klausuren").withIcon(Util.getThemedDrawable(this, R.drawable.ic_klausur, applicationTheme)).withTypeface(tkFont);
        SecondaryDrawerItem st = new SecondaryDrawerItem().withIdentifier(Util.NavFragments.SETTINGS).withName("Einstellungen").withIcon(Util.getThemedDrawable(this,R.drawable.settings, applicationTheme)).withTypeface(tkFont);
        SecondaryDrawerItem ab = new SecondaryDrawerItem().withIdentifier(Util.NavFragments.ABOUT).withName("Über...").withIcon(Util.getThemedDrawable(this,R.drawable.information, applicationTheme)).withTypeface(tkFont);

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        vp,
                        sp,
                        eb,
                        ak,
                        tm,
                        //kt,
                        ks,
                        new DividerDrawerItem(),
                        st,
                        ab
                )
                .withStickyFooter(fot)
                .withStickyFooterShadow(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        showFragment(Integer.parseInt(String.valueOf(drawerItem.getIdentifier())));
                        //result.closeDrawer();
                        return true;
                    }
                })
                .withAccountHeader(headerResult)
                .withSelectedItem(selectedItem)
                .build();

        new Feriencounter(this, new Feriencounter.FeriencounterCallback() {
            @Override
            public void run() {
                fot.setVisibility(View.VISIBLE);
                TextView hd = fot.findViewById(R.id.footer_head);
                TextView bd = fot.findViewById(R.id.footer_body);
                hd.setTypeface(tkFont);
                bd.setTypeface(tkFont);
                hd.setText(nameFerien);
                bd.setText(daysUntil);
            }
        }).requestFerien();
    }



    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putInt("GSAPP_MRLN_FRAGMENT", shownFragment);

        //Toast.makeText(this, "onSaveInstanceState", Toast.LENGTH_SHORT).show();

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(out);
    }

    @Override
    public void onBackPressed() {
        if(result != null && result.isDrawerOpen())
            result.closeDrawer();
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Erneut Zurück drücken um GSApp zu beenden.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fragments, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showFragment(int fragId) {
        Bundle bundle = new Bundle();
        bundle.putString("theme", applicationTheme);

        fragm = null;
        switch (fragId) {
            case Util.NavFragments.VERTRETUNGSPLAN:
                fragm = new VPlanFragment();
                setBarTitle("Vertretungsplan");
                break;
            case 631:
                fragm = new VPlanFragment();
                setBarTitle("Vertretungsplan");
                break;
            case Util.NavFragments.SPEISEPLAN:
                fragm = new SpeiseplanFragment();
                setBarTitle("Speiseplan");
                break;
            case Util.NavFragments.BESTELLUNG:
                fragm = new EssenbestellungFragment();
                setBarTitle("Essenbestellung");
                break;
            case Util.NavFragments.TERMINE:
                fragm = new TermineFragment();
                setBarTitle("Termine");
                break;
            case Util.NavFragments.AKTUELLES:
                fragm = new AktuellesFragment();
                setBarTitle("Aktuelles");
                break;
            case Util.NavFragments.SETTINGS:
                fragm = new Settings2Fragment();
                setBarTitle("Einstellungen");
                break;
            case Util.NavFragments.KLAUSUREN:
                fragm = new KlausurenFragment();
                setBarTitle("Klausurenplan");
                break;
            case Util.NavFragments.ABOUT:
                fragm = new AboutFragment();
                setBarTitle("Über GSApp...");
                break;
        }

        if (fragm != null) {
            fragm.setArguments(bundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
            ft.replace(R.id.content_frame, fragm);
            //ft.addToBackStack(null);
            //&try { ft.commit(); } catch(IllegalStateException e) { ft.commitAllowingStateLoss(); e.printStackTrace(); return; }
            ft.commitAllowingStateLoss();
        }

        shownFragment = fragId;

        if(result != null) {
            result.closeDrawer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.FIRSTRUN_ACTIVITY) {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }
}
