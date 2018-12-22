package com.prize.music.online.bean;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * @see 描述歌单详细信息的类，包括歌单名，播放次数，歌曲数，和里面所有的歌曲详细信息
 * @author lixing
 *
 */
public class CollectionDetailInfo implements Parcelable{

	private long list_id;
	private long user_id;
	private String collect_name;
	private String collect_logo;
	private String description;
	private int song_count;
	private int play_count;
	private int gmt_create;
	private String user_name;
	private String author_avatar;
	private List<String> tags;
	private List<String> tag_array;
	private List<SongDetailInfo> songs;
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the list_id
	 */
	public long getListId() {
		return list_id;
	}

	/**
	 * @param list_id the list_id to set
	 */
	public void setListId(long list_id) {
		this.list_id = list_id;
	}

	/**
	 * @return the user_id
	 */
	public long getUserId() {
		return user_id;
	}

	/**
	 * @param user_id the user_id to set
	 */
	public void setUserId(long user_id) {
		this.user_id = user_id;
	}

	/**
	 * @return the collect_name
	 */
	public String getCollectName() {
		return collect_name;
	}

	/**
	 * @param collect_name the collect_name to set
	 */
	public void setCollectName(String collect_name) {
		this.collect_name = collect_name;
	}

	/**
	 * @return the collect_logo
	 */
	public String getCollectLogo() {
		return collect_logo;
	}

	/**
	 * @param collect_logo the collect_logo to set
	 */
	public void setCollectLogo(String collect_logo) {
		this.collect_logo = collect_logo;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the song_count
	 */
	public int getSongCount() {
		return song_count;
	}

	/**
	 * @param song_count the song_count to set
	 */
	public void setSongCount(int song_count) {
		this.song_count = song_count;
	}

	/**
	 * @return the play_count
	 */
	public int getPlayCount() {
		return play_count;
	}

	/**
	 * @param play_count the play_count to set
	 */
	public void setPlayCount(int play_count) {
		this.play_count = play_count;
	}

	/**
	 * @return the gmt_create
	 */
	public int getGmtCreate() {
		return gmt_create;
	}

	/**
	 * @param gmt_create the gmt_create to set
	 */
	public void setGmtCreate(int gmt_create) {
		this.gmt_create = gmt_create;
	}

	/**
	 * @return the user_name
	 */
	public String getUserName() {
		return user_name;
	}

	/**
	 * @param user_name the user_name to set
	 */
	public void setUserName(String user_name) {
		this.user_name = user_name;
	}

	/**
	 * @return the author_avatar
	 */
	public String getAuthorAvatar() {
		return author_avatar;
	}

	/**
	 * @param author_avatar the author_avatar to set
	 */
	public void setAuthorAvatar(String author_avatar) {
		this.author_avatar = author_avatar;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the tag_array
	 */
	public List<String> getTagArray() {
		return tag_array;
	}

	/**
	 * @param tag_array the tag_array to set
	 */
	public void setTagArray(List<String> tag_array) {
		this.tag_array = tag_array;
	}

	/**
	 * @return the songs
	 */
	public List<SongDetailInfo> getSongs() {
		return songs;
	}

	/**
	 * @param songs the songs to set
	 */
	public void setSongs(List<SongDetailInfo> songs) {
		this.songs = songs;
	}

}
