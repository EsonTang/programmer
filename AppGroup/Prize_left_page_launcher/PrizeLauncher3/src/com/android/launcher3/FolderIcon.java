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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.download.DownLoadTaskInfo;
import com.android.launcher3.BubbleTextView.OLThemeChangeCallbacks;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.FolderInfo.FolderListener;
import com.android.launcher3.FolderInfo.State;
import com.android.launcher3.nifty.IconUninstallIndicatorAnim;
import com.android.launcher3.nifty.NiftyObserables;
import com.android.launcher3.nifty.NiftyObservers;
import com.android.launcher3.view.PrizeSwitchView;
import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.OLThemeChangeListener;
import com.lqsoft.lqtheme.OLThemeNotification;
import com.mediatek.launcher3.ext.LauncherLog;
//add by zhouerlong
//A by zhouerlong

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends LinearLayout implements FolderListener ,NiftyObservers ,OLThemeChangeListener,OLThemeChangeCallbacks{
    private static final String TAG = "FolderIcon";

    private Launcher mLauncher;
    private Folder mFolder;
    
    private View mRecommdView;
    
    private View mSwitchView;
    public View getSwitchView() {
		return mSwitchView;
	}

	public void setSwitchView(View mSwitchView) {
		this.mSwitchView = mSwitchView;
	}

	public View getRecommdView() {
		return mRecommdView;
	}

	private FolderInfo mInfo;
    
    private int mCurrentLayer=-1;
    
    private static float mDis=1f;
    private static boolean sStaticValuesDirty = true;

    private CheckLongPressHelper mLongPressHelper;
    
    private Handler h = new Handler();
    
    private boolean isDrawableLayer=true;
    
    private Runnable  mRunnable = new Runnable() {
		
		@Override
		public void run() {
			isDrawableLayer=true;
			h.removeCallbacks(mRunnable);
			invalidate();
		}
	};
    

	@Override
	public void draw(Canvas canvas) {
		 try {  
				super.draw(canvas); 
	        } catch (Exception e) {  
	            System.out  
	                    .println("MyImageView  -> onDraw() Canvas: trying to use a recycled bitmap");  
	            e.printStackTrace();
	        }  
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		/*
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		Drawable d = this.getContext().getDrawable(
				R.drawable.ic_launcher_delete_holo);
		int w = d.getIntrinsicWidth();
		int h = d.getIntrinsicHeight();
		FolderInfo shortcutInfo = (FolderInfo) this.getTag();
		if (launcher.getSpringState() == Launcher.SpringState.BATCH_EDIT_APPS
				&& shortcutInfo != null
				&& shortcutInfo.mItemState == ItemInfo.State.BATCH_SELECT_MODEL) {
			DrawEditIcons.drawStateIconForBatch(canvas, this,
					R.drawable.in_use, w, h, 1f);// 此处为画文件夹编辑小图标
		}

	*/}

	//add by zhouerlong 20150808 begin
	private NiftyObserables mNiftyObserables = null;

	public NiftyObserables getmNiftyObserables() {
		return mNiftyObserables;
	}

	public void setmNiftyObserables(NiftyObserables mNiftyObserables) {
		this.mNiftyObserables = mNiftyObserables;
	}
	//add by zhouerlong 20150808 end

	// The number of icons to display in the
    private   int NUM_ITEMS_IN_PREVIEW = 4;
//add by zhouerlong
    private static final int CONSUMPTION_ANIMATION_DURATION = 500;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 300;

    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.15f;

    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;

    // The amount of vertical spread between items in the stack [0...1]
    private   float PERSPECTIVE_SHIFT_FACTOR = 1.00f;//这个是表示越大越往内缩 0.24f;
//add by zhouerlong

    // Flag as to whether or not to draw an outer ring. Currently none is designed.
    public static final boolean HAS_OUTER_RING = true;

    // The degree to which the item in the back of the stack is scaled [0...1]
    // (0 means it's not scaled at all, 1 means it's scaled to nothing)
    private static  float PERSPECTIVE_SCALE_FACTOR = 0.60f;//这个是改变图标大小
//add by zhouerlong

    public static Drawable sSharedFolderLeaveBehind = null;

    public ImageView mPreviewBackground;
    private TextView mFolderName;

    FolderRingAnimator mFolderRingAnimator = null;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    private int mTotalWidth = -1;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mMaxPerspectiveShift;
    boolean mAnimating = false;
    private Rect mOldBounds = new Rect();

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();

	private CellLayout mLayout;

	public Bitmap bitmap;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
    	OLThemeNotification.getInstance().registerThemeChange(this, this, null);
    	if(Launcher.isSupportObs) {
    	NiftyObserables.getInstance().registerObserver(this);
    	  if(getContext() instanceof Launcher) {
          	Launcher l = (Launcher) getContext();
  			if (l.getworkspace().isInSpringLoadMoed()) {

  				StateInfo p = new StateInfo();
  				p.state = true;
  				onChanged(p);
  			}
          	
          }
    	}
        mLongPressHelper = new CheckLongPressHelper(this);
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.isSmall();
    }
    
    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }
        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        icon.setClipToPadding(false);
        
        try {
            if(folderInfo.title_id!=-1) {
            	String first = String.valueOf(folderInfo.title_id);
        		ComponentName target = new ComponentName(first,first);
        		LinkedHashMap<ComponentName, String> targetList = LauncherAppState.getInstance().getLauncehrApplication().mDefault_language;
        		if(targetList!=null&&targetList.containsKey(target)) {
        			try {
        				folderInfo.title=Utilities.toFormatLanguage(targetList.get(target),folderInfo.title.toString());
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
        			
        		}
        	}
		} catch (Exception e) {
			// TODO: handle exception
		}
        icon.mFolderName =  (TextView) icon.findViewById(R.id.folder_icon_name);
      
        icon.mFolderName.setText(folderInfo.title);
        icon.mFolderName.setTextSize(TypedValue.COMPLEX_UNIT_PX, Launcher.textSize);
        mDis = getDisp(launcher);
//        icon.mFolderName.setSingleLine(launcher.isHotseatLayout(group));
        if (folderInfo.title_id != -1&&folderInfo.title_id != 0) {
        	try {
        		if(!launcher.getString(folderInfo.title_id).equals("false")) {
                	icon.mFolderName.setText(folderInfo.title_id);
                	folderInfo.title = launcher.getText(folderInfo.title_id);
        		}
			} catch (Exception e) {
				e.printStackTrace();
			}
        }//add by zhouerlong begin 
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
        icon.mPreviewBackground.setTag(folderInfo);
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        
        // Offset the preview background to center this view accordingly
        LinearLayout.LayoutParams lp =
                (LinearLayout.LayoutParams) icon.mPreviewBackground.getLayoutParams();
        lp.topMargin = grid.folderBackgroundOffset;
        lp.width = grid.folderIconSizePx;
        lp.height = grid.folderIconSizePx;
        Drawable bg = null;//ThemeInfoUtils.getThemeFolderBg(launcher);
        if (bg!=null) {
            icon.mPreviewBackground.setBackground(bg);
        }

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        View   recommd;
        if(Launcher.isf) {
             recommd = RecommdLinearLayout.fromXml(launcher);
        }else {
        	 recommd = PrizeRecommdView.fromXml(launcher);
        }
        

        PrizeSwitchView  switchView = PrizeSwitchView.fromXml(launcher);
        switchView.setFolderInfo(folderInfo);
        

//        PrizeRecommdView recommd = (PrizeRecommdView) view.findViewById(R.id.recommd);
        
        
      /*  if (Folder.pagers) {
            folder.getViewPager().setBackground(launcher.getDrawable(R.drawable.folder_cell_bg1));
        }else {
            folder.mScrollView.setBackground(launcher.getDrawable(R.drawable.folder_cell_bg1));
        }*/
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);
        icon.mFolder = folder;
        icon.mRecommdView = recommd;
        icon.mSwitchView = switchView;
        

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);


        //begin add by ouyangjin for lqtheme
         if(LqShredPreferences.isLqtheme(launcher)){
         	if(LqService.getInstance() != null){
         		Bitmap bitmap = IconCache.getFolderIcon("");
         		if(bitmap!=null) {


            		bitmap=ImageUtils.resizeIcon(bitmap, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
    				LinearLayout.LayoutParams lps = (LayoutParams) icon.mPreviewBackground.getLayoutParams();
    				if(Launcher.isSupportIconSize) {
        				lps.width = bitmap.getWidth();
        				lps.height = bitmap.getHeight();
    				}else {
        				lps.width = Utilities.sIconTextureHeight;//bitmap.getWidth();
        				lps.height = Utilities.sIconTextureHeight;//bitmap.getHeight();
    				}
    				lps.bottomMargin=4;
         		}
         		
         		 icon.mPreviewBackground.setImageBitmap(bitmap);
         	}
         }
         //end add by ouyangjin for lqtheme

        return icon;
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static Drawable sSharedOuterRingDrawable = null;
    public static Drawable sSharedInnerRingDrawable = null;
    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        private CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
//A by zhouerlong
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (true) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables on non-UI thread "
                            + Thread.currentThread());
                }

                LauncherAppState app = LauncherAppState.getInstance();
                DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
                sPreviewSize = grid.folderIconSizePx;
                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
                
                Drawable folder_icon = null;
    			if (LqShredPreferences.isLqtheme(launcher)) {
    				Bitmap bitmap = IconCache.getFolderIcon("");
    				if(bitmap !=null) {
                		bitmap=ImageUtils.resizeIcon(bitmap, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
                        folder_icon = ImageUtils.bitmapToDrawable(bitmap);
    				}
    			}

                sSharedOuterRingDrawable = folder_icon!=null?folder_icon:ImageUtils.folder_bg;//m by zhouerlong
                sSharedInnerRingDrawable = folder_icon!=null?folder_icon:ImageUtils.folder_bg;//m by zhouerlong
                sSharedFolderLeaveBehind = folder_icon!=null?folder_icon:ImageUtils.folder_bg;//m by zhouerlong


                sStaticValuesDirty = false;
            }
        }
        //此方法是将文件夹边框背景放大动画
        public void animateToAcceptState() { 
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel(); 
            }
            mAcceptAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mAcceptAnimator.setDuration(Utilities.getRevertDuration());

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;//这个是放到的值
                    mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate(); //刷新celllayout
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }
        
        

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mNeutralAnimator.setDuration(Utilities.getRevertDuration());

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                        mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

    Folder getFolder() {
        return mFolder;
    }

    FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        boolean isExits = false;
        if (item instanceof ShortcutInfo) {
        	ShortcutInfo it = (ShortcutInfo) item;
//        	isExits = mInfo.getComponentNames().contains(it.intent.getComponent());
        }//这里是判断当前文件夹内部是否已经有了相同的icons 
        return !mFolder.isDestroyed() && willAcceptItem(item)/*&& !isExits*/;
    }

    public void addItem(ShortcutInfo item) {
        mInfo.add(item);
    }

    public void onDragEnter(Object dragInfo) {//拖动进入文件夹
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return; //判断是否能拖入
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();//读取参数
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();//这个表示进入访问动画 打开的动作
        layout.showFolderAccept(mFolderRingAnimator);
        //拖入文件夹  OnDragEnter 
    }

    public void onDragOver(Object dragInfo) {
    }
    
    public  boolean isCreate=false;

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
            final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
            float scaleRelativeToDragLayer, Runnable postAnimationRunnable,DragObject d) {

        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = ((TextView) destView).getCompoundDrawables()[1];
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, Utilities.getRevertDuration(), false, null);
        addItem(destInfo);
        mLauncher.getworkspace().isCreate=true;
        // This will animate the dragView (srcView) into the new folder
        Rect murect = new Rect(dstRect);
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable, null);
        if (d != null) {
            multipleOnDrop(d,murect);
        }
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = ((TextView) finalView).getCompoundDrawables()[1];
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(), 
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        onDragExit();
    }

    public void onDragExit() {
        mFolderRingAnimator.animateToNaturalState();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
            float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
            DragObject d) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDrop: item = " + item + ", animateView = "
                    + animateView + ", finalRect = " + finalRect + ", scaleRelativeToDragLayer = "
                    + scaleRelativeToDragLayer + ", index = " + index + ", d = " + d);
        }

        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                      center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, Utilities.getRevertDuration(),
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null,0,true,null);
            addItem(item);
            mHiddenItems.add(item);
            mFolder.hideItem(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    mFolder.showItem(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
        if(LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDrop: DragObject = " + d);
        }

        ShortcutInfo item;
        if (d.dragInfo instanceof AppInfo) {
            // Came from all apps -- make a copy
            item = ((AppInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
        multipleOnDrop(d,null);
       }
    

	
	/**批量整理 拖入文件夹时候 批量移入到文件夹方法
	 * @param d
	 * @param destRect 目标位置
	 */
	public void multipleOnDrop(DragObject d,Rect destRect) {
		MultipleDrop mulDrop;

		HashMap<Long, View> mulChilds = mLauncher.getworkspace()
				.getMultipleDragViews();

		Iterator<Long> it = mulChilds.keySet().iterator();
		while (it.hasNext()) {
			Long id = it.next();

			View child = mulChilds.get(id);
			ShortcutInfo info = (ShortcutInfo) child.getTag();
			DragView dv = d.dragViews.get(child);

			mulDrop = new MultipleDrop();
			mulDrop.onDrop(info, dv, destRect, 1.0f, mInfo.contents.size(),
					d.postAnimationRunnable, d);
			info.mItemState = ItemInfo.State.NONE;
			if (mulChilds.containsKey(info.id)) {
				it.remove();
			}
			mLauncher.getworkspace().getParentCellLayoutForView(child)
					.removeView(child);
		}
	}

	/**
	 * @author Administrator
	 *批量整理 拖动类
	 */
	class MultipleDrop {

		public void onDrop(final ShortcutInfo item, DragView animateView,
				Rect finalRect, float scaleRelativeToDragLayer, int index,
				Runnable postAnimationRunnable, DragObject d) {
			if (LauncherLog.DEBUG) {
				LauncherLog.d(TAG, "onDrop: item = " + item
						+ ", animateView = " + animateView + ", finalRect = "
						+ finalRect + ", scaleRelativeToDragLayer = "
						+ scaleRelativeToDragLayer + ", index = " + index
						+ ", d = " + d);
			}

			item.cellX = -1;
			item.cellY = -1;

			// Typically, the animateView corresponds to the DragView; however,
			// if this is being done
			// after a configuration activity (ie. for a Shortcut being dragged
			// from AllApps) we
			// will not have a view to animate
			if (animateView != null) {
				DragLayer dragLayer = mLauncher.getDragLayer();
				Rect from = new Rect();
				dragLayer.getViewRectRelativeToSelf(animateView, from);
				Rect to = finalRect;
				if (to == null) {
					to = new Rect();
					Workspace workspace = mLauncher.getWorkspace();
					// Set cellLayout and this to it's final state to compute
					// final animation locations
					workspace
							.setFinalTransitionTransform((CellLayout) getParent()
									.getParent());
					float scaleX = getScaleX();
					float scaleY = getScaleY();
					setScaleX(1.0f);
					setScaleY(1.0f);
					scaleRelativeToDragLayer = dragLayer
							.getDescendantRectRelativeToSelf(FolderIcon.this,
									to);
					// Finished computing final animation locations, restore
					// current state
					setScaleX(scaleX);
					setScaleY(scaleY);
					workspace.resetTransitionTransform((CellLayout) getParent()
							.getParent());
				}

				int[] center = new int[2];
				float scale = getLocalCenterForIndex(index, center);
				center[0] = (int) Math.round(scaleRelativeToDragLayer
						* center[0]);
				center[1] = (int) Math.round(scaleRelativeToDragLayer
						* center[1]);

				to.offset(center[0] - animateView.getMeasuredWidth() / 2,
						center[1] - animateView.getMeasuredHeight() / 2);

				float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;

				float finalScale = scale * scaleRelativeToDragLayer;
				dragLayer
						.animateView(animateView, from, to, finalAlpha, 1, 1,
								finalScale, finalScale,
								DROP_IN_ANIMATION_DURATION,
								new DecelerateInterpolator(2),
								new AccelerateInterpolator(2),
								postAnimationRunnable,
								DragLayer.ANIMATION_END_DISAPPEAR, null, 0,
								true, null);
				addItem(item);
				mHiddenItems.add(item);
				mFolder.hideItem(item);
				postDelayed(new Runnable() {
					public void run() {
						mHiddenItems.remove(item);
						mFolder.showItem(item);
						invalidate();
					}
				}, DROP_IN_ANIMATION_DURATION);
			} else {
				addItem(item);
			}
		}
	}
    
//A by zhouerlong
    private PreviewItemDrawingParams computePreviewItemDrawingParams( final int index,
            PreviewItemDrawingParams params,int def) {
    		    	if(mCurrentLayer==4) {
        		    	return computePreviewItemDrawingParams4(index,params);
    		    	}else {

        		    	return computePreviewItemDrawingParams9(index,params);
    		    	}
    }
    
	private PreviewItemDrawingParams computePreviewItemDrawingParams9(
			int index, PreviewItemDrawingParams params) {
		// add by x
		int index_order = index;
		final int previewPadding = FolderRingAnimator.sPreviewPadding;
		final int previewPaddings = (int) (previewPadding*1f);
		// add end
		index = NUM_ITEMS_IN_PREVIEW - index - 1;
		float r = (index * 1.0f) / (NUM_ITEMS_IN_PREVIEW - 1);
		float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));
		float offset = (1 - r) * mMaxPerspectiveShift;
		float scaledSize = scale * mBaselineIconSize;
		float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;
		// We want to imagine our coordinates from the bottom left, growing up
		// and to the
		// right. This is natural for the x-axis, but for the y-axis, we have to
		// invert things.
		float transY = mAvailableSpaceInPreview
				- (offset + scaledSize + scaleOffsetCorrection)
				+ getPaddingTop();
		float transX = offset + scaleOffsetCorrection;
		float totalScale = mBaselineIconScale * scale;
		final int overlayAlpha = (int) (80 * (1 - r));
		float xPad=0.2f;
		float yPad=0.2f;

		// add by x
		if (PERSPECTIVE_SHIFT_FACTOR == 0.24f) {

		} else {
			
			int previewPaddingX = previewPaddings+4;
			if (0 <= index_order && index_order < 3) { // 0 1 2
				xPad=previewPaddingX*(1-index_order);
				yPad=previewPaddings*(1+2);
				transX = index_order * mBaselineIconSize + 1 * previewPadding;
				transY = mAvailableSpaceInPreview
						- (2 * mBaselineIconSize + scaledSize + scaleOffsetCorrection)
						+ getPaddingTop() + 0.2f * mBaselineIconSize;
				

			} else if (3 <= index_order && index_order < 6) { // 3 4 5
				xPad=previewPaddingX*(1-(index_order-3));
				yPad=previewPaddings*(1-1);
				transX = (index_order - 3) * mBaselineIconSize + 1
						* previewPadding;
				// transY=1*mBaselineIconSize+9*previewPadding;
				transY = mAvailableSpaceInPreview
						- (1 * mBaselineIconSize + scaledSize + scaleOffsetCorrection)
						+ getPaddingTop() + 0.2f * mBaselineIconSize;
				
			} else if (6 <= index_order && index_order < 9) { // 6 7 8
				xPad=previewPaddingX*(1-(index_order-6));
				yPad=previewPaddings*(1-4);
				transX = (index_order - 6) * mBaselineIconSize + 1
						* previewPadding;
				// transY=2*mBaselineIconSize+9*previewPadding;
				transY = mAvailableSpaceInPreview
						- (0 * mBaselineIconSize + scaledSize + scaleOffsetCorrection)
						+ getPaddingTop() + 0.2f * mBaselineIconSize;
				
			}
			totalScale = mBaselineIconScale * 1 - 0.1f;
		}
		/*PRIZE-修改文件夹缩略图里面的图标偏左的问题--fuqiang--20160302-start
		transX+=xPad*mDis+0.3;
		PRIZE-修改文件夹缩略图里面的图标偏左的问题--fuqiang--20160302-end
		transY+=yPad*mDis + 1;*/
		// add end
		if(Launcher.scale == 2){
			transX+=xPad*mDis+2.8;
			transY+=yPad*mDis-1.5;
		}else if(Launcher.scale == 3){
			transX+=xPad*mDis+1;
			transY+=yPad*mDis-3.2;
		}else{
			transX+=xPad*mDis - 1.85;
			transY+=yPad*mDis + 5;
		}

		if (params == null) {
			params = new PreviewItemDrawingParams(transX, transY, totalScale,
					overlayAlpha);
		} else {
			params.transX = transX;
			params.transY = transY;
			params.scale = totalScale;
			params.overlayAlpha = overlayAlpha;
		}
		return params;
	}
	
	
	
	private PreviewItemDrawingParams computePreviewItemDrawingParams4(
			int index, PreviewItemDrawingParams params) {
		// add by x
		int index_order = index;
		final int previewPadding = FolderRingAnimator.sPreviewPadding;
		final int previewPaddings = (int) (previewPadding*2.5f);
		// add end
		index = NUM_ITEMS_IN_PREVIEW - index - 1;
		float r = (index * 1.0f) / (NUM_ITEMS_IN_PREVIEW - 1);
		float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));
		float offset = (1 - r) * mMaxPerspectiveShift;
		float scaledSize = scale * mBaselineIconSize;
		float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;
		// We want to imagine our coordinates from the bottom left, growing up
		// and to the
		// right. This is natural for the x-axis, but for the y-axis, we have to
		// invert things.
		float transY = mAvailableSpaceInPreview
				- (offset + scaledSize + scaleOffsetCorrection)
				+ getPaddingTop();
		float transX = offset + scaleOffsetCorrection;
		float totalScale = mBaselineIconScale * scale;
		final int overlayAlpha = (int) (80 * (1 - r));
		float xPad=0.2f;
		float yPad=0.2f;

		// add by x
		if (PERSPECTIVE_SHIFT_FACTOR == 0.24f) {

		} else {
			
			
			if (0 <= index_order && index_order < 2) { // 0 1 2
				xPad=previewPaddings*(1-index_order)+previewPadding*4;
				yPad=previewPaddings*(1-0)-previewPadding;
				transX = index_order * mBaselineIconSize + 1 * previewPadding;
				transY = mAvailableSpaceInPreview
						- (1 * mBaselineIconSize + scaledSize + scaleOffsetCorrection)
						+ getPaddingTop() + 0.0f * mBaselineIconSize;
				

			} else if (2 <= index_order && index_order < 4) { // 3 4 5
				xPad=previewPaddings*(1-(index_order-2))+previewPadding*4;
				yPad=previewPaddings*(1-1)-previewPadding;
				transX = (index_order - 2) * mBaselineIconSize + 1
						* previewPadding;
				// transY=1*mBaselineIconSize+9*previewPadding;
				transY = mAvailableSpaceInPreview
						- (0 * mBaselineIconSize + scaledSize + scaleOffsetCorrection)
						+ getPaddingTop() + 0.0f * mBaselineIconSize;
				
			} 
			totalScale = mBaselineIconScale * 1 - 0.1f;
		}
		if(Launcher.scale == 2){
			transX+=xPad*mDis-4;
			transY+=yPad*mDis ;
		}else if(Launcher.scale == 3){
			transX+=xPad*mDis-10;
			transY+=yPad*mDis + 3;
		}else if(Launcher.scale == 1.5){
			transX+=xPad*mDis-6;
			transY+=yPad*mDis + 2;
		}else{
			transX+=xPad*mDis -11;
			transY+=yPad*mDis + 8;
		}
		// add end

		if (params == null) {
			params = new PreviewItemDrawingParams(transX, transY, totalScale,
					overlayAlpha);
		} else {
			params.transX = transX;
			params.transY = transY;
			params.scale = totalScale;
			params.overlayAlpha = overlayAlpha;
		}
		return params;
	}

    
