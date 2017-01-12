package de.xorg.gsapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class VPlanViewer extends ActionBarActivity {
	public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
    public boolean fallback = false;
	public String cDeutsch = "#3f51b5";
	public String cMathe = "#f44336";
	public String cMusik = "#9e9e9e";
	public String cKunst = "#673ab7";
	public String cGeografie = "#9e9d24";
	public String cReligion = "#ff8f00";
	public String cEthik = "#ff8f00";
	public String cMNT = "#4caf50";
	public String cEnglisch = "#ff9800";
	public String cSport = "#607d8b";
	public String cBiologie = "#4caf50";
	public String cChemie = "#e91e63";
	public String cPhysik = "#009688";
	public String cSozialkunde = "#795548";
	public String cInformatik = "#03a9f4";
	public String cWirtschaftRecht = "#ff5722";
	public String cGeschichte = "#9c27b0";
    public String cFRL="#558b2f";
	public Eintrage vplane;
    public String UrlToLoad;
    @SuppressWarnings("unused")
    private Context c;
    private boolean isFiltered = false;
    private String Filter = null;
    private boolean singleMode = false;
    private CardUI mCardView;
    private String dateD = "unbekannt";
    private String hinweisD = "kein Hinweis";
    private ProgressDialog progressDialog;

    private static int[] unset(int[] arrIn, int index) {
        int i;

        // new array is shorter
        int[] arrOut = new int[arrIn.length - 1];

        // copy element "before" arrIn[index]
        for (i = 0; i < index; i++) {
            arrOut[i] = arrIn[i];
        }

        // copy element "after" arrIn[index]
        for (i = index; i < arrOut.length; i++) {
            arrOut[i] = arrIn[i + 1];
        }

        return arrOut;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
        Util.setThemeUI(this);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		setContentView(R.layout.cards);

		//Tablet-Oberfläche einstellen
        Util.setOrientation(this);

		final Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide);
		//isDarkUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);

		mCardView = (CardUI) findViewById(R.id.cardsview);
		mCardView.setAnimation(anim);
		mCardView.setSwipeable(false);

		vplane = new Eintrage();

		//Util.setTranscluent(this, BeanUI);

		magic();

	}

	public void magic() {
		if(Util.hasInternet(getApplicationContext())) {
			mCardView.clearCards();

			if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("loadAsync", false)) {
				new GetVPL().execute();
			} else {
				loadSynced();
			}
		} else {
            File outputDir = getCacheDir(); // context being the Activity pointer
            File GXF = new File(outputDir, "vertretung.gxcache");

            if (GXF.exists()) {
                readFromCache();
                displayAll();
                Toast.makeText(this, "Es besteht keine Internetverbindung - Plan wird vom Zwischenspeicher geladen!", Toast.LENGTH_SHORT).show();
            }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vcach, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                vplane.clear();
                magic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onBackPressed() {
		if(singleMode) {
			singleMode = false;
			displayAll();
		} else {
			//Toast.makeText(this, "loading synced", Toast.LENGTH_SHORT).show();
			//loadSynced();
			super.onBackPressed();
		}

        return;
	}

	public void loadSynced() {
		String result = "";
		try {
            /*Apache HttpClient Library*/
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(getURL());
            //HttpGet request = new HttpGet("http://gsapp.xorg.ga/debug/vp.html");
            if (Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            request.setHeader("Accept-Charset", "utf-8");
            request.setHeader("User-Agent", Util.getUserAgentString(this, true));
            HttpResponse response = client.execute(request);
			/* response code*/
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result = result + line + "\n";
            }
            //TODO: add parser Method
            parseResponse(result);
        } catch (ArrayIndexOutOfBoundsException e) {
            fallbackLoad(result);
		} catch (Exception exe) {
            mCardView.clearCards();
            MyPlayCard card = new MyPlayCard("Interner Fehler", "Es ist ein Fehler beim Herunterladen des Vertretungsplans aufgetreten!", "#FF0000", "#FF0000", true, false);
            mCardView.addCard(card);

            Toast.makeText(VPlanViewer.this, "Vertretungsplan konnte nicht angezeigt werden, zeige im Browser..", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(VPlanViewer.this, InternetViewer.class);
            intent.putExtra("de.xorg.gsapp.MESSAGE", "http://www.gymnasium-sonneberg.de/Informationen/vp.html");
            intent.putExtra("de.xorg.gsapp.MESSAGENAME", "[!] Vertretungsplan [!]");
            VPlanViewer.this.startActivity(intent);
			exe.printStackTrace();
        }
	}

    public void parseResponse(String result) throws ArrayIndexOutOfBoundsException {
        char gf = (char) 34;
        String Klasse = PreferenceManager.getDefaultSharedPreferences(VPlanViewer.this).getString("klasse", "");

        if (result.equals("E")) {
            return;
        }

        Document doc = Jsoup.parse(result);
        Element date = doc.select("td[class=vpUeberschr]").first();
        Element bemerk = doc.select("td[class=vpTextLinks]").first();

        Log.d("GSApp5", "JBMK: *" + bemerk.text() + "*");

        if (date.text().equals("Beschilderung beachten!")) {
            //TODO: Ferien
            Toast.makeText(this, "Es sind Ferien!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            dateD = date.text();
            hinweisD = bemerk.text().replace("Hinweis: ", "");
        }

        Elements vpEnts = doc.select("tr[id=Svertretungen] ~ tr");

        for (Element e : vpEnts) {
            Elements d = e.children();
            String[] data = new String[7];
            int dID = 0;
            for (Element g : d) {
                data[dID] = g.text();
                dID++;
            }

            if (Klasse.equals("")) {
                isFiltered = false;
                displayStuff(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
            } else {
                isFiltered = true;
                Filter = Klasse;
                String skl = String.valueOf(data[0].charAt(0));
                String SUCL = data[0].replace("/2", " " + skl + ".2");
                SUCL = SUCL.replace("/3", " " + skl + ".3");
                SUCL = SUCL.replace("/4", " " + skl + ".4");
                SUCL = SUCL.replace("/5", " " + skl + ".5");

                if (SUCL.length() == 1) {
                    if (Klasse.startsWith(SUCL)) {
                        displayStuff(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                    }
                } else {
                    if (SUCL.contains(Klasse)) {
                        displayStuff(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                    }
                }
            }
        }

        displayAll();
        return;


    }

    private void fallbackLoad(String result) {
        Log.d("GSApp", "--------- LOADING FALLBACK ----------");
        char gf = (char) 34;
        String Klasse = PreferenceManager.getDefaultSharedPreferences(VPlanViewer.this).getString("klasse", "");

        try {
            if(result != "E") {
                String gPart = result.split("<td colspan=\"7\" class=\"rundeEckenOben vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
                dateD = gPart;
                String bemerkung = result.split("<tr id=\"Shinweis\">")[1].split("</tr>")[0].replace("Hinweis: <br />","").replace("<br />", "· ").replaceAll("\\<.*?>", "").replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß").replaceAll("[\\\r\\\n]+", "").trim();
                hinweisD = "[!] " + bemerkung;
            }
        } catch(Exception ex) {
            //Allgemeiner Fehler beim Auswerten
            hinweisD = "[!] kein Hinweis";
            Log.d("GSApp VPCF", "Fehler beim Auswerten der Informationen");
        }

        if(result != "E") {
            String gPart = result.split("<td class=\"vpTextZentriert\">", 2)[1];
            String[] rawC = gPart.split("\n");

            String[] newC = clearUp(rawC).split("\n");

            int counter = 1;
            int va = 0;
            String klasse = "";
            String stunde = "";
            String orgfach = "";
            String vertret = "";
            String raum = "";
            String verfach = "";
            String bemerkung = "";

            for(String cnt : newC) {
                if(counter == 1) {
                    klasse = cnt;
                    counter = counter + 1;
                } else if(counter == 2) {
                    stunde = cnt;
                    counter = counter + 1;
                } else if(counter == 3) {
                    orgfach = cnt;
                    counter = counter + 1;
                } else if(counter == 4) {
                    vertret = cnt;
                    counter = counter + 1;
                } else if(counter == 5) {
                    raum = cnt;
                    counter = counter + 1;
                } else if(counter == 6) {
                    verfach = cnt;
                    counter = counter + 1;
                } else if(counter == 7) {
                    bemerkung = cnt;
                    counter = 1;

                    if(Klasse.equals("")) {
                        isFiltered = false;
                        displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
                        va = va + 1;
                        klasse = "";
                        stunde = "";
                        orgfach = "";
                        vertret = "";
                        raum = "";
                        verfach = "";
                        bemerkung = "";
                    } else {
                        isFiltered = true;
                        Filter = Klasse;
                        String skl = String.valueOf(klasse.charAt(0));
                        String SUCL = klasse.replace("/2", " " + skl + ".2");
                        SUCL = SUCL.replace("/3", " " + skl + ".3");
                        SUCL = SUCL.replace("/4", " " + skl + ".4");
                        SUCL = SUCL.replace("/5", " " + skl + ".5");

                        if(SUCL.length() == 1) {
                            if(Klasse.startsWith(SUCL)) {
                                displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
                                va = va + 1;
                            }
                        } else {
                            if (SUCL.contains(Klasse)) {
                                displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
                                va = va + 1;
                            }
                        }
                    }
                }
            }
            System.out.println("--- PARSE END AT " + System.currentTimeMillis() + " ---");
            displayAll();



            System.out.println("--- DISPLAY END AT " + System.currentTimeMillis() + " ---");
        } else {
            mkMsg("Space error :(");
        }
    }
	
	private void displayStuff(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {
		Eintrag mei = new Eintrag(klasse, stunde, fachnormal, vertretung, raum, fachvertret, bemerkung);
		vplane.add(mei);
		//mCardView.addCard(new MyPlayCard(stunde + ". Stunde", "Statt " + fachnormal + " " + fachvertret + " bei " + vertretung + " in Raum " + raum + ".\n" + bemerkung, getFachColor(fachnormal), getFachColor(fachnormal), false, false));
		//TextView vpv = (TextView) findViewById(R.id.vpView);
		//vpv.setText(vpv.getText().toString().replace("Laden...", "") + "\n\nKlasse: " + klasse + " || Stunde: " + stunde + " || Fach normal: " + fachnormal + " || Vertretung: " + vertretung + " || Raum: " + raum + " || Fach vertr.: " + fachvertret + " || Bemerkung: " + bemerkung);
	}



    private String getURL() {
        int mode = PreferenceManager.getDefaultSharedPreferences(this).getInt("debugSrc", 0);
        String URL = null;
        switch (mode) {
            case 0:
                URL = "http://www.gymnasium-sonneberg.de/Informationen/vp.html";
                break;
            case 1:
                URL = "http://gsapp.xorg.ga/debug/vp.html";
                break;
            case 2:
                URL = "http://gsapp.xorg.ga/debug/vp2.html";
                break;
            case 3:
                URL = "http://gsapp.xorg.ga/debug/vp3.html";
                break;
            default:
                URL = "http://www.gymnasium-sonneberg.de/Informationen/vp.html";
                break;
        }

        return URL;
    }
	
	private void displayAll() {
		mCardView.clearCards();
		CardStack dateHead = new CardStack();
		dateHead.setTitle("Für " + dateD);
		mCardView.addStack(dateHead);
		
		if(!hinweisD.equals("Hinweis:")) {
			MyPlayCard card = new MyPlayCard("Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false);
			card.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog ad = new AlertDialog.Builder(VPlanViewer.this).create();  
				    ad.setCancelable(true); // This blocks the 'BACK' button  
				    ad.setTitle("Hinweis");
				    ad.setMessage(hinweisD);
				      
				    ad.setButton("OK", new DialogInterface.OnClickListener() {  
				        @Override  
				        public void onClick(DialogInterface dialog, int which) {  
				            dialog.dismiss();                      
				        }  
				    });  
				    ad.show();  
				}
				
			});
			mCardView.addCard(card);
		}
		
		try {
			if(isFiltered) {
				CardStack stacky = new CardStack();
				stacky.setTitle("Vertretungen für Klasse " + Filter);
				mCardView.addStack(stacky);
			}
			for(String klassee : vplane.getKlassen()) {
				if(!isFiltered) {
					CardStack stacky = new CardStack();
					stacky.setTitle("Klasse " + klassee);
					mCardView.addStack(stacky);
				}
				for(final Eintrag single : vplane.getKlasseGruppe(klassee, !isFiltered)) {
					MyPlayCard card;
					if(single.getBemerkung().equals("Ausfall")) {
						card = new MyPlayCard(single.getStunde() + ". Stunde - Ausfall!", "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
					} else if(single.getBemerkung().equals("Stillbesch.")) {
						card = new MyPlayCard(single.getStunde() + ". Stunde - Stillbesch.!", "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
					} else if(single.getBemerkung().equals("AA")) {
						card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
					} else if(single.getFachNormal().equals(single.getFachVertretung())) {
						card = new MyPlayCard(single.getStunde() + ". Stunde", "Du hast " + LongName(single.getFachNormal()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
					} else {
						card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
					}
					if(isFiltered) {
						card.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								displayMoreInformation(single);
							}
							
						});
						mCardView.addCard(card);
					} else {
						card.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								displaySingleClass(single.getKlasse());
							}
							
						});
						mCardView.addCardToLastStack(card);
					}
				}
			}
		} catch (KeineKlassenException e) {
			// TODO Auto-generated catch block
			mCardView.addCard(new MyPlayCard("Keine Vertretungen", "", cMNT, cMNT, false, false));
			e.printStackTrace();
		} catch (KeineEintrageException e) {
			// TODO Auto-generated catch block
			mCardView.addCard(new MyPlayCard("ERROR", "KeineEinträgeException", "#FF0000", "#FF0000", false, false));
			e.printStackTrace();
		}

        if(Util.hasSoftNavigation(this)) {
            CardStack spacer = new CardStack();
            spacer.setTitle("\n\n\n\n\n-");
            mCardView.addStack(spacer);
        }
		
		mCardView.refresh();

        saveToCache();
	}
	
	private void displaySingleClass(String klasse) {
		mCardView.clearCards();
		singleMode = true;
		CardStack dateHead = new CardStack();
		dateHead.setTitle("Für " + dateD);
		mCardView.addStack(dateHead);
		
		if(!hinweisD.equals("Hinweis:")) {
			MyPlayCard card = new MyPlayCard("Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false);
			card.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    AlertDialog ad = new AlertDialog.Builder(VPlanViewer.this).create();
                    ad.setCancelable(true); // This blocks the 'BACK' button
                    ad.setTitle("Hinweis");
                    ad.setMessage(hinweisD);

                    ad.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }

            });
			mCardView.addCard(card);
		}
		
		try {
			CardStack stackPlay = new CardStack();
			stackPlay.setTitle("Vertretungen für Klasse " + klasse);
			mCardView.addStack(stackPlay);
			
			for(final Eintrag single : vplane.getKlasseGruppeS(klasse)) {
				MyPlayCard card;
				if(single.getBemerkung().equals("Ausfall")) {
					card = new MyPlayCard(single.getStunde() + ". Stunde - Ausfall!", "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
				} else if(single.getBemerkung().equals("Stillbesch.")) {
					card = new MyPlayCard(single.getStunde() + ". Stunde - Stillbesch.!", "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
				} else if(single.getBemerkung().equals("AA")) {
					card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
				} else {
					card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false);
				}
				card.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						displayMoreInformation(single);
					}
					
				});
				mCardView.addCard(card);
			}
			
			mCardView.refresh();
		} catch (KeineEintrageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void displayMoreInformation(Eintrag eintrag) {
	    AlertDialog ad = new AlertDialog.Builder(this).create();  
	    ad.setCancelable(true); // This blocks the 'BACK' button  
	    ad.setTitle("Information");
	    if(eintrag.getBemerkung().equals("Ausfall")) {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Ausfall (Raum " + eintrag.getRaum() + ")");
	    } else if(eintrag.getBemerkung().equals("Stillbesch.")) {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + eintrag.getRaum());
	    } else if(eintrag.getBemerkung().equals("AA")) {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + eintrag.getRaum());
	    } else {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du " + LongName(eintrag.getFachVertretung()) + " bei " + eintrag.getVertretung() + " im Raum " + eintrag.getRaum() + "\n\nBemerkung: " + eintrag.getBemerkung());
	    }
	      
	    ad.setButton("OK", new DialogInterface.OnClickListener() {  
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	            dialog.dismiss();                      
	        }  
	    });  
	    ad.show();  
	}
	
	private String getFachColor(String fach) {
        String col = cSport;
		switch(fach.toLowerCase()) {
		case "de":
			col= cDeutsch;
            break;
		case "ma":
			col = cMathe;
            break;
		case "mu":
			col = cMusik;
            break;
		case "ku":
			col = cKunst;
            break;
		case "gg":
			col = cGeografie;
            break;
		case "re":
			col = cReligion;
            break;
		case "et":
			col = cEthik;
            break;
		case "mnt":
			col = cMNT;
            break;
		case "en":
			col = cEnglisch;
            break;
		case "sp":
			col = cSport;
            break;
		case "spj":
			col = cSport;
            break;
		case "spm":
			col = cSport;
            break;
		case "bi":
			col = cBiologie;
            break;
		case "ch":
			col = cChemie;
            break;
		case "ph":
			col = cPhysik;
            break;
		case "sk":
			col = cSozialkunde;
            break;
		case "if":
			col = cInformatik;
            break;
		case "wr":
			col = cWirtschaftRecht;
            break;
        case "ge":
            col = cGeschichte;
            break;
        case "ru":
            col = cFRL;
            break;
        case "la":
            col = cFRL;
            break;
        case "fr":
            col = cFRL;
            break;
		default:
			col = cSport;
			break;
		}
        return col;
	}
	
	private String LongName(String fach) {
		switch(fach.toLowerCase()) {
		case "de":
			return "Deutsch";
		case "ma":
			return "Mathe";
		case "mu":
			return "Musik";
		case "ku":
			return "Kunst";
		case "gg":
			return "Geografie";
		case "re":
			return "Religion";
		case "et":
			return "Ethik";
		case "mnt":
			return "MNT";
		case "en":
			return "Englisch";
		case "sp":
			return "Sport";
		case "spj":
			return "Sport Jungen";
		case "spm":
			return "Sport Mädchen";
		case "bi":
			return "Biologie";
		case "ch":
			return "Chemie";
		case "ph":
			return "Physik";
		case "sk":
			return "Sozialkunde";
		case "if":
			return "Informatik";
		case "wr":
			return "Wirtschaft/Recht";
        case "ge":
            return "Geschichte";
        case "fr":
            return "Französisch";
        case "ru":
            return "Russisch";
        case "la":
            return "Latein";
        case "gewi":
            return "Gesellschaftswissenschaften";
        case "&nbsp;":
            return "keine Angabe";
		default:
			return fach;
			
		}
	}
	
	private String clearUp(String[] inpud) {
		char gf = (char) 34;
		String me = "";
		for(String ln : inpud) {
			ln = ln.replaceAll("\\<.*?>","");
			ln = ln.replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß");
			ln = ln.replace("                        ", "");
			//ln = ln.replace("        ", "");
			ln = ln.trim();
			ln = ln.replace("	", "");
			
			if(ln.equals("      ")) {
			} else if(ln.equals("var hoehe = parent.document.getElementById('inhframe').style.height;")) {
			} else if(ln.equals("setFrameHeight();")) {
			} else if(ln.equals("var pageTracker = _gat._getTracker(" + gf + "UA-5496889-1" + gf + ");")) {
			} else if(ln.equals("pageTracker._trackPageview();")) {
			} else if(ln.equals("    ")) {
			} else if(ln.equals("	")) {
			} else if(ln.equals("  ")) {
			} else if(ln.startsWith("var")) {
			} else if(ln.startsWith("document.write")) {
			} else if(ln.equals("")) {
			//} else if(ln.endsWith("&nbsp;")) {
			//	me = me + "XXXX\n";
			} else {
				if (ln.matches(".*\\w.*")) {
					me = me + ln + "\n";
				} else if(ln.contains("##")) {
					me = me + ln + "\n";
				}
			}
		}
		
		return me;
	}
	
	public void mkMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d("gsapp-space", msg);
    }

    public void saveToCache() {
        try {
            File outputDir = getCacheDir(); // context being the Activity pointer
            File outputFile = new File(outputDir, "vertretung.gxcache");

            JSONObject jk = new JSONObject();
            JSONArray jo = new JSONArray();
            for(int i = 0 ; i < vplane.size(); i++) {
                Eintrag single = vplane.get(i);
                JSONObject so = new JSONObject();
                so.put("Klasse", single.getKlasse());
                so.put("Stunde", single.getStunde());
                so.put("Fachnormal", single.getFachNormal());
                so.put("Vertretung", single.getVertretung());
                so.put("Raum", single.getRaum());
                so.put("Fachvertret", single.getFachVertretung());
                so.put("Bemerkung", single.getBemerkung());
                jo.put(so);
            }

            jk.put("et", jo);
            jk.put("cDate", dateD);

            Files.write(jk.toString(), outputFile, Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFromCache() {
        try {
            vplane.clear();
            File outputDir = getCacheDir(); // context being the Activity pointer
            File outputFile = new File(outputDir, "vertretung.gxcache");

            String GXCACHE = Files.toString(outputFile, Charsets.UTF_8);

            JSONObject jo = new JSONObject(GXCACHE);
            JSONArray singles = jo.getJSONArray("et");
            for(int i=0;i < singles.length(); i++){
                JSONObject single = singles.getJSONObject(i);
                Eintrag cac = new Eintrag(single);
                vplane.add(cac);
            }

            dateD = jo.getString("cDate") + " (gecached)";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetVPL extends AsyncTask<String, Void, String> {
        GetVPL() {
            Log.d("GSApp", "Lade Vertretungsplan");
        }

        @Override
        protected void onPreExecute() {
            System.setProperty("http.keepAlive", "false");
            //Create a new progress dialog
            progressDialog = new ProgressDialog(VPlanViewer.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle("GSApp 5.x »Merlin Rewrite«");
            //Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage("Lade Daten...");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(true);
            //Display the progress dialog
            progressDialog.show();

            System.out.println("--- GET START AT " + System.currentTimeMillis() + " ---");
        }

        protected String doInBackground(String... message) {
            HttpClient httpclient;
            HttpGet request;
            HttpResponse response = null;
            String result = "";
            try {
                httpclient = new DefaultHttpClient();
                //request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
                request = new HttpGet(getURL());
                request.setHeader("User-Agent", Util.getUserAgentString(VPlanViewer.this, false));
                response = httpclient.execute(request);
            } catch (Exception e) {
                result = "E";
                e.printStackTrace();
            }
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result = result + line + "\n";
                }
            } catch (Exception e) {
                result = "E";
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            System.out.println("--- GET END AT " + System.currentTimeMillis() + " ---");
            //close the progress dialog
            progressDialog.dismiss();

            try {
                parseResponse(result);
            } catch (ArrayIndexOutOfBoundsException e) {
                fallbackLoad(result);
            }

        }

    }

}

class Eintrag {
    String Klasse;
    String Stunde;
    String Fachnormal;
    String Vertretung;
    String Raum;
    String Fachvertret;
    String Bemerkung;

    Eintrag(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {
        Klasse = klasse;
        Stunde = stunde;
        Fachnormal = fachnormal;
        Vertretung = vertretung;
        Raum = raum;
        Fachvertret = fachvertret;
        Bemerkung = bemerkung;
    }

    Eintrag(JSONObject input)  throws Exception {
        Klasse = input.getString("Klasse");
        Stunde = input.getString("Stunde");
        Fachnormal = input.getString("Fachnormal");
        Vertretung = input.getString("Vertretung");
        Raum = input.getString("Raum");
        Fachvertret = input.getString("Fachvertret");
        Bemerkung = input.getString("Bemerkung");
    }

    public String getKlasse() {
        return Klasse;
    }

    public void setKlasse(String value) {
        Klasse = value;
    }

    public String getStunde() {
        return Stunde;
    }

    public void setStunde(String value) {
        Stunde = value;
    }

    public String getFachNormal() {
        return Fachnormal;
    }

    public void setFachNormal(String value) {
        Fachnormal = value;
    }

    public String getVertretung() {
        if (Vertretung.equals("##") || Vertretung.equals("&nbsp;")) {
            return "niemandem";
        } else {
            return Vertretung;
        }
    }

    public void setVertretung(String value) {
        Vertretung = value;
    }

    public String getRaum() {
        if (Raum.equals("##")) {
            return "k.A.";
        } else {
            return Raum;
        }
    }

    public void setRaum(String value) {
        Raum = value;
    }

    public String getFachVertretung() {
        if (Fachvertret.equals("##") || Vertretung.equals("&nbsp;")) {
            return "nichts";
        } else {
            return Fachvertret;
        }
    }

    public void setFachVertretung(String value) {
        Fachvertret = value;
    }

    public String getBemerkung() {
        if (Bemerkung.equals("&nbsp;")) {
            return "keine Bemerkung";
        } else {
            return Bemerkung;
        }
    }

    public void setBemerkung(String value) {
        Bemerkung = value;
    }

    public Boolean isKlasse(String input) {
        return Klasse == input;
    }

    public String[] toSaveString() {
        String[] dieser = new String[7];
        dieser[0] = Klasse;
        dieser[1] = Stunde;
        dieser[2] = Fachnormal;
        dieser[3] = Vertretung;
        dieser[4] = Raum;
        dieser[5] = Fachvertret;
        dieser[6] = Bemerkung;
        //Set<String> dieserSet = new HashSet<String>(Arrays.asList(dieser));
        return dieser;
    }

}

class Eintrage extends ArrayList<Eintrag> {
	public ArrayList<Eintrag> getKlasseGruppe(String klasse, Boolean reverse) throws KeineEintrageException {
		ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
		for(Eintrag single : this) {
            Log.d("GSApp", "ET: " + single.getKlasse() + " vs SUCH: " + klasse);
            if (single.getKlasse().equals(klasse)) {
                outp.add(single);
            }
        }

        if(outp.size() < 1) {
			throw new KeineEintrageException();
		} else {
			if(reverse) {
				Collections.reverse(outp);
			}
			return outp;
		}
	}
	
	public ArrayList<Eintrag> getKlasseGruppeS(String klasse) throws KeineEintrageException {
		ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
		for(Eintrag single : this) {
            Log.d("GSApp", "ET: " + single.getKlasse() + " vs SUCH: " + klasse);
            if (single.getKlasse().equals(klasse)) {
                outp.add(single);
            }
        }

        if(outp.size() < 1) {
			throw new KeineEintrageException();
		} else {
			return outp;
		}
	}
	
	public ArrayList<String> getKlassen() throws KeineKlassenException {
		String liste = "";
		for(Eintrag single : this) {
			if(!liste.contains(single.getKlasse())) {
				liste = liste + single.getKlasse() + ",";
			}
		}
		
		if(liste.equals("")) {
			throw new KeineKlassenException();
		} else {
			liste = method(liste);
			ArrayList<String> outp = new ArrayList<String>();
			Collections.addAll(outp, liste.split(","));
			return outp;
		}
	}
	
	public String method(String str) {
	    if (str.length() > 0 && str.charAt(str.length()-1)=='x') {
	      str = str.substring(0, str.length()-1);
	    }
	    return str;
	}
}

class KeineEintrageException extends Exception {
	
}

class KeineKlassenException extends Exception {
	
}