package cn.archko.microblog.sliding.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.AboutAppFragment;
import cn.archko.microblog.fragment.AtMeCommentsFragment;
import cn.archko.microblog.fragment.AtmeFragment;
import cn.archko.microblog.fragment.CommentsFragment;
import cn.archko.microblog.fragment.DirectMessageFragment;
import cn.archko.microblog.fragment.HomeFragment;
import cn.archko.microblog.fragment.HomeGridFragment;
import cn.archko.microblog.fragment.MyFavFragment;
import cn.archko.microblog.fragment.MyPostFragment;
import cn.archko.microblog.fragment.PrefsFragment;
import cn.archko.microblog.fragment.ProfileFragment;
import cn.archko.microblog.fragment.PublicFragment;
import cn.archko.microblog.fragment.TaskListFragment;
import cn.archko.microblog.fragment.UserFollowersGridFragment;
import cn.archko.microblog.fragment.UserFriendsGridFragment;
import cn.archko.microblog.fragment.place.PlaceFriendsFragment;
import cn.archko.microblog.fragment.place.PlaceNearbyPhotosFragment;
import cn.archko.microblog.fragment.place.PlaceNearbyPhotosGridFragment;
import cn.archko.microblog.fragment.place.PlaceNearbyUsersFragment;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko
 */
public class SidebarAdapter extends BaseAdapter {

    public final static String TAG="SidebarAdapter";

    public static class SidebarEntry {

        public String id;   //标识
        public String name; //显示的名称
        int drawableID; //显示的图片
        public Class<?> clazz; //Fragment类
        boolean isHome; //是否是上页
        String msg; //显示的消息
        /**
         * 添加的Fragment类型，如果是Intent导航的类型，需要处理为跳转，而不是构造一个Fragment
         * Fragment导航类型为0
         * Intent导航类型为1
         */
        public int navType=0;
        public static final int NAV_TYPE_FRAGMENT=0;
        public static final int NAV_TYPE_INTENT=1;

        public SidebarEntry(String _id, String _name, int _drawableID, Class<?> clazz, boolean isHome) {
            this.id=_id;
            this.name=_name;
            this.drawableID=_drawableID;
            this.clazz=clazz;
            this.isHome=isHome;
            msg="";
        }

        public SidebarEntry(String _id, String _name, int _drawableID, Class<?> clazz, boolean isHome, int type) {
            this(_id, _name, _drawableID, clazz, isHome);
            this.navType=type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg=msg;
        }
    }

    private LayoutInflater mInflater;
    private FragmentManager mFragmentManager;
    Context mContext;
    private List<SidebarEntry> entries;
    //private HashMap<String, Fragment> mFragments;
    private final SparseArray<WeakReference<Fragment>> mFragmentArray=new SparseArray<WeakReference<Fragment>>();

    public SidebarAdapter(FragmentManager fm, Context ctx) {
        mInflater=LayoutInflater.from(App.getAppContext());
        mFragmentManager=fm;
        mContext=ctx;
        entries=new ArrayList<SidebarEntry>();
        //mFragments=new HashMap<String, Fragment>();
    }

    public void addEntry(SidebarEntry entry, boolean init) {
        entries.add(entry);
        if (entry.isHome&&init) {
            getFragment(entry, entries.size()-1);
        }
    }

    void onInitFinished() {
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SidebarEntry sidebarEntry=entries.get(position);

        SidebarItemView itemView=null;
        if (null==convertView) {
            itemView=new SidebarItemView(mContext);
        } else {
            itemView=(SidebarItemView) convertView;
        }

        itemView.update(sidebarEntry);

        return itemView;
    }

    private class SidebarItemView extends LinearLayout implements Checkable {

        private TextView mTitle;
        private TextView mMsg;    //
        private ImageView mIcon;    //
        private ImageView mLeftSlider;

