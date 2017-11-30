package com.mygdx.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bushetianzhen on 2017/11/21.
 */

public class JsonUtils {

    static String json = "{\"id\":\"changsha_50252\",\"pid\":\"changsha_50231\",\"provider\":\"JIECHENG\",\"partner\":\"GUANGSHI\",\"resource_status\":1,\"name\":\"阿U之神奇萝卜2 21\",\"serial_name\":\"阿U之神奇萝卜2\",\"alias_name\":\"阿U之神奇萝卜2\",\"type\":\"动漫\",\"source_type\":\"changsha\",\"category\":\"亲子/\",\"tag\":\"\",\"duration\":\"660\",\"season\":\"\",\"total_episodes\":1,\"episode\":21,\"director\":\"曹小卉\",\"actor\":\"\",\"region\":\"中国大陆\",\"release_date\":\"2015\",\"update_time\":\"20171121114024\",\"cost\":\"\",\"hot\":\"\",\"weight\":50,\"language\":\"\",\"definition\":\"\",\"introduction\":\"该动画讲述主角调皮捣蛋，古灵精怪的故事。   阿优凡事都第一时间做出反应，解决事情都会想出稀奇古怪的招数。他也正义善良，精力旺盛，思想活跃，对事物充满好奇心，是个有爱心的孩子。同时，阿优也爱张扬，不谦虚，喜欢得瑟、出风头，却又经常好心办坏事，经常出糗......\",\"poster_url\":\"http://10.0.11.10:80/poster/15856.jpg\",\"thumb_url\":\"http://10.0.11.10:80/poster/15856.jpg\",\"token\":\"http://10.0.12.12/otv/guangshi/5/22/83/00000130101/index.m3u8\"}";

    public static String testJson() {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
