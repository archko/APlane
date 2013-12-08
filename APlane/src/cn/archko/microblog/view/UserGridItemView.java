package cn.archko.microblog.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

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

    private AbsListView parent;
    private String portraitUrl=null;
    private String mCacheDir;    //图片缓存目录
    private User user;    //微博

    public static final int TYPE_PORTRAIT=1;
    private int followingType=-1;   //0表示未关注,1表示已关注,-1表示未知

    public UserGridItemView(Context context, AbsListView view, String cacheDir, User user, boolean updateFlag) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.gridview_items, this);

        mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        mName=(TextView) findViewById(R.id.tv_name);
        mGridviewLineOne=(TextView) findViewById(R.id.gridview_line_one);
        mGridviewLineTwo=(TextView) findViewById(R.id.gridview_line_two);

        parent=view;
        mContext=context;
        mCacheDir=cacheDir;

        //update(status, updateFlag);
    }

    private void doFollow() {
        WeiboLog.i(TAG, "follow listener:"+followingType);
        if (followingType==-1) {
            WeiboLog.i(TAG, "doFollow.followingType=-1.");
            return;
        }

        if (App.OAUTH_MODE.equalsIgnoreCase(Constants.SOAUTH_TYPE_CLIENT)) {
            FollwingTask follwingTask=new FollwingTask();
            follwingTask.execute(new Integer[]{followingType});
        } else {
            App app=(App) App.getAppContext();
            if (System.currentTimeMillis()>=app.oauth2_timestampe&&app.oauth2_timestampe!=0) {
                WeiboLog.d(TAG, "web认证，token过期了.");
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
        Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(portraitUrl);
        if (null!=bitmap&&!bitmap.isRecycled()) {
            mPortrait.setImageBitmap(bitmap);
        } else {
            mPortrait.setImageResource(R.drawable.user_default_photo);
            if (updateFlag) {
                //DownloadPool.downloading.put(portraitUrl, new WeakReference<View>(parent));
                ((App) App.getAppContext()).mDownloadPool.Push(
                    mHandler, portraitUrl, TYPE_PORTRAIT, cache, mCacheDir+Constants.ICON_DIR, mPortrait);

            }
        }
    }

    Handler mHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /*Bundle bundle=msg.getData();

            String imgUrl=(String) msg.obj;
            WeiboLog.v(TAG, "imgUrl:"+imgUrl+" bundle:"+bundle);
            Bitmap bitmap;//=BitmapFactory.decodeFile(bundle.getString("name"));
            bitmap=bundle.getParcelable("name");
            if (bitmap!=null&&!bitmap.isRecycled()) {
                ImageCache2.getInstance().addBitmapToMemCache(imgUrl, bitmap);

                WeakReference<View> viewWeakReference=DownloadPool.downloading.get(imgUrl);

                if (null==viewWeakReference||viewWeakReference.get()==null) {
                    DownloadPool.downloading.remove(imgUrl);
                    WeiboLog.i(TAG, "listview is null:"+imgUrl);
                    return;
                }

                AbsListView listView=(AbsListView) viewWeakReference.get();

                //从第一个可见的项开始更新.
                int childCount=listView.getChildCount();
                if (0>=childCount) {
                    DownloadPool.downloading.remove(imgUrl);
                    WeiboLog.i(TAG, "listview has no children.");
                    return;
                }

                UserGridItemView itemView;
                View view;
                for (int i=0; i<childCount; i++) {
                    view=listView.getChildAt(i);
                    if (view instanceof UserGridItemView) {
                        itemView=(UserGridItemView) view;
                        if (itemView.portraitUrl!=null&&itemView.portraitUrl.equals(imgUrl)) {
                            itemView.mPortrait.setImageBitmap(bitmap);
                            break;
                        }
                    }
                }
            } else {
                WeiboLog.d(TAG, "bitmap is null:"+imgUrl);
            }
            DownloadPool.downloading.remove(imgUrl);*/
        }
    };
}
