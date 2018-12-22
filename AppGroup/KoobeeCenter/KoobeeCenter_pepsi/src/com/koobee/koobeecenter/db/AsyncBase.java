package com.koobee.koobeecenter.db;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AsyncBase {
	protected final static int RESULT_AVAILABLE = 1;
	private final static int MAX_THREADS = 4;

	private static BlockingQueue<WorkMessage> sWorkQueue;

	public static interface OnDataAvailable {
		void onDataBack(int id, Object object);
	}

	protected static interface BaseWorkHandler {
		void onHandleMessage(WorkMessage dcMessage);
	}

	protected static class CallbackContainer {
		int id;
		Object replyTo;
		Object arg;
	}

	protected static class WorkMessage {
		int what;
		BaseWorkHandler handler;
		Worker obj;
		Object replyTo;

		WorkMessage(int theWhat, BaseWorkHandler theHandler, Worker theObject,
				Object theReplyTo) {
			what = theWhat;
			handler = theHandler;
			obj = theObject;
			replyTo = theReplyTo;
		}

	}

	protected static interface Worker {
		void doInBackground(int backId, Object replyTo);
	}

	static {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		int count = Math.min(MAX_THREADS, cpuCount);
		sWorkQueue = new LinkedBlockingQueue<WorkMessage>();
		BaseWorkThread[] workThreads = new BaseWorkThread[count];
		for (int index = 0; index < workThreads.length; index++) {
			BaseWorkThread handler = new BaseWorkThread(sWorkQueue,
					"WorkThread: " + index);
			workThreads[index] = handler;
			handler.start();
		}
	}

	protected AsyncBase() {
	}

	protected void sendMessage(int id, BaseWorkHandler handler, Worker obj,
			Object replyTo) {
		WorkMessage message = new WorkMessage(id, handler, obj, replyTo);
		sWorkQueue.add(message);
	}

	protected void sendWorkOverMessage(int id, Object replyTo, Object arg) {
		CallbackContainer callbackContainer = new CallbackContainer();
		callbackContainer.id = id;
		callbackContainer.replyTo = replyTo;
		callbackContainer.arg = arg;

		Message message = new Message();
		message.what = RESULT_AVAILABLE;
		message.obj = callbackContainer;
		mHandler.sendMessage(message);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case RESULT_AVAILABLE:
				CallbackContainer container = (CallbackContainer) msg.obj;
				if (container.replyTo != null) {
					int id = container.id;
					OnDataAvailable onDataAvailable = (OnDataAvailable) container.replyTo;
					Object data = null;
					if (container.arg != null) {
						data = container.arg;
					}
					onDataAvailable.onDataBack(id, data);
				}
				break;
			default:
				break;
			}
		}
	};

	protected static class BaseWorkThread extends Thread {
		protected BlockingQueue<WorkMessage> mMessageQueue;

		protected BaseWorkThread(BlockingQueue<WorkMessage> queue,
				String threadName) {
			super(threadName);
			mMessageQueue = queue;
		}

		@Override
		public void run() {
			while (true) {
				try {
					WorkMessage message = mMessageQueue.take();
					message.handler.onHandleMessage(message);
				} catch (InterruptedException ex) {
					break;
				} catch (android.database.sqlite.SQLiteException e) {
					StackTraceElement stack = new Throwable().getStackTrace()[0];
					String localInfo = stack.getFileName() + ":"
							+ stack.getLineNumber();
					String message = e.toString();
				}
			}
		}
	}
}
