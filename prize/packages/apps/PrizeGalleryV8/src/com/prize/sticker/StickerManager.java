/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.RectF;
import android.nfc.Tag;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;
//import com.android.gallery3d.filtershow.LocationActivity;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
//import com.baidu.location.BDLocation;
//import com.baidu.location.Poi;
import com.prize.util.LogTools;
import com.prize.sticker.WordEditDialog.IExchangeWord;
import com.prize.sticker.db.Provider;
//import com.prize.sticker.location.StickerLocationManager;
//import com.prize.sticker.location.StickerLocationManager.Listener;

public class StickerManager {
	
	protected static final String TAG = "StickerManager";
    public static final float MAX_SCALE_SIZE = 2.0f;
    public static final float MIN_SCALE_SIZE = 0.5f;

    private RectF mViewRect;
    private float mDeviation;
    private Bitmap mControlBitmap, mDeleteBitmap, mSymmetricBitmap;
    private float mControlWidth, mControlHeight, mDeleteWidth, mDeleteHeight;
    private List<ISticker> stickers = new ArrayList<ISticker>();
    private FilterShowActivity mContext;
    private View mStickerView;
    private View mEditorView;
    private EditText mWordEdit;
    private TextView mCountTv;
	private ImageView mOkIm;
    private int mFocusStickerPosition = -1;
    private float mLastX;
    private float mLastY;
    
    static final int NONE = 0;
    static final int FOCUS = 1;
    static final int DRAG = 2;
    static final int ZOOM = 3;
    static final int ZOOM_ROTATE = 4;
    static final int NONE_MOVE = 5;
    private int mMode = NONE;
    private float mLastDist = 1f;
    private boolean mIsEditor;
    private static StickerManager sStickerManager;
    
    private HashMap<String, ArrayList<WatermarkBean>> mWatermarkMap = new HashMap<String, ArrayList<WatermarkBean>>();

    public static StickerManager getStickerManager() {
        if (sStickerManager == null) {
        	sStickerManager = new StickerManager();
        }
        return sStickerManager;
    }
    
    private StickerManager() {
    	
    }
    
    public void init(FilterShowActivity context, View imageShow) {
    	mContext = context;
    	StickerTool.queryWatermarks(context);
    	mStickerView = imageShow;
    	mControlBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bussiness_edit_control);
        mControlWidth = mControlBitmap.getWidth();
        mControlHeight = mControlBitmap.getHeight();

        mDeleteBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bussiness_edit_del);
        mSymmetricBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bussiness_edit_symmetry);
        mDeleteWidth = mDeleteBitmap.getWidth();
        mDeleteHeight = mDeleteBitmap.getHeight();
    }
    
    public ArrayList<WatermarkBean> getWatermarkBeans(String key) {
    	ArrayList<WatermarkBean> watermarkBeans = mWatermarkMap.get(key);
    	if (watermarkBeans == null) {
    		watermarkBeans = new ArrayList<WatermarkBean>();
    		mWatermarkMap.put(key, watermarkBeans);
    	}
    	return watermarkBeans;
    }

    public void addSticker(Bitmap bitmap, int width, int height, int margin) {
    	LogTools.i(TAG, "setWaterMark width=" + width + " height=" + height);
        Point point = Utils.getDisplayWidthPixels(mContext);
        Sticker sticker = new Sticker(bitmap, point.x, point.x, margin);
        stickers.add(sticker);
        mFocusStickerPosition = stickers.size() - 1;
        setFocusSticker(mFocusStickerPosition);
        mStickerView.invalidate();
    }
    
    public void addWatermark(WatermarkBean watermarkBean) {
    	List<IWatermarkResource> list = watermarkBean.getWatermarkResources();
        Point point = Utils.getDisplayWidthPixels(mContext);
        if (list.size() > 0) {
        	Watermark sticker = new Watermark(watermarkBean.getTextColor(), watermarkBean.getID(), watermarkBean.isSticker(), watermarkBean.isColorChange(), point.x, point.y, list, mContext);
        	String name = getAddress();
        	if (!TextUtils.isEmpty(name)) {
//        		sticker.replaceAddress(name, mLocationManager.getCurrentLocation().getLongitude(), mLocationManager.getCurrentLocation().getLatitude());
        	}
            stickers.add(sticker);
            mFocusStickerPosition = stickers.size() - 1;
            setFocusSticker(mFocusStickerPosition);
            mStickerView.invalidate();
            MasterImage.getImage().checkSave();
        }
        ArrayList<WatermarkBean> historys = getWatermarkBeans(StickerTool.TYPE_WATERMARK_HOT);
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Provider.HistoryColumns.WATERMARK_ID, watermarkBean.getID());
        values.put(Provider.HistoryColumns.MODIFY_TIME, getDateTime());
        if (historys.contains(watermarkBean)) {
        	historys.remove(watermarkBean);
        	resolver.update(Provider.HistoryColumns.CONTENT_URI, values, Provider.HistoryColumns.WHERE_UPDATE, new String[]{Long.toString(watermarkBean.getID())});
        } else {
        	values.put(Provider.HistoryColumns.APP_TYPE, StickerTool.APP_GALLERY);
        	resolver.insert(Provider.HistoryColumns.CONTENT_URI, values);
        }
        WatermarkBean history = (WatermarkBean) watermarkBean.clone();
		history.setType(StickerTool.TYPE_WATERMARK_HOT);
        historys.add(0, history);
    }
    
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void onDrawSticker(Canvas canvas) {
        PaintFlagsDrawFilter mSetfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
        canvas.setDrawFilter(mSetfil);
        LogTools.i(TAG, "onDrawSticker SIZE=" + stickers.size() + " VIEW=" + mStickerView);
        if (stickers.size() <= 0) {
            return;
        }
        canvas.save();
        for (int i = 0; i < stickers.size(); i++) {
            stickers.get(i).getMatrix().mapPoints(stickers.get(i).getDstPoints(), stickers.get(i).getSrcPoints());
            canvas.drawBitmap(stickers.get(i).getBitmap(false), stickers.get(i).getMatrix(), null);
            if (stickers.get(i).isFocus()) {
            	drawFocus(canvas, stickers.get(i));
            }
        }
        canvas.restore();
    }
    
    private Bitmap getColorBitmap(ISticker sticker) {
    	if (sticker.isBlackColor()) {
    		return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bussiness_edit_color_black);
    	}
    	return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bussiness_edit_color_white);
    }
    
    private void drawFocus(Canvas canvas, ISticker sticker) {
    	
    	float[] points = sticker.getDstPoints();
    	Paint borderPaint = sticker.getEdgePaint();
    	canvas.drawLine(points[0], points[1], points[2], points[3], borderPaint);
        canvas.drawLine(points[2], points[3], points[4], points[5], borderPaint);
        canvas.drawLine(points[4], points[5], points[6], points[7], borderPaint);
        canvas.drawLine(points[6], points[7], points[0], points[1], borderPaint);
        
        int controlIndex = 4;
        int deleteIndex = 0;
        int colorIndex = 2;
        int symmetricIndex = 2;
        
        boolean isSymmetric = sticker.isSymmetric();
        if (isSymmetric) {
        	controlIndex = 6;
        	deleteIndex = 2;
        	colorIndex = 0;
        	symmetricIndex = 0;
        }
        canvas.drawBitmap(mControlBitmap, points[controlIndex] - mControlWidth / 2, points[controlIndex + 1] - mControlHeight / 2, null);
        canvas.drawBitmap(mDeleteBitmap, points[deleteIndex] - mDeleteWidth / 2, points[deleteIndex + 1] - mDeleteHeight / 2, null);
        
        if (sticker.isSticker()) {
        	canvas.drawBitmap(mSymmetricBitmap, points[symmetricIndex] - mDeleteWidth / 2, points[symmetricIndex + 1] - mDeleteHeight / 2, null);
        } else if (sticker.isTextColorChange()) {
        	canvas.drawBitmap(getColorBitmap(sticker), points[colorIndex] - mDeleteWidth / 2, points[colorIndex + 1] - mDeleteHeight / 2, null);
        }
    }

    private boolean isInControl(float x, float y) {
        int position = 4;
        
        if (stickers.get(mFocusStickerPosition).isSymmetric()) {
        	position = 6;
        }
        float rx = stickers.get(mFocusStickerPosition).getDstPoints()[position];
        float ry = stickers.get(mFocusStickerPosition).getDstPoints()[position + 1];
        RectF rectF = new RectF(rx - mControlWidth / 2,
                ry - mControlHeight / 2,
                rx + mControlWidth / 2,
                ry + mControlHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;

    }

    private boolean isInDelete(float x, float y) {
        int position = 0;
        
        if (stickers.get(mFocusStickerPosition).isSymmetric()) {
        	position = 2;
        }
        float rx = stickers.get(mFocusStickerPosition).getDstPoints()[position];
        float ry = stickers.get(mFocusStickerPosition).getDstPoints()[position + 1];
        RectF rectF = new RectF(rx - mDeleteWidth / 2,
                ry - mDeleteHeight / 2,
                rx + mDeleteWidth / 2,
                ry + mDeleteHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;

    }
    
    private boolean isInColorInvertOrSymmetric(float x, float y) {
    	ISticker sticker = stickers.get(mFocusStickerPosition);
    	if (sticker.isSticker()) {
    		if (isInSymmetric(x, y)) {
    			return true;
    		}
    	} else if (sticker.isTextColorChange()) {
    		if (isInColorInvert(x, y)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isInColorInvert(float x, float y) {
        int position = 2;
        if (stickers.get(mFocusStickerPosition).isSymmetric()) {
        	position = 0;
        }
        float rx = stickers.get(mFocusStickerPosition).getDstPoints()[position];
        float ry = stickers.get(mFocusStickerPosition).getDstPoints()[position + 1];
        RectF rectF = new RectF(rx - mDeleteWidth / 2,
                ry - mDeleteHeight / 2,
                rx + mDeleteWidth / 2,
                ry + mDeleteHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;

    }
    
    private boolean isInSymmetric(float x, float y) {
    	int position = 2;
        if (stickers.get(mFocusStickerPosition).isSymmetric()) {
        	position = 0;
        }
        float rx = stickers.get(mFocusStickerPosition).getDstPoints()[position];
        float ry = stickers.get(mFocusStickerPosition).getDstPoints()[position + 1];
        RectF rectF = new RectF(rx - mDeleteWidth / 2,
                ry - mDeleteHeight / 2,
                rx + mDeleteWidth / 2,
                ry + mDeleteHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;
    }
    
    public boolean onTouchEvent(MotionEvent event, int width, int height) {
        if (mViewRect == null) {
            mViewRect = new RectF(0f, 0f, width, height);
        }

        if (stickers.size() <= 0 || mFocusStickerPosition < 0) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
//			mIsEditor = (mEditorView.getVisibility() == View.VISIBLE);
			/*if (mIsEditor) {
				hideEditor();
			} else*/ {
				handleDownEvent(x, y);
			}
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			if (!mIsEditor) {
				handlePointerDown(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mIsEditor) {
				handleUpEvent(x, y);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			mMode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (x != mLastX && y != mLastY && !mIsEditor) {
				handleMoveEvent(x, y, event);
			}
			break;
		}
        return true;
    }
    
    private void handlePointerDown(MotionEvent event) {
    	if (mMode != NONE && mMode != NONE_MOVE) {
			mLastDist = spacing(event);
			mLastX = (event.getX(0) + event.getX(1)) / 2;
			mLastY = (event.getY(0) + event.getY(1)) / 2;
			mMode = ZOOM;
		}
    }
    
    private void handleUpEvent(float x, float y) {
    	if (mMode == NONE) { // Single Click
			if (isInSticker(x, y)) {
				mStickerView.invalidate();
			}
		} else if (mMode == FOCUS) {
			if (isInDelete(x, y)) {
				doDeleteSticker();
			} else {
				ISticker iSticker = stickers.get(mFocusStickerPosition);
				Object touch = iSticker.getTouchResource(x, y);
				if (touch != null) {
					if (touch instanceof PointD) {
						gotoPickAddress((PointD) touch);
					} else if (touch instanceof WordResource) {
						WordResource wordResource = (WordResource) touch;
						showEditor(wordResource);
					}
				} else if (isInColorInvertOrSymmetric(x, y)) {
					if (iSticker.isSticker()) {
						doSymmetric();
					} else {
						doInvertColor();
					}
        		} else {
					isInSticker(x, y);
					mStickerView.invalidate();
				}
			}
		}
		mMode = NONE;
    }
    
    private void gotoPickAddress(PointD touch) {
    	/*Intent it = new Intent(mContext, LocationActivity.class);
    	it.putExtra(FilterShowActivity.KEY_ADDR_LONGITUDE, touch.getX());
    	it.putExtra(FilterShowActivity.KEY_ADDR_LATITUDE, touch.getY());
    	it.putExtra(FilterShowActivity.LAUNCH_FULLSCREEN,
                mContext.isFullscreen());
    	mContext.startActivityForResult(it, FilterShowActivity.PICK_ADDRESS_REQUEST_CODE);
    	mContext.overrideTransition();*/
    }
    
    private void handleDownEvent(float x, float y) {
    	if (stickers.get(mFocusStickerPosition).isFocus()) {
			mMode = FOCUS;
		} else {
			mMode = NONE;
		}

		if (mMode == FOCUS) {
        	if (isInControl(x, y)) {
                float nowLenght = distanceToCenter(stickers.get(mFocusStickerPosition).getDstPoints()[0], stickers.get(mFocusStickerPosition).getDstPoints()[1]);
                float touchLenght = distanceToCenter(x, y);
                mDeviation = touchLenght - nowLenght;
                mMode = ZOOM_ROTATE;
            }
        }
		mLastX = x;
		mLastY = y;
    }
    
    private void handleMoveEvent(float x, float y, MotionEvent event) {
    	if (mMode == DRAG) {
			float dx = x - mLastX;
            float dy = y - mLastY;
            if (Math.sqrt(dx * dx + dy * dy) > 2.0f  && canStickerMove(dx, dy)) {
                stickers.get(mFocusStickerPosition).getMatrix().postTranslate(dx, dy);
				mStickerView.invalidate();
			}
		} else if (mMode == ZOOM) { 
			if (event.getPointerCount() > 1) { 
                float newDist = spacing(event);
                boolean isChange = false;
                if (newDist > mLastDist + 1) {  
                	isChange = true;  
                }  
                if (newDist < mLastDist - 1) {  
                	isChange = true;  
                }  
                if (isChange) {
                	float scale = newDist / mLastDist;
                    float curScale = stickers.get(mFocusStickerPosition).getScale() * scale;
                    if (curScale >= MIN_SCALE_SIZE && curScale <= MAX_SCALE_SIZE) {
                        stickers.get(mFocusStickerPosition).getMatrix().postScale(scale, scale, stickers.get(mFocusStickerPosition).getDstPoints()[8], stickers.get(mFocusStickerPosition).getDstPoints()[9]);
                        stickers.get(mFocusStickerPosition).setScale(curScale);
                        mStickerView.invalidate();
                        mLastDist = newDist;  
                    }
                }
            }
		} else if (mMode == ZOOM_ROTATE) {
			stickers.get(mFocusStickerPosition).getMatrix().postRotate(rotateAngle(event), stickers.get(mFocusStickerPosition).getDstPoints()[8], stickers.get(mFocusStickerPosition).getDstPoints()[9]);
            float nowLenght = distanceToCenter(stickers.get(mFocusStickerPosition).getDstPoints()[0], stickers.get(mFocusStickerPosition).getDstPoints()[1]);
            float touchLenght = distanceToCenter(x, y) - mDeviation;
            if (Math.sqrt((nowLenght - touchLenght) * (nowLenght - touchLenght)) > 0.0f) {
                float scale = touchLenght / nowLenght;
                float nowsc = stickers.get(mFocusStickerPosition).getScale() * scale;
                if (nowsc >= MIN_SCALE_SIZE && nowsc <= MAX_SCALE_SIZE) {
                    stickers.get(mFocusStickerPosition).getMatrix().postScale(scale, scale, stickers.get(mFocusStickerPosition).getDstPoints()[8], stickers.get(mFocusStickerPosition).getDstPoints()[9]);
                    stickers.get(mFocusStickerPosition).setScale(nowsc);
                }
            }
			mStickerView.invalidate();
		} else {
			if (mMode == FOCUS) {
				mMode = DRAG;
			} else {
				mMode = NONE_MOVE;
			}
		}
		mLastX = x;
		mLastY = y;
    }
    
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void doDeleteSticker() {
        stickers.remove(mFocusStickerPosition);
        mFocusStickerPosition = stickers.size() - 1;
        mStickerView.invalidate();
        MasterImage.getImage().checkSave();
    }
    
    private void doInvertColor() {
       stickers.get(mFocusStickerPosition).invertColor();
       mStickerView.invalidate();
    }
    
    private void doSymmetric() {
    	stickers.get(mFocusStickerPosition).doSymmetric();
  	 	mStickerView.invalidate();
    }

    private boolean canStickerMove(float cx, float cy) {
        float px = cx + stickers.get(mFocusStickerPosition).getDstPoints()[8];
        float py = cy + stickers.get(mFocusStickerPosition).getDstPoints()[9];
        if (mViewRect.contains(px, py)) {
            return true;
        } else {
            return false;
        }
    }


    private float distanceToCenter(float x, float y) {
        return (float)Utils.lineSpace(x, y, stickers.get(mFocusStickerPosition).getDstPoints()[8], stickers.get(mFocusStickerPosition).getDstPoints()[9]);
    }

    private float rotateAngle(MotionEvent event) {
        float originDegree = computeDegree(mLastX, mLastY);
        float nowDegree = computeDegree(event.getX(), event.getY());
        return nowDegree - originDegree;
    }

    private float computeDegree(float x, float y) {
        double dx = x - stickers.get(mFocusStickerPosition).getDstPoints()[8];
        double dy = y - stickers.get(mFocusStickerPosition).getDstPoints()[9];
        double radians = Math.atan2(dy, dx);
        return (float) Math.toDegrees(radians);
    }

    private boolean isInSticker(float x, float y) {
        for (int i = stickers.size() - 1; i >= 0; i--) {
            ISticker sticker = stickers.get(i);
            if (sticker.isInWatemark(x, y)) {
                setFocusSticker(i);
                return true;
            }
        }
        setFocusSticker(-1);
        return false;
    }

    public ArrayList<ISticker> saveStickers() {
    	ArrayList<ISticker> stickerList = new ArrayList<ISticker>(stickers.size());
    	stickerList.addAll(stickers);
        stickers.clear();
        mFocusStickerPosition = -1;
        return stickerList;
    }
    
    public boolean hasStickerModify() {
    	return stickers.size() > 0;
    }

    private void setFocusSticker(int position) {
        int focusPosition = stickers.size() - 1;
        for (int i = 0; i < stickers.size(); i++) {
            if (i == position) {
                focusPosition = i;
                stickers.get(i).setFocus(true);
            } else {
                stickers.get(i).setFocus(false);
            }
        }
        ISticker sticker = stickers.remove(focusPosition);
        stickers.add(sticker);
        mFocusStickerPosition = stickers.size() - 1;
    }

	public void clearStickers() {
		stickers.clear();
		mFocusStickerPosition = -1;
		mWatermarkMap.clear();
	}
	
	private IExchangeWord mExchangeWord = new IExchangeWord() {
		
		@Override
		public void callback(String newWord) {
			ISticker iSticker = stickers.get(mFocusStickerPosition);
			iSticker.replaceWord(newWord);
    		mStickerView.invalidate();
		}
	};
	
	private void showEditor(WordResource wResource) {
//		mWordEdit.setText(word);
//		mEditorView.setVisibility(View.VISIBLE);
		WordEditDialog wordEditDialog = new WordEditDialog(mContext, wResource.getWord(), wResource.getLimitLen(), wResource.isSingleLine(), mExchangeWord);
		wordEditDialog.show();
	}
	
	private void hideEditor() {
//  	  InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//  	  imm.hideSoftInputFromWindow(mWordEdit.getWindowToken(), 0);
//  	  mEditorView.setVisibility(View.GONE);
  }

	public void setEditorView(View etView) {
//		this.mEditorView = etView;
//		mWordEdit = (EditText) mEditorView.findViewById(R.id.et);
//		mCountTv = (TextView) mEditorView.findViewById(R.id.tv_count);
//		mOkIm = (ImageView) mEditorView.findViewById(R.id.ok);
//		mOkIm.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				String word = mWordEdit.getText().toString();
//				if (!TextUtils.isEmpty(word)) {
//					ISticker iSticker = stickers.get(mFocusStickerPosition);
//					iSticker.replaceWord(word);
//	        		mStickerView.invalidate();
//					hideEditor();
//				}
//			}
//		});
//		
//		mWordEdit.addTextChangedListener(new TextWatcher() {
//			
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				
//			}
//			
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				
//			}
//			
//			@Override
//			public void afterTextChanged(Editable s) {
//				Editable editable = mWordEdit.getText();
//				int len = editable.length();
//				int newLen = len;
//				
//				if (len > mLimitLen) {
//					int selEndIndex = Selection.getSelectionEnd(editable);
//					String str = editable.toString();
//					String newStr = str.substring(len - mLimitLen, len);
//					mWordEdit.setText(newStr);
//					editable = mWordEdit.getText();
//					
//					newLen = editable.length();
//					if (selEndIndex > newLen) {
//						selEndIndex = editable.length();
//					}
//					Selection.setSelection(editable, selEndIndex);
//				}
//				mCountTv.setText(newLen + "/" + mLimitLen);
//			}
//		});
	}
	
	private String getAddress() {
		/*if (mLocationManager != null) {
			BDLocation location = mLocationManager.getCurrentLocation();
			if (location != null && location.getPoiList() != null && !location.getPoiList().isEmpty()) {
				Poi poi = (Poi) location.getPoiList().get(0);
				String name = poi.getName();
				return name;
			}
		}*/
		return null;
	}
	
	private void updateAddress() {
		String name = getAddress();
		if (TextUtils.isEmpty(name)) {
			return;
		}
		for (int i = 0, size = stickers.size(); i < size; i++) {
			ISticker iSticker = stickers.get(i);
//			iSticker.replaceAddress(name, mLocationManager.getCurrentLocation().getLongitude(), mLocationManager.getCurrentLocation().getLatitude());
		}
		mStickerView.invalidate();
	}
	
//	private StickerLocationManager mLocationManager;
	/*public void setLocationManager(StickerLocationManager locationManager) {
		mLocationManager = locationManager;
		mLocationManager.setListener(new Listener() {
			
			@Override
			public void updateLocation() {
				updateAddress();
			}
		});
	}*/

	public boolean handleBackKey() {
//		if (mEditorView.getVisibility() == View.VISIBLE) {
//			mWordEdit.clearFocus();
//			mEditorView.setVisibility(View.GONE);
//			return true;
//		}
		return false;
	}

	public void updateAddress(String name) {
		ISticker iSticker = stickers.get(mFocusStickerPosition);
		iSticker.replaceWord(name);
		mStickerView.invalidate();
	}

	public void reset() {
		stickers.clear();
		mFocusStickerPosition = -1;
		mStickerView.invalidate();
	}

}

