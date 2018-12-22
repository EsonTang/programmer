//package com.prize.push.android.listeners;
//
//import android.content.Intent;
//
//import com.prize.app.BaseApplication;
//import com.prize.app.util.JLog;
//import com.prize.appcenter.service.PrizeAppCenterService;
//import com.prize.push.client.packets.listeners.PacketListener;
//import com.prize.push.mqtt.packets.Packet;
//import com.prize.push.mqtt.packets.TextPacket;
//
///**
// * 收到push的消息
// */
//public class TextPacketListner implements PacketListener {
//    @Override
//    public void processPacket(Packet packet) {
//        // TODO Auto-generated method stub
//        TextPacket textPacket = (TextPacket) packet;
//        String handler = textPacket.getHandler();
////        String text = textPacket.getText();
//        if (handler.equals("com.prize.appcenter")) {
//            if (JLog.isDebug) {
//                JLog.i("PushAndroidClient", textPacket.getText());
//            }
//            Intent intent = new Intent(
//                    "com.prize.appcenter.service.PrizeAppCenterService");
//            intent.setClassName(BaseApplication.curContext,
//                    "com.prize.appcenter.service.PrizeAppCenterService");
//            intent.putExtra("content", textPacket.getText());
//            intent.putExtra(PrizeAppCenterService.OPT_TYPE, 7);
//            BaseApplication.curContext.startService(intent);
//        } else {
//
//        }
//
//    }
//
//    private volatile static TextPacketListner client;
//
//    public static TextPacketListner getInstance() {
//        if (client == null) {
//            synchronized (TextPacketListner.class) {
//                if (client == null) {
//                    client = new TextPacketListner();
//                }
//            }
//        }
//        return client;
//    }
//
//    private TextPacketListner() {
//    }
//}
