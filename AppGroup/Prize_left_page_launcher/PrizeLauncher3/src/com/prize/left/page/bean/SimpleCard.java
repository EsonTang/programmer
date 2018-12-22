package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.HashMap;

import com.prize.left.page.ItemViewType;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.util.IConstants;

@SuppressWarnings("serial")
public class SimpleCard implements Serializable {

	public SimpleCard() {
		// 
	}
	public int id;
	/**card编号*/
//	public int code;
	/**卡片类型*/
	public String uitype;
	/**卡片数据类型*/
	public String dataCode;
	/**card状态, 是否可见, 1可见, 否则不可见*/
	public int status;
	
	public String name;
//	public String dataUrl;
	/**card状态, 是否有更多*/
	public int  moreType;
	/**卡片更多地址*/
	public String moreUrl;
//	public String bigCode;
	/**是否需要定位*/
	public int needLoc;
	//同类型卡片第几张
	public int count;
	
	public CardType toCardType() {
		HashMap<String, Integer> cmap=LeftModel.cmap;
		CardType a = new CardType();
		a.code = cmap.get(uitype);
		a.name= this.name;
		a.dataUrl=dataCode;
		a.bigCode=uitype;
		a.moreUrl=moreUrl;
		a.moreType=moreType;
		a.status = status;
		a.needLoc=needLoc;
		a.uitype=uitype;
		a.dataCode=dataCode;
		a.subCode=count;
		return a;
	}
	
	public SelCardType toSelCardType() {
		HashMap<String, Integer> cmap=LeftModel.cmap;
		SelCardType a = new SelCardType();
		a.code = cmap.get(uitype);
		a.dataCode = dataCode;
		a.name= this.name;
		a.status=this.status;
		a.subCode=this.count;
		return a;
	}

	@Override
	public String toString() {
		return "SimpleCard [id=" + id + ", uitype=" + uitype + ", dataCode="
				+ dataCode + ", status=" + status + ", name=" + name
				+ ", moreType=" + moreType + ", moreUrl=" + moreUrl
				+ ", needLoc=" + needLoc + "]";
	}
	
	
}
