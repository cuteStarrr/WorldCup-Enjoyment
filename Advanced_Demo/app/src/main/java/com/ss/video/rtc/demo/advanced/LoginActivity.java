package com.ss.video.rtc.demo.advanced;

import static com.ss.video.rtc.demo.advanced.RoomCreation.map2params;
import static com.ss.video.rtc.demo.advanced.RoomCreation.startActivityBasedOnNum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.ss.bytertc.engine.RTCEngine;
import com.ss.rtc.demo.advanced.R;
import com.ss.video.rtc.demo.advanced.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * VolcEngineRTC 音视频通话入口页面
 * <p>
 * 包含如下简单功能：
 * - 该页面用来跳转至音视频通话主页面
 * - 申请相关权限
 * - 校验房间名和用户名
 * - 展示当前 SDK 使用的版本号 {@link RTCEngine#getSdkVersion()}
 * <p>
 * 有以下常见的注意事项：
 * 1.SDK必要的权限有：外部内存读写、摄像头权限、麦克风权限，其余完整的权限参见{@link src/main/AndroidManifest.xml}。
 * 没有这些权限不会导致崩溃，但是会影响SDK的正常使用。
 * 2.SDK 对房间名、用户名的限制是：非空且最大长度不超过128位的数字、大小写字母、@ . _ \ -
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSIONS_REQUEST_CODE = 1000;
    private Context mContext;
    private EditText mRoomInput;
    private EditText mUserInput;
//    private String mResult;
//    private Boolean flag_password = false;
//    private Boolean flag_max4 = true;
//    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = getApplicationContext();
        TextView title = findViewById(R.id.title_bar_title_tv);
        title.setText(R.string.login);
        mRoomInput = findViewById(R.id.room_id_input);
        mUserInput = findViewById(R.id.user_id_input);
        findViewById(R.id.join_room_btn).setOnClickListener(this);
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

    private void joinChannel(String roomId, String userId) {
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "房间号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.matches(Constants.INPUT_REGEX, roomId)) {
            Toast.makeText(this, "房间号格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.matches(Constants.INPUT_REGEX, userId)) {
            Toast.makeText(this, "用户名格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest queryRequest = new StringRequest(Request.Method.POST, Constants.DATABASE_URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("query", "receive success");
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                            String result = jsonObject.getString("Result");
                            //Boolean flag_password = false;
                            Log.d("query", "the result is: " + result);
                            //mResult = result;
                            if(result.equals("success")) {
                                //flag_password = Integer.valueOf(jsonObject.getString("Flag_ifPassword")) == 1;
                                String mToken = jsonObject.getString("Token");
                                Boolean flag_max4 = Integer.valueOf(jsonObject.getString("Maxpeople")) == 4;
                                startActivity(startActivityBasedOnNum(roomId, userId, mToken, flag_max4, getApplicationContext()));
                            }
                            else {
                                if(result.equals("null")) {
                                    Toast.makeText(getApplicationContext(), "该房间号不存在", Toast.LENGTH_SHORT).show();
                                    return ;
                                }
                                else {
                                    PasswordInputDialogFragment password_dialog = new PasswordInputDialogFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Constants.ROOM_ID_EXTRA, roomId);
                                    bundle.putString(Constants.USER_ID_EXTRA, userId);
                                    //bundle.putString(Constants.PASSWORD_EXTRA, password);
                                    password_dialog.setArguments(bundle);

                                    password_dialog.show(getSupportFragmentManager(), "password");
                                }
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
                return map2params(roomId, userId, "", "Login");
            }
        };

//        JSONObject params = map2Jsonobj(roomId, "", "Login");
//        JsonObjectRequest queryRequest = new JsonObjectRequest(Request.Method.POST, Constants.DATABASE_URI, params,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            String result = response.getString("Result");
//                            mResult = result;
//                            if(result.equals("success")) {
//                                flag_password = response.getBoolean("Flag_ifPassword");
//                                mToken = response.getString("Token");
//                                flag_max4 = response.getInt("Maxpeople") == 4;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.getMessage();
//            }
//        });

        RoomInfoRequestSingleton.getInstance(this).addToRequestQueue(queryRequest);

//        if(mResult.equals("null")) {
//            Toast.makeText(this, "该房间号不存在", Toast.LENGTH_SHORT).show();
//            return ;
//        } else {
//            if(flag_password) {
//                PasswordInputDialogFragment password_dialog = new PasswordInputDialogFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString(Constants.ROOM_ID_EXTRA, roomId);
//                bundle.putString(Constants.USER_ID_EXTRA, userId);
//                //bundle.putString(Constants.PASSWORD_EXTRA, password);
//                password_dialog.setArguments(bundle);
//                password_dialog.show(getSupportFragmentManager(), "password");
//            }
//            else {
//                startActivity(startActivityBasedOnNum(roomId, userId, mToken, flag_max4, this));
//            }
//        }

//        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.password_file), Context.MODE_PRIVATE);
//        String password = sharedPref.getString(roomId, getString(R.string.default_preference_string));
//        if(!password.equals(getString(R.string.default_preference_string))) {
//            if(!TextUtils.isEmpty(password)) {
//                PasswordInputDialogFragment password_dialog = new PasswordInputDialogFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString(Constants.ROOM_ID_EXTRA, roomId);
//                bundle.putString(Constants.USER_ID_EXTRA, userId);
//                bundle.putString(Constants.PASSWORD_EXTRA, password);
//                password_dialog.setArguments(bundle);
//                password_dialog.show(getSupportFragmentManager(), "password");
//            }
//            else {
//                SharedPreferences sharedPref_num = getSharedPreferences(getString(R.string.maxnum_file), Context.MODE_PRIVATE);
//                Boolean num_flag = sharedPref_num.getBoolean(roomId, true);
//                startActivity(startActivityBasedOnNum(roomId, userId, num_flag, this));
//            }
//        }
//        else {
//            Toast.makeText(this, "该房间号不存在", Toast.LENGTH_SHORT).show();
//            return ;
//        }

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
        if (id == R.id.join_room_btn) {
            String roomId = mRoomInput.getText().toString();
            String userId = mUserInput.getText().toString();
            joinChannel(roomId, userId);
        } else if (id == R.id.setting_btn_tv) {
            PreJoinSettingsDialog settingsDialog = new PreJoinSettingsDialog();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(settingsDialog, PreJoinSettingsDialog.TAG_FOR_SHOW);
            ft.commitAllowingStateLoss();
        }
    }

    public void startActivityInDialog(String rooid, String userid, String token, Boolean flag) {
        startActivity(startActivityBasedOnNum(rooid, userid, token, flag, this));
    }
}