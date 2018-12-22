/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextView;

import com.android.launcher3.Launcher.IconChangeState;
import com.android.launcher3.nifty.IconUninstallIndicatorAnim;
import com.android.launcher3.nifty.NiftyObserables;
import com.android.launcher3.nifty.NiftyObservers;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.OLThemeChangeListener;
import com.lqsoft.lqtheme.OLThemeNotification;
import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
//m by zhouerlong
//A by zel
//A by zel
//A by zel
//add by zhouerlong
//A by zel
//add by zhouerlong
//add by zhouerlong
//m by zhouerlong
//A by zel

/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class BubbleTextView extends TextView implements NiftyObservers,OLThemeChangeListener{//add by zhouerlong
    static final float SHADOW_LARGE_RADIUS = 4.0f;
    static final float SHADOW_SMALL_RADIUS = 1.75f;
    static final float SHADOW_Y_OFFSET = 2.0f;
    static final int SHADOW_LARGE_COLOUR = 0xDD000000;
    static final int SHADOW_SMALL_COLOUR = 0xCC000000;
    static final float PADDING_H = 8.0f;
    static final float PADDING_V = 3.0f;

    private int mPrevAlpha = -1;

    private HolographicOutlineHelper mOutlineHelper;
    private final Canvas mTempCanvas = new Canvas();
    private final Rect mTempRect = new Rect();
    private boolean mDidInvalidateForPressedState;
    private Bitmap mPressedOrFocusedBackground;
    private int mFocusedOutlineColor;
    private int mFocusedGlowColor;
    private int mPressedOutlineColor;
    private int mPressedGlowColor;

    private int mTextColor;
    private boolean mShadowsEnabled = true;
    private boolean mIsTextVisible;

    private boolean mBackgroundSizeChanged;
    private Drawable mBackground;

    private boolean mStayPressed;
    private CheckLongPressHelper mLongPressHelper;
    
	//add by zhouerlong 20150808 begin
    /**
     * 创建被观察者
     */
    private NiftyObserables mNiftyObserables = null;

    /**
     * 读取被观察则
     * @return
     */
    public NiftyObserables getmNiftyObserables() {
		return mNiftyObserables;
	}

	/**
	 * @param mNiftyObserables
	 */
	public void setmNiftyObserables(NiftyObserables mNiftyObserables) {
		this.mNiftyObserables = mNiftyObserables;
	}
	//add by zhouerlong 20150808 begin

	public BubbleTextView(Context context) {
        super(context);
        init();
    }
	
	
	

    //begin add by ouyangjin for lqtheme
    private Handler mHandler=new Handler();
    private OLThemeChangeCallbacks mFolderCallback;
    public interface OLThemeChangeCallbacks {
  	   public void updateViewAfterChildThemeChange();
     }
    //end add by ouyangjin for lqtheme

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    
	//add by zhouerlong 20150808 begin
    /**注册观测者
     * @param obserables
     */
    public void registerObserver(NiftyObserables obserables)  {
    	obserables.registerObserver(this);
    }
	//add by zhouerlong 20150808 end

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void onFinishInflate() {
        super.onFinishInflate();

        // Ensure we are using the right text size
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        /// M: Whether is tablet or use tablet solution, need keep textsize.
        if (grid.isTablet() || getResources().getBoolean(R.bool.allow_rotation)) {
            float fontSize = getContext().getResources().getDimensionPixelSize(
                    R.dimen.normal_text_size);
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        } else {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, Launcher.textSize);
        }
        setTextColor(getResources().getColor(R.color.workspace_icon_text_color));
    }
    

    private void init() {
    	//begin add by ouyangjin for lqtheme
    	OLThemeNotification.registerThemeChange(this, this, null);
    	//end add by ouyangjin for lqtheme
        mLongPressHelper = new CheckLongPressHelper(this);
        mBackground = getBackground();

        mOutlineHelper = HolographicOutlineHelper.obtain(getContext());

        final Resources res = getContext().getResources();
        mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor =
            res.getColor(R.color.outline_color);

//        setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
    }
    public void applyFromShortcutInfo(ShortcutInfo info, final IconCache iconCache) {
    	Bitmap b=null;
        final LauncherAppState app = LauncherAppState.getInstance();
    	if(info.fromAppStore ==1) {
    		loadImage(info,iconCache);
    	}else {
            b= info.getIcon(iconCache);
            final ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveActivity(info.intent, 0);

            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            if(Launcher.isSupportIconSize) {
					setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(b), null, null);
            }else {
    			 setCompoundDrawables(null,
				 Utilities.createIconDrawable(b), null, null);
            }

    	}
		//m by zhouerlong
        
			//add by zhouerlong
        //m by zhouerlong
