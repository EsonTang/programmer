package com.android.launcher3.notify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    /**
     * 从流中读取所有字节流并关闭流
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = -1;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {}
            try {
                inputStream.close();
            } catch (IOException e) {}
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void copyStream(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        try {
            while((read = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, read);
            }
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {}
            try {
                inputStream.close();
            } catch (IOException e) {}
        }
    }
}
