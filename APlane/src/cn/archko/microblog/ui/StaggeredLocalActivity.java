package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.bean.ImageBean;
import cn.archko.microblog.view.ScaleImageView;
import com.andrew.apollo.utils.ApolloUtils;
import com.android.photos.FullscreenViewer;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;
import org.fengwx.GifViewer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author archko
 */
public class StaggeredLocalActivity extends SkinFragmentActivity {

    protected int COUNT=800;
    final int maxSize=4096000;
    final int minSize=4000;
    //protected ArrayList<File> mDataList=new ArrayList<File>();
    public static final String PICTURE_PATH=Environment.getExternalStorageDirectory().getPath()+"/.microblog/picture";
    public static final String GIF_PATH=Environment.getExternalStorageDirectory().getPath()+"/.microblog/gif";
    public static final String SINA_PICTURE_PATH=Environment.getExternalStorageDirectory().getPath()+"/sina/weibo/.prenew";
    private String filepath=PICTURE_PATH;
    private RecyclerView mRecyclerView;
    LayoutAdapter mAdapter;
    protected ProgressDialog mProgress;

    /**
     * This will not work so great since the heights of the imageViews
     * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get
     * the (intrinsic) image width and height and set the views height manually. I will
     * look into a fix once I find extra time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ak_layout_local_recycler_view);
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        //mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle("查看缓存图片");

        mRecyclerView=(RecyclerView) findViewById(R.id.statusList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        /*final ItemClickSupport itemClick=ItemClickSupport.addTo(mRecyclerView);

        itemClick.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View child, int position, long id) {
                ImageBean ImageBean=(ImageBean) mAdapter.getItems().get(position);
                WeiboLog.d("", "item:"+position+" image:"+ImageBean);
            }
        });

        itemClick.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View child, int position, long id) {
                return true;
            }
        });*/