        private SidebarItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.sidebar_item, this);
            mTitle=(TextView) findViewById(R.id.title);
            mMsg=(TextView) findViewById(R.id.msg);
            mIcon=(ImageView) findViewById(R.id.image);
            int pref_sidebar_color=PreferenceUtils.getInstace(App.getAppContext()).getDefaultSidebarThemeColor(App.getAppContext());
            mTitle.setTextColor(pref_sidebar_color);
            mLeftSlider=(ImageView) findViewById(R.id.left_slider);
        }

        public void update(final SidebarEntry entry) {
            mTitle.setText(entry.name);
            if ("0".equals(entry.msg)||"".equals(entry.msg)) {
                mMsg.setVisibility(GONE);
            } else {
                mMsg.setVisibility(VISIBLE);
                mMsg.setText(entry.msg);
            }
            mIcon.setImageResource(entry.drawableID);
        }

        private boolean checked=false;

        @Override
        public boolean isChecked() {
            return checked;
        }

        @Override
        public void setChecked(boolean aChecked) {
            if (checked==aChecked) {
                return;
            }
            checked=aChecked;
            setIndicatorVisible(checked);
        }

        public void setIndicatorVisible(boolean checked) {
            setBackgroundResource(checked ? R.drawable.abs__list_focused_holo : android.R.color.transparent);
            mLeftSlider.setVisibility(checked ? VISIBLE : INVISIBLE);
        }

        @Override
        public void toggle() {
            setChecked(!checked);
        }
    }

    /**
     * 根据id(标签)获取Fragment位置.
     *
     * @param id
     * @return
     */
    public int getFragmentPos(String id) {
        int index=0;
        for (SidebarEntry entry : entries) {
            if (entry.id.equals(id)) {
                break;
            }
            index++;
        }
        WeiboLog.d(TAG, "index:"+index+" id:"+id);
        return index;
    }

    /**
     * 根据SidebarEntry获取Fragment
     *
     * @param entry
     * @return
     */
    /*public Fragment getFragment(SidebarEntry entry) {
        if (mFragments.containsKey(entry.id)&&mFragments.get(entry.id)!=null) {
            return mFragments.get(entry.id);
        }

        Fragment f=null;
        try {
            f=(Fragment) entry.clazz.newInstance();
            f.setRetainInstance(true);
            mFragmentManager.beginTransaction()
                .add(R.id.fragment_placeholder, f, entry.id)
                .commitAllowingStateLoss();
            mFragments.put(entry.id, f);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return f;
    }*/

    /**
     * 根据SidebarEntry获取Fragment
     *
     * @param entry
     * @return
     */
    public Fragment getFragment(SidebarEntry entry, int position) {
        final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
        WeiboLog.d(TAG, "getFragment:"+position+" weakFragment:"+mWeakFragment);
        if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
            return mWeakFragment.get();
        }

        Fragment f=null;
        try {
            f=(Fragment) entry.clazz.newInstance();
            f.setRetainInstance(true);
            mFragmentManager.beginTransaction()
                .add(R.id.fragment_placeholder, f, entry.id)
                .commitAllowingStateLoss();
            mFragmentArray.put(position, new WeakReference<Fragment>(f));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return f;
    }

    //--------------------- fragment ---------------------

    /**
     * 添加侧边栏的导航Fragment
     */
    public int addFragment(boolean init) {
        entries.clear();
        int theme=0;
        SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(mContext);
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {//if ("2".equals(themeId)) {
            theme=2;
        }

        //主页
        int home=0;
        SidebarAdapter.SidebarEntry entry;
        int drawableId=R.drawable.tab_home_light;
        if (theme==2) {
            drawableId=R.drawable.tab_home_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_HOME, mContext.getString(R.string.tab_label_home),
            drawableId, HomeGridFragment.class, true);
        addEntry(entry, init);
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_HOME, mContext.getString(R.string.tab_label_home)+"2",
            drawableId, HomeFragment.class, false);
        addEntry(entry, init);

        //热门数据，包含热门用户与精选微博, api过期了。
        /*entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_HOT, getString(R.string.tab_label_hot),
            R.drawable.tab_other, HotFragmentActivity.class, false, SidebarAdapter.SidebarEntry.NAV_TYPE_INTENT);
        mSidebarAdapter.addEntry(entry);*/

        /*entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_HOT_REPOST, getString(R.string.tab_label_hot_repost),
            R.drawable.tab_other, HotRepostFragment.class, false);
        mSidebarAdapter.addEntry(entry);

        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_HOT_COMMENT, getString(R.string.tab_label_hot_comment),
            R.drawable.tab_other, HotCommentFragment.class, false);
        mSidebarAdapter.addEntry(entry);*/

        //我发布的微博
        boolean show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_MY_POST, true);
        if (show) {
            if (theme==0) {
                drawableId=R.drawable.tab_expression_light;
            } else if (theme==2) {
                drawableId=R.drawable.tab_expression_light;
            }
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_MY_POST, mContext.getString(R.string.tab_label_my_post),
                drawableId, MyPostFragment.class, false);
            addEntry(entry, init);
        }

        //评论时间线，是我收到的评论
        if (theme==0) {
            drawableId=R.drawable.tab_other_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_other_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_COMMENT, mContext.getString(R.string.tab_label_comment_timeline),
            drawableId, CommentsFragment.class, false);
        addEntry(entry, init);

        //我的关注列表
        if (theme==0) {
            drawableId=R.drawable.tab_other_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_other_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_FRIEND, mContext.getString(R.string.tab_label_friends),
            drawableId, UserFriendsGridFragment.class, false);
        addEntry(entry, init);

        //我的粉丝
        if (theme==0) {
            drawableId=R.drawable.tab_other_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_other_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_FOLLOWER, mContext.getString(R.string.tab_label_followers),
            drawableId, UserFollowersGridFragment.class, false);
        addEntry(entry, init);

        //@我的微博
        if (theme==0) {
            drawableId=R.drawable.tab_at_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_at_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_AT_STATUS, mContext.getString(R.string.tab_label_at_status),
            drawableId, AtmeFragment.class, false);
        addEntry(entry, init);

        //@我的评论
        if (theme==0) {
            drawableId=R.drawable.tab_at_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_at_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_AT_COMMENT, mContext.getString(R.string.tab_label_at_comment),
            drawableId, AtMeCommentsFragment.class, false);
        addEntry(entry, init);

        //发送队列
        if (theme==0) {
            drawableId=R.drawable.tab_profile_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_profile_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_SEND_TASK, mContext.getString(R.string.tab_label_send_task),
            drawableId, TaskListFragment.class, false);
        addEntry(entry, init);

        //随便看看
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_PUBLIC, true);
        if (show) {
            if (theme==0) {
                drawableId=R.drawable.tab_public_light;
            } else if (theme==2) {
                drawableId=R.drawable.tab_public_light;
            }
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_PUBLIC, mContext.getString(R.string.tab_label_public),
                drawableId, PublicFragment.class, false);
            addEntry(entry, init);
        }

        //个人资料
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_PROFILE, true);
        if (show) {
            if (theme==0) {
                drawableId=R.drawable.tab_profile_light;
            } else if (theme==2) {
                drawableId=R.drawable.tab_profile_light;
            }
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_PROFILE, mContext.getString(R.string.tab_label_profile),
                drawableId, ProfileFragment.class, false);
            addEntry(entry, init);
        }

        //我的收藏列表
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_MY_FAV, true);
        if (show) {
            if (theme==0) {
                drawableId=R.drawable.tab_fav_light;
            } else if (theme==2) {
                drawableId=R.drawable.tab_fav_light;
            }
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_MY_FAV, mContext.getString(R.string.tab_label_my_fav),
                drawableId, MyFavFragment.class, false);
            addEntry(entry, init);
        }

        //附近的照片
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_PLACE_NEARBY_PHOTOS, false);
        if (show) {
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_PLACE_NEARBY_PHOTOS, mContext.getString(R.string.tab_label_place_nearby_photos),
                R.drawable.location, PlaceNearbyPhotosFragment.class, false);
            addEntry(entry, init);

            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_PLACE_NEARBY_PHOTOS, mContext.getString(R.string.tab_label_place_nearby_photos)+"2",
                R.drawable.location, PlaceNearbyPhotosGridFragment.class, false);
            addEntry(entry, init);
        }

        //附近的用户
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_PLACE_NEARBY_USERS, false);
        if (show) {
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_PLACE_NEARBY_USERS, mContext.getString(R.string.tab_label_place_nearby_users),
                R.drawable.location, PlaceNearbyUsersFragment.class, false);
            addEntry(entry, init);
        }

        //位置时间线
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_PLACE_FRIEND_TIMELINE, false);
        if (show) {
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_PLACE_FRIEND_TIMELINE, mContext.getString(R.string.tab_label_place_friend_timeline),
                R.drawable.location, PlaceFriendsFragment.class, false);
            addEntry(entry, init);
        }

        //私信
        show=mPrefs.getBoolean(PreferenceUtils.PREF_SIDEBAR_DM, false);
        if (show) {
            if (theme==0) {
                drawableId=R.drawable.tab_profile_light;
            } else if (theme==2) {
                drawableId=R.drawable.tab_profile_light;
            }
            entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_DIRECT_MSG, mContext.getString(R.string.tab_label_direct_msg),
                drawableId, DirectMessageFragment.class, false);
            addEntry(entry, init);
        }

        if (theme==0) {
            drawableId=R.drawable.tab_profile_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_profile_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_ABOUT, mContext.getString(R.string.tab_label_about),
            drawableId, AboutAppFragment.class, false);
        addEntry(entry, init);

        //设置
        if (theme==0) {
            drawableId=R.drawable.tab_setting_light;
        } else if (theme==2) {
            drawableId=R.drawable.tab_setting_light;
        }
        entry=new SidebarAdapter.SidebarEntry(Constants.TAB_ID_SETTINGS, mContext.getString(R.string.tab_label_settings),
            drawableId, PrefsFragment.class, false);
        addEntry(entry, init);

        return home;
    }
}
