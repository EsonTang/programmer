package com.prize.left.page.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xutils.ex.DbException;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;

import com.android.launcher3.IconCache;
import com.android.launcher3.ImageUtils;
import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.lqsoft.LqServiceUpdater.LqService;
import com.prize.left.page.ItemViewType;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.ContactPerson;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.NormalAddrTable;
import com.prize.left.page.bean.table.PushTable;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.bean.table.SubCardType;

public class DBUtils {
	/***
	 * 查找所有已经选好可用的cardtype
	 * @return
	 */
	public static List<CardBean> findAllUsedCard(boolean hasNet) {
		StringBuilder sql = new StringBuilder(512);
		sql.append("select a.code, a.name, a.dataUrl, a.status,a.moreType,a.dataCode,a.uitype").append(", a.moreUrl")
		.append(", a.pkg")
		.append(", a.clsName")
		.append(", b._sort")
		.append(", a.subCode")
		.append(", a.needLoc")
		.append(" from t_cardType a, t_sel_cardType b where a.dataCode=b.dataCode")
		.append(" and a.status>0")
		.append(" and b.status>0")
		.append(" order by b._sort asc");
		
		List<CardBean> rs = null;
		try {
			Cursor cursor = LauncherApplication.getDbManager().execQuery(sql.toString());
			int codeIndex = cursor.getColumnIndex("code");
			int nameIndex = cursor.getColumnIndex("name");
			int dataUrlIndex = cursor.getColumnIndex("dataUrl");
			int moreTypeIndex = cursor.getColumnIndex("moreType");
			int moreUrlIndex = cursor.getColumnIndex("moreUrl");
			int pkgIndex = cursor.getColumnIndex("pkg");
			int clsNameIndex = cursor.getColumnIndex("clsName");
			int subIndex = cursor.getColumnIndex("subCode");
			int stIndex = cursor.getColumnIndex("status");
			int locIndex = cursor.getColumnIndex("needLoc");
			int dataCodeIndex = cursor.getColumnIndex("dataCode");
			int uitypeIndex = cursor.getColumnIndex("uitype");
			
			sql = null;
			rs = new ArrayList<CardBean>();
			
			while(cursor.moveToNext()) {
				
				int code = cursor.getInt(codeIndex);
				if (!hasNet && code >= ItemViewType.NEWS) {
					continue;
				}
				
				CardType c = new CardType();
				c.code = code;
				c.name = cursor.getString(nameIndex);
				c.dataUrl = cursor.getString(dataUrlIndex);
				c.moreType = cursor.getInt(moreTypeIndex);
				c.moreUrl = cursor.getString(moreUrlIndex);
				c.pkg = cursor.getString(pkgIndex);
				c.clsName = cursor.getString(clsNameIndex);
				c.subCode = cursor.getInt(subIndex);
				c.status = cursor.getInt(stIndex);
				c.dataCode = cursor.getString(dataCodeIndex);
				c.needLoc = cursor.getInt(locIndex);
				c.uitype=cursor.getString(uitypeIndex);
				CardBean cb = new CardBean();
				cb.cardType = c;
				rs.add(cb);
				c = null;
				cb = null;
			}
			cursor.close();
		} catch (DbException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	
	
	public static int disablePushView() {
		
		CardType card=null;
		int status =0;
		WhereBuilder builder =	WhereBuilder.b("uitype", " = ", IConstants.RECENT_USE_CARD_UITYPE);
		try {
			card =  LauncherApplication.getDbManager().selector(CardType.class).where(builder).findFirst();
			  if(card!=null) {
//				  LauncherApplication.getDbManager().delete(PushTable.class, WhereBuilder.b("status", "=", 1).and("uitype","="," IConstants.RECENT_USE_CARD_UITYPE"));
				  
				  status=card.status;
			  }

		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		  return status;
	}

	/***
	 * 查找所有已经选择的cardtype
	 * @return
	 */
	public static List<SelCardType> findAllSelCard() {
		List<SelCardType> rs = null;
		try {
			//rs = LauncherApplication.getDbManager().findAll(SelCardType.class);

			StringBuilder sql = new StringBuilder(512);
			sql.append("select b.* ")
			.append(" from t_cardType a, t_sel_cardType b where a.dataCode=b.dataCode")
			.append(" and a.status=1 and a.uitype != 'common' and b.status=1")
			.append(" order by b._sort asc");
			
			Cursor cursor = LauncherApplication.getDbManager().execQuery(sql.toString());
//			int codeIndex = cursor.getColumnIndex("code");
			int dataCodeIndex = cursor.getColumnIndex("dataCode");
			int nameIndex = cursor.getColumnIndex("name");
			int subIndex = cursor.getColumnIndex("subCode");
			int delIndex = cursor.getColumnIndex("canDel");
			int idIndex = cursor.getColumnIndex("id");
			
			rs = new ArrayList<SelCardType>();
			
			while(cursor.moveToNext()) {
				
//				int code = cursor.getInt(codeIndex);
				String dataCode = cursor.getString(dataCodeIndex);
				
				SelCardType c = new SelCardType();
//				c.code = code;
				c.dataCode =dataCode;
				c.name = cursor.getString(nameIndex);
				c.canDel = cursor.getInt(delIndex) == 1;
				c.subCode = cursor.getInt(subIndex);
				c.setId(cursor.getInt(idIndex));
				rs.add(c);
				c = null;
			}
			cursor.close();
		} catch (DbException e) {
			e.printStackTrace();
		}
		return rs;
	}

	/***
	 * 更新排序结果
	 * 
	 * @param selCards
	 * @return
	 */
	public static boolean updateSelCardSort(List<SelCardType> selCards) {
		if (null == selCards || selCards.size() < 1)
			return true;
		SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
		db.beginTransaction();
		try {
			String[] cols = new String[] {"_sort"};
			int sz = selCards.size();
			for(int i=0; i<sz; i++) {
				SelCardType card = selCards.get(i);
				card.sort = i + 4;
				LauncherApplication.getDbManager().update(card, cols);
			}
			db.setTransactionSuccessful();
		} 
		catch (Exception e) {
			return false;
		}
		finally {
			db.endTransaction();
		}
		return true;
	}
	
	/***
	 * 删除已经频道结果
	 * @param card
	 * @return
	 */
	public static boolean delSelCardByCode(CardBean card) {
		SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
		try {
			db.delete(new SelCardType().getTableName(), "(dataCode=? and subCode=?)", 
					new String[] {String.valueOf(card.cardType.dataCode), String.valueOf(card.cardType.subCode)});
		} 
		catch (Exception e) {
			return false;
		}
		return true;
	}
	

	private ComponentName c=new ComponentName("com.android.contacts", "com.android.contacts.activities.peopleactivity");
	/***
	 * 获取通话记录
	 * @param ctx
	 * @param num 最多获取的条数
	 */
	public static List<ContactPerson> getCallRecord(Context ctx, int num) {
		  
		
				List<ContactPerson> persons = null;
		try {
		Cursor c = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,                            
		        null, null, null, CallLog.Calls.DATE + " desc"); 
		if(c.moveToFirst()){ 
			persons = new ArrayList<ContactPerson>(num);
			int count = 0;
			
			int numIndex = c.getColumnIndex(CallLog.Calls.NUMBER);
			int typeIndex = c.getColumnIndex(CallLog.Calls.TYPE);
			int dateIndex = c.getColumnIndexOrThrow(CallLog.Calls.DATE);
			int cIndex = c.getColumnIndexOrThrow(CallLog.Calls.RAW_CONTACT_ID);
			int nameIndex = c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME);
			
			List<String> nums = new ArrayList<String>(5);
			
		    do {
		    	ContactPerson p = new ContactPerson();
		    	//号码                                                                                             
		        p.phoneNum = c.getString(numIndex);                           
		        //呼叫类型                                                                                           
		        p.type = Integer.parseInt(c.getString(typeIndex)); 
		        /*SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");                              
		        Date date = new Date(Long.parseLong(c.getString(dateIndex)));
		        //呼叫时间                                                                                           
		        String time = sfd.format(date);*/                                                                  
		        //联系人                                                                                            
		        p.name = c.getString(nameIndex);
		        p.contactId = getContactId(ctx, c.getString(cIndex));
		        String a = p.name;

				// 得到联系人头像Bitamp
				Bitmap headIco = null;
				// photoid 大于0 表示联系人有头像
				if (p.contactId>0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, p.contactId);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(ctx.getContentResolver(), uri);
					headIco = BitmapFactory.decodeStream(input);
					if(headIco!=null) {	
						 headIco = IconCache.getLqIcon(null, headIco, true, null);
					}else {
						  headIco= IconCache.getThemeIcon(cp, ImageUtils.drawableToBitmap(ctx.getDrawable(R.drawable.ico_person_head)), true, null, ctx);
					}
				}
				p.headIco = headIco;
		        
		        if (TextUtils.isEmpty(a))
		        	a = p.phoneNum;
		        if (!nums.contains(a)) {
		        	count ++;
		        	nums.add(a);
		        }
		        else
		        	continue;
		        //通话时间,单位:s                                                                                      
		        //String duration = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.DURATION));
		        persons.add(p);
		    } while(c.moveToNext() && count < num);
		    c.close();
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return persons;
	}
	
	/***
	 * 获取通话记录条数
	 * @param ctx
	 * @param num 最多获取的条数
	 */
	public static int getCallRecordNum(Context ctx, int num) {
		
		Cursor c = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,                            
		        null, null, null, CallLog.Calls.DATE + " desc");   
		int r = 0;
		if(c.moveToFirst()){ 
			r = c.getCount();
		    c.close();
		}
		return r;
	}
	/**
	 * 手机联系人条数
	 * @param ctx
	 * @param num
	 * @return
	 */
	public static int getPhoneContactsNum(Context ctx, int num) {
		
		ContentResolver resolver = ctx.getContentResolver();
		// 获取手机联系人
		Cursor c = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null,
				null, null);
		int count = 0;
		if (c != null) {
			count = c.getCount();
			c.close();
		}
		return count;
	}
	
