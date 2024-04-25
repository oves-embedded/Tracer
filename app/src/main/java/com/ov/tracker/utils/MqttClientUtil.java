package com.ov.tracker.utils;

import android.text.TextUtils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MqttClientUtil {
    private String address = "mqtt-factory.omnivoltaic.com:18883";

    String username;
    String password;
    private MqttConnectOptions options;

    private MqttClient client;

    private MqttCallback callback;

    public MqttClientUtil(String ip, String port, String username, String password, MqttCallback callback) {
        this.address = "tcp://" + ip + ":" + port;
        this.callback = callback;
        this.username = username;
        this.password = password;
    }


    public boolean isConnected() {
        return client != null ? client.isConnected() : false;
    }

    /**
     * release resource
     */
    public void release() {
        try {
            if (client != null) {
                disConnect();
                client = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消连接
     *
     * @throws MqttException
     */
    public void disConnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新连接
     */
    public void reconnect() {
        if (client != null && !client.isConnected()) {
            try {
                client.setCallback(callback);
                client.connect(options);
//                if (client.isConnected()) {
//                    LogUtil.debug("reconnect() topic:"+topics);
//                    client.subscribe(topics);
//                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean createConnect() {
        if (client != null && client.isConnected()) {
            LogUtil.debug("===========client connected==============");
            return true;
        } else {
            release();
        }
        boolean flag = false;
        try {
            options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setAutomaticReconnect(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(10);
            if (!TextUtils.isEmpty(password)) {
                options.setPassword(password.toCharArray());
            }
            if (!TextUtils.isEmpty(username)) {
                options.setUserName(username);
            }
            //MqttDefaultFilePersistence||MemoryPersistence
            client = new MqttClient(address, UUID.randomUUID().toString().replace("-", "").trim(), new MemoryPersistence());
            client.setCallback(callback);
            flag = doConnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private boolean doConnect() {
        if (client != null) {
            try {
                IMqttToken iMqttToken = client.connectWithResult(options);
                iMqttToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        LogUtil.debug("mqtt client [doConnect] success!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        LogUtil.debug("mqtt client [doConnect] fail:" + exception.getMessage());
                    }
                });
                iMqttToken.waitForCompletion();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean publish(String topicName, int qos, byte[] payload) {
        try {
            if (client == null) {
                LogUtil.debug("mqtt client is null");
                return false;
            }
            if (!client.isConnected()) {
                LogUtil.debug("mqtt client is not connected");
                this.reconnect();
            }
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            message.setId(getMsgId());
            client.publish(topicName, message);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            LogUtil.debug("publish msg error" + e.getMessage());
            return false;
        }
    }


    public boolean subscribe(String topicName, int qos) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topicName, qos);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public boolean unSubscribe(String topicName) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            try {
                client.unsubscribe(topicName);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public boolean unSubscribe(String[] topicName) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            try {
                client.unsubscribe(topicName);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public boolean subscribe(String[] topicName, int qos[]) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topicName, qos);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    private volatile AtomicInteger seqNum = new AtomicInteger(0);


    public synchronized int getMsgId() {
        int i = seqNum.addAndGet(1);
        if (i >= Integer.MAX_VALUE) {
            seqNum.set(0);
        }
        return i;
    }


}
