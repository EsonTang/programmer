package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * @author pengy  壁纸详情表
 */
@SuppressWarnings("serial")
@Table(name = "table_wallDetail")
public class WallDetailTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true)
    public int id;
    /**壁纸ID*/
    @Column(name = "wallID")
    public String wallID;
	/**壁纸描述*/
    @Column(name = "describe")
    public String descripe;
    /**大图片URL*/
    @Column(name = "path")
    public String path;

    @Override
	public String getTableName() {
        return "table_wallDetail";
	}
}
