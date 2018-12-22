package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.R;
import android.view.ViewGroup;
import android.view.View;
import java.util.ArrayList;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.StatusBarIconView;
import android.graphics.Point;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import android.annotation.DrawableRes;
import android.telephony.SubscriptionManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.os.Handler;
import android.os.Message;
import com.android.systemui.statusbar.LiuHaiSignalView2;
import android.text.TextUtils;
import android.app.StatusBarManager;
import com.android.systemui.LiuHaiBatteryMeterViewDefinedNew2;
import android.content.res.ColorStateList;

public class LiuHaiStatusBarIconController2 implements PrizeStatusBarStyleListener{

    private Context mContext;
    private PhoneStatusBar mPhoneStatusBar;
    public LiuHaiStatusBarIconController2(Context context, PhoneStatusBar bar){
        mContext = context;
        mPhoneStatusBar = bar;
    }

    /*-------------------StatusIconsListener----------------*/
    public interface StatusIconsListener{
        public void refreshStatusIcon(StatusBarIcon icon, String slot, int index, boolean visibility);
    }
    private StatusIconImpI mStatusIconImpI = new StatusIconImpI();
    public StatusIconImpI getStatusIconImpI(){
        return mStatusIconImpI;
    }

    public class StatusIconImpI implements StatusIconsListener{

        public ArrayList<LiuHaiStatusBarIconData> iconList = new ArrayList<LiuHaiStatusBarIconData>();

        public LiuHaiIconGroupPrize2 mLeftGroup;
        public LiuHaiIconGroupPrize2 mRightGroup;

        private String[] defaultSlots;//default icons
        private int[] defaultDirections;
        private int[] defaultIconWidths;
        private int mIconSpace;

        public String mVolteSlot;
        public String mNetworkSpeedSlot;

        public StatusIconImpI(){
        }

        private RightIconController mRightIconController;
        public void setRightIconController(RightIconController controller){
            mRightIconController = controller;
            if(mRightIconController != null){
                if(mRightGroup != null) mRightGroup.setLimitShow(true);
                if(mLeftGroup != null) mLeftGroup.setLimitShow(true);
                mRightIconController.setRightGroup(mRightGroup);
            }
        }

        private boolean isLimitShow = false;
        public void setLimitShow(boolean limit){
            isLimitShow = limit;
        }

        public void initValue(LiuHaiIconGroupPrize2 leftGroup, LiuHaiIconGroupPrize2 rightGroup){
            mLeftGroup = leftGroup;
            mRightGroup = rightGroup;
            mLeftGroup.setLiuHaiStatusBarIconController2(LiuHaiStatusBarIconController2.this);
            mRightGroup.setLiuHaiStatusBarIconController2(LiuHaiStatusBarIconController2.this);
        }

        public void initLiuHaiStatusBarIconData(int iconSpaceRes, int arr_direction, int arr_width, int arr_slot){
            mVolteSlot = mContext.getString(R.string.volte);
            mNetworkSpeedSlot = mContext.getString(R.string.network_speed);

            mIconSpace = mContext.getResources().getDimensionPixelSize(iconSpaceRes);
            defaultDirections = mContext.getResources().getIntArray(arr_direction);
            defaultIconWidths = mContext.getResources().getIntArray(arr_width);
            defaultSlots = mContext.getResources().getStringArray(arr_slot);
            debugLiuHai("defaultDirections.length = " + defaultDirections.length
                + ", defaultSlots.length = " + defaultSlots.length + ", mIconSpace = " + mIconSpace);
            int leftSum = 0;
            int rightSum = 0;
            for(int i = 0; i < defaultSlots.length; i++){
                String slot = defaultSlots[i];
                int direction = defaultDirections[i];
                int width = defaultIconWidths[i];
                debugLiuHai("init [" + i + "] slot = " + slot);
                debugLiuHai("init [" + i + "] direction = " + direction);
                debugLiuHai("init [" + i + "] width = " + width);
                LiuHaiStatusBarIconData data = new LiuHaiStatusBarIconData();
                data.slot = slot;
                data.direction = direction;
                data.viewWidth = width;
                if(direction == 0){
                    data.index = leftSum;
                    leftSum++;
                } else {
                    data.index = rightSum;
                    rightSum++;
                }
                if(mVolteSlot.equals(slot)){
                    data.view = createLiuHaiStatusBarView(data, null, slot);
                } else if(mNetworkSpeedSlot.equals(slot)){
                    data.view = createLiuHaiStatusBarView(data, null, slot);
                    data.speedTxt = (TextView)data.view.findViewById(R.id.liuhai2_speed_txt);
                    data.unitTxt = (TextView)data.view.findViewById(R.id.liuhai2_speed_unit_txt);
                }
                debugLiuHai("init [" + i + "] data.index = " + data.index);
                iconList.add(data);
            }
        }

