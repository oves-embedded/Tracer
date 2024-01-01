package com.ov.tracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.tracker.callback.EventCallBack;
import com.ov.tracker.databinding.ActivityMain2Binding;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.service.BleService;
import com.ov.tracker.utils.LogUtil;
import com.ov.tracker.utils.permission.PermissionInterceptor;
import com.ov.tracker.utils.permission.PermissionNameConvert;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMain2Binding binding;

    private BleService bleService;

    private List<EventCallBack> list =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);
        initService();

    }


    public void initService() {
        Intent intent = new Intent(MainActivity.this, BleService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleService = ((BleService.BleServiceBinder) service).getService();
                startScan();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, BIND_AUTO_CREATE);
    }


    public void startScan() {
        XXPermissions.with(MainActivity.this).permission(Permission.BLUETOOTH_SCAN).permission(Permission.BLUETOOTH_CONNECT).permission(Permission.BLUETOOTH_ADVERTISE).permission(Permission.ACCESS_FINE_LOCATION).permission(Permission.ACCESS_COARSE_LOCATION).interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
            @Override
            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                if (!allGranted) {
                    return;
                }
                if (bleService != null) {
                    bleService.stopScan();
                    bleService.startBleScan();
                }
                Toaster.show(String.format(getString(R.string.demo_obtain_permission_success_hint), PermissionNameConvert.getPermissionString(MainActivity.this, permissions)));
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void subscriber(EventBusMsg msg) {
        LogUtil.error(JSON.toJSONString(msg));

        if (list != null && !list.isEmpty()) {
            for (EventCallBack callBack : list) {
                callBack.eventBusListener(msg);
            }
        }

    }

    public void addCallBack(EventCallBack callBack) {
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
        }
        list.add(callBack);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(MainActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(MainActivity.this);
    }

}