package com.mycompany.geodoc;

/**
 * Created by root on 8/8/15.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
    private static final String appURL = "https://young-sierra-1628.herokuapp.com";

    public HttpRequestTask(Context context) {
        this.context = context;
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            final String lookupURL = appURL + "/places/{latlng}/document/status?profile={profile}";
            //"/locate/{client_id}/{number}/lookup";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
            HashMap<String, String> param = new HashMap<>();
            param.put("latlng", params[0]);
            param.put("profile", params[1]);
            String status = restTemplate.getForObject(lookupURL, String.class, param);
            //missedCallNumber = params[1];
            clientId = params[1];
            return status;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String status) {
        if(status.equals("true")) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.abc_ab_share_pack_mtrl_alpha)
                            .setContentTitle("Enhanced Call Information Available")
                            .setContentText("Enhanced Call Information available for this missed called: " + missedCallNumber);

            String documentURL = appURL + "/places/" + clientId + "/documents";
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

            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

        }


    }

}

