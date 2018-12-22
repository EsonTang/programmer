package com.android.prize.simple.model;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.android.launcher3.R;

/**
 *#Lunar#
 * A Util class  for Lunar
 *
 */
public class LunarCalendarUtil {
    private static final String TAG = "LunarUtil";

    public static final int LEAP_MONTH = 0;
    public static final int NORMAL_MONTH = 1;
    public static final int DECREATE_A_LUANR_YEAR = -1;
    public static final int INCREASE_A_LUANR_YEAR = 1;
    public static final String DELIM = ";";

    ///M: these strings are inited in constructor @{
    private String[] mMonthNumberArray;
    private String[] mTensPrefixArray;
    private String mLunarTextLeap;
    private String mLunarTextTensDay;
	private String mLunarTextTwentithDay;
    private String mLunarTextThirtiethDay;
    private String mLunarTextYear;
    private String mLunarTextMonth;
    private String mLunarTextDay;

    private String mLunarFestCHUNJIE;
    private String mLunarFestDUANWU;
    private String mLunarFestZHONGQIU;
    private String mLunarFestYUANDAN;
    private String mLunarFestLAODONG;
    private String mLunarFestGUOQING;
    private String mLunarFestYUANXIAO;
    private String mLunarFestQIXI;
    private String mLunarFestCHONGYANG;
    private String mLunarFestQINGNIAN;
    private String mLunarFestQINGREN;
    private String mLunarFestFUNV;
    private String mLunarFestZHISHU;
    private String mLunarFestYUREN;
    private String mLunarFestERTONG;
    private String mLunarFestJIANDANG;
    private String mLunarFestJIANJUN;
    private String mLunarFestJIAOSHI;
    private String mLunarFestSHENGDAN;
    ///@}

    private static final int LUNAR2GRE_START_CHECK_DAY = 400;

    /// M: an index refer to word "闰".
    private static final int LUNAR_WORD_RUN = 1;

    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int MONTH_DAY = 2;

    /**生肖数组*/
    private String[] mShenXiaoArray;
    /**
     * get a lunar day's chnese String.
     * @param lunarDay the number of which day
     * @return the chnese string that the luanr day corresponded. like:初二,初二三.
     */
    public String getLunarDayString(int lunarDay) {
        int n = lunarDay % 10 == 0 ? 9 : lunarDay % 10 - 1;
        if (lunarDay < 0 || lunarDay > 30) {
            return "";
        }

        String ret;
        switch (lunarDay) {
        case 10:
            ret = mLunarTextTensDay;
            break;
        case 20:
            ret = mLunarTextTwentithDay;
            break;
        case 30:
            ret = mLunarTextThirtiethDay;
            break;
        default:
            ret = mTensPrefixArray[lunarDay / 10] + mMonthNumberArray[n];
            break;
        }

        return ret;
    }

    /**
     *get the lunar date string by calendar
     * @param cal   Gregorian calendar objectw
     * @return   the lunar date string like:xx年[闰]xx月初xx
     */
    public String getLunarDateString(Calendar cal) {
        int gregorianYear = cal.get(Calendar.YEAR);
        int gregorianMonth = cal.get(Calendar.MONTH) + 1;
        int gregorianDay = cal.get(Calendar.DAY_OF_MONTH);

        int lunarDate[] = mLunarAlgorithm.calculateLunarByGregorian(gregorianYear, gregorianMonth,
                gregorianDay);

        return getLunarDateString(lunarDate[0], lunarDate[1], lunarDate[2], lunarDate[3]);
    }

