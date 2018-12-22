package com.example.longshotscreen.ui;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.example.longshotscreen.utils.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.longshotscreen.R;
import com.example.longshotscreen.SuperShotApp;

public class GossipView extends View {

	public class Point {
		public float x;
		public float y;   	 
		public Point (float x , float y){
			this.x = x;
			this.y = y;
		}
	} 

	public interface OnPieceClickListener {
		void onPieceClick(int whitchPiece);
	}

	private static final String TAG = "com.jcodecraeer.gossipview";
	private RectF mBigArcRectangle = new RectF();
	private RectF mOuterArcRectangle = new RectF();
	private RectF mInnerArcRectangle = new RectF();
	private float mBigArcRadius; //syc add for big
	private float mOuterArcRadius;
	private float mInnerArcRadius;
	private Paint mBigArcPaint;//syc add for big
	private Paint mOuterArcPaint;
	private Paint mInnerArcPaint;

	private Paint mOuterTextMovePaint;
	private Paint mOuterTextFunnyPaint;
	private Paint mOuterTextLongPaint;
	private Paint mOuterTextRecorderPaint;
	//private boolean mMoveActionDown = false;
	private boolean mFunnyActionDown = false;
	private boolean mLongActionDown = false;
	private boolean mRecorderActionDown = false;

	private Paint mNumberTextPaint;
	//private Paint mProgressPaint;
	private float outArctrokeWidth;
	private float mInnerArctrokeWidth;
	private int mPieceNumber = 4;
	private int mPieceDegree = 360/mPieceNumber;
	private int mDividerDegree = 0;
	private int mWidth;
	private Drawable mInnerBackGroud;
	private Drawable mOuterBackGroud;
	private Drawable mBigBackGroud;//syc add for big
	public int mSelectIndex = -2;
	private int mNumber;

	private int[] outArcColor = {0x7f0ccec3,0x7fb7db10,0x7f22abff,0x7fffae00};
	private int[] bigArcColor = {0xff0ccec3,0xffb7db10,0xff22abff,0xffffae00}; 
	
	private Context mContext;
	private SweepGradient mSweepGradient;
	private int overTouchDistance = MyUtils.dip2px(getContext(), 15); //Distance, we expand peripheral button click effect
	private int progressAnimateStartAngle = 0; //The animation
	
	private int padding = MyUtils.dip2px(getContext(), 0) + 10; 

	private List<GossipItem> items;
	private static int HOME_NUMBER_TEXT_SIZE = 25;
	private static float mScale = 0; // Used for supporting different screen densities
	private OnPieceClickListener mListener;
	public GossipView(Context context) {
		super(context);
		init(context , null, 0);
	}

