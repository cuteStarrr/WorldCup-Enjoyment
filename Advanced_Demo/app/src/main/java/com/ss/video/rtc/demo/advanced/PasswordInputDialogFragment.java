package com.ss.video.rtc.demo.advanced;

import static com.ss.video.rtc.demo.advanced.RoomCreation.startActivityBasedOnNum;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.ss.rtc.demo.advanced.R;

public class PasswordInputDialogFragment extends DialogFragment {
    private EditText mPassword;
    private String roomID;
    private String userID;
    private String password;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle bundle = getArguments();
        roomID = bundle.getString(Constants.ROOM_ID_EXTRA);
        userID = bundle.getString(Constants.USER_ID_EXTRA);
        password = bundle.getString(Constants.PASSWORD_EXTRA);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialog_layout = inflater.inflate(R.layout.dialog_password_input, null);
        builder.setView(dialog_layout)
                .setTitle(R.string.dialog_input_password)
                // Add action buttons
                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        mPassword = dialog_layout.findViewById(R.id.password);
                        if(mPassword.getText().toString().equals(password)) {
                            SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.maxnum_file), Context.MODE_PRIVATE);
                            Boolean num_flag = sharedPref.getBoolean(roomID, true);
                            startActivity(startActivityBasedOnNum(roomID, userID, num_flag, getContext()));
                        }
                        else {
                            Toast.makeText(getContext(), "密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PasswordInputDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
