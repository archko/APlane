package cn.archko.microblog.fragment.impl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Favorite;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.SinaStatusApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 主页数据获取实现，有查询与网络数据
 */
public class SinaMyFavStatusImpl extends AbsStatusImpl<Favorite> {

    public static final String TAG="SinaMyFavStatusImpl";

    public SinaMyFavStatusImpl() {
        /*AbsApiImpl absApi=new SinaStatusApi();
        mAbsApi=absApi;*/
    }

    @Override
    public SStatusData<Favorite> loadData(Object... params) throws WeiboException {
        WeiboLog.d(TAG, "loadData.");
        SinaStatusApi sWeiboApi2=(SinaStatusApi) mAbsApi;
        SStatusData<Favorite> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Favorite>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            /*Long userId=(Long) params[1];
            Long sinceId=(Long) params[2];
            Long maxId=(Long) params[3];*/
            Integer c=(Integer) params[4];
            Integer p=(Integer) params[5];
            WeiboLog.d(/*"userId:"+userId+" sinceId:"+sinceId+", maxId:"+maxId+*/", count:"+c+", page:"+p);
            sStatusData=sWeiboApi2.myFavorites(c, p);
        }

        return sStatusData;
    }

    /**
     * 这里是针对登录用户的，存储的是刷新后的第一页。
     *
     * @param data
     */
    @Override
    public void saveData(SStatusData<Favorite> data) {
        try {
            ArrayList<Favorite> newList=data.mStatusData;
            if (null==newList||newList.size()<1) {
                WeiboLog.w(TAG, "no datas");
                return;
            }
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            String filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+currentUserId+Constants.MY_FAV_FILE;

            FileOutputStream fos=null;
            ObjectOutputStream out=null;
            fos=new FileOutputStream(filename);
            out=new ObjectOutputStream(fos);
            out.writeObject(newList);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        try {
            Long currentUserId=(Long) params[1];
            String filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+String.valueOf(currentUserId)+Constants.MY_FAV_FILE;
            File file=new File(filename);
            WeiboLog.d(TAG, "filename:"+filename+" file:"+file.exists());
            if (file.exists()) {
                FileInputStream fis=null;
                fis=new FileInputStream(filename);
                BufferedInputStream br=new BufferedInputStream(fis);
                ObjectInputStream in=new ObjectInputStream(br);
                ArrayList<Favorite> datas=(ArrayList<Favorite>) in.readObject();
                SStatusData<Favorite> sStatusData=new SStatusData<Favorite>();
                sStatusData.mStatusData=datas;
                return new Object[]{sStatusData, params};
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
