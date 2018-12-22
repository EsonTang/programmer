package com.prize.music.admanager.statistics.model;

import java.util.List;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/17.14:51
 * @描述 对应的event事件
 */
public class Event {
    public String event_name;
    public long action_time;
    public List<KeyValueBean> parameter;

    public Event() {
    }

}
