package com.ov.tracker.service;

import static com.ov.tracker.enums.EventBusTagEnum.BLE_INIT_ERROR;
import static com.ov.tracker.enums.EventBusTagEnum.NOT_ENABLE_LE;
import static com.ov.tracker.enums.EventBusTagEnum.NOT_SUPPORT_LE;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;


import com.ov.tracker.entity.BleDeviceInfo;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.enums.EventBusTagEnum;
import com.ov.tracker.utils.BleDeviceUtil;
import com.ov.tracker.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BleService extends Service {

    private Map<String, BluetoothDevice> bleDeviceMap = new ConcurrentHashMap<>();

    private Map<String, BleDeviceInfo> bleDeviceInfoMap = new ConcurrentHashMap<>();

    private BluetoothAdapter bluetoothAdapter;
    //low power ble
    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    public void onCreate() {
        super.onCreate();
        initBleConfig();
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

    public class BleServiceBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }
}
