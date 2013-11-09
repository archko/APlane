package cn.archko.microblog.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.FragmentManager;
import android.support.v4.view.LazyViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.me.microblog.WeiboUtil;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;
import cn.archko.microblog.view.AKSnapImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * @description:
 * @author: archko 13-9-22 :上午10:27
 */
public class ImageViewerActivity extends SkinFragmentActivity {

    private LazyViewPager mViewPager;
    SamplePagerAdapter mPagerAdapter;
    String[] mUrls;
    int mSelectedIdx;
    ImageView mSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();

        if (null==getIntent()) {
            WeiboLog.e("null==getIntent");
            AKUtils.showToast("null==getIntent");
            return;
        }
        mUrls=getIntent().getStringArrayExtra("thumbs");
        mSelectedIdx=getIntent().getIntExtra("pos", 0);
        WeiboLog.d("mSelectedIdx:"+mSelectedIdx);

        if (null==mUrls) {
            WeiboLog.e("null==url");
            AKUtils.showToast("null==url");
            return;
        }

        setContentView(R.layout.imageviewer);
        mViewPager=(LazyViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(0);
        mSave=(ImageView) findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        mPagerAdapter=new SamplePagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mSelectedIdx);

    }

    private void saveImage() {
        AKSnapImageView imageView=mPagerAdapter.getAKSnapImageView(mViewPager.getCurrentItem());
        if (null!=imageView) {
            if (!imageView.isImageDownloaded()) {
                WeiboLog.d("正在下载中...");
                AKUtils.showToast("正在下载中...");
                return;
            }
            String path=imageView.getBmidPath();
            if (!TextUtils.isEmpty(path)) {
                File file=new File(path);
                if (file.exists()) {
                    WeiboLog.d("保存文件:"+path);
                    new SaveImageTask().execute(path);
                } else {
                    WeiboLog.d("保存失败."+path);
                    AKUtils.showToast("保存失败.");
                }
            } else {
                WeiboLog.d("保存失败,路径为空.");
                AKUtils.showToast("保存失败.");
            }
        } else {
            WeiboLog.d("当前的view是空的,无法 保存.");
        }
    }

    class SamplePagerAdapter extends PagerAdapter {

        private final SparseArray<WeakReference<AKSnapImageView>> mFragmentArray=new SparseArray<WeakReference<AKSnapImageView>>();

        public SamplePagerAdapter(FragmentManager fm) {
            super();
        }

        @Override
        public int getCount() {
            return mUrls.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            AKSnapImageView itemView=null;
            String bean=mUrls[position];
            final WeakReference<AKSnapImageView> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                itemView=mWeakFragment.get();
                itemView.update(bean);
                container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return itemView;
            }

            itemView=new AKSnapImageView(ImageViewerActivity.this, bean);
            itemView.update(bean);
            mFragmentArray.put(position, new WeakReference<AKSnapImageView>(itemView));
            container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            final WeakReference<AKSnapImageView> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null) {
                mWeakFragment.clear();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        public AKSnapImageView getAKSnapImageView(int position) {
            AKSnapImageView itemView=null;
            final WeakReference<AKSnapImageView> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                itemView=mWeakFragment.get();
            }
            return itemView;
        }
    }

    public class SaveImageTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            boolean flag=false;
            String targetFilePath=null;
            try {
                String path=(String) params[0];
                targetFilePath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/image"+
                    DateUtils.formatDate(new Date(), "yyyy-MM-dd_HH-mm-ss")+WeiboUtil.getExt(path);
                flag=ImageCache2.getInstance().getImageManager().copyFileToFile(targetFilePath, path);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return flag ? targetFilePath : null;
        }

        protected void onPostExecute(String bitmap) {
            if (!isFinishing()) {
                if (bitmap!=null) {
                    AKUtils.showToast("保存成功:"+bitmap, Toast.LENGTH_LONG);
                } else {
                    AKUtils.showToast("保存失败:");
                }
            }
        }
    }
}
