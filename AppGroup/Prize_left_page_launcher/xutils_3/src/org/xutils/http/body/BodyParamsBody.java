package org.xutils.http.body;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Author: wyouflf
 * Time: 2014/05/30
 */
public class BodyParamsBody implements RequestBody {

    private byte[] content;
    private String charset;

    public BodyParamsBody(Map<String, String> params, String charset) throws IOException {
        StringBuilder contentSb = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> kv : params.entrySet()) {
                String name = kv.getKey();
                String value = kv.getValue();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                    if (contentSb.length() > 0) {
                        contentSb.append("&");
                    }
                    contentSb.append(Uri.encode(name, charset))
                            .append("=")
                            .append(Uri.encode(value, charset));
                }
            }
        }

        this.content = contentSb.toString().getBytes(charset);
        this.charset = charset;
    }

    @Override
    public long getContentLength() {
        return content.length;
    }

    @Override
    public void setContentType(String contentType) {
    }

    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded;charset=" + charset;
    }

    @Override
    public void writeTo(OutputStream sink) throws IOException {
        sink.write(this.content);
        sink.flush();
    }
}