        public void refreshStatusIcon(StatusBarIcon icon, String slot, int index, boolean visibility){
            if(mLeftGroup == null){
                debugLiuHai("mLeftGroup is null, return;");
                return ;
            }
            if(mRightGroup == null){
                debugLiuHai("mRightGroup is null, return;");
                return ;
            }
            if(!isContainTheSlot(slot)){
                debugLiuHai(slot + " is not in liuhai statusbar list.");
                return ;
            }
            debugLiuHai("refreshStatusIcon slot = "+ slot + ", visibility = " + visibility);

            LiuHaiStatusBarIconData data = getSlotData(slot);
            if(data != null) debugLiuHai("data.visibility = " + data.visibility);
            if(visibility){
                if(data != null){
                    if(data.view == null){
                        data.view = createLiuHaiStatusBarView(data, icon, slot);
                    } else {
                        if(icon!= null && data.view instanceof StatusBarIconView){
                            StatusBarIconView sview = (StatusBarIconView)data.view;
                            StatusBarIcon liuhai_icon = useLiuHaiIcon(slot, icon);
                            data.icon = liuhai_icon;
                            sview.set(liuhai_icon);
                        }
                    }
                    Point point = getViewPoint(data);
                    LiuHaiIconGroupPrize2 vg = data.direction == 0 ? mLeftGroup : mRightGroup;
                    if(data.view.getParent() == vg){
                        vg.removeView(data);
                    }
                    vg.addView(data, point.y);
                    data.visibility = true;
                    inverseView(data);
                    if(mRightIconController != null && isLimitShow && data.direction != 0){
                        data.view.setVisibility(View.GONE);
                        mRightIconController.addItem(data);
                    }
                }
            } else {
                if(data != null){
                    LiuHaiIconGroupPrize2 vg = data.direction == 0 ? mLeftGroup : mRightGroup;
                    if(data.visibility && data.view != null && data.view.getParent() == vg){
                        vg.removeView(data);
                        data.visibility = false;
                    }
                    //data.willShow = false;
                    if(mRightIconController != null && isLimitShow && data.direction != 0){
                        mRightIconController.deleteItem(data);
                    }
                }
            }
        }

        public boolean isContainTheSlot(String slot){
            for(int i = 0; i < defaultSlots.length; i++){
                String s = defaultSlots[i];
                if(s.equals(slot)){
                    return true;
                }
            }
            return false;
        }

        public LiuHaiStatusBarIconData getSlotData(String slot){
            for(int i = 0; i < iconList.size(); i++){
                LiuHaiStatusBarIconData data = iconList.get(i);
                if(data != null && data.slot.equals(slot)){
                    return data;
                }
            }
            return null;
        }

        public View createLiuHaiStatusBarView(LiuHaiStatusBarIconData data, StatusBarIcon icon, String slot){
            View view = null;
            if(slot.equals(mVolteSlot)){
                view = new ImageView(mContext);
            } else if(slot.equals(mNetworkSpeedSlot)){
                view = (ViewGroup) LayoutInflater.from(mContext)
                                .inflate(R.layout.statusbar_network_speed_liuhai_prize2, null);
            } else {
                StatusBarIconView iconView = new StatusBarIconView(mContext, slot, null, false);
                StatusBarIcon liuhai_icon = useLiuHaiIcon(slot, icon);
                data.icon = liuhai_icon;
                iconView.set(liuhai_icon);
                //iconView.set(icon);
                view = iconView;
            }
            
            //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            //        data.viewWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            //if(data.direction == 0){
            //    lp.setMargins(mIconSpace, 0, 0, 0);
            //} else {
            //    lp.setMargins(0, 0, mIconSpace, 0);
            //}
            //view.setLayoutParams(lp);
            view.setPadding(0, 0, mIconSpace, 0);
            debugLiuHai("create slot(" + slot + ") view ");
            return view;
        }

        //Point x = icon sum; y = added view index;
        public Point getViewPoint(LiuHaiStatusBarIconData data){
            int sum = 0;
            int count = 0;
            for(int i = 0; i < iconList.size(); i++){
                LiuHaiStatusBarIconData d = iconList.get(i);
                if(data.direction == d.direction){
                    if(d.visibility) {
                        if(d.index < data.index){
                            count++;
                        }
                        sum++;
                    }
                }
            }
            debugLiuHai(data.slot + " view Point = (" + sum + ", " + count + ")");
            return new Point(sum, count);
        }

