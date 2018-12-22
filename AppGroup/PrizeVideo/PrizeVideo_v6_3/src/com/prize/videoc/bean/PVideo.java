package com.prize.videoc.bean;

import java.io.Serializable;

import com.lidroid.xutils.db.annotation.Id;

/**
 * Created by yiyi on 2015/6/9.
 */

public class PVideo implements Serializable {

	private static final long serialVersionUID = -9149688992790015154L;

	@Id
	private int _id;
	private int id;
	private String title;
	private String album;
	private String artist;
	private String displayName;
	private String mimeType;
	private String path;
	private long size;
	private long duration;
	private long timeStamp;

	/**
     *
     */
	public PVideo() {
		super();
	}

	public PVideo(int id, String title, String album, String artist,
			String displayName, String mimeType, String path, long size,
			long duration) {
		super();
		this.id = id;
		this.title = title;
		this.album = album;
		this.artist = artist;
		this.displayName = displayName;
		this.mimeType = mimeType;
		this.path = path;
		this.size = size;
		this.duration = duration;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

}
