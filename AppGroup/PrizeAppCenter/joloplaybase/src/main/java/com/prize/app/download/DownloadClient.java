package com.prize.app.download;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.safe.XXTEAUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class DownloadClient {
    private HttpURLConnection conn;
    private String url;
    //    private String agency;
    private static final int CONNECTION_TIME_OUT = 25 * 1000; // 25 secs
    private static final int READING_TIME_OUT = 50 * 1000; // 50 secs
    private String mPkg;
    private final String TAG = "DownloadClient";

    private ClientInfo mClientInfo = ClientInfo.getInstance();

    public DownloadClient(String dl_url, String pkg) {
        this.url = dl_url.replace(" ", "%20");
        mPkg = pkg;
    }

//    public InputStream getInputStream() throws IOException,
//            SocketTimeoutException {
//        return getInputStream(0);
//    }

    /**
     * 从网络读取数据
     *
     * @param sizePos ： 断点续传时的起始位置
     * @return InputStream
     * @throws MalformedURLException  MalformedURLException
     * @throws SocketTimeoutException SocketTimeoutException
     * @throws IOException            IOException
     */
    InputStream getInputStream(long sizePos, String pageInfo)
            throws MalformedURLException, SocketTimeoutException, IOException {

        URL aURL = new URL(url);
        if (JLog.isDebug) {
            JLog.i(TAG, "---->getInputStreamurl：" + url + "--pageInfo=" + pageInfo);
        }
        conn = (HttpURLConnection) aURL.openConnection();
        conn.setConnectTimeout(CONNECTION_TIME_OUT);
        conn.setReadTimeout(READING_TIME_OUT);
        //想对中间的Location做些处理则可以通过HttpURLConnection实现。 // 必须设置false
        if (isNeedGetTransfer(url, pageInfo)) {
            conn.setInstanceFollowRedirects(false);
        }
        mClientInfo.setUserId(CommonUtils.queryUserId());
        mClientInfo.setClientStartTime(System.currentTimeMillis());
        mClientInfo.setNetStatus(ClientInfo.networkType);
        mClientInfo.ua=System.getProperty("http.agent");
        if (JLog.isDebug) {
            JLog.i(TAG, "---->断点续传mClientInfo.ua：" + mClientInfo.ua);
        }
        if (!TextUtils.isEmpty(pageInfo)) {
            mClientInfo.pageInfo = pageInfo;
        }
        String headParams = new Gson().toJson(mClientInfo);

        headParams = XXTEAUtil.getParamsEncypt(headParams);
        if (!TextUtils.isEmpty(headParams)) {
            conn.setRequestProperty("params", headParams);
        }
        conn.setRequestProperty("User-Agent", "koobee");
        if (0 != sizePos) {
            conn.setRequestProperty("Range", "bytes=" + sizePos + "-");
            JLog.i(TAG, "---->断点续传sizePos：" + sizePos);
        }
        conn.connect();
        if (JLog.isDebug) {
            JLog.i(TAG, "---->conn.getResponseCode()：" + conn.getResponseCode());
            String header = conn.getHeaderField("Content-Type");
            JLog.i(TAG, "---->header：" + header);
        }
        String locationUrl;
        if (isNeedGetTransfer(url, pageInfo)) {
            if (conn.getResponseCode() == 302 && conn.getHeaderField("Content-Type") == null) {
                String agency = conn.getHeaderField("agency");
                if (TextUtils.isEmpty(agency))
                    agency = "default";
                StringBuilder sb = new StringBuilder(pageInfo);
                sb.insert(sb.length() - 1, ",\"agency\":\"" + agency + "\"");
                DownloadTaskMgr.getInstance().updatePageInfo(mPkg, sb.toString());
                if (JLog.isDebug) {
                    JLog.i(TAG, "---->agency:" + agency + "--sb=" + sb);
                }
                locationUrl = conn.getHeaderField("Location");
                if (JLog.isDebug) {
                    JLog.i(TAG, "---->agency:" + agency + "--locationUrl=" + locationUrl);
                }
                conn.disconnect();
            } else {
                conn.disconnect();
                return null;
            }
            URL bURL = new URL(url);
            if (!TextUtils.isEmpty(locationUrl)) {
                bURL = new URL(locationUrl);
            }
            if (JLog.isDebug) {
                JLog.i(TAG, "---->getInputStreamurl：" + url + "-locationUrl=" + locationUrl);
            }
            conn = (HttpURLConnection) bURL.openConnection();
            conn.setConnectTimeout(CONNECTION_TIME_OUT);
            conn.setReadTimeout(READING_TIME_OUT);
            //想对中间的Location做些处理则可以通过HttpURLConnection实现。 // 必须设置false
            conn.setInstanceFollowRedirects(true);
            conn.connect();
        }
        InputStream is = conn.getInputStream();
        URL redirectUrl = conn.getURL();
        String header = conn.getHeaderField("Content-Type");
        if (JLog.isDebug) {
            JLog.i(TAG, "---->header：" + header + "---redirectUrl=" + redirectUrl);
        }
        // 无效的网络会返回流的大小，造成下载任务的错误
        if (!Constants.QES_ACCEPT_CONTENT_TYPE.contains(header)
                || Constants.QES_UNACCEPT_CONTENT_TYPE.contains(header)) {
            conn.disconnect();
            return null;
        }
        if (redirectUrl != null) {
            String sUrl = redirectUrl.toString();
            if (!url.equals(sUrl) && !TextUtils.isEmpty(sUrl)) {
                GameDAO.getInstance().updateDownUrl(sUrl, mPkg);
            }
        }
        return is;
    }

    int getContentLength() {
        if (conn != null) {
            return conn.getContentLength();
        }
        return 0;
    }

    /**
     * 是否需要获取302跳转状态
     *
     * @param url url
     * @return boolean
     */
    private boolean isNeedGetTransfer(String url, String pageInfo) {
        return (!TextUtils.isEmpty(pageInfo) && !pageInfo.contains("agency") && url.contains("/appstore/appinfo/"));
//        return (url.contains("type=up") || url.contains("type=down"));
    }

    public void close() {
        disconnect();
        conn = null;
    }

    private void disconnect() {
        if (conn != null)
            conn.disconnect();
    }

}
