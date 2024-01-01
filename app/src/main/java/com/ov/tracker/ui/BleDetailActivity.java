package com.ov.tracker.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson2.JSON;
import com.hjq.bar.TitleBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.tracker.R;
import com.ov.tracker.entity.BleDeviceInfo;
import com.ov.tracker.entity.CharacteristicDomain;
import com.ov.tracker.entity.DescriptorDomain;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.entity.ServicesPropertiesDomain;
import com.ov.tracker.service.BleService;
import com.ov.tracker.utils.BleDeviceUtil;
import com.ov.tracker.utils.permission.PermissionInterceptor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BleDetailActivity extends AppCompatActivity {
    private BleDeviceInfo info;
    private BleService bleService;
    private BleDeviceUtil bleDeviceUtil;
    @BindView(R.id.titleBar)
    TitleBar titleBar;
    @BindView(R.id.iv_device)
    ImageView iv_device;
    private Map<String, ServicesPropertiesDomain> domainMap = new ConcurrentHashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_detail);
        ButterKnife.bind(this);
        info = JSON.parseObject(getIntent().getStringExtra("data"), BleDeviceInfo.class);
        initService();
    }


    public void initService() {
        Intent intent = new Intent(BleDetailActivity.this, BleService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleService = ((BleService.BleServiceBinder) service).getService();
                XXPermissions.with(BleDetailActivity.this).permission(Permission.BLUETOOTH_SCAN).permission(Permission.BLUETOOTH_CONNECT).permission(Permission.BLUETOOTH_ADVERTISE).permission(Permission.ACCESS_FINE_LOCATION).permission(Permission.ACCESS_COARSE_LOCATION).interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            return;
                        }
                        mHandler.sendEmptyMessageDelayed(1, 200);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, BIND_AUTO_CREATE);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if (info != null) {
                        try {
                            showDialog();
                            if (popWindow.isShowing()) {
                                tv_content.setText("Bluetooth connecting...");
                                tv_title.setText("LOADING");
                                loading_progress.setVisibility(View.INVISIBLE);
                            }
                            connectBle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    Toaster.showLong(msg.obj.toString());
                    break;
                case 3:
                    if(loading_progress!=null&&popWindow.isShowing()){
                        loading_progress.setVisibility(View.VISIBLE);
                        loading_progress.setMax(bleDeviceUtil.getMaxCharacteristicCount());
                        loading_progress.setProgress(currentProgress);
                    }
                    break;
            }
        }

    };


    private int currentProgress=0;
    private void connectBle() {
        new Thread() {
            @Override
            public void run() {
                try {
                    bleDeviceUtil = bleService.connectBle(info.getAddress());
                    if (bleDeviceUtil == null) {
                        Message message = new Message();
                        message.what=2;
                        message.obj="Device connect error!";
                        mHandler.sendMessage(message);
                    } else {
                        Map<String, ServicesPropertiesDomain> serviceDataDtoMap = bleDeviceUtil.getServiceDataDtoMap();
                        int maxCharacteristicCount = bleDeviceUtil.getMaxCharacteristicCount();
                        bleDeviceUtil.setMtu(100);
                        Collection<ServicesPropertiesDomain> values = serviceDataDtoMap.values();
                        for (ServicesPropertiesDomain servicesPropertiesDomain : values) {
                            Map<String, CharacteristicDomain> characterMap = servicesPropertiesDomain.getCharacterMap();
                            Collection<CharacteristicDomain> chValues = characterMap.values();
                            String serviceUUID = servicesPropertiesDomain.getUuid();
                            for (CharacteristicDomain characteristicDomain : chValues) {
                                String chUUID = characteristicDomain.getUuid();
                                Map<String, DescriptorDomain> descMap = characteristicDomain.getDescMap();
                                Collection<DescriptorDomain> descValues = descMap.values();
                                for (DescriptorDomain descriptorDomain : descValues) {
                                    bleDeviceUtil.readDescriptor(serviceUUID, chUUID, descriptorDomain.getUuid());
                                }
                                bleDeviceUtil.readCharacteristic(serviceUUID, chUUID);
                                currentProgress++;
                                mHandler.sendEmptyMessage(3);
                            }
                        }
                        domainMap = bleDeviceUtil.getServiceDataDtoMap();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                }
            }
        }.start();
    }


    private TextView tv_content;
    private TextView tv_title;
    private ProgressBar loading_progress;
    private PopupWindow popWindow;

    private void showDialog() {
        View view = LayoutInflater.from(BleDetailActivity.this).inflate(R.layout.dialog_show_progress, null);
        popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popWindow.setAnimationStyle(android.R.style.Animation_Dialog);//设置动画
        popWindow.setFocusable(false); // 设置不允许在外点击消失
        popWindow.setOutsideTouchable(false);
        // 设置背景，这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        //软键盘不会挡着popupwindow
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //设置SelectPicPopupWindow弹出窗体的背景
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText("Loading...");
        tv_content = view.findViewById(R.id.tv_content);
        tv_content.setText("Loading...");
        loading_progress = view.findViewById(R.id.loading_progress);

        //监听菜单的关闭事件
        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        //设置菜单显示的位置
        popWindow.showAtLocation(iv_device.getRootView(), Gravity.CENTER, 0, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void subscriber(EventBusMsg msg) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bleDeviceUtil!=null){
            bleDeviceUtil.destroy();
        }
        bleDeviceUtil=null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
