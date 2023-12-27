package com.ov.tracker.entity;


import com.ov.tracker.enums.EventBusTagEnum;

public class EventBusMsg<T> {
    private EventBusTagEnum tagEnum;
    private T t;

    public EventBusMsg() {
    }

    public EventBusMsg(EventBusTagEnum tagEnum, T t) {
        this.tagEnum = tagEnum;
        this.t = t;
    }

    public EventBusTagEnum getTagEnum() {
        return tagEnum;
    }

    public void setTagEnum(EventBusTagEnum tagEnum) {
        this.tagEnum = tagEnum;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
