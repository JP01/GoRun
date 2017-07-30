package com.gorun;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gorun.jp.a14_04.R;

public class SingleRunDetails extends Fragment {
    private TextView runIDText, nameText, dateText, timeText, speedText, distanceText, durationText;
    private String runIDString, nameString, dateString, timeString, speedString, distanceString, durationString;
    DBHelper db;
    private SAutoBgButton menuButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_run_details, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DBHelper(getContext());

        menuButton = (SAutoBgButton) view.findViewById(R.id.menuButton);
        // populate the text fields
        populateText();


        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });


    }

    /**
     * Populates the text fields on the run details screen with details for that run
     */
    public void populateText(){
        Bundle extras = getActivity().getIntent().getExtras();
        runIDString = extras.getString("runID");
        Cursor cursor = db.getRun(Integer.parseInt(runIDString));

        // check if stats have been calculated by the map activity
        if (cursor.getString(6)==null){
            // if not, wait 1 second and start method again
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    populateText();
                }
            }, 1000);
        }

        // assign string variables
        nameString = cursor.getString(1);
        dateString = cursor.getString(2);
        timeString = cursor.getString(3);
        distanceString = cursor.getString(4);
        durationString = cursor.getString(5);
        speedString = cursor.getString(6);


        // instantiate and populate text views
        runIDText = (TextView)getView().findViewById(R.id.runID);
        nameText = (TextView)getView().findViewById(R.id.runName);
        dateText = (TextView)getView().findViewById(R.id.runDate);
        timeText = (TextView)getView().findViewById(R.id.runTime);
        speedText = (TextView)getView().findViewById(R.id.runSpeed);
        distanceText = (TextView)getView().findViewById(R.id.runDistance);
        durationText = (TextView)getView().findViewById(R.id.runDuration);

        runIDText.setText(runIDString);
        nameText.setText(nameString);
        dateText.setText(dateString);
        timeText.setText(timeString);
        speedText.setText(speedString);
        distanceText.setText(distanceString);
        durationText.setText(durationString);

    }
}
