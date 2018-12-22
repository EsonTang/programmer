package com.prize.music.online.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.online.bean.CollectionDetailInfo;
import com.prize.music.R;
import com.prize.onlinemusibean.Permission;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * @see 解析网络接口返回数据的工具类
 * @author lixing
 *
 */
public class PrizeParesNetData {

	/**
	 * @see 解析歌曲详情，传入参数为String，返回歌曲详细SongDetailInfo的实例
	 * @author lixing
	 * @param result
	 * @return SongDetailInfo
	 */
	public static SongDetailInfo parseSongDetailInfo(String result) {
		// TODO Auto-generated method stub
		SongDetailInfo songdetailinfo  = null;
		try {
			JSONObject resultJSON = new JSONObject(result);
			int state = resultJSON.getInt("state");
			JLog.i("hu", "state=="+state);
			if(state==20000){
				ToastUtils.showToast(R.string.no_permission_music);
				return null;
			}
			if(state == 0){
				songdetailinfo  = new SongDetailInfo();
				songdetailinfo.setState(state);
				songdetailinfo.setRequestId(resultJSON.getString("request_id"));
				JSONObject dataJSON = resultJSON.getJSONObject("data");
				songdetailinfo.setSongId(dataJSON.getInt("song_id"));
				songdetailinfo.setSongName(dataJSON.getString("song_name"));
				songdetailinfo.setAlbumId(dataJSON.getInt("album_id"));
				songdetailinfo.setPace(dataJSON.getInt("pace"));
				songdetailinfo.setAlbumName(dataJSON.getString("album_name"));
				songdetailinfo.setAlbumLogo(dataJSON.getString("album_logo"));
				songdetailinfo.setArtistId(dataJSON.getInt("artist_id"));
				songdetailinfo.setArtistName(dataJSON.getString("artist_name"));
				songdetailinfo.setSingers(dataJSON.getString("singers"));
				songdetailinfo.setArtistLogo(dataJSON.getString("artist_logo"));
				songdetailinfo.setLength(dataJSON.getInt("length"));
				songdetailinfo.setListenFile(dataJSON.getString("listen_file"));
				songdetailinfo.setLyric(dataJSON.getString("lyric"));
				songdetailinfo.setLyricType(dataJSON.getInt("lyric_type"));
				songdetailinfo.setPlayVolume(dataJSON.getInt("play_volume"));
				songdetailinfo.setMusicType(dataJSON.getInt("music_type"));
				songdetailinfo.setTrack(dataJSON.getInt("track"));
				songdetailinfo.setCDSerial(dataJSON.getInt("cd_serial"));
				songdetailinfo.setQuality(dataJSON.getString("quality"));
				songdetailinfo.setRate(dataJSON.getInt("rate"));
				songdetailinfo.setExpire(dataJSON.getInt("expire"));
				
				JSONObject permissionJSON = dataJSON.getJSONObject("permission");
												
				JSONArray qualityArray = permissionJSON.getJSONArray("quality");
				String[] quality = new String[qualityArray.length()];
				for(int i = 0; i < qualityArray.length(); i++){
					quality[i] = qualityArray.getString(i);
				}
				songdetailinfo.permission.quality = quality;
//				songdetailinfo./*getPermission().*/setQualityPermission(quality);
				
				JSONArray needVipArray = permissionJSON.getJSONArray("need_vip");
				String[] need_vip = new String[needVipArray.length()];
				for(int i = 0; i < needVipArray.length(); i++){
					need_vip[i] = needVipArray.getString(i);
				}
				songdetailinfo.permission.need_vip = need_vip;
				songdetailinfo.permission.available = permissionJSON.getBoolean("available");
//				songdetailinfo./*getPermission().*/setNeedVip(need_vip);				
//				songdetailinfo./*getPermission().*/setAvailable(permissionJSON.getBoolean("available"));				
			}						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return songdetailinfo;
		}
		return songdetailinfo;
	}
	
	
	/**
	 * @author lixing
	 * @see 解析歌单详情，返回的是歌单详细信息(包括里面的歌曲信息),此处歌曲详细信息里没有播放下载地址，可以根据歌曲id重获取播放地址
	 * @param result
	 * @return CollectionDetailInfo
	 */
	public static CollectionDetailInfo parseCollectDetailInfo(String result){
		CollectionDetailInfo info = null ;		
		try {
			JSONObject resultJSON = new JSONObject(result);
			int state = resultJSON.getInt("state");
			if(state == 0){
				JSONObject dataJSON = resultJSON.getJSONObject("data");
				info = new CollectionDetailInfo();
				info.setListId(dataJSON.optLong("list_id"));
				info.setUserId(dataJSON.getInt("user_id"));
				info.setCollectName(dataJSON.getString("collect_name"));
				info.setCollectLogo(dataJSON.getString("collect_logo"));
				info.setDescription(dataJSON.getString("description"));
				info.setSongCount(dataJSON.getInt("song_count"));
				info.setPlayCount(dataJSON.getInt("play_count"));
				info.setGmtCreate(dataJSON.getInt("gmt_create"));
				info.setUserName(dataJSON.getString("user_name"));
				info.setAuthorAvatar(dataJSON.getString("author_avatar"));
				
				JSONArray songsArray = dataJSON.getJSONArray("songs");
				List<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
				for(int i = 0; i < songsArray.length(); i++){
					SongDetailInfo songdetailinfo = new SongDetailInfo();
					songdetailinfo.setSongId(songsArray.getJSONObject(i).getInt("song_id"));
					songdetailinfo.setSongName(songsArray.getJSONObject(i).getString("song_name"));
					songdetailinfo.setAlbumId(songsArray.getJSONObject(i).getInt("album_id"));
					songdetailinfo.setPace(songsArray.getJSONObject(i).getInt("pace"));
					songdetailinfo.setAlbumName(songsArray.getJSONObject(i).getString("album_name"));
					songdetailinfo.setAlbumLogo(songsArray.getJSONObject(i).getString("album_logo"));
					songdetailinfo.setArtistId(songsArray.getJSONObject(i).getInt("artist_id"));
					songdetailinfo.setArtistName(songsArray.getJSONObject(i).getString("artist_name"));
					songdetailinfo.setSingers(songsArray.getJSONObject(i).getString("singers"));
					songdetailinfo.setArtistLogo(songsArray.getJSONObject(i).getString("artist_logo"));
					songdetailinfo.setLength(songsArray.getJSONObject(i).getInt("length"));
					songdetailinfo.setListenFile(songsArray.getJSONObject(i).optString("listen_file"));
					songdetailinfo.setLyric(songsArray.getJSONObject(i).optString("lyric"));
					songdetailinfo.setLyricType(songsArray.getJSONObject(i).optInt("lyric_type"));
					songdetailinfo.setPlayVolume(songsArray.getJSONObject(i).optInt("play_volume"));
					songdetailinfo.setMusicType(songsArray.getJSONObject(i).optInt("music_type"));
					songdetailinfo.setTrack(songsArray.getJSONObject(i).optInt("track"));
					songdetailinfo.setCDSerial(songsArray.getJSONObject(i).optInt("cd_serial"));
					songdetailinfo.setQuality(songsArray.getJSONObject(i).optString("quality"));
					songdetailinfo.setRate(songsArray.getJSONObject(i).optInt("rate"));
					songdetailinfo.setExpire(songsArray.getJSONObject(i).optInt("expire"));
					
					JSONObject permissionJSON = songsArray.getJSONObject(i).getJSONObject("permission");
					
					JSONArray qualityArray = permissionJSON.getJSONArray("quality");
					String[] quality = new String[qualityArray.length()];
					for(int j = 0; j < qualityArray.length(); j++){
						quality[j] = qualityArray.getString(j);
					}
					
					songdetailinfo.permission.quality = quality;
//					songdetailinfo./*getPermission().*/setQualityPermission(quality);
					
					JSONArray needVipArray = permissionJSON.getJSONArray("need_vip");
					String[] need_vip = new String[needVipArray.length()];
					for(int j = 0; j < needVipArray.length(); j++){
						need_vip[j] = needVipArray.getString(j);
					}
					songdetailinfo.permission.need_vip = need_vip;
					songdetailinfo.permission.available = permissionJSON.getBoolean("available");

//					songdetailinfo./*getPermission().*/setNeedVip(need_vip);				
//					songdetailinfo./*getPermission().*/setAvailable(permissionJSON.getBoolean("available"));	
					
					list.add(songdetailinfo);
				}
				
				info.setSongs(list);
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			String error = e.toString();
			Log.d("PrizeParseNet",error);
			e.printStackTrace();
		}
		
		
		
		return info;
	}
	
	
	
	
	
	
	

}
