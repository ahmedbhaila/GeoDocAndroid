package com.mycompany.geodoc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient mGoogleApiClient;
    Location location;
    Intent intent;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("SendLocation", "Location service is " + checkIfGooglePlayServicesAreAvailable());
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //startService(new Intent(this, SendLocationService.class));
        Calendar cal = Calendar.getInstance();

        intent = new Intent(this, SendLocationService.class);
        PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                20 * 1000, pIntent);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().remove(getString(R.string.last_location)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        String locationString = location.getLatitude() + "," + location.getLongitude();
        Log.i("SendLocation", "Location is " + locationString);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.location), locationString);
        editor.commit();

        //if (location != null) {
          //  Toast.makeText(this, location.getLongitude() + " , " + location.getLatitude() + " : " + location.getAccuracy(), Toast.LENGTH_LONG).show();
       //}

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, location.getLongitude() + " , " + location.getLatitude() + " : " + location.getAccuracy(), Toast.LENGTH_LONG).show();
        Log.i("SendLocation", "Connection failed");
    }

    @Override
    public void onConnectionSuspended(int a) {

    }

    private boolean checkIfGooglePlayServicesAreAvailable() {
        int isAvailable = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        Log.i("LocationService", "is available value is " + isAvailable);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {

        } else {
            Toast.makeText(this, "Connect Connect to Maps", Toast.LENGTH_SHORT)
                    .show();

        }
        return false;
    }

}
