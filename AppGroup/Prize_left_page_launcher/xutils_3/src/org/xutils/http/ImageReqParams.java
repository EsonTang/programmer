package org.xutils.http;

import org.xutils.http.app.ParamsBuilder;
/***
 * added by fanjunchen
 * image 的特殊处理
 * @author fanjunchen
 *
 */
public class ImageReqParams extends RequestParams {

	public ImageReqParams() {
	}

	public ImageReqParams(String uri) {
		super(uri);
	}

	public ImageReqParams(String uri, ParamsBuilder builder, String[] signs,
			String[] cacheKeys) {
		super(uri, builder, signs, cacheKeys);
	}

}
