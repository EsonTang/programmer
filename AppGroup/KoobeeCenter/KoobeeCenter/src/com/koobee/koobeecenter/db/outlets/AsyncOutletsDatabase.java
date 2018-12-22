package com.koobee.koobeecenter.db.outlets;

import com.koobee.koobeecenter.db.AsyncBase;
import com.koobee.koobeecenter.db.AsyncBaseExpand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by yiyi on 2015/5/20.
 */
public class AsyncOutletsDatabase extends AsyncBaseExpand {

	public static final int Q_CITYS = 1;
	public static final int Q_BYAREA = 2;

	private static AsyncOutletsDatabase sIntance = new AsyncOutletsDatabase();

	private AsyncOutletsDatabase() {
		super();
	}

	public static AsyncOutletsDatabase getInstance() {
		return sIntance;
	}

	public void queryCityes(int what, AsyncBase.OnDataAvailable callback) {
		if (callback == null)
			return;
		sendMessage(what, mWorkHandler, new Worker() {
			@Override
			public void doInBackground(int backId, Object replyTo) {
				List<CustomerTable.Info> datas = OutletsDatabase.getInstance()
						.queryCitys();
				Map<String, Set<String>> map = new LinkedHashMap<String, Set<String>>();
				for (CustomerTable.Info info : datas) {
					Set<String> set = map.get(info.province);
					if (set == null) {
						set = new LinkedHashSet<String>();
						map.put(info.province, set);
					}
					set.add(info.area);
				}
				sendWorkOverMessage(backId, replyTo, map);
			}
		}, callback);
	}

	public void queryByArea(int what, String area,
			AsyncBase.OnDataAvailable callback) {
		if (callback == null)
			return;
		final String areaValue = area;
		sendMessage(what, mWorkHandler, new Worker() {
			@Override
			public void doInBackground(int backId, Object replyTo) {
				List<CustomerTable.Info> data = OutletsDatabase.getInstance()
						.queryByArea(areaValue);
				sendWorkOverMessage(backId, replyTo, data);
			}
		}, callback);
	}

}
