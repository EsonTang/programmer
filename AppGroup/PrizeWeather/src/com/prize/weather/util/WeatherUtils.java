package com.prize.weather.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.prize.weather.R;

public class WeatherUtils{
	
	public static String getSurfix(String str){
		return str.substring(str.lastIndexOf("/")+1);
	}

    /*
    0���� 1�Ϻ� 2��� 3���� 4�㶫 5���� 6�㽭 7���� 8���� 9���� 10�ӱ�
    11���� 12�Ĵ� 13���� 14���� 15���� 16���� 17���� 18���� 19ɽ�� 20ɽ��
    21���� 22���� 23���� 24���� 25�ຣ 26���� 27���� 28�½� 29���� 30���ɹ� 
    31̨�� 32��� 33����
    */
	public static int getArray(int index){
		if(index == 0){
			return R.array.beijing_option;
		}else if(index == 1){
			return R.array.shanghai_option;
		}else if(index == 2){
			return R.array.tianjin_option;
		}else if(index == 3){
			return R.array.chongqing_option;
		}else if(index == 4){
			return R.array.guangdong_option;
		}else if(index == 5){
			return R.array.guangxi_option;
		}else if(index == 6){
			return R.array.zhejiang_option;
		}else if(index == 7){
			return R.array.fujian_option;
		}else if(index == 8){
			return R.array.hubei_option;
		}else if(index == 9){
			return R.array.hunan_option;
		}else if(index == 10){
			return R.array.hebei_option;
		}else if(index == 11){
			return R.array.henan_option;
		}else if(index == 12){
			return R.array.sichuan_option;
		}else if(index == 13){
			return R.array.yunnan_option;
		}else if(index == 14){
			return R.array.guizhou_option;
		}else if(index == 15){
			return R.array.hainan_option;
		}else if(index == 16){
			return R.array.jiangsu_option;
		}else if(index == 17){
			return R.array.jiangxi_option;
		}else if(index == 18){
			return R.array.anhui_option;
		}else if(index == 19){
			return R.array.shandong_option;
		}else if(index == 20){
			return R.array.shanxi1_option;
		}else if(index == 21){
			return R.array.liaoning_option;
		}else if(index == 22){
			return R.array.jilin_option;
		}else if(index == 23){
			return R.array.shanxi2_option;
		}else if(index == 24){
			return R.array.gansu_option;
		}else if(index == 25){
			return R.array.qinghai_option;
		}else if(index == 26){
			return R.array.ningxia_option;
		}else if(index == 27){
			return R.array.xizang_option;
		}else if(index == 28){
			return R.array.xinjiang_option;
		}else if(index == 29){
			return R.array.heilongjiang_option;
		}else if(index == 30){
			return R.array.neimenggu_option;
		}else if(index == 31){
			return R.array.taiwan_option;
		}else if(index == 32){
			return R.array.hongkong_option;
		}else if(index == 33){
			return R.array.macau_option;
		}else{
			return -1;
		}
	}

	public static int getPinyinArray(int index){
		if(index == 0){
			return R.array.beijing;
		}else if(index == 1){
			return R.array.shanghai;
		}else if(index == 2){
			return R.array.tianjin;
		}else if(index == 3){
			return R.array.chongqing;
		}else if(index == 4){
			return R.array.guangdong;
		}else if(index == 5){
			return R.array.guangxi;
		}else if(index == 6){
			return R.array.zhejiang;
		}else if(index == 7){
			return R.array.fujian;
		}else if(index == 8){
			return R.array.hubei;
		}else if(index == 9){
			return R.array.hunan;
		}else if(index == 10){
			return R.array.hebei;
		}else if(index == 11){
			return R.array.henan;
		}else if(index == 12){
			return R.array.sichuan;
		}else if(index == 13){
			return R.array.yunnan;
		}else if(index == 14){
			return R.array.guizhou;
		}else if(index == 15){
			return R.array.hainan;
		}else if(index == 16){
			return R.array.jiangsu;
		}else if(index == 17){
			return R.array.jiangxi;
		}else if(index == 18){
			return R.array.anhui;
		}else if(index == 19){
			return R.array.shandong;
		}else if(index == 20){
			return R.array.shanxi1;
		}else if(index == 21){
			return R.array.liaoning;
		}else if(index == 22){
			return R.array.jilin;
		}else if(index == 23){
			return R.array.shanxi2;
		}else if(index == 24){
			return R.array.gansu;
		}else if(index == 25){
			return R.array.qinghai;
		}else if(index == 26){
			return R.array.ningxia;
		}else if(index == 27){
			return R.array.xizang;
		}else if(index == 28){
			return R.array.xinjiang;
		}else if(index == 29){
			return R.array.heilongjiang;
		}else if(index == 30){
			return R.array.neimenggu;
		}else if(index == 31){
			return R.array.taiwan;
		}else if(index == 32){
			return R.array.hongkong;
		}else if(index == 33){
			return R.array.macau;
		}else{
			return -1;
		}
	}

    public static String getDateString(Long m){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");   	
    	return sdf.format(new Date(m));
    }
    
    public static int getDateLine(Long m){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");   	
    	return Integer.parseInt(sdf.format(new Date(m)));
    }
    
    public static String getShortDateString(int dateLine){
    	String month = null;
    	String day = null;
    	String year = null;
    	year = dateLine / 10000+"";
    	int month_day = dateLine % 10000;
    	if(month_day/100<10){
    		month = "0" + month_day/100;
    	}else{
    		month = String.valueOf(month_day/100);
    	}
    	if(month_day%100<10){
    		day = "0" + month_day%100;
    	}else{
    		day = String.valueOf(month_day%100);
    	}   	   	
    	return month + "-" + day ;    	
    }
    
    public static String getDateString(int dateLine){
    	String month = null;
    	String day = null;
    	String year = null;
    	year = dateLine / 10000+"";
    	int month_day = dateLine % 10000;
    	if(month_day/100<10){
    		month = "0" + month_day/100;
    	}else{
    		month = String.valueOf(month_day/100);
    	}
    	if(month_day%100<10){
    		day = "0" + month_day%100;
    	}else{
    		day = String.valueOf(month_day%100);
    	}   	   	
    	//return year+"-"+month + "-" + day ; 
    	return month + "/" + day ; 
    }

    public static String getSystemVersion2() {
		String systemVersion = android.os.Build.DISPLAY;
		if (systemVersion != null && systemVersion.endsWith("CN")) {			
				return "CN";			
		}
		return "NOR";
	}
    
    public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	 /**
	 * ����������
	 * @param ������ ˵��
	 * @return �������� ˵��
	 * @see ����/��������/��������#������
	 */
	public static int getCountyArray(int position) {
		return 0;
	}
	
}
