package com.android.calendar.hormonth;

import com.android.calendar.Utils;
import com.android.calendar.Lunar.LunarCalendarUtil;

import android.content.Context;
import android.text.format.Time;

public class MonthUtils {

	private LunarCalendarUtil mLunarUtil;

	private Context mContext;

	// When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
	protected int mFirstDayOfWeek;

	private final static int WeekSize = 7;

	public MonthUtils(Context mContext) {
		mLunarUtil = LunarCalendarUtil.getInstance(mContext);

		mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
	}

	public MonthDataDay getMonthDayData(Time time) {

		MonthDataDay mMonthDataDay = new MonthDataDay();

		mMonthDataDay.MonthDayData = new int[42];

		mMonthDataDay.MonthLunarData = new String[42];

		Time FristNextMonthTime = new Time();

		FristNextMonthTime.set(1, time.month + 1, time.year);

		FristNextMonthTime.normalize(true);

		Time FristCurrentMonthTime = new Time();

		FristCurrentMonthTime.set(1, time.month, time.year);

		FristCurrentMonthTime.normalize(true);

		Time FristShowCurrentMonthTime = new Time();

		FristShowCurrentMonthTime = getMonthShowFirstDayTime(FristCurrentMonthTime);

		for (int i = 0; i < 42; i++) {

			if (FristShowCurrentMonthTime.monthDay != FristCurrentMonthTime.monthDay
					&& (FristShowCurrentMonthTime.monthDay + i) <= FristShowCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY)) {
				mMonthDataDay.MonthDayData[i] = FristShowCurrentMonthTime.monthDay
						+ i;
				mMonthDataDay.MonthLunarData[i] = mLunarUtil
						.getFestivalChineseString(
								FristShowCurrentMonthTime.year,
								FristShowCurrentMonthTime.month + 1,
								mMonthDataDay.MonthDayData[i]);
			} else if (FristShowCurrentMonthTime.monthDay == FristCurrentMonthTime.monthDay
					&& (FristShowCurrentMonthTime.monthDay + i) <= FristShowCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY)) {
				mMonthDataDay.MonthDayData[i] = FristShowCurrentMonthTime.monthDay
						+ i;
				mMonthDataDay.MonthLunarData[i] = mLunarUtil
						.getFestivalChineseString(
								FristShowCurrentMonthTime.year,
								FristShowCurrentMonthTime.month + 1,
								mMonthDataDay.MonthDayData[i]);
			} else if (FristShowCurrentMonthTime.monthDay != FristCurrentMonthTime.monthDay
					&& (FristShowCurrentMonthTime.monthDay + i) > FristShowCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY)
					&& (FristShowCurrentMonthTime.monthDay + i) <= (FristShowCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY) + FristCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY))) {
				mMonthDataDay.MonthDayData[i] = (FristShowCurrentMonthTime.monthDay - FristShowCurrentMonthTime
						.getActualMaximum(Time.MONTH_DAY)) + i;
				mMonthDataDay.MonthLunarData[i] = mLunarUtil
						.getFestivalChineseString(FristCurrentMonthTime.year,
								FristCurrentMonthTime.month + 1,
								mMonthDataDay.MonthDayData[i]);
			} else if (FristShowCurrentMonthTime.monthDay == FristCurrentMonthTime.monthDay
					&& (FristShowCurrentMonthTime.monthDay + i) > FristShowCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY)) {
				mMonthDataDay.MonthDayData[i] = (FristShowCurrentMonthTime.monthDay - FristShowCurrentMonthTime
						.getActualMaximum(Time.MONTH_DAY)) + i;
				mMonthDataDay.MonthLunarData[i] = mLunarUtil
						.getFestivalChineseString(FristNextMonthTime.year,
								FristNextMonthTime.month + 1,
								mMonthDataDay.MonthDayData[i]);
			} else if (FristShowCurrentMonthTime.monthDay != FristCurrentMonthTime.monthDay
					&& (FristShowCurrentMonthTime.monthDay + i) > (FristShowCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY) + FristCurrentMonthTime
							.getActualMaximum(Time.MONTH_DAY))) {
				mMonthDataDay.MonthDayData[i] = FristShowCurrentMonthTime.monthDay
						- (FristShowCurrentMonthTime
								.getActualMaximum(Time.MONTH_DAY) + FristCurrentMonthTime
								.getActualMaximum(Time.MONTH_DAY)) + i;
				mMonthDataDay.MonthLunarData[i] = mLunarUtil
						.getFestivalChineseString(FristNextMonthTime.year,
								FristNextMonthTime.month + 1,
								mMonthDataDay.MonthDayData[i]);
			}
		}

		return mMonthDataDay;
	}

	public String getMonthShowLunar(int year, int month, int day) {
		String string = null;
		string = mLunarUtil.getFestivalChineseString(year, month, day);
		return string;
	}

	private Time getMonthShowFirstDayTime(Time MonthShowTime) {
		Time time = new Time();
		time.set(1, MonthShowTime.month, MonthShowTime.year);
		time.normalize(true);
		int juLianDay = Utils.getJulianDayInGeneral(time, false);

		juLianDay -= ((WeekSize + time.weekDay - mFirstDayOfWeek) % WeekSize);

		time = new Time();
		time.setJulianDay(juLianDay);
		time.normalize(true);
		return time;
	}

}
