package com.mediatek.camera.mode.watermark;

import com.android.camera.CameraActivity;
import com.android.camera.ExtensionHelper;
import com.mediatek.camera.ICameraAddition;
import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ICameraAddition.AdditionActionType;
import com.mediatek.camera.ICameraMode.ModeState;
import com.mediatek.camera.mode.PhotoMode;
import com.mediatek.camera.mode.pip.pipwrapping.AnimationRect;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.platform.IFocusManager;
import com.mediatek.camera.platform.ICameraAppUi.ShutterButtonType;
import com.mediatek.camera.platform.ICameraAppUi.SpecViewType;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.platform.IFileSaver.FILE_TYPE;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.util.Log;
import com.prize.mix.PrizeCaptureExecutor;
import com.prize.mix.PrizeRendererManager;
import com.prize.mix.PrizeCaptureExecutor.ImageCallback;
import com.prize.setting.BitmapCreator;

import android.R.integer;
import android.R.xml;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

/*prize-xuchunming-20171211-bugid:44985-start*/
import com.android.camera.bridge.CameraAppUiImpl;
/*prize-xuchunming-20171211-bugid:44985-end*/

import com.android.camera.R;
public class WaterMarkMode extends PhotoMode implements IFocusManager.FocusListener, ICameraAddition.Listener  {
	
private static final String TAG = "WaterMarkMode";
	
	protected static final int ON_CAMERA_PARAMETERS_READY = 101;
    protected static final int INFO_FACE_DETECTED = 102;
    protected static final int ORIENTATION_CHANGED = 103;
    protected static final int ON_BACK_PRESSED = 104;
    protected static final int ON_FULL_SCREEN_CHANGED = 106;
    protected static final int ON_CAMERA_CLOSED = 107;
    protected static final int ON_SETTING_BUTTON_CLICK = 108;
    protected static final int ON_LEAVE_WATERMARK_MODE = 109;
    protected static final int ON_SELFTIMER_CAPTUEING = 110;
    protected static final int IN_PICTURE_TAKEN_PROGRESS = 111;
    protected static final int REMVOE_BACK_TO_NORMAL = 112;
    
    protected static final int ON_CONFIGURATION_CHANGED = 201;
    protected static final int RE_OPEN_GESTURE_SHOT = 202;
    protected static final int RE_OPEN_GESTURE_SHOT_TIME = 1500;
    
    private ICameraView mICameraView = null;
	
	private Handler mHandler;
	
	private int mWbWidth;
	private int mWbHeight;
	private float mX;
	private float mY;
    /*prize-xuchunming-20180411-bugid:50825-start*/
    private static final int POST_VIEW_FORMAT = ImageFormat.NV21;
    /*prize-xuchunming-20180411-bugid:50825-end*/
	public WaterMarkMode(ICameraContext cameraContext) {
		super(cameraContext);
		initRenderer();
		mModeOpened = true;
		mCameraCategory = new WaterMarkCameraCategory();
		mICameraView = mICameraAppUi.getCameraView(SpecViewType.MODE_WATERMARK);
		if (mICameraView != null) {
			mICameraView.init(mActivity, mICameraAppUi, mIModuleCtrl);
		}
		mHandler = new MainHandler(mActivity.getMainLooper());
		if (mActivity instanceof CameraActivity) {
			/*prize-xuchunming-20180108-bugid:47105-start*/
			mPreviewWidth = ((CameraActivity) mActivity).getPreviewSurfaceView().getWidth();
			mPreviewHeight = ((CameraActivity) mActivity).getPreviewSurfaceView().getHeight();
			/*prize-xuchunming-20180108-bugid:47105-end*/
		}
	}
	
	@Override
    public void resume() {
        Log.i(TAG, "[resume]");
        mModeOpened = true;
//        initRenderer();
    }
	
	@Override
	public boolean open() {
		// TODO Auto-generated method stub
		return super.open();
	}
	
