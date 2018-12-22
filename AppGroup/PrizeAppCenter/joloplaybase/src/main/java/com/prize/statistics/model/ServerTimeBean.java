package com.prize.statistics.model;

import java.io.Serializable;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/19.17:21
 * @描述
 */

public class ServerTimeBean implements Serializable {
    public Settings  settings;


    public static class Settings implements Serializable {
        public String serverTime;

    }
}
