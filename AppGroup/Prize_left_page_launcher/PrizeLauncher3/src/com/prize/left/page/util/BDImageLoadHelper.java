package com.prize.left.page.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;

public class BDImageLoadHelper {
    
    private RequestQueue mRequestQueue;

    private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(50);
    
    public BDImageLoadHelper(Context mContext) {
        mRequestQueue = Volley.newRequestQueue(mContext);
    }
    
    public void executeImageUrl(String url, final ImageView imageView) {
        ImageCache imageCache = new ImageCache() {

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                mLruCache.put(url, bitmap);
            }

            @Override
            public Bitmap getBitmap(String url) {
                return mLruCache.get(url);
            }
        };
        ImageLoader imageLoader = new ImageLoader(mRequestQueue, imageCache);
        ImageListener listener = ImageLoader.getImageListener(imageView, 0, 0);
        imageLoader.get(url, listener);
    }
}