        //final Drawable divider=getResources().getDrawable(R.drawable.divider);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        mAdapter=new LayoutAdapter(this, 0);
        mRecyclerView.setAdapter(mAdapter);
        initData();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProgress!=null) {
            mProgress.dismiss();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showProgress() {
        mProgress=ProgressDialog.show(this, "", "Loading...");
    }

    public void stopProgress() {
        if (null!=mProgress) {
            mProgress.cancel();
        }
    }

    public void initData() {
        showProgress();
        ApolloUtils.execute(false, new AsyncTask<Object, Object, ArrayList<ImageBean>>() {
            @Override
            protected ArrayList<ImageBean> doInBackground(Object... params) {
                WeiboLog.d("", "file path:"+filepath);
                File dir=new File(filepath);
                if (dir.exists()) {
                    File[] files=dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            int i=0;
                            i++;
                            if (i>COUNT) {
                                return false;
                            }

                            /*if (pathname.length()>minSize&&pathname.length()<maxSize) {
                                return true;
                            }*/
                            return true;
                        }
                    });
                    WeiboLog.d("", "file length:"+files.length);
                    /*List<File> fileList=Arrays.asList(files);
                    Collections.sort(fileList, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            WeiboLog.d("", "modify:"+f1.lastModified()+" modify2:"+f2.lastModified());
                            return f1.lastModified()>f2.lastModified() ? 1 : 0;
                        }
                    });*/
                    return parseImageBean(files);
                }
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<ImageBean> list) {
                stopProgress();
                if (null!=list&&list.size()>0) {
                    mAdapter.setDatas(list);
                } else {
                    NotifyUtils.showToast("没有图片.");
                    mAdapter.setDatas(new ArrayList<ImageBean>());
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public ArrayList<ImageBean> parseImageBean(File[] files) {
        ArrayList<ImageBean> list=new ArrayList<ImageBean>();
        if (null!=files) {
            ImageBean bean;
            for (File f : files) {
                bean=new ImageBean();
                bean.path=f.getAbsolutePath();
                bean.filesize=f.length();
                bean.name=f.getName();
                list.add(0, bean);
            }
        }
        return list;
    }

    public ArrayList<ImageBean> parseImageBean(List<File> files) {
        ArrayList<ImageBean> list=new ArrayList<ImageBean>();
        if (null!=files) {
            ImageBean bean;
            for (File f : files) {
                bean=new ImageBean();
                bean.path=f.getAbsolutePath();
                bean.filesize=f.length();
                bean.name=f.getName();
                list.add(bean);
            }
        }
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST+1, 1, "缓存照片");
        menu.add(0, Menu.FIRST+2, 2, "缓存gif");
        menu.add(0, Menu.FIRST+3, 3, "新浪缓存");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if (id==Menu.FIRST+1) {
            if (!PICTURE_PATH.equals(filepath)) {
                filepath=PICTURE_PATH;
                initData();
            }
        } else if (id==Menu.FIRST+2) {
            if (!GIF_PATH.equals(filepath)) {
                filepath=GIF_PATH;
                initData();
            }
        } else if (id==Menu.FIRST+3) {
            if (!SINA_PICTURE_PATH.equals(filepath)) {
                filepath=SINA_PICTURE_PATH;
                initData();
            }
        } else if (item.getItemId()==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteDialog(String title, String msg, final int pos) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        AlertDialog dialog=builder.setTitle(title).setMessage(msg)
            .setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();
                    }
                }
            ).setPositiveButton(getResources().getString(R.string.confirm),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        delete(pos);
                        arg0.cancel();
                    }
                }
            ).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void delete(final int pos) {
        ApolloUtils.execute(false, new AsyncTask<Object, Object, File>() {
            @Override
            protected File doInBackground(Object... params) {
                doDelete(pos);
                afterDelete(null, pos);
                return null;
            }

            @Override
            protected void onPostExecute(File o) {
                //Log.d("onPostExecute", "delete"+(o));
            }
        });
    }

    public File doDelete(int pos) {
        if (mAdapter!=null||mAdapter.getItemCount()>0&&pos<mAdapter.getItemCount()) {
            ImageBean ImageBean=(ImageBean) mAdapter.mItems.get(pos);
            File file=new File(ImageBean.path);
            boolean flag=file.delete();
            WeiboLog.d("doDelete", "pos:"+pos+" flag:"+flag+" delete file:"+file);
            return file;
        }
        return null;
    }

    public void afterDelete(File o, int pos) {
        try {
            mAdapter.removeItem(pos);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------
    private class LayoutAdapter extends RecyclerView.Adapter<LayoutAdapter.SimpleViewHolder> {

        private final Context mContext;
        private List<ImageBean> mItems;

        public class SimpleViewHolder extends RecyclerView.ViewHolder {

            public View root;
            public final TextView title;
            public ScaleImageView imageView;

            public SimpleViewHolder(View view) {
                super(view);
                root=view;
                title=(TextView) view.findViewById(R.id.txt);
                imageView=(ScaleImageView) view.findViewById(R.id.imageView1);
            }
        }

        public LayoutAdapter(Context context, int layoutId) {
            mContext=context;
            mItems=new ArrayList<ImageBean>();
        }

        public void setDatas(ArrayList<ImageBean> mDatas) {
            this.mItems=mDatas;
        }

        public List<ImageBean> getItems() {
            return mItems;
        }

        /*public void addItem(int position) {
            final int id = mCurrentItemId++;
            mItems.add(position, id);
            notifyItemInserted(position);
        }*/

        public void removeItem(int position) {
            if (position<mItems.size()) {
                mItems.remove(position);
                notifyItemRemoved(position);
            }
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view=LayoutInflater.from(mContext).inflate(R.layout.staggered_row, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, final int position) {
            final ImageBean bean=(ImageBean) mItems.get(position);
            //直接加载本地照片,使用file://或者 /开头就可以
            ApolloUtils.getImageFetcher(mContext).startLoadImage(bean.path, holder.imageView);
            String title=bean.name;
            if (0!=bean.filesize) {
                title="size:"+bean.filesize+"=>"+title;
            }
            holder.title.setText(title);
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImage(view, bean);
                }
            });
            holder.root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String msg="确定删除:"+bean.name;
                    deleteDialog("删除文件:", msg, position);
                    return false;
                }
            });
        }

        private void showImage(View view, ImageBean bean) {
            if (null!=bean&&!TextUtils.isEmpty(bean.path)) {
                Intent intent;
                if (bean.path.endsWith("gif")) {
                    intent=new Intent(StaggeredLocalActivity.this, GifViewer.class);
                    intent.putExtra(GifViewer.EXTRA_URL, bean.path);
                    intent.setData(Uri.parse(bean.path));
                } else {
                    intent=new Intent(StaggeredLocalActivity.this, FullscreenViewer.class);
                    intent.putExtra(GifViewer.EXTRA_URL, bean.path);
                    intent.setData(Uri.parse(bean.path));
                }
                StaggeredLocalActivity.this.startActivity(intent);
            }
        }

        @Override
        public int getItemCount() {
            return null==mItems ? 0 : mItems.size();
        }
    }
}
