package cn.archko.microblog.sliding.app;

import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import cn.archko.microblog.R;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko
 */
public class SidebarMenuFragment extends ListFragment {

    private SidebarAdapter mSidebarAdapter;
    SlidingMenuChangeListener mMenuChangeListener;
    View mRoot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.sidebar, null);
        WeiboLog.d("SidebarMenuFragment", "onCreateView."+this);
        mRoot=view;
        themeBackground(false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setItemsCanFocus(false);

        if (null==mSidebarAdapter) {
            mSidebarAdapter=new SidebarAdapter(getActivity().getFragmentManager(), getActivity());
            mSidebarAdapter.addFragment(true);
        }
        setListAdapter(mSidebarAdapter);
    }

    public void setSidebarAdapter(SidebarAdapter mSidebarAdapter) {
        WeiboLog.d(getTag(), "sa:"+mSidebarAdapter.getCount());
        this.mSidebarAdapter=mSidebarAdapter;
        setListAdapter(mSidebarAdapter);
    }

    public void setMenuChangeListener(SlidingMenuChangeListener mMenuChangeListener) {
        this.mMenuChangeListener=mMenuChangeListener;
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        if (null!=mMenuChangeListener) {
            mMenuChangeListener.showMenu(position);
        }
    }

    public void selectItem(int postion){
        getListView().setItemChecked(getListView().getCheckedItemPosition(), false);
        getListView().setItemChecked(postion, true);
        mSidebarAdapter.notifyDataSetChanged();
    }

    public void themeBackground(boolean refresh) {
        if (null!=mRoot) {
            ThemeUtils.getsInstance().themeBackground(mRoot, getActivity());
        }
        if (refresh) {
            getListView().removeAllViewsInLayout();
            mSidebarAdapter.addFragment(false);
            mSidebarAdapter.notifyDataSetChanged();
        }
    }
}
