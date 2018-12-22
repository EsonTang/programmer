package com.prize.onlinemusibean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @see 保存从网络解析的歌曲详细信息的类
 * @author lixing
 *
 */
public class SongDetailInfo implements Parcelable {

	public SongDetailInfo() {

	}

	public Permission permission = new Permission();
	public int state; //

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public String request_id;

	public void setRequestId(String requese_id) {
		this.request_id = requese_id;
	}

	public String getRequestId() {
		return request_id;
	}

	// ===========================Data===========================//
	public int song_id;

	public void setSongId(int song_id) {
		this.song_id = song_id;
	}

	public int getSongId() {
		return song_id;
	}

	public String song_name;

	public void setSongName(String song_name) {
		this.song_name = song_name;
	}

	public String getSongName() {
		return song_name;
	}

	public int album_id;

	public void setAlbumId(int album_id) {
		this.album_id = album_id;
	}

	public int getAlbumId() {
		return album_id;
	}

	public int pace;

	public void setPace(int pace) {
		this.pace = pace;
	}

	public int getPace() {
		return pace;
	}

	public String album_name;

	public void setAlbumName(String album_name) {
		this.album_name = album_name;
	}

	public String getAlbumName() {
		return album_name;
	}

	public String album_logo;

	public void setAlbumLogo(String album_logo) {
		this.album_logo = album_logo;
	}

	public String getAlbumLogo() {
		return album_logo;
	}

	public int artist_id;

	public void setArtistId(int artist_id) {
		this.artist_id = artist_id;
	}

	public int getArtistId() {
		return artist_id;
	}

	public String artist_name;

	public void setArtistName(String artist_name) {
		this.artist_name = artist_name;
	}

	public String getArtistName() {
		return artist_name;
	}

	public String singers;

	public void setSingers(String singers) {
		this.singers = singers;
	}

	public String getSingers() {
		return singers;
	}

	public String artist_logo;

	public void setArtistLogo(String artist_logo) {
		this.artist_logo = artist_logo;
	}

	public String getArtistLogo() {
		return artist_logo;
	}

	public int length;

	public void setLength(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public String listen_file;

	public void setListenFile(String listen_file) {
		this.listen_file = listen_file;
	}

	public String getListenFile() {
		return listen_file;
	}

	public String lyric;

	public void setLyric(String lyric) {
		this.lyric = lyric;
	}

	public String getLyric() {
		return lyric;
	}

	public int lyric_type;

	public void setLyricType(int lyric_type) {
		this.lyric_type = lyric_type;
	}

	public int getLyricType() {
		return lyric_type;
	}

	public double play_volume;

	public void setPlayVolume(double play_volume) {
		this.play_volume = play_volume;
	}

	public double getPlayVolume() {
		return play_volume;
	}

	public int music_type;

	public void setMusicType(int music_type) {
		this.music_type = music_type;
	}

	public int getMusicType() {
		return music_type;
	}

	public int track;

	public void setTrack(int track) {
		this.track = track;
	}

	public int getTrack() {
		return track;
	}

	public int cd_serial;

	public void setCDSerial(int cd_serial) {
		this.cd_serial = cd_serial;
	}

	public int getCDSerial() {
		return cd_serial;
	}

	public String quality;

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getQuality() {
		return quality;
	}

	public int rate;

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getRate() {
		return rate;
	}

	public int expire;

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public int getExpire() {
		return expire;
	}

	// public Permission permission = new Permission();
	// public Permission getPermission(){
	// return permission;
	// }

//	public String[] quality_permission;
//
//	public void setQualityPermission(String[] quality_permission) {
//		this.quality_permission = quality_permission;
//	}
//
//	public String[] getQualityPermission() {
//		return quality_permission;
//	}

//	public String[] need_vip;
//
//	public void setNeedVip(String[] need_vip) {
//		this.need_vip = need_vip;
//	}
//
//	public String[] getNeedVip() {
//		return need_vip;
//	}

//	public boolean available;
//	public int i_available = 0; // 这个变量主要用于序列化
	public int totalSize;
	public String songs_quality;

//	public void setAvailable(boolean available) {
//		this.available = available;
//		if (available) {
//			i_available = 1;
//		} else {
//			i_available = 0;
//		}
//	}
//
//	public boolean getAvailable() {
//		return available;
//	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String resule = "song_id :" + song_id + " ,song_name =" + song_name
				+ " ,album_id = " + album_id + " ,pace = " + pace
				+ " ,album_name =" + album_name + " ,album_logo = "
				+ album_logo + " ,artist_id = " + artist_id
				+ " ,artist_name = " + artist_name + " ,singers = " + singers
				+ " ,artist_logo = " + artist_logo + " ,length = " + length
				+ " ,listen_file = " + listen_file + " ,lyric = " + lyric
				+ " ,lyric_type = " + lyric_type + " ,play_volume = "
				+ play_volume + " , music_type = " + music_type + " ,track = "
				+ track + " ,cd_serial = " + cd_serial + " ,quality = "
				+ quality + " ,rate = " + rate + " ,expire = " + expire;

		return /* super.toString() */resule;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int flag) {
		// TODO Auto-generated method stub
		arg0.writeInt(song_id);
		arg0.writeString(song_name);
		arg0.writeInt(album_id);
		arg0.writeInt(pace);
		arg0.writeString(album_name);
		arg0.writeString(album_logo);
		arg0.writeInt(artist_id);
		arg0.writeString(artist_name);
		arg0.writeString(singers);
		arg0.writeString(artist_logo);
		arg0.writeInt(length);
		arg0.writeString(listen_file);
		arg0.writeString(lyric);
		arg0.writeInt(lyric_type);
		arg0.writeDouble(play_volume);
		arg0.writeInt(music_type);
		arg0.writeInt(track);
		arg0.writeInt(cd_serial);
		arg0.writeString(quality);
		arg0.writeInt(rate);
		arg0.writeInt(expire);
//		arg0.writeStringArray(quality_permission);
//		arg0.writeStringArray(need_vip);
//		arg0.writeInt(i_available);
		arg0.writeParcelable(permission, flag);
	}

	public SongDetailInfo(Parcel arg0) {
		// TODO Auto-generated constructor stub
		song_id = arg0.readInt();
		song_name = arg0.readString();
		album_id = arg0.readInt();
		pace = arg0.readInt();
		album_name = arg0.readString();
		album_logo = arg0.readString();
		artist_id = arg0.readInt();
		artist_name = arg0.readString();
		singers = arg0.readString();
		artist_logo = arg0.readString();
		length = arg0.readInt();
		listen_file = arg0.readString();
		lyric = arg0.readString();
		lyric_type = arg0.readInt();
		play_volume = arg0.readDouble();
		music_type = arg0.readInt();
		track = arg0.readInt();
		cd_serial = arg0.readInt();
		quality = arg0.readString();
		rate = arg0.readInt();
		expire = arg0.readInt();
//		quality_permission = arg0.readStringArray();
//		need_vip = arg0.readStringArray();
//		i_available = arg0.readInt();
//		if (i_available == 0) {
//			available = false;
//		} else if (i_available == 1) {
//			available = true;
//		}
		permission = arg0.readParcelable(Permission.class.getClassLoader());
	}

	public static final Parcelable.Creator<SongDetailInfo> CREATOR = new Parcelable.Creator<SongDetailInfo>() {
		public SongDetailInfo createFromParcel(Parcel in) {
			return new SongDetailInfo(in);
		}

		public SongDetailInfo[] newArray(int size) {
			return new SongDetailInfo[size];
		}
	};
}
