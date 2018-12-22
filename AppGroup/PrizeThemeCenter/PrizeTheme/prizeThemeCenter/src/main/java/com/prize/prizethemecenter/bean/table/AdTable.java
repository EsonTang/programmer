package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 首页滚动条广告TABLE
 * @author pengy
 *
 */
@SuppressWarnings("serial")
@Table(name = "table_ad")
public class AdTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true)
    public int id;
	/**图片URL*/
    @Column(name = "")
    public String imageurl;
    /**大图片URL*/
    @Column(name = "bigimageurl")
    public String bigimageurl;

    @Override
	public String getTableName() {
        return "table_ad";
	}
}
