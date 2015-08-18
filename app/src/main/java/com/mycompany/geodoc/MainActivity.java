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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
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
    private RadioGroup radioGroup;
    private static final String herokuAppURL = "http://young-sierra-1628.herokuapp.com";
    private static final String openShiftAppURL = "http://geodoc-tricki.rhcloud.com";
    AlarmManager alarm = null;
    private PendingIntent pendingIntent;

    private void setAlarm() {

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        Calendar cal = Calendar.getInstance();
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 60, createPendingIntent());
    }

    private void cancelAlarm() {
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(createPendingIntent());
    }

    private PendingIntent createPendingIntent() {
        intent = new Intent(this, SendLocationService.class);
        PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
        return pIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getString(R.string.last_location));
        editor.commit();

        editor.putString(getString(R.string.appURL), herokuAppURL);
        editor.commit();

        Switch toggle = (Switch) findViewById(R.id.switch1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAlarm();
                } else {
                    cancelAlarm();
                }
            }
        });

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove(getString(R.string.last_location));
                editor.commit();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if(checkedId == R.id.openShiftButton) {
                    editor.putString(getString(R.string.appURL), openShiftAppURL);
                } else {
                    editor.putString(getString(R.string.appURL), herokuAppURL);
                }
                editor.commit();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.location), locationString);
        editor.commit();
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