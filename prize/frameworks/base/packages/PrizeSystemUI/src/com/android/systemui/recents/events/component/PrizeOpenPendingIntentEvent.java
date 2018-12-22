


package com.android.systemui.recents.events.component;

import android.content.Context;

import com.android.systemui.recents.events.EventBus;



public class PrizeOpenPendingIntentEvent extends EventBus.Event {

    public final int type;  //1 qq,  2 mm
    
    public static final int QQ_TYPE = 1;
    public static final int MM_TYPE = 2;

    public PrizeOpenPendingIntentEvent( int type ) {
        this.type = type;
    }
}