	@Override
	public boolean close() {
		Log.i(TAG, "[closeMode]NextMode = " + mIModuleCtrl.getNextMode());
		mModeOpened = false;
		if (mIModuleCtrl.getNextMode() != null) {
			if (mHandler != null) {
				mHandler.sendEmptyMessage(ON_LEAVE_WATERMARK_MODE);
			}
			if (mICameraView != null) {
				mICameraView.update(REMVOE_BACK_TO_NORMAL);
			}
		}
	    // when close the mode ,need remove all the Msg when not execute
		removeAllMsg();
		return super.close();
	}
	
	private void removeAllMsg() {
        if (mHandler != null) {
            mHandler.removeMessages(ON_CAMERA_PARAMETERS_READY);
            mHandler.removeMessages(INFO_FACE_DETECTED);
            mHandler.removeMessages(ORIENTATION_CHANGED);
            mHandler.removeMessages(RE_OPEN_GESTURE_SHOT);
        }
    }
	
	@Override
	public boolean execute(ActionType type, Object... arg) {
		switch (type) {
        case ACTION_ON_CAMERA_OPEN:
            break;

        case ACTION_ON_CAMERA_CLOSE:
            if (mICameraView != null) {
                mICameraView.update(ON_CAMERA_CLOSED);
            }
            break;

        case ACTION_ON_START_PREVIEW:
            break;

        case ACTION_ON_CAMERA_PARAMETERS_READY:
        	if (mHandler != null) {
        		mHandler.sendEmptyMessage(ON_CAMERA_PARAMETERS_READY);
			}
            break;

        case ACTION_FACE_DETECTED:
            break;

        case ACTION_ON_COMPENSATION_CHANGED:
            break;

        case ACTION_ON_FULL_SCREEN_CHANGED:
            if (mICameraView != null) {
                mICameraView.update(ON_FULL_SCREEN_CHANGED, (Boolean) arg[0]);
            }
            break;

        case ACTION_SHUTTER_BUTTON_FOCUS:
            //when focus is detected, not need to do AF
            break;

        case ACTION_PHOTO_SHUTTER_BUTTON_CLICK:
            Log.i(TAG, "ACTION_PHOTO_SHUTTER_BUTTON_CLICK, mode state = " + getModeState());
            break;

        case ACTION_SHUTTER_BUTTON_LONG_PRESS:
            break;

        case ACTION_ON_SINGLE_TAP_UP:
            break;

        case ACTION_ON_BACK_KEY_PRESS:
            // need callback ,if true means not need activity action on-back
            // pressed
            // when just supported Cfb, so need the supper onbackpressed
            if (mICameraView != null) {
                // returnValue is false means need super action the
                // onBackPressed
            	mICameraView.update(ON_BACK_PRESSED);
            }
            break;

        case ACTION_ON_SETTING_BUTTON_CLICK:
            // when user go to setting turn off the face beauty,before change
            // the setting we have hide the VFB UI, when the setting have
            // changed,this time not need show the UI
            if (mICameraView != null) {
                mICameraView.update(ON_SETTING_BUTTON_CLICK, arg[0]);
            }
            break;

        case ACTION_ON_CONFIGURATION_CHANGED:
        	if (mHandler != null) {
                mHandler.sendEmptyMessage(ON_CONFIGURATION_CHANGED);
            }
            break;

        case ACTION_ON_SELFTIMER_STATE:
            if (mICameraView != null) {
                mICameraView.update(ON_SELFTIMER_CAPTUEING, (Boolean) arg[0]);
            }
            break;
            
        case ACTION_ON_PREVIEW_DISPLAY_SIZE_CHANGED:
        	/*prize-xuchunming-20180108-bugid:47105-start*/
        	if (mActivity instanceof CameraActivity) {
    			mPreviewWidth = ((CameraActivity) mActivity).getPreviewSurfaceView().getWidth();
    			mPreviewHeight = ((CameraActivity) mActivity).getPreviewSurfaceView().getHeight();
    		}
        	/*prize-xuchunming-20180108-bugid:47105-start*/
            break;
        case ACTION_SHOW_INFO:
        	((WaterMarkModeView)mICameraView).hideCloseTv();
            break;
        case ACTION_HIDE_INFO:
        	((WaterMarkModeView)mICameraView).showCloseTv();
            break;
        default:
            return false;
        }
		return super.execute(type, arg);
	}
	
