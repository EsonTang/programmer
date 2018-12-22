package com.prize.music;

import android.graphics.Bitmap;

import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.music.database.MusicInfo;
import android.os.Parcelable;

interface IApolloService
{
    void openFile(String path);
    void open(in long [] list, int position);
    long getIdFromPath(String path);
    int getQueuePosition();
    boolean isPlaying();
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    long duration();
    long position();
    long seek(long pos);
    String getTrackName();
    String getAlbumName();
    long getAlbumId();
    Bitmap getAlbumBitmap();
    String getArtistName();
    long getArtistId();
    void enqueue(in long [] list, int action);
    long [] getQueue();
    void setQueuePosition(int index);
    String getPath();
    long getAudioId();
    void setShuffleMode(int shufflemode);
    void notifyChange(String what);
    int getShuffleMode();
    int removeTracks(int first, int last);
    void moveQueueItem(int from, int to);
    int removeTrack(long id);
    void setRepeatMode(int repeatmode);
    int getRepeatMode();
    int getMediaMountedCount();
    int getAudioSessionId();
	void addToFavorites(long id);
	
	void removeFromFavorites(long id);
	boolean isFavorite(long id);
    boolean toggleFavorite();
    
    void playSongDetailInfo(in SongDetailInfo song_info, String from_class_name,long sheet_id,in List<SongDetailInfo> list, String type);
    void openNetSongById(int songId, String from_class_name,long from_id,in List<SongDetailInfo> list, String type); 
    List<SongDetailInfo> getCurrentNetSongsList();
    SongDetailInfo getCurrentSongDetailInfo();
    void setIsPlayNetSong(boolean isPlayNetSong);
    boolean getIsPlayNetSong();
    String getCurrentPlaySheetType();
    boolean toggleMyLove(in MusicInfo music_info, String post_or_cancel);
    boolean addAllToMyLove(in List<MusicInfo> musics);   
    MusicInfo getCurrentMusicInfo();
    List<MusicInfo> getCurrentMusicInfoList();
    void openMusic(in MusicInfo music_info, String table_name ,in List<MusicInfo> list, String type);
    void openNetSheet(String key, String type);
    int removeMusicInfoTrack(in MusicInfo music_info);
    int getAlbumBitmapColor();
}

