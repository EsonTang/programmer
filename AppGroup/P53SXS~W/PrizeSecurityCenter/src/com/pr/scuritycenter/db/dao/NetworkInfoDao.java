package com.pr.scuritycenter.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import tmsdk.bg.module.network.INetworkInfoDao;
import tmsdk.bg.module.network.NetDataEntity;
import tmsdk.common.module.network.NetworkInfoEntity;
import android.content.Context;

import com.pr.scuritycenter.db.NetworkInfoDBHelper;

/**
 * 
 * @author Bian
 * 
 */
public final class NetworkInfoDao implements INetworkInfoDao {

	private static NetworkInfoDBHelper helper;
	private static Context context;
	private long mUsedForMonth = 0;
	private int mClosingDay = 1;
	private long mTotalForMonth = 30 * 1024 * 1024;
	private ArrayList<NetworkInfoEntity> mDatas = new ArrayList<NetworkInfoEntity>();
	private NetDataEntity mLastNetDataEntity;
	private NetworkInfoEntity mTodayNetworkInfoEntity = new NetworkInfoEntity();

	public NetworkInfoDao(Context context) {
		super();
		helper = NetworkInfoDBHelper.getHelper(context);
		mTodayNetworkInfoEntity.mTotalForMonth = mTotalForMonth;// 每月限额流量
		mTodayNetworkInfoEntity.mUsedForMonth = mUsedForMonth;// 该月已用的流量
		mTodayNetworkInfoEntity.mUsedForDay = 0l;// 本日已经用流量
		mTodayNetworkInfoEntity.mRetialForMonth = mTotalForMonth;// 该月剩余流量
	}

	private static HashMap<String, INetworkInfoDao> sInstances = new HashMap<String, INetworkInfoDao>();

	public static synchronized INetworkInfoDao getInstance(String name) {

		INetworkInfoDao result = null;
		if (!sInstances.containsKey(name)) {
			result = new NetworkInfoDao(context);
			sInstances.put(name, result);
		} else {
			result = sInstances.get(name);
		}
		return result;
	}

	@Override
	public void clearAll() {
		mDatas.clear();
	}

	// 获取当前所有流量监控日志,并返回流量日志列表
	@Override
	public ArrayList<NetworkInfoEntity> getAll() {
		return (ArrayList<NetworkInfoEntity>) mDatas.clone();
	}

	// 获取月结日，并返回月结日 [1~31]
	@Override
	public int getClosingDayForMonth() {
		return mClosingDay;
	}

	// 获取上一次网络情况
	@Override
	public NetDataEntity getLastNetDataEntity() {
		return mLastNetDataEntity;
	}

	// 获取当天网络流量监控情况
	@Override
	public NetworkInfoEntity getTodayNetworkInfoEntity() {
		return mTodayNetworkInfoEntity;
	}

	// 获取本月限制的流量，单位B
	@Override
	public long getTotalForMonth() {
		return mTotalForMonth;
	}

	// 获取本月已用流量,单位B,并返回本于剩余流量
	@Override
	public long getUsedForMonth() {
		return mUsedForMonth;
	}

	// 插入一条流量监控日志
	@Override
	public void insert(NetworkInfoEntity arg0) {
		mDatas.add(arg0);
	}

	// 清空当天流量监控情况
	@Override
	public void resetToDayNetworkInfoEntity() {
		mTodayNetworkInfoEntity = new NetworkInfoEntity();

	}

	// 设置月结日，只对Mobile有效，WIFI无效
	@Override
	public void setClosingDayForMonth(int arg0) {
		mClosingDay = arg0;
	}

	// 保存上一次网络情况
	@Override
	public void setLastNetDataEntity(NetDataEntity arg0) {
		mLastNetDataEntity = arg0;
	}

	// 设置当天的网络流量监控情况
	@Override
	public void setTodayNetworkInfoEntity(NetworkInfoEntity arg0) {
		mTodayNetworkInfoEntity = arg0;
	}

	// 设置本月限制的流量，单位B
	@Override
	public void setTotalForMonth(long arg0) {
		mTotalForMonth = arg0;
	}

	// 设置本月已用流量,单位B
	@Override
	public void setUsedForMonth(long arg0) {
		mUsedForMonth = arg0;
	}

	/*
	 * 系统时间改变后获取最新数据。
	 * 
	 * @param mStartDate,调整系统前一次的刷新时间
	 * 
	 * @return 新的数据，包括每月限额，本月已用，今日已用。
	 */
	@Override
	public NetworkInfoEntity getSystemTimeChange(Date mStartDate) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc) add by gabeli 不解其中含义，先当成清空当月的，保留当天的记录。
	 * 
	 * @see
	 * tmsdk.bg.module.network.INetworkInfoDao#resetMonthNetworkinfoEntity()
	 */
	@Override
	public void resetMonthNetworkinfoEntity() {
		// TODO Auto-generated method stub
		mTodayNetworkInfoEntity.mUsedForMonth = 0l;
	}

}
