
package com.prize.qihoo.cleandroid.sdk;

import android.os.Bundle;

public class ResultSummaryInfo {

    /** 总大小 */
    public long size;

    /** 总数量 */
    public long count;

    /** 已选择大小 */
    public long selectedSize;

    /** 已选择数量 */
    public long selectedCount;

    /** 扩展字段 */
    public Bundle bundle;

    /** 缓存路径数量 */
    public int argInt1;

    /** 敏感项个数 */
    public int cautiousClearCount;

    /** 选中的数量 */
    public long argLong1;

    /** QQ清理项大小 */
    public long qqSize;

    /** QQ清理项已选择大小  */
    public long qqSelectedSize;

    /** 微信清理项大小 */
    public long weixinSize;

    /** 微信清理项已选择大小  */
    public long weixinSelectedSize;

    @Override
    public String toString() {
        if (SDKEnv.DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("ResultSummaryInfo [size=");
            builder.append(TrashClearUtils.getHumanReadableSizeMore(size));
            builder.append(", count=");
            builder.append(count);
            builder.append(", selectedSize=");
            builder.append(TrashClearUtils.getHumanReadableSizeMore(selectedSize));
            builder.append(", selectedCount=");
            builder.append(selectedCount);
            builder.append(", bundle=");
            builder.append(bundle);
            builder.append(", argInt1=");
            builder.append(argInt1);
            builder.append(", cautiousClearCount=");
            builder.append(cautiousClearCount);
            builder.append(", argLong1=");
            builder.append(argLong1);
            builder.append(", qqSize=");
            builder.append(qqSize);
            builder.append(", qqSelectedSize=");
            builder.append(qqSelectedSize);
            builder.append(", weixinSize=");
            builder.append(weixinSize);
            builder.append(", weixinSelectedSize=");
            builder.append(weixinSelectedSize);
            builder.append("]");
            return builder.toString();
        }
        return super.toString();
    }

}
