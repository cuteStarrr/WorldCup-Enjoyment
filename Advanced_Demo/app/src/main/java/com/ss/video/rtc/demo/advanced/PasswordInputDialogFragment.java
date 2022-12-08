package com.ss.video.rtc.demo.advanced;

import static com.ss.video.rtc.demo.advanced.RoomCreation.map2params;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ss.rtc.demo.advanced.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PasswordInputDialogFragment extends DialogFragment {
    private EditText mPassword;
    private LoginActivity mActivity;
    private String roomID;
    private String userID;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle bundle = getArguments();
        roomID = bundle.getString(Constants.ROOM_ID_EXTRA);
        userID = bundle.getString(Constants.USER_ID_EXTRA);
        mActivity = (LoginActivity) getContext();

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
                        String input_password = mPassword.getText().toString();

                        StringRequest queryRequest = new StringRequest(Request.Method.POST, Constants.DATABASE_URI,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            //Log.d("query", "receive success");
                                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                                            String result = jsonObject.getString("Result");
                                            //Log.d("query", "the result is: " + result);

                                            if(result.equals("success")) {
                                                String mToken = jsonObject.getString("Token");
                                                //Log.d("query", "the token is: " + mToken);
                                                Boolean flag_max4 = Integer.valueOf(jsonObject.getString("Maxpeople")) == 4;
                                                //Log.d("query", "the flag_max4 is: " + flag_max4);

                                                mActivity.startActivityInDialog(roomID, userID, mToken, flag_max4);
                                            }
                                            else {
                                                Toast.makeText(mActivity, "密码错误", Toast.LENGTH_SHORT).show();
                                                return;

                                            }

                                        } catch (JSONException e) {
                                            Log.d("query", "JSONException error: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("query", error.getMessage());
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                return map2params(roomID, userID, input_password, "Login");
                            }
                        };

                        RoomInfoRequestSingleton.getInstance(getContext()).addToRequestQueue(queryRequest);

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
