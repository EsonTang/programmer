package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ICallLogDao;

/**
 * 电话拦截记录配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class CallLogDao implements ICallLogDao<CallLogEntity> {
	private static List<CallLogEntity> mCallLogList = new ArrayList<CallLogEntity>();
	private static CallLogDao mCallLogDao;
	
	private CallLogDao() {
	}
	
	// 获取电话拦截记录配置实例
	public static CallLogDao getInstance() {
		if (null == mCallLogDao) {
			synchronized (CallLogDao.class) {
				mCallLogDao = new CallLogDao();
			}
		}
		return mCallLogDao;
	}
	
	/**
	 * 获取私密通话记录
	 * @return
	 */
	public List<CallLogEntity> getSecureCallList(){
		List<CallLogEntity> callLogList = new ArrayList<CallLogEntity>();

		for(CallLogEntity entity : callLogList){
//			if(entity.isSecure)
//				callLogList.add(entity);
		}
		return callLogList;
	}

	// 将通话记录保存到变量 mCallLogList 中
	@Override
	public long insert(CallLogEntity entity, FilterResult result) {
		mCallLogList.add(entity);
		return -1;
	}
}
