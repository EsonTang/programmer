package com.pr.scuritycenter.activity;

import java.util.ArrayList;

import tmsdk.bg.module.network.CodeName;
import tmsdk.common.ErrorCode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.widget.Toast;

import com.pr.scuritycenter.R;

/**
 * @author jiangweiti
 *
 */
public class LocationSetDlg implements OnClickListener {
	private Activity mContext;
	private Handler mHandler;
	private ArrayList<CodeName> mProvinces;
	private ArrayList<CodeName> mCitysList;

	private AlertDialog mCitysDlg;
	private AlertDialog mProvinceDlg;
	private ArrayList<CodeName> mCarrys;
	private AlertDialog mCarrysDlg;
	private ArrayList<CodeName> mBrands;
	private AlertDialog mBrandsDlg;
	private int mSimIndex;
	
	
	String mProvinceId, mCityId, mCarryId,mBrandId;
	public LocationSetDlg(Activity context, int simIndex) {
		mContext = context;
		mSimIndex = simIndex;
		
	}
	
	public void startDlg() {
		mProvinces = TrafficCorrectionWrapper.getInstance().getAllProvinces();//返回所有省份,CodeName格式如("10", "北京")
		String[] nItems = new String[mProvinces.size()];
		for(int i = 0; i < nItems.length; i++) {
			nItems[i] = mProvinces.get(i).mName;//各省份名称
		}
		
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setTitle(R.string.select_province);//选择省份
		ab.setItems(nItems, this);
//		ab.setOnCancelListener(this);
		mProvinceDlg = ab.create();
		mProvinceDlg.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(mProvinceDlg == dialog) {//province
			mCitysList = TrafficCorrectionWrapper.getInstance().getCities(mProvinces.get(which).mCode);//城市列表
			mProvinceId = mProvinces.get(which).mCode;//获取SIM卡所属省份ID
			String[] nItems = new String[mCitysList.size()];
			for(int i = 0; i < nItems.length; i++) {
				nItems[i] = mCitysList.get(i).mName;//各城市名称
			}
			
			AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
			ab.setTitle(R.string.select_city);
			ab.setItems(nItems, this);
//			ab.setOnCancelListener(this);
			mCitysDlg = ab.create();
			mCitysDlg.show();
		} else if(dialog == mCitysDlg) {
			mCityId = mCitysList.get(which).mCode;//SIM卡所属城市ID
			mCarrys = TrafficCorrectionWrapper.getInstance().getCarries();//返回运营商列表,CodeName格式如("CMCC","中国移动")
			String[] nItems = new String[mCarrys.size()];
			for(int i = 0; i < nItems.length; i++) {
				nItems[i] = mCarrys.get(i).mName;//各运营商名称
			}
			
			AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
			ab.setTitle(R.string.select_carry);
			ab.setItems(nItems, this);
//			ab.setOnCancelListener(this);
			mCarrysDlg = ab.create();
			mCarrysDlg.show();
		} else if(dialog == mCarrysDlg) {
			mCarryId = mCarrys.get(which).mCode;//运营商ID
			mBrands = TrafficCorrectionWrapper.getInstance().getBrands(mCarrys.get(which).mCode);//获取运营商下面的品牌列表(格式：编号,中文名)，如果是错误的参数，则返回null
			String[] nItems = new String[mBrands.size()];
			for(int i = 0; i < nItems.length; i++) {
				nItems[i] = mBrands.get(i).mName;//运营商下面的品牌名称
			}
			
			AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
			ab.setTitle(R.string.select_brands);
			ab.setItems(nItems, this);
//			ab.setOnCancelListener(this);
			mBrandsDlg = ab.create();
			mBrandsDlg.show();
		} else if(dialog == mBrandsDlg) {
			mBrandId = mBrands.get(which).mCode;//SIM卡类型ID
			
			android.util.Log.v("TrafficCorrectionUser", "ConfigInfo-mProvinceId:[" + 
					mProvinceId +"]mCityId:["+mCityId+"]mCarryId:["+
					mCarryId + "]mBrandId:[" + mBrandId + "]");
			int result = TrafficCorrectionWrapper.getInstance().setConfig(
					mSimIndex,
					mProvinceId,
					mCityId,
					mCarryId,
					mBrandId,
					1);//保存配置。在进行流量校正之前，必要进行设置。返回ErrorCode
			if(result != ErrorCode.ERR_NONE) {
				Toast.makeText(mContext, "set config error : "+result, Toast.LENGTH_LONG).show();
			}
		}
	}

//	@Override
//	public void onCancel(DialogInterface dialog) {
//		Toast.makeText(mContext, "set config canceled", Toast.LENGTH_SHORT).show();
//		mContext.finish();
//	}
}