	/**
	 * 获取contactId
	 * @param ctx
	 * @param rawContactId
	 * @return
	 */
	public static long getContactId(Context ctx, String rawContactId) {
		
		if (null == rawContactId)
			return -1;
		
		ContentResolver resolver = ctx.getContentResolver();
		
		String[] prj = new String[] {Phone.CONTACT_ID};
		// 获取手机联系人
		Cursor c = resolver.query(Phone.CONTENT_URI, prj, Phone.RAW_CONTACT_ID + "=?",
				new String[] {rawContactId}, null);
		long count = -1;
		if (c != null) {
			int index = c.getColumnIndex(Phone.CONTACT_ID);
			if (c.moveToFirst() && index > -1) {
				count = c.getLong(index);
			}
			c.close();
		}
		return count;
	}
	
	private static final String[] PHONES_PROJECTION = new String[] {  
	       Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID};
	private static ComponentName cp =new ComponentName("com.android.contacts", "com.android.contacts.activities.peopleactivity");
	/***
	 * 获取联系人信息
	 * @param ctx
	 * @param num
	 * @param ps
	 * @return
	 */
	public static List<ContactPerson> getPhoneContacts(Context ctx, int num, List<ContactPerson> ps) {
		
		ContentResolver resolver = ctx.getContentResolver();
		
		String selection = null;
		
		String[] args = null;
		if (ps != null && ps.size() > 0) {
			int sz = ps.size();
			args = new String[sz];
			selection = Phone.DISPLAY_NAME + " not in (";
			int count = 0;
			for (int i=0; i<sz; i++) {
				ContactPerson c = ps.get(i);
				List<ContactPerson> list = getQueryContacts(ctx,c.phoneNum);
				if(list != null){
					for(ContactPerson person: list){
						if(!TextUtils.isEmpty(person.name)){
							c.name = person.name;
							break;
						}
					}
				}
				if (TextUtils.isEmpty(c.name))
					continue;
				args[count] = c.name;
				if (count > 0)
					selection += ",";
				selection += "?";
				count ++;
			}
			if (count < 1) {
				selection = null;
				args = null;
			}
			else {
				String[] tp = new String[count];
				
				for (int i=0; i<count; i++) {
					tp[i] = args[i];
				}
				args = tp;
				selection += ")";
			}
		}
		
		
		
		// 获取手机联系人
		Cursor c = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, selection,
				args, null);
		
