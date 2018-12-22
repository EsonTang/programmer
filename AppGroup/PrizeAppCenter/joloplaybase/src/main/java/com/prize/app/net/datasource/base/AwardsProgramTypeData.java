package com.prize.app.net.datasource.base;

import com.prize.app.beans.AwardaProgramBean;
import com.prize.app.net.AbstractNetData;

import java.util.ArrayList;
import java.util.List;

/**
 **
 * 有奖活动返回对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AwardsProgramTypeData extends AbstractNetData {
	public List<AwardaProgramBean> list = new ArrayList<AwardaProgramBean>();

	public int pageCount;
	public int pageIndex;
	public short pageSize;
	public int pageItemCount;


}
