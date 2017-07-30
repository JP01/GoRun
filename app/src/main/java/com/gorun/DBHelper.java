package com.gorun;

/**
 * Class containing all database manipulation functionality
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {

    public Cursor cur;
    public DBHelper(Context context){
        // define running database to be created
        super(context, "running.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create waypoints table with id, lat, long, date and time (h, m, s, ms)
        db.execSQL("create table waypoints (id INTEGER PRIMARY KEY AUTOINCREMENT, runID INTEGER, latitude TEXT, longitude TEXT, date TEXT, hour TEXT, min TEXT, sec TEXT, ms TEXT)");
        // create runs table with id, name, start date and start time
        db.execSQL("create table runs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date TEXT, time TEXT, distance TEXT, duration TEXT, speed TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop table and recreate to avoid duplicates
        db.execSQL("DROP TABLE IF EXISTS waypoints");
        onCreate(db);
    }

    /**
     * Get the ID of the last entry in the runs table
     * @return the latest run ID
     */
    public String GetLastRunID(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("SELECT MAX(id) FROM runs", null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    /**
     * Get the Name of the last entry in the runs table
     * @return the latest run ID
     */
    public String GetLastRunName(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT name FROM runs WHERE id = " + GetLastRunID();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }


    /**
     * Get a Cursor object containing every run in the runs table
     * @return all runs
     */
    public Cursor getAllRuns(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT id as _id, name, date, time, distance, duration, speed FROM runs ORDER BY _id DESC", null);
        if (c != null) {
            c.moveToFirst();
        }

        return c;
    }

    /**
     * Get a Cursor object containing the details for a single run
     * @param id the ID of the run in question
     * @return details of the run
     */
    public Cursor getRun(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = 	db.rawQuery("select * from runs where id = "+id, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * Get waypoint details for a specific run
     * @param runID the ID of the run in question
     * @return a Cursor object containing run details
     */
    public Cursor getWaypointDetails(long runID){
        String query = "SELECT id as _id, latitude, longitude, date FROM waypoints WHERE runID = " + runID;
        cur = executeCursorQuery(query);
        return cur;
    }


    /**
     * Execute any Cursor query on the database
     * @param query the SQL query to execute
     * @return the resulting Cursor
     */
    public Cursor executeCursorQuery(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    /**
     * Insert a new run in the runs table
     * @param name the name for the new run
     * @return true/false depending on success
     */
    public boolean insertRun(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);
        // formatted date and time
        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        contentValues.put("date", date);
        contentValues.put("time", time);

        long result = db.insert("runs", null, contentValues);
        // result will be -1 if an error occurs
        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    /**
     * Delete a specific run from the database
     * @param runID the ID of the run to delete
     */
    public void deleteRun(int runID){
        SQLiteDatabase db = this.getWritableDatabase();
        // delete the run and all its corresponding points
        db.execSQL("DELETE FROM runs WHERE id="+runID);
        db.execSQL("DELETE FROM waypoints WHERE runID="+runID);

    }

    /**
     * Insert a new waypoint in the waypoints table for a specific run
     * @param latitude the latitude of the point
     * @param longitude the longitude of the point
     * @param runID the runID to assign the point to
     * @return true/false depending on success
     */
    public boolean insertWaypoint(String latitude, String longitude, String runID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("runID", runID);

        // formatted date
        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        contentValues.put("date", date);

        // get time values
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);
        int ms = Calendar.getInstance().get(Calendar.MILLISECOND);
        contentValues.put("hour", hour);
        contentValues.put("min", min);
        contentValues.put("sec", sec);
        contentValues.put("ms", ms);

        long result = db.insert("waypoints", null, contentValues);
        // result will be -1 if an error occurs
        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the time of the first or last point for a specific run
     * @param runID the runID to work with
     * @param firstLast either 'first' or 'last' to indicate first or last point
     * @return
     */
    public int[] getFirstLastPointTime(int runID, String firstLast){
        SQLiteDatabase db = this.getReadableDatabase();

        String maxMin = "";

        switch (firstLast){
            case "first": maxMin="min";
                break;
            case "last": maxMin="max";
                break;
        }

        Cursor c = 	db.rawQuery("select hour, min, sec, ms from waypoints where id = " +
                "(select "+maxMin+"(id) from waypoints where runID = "+runID+")", null);
        if (c != null) {
            c.moveToFirst();
        }

        // add time to array
        int[] time = {Integer.valueOf(c.getString(0)), Integer.valueOf(c.getString(1)), Integer.valueOf(c.getString(2)), Integer.valueOf(c.getString(3))};

        return time;

    }

    /**
     * Updates a record of a run with calculated run stats
     * @param runID the run to update
     * @param duration the duration of the run
     * @param speed the speed of the run
     * @param distance the distance of the run
     */
    public void updateRunStats(int runID, String duration, String speed, int distance){
        SQLiteDatabase db = this.getWritableDatabase();
        //db.execSQL("UPDATE runs SET "+column+"='"+data+"' WHERE id = "+runID);
        db.execSQL("UPDATE runs SET duration='"+duration+"', speed='"+speed+"', distance='"+String.valueOf(Double.valueOf(distance)/1000 + "km"+"' WHERE id = "+runID));

    }

}