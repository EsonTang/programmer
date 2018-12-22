package com.pr.scuritycenter.aresengine;

import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.bg.module.aresengine.PhoneDeviceController;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.ContactEntity;
import tmsdk.common.module.aresengine.DefaultSysDao;
import tmsdk.common.module.aresengine.ICallLogDao;
import tmsdk.common.module.aresengine.IContactDao;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.IKeyWordDao;
import tmsdk.common.module.aresengine.ILastCallLogDao;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.SmsEntity;
import android.content.Context;

import com.pr.scuritycenter.aresengine.dao.BlackListDao;
import com.pr.scuritycenter.aresengine.dao.CallLogDao;
import com.pr.scuritycenter.aresengine.dao.KeyWordDao;
import com.pr.scuritycenter.aresengine.dao.LastCallLogDao;
import com.pr.scuritycenter.aresengine.dao.PrivateCallLogDao;
import com.pr.scuritycenter.aresengine.dao.PrivateListDao;
import com.pr.scuritycenter.aresengine.dao.PrivateSmsDao;
import com.pr.scuritycenter.aresengine.dao.SmsDao;
import com.pr.scuritycenter.aresengine.dao.WhiteListDao;

/**
 * “战神”引擎的构造器, 提供了拦截功能 项目若要提供拦截功能，要实现此类中的所有方法
 * 
 */
public final class InterceptAresEngineFactor extends AresEngineFactor {
	private Context mContext;

	public InterceptAresEngineFactor(Context context) {
		mContext = context;
	}

	@Override
	public IContactDao<? extends ContactEntity> getBlackListDao() {
		return BlackListDao.getInstance(mContext);
	}

	@Override
	public ICallLogDao<? extends CallLogEntity> getCallLogDao() {
		return CallLogDao.getInstance();
	}

	@Override
	public IEntityConverter getEntityConverter() {
		return new EntityConvert();
	}

	@Override
	public IKeyWordDao getKeyWordDao() {
		return KeyWordDao.getInstance();
	}

	@Override
	public ILastCallLogDao getLastCallLogDao() {
		return LastCallLogDao.getInstance();
	}

	@Override
	public ICallLogDao<? extends CallLogEntity> getPrivateCallLogDao() {
		return PrivateCallLogDao.getInstance();
	}

	@Override
	public IContactDao<? extends ContactEntity> getPrivateListDao() {
		return PrivateListDao.getInstance();
	}

	@Override
	public ISmsDao<? extends SmsEntity> getPrivateSmsDao() {
		return PrivateSmsDao.getInstance();
	}

	@Override
	public ISmsDao<? extends SmsEntity> getSmsDao() {
		return SmsDao.getInstance();
	}

	@Override
	public IContactDao<? extends ContactEntity> getWhiteListDao() {
		return WhiteListDao.getInstance();
	}
	
	@Override
	public PhoneDeviceController getPhoneDeviceController() {
		return super.getPhoneDeviceController();
	}

	@Override
	public AbsSysDao getSysDao() {
		return DefaultSysDao.getInstance(mContext);
	}
}
