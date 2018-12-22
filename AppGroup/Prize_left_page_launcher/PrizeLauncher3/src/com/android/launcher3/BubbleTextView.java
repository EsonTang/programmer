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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.TextView;

import com.android.download.DownLoadService;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.Launcher.IconChangeState;
import com.android.launcher3.nifty.IconUninstallIndicatorAnim;
import com.android.launcher3.nifty.NiftyObserables;
import com.android.launcher3.nifty.NiftyObservers;
import com.android.launcher3.view.ExplosionView;
import com.android.launcher3.view.PrizeBubbleTextView;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.OLThemeChangeListener;
import com.lqsoft.lqtheme.OLThemeNotification;
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
	private int oldUnreadNum;
	//add by zhouerlong 20150808 begin

	public BubbleTextView(Context context) {
        super(context);
        init();
    }
	
	
	

    private OLThemeChangeCallbacks mFolderCallback;
	private Handler mHandler;
	private boolean mIsUnInstallShortCut=false;
    public interface OLThemeChangeCallbacks {
  	   public void updateViewAfterChildThemeChange();
     }
    //end add by ouyangjin for lqtheme

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    
	@Override
	@RemotableViewMethod
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		super.setVisibility(visibility);
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
        
        String color =Utilities.getDefaultTextColor();
        if(color!=null) {
            setTextColor(android.graphics.Color.parseColor(color));
        }else {
          setTextColor(getResources().getColor(R.color.workspace_icon_text_color));
        }
    }
    

    private void init() {
    	//begin add by ouyangjin for lqtheme
    	OLThemeNotification.getInstance().registerThemeChange(this, this, null);
    	if(Launcher.isSupportObs) {
    	NiftyObserables.getInstance().registerObserver(this);
    	}
    	

        if(getContext() instanceof Launcher) {
        	Launcher l = (Launcher) getContext();
			if (l.getworkspace().isInSpringLoadMoed()) {
				this.setScaleX(1f);
				this.setScaleY(1f);
				this.setAlpha(1f);
				StateInfo p = new StateInfo();
				p.state = true;
				onChanged(p);
			}
        	
        }
    	mHandler = new Handler();
    	//end add by ouyangjin for lqtheme
        mLongPressHelper = new CheckLongPressHelper(this);
        mBackground = getBackground();

        mOutlineHelper = HolographicOutlineHelper.obtain(getContext());

        final Resources res = getContext().getResources();
        mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor =
            res.getColor(R.color.outline_color);

//        setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
    }
    
    public void setProgress(int currentProgress) {
    	
    }
    public void applyFromShortcutInfo(ShortcutInfo info, final IconCache iconCache) {
    	Bitmap b=null;
        final LauncherAppState app = LauncherAppState.getInstance();
        

    	mIsUnInstallShortCut =isUninstallFromWorkspace(info);
    	if(info.fromAppStore ==1) {
            		initThemeMaskBitmap();
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
    	if(info.intent!=null&&info.intent.getComponent()!=null) {
    		ComponentName target = info.intent.getComponent();
    		LinkedHashMap<ComponentName, String> targetList = LauncherAppState.getInstance().getLauncehrApplication().mDefault_language;
    		if(targetList!=null&&targetList.containsKey(target)) {
    			try {
        			info.title=toFormatLanguage(targetList.get(target),info.title.toString());
				} catch (Exception e) {
					// TODO: handle exception
				}
    			
    		}
    	}
    	if(info.fromAppStore==1) {
    		 this.setProgress(info.progress);
    		 switch (info.down_state) {
			case  DownLoadService.APP_STATE_INSTALLING:
				info.title = mContext.getString(R.string.installing);
				break;
			case  DownLoadService.STATE_DOWNLOAD_WAIT:
				info.title = mContext.getString(R.string.waiting);
				break;
			case  DownLoadService.STATE_DOWNLOAD_START_LOADING:
				info.title = mContext.getString(R.string.downloading);
				break;
			case  DownLoadService.STATE_DOWNLOAD_PAUSE:
				info.title = mContext.getString(R.string.pause);
				break;

			default:
				break;
			}
    	}
        setText(info.title);
        setTag(info);
    }
    
    
    private String toFormatLanguage(String name,String defaultName) {
    	String result=defaultName;
        String t ="CN;TW;US";
        String t1 ="CN;HK;US";
        String t2 ="CN;TW;GB";
        List<String> languages = Arrays.asList(t.split(";"));
        String []names = name.split(";");
        int i=languages.indexOf(Launcher.locale);
        if(i==-1) {
        	languages = Arrays.asList(t1.split(";"));
        	i=languages.indexOf(Launcher.locale);
        }
        if(i==-1) {
        	languages = Arrays.asList(t2.split(";"));
        	i=languages.indexOf(Launcher.locale);
        }
        if(i !=-1) {
        	result=	names[i];
        }
        
        return result;
        
        
    
    }
    public void loadImage(final ShortcutInfo info,final IconCache iconCache) {
    	
    	try {
    		BubbleTextView.this.setCompoundDrawablesWithIntrinsicBounds(
					null,
					new FastBitmapDrawable(UILimageUtil
							.getDefaultBitmap(mContext)), null, null);
        	
		} catch (Exception e) {
			// TODO: handle exception
		}
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
    	Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
        if (isPressed()) {
            // In this case, we have already created the pressed outline on ACTION_DOWN,
            // so we just need to do an invalidate to trigger draw
            if (!mDidInvalidateForPressedState) {
            	try {
                	int x=(int) launcher.getDragController().getmMotionEvent().getRawX();
                	int y = (int) launcher.getDragController().getmMotionEvent().getRawY();
                	if(launcher.findIconArean(x, y, this)) {
                        setCellLayoutPressedOrFocusedIcon();
                	}
				} catch (Exception e) {
                    setCellLayoutPressedOrFocusedIcon();
				}
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
//                setCellLayoutPressedOrFocusedIcon();
            } else {
            	//add by huanglingjun
            	mPressedOrFocusedBackground = null;
                setCellLayoutPressedOrFocusedIcon();
            }
            final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
            if (!backgroundEmptyBefore && backgroundEmptyNow) {
            	
            	try {
                	int x=(int) launcher.getworkspace().getmMotionEvent().getRawX();
                	int y = (int) launcher.getworkspace().getmMotionEvent().getRawY();
                	if(launcher.findIconArean(x, y, this)) {
                        setCellLayoutPressedOrFocusedIcon();
                	}
				} catch (Exception e) {
	                setCellLayoutPressedOrFocusedIcon();
				}
            }
        }

        Drawable d = mBackground;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }

        super.drawableStateChanged();
    }
    
    /**  
     *   设置滤镜
     */
    private void setFilter() {
    	
        //先获取设置的src图片
        Drawable drawable=this.getCompoundDrawables()[1];
        
        
        //当src图片为Null，获取背景图片
        if(mPressedOrFocusedBackground != null)  {
            if(drawable!=null){
                //设置滤镜
                drawable.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);  
            }
        }else {

            if(drawable!=null){

        		ItemInfo info = (ItemInfo) getTag();
        		if (info != null && info.fromAppStore != 1) {
                //设置滤镜
                drawable.setColorFilter(null); 
        		}
            }
        }
        this.invalidate();
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
                    Launcher launcher = null;
            		if (this.getContext() instanceof Launcher) {
            			launcher = (Launcher) this.getContext();

            		}
                	try {
                    	int x=(int) launcher.getDragController().getmMotionEvent().getRawX();
                    	int y = (int) launcher.getDragController().getmMotionEvent().getRawY();
                    	if(launcher.findIconArean(x, y, this)) {
                            setCellLayoutPressedOrFocusedIcon();
                    	}
    				} catch (Exception e) {
                        setCellLayoutPressedOrFocusedIcon();
    				}
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
                setFilter();
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
            drawUnreadEvent(canvas,anim.getUnreadOuterRingSize());
            canvas.restore();
            ///: @}
            return;
        }
		
	// add by huanglingjun 只针对电话做处理
		/*if ("com.android.dialer".equals(MTKUnreadLoader.packageName)) {
			ItemInfo info = (ItemInfo) getTag();
			if (info != null) {
				if (oldUnreadNum == 0) {
					oldUnreadNum = info.unreadNum;
				}
				if (oldUnreadNum > 0 && (oldUnreadNum - info.unreadNum) > 0) {
					oldUnreadNum = info.unreadNum;
					return;
				}
			}
		}*/
        super.draw(canvas);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(),
                getScrollX() + getWidth(),
                getScrollY() + getHeight(), Region.Op.INTERSECT); 
        canvas.restore();
        canvas.save();
        drawUnreadEvent(canvas,anim.getUnreadOuterRingSize());

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
			if (launcher.getWorkspace().isInSpringLoadMoed()) {
				// DrawEditIcons.drawUnreadEventIfNeed(canvas, this);
				//add by zhouerlong begin 20150901
				boolean isSpringfinish = launcher.getworkspace().ismSpringLoadFinish();
				if (/*launcher.getSpringState() != Launcher.SpringState.BATCH_EDIT_APPS&&*/launcher.getworkspace().isInSpringLoadMoed()) {
					
					
					drawUninstallIndicator(shortcutInfo,canvas);
					
					if (launcher.getSpringState() == Launcher.SpringState.BATCH_EDIT_APPS&&shortcutInfo !=null &&shortcutInfo.mItemState == ItemInfo.State.BATCH_SELECT_MODEL/*&& launcher.getIconState() !=IconChangeState.EDIT*/) {

						int w = FolderIcon.getIndicatorSize(mContext);
						int h = FolderIcon.getIndicatorSize(mContext);
						DrawEditIcons.drawStateIconForBatch(canvas, this,R.drawable.in_use,(int)(w*Workspace.mNewScale),(int)(h*Workspace.mNewScale),1f);//此处为画文件夹编辑小图标
					}
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
				
//				else {
				if(launcher.getworkspace().isExitedSpringMode()) {
					drawUninstallIndicator(shortcutInfo,canvas);
				}
//				}
			}

			if (shortcutInfo !=null &&shortcutInfo.select) {
//				DrawEditIcons.drawStateIcon(canvas, this,R.drawable.in_use,w,h,1f);//此处为画文件夹编辑小图标
			}
			/*Prize_修改bug卸载新安装且未打开过的应用，在卸载提示界面仍显示有新安装未打开过的标识__by_fuqiang_20160301_begin*/
			if (shortcutInfo!=null&&shortcutInfo.firstInstall==1 && !(getParent() instanceof ExplosionView)) {
				DrawEditIcons.drawFirstInstall(canvas, this,R.drawable.first_install);
			}
			/*Prize_修改bug卸载新安装且未打开过的应用，在卸载提示界面仍显示有新安装未打开过的标识__by_fuqiang_20160301_end*/
		}
	}
    
    
    private boolean isUninstallFromWorkspace(ShortcutInfo shortcut) {
        if (AppsCustomizePagedView.DISABLE_ALL_APPS ) {
            if (shortcut.intent != null && shortcut.intent.getComponent() != null) {
                Set<String> categories = shortcut.intent.getCategories();
                boolean includesLauncherCategory = false;
                if (categories != null) {
                    for (String category : categories) {
                        if (category.equals(Intent.CATEGORY_LAUNCHER)) {
                            includesLauncherCategory = true;
                            break;
                        }
                    }
                }
                return includesLauncherCategory;
            }
        }
        return false;
    }
    
    private void drawUninstallIndicator(ShortcutInfo shortcutInfo,Canvas canvas) {
		if (shortcutInfo !=null &&(shortcutInfo.flags&AppInfo.DOWNLOADED_FLAG)!=0/*&& launcher.getIconState() !=IconChangeState.EDIT*/) {
			Drawable d = getContext().getDrawable(R.drawable.ic_launcher_delete_holo);
			int w = d.getIntrinsicWidth();
			int h = d.getIntrinsicWidth();

			if(getContext() instanceof Launcher) {
			Launcher	l = (Launcher) getContext();
			List ls = l.getActives();

			 if( ls!= null && shortcutInfo instanceof ShortcutInfo){
		    		if( ls.contains(shortcutInfo.packageName)) {
			    		return;
		    		}
		    	}
			}
			DrawEditIcons.drawStateIcon(canvas, this,R.drawable.ic_launcher_delete_holo,w,h,anim.getOuterRingSize());
			
		}else if(!mIsUnInstallShortCut) {
		/*	Drawable d = getContext().getDrawable(R.drawable.ic_launcher_delete_holo);
			int w = d.getIntrinsicWidth();
			int h = d.getIntrinsicWidth();
			DrawEditIcons.drawStateIcon(canvas, this,R.drawable.ic_launcher_delete_holo,w,h,anim.getOuterRingSize());*/
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
//        mHandler=null;
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
	public void onThemeChange(boolean end) {
		Object itemObject = getTag();
        final IconCache cache = LauncherAppState.getInstance().getIconCache();
        if (itemObject instanceof ShortcutInfo) {
            final ShortcutInfo info = (ShortcutInfo) itemObject;
             Bitmap b=null;
             if(info.fromAppStore!=1) {
            if(info.intent.getComponent()!=null) {
                info.updateIcon(cache);
                b = info.getIcon(cache);
            }else if(info.mIcon!=null){
            	b=IconCache.getLqIcon(null, info.mIcon, true, "");
            	if(b.getHeight()!=Utilities.sIconTextureHeight) {

            		b=ImageUtils.resizeIcon(b, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
            	}
            }else {
            	b = info.getIcon(cache);
            }
             
             }
            applyThemeIcon(info,b,cache);

            System.gc();

			if (Utilities.supportTestTheme()) {
            if(end) {
            	((Launcher)getContext()).end();
            }
			}
        }
        
        if(this instanceof PrizeBubbleTextView) {
        	PrizeBubbleTextView  prize = (PrizeBubbleTextView) this;
        	prize.updateDeskcomponent();
        }
	}
	
		void applyThemeIcon(final ShortcutInfo info ,final Bitmap b,final IconCache cache) {
//			if(mHandler==null) {
//				mHandler = new Handler();
//			}
            mHandler.post(new Runnable() {
				@Override
				public void run() {
					synchronized (BubbleTextView.this) {
							setText(info.title);
		            if(info.fromAppStore ==1) {
		            	if (LqShredPreferences.isLqtheme(mContext)) {
		            		initThemeMaskBitmap();
		        		}
		        		loadImage(info,cache);
		            }else {
		            	if(Launcher.isSupportIconSize) {
		            		Bitmap bm=b;
		            		if(bm!=null&&bm.getHeight()!=Utilities.sIconTextureHeight) {

								setCompoundDrawables(null, Utilities.createIconDrawable(b)/*new FastBitmapDrawable(b)*/, null, null);
		            		}else {
//			            		ImageUtils.savePNG_After(bm, "syj.png")
			            		FastBitmapDrawable mfast = new FastBitmapDrawable(bm);
								setCompoundDrawablesWithIntrinsicBounds(null, mfast, null, null);
								mfast=null;
		            		}
							
		            	}else {
		            		
							setCompoundDrawables(null, Utilities.createIconDrawable(b)/*new FastBitmapDrawable(b)*/, null, null);
		            	}
//						setCompoundDrawables(null, Utilities.createIconDrawable(b)/*new FastBitmapDrawable(b)*/, null, null);
						if(mFolderCallback!=null){
							mFolderCallback.updateViewAfterChildThemeChange();
						}
		            }
				}
//					mHandler=null;
				}
			});
            
			
		}
	  public void setThemeChangeCallback(OLThemeChangeCallbacks callbacks){
		  mFolderCallback=callbacks;
	    }
	  public void finalize() throws Throwable {
	        super.finalize();
	        mHandler=null;
	        OLThemeNotification.getInstance().unRegisterThemeChange(this, this);
	        NiftyObserables.getInstance().unregisterObserver(this);
	        
	    }
	// END add by ouyangjin at 2015-11-29 下午12:58:38 for 更换主题
	
    
    ///: Added for MTK unread message feature.@{
    private void drawUnreadEvent(Canvas canvas,float progress){
        MTKUnreadLoader.drawUnreadEventIfNeed(canvas, this,progress);
    }
    ///: @}
	//add by zhouerlong 20150808 begin
    /**
     * 未完善。。卸载小图标以动画的方式出来
     */
    IconUninstallIndicatorAnim anim = new IconUninstallIndicatorAnim();

	AnimTool tool;
	@Override
	public void onChanged(StateInfo p) {
		
		if (tool == null) {
			tool = new AnimTool();
		}
			if(p.state) {
				tool.start(this);
			}else {
				tool.cacel();
			}
		
		
		this.requestLayout();

		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		if (launcher.getworkspace()!=null&&launcher.getworkspace().isInSpringLoadMoed()) {
				if(p.state) {
					if(anim.getOuterRingSize()!=1f) {
						anim.animateToIconIndcatorDraw(this,0,1f,p,false);
					}
				}
			
		}else {

			anim.animateToIconIndcatorDraw(this,1,0f,p,false);
			
		}
			/*else if (!p.state){
		}
			anim.animateToIconIndcatorDraw(this,1f,0f,p);
		}else if (p.lasted){
			launcher.getworkspace().OnSpringFinish(true);
		}*/
		
	
	}
	
	
	public void onChanged(int unreadNumber) {
		this.requestLayout();
		anim.animateToIconIndcatorDraw(this,0,1f,null,true);
	
	}


	@Override
	public AnimTool getAinmTool() {
		return tool;
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
