package com.android.lpserver.job;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.lpserver.BuildConfig;
import com.android.lpserver.Config;
import com.android.lpserver.IStatusBarNotification;
import com.android.lpserver.QHBApplication;
import com.android.lpserver.QiangHongBaoService;
import com.android.lpserver.R;
import com.android.lpserver.db.HBHelper;
import com.android.lpserver.util.AccessibilityHelper;
import com.android.lpserver.util.NotifyHelper;
import com.android.lpserver.util.ResolutionUtils;
import com.android.lpserver.util.WechatVersionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.lpserver.util.AccessibilityHelper.findNodeInfosByText;
import static com.android.lpserver.util.AccessibilityHelper.findNodeInfosByTexts;

public class WechatAccessbilityJob extends BaseAccessbilityJob {
    private static final String TAG = "WechatAccessbilityJob";

    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    public static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
    public static final String LAUNCHER_PACKAGENAME = "com.android.launcher3";
    public static final String LAUNCHER_ACTIVITY_NAME = "com.android.launcher3.Launcher";

    private static final String HONGBAO_TEXT_KEY = "[微信红包]";
    private static final String QQHONGBAO_TEXT_KEY = "[QQ红包]";
    private static final String GET_HONGBAO = "领取红包";
    private static final String OPEN_HONGBAO = "拆红包";
    private static final String WECHAT_OPEN_EN = "Open";
    private static final String WECHAT_OPENED_EN = "You've opened";
    private final static String QQ_DEFAULT_CLICK_OPEN = "点击拆开";
    private final static String QQ_HONG_BAO_PASSWORD = "口令红包";
    private final static String QQ_CLICK_TO_PASTE_PASSWORD = "点击输入口令";
    private final static String QQ_SEND_BUTTON = "发送";
    private static final String BUTTON_CLASS_NAME = "android.widget.Button";
    private static final String SPLIT_KEY_WORD = ":";
    private static final String MONEY_KEY_WORD = ".";
    private static final String LOOK_LUCKS = "看看大家的手气";
    private static final String KEY_PACKET_HAS_OPEN = "口令红包已拆开";
    private static final String QQ_HAS_OPEN = "已拆开";
    private static final String QQ_IS_LATE = "来晚一步，红包被领完了";
    private static final String LUCKY_MONEY_PACKAGENAME_BEGINNING = "com.tencent.mm.plugin.luckymoney.ui";
    private static final String HONGBAO_DETAIL_PACKAGENAME = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private static final String KING_GLORY = "com.tencent.tmgp.sgame.SGameActivity";

