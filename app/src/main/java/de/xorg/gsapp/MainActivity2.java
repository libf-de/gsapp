package de.xorg.gsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import timber.log.Timber;

public class MainActivity2 extends AppCompatActivity {

    Fragment fragm;
    Toolbar toolbar;
    int shownFragment = -1;
    Drawer result;
    String applicationTheme;
    boolean doubleBackToExitPressedOnce = false;
    long first;
    Typeface tkFont;
    boolean popBs = false;

    TextView toolbarTextView;

    @Override
    protected void onNewIntent(Intent i) {
        Timber.d("Got Intent...");
        if(i.hasExtra("FRAG_SHOW") && (i.getIntExtra("FRAG_SHOW", Util.NavFragments.VERTRETUNGSPLAN) == Util.NavFragments.VERTRETUNGSPLAN)) {
            Timber.d("To VPlan...");
                if (fragm instanceof VPlanFragment) {
                    Timber.d("Showing VPlan...");
                    VPlanFragment vpf = (VPlanFragment) fragm;
                    new Thread(() -> vpf.loadData(true)).start();
                } else {
                    Timber.d("Opening VPlan...");
                    showFragment(Util.NavFragments.VERTRETUNGSPLAN);
                }
        } else if(i.hasExtra("FRAG_SHOW")) {
            showFragment(i.getIntExtra("FRAG_SHOW", Util.NavFragments.VERTRETUNGSPLAN));
        }
    }

    public boolean hasNavBar(Resources resources)
    {
        if(Build.MANUFACTURER.toLowerCase().equals("oneplus")) return false;
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    @Override
    public void onResume(){
        super.onResume();
        boolean previouslyStarted = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(Util.Preferences.FIRST_RUN3, false);
        if(!previouslyStarted) {
            startActivity(new Intent(MainActivity2.this, WelcomeActivity.class));
        }
    }

    private void showChangelog(int newVer) {
        LayoutInflater inflater= LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.changelog_dialog, null);

        TextView cl = view.findViewById(R.id.changelog);
        StringBuilder htmlstr = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for(String s : Util.CHANGELOG.split("\n")) {
                htmlstr.append("<li>&nbsp;").append(s).append("</li>");
            }
            cl.setText(Html.fromHtml("<pre>Version " + getResources().getString(R.string.version) + "</pre><ul>" + htmlstr.toString() + "</ul>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            for(String s : Util.CHANGELOG.split("\n")) {
                htmlstr.append("*&nbsp;").append(s).append("<br/>");
            }
            cl.setText(Html.fromHtml("<pre>Version " + getResources().getString(R.string.version) + "</pre><br/>" + htmlstr.toString() + ""));
        }

        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("Neuigkeiten")
                .setMessage("GSApp wurde aktualisiert!\nHier sind die Neuerungen:")
                .setView(view)
                .setNeutralButton("Okay", (dialog, which) -> {
                    SharedPreferences.Editor edit = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).edit();
                    edit.putInt(Util.Preferences.LAST_VERSION, newVer);
                    edit.apply();
                    dialog.dismiss();
                })
                .create();

