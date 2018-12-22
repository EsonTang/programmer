/*
 * Copyright (c) 2015, The Linux Foundation. All rights reserved.
 * Not a Contribution
 *
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.data.ClusterAlbum;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.TitleLocation;
import com.android.gallery3d.util.LogUtil;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.photos.data.GalleryBitmapPool;
import com.android.gallery3d.R;

import java.util.ArrayList;

public class TimeLineTitleMaker {

    private final String TAG = "TimelineTitleMaker";
    private static final int BORDER_SIZE = 0;

    private final TimeLineSlotRenderer.LabelSpec mSpec;
    private final TextPaint mTitlePaint;
    private final TextPaint mLocationPaint;
    private final AbstractGalleryActivity mActivity;
    private final TimeLineSlotView mTimeLineSlotView;

    public TimeLineTitleMaker(AbstractGalleryActivity context, TimeLineSlotRenderer.LabelSpec spec, TimeLineSlotView slotView) {
        mActivity = context;
        mSpec = spec;
        mTimeLineSlotView = slotView;
        mTitlePaint = getTextPaint(spec.timeLineTitleFontSize, spec.timeLineTitleTextColor , false);
        mLocationPaint = getTextPaint(spec.timeLineLocationFontSize, spec.timeLineLocationTextColor, false);
    }
	
	public Context getContext() {
        return mActivity;
    }

    private static TextPaint getTextPaint(
            int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTypeface(Typeface.SANS_SERIF);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return paint;
    }

    public ThreadPool.Job<Bitmap> requestTimeLineTitle( String title,
            int count, SelectionManager selectionManager, Path path) {
        return new TimeLineTitle(title, count, mActivity, selectionManager, path);
    }

    static void drawText(Canvas canvas,
                         int x, int y, String text, int lengthLimit, TextPaint p) {
        synchronized (p) {
            text = TextUtils.ellipsize(
                    text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
            canvas.drawText(text, x, y - p.getFontMetricsInt().ascent, p);
        }
    }

    private class TimeLineTitle implements ThreadPool.Job<Bitmap> {
        private String mTitle;
        private int mCount;
        private Context mContext;
        private SelectionManager mSelectionManager;
        private Path mPath;
        public TimeLineTitle(String title, int count, Context context, SelectionManager selectionManager, Path path) {
            mTitle = title;
            mCount = count;
            mContext = context;
            mSelectionManager = selectionManager;
            LogUtil.i(TAG, "TimeLineTitle selectionManager=" + selectionManager);
            mPath = path;
        }

        @Override
        public Bitmap run(JobContext jc) {
            TimeLineSlotRenderer.LabelSpec spec = mSpec;

            Bitmap bitmap;
            int width = mTimeLineSlotView.getTitleWidth();
            int height= spec.timeLineTitleHeight;
            synchronized (this) {
                bitmap = GalleryBitmapPool.getInstance().get(width, height);
            }
            if (bitmap == null) {
                int borders = 2 * BORDER_SIZE;
                bitmap = Bitmap.createBitmap(width + borders,
                        height + borders, Config.ARGB_8888);
            }
            if (bitmap == null) {
                return null;
            }

            Canvas canvas = new Canvas(bitmap);
            canvas.clipRect(BORDER_SIZE, BORDER_SIZE,
                    bitmap.getWidth() - BORDER_SIZE,
                    bitmap.getHeight() - BORDER_SIZE);
            canvas.drawColor(mSpec.timeLineTitleBackgroundColor, PorterDuff.Mode.SRC);

            canvas.translate(BORDER_SIZE, BORDER_SIZE);

            MediaSet targetSet = mActivity.getDataManager().getMediaSet(mPath);
            TitleLocation rootTilleLocation = null;
            if (targetSet != null) {
                ClusterAlbum clusterAlbum = ((ClusterAlbum)targetSet);
                rootTilleLocation = clusterAlbum.getTitleLocation();
            }

            StringBuilder locationBuilder = new StringBuilder();

            if((rootTilleLocation != null)&&(rootTilleLocation.mHasAbroad)){
				for (int i = 0, size = rootTilleLocation.mChildren.size(); i < size; i++) {
					TitleLocation Title = rootTilleLocation.mChildren.get(i);
					locationBuilder.append(Title.mProvince);
					if (i != size - 1) {
						locationBuilder.append("\u3001");
					}
				}
            }
			/*
            if (rootTilleLocation != null && rootTilleLocation.mLocationCount > 0) {
                if (rootTilleLocation.mHasAbroad) {
                    for (int i = 0, size = rootTilleLocation.mChildren.size(); i < size; i++) {
                        TitleLocation country = rootTilleLocation.mChildren.get(i);
                        TitleLocation cityTitlle = rootTilleLocation.mChildren.get(i).mChildren.get(0).mChildren.get(0);
                        if (country.mInCn) {
                            locationBuilder.append(cityTitlle.mProvince);
                        } else {
                            locationBuilder.append(country.mProvince);
                            locationBuilder.append(cityTitlle.mProvince);
                        }
                        if (i != size - 1) {
                            locationBuilder.append("\u3001");
                        }
                    }
                } else {
                    if (rootTilleLocation.mCityCount == 1) {
                        TitleLocation cityTitlle = rootTilleLocation.mChildren.get(0).mChildren.get(0).mChildren.get(0);
                        locationBuilder.append(cityTitlle.mProvince);
                        if (rootTilleLocation.mDistrictCount < 3) {
                            for (int i = 0, size = cityTitlle.mChildren.size(); i < size; i++) {
                                locationBuilder.append(cityTitlle.mChildren.get(i).mProvince);
                                if (i != size - 1) {
                                    locationBuilder.append("\u3001");
                                }
                            }
                        }
                    } else if (rootTilleLocation.mCityCount > 1) {
                        ArrayList<TitleLocation> provinceTitlles = rootTilleLocation.mChildren.get(0).mChildren;
                        for (int k = 0, total = provinceTitlles.size(); k < total; k++) {
                            TitleLocation provinceTitlle = provinceTitlles.get(k);
                            for (int i = 0, size = provinceTitlle.mChildren.size(); i < size; i++) {
                                locationBuilder.append(provinceTitlle.mChildren.get(i).mProvince);
                                if (i != size - 1) {
                                    locationBuilder.append("\u3001");
                                }
                            }
                            if (k != total - 1) {
                                locationBuilder.append("\u3001");
                            }
                        }
                    }
                }
            }
            */
            if (jc.isCancelled()) return null;


            // draw time
            int x = spec.timeLineTitleMarginLeft;
            int y = (height - spec.timeLineTitleFontSize)/2 + spec.timeLineTitlePaddingBottom;
            drawText(canvas, x, y, mTitle, width - x, mTitlePaint);

            // draw location
            String location = locationBuilder.toString();
            LogUtil.i(TAG, "title=" + mTitle + " location=" + location + " width=" + width + " x=" + x);
            if (!TextUtils.isEmpty(location)) {
                Rect locationBounds = new Rect();
                mLocationPaint.getTextBounds(
                        location, 0, location.length(), locationBounds);
                x = width - spec.timeLineTitleMarginRight - locationBounds.width();
                if (x < spec.timeLineLocationPaddingLeft) {
                    x = spec.timeLineLocationPaddingLeft;
                }
                y = (height - spec.timeLineLocationFontSize) / 2  + spec.timeLineTitlePaddingBottom;
                drawText(canvas, x, y, location, width - spec.timeLineLocationPaddingLeft - spec.timeLineTitleMarginRight, mLocationPaint);
            }

            /*x = width - spec.timeLineTitleMarginRight;
            y = (height - spec.timeLineNumberFontSize) / 2;
            if (countString != null) {

                RectF dst = new RectF();
                Bitmap backBm;
                if (countString!=null && countString.length() < 3) {
                    backBm = mCountBackgroupBitmap;
                } else {
                    backBm = mCountBackgroupTwoBitmap;
                }
                Rect mediaCountBounds = new Rect();
                mCountPaint.getTextBounds(
                        countString, 0, countString.length(), mediaCountBounds);
                int w = mediaCountBounds.width();
                drawText(canvas, x - w / 2 - backBm.getWidth() / 2, y, countString,
                        width - x, mCountPaint);
                dst.left = x - backBm.getWidth();
                dst.right = x;
                dst.top = (height - backBm.getHeight()) / 2 + spec.slotGapPort / 3;
                dst.bottom = dst.top + backBm.getHeight();
                canvas.drawBitmap(backBm, null, dst, mTitlePaint);
            }*/
            return bitmap;
        }
    }
}
