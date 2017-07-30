package com.gorun;

/**
 * Class to log a new run
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gorun.jp.a14_04.R;

public class LogRun extends AppCompatActivity {

    private static final int GPS_LOGGING_INTERVAL = 3000; // logging interval in ms
    private static final int GPS_DISTANCE_INTERVAL = 10; // distance between updates in m

    DBHelper myDB;

    private SAutoBgButton startButton, stopButton, menuButton;
    private TextView latText, longText, runIDText, runNameText, instruction;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String latitude, longitude, runID, runName;
    private miliChrono chronometer;
    private ShakeNGo shakeNGoListener;
    private SensorManager mSensorManager;
    private boolean isStarted; //used to determine if the startButton has been pressed

    // boolean to store whether any points have been logged
    private boolean pointLogged = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_new_run);

        myDB = new DBHelper(this);

        isStarted = false;

        // map UI elements
        startButton = (SAutoBgButton) findViewById(R.id.startButton);
        stopButton = (SAutoBgButton) findViewById(R.id.stopButton);
        stopButton.setVisibility(View.INVISIBLE);

        latText = (TextView) findViewById(R.id.latText);
        longText = (TextView) findViewById(R.id.longText);
        runIDText = (TextView) findViewById(R.id.runIDText);
        chronometer = (miliChrono) findViewById(R.id.chronometer);
        runNameText = (TextView) findViewById(R.id.runNameText);
        instruction = (TextView) findViewById(R.id.instruction);

        // get current run ID and update textView
        runID = myDB.GetLastRunID();
        runIDText.setText(runID);
        // get current runName and update textView
        runName = myDB.GetLastRunName();
        runNameText.setText(runName);
        runNameText.setVisibility(View.INVISIBLE);



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // get current latitude
                latitude = String.valueOf(location.getLatitude());
                latText.setText(latitude);

                // get current longitude
                longitude = String.valueOf(location.getLongitude());
                longText.setText(longitude);

                // log current location
                saveLocation();

                // set flag
                pointLogged = true;

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                // Display alert prompting user to enable GPS if it is currently disabled
                AlertDialog.Builder builder = new AlertDialog.Builder(LogRun.this);
                builder.setTitle("GPS Currently Disabled")
                        .setMessage("This app requires GPS to be switched on to log location\n\nOpen settings to enable GPS?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // open settings if GPS disabled
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(LogRun.this, "This app will not function unless GPS is enabled", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        };




        // configure start and stop button functionality
        configureStartButton();
        configureStopButton();
        runShakeNGo();
        menuButton();


    }

    /**
     * Discard the current run and return to main menu
     */
    public void discardRun(){
        // delete record from database
        myDB.deleteRun(Integer.parseInt(runID));
        // stop location listener
        locationManager.removeUpdates(locationListener);
        // stop chronometer
        chronometer.stop();
        // alert user that run has not been logged
        Toast.makeText(LogRun.this, "Nothing recorded, returning to Main Menu", Toast.LENGTH_SHORT).show();
    }

    /**
     * Defines menu button functionality
     */
    public void menuButton(){
        menuButton = (SAutoBgButton) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * Configure start button functionality
     */
    private void configureStartButton() {
        //checks if already started, if false then start recording, if true then do nothing.

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isStarted) {

                    locationManager.requestLocationUpdates("gps", GPS_LOGGING_INTERVAL, GPS_DISTANCE_INTERVAL, locationListener);

                    // start timer
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                    //disable start button and enable stop button
                    runNameText.setVisibility(View.VISIBLE);
                    instruction.setVisibility(View.INVISIBLE);
                    startButton.setEnabled(false);
                    startButton.setVisibility(View.INVISIBLE);
                    stopButton.setEnabled(true);
                    stopButton.setVisibility(View.VISIBLE);
                    isStarted = true;
                    }
                }
            });

    }

    /**
     * Configure stop button functionality
     */
    private void configureStopButton() {
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                locationManager.removeUpdates(locationListener);

                // stop timer
                chronometer.stop();

                // disable stop button
                stopButton.setEnabled(false);


                // if nothing has been logged, delete record of run
                if (!pointLogged){
                    onBackPressed();
                } else {
                    Toast.makeText(LogRun.this, "Run saved, loading map...", Toast.LENGTH_SHORT).show();
                    // open map view
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    //FROM_LOGRUN used for controlling onBackPressed in MapsActivity
                    intent.putExtra("runID", runID).putExtra("FROM_LOGRUN", "TRUE");
                    startActivity(intent);

                }
            }
        });

    }



    /**
     * Save the current location to the database
     */
    private void saveLocation(){
        // insert data
        boolean isInserted = myDB.insertWaypoint(latitude, longitude, runID);

        if (isInserted){
            Toast.makeText(LogRun.this, "Location Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LogRun.this, "Location not saved - error!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Method to override standard backpress, to ensure record deletion if nothing is recorded
     */
    @Override
    public void onBackPressed(){
        // if nothing has been logged, delete record of run
        if (!pointLogged){

            discardRun();
            super.onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }


    /**
     * Configure ShakeNGo functionality
     */
    private void runShakeNGo(){
        //Checks if the run has been started, if not, then shakeNGo, if true then do nothing

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            shakeNGoListener = new ShakeNGo();


            shakeNGoListener.setOnShakeListener(new ShakeNGo.OnShakeListener() {
                public void onShake() {
                    if (!isStarted) {
                        //Vibrate for 100ms
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(100);
                        //Make toast to prompt user that run is being recorded
                        Toast.makeText(LogRun.this, "Started Running!", Toast.LENGTH_SHORT).show();
                        //autoclick startButton
                        startButton.performClick();
                        //stop listening
                        mSensorManager.unregisterListener(shakeNGoListener);
                    }
                }
            });

    }


    /**
     * Restarts the listener when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        //Checks if the run has been started, if not, then shakeNGo, if true then do nothing
        if (!isStarted) {
            mSensorManager.registerListener(shakeNGoListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Stops the listener when activity is resumed
     */
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(shakeNGoListener);
        super.onPause();
    }



}
