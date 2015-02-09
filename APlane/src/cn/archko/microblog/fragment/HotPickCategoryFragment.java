package cn.archko.microblog.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.fragment.abs.FragmentCallback;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 微博精选，类别有很多，可以用list导航来处理。 api过期了。
 * @author: archko 12-9-12
 */
@Deprecated
public class HotPickCategoryFragment extends AbstractBaseFragment {

    FragmentCallback mFragmentCallback;
    //String category=keys[0];
    /**
     * oauth2的热门用户分类也不同了。
     */
    static final String[] keys={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    static final String[] names={"娱乐", "搞笑", "美女", "视频", "星座", "各种萌", "时尚", "名车", "美食", "音乐"};

    protected ListView mListView;
    TimeLineAdapter mAdapter;

    @Override
    public void postOauth(Object[] params) {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        WeiboLog.d("onAttach:"+this);
        try {
            mFragmentCallback=(FragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement FragmentCallback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root=(RelativeLayout) inflater.inflate(R.layout.friend_list, null);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null==mAdapter) {
            mAdapter=new TimeLineAdapter();
        }

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(parent, view, position, id);
            }
        });
    }

    public void onListItemClick(AdapterView<?> parent, View v, int position, long id) {
        WeiboLog.i("Item clicked,position: "+position);
        selectedPos=position;
        viewUserHot();
    }

    /**
     * 查看推荐类型的用户
     */
    private void viewUserHot() {
        int pos=selectedPos;
        if (mListView.getHeaderViewsCount()>0) {
            pos--;
        }
        String name=keys[pos];
        WeiboLog.d("选中的分类："+pos+" type："+name);
        //mFragmentCallback.switchTab(name, HotFragmentActivity.TYPE_HOT_STATUS_PICK);
    }

    class TimeLineAdapter extends BaseAdapter {

        public TimeLineAdapter() {
        }

        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int i) {
            return names[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView==null) {
                convertView=((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                    inflate(android.R.layout.simple_list_item_1, null);

                holder=new ViewHolder();
                holder.text1=(TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView and the ImageView.
                holder=(ViewHolder) convertView.getTag();
            }

            holder.text1.setText(names[position]);

            return convertView;
        }

        class ViewHolder {

            TextView text1;
        }
    }
}
