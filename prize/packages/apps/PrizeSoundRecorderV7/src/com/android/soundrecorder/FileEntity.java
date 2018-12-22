package com.android.soundrecorder;

import android.widget.CheckBox;

public class FileEntity {
	private String path;
	private String fileName;
	private String createTime;
	private CheckBox checkBox;
	private boolean isChecked;
	private String duration;
	/// @prize fanjunchen 2015-05-06 {
	/**DB ID*/
	public int _id;
	/// }

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}

	public void setCheckBox(CheckBox checkBox) {
		this.checkBox = checkBox;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "FileEntity [path=" + path + ", fileName=" + fileName
				+ ", createTime=" + createTime + ", checkBox=" + checkBox
				+ ", isChecked=" + isChecked + ", duration=" + duration
				+ ", _id=" + _id + "]";
	}
}
