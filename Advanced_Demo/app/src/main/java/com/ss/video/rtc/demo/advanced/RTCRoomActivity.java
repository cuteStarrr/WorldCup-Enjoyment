package com.ss.video.rtc.demo.advanced;

import static com.ss.bytertc.engine.data.VideoSourceType.VIDEO_SOURCE_TYPE_EXTERNAL;
import static com.ss.bytertc.engine.data.VideoSourceType.VIDEO_SOURCE_TYPE_INTERNAL;
import static com.ss.bytertc.engine.type.AudioScenarioType.AUDIO_SCENARIO_HIGHQUALITY_COMMUNICATION;
import static com.ss.video.rtc.demo.advanced.utils.CommonUtil.byteBufferToString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoEncoderConfig;
import com.ss.bytertc.engine.data.AudioRoute;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.ScreenMediaType;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoPixelFormat;
import com.ss.bytertc.engine.data.VideoRotation;
import com.ss.bytertc.engine.data.VideoSourceType;
import com.ss.bytertc.engine.handler.AppExecutors;
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler;
import com.ss.bytertc.engine.handler.IRTCVideoEventHandler;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.MediaDeviceState;
import com.ss.bytertc.engine.type.MediaStreamType;
import com.ss.bytertc.engine.type.StreamRemoveReason;
import com.ss.bytertc.engine.type.VideoDeviceType;
import com.ss.bytertc.engine.video.IVideoSink;
import com.ss.bytertc.engine.video.builder.CpuBufferVideoFrameBuilder;
import com.ss.rtc.demo.advanced.R;
import com.ss.video.rtc.demo.advanced.chat.ChatMessage;
import com.ss.video.rtc.demo.advanced.effects.EffectNodeCallback;
import com.ss.video.rtc.demo.advanced.effects.dialog.VideoEffectDialog;
import com.ss.video.rtc.demo.advanced.effects.manager.VolcEffectManager;
import com.ss.video.rtc.demo.advanced.effects.model.BeautyEffectNode;
import com.ss.video.rtc.demo.advanced.effects.model.EffectModel;
import com.ss.video.rtc.demo.advanced.effects.model.EffectNode;
import com.ss.video.rtc.demo.advanced.effects.model.EffectSection;
import com.ss.video.rtc.demo.advanced.effects.model.FilterEffectNode;
import com.ss.video.rtc.demo.advanced.effects.model.StickerEffectNode;
import com.ss.video.rtc.demo.advanced.effects.model.VirtualBackgroundEffectNode;
import com.ss.video.rtc.demo.advanced.effects.resource.EffectResource;
import com.ss.video.rtc.demo.advanced.entity.VideoConfigEntity;
import com.ss.video.rtc.demo.advanced.external.CustomCapture;
import com.ss.video.rtc.demo.advanced.external.CustomRenderView;
import com.ss.video.rtc.demo.advanced.sharescreen.ShareScreenComponent;
import com.ss.video.rtc.demo.advanced.utils.CommonUtil;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;

