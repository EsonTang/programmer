package com.prize.statistics.model;

import java.util.List;

/**
 * longbaoxiu
 * 2016/12/17.14:51
 * 对应的event事件
 */
public class Event {
    public String event_name;
    public long action_time;
    public List<KeyValueBean> parameter;

    public Event() {
    }

}
