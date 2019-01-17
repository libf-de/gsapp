package de.xorg.gsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import androidx.core.app.NotificationCompat;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class FirebaseService extends FirebaseMessagingService {
    /**
     * Aufgerufen, wenn der Service eine Push-Benachrichtigung aus Firebase erhält
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage); //TODO: Only parse classes/teachers in their respective mode
        Map<String, String> data = remoteMessage.getData();

        Timber.d("Received Push!");

        Timber.d(data.get("forDate"));

        String[] klassen = {"ALL"};
        String[] lehrer = {"ALL"};
        try {
            JSONArray recvClasses = new JSONArray(data.get("klassen"));
            JSONArray recvLehrers = new JSONArray(data.get("lehrer"));
            klassen = new String[recvClasses.length()];
            lehrer = new String[recvLehrers.length()];

            for(int i = 0; i < recvClasses.length(); i++) {
                klassen[i] = recvClasses.getString(i);
            }
            for(int i = 0; i < recvLehrers.length(); i++) {
                lehrer[i] = recvLehrers.getString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.w("Konnte Klassen aus Push-Nachricht nicht verarbeiten!");
        }

        Date forDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        try {
            forDate = format.parse(data.get("forDate"));
        } catch (ParseException e) {
            e.printStackTrace();
            Timber.w("Konnte Datum nicht verarbeiten");
        }

        if (getPushMode().equals(Util.PushMode.PUBLIC)) { //Push-Benachrichtigung immer
            PostNotification(this,"Neuer Vertretungsplan für " + getRelativeDateForNotification(forDate) + " verfügbar!");
        } else {
            if(Util.isLehrerModus(this)) { //Lehrer-Benachrichtigung
                if(Arrays.asList(lehrer).contains(getLehrer()))
                    PostNotification(this,"Sie haben " + getRelativeDateForNotification(forDate) + " Vertretung!");
            } else {
                if (Arrays.asList(klassen).contains(getKlasse()))
                    PostNotification(this,"Du hast " + getRelativeDateForNotification(forDate) + " Vertretung!");
            }
        }
    }

    /**
     * Gibt das relative Datum im Bezug auf d zurück (Heute, morgen, Montag, …)
     * @param d Bezugsdatum
     * @return relatives Datum
     */
    public static String getRelativeDateForNotification(Date d) {
        Calendar c = Calendar.getInstance();

        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DAY_OF_YEAR, 1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(d);

        if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
            return "morgen";
        } else if(c.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
            return "heute";
        } else {
            String[] days = {"", "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"};
            return days[c2.get(Calendar.DAY_OF_WEEK)];
        }
    }

    /**
     * Erstellt die Benachrichtigung über einen neuen Vertretungsplan
     *
     * @param text  - Inhalt der Benachrichtigung
     * @param c   - Anwendungs-Kontext
     */
    public void PostNotification(Context c, String text) {
        Intent intent = new Intent(c, MainActivity2.class);
        intent.putExtra("FRAG_SHOW", Util.NavFragments.VERTRETUNGSPLAN);
        intent.putExtra("FROM_NTF", 1);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Util.NTF_CHANNEL_ID)
                .setContentTitle("Neuer Vertretungsplan!")
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pIntent)
                .setColor(getResources().getColor(R.color.gsgelb))
                .setLights(getResources().getColor(R.color.gsgelb), 1000, 300)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.vertretung);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Benachrichtigungskanal für Android O und neuer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Util.NTF_CHANNEL_ID, getString(R.string.ntf_channel), NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.ntf_channel_desc));
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#fed21b"));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    /**
     * Liest die gespeicherte Klasse aus
     * @return Gespeicherte Klasse
     */
    public String getKlasse() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(Util.Preferences.KLASSE, "");
    }

    /**
     * Liest den gespeicherten Lehrer aus
     * @return Gespeicherter Lehrer
     */
    public String getLehrer() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(Util.Preferences.LEHRER, "");
    }

    /**
     * Überprüfungsmodus auslesen
     * @return Überprüfungsmodus
     */
    public String getPushMode() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(Util.Preferences.PUSH_MODE, Util.PushMode.DISABLED);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Timber.d("Refreshed token: %s", token);
        sendToken(this, token);
    }

    /**
     * De-/Aktiviert Push-Benachrichtigungen
     * @param c AnwendungskontextRefreshed
     * @param enable De-/Aktivieren
     */
    public static void changePush(Context c, boolean enable) {
        boolean InDebug = (c.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0; //Für Debug oder Release-Benachrichtigungen?

        if (enable) {
            FirebaseMessaging.getInstance().subscribeToTopic(Util.PushTopics.get(InDebug))
                    .addOnCompleteListener(task -> {
                        String msg = c.getString(R.string.push_enable_success) + (InDebug ? " (DEBUG)" : "");
                        if (!task.isSuccessful()) {
                            msg = c.getString(R.string.push_enable_failed);
                        }
                        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
                    });

        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Util.PushTopics.get(InDebug))
                    .addOnCompleteListener(task -> {
                        String msg = c.getString(R.string.push_disable_success) + (InDebug ? " (DEBUG)" : "");
                        if (!task.isSuccessful()) {
                            msg = c.getString(R.string.push_disable_failed);
                        }
                        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Erzeugt eine einzigartige Geräte-ID zur Registrierung am Server
     * @param c Anwendungskontext
     * @return Geräte-ID
     */
    public static String getDeviceId(Context c) {
        String stored = PreferenceManager.getDefaultSharedPreferences(c).getString(Util.Preferences.DEVICE_ID, null);
        String returnValue;

        if (stored == null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
            String android_id = Settings.Secure.getString(c.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if(android_id.length() < 5) {
                Timber.d("Using uuid as device-id");
                returnValue = UUID.randomUUID().toString();
                editor.putString(Util.Preferences.DEVICE_ID, returnValue);
            } else {
                Timber.d("Using android_id as device-id");
                returnValue = android_id;
                editor.putString(Util.Preferences.DEVICE_ID, android_id);
            }

            editor.commit();
        } else {
            returnValue = stored;
        }

        return returnValue;
    }

    /**
     * Am Server registrieren (im Moment unnötig, in Zukunft evtl verwendet
     * @param c Anwendungskontext
     * @param tokn Push-Token
     */
    public static void sendToken(Context c, String tokn) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("signature", checkAppSignature(c))
                .addFormDataPart("token", tokn)
                .addFormDataPart("deviceid", getDeviceId(c))
                .build();

        Request request = new Request.Builder()
                .url("https://xorg.ga/gsapp/tkbackend/register.php")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String resp = response.body().string();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
            editor.putBoolean(Util.Preferences.HAS_REGISTERED, resp.equals("ACK"));
            editor.commit();

            if (!resp.equals("ACK")) {
                Timber.w("RegFailed:%s", resp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt die App-Signatur zurück um am Server zu überprüfen ob es eine offizielle Version ist
     * @param context Anwendungs-Kontext
     * @return App-Signatur
     */
    public static String checkAppSignature(Context context) {
        String ret = "ERR";
        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());

                ret = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
