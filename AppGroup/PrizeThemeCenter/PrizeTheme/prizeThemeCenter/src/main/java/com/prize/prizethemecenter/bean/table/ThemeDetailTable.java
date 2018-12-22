package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/8.
 */

@Table(name = "theme_download")
public class ThemeDetailTable implements ITable ,Serializable {

    /**
     * 主题、壁纸、字体 的所有下载记录
     * @return
     */

    @Column(name = "id")
    public int id;
    /**
     *  下载的类型
      */
    @Column(name = "loadFlag")
    public int loadFlag;

    @Column(name = "type")
    public int type;

    @Column(name = "themeId",isId = true)
    public String themeID;

     @Column(name = "title")
     public String title;

    /*下载的状态*/
    @Column(name = "status")
    public int status;

    @Column(name = "total_size")
    public long total_size;

    @Column(name = "download_url")
    public String download_url;

    @Column(name = "download_progress")
    public long download_progress;

//    /*主题是否属于收费主题*/
//    @Column(name = "isFree",property = "1")
//    public boolean isFree;

    /* 收费主题是否已经付费 */
    @Column(name = "isPay")
    public boolean isPay;

    /*缩略图路径 */
    @Column(name = "thumbnail")
    public  String thumbnail;

    @Column(name = "isSelect")
    public boolean isSelect;

    @Column(name = "md5")
    public String md5;

    @Column(name = "wallType")
    public String wallType;

    public int getLoadFlag() {
        return loadFlag;
    }

    public void setLoadFlag(int loadFlag) {
        this.loadFlag = loadFlag;
    }

    public String getWallType() {
        return wallType;
    }

    public void setWallType(String pWallType) {
        wallType = pWallType;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getThemeID() {
        return themeID;
    }

    public void setThemeID(String themeID) {
        this.themeID = themeID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public long getDownload_progress() {
        return download_progress;
    }

    public void setDownload_progress(long download_progress) {
        this.download_progress = download_progress;
    }

    public boolean isPay() {
        return isPay;
    }

    public void setIsPay(boolean isPay) {
        this.isPay = isPay;
    }

    public long getTotal_size() {
        return total_size;
    }

    public void setTotal_size(long total_size) {
        this.total_size = total_size;
    }

    @Override
    public String getTableName() {
        return "theme_download_history";
    }
}
