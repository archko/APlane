package cn.archko.microblog.smiley;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 解析文本的表情
 *
 * @author archko
 */
public class AKTxtSmileyParser {

    // Singleton stuff
    private static AKTxtSmileyParser sInstance;

    public static AKTxtSmileyParser getInstance() {
        if (null==sInstance) {
            sInstance=new AKTxtSmileyParser();
        }
        return sInstance;
    }

    public static final HashMap<Integer, String[]> mSmileyMap=new HashMap<Integer, String[]>();

    private AKTxtSmileyParser() {
    }

    public String[] getSmileyTexts(Context ctx, int id) {
        if (mSmileyMap.get(id)==null) {
            InputStream is=null;
            ArrayList<String> smileyList=new ArrayList<String>();
            try {
                is=ctx.getResources().openRawResource(id);
                BufferedReader br=new BufferedReader(new InputStreamReader(is));
                String temp=null;
                temp=br.readLine();
                while (temp!=null) {
                    temp=br.readLine();
                    if (!TextUtils.isEmpty(temp)) {
                        smileyList.add(temp);
                    }
                }
                mSmileyMap.put(id, smileyList.toArray(new String[smileyList.size()]));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null!=is) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return mSmileyMap.get(id);
    }
}


