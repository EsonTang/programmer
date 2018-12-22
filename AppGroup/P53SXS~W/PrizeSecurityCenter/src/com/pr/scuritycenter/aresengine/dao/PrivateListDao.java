package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.ContactEntity;
import tmsdk.common.module.aresengine.IContactDao;

/**
 * 私密联系人配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class PrivateListDao implements IContactDao<ContactEntity> {
	private static List<ContactEntity> mSecureList = new ArrayList<ContactEntity>();
	private static PrivateListDao mSecureListDao;
	private static final int NUM_ENTITIES = 5;  
	private static int mEntityIds[] = {13, 15, 16, 20, 22};
	private static String mPhoneNums[] = {"15914354346", "076926709394", "59276037", "58376035", "83423734"};
	private static String mNames[] = {"小秘", "小丽", "高警官", "张教主", "李老板"};

	private PrivateListDao() {
		InterceptDaoHelper.populateStaticData(mSecureList, NUM_ENTITIES, mEntityIds, mPhoneNums, mNames);
	}

	// 获取私密联系人配置实例
	public static PrivateListDao getInstance() {
		if (null == mSecureListDao) {
			synchronized (PrivateListDao.class) {
				mSecureListDao = new PrivateListDao();
			}
		}
		return mSecureListDao;
	}

	@Override
	public boolean contains(String phonenum, int flags) {
		// 不同的比较方式，可以达到不同的效果，比如可以定义前缀匹配等，都可以根据具体的业务需要实现
		// 这里以后8位来匹配
		return InterceptDaoHelper.contains(mSecureList, phonenum, flags);
	}
}
