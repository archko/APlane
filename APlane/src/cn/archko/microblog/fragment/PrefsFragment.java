package cn.archko.microblog.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.ui.AccountUserActivity;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.PrefsActivity;
import cn.archko.microblog.ui.SearchActivity;
import cn.archko.microblog.ui.StaggeredLocalActivity;
import cn.archko.microblog.utils.AKUtils;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 设置, 所有的设置从此开始
 * @author: archko 13-2-17
 */
public class PrefsFragment extends AbstractBaseFragment {

    public static final String TAG="PrefsFragment";
    public static final int MODE_EXIT=0;
    public static final int MODE_LOGOUT=1;
    int mode=MODE_EXIT;

    @Override
    public void postOauth(Object[] params) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        WeiboLog.v(TAG, "onCreateView:"+this);

        View view=inflater.inflate(R.layout.ak_settings, container, false);

        RelativeLayout layout=(RelativeLayout) view.findViewById(R.id.menu_account_user_manager);
        layout.setOnClickListener(clickListener);
        layout=(RelativeLayout) view.findViewById(R.id.menu_search);
        layout.setOnClickListener(clickListener);
        layout=(RelativeLayout) view.findViewById(R.id.menu_at_author);
        layout.setOnClickListener(clickListener);
        layout=(RelativeLayout) view.findViewById(R.id.menu_pref);
        layout.setOnClickListener(clickListener);
        layout=(RelativeLayout) view.findViewById(R.id.menu_logout);
        layout.setOnClickListener(clickListener);
        layout=(RelativeLayout) view.findViewById(R.id.menu_exit);
        layout.setOnClickListener(clickListener);
        layout=(RelativeLayout) view.findViewById(R.id.menu_cache);
        layout.setOnClickListener(clickListener);

        ThemeUtils.getsInstance().themeBackground(view, getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickMethod(view);
        }
    };

    private void clickMethod(View view) {
        int id=view.getId();
        if (id==R.id.menu_account_user_manager) {
            Intent intent=new Intent(getActivity(), AccountUserActivity.class);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
        } else if (id==R.id.menu_search) {
            Intent intent=new Intent(getActivity(), SearchActivity.class);
            getActivity().startActivity(intent);
        } else if (id==R.id.menu_home_user) {
        } else if (id==R.id.menu_at_author) {
            atStatus();
        } else if (id==R.id.menu_pref) {
            Intent intent=new Intent(getActivity(), PrefsActivity.class);
            getActivity().startActivity(intent);
        } else if (id==R.id.menu_logout) {
            mode=MODE_LOGOUT;
            exitConfirm(R.string.app_logout_title, R.string.app_logout_msg);
        } else if (id==R.id.menu_update) {
        } else if (id==R.id.menu_exit) {
            mode=MODE_EXIT;
            exitConfirm(R.string.exit_title, R.string.exit_msg);
        } else if (id==R.id.menu_cache) {
            Intent intent=new Intent(getActivity(), StaggeredLocalActivity.class);
            getActivity().startActivity(intent);
        }
    }

    /**
     * 反馈信息，也是发新微博
     */
    private void atStatus() {
        String atString=getString(R.string.feedback_at_name);
        Intent intent=new Intent(getActivity(), NewStatusActivity.class);
        intent.putExtra("at_some", atString);
        intent.setAction(Constants.INTENT_NEW_BLOG);
        startActivity(intent);
    }

    /**
     * 退出确认，有注销与退出程序确认
     *
     * @param title
     * @param msg
     */
    private void exitConfirm(int title, int msg) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(msg)
            .setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.confirm),
            new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.cancel();
                    if (mode==MODE_EXIT) {
                        AKUtils.exit(getActivity());
                    } else {
                        AKUtils.logout(getActivity());
                    }
                }
            }).create().show();
    }
}
