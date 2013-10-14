package com.me.microblog;

import java.io.File;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import com.me.microblog.bean.ContentItem;
import com.me.microblog.bean.Status;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import org.apache.http.HttpResponse;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import java.io.*;
import java.util.List;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-6-24
 */
public class WeiboUtil {

    private MessageDigest mDigest;
    private static WeiboUtil md5Util=new WeiboUtil();
    static int sActiveTabIndex=-1;
    public static final Pattern p=Pattern.compile("@[^(:| |：| |,|@)]*(:| |：| |,|。)", Pattern.MULTILINE);
    public static final Pattern ATPATTERN=Pattern.compile("@[[^@\\s%s|(:| |：|　|,|。|\\[)]0-9]{1,20}", Pattern.MULTILINE);
    public static final String schema="weibo://";
    private static Pattern atpattern=ATPATTERN;
    private static Pattern allPattern;
    private static Pattern sharppattern;
    private static Pattern webpattern;
    private static Pattern webpattern2;
    public static final Pattern comeFrom=Pattern.compile("<a[^>]*>");

    private WeiboUtil() {
        try {
            mDigest=MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // This shouldn't happen.
            throw new RuntimeException("No MD5 algorithm.");
        }
    }

    public static WeiboUtil getWeiboUtil() {
        return md5Util;
    }

    public String getMd5(String url) {
        mDigest.update(url.getBytes());

        return getHashString(mDigest);
    }

    public static String getFilePath(String dir, String url) {
        WeiboUtil weiboUtil=WeiboUtil.getWeiboUtil();
        String file=dir+"/"+weiboUtil.getMd5(url);
        return file;
    }

