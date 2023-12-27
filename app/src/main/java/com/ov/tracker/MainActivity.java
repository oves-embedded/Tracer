package com.ov.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.tracker.entity.BleDeviceInfo;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.enums.EventBusTagEnum;
import com.ov.tracker.service.BleService;
import com.ov.tracker.utils.LogUtil;
import com.ov.tracker.utils.permission.PermissionInterceptor;
import com.ov.tracker.utils.permission.PermissionNameConvert;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private BleService bleService;

    private BleAdapter bleAdapter;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private List<BleDeviceInfo>list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initService();
        initView();
        bleAdapter = new BleAdapter(R.layout.item_ble_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bleAdapter);
    }

    public void initView(){
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if(bleService!=null){
                    startScan();
                }
            }
        });
    }

    private void startScan() {
        XXPermissions.with(MainActivity.this)
                .permission(Permission.BLUETOOTH_SCAN)
                .permission(Permission.BLUETOOTH_CONNECT)
                .permission(Permission.BLUETOOTH_ADVERTISE)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            return;
                        }
                        if(bleService!=null){
                            bleService.stopScan();
                            bleService.startBleScan();
                        }
                        Toaster.show(String.format(getString(R.string.demo_obtain_permission_success_hint), PermissionNameConvert.getPermissionString(MainActivity.this, permissions)));
                    }
                });
    }

    private void initService() {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void subscriber(EventBusMsg msg){
        LogUtil.error(JSON.toJSONString(msg));
        if(msg.getTagEnum()== EventBusTagEnum.BLE_FIND){
            List<BleDeviceInfo> deviceInfos= (List<BleDeviceInfo>) msg.getT();
            list=deviceInfos;
            bleAdapter.setNewInstance(list);
            if(refreshLayout.isRefreshing()){
                refreshLayout.finishRefresh();
            }
        }
    }




    class BleAdapter extends BaseQuickAdapter<BleDeviceInfo, BaseViewHolder>{

        public BleAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, BleDeviceInfo info) {
            String fullName = info.getFullName();
            if(!TextUtils.isEmpty(fullName)){
                baseViewHolder.setText(R.id.tv_bleName,fullName);
            }
            String productName = info.getProductName()==null?"":info.getProductName().toLowerCase();
            productName=productName.replace("-","_");
            int drawableId = MainActivity.this.getResources().getIdentifier("icon_" + productName, "mipmap", MainActivity.this.getPackageName());
            baseViewHolder.setImageResource(R.id.iv_devIcon,drawableId);

            String address = info.getAddress();
            if(!TextUtils.isEmpty(address)){
                baseViewHolder.setText(R.id.tv_mac,address);
            }

        }
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