        public StatusBarIcon useLiuHaiIcon(String slot, StatusBarIcon icon){
            if(slot == null) {
                debugLiuHai("useLiuHaiIcon slot is null, use icon");
                return icon;
            }
            int resourceId = 0;
            if("bluetooth".equals(slot)){
                int type = icon.icon.getType();
                if(Icon.TYPE_RESOURCE == type){
                    int res = icon.icon.getResId();
                    if(res == R.drawable.stat_sys_data_bluetooth){
                        resourceId = R.drawable.liuhai2_bluetooth;
                    } else if(res == R.drawable.stat_sys_data_bluetooth_connected){
                        resourceId = R.drawable.liuhai2_bluetooth_connected;
                    }
                }
            } else if("alarm_clock".equals(slot)){
                resourceId = R.drawable.liuhai2_alarm_clock;
            } else if("zen".equals(slot)){
                int type = icon.icon.getType();
                if(Icon.TYPE_RESOURCE == type){
                    int res = icon.icon.getResId();
                    if(res == R.drawable.stat_sys_zen_none){
                        resourceId = R.drawable.liuhai2_zen;
                    } else if(res == R.drawable.stat_sys_zen_important){
                        resourceId = R.drawable.liuhai2_zen_important;
                    }
                }
            } else if("volume".equals(slot)){
                resourceId = R.drawable.liuhai2_volume;
            } else if("hotspot".equals(slot)){
                resourceId = R.drawable.liuhai2_hotspot;
            } else if("location".equals(slot)){
                resourceId = R.drawable.liuhai2_location;
            } else if("headset".equals(slot)){
                int type = icon.icon.getType();
                if(Icon.TYPE_RESOURCE == type){
                    int res = icon.icon.getResId();
                    if(res == R.drawable.stat_sys_headset_with_mic){
                        resourceId = R.drawable.liuhai2_headset_with_mic;
                    } else if(res == R.drawable.stat_sys_headset_without_mic){
                        resourceId = R.drawable.liuhai2_headset_without_mic;
                    }
                }
            } else if("cast".equals(slot)){
                resourceId = R.drawable.liuhai2_cast;
            } else if("tty".equals(slot)){
                resourceId = R.drawable.liuhai2_tty;
            } else if("mute".equals(slot)){
                resourceId = R.drawable.liuhai2_mute;
            } else if("cdma_eri".equals(slot)){
                resourceId = R.drawable.liuhai2_cdma;
            } else if("speakerphone".equals(slot)){
                resourceId = R.drawable.liuhai2_speakerphone;
            } else if("sync_active".equals(slot)){
                resourceId = R.drawable.liuhai2_sync;
            }
            if(resourceId != 0 ){
                StatusBarIcon sbIcon = new StatusBarIcon(UserHandle.ALL, mContext.getPackageName(),
                        Icon.createWithResource(mContext, resourceId), 0, 0, null);
                return sbIcon;
            }
            return icon;
        }
    }
    public class LiuHaiStatusBarIconData{
        public String slot;
        public int direction;
        public int index;
        public StatusBarIcon icon;
        //public StatusBarIconView view;
        public View view;
        public boolean visibility;
        public int viewWidth;

        public TextView speedTxt;
        public TextView unitTxt;

        public boolean limitSimShow;//add for sim card show
    }

    /*-------------------RightIconController----------------*/
    private RightIconController mRightIconController = new RightIconController();
    public RightIconController getRightIconController(){
        return mRightIconController;
    }
    public class RightIconController{
        private ArrayList<LiuHaiStatusBarIconData> allList = new ArrayList<LiuHaiStatusBarIconData>();
        private ArrayList<LiuHaiStatusBarIconData> iconList = new ArrayList<LiuHaiStatusBarIconData>();
        private ArrayList<LiuHaiStatusBarIconData> willShowList = new ArrayList<LiuHaiStatusBarIconData>();

        private boolean isInAnimation = false;
        private final int ANIMATE_MSG = 123;
        private final int ANIMATE_DURTION = 3000;
        private LiuHaiStatusBarIconData animData;

        private LiuHaiIconGroupPrize2 mRightGroup;
        public void setRightGroup(LiuHaiIconGroupPrize2 group){
            mRightGroup = group;
        }

