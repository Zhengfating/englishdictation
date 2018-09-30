package com.open_open.englishdictation;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by dellpc on 2017/6/10.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的Word数据
     */
    public static Word handleWord(String response, String titleId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject resultJson = new JSONObject(response);
                //获取翻译成功的结果
                JSONArray jsonArray = (JSONArray) resultJson.get("trans_result");
                JSONObject dstJson = (JSONObject) jsonArray.get(0);
                String text_en = dstJson.getString("src");
                String text_zh = dstJson.getString("dst");
                try {
                    //utf-8译码
                    text_zh = URLDecoder.decode(text_zh, "utf-8");
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                Word word = new Word();
                word.setWord_en(text_en);
                word.setWord_ch(text_zh);
                word.setTitleId(titleId);
                word.save();
                return word;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
