package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.fragment.impl.SinaPublucStatusImpl;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.me.microblog.bean.Status;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 随便看看
 * @author: archko 11-11-17
 */
public class PublicFragment extends StatusListFragment {

    public static final String TAG="PublicFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusImpl=new SinaPublucStatusImpl();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /*if (null!=mDataList&&mDataList.size()>weibo_count) {
            List list=mDataList.subList(0, weibo_count);
            mDataList.clear();
            mDataList.addAll(list);
        }*/
    }

    //--------------------- 数据加载 ---------------------
    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        int count=mAdapter.getCount();
        if (count<1) {
            WeiboLog.w(TAG, "no other data.");
            return;
        }

        boolean isRefresh=false;
        if (count>=weibo_count*3) {   //refresh list
            isRefresh=true;
        }
        Status st;
        st=(Status) mAdapter.getItem(mAdapter.getCount()-1);
        fetchData(-1, st.id, isRefresh, false);
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为false,其它的不缓存,比如随便看看.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //WeiboLog.d(TAG, "getView.pos:"+position+" getCount():"+getCount()+" lastItem:");

        ThreadBeanItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new ThreadBeanItemView(getActivity(), mListView, mCacheDir, status, updateFlag, false, showLargeBitmap, showBitmap);
        } else {
            itemView=(ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, false, showLargeBitmap, showBitmap);

        return itemView;
    }
}
