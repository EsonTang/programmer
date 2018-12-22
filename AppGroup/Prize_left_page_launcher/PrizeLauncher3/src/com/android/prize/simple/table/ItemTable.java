package com.android.prize.simple.table;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import android.content.Intent;
import android.text.TextUtils;

/**
 * 简易桌面数据表
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
@Table(name = "t_simple_favarite")
public class ItemTable implements Serializable, Cloneable {
	/**表名*/
	public static final String TABLE_NAME = "t_simple_favarite";
    @Column(name = "id", isId = true)
    private int id;
    /**第几屏(第几页,页码)*/
    @Column(name = "screen")
    public int screen = -1;
    /**列位置*/
    @Column(name = "x")
    public int x;
    /**行位置*/
    @Column(name = "y")
    public int y;
    /**占几列*/
    @Column(name = "spanX")
    public int spanX = 1;
    /**占几行*/
    @Column(name = "spanY")
    public int spanY = 1;
    /**类型, 1:天气时间, 0:应用, 2:联系人, 3: 添加类型*/
    @Column(name = "type")
    public int type;
    /**名称,应用名称或联系人姓名*/
    @Column(name = "title")
    public String title;
    /**若为应用,则intent不为空*/
    @Column(name = "_intent")
    public String intent = "";
    /**若为通讯录,则此字段为通讯录ID*/
    @Column(name = "contactId")
    public long contactId;
    /**背景图片资源ID*/
    @Column(name = "bgResId")
    public int bgResId;
    /**描述*/
    @Column(name = "describ")
    public String describ;
    /**是否允许被删除*/
    @Column(name = "canDel")
    public boolean canDel = true;
    /**包名*/
    @Column(name = "pkgName")
    public String pkgName;
    /**类名*/
    @Column(name = "clsName")
    public String clsName;
    /**是否在表中存在, 默认为true*/
    public boolean isExist = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ItemTable {" +
                "id=" + getId() +
                ", screen='" + screen + '\'' +
                ", x='" + x + '\'' +
                ", y='" + y + '\'' +
                ", spanX='" + spanX + '\'' +
                ", spanY='" + spanY + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", clsName='" + clsName + '\'' +
                ", intent='" + intent + '\'' +
                ", contactId='" + contactId + '\'' +
                ", bgResId='" + bgResId + '\'' +
                ", describ='" + describ + '\'' +
                '}';
    }
    
    public void reset() {
    	pkgName = null;
    	clsName = null;
    	
    	id = -1;
        screen = -1;
        x = -1;
        y = -1;
        spanX = 1;
        spanY = 1;
        type = -1;
        title = null;
        intent = null;
        contactId = -1;
        bgResId = 0;
        describ = null;
    }
    
    private Intent it = null;
    /**
     * 获取intent
     * @return
     */
    public Intent getIt() {
    	if (it == null && !TextUtils.isEmpty(intent)) {
    		try {
				it = Intent.parseUri(intent, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return it;
    }
}
