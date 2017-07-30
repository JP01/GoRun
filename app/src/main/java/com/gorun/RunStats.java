package com.gorun;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A class to calculate speed, distance and time stats for a run
 */
public class RunStats {

    /**
     * Iterates through an ArrayList of LatLng objects and calculates the total distance between all objects
     * @param input the ArrayList<LatLng> to process
     * @return the calculated total distance in metres
     */
    public int getTotalDistance(ArrayList<LatLng> input){
        // int to hold the distance of the run
        int distance = 0;
        // loop through every point in coords
        for (int i=0; i<input.size()-1; i++){
            double startLat, endLat, startLong, endLong;

            // get points for the current coord and the one after it
            startLat = input.get(i).latitude;
            endLat = input.get(i+1).latitude;
            startLong = input.get(i).longitude;
            endLong = input.get(i+1).longitude;

            // calculate the distance between those two points and add to running total
            // array to hold result of distance calculation
            float[] results = {0,0,0};
            Location.distanceBetween(startLat, startLong, endLat, endLong, results);
            distance+=results[0];
        }

        return distance;
    }

    /**
     * Calculate the difference between two times
     * @param startH The hour value of the start time
     * @param startM The minute value of the start time
     * @param startS The second value of the start time
     * @param startMS The millisecond value of the start time
     * @param endH The hour value of the end time
     * @param endM The minute value of the end time
     * @param endS The second value of the end time
     * @param endMS The millisecond value of the end time
     * @return The time difference in a readable String
     */

    public String getRunDuration(int startH, int startM, int startS, int startMS, int endH, int endM, int endS, int endMS){
        return millisecondsToReadable(timeDifference(startH, startM, startS, startMS, endH, endM, endS, endMS));
    }

    /**
     * Calculate the difference between two times
     * @param startH The hour value of the start time
     * @param startM The minute value of the start time
     * @param startS The second value of the start time
     * @param startMS The millisecond value of the start time
     * @param endH The hour value of the end time
     * @param endM The minute value of the end time
     * @param endS The second value of the end time
     * @param endMS The millisecond value of the end time
     * @return The time difference in milliseconds
     */
    public long timeDifference(int startH, int startM, int startS, int startMS, int endH, int endM, int endS, int endMS){
        // instances of Calendar for start and end times
        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();

        // set time with constructor and add on milliseconds
        end.set(0, 0, 0, endH, endM, endS);
        end.setTimeInMillis(end.getTimeInMillis() + endMS);

        // set time with constructor and add on milliseconds
        start.set(0, 0, 0, startH, startM, startS);
        start.setTimeInMillis(start.getTimeInMillis() + startMS);

        // total time difference in milliseconds
        long millis = end.getTimeInMillis()-start.getTimeInMillis();

        return millis;
    }



    /**
     * Converts a time in milliseconds into a more readable timestamp
     * @param millis
     * @return A formatted timestamp in hh:mm:ss.msmsms
     */
    public String millisecondsToReadable(long millis){

        // total time in seconds
        double seconds = (double) millis/1000;

        // total time in minutes
        double minutes = seconds/60;

        // total time in hours
        double hours = minutes/60;

        // hours value only
        int hoursOnly = (int) Math.floor(hours);

        // remaining minutes
        double remainingMins = (hours-hoursOnly)*60;

        // minutes value only
        int minsOnly = (int) Math.floor(remainingMins);

        // remaining seconds
        double remainingSecs = (remainingMins-minsOnly)*60;


        return String.format("%d:%02d:%06.3f",hoursOnly, minsOnly, remainingSecs);
    }

    /**
     * Calculates speed in km/h
     * @param distance
     * @param duration
     * @return
     */
    public static String getSpeed(int distance, long duration){
        // distance in m, duration in ms
        double kmph = (double)distance / (double)duration*3600;
        return String.format("%.2f", kmph) + "km/h";
    }

}