//add by zhouerlong
    
    
	private static float getDisp(Context ctx) {
		DisplayMetrics metric = ctx.getResources().getDisplayMetrics();
		int a = metric.densityDpi;
		float target = 1f;
		// 忽略ldpi与mdpi， 小于280为hdpi, 大于等于280且小于等于360为xhdpi, 大于360的为xxhdpi
		if (a < 280) {
			target = 1f;
		} else if (a >= 280 && a <= 360) {

			target = 1f;
		} else {
			return 0.5f;
		}
		metric = null;
		return target;
	}
	
	public final static int getIndicatorSize(Context ctx) {
		DisplayMetrics metric = ctx.getResources().getDisplayMetrics();
		int a = metric.densityDpi;
		int target = 42;
		// 忽略ldpi与mdpi， 小于280为hdpi, 大于等于280且小于等于360为xhdpi, 大于360的为xxhdpi
		if (a < 280) {
			target = 36;
		} else if (a >= 280 && a <= 360) {

			target = 42;
		} else {
			return 63;
		}
		metric = null;
		return target;
	}
    
	//A by zel
    public int getPreviewBackgroundSize() {
    	return this.mPreviewBackground.getLayoutParams().height;
    }
//add by zhouerlong
    private void computePreviewDrawingParams(int drawableSize, int totalSize) {

        if (true) {
            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;

            final int previewSize = mPreviewBackground.getLayoutParams().height;
            final int previewPadding = FolderRingAnimator.sPreviewPadding;

            mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
            // cos(45) = 0.707  + ~= 0.1) = 0.8f
            int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.88f));
            if(Launcher.scale == 1.5){
            	adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 1.30f));
            }

            int unscaledHeight = (int) (mIntrinsicIconSize * (1 + PERSPECTIVE_SHIFT_FACTOR));
            mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

            mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
            mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;

            mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
            mPreviewOffsetY = previewPadding + grid.folderBackgroundOffset;
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }
        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;
    }