    /**
     * get the lunar date string,like xx年[闰]xx月初xx
     *
     * @param gregorianYear
     * @param gregorianMonth
     * @param gregorianDay
     * @return the lunar date string like:xx年[闰]xx月初xx
     */
    public String getLunarDateString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        int lunarDate[] = mLunarAlgorithm.calculateLunarByGregorian(gregorianYear, gregorianMonth,
                gregorianDay);
        return getLunarDateString(lunarDate[0], lunarDate[1], lunarDate[2], lunarDate[3]);
    }

    /**
     * The really function produce lunar date string.
     * @param lunarYear
     * @param lunarMonth
     * @param lunarDay
     * @param leapMonthCode  LEAP_MONTH or NORMAL_MONTH
     * @return the lunar date string like:xx年[闰]xx月初xx
     */
    private String getLunarDateString(int lunarYear, int lunarMonth, int lunarDay, int leapMonthCode) {
        /// M: If the leapMonthCode is LEAP_MONTH show special word by getSpecialWord function, because
        //  should show "閏" in TC, "闰" in SC.
        String luanrDateString = lunarYear + mLunarTextYear
                + (leapMonthCode == LEAP_MONTH ? getSpecialWord(LUNAR_WORD_RUN) : "")
                + mMonthNumberArray[lunarMonth - 1] + mLunarTextMonth + getLunarDayString(lunarDay);
        return luanrDateString;
    }

    public String getFestivalChineseString(int gregorianYear, int gregorianMonth, int gregorianDay){
    	String chineseString = null;

        chineseString = getGregFestival(gregorianMonth, gregorianDay);
        
        if(chineseString!=null){
        	return chineseString;
        }
        int lunarDate[] = mLunarAlgorithm.calculateLunarByGregorian(gregorianYear, gregorianMonth,
                gregorianDay);
        
        chineseString = getLunarFestival(lunarDate[1], lunarDate[2], lunarDate[3]);
        
        if(chineseString!=null){
        	return chineseString;
        }
        
        if (!TextUtils.isEmpty(chineseString)) {
          return chineseString;
        }
        if (chineseString == null || chineseString.length() == 0) {
            boolean isLeapMonth = lunarDate[3] == LEAP_MONTH ? true : false;
            return getLunarNumber(lunarDate[1], lunarDate[2], isLeapMonth);
        } else {
            return chineseString.toString();
        }
    }
    
    /**
     * Change given year.month.day to Chinese string. Festival, SolarTerm, or
     * Chinese number.
     * in this method, the Lunar state is force updated to the
     * transfered lunar date.
     * @param gregorianYear
     * @param gregorianMonth
     * @param gregorianDay
     * @return lunar festival chinese string,
     */
    public String getLunarFestivalChineseString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        StringBuilder chineseStringBuilder = new StringBuilder();
        String chineseString = null;

        chineseString = getGregFestival(gregorianMonth, gregorianDay);
      
        if (!TextUtils.isEmpty(chineseString)) {
            chineseStringBuilder.append(chineseString).append(DELIM);
        }
        int lunarDate[] = mLunarAlgorithm.calculateLunarByGregorian(gregorianYear, gregorianMonth,
                gregorianDay);
        
        chineseString = getLunarFestival(lunarDate[1], lunarDate[2], lunarDate[3]);
        
       
        if (!TextUtils.isEmpty(chineseString)) {
            chineseStringBuilder.append(chineseString).append(DELIM);
        }
       
        if (!TextUtils.isEmpty(chineseString)) {
            chineseStringBuilder.append(chineseString).append(DELIM);
        }
        if (chineseStringBuilder.length() == 0) {
            boolean isLeapMonth = lunarDate[3] == LEAP_MONTH ? true : false;
            return getLunarNumber(lunarDate[1], lunarDate[2], isLeapMonth);
        } else {
            return chineseStringBuilder.toString();
        }
    }

    /**
     * get the current Lunar day number
     * @param lunarDay
     * @return the string as the lunar number day.
     */
    private String getLunarNumber(int lunarMonth, int lunarDay, boolean isLeapMonth) {
        // The first day of each month will display like X月 or 闰X
        if (lunarDay == 1) {
            if (isLeapMonth) {
                /// M: If the leapMonthCode is LEAP_MONTH show special word by
                // getSpecialWord function, because should show "閏" in TC, "闰" in SC.
                return getSpecialWord(LUNAR_WORD_RUN) + mMonthNumberArray[lunarMonth - 1];
            }
            return mMonthNumberArray[lunarMonth - 1] + mLunarTextMonth;
        }
        return getLunarDayString(lunarDay);
    }

    /**
     * This Extension is for the festivals display.
     * typically, the TC lunar will have different text displayed from SC Lunar.
     */

    private LunarCalenarAlgorithm mLunarAlgorithm;

    /**
     * Constructor
     * @param context the context is for looking up via PluginManager
     */
    private LunarCalendarUtil(Context context) {
        Log.d(TAG, "LunarUtil context = " + context);
     
        mLunarAlgorithm = new LunarCalenarAlgorithm(context);
        Resources res = context.getResources();
        initArrays(res);
        initStrings(res);
    }

    private void initArrays(Resources res) {
        mMonthNumberArray = res.getStringArray(R.array.prize_month_number_array);
        mTensPrefixArray = res.getStringArray(R.array.prize_tens_prefix_array);
        
        mShenXiaoArray = res.getStringArray(R.array.prize_shen_xiao);
    }

    private void initStrings(Resources res) {
        mLunarTextYear = res.getString(R.string.prize_lunar_year);
        mLunarTextMonth = res.getString(R.string.prize_lunar_month);
        mLunarTextDay = res.getString(R.string.prize_lunar_day);
    	mLunarFestCHUNJIE = res.getString(R.string.prize_lunar_fest_chunjie);
        mLunarFestDUANWU = res.getString(R.string.prize_lunar_fest_duanwu);
        mLunarFestZHONGQIU = res.getString(R.string.prize_lunar_fest_zhongqiu);
        mLunarFestYUANDAN = res.getString(R.string.prize_lunar_fest_yuandan);
        mLunarFestLAODONG = res.getString(R.string.prize_lunar_fest_laodong);
        mLunarFestGUOQING = res.getString(R.string.prize_lunar_fest_guoqing);
        mLunarFestYUANXIAO = res.getString(R.string.prize_lunar_fest_yuanxiao);
        mLunarFestQIXI = res.getString(R.string.prize_lunar_fest_qixi);
        mLunarFestCHONGYANG = res.getString(R.string.prize_lunar_fest_chongyang);
        mLunarFestQINGNIAN = res.getString(R.string.prize_lunar_fest_qingnian);
        mLunarFestQINGREN = res.getString(R.string.prize_lunar_fest_qingren);
        mLunarFestFUNV = res.getString(R.string.prize_lunar_fest_funv);
        mLunarFestZHISHU = res.getString(R.string.prize_lunar_fest_zhishu);
        mLunarFestYUREN = res.getString(R.string.prize_lunar_fest_yuren);
        mLunarFestERTONG = res.getString(R.string.prize_lunar_fest_ertong);
        mLunarFestJIANDANG = res.getString(R.string.prize_lunar_fest_jiandang);
        mLunarFestJIANJUN = res.getString(R.string.prize_lunar_fest_jianjun);
        mLunarFestJIAOSHI = res.getString(R.string.prize_lunar_fest_jiaoshi);
        mLunarFestSHENGDAN = res.getString(R.string.prize_lunar_fest_shengdan);
        
        mLunarTextLeap = res.getString(R.string.prize_lunar_leap);
        mLunarTextTensDay = res.getString(R.string.prize_lunar_tenth_day);
        mLunarTextTwentithDay = res.getString(R.string.prize_lunar_twentieth_day);
        mLunarTextThirtiethDay = res.getString(R.string.prize_lunar_thirtieth_day);
    }

    /**
     * Whether Lunar calendar can be shown in current system env.
     * @return true if yes.
     */
    public boolean canShowLunarCalendar() {
        return canShowSCLunar() ? true : true ? true : false;
    }

    /**
     * M: judge whether a day is a lunar festival
     * @param lunarMonth
     * @param lunarDay
     * @param lunarMonthType lunar month type, is leap?
     * @return festival text
     */
    private String getLunarFestival(int lunarMonth, int lunarDay, int lunarMonthType) {
        if (LEAP_MONTH == lunarMonthType) {
            return null;
        }
        if (canShowSCLunar()) {
            if ((lunarMonth == 1) && (lunarDay == 1)) {
                return mLunarFestCHUNJIE;
            } else if ((lunarMonth == 5) && (lunarDay == 5)) {
                return mLunarFestDUANWU;
            } else if ((lunarMonth == 8) && (lunarDay == 15)) {
                return mLunarFestZHONGQIU;
            } else if ((lunarMonth == 1) && (lunarDay == 15)) {
                return mLunarFestYUANXIAO;
            } else if ((lunarMonth == 7) && (lunarDay == 7)) {
                return mLunarFestQIXI;
            } else if ((lunarMonth == 9) && (lunarDay == 9)) {
                return mLunarFestCHONGYANG;
            }
        }

        return null;
    }


    /**
     * M: if the date is a greg festival, return the text, or null if not
     * @param gregorianMonth
     * @param gregorianDay
     * @return text or null
     */
    public String getGregFestival(int gregorianMonth, int gregorianDay) {
    	
        if (canShowSCLunar()) {
            if ((gregorianMonth == 1) && (gregorianDay == 1)) {
                return mLunarFestYUANDAN;
            }
            if (gregorianMonth == 5) {
                if (gregorianDay == 1) {
                    return mLunarFestLAODONG;
                } else if (gregorianDay == 4) {
                    return mLunarFestQINGNIAN;
                }
            }
            if ((gregorianMonth == 10) && (gregorianDay == 1)) {
                return mLunarFestGUOQING;
            }
            if ((gregorianMonth == 2) && (gregorianDay == 14)) {
                return mLunarFestQINGREN;
            }
            if (gregorianMonth == 3) {
                if (gregorianDay == 8) {
                    return mLunarFestFUNV;
                } else if (gregorianDay == 12) {
                    return mLunarFestZHISHU;
                }
            }
            if ((gregorianMonth == 4) && (gregorianDay == 1)) {
                return mLunarFestYUREN;
            }
            if ((gregorianMonth == 6) && (gregorianDay == 1)) {
                return mLunarFestERTONG;
            }
            if ((gregorianMonth == 7) && (gregorianDay == 1)) {
                return mLunarFestJIANDANG;
            }
            if ((gregorianMonth == 8) && (gregorianDay == 1)) {
                return mLunarFestJIANJUN;
            }
            if ((gregorianMonth == 9) && (gregorianDay) == 10) {
                return mLunarFestJIAOSHI;
            }
            if ((gregorianMonth == 12) && (gregorianDay == 25)) {
                return mLunarFestSHENGDAN;
            }
        }

        return null;
    }

    /**
     * M: if the Locale is SC chinese, lunar can show
     * @return true if can
     */
    private boolean canShowSCLunar() {
        return Locale.SIMPLIFIED_CHINESE.equals(Locale.getDefault());
    }

    private static LunarCalendarUtil sInstance;
    public static LunarCalendarUtil getInstance(Context context) {

            Log.d(TAG, "getInstance null == sInstance new LunarUtil");
            sInstance = new LunarCalendarUtil(context);
 
        return sInstance;
    }

    /**
     * M: Get the special word, If local language is SC, return the needed word in SC,
     * else return the TC word.
     * @param index refer to the special word.
     * @return the word needed,like: "闰" need by LUNAR_WORD_RUN in SC, "閏" in TC.
     */
    public String getSpecialWord(int index) {
        if (canShowSCLunar()) {
            if (index == LUNAR_WORD_RUN) {
                return mLunarTextLeap;
            }
        }
        return null;
    }

    /***
     * 获取农历年的生肖
     * @param y 公历年
     * @param m 公历月
     * @param d 公历日
     * @return
     */
    public String animalsYear(int y, int m, int d) {
    	int[] r = mLunarAlgorithm.calculateLunarByGregorian(y, m, d);
        return mShenXiaoArray[(r[0] - 4) % 12];
    }
    /**
     * get lunar day string of gregorian date.
     * @param gregorianYear
     * @param gregorianMonth (1-12)
     * @param gregorianDay (1-31)
     * @return lunar day string,
     */
    public String getLunarDay(int gregorianYear, int gregorianMonth, int gregorianDay) {

        int lunarDate[] = mLunarAlgorithm.calculateLunarByGregorian(gregorianYear, gregorianMonth,
                gregorianDay);
        boolean isLeapMonth = lunarDate[3] == LEAP_MONTH ? true : false;
        return getLunarNumber(lunarDate[1], lunarDate[2], isLeapMonth);
    }

    /**
     * get lunar year, month and day string.
     *
     * @return ArrayList<String> is 3 elements array, position 0(YEAR) is year
     *         string, position 1(MONTH) is month string, position 2(MONTH_DAY)
     *         is day String.
     */
    public ArrayList<String> getLunarYMD(int gregorianYear, int gregorianMonth, int gregorianDay) {

        int lunarDate[] = mLunarAlgorithm.calculateLunarByGregorian(gregorianYear, gregorianMonth,
                gregorianDay);
        int lunarMonth = lunarDate[1];
        int lunarDay = lunarDate[2];
        boolean isLeapMonth = lunarDate[3] == LEAP_MONTH ? true : false;
        ArrayList<String> lunarString = new ArrayList<String>(2);
        lunarString.add(YEAR, String.valueOf(lunarDate[0]));
        // The first day of each month will display like X月 or 闰X
        if (isLeapMonth) {
            // If the leapMonthCode is LEAP_MONTH show special word by
            // getSpecialWord function, because should show "閏" in TC, "闰" in
            // SC.
            lunarString.add(MONTH, getSpecialWord(LUNAR_WORD_RUN) + mMonthNumberArray[lunarMonth - 1]);
        } else {
            lunarString.add(MONTH, mMonthNumberArray[lunarMonth - 1] + mLunarTextMonth);
        }

        lunarString.add(MONTH_DAY, getLunarDayString(lunarDay));
        return lunarString;
    }

    /**
     * get lunar month and monthDay string.
     * @param gregorianYear
     * @param gregorianMonth (1-12)
     * @param gregorianDay (1-31)
     * @return String with the month and monthDay info.
     */
    public String getLunarMDString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        ArrayList<String> lunarArray = getLunarYMD(gregorianYear, gregorianMonth, gregorianDay);
        String lunarString = lunarArray.get(MONTH) + lunarArray.get(MONTH_DAY);
        return lunarString ;
    }

    public String formatLunarDateRange(Context context, Time startDate, Time endDate, boolean showDateRange) {
        ArrayList<String> lunarStartDate = getLunarYMD(startDate.year, startDate.month + 1, startDate.monthDay);
        ArrayList<String> lunarEndDate = getLunarYMD(endDate.year, endDate.month + 1, endDate.monthDay);

        Time current = new Time(startDate.timezone);
        current.setToNow();
        int currentYear = current.year;
        int startYear = Integer.valueOf(lunarStartDate.get(YEAR));
        int endYear = Integer.valueOf(lunarEndDate.get(YEAR));
        boolean showYear = false;
        if (startDate.year != startYear || endDate.year != endYear
                || startDate.year != endDate.year
                || startYear != endYear
                || startYear != currentYear || endYear != currentYear) {
            showYear = true;
        }

        // if the start time and end time is in one day, then show date only.
        if (!showDateRange) {
            if (canShowSCLunar()) {
                return context.getString(R.string.prize_lunar_detail_info_fmt1, (showYear ? startYear + mLunarTextYear : "")
                        + lunarStartDate.get(MONTH) + lunarStartDate.get(MONTH_DAY));
            } else {
                return null;
            }

        }

        String lunarStartDateStr = "";
        String lunarEndDateStr = "";
        if (showYear) {
            lunarStartDateStr = startYear + mLunarTextYear;
            lunarEndDateStr = endYear + mLunarTextYear;
        }

        lunarStartDateStr += lunarStartDate.get(MONTH) + lunarStartDate.get(MONTH_DAY);
        lunarEndDateStr += lunarEndDate.get(MONTH) + lunarEndDate.get(MONTH_DAY);

        if (canShowSCLunar()) {
            return context.getString(R.string.prize_lunar_detail_info_fmt2, lunarStartDateStr, lunarEndDateStr);
        } else {
            return null;
        }

    }

    /**
     * build lunar date according a specific date
     * @param date if it is null, timezone and milliTime must be assigned
     * @return lunar date string
     */
    public String buildLunarDate(Context context, Time date, String timeZone, long milliTime) {
        String lunarDate = "";
        if (canShowLunarCalendar()) {
            if (date == null) {
                date = new Time(timeZone);
                date.set(milliTime);
            }
            lunarDate = getLunarMDString(date.year, date.month + 1, date.monthDay);
            lunarDate = context.getString(R.string.prize_lunar_info_fmt, lunarDate);
        }
        return lunarDate;
    }

    /**
     * get lunar date for startMillis and endMillis,then format them
     * @return lunar date string or lunar date string range which can be displayed directly
     */
    public String getLunarDisplayedDate(Context context, String localTimezone, long startMillis,
            long endMillis, boolean allDay) {
        if (!canShowLunarCalendar()) {
            return "";
        }
        Time startDate = new Time(localTimezone);
        startDate.set(startMillis);
        startDate.normalize(false);
        Time endDate = new Time(localTimezone);
        endDate.set(endMillis);
        if (allDay) {
            endDate.monthDay -= 1;
        }
        endDate.normalize(false);

        boolean showDateRange = true;
        if ((startDate.year == endDate.year) && (startDate.month == endDate.month)
                && (startDate.monthDay == endDate.monthDay)) {
            showDateRange = false;
            // if the date is today, no need to display lunar info.
            if (isToday(startMillis, System.currentTimeMillis(), startDate.gmtoff,
                    allDay, localTimezone)) {
                return "";
            }
        }

        return formatLunarDateRange(context, startDate, endDate, showDateRange);
    }

    private boolean isToday(long dayMillis, long currentMillis, long localGmtOffset, boolean allday,
            String localTimezone) {
        if (allday) {
            // All day events require special timezone adjustment.
            dayMillis = convertAlldayUtcToLocal(null, dayMillis, localTimezone);
        }
        int startDay = Time.getJulianDay(dayMillis, localGmtOffset);
        int currentDay = Time.getJulianDay(currentMillis, localGmtOffset);
        int days = startDay - currentDay;
        if (days == 0) {
            return true;
        }
        return false;
    }

    private long convertAlldayUtcToLocal(Time recycle, long utcTime, String tz) {
        if (recycle == null) {
            recycle = new Time();
        }
        recycle.timezone = Time.TIMEZONE_UTC;
        recycle.set(utcTime);
        recycle.timezone = tz;
        return recycle.normalize(true);
    }
}
