package com.ss.video.rtc.demo.advanced;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ss.bytertc.engine.RTCEngine;
import com.ss.rtc.demo.advanced.R;
import com.ss.video.rtc.demo.advanced.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RoomCreation extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 1000;
    private Context mContext;
    private EditText mRoomInput;
    private EditText mUserInput;
    private EditText mPasswordInput;
    private Boolean check_flag_4 = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_creation);
        mContext = getApplicationContext();
        TextView title = findViewById(R.id.title_bar_title_tv);
        title.setText(R.string.create_room);
        mRoomInput = findViewById(R.id.creation_room_id_input);
        mUserInput = findViewById(R.id.creation_user_id_input);
        mPasswordInput = findViewById(R.id.creation_password_input);
        findViewById(R.id.create_room_btn).setOnClickListener(this);
        findViewById(R.id.setting_btn_tv).setOnClickListener(this);
        // 获取当前SDK的版本号
        String sdkVersion = RTCEngine.getSdkVersion();
        TextView versionTv = findViewById(R.id.version_tv);
        versionTv.setText(String.format("VolcEngineRTC v%s", sdkVersion));

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_REQUEST_CODE);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.creation_ratiobutton_maxnum_4:
                if (checked) {
                    check_flag_4 = true;
                    break;
                }
            case R.id.creation_ratiobutton_maxnum_8:
                if (checked) {
                    check_flag_4 = false;
                    break;
                }
        }
    }

    private void joinChannel(String roomId, String userId, String password) {
        Log.d("query", "joinChannel: step into");
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "房间号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.matches(Constants.INPUT_REGEX, roomId)) {
            Toast.makeText(this, "房间号格式错误,正确格式应为：非空且最大长度不超过128位的数字、大小写字母、@ . _ -", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.matches(Constants.INPUT_REGEX, userId)) {
            Toast.makeText(this, "用户名格式错误,正确格式应为：非空且最大长度不超过128位的数字、大小写字母、@ . _ -", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.matches(Constants.PASSWORD_REGEX, password)) {
            Toast.makeText(this, "密码格式错误,正确格式应为：最大长度不超过128位的数字和大小写字母", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("query", "joinChannel: start query");

        RequestQueue queue = RoomInfoRequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();


        StringRequest queryRequest = new StringRequest(Request.Method.POST, Constants.DATABASE_URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
//                            Log.d("query", "receive success");
//                            Log.d("query", "the response is: " + response);
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                            String result_query = jsonObject.getString("Result");
//                            Log.d("query", "the result is: " + result_query);

                            if(result_query.equals("null")) {
                                StringRequest registerRequest = new StringRequest(Request.Method.POST, Constants.DATABASE_URI, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            //Log.d("register", "receive success");
                                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                                            String result_register = jsonObject.getString("Result");
                                            //Log.d("register", "the result is: " + result_register);

                                            String token = jsonObject.getString("Token");
                                            //Log.d("register", "the token is: " + token);
                                            if(result_register.equals("failed")) {
                                                Toast.makeText(getApplicationContext(), "创建房间失败，请联系技术人员", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            else {
                                                startActivity(startActivityBasedOnNum(roomId, userId, token, check_flag_4, getApplicationContext()));
                                            }

                                        } catch (JSONException e) {
                                            Log.d("query", "JSONException error: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("register", error.getMessage());
                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String, String> register_params = new HashMap<String, String>();
                                        register_params.put("RequestType", "Register");
                                        register_params.put("roomid", roomId);
                                        register_params.put("password", password);
                                        register_params.put("userid", userId);
                                        if(check_flag_4) {
                                            register_params.put("maxpeople", "4");
                                        }
                                        else {
                                            register_params.put("maxpeople", "8");
                                        }

                                        return register_params;
                                    }
                                };

                                RoomInfoRequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(registerRequest);

                            }
                            else {
                                Toast.makeText(getApplicationContext(), "该房间号已存在，请修改房间号", Toast.LENGTH_SHORT).show();
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
                return map2params(roomId, userId, password, "Login");
            }
        };

        //Log.d("query", "add the query");
        RoomInfoRequestSingleton.getInstance(this).addToRequestQueue(queryRequest);
        //Log.d("query", "after add the query");

    }

    public static Map<String, String> map2params(String roomid, String userid, String password, String requesttype) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("RequestType", requesttype);
        map.put("roomid", roomid);
        map.put("password", password);
        map.put("userid", userid);
        return map;
    }

    public static Intent startActivityBasedOnNum(String roomId, String userId, String token, Boolean num_flag, Context context) {
        if(num_flag) {
            Intent intent = new Intent(context, RTCRoomActivity.class);
            intent.putExtra(Constants.ROOM_ID_EXTRA, roomId);
            intent.putExtra(Constants.USER_ID_EXTRA, userId);
            intent.putExtra(Constants.TOKEN_EXTRA, token);
            return intent;
        }
        else {
            Intent intent = new Intent(context, RTCRoom4EightActivity.class);
            intent.putExtra(Constants.ROOM_ID_EXTRA, roomId);
            intent.putExtra(Constants.USER_ID_EXTRA, userId);
            intent.putExtra(Constants.TOKEN_EXTRA, token);
            return intent;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            int len = permissions.length;
            if (len == 0) {
                return;
            }
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(permissions[i], Manifest.permission.CAMERA)
                        && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    CommonUtil.showShortToast(mContext, R.string.login_activity_permission_deny_hint_for_camera);
                }
                if (TextUtils.equals(permissions[i], Manifest.permission.RECORD_AUDIO)
                        && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    CommonUtil.showShortToast(mContext, R.string.login_activity_permission_deny_hint_for_audio);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.create_room_btn) {
            String roomId = mRoomInput.getText().toString();
            String userId = mUserInput.getText().toString();
            String password = mPasswordInput.getText().toString();
            joinChannel(roomId, userId, password);
        } else if (id == R.id.setting_btn_tv) {
            PreJoinSettingsDialog settingsDialog = new PreJoinSettingsDialog();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(settingsDialog, PreJoinSettingsDialog.TAG_FOR_SHOW);
            ft.commitAllowingStateLoss();
        }
    }
}