import org.webrtc.RXScreenCaptureService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * VolcEngineRTC 视频通话的主页面
 * 本示例不限制房间内最大用户数；同时最多渲染四个用户的视频数据（自己和三个远端用户视频数据）；
 * <p>
 * 包含如下简单功能：
 * - 创建引擎
 * - 设置视频发布参数
 * - 渲染自己的视频数据
 * - 创建房间
 * - 加入音视频通话房间
 * - 打开/关闭麦克风
 * - 打开/关闭摄像头
 * - 渲染远端用户的视频数据
 * - 离开房间
 * - 销毁引擎
 * <p>
 * 实现一个基本的音视频通话的流程如下：
 * 1.创建 IRTCVideo 实例。
 * RTCVideo.createRTCVideo(Context context, String appId, IRTCVideoEventHandler handler,
 * Object eglContext, JSONObject parameters)
 * 2.视频发布端设置期望发布的最大分辨率视频流参数，包括分辨率、帧率、码率、缩放模式、网络不佳时的回退策略等。
 * RTCVideo.setVideoEncoderConfig(VideoEncoderConfig maxSolution)
 * 3.开启本地视频采集。 RTCVideo.startVideoCapture()
 * 4.设置本地视频渲染时，使用的视图，并设置渲染模式。
 * RTCVideo.setLocalVideoCanvas(StreamIndex streamIndex, VideoCanvas videoCanvas)
 * 5.创建房间。RTCVideo.createRTCRoom(String roomId)
 * 6.加入音视频通话房间。
 * RTCRoom.joinRoom(String token, UserInfo userInfo, RTCRoomConfig roomConfig)
 * 7.SDK 接收并解码远端视频流首帧后，设置用户的视频渲染视图。
 * RTCVideo.setRemoteVideoCanvas(String userId, StreamIndex streamIndex, VideoCanvas videoCanvas)
 * 8.在用户离开房间之后移出用户的视频渲染视图。
 * 9.离开音视频通话房间。RTCRoom.leaveRoom()
 * 10.调用 RTCRoom.destroy() 销毁房间对象。
 * 11.调用 RTCVideo.destroyRTCVideo() 销毁引擎。
 * <p>
 * 详细的API文档参见{https://www.volcengine.com/docs/6348/70080}
 */
public class RTCRoomActivity extends AppCompatActivity implements ConfigManger.ConfigObserver {
    private static final String TAG = "RTCRoomActivity";

    private String mRoomId;
    private String mUserId;

    private ImageView mSpeakerIv;
    private ImageView mAudioIv;
    private ImageView mVideoIv;
    private ImageView mBeautyIv;
    private TextView mUserIDTV;
    private ImageView mShareVideoIv;//开启/关闭分享本地视频图标
    private MediaProjectionManager mProjectionManager;//屏幕共享

    public static final int SELECT_LOCAL_VIDEO = 127;
    public static final int STOP_SHARING_VIDEO_CODE = 825; //结束本地视频共享标志


    private Uri mUri; //视频路径
    private boolean mShareVideo = false; //是否分享本地视频
    private boolean mIsSpeakerPhone = true;
    private boolean mIsMuteAudio = false;
    private boolean mIsMuteVideo = false;
    private CameraId mCameraID = CameraId.CAMERA_ID_FRONT;

    private ViewGroup.LayoutParams[] allLayoutParams; //记录布局 以便恢复
    private FrameLayout mSelfContainer;
    private FrameLayout[] mRemoteContainerArray;
    private TextView[] mUserIdTvArray;
    private final RemoteStreamKey[] mShowRemoteStreamArray = new RemoteStreamKey[3];

    private RTCVideo mRTCVideo;
    private RTCRoom mRTCRoom;
    private ShareScreenComponent mShareScreenComponent;

    private final VolcEffectManager mVolcEffectManager = new VolcEffectManager();
    private EffectModel mEffectModel;

    private ChatDialog mChatDialog = new ChatDialog();

    public int dpToPx(float dpValue) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private final EffectNodeCallback mEffectNodeCallback = new EffectNodeCallback() {
        @Override
        public void onEffectClicked(EffectNode effectNode) {
            RTCVideo rtcVideo = mRTCVideo;
            if (rtcVideo == null) {
                Log.i("EffectNodeCallback", "onEffectClicked() : RTCVideo is null");
                return;
            }
            if (effectNode instanceof BeautyEffectNode) {
                if (TextUtils.equals(((BeautyEffectNode) effectNode).type, "beauty")) {
                    mVolcEffectManager.onBeautyEffectChanged(effectNode.key, effectNode.value);
                } else if (TextUtils.equals(((BeautyEffectNode) effectNode).type, "reshape")) {
                    mVolcEffectManager.onReshapeEffectChanged(effectNode.key, effectNode.value);
                }
            } else if (effectNode instanceof FilterEffectNode) {
                mVolcEffectManager.onFilterEffectChanged(effectNode.key, effectNode.value);
            } else if (effectNode instanceof StickerEffectNode) {
                mVolcEffectManager.onStickerEffectClicked(effectNode.key, effectNode.selected);
            } else if (effectNode instanceof VirtualBackgroundEffectNode) {
                mVolcEffectManager.onVirtualEffectClicked(effectNode.key, effectNode.selected);
            } else {
                Log.i("EffectNodeCallback", "unknown EffectNode type");
            }
        }

        @Override
        public void onEffectValueChanged(EffectNode effectNode) {
            RTCVideo rtcVideo = mRTCVideo;
            if (rtcVideo == null) {
                Log.i("EffectNodeCallback", "onEffectValueChanged() : RTCEngine is null");
                return;
            }
            if (effectNode instanceof BeautyEffectNode) {
                if (TextUtils.equals(((BeautyEffectNode) effectNode).type, "beauty")) {
                    mVolcEffectManager.onBeautyEffectChanged(effectNode.key, effectNode.value);
                } else if (TextUtils.equals(((BeautyEffectNode) effectNode).type, "reshape")) {
                    mVolcEffectManager.onReshapeEffectChanged(effectNode.key, effectNode.value);
                }
            } else if (effectNode instanceof FilterEffectNode) {
                mVolcEffectManager.onFilterEffectChanged(EffectResource.getFilterPathByName(effectNode.key), effectNode.value);
            } else {
                Log.i("EffectNodeCallback", "invalid EffectNode type");
            }
        }
    };

    private IRTCRoomEventHandler mItcRoomEventHandler = new RTCRoomEventHandlerAdapter() {

        /**
         * 远端主播角色用户加入房间回调。
         */
        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            super.onUserJoined(userInfo, elapsed);
            Log.d(TAG, "onUserJoined: " + userInfo.getUid());
            mChatDialog.addReceiver(userInfo.getUid());
        }

        /**
         * 远端用户离开房间回调。
         */
        @Override
        public void onUserLeave(String uid, int reason) {
            super.onUserLeave(uid, reason);
            Log.d(TAG, "onUserLeave: " + uid);
            runOnUiThread(() -> removeRemoteView(uid));
            mChatDialog.removeReceiver(uid);
        }

        /**
         * 房间内新增远端屏幕共享音视频流的回调
         */
        @Override
        public void onUserPublishScreen(String uid, MediaStreamType type) {
            Log.d(TAG, "onUserPublishScreen: " + uid);
            if (type != MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO) {
                runOnUiThread(() -> setRemoteView(new RemoteStreamKey(mRoomId, uid, StreamIndex.STREAM_INDEX_SCREEN)));
            }
        }

        /**
         * 远端用户停止分享屏幕
         */
        @Override
        public void onUserUnpublishScreen(String uid, MediaStreamType type, StreamRemoveReason reason) {
            Log.d(TAG, "onUserUnPublishScreen: " + uid);
            if (type != MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO) {
                runOnUiThread(() -> refresh_after_fullscreen(uid));
            }
        }

        /**
         * 房间内新增远端摄像头/麦克风采集音视频流的回调。
         */
        @Override
        public void onUserPublishStream(String uid, MediaStreamType type) {
            Log.d(TAG, "onUserPublishStream: " + uid);
            if (type != MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO) {
                runOnUiThread(() -> setRemoteView(new RemoteStreamKey(mRoomId, uid, StreamIndex.STREAM_INDEX_MAIN)));
            }
        }

        /**
         * 房间内远端摄像头/麦克风采集的媒体流移除的回调
         */
        @Override
        public void onUserUnpublishStream(String uid, MediaStreamType type, StreamRemoveReason reason) {
            Log.d(TAG, "onUserUnPublishStream: " + uid);
            if (type != MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO) {
                runOnUiThread(() -> removeRemoteView(uid));
            }
        }


        @Override
        public void onUserMessageReceived(String uid, String message) {
            super.onUserMessageReceived(uid, message);
            showMessage(ChatMessage.TYPE_PRIVATE, uid, message);
        }

        @Override
        public void onUserBinaryMessageReceived(String uid, ByteBuffer message) {
            super.onUserBinaryMessageReceived(uid, message);
            showMessage(ChatMessage.TYPE_PRIVATE, uid, byteBufferToString(message));
        }

        @Override
        public void onRoomMessageReceived(String uid, String message) {
            super.onRoomMessageReceived(uid, message);
            showMessage(ChatMessage.TYPE_PUBLIC, uid, message);
        }

        @Override
        public void onRoomBinaryMessageReceived(String uid, ByteBuffer message) {
            super.onRoomBinaryMessageReceived(uid, message);
            showMessage(ChatMessage.TYPE_PUBLIC, uid, byteBufferToString(message));
        }

        @Override
        public void onUserMessageSendResult(long msgid, int error) {
            super.onUserMessageSendResult(msgid, error);
            String tip;
            if (error != 0) {
                tip = String.format(Locale.US, "点对点消息发送失败(%d)", error);
            } else {
                tip = "点对点消息发送成功";
            }
            runOnUiThread(() -> SafeToast.show(RTCRoomActivity.this, tip, Toast.LENGTH_SHORT));
        }

        @Override
        public void onRoomError(int err) {
            super.onRoomError(err);
            Log.d(TAG, "onRoomError: " + err);
            showAlertDialog(String.format(Locale.US, "error: %d", err));
        }

        @Override
        public void onRoomWarning(int warn) {
            super.onRoomWarning(warn);
            Log.d(TAG, "onRoomWarning: " + warn);
        }
    };

    private IRTCVideoEventHandler mIRtcEngineEventHandler = new IRTCVideoEventHandler() {

        @Override
        public void onSEIMessageReceived(RemoteStreamKey remoteStreamKey, ByteBuffer message) {
            super.onSEIMessageReceived(remoteStreamKey, message);
            runOnUiThread(() -> showMessage(ChatMessage.TYPE_SEI, remoteStreamKey.getUserId(), byteBufferToString(message)));
        }

        @Override
        public void onVideoDeviceStateChanged(String deviceId, VideoDeviceType deviceType, int deviceState, int deviceError) {
            if (deviceType == VideoDeviceType.VIDEO_DEVICE_TYPE_SCREEN_CAPTURE_DEVICE) {
                if (mShareScreenComponent == null) {
                    return;
                }
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (deviceState == MediaDeviceState.MEDIA_DEVICE_STATE_STARTED) {
                        mShareScreenComponent.publishScreen();
                    } else if (deviceState == MediaDeviceState.MEDIA_DEVICE_STATE_STOPPED) {
                        mShareScreenComponent.unPublishScreen();
                    }
                });
            }
        }

        /**
         * 警告回调，详细可以看 {https://www.volcengine.com/docs/6348/70082#warncode}
         */
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            Log.d(TAG, "onWarning: " + warn);
        }

        /**
         * 错误回调，详细可以看 {https://www.volcengine.com/docs/6348/70082#errorcode}
         */
        @Override
        public void onError(int err) {
            super.onError(err);
            Log.d(TAG, "onError: " + err);
            showAlertDialog(String.format(Locale.US, "error: %d", err));
        }
    };







    public void refresh_after_fullscreen(String uid){
        mSelfContainer.setLayoutParams(allLayoutParams[0]);
        for (int i = 0; i < mRemoteContainerArray.length; i++) {
            mRemoteContainerArray[i].setLayoutParams(allLayoutParams[i+1]);
        }
        removeRemoteView(uid);
    }








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "step into onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        //Log.d(TAG, "step into onCreate");
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getColor(R.color.white));
            getWindow().setNavigationBarColor(getColor(R.color.white));
        }
        Intent intent = getIntent();
        mRoomId = intent.getStringExtra(Constants.ROOM_ID_EXTRA);
        String userId = intent.getStringExtra(Constants.USER_ID_EXTRA);
        String token = intent.getStringExtra(Constants.TOKEN_EXTRA);
        //Log.d(TAG, "before initUI");
        mUserId = userId;

        initUI(mRoomId, userId);
        initEngineAndJoinRoom(mRoomId, userId, token);
        ConfigManger.getInstance().addObserver(this);
        setLocalRenderView(userId);
        startMediaCapture();

        int index = mVideoConfig.mLocalVideoMirrorType == 2 ? 3 : mVideoConfig.mLocalVideoMirrorType;
        mRTCVideo.setLocalVideoMirrorType(MirrorType.fromId(index));

        EffectResource.initVideoEffectResource();
        mEffectModel = new EffectModel(this);
        mVolcEffectManager.initEffect(mRTCVideo);

        setMoreFunctionButton();//设置更多功能按钮

        //设置聊天小窗
        mChatDialog.setConfig(mRTCRoom, mUserId);

        Log.e("RTCRoom", "RTCRoomActivity.onCreate  this:" + this);
    }

    private void setMoreFunctionButton() {
        //设置更多功能按钮
        ImageView button_more = findViewById(R.id.button_more);
        button_more.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View view) {
                //出现更多功能菜单
                PopupWindow popupWindow = new PopupWindow(RTCRoomActivity.this);
                popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(LayoutInflater.from(RTCRoomActivity.this).inflate(R.layout.more_function, null));
                popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                LinearLayout bottom_bar = findViewById(R.id.bottom_bar);
                popupWindow.showAsDropDown(findViewById(R.id.button_more), 0, -bottom_bar.getHeight() - dpToPx(44));

                //设置setting按钮点击事件
                ImageView button_setting = popupWindow.getContentView().findViewById(R.id.setting_btn);
                button_setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RoomSettingsDialog settingsDialog = new RoomSettingsDialog();
                        settingsDialog.setConfig(mRTCVideo, mRTCRoom);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        ft.add(settingsDialog, RoomSettingsDialog.TAG_FOR_SHOW);
                        ft.commitAllowingStateLoss();
                    }
                });

                //设置chat按钮
                ImageView button_chat = popupWindow.getContentView().findViewById(R.id.button_chat);
                button_chat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        mChatDialog.show(fragmentManager,ChatDialog.TAG_FOR_SHOW);
                    }
                });

                //设置切换分享本地视频的按钮的点击事件
                mShareVideoIv = popupWindow.getContentView().findViewById(R.id.switch_share_video);
                mShareVideoIv.setImageResource(mShareVideo ? R.drawable.screen_share_on : R.drawable.screen_share_off);
                mShareVideoIv.setColorFilter(mShareVideo ? Color.TRANSPARENT : Color.GRAY);
                mShareVideoIv.setOnClickListener((v) -> updateShareVideoStatus());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (mShareScreenComponent != null && !mShareVideo && requestCode == REQUEST_CODE_OF_SCREEN_SHARING) {
            mShareScreenComponent.handlePermissionResult(resultCode, data);
        } else if (requestCode == SELECT_LOCAL_VIDEO && resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Log.i("cc_test", "into on Activity result 127");
            mUri = data.getData();//得到uri，后面就是将uri转化成file的过程。
//            mVideoView.setVideoURI(uri);
            requestPermissionForScreenSharing();

//            mVideoView.start();
        } else if (mShareVideo && requestCode == REQUEST_CODE_OF_SCREEN_SHARING) {
            if (resultCode == Activity.RESULT_OK) {
                startVideoShareCapture(data);
            } else {
                Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
            }
        } else if(mShareVideo && requestCode == STOP_SHARING_VIDEO_CODE){
            updateShareVideoStatus();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initUI(String roomId, String userId) {
        mSelfContainer = findViewById(R.id.self_video_container);
        mRemoteContainerArray = new FrameLayout[]{
                findViewById(R.id.remote_video_0_container),
                findViewById(R.id.remote_video_1_container),
                findViewById(R.id.remote_video_2_container)
        };
        allLayoutParams = new ViewGroup.LayoutParams[]{
                mSelfContainer.getLayoutParams(),
                mRemoteContainerArray[0].getLayoutParams(),
                mRemoteContainerArray[1].getLayoutParams(),
                mRemoteContainerArray[2].getLayoutParams()
        };

        mUserIdTvArray = new TextView[]{
                findViewById(R.id.remote_video_0_user_id_tv),
                findViewById(R.id.remote_video_1_user_id_tv),
                findViewById(R.id.remote_video_2_user_id_tv)
        };
        findViewById(R.id.switch_camera).setOnClickListener((v) -> onSwitchCameraClick());
        mSpeakerIv = findViewById(R.id.switch_audio_router);
        mAudioIv = findViewById(R.id.switch_local_audio);
        mVideoIv = findViewById(R.id.switch_local_video);
        //mSetting = findViewById(R.id.setting_btn);
        mBeautyIv = findViewById(R.id.beauty);
        findViewById(R.id.hang_up).setOnClickListener((v) -> onBackPressed());
        mSpeakerIv.setOnClickListener((v) -> updateSpeakerStatus());
        mAudioIv.setOnClickListener((v) -> updateLocalAudioStatus());
        mVideoIv.setOnClickListener((v) -> updateLocalVideoStatus());
        mBeautyIv.setOnClickListener((v) -> showCVDialog());
        TextView roomIDTV = findViewById(R.id.room_id_text);
        mUserIDTV = findViewById(R.id.self_video_user_id_tv);
        roomIDTV.setText(String.format("RoomID:%s", roomId));
        mUserIDTV.setText(String.format("UserID:%s", userId));
        if (needShareScreen()) {
            mVideoIv.setImageResource(R.drawable.mute_video);
        }
    }

    private void showCVDialog() {
        final List<EffectSection> effectSectionList = new LinkedList<>();
        effectSectionList.add(mEffectModel.getBeautySection());
        effectSectionList.add(mEffectModel.getFilterSection());
        effectSectionList.add(mEffectModel.getStickerSection());
        effectSectionList.add(mEffectModel.getVirtualBackgroundSection());
        VideoEffectDialog videoEffectDialog = new VideoEffectDialog(this, effectSectionList, mEffectNodeCallback);
        videoEffectDialog.show();
    }

    private final VideoConfigEntity mVideoConfig = ConfigManger.getInstance().getVideoConfig();

    private void initEngineAndJoinRoom(String roomId, String userId, String token) {
        // 创建引擎
        mRTCVideo = RTCVideo.createRTCVideo(getApplicationContext(), Constants.APPID, mIRtcEngineEventHandler, null, null);
        // 设置视频发布参数
        VideoEncoderConfig videoEncoderConfig = new VideoEncoderConfig(
                mVideoConfig.getResolution().first,
                mVideoConfig.getResolution().second,
                mVideoConfig.getFrameRate(),
                mVideoConfig.getBitRate());
        mRTCVideo.setVideoEncoderConfig(videoEncoderConfig);
        mRTCVideo.setAudioScenario(AUDIO_SCENARIO_HIGHQUALITY_COMMUNICATION);
        // 加入房间
        mRTCRoom = mRTCVideo.createRTCRoom(roomId);
        mRTCRoom.setRTCRoomEventHandler(mItcRoomEventHandler);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                true, true, true);
//        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.token_file), Context.MODE_PRIVATE);
//        String token = sharedPref.getString(roomId, getString(R.string.default_preference_string));
        int joinRoomRes = mRTCRoom.joinRoom(token,
                UserInfo.create(userId, ""), roomConfig);
        Log.i(TAG, "initEngineAndJoinRoom: " + joinRoomRes);
    }

    private void startMediaCapture() {
        // 开启本地音频采集
        mRTCVideo.startAudioCapture();
        if (needShareScreen()) {
            mShareScreenComponent = new ShareScreenComponent(mRTCVideo, mRTCRoom, this);
            mShareScreenComponent.setStatusTvAndBtn(mUserIDTV, mBeautyIv);
            getLifecycle().addObserver(mShareScreenComponent);
            mShareScreenComponent.startScreenSharing();
            mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_VIDEO);
            return;
        }
        // 开启本地视频采集
        startVideoCapture();
    }

    private boolean needShareScreen() {
        return mVideoConfig != null && mVideoConfig.mVideoSource == VideoConfigEntity.VIDEO_SOURCE_TYPE_SCREEN;
    }

    private void setLocalRenderView(String uid) {
        if (needShareScreen()) {
            return;
        }
        boolean customRender = ConfigManger.getInstance().isCustomRender();
        if (customRender) {
            IVideoSink videoSink = new CustomRenderView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mSelfContainer.removeAllViews();
            mSelfContainer.addView((View) videoSink, params);
            mRTCVideo.setLocalVideoSink(StreamIndex.STREAM_INDEX_MAIN, videoSink, IVideoSink.PixelFormat.I420);
        } else {
            VideoCanvas videoCanvas = new VideoCanvas();
            videoCanvas.uid = uid;
            videoCanvas.isScreen = false;
            videoCanvas.renderMode = mVideoConfig.mLocalVideoFillMode;
            videoCanvas.renderView = new TextureView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mSelfContainer.removeAllViews();
            mSelfContainer.addView(videoCanvas.renderView, params);
            // 设置本地视频渲染视图
            mRTCVideo.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas);
        }
    }

    private void setRemoteView(RemoteStreamKey remoteStreamKey) {
        int renderIndex = -1;
        String uid = remoteStreamKey.getUserId();
        if (TextUtils.isEmpty(uid)) return;
        int firstEmptyIndex = -1;
        for (int i = 0; i < mShowRemoteStreamArray.length; i++) {
            String showedUid = mShowRemoteStreamArray[i] == null ? null : mShowRemoteStreamArray[i].getUserId();
            if (TextUtils.equals(uid, showedUid)) {
                renderIndex = i;
                break;
            }
            if (TextUtils.isEmpty(showedUid) && firstEmptyIndex < 0) {
                firstEmptyIndex = i;
            }
        }
        if (renderIndex < 0 && firstEmptyIndex >= 0) {
            renderIndex = firstEmptyIndex;
        }
        if (renderIndex < 0) {
            return;
        }
        mShowRemoteStreamArray[renderIndex] = remoteStreamKey;
        boolean sharingScreen = remoteStreamKey.getStreamIndex() == StreamIndex.STREAM_INDEX_SCREEN;
        mUserIdTvArray[renderIndex].setText(sharingScreen ? String.format("%s屏幕分享", uid) : String.format("UserId:%s", uid));
        setRemoteRenderView(sharingScreen, remoteStreamKey, mRemoteContainerArray[renderIndex]);
    }

    private void setRemoteRenderView(boolean sharingScreen, RemoteStreamKey remoteStreamKey, FrameLayout container) {
        boolean customRender = ConfigManger.getInstance().isCustomRender();
        if (customRender && !sharingScreen) {
            IVideoSink videoSink = new CustomRenderView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            container.removeAllViews();
            container.addView((View) videoSink, params);
            mRTCVideo.setRemoteVideoSink(remoteStreamKey, videoSink, IVideoSink.PixelFormat.I420);
        } else {
            if(sharingScreen){
                mSelfContainer.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                for (int i = 0; i < mRemoteContainerArray.length; i++) {
                    mRemoteContainerArray[i].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                }
            }
            VideoCanvas videoCanvas = new VideoCanvas();
            videoCanvas.renderView = new SurfaceView(Utilities.getApplicationContext());
            videoCanvas.roomId = remoteStreamKey.getRoomId();
            videoCanvas.uid = remoteStreamKey.getUserId();
            videoCanvas.renderMode = mVideoConfig.mRemoteVideoFillMode;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            container.removeAllViews();
            container.addView(videoCanvas.renderView, params);
            if(sharingScreen) {
                container.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
            //改container布局 变大
            // 设置远端用户视频渲染视图
            mRTCVideo.setRemoteVideoCanvas(remoteStreamKey.getUserId(), remoteStreamKey.getStreamIndex(), videoCanvas);
        }
    }

    private void removeRemoteView(String uid) {
        for (int i = 0; i < mShowRemoteStreamArray.length; i++) {
            String showedUid = mShowRemoteStreamArray[i] == null ? null : mShowRemoteStreamArray[i].getUserId();
            if (TextUtils.equals(uid, showedUid)) {
                mShowRemoteStreamArray[i] = null;
                TextView textView = mUserIdTvArray[i];
                Log.d(TAG, "removeRemoteView :" + textView.getText());
                textView.setText("");
                mRemoteContainerArray[i].removeAllViews();
            }
        }
    }

    private void onSwitchCameraClick() {
        boolean isCustomCapture = ConfigManger.getInstance().isCustomCapture();
        if (isCustomCapture) {
            CustomCapture.ins().switchCamera();
            return;
        }
        // 切换前置/后置摄像头（默认使用前置摄像头）
        if (mCameraID.equals(CameraId.CAMERA_ID_FRONT)) {
            mCameraID = CameraId.CAMERA_ID_BACK;
        } else {
            mCameraID = CameraId.CAMERA_ID_FRONT;
        }
        mRTCVideo.switchCamera(mCameraID);
    }

    private void updateSpeakerStatus() {
        mIsSpeakerPhone = !mIsSpeakerPhone;
        // 设置使用哪种方式播放音频数据
        mRTCVideo.setAudioRoute(mIsSpeakerPhone ? AudioRoute.AUDIO_ROUTE_SPEAKERPHONE
                : AudioRoute.AUDIO_ROUTE_EARPIECE);
        mSpeakerIv.setImageResource(mIsSpeakerPhone ? R.drawable.speaker_on : R.drawable.speaker_off);
    }

    private void updateLocalAudioStatus() {
        mIsMuteAudio = !mIsMuteAudio;
        // 开启/关闭本地音频发送
        if (mIsMuteAudio) {
            mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        } else {
            mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        }
        mAudioIv.setImageResource(mIsMuteAudio ? R.drawable.mute_audio : R.drawable.normal_audio);
    }

    private void updateLocalVideoStatus() {
        //屏幕分享时不支持切换摄像头
        if (needShareScreen()) {
            return;
        }
        mIsMuteVideo = !mIsMuteVideo;
        if (mIsMuteVideo) {
            // 关闭视频采集
            stopVideoCapture();
            // 这里换成黑屏
            VideoCanvas videoCanvas = new VideoCanvas();
            videoCanvas.uid = "";
            videoCanvas.isScreen = false;
            videoCanvas.renderMode = mVideoConfig.mLocalVideoFillMode;
            videoCanvas.renderView = new TextureView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mSelfContainer.removeAllViews();
            mSelfContainer.addView(videoCanvas.renderView, params);
            // 设置本地视频渲染视图
            mRTCVideo.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas);

        } else {
            // 开启视频采集

            startVideoCapture();
        }
        mVideoIv.setImageResource(mIsMuteVideo ? R.drawable.mute_video : R.drawable.normal_video);
    }


    private void updateShareVideoStatus(){
        if(needShareScreen()){
            return;
        }

        mShareVideo = !mShareVideo;

        mShareVideoIv.setImageResource(mShareVideo ? R.drawable.screen_share_on : R.drawable.screen_share_off);
        mShareVideoIv.setColorFilter(mShareVideo ? Color.TRANSPARENT : Color.GRAY);


        if(mShareVideo){
            stopVideoCapture();
            startVideoShare();
        }else {
            stopVideoShare();
            startVideoCapture();
        }
    }




    private void startVideoShare(){
        //开一个新页播视频
        mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_VIDEO);

        //读入本地视频
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        requestLocalFilePermission();
        startActivityForResult(intent, SELECT_LOCAL_VIDEO);


    }

    private void requestLocalFilePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }





    public static final int REQUEST_CODE_OF_SCREEN_SHARING = 101;

    /*** 向系统发起屏幕共享的权限请求*/
    private void requestPermissionForScreenSharing() {
        if (isFinishing()) {
            CommonUtil.showShortToast(Utilities.getApplicationContext(), "请求屏幕共享权限失败:activity is null/finishing");
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CommonUtil.showShortToast(this,  "当前系统版本过低，无法支持屏幕共享");
            return;
        }
        if (mProjectionManager == null) {
            mProjectionManager = (MediaProjectionManager) Utilities.getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
        if (mProjectionManager != null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_OF_SCREEN_SHARING);
        } else {
            CommonUtil.showShortToast(this, "当前系统版本过低，无法支持屏幕共享");
        }
    }


    private void startVideoShareCapture(Intent data) {
        startRXVideoShareService(data);
        //编码参数
        VideoEncoderConfig config = new VideoEncoderConfig();
        Pair<Integer, Integer> videoSize = mVideoConfig.getResolution();
        config.width = (videoSize.first != null && videoSize.first > 0) ? videoSize.first : 1280;
        config.height = (videoSize.second != null && videoSize.second > 0) ? videoSize.second : 720;
        config.frameRate = mVideoConfig.getFrameRate() > 0 ? mVideoConfig.getFrameRate() : 15;
        config.maxBitrate = mVideoConfig.getBitRate() > 0 ? mVideoConfig.getBitRate() : 1600;
        mRTCVideo.setScreenVideoEncoderConfig(config);
        // 开启屏幕视频数据采集
        mRTCVideo.startScreenCapture(ScreenMediaType.SCREEN_MEDIA_TYPE_VIDEO_AND_AUDIO, data);
        mRTCRoom.publishScreen(MediaStreamType.RTC_MEDIA_STREAM_TYPE_BOTH);

        Intent intent = new Intent(RTCRoomActivity.this, VideoPlayingActivity.class);
        intent.putExtra("uri", mUri.toString());
        startActivityForResult(intent, STOP_SHARING_VIDEO_CODE);
    }

    private void startRXVideoShareService(@Nullable Intent data) {
        Context context = Utilities.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent iData = new Intent();
            iData.putExtra(RXScreenCaptureService.KEY_LARGE_ICON, R.drawable.launcher_quick_start);
            iData.putExtra(RXScreenCaptureService.KEY_SMALL_ICON, R.drawable.launcher_quick_start);
            iData.putExtra(RXScreenCaptureService.KEY_LAUNCH_ACTIVITY, this.getClass().getCanonicalName());
            iData.putExtra(RXScreenCaptureService.KEY_CONTENT_TEXT, "正在录制/投射您的屏幕");
            iData.putExtra(RXScreenCaptureService.KEY_RESULT_DATA, data);
            context.startForegroundService(RXScreenCaptureService.getServiceIntent(context, RXScreenCaptureService.COMMAND_LAUNCH, iData));
        }
    }




    private void stopVideoShare(){
        mRTCVideo.stopScreenCapture();
        setLocalRenderView(mUserId);
    }


    private void showAlertDialog(String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message);
            builder.setPositiveButton("知道了", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    /* 视频采集:主要是自定义外部采集源相关功能 */

    /* 视频发布数据线程 */
    private HandlerThread mPushStreamThread;
    /* 视频发布数据线程对应Handler */
    private Handler mPushStreamHandler;
    private boolean mIsCapturing;


    private final Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mPushStreamThread == null || !mPushStreamThread.isAlive() || mPushStreamHandler == null) {
                return;
            }
            mPushStreamHandler.post(() -> {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                int cameraId = CustomCapture.ins().getCameraId();
                int width = size.width;
                int height = size.height;

                VideoRotation rotation;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = VideoRotation.VIDEO_ROTATION_270;
                } else {
                    android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                    android.hardware.Camera.getCameraInfo(CustomCapture.ins().getCameraId(), info);
                    rotation = VideoRotation.fromId(info.orientation);
                }

                byte[] yuv = CommonUtil.nv21ToI420(data, width, height);

                ByteBuffer directBuffer = ByteBuffer.allocateDirect(yuv.length);
                int chromaWidth = (width + 1) / 2;
                int chromaHeight = (height + 1) / 2;
                int uvSize = chromaWidth * chromaHeight;
                int uStart = width * height;
                int vStart = uStart + uvSize;

                directBuffer.put(yuv);
                directBuffer.rewind();
                directBuffer.limit(uStart);
                ByteBuffer directBufferY = directBuffer.slice();

                directBuffer.position(uStart);
                directBuffer.limit(uStart + uvSize);
                ByteBuffer directBufferU = directBuffer.slice();

                directBuffer.position(vStart);
                directBuffer.limit(vStart + uvSize);
                ByteBuffer directBufferV = directBuffer.slice();

                CpuBufferVideoFrameBuilder builder = new CpuBufferVideoFrameBuilder(VideoPixelFormat.kVideoPixelFormatI420);
                builder.setWidth(width)
                        .setHeight(height)
                        .setRotation(rotation)
                        .setTimeStampUs(System.currentTimeMillis() * TimeUnit.MILLISECONDS.toNanos(1))
                        .setPlaneData(0, directBufferY)
                        .setPlaneStride(0, width)
                        .setPlaneData(1, directBufferU)
                        .setPlaneStride(1, chromaWidth)
                        .setPlaneData(2, directBufferV)
                        .setPlaneStride(2, chromaWidth);

                if (mRTCVideo != null) {
                    mRTCVideo.pushExternalVideoFrame(builder.build());
                }

                CustomCapture.ins().updateBuffer();
            });
        }
    };

    private void startVideoCapture() {
        if (mIsCapturing) {
            return;
        }


        mRTCRoom.unpublishScreen(MediaStreamType.RTC_MEDIA_STREAM_TYPE_BOTH);
        mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_VIDEO);
        mIsCapturing = true;
        boolean isCustomCapture = ConfigManger.getInstance().isCustomCapture();
        mRTCVideo.setVideoSourceType(StreamIndex.STREAM_INDEX_MAIN, isCustomCapture
                ? VIDEO_SOURCE_TYPE_EXTERNAL
                : VIDEO_SOURCE_TYPE_INTERNAL);
        if (isCustomCapture) {
            mPushStreamThread = new HandlerThread("PushHandlerThread");
            mPushStreamThread.start();
            mPushStreamHandler = new Handler(mPushStreamThread.getLooper());
            mPushStreamHandler.post(() -> CustomCapture.ins().startCapture(this, mPreviewCallback));
        } else {
            mRTCVideo.startVideoCapture();
        }


    }

    private void stopVideoCapture() {
        if (!mIsCapturing) {
            return;
        }
        boolean isCustomCapture = ConfigManger.getInstance().isCustomCapture();
        CommonUtil.printDebugLog("RTCRoomActivity stopVideoCapture isCustomCapture:" + isCustomCapture);
        if (isCustomCapture) {
            if (mPushStreamHandler != null) {
                mPushStreamHandler.removeCallbacksAndMessages(null);
                mPushStreamHandler = null;
            }
            if (mPushStreamThread != null && mPushStreamThread.isAlive()) {
                mPushStreamThread.quit();
            }
            CustomCapture.ins().stopCapture();
        } else {
            if (mRTCVideo != null) {
                mRTCVideo.stopVideoCapture();
            }
        }
        mIsCapturing = false;
    }

    @Override
    public void finish() {
        super.finish();
        stopVideoCapture();
        if (needShareScreen()) {
            mShareScreenComponent.stopScreenSharing();
        }
        // 离开房间
        if (mRTCRoom != null) {
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
        }
        // 销毁引擎
        RTCVideo.destroyRTCVideo();
        mIRtcEngineEventHandler = null;
        mItcRoomEventHandler = null;
        mRTCVideo = null;
        ConfigManger.getInstance().removeObserver(this);
    }

    @Override
    public void onConfigChange(VideoConfigEntity config) {
        if (mRTCVideo == null) {
            return;
        }
        VideoEncoderConfig videoEncoderConfig = new VideoEncoderConfig(
                mVideoConfig.getResolution().first,
                mVideoConfig.getResolution().second,
                mVideoConfig.getFrameRate(),
                mVideoConfig.getBitRate());
        mRTCVideo.setVideoEncoderConfig(videoEncoderConfig);
        int index = config.mLocalVideoMirrorType == 2 ? 3 : config.mLocalVideoMirrorType;
        mRTCVideo.setLocalVideoMirrorType(MirrorType.fromId(index));
    }

    private void showMessage(int type, String uid, String message) {
//        runOnUiThread(() -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setCancelable(true);
//            builder.setTitle(title);
//            builder.setMessage(uid + ": " + message);
//            final AlertDialog dialog = builder.create();
//            dialog.show();
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                if (dialog.isShowing()) {
//                    dialog.dismiss();
//                }
//            }, 2000);
//        });
        ChatMessage message_t = new ChatMessage(message, uid, ChatMessage.TYPE_RECEIVED, type);
        mChatDialog.addMessage(message_t);
    }
}