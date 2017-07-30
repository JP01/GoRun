package com.gorun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.gorun.jp.a14_04.R;

public class MainActivity extends AppCompatActivity {

    private SAutoBgButton newRun, previousRuns;
    private EditText newRunName, nameInput;
    DBHelper myDB;
    private String previousActivity = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        newRun = (SAutoBgButton) findViewById(R.id.newRunButton);
        previousRuns = (SAutoBgButton) findViewById(R.id.previousRunsButton);


        newRun.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                NoticeDialogFragment nDF = new NoticeDialogFragment();
                nDF.show(getFragmentManager(), "enterRunName");


            }
        });

        previousRuns.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewAllRuns.class);
                startActivity(intent);
            }
        });

        Bundle extras = getIntent().getExtras();
        //Used for backButton handling
        if (extras!=null){
            previousActivity = String.valueOf(extras.getString("FROM_LOGRUN"));
        }

    }


    @Override
    public void onBackPressed(){
        if (previousActivity.equals("TRUE")){
            previousActivity = "FALSE";
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }


}