        private Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case ANIMATE_MSG:
                        debugLiuHai("handleMessage animData = " + animData);
                        if(animData != null){
                            if(iconList.size() > 1){
                                LiuHaiStatusBarIconData d = iconList.get(1);
                                if(d.index > animData.index){
                                    d.view.setVisibility(View.VISIBLE);
                                    animData.view.setVisibility(View.GONE);
                                } else {
                                    animData.view.setVisibility(View.VISIBLE);
                                    d.view.setVisibility(View.GONE);
                                    iconList.set(1, animData);
                                    debugLiuHai("change iconList second item to " + animData.slot);
                                    sortIconList();
                                }
                                if(mRightGroup != null) mRightGroup.setVisibleData(null);
                                d.limitSimShow = false;
                            }
                            allList.add(animData);
                        }
                        isInAnimation = false;
                        animData = null;
                        if(willShowList.size() != 0){
                            addItem(willShowList.remove(0));
                        }
                        break;
                }
            }
        };

        private void sortIconList(){
            if(iconList.size() <= 1){
                debugLiuHai("sortIconList iconList.size <=1, do not need sort.");
                return ;
            }
            LiuHaiStatusBarIconData firstData = iconList.get(0);
            LiuHaiStatusBarIconData secondData = iconList.get(1);
            if(firstData.index < secondData.index){
                debugLiuHai("sortIconList exchange the first and second item");
                iconList.set(0, secondData);
                iconList.set(1, firstData);
            }
        }

        public void addItem(LiuHaiStatusBarIconData data){
            if(willShowList.contains(data)){
                debugLiuHai("addItem willShowList contains " + data.slot + ", return");
                return ;
            }
            if(allList.contains(data) && !iconList.contains(data)){
                debugLiuHai("addItem data is in allList, not in iconList, don't need to show");
                return ;
            }
            int size = iconList.size();
            debugLiuHai("RightIconController addItem iconList.size = " + size + ", data.slot = " + data.slot);
            for(int i = 0 ; i < iconList.size(); i++){
                LiuHaiStatusBarIconData d = iconList.get(i);
                debugLiuHai("iconList item[" + i +"] = " + d.slot);
            }
            if(size < 2){
                data.view.setVisibility(View.VISIBLE);
                if(size == 0){
                    iconList.add(data);
                    allList.add(data);
                } else if(size == 1){
                    if(!iconList.contains(data)){
                        LiuHaiStatusBarIconData d = iconList.get(0);
                        if(d.index < data.index){
                            iconList.clear();
                            iconList.add(data);
                            iconList.add(d);

                            allList.clear();
                            allList.add(data);
                            allList.add(d);
                        } else {
                            iconList.add(data);
                            allList.add(data);
                        }
                    }
                }
            } else {
                debugLiuHai("RightIconController addItem isInAnimation = " + isInAnimation);
                if(!isInAnimation){
                    if(iconList.contains(data)){
                        data.view.setVisibility(View.VISIBLE);
                        debugLiuHai("addItem iconList contains " + data.slot + ", return 1");
                        return ;
                    }
                    isInAnimation = true;
                    animData = data;
                    LiuHaiStatusBarIconData d = iconList.get(1);
                    d.view.setVisibility(View.GONE);
                    if(d.slot.equals(LiuHaiSignalView2.SIM_SLOT)){
                        d.limitSimShow = true;
                    }

                    if(mRightGroup != null) mRightGroup.setVisibleData(animData);
                    animData.view.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(ANIMATE_MSG, ANIMATE_DURTION);
                } else {
                    if(animData != null && animData == data){
                        animData.view.setVisibility(View.VISIBLE);
                        debugLiuHai("addItem animData equals " + data.slot + ", return");
                        return ;
                    }
                    if(iconList.contains(data)){
                        LiuHaiStatusBarIconData d = iconList.get(0);
                        d.view.setVisibility(View.VISIBLE);
                        debugLiuHai("addItem iconList contains " + data.slot + ", return 2");
                        return ;
                    }
                    willShowList.add(data);
                }
            }
        }

        public void deleteItem(LiuHaiStatusBarIconData data){
            if(data == null){
                return ;
            }
            if(animData != null && animData == data){
                debugLiuHai("deleteItem data.slot = " + data.slot +", equals animData");
                mHandler.removeMessages(ANIMATE_MSG);
                if(mRightGroup != null) mRightGroup.setVisibleData(null);
                animData = null;
                isInAnimation = false;
                for(int i = 0 ; i < iconList.size(); i++){
                    LiuHaiStatusBarIconData d = iconList.get(i);
                    d.view.setVisibility(View.VISIBLE);
                    d.limitSimShow = false;
                }
                return ;
            }
            if(willShowList.contains(data)){
                debugLiuHai("deleteItem data.slot = " + data.slot+", from willShowList");
                willShowList.remove(data);
            }
            if(iconList.contains(data) || allList.contains(data)){
                data.view.setVisibility(View.GONE);
                iconList.remove(data);
                allList.remove(data);
                debugLiuHai("deleteItem data.slot = " + data.slot+", from iconList, size = " + iconList.size());

                addToIconList();
            }
        }

        public void deleteItem(String slot){
            if(slot == null){
                return ;
            }
            if(animData != null && animData.slot.equals(slot)){
                debugLiuHai("deleteItem slot = " + slot +", equals animData");
                mHandler.removeMessages(ANIMATE_MSG);
                if(mRightGroup != null) mRightGroup.setVisibleData(null);
                animData = null;
                isInAnimation = false;
                for(int i = 0 ; i < iconList.size(); i++){
                    LiuHaiStatusBarIconData d = iconList.get(i);
                    d.view.setVisibility(View.VISIBLE);
                    d.limitSimShow = false;
                }
                return ;
            }
            boolean flag = false;
            for(int i = 0 ; i < iconList.size(); i++){
                LiuHaiStatusBarIconData data = iconList.get(i);
                if(data != null && data.slot.equals(slot)){
                    allList.remove(data);
                    iconList.remove(data);
                    i--;
                    flag = true;
                    debugLiuHai("deleteItem slot = " + slot +", from iconList, size = " + iconList.size());
                }
            }
            if(flag) addToIconList();
            for(int i = 0 ; i < willShowList.size(); i++){
                LiuHaiStatusBarIconData data = willShowList.get(i);
                if(data != null && data.slot.equals(slot)){
                    willShowList.remove(data);
                    i--;
                    debugLiuHai("deleteItem slot = " + slot +", from willShowList, size = " + willShowList.size());
                }
            }
        }

        public void addToIconList(){
            if(iconList.size() == 0){
                LiuHaiStatusBarIconData maxData = getMaxData();
                if(maxData != null){
                    iconList.add(maxData);
                    maxData.view.setVisibility(View.VISIBLE);
                    debugLiuHai("addToIconList add first item : " + maxData.slot);
                    addToIconList();
                } else {
                    debugLiuHai("addToIconList allList has no item to show");
                }
            } else if(iconList.size() == 1){
                LiuHaiStatusBarIconData maxData = getMaxData();
                if(maxData != null){
                    iconList.add(maxData);
                    maxData.view.setVisibility(View.VISIBLE);
                    debugLiuHai("addToIconList add second item : " + maxData.slot);
                } else {
                    debugLiuHai("addToIconList allList two item has show");
                }
            } else {
                for(int i = 0 ; i < iconList.size(); i++){
                    LiuHaiStatusBarIconData data = iconList.get(i);
                    data.view.setVisibility(View.VISIBLE);
                }
                debugLiuHai("addToIconList no need add item to icon list");
            }
        }

        public LiuHaiStatusBarIconData getMaxData(){
            LiuHaiStatusBarIconData maxData = null;
            for(int i = 0; i < allList.size(); i++){
                LiuHaiStatusBarIconData d = allList.get(i);
                if(!iconList.contains(d)){
                    if(maxData == null){
                        maxData = d;
                    } else {
                        if(maxData.index <= d.index){
                            maxData = d;
                        }
                    }
                }
            }
            return maxData;
        }
    }

    /*-------------------ShowOnLeftListener----------------*/
    public interface ShowOnLeftListener{
        public void setWifiIcon(int wifiStrengthId, float iconScaleFactor);
        public void setWifiInOutIcon(int wifiInOutId, float iconScaleFactor);
        public void setWifiIconVisible(int visibility);

        public void setAirplaneIcon(int airplaneIconId, float iconScaleFactor);
        public void setAirplaneIconVisible(int visibility);

        public void refreshNetworkSpeed(int visibility, String text);

        //public void refreshVolteIcon(boolean visibility, int resid);
        public void showVolteIcon(int subId, int resid, int simCount);
        public void hideVolteIcon(int subId, int simCount);
        public void hideAllVolteIcon();
    }
    private ShowOnLeftListenerImpI mShowOnLeftListenerImpI = new ShowOnLeftListenerImpI();
    public ShowOnLeftListenerImpI getShowOnLeftListenerImpI(){
        return mShowOnLeftListenerImpI;
    }

    private int firstSubId = -1;
    private int secondSubId = -1;
    private int mSimId = 0;//0->no volte icon, 1->volte 1, 2->volte 2, 3->volte 1&2;
    private int mSimCount = 0;
    public class ShowOnLeftListenerImpI implements ShowOnLeftListener {
        private ViewGroup mWifiGroup;
        private ImageView mWifi;
        private ImageView mWifiInOut;
        private ImageView mAirplane;
        private boolean DEBUG = false;
        public ShowOnLeftListenerImpI(){
            
        }

        public void initValue(ViewGroup group, ImageView wifi, ImageView wifi_inout, ImageView airplane){
            debugLiuHai("ShowOnLeftListenerImpI initValue");
            mWifiGroup = group;
            mWifi = wifi;
            mWifiInOut = wifi_inout;
            mAirplane = airplane;
        }

        public void setWifiIcon(int wifiStrengthId, float iconScaleFactor){
            if(mWifi == null){
                debugLiuHai("mWifi is null");
                return ;
            }
            if(DEBUG) debugLiuHai("setWifiIcon wifiStrengthId = " + wifiStrengthId);
            setIconForView(mWifi, wifiStrengthId, iconScaleFactor);
        }

        public void setWifiInOutIcon(int wifiInOutId, float iconScaleFactor){
            if(mWifiInOut == null){
                debugLiuHai("mWifiInOut is null");
                return ;
            }
            if(DEBUG) debugLiuHai("setWifiInOutIcon wifiInOutId = " + wifiInOutId);
            setIconForView(mWifiInOut, wifiInOutId, iconScaleFactor);
        }

        public void setWifiIconVisible(int visibility){
            if(mWifiGroup == null){
                debugLiuHai("showWifiIcon mWifiGroup is null");
                return ;
            }
            if(DEBUG) debugLiuHai("setWifiIconVisible visibility = " + visibility);
            mWifiGroup.setVisibility(visibility);
        }

        public void setAirplaneIcon(int airplaneIconId, float iconScaleFactor){
            if(mAirplane == null){
                debugLiuHai("mAirplane is null");
                return ;
            }
            if(DEBUG) debugLiuHai("setAirplaneIcon airplaneIconId = " + airplaneIconId);
            setIconForView(mAirplane, airplaneIconId, iconScaleFactor);
        }

        public void setAirplaneIconVisible(int visibility){
            if(mAirplane == null){
                debugLiuHai("hideWifiIcon mAirplane is null");
                return ;
            }
            if(DEBUG) debugLiuHai("setAirplaneIconVisible visibility = " + visibility);
            mAirplane.setVisibility(visibility);
        }

        private void setIconForView(ImageView imageView, @DrawableRes int iconId, float iconScaleFactor) {
            iconId = exchangeIcon(iconId);
            if(imageView != null) imageView.setImageResource(iconId);
            /*Drawable icon = imageView.getContext().getDrawable(iconId);
            imageView.setImageDrawable(icon);
            if (iconScaleFactor == 1.f) {
                imageView.setImageDrawable(icon);
            } else {
                imageView.setImageDrawable(new LiuHaiScalingDrawableWrapper(icon, iconScaleFactor));
            }*/
        }

        public void refreshNetworkSpeed(int visibility, String text){
            debugLiuHai("refreshNetworkSpeed, visibility = " + visibility + ", text = " + text);
            String[] arr = text.split(" ");
            if(visibility == View.VISIBLE && !TextUtils.isEmpty(text) && arr.length >= 2){
                LiuHaiStatusBarIconData data = mStatusIconImpI.getSlotData(mStatusIconImpI.mNetworkSpeedSlot);
                mStatusIconImpI.refreshStatusIcon(null, mStatusIconImpI.mNetworkSpeedSlot, 0, true);
                data.speedTxt.setText(arr[0]);
                data.unitTxt.setText(arr[1]);
            } else {
                mStatusIconImpI.refreshStatusIcon(null, mStatusIconImpI.mNetworkSpeedSlot, 0, false);
            }
        }

        public void showVolteIcon(int subId, int resid, int simCount){
            subId = SubscriptionManager.getSlotId(subId) + 1;//get slot id
            int simId = addSubId(subId);
            if(mSimId == simId && mSimCount == simCount){
                return ;
            }
            mSimId = simId;
            mSimCount = simCount;
            debugLiuHai("showVolteIcon subId = " + subId + ", mSimId = " + mSimId
                    + ", firstSubId = " + firstSubId + ", secondSubId = " + secondSubId);
            refreshVolteIcon();
        }

        public void hideVolteIcon(int subId, int simCount){
            subId = SubscriptionManager.getSlotId(subId) + 1;//get slot id
            int simId = deleteSubId(subId);
            if(mSimId == simId && mSimCount == simCount){
                return ;
            }
            mSimId = simId;
            mSimCount = simCount;
            debugLiuHai("hideVolteIcon subId = " + subId + ", mSimId = " + mSimId
                    + ", firstSubId = " + firstSubId + ", secondSubId = " + secondSubId);
            refreshVolteIcon();
        }

        public void hideAllVolteIcon(){
            debugLiuHai("hideAllVolteIcon, firstSubId = " + firstSubId
                    + ", secondSubId = " + secondSubId);
            firstSubId = -1;
            secondSubId = -1;
            mSimId = 0;
            mSimCount = 0;
            refreshVolteIcon();
        }

        public void refreshVolteIcon(){
            int curSubResId = getVolteResId(mSimId);
            LiuHaiStatusBarIconData data = mStatusIconImpI.getSlotData(mStatusIconImpI.mVolteSlot);
            if(data.view instanceof ImageView){
                if(curSubResId != 0){
                    mStatusIconImpI.refreshStatusIcon(null, mStatusIconImpI.mVolteSlot, 0, true);
                    ImageView iv = (ImageView)data.view;
                    iv.setImageResource(curSubResId);
                } else {
                    mStatusIconImpI.refreshStatusIcon(null, mStatusIconImpI.mVolteSlot, 0, false);
                    return ;
                }
            }
        }

        private int addSubId(int subId){
            int simId = 0;
            if(firstSubId == -1 && secondSubId == -1){
                firstSubId = subId;
                simId = subId;
            } else if(firstSubId != -1 && secondSubId == -1){
                if(firstSubId == subId){
                    simId = firstSubId;
                } else {
                    secondSubId = subId;
                    simId = 3;
                }
            } else if(firstSubId == -1 && secondSubId != -1){
                if(secondSubId == subId){
                    simId = secondSubId;
                } else {
                    firstSubId = subId;
                    simId = 3;
                }
            } else if(firstSubId != -1 && secondSubId != -1){
                if(firstSubId == subId || secondSubId == subId){
                    simId = 3;
                } else {
                    simId = 0;
                }
            }
            return simId;
        }

        private int deleteSubId(int subId){
            int simId = 0;
            if(firstSubId == -1 && secondSubId == -1){
                simId = 0;
            } else if(firstSubId != -1 && secondSubId == -1){
                if(firstSubId == subId){
                    firstSubId = -1;
                    simId = 0;
                } else {
                    simId = firstSubId;
                }
            } else if(firstSubId == -1 && secondSubId != -1){
                if(secondSubId == subId){
                    secondSubId = -1;
                    simId = 0;
                } else {
                    simId = secondSubId;
                }
            } else if(firstSubId != -1 && secondSubId != -1){
                if(firstSubId == subId){
                    firstSubId = -1;
                    simId = secondSubId;
                } else if(secondSubId == subId){
                    secondSubId = -1;
                    simId = firstSubId;
                } else {
                    firstSubId = -1;
                    secondSubId = -1;
                    simId = 0;
                }
            }
            return simId;
        }

        public int getVolteResId(int simId){
            int resid = 0;
            if(mSimCount == 1){
                if(simId == 1 || simId == 2){
                    resid = R.drawable.liuhai2_volte0;
                } else {
                    resid = 0;
                }
            } else {
                if(simId == 1){
                    resid = R.drawable.liuhai2_volte1;
                } else if(simId == 2){
                    resid = R.drawable.liuhai2_volte2;
                } else if(simId == 3){
                    resid = R.drawable.liuhai2_volte3;
                } else {
                    resid = 0;
                }
            }
            return resid;
        }

        private int exchangeIcon(int iconId){
            int result = iconId;
            switch(iconId){
                case R.drawable.stat_sys_wifi_signal_0_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_0_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_1_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_1_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_2_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_2_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_3_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_3_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_4_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_4_prize;
                    break;
                case R.drawable.stat_sys_wifi_inout_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_inout_prize;
                    break;
                case R.drawable.stat_sys_wifi_in_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_in_prize;
                    break;
                case R.drawable.stat_sys_wifi_out_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_out_prize;
                    break;

                case R.drawable.stat_sys_wifi_signal_0_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_0_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_1_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_1_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_2_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_2_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_3_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_3_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_signal_4_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_signal_4_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_inout_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_inout_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_in_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_in_gray_prize;
                    break;
                case R.drawable.stat_sys_wifi_out_gray_prize:
                    result = R.drawable.liuhai2_stat_sys_wifi_out_gray_prize;
                    break;
            }
            return result;
        }
    }

    private final String DEBUG_TAG = PhoneStatusBar.DEBUG_LIUHAI_TAG + "_controller_";
    private String TAG = DEBUG_TAG;
    private boolean debug_switch = false;
    public void setDebugSwitch(boolean value, int index){
        debug_switch = value;
        TAG = DEBUG_TAG + index;
    }
    public void debugLiuHai(String msg){
        if(debug_switch){
            android.util.Log.d(TAG, msg);
        }
    }

    private InverseController mInverseController = new InverseController();
    public InverseController getInverseController(){
        return mInverseController;
    }
    public class InverseController{
        private ArrayList<LiuHaiStatusBarIconData> iconList = null;
        private TextView mClock;
        private LiuHaiBatteryMeterViewDefinedNew2 mLiuHaiBatteryView;
        private LiuHaiSignalView2 mLiuHaiSignalClusterView;

        public void initData(LinearLayout group){
            iconList = mStatusIconImpI.iconList;
            mClock = (TextView)group.findViewById(R.id.clock_liuhai);
            mLiuHaiBatteryView = (LiuHaiBatteryMeterViewDefinedNew2) group.findViewById(R.id.liuhai_battery_new2);
            mLiuHaiSignalClusterView = (LiuHaiSignalView2) group.findViewById(R.id.prize_liuhai_signal_cluster2);
            mLiuHaiSignalClusterView.setIgnoreStatusBarStyleChanged(false);
        }

        public void inverse(int style){
            onStatusBarStyleChanged(style);
            if(mLiuHaiSignalClusterView != null){
                mLiuHaiSignalClusterView.onStatusBarStyleChanged(style);
            }

            int color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
            if(mCurStatusBarStyle != 0){
                color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
            }
            if(mClock != null){
                mClock.setTextColor(color);
            }

            if(mLiuHaiBatteryView != null){
                mLiuHaiBatteryView.onStatusBarStyleChanged(style);
            }
        }
    }
    
    public void setStatusBarIconController(StatusBarIconController controller){
        mStatusBarIconController = controller;
    }
    public boolean isAllowInverse(){
        return mStatusBarIconController != null;
    }

    private StatusBarIconController mStatusBarIconController;
    private int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    @Override
    public void onStatusBarStyleChanged(int style) {
        if(mStatusBarIconController == null){
            return ;
        }
        debugLiuHai("onStatusBarStyleChanged(). curStyle=" + mCurStatusBarStyle + ", newStyle=" + style);
        if(mCurStatusBarStyle != style) {
            mCurStatusBarStyle = style;
            refreshAllIcons();
        }
    }

    public void refreshAllIcons(){
        for(int i = 0; i < mStatusIconImpI.iconList.size(); i++){
            LiuHaiStatusBarIconData d = mStatusIconImpI.iconList.get(i);
            if(d.visibility){
                mStatusIconImpI.refreshStatusIcon(null, d.slot, 0, true);
            }
        }
    }

    public void inverseView(LiuHaiStatusBarIconData data){
        if(mStatusBarIconController != null){
            debugLiuHai("inverseView data.slot = " + data.slot + ", mCurStatusBarStyle = " + mCurStatusBarStyle);
            if(data.slot.equals(mStatusIconImpI.mVolteSlot)){
                ImageView iv = (ImageView)data.view;
                int color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
                if(mCurStatusBarStyle != 0){
                    color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
                }
                iv.setImageTintList(ColorStateList.valueOf(color));
            } else if(data.slot.equals(mStatusIconImpI.mNetworkSpeedSlot)){
                if(data.speedTxt != null && data.unitTxt != null){
                    int color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
                    if(mCurStatusBarStyle != 0){
                        color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
                    }
            debugLiuHai("inverseView data.speedTxt = " + data.speedTxt);
                    data.speedTxt.setTextColor(color);
                    data.unitTxt.setTextColor(color);
                }
            } else {
                if(data.view instanceof StatusBarIconView){
                    StatusBarIconView view = (StatusBarIconView)data.view;
                    //if(isKeyguardController){
                    //    mStatusBarIconController.setKeyguardStatusIconForStyle(view, true, true);
                    //} else {
                        mStatusBarIconController.setStatusIconForStyle(view, true, true);
                    //}
                }
            }
        }
    }
}
