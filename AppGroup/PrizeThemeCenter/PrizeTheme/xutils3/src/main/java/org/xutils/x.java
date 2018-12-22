package org.xutils;

import org.xutils.common.TaskController;
import org.xutils.common.task.TaskControllerImpl;
import org.xutils.db.DbManagerImpl;
import org.xutils.http.HttpManagerImpl;

import android.app.Application;

/**
 * Created by wyouflf on 15/6/10. 任务控制中心, http, image, db, view注入等接口的入口.
 * 需要在在application的onCreate中初始化: x.Ext.init(this);
 */
public class x {

	protected x() {
	}

	public static boolean isDebug() {
		return Ext.debug;
	}

	public static Application app() {
		if (Ext.app == null) {
			throw new RuntimeException(
					"please invoke x.Ext.init(app) on Application#onCreate()");
		}
		return Ext.app;
	}

	public static TaskController task() {
		return Ext.taskController;
	}

	public static HttpManager http() {
		if (Ext.httpManager == null) {
			HttpManagerImpl.registerInstance();
		}
		return Ext.httpManager;
	}

	// public static ImageManager image() {
	// if (Ext.imageManager == null) {
	// ImageManagerImpl.registerInstance();
	// }
	// return Ext.imageManager;
	// }

	public static DbManager getDb(DbManager.DaoConfig daoConfig) {
		return DbManagerImpl.getInstance(daoConfig);
	}

	public static class Ext {
		private static boolean debug;
		private static Application app;
		private static TaskController taskController;
		public static HttpManager httpManager;

		private Ext() {
		}

		static {
			TaskControllerImpl.registerInstance();
		}

		public static void init(Application app) {
			if (Ext.app == null) {
				Ext.app = app;
			}
		}

		public static void setDebug(boolean debug) {
			Ext.debug = debug;
		}

		public static void setTaskController(TaskController taskController) {
			if (Ext.taskController == null) {
				Ext.taskController = taskController;
			}
		}

		public static void setHttpManager(HttpManager httpManager) {
			Ext.httpManager = httpManager;
		}

	}
}
