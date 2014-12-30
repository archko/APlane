package cn.archko.microblog.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.view.AKSnapImageView;
import com.me.microblog.WeiboUtils;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.StreamUtils;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * @description:
 * @author: archko 13-9-22 :上午10:27
 */
public class ImageViewerActivity extends Activity {

    private ViewPager mViewPager;
    SamplePagerAdapter mPagerAdapter;
    String[] mUrls;
    int mSelectedIdx;
    ImageView mSave;
    TextView mTxtPager;
    int mTotal = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();

        if (null == getIntent()) {
            WeiboLog.e("null==getIntent");
            NotifyUtils.showToast("null==getIntent");
            return;
        }
        mUrls = getIntent().getStringArrayExtra("thumbs");
        mSelectedIdx = getIntent().getIntExtra("pos", 0);
        WeiboLog.d("mSelectedIdx:" + mSelectedIdx);

        if (null == mUrls) {
            WeiboLog.e("null==url");
            NotifyUtils.showToast("null==url");
            return;
        }
        mTotal = mUrls.length;

        setContentView(R.layout.imageviewer);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(0);
        mSave = (ImageView) findViewById(R.id.save);
        mTxtPager = (TextView) findViewById(R.id.txt_pager);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        mPagerAdapter = new SamplePagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        //mViewPager.setCurrentItem(mSelectedIdx);
        mViewPager.setOnPageChangeListener(mPagerAdapter);

        if (mSelectedIdx != 0) {
            mViewPager.setCurrentItem(mSelectedIdx);
        }/* else {*/
        mViewPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                WeiboLog.d("onPreDraw:" + mSelectedIdx);
                mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);
                //if (mSelectedIdx==0) {
                mPagerAdapter.onPageSelected(mSelectedIdx);
                    /*} else {
                        mViewPager.setCurrentItem(mSelectedIdx);
                    }*/
                return true;
            }
        });
        //}
    }

    private void updatePager() {
        mTxtPager.setText((mSelectedIdx + 1) + "/" + mTotal);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void saveImage() {
        AKSnapImageView imageView = mPagerAdapter.getAKSnapImageView(mViewPager.getCurrentItem());
        if (null != imageView) {
            if (! imageView.isImageDownloaded()) {
                WeiboLog.d("正在下载中...");
                NotifyUtils.showToast("正在下载中...");
                return;
            }
            String path = imageView.getBmidPath();
            if (! TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (file.exists()) {
                    WeiboLog.d("保存文件:" + path);
                    new SaveImageTask().execute(path);
                } else {
                    WeiboLog.d("保存失败." + path);
                    NotifyUtils.showToast("保存失败.");
                }
            } else {
                WeiboLog.d("保存失败,路径为空.");
                NotifyUtils.showToast("保存失败.");
            }
        } else {
            WeiboLog.d("当前的view是空的,无法 保存.");
        }
    }

    class SamplePagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

        private final SparseArray<WeakReference<AKSnapImageView>> mFragmentArray = new SparseArray<WeakReference<AKSnapImageView>>();

        public SamplePagerAdapter(FragmentManager fm) {
            super();
        }

        @Override
        public int getCount() {
            return mUrls.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            AKSnapImageView itemView = null;
            String bean = mUrls[ position ];
            final WeakReference<AKSnapImageView> mWeakFragment = mFragmentArray.get(position);
            if (mWeakFragment != null && mWeakFragment.get() != null) {
                itemView = mWeakFragment.get();
                itemView.update(bean);
                container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return itemView;
            }

            itemView = new AKSnapImageView(ImageViewerActivity.this, bean);
            itemView.update(bean);
            itemView.loadThumb();
            mFragmentArray.put(position, new WeakReference<AKSnapImageView>(itemView));
            container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            final WeakReference<AKSnapImageView> mWeakFragment = mFragmentArray.get(position);
            if (mWeakFragment != null) {
                mWeakFragment.clear();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public AKSnapImageView getAKSnapImageView(int position) {
            AKSnapImageView itemView = null;
            final WeakReference<AKSnapImageView> mWeakFragment = mFragmentArray.get(position);
            if (mWeakFragment != null && mWeakFragment.get() != null) {
                itemView = mWeakFragment.get();
            }
            return itemView;
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int i) {
            WeiboLog.d("onPageSelected." + i);
            mSelectedIdx = i;
            updatePager();

            int size = mFragmentArray.size();
            for (int k = 0; k < size; k++) {
                int key = mFragmentArray.keyAt(k);
                WeakReference<AKSnapImageView> viewWeakReference = mFragmentArray.get(key);
                if (null != viewWeakReference && null != viewWeakReference.get()) {
                    WeiboLog.d("size:" + size + " key:" + key + " view:" + viewWeakReference.get());
                    AKSnapImageView imagePageView = (AKSnapImageView) viewWeakReference.get();
                    if (key == i) {
                        imagePageView.loadLargeBitmap();
                    } else {
                        imagePageView.loadThumb();
                    }
                } else {
                    WeiboLog.d("key:" + key);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    public class SaveImageTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            boolean flag = false;
            String targetFilePath = null;
            try {
                String path = (String) params[ 0 ];
                targetFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/image" +
                    DateUtils.formatDate(new Date(), "yyyy-MM-dd_HH-mm-ss") + WeiboUtils.getExt(path);
                flag = StreamUtils.copyFileToFile(targetFilePath, path);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return flag ? targetFilePath : null;
        }

        protected void onPostExecute(String bitmap) {
            if (! isFinishing()) {
                if (bitmap != null) {
                    NotifyUtils.showToast("保存成功:" + bitmap, Toast.LENGTH_LONG);
                } else {
                    NotifyUtils.showToast("保存失败:");
                }
            }
        }
    }
}
