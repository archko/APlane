package cn.archko.microblog.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;
/*import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;*/

/**
 * 修改后继承ThreadBeanItemView,多了一个Touch,左边的头像点击后的处理.
 *
 * @author: archko 11-8-24
 */
public class UserItemView extends LinearLayout implements View.OnClickListener {

    private static final String TAG="UserItemView";
    private Context mContext;
    private TextView status_content;    //微博的内容
    private Button followBtn;
    private ImageView mPortrait;    //微博作者头像
    private TextView mName;

    private String mPortraitUrl=null;
    private String mCacheDir;    //图片缓存目录
    private User user;    //微博
    View right;

    public static final int TYPE_PORTRAIT=1;
    private int followingType=-1;   //0表示未关注,1表示已关注,-1表示未知
    //protected DisplayImageOptions options;

    public UserItemView(Context context, boolean updateFlag) {
        super(context);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.friend_item, this);

        right=findViewById(R.id.right);
        status_content=(TextView) findViewById(R.id.status_content);
        followBtn=(Button) findViewById(R.id.follow_btn);
        mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        mName=(TextView) findViewById(R.id.tv_name);

        //setOnTouchListener(this);
        mPortrait.setOnClickListener(this);
        followBtn.setOnClickListener(this);

        mContext=context;
        mCacheDir=AppSettings.current().mCacheDir;

        /*options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();*/
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "id:"+id);
        }
        String imgUrl;
        /*if (mPortrait==view) {
            WeiboLog.d(TAG, "onClick:"+id);
            Intent intent=new Intent(mContext, UserFragmentActivity.class);//new Intent(mContext, UserActivity.class);
            intent.putExtra("nickName", user.screenName);
            intent.putExtra("user_id", user.id);
            intent.putExtra("type", UserFragmentActivity.TYPE_USER_INFO);
            mContext.startActivity(intent);
            return;
        }*/
        if (id==R.id.follow_btn) {
            //Toast.makeText(mContext, "not implemented.",Toast.LENGTH_SHORT).show();
            doFollow();
        }
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
                NotifyUtils.showToast("token过期了,处理失败,可以刷新列表重新获取token.", Toast.LENGTH_LONG);
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

                if (user.following) {
                    followingType=1;
                    followBtn.setText(R.string.unfollow);
                } else {
                    followingType=0;
                    followBtn.setText(R.string.follow);
                }
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
        if (null!=status) {
            status_content.setText(status.text);
        } else {
            status_content.setText(null);
        }

        if (user.following) {
            followingType=1;
            followBtn.setText(R.string.unfollow);
        } else {
            followingType=0;
            followBtn.setText(R.string.follow);
        }

        mPortraitUrl=user.profileImageUrl;
        //获取头像.
        Bitmap bitmap=ImageCache.getInstance(mContext).getBitmapFromMemCache(mPortraitUrl);
        if (null!=bitmap&&!bitmap.isRecycled()) {
            mPortrait.setImageBitmap(bitmap);
        } else {
            mPortrait.setImageResource(R.drawable.user_default_photo);
            if (updateFlag) {
                //DownloadPool.downloading.put(mPortraitUrl, new WeakReference<View>(parent));
                /*((App) App.getAppContext()).mDownloadPool.Push(
                    mHandler, mPortraitUrl, TYPE_PORTRAIT, cache, mCacheDir+Constants.ICON_DIR, mPortrait);*/
                /*ImageLoader imageLoader=ImageLoader.getInstance();
                imageLoader.displayImage(mPortraitUrl, mPortrait, options);*/
                ApolloUtils.getImageFetcher(mContext).startLoadImage(mPortraitUrl, mPortrait);
            }
        }
    }

    Handler mHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /*Bundle bundle=msg.getData();

            String imgUrl=(String) msg.obj;
            WeiboLog.d(TAG, "imgUrl:"+imgUrl+" bundle:"+bundle);
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

                ListView listView=(ListView) viewWeakReference.get();

                //从第一个可见的项开始更新.
                int childCount=listView.getChildCount();
                if (0>=childCount) {
                    DownloadPool.downloading.remove(imgUrl);
                    WeiboLog.i(TAG, "listview has no children.");
                    return;
                }

                UserItemView itemView;
                View view;
                for (int i=0; i<childCount; i++) {
                    view=listView.getChildAt(i);
                    if (view instanceof UserItemView) {
                        itemView=(UserItemView) view;
                        if (itemView.mPortraitUrl!=null&&itemView.mPortraitUrl.equals(imgUrl)) {
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