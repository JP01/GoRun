package com.gorun;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gorun.jp.a14_04.R;

/**
 * Dialog Class used to create a new run
 */
public class NoticeDialogFragment extends DialogFragment {

    DBHelper myDB;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final Context context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.dialog_runname, null);
        final EditText newRunName = (EditText) view.findViewById(R.id.newRunName);

        myDB = new DBHelper(context);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        //checks for empty or whitespace RunName
                        if (newRunName.getText().toString().trim().isEmpty()) {
                            // Alert user with toast
                            Toast.makeText(getActivity(), "Please enter a name for your run!", Toast.LENGTH_SHORT).show();
                            //checks for max chars
                        } else if (newRunName.getText().toString().trim().length() > 15) {
                            // Alert user with toast
                            Toast.makeText(getActivity(), "Run Name must be less than 15 chars", Toast.LENGTH_SHORT).show();
                         }else{
                            // create new run entry in database
                            boolean isInserted = myDB.insertRun(newRunName.getText().toString());

                            if (isInserted) {
                                // alert user with toast
                                Toast.makeText(getActivity(), "New Run Successfully Created", Toast.LENGTH_SHORT).show();
                                // open new run activity
                                Intent intent = new Intent(getActivity(), LogRun.class);
                                startActivity(intent);
                            } else {
                                // Alert user with toast
                                Toast.makeText(getActivity(), "Error creating new run", Toast.LENGTH_SHORT).show();
                            }
                        }

                        }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NoticeDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }



}