        ad.show();
    }


    public String applyTheme() {
        String appTheme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_theme", Util.AppTheme.AUTO); //TODO: not gud"!
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        first = System.currentTimeMillis();
        applicationTheme = applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.setNavigationBarColor(Color.TRANSPARENT);
            RelativeLayout rl = findViewById(R.id.rootLayout2);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();
            lp.setMargins(0, getStatusHeight(), 0, 0);
            rl.setLayoutParams(lp);

        }*/

        if(Timber.treeCount() < 1) {
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new FileLogTree(this));
            Timber.tag("GSApp");
        }

        tkFont = Util.getTKFont(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (applicationTheme.equals(Util.AppTheme.DARK)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) toolbar.setElevation(0.0f);
            toolbar.setBackgroundColor(Color.BLACK);
        } else
            toolbar.setBackgroundResource(R.color.gsgelb);

        toolbarTextView = findViewById(R.id.toolbar_title);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            toolbarTextView.setTypeface(Util.getTKFont(this, false));

        toolbarTextView.setOnLongClickListener(v -> {
            if(fragm instanceof Settings2Fragment) {
                ((Settings2Fragment) fragm).toggleLehrer();
                return true;
            }
            return false;
            });

        popBs = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_popbs", false);


        //TODO: First-Run-Zeug
        /*

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

        getSupportFragmentManager().addOnBackStackChangedListener(
                () -> {
                    if(result != null)
                        result.setSelection(Util.NavFragments.getIdentifier(getSupportFragmentManager().findFragmentById(R.id.content_frame)),false);
                });

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

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int verCode = pInfo.versionCode;
            if(androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).getInt(Util.Preferences.LAST_VERSION, verCode - 1) < verCode) {
                showChangelog(verCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        Timber.d("onCreate finished after " + (System.currentTimeMillis() - first) + "ms");
    }



    public void changeTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Fade());
        }
        this.recreate();
    }

    public void setBarTitle(String str) {
        if(this.toolbarTextView == null)
            return;
        this.toolbarTextView.setText(str);
        setTitle("");
    }

    public void setupDrawer(long selectedItem) {
        ViewGroup fot = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ferien_footer, null);
        fot.setPadding(50, 50, 50, 50);

        boolean hasNav = hasNavBar(getResources());
        //boolean hasNav = false;

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_background)
                .withSelectionListEnabled(false)
                .withProfileImagesClickable(false)
                .addProfiles(
                        new ProfileDrawerItem().withName("GSApp").withEmail(Util.getVersion(this)).withIcon(R.mipmap.ic_launcher_round)
                )
                .withProfileImagesVisible(true)
                .withCompactStyle(true)
                .withOnAccountHeaderListener((view, profile, currentProfile) -> false)
                .build();

        PrimaryDrawerItem vp = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.VERTRETUNGSPLAN).withName("Vertretungsplan").withIcon(Util.getThemedDrawable(this, R.drawable.school, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem sp = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.SPEISEPLAN).withName("Speiseplan").withIcon(Util.getThemedDrawable(this,R.drawable.food_fork_drink, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem eb = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.BESTELLUNG).withName("Essenbestellung").withIcon(Util.getThemedDrawable(this,R.drawable.shopping, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem ak = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.AKTUELLES).withName("Aktuelles").withIcon(Util.getThemedDrawable(this,R.drawable.newspaper, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem tm = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.TERMINE).withName("Termine").withIcon(Util.getThemedDrawable(this,R.drawable.calendar_clock, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem ks = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.KLAUSUREN).withName("Klausuren").withIcon(Util.getThemedDrawable(this, R.drawable.ic_klausur, applicationTheme)).withTypeface(tkFont);
        PrimaryDrawerItem kt = new PrimaryDrawerItem().withIdentifier(Util.NavFragments.KONTAKT).withName("Kontakt").withIcon(Util.getThemedDrawable(this, R.drawable.phone_in_talk, applicationTheme)).withTypeface(tkFont);
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
                        ks,
                        kt,
                        new DividerDrawerItem(),
                        st,
                        ab
                )
                .withStickyFooter(fot)
                .withStickyFooterShadow(false)

                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    showFragment(Integer.parseInt(String.valueOf(drawerItem.getIdentifier())));
                    return true;
                            })
                .withAccountHeader(headerResult)
                .withSelectedItem(selectedItem)
                .withTranslucentNavigationBar(hasNav)
                .withTranslucentNavigationBarProgrammatically(hasNav)
                .withTranslucentStatusBar(true)
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
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(out);
    }

    @Override
    public void onBackPressed() {
        if(result != null && result.isDrawerOpen())
            result.closeDrawer();
        else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Erneut Zurück drücken um GSApp zu beenden.", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
            } else {
                super.onBackPressed();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fragments, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showFragment(int fragId) {
        Bundle bundle = new Bundle();
        bundle.putString("theme", applicationTheme);

        if(fragId == Util.NavFragments.getIdentifier(fragm)) return;

        fragm = null;
        switch (fragId) {
            case Util.NavFragments.VERTRETUNGSPLAN:
                fragm = new VPlanFragment();
                //setBarTitle("Vertretungsplan");
                break;
            case 631:
                fragm = new VPlanFragment();
                //setBarTitle("Vertretungsplan");
                break;
            case Util.NavFragments.SPEISEPLAN:
                fragm = new SpeiseplanFragment();
                //setBarTitle("Speiseplan");
                break;
            case Util.NavFragments.BESTELLUNG:
                fragm = new EssenbestellungFragment();
                //setBarTitle("Essenbestellung");
                break;
            case Util.NavFragments.TERMINE:
                fragm = new TermineFragment();
                //setBarTitle("Termine");
                break;
            case Util.NavFragments.AKTUELLES:
                fragm = new AktuellesFragment();
                //setBarTitle("Aktuelles");
                break;
            case Util.NavFragments.SETTINGS:
                fragm = new Settings2Fragment();
                //setBarTitle("Einstellungen");
                break;
            case Util.NavFragments.KLAUSUREN:
                fragm = new KlausurenFragment();
                //setBarTitle("Klausurenplan");
                break;
            case Util.NavFragments.KONTAKT:
                fragm = new KontaktFragment();
                //setBarTitle("Klausurenplan");
                break;
            case Util.NavFragments.ABOUT:
                fragm = new AboutFragment();
                //setBarTitle("Über GSApp...");
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setElevation(fragId == Util.NavFragments.KONTAKT ? 0.0f : Util.convertToPixels(this, 4));

        if (fragm != null) {
            fragm.setArguments(bundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
            ft.replace(R.id.content_frame, fragm);

            if(this.popBs) {
                if (getSupportFragmentManager().popBackStackImmediate(fragm.getClass().getName(), 0)) {
                    shownFragment = fragId;

                    if (result != null) {
                        result.closeDrawer();
                    }
                    return;
                }
            }

            ft.replace(R.id.content_frame, fragm);

            if( ft.isAddToBackStackAllowed()) ft.addToBackStack(fragm.getClass().getName()); else Toast.makeText(this, "addToBackStack not allowed, gib mir bitte Bescheid wann diese Meldung kommt", Toast.LENGTH_LONG).show();
            try {
                if( getSupportFragmentManager().isStateSaved() ) {
                    Toast.makeText(this, "BackStack in isStateSaved, gib mir bitte einmalig Bescheid wann diese Meldung kommt (App von Benachrichtigung geöffnet, ...)", Toast.LENGTH_LONG).show();
                    ft.commitAllowingStateLoss();
                } else
                    ft.commit();
            } catch( IllegalStateException ise ) {
                ise.printStackTrace();
                Toast.makeText(this, "BackStack caught IllegalStateException, gib mir bitte Bescheid wenn und wann dies auftritt :-)", Toast.LENGTH_LONG).show();
                ft.commitAllowingStateLoss();
            }
            //ft.addToBackStack(null);
            //try { ft.commit(); } catch(IllegalStateException e) { ft.commitAllowingStateLoss(); e.printStackTrace(); return; }
            //ft.commit();
            //ft.commitAllowingStateLoss();
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
