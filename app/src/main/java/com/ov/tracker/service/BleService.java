package com.ov.tracker.service;

import static com.ov.tracker.enums.EventBusTagEnum.BLE_INIT_ERROR;
import static com.ov.tracker.enums.EventBusTagEnum.NOT_ENABLE_LE;
import static com.ov.tracker.enums.EventBusTagEnum.NOT_SUPPORT_LE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;


import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.tracker.MainActivity;
import com.ov.tracker.R;
import com.ov.tracker.entity.BleDeviceInfo;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.entity.MqttRevMessage;
import com.ov.tracker.enums.EventBusTagEnum;
import com.ov.tracker.utils.BleDeviceUtil;
import com.ov.tracker.utils.LogUtil;
import com.ov.tracker.utils.MqttClientManager;
import com.ov.tracker.utils.permission.PermissionInterceptor;
import com.ov.tracker.utils.permission.PermissionNameConvert;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BleService extends Service implements MqttCallback, LocationListener {

    private Map<String, BluetoothDevice> bleDeviceMap = new ConcurrentHashMap<>();

    private Map<String, BleDeviceInfo> bleDeviceInfoMap = new ConcurrentHashMap<>();

    private BluetoothAdapter bluetoothAdapter;
    //low power ble
    private BluetoothLeScanner bluetoothLeScanner;

    private MqttClientManager instance;

    private LocationManager locationManager = null;


    @Override
    public void onCreate() {
        super.onCreate();
        initBleConfig();
        instance = MqttClientManager.getInstance(this);
        instance.createConnect("tcp://mqtt-2.omnivoltaic.com:1883", null, null);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, BleService.this);
    }

    public void initBleConfig() {
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                EventBus.getDefault().post(new EventBusMsg(NOT_SUPPORT_LE, null));
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                EventBus.getDefault().post(new EventBusMsg(NOT_ENABLE_LE, null));
                return;
            }
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new EventBusMsg(BLE_INIT_ERROR, e.getMessage()));
        }
    }


    public BleDeviceUtil connectBle(String mac) throws Exception {
        BluetoothDevice bleDevice = bleDeviceMap.get(mac);
        if (bleDevice != null) {
            BleDeviceUtil bleDeviceUtil = new BleDeviceUtil(bleDevice, BleService.this);
            boolean b = bleDeviceUtil.connectGatt();
            if (b) {
                return bleDeviceUtil;
            }
            bleDeviceUtil.destroy();
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public void startBleScan() {
        bleDeviceMap.clear();
        bleDeviceInfoMap.clear();
        EventBus.getDefault().post(new EventBusMsg(EventBusTagEnum.BLE_FIND, new ArrayList<>(bleDeviceInfoMap.values())));
        if (bluetoothLeScanner == null) {
            initBleConfig();
        }
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(scanCallback);
        }
    }


    public ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
//            Log.e("scanBle->onScanStarted", device.getAddress() + "," + device.getName());
            String bleName = device.getName();
            if (!TextUtils.isEmpty(bleName)) {
                bleName = bleName.trim();
                if (!bleName.startsWith("OV")) {
                    return;
                }
                String typeStr = "Unknown";
                if (device.getType() == 1) {
                    typeStr = "Classic";
                } else if (device.getType() == 2) {
                    typeStr = "Low Energy";
                } else if (device.getType() == 3) {
                    typeStr = "DUAL";
                }
                BleDeviceInfo checkRecord = new BleDeviceInfo();
                checkRecord.setAddress(device.getAddress());
                checkRecord.setFullName(bleName);
                String[] nameArr = bleName.split(" ");
                if (nameArr.length >= 3) {
                    checkRecord.setProductName(nameArr[1]);
                    checkRecord.setProductId(nameArr[2]);
                } else if (nameArr.length == 2) {
                    checkRecord.setProductName(nameArr[1]);
                    checkRecord.setProductId("");
                }

                checkRecord.setRssi(result.getRssi());
                bleDeviceMap.put(device.getAddress(), device);
                if (bleDeviceInfoMap.containsKey(device.getAddress())) {
//                    LogUtil.info("==========find device/update device info" + JSON.toJSONString(checkRecord) + "==========");
                } else {
//                    LogUtil.info("==========find new device" + JSON.toJSONString(checkRecord) + "==========");
                }
                bleDeviceInfoMap.put(device.getAddress(), checkRecord);
                EventBus.getDefault().post(new EventBusMsg(EventBusTagEnum.BLE_FIND, new ArrayList<>(bleDeviceInfoMap.values())));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            LogUtil.debug("ScanCallback==>onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.debug("ScanCallback==>onScanFailed");
        }
    };

    @SuppressLint("MissingPermission")
    public void stopScan() {
        try {
            bluetoothLeScanner.stopScan(scanCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BleServiceBinder();
    }


    @Override
    public void connectionLost(Throwable cause) {
        LogUtil.debug("connectionLost:" + cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LogUtil.debug("messageArrived:topic>" + topic + ";MqttRevMessage>" + new String(message.getPayload()));
        try {
            EventBusMsg<MqttRevMessage> busMsg = new EventBusMsg<>();
            MqttRevMessage mqttRevMessage = new MqttRevMessage(topic, new String(message.getPayload()));
            busMsg.setTagEnum(EventBusTagEnum.MQTT_DATA_REV);
            busMsg.setT(mqttRevMessage);
            EventBus.getDefault().post(busMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            LogUtil.debug("deliveryComplete MqttRevMessage>"+new Gson().toJson(token.getMessage()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public MqttClientManager getInstance() {
        return instance;
    }

    /**
     * 时间、地点（GPS坐标）、agentID, tracerPhoneID、以及此时手机能看到全部BLE Scanner看到的设备列表。
     *
     */
    // 位置改变
    // 在设备的位置改变时被调用
    @Override
    public void onLocationChanged(@NonNull Location location) {
        try{
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Iterator<BleDeviceInfo> iterator = bleDeviceInfoMap.values().iterator();
            List<String>bleList=new ArrayList<>();
            while(iterator.hasNext()){
                BleDeviceInfo next = iterator.next();
                String fullName = next.getFullName();
                bleList.add(fullName);
            }
            Map<String,Object>map=new HashMap<>();
            map.put("latitude",latitude);
            map.put("longitude",longitude);
            map.put("timestamp",System.currentTimeMillis());
            map.put("bleList",bleList);

            if(instance!=null){
                String s = new Gson().toJson(map);
                LogUtil.debug("location===>"+s);
                instance.publish("/dt/ov/location/",0,s.getBytes(StandardCharsets.US_ASCII));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //在用户禁用具有定位功能的硬件时被调用
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    // 位置服务可用
    // 在用户启动具有定位功能的硬件是被调用
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    //在提供定位功能的硬件状态改变是被调用
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
//        locationManager.isProviderEnabled(provider);判断提供者是否可用
    }



    public class BleServiceBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }
}