//add by zhouerlong
    private float getLocalCenterForIndex(int index, int[] center) {
        mParams = computePreviewItemDrawingParams(Math.min(NUM_ITEMS_IN_PREVIEW, index), mParams,0);

        mParams.transX += mPreviewOffsetX;
        mParams.transY += mPreviewOffsetY;
        float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize) / 2;
        float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize) / 2;

        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mParams.scale;
    }

    

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params,Canvas c) {
        canvas.save();
        canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        canvas.scale(params.scale, params.scale);
        

        c.save();
        c.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        c.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            mOldBounds.set(d.getBounds());
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            d.setFilterBitmap(true);
            d.setColorFilter(Color.argb(params.overlayAlpha, 255, 255, 255),
                    PorterDuff.Mode.SRC_ATOP);
            d.draw(canvas);
            d.draw(c);
            d.clearColorFilter();
            d.setFilterBitmap(false);
            d.setBounds(mOldBounds);
        }
        canvas.restore();
        c.restore();
        c=null;
    	Launcher launcher = null;
		// TODO Auto-generated method stub
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		FolderInfo shortcutInfo = (FolderInfo) this.getTag();
		if (launcher.getSpringState() == Launcher.SpringState.BATCH_EDIT_APPS
				&& shortcutInfo != null
				&& shortcutInfo.mItemState == ItemInfo.State.BATCH_SELECT_MODEL) {
			int w,h;
			 w=h=getIndicatorSize(mContext);
			DrawEditIcons.drawStateIconForBatch(canvas, this,
					R.drawable.in_use, w, h, 1f);// 此处为画文件夹编辑小图标
		}
        
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	
    	

		Launcher launcher = null;
        if (mFolder == null) return;
        if (mFolder.getItemCount() == 0 && !mAnimating) return;
        super.dispatchDraw(canvas);


		if(mCurrentLayer!=Launcher.currentFolderLayer) {
			isDrawableLayer=false;
            if(Launcher.currentFolderLayer==4) {
            	PERSPECTIVE_SHIFT_FACTOR=1.0f;
            	if(Launcher.scale==1.5f) {
                	PERSPECTIVE_SHIFT_FACTOR=1.4f;
            	}
            	NUM_ITEMS_IN_PREVIEW=4;
            }else {
            	PERSPECTIVE_SHIFT_FACTOR = 1.79f;
            	if(Launcher.scale==1.5f) {
                	PERSPECTIVE_SHIFT_FACTOR=2.09f;
            	}
            	NUM_ITEMS_IN_PREVIEW=9;
            }

			h.postDelayed(mRunnable, 70);
            mCurrentLayer=Launcher.currentFolderLayer;
		}
		if(!isDrawableLayer) {
			return;
		}
        ArrayList<View> items = mFolder.getItemsInReadingOrder();
        Drawable d;
        TextView v;

        // Update our drawing parameters if necessary
        if (mAnimating) {
            computePreviewDrawingParams(mAnimParams.drawable);
        } else {
            v = (TextView) items.get(0);
            d = v.getCompoundDrawables()[1];
            if (d !=null)
            computePreviewDrawingParams(d);
        }

        int nItemsInPreview = Math.min(items.size(), NUM_ITEMS_IN_PREVIEW);
