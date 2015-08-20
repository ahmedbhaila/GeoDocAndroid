package com.mycompany.geodoc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

public class SendLocationService extends Service {


    private String getGoogleAccount(Context context) {
        String googleAccount = null;
        AccountManager manager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        if (list.length > 0) {
            googleAccount = list[0].name;
        }
        return googleAccount;
    }


    public SendLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String location = prefs.getString(getString(R.string.location), "");
        Calendar calendar = Calendar.getInstance();
        Log.i("SendLocationService", "My location is " + location);
        Log.i("SendLocationService", "My last location is " + prefs.getString(getString(R.string.last_location), ""));

        // check for last known location
        if(!(prefs.getString(getString(R.string.last_location), "").equals(location))) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.location), location);
            editor.commit();

            editor.putString(getString(R.string.last_location), location);
            editor.commit();

            new HttpRequestTask(getApplicationContext()).execute(location, getGoogleAccount(context),
                    String.valueOf(calendar.getTimeInMillis()), calendar.getTimeZone().getDisplayName(),
                    prefs.getString(getString(R.string.appURL), ""));

        }
        return START_STICKY;
    }
}