	public GossipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context , attrs , 0);
	}

	public GossipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context , attrs , defStyle);
	}


	private void init(Context c , AttributeSet attrs , int defStyle) {

		mContext = c;

		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("syc.syc.com.move.button");
	    mContext.registerReceiver(mReceiver, intentFilter);
	       
		if (mScale == 0) {
			mScale = getContext().getResources().getDisplayMetrics().density;
			//Log.i(TAG, "mScale = " + mScale);
			if (mScale != 1) {
				HOME_NUMBER_TEXT_SIZE *= mScale;
			}
		}
		//syc add for big
		mBigArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBigArcPaint.setAntiAlias(true);
		mBigArcPaint.setStyle(Paint.Style.STROKE);
		
		mOuterArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mOuterArcPaint.setAntiAlias(true);
		mOuterArcPaint.setStyle(Paint.Style.STROKE);

		mInnerArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mInnerArcPaint.setAntiAlias(true);
		mInnerArcPaint.setStyle(Paint.Style.STROKE);
		mInnerArcPaint.setColor(0xfff39700);

		//文字绘制笔
		mOuterTextMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//字体颜色
		mOuterTextMovePaint.setColor(0xff969696);
		mOuterTextMovePaint.setTextSize(outArctrokeWidth / 8);
		mOuterTextMovePaint.setAntiAlias(true);

		//文字绘制笔
		mOuterTextFunnyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//字体颜色
		mOuterTextFunnyPaint.setColor(0xff969696);
		mOuterTextFunnyPaint.setTextSize(outArctrokeWidth / 8);
		mOuterTextFunnyPaint.setAntiAlias(true);

		//文字绘制笔
		mOuterTextLongPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//字体颜色
		mOuterTextLongPaint.setColor(0xff969696);
		mOuterTextLongPaint.setTextSize(outArctrokeWidth / 8);
		mOuterTextLongPaint.setAntiAlias(true);

		//文字绘制笔
		mOuterTextRecorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//字体颜色
		mOuterTextRecorderPaint.setColor(0xff969696);
		mOuterTextRecorderPaint.setTextSize(outArctrokeWidth / 8);
		mOuterTextRecorderPaint.setAntiAlias(true);


		mNumberTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNumberTextPaint.setColor(0xff076291);
		mNumberTextPaint.setTextSize(HOME_NUMBER_TEXT_SIZE);
		mInnerBackGroud  = mContext.getResources().getDrawable(R.drawable.home_score_bg_selector);
		mOuterBackGroud = mContext.getResources().getDrawable(R.drawable.home_view_bg);
		//mBigBackGroud = mContext.getResources().getDrawable(R.drawable.home_view_bg);//syc add for big
		//mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//mProgressPaint.setStyle(Paint.Style.STROKE);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mOuterBackGroud.draw(canvas);
		//mBigBackGroud.draw(canvas);
		for(int i = 0;i < mPieceNumber ; i++){
			prizeDrawArc(i , canvas);
			//Log.i("xxx", "----102");
		}
		if(mSelectIndex == -1){
			//Log.i(TAG,"mSelectIndex = "+mSelectIndex);
			//mInnerBackGroud.setState(PRESSED_FOCUSED_STATE_SET);
			//Log.i("xxx", "----101");
		}else{
			//mInnerBackGroud.setState(EMPTY_STATE_SET);
			//Log.i("xxx", "----100");
			//mOuterArcPaint.setColor(0xffffffff);
			//mOuterTextMovePaint.setColor(0xff969696);
			//invalidate();
		}
		mInnerBackGroud.draw(canvas);

		//绘制内圆底色
		//mInnerArcPaint.setColor(0x7fffffff);
		//mInnerArcPaint.setStrokeWidth(5);
		//canvas.drawArc(mInnerArcRectangle, 0, 360, false, mInnerArcPaint);

		if(mNumber == 0){
			//canvas.save();
			//canvas.rotate(progressAnimateStartAngle, getOriginal().x, getOriginal().y);
			//canvas.drawArc(mInnerArcRectangle, 0 , 360, false, mProgressPaint);		
			//canvas.restore();
		}else{
			//mInnerArcPaint.setColor(0xfff39700);
			//canvas.drawArc(mInnerArcRectangle, -90, (360*mNumber/9000), false, mInnerArcPaint);			
		}

		//Rect rect = new Rect();
		//mNumberTextPaint.getTextBounds(mNumber + "", 0, (mNumber + "").length(), rect); 
		//int txWidth  = rect.width();
		//int txHeight = rect.height();
		//canvas.drawText(mNumber + "", getOriginal().x - txWidth/2, getOriginal().y + txHeight/2, mNumberTextPaint);
	}

	/**Drawing according to the index value sectors, the first sector is located in the center of three o 'clock direction*/
	public void prizeDrawArc(int index , Canvas canvas){
		int startdegree  =  mPieceDegree * (index) - (mPieceDegree - mDividerDegree) / 2;
		
		if(index == mSelectIndex && !SuperShotApp.isOnClickMove){
			mOuterArcPaint.setColor(outArcColor[index]);
			mBigArcPaint.setColor(bigArcColor[index]);
			//Log.i("xxx", "----21");
		}else{
			//Log.i("xxx", "----20");
			mOuterArcPaint.setColor(0x7fffffff);
			//mOuterArcPaint.setColor(0xfff035ad);
			
			mBigArcPaint.setColor(bigArcColor[index]);
		}
		float radious  = ((float)mWidth - (float)outArctrokeWidth) / 2 - padding ;
		float midDegree = startdegree + ( mPieceDegree  - mDividerDegree) /2 ;
		double x  = radious * Math.cos(midDegree * Math.PI/180);
		double y  = radious * Math.sin(midDegree  * Math.PI/180);
		x = x + getOriginal().x;
		y = y + getOriginal().y;
		
		//syc add for big
		//canvas.drawArc(mBigArcRectangle, startdegree, mPieceDegree  - mDividerDegree, false, mBigArcPaint);
		
		//Log.i("xxx", "---startdegree = " + startdegree);
		//Log.i("xxx", "---mPieceDegree = " + mPieceDegree);
		//Log.i("xxx", "---mDividerDegree = " + mDividerDegree);
		
		canvas.drawArc(mOuterArcRectangle, startdegree, mPieceDegree  - mDividerDegree, false, mOuterArcPaint);
		Rect rect = new Rect();
		
		mOuterTextMovePaint.getTextBounds(items.get(index).getTitle(), 0, items.get(index).getTitle().length(), rect); 
		mOuterTextFunnyPaint.getTextBounds(items.get(index).getTitle(), 0, items.get(index).getTitle().length(), rect); 
		mOuterTextLongPaint.getTextBounds(items.get(index).getTitle(), 0, items.get(index).getTitle().length(), rect); 
		mOuterTextRecorderPaint.getTextBounds(items.get(index).getTitle(), 0, items.get(index).getTitle().length(), rect); 

		int txWidth  = rect.width();
		int txHeight = rect.height();
		//Log.i("xxx", "----txHeight" + txHeight);
		Bitmap mbitmap = BitmapFactory.decodeResource(mContext.getResources(),(items.get(index).getPrctureSource()));

		/*prize-add-huangpengfei-2016-8-31-start*/
		int outerTextMoveImageLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_image_left_bound);
		int outerTextMoveImageTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_image_top_bound);
		int outerTextMoveTextLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_left_bound);
		int outerTextMoveTextTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_top_bound);
		
		int outerTextFunnyImageLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_funny_image_left_bound);
		int outerTextFunnyImageTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_funny_image_top_bound);
		int outerTextFunnyTextLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_left_bound);
		int outerTextFunnyTextTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_top_bound);
		
		int outerTextLongImageLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_long_image_left_bound);
		int outerTextLongImageTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_long_image_top_bound);
		int outerTextLongTextLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_left_bound);
		int outerTextLongTextTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_top_bound);
		
		int outerTextRecorderImageLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_recorder_image_left_bound);
		int outerTextRecorderImageTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_recorder_image_top_bound);
		int outerTextRecorderTextLeftBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_left_bound);
		int outerTextRecorderTextTopBound = getResources().getInteger(R.integer.prize_gossip_view_outer_text_move_text_top_bound);
		
		
		Log.d(TAG, "[prizeDrawArc]   outerTextMoveImageLeftBound = "+outerTextMoveImageLeftBound+
				"   outerTextMoveImageTopBound = "+outerTextMoveImageTopBound+
				"   outerTextMoveImageTopBound = "+outerTextFunnyImageLeftBound+
				"   outerTextMoveImageTopBound = "+outerTextFunnyImageTopBound+
				"   outerTextMoveImageTopBound = "+outerTextLongImageLeftBound+
				"   outerTextMoveImageTopBound = "+outerTextLongImageTopBound+
				"   outerTextMoveImageTopBound = "+outerTextRecorderImageLeftBound+
				"   outerTextMoveImageTopBound = "+outerTextRecorderImageTopBound+
				"   x = "+x+
				"   y = "+y+
				"   txWidth = "+txWidth+
				"   txHeight = "+txHeight);
		
		
		/*prize-add-huangpengfei-2016-8-31-end*/
		if(mbitmap != null){
			//Log.i("xxx", "index = " + index);
			//Log.i("xxx", "mSelectIndex = " + mSelectIndex);
			/*prize-change-huangpengfei-2016-8-31-start*/
			if(index == 0){
				canvas.drawBitmap(mbitmap, (int)x - outerTextMoveImageLeftBound, (int)y + txHeight/2 - outerTextMoveImageTopBound, mOuterTextMovePaint);
				canvas.drawText(items.get(index).getTitle(), (int)x - txWidth/2 - outerTextMoveTextLeftBound, (int)y + txHeight/2 + outerTextMoveTextTopBound , mOuterTextMovePaint);
			}
			if(index == 1 ){
				if(!mFunnyActionDown){
					canvas.drawBitmap(mbitmap, (int)x - outerTextFunnyImageLeftBound, (int)y + txHeight/2 - outerTextFunnyImageTopBound, mOuterTextFunnyPaint);
				}
				
				canvas.drawText(items.get(index).getTitle(), (int)x - txWidth/2 + outerTextFunnyTextLeftBound, (int)y + txHeight/2 + outerTextFunnyTextTopBound , mOuterTextFunnyPaint);
			}
			if(index == 2 ){
				if(!mLongActionDown){
					canvas.drawBitmap(mbitmap, (int)x - outerTextLongImageLeftBound, (int)y + txHeight/2 - outerTextLongImageTopBound, mOuterTextLongPaint);
				}
				
				canvas.drawText(items.get(index).getTitle(), (int)x - txWidth/2 + outerTextLongTextLeftBound, (int)y + txHeight/2 + outerTextLongTextTopBound , mOuterTextLongPaint);
			}
			if(index == 3 ){
				if(!mRecorderActionDown){
					canvas.drawBitmap(mbitmap, (int)x - outerTextRecorderImageLeftBound, (int)y + txHeight/2 - outerTextRecorderImageTopBound, mOuterTextRecorderPaint);
				}
				
				canvas.drawText(items.get(index).getTitle(), (int)x - txWidth/2 + outerTextRecorderTextLeftBound, (int)y + txHeight/2 + outerTextRecorderTextTopBound , mOuterTextRecorderPaint);
			}
		}

		if(index == 0 && SuperShotApp.mMoveActionDown && SuperShotApp.isOnClickMove){
			canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
								R.drawable.main_menu_move_normal), 
								(int)x - txWidth/2 + outerTextMoveImageLeftBound, (int)y + txHeight/2 - outerTextMoveImageTopBound, 
								mOuterTextMovePaint);
		}
		
		
		if(index == 0 && SuperShotApp.mMoveActionDown && !SuperShotApp.isOnClickMove){
			canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
								R.drawable.main_menu_move_pressed), 
								(int)x - outerTextMoveImageLeftBound, (int)y + txHeight/2 - outerTextMoveImageTopBound, 
								mOuterTextMovePaint);
			
		}
	
		if(index == 1 && mFunnyActionDown){
			canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.main_menu_funny_shot_pressed), 
					(int)x - outerTextFunnyImageLeftBound, (int)y + txHeight/2 - outerTextFunnyImageTopBound, 
					mOuterTextFunnyPaint);
		}
		if(index == 2 && mLongActionDown){
			canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.main_menu_scroll_shot_pressed), 
					(int)x - outerTextLongImageLeftBound, (int)y + txHeight/2 - outerTextLongImageTopBound, 
					mOuterTextLongPaint);
		}
		if(index == 3 && mRecorderActionDown){
			canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.main_menu_screen_record_pressed), 
					(int)x - outerTextRecorderImageLeftBound, (int)y + txHeight/2 - outerTextRecorderImageTopBound, 
					mOuterTextRecorderPaint);
		}
		/*prize-change-huangpengfei-2016-8-31-end*/
		
		//canvas.drawText(items.get(index).getTitle(), (int)x - txWidth/2, (int)y + txHeight/2 + 30 , mOuterTextPaint);
		//Log.i("xxx", "draw--text");
		canvas.drawArc(mBigArcRectangle, startdegree, mPieceDegree  - mDividerDegree, false, mBigArcPaint);
		SuperShotApp.isOnClickMove = false;
	}

	/**According to the touch coordinates for sector index*/
	public int getTouchArea(Point p){
		int index = -2;
		float absdy = Math.abs(p.y - getOriginal().y);
		float absdx = Math.abs(p.x - getOriginal().x);
		//Log.i("xxx", "mPieceDegree" + mPieceDegree);
		
		if( absdx * absdx + absdy * absdy < ((float)mWidth/2 - outArctrokeWidth - overTouchDistance - padding) * ( (float)mWidth/2 - outArctrokeWidth - overTouchDistance - padding )){
			return -1;
		}
		double dx = Math.atan2(p.y - getOriginal().y, p.x - getOriginal().x);
		float fDegree = (float) (dx / (2 * Math.PI) * 360);
		fDegree = (fDegree + 360) % 360;
		int start =  - (mPieceDegree - mDividerDegree) / 2 ;
		
		//Log.i("xxx" ,"fDegree =" +fDegree);
		for(int i = 0 ; i < mPieceNumber ; i++){
			int end = start + mPieceDegree  - mDividerDegree;
			//Log.i("xxx" ,"start =" +start);
			//Log.i("xxx" ,"end =" +end);
			if( start < fDegree &&  fDegree < end){
				index = i;
			}
			if(315 < fDegree &&  fDegree < 360){
				index = 0;	
			}
			start = mPieceDegree * (i + 1) - (mPieceDegree - mDividerDegree) / 2;;
		}
		return index;
	}

	public Point getOriginal(){
		return new Point((float)mWidth/2 , (float)mWidth/2);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(),heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(width*3/5, height*3/5);
		//Log.i("xxx", "----min" + min);
		mWidth = min;
		//The thickness of the fan: outArctrokeWidth
		outArctrokeWidth = min/3;
		//mInnerArctrokeWidth = outArctrokeWidth / 7;
		//mInnerArcPaint.setStrokeWidth(mInnerArctrokeWidth);
		//mInnerArcPaint.setAntiAlias(true);
		//Text size: mOuterTextPaint
		
		/*prize-change-huangpengfei-2016-8-31-start*/
		int outerTextPaintTextSize = getResources().getInteger(R.integer.prize_gossip_view_outer_text_paint_text_size);
		mOuterTextMovePaint.setTextSize(outArctrokeWidth *2/ outerTextPaintTextSize);
		mOuterTextFunnyPaint.setTextSize(outArctrokeWidth *2/ outerTextPaintTextSize);
		mOuterTextLongPaint.setTextSize(outArctrokeWidth *2/ outerTextPaintTextSize);
		mOuterTextRecorderPaint.setTextSize(outArctrokeWidth *2/ outerTextPaintTextSize);
		/*prize-change-huangpengfei-2016-8-31-end*/
		
		mBigArcPaint.setStrokeWidth(outArctrokeWidth *7/128);
		mOuterArcPaint.setStrokeWidth(outArctrokeWidth - 6);
		
		//Outer radius of the fan: mOuterArcRadius
		mOuterArcRadius = mWidth - outArctrokeWidth/2 - padding;
		mBigArcRadius = mWidth; //syc add for big
		//mInnerArcRadius = mWidth /6 ;

		//mProgressPaint.setStrokeWidth(mInnerArctrokeWidth);
		//mSweepGradient = new SweepGradient (getOriginal().x, getOriginal().y, 0xfff39700, Color.WHITE);
		//mProgressPaint.setShader(mSweepGradient);
		
		//Log.i("xxx", "outArctrokeWidth" + outArctrokeWidth);
		//Log.i("xxx", "padding" + padding);
		//Log.i("xxx", "mOuterArcRadius" + mOuterArcRadius);
		
		//syc add for big
		mBigArcRectangle.set(outArctrokeWidth/2 + padding - 60 * outArctrokeWidth/115, 
							outArctrokeWidth/2 + padding - 60 * outArctrokeWidth/115,
							mOuterArcRadius + 60 * outArctrokeWidth/115, 
							mOuterArcRadius + 60  * outArctrokeWidth/115);
				
		//Fan to draw a rectangular area: mOuterArcRectangle
		mOuterArcRectangle.set(outArctrokeWidth/2 + padding - 4, 
								outArctrokeWidth/2 + padding - 4,
								mOuterArcRadius + 4, 
								mOuterArcRadius + 4);

		//mInnerArcRectangle.set(mWidth/2 -mInnerArcRadius -60, 
		//						mWidth/2 - mInnerArcRadius -60,
		//						mWidth/2  + mInnerArcRadius + 60, 
		//						mWidth/2 + mInnerArcRadius + 60);
		//Set inside the circle: bounds
		mInnerBackGroud.setBounds((int)outArctrokeWidth + padding - 7, 
									(int)outArctrokeWidth + padding - 7, 
									(int)(min - outArctrokeWidth - padding + 7), 
									(int)(min - outArctrokeWidth - padding + 7));
		mOuterBackGroud.setBounds(0 + 10, 0 + 10, mWidth -10, mWidth -10);
		//mBigBackGroud.setBounds(0, 0, mWidth, mWidth);
		setMeasuredDimension(min, min);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN) {

			
			mSelectIndex = getTouchArea(new Point(event.getX() , event.getY()));

			invalidate();

			if(mSelectIndex == 0){
				SuperShotApp.mMoveActionDown = true;
				mOuterTextMovePaint.setColor(0xff0ccec3);
				return false;
			}
			if(mSelectIndex == 1){
				mFunnyActionDown = true;
				mOuterTextFunnyPaint.setColor(0xffb7db10);
			}
			if(mSelectIndex == 2){
				mLongActionDown = true;
				mOuterTextLongPaint.setColor(0xff22abff);
			}
			if(mSelectIndex == 3){
				mRecorderActionDown = true;
				mOuterTextRecorderPaint.setColor(0xffffae00);
			}

			//Log.i("xxx" ,"mSelectIndex =" +mSelectIndex);
			//mSelectIndex = -1;
		}else if(event.getAction() == MotionEvent.ACTION_UP){

			SuperShotApp.mMoveActionDown = false;
			mFunnyActionDown = false;
			mLongActionDown = false;
			mRecorderActionDown = false;
			
			int upIndex = getTouchArea(new Point(event.getX() , event.getY()));
			if(mListener != null){
				mListener.onPieceClick(upIndex);
			}
			mSelectIndex = -2;

				//Log.i("xxx", "--up--redraw");
				mOuterTextMovePaint.setColor(0xff969696);
				
				invalidate();
		}else if(event.getAction() == MotionEvent.ACTION_MOVE){

			mSelectIndex = getTouchArea(new Point(event.getX() , event.getY()));
			
			invalidate();
			if(mSelectIndex == 0){
				SuperShotApp.mMoveActionDown = true;
				mFunnyActionDown = false;
				mLongActionDown = false;
				mRecorderActionDown = false;
				mOuterTextMovePaint.setColor(0xff0ccec3);
				mOuterTextFunnyPaint.setColor(0xff969696);
				mOuterTextLongPaint.setColor(0xff969696);
				mOuterTextRecorderPaint.setColor(0xff969696);
				return false;
			}
			if(mSelectIndex == 1){
				mFunnyActionDown = true;
				mLongActionDown = false;
				mRecorderActionDown = false;
				SuperShotApp.mMoveActionDown = false;
				mOuterTextFunnyPaint.setColor(0xffb7db10);
				mOuterTextMovePaint.setColor(0xff969696);
				mOuterTextLongPaint.setColor(0xff969696);
				mOuterTextRecorderPaint.setColor(0xff969696);
			}
			if(mSelectIndex == 2){
				mLongActionDown = true;
				mFunnyActionDown = false;
				mRecorderActionDown = false;
				SuperShotApp.mMoveActionDown = false;
				mOuterTextLongPaint.setColor(0xff22abff);
				mOuterTextFunnyPaint.setColor(0xff969696);
				mOuterTextMovePaint.setColor(0xff969696);
				mOuterTextRecorderPaint.setColor(0xff969696);
			}
			if(mSelectIndex == 3){
				mRecorderActionDown = true;
				mFunnyActionDown = false;
				mLongActionDown = false;
				SuperShotApp.mMoveActionDown = false;
				mOuterTextRecorderPaint.setColor(0xffffae00);
				mOuterTextLongPaint.setColor(0xff969696);
				mOuterTextFunnyPaint.setColor(0xff969696);
				mOuterTextMovePaint.setColor(0xff969696);
			}
			
		}else if(event.getAction() == MotionEvent.ACTION_CANCEL){
			//Log.i("xxx", "--ACTION_CANCEL-");
			mSelectIndex = -2;
			this.invalidate();
		}
		return true;
	}


	/**Set up the number in the circle*/
	public void setNumber(int number){
		mNumber = number;
		this.invalidate();
	}

	/**Get the number in the circle*/
	public int getNumber(){
		return mNumber;
	}

	/** Set the animation starting value*/
	public void setProgressAnimateStartAngle(int startAngle){
		progressAnimateStartAngle = startAngle;
		this.invalidate();
	}

	public int getProgressAnimateStartAngle(){
		return progressAnimateStartAngle;
	}	

	public void setItems(List<GossipItem> items1){
		this.items = items1; 
		mPieceNumber = items.size();
		mPieceDegree = 360/mPieceNumber;
	}

	/** Set the click event*/
	public void setOnPieceClickListener(OnPieceClickListener l){
		mListener = l;
	}

	
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {

	            String action = intent.getAction();
	            if (action.equals("syc.syc.com.move.button")) {
	            	mOuterArcPaint.setColor(0xffffffff);
	    			mOuterTextMovePaint.setColor(0xff969696);
	    			invalidate();
	            }
				
	        }
	    };
}
