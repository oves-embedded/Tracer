package com.ov.tracker.entity;

import com.alibaba.fastjson.JSON;

public class DescriptorDomain {

    private String uuid;

    private String desc;



    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }




}
