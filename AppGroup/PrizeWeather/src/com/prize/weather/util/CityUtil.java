
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize.weather.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prize.weather.city.CharacterParser;
import com.prize.weather.city.CityEntity;
import com.prize.weather.city.PinyinComparator;
import com.prize.weather.city.SortModel;
import com.prize.weather.db.DBManager;

/**
 **
 * 类描述：
 * @author 作者
 * @version 版本
 */
public class CityUtil {

	private static DBManager db ;
	private static Thread thread;
	public static String names[];
	public static Integer codes[];
	public static List<SortModel> SourceDateList;
	public static SortModel fs[][];
	
	public static void getAllCity(Context context) {
		db = new DBManager(context);
		thread = new Thread(r);
		thread.start();
	}

	public static Handler mHandler = new Handler() {
		public void handleMessage(Message msg){
			switch (msg.what){
			case 1000:
				names = (String[]) ((ArrayList) msg.obj).get(0);
				codes = (Integer[]) ((ArrayList) msg.obj).get(1);
				SourceDateList = (List<SortModel>) ((List) msg.obj).get(2); 
				fs = (SortModel[][]) ((ArrayList) msg.obj).get(3);
//				initSearchView();
				PinyinComparator pinyinComparator = new PinyinComparator();
				if(null != CityUtil.SourceDateList){
					Collections.sort(CityUtil.SourceDateList, pinyinComparator);					
				}
			}
		}
	};
	
	private static Runnable r = new Runnable() {
		@Override
		public void run() {
			ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryAllCitys();
			Log.d("search", "initSearchView  citys.length = " + citys.size());
			String[] names = new String[citys.size()];
			Integer[] codes = new Integer[citys.size()];
			for (int i = 0; i < citys.size(); i++) {
				names[i] = citys.get(i).getName();
				codes[i] = citys.get(i).getcCode();
			}

			Log.d("search", "names = " + names + " codes = " + codes);
			CharacterParser characterParser = CharacterParser.getInstance();;
			List<SortModel> SourceDateList = filledData(names, codes, characterParser);
			SortModel fs[][] = filterSource(SourceDateList, characterParser);
			
			Message msg = new Message();
			msg.what = 1000;
			ArrayList al = new ArrayList();
			al.add(names);
			al.add(codes);
			al.add(SourceDateList);
			al.add(fs);
			msg.obj = al;
			mHandler.sendMessage(msg);
		}
	};
	
