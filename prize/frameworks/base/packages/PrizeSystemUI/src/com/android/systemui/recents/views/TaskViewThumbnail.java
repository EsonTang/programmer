/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.recents.views;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;

import com.android.systemui.R;
import com.android.systemui.recents.model.Task;
import com.mediatek.systemui.statusbar.util.FeatureOptions;

/*prize add by xiarui 2017-11-15 start*/
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.Handler;
import android.content.res.Resources;
import android.support.v7.graphics.Palette;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.android.systemui.recents.LogUtils;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.phone.BlurPic;
/*prize add by xiarui 2017-11-15 end*/


/**
 * The task thumbnail view.  It implements an image view that allows for animating the dim and
 * alpha of the thumbnail image.
 */
public class TaskViewThumbnail extends View {

    private static final ColorMatrix TMP_FILTER_COLOR_MATRIX = new ColorMatrix();
    private static final ColorMatrix TMP_BRIGHTNESS_COLOR_MATRIX = new ColorMatrix();

    private Task mTask;

    private int mDisplayOrientation = Configuration.ORIENTATION_UNDEFINED;
    private Rect mDisplayRect = new Rect();

    // Drawing
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mTaskViewRect = new Rect();
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mThumbnailRect = new Rect();
    @ViewDebug.ExportedProperty(category="recents")
    private float mThumbnailScale;
    private float mFullscreenThumbnailScale;
    private ActivityManager.TaskThumbnailInfo mThumbnailInfo;

    private int mCornerRadius;
    @ViewDebug.ExportedProperty(category="recents")
    private float mDimAlpha;
    private Matrix mScaleMatrix = new Matrix();
    private Paint mDrawPaint = new Paint();
    private Paint mBgFillPaint = new Paint();
    private BitmapShader mBitmapShader;
    private LightingColorFilter mLightingColorFilter = new LightingColorFilter(0xffffffff, 0);

    // Clip the top of the thumbnail against the opaque header bar that overlaps this view
    private View mTaskBar;

    // Visibility optimization, if the thumbnail height is less than the height of the header
    // bar for the task view, then just mark this thumbnail view as invisible
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mInvisible;

    @ViewDebug.ExportedProperty(category="recents")
    private boolean mDisabledInSafeMode;

    /**prize add by xiarui round blur encryption 2017-11-16 start**/
    private int mTaskViewHeaderHeight = 112;
    private Bitmap mBlurScrimeBitmap;
    private Bitmap mBlurScrimIcon;
    private String mBlurScrimPrompt;
    private float mThumbnailScaleWidth;
    private int mEncryptionTextColor;
    private Paint mEncryptionDrawPaint = new Paint();
    private Paint mEncryptionDrawTextPaint = new Paint();
    private Paint mDockedShadeLayerPaint = new Paint();
    /**prize add by xiarui round blur encryption 2017-11-16 end**/

    public TaskViewThumbnail(Context context) {
        this(context, null);
    }

    public TaskViewThumbnail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDrawPaint.setColorFilter(mLightingColorFilter);
        mDrawPaint.setFilterBitmap(true);
        mDrawPaint.setAntiAlias(true);
        mCornerRadius = getResources().getDimensionPixelSize(
                R.dimen.recents_task_view_rounded_corners_radius);
        mBgFillPaint.setColor(Color.WHITE);
        mFullscreenThumbnailScale = context.getResources().getFraction(
                com.android.internal.R.fraction.thumbnail_fullscreen_scale, 1, 1);

