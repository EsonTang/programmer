 /* Copyright (C) 2013 The Android Open Source Project
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.photos.BitmapRegionTileSource;
import com.mediatek.common.featureoption.FeatureOption;
///* M alps01601180, use AsnycTask get last thumbnail.
/// M.
// /M: Filter drm files @ {
// /@}
//xieweiwei_20150205_add_begin
//xieweiwei_20150205_add_end

public class WallpaperPickerActivity extends WallpaperCropActivity {
    static final String TAG = "Launcher.WallpaperPickerActivity";

    public static final int IMAGE_PICK = 5;
    public static final int PICK_WALLPAPER_THIRD_PARTY_ACTIVITY = 6;
    public static final int PICK_LIVE_WALLPAPER = 7;
    private static final String TEMP_WALLPAPER_TILES = "TEMP_WALLPAPER_TILES";
    private static final String SMARTBOOK_PLUGIN_KEY = "smartbook.plugin";

    private View mSelectedThumb;
    private boolean mIgnoreNextTap;
    private static OnClickListener mThumbnailOnClickListener;

    private LinearLayout mWallpapersView;
    private View mWallpaperStrip;
    
    private TextView mFromGallery;

    private ActionMode.Callback mActionModeCallback;

	public final static String IMG_CACHE = "imglod/cache";
    public static final int SHAPE_RECTANGLE = 0;

    /**
     * intent extra name : uri
     */
    public static final String INTENT_EXTRA_URI = "INTENT_EXTRA_URI";
    /**
     * intent extra name : outputWidth
     */
    public static final String INTENT_EXTRA_OUTPUT_WIDTH = "INTENT_EXTRA_OUTPUT_WIDTH";
    /**
     * intent extra name : outputHeight
     */
    public static final String INTENT_EXTRA_OUTPUT_HEIGHT = "INTENT_EXTRA_OUTPUT_HEIGHT";
    /**
     * intent extra name : cropShape
     */
    public static final String INTENT_EXTRA_CROP_SHAPE = "INTENT_EXTRA_CROP_SHAPE";
    /**
     * intent extra name : mDir
     */
    public static final String INTENT_EXTRA_SAVE_DIR = "INTENT_EXTRA_SAVE_DIR";
    /**
     * intent extra name : mFileName
     */
    public static final String INTENT_EXTRA_FILE_NAME = "INTENT_EXTRA_FILE_NAME";
    

    /**剪切图的宽度*/
    public static int CROP_IMG_WIDTH = 720;
    /**剪切图的高度*/
    public static int CROP_IMG_HEIGHT = 640;
    /**剪切图的名称*/
    public static final String COP_FILE_NAME = "croppedimg";
    
    private ActionMode mActionMode;

    private View.OnLongClickListener mLongClickListener;

    ArrayList<Uri> mTempWallpaperTiles = new ArrayList<Uri>();
    private SavedWallpaperImages mSavedImages;
    private WallpaperInfo mLiveWallpaperInfoOnPickerLaunch;
    private static int  REQ_PICK_IMG=122;

    public static abstract class WallpaperTileInfo {
        protected View mView;
        public void setView(View v) {
            mView = v;
        }
        public void onClick(WallpaperPickerActivity a) {}
        public void onSave(WallpaperPickerActivity a) {}
        public void onDelete(WallpaperPickerActivity a) {}
        public boolean isSelectable() { return false; }
        public boolean isNamelessWallpaper() { return false; }
        public void onIndexUpdated(CharSequence label) {
            if (isNamelessWallpaper()) {
                mView.setContentDescription(label);
            }
        }
    }

    public static class PickImageInfo extends WallpaperTileInfo {
        @Override
        public void onClick(WallpaperPickerActivity a) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            // /M: Filter drm files @ {
          /*  intent.putExtra(OmaDrmStore.DrmExtra.EXTRA_DRM_LEVEL,
                    OmaDrmStore.DrmExtra.DRM_LEVEL_FL);*/
            // /@}
            Utilities.startActivityForResultSafely(a, intent, IMAGE_PICK);
        }
    }

    public static class UriWallpaperInfo extends WallpaperTileInfo {
        private Uri mUri;
        public UriWallpaperInfo(Uri uri) {
            mUri = uri;
        }
        @Override
        public void onClick(WallpaperPickerActivity a) {
            CropView v = a.getCropView();
            int rotation = WallpaperCropActivity.getRotationFromExif(a, mUri);
            v.setTileSource(new BitmapRegionTileSource(a, mUri, 1024, rotation), null);
            v.setTouchEnabled(true);
        }
        @Override
        public void onSave(final WallpaperPickerActivity a) {
            boolean finishActivityWhenDone = true;
            OnBitmapCroppedHandler h = new OnBitmapCroppedHandler() {
                public void onBitmapCropped(byte[] imageBytes) {
                    Point thumbSize = getDefaultThumbnailSize(a.getResources());
                    // rotation is set to 0 since imageBytes has already been correctly rotated
                    Bitmap thumb = createThumbnail(
                            thumbSize, null, null, imageBytes, null, 0, 0, true);
                    a.getSavedImages().writeImage(thumb, imageBytes);
                }
            };
            a.cropImageAndSetWallpaper(mUri, h, finishActivityWhenDone);
        }
        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
        public Uri getUri() {
            return mUri;
        }
    }
    
	
    
    public static class ResourceWallpaperInfo extends WallpaperTileInfo {
        private Resources mResources;
        private Context mContext;
        private int mResId;
        private Drawable mThumb;

        public ResourceWallpaperInfo(Resources res, int resId, Drawable thumb,Context c) {
            mResources = res;
            mResId = resId;
            mThumb = thumb;
            mContext = c;
        }
        
      
        public int getResId() {
        	return mResId;
        }
         @Override
        public void onClick(WallpaperPickerActivity a) {

        	
        	
            int rotation = WallpaperCropActivity.getRotationFromExif(mResources, mResId);
            BitmapRegionTileSource source = new BitmapRegionTileSource(
                    mResources, a, mResId, 1024, rotation);
            CropView v = a.getCropView();
            v.setTileSource(source, null);
            
            Point wallpaperSize = WallpaperCropActivity.getDefaultWallpaperSize(
                    a.getResources(), a.getWindowManager(),cropMaxScreenSpan(source.getImageWidth(),source.getImageHeight(),a));
            RectF crop = WallpaperCropActivity.getMaxCropRect(
                    source.getImageWidth(), source.getImageHeight(),
                    wallpaperSize.x, wallpaperSize.y, false);
            
            v.setScale(wallpaperSize.x / crop.width());
            v.setTouchEnabled(false);
        }
        @Override
        public void onSave(WallpaperPickerActivity a) {
            boolean finishActivityWhenDone = true;
            a.cropImageAndSetWallpaper(mResources, mResId, finishActivityWhenDone);
        }
        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
    }

	///M: alps016011980, use AsyncTask, when get last thumbnail.
	protected static class GetLastThumbnailTask extends AsyncTask<Void,Void,Boolean> {
		Bitmap mLastPhoto;
		Context mContext;
		FrameLayout mPickImageTile;
				
		public GetLastThumbnailTask(Context c, FrameLayout pickImageTile) {
			mContext = c;
			mPickImageTile = pickImageTile;
		}
			
		protected Boolean doInBackground(Void... params) {
			Log.i(TAG, "GetLastThumbnailTask: doInBackground_in");
				
			Cursor cursor = MediaStore.Images.Media.query(mContext.getContentResolver(),
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							new String[] { MediaStore.Images.ImageColumns._ID,
							MediaStore.Images.ImageColumns.DATE_TAKEN},
							null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1");
			if(cursor == null) {
				return false;
			}
				
			try {				
				if (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					mLastPhoto = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
						id, MediaStore.Images.Thumbnails.MINI_KIND, null);
				}
			} finally { 			
				cursor.close();
			}
	
			Log.i(TAG, "GetLastThumbnailTask: out");
			return true;
		}
			
		protected void onPostExecute(Boolean result) {
			Log.i(TAG, "GetLastThumbnailTask: onPostExecute");
			if (mLastPhoto != null) {
				ImageView galleryThumbnailBg =
					(ImageView) mPickImageTile.findViewById(R.id.wallpaper_image);
				galleryThumbnailBg.setImageBitmap(mLastPhoto);
				int colorOverlay = mContext.getResources()
					.getColor(R.color.wallpaper_picker_translucent_gray);
				galleryThumbnailBg.setColorFilter(colorOverlay, PorterDuff.Mode.SRC_ATOP);
			}
		}
	}
	/// M.

    public void setWallpaperStripYOffset(float offset) {
        mWallpaperStrip.setPadding(0, 0, 0, (int) offset);
    }
    //add by zel
    
    @Override
    synchronized protected void OnSetWallpaperFinish() {//by zel
	}

	protected void setContent() {
//add by zhouerlong begin 20150811
//		if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
//	        setContentView(R.layout.wallpaper_picker_mi);
//		}else {
	        setContentView(R.layout.wallpaper_picker);
//		}
//add by zhouerlong end 20150811
        mCropView = (CropView) findViewById(R.id.cropView);
        mCropView.setVisibility(View.VISIBLE);

        //xieweiwei_20150205_add_begin
        mIsSetLockscreenWallpaper = Utilities.isLockscreenWallpaperIntent(getIntent());
        //xieweiwei_20150205_add_end

    }
    // called by onCreate; this is subclassed to overwrite WallpaperCropActivity
    protected void init() {
    	setContent();
        //setContentView(R.layout.wallpaper_picker);
			//M by zhouerlong

        mCropView = (CropView) findViewById(R.id.cropView);
        mWallpaperStrip = findViewById(R.id.wallpaper_strip);
        mFromGallery = (TextView) findViewById(R.id.from_gallery);
        mFromGallery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent it = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(it, REQ_PICK_IMG);
			}
		});
        
        /*mCropView.setTouchCallback(new CropView.TouchCallback() {
            LauncherViewPropertyAnimator mAnim;
            @Override
            public void onTouchDown() {
                if (mAnim != null) {
                    mAnim.cancel();
                }
                if (mWallpaperStrip.getAlpha() == 1f) {
                    mIgnoreNextTap = true;
                }
                mAnim = new LauncherViewPropertyAnimator(mWallpaperStrip);
                mAnim.alpha(0f)
                     .setDuration(150)
                     .addListener(new Animator.AnimatorListener() {
                         public void onAnimationStart(Animator animator) { }
                         public void onAnimationEnd(Animator animator) {
                             mWallpaperStrip.setVisibility(View.INVISIBLE);
                         }
                         public void onAnimationCancel(Animator animator) { }
                         public void onAnimationRepeat(Animator animator) { }
                     });
                mAnim.setInterpolator(new AccelerateInterpolator(0.75f));
                mAnim.start();
            }
            @Override
            public void onTouchUp() {
                mIgnoreNextTap = false;
            }
            @Override
            public void onTap() {
                boolean ignoreTap = mIgnoreNextTap;
                mIgnoreNextTap = false;
                if (!ignoreTap) {
                    if (mAnim != null) {
                        mAnim.cancel();
                    }
                    mWallpaperStrip.setVisibility(View.VISIBLE);
                    mAnim = new LauncherViewPropertyAnimator(mWallpaperStrip);
                    mAnim.alpha(1f)
                         .setDuration(150)
                         .setInterpolator(new DecelerateInterpolator(0.75f));
                    mAnim.start();
                }
            }
        });*/
        mThumbnailOnClickListener = new OnClickListener() {
            public void onClick(final View v) {
                if (mActionMode != null) {
                    // When CAB is up, clicking toggles the item instead
                    if (v.isLongClickable()) {
                        mLongClickListener.onLongClick(v);
                    }
                    return;
                }
                 WallpaperTileInfo infos = (WallpaperTileInfo) v.getTag();
                if (v.getId() == R.id.wallpaper_set) {
                	ViewGroup parent = (ViewGroup) v.getParent();
                	infos = (WallpaperTileInfo) parent.getTag();
                }
              final   WallpaperTileInfo info = infos;
                
                if (info.isSelectable()) {
                    if (mSelectedThumb != null) {
                        mSelectedThumb.setSelected(false);
                        mSelectedThumb = null;
                    }
                    mSelectedThumb = v;
                    v.setSelected(true);
                    // TODO: Remove this once the accessibility framework and
                    // services have better support for selection state.
                    v.announceForAccessibility(
                            getString(R.string.announce_selection, v.getContentDescription()));
                }
				if (info instanceof ResourceWallpaperInfo) {
					if (v.getId() == R.id.wallpaper_set) {

						AlertDialog.Builder applyDialog = new Builder(
								v.getContext());
						applyDialog.setItems(R.array.applytype,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch (which) {
										case 0:
											mApplyType = WALLAPPLYTYPE.DESK;
											info.onClick(WallpaperPickerActivity.this);
											info.onSave(WallpaperPickerActivity.this);
											break;
										case 1:
											mApplyType = WALLAPPLYTYPE.LOCK;
											applyType(
													mApplyType,
													(ResourceWallpaperInfo) info);
											break;
										case 2:
											mApplyType = WALLAPPLYTYPE.ALL;
											info.onClick(WallpaperPickerActivity.this);
											info.onSave(WallpaperPickerActivity.this);
											applyType(
													mApplyType,
													(ResourceWallpaperInfo) info);
											break;
										default:
											mApplyType = WALLAPPLYTYPE.DESK;
											break;
										}
									}
								});
						applyDialog.show();
					}
					else {
						info.onClick(WallpaperPickerActivity.this);
						info.onSave(WallpaperPickerActivity.this);// ¡§?¡§a?¡§?¡§|¡§¡§???¨¤¡§2????
					}
				} else {
					info.onClick(WallpaperPickerActivity.this);
					info.onSave(WallpaperPickerActivity.this);// ¡§?¡§a?¡§?¡§|¡§¡§???¨¤¡§2????
				}
                
            }
            
				//add by zhouerlong
            };
        mLongClickListener = new View.OnLongClickListener() {
            // Called when the user long-clicks on someView
            public boolean onLongClick(View view) {
                CheckableFrameLayout c = (CheckableFrameLayout) view;
                c.toggle();

                if (mActionMode != null) {
                    mActionMode.invalidate();
                } else {
                    // Start the CAB using the ActionMode.Callback defined below
                    mActionMode = startActionMode(mActionModeCallback);
                    int childCount = mWallpapersView.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        mWallpapersView.getChildAt(i).setSelected(false);
                    }
                }
                return true;
            }
        };

        /// M: for smart book
        if(getSmartBookPrefs() != isSmartBookPluggedIn(this)) {
            deleteDefaultWallpaperThumbnail();
        }

        // Populate the built-in wallpapers
        ArrayList<ResourceWallpaperInfo> wallpapers = findBundledWallpapers();
        if (wallpapers.size()>0) {
        	wallpapers.get(0).onClick(this);
        }
        mWallpapersView = (LinearLayout) findViewById(R.id.wallpaper_list);
        BuiltInWallpapersAdapter ia = new BuiltInWallpapersAdapter(this, wallpapers);
        populateWallpapersFromAdapter(mWallpapersView, ia, false, true);

        // Populate the saved wallpapers
        mSavedImages = new SavedWallpaperImages(this);
        mSavedImages.loadThumbnailsAndImageIdList();
        populateWallpapersFromAdapter(mWallpapersView, mSavedImages, true, true);

        //xieweiwei_20150205_add_begin
        if (!mIsSetLockscreenWallpaper) {
        //xieweiwei_20150205_add_end

        // Populate the live wallpapers
        final LinearLayout liveWallpapersView =
                (LinearLayout) findViewById(R.id.live_wallpaper_list);
        final LiveWallpaperListAdapter a = new LiveWallpaperListAdapter(this);
        a.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                liveWallpapersView.removeAllViews();
                populateWallpapersFromAdapter(liveWallpapersView, a, false, false);
                initializeScrollForRtl();
//                updateTileIndices();
            }
        });

        //xieweiwei_20150205_add_begin
        }
        if (!mIsSetLockscreenWallpaper) {
        //xieweiwei_20150205_add_end

        // Populate the third-party wallpaper pickers
        final LinearLayout thirdPartyWallpapersView =
                (LinearLayout) findViewById(R.id.third_party_wallpaper_list);
        final ThirdPartyWallpaperPickerListAdapter ta =
                new ThirdPartyWallpaperPickerListAdapter(this);
        populateWallpapersFromAdapter(thirdPartyWallpapersView, ta, false, false);

        //xieweiwei_20150205_add_begin
        }
        //xieweiwei_20150205_add_end

        // Add a tile for the Gallery
        LinearLayout masterWallpaperList = (LinearLayout) findViewById(R.id.master_wallpaper_list);
        FrameLayout pickImageTile = (FrameLayout) getLayoutInflater().
                inflate(R.layout.wallpaper_picker_image_picker_item, masterWallpaperList, false);
        setWallpaperItemPaddingToZero(pickImageTile);
