package com.prize.left.page.bean.table;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import com.prize.left.page.bean.CardBean;

/**
 * 卡片类型表
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
@Table(name = "t_cardType")
public class CardType implements ITable, Serializable, Cloneable {

    @Column(name = "id", isId = true)
    private int id;
    /**卡片类型编码*/
    @Column(name = "code")
    public int code;
    /**类型名称*/
    @Column(name = "name")
    public String name;
    /**类型排序号*/
    @Column(name = "_sort")
    public int sort;
    /**此类型卡片数据来源*/
    @Column(name = "dataUrl")
    public String dataUrl;
    /**此类型卡片点击更多时类型*/
    @Column(name = "moreType")
    public int moreType;
    /**此类型卡片点击更多时类型*/
    @Column(name = "moreUrl")
    public String moreUrl;
    /**此类型卡片点击更多时跳转的包名*/
    @Column(name = "pkg")
    public String pkg;
    /**此类型卡片点击更多时跳转的类名*/
    @Column(name = "clsName")
    public String clsName;
    /**此类型卡片的logo*/
    @Column(name = "typeIcon")
    public String typeIconUrl;
    /**是否需要位置信息 1:需要, 否则不需要*/
    @Column(name = "needLoc")
    public int needLoc = 0;
    /**归属大类编号*/
    @Column(name = "bigCode")
    public String bigCode;
    /**可用状态 1:可用, 否则不可用, 这个字段主要是用来与网络同步*/
    @Column(name = "status")
    public int status = 1;
    /**子类卡片请求参数*/
    @Column(name = "uitype")
    public String uitype;
    @Column(name = "dataCode")
	public String dataCode;
    
    /**子类弄编码*/
    @Column(name = "subCode")
    public int subCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CardType {" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", sort='" + sort + '\'' +
                ", dataUrl='" + dataUrl + '\'' +
                ", moreType='" + moreType + '\'' +
                ", moreUrl='" + moreUrl + '\'' +
                ", pkg='" + pkg + '\'' +
                ", clsName='" + clsName + '\'' +
                ", typeIconUrl='" + typeIconUrl + '\'' +
                ", subCode='" + subCode + '\'' +
                '}';
    }
    
    @Override
	public String getTableName() {
		return "t_cardType";
	}
    
    @Override
	public CardType clone() throws CloneNotSupportedException {
    	CardType c = new CardType();
    	c.bigCode = bigCode;
    	c.setId(id);
    	c.clsName = clsName;
    	c.code = code;
    	c.dataUrl = dataUrl;
    	c.moreType = moreType;
    	c.moreUrl = moreUrl;
    	c.name = name;
    	c.needLoc = needLoc;
    	c.pkg = pkg;
    	c.sort = sort;
    	c.status = status;
    	c.subCode = subCode;
    	c.typeIconUrl = typeIconUrl;
    	c.dataCode=dataCode;
    	return c;
    }
}
