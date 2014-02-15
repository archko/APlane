package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Status;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-11-17
 */
public class TestFragment extends AbstractBaseFragment {

    public static final String TAG="TestFragment";

    boolean isLoading=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.d(TAG, "onCreate:"+this);
    }

    ArrayList<Status> getStatuses(Long sinceId, Long maxId, int c, int p) throws WeiboException {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        WeiboLog.d(TAG, "onCreateView:"+this);

        View view=inflater.inflate(R.layout.ak_test_view, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