//        setCompoundDrawablePadding((int) ((grid.folderIconSizePx - grid.iconSizePx) / 2f));
        setText(info.title);
        setTag(info);
    }
    
    public void loadImage(final ShortcutInfo info,final IconCache iconCache) {
    	
    	
		ImageLoader.getInstance().displayImage(
				info.iconUri,
				new TextViewWrapAware(BubbleTextView.this,
						mContext, iconCache),UILimageUtil.getUILoptions(mContext));
    }
    
    
    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (getLeft() != left || getRight() != right || getTop() != top || getBottom() != bottom) {
            mBackgroundSizeChanged = true;
        }
        return super.setFrame(left, top, right, bottom);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mBackground || super.verifyDrawable(who);
    }

    @Override
    public void setTag(Object tag) {
        if (tag != null) {
            LauncherModel.checkItemInfo((ItemInfo) tag);
        }
        super.setTag(tag);
    }

    @Override
    protected void drawableStateChanged() {
        if (isPressed()) {
            // In this case, we have already created the pressed outline on ACTION_DOWN,
            // so we just need to do an invalidate to trigger draw
            if (!mDidInvalidateForPressedState) {
                setCellLayoutPressedOrFocusedIcon();
            }
        } else {
            // Otherwise, either clear the pressed/focused background, or create a background
            // for the focused state
            final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
            if (!mStayPressed) {
                mPressedOrFocusedBackground = null;
            }
            if (isFocused()) {
                if (getLayout() == null) {
                    // In some cases, we get focus before we have been layed out. Set the
                    // background to null so that it will get created when the view is drawn.
                    mPressedOrFocusedBackground = null;
                } else {
                    mPressedOrFocusedBackground = createGlowingOutline(
                            mTempCanvas, mFocusedGlowColor, mFocusedOutlineColor);
                }
                mStayPressed = false;
                setCellLayoutPressedOrFocusedIcon();
            }
            final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
            if (!backgroundEmptyBefore && backgroundEmptyNow) {
                setCellLayoutPressedOrFocusedIcon();
            }
        }

        Drawable d = mBackground;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }

        setFilter();
        super.drawableStateChanged();
    }
    
    /**  
     *   设置滤镜
     */
    private void setFilter() {
    	
        //先获取设置的src图片
        Drawable drawable=this.getCompoundDrawables()[1];
        //当src图片为Null，获取背景图片
        if(isPressed()||isFocused())  {
            if(drawable!=null){
                //设置滤镜
                drawable.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);  
            }
        }else {

            if(drawable!=null){
                //设置滤镜
                drawable.setColorFilter(null); 
            }
        }
    }

    /**
     * Draw this BubbleTextView into the given Canvas.
     *
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private void drawWithPadding(Canvas destCanvas, int padding) {
        final Rect clipRect = mTempRect;
        getDrawingRect(clipRect);

        // adjust the clip rect so that we don't include the text label
        clipRect.bottom =
            getExtendedPaddingTop() - (int) BubbleTextView.PADDING_V + getLayout().getLineTop(0);

        // Draw the View into the bitmap.
        // The translate of scrollX and scrollY is necessary when drawing TextViews, because
        // they set scrollX and scrollY to large values to achieve centered text
        destCanvas.save();
        destCanvas.scale(getScaleX(), getScaleY(),
                (getWidth() + padding) / 2, (getHeight() + padding) / 2); //放大缩小
        destCanvas.translate(-getScrollX() + padding / 2, -getScrollY() + padding / 2); //位移
        destCanvas.clipRect(clipRect);
        draw(destCanvas);
        destCanvas.restore();
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createGlowingOutline(Canvas canvas, int outlineColor, int glowColor) {
        final int padding = mOutlineHelper.mMaxOuterBlurRadius;
        final Bitmap b = Bitmap.createBitmap(
                getWidth() + padding, getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawWithPadding(canvas, padding);
       // mOutlineHelper.applyExtraThickExpensiveOutlineWithBlur(b, canvas, glowColor, outlineColor); //添加模糊轮廓 blur 模糊 outLine 轮廓   expensive 不清晰的  Extra 额外的  apply 应用
        canvas.setBitmap(null);

        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // So that the pressed outline is visible immediately when isPressed() is true,
                // we pre-create it on ACTION_DOWN (it takes a small but perceptible amount of time
                // to create it)
                if (mPressedOrFocusedBackground == null) {
                    mPressedOrFocusedBackground = createGlowingOutline(
                            mTempCanvas, mPressedGlowColor, mPressedOutlineColor);
                }
                // Invalidate so the pressed state is visible, or set a flag so we know that we
                // have to call invalidate as soon as the state is "pressed"
                if (isPressed()) {
                    mDidInvalidateForPressedState = true;
                    setCellLayoutPressedOrFocusedIcon();
                } else {
                    mDidInvalidateForPressedState = false;
                }

//                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // If we've touched down and up on an item, and it's still not "pressed", then
                // destroy the pressed outline
                if (!isPressed()) {
                    mPressedOrFocusedBackground = null;
                }

                mLongPressHelper.cancelLongPress();
                break;
        }
        return true;
    }

    void setStayPressed(boolean stayPressed) {
        mStayPressed = stayPressed;
        if (!stayPressed) {
            mPressedOrFocusedBackground = null;
        }
        setCellLayoutPressedOrFocusedIcon();
    }

    void setCellLayoutPressedOrFocusedIcon() {
        if (getParent() instanceof ShortcutAndWidgetContainer) {
            ShortcutAndWidgetContainer parent = (ShortcutAndWidgetContainer) getParent();
            if (parent != null) {
                CellLayout layout = (CellLayout) parent.getParent();
                layout.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ? this : null);
            }
        }
    }
    
			//A by zel
    void setCellLayoutNormalIcon() {

        if (getParent() instanceof ShortcutAndWidgetContainer) {
            ShortcutAndWidgetContainer parent = (ShortcutAndWidgetContainer) getParent();
            if (parent != null) {
                CellLayout layout = (CellLayout) parent.getParent();
                layout.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ? this : null);
            }
        }
    }

    void clearPressedOrFocusedBackground() {
        mPressedOrFocusedBackground = null;
        setCellLayoutPressedOrFocusedIcon();
    }

    Bitmap getPressedOrFocusedBackground() {
        return mPressedOrFocusedBackground;
    }

    int getPressedOrFocusedBackgroundPadding() {
        return mOutlineHelper.mMaxOuterBlurRadius / 2;
    }
			//A by zel
    public void drawIconShadow(Bitmap  originalBitmap){
    	BlurMaskFilter blurFilter = new BlurMaskFilter(3, BlurMaskFilter.Blur.OUTER);
 		Paint shadowPaint = new Paint();		
 		shadowPaint.setMaskFilter(blurFilter);
 		int[] offsetXY =new int[2];
 		Bitmap shadowBitmap = originalBitmap.extractAlpha(shadowPaint,offsetXY);
 		
 		Bitmap shadowImage32 = shadowBitmap.copy(Bitmap.Config.ARGB_8888, true);
 		Canvas c = new Canvas(shadowImage32);		
 		c.drawBitmap(originalBitmap, 0,0, null);  
    }
	public Bitmap  getExtractDrawable(Bitmap  src) {
		
		 Paint p = new Paint();
	        p.setColor(Color.WHITE);
		Bitmap b = src;
		Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(b.extractAlpha(), 0, 0, p);
		return  bitmap;
	}
			//A by zel
    @Override
    public void draw(Canvas canvas) {

        final Drawable background = mBackground;
        canvas.save();
        if (background != null) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0,  getRight() - getLeft(), getBottom() - getTop());
                mBackgroundSizeChanged = false;
            }

            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }

        ///: Added for MTK unread message feature.@{
        ///: @}
        // If text is transparent, don't draw any shadow
        if (getCurrentTextColor() == getResources().getColor(android.R.color.transparent)) {
            getPaint().clearShadowLayer();
            super.draw(canvas);
            ///: Added for MTK unread message feature.@{
            drawUnreadEvent(canvas);
            canvas.restore();
            ///: @}
            return;
        }
        super.draw(canvas);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
         canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(),
                getScrollX() + getWidth(),
                getScrollY() + getHeight(), Region.Op.INTERSECT); 
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(),
                getScrollX() + getWidth(),
                getScrollY() + getHeight(), Region.Op.INTERSECT); 
        canvas.restore();
        canvas.save();
        drawUnreadEvent(canvas);

        canvas.restore();
    }
   

    @Override
	protected void onDraw(Canvas canvas) {
    /*	if(this.getTag() instanceof ShortcutInfo) {
    		ShortcutInfo shot = (ShortcutInfo) this.getTag();
    		Log.i("zhouerlong", "short:----"+shot.title);
    	}
    	Drawable d = this.getCompoundDrawables()[1];
    	if(d instanceof FastBitmapDrawable) {
    		FastBitmapDrawable f = (FastBitmapDrawable) d;
    		if((f.getBitmap().isRecycled())) {
    			return;
    		}
    	}*/
    			
		super.onDraw(canvas);
		
		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
        ShortcutInfo shortcutInfo = (ShortcutInfo) this.getTag();
		if (launcher != null) {
			if (launcher.getWorkspace().isInDragModed()) {
				// DrawEditIcons.drawUnreadEventIfNeed(canvas, this);
				//add by zhouerlong begin 20150901
				boolean isSpringfinish = launcher.getworkspace().ismSpringLoadFinish();
				if (/*launcher.getSpringState() != Launcher.SpringState.BATCH_EDIT_APPS&&*/isSpringfinish) {
					
					if (shortcutInfo !=null &&(shortcutInfo.flags&AppInfo.DOWNLOADED_FLAG)!=0/*&& launcher.getIconState() !=IconChangeState.EDIT*/) {
//						DrawEditIcons.drawStateIcon(canvas, this,R.drawable.ic_launcher_delete_holo,w,h,anim.getOuterRingSize());
						
					}
					
					if (launcher.getSpringState() == Launcher.SpringState.BATCH_EDIT_APPS&&shortcutInfo !=null &&shortcutInfo.mItemState == ItemInfo.State.BATCH_SELECT_MODEL/*&& launcher.getIconState() !=IconChangeState.EDIT*/) {

						int w = FolderIcon.getIndicatorSize(mContext);
						int h = FolderIcon.getIndicatorSize(mContext);
						DrawEditIcons.drawStateIconForBatch(canvas, this,R.drawable.in_use,w,h,1f);//此处为画文件夹编辑小图标
					}
				}else {
					
				//add by zhouerlong end 20150901
				}
			}else {
				if (launcher.getSpringState() != Launcher.SpringState.BATCH_EDIT_APPS) {
					if (shortcutInfo != null
							&& (shortcutInfo.flags & AppInfo.DOWNLOADED_FLAG) != 0) {
						/*DrawEditIcons.drawStateIcon(canvas, this,
								R.drawable.ic_launcher_delete_holo, w, h,
								anim.getOuterRingSize());*/

					}
				}
			}

			if (shortcutInfo !=null &&shortcutInfo.select) {
//				DrawEditIcons.drawStateIcon(canvas, this,R.drawable.in_use,w,h,1f);//此处为画文件夹编辑小图标
			}
			if (shortcutInfo!=null&&shortcutInfo.firstInstall==1) {
				DrawEditIcons.drawFirstInstall(canvas, this,R.drawable.first_install,15,15);
			}
		}
	}

    
	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mBackground != null) mBackground.setCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBackground != null) mBackground.setCallback(null);
    }

    @Override
    public void setTextColor(int color) {
        mTextColor = color;
        super.setTextColor(color);
    }

    public void setShadowsEnabled(boolean enabled) {
        mShadowsEnabled = enabled;
        getPaint().clearShadowLayer();
        invalidate();
    }

    public	void  initThemeMaskBitmap() {
		
	}
    public void setTextVisibility(boolean visible) {
        Resources res = getResources();
        if (visible) {
            super.setTextColor(mTextColor);
        } else {
            super.setTextColor(res.getColor(android.R.color.transparent));
        }
        mIsTextVisible = visible;
    }

    public boolean isTextVisible() {
        return mIsTextVisible;
    }

    @Override
    protected boolean onSetAlpha(int alpha) {
        if (mPrevAlpha != alpha) {
            mPrevAlpha = alpha;
            super.onSetAlpha(alpha);
        }
        return true;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }
	
	
	// BEGIN add by ouyangjin at 2015-11-29 下午12:58:38 for 更换主题
	@Override
	public void onThemeChange() {
		Object itemObject = getTag();
        final IconCache cache = LauncherAppState.getInstance().getIconCache();
        if (itemObject instanceof ShortcutInfo) {
            final ShortcutInfo info = (ShortcutInfo) itemObject;
            info.updateIcon(cache);
            final Bitmap b = info.getIcon(cache);
            mHandler.post(new Runnable() {
				@Override
				public void run() {

		            if(info.fromAppStore ==1) {
		            	if (LqShredPreferences.isLqtheme(mContext)) {
		            		initThemeMaskBitmap();
		        		}
		        		loadImage(info,cache);
		            }else {
		            	if(Launcher.isSupportIconSize) {
							setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(b), null, null);
		            	}else {
							setCompoundDrawables(null, Utilities.createIconDrawable(b)/*new FastBitmapDrawable(b)*/, null, null);
		            	}
//						setCompoundDrawables(null, Utilities.createIconDrawable(b)/*new FastBitmapDrawable(b)*/, null, null);
						if(mFolderCallback!=null){
							mFolderCallback.updateViewAfterChildThemeChange();
						}
		            }
				}
			});
            
            
        }
	}
	  public void setThemeChangeCallback(OLThemeChangeCallbacks callbacks){
		  mFolderCallback=callbacks;
	    }
	  public void finalize() throws Throwable {
	        super.finalize();
	        OLThemeNotification.unRegisterThemeChange(this, this);
	    }
	// END add by ouyangjin at 2015-11-29 下午12:58:38 for 更换主题
	
    
    ///: Added for MTK unread message feature.@{
    private void drawUnreadEvent(Canvas canvas){
        MTKUnreadLoader.drawUnreadEventIfNeed(canvas, this);
    }
    ///: @}
	//add by zhouerlong 20150808 begin
    /**
     * 未完善。。卸载小图标以动画的方式出来
     */
    IconUninstallIndicatorAnim anim = new IconUninstallIndicatorAnim();

	@Override
	public void onChanged(StateInfo p) {
		this.requestLayout();

		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		if (launcher.getworkspace().isInDragModed()&p.state) {

			if (launcher.getIconState() == IconChangeState.EDIT) {

				anim.animateToIconIndcatorDraw(this,1,0f,p);
			}else {

				anim.animateToIconIndcatorDraw(this,0,1f,p);
			}
		}else if (!p.state){
			anim.animateToIconIndcatorDraw(this,1f,0f,p);
		}else if (p.lasted){
			launcher.getworkspace().OnSpringFinish(true);
		}
		
	
	}

	/*@Override
	public void requestLayout() {
		// TODO Auto-generated method stub
		super.requestLayout();
		ItemInfo tag = (ItemInfo) this.getTag();
//		Log.i("zhouerlong", "tititle 这个被刷新了 requestLayout"+(tag !=null?tag.title:null));
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		ItemInfo tag = (ItemInfo) this.getTag();
//		Log.i("zhouerlong", "tititle 这个被刷新了 invalidate"+(tag !=null?tag.title:null));
		super.invalidate();
	}*/

	//add by zhouerlong 20150808 end
}
