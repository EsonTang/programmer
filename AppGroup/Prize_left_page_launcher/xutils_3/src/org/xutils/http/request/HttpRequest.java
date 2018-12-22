package org.xutils.http.request;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import org.xutils.cache.DiskCacheEntity;
import org.xutils.cache.LruDiskCache;
import org.xutils.common.util.IOUtil;
import org.xutils.common.util.LogUtil;
import org.xutils.ex.HttpException;
import org.xutils.http.HttpMethod;
import org.xutils.http.ImageReqParams;
import org.xutils.http.RequestParams;
import org.xutils.http.body.ProgressBody;
import org.xutils.http.body.RequestBody;
import org.xutils.http.cookie.DbCookieStore;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by wyouflf on 15/7/23.
 * Uri请求发送和数据接收
 */
public class HttpRequest extends UriRequest {

    private String cacheKey = null;
    private boolean isLoading = false;
    private InputStream inputStream = null;
    private HttpURLConnection connection = null;

    // cookie manager
    private static final CookieManager COOKIE_MANAGER =
            new CookieManager(DbCookieStore.INSTANCE, CookiePolicy.ACCEPT_ALL);

    /*package*/ HttpRequest(RequestParams params, Type loadType) throws Throwable {
        super(params, loadType);
    }

    // build query
    @Override
    protected String buildQueryUrl(RequestParams params) {
        String uri = params.getUri();
        StringBuilder queryBuilder = new StringBuilder(uri);
        if (!uri.contains("?")) {
            queryBuilder.append("?");
        } else if (!uri.endsWith("?")) {
            queryBuilder.append("&");
        }
        HashMap<String, String> queryParams = params.getQueryStringParams();
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                    queryBuilder.append(Uri.encode(name)).append("=").append(Uri.encode(value)).append("&");
                }
            }
            if (queryBuilder.charAt(queryBuilder.length() - 1) == '&') {
                queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            }
        }

        if (queryBuilder.charAt(queryBuilder.length() - 1) == '?') {
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        }
        return queryBuilder.toString();
    }

    @Override
    public String getRequestUri() {
        String result = queryUrl;
        if (connection != null) {
            URL url = connection.getURL();
            if (url != null) {
                result = url.toString();
            }
        }
        return result;
    }

    /**
     * invoke via Loader
     *
     * @throws IOException
     */
    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void sendRequest() throws IOException {
        isLoading = false;

        URL url = new URL(queryUrl);
        { // init connection
            Proxy proxy = params.getProxy();
            if (proxy != null) {
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setInstanceFollowRedirects(true);
            connection.setReadTimeout(params.getConnectTimeout());
            connection.setConnectTimeout(params.getConnectTimeout());
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(params.getSslSocketFactory());
            }
        }

        {// add headers
        	addHeader(url);
        }

        HttpMethod method = params.getMethod(); //  added by fanjunchen
        { // write body
            // HttpMethod method = params.getMethod();
            connection.setRequestMethod(method.toString());
            if (HttpMethod.permitsRequestBody(method)) {
                RequestBody body = params.getRequestBody();
                if (body != null) {
                    if (body instanceof ProgressBody) {
                        ((ProgressBody) body).setProgressHandler(progressHandler);
                    }
                    String contentType = body.getContentType();
                    if (!TextUtils.isEmpty(contentType)) {
                        connection.setRequestProperty("Content-Type", contentType);
                    }
                    long contentLength = body.getContentLength();
                    if (contentLength < 0) {
                        connection.setChunkedStreamingMode(256 * 1024);
                    } else {
                        if (contentLength < Integer.MAX_VALUE) {
                            connection.setFixedLengthStreamingMode((int) contentLength);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            connection.setFixedLengthStreamingMode(contentLength);
                        } else {
                            connection.setChunkedStreamingMode(256 * 1024);
                        }
                    }
                    connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
                    connection.setDoOutput(true);
                    body.writeTo(connection.getOutputStream());
                }
            }
        }

        LogUtil.d(queryUrl);
        int code = connection.getResponseCode();
        if (method == HttpMethod.GET) {
        	int redirectCount = 0;
    		while (code / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
    			addHeader(createConnection(connection.getHeaderField("Location")));
    			redirectCount++;
    			code = connection.getResponseCode();
    		}
        }
        if (code >= 300) {
            HttpException httpException = new HttpException(code, connection.getResponseMessage());
            try {
                httpException.setResult(IOUtil.readStr(connection.getInputStream(), params.getCharset()));
            } catch (Throwable ignored) {
            }
            throw httpException;
        }

        { // save cookies
            try {
                Map<String, List<String>> headers = connection.getHeaderFields();
                if (headers != null) {
                    COOKIE_MANAGER.put(url.toURI(), headers);
                }
            } catch (Throwable ex) {
                LogUtil.e(ex.getMessage(), ex);
            }
        }

        isLoading = true;
    }
    /**added by fanjunchen*/
    
    private void addHeader(URL url) {
    	String a = url.getPath();
    	if ((params instanceof ImageReqParams) || (a != null && (a.toLowerCase().endsWith(".png")
    			|| a.toLowerCase().endsWith(".jpg") || a.toLowerCase().endsWith(".gif"))))
    		return ;
    	try {
            Map<String, List<String>> singleMap =
                    COOKIE_MANAGER.get(url.toURI(), new HashMap<String, List<String>>(0));
            List<String> cookies = singleMap.get("Cookie");
            if (cookies != null) {
                connection.setRequestProperty("Cookie", TextUtils.join(";", cookies));
            }
        } catch (Throwable ex) {
            LogUtil.e(ex.getMessage(), ex);
        }

        HashMap<String, String> headers = params.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                    connection.setRequestProperty(name, value);
                }
            }
        }
    }
    
    protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    private static final int MAX_REDIRECT_COUNT = 4;
    
    private URL createConnection(String url) throws IOException {
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		URL mURL = new URL(encodedUrl);
		// init connection
		Proxy proxy = params.getProxy();
        if (proxy != null) {
            connection = (HttpURLConnection) mURL.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) mURL.openConnection();
        }
        connection.setInstanceFollowRedirects(true);
        connection.setReadTimeout(params.getConnectTimeout());
        connection.setConnectTimeout(params.getConnectTimeout());
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(params.getSslSocketFactory());
        }
		return mURL;
	}
    /**added by fanjunchen*/

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public String getCacheKey() {
        if (cacheKey == null) {

            cacheKey = params.getCacheKey();

            if (TextUtils.isEmpty(cacheKey)) {
                cacheKey = queryUrl;
            }
        }
        return cacheKey;
    }

    @Override
    public Object loadResult() throws Throwable {
        isLoading = true;
        return super.loadResult();
    }

    /**
     * 尝试从缓存获取结果, 并为请求头加入缓存控制参数.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object loadResultFromCache() throws Throwable {
        isLoading = true;
        DiskCacheEntity cacheEntity = LruDiskCache.getDiskCache(params.getCacheDirName()).get(this.getCacheKey());

        if (cacheEntity != null) {
            if (HttpMethod.permitsCache(params.getMethod())) {
                Date lastModified = cacheEntity.getLastModify();
                if (lastModified.getTime() > 0) {
                    params.addHeader("If-Modified-Since", toGMTString(lastModified));
                }
                String eTag = cacheEntity.getEtag();
                if (!TextUtils.isEmpty(eTag)) {
                    params.addHeader("If-None-Match", eTag);
                }
            }
            return loader.loadFromCache(cacheEntity);
        } else {
            return null;
        }
    }

    @Override
    public void clearCacheHeader() {
        params.addHeader("If-Modified-Since", null);
        params.addHeader("If-None-Match", null);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (connection != null && inputStream == null && shouldBeProcessed(connection)) {
        	connection.getContentLength();
            inputStream = connection.getInputStream();
        }
        return inputStream;
    }
    
    protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
		return conn.getResponseCode() == 200;
	}

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            IOUtil.closeQuietly(inputStream);
            inputStream = null;
        }
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    @Override
    public long getContentLength() {
        long result = 0;
        if (connection != null) {
            try {
                result = connection.getContentLength();
            } catch (Throwable ex) {
                LogUtil.e(ex.getMessage(), ex);
            }
            if (result < 1) {
                try {
                    result = this.getInputStream().available();
                } catch (Throwable ignored) {
                }
            }
        } else {
            try {
                result = this.getInputStream().available();
            } catch (Throwable ignored) {
            }
        }
        return result;
    }

    @Override
    public int getResponseCode() throws IOException {
        if (connection != null) {
            return connection.getResponseCode();
        } else {
            if (this.getInputStream() != null) {
                return 200;
            } else {
                return 404;
            }
        }
    }

    @Override
    public long getExpiration() {
        if (connection == null) return -1;
        long result = connection.getExpiration();
        if (result <= 0) {
            result = Long.MAX_VALUE;
        }
        else {
        	result += 5 * 24 * 60 * 60 * 1000;
        }
        return result;
    }

    @Override
    public long getLastModified() {
        return getHeaderFieldDate("Last-Modified", System.currentTimeMillis());
    }

    @Override
    public String getETag() {
        if (connection == null) return null;
        return connection.getHeaderField("ETag");
    }

    @Override
    public String getResponseHeader(String name) {
        if (connection == null) return null;
        return connection.getHeaderField(name);
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        if (connection == null) return null;
        return connection.getHeaderFields();
    }

    @Override
    public long getHeaderFieldDate(String name, long defaultValue) {
        if (connection == null) return defaultValue;
        return connection.getHeaderFieldDate(name, defaultValue);
    }

    private static String toGMTString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, dd MMM y HH:mm:ss 'GMT'", Locale.US);
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(gmtZone);
        GregorianCalendar gc = new GregorianCalendar(gmtZone);
        gc.setTimeInMillis(date.getTime());
        return sdf.format(date);
    }
}
