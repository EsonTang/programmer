package com.prize.app.beans;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;


/**
 * 应用安装失败
 */
@Table(name = "AppInstallFaile")
public class AppInstallFaile implements Serializable {
    @Column(name = "id", autoGen = true, isId = true)
    public int id;
    @Column(name = "packageName")
    public String packageName;
    @Column(name = "versionCode")
    public int versionCode;
    @Column(name = "installCount")
    public int installCount;
    @Column(name = "timeStamp")
    public long timeStamp;

    @Override
    public String toString() {
        return "AppInstallFaile{" +
                "id=" + id +
                ", packageName='" + packageName + '\'' +
                ", versionCode=" + versionCode +
                ", installCount=" + installCount +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
