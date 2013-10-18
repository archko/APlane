package cn.archko.microblog.fragment;

import android.os.Bundle;
import cn.archko.microblog.fragment.impl.SinaUserFriendsImpl;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.SinaApiFactory;

/**
 * @version 1.00.00  用户的关注列表
 * @description:
 * @author: archko 11-11-17
 */
public class UserFriendsGridFragment extends UserGridFragment {

    public static final String TAG="UserFriendsGridFragment";

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaUserFriendsImpl();
    }

    public void initApi() {
        mStatusImpl=new SinaUserFriendsImpl();

        AbsApiFactory absApiFactory=new SinaApiFactory();
        mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.userApiFactory());
    }

    //--------------------- 数据加载 ---------------------
}
