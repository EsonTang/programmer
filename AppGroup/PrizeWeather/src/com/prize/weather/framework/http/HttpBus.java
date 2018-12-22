package com.prize.weather.framework.http;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.prize.weather.framework.Constants;
import com.prize.weather.util.FastJsonUtils;
import com.prize.weather.util.NetworkUtils;

/**
 * 
 * @author wangzhong
 *
 */
@SuppressWarnings("deprecation")
public class HttpBus {

	private final static String TAG = HttpConnection.class.getSimpleName();
	
	private static HttpBus instance;
	private static DefaultHttpClient mHttpClient = createClient();
	private HttpPost mHttpPost;
	private HttpGet mHttpGet;
	private HttpEntity mHttpEntity;
	private HttpResponse mHttpResponse;
	
	private static CookieStore mCookieStore = null;
	
	private INetworkExcetpionHandler mINetworkExcetpionHandler;
	
	// 发起请求并建立连接的超时设定为20秒。
	private static final int CONNECTION_TIMEOUT = 20 * 1000;
	// 建立连接到获取数据的超时设定为20秒。
	private static final int SOCKET_TIMEOUT 	= 20 * 1000;
	// 连续两个小时之内不发起请求，整个应用的连接都超时。
	private static final long MCC_TIMEOUT 		= 2 * 3600 * 1000;
	
	public void setNetWorkExcetpionHandler(
			INetworkExcetpionHandler mINetworkExcetpionHandler) {
		this.mINetworkExcetpionHandler = mINetworkExcetpionHandler;
	}

	private HttpBus() {
		
	}
	
	public static HttpBus getInstance() {
		if (instance == null) {
			synchronized (HttpBus.class) {
				if (instance == null) {
					instance = new HttpBus();
				}
			}
		}
		return instance;
	}

	/**
	 * 
	 * @return DefaultHttpClient
	 */
	private static DefaultHttpClient createClient() {
		BasicHttpParams params = new BasicHttpParams();
		// 设置HttpClient支持http和https两种模式。（http对应的80端口，https对应的443端口）
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		// 使用线程安全的连接管理来创建HttpClient.
		ClientConnectionManager ccm = new ThreadSafeClientConnManager(
				params, schemeRegistry);
		DefaultHttpClient httpclient = new DefaultHttpClient(
				ccm, params);
		httpclient.getCookieStore().getCookies();
		
		return httpclient;
	}
	
	/**
	 * Access to global HttpClient.
	 * @return HttpClient
	 */
	public static HttpClient getGlobalHttpClient() {
		if (mHttpClient == null) {
			mHttpClient = createClient();
		}
		return mHttpClient;
	}

