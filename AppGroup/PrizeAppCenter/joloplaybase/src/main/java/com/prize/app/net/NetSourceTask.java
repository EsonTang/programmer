package com.prize.app.net;

import com.prize.app.net.req.BaseReq;
import com.prize.app.net.req.BaseResp;
import com.prize.app.util.HttpClientUtils;
import com.prize.app.util.JLog;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

/**
 ** 网络请求（实现了Runnable接口，在run方法中请求且回调请求返回结果）
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class NetSourceTask<T extends BaseReq, Q extends BaseResp> implements
		Runnable {
	private OnReslutListener listener = null;
	private String url = null;
	private T req = null;
	private String TAG = "NetSourceTask";

	private static final int MAX_TIMEOUT_TIME = 60 * 1000;
	private static final String GZIP_ENCODING = "gzip";

	/**
	 * @param url
	 *            请求url
	 * @param listener
	 *            结果返回监听
	 * @param req
	 *            请求参数
	 */
	// public NetSourceTask(String url, OnReslutListener listener, T req) {
	// this.url = url;
	// this.listener = listener;
	// this.rams = req;
	// }

	public NetSourceTask(String url, OnReslutListener listener, T req) {
		this.url = url;
		this.listener = listener;
		this.req = req;
	}

	/**
	 * post请求
	 *
	 * @return byte[] 请求返回内容
	 */
	private byte[] postMethod() {
		HttpPost httpRequest = new HttpPost(url);
		httpRequest.setHeader("content-type", "application/x-tar");
		httpRequest.setHeader("Accept-Encoding", GZIP_ENCODING);
		httpRequest.setHeader("Connection", "Keep-Alive");
		HttpClient httpClient = HttpClientUtils.getHttpClient();
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, MAX_TIMEOUT_TIME);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				MAX_TIMEOUT_TIME);
		HttpResponse httpResponse = null;
		int status = -1;
		GZIPInputStream inPutStream = null;
		ByteArrayInputStream gzipByteStream = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			JLog.i(TAG, httpResponse.getParams().getParameter("pageIndex")
					+ "--");
			status = httpResponse.getStatusLine().getStatusCode();
			if (status == 200) {
				/** 判断是否是GZIP **/
				boolean isGzipEncoding = false;

				// 读取数据
				HttpEntity entity = httpResponse.getEntity();
				Header header = entity.getContentEncoding();
				if (null != header) {
					String contentEncoding = header.getValue();
					if ((null != contentEncoding)
							&& contentEncoding.contains(GZIP_ENCODING)) {
						isGzipEncoding = true;
					}
				}

				byte[] entityBytes = EntityUtils.toByteArray(entity);
				if (isGzipEncoding) {
					// 如果是GZIP压缩
					gzipByteStream = new ByteArrayInputStream(entityBytes);
					inPutStream = new GZIPInputStream(gzipByteStream);
					int size = (entityBytes.length << 1);
					ByteArrayBuffer buffer = new ByteArrayBuffer(size);
					byte[] readBuffer = new byte[1024];
					int len = 0;
					while ((len = inPutStream.read(readBuffer)) != -1) {
						buffer.append(readBuffer, 0, len);
					}

					return buffer.toByteArray();
				} else {
					return entityBytes;
				}
			} else {
				httpRequest.abort();
			}
		} catch (Exception e) {
			JLog.i(TAG, e.toString());
		} finally {
			if (null != inPutStream) {
				try {
					inPutStream.close();
				} catch (Exception e) {
				}
			}

			if (null != gzipByteStream) {
				try {
					gzipByteStream.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	@Override
	public void run() {
		byte[] result = postMethod();
		if (null == result) {
			listener.onFailed();
		} else {
			listener.onSucess(result);
		}
	}

	/**
	 * 网络请求 结果监听
	 *
	 * @author prize
	 *
	 */
	public interface OnReslutListener {

		/**
		 * 成功 原始流
		 *
		 * @param bytes
		 */
		void onSucess(byte[] bytes);

		/**
		 * 失败 请求后返回数据为null
		 */
		void onFailed();
	}
}