//        masterWallpaperList.addView(pickImageTile, 0);

        /// M
        ///* M alps01601180, use AsnycTask get last thumbnail.
        // Make its background the last photo taken on external storage
        /*Bitmap lastPhoto = getThumbnailOfLastPhoto();
        if (lastPhoto != null) {
            ImageView galleryThumbnailBg =
                    (ImageView) pickImageTile.findViewById(R.id.wallpaper_image);
            galleryThumbnailBg.setImageBitmap(getThumbnailOfLastPhoto());
            int colorOverlay = getResources().getColor(R.color.wallpaper_picker_translucent_gray);
            galleryThumbnailBg.setColorFilter(colorOverlay, PorterDuff.Mode.SRC_ATOP);
        }*/

        /* Begin: Delete by bxc 2015-03-09, the code is not used, so delete patch code block.*/
        /*GetLastThumbnailTask task = new GetLastThumbnailTask(this,pickImageTile);
        task.execute();*/
        /* End: Delete by bxc 2015-03-09, the code is not used, so delete patch code block.*/
        /// M


        PickImageInfo pickImageInfo = new PickImageInfo();
        pickImageTile.setTag(pickImageInfo);
        pickImageInfo.setView(pickImageTile);
        pickImageTile.setOnClickListener(mThumbnailOnClickListener);
        pickImageInfo.setView(pickImageTile);

