package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/8.
 */

@Table(name = "table_downloaded")
public class DownloadedTable implements ITable,Serializable {

    @Column(name = "id")
    public int id;

    @Column(name = "type")
    public int type;

    @Column(name = "themeId",isId = true)
    public String themeID;

    /*缩略图路径 */
    @Column(name = "thumbnail")
    public  String thumbnail;

    @Column(name = "wallType")
    public String wallType;

    @Column(name = "total_size")
    public long total_size;

    @Column(name = "title")
    public String title;

    public long getTotal_size() {
        return total_size;
    }

    public void setTotal_size(long total_size) {
        this.total_size = total_size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getThemeID() {
        return themeID;
    }

    public void setThemeID(String themeID) {
        this.themeID = themeID;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getWallType() {
        return wallType;
    }

    public void setWallType(String wallType) {
        this.wallType = wallType;
    }

    @Override
    public String getTableName() {
        return "DwonloadedTable";
    }
}
