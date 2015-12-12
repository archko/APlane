package cn.archko.microblog.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaDraftImpl;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.NewStatusActivity;
import com.me.microblog.App;
import com.me.microblog.bean.Draft;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 草稿管理Fragment，用Draft作为实体，只是有部分属性数据库有存储的。
 * 像这样的加载本地数据，需要覆盖loadData，直接调用newTask，否则本先判断网络。
 * 需要覆盖fetchMore，通常不是加载更多，这里采取一次性加载。
 * 需要覆盖showMoreView，展示底部的footerview不同的内容
 * 如果是静态数据，不需要刷新的，需要修改onCreateView，将ListView设置成下拉刷新失效的。
 * 覆盖basePostOperation方法，因为它与网络数据相关，而且当数据为空时，会在footerview中显示
 * @author: archko 12-10-17
 */
public class DraftListFragment extends AbstractLocalListFragment<Draft> {

    public static final String TAG="DraftListFragment";
    public static final int GET_DRAFT=1;
    /**
     * 当前的模式，如果为get_draft，就是获取数据用的。
     */
    int mode=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getActivity().getIntent();
        mode=intent.getIntExtra("mode", 0);
        /*if (mode==GET_DRAFT) {  //在获取数据时，不能再有编辑操作。
            mQuickAction.setActionItemVisible(View.GONE, 0);
            mQuickAction.setActionItemVisible(View.GONE, 1);
        }*/
        //mStatusImpl=new SinaDraftImpl();
    }

    public void initApi() {
        mStatusImpl=new SinaDraftImpl();
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        WeiboLog.v(TAG, "showMoreView");

        super.showMoreView();
        mMoreProgressBar.setVisibility(View.GONE);
        mMoreTxt.setText(R.string.more_add_status);
    }

    /**
     * 添加新的帐户。
     */
    @Override
    public void addNewData() {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "pull up to refresh.");
        }
        mSwipeLayout.setRefreshing(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=super.onCreateView(inflater, container, savedInstanceState);
        //mListView.setLockScrollWhileRefreshing(true);

        return root;
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为true,其它的不缓存,比如随便看看.
     *
     * @param convertView
     * @param parent
     * @param position
     * @param itemType
     * @return
     */
    @Override
    public View getView(SimpleViewHolder holder, final int position, int itemType) {
        View convertView=holder.baseItemView;
        DraftItemView itemView=null;
        Draft draft=mDataList.get(position);

        if (convertView==null) {
            itemView=new DraftItemView(getActivity());
        } else {
            itemView=(DraftItemView) convertView;
        }
        itemView.update(draft);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick(position, view);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPos=position;
                prepareMenu(up);
                return true;
            }
        });

        return itemView;
    }

    @Override
    public View newView(ViewGroup parent, int viewType) {
        //WeiboLog.d(TAG, "newView:" + parent + " viewType:" + viewType);
        DraftItemView itemView=null;
        itemView=new DraftItemView(getActivity());
        return itemView;
    }

    private class DraftItemView extends LinearLayout {

        private TextView mTitle;    //帐号名字，登录号。
        private TextView mMsg;    //
        private ImageView icon; //头像

        private DraftItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.draft_item, this);
            setMinimumHeight(40);
            mTitle=(TextView) findViewById(R.id.title);
            mMsg=(TextView) findViewById(R.id.msg);
            icon=(ImageView) findViewById(R.id.image);
        }

        public void update(String text1, String text2) {
            mTitle.setText(text1);
            mMsg.setText("粉丝："+text2);
        }

        public void update(Draft draft) {
            String content=draft.content;
            /*if (content.length()>45) {
                content=content.substring(0, 45)+"...";
            }*/
            mTitle.setText(content);
            mMsg.setText(DateUtils.longToDateTimeString(draft.createdAt));
        }
    }

    //--------------------- 微博操作 ---------------------

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_draft_edit);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_draft_delete);
    }

    @Override
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        if (mode==GET_DRAFT) {
            menuBuilder.getMenu().findItem(Constants.OP_ID_QUICK_REPOST).setVisible(false);
            menuBuilder.getMenu().findItem(Constants.OP_ID_COMMENT).setVisible(false);
        } else {
            menuBuilder.getMenu().findItem(Constants.OP_ID_QUICK_REPOST).setVisible(true);
            menuBuilder.getMenu().findItem(Constants.OP_ID_COMMENT).setVisible(true);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_QUICK_REPOST: {
                quickRepostStatus();
                break;
            }
            case Constants.OP_ID_COMMENT: {
                commentStatus();
                break;
            }
        }
        return false;
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(View achor) {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "mDataList.size():"+mDataList.size());
        }
        if (selectedPos>=mDataList.size()) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            }
            return;
        }

        try {
            Draft data=mDataList.get(selectedPos);
            if (mode==GET_DRAFT) {
                Intent intent=new Intent();
                intent.putExtra("draft", data);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            } else {
                Intent intent=new Intent(getActivity(), NewStatusActivity.class);
                intent.putExtra("draft", data);
                intent.setAction(Constants.INTENT_NEW_BLOG);
                getActivity().startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 快速转发，在这里是编辑草稿
     */
    public void quickRepostStatus() {
        if (selectedPos>=mDataList.size()) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            }
            return;
        }

        try {
            Draft data=mDataList.get(selectedPos);

            Intent intent=new Intent(getActivity(), NewStatusActivity.class);
            intent.putExtra("draft", data);
            intent.setAction(Constants.INTENT_NEW_BLOG);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面，在这里是删除草稿
     */
    public void commentStatus() {
        if (selectedPos>=mDataList.size()) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            }
            return;
        }

        try {
            Draft data=mDataList.get(selectedPos);

            int res=SqliteWrapper.deleteDraft(App.getAppContext(), data);
            if (res>0) {
                newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
            } else {
                NotifyUtils.showToast("删除失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
