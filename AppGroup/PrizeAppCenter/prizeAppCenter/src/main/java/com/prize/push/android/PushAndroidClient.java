//package com.prize.push.android;
//
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.google.gson.Gson;
//import com.prize.app.BaseApplication;
//import com.prize.app.beans.ClientInfo;
//import com.prize.app.util.CommonUtils;
//import com.prize.app.util.JLog;
//import com.prize.app.util.PreferencesUtils;
//import com.prize.app.util.safe.XXTEAUtil;
//import com.prize.push.android.listeners.PrizeMqttCallback;
//import com.prize.push.android.listeners.TextPacketListner;
//import com.prize.push.client.AbstractPushClient;
//import com.prize.push.client.IClient;
//import com.prize.push.client.ISender;
//import com.prize.push.client.MqttConfig;
//import com.prize.push.client.exceptions.AuthorizatorFailException;
//import com.prize.push.client.exceptions.ConnectFailException;
//import com.prize.push.client.exceptions.SubscriptionException;
//import com.prize.push.client.packets.PacketFilter;
//import com.prize.push.client.packets.PacketReader;
//import com.prize.push.client.packets.PacketWriter;
//import com.prize.push.client.packets.listeners.AckPacketListener;
//import com.prize.push.client.packets.listeners.HttpPacketListener;
//import com.prize.push.client.packets.listeners.SubPacketListener;
//import com.prize.push.mqtt.packets.Packet;
//import com.prize.push.mqtt.packets.StatPacket;
//
//import org.eclipse.paho.android.service.MqttAndroidClient;
//import org.eclipse.paho.client.mqttv3.IMqttActionListener;
//import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
//import org.eclipse.paho.client.mqttv3.MqttSecurityException;
//
//import java.util.UUID;
//
//public class PushAndroidClient extends AbstractPushClient implements IClient {
//    private String TAG = "PushAndroidClient";
////	private Context context;
//
//    private MqttConfig config;
//
//    private MqttConnectOptions connOpts;
//    private String clientId;
//    private MqttAndroidClient mqttClient;
//    /**
//     * 消息包写入器
//     */
//    private PacketWriter packetWriter;
//    /**
//     * 消息包读取器
//     */
//    private PacketReader packetReader;
//
//
//    private boolean isSetConfig = false;
////    private boolean isExtralSubscription = false;
//
//    private PushAndroidClient() {
//        init();
//    }
//
//    @Override
//    public String getClientId() {
//        UUID uuid = UUID.randomUUID();
//        return uuid.toString();
//    }
//
//    private void init() {
//        this.packetReader = new PacketReader(this);
//        this.packetWriter = new PacketWriter(this);
//        this.startup();
//    }
//
//    public void setConfig(MqttConfig config) {
//        this.config = config;
//    }
//
//    private void startup() {
//
//        this.packetReader.startup();
//        this.packetWriter.startup();
//    }
//
//    @Override
//    public boolean isConnected() {
//        return (mqttClient != null && mqttClient.isConnected());
//    }
//
//    @Override
//    public void config() {
//        if (isSetConfig) return;
//        JLog.i("AlarmPingSender", "config()--");
//        if (config.getClientId() == null || "".equals(config.getClientId())) {
//            config.setClientId(getClientId());
//        }
//        isSetConfig = true;
//        mqttClient = new MqttAndroidClient(BaseApplication.curContext, config.getBroker(), config.getClientId());
//        mqttClient.setCallback(new PrizeMqttCallback(this));
//        connOpts = new MqttConnectOptions();
////        connOpts.setAutomaticReconnect(false);
//        connOpts.setAutomaticReconnect(config.isAutoReconnect());
//        // 连接前清空会话信息, false表示保留回话信息，在用户离线的情况下保留要发送给此客户端的消息
//        connOpts.setCleanSession(false);
////        connOpts.setWill();
//        connOpts.setConnectionTimeout(config.getConnectTimeout());
//        connOpts.setKeepAliveInterval(config.getKeepAlive());
//
//	    /* 配置回发Ack消息监听器 */
//        this.getPacketListeners().clear();
//        PacketFilter ackFilter = new PacketFilter() {
//            @Override
//            public boolean accept(Packet packet) {
//                // TODO Auto-generated method stub
//                return packet.isAck() && (packet.getPacketType() == Packet.TEXT || packet.getPacketType() == Packet.FILE);
//            }
//        };
//        addPacketListener(new AckPacketListener(this), ackFilter);
//        /* 配置Http消息监听器 */
//        PacketFilter httpFilter = new PacketFilter() {
//            @Override
//            public boolean accept(Packet packet) {
//                // TODO Auto-generated method stub
//                return packet.getPacketType() == Packet.HTTP;
//            }
//        };
//        addPacketListener(new HttpPacketListener(this), httpFilter);
//        /* 分段消息处理监听器 */
//        PacketFilter subFilter = new PacketFilter() {
//            @Override
//            public boolean accept(Packet packet) {
//                // TODO Auto-generated method stub
//                return packet.isSub() && packet.getPacketType() == Packet.SUB;
//            }
//        };
//        addPacketListener(new SubPacketListener(this), subFilter);
//        PacketFilter textFilter = new PacketFilter() {
//            @Override
//            public boolean accept(Packet packet) {
//                return packet.getPacketType() == Packet.TEXT && packet.getTopic().startsWith("/self");
//            }
//        };
////        removePacketListener(TextPacketListner.getInstance());
//        addPacketListener(TextPacketListner.getInstance(), textFilter);
////		this.addPacketListener(new TextPacketListner(), textFilter);
//
//    }
//
//    public void setConfigKeepAliveT(int sec) {
//      if(config !=null&&connOpts!=null){
//          config.setKeepAlive(sec);
//          connOpts.setKeepAliveInterval(config.getKeepAlive());
//      }
//    }
//
//    @Override
//    public void register() throws AuthorizatorFailException, ConnectFailException, SubscriptionException {
//        // 用户认证
//        connOpts.setUserName(config.getAccessId());
//        connOpts.setPassword(config.getAccessKey().toCharArray());
//        try {
//            mqttClient.connect(connOpts, null, new IMqttActionListener() {
//
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    // 发送统计信息
//                    JLog.i(TAG, "连接onSuccess");
//                    StatPacket info = new StatPacket();
//                    info.setPlatform(config.getPlatform());
//                    info.setType("info");
//                    info.setClientInfo(config.getClientInfo());
//                    info.setTopic("/stat/" + config.getAccessId());
//                    sendPacket(info);//extralSubscription();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    JLog.i(TAG, "链接onFailure-" + exception.getMessage());
//                }
//            });
//        } catch (MqttSecurityException e) {
//            // 认证授权异常
//            e.printStackTrace();
//            JLog.i(TAG, "register-MqttSecurityException-" + e.getMessage());
//            throw new AuthorizatorFailException();
//        } catch (MqttException e) {
//            //
//            e.printStackTrace();
//            JLog.i(TAG, "register-MqttException2-" + e.getMessage());
//            throw new ConnectFailException();
//        }
//
//
//    }
//
//    @Override
//    public PacketReader getPacketReader() {
//        return packetReader;
//    }
//
//    @Override
//    public void sendPacket(Packet packet) {
//        int size = config.getBufferSize();
//        if (packet.toByteArray().length > size) {  // 分段消息发送
//            Packet[] packets = Packet.subsection(packet, size);  // 将消息转换成分段消息
//            for (int i = 0; i < packets.length; i++) {
//                Packet subPacket = packets[i];
//                packetWriter.sendPacket(subPacket);
//            }
//        } else {
//            packetWriter.sendPacket(packet);
//        }
//    }
//
//    @Override
//    public void shutdown() {
//        try {
//            this.mqttClient.disconnect();
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public MqttConfig getConfig() {
//        return this.config;
//    }
//
//    @Override
//    public void subscribe(String[] topics) throws SubscriptionException {
//        try {
//            int[] qoses = new int[topics.length];
//            for (int i =0 ; i < qoses.length; i++) {
//                qoses[i] = config.getQos();
//            }
//            mqttClient.subscribe(topics, qoses);
//        } catch (MqttException e) {
//            // 订阅异常
//            e.printStackTrace();
//            throw new SubscriptionException();
//        }
//    }
//
//    @Override
//    public void subscribe(String s) throws SubscriptionException {
//        JLog.i(TAG, "subscribe--" + s);
//        try {
//            mqttClient.subscribe(s, config.getQos());
//        } catch (MqttException e) {
//            // 订阅异常
//            e.printStackTrace();
//            throw new SubscriptionException();
//        }
//    }
//
//    /**
//     * 重新连接上服务器后调用
//     */
//    @Override
//    public void extralSubscription() {
//        JLog.i("AlarmPingSender", "extralSubscription-isConnected():" + isConnected());
//        String[] topics = new String[]{
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "channel", ClientInfo.getInstance().channel),
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "brand", ClientInfo.getInstance().brand),
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "model", ClientInfo.getInstance().model),
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "systemVersion", ClientInfo.getInstance().systemVersion),
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "androidVersion", ClientInfo.getInstance().androidVersion),
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "appVersionCode", ClientInfo.getInstance().appVersionCode),
//                String.format("/%s/tags/%s/%s", config.getAccessId(), "operator", ClientInfo.getInstance().operator),
//
//        };
//        int[] qoses = new int[topics.length];
//        for(int i = 0 ; i < qoses.length; i++) {
//            qoses[i] = config.getQos();
//        }
//        try {
//            mqttClient.subscribe(topics, qoses);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public ISender getSender() {
//        return new ISender() {
//            @Override
//            public void publish(String topic, byte[] payload, int qos, boolean retained)
//                    throws MqttPersistenceException, MqttException {
//                mqttClient.publish(topic, payload, qos, retained);
//            }
//        };
//    }
//
//    @Override
//    public void bindTags(String... tags) {
//        // TODO Auto-generated method stub
//        String[] topics = new String[tags.length];
//
//        for (int i = 0; i < tags.length; i++) {
//            String topic = topics[i] = String.format("/%s/tags/%s", config.getAccessId(), tags[i]);
//            try {
//                //mqttClient.s
//                mqttClient.subscribe(topic, config.getQos());
//            } catch (MqttException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void bindTags(String type, String tag) {
//        // TODO Auto-generated method stub
//        String topic = String.format("/%s/tags/%s/%s", config.getAccessId(), type, tag);
//        try {
//            mqttClient.subscribe(topic, config.getQos());
//        } catch (MqttException e) {
//            JLog.i(TAG, "bindTags" + e);
//
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void unbindTags(String... tags) {
//        // TODO Auto-generated method stub
//        String[] topics = new String[tags.length];
//
//        for (int i = 0; i < tags.length; i++) {
//            String topic = topics[i] = String.format("/%s/tags/%s", config.getAccessId(), tags[i]);
//            try {
//                mqttClient.unsubscribe(topic);
//            } catch (MqttException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public void setClientId(String clientId) {
//        // TODO Auto-generated method stub
//        config.setClientId(clientId);
//    }
//
//    /**
//     * 注册
//     */
//    public void registerSelfPush() {
//        JLog.i(TAG, "isConnected()=" + isConnected());
//        if (isConnected()) {
//            return;
//        }
//        String tids = PreferencesUtils.getKEY_TID();
//        if (TextUtils.isEmpty(tids)) return;
//        MqttConfig config = new MqttConfig();
//        ClientInfo mClientInfo = ClientInfo.getInstance();
//        if (TextUtils.isEmpty(mClientInfo.userId)) {
//            mClientInfo.setUserId(CommonUtils.queryUserId());
//        }
//        if (TextUtils.isEmpty(mClientInfo.tid)) {
//            mClientInfo.tid = tids;
//        }
//        mClientInfo.setClientStartTime(System.currentTimeMillis());
//        mClientInfo.setNetStatus(ClientInfo.networkType);
//        String headParams = new Gson().toJson(mClientInfo);
//
//        headParams = XXTEAUtil.getParamsEncypt(headParams);
//
//        config.setBroker("tcp://123.206.59.60:1883");
////        config.setBroker("tcp://192.168.1.148:1883");
////        config.setBroker("tcp://192.168.1.187:1883");//黄必庆电脑
////        config.setBroker("tcp://push.szprize.cn:1883");
//        if (!TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
//            config.setClientId(PreferencesUtils.getKEY_TID());
//        }
//        config.setAlias(mClientInfo.imei); // tid
//        config.setPlatform("ANDROID");
//        config.setClientInfo(headParams);
//        config.setAccessId("prize");
//        config.setAccessKey("x2dyhbbb7");
//        //秒为单位，心跳300s
//        config.setKeepAlive(5*60);
//        setConfig(config);
//        config();
//        try {
//            register();
//        } catch (AuthorizatorFailException e) {
//            e.printStackTrace();
//            Log.i("SelfPushManager", "AuthorizatorFailException:" + e.getMessage());
//        } catch (ConnectFailException e) {
//            e.printStackTrace();
//            Log.i("SelfPushManager", "ConnectFailException:" + e.getMessage());
//        } catch (SubscriptionException e) {
//            e.printStackTrace();
//            Log.i("SelfPushManager", "SubscriptionException:" + e.getMessage());
//        }
//    }
//
//    private volatile static PushAndroidClient client;
//
//    /**
//     * Returns singleton class instance
//     */
//    public static PushAndroidClient getInstance() {
//        if (client == null) {
//            synchronized (PushAndroidClient.class) {
//                if (client == null) {
//                    client = new PushAndroidClient();
//                }
//            }
//        }
//        return client;
//    }
//
//}