    /** The minimum version number that cannot be used to match the text */
    private static final int USE_ID_MIN_VERSION = 700;  // 6.3.8 corresponds to 680,6.3.9 corresponding to code code is 700
    private static final int WINDOW_NONE = 0;
    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
    private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
    private static final int WINDOW_LAUNCHER = 3;
    private static final int WINDOW_OTHER = -1;
    private int mCurrentWindow = WINDOW_NONE;
    private boolean isReceivingHongbao;
    private PackageInfo mWechatPackageInfo = null;
    private Handler mHandler = null;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update installation package information
            updatePackageInfo();
        }
    };
    @Column(name = "sender")
    private String sender;
    @Column(name = "money")
    private String money;
    @Column(name = "time")
    private String time;
    private long preTime;       //Click on the chat interface of the red time
    private long afterTime;     //The time after the end of the red envelope
    private double asumeTime;     //Time consumed by a single grab
    private int count = 0;      //The number of grab red
    private List<Double> timeList  = new ArrayList<>();      //Store the collection of time spent each time to grab a red envelope
    private double aveTime;         //Average time to grab a red envelope
    private List<Double> sumList;
    private double sum = 0;
    BroadcastReceiver isClearReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isClear = intent.getBooleanExtra("isClear",false);
            if(isClear){
                Log.d(TAG,"Clear data");
                sum = 0.00;
                aveTime = 0.0;
                count = 0;
                totaltime = 0.0;
                timeList.clear();
            }
        }
    };
    private PowerManager.WakeLock wakeLock;
    private boolean lock;
    private boolean beforeIsLock;
    private boolean isFirstEnterDetail;
    private AccessibilityNodeInfo rootNodeInfo;
    private List<AccessibilityNodeInfo> mReceiveNode;
    private boolean mLuckyMoneyReceived;
    private String lastFetchedHongbaoId = null;
    private long lastFetchedTime = 0;
    private static final int MAX_CACHE_TOLERANCE = 5000;
    private SQLiteDatabase readableDatabase;
    public static final String action = "hb.broadcast.action";
    private double totaltime;
    private boolean isClickedQQHb;
    private boolean clickedKai;
    private boolean isHasMsgText;
    private boolean isQQHbCome;
    private boolean flags;
    private  boolean canFindText = false;
    // prize add v8.0 20170905 start
    private static final String LUCKY_MONRY = "luckymoney";
    private static final String DETAIL = "Detail";
    /*用于在红包接收界面判断是不是自动抢的，当返回桌面后置为false，下次手动点击进红包接收界面时就不会自动点击了*/
    private boolean mIsAutoEnterReceiveUi = false;
    private boolean mDoScreenOff;
    // prize add v8.0 20170905 end

    // prize add for bug 44603 20171205 start
    private MediaPlayer mMediaPlayer;
    // prize add for bug 44603 20171205 end

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.d(TAG,"lock screen after 10 seconds");
                    screenOffBroadcast();
                    break;
            }
            /*super.handleMessage(msg);*/
        }
    };

    BroadcastReceiver receiverMsg = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // prize add if-judgement by zhaojian 20171102 start
            Log.d(TAG,"receiverMsg...onReceive");
            if(!mDoScreenOff){
                mDoScreenOff = false;
                handler.removeMessages(1);   //remove timer
                beforeIsLock = false;
                flags = false;
                Log.d(TAG,"set the flag as false when screen off");
            }
            // prize add if-judgement by zhaojian 20171102 end
        }
    };
    private AccessibilityNodeInfo nodeInfoById;
    private List<AccessibilityNodeInfo> list;

    @Override
    public void onCreateJob(QiangHongBaoService service) {
        super.onCreateJob(service);
        Log.d(TAG,"onCreateJob");
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK  | PowerManager.ACQUIRE_CAUSES_WAKEUP,"WechatAccessbilityJob" );

        updatePackageInfo();

        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        getContext().registerReceiver(broadcastReceiver, filter);

        HBHelper hbHelper = new HBHelper(getContext(),"RED_BAG.db",null,3);
        readableDatabase = hbHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query("red_bag2", new String[]{"success_amount","ave_time","sum","total_time"}, null, null, null, null, null);
        while (cursor.moveToNext()){
            count = Integer.parseInt(cursor.getString(cursor.getColumnIndex("success_amount")));
            aveTime = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ave_time")));
            sum = Double.parseDouble(cursor.getString(cursor.getColumnIndex("sum")));
            totaltime = Double.parseDouble(cursor.getString(cursor.getColumnIndex("total_time")));
        }
        cursor.close();

        getContext().registerReceiver(isClearReceiver,new IntentFilter(action));

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        getContext().registerReceiver(receiverMsg,intentFilter);
    }

    @Override
    public void onStopJob() {
        Log.d(TAG,"onStopJob");
        try {
            getContext().unregisterReceiver(broadcastReceiver);
            getContext().unregisterReceiver(isClearReceiver);
            getContext().unregisterReceiver(receiverMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // prize modify for bug 44603 by zhaojian 20171205 start
        if(mMediaPlayer != null){
            mMediaPlayer.release();
        }
        // prize modify for bug 44603 by zhaojian 20171205 end
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(IStatusBarNotification sbn) {
        Notification nf = sbn.getNotification();
        String text = String.valueOf(sbn.getNotification().tickerText);
        notificationEvent(text, nf);
    }

    @Override
    public boolean isEnable() {
        return getConfig().isEnableWechat();                //Red assistant switch value
    }

    @Override
    public String getTargetPackageName() {
        SharedPreferences packNameSp = getContext().getSharedPreferences("PACK_NAME",Context.MODE_PRIVATE);
        String packName = packNameSp.getString("pack_name", WECHAT_PACKAGENAME);
        if(WECHAT_PACKAGENAME.equals(packName)){
            return WECHAT_PACKAGENAME;
        }/*else if(QQ_PACKAGENAME.equals(packName)) {
            return QQ_PACKAGENAME;
        }*/else {
            return LAUNCHER_PACKAGENAME;
        }
    }

    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        SharedPreferences packNameSp = getContext().getSharedPreferences("PACK_NAME",Context.MODE_PRIVATE);
        String packName = packNameSp.getString("pack_name", WECHAT_PACKAGENAME);
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {       // NotificationBar event
            Log.d(TAG,"onReceiveJob...NotificationBar event");
            Parcelable data = event.getParcelableData();
            if(data == null || !(data instanceof Notification)) {
                return;
            }
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                notificationEvent(text, (Notification) data);
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {          // Interface change event
            Log.d(TAG,"Interface change event,flags = "+flags + ",beforeIsLock = "+beforeIsLock);
            if(flags){
                Log.d(TAG,"openHongBao...activity ："+getRunningActivityName());
                if("com.tencent.mm.ui.LauncherUI".equals(getRunningActivityName())
                        || "com.android.launcher3.Launcher".equals(getRunningActivityName())){
                    if(beforeIsLock){
                        beforeIsLock = false;
                        screenOffBroadcast();
                    }
                    //fix：cancel the group packet ,then it will be save the record when crab the private packet
                    canFindText = false;
                }
                flags = false;
            }

            openHongBao(event);

//            if(QQ_PACKAGENAME.equals(packName)){
//                if(isQQHbCome){
//                    handleQQChatListHb();         // crab QQ hongbao start
//                    isQQHbCome = false;
//                }
//
//                //If black, red is demolished after the lock
//                if(beforeIsLock && isClickedQQHb){
//                    screenOffBroadcast();
//                    isClickedQQHb = false;
//                    beforeIsLock = false;
//                }
//            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ) {        // Interface content change event
            //2016.1.18 add a flag,fix:When clicked the "open" but we did not grab the red envelope,
            // then clicked the previous grabbed packet,it will record information
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                if(clickedKai){
                    AccessibilityNodeInfo rootInActiveWindow = getService().getRootInActiveWindow();
                    Log.d(TAG,"rootInActiveWindow = " + rootInActiveWindow + ",rootInActiveWindow2 = " + getService().getRootInActiveWindow());
                    if(rootInActiveWindow != null && rootInActiveWindow.getChildCount() >= 2){
                        if(rootInActiveWindow.getChild(1) != null
                                && rootInActiveWindow.getChild(1).getText() != null){
                            String delayText = rootInActiveWindow.getChild(1).getText().toString();
                            Log.d(TAG,"window content changed...delayText = "+delayText);
                            if(delayText != null){
                                if(getContext().getResources().getString(R.string.have_not_get).equals(delayText)){
                                    clickedKai = false;
                                    if(beforeIsLock){
                                        beforeIsLock = false;
                                        screenOffBroadcast();
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if(WECHAT_PACKAGENAME.equals(packName)){
                if(mCurrentWindow != WINDOW_LAUNCHER) { //Not in the chat interface or chat list, do not deal with
                    return;
                }
                if(isReceivingHongbao) {
                    handleChatListHongBao();
                }
            }
        }

        preTime = System.currentTimeMillis();
    }

    private void handleQQChatListHb(){
        //To execute QQ to grab a red envelope
        /*rootNodeInfo = event.getSource();*/
        rootNodeInfo = getService().getRootInActiveWindow();
        Log.d(TAG,"enter the qq group interface...rootNodeInfo = "+rootNodeInfo);

        if(rootNodeInfo == null){
            return;
        }
        handleQQHb();
    }

    private void handleQQHb() {
        mReceiveNode = null;
        checkNodeInfo();
        getQQHb();
    }

    private void getQQHb() {
    /* If you have received a red envelope and have not yet */
        if (mLuckyMoneyReceived && (mReceiveNode != null)) {
            int size = mReceiveNode.size();
            Log.d(TAG,"size = " + size);
            if (size > 0) {
                String id = getHongbaoText(mReceiveNode.get(size - 1));      // Get the latest red envelope on the text
                Log.d(TAG,"text="+id);
                long now = System.currentTimeMillis();
                if (shouldReturn(id, now - lastFetchedTime))
                    return;

                lastFetchedHongbaoId = id;
                lastFetchedTime = now;
                final AccessibilityNodeInfo cellNode = mReceiveNode.get(size - 1);     // add
                if (cellNode.getText().toString().equals(KEY_PACKET_HAS_OPEN) || cellNode.getText().toString().equals(QQ_HAS_OPEN)) {
                    return;
                }
                if(cellNode.getText().toString().equals(QQ_SEND_BUTTON)){
                    return;
                }

                // prize add 20171204 start
                SharedPreferences sp = getContext().getSharedPreferences("DELAY",Context.MODE_PRIVATE);
                int delay_time = sp.getInt("delay_time", 4);
                switch (delay_time){
                    case 1:
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }, 0);
                        break;
                    case 2:
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }, 500);
                        break;
                    case 3:
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }, 1000);
                        break;
                    case 4:
                        int s = (int)(Math.random()*7)+2;
                        long delay = s*1000;
                        Log.d(TAG,"random delay "+s +" 秒");
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }, delay);
                        break;
                }
                //cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                // prize add 20171204 end

                isClickedQQHb = true;
                Log.d(TAG,"clicked...cellNode = "+cellNode);

                //Password red packet
                if(cellNode.getText() != null){
                    if (cellNode.getText().toString().equals(QQ_HONG_BAO_PASSWORD)) {
                        AccessibilityNodeInfo rowNode = getService().getRootInActiveWindow();

                        if (rowNode == null) {
                            Log.d(TAG, "noteInfo is null");
                            return;
                        } else {
                            recycle(rowNode);
                        }
                    }
                }

                mLuckyMoneyReceived = false;

                //If you click on a red envelope but did not get a red envelope
                if(cellNode != null){
                    if(cellNode.getText().toString().equals(QQ_IS_LATE)){
                        if(!beforeIsLock){
                            Log.d(TAG, "cellNode");
                            return;
                        }
                    }
                }
            }
        }
    }

    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            /*Matching "click on the input password of the node, and click on this node"*/
            if(info.getText() != null && info.getText().toString().equals(QQ_CLICK_TO_PASTE_PASSWORD)) {
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                isClickedQQHb = true;
            }

            /*Matching text edit box behind the send button, and click on the send password*/
            if (info.getClassName().toString().equals(BUTTON_CLASS_NAME) && info.getText().toString().equals(QQ_SEND_BUTTON)) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                isClickedQQHb = true;
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    private void checkNodeInfo() {
        if (rootNodeInfo == null) {
            return;
        }
         /* Chat conversation window, traverse the node to match "click apart",
         "password red envelope", "click enter the password" " */
        List<AccessibilityNodeInfo> nodes1 = findAccessibilityNodeInfosByTexts(rootNodeInfo,
                new String[]{QQ_DEFAULT_CLICK_OPEN, QQ_HONG_BAO_PASSWORD, QQ_CLICK_TO_PASTE_PASSWORD, QQ_SEND_BUTTON});
        Log.d(TAG,"nodes1 = " + nodes1);
        if (!nodes1.isEmpty()) {
            getReceiveNode(nodes1);
        }else {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<AccessibilityNodeInfo> nodes2 = findAccessibilityNodeInfosByTexts(rootNodeInfo,
                            new String[]{QQ_DEFAULT_CLICK_OPEN, QQ_HONG_BAO_PASSWORD, QQ_CLICK_TO_PASTE_PASSWORD, QQ_SEND_BUTTON});
                    Log.d(TAG,"nodes2 = " + nodes2);
                    getReceiveNode(nodes2);

                    getQQHb();
                }
            },3000);
        }
    }

    private void getReceiveNode(List<AccessibilityNodeInfo> nodes1) {
        String nodeId = Integer.toHexString(System.identityHashCode(this.rootNodeInfo));
        Log.d(TAG,"nodeId = " + nodeId + ",lastFetchedHongbaoId = " + lastFetchedHongbaoId);
        if (!nodeId.equals(lastFetchedHongbaoId)) {
            mLuckyMoneyReceived = true;
            mReceiveNode = nodes1;
            Log.d(TAG,"checkNodeInfo...mReceiveNode="+mReceiveNode);
        }
    }

    private String getHongbaoText(AccessibilityNodeInfo node) {
        /* Get a red envelope on the text */
        String content;
        try {
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = i.getText().toString();
        } catch (NullPointerException npe) {
            return null;
        }
        return content;
    }

    private boolean shouldReturn(String id, long duration) {
        // ID is empty
        if (id == null) {
            return true;
        }

        // Name and cache exist difference
        if (duration < MAX_CACHE_TOLERANCE && id.equals(lastFetchedHongbaoId)) {
            return true;
        }
        return false;
    }

    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts) {
        for (String text : texts) {
            if (text == null) {
                continue;
            }

            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) {
                if (text.equals(WECHAT_OPEN_EN) && !nodeInfo.findAccessibilityNodeInfosByText(WECHAT_OPENED_EN).isEmpty()) {
                    continue;
                }
                return nodes;
            }
        }
        return new ArrayList<>();
    }


    /** Notification bar event*/
    private void notificationEvent(String ticker, Notification nf) {
        String text = ticker;
        int index = text.indexOf(SPLIT_KEY_WORD);
        if(index != -1) {
            text = text.substring(index + 1);
        }
        text = text.trim();
        Log.d(TAG,"text = "+text);
        // If you have a WeChat red keyword on the next step to deal with
        if(text.contains(HONGBAO_TEXT_KEY) /*|| text.contains(QQHONGBAO_TEXT_KEY)*/) {
            if(text.contains(HONGBAO_TEXT_KEY)){
                isReceivingHongbao = true;
            }else {
                isReceivingHongbao = false;
            }

            newHongBaoNotification(nf);

            /*To QQ when a red notice to set a true,
                    and then judge ture only click on the red envelope, and finally set to false*/
            isQQHbCome = true;
        }
    }

    /** Open the notification bar message*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newHongBaoNotification(Notification notification) {
        flags = false;
        // v8
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mIsAutoEnterReceiveUi = true;
        }

        //prize modify for bug 44603 by zhaojian 20171205 start
        new Thread(new Runnable() {
            @Override
            public void run() {
                playHintAudio();
            }
        }).start();
        //prize modify for bug 44603 by zhaojian 20171205 end

        //Open WeChat's notification bar message
        final PendingIntent pendingIntent = notification.contentIntent;
        lock = NotifyHelper.isLockScreen(getContext());
        if(!lock) {
            Log.d(TAG,"before is screen on" + ", handler.hasMessages(1) = "+handler.hasMessages(1));

            // prize add if-condition by zhaojian 20171031 start
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if(handler != null && handler.hasMessages(1)){
                    handler.removeMessages(1);
                    Message message = handler.obtainMessage(1);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        handler.sendMessageDelayed(message,11000);
                    }else {
                        handler.sendMessageDelayed(message,13000);
                    }
                }
            }
            // prize add if-condition by zhaojian 20171031 end

            Log.d(TAG,"current activity = " + getRunningActivityName());
            if(!KING_GLORY.equals(getRunningActivityName())){
                NotifyHelper.send(pendingIntent);
            }

        } else {
            Log.d(TAG,"before is screen off");
            beforeIsLock = true;
            //Bright screen
            wakeLock.acquire();
            wakeLock.release();
            //Make unlock
            Intent intent = new Intent("prize.set.keyguard.state");
            intent.putExtra("hide",true);
            intent.putExtra("secure",true);
            getContext().sendBroadcast(intent);

            Log.d(TAG,"top activity is ："+getRunningActivityName());
            //If the light screen is the main interface, the delay processing, otherwise not delay processing
             if("com.tencent.mm.ui.LauncherUI".equals(getRunningActivityName())){
                Log.d(TAG,"is wechat main interface");
                NotifyHelper.send(pendingIntent);
            }else{
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotifyHelper.send(pendingIntent);
                    }
                },300);
                Log.d(TAG,"delay 300 ms to open notification");
            }

            Message message = handler.obtainMessage(1);
            if(beforeIsLock){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    handler.sendMessageDelayed(message,11000);
                }else {
                    handler.sendMessageDelayed(message,13000);
                }
            }
        }
    }

    // prize add for bug 39063 by zhaojian 20171030 start
    private void playHintAudio() {
        AudioManager audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        int mode = audioManager.getRingerMode();
        Log.d(TAG,"mode = " + mode);

        if(mode != AudioManager.RINGER_MODE_SILENT && mode != AudioManager.RINGER_MODE_VIBRATE) {
            // prize modify for bug 44603 by zhaojian 20171205 start
            mMediaPlayer = MediaPlayer.create(getContext(),R.raw.hb_sound2);
            Log.d("debug","isNotifySound = " + getConfig().isNotifySound());
            if(getConfig().isNotifySound()) {
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                mMediaPlayer.start();
            }
            // prize modify for bug 44603 by zhaojian 20171205 end
        }
    }
    // prize add for bug 39063 by zhaojian 20171030 end

    private String getRunningActivityName(){
        ActivityManager activityManager=(ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(final AccessibilityEvent event) {
        int wechatVersion = getWechatVersion();
        String receiveMoneyActivity = WechatVersionHelper.getMoneyReceiveActivityName(wechatVersion);
        AccessibilityNodeInfo rootInActiveWindow = getService().getRootInActiveWindow();
        String currentClassName = event.getClassName().toString();
        Log.d(TAG,"wechatVersion = " + wechatVersion + ",receiveMoneyActivity = " + receiveMoneyActivity
                + ",event.getClassName() = " + event.getClassName() + "rootInActiveWindow = " + rootInActiveWindow);

        if(receiveMoneyActivity.equals(currentClassName)
                || WechatVersionHelper.MONEY_RECEIVE_ACTIVITY_NAME.equals(currentClassName)   //prize add by zj 20180105
                || (detailForSevenOrEight(currentClassName) && detailForSevenAndEight(currentClassName,rootInActiveWindow))){
            Log.d(TAG,"enter the hongbao receive interface");
            mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                if(mIsAutoEnterReceiveUi){
                    SharedPreferences sp = getContext().getSharedPreferences("DELAY",Context.MODE_PRIVATE);
                    int delay_time = sp.getInt("delay_time", 4);
                    switch (delay_time){
                        case 1:
                            delayClick(0);
                            break;
                        case 2:
                            delayClick(500);
                            break;
                        case 3:
                            delayClick(1000);
                            break;
                        case 4:
                            // v8 start
//                            int s = (int)(Math.random()*3)+2;
                            int s = (int)(Math.random()*2)+4;
                            // v8 end
                            long delay = s*1000;
                            Log.d(TAG,"random delay "+s +" second");
                            delayClick(delay);
                            break;
                    }
                    flags = true;
                }
            }else{
                if(rootInActiveWindow != null){
                    if(rootInActiveWindow.getChildCount() > 1 && rootInActiveWindow.getChild(1) != null){      // prize add getChildCount()  by zhaojian 2017830
                        String delayText = rootInActiveWindow.getChild(1).getText().toString();
                        Log.d(TAG,"delayText="+delayText);
                        if(delayText != null){
                            if(getContext().getResources().getString(R.string.have_not_get).equals(delayText)){
                                if(beforeIsLock){
                                    beforeIsLock = false;
                                    screenOffBroadcast();
                                }
                            }
                        }
                    }
                }

                //Points in the red, the next step is to remove the red
                handleLuckyMoneyReceive(rootInActiveWindow);               // prize add a param rootInActiveWindow v8.0 zhaojian 2017830
            }
            isFirstEnterDetail = true;
        } else if((HONGBAO_DETAIL_PACKAGENAME.equals(event.getClassName())
                || (event.getClassName().toString().startsWith(LUCKY_MONEY_PACKAGENAME_BEGINNING)
                && isHasDetailDescription(rootInActiveWindow))) && isFirstEnterDetail) {
            Log.d(TAG,"enter the hongbao detail interface");
            mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
//            AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if(nodeInfo == null) {
                return;
            }

            String moneyButtonId;
            wechatVersion = getWechatVersion();
            moneyButtonId = WechatVersionHelper.getMoneyTextId(wechatVersion);
            AccessibilityNodeInfo recorderNode = null;
            if(moneyButtonId != null){
                recorderNode = AccessibilityHelper.findNodeInfosById(nodeInfo,moneyButtonId);
                Log.d(TAG,"recorderNode = "+recorderNode);
            }else {
                recorderNode = findNodeInfosByText(nodeInfo,MONEY_KEY_WORD);
                Log.d(TAG,"recorderNode = "+recorderNode);
            }

            if(recorderNode != null) {
                try {
                    String moneyText = recorderNode.getText().toString();
                    Log.d(TAG,"moneyText = "+moneyText);
                    boolean isNum = moneyText.matches("^[0-9]+(.[0-9]{2})?$");
                    Log.d(TAG,"isNum = "+isNum);
                    if(isNum){
                        String moneyStr = String.valueOf(moneyText);
                        float money = Float.valueOf(moneyStr);
                        Log.d(TAG,"money = "+money);
                        if(money >= 0.01f && money <= 200.00f && canFindText){
                            isHasMsgText = true;
                            canFindText = false;
                        }
                    }
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            //private red packet
            String singleHBButtonId;
            singleHBButtonId = WechatVersionHelper.getSingleChatMessageId(wechatVersion);
            AccessibilityNodeInfo singleRecorderNode = null;
            if(singleHBButtonId != null) {
                singleRecorderNode = AccessibilityHelper.findNodeInfosById(nodeInfo,singleHBButtonId);
                Log.d(TAG,"singleRecorderNode = "+singleRecorderNode);
            }else {
                singleRecorderNode = findNodeInfosByText(nodeInfo
                        , getContext().getResources().getString(R.string.leaveing_message));
                Log.d(TAG,"singleRecorderNode = "+singleRecorderNode);
            }

            if(singleRecorderNode != null) {
                String singleLeaveMsgStr = String.valueOf(singleRecorderNode.getText());
                Log.d(TAG,"singleLeaveMsgStr="+singleLeaveMsgStr);
                if(getContext().getResources().getString(R.string.leaveing_message).equals(singleLeaveMsgStr)) {
                    if(beforeIsLock) {
                        beforeIsLock = false;
                        screenOffBroadcast();
                    }
                }
            }

            //After the demolition of a red envelope to see a detailed record of the interface
            Log.d(TAG,"clickedKai="+clickedKai + ",isHasMsgText = " + isHasMsgText);
            if(clickedKai && isHasMsgText){
                getTotalTime();
                getInfo();     //Date, number of times, average time of use
                getMoneyInfo(event);    // Single amount, total amount

                // v8.0
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    getSenderInfo();
                }
                // end

                //Enter the details of the red envelope to store a value
                SharedPreferences sp = getContext().getSharedPreferences(Config.KEY_WECHAT_AFTER_GET_HONGBAO,Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putInt("after_get_hongbao",0);
                edit.commit();

                if(getConfig().getWechatAfterGetHongBaoEvent() == Config.WX_AFTER_GET_GOHOME) {
                    insertAndUpdateSql();
                }

                //screen off after Grab the red packet
                if(beforeIsLock && clickedKai){
                    beforeIsLock = false;
                    screenOffBroadcast();
                    clickedKai = false;
                }
            }
            isFirstEnterDetail = false;
            clickedKai = false;
            isHasMsgText = false;
            canFindText = false;

            // v8 start
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                mIsAutoEnterReceiveUi = false;
            }
            // v8 end
        } else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            mCurrentWindow = WINDOW_LAUNCHER;
            Log.d(TAG,"enter the wechat interface");
            //In the chat interface, go to the point of red
            handleChatListHongBao();
        } else {
            mCurrentWindow = WINDOW_OTHER;
        }
    }

    private boolean detailForSevenAndEight(String currentClassName, AccessibilityNodeInfo rootInActiveWindow) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? (!currentClassName.contains(DETAIL))
                : (getReceiveUiMessage(rootInActiveWindow) != null);
    }

    private boolean detailForSevenOrEight(String currentClassName) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? (currentClassName.contains(LUCKY_MONRY))
                :(currentClassName.startsWith(LUCKY_MONEY_PACKAGENAME_BEGINNING));
    }

    private AccessibilityNodeInfo getReceiveUiMessage(AccessibilityNodeInfo rootInActiveWindow){
        AccessibilityNodeInfo node = null;
        if(rootInActiveWindow != null){
            node = findNodeInfosByTexts(rootInActiveWindow,
                    getContext().getResources().getString(R.string.send_one_randomly),
                    getContext().getResources().getString(R.string.send_one_to_you),
                    getContext().getResources().getString(R.string.have_not_get));
        }
        Log.d(TAG,"node = " + node);
        return node;
    }

    private boolean isHasDetailDescription(AccessibilityNodeInfo rootInActiveWindow){
        Log.d(TAG,"rootInActiveWindow = " + rootInActiveWindow);
        if(rootInActiveWindow != null){
            CharSequence detailDescriptionChar = rootInActiveWindow.getContentDescription();
            if(detailDescriptionChar != null){
                String detailDescription = detailDescriptionChar.toString();
                Log.d(TAG,"detailDescription = " + detailDescription);
                if(getContext().getResources().getString(R.string.detail_description).equals(detailDescription)
                        || detailDescription.contains(getContext().getResources().getString(R.string.red_packet_detail))){
                    return true;
                }
            }
        }
        return false;
    }

    // v8
    private void getSenderInfo(){
        AccessibilityNodeInfo rootNodeInfo = getService().getRootInActiveWindow();
        String nameId = WechatVersionHelper.getSenderNameId(getWechatVersion());
        AccessibilityNodeInfo nameNodeInfo = null;
        if(nameId != null){
            nameNodeInfo = AccessibilityHelper.findNodeInfosById(rootNodeInfo,nameId);
        }else {
            AccessibilityNodeInfo moneyFunctionNode = AccessibilityHelper.findNodeInfosByText(
                    rootNodeInfo,getContext().getResources().getString(R.string.money_function));
            Log.d(TAG,"moneyFunctionNode = " + moneyFunctionNode);
            if(moneyFunctionNode != null) {
                AccessibilityNodeInfo parentNode = moneyFunctionNode.getParent();
                if(parentNode != null){
                    Log.d(TAG, "parentNode = " + parentNode + ",childCount = " + parentNode.getChildCount());
                    nameNodeInfo = parentNode.getChild(0);
                }
            }else {
                // in case of there is no String of money_function
                nameNodeInfo = null;
            }
        }
        Log.d(TAG,"nameNodeInfo = " + nameNodeInfo);

        if(nameNodeInfo != null){
            if(nameNodeInfo.getText() != null){
                sender = nameNodeInfo.getText().toString();
            }
        }else {
            // set the sender as " " while nameNodeInfo is null
            sender = " ";
        }
        Log.d(TAG,"the sender is : "+sender);
    }

    private void getTotalTime() {
        afterTime = System.currentTimeMillis();
        asumeTime = (double)(afterTime - preTime) / 1000;
        if(asumeTime > 12.0){
            asumeTime = 10.0;
        }
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        asumeTime = Double.parseDouble(decimalFormat.format(asumeTime));
        totaltime = totaltime + asumeTime;
    }

    private void insertAndUpdateSql() {
        String sql = "insert into red_bag1 (sender,get_money,date)values(?,?,?)";
        readableDatabase.execSQL(sql,new Object[]{sender,money,time});

        String sq2 = "insert into red_bag2 (success_amount,ave_time,sum,total_time)values(?,?,?,?)";
        Cursor cursor = readableDatabase.query("red_bag2", new String[]{"success_amount", "ave_time", "sum","total_time"}, null, null, null, null, null);
        Log.d(TAG,"count="+count+",sum="+sum+",aveTme="+aveTime+",totalTime="+totaltime);
        if(cursor != null && cursor.moveToNext()){
            ContentValues value = new ContentValues();
            value.put("success_amount",count);
            value.put("ave_time",aveTime);
            value.put("sum",sum);
            value.put("total_time",totaltime);
            readableDatabase.update("red_bag2", value, null, null);
        }
        readableDatabase.execSQL(sq2,new Object[]{count,aveTime,sum,totaltime});
        if(cursor != null){
            cursor.close();
        }
    }

    private void screenOffBroadcast() {
        // prize add v8.0 by zhaojian 20171102 start
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mDoScreenOff = true;
        }
        // prize add v8.0 by zhaojian 20171102 end

        Intent intent = new Intent("prize.set.keyguard.state");
        intent.putExtra("hide",false);
        intent.putExtra("sleep",true);
        getContext().sendBroadcast(intent);
        flags = false;
        // prize add v8.0 by zhaojian 20171102 start
        beforeIsLock = false;
        // prize add v8.0 by zhaojian 20171102 end

        handler.removeMessages(1);

        // prize add v8.0 by zhaojian 2017912 start
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mIsAutoEnterReceiveUi = false;
        }
        // prize add v8.0 by zhaojian 2017912 end
        Log.d(TAG,"screen off");
    }

    private void getInfo(){
        time = getStringTime();                                     //Grab red dates
        count++;                                                    //Grab red counts
        aveTime = totaltime / count;                                //Grab red average time
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        aveTime = Double.parseDouble(decimalFormat.format(aveTime)) ;
        /*To prevent the open word is still in turn when the user manually click Cancel,
        this time aveTime is very small, uniform set to 0.5 seconds*/
        if(aveTime <= 0.5){
            aveTime = 0.5;
        }
    }

    private void getMoneyInfo(AccessibilityEvent event){
        Log.d(TAG, "getMoneyInfo");
//        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        AccessibilityNodeInfo nodeInfo = event.getSource();
        Log.d(TAG,"nodeInfo = " + nodeInfo);
        if(nodeInfo == null) {
            Log.d(TAG, "Red details rootWindow is empty");
            return;
        }

        String moneyButtonId;
        int wechatVersion = getWechatVersion();
        moneyButtonId = WechatVersionHelper.getMoneyTextId(wechatVersion);
        Log.d(TAG,"moneyButtonId = " + moneyButtonId);
        AccessibilityNodeInfo recorderNode = null;
        if(moneyButtonId != null) {
            recorderNode = AccessibilityHelper.findNodeInfosById(nodeInfo,moneyButtonId);
            Log.d(TAG,"recorderNode = " + recorderNode);
        }else {
            recorderNode = findNodeInfosByText(nodeInfo,MONEY_KEY_WORD);
            Log.d(TAG,"recorderNode = " + recorderNode);
        }

        sumList = new ArrayList<>();
        if(recorderNode != null) {
            money = recorderNode.getText().toString();
            boolean isNum = money.matches("^[0-9]+(.[0-9]{2})?$");
            Log.d(TAG,"isNum = "+isNum);
            double moneyDouble = 0;
            if(isNum){
                moneyDouble = Double.parseDouble(money);
            }
            sumList.add(moneyDouble);
            for (double money:sumList) {
                sum = sum + money;                           //Total amount
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            sum = Double.parseDouble(decimalFormat.format(sum)) ;
        }
    }

    /**
     * Click to chat in the red envelope, the display interface
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLuckyMoneyReceive(AccessibilityNodeInfo nodeInfo) {

        if(nodeInfo == null) {
            Log.d(TAG, "rootWindow is empty");
            return;
        }

        AccessibilityNodeInfo targetNode = null;
        int event = getConfig().getWechatAfterOpenHongBaoEvent();
        int wechatVersion = getWechatVersion();
        if(event == Config.WX_AFTER_OPEN_HONGBAO) { //Open a red envelope
            if (wechatVersion < USE_ID_MIN_VERSION) {
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo,OPEN_HONGBAO);
            } else {
                String buttonId = WechatVersionHelper.getOpenButtonId(wechatVersion);
                Log.d(TAG,"buttonId = " + buttonId);
                if(buttonId != null) {
                    targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, buttonId);
                }

                if(targetNode == null){
                    //Corresponding to a fixed amount of red envelopes to fight.
                    AccessibilityNodeInfo textNode = findNodeInfosByTexts(nodeInfo,
                            getContext().getResources().getString(R.string.send_one),
                            getContext().getResources().getString(R.string.send_one_to_you),
                            getContext().getResources().getString(R.string.send_one_randomly));
                    Log.d(TAG,"textNode = " + textNode);

                    if(textNode != null) {
                        for (int i = 0; i < textNode.getChildCount(); i++) {
                            AccessibilityNodeInfo node = textNode.getChild(i);
                            if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
                                targetNode = node;
                                break;
                            }
                        }
                        Log.d(TAG,"targetNode = " + targetNode);
                    }
                }

                if(targetNode == null) { //Search through component
                    targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
                }
                Log.d(TAG,"targetNode = " + targetNode);
            }
        } else if(event == Config.WX_AFTER_OPEN_SEE) { //Have a look
            if(getWechatVersion() < USE_ID_MIN_VERSION) { //Lower version have the function to have a look
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo,LOOK_LUCKS);
            }
        } else if(event == Config.WX_AFTER_OPEN_NONE) {         // Quietly watching
            return;
        }

        if(targetNode != null) {
            final AccessibilityNodeInfo n = targetNode;
            SharedPreferences sp = getContext().getSharedPreferences("DELAY",Context.MODE_PRIVATE);
            int delay_time = sp.getInt("delay_time", 4);
            switch (delay_time){
                case 1:
                    delayClick(n,0);
                    break;
                case 2:
                    delayClick(n,500);
                    break;
                case 3:
                    delayClick(n,1000);
                    break;
                case 4:
                    int s = (int)(Math.random()*7)+2;
                    long delay = s*1000;
                    Log.d(TAG,"random delay "+s +" 秒");
                    delayClick(n,delay);
                    break;
            }

            if(event == Config.WX_AFTER_OPEN_HONGBAO) {
                QHBApplication.eventStatistics(getContext(), "open_hongbao");
            } else {
                QHBApplication.eventStatistics(getContext(), "open_see");
            }

            //Get sender information
            getSenderInfo(targetNode);

            flags = true;
        }
    }

    private void delayClick(final AccessibilityNodeInfo n, long sDelayTime) {
        if(sDelayTime != 0) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AccessibilityHelper.performClick(n);
                }
            }, sDelayTime);
        } else {
            AccessibilityHelper.performClick(n);
        }

        clickedKai = true;
    }

    // v8
    private void delayClick(long sDelayTime) {
        if(sDelayTime != 0) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(getRunningActivityName().equals(
                            WechatVersionHelper.getMoneyReceiveActivityName(getWechatVersion()))
                            || getRunningActivityName().equals(WechatVersionHelper.MONEY_RECEIVE_ACTIVITY_NAME)){
                        // prize add by zj for android 7.1 not smooth when sliding the desktop start
                        //simulationClick();
                        int inputSource = getSource(InputDevice.SOURCE_UNKNOWN, InputDevice.SOURCE_TOUCHSCREEN);
                        //prize add for 1080p adaptation 20170202 start
                        int[] pixelArray = ResolutionUtils.getResolutionParams(getContext());
                        sendSwipe(inputSource,pixelArray[0],pixelArray[1],pixelArray[2],pixelArray[3],20);
                        //prize add for 1080p adaptation 20170202 end
                        // prize add by zj for android 7.1 not smooth when sliding the desktop end
                    }
                }
            }, sDelayTime);
        } else {
            if(getRunningActivityName().equals(
                    WechatVersionHelper.getMoneyReceiveActivityName(getWechatVersion()))
                    || getRunningActivityName().contains(LUCKY_MONEY_PACKAGENAME_BEGINNING)){
                // prize add by zj for android 7.1 not smooth when sliding the desktop start
                //simulationClick();
                int inputSource = getSource(InputDevice.SOURCE_UNKNOWN, InputDevice.SOURCE_TOUCHSCREEN);
                //prize add for 1080p adaptation 20170202 start
                int[] pixelArray = ResolutionUtils.getResolutionParams(getContext());
                sendSwipe(inputSource,pixelArray[0],pixelArray[1],pixelArray[2],pixelArray[3],20);
                //prize add for 1080p adaptation 20170202 end
                // prize add by zj for android 7.1 not smooth when sliding the desktop end
            }
        }

        clickedKai = true;
        mIsAutoEnterReceiveUi = false;
    }



    public void getSenderInfo(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo hongbaoNode = node.getParent();
        sender = hongbaoNode.getChild(0).getText().toString();
        Log.d(TAG,"the sender is : "+sender);
    }

    private String getStringTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());

        return format;
    }

    /**
     * Receive a red envelope in the chat
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao() {
        Log.d(TAG,"handleChatListHongBao");
        int mode = getConfig().getWechatMode();
        if(mode == Config.WX_MODE_3) {
            Log.d(TAG,"Only notification mode");
            return;
        }

        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        Log.d(TAG,"nodeInfo = " + nodeInfo);
        if(nodeInfo == null) {
            Log.d(TAG, "rootWindow is empty");
            return;
        }

        // Here is the code to grab the red envelope
        list = nodeInfo.findAccessibilityNodeInfosByText(GET_HONGBAO);

        if(list != null && list.isEmpty()) {
            Log.d(TAG,"Can not get Receive the red envelope key node");
            AccessibilityNodeInfo node = findNodeInfosByText(nodeInfo,HONGBAO_TEXT_KEY);

            if(node != null) {
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "WeChat red:" + node);
                }
                isReceivingHongbao = true;
                AccessibilityHelper.performClick(nodeInfo);
            }
        } else if(list != null) {
            Log.d(TAG,"Can get Receive the red envelope key node, isReceivingHongbao="+ isReceivingHongbao);
            if (isReceivingHongbao){
                String nameButtonId;
                int wechatVersion = getWechatVersion();
                nameButtonId = WechatVersionHelper.getChatNameId(wechatVersion);
                Log.d(TAG,"nameButtonId = " + nameButtonId);
                if(nameButtonId != null){
                    nodeInfoById = AccessibilityHelper.findNodeInfosById(nodeInfo, nameButtonId);
                    Log.d(TAG,"nodeInfoById1 = " + nodeInfoById);
                }else{
                    AccessibilityNodeInfo node = findNodeInfosByText(nodeInfo,
                            getContext().getResources().getString(R.string.back));
                    Log.d(TAG,"node = " + node);
                    if(node != null){
                        node = node.getParent();
                        if(node != null){
                            node = node.getParent();
                            if(node != null){
                                nodeInfoById = node.getChild(1);
                                Log.d(TAG,"nodeInfoById1 = " + nodeInfoById);
                            }
                        }
                    }
                }

                if(nodeInfoById != null) {
                    singleChatSwitch(list);
                }else {
                    //如果获取不到名字，宁愿私聊开关失效，也不能不抢群聊红包
                    AccessibilityNodeInfo node = list.get(list.size() - 1);
                    AccessibilityHelper.performClick(node);
                    isReceivingHongbao = false;
                }
            }
        }
    }

    private void singleChatSwitch(List<AccessibilityNodeInfo> list) {
        Log.d(TAG,"singleChatSwitch");
        String nameStr = String.valueOf(nodeInfoById.getText());
        Log.d(TAG,"nameStr = " + nameStr);
        //prize modify 2017918 zhaojian start:sometimes the WeChat name maybe show as xxx(0) when first install WeChat and get a packet
        //String regEx1 = ".*(\\([1-9]\\d*\\))";
        String regEx1 = ".*(\\([0-9]\\d*\\))";
        //prize modify  2017918 zhaojian end
        Pattern pattern = Pattern.compile(regEx1);
        Matcher matcher = pattern.matcher(nameStr);
        boolean result = matcher.find();
        Log.d(TAG,"result = " + result);

        //true represent group chat，false represent private chat
        if(result){
            canFindText = true;
            //The latest red collar
            AccessibilityNodeInfo node = list.get(list.size() - 1);
            AccessibilityHelper.performClick(node);
            isReceivingHongbao = false;
            Log.d(TAG,"clicked the newest packet");
        }else {
            //if it is private chat,we should check whether the private chat switch is on or off
            isReceivingHongbao = false;      //fix：it will open packet automatically when enter the chat window
            Log.d(TAG,"Config.isOpenSingleChat() = "+Config.isOpenSingleChat());
            if(Config.isOpenSingleChat()){
                AccessibilityNodeInfo node = list.get(list.size() - 1);
                AccessibilityHelper.performClick(node);
                return;
            }

            if(beforeIsLock){
                beforeIsLock = false;
                screenOffBroadcast();
            }
        }
    }

    private Handler getHandler() {
        if(mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /** Get the WeChat version*/
    private int getWechatVersion() {
        if(mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /** Update WeChat package information*/
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getContext().getPackageManager().getPackageInfo(WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();          // delete the error log,it necessary show
        }
    }


    // v8.0
    @TargetApi(Build.VERSION_CODES.N)
    private void simulationClick(){
        Path path = new Path();
        //path.moveTo(360, 726);
        DisplayMetrics mDisplayMetrics = getContext().getResources().getDisplayMetrics();
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        Log.d(TAG,"width = " + width + ",height = " + height);
        if(width >= 710 && width <= 730 && height >= 1180 && height <= 1290){  // it need subtract height of navigationbar(84px)
            path.moveTo(360,726);
        }else if(width >= 1070 && width <= 1090 && height >= 1700 && height <= 1930){    // 1080P
            path.moveTo(540,1123);
        }else if(width >= 710 && width <= 730 && height >= 1340 && height <= 1450){
            path.moveTo(360,860);
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 450, 50)).build();
        dispatchTouch(gestureDescription);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void dispatchTouch(GestureDescription gestureDescription){
        getService().dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "onCompleted");
                super.onCompleted(gestureDescription);
                //if the internet too slow, it maybe cannot get the record when clicked "kai"
                mIsAutoEnterReceiveUi = false;
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*mIsAutoEnterReceiveUi = false;*/

                        if(beforeIsLock){
                            beforeIsLock = false;
                            screenOffBroadcast();
                        }
                    }
                },2000);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "onCancelled");
                super.onCancelled(gestureDescription);

                dispatchTouch(gestureDescription);
            }
        }, null);
    }

    private void sendSwipe(int inputSource, float x1, float y1, float x2, float y2, int duration) {
        if (duration < 0) {
            duration = 300;
        }
        long now = SystemClock.uptimeMillis();
        injectMotionEvent(inputSource, MotionEvent.ACTION_DOWN, now, x1, y1, 1.0f);
        long startTime = now;
        long endTime = startTime + duration;
        while (now < endTime) {
            long elapsedTime = now - startTime;
            float alpha = (float) elapsedTime / duration;
            injectMotionEvent(inputSource, MotionEvent.ACTION_MOVE, now, lerp(x1, x2, alpha),
                    lerp(y1, y2, alpha), 1.0f);
            now = SystemClock.uptimeMillis();
        }
        injectMotionEvent(inputSource, MotionEvent.ACTION_UP, now, x2, y2, 0.0f);
    }

    private void injectMotionEvent(int inputSource, int action, long when, float x, float y, float pressure) {
        final float DEFAULT_SIZE = 1.0f;
        final int DEFAULT_META_STATE = 0;
        final float DEFAULT_PRECISION_X = 1.0f;
        final float DEFAULT_PRECISION_Y = 1.0f;
        final int DEFAULT_EDGE_FLAGS = 0;
        MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, DEFAULT_SIZE,
                DEFAULT_META_STATE, DEFAULT_PRECISION_X, DEFAULT_PRECISION_Y,
                getInputDeviceId(inputSource), DEFAULT_EDGE_FLAGS);
        event.setSource(inputSource);
        Log.i(TAG, "injectMotionEvent: " + event);
        /*InputManager.getInstance().injectInputEvent(event,
                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);*/
        invokeInjectInputEvent(event, 2);
    }

    private void invokeInjectInputEvent(MotionEvent event, int mode) {
        Class cl = InputManager.class;
        try {
            Method method = cl.getMethod("getInstance");
            Object result = method.invoke(cl);
            InputManager im = (InputManager) result;
            method = cl.getMethod("injectInputEvent", InputEvent.class, int.class);
            method.invoke(im, event, mode);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }  catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private int getInputDeviceId(int inputSource) {
        final int DEFAULT_DEVICE_ID = 0;
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            InputDevice inputDev = InputDevice.getDevice(devId);
            //if (inputDev.supportsSource(inputSource)) {
            if (call_supportsSource(inputDev, inputSource)) {
                return devId;
            }
        }
        return DEFAULT_DEVICE_ID;
    }

    private boolean call_supportsSource(InputDevice inputDev, int inputSource ){
        try {
            Method method = Class.forName("android.view.InputDevice").getMethod("supportsSource", int.class);
            return (Boolean) method.invoke(inputDev, inputSource);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final float lerp(float a, float b, float alpha) {
        return (b - a) * alpha + a;
    }

    private static final int getSource(int inputSource, int defaultSource) {
        return inputSource == InputDevice.SOURCE_UNKNOWN ? defaultSource : inputSource;
    }
}
