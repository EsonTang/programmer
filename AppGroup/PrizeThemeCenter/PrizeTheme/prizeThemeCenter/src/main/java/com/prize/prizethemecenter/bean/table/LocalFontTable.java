package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 本地字体表
 *
 * @author
 */
@SuppressWarnings("serial")
@Table(name = "table_local_font")
public class LocalFontTable implements ITable, Serializable {

    /**
     * id
     */
    @Column(name = "id", isId = true)
    public int id;

    @Column(name = "localFontId")

    public String localFontId;

    /**
     * 标题Title
     */
    @Column(name = "title")
    public String title;
    /**
     * 字体路径
     */
    @Column(name = "path")
    public String path;

    /**
     * 缩略图路径
     */
    @Column(name = "preview_path")
    public String preview_path;

    @Column(name = "isSelected")
    public boolean isSelected;


    @Column(name = "key")
    public String key;

    @Column(name = "md5")
    public String md5;

    @Override
    public String getTableName() {
        return "table_local_font";
    }

    public int getId() {
        return id;
    }

    public void setId(int pId) {
        id = pId;
    }

    public String getLocalFontId() {
        return localFontId;
    }

    public void setLocalFontId(String pLocalFontId) {
        localFontId = pLocalFontId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String pPath) {
        path = pPath;
    }

    public String getPreview_path() {
        return preview_path;
    }

    public void setPreview_path(String pPreview_path) {
        preview_path = pPreview_path;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean pSelected) {
        isSelected = pSelected;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String pMd5) {
        md5 = pMd5;
    }
}
