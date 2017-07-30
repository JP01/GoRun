package com.gorun;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gorun.jp.a14_04.R;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {



    private GoogleMap mMap;
    DBHelper myDB;
    RunStats stats = new RunStats();

    // the ID of the run to display on the map
    private long runID;

    // Options for the polyLine to display on the map
    private int polyLineColour = Color.argb(150,0,0,255);
    private float polyWidth = (float) 10;

    //Used for backButton handling
    String previousActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // get extras from previous activity
        Bundle extras = getIntent().getExtras();
        runID = Long.valueOf(extras.getString("runID"));

        //Used for backButton handling
        previousActivity = String.valueOf(extras.getString("FROM_LOGRUN"));



        myDB = new DBHelper(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Cursor cursor = myDB.getWaypointDetails(runID);

        //Declare values used for lat and long
        Double lat, lng;
        // ArrayList to hold every coordinate on the run
        ArrayList<LatLng> coords = new ArrayList<LatLng>();
        cursor.moveToFirst();
        do{
            // create a new LatLng object and add it to the ArrayList
            lat = Double.valueOf(cursor.getString(cursor.getColumnIndex("latitude")));
            lng = Double.valueOf(cursor.getString(cursor.getColumnIndex("longitude")));
            coords.add(new LatLng(lat,lng));
        }
        while(cursor.moveToNext());

        // calculate total distance from ArrayList of coords
        int distance = stats.getTotalDistance(coords);

        // Calculate run duration
        int[] startTime = myDB.getFirstLastPointTime((int)runID, "first");
        int[] endTime = myDB.getFirstLastPointTime((int)runID, "last");
        String duration = stats.getRunDuration(startTime[0], startTime[1], startTime[2], startTime[3], endTime[0], endTime[1], endTime[2], endTime[3]);

        // calculate speed
        long durationMS = stats.timeDifference(startTime[0], startTime[1], startTime[2], startTime[3], endTime[0], endTime[1], endTime[2], endTime[3]);
        String speed = stats.getSpeed(stats.getTotalDistance(coords), durationMS);

        // write calculated run stats to database
        myDB.updateRunStats((int)runID, duration, speed, distance);

        // refresh text fields
        SingleRunDetails run = new SingleRunDetails();
        //run.populateText((int)runID);


        // set camera zoom and draw polyline of route
        setZoom(googleMap, coords);
        googleMap.addPolyline(new PolylineOptions().geodesic(true).addAll(coords).color(polyLineColour).width(polyWidth));


    }

    /**
     * Set zoom level of map to ensure that entire route is visible
     * @param map the Map in question
     * @param coords the List of coordinates to process
     */
    public void setZoom(GoogleMap map, List<LatLng> coords){
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        // iterate through list and add all LatLng objects to bounds
        for (LatLng coord: coords){
            b.include(coord);
        }

        try {
            // set camera zoom to show all points in bounds
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 50));
        } catch (IllegalStateException e){
            Toast.makeText(MapsActivity.this, "Error Drawing Map", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Brings user back to main menu using back button
    */
    @Override
    public void onBackPressed(){
        //checks if previous activity was LogRun, if it was, then backButton takes
        //you to ViewAllRuns instead.
        if(previousActivity.equals("TRUE")) {
            Intent intent = new Intent(getApplicationContext(), ViewAllRuns.class);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }



}