	/**
	 * Sets the timeout time..
	 * @param params
	 */
	private static void setTimeouts(HttpParams params) {
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 
				CONNECTION_TIMEOUT);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 
				SOCKET_TIMEOUT);
		params.setLongParameter(ConnManagerPNames.TIMEOUT, MCC_TIMEOUT);
	}
	
	/**
	 * Processing network abnormal situation.
	 * @param e
	 * @param warningMess
	 */
	private void handleException(Exception e, String warningMess) {
		if (null != mINetworkExcetpionHandler) {
			mINetworkExcetpionHandler.handleNetworkException(e, warningMess);
		}
	}
	
	/**
	 * Session timeout, after login again need to establish a connection.
	 */
	public static void resetConnect() {
		shutdownHttpConnection();
		mHttpClient = createClient();
	}
	
	/**
	 * End of all.
	 */
	public static void shutdownHttpConnection() {
		mHttpClient.getConnectionManager().shutdown();
	}
	
	/**
	 * Abort.
	 */
	public void abort() {
		abortPost();
		abortGet();
	}
	
	public void abortPost() {
		if (mHttpPost == null) {
			return;
		}
		mHttpPost.abort();
	}
	
	public void abortGet() {
		if (mHttpGet == null) {
			return;
		}
		mHttpGet.abort();
	}
	
	/**
	 * Parse the HttpResponse.
	 * @param res
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String parseHttpResponse(HttpResponse res) 
			throws ClientProtocolException, IOException {
		String result = null;
		if (res != null && res.getStatusLine().
				getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity entity = res.getEntity();
			result = EntityUtils.toString(entity, 
					Constants.DEFAULT_ENCODING);
		}
		return result;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////
	// GET
	///////////////////////////////////////////////////////////////////////////////////
	/**
	 * Get v4.0
	 * @param requestUrl
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse hreGet(String requestUrl) 
			throws ClientProtocolException, IOException {
		HttpResponse res = null;
		mHttpGet = new HttpGet(requestUrl);
		mHttpGet.setHeader("KOOBEE", "dido");
		try {
			res = mHttpClient.execute(mHttpGet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}
	
	public String strGet(String requestUrl) 
			throws ClientProtocolException, IOException {
		HttpResponse res = hreGet(requestUrl);
		return parseHttpResponse(res);
	}
	
	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////
	// POST
	///////////////////////////////////////////////////////////////////////////////////
	/**
	 * Post v4.0
	 * @param requestUrl
	 * @param jsonParams
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse hrePost(String requestUrl, String jsonParams)
			throws ClientProtocolException, IOException {
		HttpResponse res = null;
		
		mHttpPost = new HttpPost(requestUrl);
		mHttpPost.setHeader("Content-Type", "application/json");
		setTimeouts(mHttpPost.getParams());
		
		mHttpPost.setEntity(new StringEntity(jsonParams, Constants.DEFAULT_ENCODING));
		try {
			res = mHttpClient.execute(mHttpPost);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}
	
	public String strPost(String requestUrl, String jsonParams)
			throws ClientProtocolException, IOException {
		HttpResponse res = hrePost(requestUrl, jsonParams);
		return parseHttpResponse(res);
	}

	
	
	/**
	 * 模拟表单提交(参数)
	 * @param url
	 * @param paramsMap
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String strPostForm(String url, Map<String, Object> paramsMap)
			throws ClientProtocolException, IOException {
		HttpResponse res = hrePostForm(url, paramsMap);
		return parseHttpResponse(res);
	}
	
	public HttpResponse hrePostForm(String url, Map<String, Object> paramsMap) 
			throws ClientProtocolException, IOException {
		HttpResponse res = null;
		
		mHttpPost = new HttpPost(url);
		//添加参数
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		Set<Entry<String, Object>> entrySet = paramsMap.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue() + ""));
		}
		mHttpPost.setEntity(new UrlEncodedFormEntity(list, Constants.DEFAULT_ENCODING));
		try {
			res = mHttpClient.execute(mHttpPost);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}
	
	/**
	 * 模拟表单提交(参数+图片)
	 * @param url
	 * @param paramsMap
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String strPostFormFile(String url, Map<String, Object> paramsMap,
			Map<String, Object> mapFilePaths) throws 
			ClientProtocolException, IOException {
		HttpResponse res = hrePostFormFile(url, paramsMap, mapFilePaths);
		return parseHttpResponse(res);
	}
	@SuppressWarnings("unchecked")
	public HttpResponse hrePostFormFile(String url, 
			Map<String, Object> paramsMap,
			Map<String, Object> mapFilePaths) throws 
			ClientProtocolException, IOException {
		HttpResponse res = null;
		
		mHttpPost = new HttpPost(url);
		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
		//添加参数
		Set<Entry<String, Object>> entrySet = paramsMap.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			Log.d(TAG, "paramsMap的数据为:" + 
					FastJsonUtils.convertDataToJsonString(entry.getValue()));
			meBuilder.addTextBody(entry.getKey(), 
					FastJsonUtils.convertDataToJsonString(entry.getValue()));
		}
		//添加图片
		if (mapFilePaths != null) {
			Set<Entry<String, Object>> entrySetPath = mapFilePaths.entrySet();
			for (Entry<String, Object> entry : entrySetPath) {
				Object ob = entry.getValue();
				if (ob instanceof String) {
					String filePath = (String) ob;
					Log.d(TAG, "filePath的数据为:" + entry.getKey() + ":   " + filePath);
					File file = new File(filePath);
					if (!file.exists()) {
						continue;
					}
					meBuilder.addBinaryBody(entry.getKey(), file);
				} else if (ob instanceof List) {
					List<String> filePaths = (List<String>) ob;
					if (filePaths != null && filePaths.size() > 0) {
						for (String filePath : filePaths) {
							Log.d(TAG, "filePath的数据为:" + entry.getKey() + ":   " + filePath);
							File file = new File(filePath);
							if (!file.exists()) {
								continue;
							}
							meBuilder.addBinaryBody(entry.getKey(), file);
						}
					}
				} else {
					continue;
				}
			}
		}
		mHttpPost.setEntity(meBuilder.build());
		try {
			res = mHttpClient.execute(mHttpPost);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}
//	public String strPostFormFile(String url, Map<String, Object> paramsMap,
//			List<String> filePaths) throws 
//			ClientProtocolException, IOException {
//		HttpResponse res = hrePostFormFile(url, paramsMap, filePaths);
//		return parseHttpResponse(res);
//	}
//	public HttpResponse hrePostFormFile(String url, 
//			Map<String, Object> paramsMap,
//			List<String> filePaths) throws 
//			ClientProtocolException, IOException {
//		HttpResponse res = null;
//		
//		httpPost = new HttpPost(url);
//		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
//		//添加参数
//		Set<Entry<String, Object>> entrySet = paramsMap.entrySet();
//		for (Entry<String, Object> entry : entrySet) {
//			meBuilder.addTextBody(entry.getKey(), entry.getValue() + "");
//		}
//		//添加图片
//		if (filePaths != null && filePaths.size() > 0) {
//			for (String filePath : filePaths) {
//				File file = new File(filePath);
//				if (!file.exists()) {
//					continue;
//				}
//				meBuilder.addBinaryBody("images", file);
//			}
//		}
//		httpPost.setEntity(meBuilder.build());
//		try {
//			res = httpClient.execute(httpPost);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return res;
//	}
	
	

	/**
	 * 
	 * @param requestUrl
	 * @param parameter
	 * @return
	 */
	public HttpResponse doPost(String requestUrl, 
			List<NameValuePair> parameter) {
		return doPost(requestUrl, parameter, Constants.DEFAULT_ENCODING);
	}
	
	public HttpResponse doPost(String requestUrl, 
			List<NameValuePair> parameter, String encoding) {
		mHttpPost = new HttpPost(requestUrl);
		setTimeouts(mHttpPost.getParams());
		
		try {
			if (!NetworkUtils.isNetWorkActive()) {
				throw new NetworkException("网络异常", 
						NetworkException.ExceptionType.NetworkNotActivie);
			}
			
			String enc = Constants.UTF_8;
			if (encoding != null && encoding != "") {
				enc = encoding;
			}
			
			// 设置entity参数。
			mHttpEntity = new UrlEncodedFormEntity(parameter, enc);
			mHttpPost.setEntity(mHttpEntity);
			
			// 获取cookie.
			if (mCookieStore != null) {
				mHttpClient.setCookieStore(mCookieStore);
				List<Cookie> cookies = mCookieStore.getCookies();
				for (Cookie cookie : cookies) {
					mHttpPost.addHeader(cookie.getName(), cookie.getValue());
				}
			}
			
			// 执行post
			mHttpResponse = mHttpClient.execute(mHttpPost);
			
			// 创建Cookie
			if (mCookieStore == null) {
				mCookieStore =  mHttpClient.getCookieStore();
				
				List<Cookie> cookies = mCookieStore.getCookies();
				if (cookies != null) {
					CookieManager cookieManager = CookieManager.getInstance();
					
					for (int i = 0; i < cookies.size(); i++) {
						Cookie cookie = cookies.get(i);
						cookieManager.setCookie(cookie.getName(), 
								cookie.getValue());
					}
					CookieSyncManager.getInstance().sync();
				}
			}
			return mHttpResponse;
		} catch (IOException e) {
			handleException(e, null);
		}
		return mHttpResponse;
	}

}