	private int mPreviewWidth;
	private int mPreviewHeight;
	
	private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "[handleMessage],msg = " + msg.what);

            switch (msg.what) {
            // this msg just used for VFB,so if you want use cFB,please be
            // careful
            case ON_CAMERA_PARAMETERS_READY:
                if (mICameraView != null) {
                    mICameraView.update(ON_CAMERA_PARAMETERS_READY);
                    mICameraView.show();
                }
                break;

            case ON_LEAVE_WATERMARK_MODE:
                if (mICameraView != null) {
                    mICameraView.update(ON_LEAVE_WATERMARK_MODE);
                }
                break;

            case ON_CONFIGURATION_CHANGED:
                // because configuration change,so need re-inflate the view
                // layout
                if (mICameraView != null) {
                    mICameraView.uninit();
                    mICameraView.init(mActivity, mICameraAppUi, mIModuleCtrl);
                }
                break;

            case RE_OPEN_GESTURE_SHOT:
                mICameraDevice.startGestureDetection();
                Log.i(TAG, "[handleMessage],end of re-startGD ");
                break;

            default:
                break;
            }
        }
    }
	
	@Override
	public void pause() {
		super.pause();
		mModeOpened = false;
		Log.i(TAG, "[pause()] mICameraView = " + mICameraView);
		// Need hide the view when activity is onPause
		if (mICameraView != null) {
			mICameraView.hide();
		}
	}

	private PrizeRendererManager mRendererManager;
	private PrizeCaptureExecutor mCaptureExecutor;
	private int mPictureWidth = 720;
    private int mPictureHeight = 960;
    private int mCaptureOrientation;
    private boolean mModeOpened = false;
	private PrizeCaptureExecutor.ImageCallback mImageCallback = new ImageCallback() {
		
		@Override
		public void unlockNextCapture() {
		}
		
		@Override
		public void onPictureTaken(byte[] jpegData) {
			// TODO Auto-generated method stub
			Log.i(TAG, "[onPIPPictureTaken]jpegData");
			if (jpegData == null) {
	            Log.i(TAG, "[onPIPPictureTaken]jpegData is null,return!");
	            if (mICameraView != null) {
                    mICameraView.update(IN_PICTURE_TAKEN_PROGRESS, false);
                }
	            unInitPIPRenderer();
	            mICameraAppUi.setSwipeEnabled(true);
                mICameraAppUi.restoreViewState();
                restartPreview(false);
	            return;
	        }
	        if (mICameraView != null) {
                mICameraView.update(IN_PICTURE_TAKEN_PROGRESS, false);
            }
	        mActivity.runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	Log.i(TAG, "[mCameraClosed =]" + mCameraClosed + "mModeOpened=" + mModeOpened);
	            	unInitPIPRenderer();
	                if (!mCameraClosed) {
	                	restartPreview(true);
	                }
	            }
	        });
	        mIFileSaver.init(FILE_TYPE.JPEG, 0, null, -1);
	        mIFileSaver.savePhotoFile(jpegData, null, mCaptureStartTime,
	                mIModuleCtrl.getLocation(), 0, null);
	        jpegData = null;
	        System.gc();
		}
		
		@Override
		public PrizeRendererManager getRendererManager() {
			// TODO Auto-generated method stub
			return mRendererManager;
		}
		
		@Override
		public AnimationRect getPreviewAnimationRect() {
			// TODO Auto-generated method stub
			AnimationRect animationRect = new AnimationRect();
			animationRect.setRendererSize(mWbWidth, mWbHeight);
			return animationRect;
		}
	};

	public void initRenderer() {
		Log.i(TAG, "initPIPRenderer");
		if (mRendererManager == null) {
			mRendererManager = new PrizeRendererManager(mActivity);
		}
		if (mCaptureExecutor == null) {
			mCaptureExecutor = new PrizeCaptureExecutor(mActivity,
					mRendererManager, mImageCallback);
		}
		mCaptureExecutor.init();
	}
	
	public void unInitPIPRenderer() {
		Log.i(TAG, "[unInitPIPRenderer]");
        if (mCaptureExecutor != null) {
        	mCaptureExecutor.unInit();
        }
        if (mRendererManager != null) {
        	mRendererManager.unInit();
        }
    }
	
	private void updatePictureSize() {
        Log.d(TAG, "[updatePictureSize]...");
        mCaptureOrientation = mIModuleCtrl.getOrientation();
        Size size = mICameraDeviceManager
                .getCameraDevice(mICameraDeviceManager.getCurrentCameraId())
                .getParameters().getPictureSize();
        if (size == null) {
            Log.i(TAG, "updatePictureSize size==null");
            return;
        }
        if (mCaptureOrientation % 180 == 0) {
            mPictureWidth = size.height;
            mPictureHeight = size.width;
        } else {
            mPictureWidth = size.width;
            mPictureHeight = size.height;
        }
    }
	
	private final ShutterCallback mShutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
        	/*prize-xuchunming-20171211-bugid:44985-start*/
        	if (((CameraAppUiImpl)((CameraActivity)mActivity).getCameraAppUI()) != null) {
                ((CameraAppUiImpl)((CameraActivity)mActivity).getCameraAppUI()).loadCaptureAnimation();
            }
        	/*prize-xuchunming-20171211-bugid:44985-end*/
        }
    };

    private final PictureCallback mRawPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    private final PictureCallback mJpegPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] jpegData, Camera camera) {
            Log.i(TAG, "[mJpegPictureCallback]onPictureTaken");
            if (mCaptureExecutor == null) {
                Log.e(TAG,
                        "[onPictureTaken]mJpegPictureCallback,mCaptureExecutor is null!");
                if (mICameraView != null) {
                    mICameraView.update(IN_PICTURE_TAKEN_PROGRESS, false);
                }
                unInitPIPRenderer();
                return;
            }
            mCaptureExecutor.offerJpegData(jpegData, new android.util.Size(mPreviewWidth, mPreviewHeight), true);
            // add for save jpeg
            jpegData = null;
        }
    };
    
    private AnimationRect getWbAnimationRect(int ori) {
		// TODO Auto-generated method stub
		AnimationRect animationRect = new AnimationRect();
		animationRect.setRendererSize(mPreviewWidth, mPreviewHeight);
		Log.i(TAG, "<draw>..mWbWidth=" + mWbWidth + " mWbHeight=" + mWbHeight + " mX=" + mX + " mY=" + mY + " ori=" + ori);
		/*prize-xuchunming-20180108-bugid:47105-start*/
		if (ori == 0) {
			animationRect.initialize(mX, mY+((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView(), mWbWidth + mX, mWbHeight + mY +((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView());
		} else if (ori == 180) {
			animationRect.initialize(mPreviewWidth - mX - mWbWidth, mPreviewHeight - mY - mWbHeight -((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView(), mPreviewWidth - mX, mPreviewHeight - mY - ((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView());
		} else if (ori == 90) {
			animationRect.initialize(mPreviewHeight - mY - mWbWidth - ((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView(), mX, mPreviewHeight - mY - ((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView(), mWbHeight + mX);
		} else if (ori == 270) {
			animationRect.initialize(mY +((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView(), mPreviewWidth - mX - mWbHeight, mWbWidth + mY + ((WaterMarkModeView)mICameraView).getWaterTopMarginToSurfaceView(), mPreviewWidth - mX);
		}
		/*prize-xuchunming-20180108-bugid:47105-end*/
		animationRect.setCurrrentRotationValue(ori);
		return animationRect;
	}
    
	class WaterMarkCameraCategory extends CameraCategory {
        private WaterMarkCameraCategory() {
        }

        @Override
        public void takePicture() {
        	Log.i(TAG, "[takePicture]");
            unInitPIPRenderer();
            initRenderer();
            WaterMarkModeView waterMarkModeView = ((WaterMarkModeView)mICameraView);
            int orientation = waterMarkModeView.getOrientation();
            updatePictureSize();
            PrizeWaterMarkThumbInfo info = waterMarkModeView.getPositionInfo();
            if (info != null) {
            	/*prize-xuchunming-20180108-bugid:47105-start*/
            	mX = waterMarkModeView.getWaterX();//info.getXpostion();
            	mY = waterMarkModeView.getWaterY();//info.getYpostion() + waterMarkModeView.getTopMargin();
            	/*prize-xuchunming-20180108-bugid:47105-end*/
            } else {
            	mX = 0;
            	mY = 0  + waterMarkModeView.getTopMargin();
            }
            Bitmap wbBitmap = waterMarkModeView.getScreenBitmap(orientation, mX, mY);
            mWbWidth = wbBitmap.getWidth();
            mWbHeight = wbBitmap.getHeight();
            mRendererManager.updateResource(wbBitmap);
            mCaptureExecutor.setUpCapture(new android.util.Size(mPictureWidth, mPictureHeight));
            mRendererManager.updateTopGraphic(getWbAnimationRect(orientation));
            if (!mAdditionManager.execute(AdditionActionType.ACTION_TAKEN_PICTURE)) {
            	if (mICameraView != null) {
                    mICameraView.update(IN_PICTURE_TAKEN_PROGRESS, true);
                }
            	perfBoost();
            	/*prize-xuchunming-20161118-bug:-mRawPictureCallback make app crash when zipimagethread capture-start*/
               
                /*prize-xuchunming-20180411-bugid:50825-start*/
            	if(mISettingCtrl != null && mISettingCtrl.getSettingValue(SettingConstants.KEY_HDR) != null && mISettingCtrl.getSettingValue(SettingConstants.KEY_HDR).equals("on")) {
            		mICameraDevice.takePicture(mShutterCallback, null,
                            null, mJpegPictureCallback);
    			}else {
    				mICameraDevice.takePicture(mShutterCallback, null,
           				 mPostViewCallback, mJpegPictureCallback);
    			}
            	/*prize-xuchunming-20180411-bugid:50825-end*/
                /*prize-xuchunming-20161118-bug:-mRawPictureCallback make app crash when zipimagethread capture-end*/
                mICameraAppUi.setViewState(ViewState.VIEW_STATE_CAPTURE);
            }
            waterMarkModeView.refreshHistory();
        }
    }
	
	protected PictureCallback getUncompressedImageCallback() {
			// TODO Auto-generated method stub
			return null;
	}
	
	/*prize-xuchunming-20180411-bugid:50825-start*/
    private final Camera.PictureCallback mPostViewCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            Log.d(TAG, "mPostViewCallback");
            if (bytes != null) {
                //will update the thumbnail
                int rotation = com.android.camera.Util.getJpegRotation(((CameraActivity)mActivity).getCameraId(), ((CameraActivity)mActivity).getOrietation());
                int previewWidth = camera.getParameters().getPreviewSize().width;
                int previewHeight = camera.getParameters().getPreviewSize().height;
                int thumbnialViewWidth = (int)mActivity.getResources().getDimension(R.dimen.thumbnail_image_size);
                Bitmap bitmap = BitmapCreator.createBitmapFromYuv(bytes, POST_VIEW_FORMAT,
                		previewWidth, previewHeight, thumbnialViewWidth,rotation);
                ((CameraAppUiImpl)((CameraActivity)mActivity).getCameraAppUI()).updateThumbnail(bitmap);
            }
        }
    };
    /*prize-xuchunming-20180411-bugid:50825-end*/
}
