package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;

/**
 * @version 1.00.00
 * @description: 空的Fragment, 用于详情页的手势.
 * @author: archko 13-12-9
 */
public class EmptyFragment extends AbstractBaseFragment {

    public static final String TAG="EmptyFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout root=new LinearLayout(getActivity());
        TextView textView=new TextView(getActivity());
        textView.setText("滑动关闭");
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity=Gravity.CENTER;
        root.addView(textView, lp);

        mRoot=root;
        themeBackground();
        return root;
    }

}
