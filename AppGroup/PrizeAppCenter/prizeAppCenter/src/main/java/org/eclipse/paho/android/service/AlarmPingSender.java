///*******************************************************************************
// * Copyright (c) 2014 IBM Corp.
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * and Eclipse Distribution License v1.0 which accompany this distribution.
// *
// * The Eclipse Public License is available at
// *    http://www.eclipse.org/legal/epl-v10.html
// * and the Eclipse Distribution License is available at
// *   http://www.eclipse.org/org/documents/edl-v10.php.
// */
//package org.eclipse.paho.android.service;
//
//import android.annotation.SuppressLint;
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Build;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.text.format.Time;
//import android.util.Log;
//
//import com.prize.app.beans.ClientInfo;
//import com.prize.app.util.JLog;
//import com.prize.push.android.PushAndroidClient;
//
//import org.eclipse.paho.client.mqttv3.IMqttActionListener;
//import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttPingSender;
//import org.eclipse.paho.client.mqttv3.internal.ClientComms;
//
///**
// * Default ping sender implementation on Android. It is based on AlarmManager.
// * <p>
// * <p>This class implements the {@link MqttPingSender} pinger interface
// * allowing applications to send ping packet to server every keep alive interval.
// * </p>
// *
// * @see MqttPingSender
// */
//class AlarmPingSender implements MqttPingSender {
//    // Identifier for Intents, log messages, etc..
//    private static final String TAG = "AlarmPingSender";
//
//    // TODO: Add log.
//    private ClientComms comms;
//    private MqttService service;
//    private BroadcastReceiver alarmReceiver;
//    //    private BroadcastReceiver netstateReceiver;
//    private AlarmPingSender that;
//    private PendingIntent pendingIntent;
//    private volatile boolean hasStarted = false;
//
//    public AlarmPingSender(MqttService service) {
//        if (JLog.isDebug) {
//            JLog.i(TAG, "AlarmPingSender构造");
//        }
//        if (service == null) {
//            throw new IllegalArgumentException(
//                    "Neither service nor client can be null.");
//        }
//        this.service = service;
//        that = this;
//    }
//
//    @Override
//    public void init(ClientComms comms) {
//        this.comms = comms;
//        this.alarmReceiver = new AlarmReceiver();
////        this.netstateReceiver = new NetStateReceiver();
//        if (JLog.isDebug) {
//            JLog.i(TAG, "AlarmPingSender-init-=" + alarmReceiver);
//        }
//    }
//
//    @Override
//    public void start() {
//        String action = MqttServiceConstants.PING_SENDER
//                + comms.getClient().getClientId();
//        Log.d(TAG, "Register alarmreceiver to MqttService" + action);
//        service.registerReceiver(alarmReceiver, new IntentFilter(action));
////        service.registerReceiver(netstateReceiver, new IntentFilter(
////                ConnectivityManager.CONNECTIVITY_ACTION));
//
//        pendingIntent = PendingIntent.getBroadcast(service, 0, new Intent(
//                action), PendingIntent.FLAG_UPDATE_CURRENT);
//
//        schedule(comms.getKeepAlive());
//        hasStarted = true;
//    }
//
//    @Override
//    public void stop() {
//
//        Log.d(TAG, "Unregister alarmreceiver to MqttService" + comms.getClient().getClientId());
//        if (hasStarted) {
//            if (pendingIntent != null) {
//                // Cancel Alarm.
//                AlarmManager alarmManager = (AlarmManager) service.getSystemService(Service.ALARM_SERVICE);
//                alarmManager.cancel(pendingIntent);
//            }
//
//            hasStarted = false;
//            try {
//                service.unregisterReceiver(alarmReceiver);
////                service.unregisterReceiver(netstateReceiver);
//            } catch (IllegalArgumentException e) {
//                //Ignore unregister errors.
//            }
//        }
//    }
//
//    @Override
//    public void schedule(long delayInMilliseconds) {
////        long nextAlarmInMilliseconds = System.currentTimeMillis()
////                + delayInMilliseconds;
////        if (delayInMilliseconds < 1000) {
////            nextAlarmInMilliseconds = System.currentTimeMillis()
////                    + delayInMilliseconds;
////        }
//        AlarmManager alarmManager = (AlarmManager) service
//                .getSystemService(Service.ALARM_SERVICE);
//        int wakeType = isCurrentInTimeScope(21, 0, 7, 0) ? AlarmManager.RTC : AlarmManager.RTC_WAKEUP;//晚上（21:00-次日7:00）不唤醒
//        if (JLog.isDebug) {
//            JLog.i(TAG, "AlarmPingSender-schedule(4)--wakeType=+" + wakeType + "--comms.getKeepAlive()=" + comms.getKeepAlive());
//        }
//        if (wakeType == AlarmManager.RTC_WAKEUP && ClientInfo.getAPNType(service) == ClientInfo.WIFI) {//白天
//            PushAndroidClient.getInstance().setConfigKeepAliveT(60 * 60);
//        } else {
//            PushAndroidClient.getInstance().setConfigKeepAliveT(5 * 60);
//        }
//        long nextAlarmInMilliseconds = System.currentTimeMillis() + comms.getKeepAlive();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // In SDK 23 and above, dosing will prevent setExact, setExactAndAllowWhileIdle will force
//            // the device to run this task whilst dosing.
//            alarmManager.setExactAndAllowWhileIdle(wakeType, nextAlarmInMilliseconds,
//                    pendingIntent);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(wakeType, nextAlarmInMilliseconds,
//                    pendingIntent);
//        } else {
//            alarmManager.set(wakeType, nextAlarmInMilliseconds,
//                    pendingIntent);
//        }
//    }
//
//    /*
//     * This class sends PingReq packet to MQTT broker
//     */
//    class AlarmReceiver extends BroadcastReceiver {
//        private WakeLock wakelock;
//        //        private Handler handler;
//        private final String wakeLockTag = MqttServiceConstants.PING_WAKELOCK
//                + that.comms.getClient().getClientId();
//
//        @Override
//        @SuppressLint("Wakelock")
//        public void onReceive(Context context, Intent intent) {
//            // According to the docs, "Alarm Manager holds a CPU wake lock as
//            // long as the alarm receiver's onReceive() method is executing.
//            // This guarantees that the phone will not sleep until you have
//            // finished handling the broadcast.", but this class still get
//            // a wake lock to wait for ping finished.
//
//            Log.d(TAG, "心跳开始Sending Ping at:" + System.currentTimeMillis());
//
//            PowerManager pm = (PowerManager) service
//                    .getSystemService(Service.POWER_SERVICE);
//            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
//            //PRIZE START 修复有时候长时间无响应  wakelock.acquire()变更为wakelock.acquire(2000);
//            //acquire(long timeOut):获得WakeLock timeOut时长，当超过timeOut之后系统自动释放WakeLock。
//            wakelock.acquire(2000);
//            if (JLog.isDebug) {
//                JLog.i(TAG, "AlarmPingSender-acquireWakeLock--wakelock=");
//            }
//            // Assign new callback to token to execute code after PingResq
//            // arrives. Get another wakelock even receiver already has one,
//            // release it until ping response returns.
//            IMqttToken token = comms.checkForActivity(new IMqttActionListener() {
//
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.d(TAG, "心跳成功Success. Release lock(" + wakeLockTag + "):"
//                            + System.currentTimeMillis());
//                    //Release wakelock when it is done.
//                    if (JLog.isDebug) {
//                        JLog.i(TAG, "心跳成功释放-releasewakelock=" + "--" + System.currentTimeMillis());
//                    }
//                    wakelock.release();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken,
//                                      Throwable exception) {
//                    Log.d(TAG, "心跳失败Failure. Release lock(" + wakeLockTag + "):"
//                            + System.currentTimeMillis());
//                    //Release wakelock when it is done.
//                    if (JLog.isDebug) {
//                        JLog.i(TAG, "心跳失败释放--releasewakelock=");
//                    }
//                    wakelock.release();
//                }
//            });
//            if (token == null && wakelock.isHeld()) {
//                if (JLog.isDebug) {
//                    JLog.i(TAG, "AlarmPingSender-(token == null && wakelock.isHeld(）--releasewakelock=");
//                }
//                wakelock.release();
//            }
//        }
//    }
//
////    /*
////     * This class observe netstate
////     */
////    class NetStateReceiver extends BroadcastReceiver {
////        public int netType = NONET;
////        private NetworkInfo.State currentWifi = NetworkInfo.State.UNKNOWN;
////        private NetworkInfo.State currentMobile = NetworkInfo.State.UNKNOWN;
////
////        @Override
////        @SuppressLint("Wakelock")
////        public void onReceive(Context context, Intent intent) {
////            if (JLog.isDebug) {
////                JLog.i(TAG, "AlarmPingSender-NetStateReceiver-onReceive=-that.comms.getClient().isConnected()"+that.comms.getClient().isConnected());
////            }
////            // 判断网络状态
////            ConnectivityManager manager = (ConnectivityManager) context
////                    .getSystemService(Context.CONNECTIVITY_SERVICE);
////            NetworkInfo info = manager
////                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
////
////            NetworkInfo.State wifi = (info != null) ? info.getState() : NetworkInfo.State.DISCONNECTED;
////            info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
////
////            NetworkInfo.State mobile = (info != null) ? info.getState() : NetworkInfo.State.DISCONNECTED;
////           int tempType= getAPNType(context);
////            if(netType==tempType){
////                return;
////            }else{
////                netType=tempType;
////            }
////            if ((currentWifi == wifi) && (currentMobile == mobile)) {
////                // 两者都没变化
////                return;
////            } else {
////                currentWifi = wifi;
////                currentMobile = mobile;
////            }
////            if (wifi == NetworkInfo.State.CONNECTED) {
////
////
////            } else if (mobile == NetworkInfo.State.CONNECTED) {
////
////            }
////        }
////    }
////
////    // 获取当前网络状态
////    public static int getAPNType(Context context) {
////        int netType = NONET;
////        if (null == context) {
////            return netType;
////        }
////        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext()
////                .getSystemService(Context.CONNECTIVITY_SERVICE);
////        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
////        if (networkInfo == null || (networkInfo.getState() != NetworkInfo.State.CONNECTED)) {
////            return netType;
////        }
////        int nType = networkInfo.getType();
////        if (nType == ConnectivityManager.TYPE_MOBILE) {
////            netType =ClientInfo.MOBILE;
////        } else if (nType == ConnectivityManager.TYPE_WIFI) {
////            netType = ClientInfo.WIFI;
////        } else {
////            boolean b = ConnectivityManager.isNetworkTypeValid(nType);
////            if (b) {
//////                netType = MOBILE_3G; // 联通3G就跑这里
////                netType =ClientInfo.MOBILE;// 联通3G就跑这里
////            }
////        }
////        return netType;
////    }
//
//
//    /**
//     * 判断当前系统时间是否在指定时间的范围内
//     *
//     * @param beginHour 开始小时，例如22
//     * @param beginMin  开始小时的分钟数，例如30
//     * @param endHour   结束小时，例如 8
//     * @param endMin    结束小时的分钟数，例如0
//     * @return true表示在范围内，否则false
//     */
//    private static boolean isCurrentInTimeScope(int beginHour, int beginMin, int endHour, int endMin) {
////        return false;
//
//        boolean result = false;
//        final long aDayInMillis = 1000 * 60 * 60 * 24;
//        final long currentTimeMillis = System.currentTimeMillis();
//
//        Time now = new Time();
//        now.set(currentTimeMillis);
//
//        Time startTime = new Time();
//        startTime.set(currentTimeMillis);
//        startTime.hour = beginHour;
//        startTime.minute = beginMin;
//
//        Time endTime = new Time();
//        endTime.set(currentTimeMillis);
//        endTime.hour = endHour;
//        endTime.minute = endMin;
//
//        if (!startTime.before(endTime)) {
//            // 跨天的特殊情况（比如22:00-8:00）
//            startTime.set(startTime.toMillis(true) - aDayInMillis);
//            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
//            Time startTimeInThisDay = new Time();
//            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
//            if (!now.before(startTimeInThisDay)) {
//                result = true;
//            }
//        } else {
//            // 普通情况(比如 8:00 - 14:00)
//            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
//        }
//        return result;
//    }
//}
