package com.me.microblog.util;

import com.me.microblog.util.HanziToPinyin.Token;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-11-29
 */
public class PinYin {

    //汉字返回拼音，字母原样返回，都转换为小写(默认取得的拼音全大写)
    public static String getPinYin(String input) {
        /*int len=input.length();
        for (int i=0;i<len;i++) {
            System.out.println(input.charAt(i));
        }
        System.out.println("input:"+len);*/

        /*Locale[] locales=Collator.getAvailableLocales();
        for (Locale l : locales) {
            //System.out.println(l.getDisplayName());
            if (l.equals(Locale.CHINA)) {
                System.out.println("l.equals(Locale.CHINA)");
            } else if (l.equals(Locale.CHINESE)) {
                System.out.println("l.equals(Locale.CHINESE)");
            }
        }*/

        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(input.trim());
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (Token token : tokens) {
                /*//System.out.println("token:"+token.target+" -- "+token.source+" - "+token.type);
                if (Token.PINYIN==token.type) {
                    //System.out.println("type");
                    sb.append(token.target+" ");
                } else {
                    //System.out.println("source");
                    sb.append(token.source+" ");
                }*/
                if (! token.target.equals(token.source)) {
                    //sb.append(token.target+" "+token.source+" ");
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }

        String rs = sb.toString();
        //System.out.println("rs:"+rs);
        return rs.toLowerCase();
    }

    public String getSortKey(String displayName) {
        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(displayName);
        if (tokens != null && tokens.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Token token : tokens) {
                // Put Chinese character's pinyin, then proceed with the
                // character itself.
                if (Token.PINYIN == token.type) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(token.target);
                    sb.append(' ');
                    sb.append(token.source);
                } else {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(token.source);
                }
            }
            return sb.toString();
        }

        //return super.getSortKey(displayName);
        return displayName;
    }

}
