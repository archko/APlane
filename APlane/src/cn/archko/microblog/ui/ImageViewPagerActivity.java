package cn.archko.microblog.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.HackyViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import cn.archko.microblog.R;
import cn.archko.microblog.bean.ImageBean;
import com.android.photos.BitmapRegionTileSource;
import com.android.photos.views.TiledImageView;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;
import org.fengwx.gif.GifDrawable;
import org.fengwx.gif.GifImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author archko
 */
public class ImageViewPagerActivity extends Activity {

    public static final String TAG="ImageViewPager";

    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ak_glview_pager);
        mViewPager=(HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);

        loadData();
    }

    void loadData() {
        if (null==getIntent()||null==getIntent().getParcelableArrayListExtra("items")) {
            NotifyUtils.showToast("参数不对.");
            return;
        }

        ArrayList<ImageBean> imageBeans=getIntent().getParcelableArrayListExtra("items");
        int pos=getIntent().getIntExtra("pos", 0);
        WeiboLog.d(TAG, "pos:"+pos+" size:"+imageBeans.size());
        mViewPager.setAdapter(new SamplePagerAdapter(this, imageBeans));
        mViewPager.setCurrentItem(pos);
        //loadFromFile();
    }

    private void loadFromFile() {
        /*new AsyncTask<Object, Object, List<File>>() {
            @Override
            protected List<File> doInBackground(Object... params) {
                File dir=new File();
                if (dir.exists()) {
                    File[] files=dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            int i=0;
                            i++;
                            return true;
                        }
                    });

                    return Arrays.asList(files);
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<File> list) {
                mViewPager.setAdapter(new SamplePagerAdapter(list));
            }
        }.execute();*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    static class SamplePagerAdapter extends PagerAdapter {

        List<ImageBean> dataList;
        Context mContext;
        LayoutInflater inflater;

        public SamplePagerAdapter(Context context, List<ImageBean> dataList) {
            mContext=context;
            inflater=(LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.dataList=dataList;
        }

        @Override
        public int getCount() {
            return null==dataList ? 0 : dataList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            ImageBean bean=dataList.get(position);
            if (bean.path.endsWith("gif")) {
                View photoView=inflater.inflate(R.layout.main, null);// new GifImageView(mContext);
                GifImageView gifImageView=(GifImageView) photoView.findViewById(R.id.gifview);
                if (!TextUtils.isEmpty(bean.path)) {
                    try {
                        GifDrawable drawable=new GifDrawable(bean.path);
                        gifImageView.setImageDrawable(drawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                return photoView;
            } else {
                TiledImageView photoView=new TiledImageView(container.getContext());
                if (!TextUtils.isEmpty(bean.path)) {
                    photoView.setTileSource(new BitmapRegionTileSource(bean.path));
                } else {
                    WeiboLog.d(TAG, "item is null:"+dataList.get(position));
                }

                // Now just add PhotoView to ViewPager and return it
                container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

                return photoView;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof TiledImageView) {
                TiledImageView mTextureView=(TiledImageView) object;
                mTextureView.destroy();
            }
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

    }

}
