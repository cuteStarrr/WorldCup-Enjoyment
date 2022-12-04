package com.ss.video.rtc.demo.advanced;

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
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.rtc.demo.advanced.R;
import com.ss.video.rtc.demo.advanced.rtctoken.AccessToken;
import com.ss.video.rtc.demo.advanced.rtctoken.Utils;
import com.ss.video.rtc.demo.advanced.utils.CommonUtil;

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

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.password_file), Context.MODE_PRIVATE);
        String exist_flag = sharedPref.getString(roomId, getString(R.string.default_preference_string));
        if(!exist_flag.equals(getString(R.string.default_preference_string))) {
            Toast.makeText(this, "该房间号已存在，请修改房间号", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPref_num = getSharedPreferences(getString(R.string.maxnum_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_num = sharedPref_num.edit();
        editor_num.putBoolean(roomId, check_flag_4);
        editor_num.apply();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(roomId, password);
        editor.apply();

        initToken(roomId, userId);

        startActivity(startActivityBasedOnNum(roomId, userId, check_flag_4, this));

    }

    public static Intent startActivityBasedOnNum(String roomId, String userId, Boolean num_flag, Context context) {
        if(num_flag) {
            Intent intent = new Intent(context, RTCRoomActivity.class);
            intent.putExtra(Constants.ROOM_ID_EXTRA, roomId);
            intent.putExtra(Constants.USER_ID_EXTRA, userId);
            return intent;
        }
        else {
            Intent intent = new Intent(context, RTCRoom4EightActivity.class);
            intent.putExtra(Constants.ROOM_ID_EXTRA, roomId);
            intent.putExtra(Constants.USER_ID_EXTRA, userId);
            return intent;
        }
    }

    private void initToken(String roomID, String userID) {
        AccessToken token = new AccessToken(Constants.APPID, Constants.APPKEY, roomID, userID);
        token.ExpireTime(Utils.getTimestamp() + 3600);
        token.AddPrivilege(AccessToken.Privileges.PrivSubscribeStream, 0);
        token.AddPrivilege(AccessToken.Privileges.PrivPublishStream, Utils.getTimestamp() + 3600);
        String s = token.Serialize();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.token_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(roomID, s);
        editor.apply();

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