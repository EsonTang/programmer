package com.android.launcher3.search;

import com.android.launcher3.search.data.AppsBean;
import com.android.launcher3.search.data.ContactsBean;
import com.android.launcher3.search.data.MmsBean;
import com.android.launcher3.search.data.MusicBean;
import com.android.launcher3.search.data.NotesBean;

import android.graphics.drawable.Drawable;

public class GroupMemberBean {

	private String name; // 显示的数据
	private String sortLetters; // 显示数据拼音的首字母
	private Drawable icon;
	private String msg_snippet;
	private String address;
	public Long getDate() {
		return date;
	}
	public String groupTitle;
	
	public MmsBean mms;
	
	public NotesBean notes;

	private AppsBean apps;
	
	private MusicBean music;
	
	public MusicBean getMusic() {
		return music;
	}
	
	

	public void setMusic(MusicBean music) {
		this.music = music;
	}
	private ContactsBean contacts;
	public void setContacts(ContactsBean contacts) {
		this.contacts = contacts;
	}

	public AppsBean getApps() {
		return apps;
	}

	public ContactsBean getContacts() {
		return contacts;
	}

	public void setApps(AppsBean apps) {
		this.apps = apps;
	}

	private Long date;
	private String read;

	private String thread_id;
	private String msg_count;

	public String getThread_id() {
		return thread_id;
	}

	public void setThread_id(String thread_id) {
		this.thread_id = thread_id;
	}

	public String getMsg_count() {
		return msg_count;
	}

	public void setMsg_count(String msg_count) {
		this.msg_count = msg_count;
	}

	public String getMsg_snippet() {
		return msg_snippet;
	}

	public void setMsg_snippet(String msg_snippet) {
		this.msg_snippet = msg_snippet;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRead() {
		return read;
	}

	public void setRead(String read) {
		this.read = read;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
