package com.me.microblog.core;

import android.content.Context;
import android.text.TextUtils;
import com.me.microblog.R;
import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.*;
import com.me.microblog.http.RateLimitStatus;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.me.microblog.util.PinYin;
import com.me.microblog.util.WeiboLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 没有继承weiboresponse亏大了.多写了很多无用代码.日后再重构.
 */
public class WeiboParser {

    private static Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();

    public WeiboParser() {
    }

    protected static boolean parseBoolean(String s, JSONObject jsonobject) throws WeiboException {
        boolean flag = false;
        String s1 = null;
        try {
            s1 = jsonobject.getString(s);
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + s, e);
        }
        if (s1 == null) {
            return false;
        } else {
            flag = "null".equals(s1);
            if (!flag) {
                flag = Boolean.valueOf(s1).booleanValue();
                return flag;
            } else {
                return false;
            }
        }
    }

    /**
     * 构造一个 JSONObject，因为有可能NULLPointException，所以不直接捕获JSONException
     *
     * @param js
     * @return
     * @throws WeiboException
     */
    public static JSONObject contructJSONObject(String js) throws WeiboException {
        JSONObject jo=null;
        try {
            jo=new JSONObject(js);
        } catch (Exception e) {
            throw new WeiboException(e.getMessage()+":"+js, e);
        }
        return jo;
    }

    public static JSONArray contructJSONArray(String js) throws WeiboException {
        JSONArray jsonarray = null;
        try {
            jsonarray = new JSONArray(js);
        } catch (Exception e) {
            throw new WeiboException(e.getMessage() + ":" + js, e);
        }
        return jsonarray;
    }

    public static Comment parseComment(JSONObject jsonobject) throws WeiboException {
        if(null==jsonobject){
            return null;
        }
        Comment comment = new Comment();
        try {
            try {
                Date date = parseDate(jsonobject.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
                comment.createdAt = date;
            } catch (WeiboException e) {
                e.printStackTrace();
                comment.createdAt=new Date();
            }

            comment.id = jsonobject.getLong("id");
            comment.text = jsonobject.getString("text");
            if (jsonobject.has("source")) {
                comment.source = jsonobject.getString("source");
            }
            if (!jsonobject.isNull("user")) {
                comment.user = parseUser(jsonobject.getJSONObject("user"));
            }
            if (jsonobject.has("status")) {
                comment.status = parseStatus(jsonobject.getJSONObject("status"));
            }
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return comment;
    }

    public static Comment parseComment(String js) throws WeiboException {
        return parseComment(contructJSONObject(js));
    }

    /**
     * 解析微博内容
     *
     * @param jsonarray 需要解析的微博
     * @return
     * @throws WeiboException
     */
    private static ArrayList<Comment> parseComments(JSONArray jsonarray) throws WeiboException {
        ArrayList<Comment> arraylist=new ArrayList<Comment>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                Comment comment=null;
                comment=WeiboParser.parseComment(jsonarray.getJSONObject(i));
                arraylist.add(comment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析评论，oauth1数据
     * @param ja
     * @return
     * @throws WeiboException
     */
    public static ArrayList<Comment> parseComments(String ja) throws WeiboException {

        JSONArray jsonarray = contructJSONArray(ja);
        return parseComments(jsonarray);
    }

    /**
     * 解析评论，oauth2数据
     * @param ja
     * @return
     * @throws WeiboException
     */
    public static SStatusData<Comment> parseComments2(String js) throws WeiboException {
        SStatusData<Comment> sStatusData=new SStatusData<Comment>();
        JSONObject jo=contructJSONObject(js);

        if(null==jo){
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("comments");
            if (null!=jsonarray) {
                ArrayList<Comment> arraylist=parseComments(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(jo.has("hasvisible")){
                sStatusData.hasvisible=jo.getBoolean("hasvisible");
            }

            if(jo.has("previous_cursor")){
                sStatusData.previous_cursor=jo.getInt("previous_cursor");
            }

            if(jo.has("next_cursor")){
                sStatusData.next_cursor=jo.getInt("next_cursor");
            }

            if(jo.has("total_number")){
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + jo, e);
        }
        return sStatusData;
    }

    public static Count parseCount(JSONObject jsonobject) throws WeiboException {
        Count count = new Count();
        try {
            count.id = jsonobject.getLong("id");

            count.comments = jsonobject.getInt("comments");
            if (jsonobject.has("rt")) {
                count.rt=jsonobject.getInt("rt");
            } else if (jsonobject.has("reposts")) {
                count.rt=jsonobject.getInt("reposts");
            }
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return count;
    }

    /**
     * 获取转发数
     *
     * @param jsonArrayString 需要解析的json数组
     * @return 转发数列表.
     * @throws WeiboException
     */
    public static ArrayList<Count> parseCounts(String jsonArrayString) throws WeiboException {
        ArrayList<Count> arraylist = new ArrayList<Count>();

        JSONArray jsonarray = contructJSONArray(jsonArrayString);
        int i = 0;
        try {
            int len = jsonarray.length();
            Count count = null;
            for (; i < len; i++) {
                count = WeiboParser.parseCount(jsonarray.getJSONObject(i));
                arraylist.add(count);
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + jsonArrayString, e);
        }
        return arraylist;
    }

    protected static Date parseDate(String s, String s1) throws WeiboException {
        SimpleDateFormat simpledateformat;
        simpledateformat = formatMap.get(s1);
        if (simpledateformat == null) {
            Locale obj = Locale.ENGLISH;
            simpledateformat = new SimpleDateFormat(s1, obj);
            TimeZone timeZone = TimeZone.getTimeZone("GMT");
            simpledateformat.setTimeZone(timeZone);
            formatMap.put(s1, simpledateformat);
        }
        Date date = null;
        try {
            date = simpledateformat.parse(s);
        } catch (java.text.ParseException ex) {
            //throw new WeiboException("Unexcepted format (" + s + ")");
        }
        return date;
    }

    public static Date parseDate(String s) throws WeiboException {
        String format = "EEE MMM dd HH:mm:ss z yyyy";
        return parseDate(s, format);
    }

    public static DirectMessage parseDirectMessage(JSONObject jsonobject) throws WeiboException {
        DirectMessage directmessage = new DirectMessage();
        try {
            Date date = parseDate(jsonobject.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
            directmessage.createdAt = date;
            directmessage.id = jsonobject.getLong("id");
            directmessage.idstr = jsonobject.getString("idstr");

            directmessage.text = jsonobject.getString("text");
            directmessage.senderId = jsonobject.getLong("sender_id");
            directmessage.source = jsonobject.getString("source");

            directmessage.recipientId = jsonobject.getLong("recipient_id");

            directmessage.senderScreenName = jsonobject.getString("sender_screen_name");
            directmessage.recipientScreenName = jsonobject.getString("recipient_screen_name");
            directmessage.sender = parseUser(jsonobject.getJSONObject("sender"));
            directmessage.recipient = parseUser(jsonobject.getJSONObject("recipient"));
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return directmessage;
    }

    /**
     * 解析私信内容
     *
     * @param jsonarray 需要解析的私信
     * @return
     * @throws WeiboException
     */
    private static ArrayList<DirectMessage> parseDirectMessage(JSONArray jsonarray) throws WeiboException {
        ArrayList<DirectMessage> arraylist=new ArrayList<DirectMessage>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                DirectMessage directMessage=null;
                directMessage=WeiboParser.parseDirectMessage(jsonarray.getJSONObject(i));
                arraylist.add(directMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析私信，
     *
     * @param js
     * @return
     * @throws WeiboException
     */
    public static DirectMessage parseDirectMessage(String js) throws WeiboException {
        DirectMessage directMessage=null;
        JSONObject jo=contructJSONObject(js);

        if (null==jo) {
            return directMessage;
        }

        directMessage = parseDirectMessage(jo);

        return directMessage;
    }

    /**
     * 解析私信，
     *
     * @param ja
     * @return
     * @throws WeiboException
     */
    public static SStatusData<DirectMessage> parseDirectMessages(String ja) throws WeiboException {
        SStatusData<DirectMessage> sStatusData=new SStatusData<DirectMessage>();
        JSONObject jo=contructJSONObject(ja);

        if (null==jo) {
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("direct_messages");
            if (null!=jsonarray) {
                ArrayList<DirectMessage> arraylist=parseDirectMessage(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (jo.has("total_number")) {
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage()+":"+jo, e);
        }
        return sStatusData;
    }

    public static RateLimitStatus parseRateLimitStatus(JSONObject jsonobject) throws WeiboException {
        RateLimitStatus ratelimitstatus = new RateLimitStatus();
        try {
            ratelimitstatus.hourlyLimit = jsonobject.getInt("hourly_limit");
            ratelimitstatus.resetTimeInSeconds = jsonobject.getInt("reset_time_in_seconds");
            ratelimitstatus.remainingHits = jsonobject.getInt("remaining_hits");
            Date date = parseDate(jsonobject.getString("reset_time"), "EEE MMM dd HH:mm:ss z yyyy");
            ratelimitstatus.resetTime = date;
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return ratelimitstatus;
    }

    public static RateLimitStatus parseRateLimitStatus(String js) throws WeiboException {
        return parseRateLimitStatus(contructJSONObject(js));
    }

    public static Relationship parseRelationship(JSONObject jsonobject) throws WeiboException {
        Relationship relationship = new Relationship();
        try {
            JSONObject jsonobject1 = jsonobject.getJSONObject("source");
            JSONObject jsonobject2 = jsonobject.getJSONObject("target");
            RelationInfo sourceInfo = relationship.source;
            sourceInfo.id = jsonobject1.getLong("id");
            sourceInfo.screenName = jsonobject1.getString("screen_name");
            sourceInfo.following = parseBoolean("following", jsonobject1);
            sourceInfo.followedBy = parseBoolean("followed_by", jsonobject1);
            sourceInfo.notificationsEnabled = parseBoolean("notifications_enabled", jsonobject1);

            RelationInfo targetInfo = relationship.target;
            targetInfo.id = jsonobject2.getLong("id");
            targetInfo.screenName = jsonobject2.getString("screen_name");
            targetInfo.following = parseBoolean("following", jsonobject2);
            targetInfo.followedBy = parseBoolean("followed_by", jsonobject2);
            targetInfo.notificationsEnabled = parseBoolean("notifications_enabled", jsonobject2);
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return relationship;
    }

    public static Relationship parseRelationship(String js) throws WeiboException {
        return parseRelationship(contructJSONObject(js));
    }

    /**
     * 解析单个微博，相对oauth1增加了不少内容。reposts_count ，comments_count ，mid ，geo，annotations
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    public static Status parseStatus(JSONObject jsonobject) throws WeiboException {
        Status status = new Status();
        try {
            if(jsonobject.has("status")){   //对于精选微博数据是放在status里面的
                jsonobject = jsonobject.getJSONObject("status");
            }
            Date date = parseDate(jsonobject.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
            status.createdAt = date;
            status.id = jsonobject.getLong("id");
            status.text = jsonobject.getString("text");
            status.source = jsonobject.getString("source");
            status.favorited = parseBoolean("favorited", jsonobject);
            status.truncated = parseBoolean("truncated", jsonobject);
            status.inReplyToStatusId = jsonobject.getString("in_reply_to_status_id");
            status.inReplyToUserId = jsonobject.getString("in_reply_to_user_id");
            status.inReplyToScreenName = jsonobject.getString("in_reply_to_screen_name");
            if (jsonobject.has("thumbnail_pic")) {
                status.thumbnailPic = jsonobject.getString("thumbnail_pic");
            }
            if (jsonobject.has("bmiddle_pic")) {
                status.bmiddlePic = jsonobject.getString("bmiddle_pic");
            }
            if (jsonobject.has("original_pic")) {
                status.originalPic = jsonobject.getString("original_pic");
            }

            if(jsonobject.has("reposts_count")){
                status.r_num=jsonobject.getInt("reposts_count");
            }

            if(jsonobject.has("comments_count")){
                status.c_num=jsonobject.getInt("comments_count");
            }

            if(jsonobject.has("mid")){
                status.mid=jsonobject.getString("mid");
            }

            if (jsonobject.has("user")) {
                User user = parseUser(jsonobject.getJSONObject("user"));
                status.user = user;
            }

            if (jsonobject.has("geo")&&!"null".equals(jsonobject.getString("geo"))) {
                parseGeo(jsonobject, status);
            }

            if(jsonobject.has("annotations")&&!"null".equals(jsonobject.getString("annotations"))){
                parseSAnnotation(jsonobject, status);
            }

            if(jsonobject.has("distance")){
                status.distance=jsonobject.getInt("distance");
            }

            if (jsonobject.has("pic_urls")) {
                parseThumbs(jsonobject, status);
            } else if (jsonobject.has("pic_ids")) {
                parseThumbs(jsonobject, status);
            }

            //if (!jsonobject.isNull("retweeted_status")) {
            if(jsonobject.has("retweeted_status")){
                status.retweetedStatus = parseStatus(jsonobject.getJSONObject("retweeted_status"));
            }

            String[] thumbs=status.thumbs;
            if (null==thumbs||thumbs.length==0) {
                if (null!=status.retweetedStatus) {
                    thumbs=status.retweetedStatus.thumbs;
                }
            }
            status.thumbs=thumbs;
        } catch (JSONException jsonexception) {
            //throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return status;
    }

    /**
     * 解析多个缩略图
     *
     * @param jsonobject
     * @param status
     */
    private static void parseThumbs(JSONObject jsonobject, Status status) {
        try {
            JSONArray jsonArray;
            String[] thumbs=null;
            if (jsonobject.has("pic_urls")) {
                jsonArray=jsonobject.getJSONArray("pic_urls");
                int len=jsonArray.length();
                thumbs=new String[len];
                for (int i=0; i<len; i++) {
                    thumbs[i]=((JSONObject) jsonArray.get(i)).getString("thumbnail_pic");
                }
            } else if (jsonobject.has("pic_ids")) { //是针对位置微博的.
                //因为只有id,所以前缀需要根据其它的url计算出.
                String sUrl=status.thumbnailPic;
                if (!TextUtils.isEmpty(sUrl)&&sUrl.indexOf("thumbnail")>-1) {
                    int idx=sUrl.lastIndexOf("thumbnail");
                    sUrl=sUrl.substring(0, idx+10);
                } else {
                    sUrl="";
                }
                jsonArray=jsonobject.getJSONArray("pic_ids");
                int len=jsonArray.length();
                thumbs=new String[len];
                for (int i=0; i<len; i++) {
                    thumbs[i]=sUrl+(String) jsonArray.get(i);
                }
            }
            status.thumbs=thumbs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析位置中的geo
     * @param jsonobject
     * @param status
     */
    private static void parseGeo(JSONObject jsonobject, Status status) {
        Geo geo=new Geo();
        try {
            JSONObject jo=jsonobject.getJSONObject("geo");
            geo.type=jo.getString("type");
            geo.coordinates=jo.getString("coordinates");
            status.geo=geo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析位置微博中的annotations
     * @param jsonobject
     * @param status
     */
    private static void parseSAnnotation(JSONObject jsonobject, Status status) {
        SAnnotation annotation=new SAnnotation();
        try {
            JSONArray ja=jsonobject.getJSONArray("annotations");
            SPlace place=parsePlaces(ja);
            annotation.place=place;
            status.annotations=annotation;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SPlace parsePlaces(JSONArray jsonarray) {
        int i=0;
        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                SPlace place=null;
                place=WeiboParser.parsePlace(jsonarray.getJSONObject(i));
                return place;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 这里的数据有两种可能，
     * {"place":{"poiid":"B2094652D36DABFD4598","lon":"119.278","title":"西禅古寺(东南门)","public":1,"type":"checkin","lat":"26.07093"}}
     * {"wpinfo":{"type":"place","url":"http:\/\/m.weibo.cn\/poixy?lng=121.329796&lat=30.773287","title":"上海市,金山区,G15沈海高速入口"}}
     *
     * @param jsonObject
     * @return
     */
    private static SPlace parsePlace(JSONObject jo) {
        SPlace sPlace=null;
        int i=0;
        try {
            JSONObject jsonObject=null;
            if (jo.has("place")) {
                jsonObject=jo.getJSONObject("place");
                if (!jsonObject.has("poiid")) {
                    WeiboLog.e("签到没有poiid.");
                    return sPlace;
                }

                sPlace=new SPlace();
                sPlace.poiid=jsonObject.getString("poiid");
                if (jsonObject.has("title")) {
                    sPlace.title=jsonObject.getString("title");
                }
                sPlace.type=jsonObject.getString("type");
                if (jsonObject.has("lon")) {
                    sPlace.lon=jsonObject.getDouble("lon");
                }
                if (jsonObject.has("lat")) {
                    sPlace.lat=jsonObject.getDouble("lat");
                }
                if (jsonObject.has("source")) {
                    sPlace.source=jsonObject.getString("source");
                }
                if (jsonObject.has("public")) {
                    sPlace.ppublic=jsonObject.getInt("public");
                }
            } else if (jo.has("wpinfo")) {
                jsonObject=jo.getJSONObject("wpinfo");
                sPlace=new SPlace();
                sPlace.placeUrl=jsonObject.getString("url");
                if (jsonObject.has("title")) {
                    sPlace.title=jsonObject.getString("title");
                }
                sPlace.type=jsonObject.getString("type");
            }
        } catch (JSONException e) {
            WeiboLog.e("jo:"+jo);
            e.printStackTrace();
        }
        return sPlace;
    }

    public static Status parseStatus(String js) throws WeiboException {
        JSONObject jsonObject=contructJSONObject(js);
        if(null==jsonObject){
            return null;
        }
        return parseStatus(jsonObject);
    }

    /**
     * 解析微博内容
     *
     * @param jsonarray 需要解析的微博
     * @return
     * @throws WeiboException
     */
    private static ArrayList<Status> parseStatuses(JSONArray jsonarray) throws WeiboException {
        ArrayList<Status> arraylist=new ArrayList<Status>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                Status status=null;
                status=WeiboParser.parseStatus(jsonarray.getJSONObject(i));
                arraylist.add(status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析微博内容，oauth1时的数据
     *
     * @param js 需要解析的字符串
     * @return
     * @throws WeiboException
     */
    public static ArrayList<Status> parseStatuses(String js) throws WeiboException {
        JSONArray jsonarray=null;
        try {
            jsonarray=new JSONArray(js);
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage()+":"+js, e);
        }

        return parseStatuses(jsonarray);
    }

    /**
     * 解析微博内容，oauth2时的数据
     *
     * @param js 需要解析的字符串，可能会有一些多余的数据，previous_cursor等
     * @return
     * @throws WeiboException
     */
    public static SStatusData<Status> parseStatuses2(String js) throws WeiboException {
        SStatusData<Status> sStatusData=new SStatusData<Status>();
        if("[]".equals(js)){
            return sStatusData;
        }
        JSONObject jo=contructJSONObject(js);

        if(null==jo){
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("statuses");
            if (null!=jsonarray) {
                ArrayList<Status> arraylist=parseStatuses(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(jo.has("hasvisible")){
                sStatusData.hasvisible=jo.getBoolean("hasvisible");
            }

            if(jo.has("previous_cursor")){
                sStatusData.previous_cursor=jo.getInt("previous_cursor");
            }

            if(jo.has("next_cursor")){
                sStatusData.next_cursor=jo.getInt("next_cursor");
            }

            if(jo.has("total_number")){
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + jo, e);
        }
        return sStatusData;
    }

    //热门数据不一样的
    /**
     * 解析微博内容，oauth2时的数据
     *
     * @param js 需要解析的字符串，可能会有一些多余的数据，previous_cursor等
     * @return
     * @throws WeiboException
     */
    public static SStatusData<Status> parseStatusesHot(String js) throws WeiboException {
        SStatusData<Status> sStatusData=new SStatusData<Status>();
        if ("[]".equals(js)) {
            return sStatusData;
        }
        JSONObject jo=contructJSONObject(js);

        if (null==jo) {
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("statuses");
            if (null!=jsonarray) {
                ArrayList<Status> arraylist=parseStatuses(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (jo.has("hasvisible")) {
                sStatusData.hasvisible=jo.getBoolean("hasvisible");
            }

            if (jo.has("previous_cursor")) {
                sStatusData.previous_cursor=jo.getInt("previous_cursor");
            }

            if (jo.has("next_cursor")) {
                sStatusData.next_cursor=jo.getInt("next_cursor");
            }

            if (jo.has("total_number")) {
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage()+":"+jo, e);
        }
        return sStatusData;
    }

    //--------------------- 收藏 ---------------------

    /**
     * 解析收藏结果
     *
     * @param js 收藏结果
     * @return
     * @throws WeiboException
     */
    public static Favorite parseFavorite(String js) throws WeiboException {
        Favorite favorite=null;
        JSONObject jo=contructJSONObject(js);
        if (null==jo) {
            return favorite;
        }
        favorite=parseFavorite(jo);

        return favorite;
    }

    /**
     * 解析收藏结果
     *
     * @param jo 收藏结果 JSONObject对象
     * @return
     * @throws WeiboException
     */
    public static Favorite parseFavorite(JSONObject jo) throws WeiboException {
        Favorite favorite=null;
        favorite=new Favorite();
        try {
            favorite.favorited_time=jo.getString("favorited_time");
            JSONObject jsonObject=jo.getJSONObject("status");
            Status status=parseStatus(jsonObject);
            favorite.mStatus=status;

            try {
                if (jo.has("tags")) {
                    jsonObject=jo.getJSONArray("tags").getJSONObject(0);
                    Tags tags=new Tags();
                    tags.id=jsonObject.getLong("id");
                    tags.tag=jsonObject.getString("tags");
                    favorite.tags=tags;
                }
            } catch (JSONException e) {
                WeiboLog.w("parse favorite tag error:"+e);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return favorite;
    }

    /**
     * 解析收藏列表
     *
     * @param js 收藏结果集
     * @return
     * @throws WeiboException
     */
    public static SStatusData<Favorite> parseFavorites(String js) throws WeiboException {
        SStatusData<Favorite> sStatusData=new SStatusData<Favorite>();
        if("[]".equals(js)){
            return sStatusData;
        }
        JSONObject jo=contructJSONObject(js);

        if(null==jo){
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("favorites");
            if (null!=jsonarray&&jsonarray.length()>0) {
                ArrayList<Favorite> arraylist=parseFavorites(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sStatusData;
    }

    /**
     * 解析收藏列表
     *
     * @param jsonarray 收藏结果集，一个jsonobject数组
     * @return
     * @throws WeiboException
     */
    public static ArrayList<Favorite> parseFavorites(JSONArray jsonarray) throws WeiboException {
        ArrayList<Favorite> arraylist=new ArrayList<Favorite>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                Favorite status=null;
                status=WeiboParser.parseFavorite(jsonarray.getJSONObject(i));
                arraylist.add(status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析收藏结果
     *
     * @param js 收藏结果
     * @return
     * @throws WeiboException
     */
    public static boolean parseResult(String js) throws WeiboException {
        boolean result=false;
        JSONObject jo=contructJSONObject(js);
        if (null==jo) {
            return result;
        }
        try {
            result=jo.getBoolean("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 解析各种消息未读数
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    public static Unread parseUnread(JSONObject jsonobject) throws WeiboException {
        Unread unread = new Unread();
        try {
            unread.comments = jsonobject.getInt("cmt");
            unread.dm = jsonobject.getInt("dm");
            unread.followers = jsonobject.getInt("follower");
            if (jsonobject.has("new_status")) {
                unread.newStatus = jsonobject.getInt("new_status");
            }

            if(jsonobject.has("status")){
                unread.status=jsonobject.getInt("status");
            }

            if(jsonobject.has("mentions")){
                unread.mentions = jsonobject.getInt("mentions");
            }
            unread.mention_status=jsonobject.getInt("mention_status");
            unread.mention_cmt=jsonobject.getInt("mention_cmt");
            unread.group=jsonobject.getInt("group");
            if (jsonobject.has("private_group")) {
                unread.private_group=jsonobject.getInt("private_group");
            }
            unread.notice=jsonobject.getInt("notice");
            unread.invite=jsonobject.getInt("invite");
            unread.badge=jsonobject.getInt("badge");
            unread.photo=jsonobject.getInt("photo");
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return unread;
    }

    public static Unread parseUnread(String js) throws WeiboException {
        return parseUnread(contructJSONObject(js));
    }

    /**
     * 返回未读消息清零结果，这里用SStatusData来作为结果。可以一起处理了错误代码。
     * @param js
     * @return
     * @throws WeiboException
     */
    @Deprecated
    public static SStatusData parseSetUnread(String js) throws WeiboException {
        JSONObject jo=contructJSONObject(js);
        SStatusData sStatusData=null;
        if (null==jo) {
            return sStatusData;
        }

        try {
            sStatusData=new SStatusData();
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            } else {
                sStatusData.hasvisible=jo.getBoolean("result");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sStatusData;
    }

    /**
     * 用户有不同的数据，搜索时只有screenName,followers_count,uid,但其它的可能没有uid只有id
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    public static User parseUser(JSONObject jsonobject) throws WeiboException {
        //WeiboLog.i("",jsonobject.toString());
        User user = new User();
        try {
            if (jsonobject.has("id")) {
                user.id = jsonobject.getLong("id");
            }else if(jsonobject.has("uid")){
                user.id = jsonobject.getLong("uid");    //搜索时必有
            }
            user.screenName = jsonobject.getString("screen_name");
            user.name = jsonobject.getString("name");
            user.province = jsonobject.getString("province");
            user.city = jsonobject.getString("city");
            user.location = jsonobject.getString("location");
            user.description = jsonobject.getString("description");
            user.url = jsonobject.getString("url");
            user.profileImageUrl = jsonobject.getString("profile_image_url");
            user.domain = jsonobject.getString("domain");
            if(jsonobject.has("avatar_large")) {
            	user.avatar_large=jsonobject.getString("avatar_large");
            }

            if (jsonobject.has("gender")) {
                user.gender = jsonobject.getString("gender");
            } else {
                user.gender = "n";
            }
            user.followersCount = jsonobject.getInt("followers_count");
            user.friendsCount = jsonobject.getInt("friends_count");
            user.statusesCount = jsonobject.getInt("statuses_count");
            user.favouritesCount = jsonobject.getInt("favourites_count");
            user.createdAt = parseDate(jsonobject.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
            user.following = parseBoolean("following", jsonobject);
            user.allowAllActMsg = parseBoolean("allow_all_act_msg", jsonobject);
            user.geoEnabled = parseBoolean("geo_enabled", jsonobject);
            user.verified = parseBoolean("verified", jsonobject);

            if(jsonobject.has("verified_reason")){
                user.verified_reason=jsonobject.getString("verified_reason");
            }

            if(jsonobject.has("follow_me")){
                user.follow_me=parseBoolean("follow_me",jsonobject);
            }

            if(jsonobject.has("online_status")){
                user.online_status=jsonobject.getInt("online_status");
            }

            if(jsonobject.has("bi_followers_count")){
                user.bi_followers_count=jsonobject.getInt("bi_followers_count");
            }

            if (!jsonobject.isNull("status")) {
                JSONObject jsonobject1 = jsonobject.getJSONObject("status");
                Status status = new Status();
                user.status = status;
                status.createdAt = parseDate(jsonobject1.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
                status.id = jsonobject1.getLong("id");

                status.text = jsonobject1.getString("text");
                status.source = jsonobject1.getString("source");
                status.favorited = parseBoolean("favorited", jsonobject1);
                status.truncated = parseBoolean("truncated", jsonobject1);
                status.inReplyToStatusId = jsonobject1.getString("in_reply_to_status_id");
                status.inReplyToUserId = jsonobject1.getString("in_reply_to_user_id");
                status.inReplyToScreenName = jsonobject1.getString("in_reply_to_screen_name");
            }
            return user;
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
    }

    public static User parseUser(String js) throws WeiboException {
        return parseUser(contructJSONObject(js));
    }

    public static long parseID(String js) throws WeiboException {
        try {
            return contructJSONObject(js).getLong("uid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static ArrayList<User> getUsers(String rs) throws WeiboException {
        ArrayList<User> arraylist = new ArrayList<User>();

        JSONArray jSONArray = contructJSONArray(rs);
        try {
            int len = jSONArray.length();
            JSONObject jo;
            User u;
            for (int i = 0; i < len; i++) {
                jo = jSONArray.getJSONObject(i);
                u = parseUser(jo);
                arraylist.add(u);
            }
        } catch (JSONException ex) {
            throw new WeiboException(ex.getMessage() + ":" + rs, ex);
        }
        return arraylist;
    }

    //--------------------- 搜索服务 ---------------------

    /**
     * 搜索有不同的结果段，但有类似的结果集。
     *
     * @param rs
     * @param type 类型，0为微博，1为用户，2为学校，3为公司，4为应用
     * @return
     * @throws WeiboException
     */
    public static ArrayList<Map<String, String>> getSuggestions(String rs, int type) throws WeiboException {
        ArrayList<Map<String, String>> arraylist=new ArrayList<Map<String, String>>();

        JSONArray jSONArray=contructJSONArray(rs);
        try {
            int len=jSONArray.length();
            JSONObject jo;
            Map<String, String> u=null;
            for (int i=0; i<len; i++) {
                jo=jSONArray.getJSONObject(i);
                if (type==0) {
                    u=parseStatusesMap(jo);
                } else if (type==1) {
                    u=parseUseresMap(jo);
                } else if (type==2) {
                    u=parseSchoolsMap(jo);
                } else if (type==3) {
                    u=parseCompaniesMap(jo);
                } else if (type==4) {
                    u=parseAppsMap(jo);
                }
                arraylist.add(u);
            }
        } catch (JSONException ex) {
            throw new WeiboException(ex.getMessage()+":"+rs, ex);
        }
        return arraylist;
    }

    /**
     * 解析微博搜索结果
     *
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    private static Map<String, String> parseUseresMap(JSONObject jsonobject) throws WeiboException {
        Map<String, String> map=new HashMap<String, String>();
        try {
            map.put("screen_name", jsonobject.getString("screen_name"));
            map.put("followers_count", jsonobject.getString("followers_count"));
            map.put("uid", jsonobject.getString("uid"));
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jsonobject, jsonexception);
        }
        return map;
    }

    /**
     * 解析微博搜索结果
     *
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    private static Map<String, String> parseStatusesMap(JSONObject jsonobject) throws WeiboException {
        Map<String, String> map=new HashMap<String, String>();
        try {
            map.put("suggestion", jsonobject.getString("suggestion"));
            map.put("count", jsonobject.getString("count"));
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jsonobject, jsonexception);
        }
        return map;
    }

    /**
     * 解析学校搜索结果
     *
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    private static Map<String, String> parseSchoolsMap(JSONObject jsonobject) throws WeiboException {
        Map<String, String> map=new HashMap<String, String>();
        try {
            map.put("school_name", jsonobject.getString("school_name"));
            map.put("location", "location");
            map.put("id", jsonobject.getString("id"));
            map.put("type", jsonobject.getString("type"));
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jsonobject, jsonexception);
        }
        return map;
    }

    /**
     * 解析学校搜索结果
     *
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    private static Map<String, String> parseCompaniesMap(JSONObject jsonobject) throws WeiboException {
        Map<String, String> map=new HashMap<String, String>();
        try {
            map.put("suggestion", jsonobject.getString("suggestion"));
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jsonobject, jsonexception);
        }
        return map;
    }

    /**
     * 解析应用搜索结果
     *
     * @param jsonobject
     * @return
     * @throws WeiboException
     */
    private static Map<String, String> parseAppsMap(JSONObject jsonobject) throws WeiboException {
        Map<String, String> map=new HashMap<String, String>();
        try {
            map.put("apps_name", jsonobject.getString("apps_name"));
            map.put("members_count", "members_count");
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jsonobject, jsonexception);
        }
        return map;
    }

    /**
     * 解析微博内容
     *
     * @param jsonarray 需要解析的微博
     * @return
     * @throws WeiboException
     */
    private static ArrayList<User> parseUsers(JSONArray jsonarray) throws WeiboException {
        ArrayList<User> arraylist=new ArrayList<User>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                User user=null;
                user=WeiboParser.parseUser(jsonarray.getJSONObject(i));
                arraylist.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析废除，因为现在使用泛型，而且不少的api都提供了next_cursor这样的数据。
     * @param rs
     * @return
     * @throws WeiboException
     */
    @Deprecated
    public static Object[] getUserObjs(String rs) throws WeiboException {
        Object[] objects=new Object[3];
        ArrayList<User> arraylist=new ArrayList<User>();
        objects[0]=arraylist;

        JSONObject jsonObject=contructJSONObject(rs);
        if(jsonObject!=null) {
            try {
                int next_cursor=jsonObject.getInt("next_cursor");
                int previous_cursor=jsonObject.getInt("previous_cursor");
                objects[1]=next_cursor;
                objects[2]=previous_cursor;

                JSONArray ja=jsonObject.getJSONArray("users");

                int len=ja.length();
                JSONObject jo;
                User u;
                for (int i=0; i<len; i++) {
                    jo=ja.getJSONObject(i);
                    u=parseUser(jo);
                    arraylist.add(u);
                }
            } catch (JSONException ex) {
                throw new WeiboException(ex.getMessage()+":"+rs, ex);
            }
        }
        return objects;
    }

    /**
     * 解析用户，
     * @param js
     * @return
     * @throws WeiboException
     */
    public static SStatusData<User> getUserObjs2(String js) throws WeiboException {
        SStatusData<User> sStatusData=new SStatusData<User>();
        JSONObject jo=contructJSONObject(js);

        if(null==jo){
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("users");
            if (null!=jsonarray) {
                ArrayList<User> arraylist=parseUsers(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(jo.has("hasvisible")){
                sStatusData.hasvisible=jo.getBoolean("hasvisible");
            }

            if(jo.has("previous_cursor")){
                sStatusData.previous_cursor=jo.getInt("previous_cursor");
            }

            if(jo.has("next_cursor")){
                sStatusData.next_cursor=jo.getInt("next_cursor");
            }

            if(jo.has("total_number")){
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + jo, e);
        }
        return sStatusData;
    }

    public static Object[] getIDs(String rs) throws WeiboException {
        Object aobj[] = new Object[3];
        ArrayList<IDs> arraylist = new ArrayList<IDs>();
        aobj[0] = arraylist;

        JSONObject jsonobject = contructJSONObject(rs);
        JSONArray jsonarray = null;
        try {
            jsonarray = jsonobject.getJSONArray("ids");
            int i = 0;
            int len = jsonarray.length();
            for (; i < len; i++) {
                arraylist.add((IDs) jsonarray.get(i));
            }

            if (jsonobject.has("next_cursor")) {
                String s1 = jsonobject.getString("next_cursor");
                aobj[1] = s1;
            }
            if (jsonobject.has("previous_cursor")) {
                String s2 = jsonobject.getString("previous_cursor");
                aobj[2] = s2;
            }
            return aobj;
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + rs, e);
        }
    }

    public static Trends parseTrends(String ja) throws WeiboException {
        JSONObject jo = contructJSONObject(ja);

        try {
            Trends trends = new Trends();
            trends.asOf = jo.getString("as_of");

            JSONObject jSONObject = jo.getJSONObject("trends");
            Iterator<String> it = jSONObject.keys();
            if (it.hasNext()) {
                String keyString = it.next();

                JSONArray jSONArray = (JSONArray) jSONObject.get(keyString);

                int len = jSONArray.length();
                Trend[] trendArr = new Trend[len];
                trends.trends = trendArr;
                Trend tmp;

                for (int i = len - 1; i >= 0; i--) {
                    JSONObject jsono = (JSONObject) jSONArray.getJSONObject(i);
                    tmp = new Trend();
                    tmp.name = jsono.getString("name");
                    tmp.query = jsono.getString("query");
                    trendArr[i] = tmp;
                }
            }
            return trends;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //-------------------------------
    /**
     * 解析城市
     *
     * @param ctx
     */
    public static ArrayList<Province> parseCitys(Context ctx) {
        InputStream is=null;
        String line;
        try {
            is=ctx.getResources().openRawResource(R.raw.provinces);
            line=WeiboUtil.parseInputStream(is);
            JSONObject jo=contructJSONObject(line);
            JSONArray provinces=jo.getJSONArray("provinces");
            return parseAllCity(provinces);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WeiboException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ArrayList<Province> parseAllCity(JSONArray provinces) throws WeiboException {
        //WeiboLog.d("parseCity.provinces:"+provinces);
        ArrayList<Province> arraylist=new ArrayList<Province>();

        int len=provinces.length();
        try {
            JSONObject jo;
            Province u;
            for (int i=0; i<len; i++) {
                jo=provinces.getJSONObject(i);
                u=parseProvince(jo);
                if (null!=u) {
                    arraylist.add(u);
                }
            }
        } catch (JSONException ex) {
            //throw new WeiboException(ex.getMessage()+":", ex);
        }

        return arraylist;
    }

    /**
     * 解析省份，也可能是直辖市，或自制区。
     *
     * @param jo
     * @return
     * @throws WeiboException
     */
    private static Province parseProvince(JSONObject jo) throws WeiboException {
        //WeiboLog.d("parseProvince,jo:"+jo);
        Province province=new Province();
        try {
            String id=jo.getString("id");
            if("400".equals(id)||"100".equals(id)){
                WeiboLog.d("海外,其他:"+id);
                return null;
            }

            province.id=id;
            String name=jo.getString("name");
            province.name=name;
            if ("11".equals(id)||"12".equals(id)||"31".equals(id)||"50".equals(id)) {
                WeiboLog.d("是直辖市，不解析区。"+id);
                ArrayList<City> cities=new ArrayList<City>();
                City city=new City();
                city.id=id;
                city.name=name;
                city.pinyin=PinYin.getPinYin(name);
                cities.add(city);
                province.cities=cities;
            } else {
                JSONArray citys=jo.getJSONArray("citys");
                ArrayList<City> cities=parseCity(citys);
                province.cities=cities;
            }
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jo, jsonexception);
        }

        return province;
    }

    /**
     * 处理具体的城市，但一些自制区，直辖市不应该放在这里
     *
     * @param ja
     * @return
     * @throws WeiboException
     */
    private static ArrayList<City> parseCity(JSONArray ja) throws WeiboException {
        ArrayList<City> cities;
        try {
            int len=ja.length();
            cities=new ArrayList<City>();
            JSONObject jo;
            String cityJo;
            City city;

            for (int i=0; i<len; i++) {
                jo=ja.getJSONObject(i);
                Iterator<String> it=jo.keys();
                if (it.hasNext()) {
                    String keyString=it.next();
                    if("90".equals(keyString)){
                        WeiboLog.d("其它城市不作处理。");
                        continue;
                    }
                    city=new City();
                    cityJo=(String) jo.get(keyString);
                    city.id=keyString;
                    city.name=cityJo;
                    city.pinyin=PinYin.getPinYin(cityJo);
                    cities.add(city);
                }
            }
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+ja, jsonexception);
        }

        return cities;
    }

    //-------------------------------
    /**
     * 处理更新信息
     *
     * @param rs 更新url的内容
     * @return
     */
    public static UpdateInfo parseUpdateInfo(String rs) {
        UpdateInfo updateInfo=new UpdateInfo();
        try {
            JSONObject jo=contructJSONObject(rs);
            updateInfo.updateMode=jo.getString("update_mode");
            updateInfo.updateMsg=jo.getString("update_msg");
            updateInfo.updateUrl=jo.getString("update_url");
            updateInfo.hasNewVer=jo.getString("has_new_ver");
            updateInfo.newVer=jo.getString("new_ver");
        } catch (WeiboException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return updateInfo;
    }

    //-------------------------------
    public static SPoi parseSPoi(JSONObject jsono) {
        SPoi tmp=new SPoi();
        try {
            tmp.spid=jsono.getString("spid");
            tmp.name=jsono.getString("name");
            tmp.address=jsono.getString("address");
            tmp.category=jsono.getString("category");
            tmp.navigator=jsono.getString("navigator");
            tmp.telephone=jsono.getString("telephone");
            tmp.pic_url=jsono.getString("pic_url");
            tmp.city=jsono.getString("city");
            tmp.province=jsono.getString("province");
            tmp.longitude=jsono.getLong("longitude");
            tmp.latitude=jsono.getLong("latitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    /**
     * 解析poi结果
     * @param ja
     * @return
     * @throws WeiboException
     */
    public static SPoiResult parseSPoiResult(String ja) throws WeiboException {
        JSONObject jo=contructJSONObject(ja);

        try {
            SPoiResult poiResult=new SPoiResult();
            poiResult.total=jo.getInt("total");
            poiResult.count=jo.getInt("count");
            poiResult.page=jo.getInt("page");

            JSONObject jSONObject=jo.getJSONObject("pois");
            Iterator<String> it=jSONObject.keys();
            if (it.hasNext()) {
                String keyString=it.next();

                JSONArray jSONArray=(JSONArray) jSONObject.get(keyString);

                int len=jSONArray.length();
                SPoi[] sPois=new SPoi[len];
                poiResult.sPois=sPois;
                SPoi tmp;

                for (int i=len-1; i>=0; i--) {
                    JSONObject jsono=(JSONObject) jSONArray.getJSONObject(i);
                    tmp=parseSPoi(jsono);
                    sPois[i]=tmp;
                }
            }

            return poiResult;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 解析poi搜索结果
     * @param ja
     * @return
     * @throws WeiboException
     */
    public static SPoiSearchResult parseSPoiSearchResult(String ja) throws WeiboException {
        JSONObject jo=contructJSONObject(ja);

        try {
            SPoiSearchResult poiSearchResult=new SPoiSearchResult();
            poiSearchResult.total=jo.getInt("total");
            poiSearchResult.count=jo.getInt("count");
            poiSearchResult.page=jo.getInt("page");
            if (jo.has("center_poi")&&jo.getJSONObject("center_poi").has("poi")) {
                JSONObject centerPoiJo=jo.getJSONObject("center_poi").getJSONObject("poi");
                SPoi centerPoi=parseSPoi(centerPoiJo);
                poiSearchResult.centerPoi=centerPoi;
            }

            JSONArray jSONArray=jo.getJSONArray("pois");

            int len=jSONArray.length();
            SPoi[] sPois=new SPoi[len];
            poiSearchResult.sPois=sPois;
            SPoi tmp;

            for (int i=len-1; i>=0; i--) {
                JSONObject jsono=(JSONObject) jSONArray.getJSONObject(i);
                tmp=parseSPoi(jsono);
                sPois[i]=tmp;
            }

            return poiSearchResult;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //--------------------- 位置服务 ---------------------

    /**
     * 解析位置poi
     * @param js
     * @return
     * @throws WeiboException
     */
    public static SStatusData<PlacePoi> parsePlacePois(String js) throws WeiboException {
        SStatusData<PlacePoi> sStatusData=new SStatusData<PlacePoi>();
        JSONObject jo=contructJSONObject(js);

        if(null==jo){
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("pois");
            if (null!=jsonarray) {
                ArrayList<PlacePoi> arraylist=parsePlacePois(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(jo.has("hasvisible")){
                sStatusData.hasvisible=jo.getBoolean("hasvisible");
            }

            if(jo.has("previous_cursor")){
                sStatusData.previous_cursor=jo.getInt("previous_cursor");
            }

            if(jo.has("next_cursor")){
                sStatusData.next_cursor=jo.getInt("next_cursor");
            }

            if(jo.has("total_number")){
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage() + ":" + jo, e);
        }
        return sStatusData;
    }

    /**
     * 解析位置poi
     * @param jsonarray
     * @return
     */
    private static ArrayList<PlacePoi> parsePlacePois(JSONArray jsonarray) {
        ArrayList<PlacePoi> arraylist=new ArrayList<PlacePoi>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                PlacePoi placePoi=null;
                placePoi=parsePlacePoi(jsonarray.getJSONObject(i));
                arraylist.add(placePoi);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析的问题在于，出现未知的poi，返回的不是空对象。
     * @param jsonobject
     * @return
     */
    private static PlacePoi parsePlacePoi(JSONObject jsonobject) {
        PlacePoi placePoi = new PlacePoi();
        try {
            placePoi.poiid=jsonobject.getString("poiid");
            placePoi.title = jsonobject.getString("title");
            placePoi.address = jsonobject.getString("address");
            placePoi.lon = jsonobject.getDouble("lon");
            placePoi.lat = jsonobject.getDouble("lat");
            placePoi.category = jsonobject.getString("category");
            placePoi.city = jsonobject.getString("city");
            if(jsonobject.has("province")){
                placePoi.province = jsonobject.getString("province");
            }
            if(jsonobject.has("country")){
                placePoi.country = jsonobject.getString("country");
            }
            placePoi.url = jsonobject.getString("url");
            placePoi.phone = jsonobject.getString("phone");
            placePoi.postcode = jsonobject.getString("postcode");
            placePoi.weibo_id = jsonobject.getString("weibo_id");
            placePoi.categorys = jsonobject.getString("tcategorysitle");
            placePoi.category_name = jsonobject.getString("category_name");
            placePoi.icon = jsonobject.getString("icon");
            placePoi.checkin_num = jsonobject.getInt("category_name");
            placePoi.checkin_user_num = jsonobject.getInt("checkin_user_num");
            placePoi.tip_num = jsonobject.getInt("tip_num");
            placePoi.photo_num = jsonobject.getInt("photo_num");
            placePoi.todo_num = jsonobject.getInt("todo_num");
            if (jsonobject.has("distance")) {
                placePoi.distance = jsonobject.getInt("distance");
            }
            if (jsonobject.has("checkin_time")) {
                placePoi.checkin_time = jsonobject.getString("checkin_time");
            }
        } catch (JSONException jsonexception) {
            //throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return placePoi;
    }

    public static PlacePoi parsePlacePoi(String js) throws WeiboException {
        JSONObject jsonobject=contructJSONObject(js);
        return parsePlacePoi(jsonobject);
    }

    //--------------------- 好友分组 ---------------------
    public static Group parseGroups(JSONObject jsonobject) throws WeiboException {
        Group group=new Group();
        try {
            group.createdAt=jsonobject.getString("created_at");
            group.id=jsonobject.getString("id");
            group.idstr=jsonobject.getString("idstr");
            group.name=jsonobject.getString("name");
            group.visible=jsonobject.getInt("visible");
            group.like_count=jsonobject.getInt("like_count");
            group.member_count=jsonobject.getInt("member_count");
            group.description=jsonobject.getString("description");
            group.profile_image_url=jsonobject.getString("profile_image_url");

            if (jsonobject.has("user")) {
                User user=parseUser(jsonobject.getJSONObject("user"));
                group.user=user;
            }
        } catch (JSONException jsonexception) {
            //throw new WeiboException(jsonexception.getMessage() + ":" + jsonobject, jsonexception);
        }
        return group;
    }

    public static ArrayList<Group> parseGroups(JSONArray jsonarray) throws WeiboException {
        ArrayList<Group> arraylist=new ArrayList<Group>();
        int i=0;

        try {
            int len=jsonarray.length();
            for (; i<len; i++) {
                Group status=null;
                status=WeiboParser.parseGroups(jsonarray.getJSONObject(i));
                arraylist.add(status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    /**
     * 解析好友分组
     *
     * @param js
     * @return
     * @throws WeiboException
     */
    public static SStatusData<Group> parseGroups(String js) throws WeiboException {
        SStatusData<Group> sStatusData=new SStatusData<Group>();
        if ("[]".equals(js)) {
            return sStatusData;
        }
        JSONObject jo=contructJSONObject(js);

        if (null==jo) {
            return sStatusData;
        }

        try {
            if (jo.has("error")) {
                sStatusData.errorCode=jo.getInt("error_code");
                sStatusData.errorMsg=jo.getString("error");
                return sStatusData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonarray=jo.getJSONArray("lists");
            if (null!=jsonarray) {
                ArrayList<Group> arraylist=parseGroups(jsonarray);
                sStatusData.mStatusData=arraylist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (jo.has("total_number")) {
                sStatusData.total_number=jo.getInt("total_number");
            }
        } catch (JSONException e) {
            throw new WeiboException(e.getMessage()+":"+jo, e);
        }
        return sStatusData;
    }

    public static Group parseGroup(String js) throws WeiboException {
        return parseGroups(contructJSONObject(js));
    }

    //-------------------------------
    private static void parseQVideo(QStatus status, String jo) {
        if (TextUtils.isEmpty(jo)||"null".equals(jo)) {
            return;
        }

        try {
            JSONObject jsonObject=contructJSONObject(jo);
            QVideo qVideo=new QVideo();
            qVideo.picurl=jsonObject.getString("picurl");
            qVideo.player=jsonObject.getString("player");
            qVideo.realurl=jsonObject.getString("realurl");
            qVideo.shorturl=jsonObject.getString("shorturl");
            qVideo.title=jsonObject.getString("title");
            status.qVideo=qVideo;
        } catch (JSONException ex) {
            //ex.printStackTrace();
        } catch (WeiboException e) {
            //e.printStackTrace();
        }
    }

    private static void parseQMusic(QStatus status, String jo) {
        if (TextUtils.isEmpty(jo)||"null".equals(jo)) {
            return;
        }

        try {
            JSONObject jsonObject=contructJSONObject(jo);
            QMusic qMusic=new QMusic();
            qMusic.author=jsonObject.getString("author");
            qMusic.title=jsonObject.getString("title");
            qMusic.url=jsonObject.getString("url");

            status.qMusic=qMusic;
        } catch (JSONException ex) {
            //ex.printStackTrace();
        } catch (WeiboException e) {
            //e.printStackTrace();
        }
    }

    public static QStatus parseQStatus(JSONObject jsonobject) throws WeiboException {
        QStatus status=new QStatus();
        try {
            status.id=jsonobject.getString("id");
            status.text=jsonobject.getString("text");
            status.origtext=jsonobject.getString("origtext");
            status.count=jsonobject.getInt("count");
            status.mcount=jsonobject.getInt("mcount");
            status.from=jsonobject.getString("from");
            if (jsonobject.has("fromurl")) {
                status.fromurl=jsonobject.getString("fromurl");
            }
            status.image=jsonobject.getString("image");
            status.name=jsonobject.getString("name");
            status.openId=jsonobject.getString("openid");
            status.nick=jsonobject.getString("nick");
            status.self=jsonobject.getInt("self");
            status.timestamp=jsonobject.getLong("timestamp");
            status.type=jsonobject.getInt("type");
            status.head=jsonobject.getString("head");
            status.location=jsonobject.getString("location");
            status.country_code=jsonobject.getString("country_code");
            status.province_code=jsonobject.getString("province_code");
            status.city_code=jsonobject.getString("city_code");
            status.isvip=jsonobject.getInt("isvip");
            status.geo=jsonobject.getString("geo");
            status.status=jsonobject.getInt("status");
            if (jsonobject.has("emotionurl")) { //转发的没有
                status.emotionurl=jsonobject.getString("emotionurl");
            }
            if (jsonobject.has("emotiontype")) { //转发的没有
                status.emotiontype=jsonobject.getInt("emotiontype");
            }

            if (jsonobject.has("video")) {
                parseQVideo(status, jsonobject.getString("video"));
            }
            if (jsonobject.has("music")) {
                parseQMusic(status, jsonobject.getString("music"));
            }
            if (jsonobject.has("source")&&status.type!=1) {
                QStatus retweetStatus=parseQStatus(jsonobject.getJSONObject("source"));
                status.retweetedStatus=retweetStatus;
            }
        } catch (JSONException jsonexception) {
            throw new WeiboException(jsonexception.getMessage()+":"+jsonobject, jsonexception);
        }
        return status;
    }

    public static QStatusData parseQStatuses(String js) throws WeiboException {
        JSONObject qdata=contructJSONObject(js);
        QStatusData qStatusData=new QStatusData();
        try {
            qStatusData.ret=qdata.getInt("ret");
            qStatusData.msg=qdata.getString("msg");
            qStatusData.errcode=qdata.getString("errcode");
            if (qdata.has("timestamp")) {
                qStatusData.timestamp=qdata.getLong("timestamp");
            }
            if (qdata.has("totalnum")) {
                qStatusData.totalnum=qdata.getInt("totalnum");
            }

            JSONObject data=qdata.getJSONObject("data");
            qStatusData.hasnext=data.getInt("hasnext");
            if (qStatusData.ret!=0) {
                throw new WeiboException(qStatusData.msg);
            }

            JSONArray jsonarray=data.getJSONArray("info");
            ArrayList<QStatus> arraylist=new ArrayList<QStatus>();
            qStatusData.qStatuses=arraylist;

            int i=0;

            int len=jsonarray.length();
            try {
                for (; i<len; i++) {
                    QStatus status=null;
                    status=WeiboParser.parseQStatus(jsonarray.getJSONObject(i));
                    arraylist.add(status);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
        }

        return qStatusData;
    }
}
