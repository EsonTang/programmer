package com.prize.music.database;

public class MusicInfo {
	public String title;
	public String artist;
	public Long base_id;
	public Long audio_id;

	public MusicInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setBase_id(Long base_id) {
		this.base_id = base_id;
	}

	public void setAudio_id(Long audio_id) {
		this.audio_id = audio_id;
	}

	@Override
	public String toString() {
		return "MusicInfo [title=" + title + ", artist=" + artist
				+ ", base_id=" + base_id + ", audio_id=" + audio_id + "]";
	}

}
