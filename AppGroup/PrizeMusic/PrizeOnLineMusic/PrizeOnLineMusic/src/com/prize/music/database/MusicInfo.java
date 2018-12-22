package com.prize.music.database;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * @see 保存在本地数据库的歌曲类，如收藏/播放历史
 * @author lixing
 *
 */
public class MusicInfo implements Parcelable{
	public String songName;   //歌曲名
	public String singer;  //演唱者
	/***  歌曲的id。包括本地的audio_id 和在线 的song_id, 可以根据  source_type来区分*****/
	public long songId = -1;   // 歌曲的id。包括本地的audio_id 和在线 的song_id, 可以根据  source_type来区分
	public String userId;   //收藏此歌曲的用户的id
	/*** 歌曲类型，根据这个来判断base_id是 本地audio_id，还是在线 的song_id*****/
	public String source_type;  //
	public String table_name;   //该歌曲保存在用户自建的歌单名
	
	public String albumLogo;
	public String albumName;

	public String albumId;
	
	public MusicInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public MusicInfo(String title, String artist, long base_id, String user_id, String source_type){
		this.songName = title;
		this.singer = artist;
		this.songId = base_id;
		this.userId = user_id;
		this.source_type = source_type;
	}


	public MusicInfo(String songName, String singer, long songId,
			String userId, String source_type, 
			 String albumName, String albumId) {
		this.songName = songName;
		this.singer = singer;
		this.songId = songId;
		this.userId = userId;
		this.source_type = source_type;
		this.albumName = albumName;
		this.albumId = albumId;
	}

	public MusicInfo(String songName, String singer,
			String albumName, String albumLogo, long songId,
			String userId, String source_type) {
		this.songName = songName;
		this.singer = singer;
		this.albumName = albumName;
		this.albumLogo = albumLogo;
		this.songId = songId;
		this.userId = userId;
		this.source_type = source_type;
	}

	public void setTitle(String title) {
		this.songName = title;
	}

	public void setArtist(String artist) {
		this.singer = artist;
	}

	public void setBase_id(Long base_id) {
		this.songId = base_id;
	}

	


	@Override
	public String toString() {
		return "MusicInfo [songName=" + songName + ", singer=" + singer
				+ ", songId=" + songId + ", userId=" + userId
				+ ", source_type=" + source_type + ", table_name=" + table_name
				+ ", albumLogo=" + albumLogo + ", albumName=" + albumName
				+ ", albumId=" + albumId + "]";
	}

	/**
	 * @return the user_id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param user_id the user_id to set
	 */
	public void setUserId(String user_id) {
		this.userId = user_id;
	}

	/**
	 * @return the source_type
	 */
	public String getSourceType() {
		return source_type;
	}

	/**
	 * @param source_type the source_type to set
	 */
	public void setSourceType(String source_type) {
		this.source_type = source_type;
	}

	
	public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>(){
		public MusicInfo createFromParcel(Parcel in){
			return new MusicInfo(in);
		}

		public MusicInfo[] newArray(int size){
			return new MusicInfo[size];
		}
	};

	public MusicInfo(Parcel in) {
		// TODO Auto-generated constructor stub
		albumId = in.readString();
		albumName = in.readString();
		albumLogo = in.readString();
		songName = in.readString();
		singer = in.readString();
		songId = in.readLong();
		userId = in.readString();
		source_type = in.readString();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(albumId);
		arg0.writeString(albumName);
		arg0.writeString(albumLogo);
		arg0.writeString(songName);
		arg0.writeString(singer);
		arg0.writeLong(songId);
		arg0.writeString(userId);
		arg0.writeString(source_type);

	}

}