	private static SortModel[][] filterSource(List<SortModel> SourceDateList, CharacterParser characterParser) {
		int i = 0;
		SortModel fs[][] = new SortModel[26][];
		int k0 = 0, k1 = 0, k2 = 0, k3 = 0, k4 = 0, k5 = 0, k6 = 0, k7 = 0, k8 = 0, k9 = 0, k10 = 0,
				k11 = 0, k12 = 0, k13 = 0, k14 = 0, k15 = 0, k16 = 0, k17 = 0, k18 = 0, k19 = 0, k20 = 0,
				k21 = 0, k22 = 0, k23 = 0, k24 = 0, k25 = 0;
		
		fs[0] = new SortModel[100];   //50
		fs[1] = new SortModel[200];    //92
		fs[2] = new SortModel[200];    //112
		fs[3] = new SortModel[200];    //137
		fs[4] = new SortModel[100];     //17
		fs[5] = new SortModel[150];      //87
		fs[6] = new SortModel[130];      //94
		fs[7] = new SortModel[250];      //194
		fs[8] = new SortModel[10];       //0
		fs[9] = new SortModel[200];      //148
		fs[10] = new SortModel[70];      //38
		fs[11] = new SortModel[250];     //217
		fs[12] = new SortModel[130];     //83
		fs[13] = new SortModel[130];      //84
		fs[14] = new SortModel[10];      //0
		fs[15] = new SortModel[120];      //78
		fs[16] = new SortModel[120];       //85
		fs[17] = new SortModel[70];      //34
		fs[18] = new SortModel[200];   //159
		fs[19] = new SortModel[170];    //124
		fs[20] = new SortModel[10];    //0
		fs[21] = new SortModel[10];    //0
		fs[22] = new SortModel[170];   //130
		fs[23] = new SortModel[200];   //163
		fs[24] = new SortModel[250];   //193
		fs[25] = new SortModel[310];   //267
		
		for(SortModel sortModel : SourceDateList){
			String name = sortModel.getName();
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(name);
//			String pinyin = characterParser.converterToSpell(name);
			if(pinyin.length()>1){
			String sortString = pinyin.substring(0, 1).toUpperCase(Locale.getDefault());
			char c = sortString.charAt(0);
			switch (c){
			case 'A':
//				fs[0] = new SortModel[50];
				fs[0][k0] = sortModel;
				k0++;
				break;
			case 'B':
//				fs[1] = new SortModel[92];
				fs[1][k1] = sortModel;
				k1++;
				break;
			case 'C':
//				fs[2] = new SortModel[112];
				fs[2][k2] = sortModel;
				k2++;
				break;
			case 'D':
//				fs[3] = new SortModel[137];
				fs[3][k3] = sortModel;
				k3++;
				break;
			case 'E':
//				fs[4] = new SortModel[17];
				fs[4][k4] = sortModel;
				k4++;
			break;
				case 'F':
//				fs[5] = new SortModel[87];
				fs[5][k5] = sortModel;
				k5++;
				break;
			case 'G':
//				fs[6] = new SortModel[94];
				fs[6][k6] = sortModel;
				k6++;
				break;
			case 'H':
//				fs[7] = new SortModel[194];
				fs[7][k7] = sortModel;
				k7++;
				break;
			case 'I':
//				fs[8] = new SortModel[10];
				fs[8][k8] = sortModel;
				k8++;
				break;
			case 'J':
//				fs[9] = new SortModel[148];
				fs[9][k9] = sortModel;
				k9++;
				break;
			case 'K':
//				fs[10] = new SortModel[38];
				fs[10][k10] = sortModel;
				k10++;
				break;
			case 'L':
//				fs[11] = new SortModel[217];
				fs[11][k11] = sortModel;
				k11++;
				break;
			case 'M':
//				fs[12] = new SortModel[83];
				fs[12][k12] = sortModel;
				k12++;
				break;
			case 'N':
//				fs[13] = new SortModel[84];
				fs[13][k13] = sortModel;
				k13++;
				break;
			case 'O':
//				fs[14] = new SortModel[10];
				fs[14][k14] = sortModel;
				k14++;
				break;
			case 'P':
//				fs[15] = new SortModel[78];
				fs[15][k15] = sortModel;
				k15++;
				break;
			case 'Q':
//				fs[16] = new SortModel[85];
				fs[16][k16] = sortModel;
				k16++;
				break;
			case 'R':
//				fs[17] = new SortModel[34];
				fs[17][k17] = sortModel;
				k17++;
				break;
			case 'S':
//				fs[18] = new SortModel[159];
				fs[18][k18] = sortModel;
				k18++;
				break;
			case 'T':
//				fs[19] = new SortModel[124];
				fs[19][k19] = sortModel;
				k19++;
				break;
			case 'U':
//				fs[20] = new SortModel[10];
				fs[20][k20] = sortModel;
				k20++;
				break;
			case 'V':
//				fs[21] = new SortModel[10];
				fs[21][k21] = sortModel;
				k21++;
				break;
			case 'W':
//				fs[22] = new SortModel[130];
				fs[22][k22] = sortModel;
				k22++;
				break;
			case 'X':
//				fs[23] = new SortModel[163];
				fs[23][k23] = sortModel;
				k23++;
				break;
			case 'Y':
//				fs[24] = new SortModel[193];
				fs[24][k24] = sortModel;
				k24++;
				break;
			case 'Z':
//				fs[25] = new SortModel[267];
				fs[25][k25] = sortModel;
				k25++;
				break;
				}
			}
		}
		return fs;
	}


	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private static List<SortModel> filledData(String[] date,Integer[] code , CharacterParser characterParser) {
		List<SortModel> mSortList = new ArrayList<SortModel>();
		for (int i = 0; i < date.length; i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(date[i]);
			sortModel.setCode(code[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			if(pinyin.length()>1){
			String sortString = pinyin.substring(0, 1).toUpperCase(Locale.getDefault());
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase(Locale.getDefault()));
			} else {
				sortModel.setSortLetters("#");
				}			
			}
			mSortList.add(sortModel);
		}
		return mSortList;
	}
	
}
