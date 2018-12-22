package com.pr.scuritycenter.activity;

import java.util.ArrayList;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.ITrafficCorrectionListener;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import tmsdk.common.IDualPhoneInfoFetcher;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class TrafficCorrectionWrapper {
	static final String TAG = "TrafficCorrectionDispatcher";
	// singleton
	private static TrafficCorrectionWrapper sInstance = new TrafficCorrectionWrapper();

	static TrafficCorrectionWrapper getInstance() {
		return sInstance;
	}

	ITrafficCorrectionListener mTrafficCorrectionListener;
	private TrafficCorrectionManager mTcMgr;
	// 保存流量信息
	private SharedPreferences mPreferences;

	private TrafficCorrectionWrapper() {
	}

	public void init(Context context) {
		if (context == null)
			return;
		mPreferences = context.getSharedPreferences("trafficInfo",
				Context.MODE_PRIVATE);

		mTcMgr = ManagerCreatorB.getManager(TrafficCorrectionManager.class);
		mTcMgr.setTrafficCorrectionListener(new ITrafficCorrectionListener() {

			@Override
			public void onNeedSmsCorrection(int simIndex, String queryCode,
					String queryPort) {
				if (mTrafficCorrectionListener != null) {
					mTrafficCorrectionListener.onNeedSmsCorrection(simIndex,
							queryCode, queryPort);
				}
			}

			@Override
			public void onTrafficInfoNotify(int simIndex, int trafficClass,
					int subClass, int kBytes) {
				saveTrafficInfo(simIndex, trafficClass, subClass, kBytes);

				if (mTrafficCorrectionListener != null) {
					mTrafficCorrectionListener.onTrafficInfoNotify(simIndex,
							trafficClass, subClass, kBytes);
				}
			}

			@Override
			public void onError(int simIndex, int errorCode) {
				if (mTrafficCorrectionListener != null) {
					mTrafficCorrectionListener.onError(simIndex, errorCode);
				}
			}
		});

	}

	public int startCorrection(int simIndex) {
		return mTcMgr.startCorrection(simIndex);
	}

	public int analysisSMS(int simIndex, String queryCode, String queryPort,
			String smsBody) {
		return mTcMgr.analysisSMS(simIndex, queryCode, queryPort, smsBody);
	}

	public ArrayList<CodeName> getAllProvinces() {
		return mTcMgr.getAllProvinces();
		
	}

	public ArrayList<CodeName> getCities(String provinceCode) {
		return mTcMgr.getCities(provinceCode);
	}

	public ArrayList<CodeName> getCarries() {

		return mTcMgr.getCarries();
	}

	public ArrayList<CodeName> getBrands(String carryId) {
		return mTcMgr.getBrands(carryId);
	}

	public int setConfig(int simIndex, String provinceId, String cityId,
			String carryId, String brandId, int closingDay) {
		return mTcMgr.setConfig(simIndex, provinceId, cityId, carryId, brandId,
				closingDay);
	}

	public void setTrafficCorrectionListener(ITrafficCorrectionListener listener) {
		mTrafficCorrectionListener = listener;
	}

	private void saveTrafficInfo(int simIndex, int trafficClass, int subClass,
			int kBytes) {
		if (mPreferences == null)
			return;
		Editor editor = mPreferences.edit();
		if (simIndex == IDualPhoneInfoFetcher.FIRST_SIM_INDEX) {

			// 常规流量
			if (trafficClass == ITrafficCorrectionListener.TC_TrafficCommon) {
				if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
					editor.putInt("SIM1_COMMON_LEFT_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
					editor.putInt("SIM1_COMMON_USED_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
					editor.putInt("SIM1_COMMON_TOTAL_KBYTES", kBytes);
				}
			}// 闲时
			else if (trafficClass == ITrafficCorrectionListener.TC_TrafficFree) {
				if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
					editor.putInt("SIM1_FREE_LEFT_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
					editor.putInt("SIM1_FREE_USED_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
					editor.putInt("SIM1_FREE_TOTAL_KBYTES", kBytes);
				}
			}// 4G
			else if (trafficClass == ITrafficCorrectionListener.TC_Traffic4G) {
				if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
					editor.putInt("SIM1_4G_LEFT_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
					editor.putInt("SIM1_4G_USED_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
					editor.putInt("SIM1_4G_TOTAL_KBYTES", kBytes);
				}
			}

		} else if (simIndex == IDualPhoneInfoFetcher.SECOND_SIM_INDEX) {
			// 常规流量
			if (trafficClass == ITrafficCorrectionListener.TC_TrafficCommon) {
				if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
					editor.putInt("SIM2_COMMON_LEFT_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
					editor.putInt("SIM2_COMMON_USED_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
					editor.putInt("SIM2_COMMON_TOTAL_KBYTES", kBytes);
				}
			}// 闲时
			else if (trafficClass == ITrafficCorrectionListener.TC_TrafficFree) {
				if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
					editor.putInt("SIM2_FREE_LEFT_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
					editor.putInt("SIM2_FREE_USED_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
					editor.putInt("SIM2_FREE_TOTAL_KBYTES", kBytes);
				}
			}// 4G
			else if (trafficClass == ITrafficCorrectionListener.TC_Traffic4G) {
				if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
					editor.putInt("SIM2_4G_LEFT_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
					editor.putInt("SIM2_4G_USED_KBYTES", kBytes);
				} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
					editor.putInt("SIM2_4G_TOTAL_KBYTES", kBytes);
				}
			}
		}
		editor.commit();
	}

	public int[] getTrafficInfo(int simIndex) {
		if (mPreferences == null)
			return new int[6];
		int commonLeft = -1, commonUsed = -1, commonTotal = -1;
		int freeLeft = -1, freeUsed = -1, freeTotal = -1;
		int g4Left = -1, g4Used = -1, g4Total = -1;
		if (simIndex == IDualPhoneInfoFetcher.FIRST_SIM_INDEX) {
			commonLeft = mPreferences.getInt("SIM1_COMMON_LEFT_KBYTES", -1);
			commonUsed = mPreferences.getInt("SIM1_COMMON_USED_KBYTES", -1);
			commonTotal = mPreferences.getInt("SIM1_COMMON_TOTAL_KBYTES", -1);
			freeLeft = mPreferences.getInt("SIM1_FREE_LEFT_KBYTES", -1);
			freeUsed = mPreferences.getInt("SIM1_FREE_USED_KBYTES", -1);
			freeTotal = mPreferences.getInt("SIM1_FREE_TOTAL_KBYTES", -1);
			g4Left = mPreferences.getInt("SIM1_4G_LEFT_KBYTES", -1);
			g4Used = mPreferences.getInt("SIM1_4G_USED_KBYTES", -1);
			g4Total = mPreferences.getInt("SIM1_4G_TOTAL_KBYTES", -1);

		} else if (simIndex == IDualPhoneInfoFetcher.SECOND_SIM_INDEX) {
			commonLeft = mPreferences.getInt("SIM2_COMMON_LEFT_KBYTES", -1);
			commonUsed = mPreferences.getInt("SIM2_COMMON_USED_KBYTES", -1);
			commonTotal = mPreferences.getInt("SIM2_COMMON_TOTAL_KBYTES", -1);
			freeLeft = mPreferences.getInt("SIM2_FREE_LEFT_KBYTES", -1);
			freeUsed = mPreferences.getInt("SIM2_FREE_USED_KBYTES", -1);
			freeTotal = mPreferences.getInt("SIM2_FREE_TOTAL_KBYTES", -1);
			g4Left = mPreferences.getInt("SIM2_4G_LEFT_KBYTES", -1);
			g4Used = mPreferences.getInt("SIM2_4G_USED_KBYTES", -1);
			g4Total = mPreferences.getInt("SIM2_4G_TOTAL_KBYTES", -1);
		}
		int ret[] = new int[9];
		ret[0] = commonLeft;
		ret[1] = commonUsed;
		ret[2] = commonTotal;
		ret[3] = freeLeft;
		ret[4] = freeUsed;
		ret[5] = freeTotal;
		ret[6] = g4Left;
		ret[7] = g4Used;
		ret[8] = g4Total;

		return ret;
	}
}