		List<ContactPerson> persons = null;
		if (c != null) {
			int count = 0;
			persons = new ArrayList<ContactPerson>(num);
			while (c.moveToNext()) {
				if (count >= num)
					break;
				// 得到手机号码
				String phoneNumber = c.getString(1);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				count ++;
				
				ContactPerson p = new ContactPerson();
				p.phoneNum = phoneNumber;
				// 得到联系人名称
				p.name = c.getString(0);
				// 得到联系人ID
				long contactid = c.getLong(3);
				p.contactId = contactid;
				// 得到联系人头像ID
				long photoid = c.getLong(2);
				// 得到联系人头像Bitamp
				Bitmap headIco = null;
				// photoid 大于0 表示联系人有头像
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					headIco = BitmapFactory.decodeStream(input);

					if(headIco!=null) {	
						

						 headIco = IconCache.getLqIcon(null, headIco, true, null);

					}else {
						headIco= IconCache.getThemeIcon(cp, ImageUtils.drawableToBitmap(ctx.getDrawable(R.drawable.ico_person_head)), true, null, ctx);
					}
				}
				p.headIco = headIco;
				persons.add(p);
			}
			c.close();
		}
		return persons;
	}
	
	/***
	 * 查询联系人信息
	 * @param ctx
	 * @param key 查询关键字
	 * @return
	 */
	public static List<ContactPerson> getQueryContacts(Context ctx, String key) {
		
		ContentResolver resolver = ctx.getContentResolver();
		
		StringBuilder selection = new StringBuilder(256);
		selection.append(Phone.NUMBER).append(" like ? ")
		.append(" or ").append(Phone.DISPLAY_NAME).append(" like ? ");
		if(key != null ){
			key = formatMobibleNo(key);
		}
		String[] args = new String[] {key, key};
		// 获取手机联系人
		
		Cursor c = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, selection.toString(),
				args, null);
		
		List<ContactPerson> persons = null;
		if (c != null) {
			persons = new ArrayList<ContactPerson>(5);
			while (c.moveToNext()) {
				// 得到手机号码
				String phoneNumber = c.getString(1);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				
				ContactPerson p = new ContactPerson();
				p.phoneNum = phoneNumber;
				// 得到联系人名称
				p.name = c.getString(0);
				// 得到联系人ID
				long contactid = c.getLong(3);
				p.contactId = contactid;
				// 得到联系人头像ID
				long photoid = c.getLong(2);
				// 得到联系人头像Bitamp
				Bitmap headIco = null;
				// photoid 大于0 表示联系人有头像
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactid);
					
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					headIco = BitmapFactory.decodeStream(input);
					if(headIco!=null) {

						headIco = IconCache.getLqIcon(null, headIco, true, null);

					}else {						 
						headIco= IconCache.getThemeIcon(cp, ImageUtils.drawableToBitmap(ctx.getDrawable(R.drawable.ico_person_head)), true, null, ctx);

					}
					
					
				}
				p.headIco = headIco;
				persons.add(p);
			}
			c.close();
		}
		args = null;
		selection = null;
		return persons;
	}
	
	/***
	 * 更新或新增住址
	 * @param addr
	 * @return
	 */
	public static boolean updateAddr(NormalAddrTable addr) {
		SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
		try {
			if (TextUtils.isEmpty(addr.userId)) {
				LauncherApplication.getDbManager().delete(NormalAddrTable.class);
				LauncherApplication.getDbManager().save(addr);
			}
			else {
				int r = db.update(addr.getTableName(), addr.toContentValues(), 
						"userId=?", new String[] {addr.userId});
				if (r<1) {
					LauncherApplication.getDbManager().save(addr);
				}
			}
		} 
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/***
	 * 更新或新增住址
	 * @param addr
	 * @return
	 */
	public static NormalAddrTable getAddr(String userId) {
		SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
		NormalAddrTable ad = null;
		try {
			if (!TextUtils.isEmpty(userId)) {
				ad = new NormalAddrTable();
				ad.userId = userId;
				Cursor c = db.query(ad.getTableName(), null, "userId=?", new String[] {userId}, null, null, null);
				if (!c.moveToFirst())
					return ad;
				ad.companyAddr = c.getString(c.getColumnIndexOrThrow("companyAddr"));
				ad.companyLan = c.getString(c.getColumnIndexOrThrow("companyLan"));
				ad.companyLon = c.getString(c.getColumnIndexOrThrow("companyLon"));
				
				ad.homeAddr = c.getString(c.getColumnIndexOrThrow("homeAddr"));
				ad.homeLan = c.getString(c.getColumnIndexOrThrow("homeLan"));
				ad.homeLon = c.getString(c.getColumnIndexOrThrow("homeLon"));
				
				//ad.userId = userId;
			}
			else {
				ad = LauncherApplication.getDbManager().findFirst(NormalAddrTable.class);
			}
		} 
		catch (Exception e) {
			return ad;
		}
		return ad;
	}
	
	/***
	 * 保存添加的频道
	 * @param channel
	 * @param isDel 是保存还是删除
	 * @return 0成功, 1 失败, 2 已经存在
	 */
	public static int updateChannel(SubCardType channel, boolean isDel) {
		try {
			SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
			SelCardType a = channel2SelCard(channel);
			String sel = " dataCode = ? and subCode = ? and canDel= 1";
			if (isDel) {
				int cc = db.delete(a.getTableName(), sel, new String[]{a.dataCode, String.valueOf(a.subCode)});
				if (cc != 0)
					return cc != 0 ? 0 : 1;
			}
			
			Cursor c = db.query(a.getTableName(), null, sel, new String[]{a.dataCode, String.valueOf(a.subCode)}, null, null, null);
			if (c.getCount() > 0) {
				c.close();
				return 2;
			}
			c.close();
			
			LauncherApplication.getDbManager().save(a);
		} 
		catch (Exception e) {
			return 1;
		}
		return 0;
	}
	/***
	 * 频道与已经卡片类型转换
	 * @param channel
	 * @return
	 */
	private static SelCardType channel2SelCard(SubCardType channel) {
		if (null == channel)
			return null;
		SelCardType a = new SelCardType();
		a.code = channel.newsType;
		a.name = channel.name;
		a.subCode = channel.code;
		return a;
	}
	
	/***
	 * 查找所有已经选择的cardtype
	 * @return
	 */
	public static List<String> findAllSelCardCode(int cardType) {
		List<String> rs = null;
		try {
			
			SelCardType t = new SelCardType();
			SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
			
			StringBuilder sql = new StringBuilder(100);
			sql.append("select subCode ")
			.append(" from " + t.getTableName())
			.append(" where code =" + cardType);
			
			Cursor c = db.rawQuery(sql.toString(), null);
			
			if (c.getCount()<1) {
				c.close();
				return rs;
			}
			
			rs = new ArrayList<String>(16);
			
			while(c.moveToNext()) {
				rs.add(c.getString(0));
			}
			
			c.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/***
	 * 查找某个cardType下所有的频道
	 * @return
	 */
	public static List<SubCardType> findAllSubCode(int cardType) {
		List<SubCardType> rs = null;
		try {
			
			SubCardType t = new SubCardType();
			SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
			
			StringBuilder sql = new StringBuilder(80);
			sql.append("select * ")
			.append(" from " + t.getTableName())
			.append(" where newsType =" + cardType);
			
			Cursor c = db.rawQuery(sql.toString(), null);
			
			t = null;
			
			if (c.getCount()<1) {
				c.close();
				return rs;
			}
			
			rs = new ArrayList<SubCardType>(10);
			
			int index1 = c.getColumnIndex("code");
			int index2 = c.getColumnIndex("name");
			int index3 = c.getColumnIndex("newsType");
			int index4 = c.getColumnIndex("id");
			
			while(c.moveToNext()) {
				SubCardType s = new SubCardType();
				s.code = c.getInt(index1);
				s.setId(c.getInt(index4));
				s.name = c.getString(index2);
				s.newsType = c.getInt(index3);
				rs.add(s);
			}
			
			c.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/***
	 * 更新已选卡片排序号
	 * @param card
	 * @return
	 */
	public static boolean updateCardsSort(List<CardBean> cards) {
		SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
		db.beginTransaction();
		try {
			int sz = cards.size();
			SelCardType tt = new SelCardType();
			ContentValues cv = new ContentValues();
			for (int i=0; i < sz; i++) {
				CardBean card = cards.get(i);
				cv.put("_sort", i + 3);
				db.update(tt.getTableName(), cv, "(dataCode=?)", 
						new String[] {String.valueOf(card.cardType.dataCode)});
			}
			db.setTransactionSuccessful();
			tt = null;
			cv = null;
		} 
		catch (Exception e) {
			return false;
		}
		finally {
			db.endTransaction();
		}
		return true;
	}
	
	/**
	 * 手机号码格式化
	 * @param mobibles
	 * @return
	 */
	private static String formatMobibleNo(String mobibles){
		StringBuilder builder = new StringBuilder();
		if(mobibles != null) {
			char[] charArray = mobibles.toCharArray();
			builder.append("%");
			for (int i = 0; i < charArray.length; i++) {
				builder.append(charArray[i]);
				 builder.append("%");
			}
			return builder.toString();
			}
		return null;
		
	} 
}
