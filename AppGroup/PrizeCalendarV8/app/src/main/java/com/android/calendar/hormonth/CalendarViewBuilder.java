package com.android.calendar.hormonth;

import android.content.Context;
import android.text.format.Time;

import com.android.calendar.EventLoader;
import com.android.calendar.hormonth.CalendarView.CallBack;

public class CalendarViewBuilder {

		private CalendarView[] calendarViews;

		public  CalendarView[] createMassCalendarViews(Context context,int count,int style,CallBack callBack,Time mTime,EventLoader mEventLoader){
			calendarViews = new CalendarView[count];
			for(int i = 0; i < count;i++){
				calendarViews[i] = new CalendarView(context, style,callBack,mTime,i,mEventLoader);
			}
			return calendarViews;
		}
		
		public  CalendarView[] createMassCalendarViews(Context context,int count,CallBack callBack,Time mTime , EventLoader mEventLoader){
			
			return createMassCalendarViews(context, count, CalendarView.MONTH_STYLE,callBack,mTime,mEventLoader);
		}
		
//		/**
//		 *
//		 * @param style
//		 */
//		public void swtichCalendarViewsStyle(int style){
//			if(calendarViews != null)
//			for(int i = 0 ;i < calendarViews.length;i++){
//				calendarViews[i].switchStyle(style);
//			}
//		}
//		/**
//		 * CandlendarView
//		 */
//		
//		public void backTodayCalendarViews(){
//			if(calendarViews != null)
//			for(int i = 0 ;i < calendarViews.length;i++){
//				calendarViews[i].backToday();
//			}
//		}
}
