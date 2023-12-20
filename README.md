The Tracer software is a tool for the operator's attendants (AGENTS) to take attendance at work and to monitor their work records in the field. Attendants must carry a work mobile phone loaded with the Tracer software. The operator must have deployed the "CLIENT MQTT Broker". This software includes the MQTT functionality of the Scanner software. Functionality adjustments are added:

1.Tracer Phone Registration Mobile phone registration. Each working mobile phone with the TRACER software installed is assigned a (tracerPhoneID) mobile phone number that can be used. This information is bound to the corresponding AGENT/STAFF user in the CLIENT account. Limit the registration of only one mobile phone to one AGENT, after changing the mobile phone, the previous binding is invalidated. (Responsible by Cloud Computing team)

2.Tracer Phone Activation After the Tracer Phone is registered, the phone issues a subscription request to the server "CLIENT MQTT Broker" (cmd/#/agentID). The operator can use "CLIENT MQTT Broker" at the back-end to find out the working time period (e.g., 8AM-18PM) of each working mobile phone with TRACER software installed, and report the time interval "TRACER INTERVAL (minutes)". ". After the setting, the working mobile phone posts two types of messages to the server "CLIENT MQTT Broker", namely, "Timed Location Tracking Event Report" and "Device Detection Report". The following are the two types of reports.

3.Timed Location Tracking Event Report. According to the current reporting frequency, automatically upload: time, location (GPS coordinates), agentID, tracerPhoneID, and a list of all the devices that the mobile phone can see with the BLE Scanner at this time.

4.Specific device detection messages. Publish two sets of messages: 1) According to the same logic of Scanner software, every time the device is detected, bind (Pair) action (device broadcast ID, for example, OVES CAMP 000001); trigger the upload of events, connect to the device, and then upload the device data according to the software "Scanner". 2) Automatically upload the "Timed Positioning Tracking Event Report".



Tracer 软件为运营商服务员（AGENTS）工作考勤及现场工作记录监控工具。服务员必须携带装载TRACER软件的工作手机。运营商必须已经部署了”CLIENT MQTT Broker“。此软件包含Scanner软件有关MQTT的功能。功能调整增加：

1.Tracer Phone Registration 手机注册。每一台安装了TRACER软件的工作手机 分配一个（tracerPhoneID）可以使用手机号。此信息在CLIENT账户中对应的AGENT/STAFF用户绑定。限定一个AGENT只能注册一部手机，更换手机后，之前的绑定失效。（云计算团队负责）

2.Tracer Phone Activation 激活。Tracer Phone 手机注册后，手机向服务器”CLIENT MQTT Broker发布订阅请求（cmd/#/agentID) 。运营商可以在后端通过”CLIENT MQTT Broker“每一台设安装了TRACER软件的工作手机的工作时间段 （如8AM-18PM), 及上报时间间隔“TRACER INTERVAL （分钟）” 。设定后，工作手机 向服务器”CLIENT MQTT Broker“ 发布“定时定位时跟踪事件汇报”及“设备检测汇报”两类报文。

3.定时定位时跟踪事件汇报报文。按目前上报频率设定，自动上传：时间、地点（GPS坐标）、agentID, tracerPhoneID、以及此时手机能看到全部BLE Scanner看到的设备列表。

4.具体设备检测报文。发布两组报文。1）按Scanner软件的同样逻辑，每次检验到设备后绑定（Pair）动作（设备广播的ID，例如OVES CAMP 000001）；触发事件上传，连接设备后，先按“Scanner”软件上传设备数据。2）自动上传“定时定位时跟踪事件汇报”。