        /*prize modify by xiarui round blur encryption 2017-11-16 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            mEncryptionDrawPaint.setColorFilter(mLightingColorFilter);
            mEncryptionDrawPaint.setFilterBitmap(true);
            mEncryptionDrawPaint.setAntiAlias(true);

            mEncryptionDrawTextPaint.setColorFilter(mLightingColorFilter);
            mEncryptionDrawTextPaint.setFilterBitmap(true);
            mEncryptionDrawTextPaint.setAntiAlias(true);

            mDockedShadeLayerPaint.setColor(0x88000000);

            mBlurScrimPrompt = getResources().getString(R.string.blur_scrim_prompt);
            mBlurScrimeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.encryption_bg);
        }
        /*prize modify by xiarui round blur encryption 2017-11-16 end*/
    }

    /**
     * Called when the task view frame changes, allowing us to move the contents of the header
     * to match the frame changes.
     */
    public void onTaskViewSizeChanged(int width, int height) {

        /*prize add by xiarui 2017-11-16 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            if (mTask != null) {
                height = height - mTaskBar.getHeight();
            } else {
                height = height - mTaskViewHeaderHeight;
            }
        }
        /*prize add by xiarui 2017-11-16 end*/

        // Return early if the bounds have not changed
        if (mTaskViewRect.width() == width && mTaskViewRect.height() == height) {
            return;
        }

        mTaskViewRect.set(0, 0, width, height);
        setLeftTopRightBottom(0, 0, width, height);
        updateThumbnailScale();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mInvisible) {
            return;
        }

        int viewWidth = mTaskViewRect.width();
        int viewHeight = mTaskViewRect.height();
		
        /*prize modify by xiarui 2017-11-15 start*/
        //int thumbnailWidth = Math.min(viewWidth,
        //        (int) (mThumbnailRect.width() * mThumbnailScale));
        //int thumbnailHeight = Math.min(viewHeight,
        //        (int) (mThumbnailRect.height() * mThumbnailScale));
        int thumbnailWidth = 0;
        int thumbnailHeight = 0;
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            thumbnailWidth = Math.min(viewWidth, (int) (mThumbnailRect.width() * mThumbnailScaleWidth));
            thumbnailHeight = Math.min(viewHeight, (int) (mThumbnailRect.height() * mThumbnailScale));
        } else {
        	thumbnailWidth = Math.min(viewWidth,
                (int) (mThumbnailRect.width() * mThumbnailScale));
        	thumbnailHeight = Math.min(viewHeight,
                (int) (mThumbnailRect.height() * mThumbnailScale));
        }
        /*prize modify by xiarui 2017-11-15 end*/

        /*prize add by xiarui reset thumbnail width & height 2017-11-15 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            if (mDisplayOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if ((mThumbnailInfo != null) && (mThumbnailInfo.screenOrientation == Configuration.ORIENTATION_LANDSCAPE)) {
                    if (mThumbnailRect.width() < mThumbnailRect.height()) { //横屏,分屏下截的图
                        thumbnailWidth = Math.min(viewWidth, (int) (mThumbnailRect.width() * mThumbnailScaleWidth));
                        thumbnailHeight = Math.min(viewHeight, (int) (mThumbnailRect.height() * mThumbnailScale));
                    } else {
                        thumbnailWidth = Math.min(viewWidth, (int) (mThumbnailRect.height() * mThumbnailScaleWidth));
                        thumbnailHeight = Math.min(viewHeight, (int) (mThumbnailRect.width() * mThumbnailScale));
                    }
                }
            } else {
                if ((mThumbnailInfo != null) && (mThumbnailInfo.screenOrientation == Configuration.ORIENTATION_PORTRAIT)) {
                    if (mThumbnailRect.width() > mThumbnailRect.height()) {  //竖屏,分屏下截的图
                        thumbnailWidth = Math.min(viewWidth, (int) (mThumbnailRect.width() * mThumbnailScaleWidth));
                        thumbnailHeight = Math.min(viewHeight, (int) (mThumbnailRect.height() * mThumbnailScale));
                    } else {
                        thumbnailWidth = Math.min(viewWidth, (int) (mThumbnailRect.height() * mThumbnailScaleWidth));
                        thumbnailHeight = Math.min(viewHeight, (int) (mThumbnailRect.width() * mThumbnailScale));
                    }
                }
            }
        }
        /*prize add by xiarui reset thumbnail width & height 2017-11-15 end*/

        if (mBitmapShader != null && thumbnailWidth > 0 && thumbnailHeight > 0) {
            /*prize modify by xiarui {LinearLayout replaced by FrameLayout, so topOffset = 0} 2017-11-16 start*/
            int topOffset = 0;
            if (!PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                topOffset = mTaskBar != null
                        ? mTaskBar.getHeight() - mCornerRadius
                        : 0;
            }
            /*prize modify by xiarui {LinearLayout replaced by FrameLayout, so topOffset = 0} 2017-11-16 end*/

            // Draw the background, there will be some small overdraw with the thumbnail
            if (thumbnailWidth < viewWidth) {
                // Portrait thumbnail on a landscape task view
                canvas.drawRoundRect(Math.max(0, thumbnailWidth - mCornerRadius), topOffset,
                        viewWidth, viewHeight,
                        mCornerRadius, mCornerRadius, mBgFillPaint);
            }
            if (thumbnailHeight < viewHeight) {
                // Landscape thumbnail on a portrait task view
                canvas.drawRoundRect(0, Math.max(topOffset, thumbnailHeight - mCornerRadius),
                        viewWidth, viewHeight,
                        mCornerRadius, mCornerRadius, mBgFillPaint);
            }
            // Draw the thumbnail
            canvas.drawRoundRect(0, topOffset, thumbnailWidth, thumbnailHeight,
                    mCornerRadius, mCornerRadius, mDrawPaint);

            /*prize modify by xiarui round blur encryption 2017-11-16 start*/
            if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                SystemServicesProxy ssp = Recents.getSystemServices();
                if (PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption && !ssp.hasDockedTask()) {
                    canvas.drawBitmap(mBlurScrimeBitmap, (viewWidth - mBlurScrimeBitmap.getWidth()) / 2, (viewHeight - mBlurScrimeBitmap.getHeight()) / 2, mEncryptionDrawPaint);
                    if (mTask.launcherIcon != null) {
                        mBlurScrimIcon = drawableToBitmap(mTask.launcherIcon);
                    } else if (mTask.icon != null) {
                        mBlurScrimIcon = drawableToBitmap(mTask.icon);
                    }
                    if (mBlurScrimIcon != null) {
                        canvas.drawBitmap(mBlurScrimIcon, (viewWidth - mBlurScrimIcon.getWidth()) / 2, (viewHeight - mBlurScrimIcon.getHeight()) / 2, mEncryptionDrawPaint);
                    }
                    mEncryptionDrawTextPaint.setTextSize(24);
                    mEncryptionDrawTextPaint.setColor(/*mEncryptionTextColor == 0 ? */0xff0a0a0a/* : mEncryptionTextColor*/);
                    canvas.drawText(mBlurScrimPrompt,
                            (viewWidth - mEncryptionDrawTextPaint.measureText(mBlurScrimPrompt)) / 2,
                            (viewHeight - mBlurScrimeBitmap.getHeight()) / 2 + mBlurScrimeBitmap.getHeight() + 40, mEncryptionDrawTextPaint);
                }

                //dock model
                if (mTask != null && !mTask.isDockable && ssp.hasDockedTask()) {
                    canvas.drawRoundRect(0, topOffset, thumbnailWidth, thumbnailHeight,
                            mCornerRadius, mCornerRadius, mDockedShadeLayerPaint);
                }
            }
            /*prize modify by xiarui round blur encryption 2017-11-16 end*/

        } else {
            canvas.drawRoundRect(0, 0, viewWidth, viewHeight, mCornerRadius, mCornerRadius,
                    mBgFillPaint);
        }
    }

    /**  prize modify by xiarui round blur encryption 2017-11-16 start
     * drawable to bitmap
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(120, 120, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, 120, 120);
        drawable.draw(canvas);
        return bitmap;
    }
    /**prize modify by xiarui round blur encryption 2017-11-16 end**/

    /** Sets the thumbnail to a given bitmap. */
    void setThumbnail(Bitmap bm, ActivityManager.TaskThumbnailInfo thumbnailInfo) {
        if (bm != null) {
            /*prize modify by xiarui round blur encryption 2017-11-16 start*/
            if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                if (PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption) {
                    if (mTask.blurScrimeBitmap == null) {
                        mTask.blurScrimeBitmap = BlurPic.blur(bm);
                    }
                    bm = mTask.blurScrimeBitmap;
                    //Palette p = Palette.from(bm).generate();
                    //mEncryptionTextColor = p.getVibrantColor(0xff0a0a0a);
                }
            }
            /*prize modify by xiarui round blur encryption 2017-11-16 end*/
            bm.prepareToDraw();
            mBitmapShader = new BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mDrawPaint.setShader(mBitmapShader);
            mThumbnailRect.set(0, 0, bm.getWidth(), bm.getHeight());
            mThumbnailInfo = thumbnailInfo;
            updateThumbnailScale();
        } else {
            mBitmapShader = null;
            mDrawPaint.setShader(null);
            mThumbnailRect.setEmpty();
            mThumbnailInfo = null;
        }
    }

    /** Updates the paint to draw the thumbnail. */
    void updateThumbnailPaintFilter() {
        if (mInvisible) {
            return;
        }
        int mul = (int) ((1.0f - mDimAlpha) * 255);
        if (mBitmapShader != null) {
            if (mDisabledInSafeMode) {
                // Brightness: C-new = C-old*(1-amount) + amount
                TMP_FILTER_COLOR_MATRIX.setSaturation(0);
                float scale = 1f - mDimAlpha;
                float[] mat = TMP_BRIGHTNESS_COLOR_MATRIX.getArray();
                mat[0] = scale;
                mat[6] = scale;
                mat[12] = scale;
                mat[4] = mDimAlpha * 255f;
                mat[9] = mDimAlpha * 255f;
                mat[14] = mDimAlpha * 255f;
                TMP_FILTER_COLOR_MATRIX.preConcat(TMP_BRIGHTNESS_COLOR_MATRIX);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(TMP_FILTER_COLOR_MATRIX);
                mDrawPaint.setColorFilter(filter);
                mBgFillPaint.setColorFilter(filter);
            } else {
                mLightingColorFilter.setColorMultiply(Color.argb(255, mul, mul, mul));
                mDrawPaint.setColorFilter(mLightingColorFilter);
                mDrawPaint.setColor(0xFFffffff);
                mBgFillPaint.setColorFilter(mLightingColorFilter);
            }
        } else {
            int grey = mul;
            mDrawPaint.setColorFilter(null);
            mDrawPaint.setColor(Color.argb(255, grey, grey, grey));
        }
        if (!mInvisible) {
            invalidate();
        }
    }

    /**
     * Updates the scale of the bitmap relative to this view.
     */
    public void updateThumbnailScale() {
        mThumbnailScale = 1f;
        if (mBitmapShader != null) {
            // We consider this a stack task if it is not freeform (ie. has no bounds) or has been
            // dragged into the stack from the freeform workspace
            boolean isStackTask = !mTask.isFreeformTask() || mTask.bounds == null;
            if (mTaskViewRect.isEmpty() || mThumbnailInfo == null ||
                    mThumbnailInfo.taskWidth == 0 || mThumbnailInfo.taskHeight == 0) {
                // If we haven't measured or the thumbnail is invalid, skip the thumbnail drawing
                // and only draw the background color
                mThumbnailScale = 0f;
                /*prize add by xiarui 2017-11-18 start*/
                if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                    mScaleMatrix.setScale(mThumbnailScale, mThumbnailScale);
                }
                /*prize add by xiarui 2017-11-18 end*/
            } else if (isStackTask) {
                float invThumbnailScale = 1f / mFullscreenThumbnailScale;
                // M: Slim thumbnail's size for GMO @{
                if (FeatureOptions.LOW_RAM_SUPPORT) {
                    invThumbnailScale *= 2;
                }
                // @}

                /**prize modify by xiarui 2017-12-13 start
                 * Display in portrait mode when split screen and horizontal screen
                 * **/
                boolean isShowPortStyleInDockLand = false;

                if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                    SystemServicesProxy ssp = Recents.getSystemServices();
                    isShowPortStyleInDockLand = ssp.hasDockedTask() && mDisplayOrientation == Configuration.ORIENTATION_LANDSCAPE;
                    //prize modify by xiarui for Bug#45478 @{
                    if (mThumbnailInfo.screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (mThumbnailRect.width() > mThumbnailRect.height()) {
                            float scale = (mDisplayOrientation == Configuration.ORIENTATION_LANDSCAPE) ? RecentsConfiguration.taskViewScaleLand : RecentsConfiguration.taskViewScalePortrait;//0.53f : 0.6f; //0.56f : 0.6f
                            int widthO = (int) Math.ceil(mThumbnailRect.width() / RecentsConfiguration.taskViewScaleLand); //0.6f 0.53f
                            int heightO = (int) Math.ceil(mThumbnailRect.height() / RecentsConfiguration.taskViewScaleLand); //0.6f 0.53f
                            int widthC = (int) Math.ceil(mTaskViewRect.width() / scale);
                            int heightC = (int) Math.ceil(mTaskViewRect.height() / scale);
                            if ((widthO == widthC && heightO == heightC) || (widthO == heightC && heightO == widthC)) {
                                mThumbnailInfo.screenOrientation = Configuration.ORIENTATION_LANDSCAPE;
                            }
                        }
                    }
                    //end----@}
                }
                /**prize modify by xiarui 2017-12-13 end**/
                
                if (mDisplayOrientation == Configuration.ORIENTATION_PORTRAIT || isShowPortStyleInDockLand) {
                    if (mThumbnailInfo.screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        // If we are in the same orientation as the screenshot, just scale it to the
                        // width of the task view
                        mThumbnailScale = (float) mTaskViewRect.width() / mThumbnailRect.width();
                        /*prize add by xiarui 2017-11-18 start*/
                        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                            mThumbnailScaleWidth = mThumbnailScale;
                            mScaleMatrix.setScale(mThumbnailScaleWidth, mThumbnailScale);
                        }
                        /*prize add by xiarui 2017-11-18 end*/
                    } else {
                        // Scale the landscape thumbnail up to app size, then scale that to the task
                        // view size to match other portrait screenshots
                        /*prize modify by xiarui 2017-11-15 start*/
                        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                            if (mThumbnailRect.width() < mThumbnailRect.height()) { //横屏,分屏下截的图
                                mThumbnailScaleWidth = (float) mTaskViewRect.width() / mThumbnailRect.width();
                                mThumbnailScale = mThumbnailScaleWidth;
                                if (PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption) {
                                    mThumbnailScale = (float) mTaskViewRect.height() / mThumbnailRect.height();
                                }
                                mScaleMatrix.setScale(mThumbnailScaleWidth, mThumbnailScale);
                            } else {
                                mThumbnailScaleWidth = (float) mTaskViewRect.width() / mThumbnailRect.height();
                                mThumbnailScale = mThumbnailScaleWidth;
                                if (PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption) {
                                    mThumbnailScale = (float) mTaskViewRect.height() / mThumbnailRect.width();
                                }
                                int dx = -(mThumbnailRect.width() - mThumbnailRect.height()) / 2;
                                int dy = -dx;
                                mScaleMatrix.setRotate(90, mThumbnailRect.width() / 2, mThumbnailRect.height() / 2);
                                mScaleMatrix.postTranslate(dx, dy);
                                mScaleMatrix.postScale(mThumbnailScaleWidth, mThumbnailScale);
                            }
                        } else {
	                        mThumbnailScale = invThumbnailScale *
	                                ((float) mTaskViewRect.width() / mDisplayRect.width());                            
                        }
                        /*prize modify by xiarui 2017-11-15 end*/
                    }
                } else {
                    // Otherwise, scale the screenshot to fit 1:1 in the current orientation
                    /*prize modify by xiarui 2017-11-15 start*/
                    if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                        if (mThumbnailInfo.screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            if (mThumbnailRect.width() > mThumbnailRect.height()) { //竖屏,分屏下截的图
                                mThumbnailScale = (float) mTaskViewRect.height() / mThumbnailRect.height();
                                mThumbnailScaleWidth = mThumbnailScale;
                                if (PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption) {
                                    mThumbnailScaleWidth = (float) mTaskViewRect.width() / mThumbnailRect.width();
                                }
                                mScaleMatrix.setScale(mThumbnailScaleWidth, mThumbnailScale);
                            } else {
                                mThumbnailScaleWidth = (float) mTaskViewRect.width() / mThumbnailRect.height();
                                mThumbnailScale = (float) (mTaskViewRect.height()) / mThumbnailRect.width();
                                if (!(PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption)) {
                                    mThumbnailScaleWidth = (mThumbnailScaleWidth > 1.0f) ? 1.0f : mThumbnailScaleWidth;
                                }
                                int dx = (mThumbnailRect.height() - mThumbnailRect.width()) / 2;
                                int dy = -dx;
                                mScaleMatrix.setRotate(-90, mThumbnailRect.width() / 2, mThumbnailRect.height() / 2);
                                mScaleMatrix.postTranslate(dx, dy);
                                mScaleMatrix.postScale(mThumbnailScaleWidth, mThumbnailScale);
                            }
                        } else {
                            mThumbnailScaleWidth = (float) mTaskViewRect.width() / mThumbnailRect.width();
                            mThumbnailScale = (float) mTaskViewRect.height() / mThumbnailRect.height();
                            if (!(PrizeOption.PRIZE_FINGERPRINT_APPLOCK && mTask.isEncryption)) {
                                mThumbnailScaleWidth = mThumbnailScaleWidth > 1.0f ? mThumbnailScale : mThumbnailScaleWidth;
                            }
                            mScaleMatrix.setScale(mThumbnailScaleWidth, mThumbnailScale);
                        }
                    } else {
                        mThumbnailScale = invThumbnailScale;
                    }
                    /*prize modify by xiarui 2017-11-15 end*/
                }
            } else {
                // Otherwise, if this is a freeform task with task bounds, then scale the thumbnail
                // to fit the entire bitmap into the task bounds
                mThumbnailScale = Math.min(
                        (float) mTaskViewRect.width() / mThumbnailRect.width(),
                        (float) mTaskViewRect.height() / mThumbnailRect.height());

                /*prize add by xiarui 2017-11-18 start*/
                if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                    mScaleMatrix.setScale(mThumbnailScale, mThumbnailScale);
                }
                /*prize add by xiarui 2017-11-18 end*/
            }

            /*prize modify by xiarui 2017-11-18 start*/
            if (!PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                mScaleMatrix.setScale(mThumbnailScale, mThumbnailScale);
            }
            /*prize modify by xiarui 2017-11-18 end*/

            mBitmapShader.setLocalMatrix(mScaleMatrix);
        }
        if (!mInvisible) {
            invalidate();
        }
    }

    /** Updates the clip rect based on the given task bar. */
    void updateClipToTaskBar(View taskBar) {
        mTaskBar = taskBar;

        /*prize delete by xiarui 2017-11-16 start*/
        if (!PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            invalidate();
        }
        /*prize delete by xiarui 2017-11-16 end*/
    }

    /** Updates the visibility of the the thumbnail. */
    void updateThumbnailVisibility(int clipBottom) {
        /*prize delete by xiarui 2017-11-16 start*/
        if (!PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            boolean invisible = mTaskBar != null && (getHeight() - clipBottom) <= mTaskBar.getHeight();
            if (invisible != mInvisible) {
                mInvisible = invisible;
                if (!mInvisible) {
                    updateThumbnailPaintFilter();
                }
            }
        }
        /*prize delete by xiarui 2017-11-16 end*/
    }

    /**
     * Sets the dim alpha, only used when we are not using hardware layers.
     * (see RecentsConfiguration.useHardwareLayers)
     */
    public void setDimAlpha(float dimAlpha) {
        /*prize delete by xiarui 2017-11-16 start*/
        if (!PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            mDimAlpha = dimAlpha;
            updateThumbnailPaintFilter();
        }
        /*prize delete by xiarui 2017-11-16 end*/
    }

    /**
     * Binds the thumbnail view to the task.
     */
    void bindToTask(Task t, boolean disabledInSafeMode, int displayOrientation, Rect displayRect) {
        mTask = t;
        mDisabledInSafeMode = disabledInSafeMode;
        mDisplayOrientation = displayOrientation;
        mDisplayRect.set(displayRect);
        if (t.colorBackground != 0) {
            mBgFillPaint.setColor(t.colorBackground);
        }
    }

    /**
     * Called when the bound task's data has loaded and this view should update to reflect the
     * changes.
     */
    void onTaskDataLoaded(ActivityManager.TaskThumbnailInfo thumbnailInfo) {
        if (mTask.thumbnail != null) {
            setThumbnail(mTask.thumbnail, thumbnailInfo);
        } else {
            setThumbnail(null, null);
        }
    }

    /** Unbinds the thumbnail view from the task */
    void unbindFromTask() {
        mTask = null;
        setThumbnail(null, null);
    }
}
