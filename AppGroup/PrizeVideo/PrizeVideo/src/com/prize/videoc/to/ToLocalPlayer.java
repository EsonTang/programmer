package com.prize.videoc.to;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Video;

public class ToLocalPlayer implements ITarget {

	private Uri uri;

	public ToLocalPlayer(int id) {
		Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
		uri = baseUri.buildUpon().appendPath(String.valueOf(id)).build();
	}

	@Override
	public void jumpTo(Context ctx) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "video/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String EXTRA_ALL_VIDEO_FOLDER = "mediatek.intent.extra.ALL_VIDEO_FOLDER";
		intent.putExtra(EXTRA_ALL_VIDEO_FOLDER, true);
		intent.setComponent(new ComponentName("com.android.gallery3d",
				"com.android.gallery3d.app.MovieActivity"));
		ctx.startActivity(intent);

	}

}
