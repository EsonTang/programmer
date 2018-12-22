package com.prize.left.page.bean.table;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import android.content.ContentValues;

/**
 * 常用位置(家里,公司地址)表
 * @author fanjunchen
 *
 */
@Table(name = "t_normal_addr")
public class NormalAddrTable implements ITable {

    @Column(name = "id", isId = true)
    private int id;
    /**账号*/
    @Column(name = "userId")
    public String userId;
    /**家里住址*/
    @Column(name = "homeAddr")
    public String homeAddr;
    /**家里地址经度*/
    @Column(name = "homeLan")
    public String homeLan;
    /**家里地址纬度*/
    @Column(name = "homeLon")
    public String homeLon;
    
    /**公司地址*/
    @Column(name = "companyAddr")
    public String companyAddr;
    /**公司地址经度*/
    @Column(name = "companyLan")
    public String companyLan;
    /**公司地址纬度*/
    @Column(name = "companyLon")
    public String companyLon;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SelCardType {" +
                "id=" + getId() +
                ", userId='" + userId + '\'' +
                ", homeAddr='" + homeAddr + '\'' +
                ", companyAddr='" + companyAddr + '\'' +
                '}';
    }
    /***
     * 转换成ContentValues
     * @return
     */
    public ContentValues toContentValues() {
    	ContentValues cv = new ContentValues();
    	cv.put("userId", userId);
    	cv.put("homeAddr", homeAddr);
    	cv.put("homeLan", homeLan);
    	cv.put("homeLon", homeLon);
    	cv.put("companyAddr", companyAddr);
    	cv.put("companyLan", companyLan);
    	cv.put("companyLon", companyLon);
    	return cv;
    }
    
    @Override
	public String getTableName() {
		return "t_normal_addr";
	}
}
