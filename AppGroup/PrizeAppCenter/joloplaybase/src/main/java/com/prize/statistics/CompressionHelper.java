package com.prize.statistics;

import com.prize.app.util.JLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/20.10:50
 * @描述
 */

public class CompressionHelper {
    private final static String charsetEncoder = "ISO-8859-1";


    /***
     * 压缩
     *
     * @param str
     * @return
     * @throws IOException
     */
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
            if(JLog.isDebug){
                String result= out.toString(charsetEncoder);
                JLog.i("CompressionHelper","result="+result.length());
                return result;
            }else{
                return  out.toString(charsetEncoder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解压缩
    public static String uncompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str
                .getBytes(charsetEncoder));
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toString();
    }

}
