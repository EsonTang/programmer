package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.ILastCallLogDao;

/**
 * 最近通话记录配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class LastCallLogDao implements ILastCallLogDao {
	private static List<CallLogEntity> mLastCallList = new ArrayList<CallLogEntity>();	
	private static LastCallLogDao mLastCallLogDao;
	
	private LastCallLogDao() {
	}
	
	// 获取最近通话记录配置实例
	public static LastCallLogDao getInstance() {
		if (null == mLastCallLogDao) {
			synchronized (LastCallLogDao.class) {
				mLastCallLogDao = new LastCallLogDao();
			}
		}
		return mLastCallLogDao;
	}
	
	// 判断输入的号码是否是最近通话记录中存在的
	@Override
	public boolean contains(String phonenum) {
		for(CallLogEntity tempEntity : mLastCallList){
			if(tempEntity.phonenum.equals(phonenum)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void update(CallLogEntity calllog) {
		// TODO Auto-generated method stub
	}

}
