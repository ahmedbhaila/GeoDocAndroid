package com.mycompany.geodoc;

/**
 * Created by root on 8/8/15.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * Created by root on 7/13/15.
 */
public class HttpRequestTask extends AsyncTask<String, String, String> {

    protected Context context;
    protected String missedCallNumber;
    protected String clientId;
    private String appURL;
    public HttpRequestTask(Context context) {
        this.context = context;
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            appURL = params[4];
            final String lookupURL = appURL + "/places/{latlng}/document/status?profile={profile}&time={time}&timezone={timezone}";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            HashMap<String, String> param = new HashMap<>();
            param.put("latlng", params[0]);
            param.put("profile", params[1]);
            param.put("time", params[2]);
            param.put("timezone", params[3]);
            String status = restTemplate.getForObject(lookupURL, String.class, param);

            clientId = params[1];
            return status;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String status) {
        if(status != null && status.equals("true")) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.abc_ab_share_pack_mtrl_alpha)
                            .setContentTitle("Files found")
                            .setContentText("There are files found at this location");

            String documentURL = appURL + "/getFiles.html?clientId=" + clientId;
            Intent resultIntent = new Intent(Intent.ACTION_VIEW);
            resultIntent.setData(Uri.parse(documentURL));
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);

            int mNotificationId = 001;
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

        }
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(context.getString(R.string.last_location));
            editor.commit();
        }


    }

}