//        updateTileIndices();

        // Update the scroll for RTL
        initializeScrollForRtl();

        // Create smooth layout transitions for when items are deleted
        final LayoutTransition transitioner = new LayoutTransition();
        transitioner.setDuration(200);
        transitioner.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, null);
        mWallpapersView.setLayoutTransition(transitioner);

        // Action bar
        // Show the custom action bar view
        /*final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_set_wallpaper);
        actionBar.getCustomView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSelectedThumb != null) {
                            WallpaperTileInfo info = (WallpaperTileInfo) mSelectedThumb.getTag();
                            info.onSave(WallpaperPickerActivity.this);
                        }
                    }
                });*/
			//M by zhouerlong

        // CAB for deleting items
        mActionModeCallback = new ActionMode.Callback() {
            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cab_delete_wallpapers, menu);
                return true;
            }

            private int numCheckedItems() {
                int childCount = mWallpapersView.getChildCount();
                int numCheckedItems = 0;
                for (int i = 0; i < childCount; i++) {
                    CheckableFrameLayout c = (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                    if (c.isChecked()) {
                        numCheckedItems++;
                    }
                }
                return numCheckedItems;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode,
            // but may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                int numCheckedItems = numCheckedItems();
                if (numCheckedItems == 0) {
                    mode.finish();
                    return true;
                } else {
                    mode.setTitle(getResources().getQuantityString(
                            R.plurals.number_of_items_selected, numCheckedItems, numCheckedItems));
                    return true;
                }
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_delete) {
                    int childCount = mWallpapersView.getChildCount();

                    /// M: find out selected thumb index
                    int selectedIndex = -1;
                    for (int i = 0; i < childCount; i++) {
                        if(mSelectedThumb == mWallpapersView.getChildAt(i)) {
                            selectedIndex = i;
                            break;
                        }
                    }

                    ArrayList<View> viewsToRemove = new ArrayList<View>();
                    for (int i = 0; i < childCount; i++) {
                        CheckableFrameLayout c =
                                (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                        if (c.isChecked()) {
                            WallpaperTileInfo info = (WallpaperTileInfo) c.getTag();
                            info.onDelete(WallpaperPickerActivity.this);
                            viewsToRemove.add(c);
                            /// M: update temp wallpaper tiles
                            if(info instanceof UriWallpaperInfo) {
                                UriWallpaperInfo uriInfo = (UriWallpaperInfo)info;
                                int size = mTempWallpaperTiles.size();
                                for (int j = size -1; j >= 0; j--) {
                                    if(mTempWallpaperTiles.get(j).equals(uriInfo.getUri())) {
                                        mTempWallpaperTiles.remove(j);
                                        break;
                                    }
                                }
                            }
                            if(selectedIndex == i) {
                                selectedIndex = -1;
                            }
                        }
                    }
                    for (View v : viewsToRemove) {
                        mWallpapersView.removeView(v);
                    }
//                    updateTileIndices();

                    /// M: old thumb title was removed, and need to update it
                    if(selectedIndex == -1) {
                        childCount = mWallpapersView.getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            CheckableFrameLayout c = (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                            WallpaperTileInfo info = (WallpaperTileInfo) c.getTag();
                            mSelectedThumb = mWallpapersView.getChildAt(i);
                            mSelectedThumb.setSelected(true);
                            // TODO: Remove this once the accessibility framework and
                            // services have better support for selection state.
                            mSelectedThumb.announceForAccessibility(
                                    getString(R.string.announce_selection, mSelectedThumb.getContentDescription()));
                            info.onClick(WallpaperPickerActivity.this);
                            break;
                        }
                    }

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                } else {
                    return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                int childCount = mWallpapersView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    CheckableFrameLayout c = (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                    c.setChecked(false);
                }
                mSelectedThumb.setSelected(true);
                mActionMode = null;
            }
        };
    }
    enum WALLAPPLYTYPE {
		ALL,DESK,LOCK
	};// add by zhouerlong

	WALLAPPLYTYPE mApplyType;
    public String applyType(WALLAPPLYTYPE type,ResourceWallpaperInfo info) {
    	if (type != WALLAPPLYTYPE.DESK) {
    		  final ProgressDialog dialog = new ProgressDialog(WallpaperPickerActivity.this);
    	        dialog.setMessage(WallpaperPickerActivity.this.getString(R.string.dialog_loading));
    	        dialog.setIndeterminate(false);
    	        dialog.setCancelable(false);
    	        dialog.show();
    	        ApplyWallLockTask applyLock = new ApplyWallLockTask(info.mResources,dialog);
    	        applyLock.execute(info.mResId);
    	        return applyLock.getPath();
    	        
    	        
    	}
    	return null;
    }

    
    class ApplyWallLockTask extends AsyncTask<Integer , Boolean, Boolean> {

    	Resources mRes;
    	ProgressDialog mDialog;
    	String path;
		public ApplyWallLockTask(Resources mRes,ProgressDialog d) {
			super();
			this.mRes = mRes;
			mDialog=d;
		}

		@Override
		protected Boolean doInBackground(Integer... resIds) {
			InputStream is =mRes.openRawResource(resIds[0]);
			boolean isSuccess =FileUtils.copyFile(is, getRawFile(String.valueOf(resIds[0])+".png"));

			return isSuccess;
		}
		
		private File getRawFile(String fileName) {
			File dir = WallpaperPickerActivity.this.getExternalFilesDir(IMG_CACHE);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(dir,fileName);
			path = file.getPath();
			return file;
			
		}
		
		public String getPath() {
			return path;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result) {
				Settings.System.putString(WallpaperPickerActivity.this.getContentResolver(), "keyguard_wallpaper", path);
//				Toast.makeText(WallpaperPickerActivity.this, "锁屏壁纸设置成功", Toast.LENGTH_SHORT).show();

			}else {
//				Toast.makeText(WallpaperPickerActivity.this, "锁屏壁纸设置错误", Toast.LENGTH_SHORT).show();
			}
            OnSetWallpaperFinish();//add b zel
			mDialog.dismiss();
			
		}
		
		
    	
    }
    private void initializeScrollForRtl() {
        final HorizontalScrollView scroll =
                (HorizontalScrollView) findViewById(R.id.wallpaper_scroll_container);
        if (scroll ==null) {
        	return;
        }
        if (scroll.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            final ViewTreeObserver observer = scroll.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    LinearLayout masterWallpaperList =
                            (LinearLayout) findViewById(R.id.master_wallpaper_list);
                    scroll.scrollTo(masterWallpaperList.getWidth(), 0);
                    scroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    public boolean enableRotation() {
        return super.enableRotation() || Launcher.sForceEnableRotation;
    }

    protected Bitmap getThumbnailOfLastPhoto() {
        Cursor cursor = MediaStore.Images.Media.query(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATE_TAKEN},
                null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1");
        /// M: cursor may be null when in USB mode
        if(cursor == null) {
            return null;
        }
        Bitmap thumb = null;
        if (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            thumb = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(),
                    id, MediaStore.Images.Thumbnails.MINI_KIND, null);
        }
        cursor.close();
        return thumb;
    }

    protected void onStop() {
        super.onStop();
        mWallpaperStrip = findViewById(R.id.wallpaper_strip);
        if (mWallpaperStrip.getAlpha() < 1f) {
            mWallpaperStrip.setAlpha(1f);
            mWallpaperStrip.setVisibility(View.VISIBLE);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TEMP_WALLPAPER_TILES, mTempWallpaperTiles);

        //xieweiwei_20150205_add_begin
        outState.putBoolean(Utilities.KEY_IS_SET_LOCKSCREEN_WALLPAPER, mIsSetLockscreenWallpaper);
        //xieweiwei_20150205_add_end

    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        ArrayList<Uri> uris = savedInstanceState.getParcelableArrayList(TEMP_WALLPAPER_TILES);
        for (Uri uri : uris) {
            addTemporaryWallpaperTile(uri);
        }

        //xieweiwei_20150205_add_begin
        mIsSetLockscreenWallpaper = savedInstanceState.getBoolean(Utilities.KEY_IS_SET_LOCKSCREEN_WALLPAPER, false);
        //xieweiwei_20150205_add_end

    }

    private void populateWallpapersFromAdapter(ViewGroup parent, BaseAdapter adapter,
            boolean addLongPressHandler, boolean selectFirstTile) {
        for (int i = 0; i < adapter.getCount(); i++) {
            FrameLayout thumbnail = (FrameLayout) adapter.getView(i, null, parent);
            parent.addView(thumbnail, i);
            WallpaperTileInfo info = (WallpaperTileInfo) adapter.getItem(i);
            thumbnail.setTag(info);
            info.setView(thumbnail);
            if (addLongPressHandler) {
                addLongPressHandler(thumbnail);
            }
            thumbnail.setOnClickListener(mThumbnailOnClickListener);
            if (i == 0 && selectFirstTile) {
//                mThumbnailOnClickListener.onClick(thumbnail);//?¨¢?¨¦¡§o¡§a|¨¬??a¡§¡濡§o?2?¡§¡§? launcher ¡§¡὿-¡§¡潿?¡§a¡§|¡§¡§???¨¤¡§2??
//del by zhouerlong
            }
        }
    }

    private void updateTileIndices() {
        LinearLayout masterWallpaperList = (LinearLayout) findViewById(R.id.master_wallpaper_list);
        if (masterWallpaperList==null) {
        	return ;
        }
        final int childCount = masterWallpaperList.getChildCount();
        final Resources res = getResources();

        // Do two passes; the first pass gets the total number of tiles
        int numTiles = 0;
        for (int passNum = 0; passNum < 2; passNum++) {
            int tileIndex = 0;
            for (int i = 0; i < childCount; i++) {
                View child = masterWallpaperList.getChildAt(i);
                LinearLayout subList;

                int subListStart;
                int subListEnd;
                if (child.getTag() instanceof WallpaperTileInfo) {
                    subList = masterWallpaperList;
                    subListStart = i;
                    subListEnd = i + 1;
                } else { // if (child instanceof LinearLayout) {
                    subList = (LinearLayout) child;
                    subListStart = 0;
                    subListEnd = subList.getChildCount();
                }

                for (int j = subListStart; j < subListEnd; j++) {
                    WallpaperTileInfo info = (WallpaperTileInfo) subList.getChildAt(j).getTag();
                    if (info.isNamelessWallpaper()) {
                        if (passNum == 0) {
                            numTiles++;
                        } else {
                            CharSequence label = res.getString(
                                    R.string.wallpaper_accessibility_name, ++tileIndex, numTiles);
                            info.onIndexUpdated(label);
                        }
                    }
                }
            }
        }
    }

    private static Point getDefaultThumbnailSize(Resources res) {
        return new Point(res.getDimensionPixelSize(R.dimen.wallpaperThumbnailWidth),
                res.getDimensionPixelSize(R.dimen.wallpaperThumbnailHeight));

    }

    private static Bitmap createThumbnail(Point size, Context context, Uri uri, byte[] imageBytes,
            Resources res, int resId, int rotation, boolean leftAligned) {
        int width = size.x;
        int height = size.y;

        BitmapCropTask cropTask;
        if (uri != null) {
            cropTask = new BitmapCropTask(
                    context, uri, null, rotation, width, height, false, true, null);
        } else if (imageBytes != null) {
            cropTask = new BitmapCropTask(
                    imageBytes, null, rotation, width, height, false, true, null);
        }  else {
            cropTask = new BitmapCropTask(
                    context, res, resId, null, rotation, width, height, false, true, null);
        }
        Point bounds = cropTask.getImageBounds();
        if (bounds == null || bounds.x == 0 || bounds.y == 0) {
            return null;
        }

        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(rotation);
        float[] rotatedBounds = new float[] { bounds.x, bounds.y };
        rotateMatrix.mapPoints(rotatedBounds);
        rotatedBounds[0] = Math.abs(rotatedBounds[0]);
        rotatedBounds[1] = Math.abs(rotatedBounds[1]);

        RectF cropRect = WallpaperCropActivity.getMaxCropRect(
                (int) rotatedBounds[0], (int) rotatedBounds[1], width, height, leftAligned);
        cropTask.setCropBounds(cropRect);

        if (cropTask.cropBitmap()) {
            return cropTask.getCroppedBitmap();
        } else {
            return null;
        }
    }

    private void addTemporaryWallpaperTile(Uri uri) {
        mTempWallpaperTiles.add(uri);
        // Add a tile for the image picked from Gallery
        FrameLayout pickedImageThumbnail;
        if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
             pickedImageThumbnail = (FrameLayout) getLayoutInflater().
                    inflate(R.layout.wallpaper_picker_item_mi, mWallpapersView, false);
        }else {
             pickedImageThumbnail = (FrameLayout) getLayoutInflater().
                    inflate(R.layout.wallpaper_picker_item, mWallpapersView, false);
        }
        setWallpaperItemPaddingToZero(pickedImageThumbnail);

        // Load the thumbnail
        ImageView image = (ImageView) pickedImageThumbnail.findViewById(R.id.wallpaper_image);
        Point defaultSize = getDefaultThumbnailSize(this.getResources());
        int rotation = WallpaperCropActivity.getRotationFromExif(this, uri);
        Bitmap thumb = createThumbnail(defaultSize, this, uri, null, null, 0, rotation, false);
        if (thumb != null) {
            image.setImageBitmap(thumb);
            Drawable thumbDrawable = image.getDrawable();
            thumbDrawable.setDither(true);
        } else {
            Log.e(TAG, "Error loading thumbnail for uri=" + uri);
        }
        mWallpapersView.addView(pickedImageThumbnail, 0);

        UriWallpaperInfo info = new UriWallpaperInfo(uri);
        pickedImageThumbnail.setTag(info);
        info.setView(pickedImageThumbnail);
        addLongPressHandler(pickedImageThumbnail);
//        updateTileIndices();
        pickedImageThumbnail.setOnClickListener(mThumbnailOnClickListener);
        mThumbnailOnClickListener.onClick(pickedImageThumbnail);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(true) {
    		return;
    	}
        if (requestCode == IMAGE_PICK && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                addTemporaryWallpaperTile(uri);
            }
        } else if (requestCode == PICK_WALLPAPER_THIRD_PARTY_ACTIVITY) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == PICK_LIVE_WALLPAPER) {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            final WallpaperInfo oldLiveWallpaper = mLiveWallpaperInfoOnPickerLaunch;
            WallpaperInfo newLiveWallpaper = wm.getWallpaperInfo();
            /// M: always return to home screen
            setResult(RESULT_OK);
            finish();
            /*
            // Try to figure out if a live wallpaper was set;
            if (newLiveWallpaper != null &&
                    (oldLiveWallpaper == null ||
                    !oldLiveWallpaper.getComponent().equals(newLiveWallpaper.getComponent()))) {
                // Return if a live wallpaper was set
                setResult(RESULT_OK);
                finish();
            }
            */
        }
        
        else if( requestCode==REQ_PICK_IMG) {
		Uri uri = data.getData();

		ComponentName com = new ComponentName("com.prize.theme.chooser",
				"com.prize.theme.crop.ZoomCropWallActivity");
        Intent cropIntent = new Intent("android.intent.action.ZOOMCROPWALL");
//        cropIntent.setComponent(com);
        cropIntent.putExtra(INTENT_EXTRA_URI, uri);
        cropIntent.putExtra(INTENT_EXTRA_OUTPUT_WIDTH, CROP_IMG_WIDTH);
        cropIntent.putExtra(INTENT_EXTRA_OUTPUT_HEIGHT, CROP_IMG_HEIGHT);
        cropIntent.putExtra(INTENT_EXTRA_CROP_SHAPE, SHAPE_RECTANGLE);
        File fDir = getRawFileDir(IMG_CACHE);
        cropIntent.putExtra(INTENT_EXTRA_SAVE_DIR,
        		fDir.getAbsolutePath());   //optional
        fDir = null;
        cropIntent.putExtra(INTENT_EXTRA_FILE_NAME, COP_FILE_NAME);
        try {

            startActivity(cropIntent);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        }
    }

    private File getRawFileDir(String cache) {
		File dir = WallpaperPickerActivity.this.getExternalFilesDir(cache);
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
		
	}
    
    static void setWallpaperItemPaddingToZero(FrameLayout frameLayout) {
        frameLayout.setPadding(0, 0, 0, 0);
        frameLayout.setForeground(new ZeroPaddingDrawable(frameLayout.getForeground()));
    }

    private void addLongPressHandler(View v) {
        v.setOnLongClickListener(mLongClickListener);
    }

    private ArrayList<ResourceWallpaperInfo> findBundledWallpapers() {
        ArrayList<ResourceWallpaperInfo> bundledWallpapers =
                new ArrayList<ResourceWallpaperInfo>(24);

        Pair<ApplicationInfo, Integer> r = getWallpaperArrayResourceId();
        if (r != null) {
            try {
                Resources wallpaperRes = getPackageManager().getResourcesForApplication(r.first);
                    bundledWallpapers = addWallpapers(wallpaperRes, r.first.packageName, r.second);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        // Add an entry for the default wallpaper (stored in system resources)
        ResourceWallpaperInfo defaultWallpaperInfo = getDefaultWallpaperInfo();
        if (defaultWallpaperInfo != null) {
            bundledWallpapers.add(0, defaultWallpaperInfo);
        }
        return bundledWallpapers;
    }

    private ResourceWallpaperInfo getDefaultWallpaperInfo() {
        Resources sysRes = Resources.getSystem();
        int resId = sysRes.getIdentifier("default_wallpaper", "drawable", "android");

        File defaultThumbFile = new File(getFilesDir(), "default_thumb.jpg");
        Bitmap thumb = null;
        boolean defaultWallpaperExists = false;
        if (defaultThumbFile.exists()) {
            thumb = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            defaultWallpaperExists = true;
        } else {
            Resources res = getResources();
            Point defaultThumbSize = getDefaultThumbnailSize(res);
            int rotation = WallpaperCropActivity.getRotationFromExif(res, resId);
            thumb = createThumbnail(
                    defaultThumbSize, this, null, null, sysRes, resId, rotation, false);
            if (thumb != null) {
                try {
                    defaultThumbFile.createNewFile();
                    FileOutputStream thumbFileStream =
                            openFileOutput(defaultThumbFile.getName(), Context.MODE_PRIVATE);
                    thumb.compress(Bitmap.CompressFormat.JPEG, 95, thumbFileStream);
                    thumbFileStream.close();
                    defaultWallpaperExists = true;
                    setSmartBookPrefs();
                } catch (IOException e) {
                    Log.e(TAG, "Error while writing default wallpaper thumbnail to file " + e);
                    defaultThumbFile.delete();
                }
            }
        }
        if (defaultWallpaperExists) {
            return new ResourceWallpaperInfo(sysRes, resId, new BitmapDrawable(thumb),this);
        }
        return null;
    }
    
    private void deleteDefaultWallpaperThumbnail() {
        File defaultThumbFile = new File(getFilesDir(), "default_thumb.jpg");
        if (defaultThumbFile.exists()) {
            defaultThumbFile.delete();
        }
    }

    public static boolean isSmartBookPluggedIn(Context mContext) {
        if (FeatureOption.MTK_SMARTBOOK_SUPPORT) {
            DisplayManager mDisplayManager = (DisplayManager)mContext.getSystemService(Context.DISPLAY_SERVICE);
            return mDisplayManager.isSmartBookPluggedIn();
        } else {
            return false;
        }
    }
    
    private String getSmartBookPrefsFile() {
        return WallpaperCropActivity.class.getName();
    }

    private void setSmartBookPrefs() {
        String spKey = getSmartBookPrefsFile();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SMARTBOOK_PLUGIN_KEY, isSmartBookPluggedIn(this));
        editor.commit();
    }
    
    private boolean getSmartBookPrefs() {
        String spKey = getSmartBookPrefsFile();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(SMARTBOOK_PLUGIN_KEY, false);
    }

    public Pair<ApplicationInfo, Integer> getWallpaperArrayResourceId() {
        // Context.getPackageName() may return the "original" package name,
        // com.android.launcher3; Resources needs the real package name,
        // com.android.launcher3. So we ask Resources for what it thinks the
        // package name should be.
        final String packageName = getResources().getResourcePackageName(R.array.wallpapers);
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(packageName, 0);
            return new Pair<ApplicationInfo, Integer>(info, R.array.wallpapers);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

	
	
	
    private ArrayList<ResourceWallpaperInfo> addWallpapers(
            Resources res, String packageName, int listResId) {
        ArrayList<ResourceWallpaperInfo> bundledWallpapers =
                new ArrayList<ResourceWallpaperInfo>(24);
        final String[] extras = res.getStringArray(listResId);
        for (String extra : extras) {
            int resId = res.getIdentifier(extra, "drawable", packageName);
            if (resId != 0) {
                final int thumbRes = res.getIdentifier(extra + "_small", "drawable", packageName);

                if (thumbRes != 0) {
                    ResourceWallpaperInfo wallpaperInfo =
                            new ResourceWallpaperInfo(res, resId, res.getDrawable(thumbRes),this);
                    bundledWallpapers.add(wallpaperInfo);
                    // Log.d(TAG, "add: [" + packageName + "]: " + extra + " (" + res + ")");
                }
            } else {
                Log.e(TAG, "Couldn't find wallpaper " + extra);
            }
        }
        return bundledWallpapers;
    }


	

    public CropView getCropView() {
        return mCropView;
    }

    public SavedWallpaperImages getSavedImages() {
        return mSavedImages;
    }

    public void onLiveWallpaperPickerLaunch() {
        mLiveWallpaperInfoOnPickerLaunch = WallpaperManager.getInstance(this).getWallpaperInfo();
    }

    static class ZeroPaddingDrawable extends LevelListDrawable {
        public ZeroPaddingDrawable(Drawable d) {
            super();
            addLevel(0, 0, d);
            setLevel(0);
        }

        @Override
        public boolean getPadding(Rect padding) {
            padding.set(0, 0, 0, 0);
            return true;
        }
    }

    private static class BuiltInWallpapersAdapter extends BaseAdapter implements ListAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<ResourceWallpaperInfo> mWallpapers;

        BuiltInWallpapersAdapter(Activity activity, ArrayList<ResourceWallpaperInfo> wallpapers) {
            mLayoutInflater = activity.getLayoutInflater();
            mWallpapers = wallpapers;
        }

        public int getCount() {
            return mWallpapers.size();
        }

        public ResourceWallpaperInfo getItem(int position) {
            return mWallpapers.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Drawable thumb = mWallpapers.get(position).mThumb;
            if (thumb == null) {
                Log.e(TAG, "Error decoding thumbnail for wallpaper #" + position);
            }
            return createImageTileView(mLayoutInflater, position, convertView, parent, thumb);
        }
    }

    public static View createImageTileView(LayoutInflater layoutInflater, int position,
            View convertView, ViewGroup parent, Drawable thumb) {
        View view;

        if (convertView == null) {
        	if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
                view = layoutInflater.inflate(R.layout.wallpaper_picker_item_mi, parent, false);
        	}else {
                view = layoutInflater.inflate(R.layout.wallpaper_picker_item, parent, false);
        	}
        } else {
            view = convertView;
        }

        setWallpaperItemPaddingToZero((FrameLayout) view);

        ImageView image = (ImageView) view.findViewById(R.id.wallpaper_image);
        ImageView wall_set = (ImageView) view.findViewById(R.id.wallpaper_set);
        wall_set.setOnClickListener(mThumbnailOnClickListener);
        

        if (thumb != null) {
            image.setImageDrawable(thumb);
            thumb.setDither(true);
        }

        return view;
    }
}
