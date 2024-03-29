package cn.archko.microblog.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.settings.AppSettings;
import com.andrew.apollo.cache.ImageCache;
import com.andrew.apollo.utils.ApolloUtils;
import com.me.microblog.App;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.oauth.Oauth2;
import com.me.microblog.util.WeiboLog;
/*import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;*/

/**
 * 修改后继承ThreadBeanItemView,多了一个Touch,左边的头像点击后的处理.
 *
 * @author: archko 11-8-24
 */
public class UserGridItemView extends LinearLayout {

    private static final String TAG="UserGridItemView";
    private Context mContext;
    private ImageView mPortrait;    //微博作者头像
    private TextView mName;
    private TextView mGridviewLineOne;
    private TextView mGridviewLineTwo;

    private String portraitUrl=null;
    private String mCacheDir;    //图片缓存目录
    private User user;    //微博

    public static final int TYPE_PORTRAIT=1;
    private int followingType=-1;   //0表示未关注,1表示已关注,-1表示未知
    //protected DisplayImageOptions options;

    public UserGridItemView(Context context, boolean updateFlag) {
        super(context);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ak_user_grid_item, this);

        mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        mName=(TextView) findViewById(R.id.tv_name);
        mGridviewLineOne=(TextView) findViewById(R.id.gridview_line_one);
        mGridviewLineTwo=(TextView) findViewById(R.id.gridview_line_two);

        mContext=context;
        mCacheDir=AppSettings.current().mCacheDir;

        //update(status, updateFlag);
        /*options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();*/
    }

    private void doFollow() {
        WeiboLog.i(TAG, "follow listener:"+followingType);
        if (followingType==-1) {
            WeiboLog.i(TAG, "doFollow.followingType=-1.");
            return;
        }

        App app=(App) App.getAppContext();
        if (app.getOauthBean().oauthType==Oauth2.OAUTH_TYPE_WEB) {
            FollwingTask follwingTask=new FollwingTask();
            follwingTask.execute(new Integer[]{followingType});
        } else {
            if (System.currentTimeMillis()>=app.getOauthBean().expireTime&&app.getOauthBean().expireTime!=0) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "web认证，token过期了.");
                }
                Toast.makeText(mContext, "token过期了,处理失败,可以刷新列表重新获取token.", Toast.LENGTH_LONG).show();
            } else {
                FollwingTask follwingTask=new FollwingTask();
                follwingTask.execute(new Integer[]{followingType});
            }
        }
    }

    class FollwingTask extends AsyncTask<Integer, Void, User> {

        @Override
        protected User doInBackground(Integer... params) {
            try {
                User now=null;
                SinaUserApi weiboApi2=new SinaUserApi();
                weiboApi2.updateToken();
                if (null==weiboApi2) {
                    return now;
                }

                if (followingType==0) {
                    now=weiboApi2.createFriendships(user.id);
                } else if (followingType==1) {
                    now=weiboApi2.deleteFriendships(user.id);
                }

                return now;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User resultObj) {
            if (resultObj==null) {
                Toast.makeText(mContext, "处理失败", Toast.LENGTH_LONG).show();
                WeiboLog.e(TAG, "can't not follow.");
                return;
            }

            try {

                if (user.id!=resultObj.id) {
                    WeiboLog.i(TAG, "用户项已经过期了.");
                    Toast.makeText(mContext, "处理成功!", Toast.LENGTH_LONG);
                    return;
                }

                boolean oldFollowing=user.following;
                user.following=resultObj.following;
                boolean newFollowing=user.following;

                if (oldFollowing==newFollowing) {
                    Toast.makeText(mContext, "unfollow "+user.screenName+" successfully!", Toast.LENGTH_LONG);
                } else {
                    Toast.makeText(mContext, "follow "+user.screenName+" successfully!", Toast.LENGTH_LONG);
                }

                /*if (user.isFollowing) {
                    followingType=1;
                    followBtn.setText(R.string.unfollow);
                } else {
                    followingType=0;
                    followBtn.setText(R.string.follow);
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*if (followingType==0) {
                followBtn.setText(R.string.unfollow);
                //setUnFollowingButton();
                Toast.makeText(mContext,"follow "+user.screenName+" successfully!",Toast.LENGTH_LONG);
            } else if (followingType==1) {
                followBtn.setText(R.string.follow);
                //setFollowingButton();
                Toast.makeText(mContext,"unfollow "+user.screenName+" successfully!",Toast.LENGTH_LONG);
            }*/
        }

    }

    public void update(final User bean, boolean updateFlag, boolean cache) {
        user=bean;
        mName.setText(user.screenName);
        Status status=user.status;
        /*if (user.isFollowing) {
            followingType=1;
            followBtn.setText(R.string.unfollow);
        } else {
            followingType=0;
            followBtn.setText(R.string.follow);
        }*/

        int followersCount=user.followersCount;
        int friendsCount=user.friendsCount;
        mGridviewLineOne.setText(String.valueOf(followersCount));
        mGridviewLineTwo.setText(String.valueOf(friendsCount));

        portraitUrl=user.profileImageUrl;
        String avatar_large=user.avatar_large;
        if (!TextUtils.isEmpty(avatar_large)) {
            portraitUrl=avatar_large;
        }

        //获取头像.
        Bitmap bitmap=ImageCache.getInstance(mContext).getBitmapFromMemCache(portraitUrl);
        if (null!=bitmap&&!bitmap.isRecycled()) {
            mPortrait.setImageBitmap(bitmap);
        } else {
            mPortrait.setImageResource(R.drawable.user_default_photo);
            if (updateFlag) {
                //DownloadPool.downloading.put(portraitUrl, new WeakReference<View>(parent));
                /*((App) App.getAppContext()).mDownloadPool.Push(
                    mHandler, portraitUrl, TYPE_PORTRAIT, cache, mCacheDir+Constants.ICON_DIR, mPortrait);*/
                /*ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(portraitUrl, mPortrait, options);*/
                ApolloUtils.getImageFetcher(mContext).startLoadImage(portraitUrl, mPortrait);
            }
        }
    }
}
