/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class Provider {

	public static final String AUTHORITY = "com.prize.sticker.db";  
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.prize.sticker";  
  
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/prize.sticker";  
  
    public static final class WatermarkColumns implements BaseColumns {  
    	
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/watermark_datas");  
        public static final String TABLE_NAME = "watermark_data";
        public static String DEFAULT_SORT_ORDER = "_id asc";  
          
        public static final String KEY_ID = "mid";  
        public static final String NAME = "name";  
        public static final String URL = "url";  
        public static final String ALBUM_ID = "t_id";  
        public static final String THUMB_URL = "thumburl";  
        public static final String THUMB_PATH = "thumbpath";  
        public static final String IS_STICKER = "sticker";  
        public static final String WIDTH = "m_width";  
        public static final String HEIGHT = "m_height";  
        public static final String IS_COLOR_CHANGE = "color_change";  
        public static final String X_SCREEN = "x_position";  
        public static final String Y_SCREEN = "y_position";  
        public static final String WHERE_ALBUM_ID = "t_id=?";
          
    }  
      
    public static final class AlbumColumns implements BaseColumns {  
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/watermark_albums");  
        public static final String TABLE_NAME = "watermark_album";
        public static String DEFAULT_SORT_ORDER = "_id asc";  
          
        public static final String NAME = "tname";  
        public static final String APP_TYPE = "tid";  
        public static final String KEY = "t_key";
        public static final String WHERE_APP_ID = "tid != ?";    
    }  
    
    public static final class PhotoResColumns implements BaseColumns {  
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/image_resource_datas");  
        public static final String TABLE_NAME = "image_resource_data";
        public static String DEFAULT_SORT_ORDER = "_id asc";  
          
        public static final String RESOURCE_ID = "rid";  
        public static final String WATERMARK_ID = "mid";  
        public static final String RES_PATH = "rpath";  
        public static final String X = "x";  
        public static final String Y = "y";  
        public static final String ABSOLUTE = "absolute";  
        public static final String RELATIVE_ID = "next_id";  
        public static final String URL = "rurl";  
        public static final String WHERE_WATERMARK_ID = "mid=?";  
    }  
    
    public static final class WordResColumns implements BaseColumns {  
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/text_resource_datas");  
        public static final String TABLE_NAME = "text_resource_data";
        public static String DEFAULT_SORT_ORDER = "_id asc";  
          
        public static final String RESOURCE_ID = "rid";  
        public static final String WATERMARK_ID = "mid";  
        public static final String WORD = "word";  
        public static final String TEXT_SIZE = "text_size";  
        public static final String IS_TEXT_VERTICAL = "text_v";  
        public static final String TEXT_COLOR = "text_color";  
        public static final String TYPE = "wtype";  
        public static final String X = "x";  
        public static final String Y = "y";  
        public static final String ALIGN = "align";  
        public static final String LIMIT = "text_limit";  
        public static final String FONT = "font";  
        public static final String TIME_FORMAT = "t_time";  
        public static final String WHERE_WATERMARK_ID = "mid=?";  
    }  
    
    public static final class HistoryColumns implements BaseColumns {  
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/watermark_historys");  
        public static final String TABLE_NAME = "watermark_history";
        public static final String DEFAULT_SORT_ORDER = "edit_time desc";  
        public static final String WHERE_APP_TYPE = "ttype=?";  
        public static final String WHERE_UPDATE = "data_id=?";  
          
        public static final String APP_TYPE = "ttype";  
        public static final String WATERMARK_ID = "data_id";  
        public static final String MODIFY_TIME = "edit_time";  
          
    }  
}
