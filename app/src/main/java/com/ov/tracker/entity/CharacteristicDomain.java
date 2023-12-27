package com.ov.tracker.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Map;

public class CharacteristicDomain implements Serializable {

    private String uuid;
    private String name;
    private Integer properties;
    private String desc;
    private byte[] values;
    private Object realVal;
    private int valType;
    private Map<String,DescriptorDomain> descMap;

    private boolean enableWrite=false;

    private boolean enableRead=false;

    private boolean enableIndicate=false;

    private boolean enableNotify=false;

    private boolean enableWriteNoResp=false;

    public boolean isEnableWrite() {
        return enableWrite;
    }

    public void setEnableWrite(boolean enableWrite) {
        this.enableWrite = enableWrite;
    }

    public boolean isEnableRead() {
        return enableRead;
    }

    public void setEnableRead(boolean enableRead) {
        this.enableRead = enableRead;
    }

    public boolean isEnableIndicate() {
        return enableIndicate;
    }

    public void setEnableIndicate(boolean enableIndicate) {
        this.enableIndicate = enableIndicate;
    }

    public boolean isEnableNotify() {
        return enableNotify;
    }

    public void setEnableNotify(boolean enableNotify) {
        this.enableNotify = enableNotify;
    }

    public boolean isEnableWriteNoResp() {
        return enableWriteNoResp;
    }

    public void setEnableWriteNoResp(boolean enableWriteNoResp) {
        this.enableWriteNoResp = enableWriteNoResp;
    }

    public Map<String, DescriptorDomain> getDescMap() {
        return descMap;
    }

    public void setDescMap(Map<String, DescriptorDomain> descMap) {
        this.descMap = descMap;
    }

    public int getValType() {
        return valType;
    }

    public void setValType(int valType) {
        this.valType = valType;
    }

    public Object getRealVal() {
        return realVal;
    }

    public void setRealVal(Object realVal) {
        this.realVal = realVal;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public byte[] getValues() {
        return values;
    }

    public void setValues(byte[] values) {
        this.values = values;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProperties() {
        return properties;
    }

    public void setProperties(Integer properties) {
        this.properties = properties;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
