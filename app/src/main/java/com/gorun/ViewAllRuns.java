package com.gorun;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gorun.jp.a14_04.R;

/**
 * A class to display all records in the runs table of the database
 */
public class ViewAllRuns extends AppCompatActivity {

    // DB helper class
    DBHelper myDB;

    // listView UI element
    private ListView runList;
    private TextView noRunMessage;
    private SAutoBgButton menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_runs);

        myDB = new DBHelper(this);




        // populate list view with data
        populateListView();
        // configure actions for clicking on list items
        clickListItem();

        //configure action for clicking menu button
        menuButton();


    }

    public void menuButton(){
        menu = (SAutoBgButton) findViewById(R.id.menuButton);

        menu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload list view
        populateListView();

    }

    /**
     * Populate the list view with records from the database
     */
    private void populateListView() {
        // cursor object containing query results
        Cursor cursor = myDB.getAllRuns();

        // allow activity to manage lifetime of cursor
        startManagingCursor(cursor);

        // set up mapping from cursor to view fields
        String[] columns = {"name", "date", "duration", "distance", "speed", "time"};
        int[] IDs = {R.id.runName, R.id.runDate, R.id.runDuration, R.id.runDistance, R.id.runSpeed, R.id.runTime};

        // create adapter to map columns to UI elements
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.view_all_list_item, cursor, columns, IDs, 0);
        runList = (ListView)findViewById(R.id.runListView);
        runList.setAdapter(adapter);

        // If list view is empty, display empty message
        View empty = findViewById(R.id.empty);
        runList.setEmptyView(empty);

    }

    /**
     * Specify on click and on long click actions for listview items
     */
    public void clickListItem(){
        runList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // cursor object containing details of the run which has been clicked
                Cursor cursor = myDB.getRun(id);
                if (cursor.moveToFirst()){
                    // String variables to contain attributes of run
                    String runID = cursor.getString(0);
                    String name = cursor.getString(1);
                    String date = cursor.getString(2);
                    String time = cursor.getString(3);

                    // open map activity passing in runID, name, date and time
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("runID", runID);
                    intent.putExtra("name", name);
                    intent.putExtra("date", date);
                    intent.putExtra("time", time);
                    startActivity(intent);


                    Toast.makeText(ViewAllRuns.this, "Loading map...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        runList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // cursor object containing details of the run which has been long clicked
                Cursor cursor = myDB.getRun(id);
                if (cursor.moveToFirst()){
                    // pop up alert to confirm delete
                    confirmDeleteDialog(cursor);
                }
                return true;
            }
        });
    }

    /**
     * Configure the Dialog which appears when an item is long clicked
     * @param cursor Cursor object containing attributes of the run which has been long clicked
     */
    public void confirmDeleteDialog(Cursor cursor){
        cursor.moveToFirst();
        // variables to contain attributes of run
        final String runID = cursor.getString(0);
        String name = cursor.getString(1);
        String date = cursor.getString(2);
        String time = cursor.getString(3);

        // display alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewAllRuns.this, R.style.AlertDialogCustom);
        builder.setMessage("Are you sure you wish to delete this run?\n" + "ID: " + runID + "\n" +
                "Name: " + name + "\n" +
                "Date: " + date + "\n" +
                "Time: " + time)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete record
                        myDB.deleteRun(Integer.parseInt(runID));
                        // refresh list view to remove deleted element
                        populateListView();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing when cancel button is clicked
                    }
                })
                .setTitle("Delete Run?")
                .show();


    }

}