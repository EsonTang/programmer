package com.pr.scuritycenter.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.SmsEntity;

/**
 * 实体转换器 作为DEMO，只简单实现了联系人实体的转换的 实际项目开发当中，要根据项目中具体实体的义做相应的转换，这些方法都需要实现
 * 
 * @author serenazhou
 * 
 */
public class EntityConvert implements IEntityConverter {

	// 信息实体转换器
	@Override
	public SmsEntity convert(SmsEntity entity) {
		SmsEntity sms = new SmsEntity(entity);
		return sms;
	}
	// 电话实体转换器
	@Override
	public CallLogEntity convert(CallLogEntity entity) {
		CallLogEntity calllog = new CallLogEntity(entity);
		return calllog;
	}
}