    private String getHashString(MessageDigest digest) {
        StringBuilder builder=new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b>>4)&0xf));
            builder.append(Integer.toHexString(b&0xf));
        }

        return builder.toString();
    }

    public static String checkDiskCache(String dir, String fileName) {
        String str1=String.valueOf(dir);
        String filePath=dir+"/"+fileName;
        WeiboUtil weiboUtil=WeiboUtil.getWeiboUtil();
        String str2=weiboUtil.getMd5(filePath);
        boolean isExists=new File(str2).exists();
        if (!isExists)
            return str2;
        return null;
    }

    ///////////////////////////////////

    /**
     * 读取HttpResponse返回的流.
     *
     * @param response
     * @return
     * @throws IOException
     */
    public static String parseInputStream(HttpResponse response) throws IOException {
        InputStream is=response.getEntity().getContent();
        return parseInputStream(is);
    }

    /**
     * 从输入流中读取字符串.
     *
     * @param is 输入流
     * @return
     * @throws IOException
     */
    public static String parseInputStream(InputStream is) throws IOException {
        BufferedReader reader=new BufferedReader(new InputStreamReader(is), 1000);
        StringBuilder responseBody=new StringBuilder();
        String line=reader.readLine();
        while (line!=null) {
            responseBody.append(line);
            line=reader.readLine();
        }
        String string=responseBody.toString();
        return string;
    }

    public static void highlightContent(Context context, Spannable spannable, int color) {
        Matcher atMatcher=getAtPattern().matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            //WeiboLog.i("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
            } else {
                if (end-start<=2) {
                    return;
                }
            }

            ForegroundColorSpan colorSpan=new ForegroundColorSpan(color);
            spannable.setSpan(colorSpan, start, end, 33);
        }
    }

    public static ArrayList<ContentItem> getAtHighlightContent(Context context, Spannable spannable) {
        //WeiboLog.d("ori:"+spannable);
        ArrayList<ContentItem> contentItems=new ArrayList<ContentItem>();
        ContentItem item;
        Matcher atMatcher=getAtPattern().matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            //WeiboLog.i("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
            } else {
                if (end-start<=2) {
                    //return;
                    continue;
                }
            }

            item=new ContentItem();
            item.type="@";
            item.content=String.valueOf(spannable.subSequence(start, end)).trim();
            boolean exist=false;
            for (ContentItem ci : contentItems) {
                if (ci.type.equals(item.type)&&ci.content.equals(item.content)) {
                    exist=true;
                    break;
                }
            }

            if (!exist) {
                contentItems.add(item);
            }

            //WeiboLog.d("content:"+item.content+" exist:"+exist);
        }

        return contentItems;
    }

    /**
     * *****************************************
     */
    public static void highlightContentClickable(Context context, Spannable spannable) {
        Matcher atMatcher=getAtPattern().matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            WeiboLog.i("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
                /*String me = "我";
                 int k = start + 1;
                 Character character = Character.valueOf(spannable.charAt(k));*/
                //if (me.equals(character))
            } else {
                if (end-start<=2) {
                    break;
                }
            }

            /*ForegroundColorSpan colorSpan=new ForegroundColorSpan(color);
            spannable.setSpan(colorSpan, start, end, 33);*/

            String name=spannable.subSequence(start, end).toString();
            AtClicker atClicker=new AtClicker(context, name);
            spannable.setSpan(atClicker, start, end, 33);
        }

        atMatcher=getWebPattern().matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            WeiboLog.i("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
                /*String me = "我";
                 int k = start + 1;
                 Character character = Character.valueOf(spannable.charAt(k));*/
                //if (me.equals(character))
            } else {
                if (end-start<=2) {
                    break;
                }
            }

            String name=spannable.subSequence(start, end).toString();
            UrlClicker atClicker=new UrlClicker(context, name);
            spannable.setSpan(atClicker, start, end, 33);
        }
    }

    /**
     * 高亮且有点击功能
     *
     * @param spannable 需要处理的字符串
     * @param pattern   匹配模式
     * @param clicker   点击处理器，需要与匹配模式对应。
     */
    public static void highlightContentClickable(Spannable spannable, Pattern pattern, MyClicker clicker) {
        Matcher atMatcher=pattern.matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            //WeiboLog.d("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
                /*String me = "我";
                 int k = start + 1;
                 Character character = Character.valueOf(spannable.charAt(k));*/
                //if (me.equals(character))
            } else {
                if (end-start<=2) {
                    break;
                }
            }

            /*ForegroundColorSpan colorSpan=new ForegroundColorSpan(color);
            spannable.setSpan(colorSpan, start, end, 34);*/

            String name=spannable.subSequence(start, end).toString();
            clicker.name=name;
            spannable.setSpan(clicker, start, end, 33);
        }
    }

    public static Pattern getAtPattern() {
        if (atpattern==null) {
            Object[] arrayOfObject=new Object[1];
            String str=getPunctuation();
            arrayOfObject[0]=str;
            atpattern=Pattern.compile(String.format("@[[^@\\s%s|(:| |：| |,|.|。|)]0-9]{1,20}", arrayOfObject), Pattern.MULTILINE);
        }
        return atpattern;
    }

    public static Pattern getAllPattern() {
        if (allPattern==null) {
            Object[] arrayOfObject=new Object[1];
            String str1=getPunctuation();
            arrayOfObject[0]=str1;
            String str2=String.valueOf(String.format("(@[[^@\\s%s]0-9]{1,20})|(", arrayOfObject));
            allPattern=Pattern.compile(str2+"http[s]*://[[[^/:]&&[a-zA-Z_0-9]]\\.]+(:\\d+)?(/[a-zA-Z_0-9]+)*(/[a-zA-Z_0-9]*([a-zA-Z_0-9]+\\.[a-zA-Z_0-9]+)*)?(\\?(&?[a-zA-Z_0-9]+=[%[a-zA-Z_0-9]-]*)*)*(#[[a-zA-Z_0-9]|-]+)?"+")|("+"#[^#]+?#"+"|"+"\\[(\\S+?)\\])");
        }
        return allPattern;
    }

    private static String getPunctuation() {
        return "`~!@#\\$%\\^&*()=+\\[\\]{}\\|/\\?<>,\\.:\\u00D7\\u00B7\\u2014-\\u2026\\u3001-\\u3011\\uFE30-\\uFFE5";
    }

    private static Pattern getSharpPattern() {
        if (sharppattern==null)
            sharppattern=Pattern.compile("#[^#]+?#");
        return sharppattern;
    }

    public static Pattern getWebPattern() {
        if (webpattern==null)
            webpattern=Pattern.compile("http[s]*://[[[^/:]&&[a-zA-Z_0-9]]\\.]+(:\\d+)?(/[a-zA-Z_0-9]+)*(/[a-zA-Z_0-9]*([a-zA-Z_0-9]+\\.[a-zA-Z_0-9]+)*)?(\\?(&?[a-zA-Z_0-9]+=[%[a-zA-Z_0-9]-]*)*)*(#[[a-zA-Z_0-9]|-]+)?");
        return webpattern;
    }

    /*public static Pattern getWebPattern2() {
     if (webpattern2 == null)
     webpattern2 = Pattern.compile("((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)");
     return webpattern2;
     }*/
    public static abstract class MyClicker extends ClickableSpan {

        /**
         * 作为高亮的名字
         */
        public String name;

        @Override
        public void updateDrawState(TextPaint textPaint) {
            textPaint.setColor(-15050856);
            textPaint.setUnderlineText(true);
        }

    }

    private static class AtClicker extends WeiboUtil.MyClicker {

        private Context mContext;
        private String nickName;

        public AtClicker(Context ctx, String name) {
            super();
            this.mContext=ctx;
            this.nickName=name;
        }

        public void onClick(View view) {
            WeiboLog.d("AtClicker:"+nickName);
            /*String nn=this.nickName;
            Intent intent=new Intent(mContext, UserFragmentActivity.class);//new Intent(mContext, UserActivity.class);
            intent.putExtra("nickName", nn);
            intent.putExtra("user_id", "");
            intent.putExtra("type", UserFragmentActivity.TYPE_USER_INFO);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);*/
        }

    }

    private static class TopicClicker extends WeiboUtil.MyClicker {

        private Context mContext;
        private String topic;

        public TopicClicker(Context context, String topicContent) {
            super();
            this.mContext=context;
            this.topic=topicContent;
        }

        public void onClick(View paramView) {
            Context localContext=this.mContext;
            /*Intent localIntent1 = new Intent(localContext, UserTopicAttentionList.class);
             String str = this.topic;
             Intent localIntent2 = localIntent1.putExtra("query", str);
             this.mContext.startActivity(localIntent1);*/
        }

    }

    private static class UrlClicker extends WeiboUtil.MyClicker {

        private static final String URL_PREX="http://t.sina.cn/dpool/ttt/sinaurlc.php?vt=3&u=%s&gsid=%s";
        private String gsid;
        private Context mContext;
        private String urlPath;

        public UrlClicker(Context context, String url) {
            super();
            this.mContext=context;
            this.urlPath=url;
        }

        public UrlClicker(Context context, String url, String paramString2) {
            super();
            this.mContext=context;
            this.urlPath=url;
            this.gsid=paramString2;
        }

        public void onClick(View paramView) {
            Object[] arrayOfObject=new Object[2];
            String str1=URLEncoder.encode(this.urlPath);
            arrayOfObject[0]=str1;
            String str2=this.gsid;
            arrayOfObject[1]=str2;
            String str3=String.format("http://t.sina.cn/dpool/ttt/sinaurlc.php?vt=3&u=%s&gsid=%s", arrayOfObject);
            str3=str1;
            openUrlByDefaultBrowser(this.mContext, str3);
        }
    }

    public static void openUrlByDefaultBrowser(Context context, String url) {
        try {
            Intent intent=new Intent("android.intent.action.VIEW");
            if ((url!=null)&&(!url.startsWith("http://"))&&(!url.startsWith("https://"))) {
                url="http://"+url;
            }
            Uri uri=Uri.parse(url);
            intent.setData(uri);
            context.startActivity(intent);
            return;
        } catch (ActivityNotFoundException ane) {
            WeiboLog.e("", ane.getMessage());
        }
    }

    public static List<Status> getStatusFromLocal(String path) {
        List<Status> list=null;
        try {
            String rs=parseInputStream(new FileInputStream(path));
            list=WeiboParser.parseStatuses(rs);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (WeiboException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public static List<Status> getStatusFromLocal(InputStream is) {
        List<Status> list=null;
        try {
            String rs=parseInputStream(is);
            list=WeiboParser.parseStatuses(rs);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (WeiboException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public static List<Status> getStatusFromLocal() {
        return getStatusFromLocal("/sdcard/user_time_line.json");
    }

    ///////////////////////////////
    //提供静态数据供无网络测试.

    /**
     * 保存随便看看
     *
     * @param s
     */
    public static void savePublicTimeLine(String s) {
        savePublicTimeLine(s, "/sdcard/");
    }

    /**
     * 保存随便看看,
     *
     * @param s内容
     * @param dir 保存文件路径
     */
    public static void savePublicTimeLine(String s, String dir) {
        saveStatus(s, dir, "public_time_line.json");
    }

    public static void saveUserTimeLine(String s) {
        saveStatus(s, "/sdcard/", "user_time_line.json");
    }

    /**
     * 保存文件
     *
     * @param s        内容
     * @param dir      目录
     * @param fileName 文件名
     */
    public static void saveStatus(String s, String dir, String fileName) {
        BufferedWriter fos=null;
        try {
            if (!dir.endsWith("/")) {
                dir+="/";
            }
            fos=new BufferedWriter(new FileWriter(dir+fileName, false));
            fos.write(s);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////--------------------------

    /**
     * 获取扩展名,带点号的,这里有一个问题就是当图片的url不是以png,gif,jpg这样结束的,
     * 没有后缀名,就无法获取对应的值,反而会以http://xx.cn/adf 取cn为扩展名.
     *
     * @return
     */
    public static String getExt(String uri) {
        /*String ext=".png";
        int dot=uri.lastIndexOf('.');
        if ((dot>-1)&&(dot<(uri.length()-1))) {
            ext=uri.substring(dot);
        }
        return ext;*/
        String ext=".png";
        if (uri.endsWith("gif")) {
            ext=".gif";
        } else if (uri.endsWith(".jpg")) {
            ext=".jpg";
        }

        return ext;
    }

    public static Map<String, String> categoryMap=null;

    {
        categoryMap=new HashMap<String, String>();
        categoryMap.put("default", "人气关注");
        categoryMap.put("ent", "影视名星");
        categoryMap.put("music", "音乐");
        categoryMap.put("fashion", "时尚");
        categoryMap.put("literature", "文学");
        categoryMap.put("business", "商界");
        categoryMap.put("sports", "体育");
        categoryMap.put("health", "健康");
        categoryMap.put("auto", "汽车");
        categoryMap.put("house", "房产");
        categoryMap.put("trip", "旅行");
        categoryMap.put("stock", "炒股");
        categoryMap.put("food", "美食");
        categoryMap.put("fate", "命理");
        categoryMap.put("art", "艺术");
        categoryMap.put("tech", "科技");
        categoryMap.put("cartoon", "动漫");
        categoryMap.put("games", "游戏");
    }

    /**
     * 处理注销用户清除所有的设置。
     *
     * @param context
     */
    public static final void logout(Context context) {
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);

        long currentUserId=preferences.getLong(Constants.PREF_CURRENT_USER_ID, -1);

        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(Constants.PREF_USERNAME_KEY);
        // add 存储当前用户的id
        editor.remove(Constants.PREF_CURRENT_USER_ID);
        editor.remove(Constants.PREF_TIMESTAMP);
        editor.remove(Constants.PREF_TOKEN);
        editor.remove(Constants.PREF_SECRET);
        editor.remove(Constants.PREF_SCREENNAME_KEY);
        editor.remove(Constants.PREF_FOLLWWERCOUNT_KEY);
        editor.remove(Constants.PREF_FRIENDCOUNT_KEY);
        editor.remove(Constants.PREF_FAVOURITESCOUNT_KEY);
        editor.remove(Constants.PREF_STATUSCOUNT_KEY);
        editor.remove(Constants.PREF_TOPICCOUNT_KEY);
        editor.remove(Constants.PREF_PORTRAIT_URL);
        editor.remove(Constants.PREF_NEES_TO_UPDATE);

        //清除未读消息
        editor.remove(Constants.PREF_SERVICE_STATUS);
        editor.remove(Constants.PREF_SERVICE_COMMENT);
        editor.remove(Constants.PREF_SERVICE_FOLLOWER);
        editor.remove(Constants.PREF_SERVICE_AT);
        editor.remove(Constants.PREF_SERVICE_AT_COMMENT);
        editor.remove(Constants.PREF_SERVICE_DM);

        //清除认证
        //editor.remove(Constants.PREF_SOAUTH_TYPE);

        editor.commit();
        //App.logout();

        //delete cache file
        /*String filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+Constants.TREND_FILE;
        filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+String.valueOf(currentUserId)+Constants.GROUP_FILE;
        filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+String.valueOf(currentUserId)+Constants.USER_SELF_FILE;*/

        try {
            ContentResolver resolver=context.getContentResolver();
            //resolver.delete(TwitterTable.StatusTbl.CONTENT_URI, null, null);
            //清除当前用户的主页数据
            resolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID+"='"+currentUserId+"'", null);
            //清除当前用户的认证数据
            resolver.delete(TwitterTable.AUTbl.CONTENT_URI, TwitterTable.AUTbl.ACCOUNT_USERID+"='"+currentUserId+"'", null);
            //清除当前用户的@用户数据
            resolver.delete(TwitterTable.UserTbl.CONTENT_URI, TwitterTable.UserTbl.UID+"='"+currentUserId+"'", null);
            //清除当前用户的草稿数据
            resolver.delete(TwitterTable.DraftTbl.CONTENT_URI, TwitterTable.DraftTbl.UID+"='"+currentUserId+"'", null);
            //getContentResolver().delete(TwitterTable.AccountTbl.CONTENT_URI, null, null);
            //清除当前用户的队列数据
            resolver.delete(TwitterTable.SendQueueTbl.CONTENT_URI, TwitterTable.SendQueueTbl.USER_ID+"='"+currentUserId+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to determine if the device is running ICS or greater
     *
     * @return True if the device is running Ice Cream Sandwich or greater,
     *         false otherwise
     */
    public static final boolean hasICS() {
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean isHoneycombOrLater() {
        return android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Used to know if Apollo was sent into the background
     *
     * @param context The {@link Context} to use
     */
    public static final boolean isApplicationSentToBackground(final Context context) {
        final ActivityManager activityManager=(ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> tasks=activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            final ComponentName topActivity=tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
