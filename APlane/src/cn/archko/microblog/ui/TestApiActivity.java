package cn.archko.microblog.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import cn.archko.microblog.fragment.TestFragment;
import cn.archko.microblog.fragment.place.PlaceNearbyPhotosGridFragment;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午8:07
 * @description:
 */
public class TestApiActivity extends ListActivity {

    ListView mListView;
    ArrayAdapter<ApiBean> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView=getListView();

        ArrayList<ApiBean> beans=new ArrayList<ApiBean>();
        addApi(PlaceNearbyPhotosGridFragment.class, beans);
        addApi(TestFragment.class, beans);

        mAdapter=new ArrayAdapter<ApiBean>(this, android.R.layout.simple_list_item_1, android.R.id.text1, beans);
        mListView.setAdapter(mAdapter);

    }

    private void addApi(Class clazz, ArrayList<ApiBean> beans) {
        ApiBean bean=new ApiBean(clazz);
        beans.add(bean);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ApiBean bean=mAdapter.getItem(position);
        initFragment(bean);
    }

    public void initFragment(ApiBean bean) {
        Intent loginIntent=new Intent(this, EmptyFragmentActivity.class);
        loginIntent.putExtra("title", bean.className);
        loginIntent.putExtra("fragment_class", bean.aClass.getName());
        loginIntent.putExtra("mode", "1");
        startActivity(loginIntent);
    }

    static class ApiBean {

        public String className;
        public Class aClass;

        ApiBean(Class aClass) {
            this.aClass=aClass;
            this.className=aClass.getSimpleName();
        }

        @Override
        public String toString() {
            return className;
        }
    }
}
