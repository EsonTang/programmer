//package com.prize.push.android.listeners;
//
//import com.prize.app.util.JLog;
//import com.prize.push.client.IClient;
//import com.prize.push.client.MqttConfig;
//import com.prize.push.client.exceptions.SubscriptionException;
//import com.prize.push.mqtt.packets.Packet;
//import com.prize.push.mqtt.packets.PacketFactory;
//
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//
//import java.nio.ByteBuffer;
//
///**
// * @创建者 longbaoxiu
// * @创建者 2017/9/29.17:51
// * @描述
// */
//
//public class PrizeMqttCallback implements MqttCallbackExtended {
//
//    private IClient client;
//
//    public PrizeMqttCallback(IClient client) {
//        this.client = client;
//    }
//    @Override
//    public void connectComplete(boolean reconnect, String serverURI) {
//        if (JLog.isDebug) {
//            JLog.i("AlarmPingSender", "PrizeMqttCallback-connectComplete-serverURI:" + serverURI+"--reconnect="+reconnect);
//        }
//        MqttConfig config = this.client.getConfig();
//        String[] topicSets = new String[]{"/set", "/set/" + config.getAccessId(), "/set/" + config.getAccessId() + "/" + config.getClientId(), "/self/" + config.getAccessId(), "/self/" + config.getAlias(), "/self/" + config.getAccessId() + "/" + config.getClientId(), "/ack/" + config.getClientId()};
//
//        try {
//            this.client.subscribe(topicSets);
//            this.client.extralSubscription();
//        } catch (SubscriptionException var6) {
//            var6.printStackTrace();
//            if (JLog.isDebug) {
//                JLog.i("AlarmPingSender", "PrizeMqttCallback-connectComplete-SubscriptionException:" + var6.getMessage());
//            }
//        }
//    }
//
//    @Override
//    public void connectionLost(Throwable cause) {
//        if (JLog.isDebug) {
//            JLog.i("AlarmPingSender", "PrizeMqttCallback-connectionLost:" + cause);
//        }
//    }
//
//    @Override
//    public void messageArrived(String topic, MqttMessage message) throws Exception {
//        ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
//        Packet packet = PacketFactory.getPacket(buffer);
//        packet.setTopic(topic);
//        this.client.getPacketReader().putPacket(packet);
//    }
//
//    @Override
//    public void deliveryComplete(IMqttDeliveryToken token) {
//
//    }
//}
