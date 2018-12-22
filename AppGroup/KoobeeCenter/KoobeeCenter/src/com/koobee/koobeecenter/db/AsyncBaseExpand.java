package com.koobee.koobeecenter.db;

/**
 * Created by yiyi .
 */
public abstract class AsyncBaseExpand extends AsyncBase {

	protected AsyncBaseExpand() {
		this.mWorkHandler = new WorkHandler();
	}

	public WorkHandler mWorkHandler;

	private class WorkHandler implements AsyncBase.BaseWorkHandler {

		@Override
		public void onHandleMessage(AsyncBase.WorkMessage msg) {
			if (msg.obj != null)
				msg.obj.doInBackground(msg.what, msg.replyTo);
			else
				sendWorkOverMessage(msg.what, msg.replyTo, null);
		}
	}
}
