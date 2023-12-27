package com.ov.tracker.entity;


import com.alibaba.fastjson.JSON;

public class BleDeviceInfo {

    private int rssi;
    private String address;
    private long timestampNanos;
    private String deviceType;
    private int majorDeviceClass;
    private boolean isConnected;
    private String fullName;
    private String productName;
    private String productId;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BleDeviceInfo() {
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }

    public void setTimestampNanos(long timestampNanos) {
        this.timestampNanos = timestampNanos;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getMajorDeviceClass() {
        return majorDeviceClass;
    }

    public void setMajorDeviceClass(int majorDeviceClass) {
        this.majorDeviceClass = majorDeviceClass;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
