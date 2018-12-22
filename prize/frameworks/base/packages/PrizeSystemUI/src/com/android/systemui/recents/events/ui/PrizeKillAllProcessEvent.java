package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;

/**
 * Created by prize on 2017/12/4.
 */

public class PrizeKillAllProcessEvent extends EventBus.Event {
    public Task task;

    public PrizeKillAllProcessEvent() {
        task = null;
    }

    public PrizeKillAllProcessEvent(Task t) {
        task = t;
    }
}
