package com.android.calendar.hormonth;

import java.io.Serializable;

public class CustomDate implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public int year=0;
	public int month=0;
	public int day=0;
	public int week=0;
	DateUtil mDateUtil = DateUtil.dateUtil;
	public String LinearString="" ;
	
	private int mLunarMDType;
	
	public int geLunarMDType() {
		return mLunarMDType;
	}
	
	public void setLunarMDType(int mLunarMDType) {
		this.mLunarMDType = mLunarMDType;
	}
	
	public CustomDate(int year,int month,int day){
		if(month > 12){
			month = 1;
			year++;
		}else if(month <1){
			month = 12;
			year--;
		}
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public CustomDate(){
		this.year = mDateUtil.getYear();
		this.month = mDateUtil.getMonth();
		this.day = mDateUtil.getCurrentMonthDay();
	}
	
	public static CustomDate modifiDayForObject(CustomDate date,int day){
		CustomDate modifiDate = new CustomDate(date.year,date.month,day);
		return modifiDate;
	}
	
	public void setLinear(String LinearString){
		this.LinearString = LinearString;
	}
	
	@Override
	public String toString() {
		return year+"-"+month+"-"+day;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

}
