package com.ov.tracker.callback;

import com.ov.tracker.entity.EventBusMsg;

public interface EventCallBack {

    void eventBusListener(EventBusMsg msg);

}
