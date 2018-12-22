package com.prize.weather.framework.mvp.presenter;

import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.prize.weather.framework.http.IJsonParseExceptionHandler;
import com.prize.weather.framework.http.INetworkExcetpionHandler;
import com.prize.weather.framework.http.NetworkException;
import com.prize.weather.framework.mvp.model.BaseModel;
import com.prize.weather.framework.mvp.view.ICancelListener;
import com.prize.weather.framework.mvp.view.IView;
import com.prize.weather.util.NetworkUtils;

/**
 * 
 * @author wangzhong
 *
 * @param <V>
 * @param <M>
 * @param <R>
 */
public abstract class BasePresenter<V extends IView, M extends BaseModel, R> 
		implements INetworkExcetpionHandler, IJsonParseExceptionHandler, 
		ICancelListener, IUpdate<R> {
	
	protected final static int TO_HANDLE_HTTP_EXCEPTION 		= 9112100;
	protected final static int TO_HANDLE_JSONPARSE_EXCEPTION 	= 9112200;
	
	protected final static int TO_CANCEL						= 9113250;
	
	protected final static int TO_SHOW_PROGRESSDIALOG 			= 9114280;
	protected final static int TO_START_BACKGROUND_TASK 		= 9114300;
	
	protected final static int BACKGROUND_SUCCESS 				= 9115310;
	protected final static int BACKGROUND_FAIL 					= 9115320;	

	protected V mView;
	protected M mModel;
	protected R mBackgroundResult;

	private static HandlerThread mHandlerThread;
	private Handler mBackgroundHandler;
	private Handler mViewHanlder;
	
	/**
	 * <b>Background request</b> is divided into two kinds:<br>
	 * <b>1. false : SQLite connection request;<br>
	 * 2. true : HTTP request.</b>
	 */
	private boolean isHttpRequest = true;
	
	/**
	 * Background thread after return will not go to call the IView subclass update IView interface classes.
	 * 1. true : Cancel the update task;
	 */
	private boolean isCancelUpdateViewTask = false;
	
	private boolean isTimeOutExceptionHappened = false;
	private boolean isHttpRefusedError = false;
	
	protected boolean isHttpRequest() {
		return isHttpRequest;
	}

	protected void setHttpRequest(boolean isHttpRequest) {
		this.isHttpRequest = isHttpRequest;
	}

	/**
	 * <b>Perform the entrance.</b>
	 */
	public final void exeuteMultiTasks() {
		mBackgroundHandler.sendEmptyMessage(TO_START_BACKGROUND_TASK);
		mViewHanlder.sendEmptyMessage(TO_SHOW_PROGRESSDIALOG);
	}
	
	public BasePresenter(V mView) {
		super();
		this.mView = mView;
		
		initMultiTasks();
	}

	private void initMultiTasks() {
		if (null == mHandlerThread) {
			mHandlerThread = new HandlerThread("prize_weather");
			mHandlerThread.start();
		}
		mBackgroundHandler = new Handler(mHandlerThread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				reset();
				handleMsgInBackgroundThread(msg);
			}
		};
		mViewHanlder = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				handleMsgInMainThread(msg);
			}
			
		};
	}
	
	public static void shutdownBackgroundThread() {
		/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
		if (null != mHandlerThread) {
			mHandlerThread.quit();
			mHandlerThread.interrupt();
			mHandlerThread = null;
		}
		/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/
	}
	
	public void closeUpdateViewStatus() {
		mViewHanlder.sendEmptyMessage(TO_CANCEL);
	}
	
	private void reset() {
		isCancelUpdateViewTask = false;
		isTimeOutExceptionHappened = false;
		isHttpRefusedError = false;
	}

	///////////////////////////////////////////////////////////////////////////////
	// BackgroundThread
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * <b>BackgroundThread</b>
	 * @param msg
	 */
	private void handleMsgInBackgroundThread(Message msg) {
		if (!processCommonMsgInBackgroundThread(msg)) {
			customProcessMsgInBackgroundThread(msg);
		}
	}

	/**
	 * <b>When the default process can not meet the requirements, please overwrite the method.</b>
	 * @param msg
	 */
	protected void customProcessMsgInBackgroundThread(Message msg) {
		
	}

	private boolean processCommonMsgInBackgroundThread(Message msg) {
		boolean isProcessed = false;
		if (isHttpRequest && !NetworkUtils.isNetWorkActive()) {
			isCancelUpdateViewTask = true;
			isProcessed = true;
			
			Message mess = mViewHanlder.obtainMessage(TO_HANDLE_HTTP_EXCEPTION, 
					new NetworkException("The network connection is unavailable", 
							NetworkException.ExceptionType.NetworkNotActivie));
			mViewHanlder.sendMessageAtFrontOfQueue(mess);
			return isProcessed;
		}

		switch (msg.what) {
		case TO_START_BACKGROUND_TASK:
			executeBackgroundTask(msg);
			isProcessed = true;
			break;
		case TO_CANCEL:
			mModel.abortRequest();
			isProcessed = true;
			break;
		default:
			isProcessed = false;
			break;
		}
		
		return isProcessed;
	}

	private void executeBackgroundTask(Message msg) {
		mBackgroundResult = doInBackground();
		if (null != mBackgroundResult && !isCancelUpdateViewTask) {
			Message uiMsg = mViewHanlder.obtainMessage(BACKGROUND_SUCCESS, mBackgroundResult);
			mViewHanlder.sendMessageAtFrontOfQueue(uiMsg);
		} else {
			Log.d("WEATHER", "BASE  PRESENTER  executeBackgroundTask() ----> BACKGROUND_FAIL  isCancelUpdateViewTask : " + isCancelUpdateViewTask);
			mViewHanlder.sendEmptyMessage(BACKGROUND_FAIL);
		}
	}

	///////////////////////////////////////////////////////////////////////////////
	// MainThread
	///////////////////////////////////////////////////////////////////////////////
	private void handleMsgInMainThread(Message msg) {
		if (!processCommonMsgInMainThread(msg)) {
			customProcessMsgInMainThread(msg);
		}
	}

	protected void customProcessMsgInMainThread(Message msg) {
		
	}

	@SuppressWarnings("unchecked")
	private boolean processCommonMsgInMainThread(Message msg) {
		boolean isProcessed = false;
		switch (msg.what) {
		case TO_SHOW_PROGRESSDIALOG:
			mViewHanlder.removeMessages(TO_SHOW_PROGRESSDIALOG);
			mView.openUpdateStatus();
			isProcessed = true;
			break;
		case BACKGROUND_SUCCESS:
			mView.closeUpdateStatus();
			updateView((R) msg.obj);
			isProcessed = true;
			break;
		case BACKGROUND_FAIL:
//			mView.closeUpdateStatus();
//			isProcessed = true;
			mView.showNodataView(true);
			isCancelUpdateViewTask = true;
			break;
		case TO_CANCEL:
			mViewHanlder.removeMessages(TO_SHOW_PROGRESSDIALOG);
//			mView.closeUpdateStatus();
//			isProcessed = true;
			isCancelUpdateViewTask = true;
			break;
		case TO_HANDLE_HTTP_EXCEPTION:
//			mView.closeUpdateStatus();
//			isProcessed = true;
			mView.handleException((Exception) msg.obj);
			isCancelUpdateViewTask = true;
			break;
		case TO_HANDLE_JSONPARSE_EXCEPTION:
			mView.closeUpdateStatus();
			mView.handleJsonExcetpion((JSONException) msg.obj);
			isProcessed = true;
			break;
		default:
			isProcessed = false;
			break;
		}
		
		if (isCancelUpdateViewTask) {
			mView.closeUpdateStatus();
			isProcessed = true;
			mViewHanlder.removeMessages(msg.what);
		}
		
		return isProcessed;
	}

	///////////////////////////////////////////////////////////////////////////////
	// implements interface
	///////////////////////////////////////////////////////////////////////////////
	@Override
	public void onCancel() {
		isCancelUpdateViewTask = true;
		mBackgroundHandler.sendEmptyMessage(TO_CANCEL);
		mViewHanlder.sendEmptyMessage(TO_CANCEL);
	}

	@Override
	public void handleParseException(JSONException e) {
		if (isTimeOutExceptionHappened || isHttpRefusedError) {
			return;
		}
		Message msg = mViewHanlder.obtainMessage(TO_HANDLE_JSONPARSE_EXCEPTION, e);
		mViewHanlder.sendMessageAtFrontOfQueue(msg);
	}

	@Override
	public void handleNetworkException(Exception e, String warningMessage) {
		if (e instanceof com.alibaba.fastjson.JSONException) {
			isTimeOutExceptionHappened = true;
			e = new SocketTimeoutException("Timeout data back");
		} else if (e instanceof SocketTimeoutException) {
			isTimeOutExceptionHappened = true;
		}
		mViewHanlder.sendEmptyMessage(BACKGROUND_FAIL);
		Message msg = mViewHanlder.obtainMessage(TO_HANDLE_HTTP_EXCEPTION, e);
		mViewHanlder.sendMessageAtFrontOfQueue(msg);
	}
	
}
