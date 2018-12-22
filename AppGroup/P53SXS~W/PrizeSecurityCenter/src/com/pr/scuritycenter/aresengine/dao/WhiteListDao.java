package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.ContactEntity;
import tmsdk.common.module.aresengine.IContactDao;

/**
 * 白名单配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class WhiteListDao implements IContactDao<ContactEntity> {
	private static List<ContactEntity> mWhiteList = new ArrayList<ContactEntity>();
	private static WhiteListDao mWhiteListDao;
	private static final int NUM_ENTITIES = 5;  
	private static int mEntityIds[] = {4, 7, 10, 11, 12};
	private static String mPhoneNums[] = {"135127205162", "075532146367", "+85298763456", "64373567", "02033319991"};
	private static String mNames[] = {"教练", "马总", "马总(香港)", "李总", "吴总"};

	private WhiteListDao() {
		InterceptDaoHelper.populateStaticData(mWhiteList, NUM_ENTITIES, mEntityIds, mPhoneNums, mNames);
	}
	
	// 获取白名单配置实例
	public static WhiteListDao getInstance() {
		if (null == mWhiteListDao) {
			synchronized (WhiteListDao.class) {
				mWhiteListDao = new WhiteListDao();
			}
		}
		return mWhiteListDao;
	}
	
	// 对号码进行是否在白名单中的匹配
	@Override
	public boolean contains(String phonenum, int flags) {
		// 不同的比较方式，可以达到不同的效果，比如可以定义前缀匹配等，都可以根据具体的业务需要实现
		// 这里以后8位来匹配
		return InterceptDaoHelper.contains(mWhiteList, phonenum, flags);
	}

	// 从白名单中移除
	public boolean delete(ContactEntity entity) {
		mWhiteList.remove(entity);
		return false;
	}
}
