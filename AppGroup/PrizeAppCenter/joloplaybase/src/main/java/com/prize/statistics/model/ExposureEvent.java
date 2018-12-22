package com.prize.statistics.model;

import java.util.List;

/**
 * longbaoxiu
 * 2016/12/17.14:51
 * 曝光的event事件
 */
public class ExposureEvent {
    public String event_name;
    public long action_time;
    public List<ExposureBean> parameter;

    public ExposureEvent() {
    }

    @Override
    public String toString() {
        return "ExposureEvent{" +
                "event_name='" + event_name + '\'' +
                ", action_time=" + action_time +
                ", parameter=" + parameter +
                '}';
    }
}