//        if(bitmap==null) {
     		 bitmap = Bitmap
     				.createBitmap(
     						this.getWidth(),
     						this.getHeight(), Bitmap.Config.ARGB_8888);
//        }
     		 Canvas c = new Canvas();
        c.setBitmap(bitmap);
        if (!mAnimating) {
            for (int i = nItemsInPreview - 1; i >= 0; i--) {
                v = (TextView) items.get(i);
                if (!mHiddenItems.contains(v.getTag())) {
                    d = v.getCompoundDrawables()[1];
                    mParams = computePreviewItemDrawingParams(i, mParams,0);
//add by zhouerlong
                    mParams.drawable = d;
                    drawPreviewItem(canvas, mParams,c);
                }
            }
            
            
        } else {
            drawPreviewItem(canvas, mAnimParams,c);
        }
		 /**M: Draw unread event number.@{**/
//        ImageUtils.savePNG_After(bitmap, "sdfsdf.png");
        MTKUnreadLoader.drawUnreadEventIfNeed(canvas, this,anim.getUnreadOuterRingSize());
        /**@}**/
		
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		if (launcher != null) {
			if (launcher.getWorkspace().isInSpringLoadMoed()) {
				// DrawEditIcons.drawUnreadEventIfNeed(canvas, this);
				if (!AppsCustomizePagedView.DISABLE_ALL_APPS) {
					DrawEditIcons.drawStateIcon(canvas, this,R.drawable.ic_launcher_delete_holo,0,0,1f);
				}
			}
		}
		

	

	
    }

    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
            final Runnable onCompleteRunnable) {
        final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(0, null,0);
//add by zhouerlong

        final float scale0 = 1.0f;
        final float transX0 = (mAvailableSpaceInPreview - d.getIntrinsicWidth()) / 2;
        final float transY0 = (mAvailableSpaceInPreview - d.getIntrinsicHeight()) / 2 + getPaddingTop();
        mAnimParams.drawable = d;

        ValueAnimator va = LauncherAnimUtils.ofFloat(this, 0f, 1.0f);
        va.addUpdateListener(new AnimatorUpdateListener(){
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if (reverse) {
                    progress = 1 - progress;
                    mPreviewBackground.setAlpha(progress);
                }
                
                mAnimParams.transX = transX0 + progress * (finalParams.transX - transX0);
                mAnimParams.transY = transY0 + progress * (finalParams.transY - transY0);
                mAnimParams.scale = scale0 + progress * (finalParams.scale - scale0);
                invalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            	FolderIcon.this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
            	FolderIcon.this.setLayerType(View.LAYER_TYPE_NONE, null);
                mAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        va.setDuration(duration);
        va.start();
    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAdd item = " + item);
        }
        /**
         * M: added for unread feature, when add a item to a folder, we need to update
         * the unread num of the folder.@{
         */
        try {
            final ComponentName componentName = item.intent.getComponent();
            int index=-1;
            if(Launcher.isSupportClone) {
            	index = item.intent.getAppInstanceIndex();
            }
            updateFolderUnreadNum(componentName, item.unreadNum,item.unreadTitle,item.messageIcon,item.pendingIntent,index);
            /**@}**/
		} catch (Exception e) {
			e.printStackTrace();
		}

        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item,State state) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onRemove item = " + item);
        }
        /**M: added for Unread feature, when remove a item from a folder, we need to update
         *  the unread num of the folder.@{
         */
        try {
            final ComponentName componentName = item.intent.getComponent();
            int index=-1;
            if(Launcher.isSupportClone) {
            	index = item.intent.getAppInstanceIndex();
            }
            updateFolderUnreadNum(componentName, item.unreadNum,item.unreadTitle,item.messageIcon,item.pendingIntent,index);
            /**@}**/
		} catch (Exception e) {
			e.printStackTrace();
		}

        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title.toString());
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
        }
        return result;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }

    /**M: Added for unread message feature.@{**/
    
   /**
    * M: Update the unread message number of the shortcut with the given value.
    *
    * @param unreadNum the number of the unread message.
    */
   public void setFolderUnreadNum(int unreadNum) {
       if (LauncherLog.DEBUG_UNREAD) {
           LauncherLog.d(TAG, "setFolderUnreadNum: unreadNum = " + unreadNum + ", mInfo = " + mInfo
                   + ", this = " + this);
       }

       if (unreadNum <= 0) {
           mInfo.unreadNum = 0;
       } else {
           mInfo.unreadNum = unreadNum;
       }
   }
   /**
    * M: Update unread number of the folder, the number is the total unread number
    * of all shortcuts in folder, duplicate shortcut will be only count once.
    */
   public void updateFolderUnreadNum() {
       final ArrayList<ShortcutInfo> contents = mInfo.contents;
       final int contentsCount = contents.size();
       int unreadNumTotal = 0;
       final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
       ShortcutInfo shortcutInfo = null;
       ComponentName componentName = null;
       int unreadNum = 0;
       for (int i = 0; i < contentsCount; i++) {
           shortcutInfo = contents.get(i);
           componentName = shortcutInfo.intent.getComponent();
           unreadNum = MTKUnreadLoader.getUnreadNumberOfComponent(componentName);
           if (unreadNum > 0) {
               shortcutInfo.unreadNum = unreadNum;
               int j = 0;
               for (j = 0; j < components.size(); j++) {
                   if (componentName != null && componentName.equals(components.get(j))) {
                       break;
                   }
               }
               if (LauncherLog.DEBUG_UNREAD) {
                   LauncherLog.d(TAG, "updateFolderUnreadNum: unreadNumTotal = " + unreadNumTotal
                           + ", j = " + j + ", components.size() = " + components.size());
               }
               if (j >= components.size()) {
                   components.add(componentName);
                   unreadNumTotal += unreadNum;
               }
           }
       }
       if (LauncherLog.DEBUG_UNREAD) {
           LauncherLog.d(TAG, "updateFolderUnreadNum 1 end: unreadNumTotal = " + unreadNumTotal);
       }
       setFolderUnreadNum(unreadNumTotal);
   }

   /**
    * M: Update the unread message of the shortcut with the given information.
    *
    * @param unreadNum the number of the unread message.
    */
   public void updateFolderUnreadNum(ComponentName component, int unreadNum,String title,Bitmap messageIcon,PendingIntent p,int appInstanceIndex) {
       final ArrayList<ShortcutInfo> contents = mInfo.contents;
       final int contentsCount = contents.size();
       int unreadNumTotal = 0;
       ShortcutInfo appInfo = null;
       ComponentName name = null;
       int lastUnread = mInfo.unreadNum;
       final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
       for (int i = 0; i < contentsCount; i++) {
           appInfo = contents.get(i);
           try {
               name = appInfo.intent.getComponent();
		} catch (Exception e) {
			// TODO: handle exception
		}
           int index=-1;
           if(Launcher.isSupportClone) {
        	   try {
                  	index = appInfo.intent.getAppInstanceIndex();
			} catch (Exception e) {
				// TODO: handle exception
			}
           }
			if (appInfo.intent!=null&&index == appInstanceIndex) {
           if (name != null && name.equals(component)) {
               appInfo.unreadNum = unreadNum;
               appInfo.messageIcon = messageIcon;
               appInfo.pendingIntent = p;
               
           }
			}
           if (appInfo.unreadNum > 0) {
               int j = 0;
               for (j = 0; j < components.size(); j++) {
                   if (name != null && name.getPackageName().equals(components.get(j).getPackageName())) {
                	   if(Launcher.isSupportClone&&!(contents.get(j)!=null&&appInfo.intent!=null&&contents.get(j).getIntent().getAppInstanceIndex()!=appInfo.intent.getAppInstanceIndex()))
                       break;
                   }
               }
               if (LauncherLog.DEBUG_UNREAD) {
                   LauncherLog.d(TAG, "updateFolderUnreadNum: unreadNumTotal = " + unreadNumTotal
                           + ", j = " + j + ", components.size() = " + components.size());
               }
               if (j >= components.size()) {
//       			if (appInfo.intent!=null&&appInfo.intent.getAppInstanceIndex() == appInstanceIndex) {
                   components.add(name);
                   unreadNumTotal += appInfo.unreadNum;
//       			}
               }
               

               if(lastUnread==0&&lastUnread<unreadNumTotal) {

                   onChanged(unreadNum);
               }
           }
       }
       if (LauncherLog.DEBUG_UNREAD) {
           LauncherLog.d(TAG, "updateFolderUnreadNum 2 end: unreadNumTotal = " + unreadNumTotal);
       }
       setFolderUnreadNum(unreadNumTotal);
   }
   
   
   /**
    * M: Update the unread message of the shortcut with the given information.
    *
    * @param unreadNum the number of the unread message.
    */
	public void updateFolderDownload(DownLoadTaskInfo dInfo) {
		final ArrayList<ShortcutInfo> contents = mInfo.contents;
		final int contentsCount = contents.size();
		ShortcutInfo appInfo = null;
		for (int i = 0; i < contentsCount; i++) {
			appInfo = contents.get(i);

		}

	}
	
	/**
	    * M: Update the unread message of the shortcut with the given information.
	    *
	    * @param unreadNum the number of the unread message.
	    */
		public void removeFolderDownloadItem(String  pkg) {
			final ArrayList<ShortcutInfo> contents = mInfo.contents;
			final int contentsCount = contents.size();
			ShortcutInfo appInfo = null;
			for (int i = 0; i < contentsCount; i++) {
				appInfo = contents.get(i);

			}

		}
   
   /**@**/
   
   public void onChanged(int unreadNumber) {
		this.requestLayout();
		anim.animateToIconIndcatorDraw(this,0,1f,null,true);
	
	}
   

   ///: @}
	//add by zhouerlong 20150808 begin
   /**
    * 未完善。。卸载小图标以动画的方式出来
    */
   IconUninstallIndicatorAnim anim = new IconUninstallIndicatorAnim();

   //begin add by ouyangjin for lqtheme
	@Override
	public void onThemeChange(boolean end) {
		final Bitmap iconbBitmap = getThemeFolderIcon();
		if (iconbBitmap == null) {
			post(new Runnable() {
				@Override
				public void run() {
					mPreviewBackground.setImageResource(R.id.preview_background);
				}
			});
			
		}
		else {
			post(new Runnable() {
				@Override
				public void run() {
					LinearLayout.LayoutParams lp = (LayoutParams) mPreviewBackground.getLayoutParams();
					Bitmap bitmap=	IconCache.getFolderIcon("");
            		bitmap=ImageUtils.resizeIcon(bitmap, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
	                Drawable folder_icon = ImageUtils.bitmapToDrawable(bitmap);

	                sSharedOuterRingDrawable = folder_icon!=null?folder_icon:ImageUtils.folder_bg;//m by zhouerlong
	                sSharedInnerRingDrawable = folder_icon!=null?folder_icon:ImageUtils.folder_bg;//m by zhouerlong
	                sSharedFolderLeaveBehind = folder_icon!=null?folder_icon:ImageUtils.folder_bg;//m by zhouerlong
    				if(Launcher.isSupportIconSize) {
        				lp.width = iconbBitmap.getWidth();
        				lp.height = iconbBitmap.getHeight();
    				}else {
        				lp.width = Utilities.sIconTextureHeight;//bitmap.getWidth();
        				lp.height = Utilities.sIconTextureHeight;//bitmap.getHeight();
    				}
					mPreviewBackground.setImageBitmap(iconbBitmap);
				}
			});
			
		}
//		LauncherAppState app = LauncherAppState.getInstance();
//		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
//		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mPreviewBackground.getLayoutParams();
//		lp.width = grid.folderIconSizePx;
//		lp.height = grid.folderIconSizePx;

	}
	private Bitmap getThemeFolderIcon() {
		//拿图标
		Bitmap bitmap=null;
		if (LqShredPreferences.isLqtheme(mContext)) {
			 bitmap =IconCache.getFolderIcon("");
		}
		if(bitmap !=null)
		bitmap=ImageUtils.resizeIcon(bitmap, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
		return bitmap;
	}
	@Override
	public void updateViewAfterChildThemeChange() {
		invalidate();
		requestLayout();
	}
	 public void finalize() throws Throwable {
	        super.finalize();
	        OLThemeNotification.getInstance().unRegisterThemeChange(this, this);
	        NiftyObserables.getInstance().unregisterObserver(this);
	    }
	//end add by ouyangjin for lqtheme

	AnimTool tool;
	@Override
	public void onChanged(StateInfo st) {
		if (tool == null) {
			tool = new AnimTool();
		}
		if(st.state) {
			tool.start(this);
		}else {
			tool.cacel();
		}
	}

	@Override
	public AnimTool getAinmTool() {
		// TODO Auto-generated method stub
		return tool;
	}
}
