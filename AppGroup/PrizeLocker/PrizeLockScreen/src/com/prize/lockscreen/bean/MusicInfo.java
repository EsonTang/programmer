package com.prize.lockscreen.bean;

public class MusicInfo {

	private static boolean isMusic;
	private static boolean playing;
	private static String artistName;
	private static String musicName;

	public static boolean isMusic() {
		return isMusic;
	}

	public static void setMusic(boolean isMusic) {
		MusicInfo.isMusic = isMusic;
	}

	public static boolean isPlaying() {
		return playing;
	}

	public static void setPlaying(boolean playing) {
		MusicInfo.playing = playing;
	}

	public static String getArtistName() {
		return artistName;
	}

	public static void setArtistName(String artistName) {
		MusicInfo.artistName = artistName;
	}

	public static String getMusicName() {
		return musicName;
	}

	public static void setMusicName(String musicName) {
		MusicInfo.musicName = musicName;
	}

}