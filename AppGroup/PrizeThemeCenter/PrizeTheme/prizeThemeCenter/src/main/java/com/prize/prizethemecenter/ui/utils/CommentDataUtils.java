package com.prize.prizethemecenter.ui.utils;

import android.content.Context;

import com.prize.app.beans.ClientInfo;
import com.prize.cloud.bean.Person;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.FontSubmitRequest;
import com.prize.prizethemecenter.request.ThemeSubmitRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

/**
 * Created by liukun on 2016/11/22.
 */
public class CommentDataUtils {

    /**
     * 根据主题ID添加评论
     */
//add by zhouerlong  comment
    public static void setCommentByThemeId(int themeId, String content, String user_name, String icon,Runnable r){
        if (content!=null&&!content.equals("")) {
            ThemeSubmitRequest themeSubmitRequest = new ThemeSubmitRequest();
            themeSubmitRequest.themeId = themeId;
            themeSubmitRequest.content = content;
            themeSubmitRequest.user_name = user_name;
            themeSubmitRequest.icon = icon;
            themeSubmitRequest.user_id = CommonUtils.queryUserId();
            themeSubmitRequest.phone_model = ClientInfo.getInstance().getModel();
            setCommentData(themeSubmitRequest,r);
        }else {
            ToastUtils.showToast(R.string.comment_empty_hint);
        }
    }
//add by zhouerlong  comment

    /**
     * 根据字体ID添加评论
     */
//add by zhouerlong  comment
    public static void setCommentByFontId(int fontId, String content, String user_name, String icon,Runnable r){
        if (content!=null&&!content.equals("")) {
            FontSubmitRequest fontSubmitRequest = new FontSubmitRequest();
            fontSubmitRequest.fontId = fontId;
            fontSubmitRequest.content = content;
            fontSubmitRequest.user_name = user_name;
            fontSubmitRequest.icon = icon;
            fontSubmitRequest.user_id = CommonUtils.queryUserId();
            fontSubmitRequest.phone_model = ClientInfo.getInstance().getModel();
            setCommentData(fontSubmitRequest,r);
        }else {
            ToastUtils.showToast(R.string.comment_empty_hint);
        }
    }
//add by zhouerlong  comment

    /**
     * 提交评论
     * @param request
     */
//add by zhouerlong  comment
    protected static void setCommentData(BaseRequest request, final Runnable r) {
        x.http().post(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject object=new JSONObject(result);
                    if (object.getInt("code")==0){
                        if(r!=null) {
                            r.run();
                        }
                        ToastUtils.showToast(R.string.comment_success);
                    }else {
                        ToastUtils.showToast(R.string.comment_failed);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastUtils.showToast(R.string.comment_failed);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

//add by zhouerlong  comment
    /**
     * 查询账号信息
     */
    private static Person queryUser(Context context) {

        return UIUtils.queryUserPerson(context);

    }

    public static String getUserName(Context context){
        Person person = queryUser(context);
        if (person!=null&&person.getRealName()!="")
            return person.getRealName();
        else{
            return String.valueOf(R.string.anonymity_user);
        }
    }

    public static String getUserIcon(Context context){
        Person person = queryUser(context);
        if (person!=null&&person.getAvatar()!=""){
            return person.getAvatar();
        }
        else{
            return String.valueOf(R.drawable.person_img);
        }

    }
}
