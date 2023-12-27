package com.ov.tracker.entity;

import com.alibaba.fastjson.JSON;

import java.util.Map;

public class ServicesPropertiesDomain {

    private String uuid;

    private String serviceName;

    private String type;

    private String deviceOpid;

    private Map<String,CharacteristicDomain> characterMap;


    public Map<String, CharacteristicDomain> getCharacterMap() {
        return characterMap;
    }

    public void setCharacterMap(Map<String, CharacteristicDomain> characterMap) {
        this.characterMap = characterMap;
    }

    public String getDeviceOpid() {
        return deviceOpid;
    }

    public void setDeviceOpid(String deviceOpid) {
        this.deviceOpid = deviceOpid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
