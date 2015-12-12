package cn.archko.microblog.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.fragment.impl.AbsStatusImpl;
import cn.archko.microblog.listeners.OnPickPhotoListener;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.ImageViewerActivity;
import cn.archko.microblog.utils.BitmapThread;
import cn.archko.microblog.utils.TakePictureUtil;
import com.andrew.apollo.cache.LruCache;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.UploadImage;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.util.ArrayList;

/**
 * 发布微博的多图多图的对话框
 * TODO 需要处理程序被回收的状况,之后用PhotoHelper替换照片的选择.
 *
 * @author: archko 11-1-12 :上午7:48
 */
public class PickImageFragment extends AbsBaseListFragment<UploadImage> {

    private static final String TAG="PickImage";
    private static final int MENU_ADD=Menu.FIRST+100;
    private static final int MENU_SAVE=Menu.FIRST+101;
    public static final String KEY_PHOTO="key_photo";
    protected TimeLineAdapter mAdapter;
    Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1) {
                if (isResumed()) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };
    BitmapThread mBitmapThread;
    protected GridView mGridView;
    int width=120;
    int height=120;
    TakePictureUtil takePictureUtil;
    public OnPickPhotoListener mPickPhotoListener;
    public static LruCache<String, Bitmap> bitmapLruCache=new LruCache<String, Bitmap>(12);

    public PickImageFragment() {
        mStatusImpl=new AbsStatusImpl<UploadImage>() {
            @Override
            public SStatusData<UploadImage> loadData(Object... params) throws WeiboException {
                return null;
            }

            @Override
            public void saveData(SStatusData<UploadImage> data) {

            }
        };
    }

    public PickImageFragment(Handler handler) {
        super();
        mHandler=handler;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        WeiboLog.v(TAG, "onAttach:"+this);
        try {
            mPickPhotoListener=(OnPickPhotoListener) activity;
        } catch (ClassCastException e) {
            //throw new ClassCastException(activity.toString()+" must implement OnRefreshListener");
            mPickPhotoListener=null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //WeiboLog.d(TAG, "args:"+getArguments());
        if (null!=getArguments()&&null!=getArguments().getString(KEY_PHOTO)) {
            String url=getArguments().getString(KEY_PHOTO);
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "url:"+url);
            }
            if (null==mDataList) {
                mDataList=new ArrayList<UploadImage>();
            }
            UploadImage image=new UploadImage();
            image.path=url;
            mDataList.add(image);
        }
    }

    @Override
    public void initApi() {

    }

    @Override
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.ak_pick_image, container, false);
        mGridView=(GridView) root.findViewById(R.id.gridview);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "itemClick:"+pos);
                }
                selectedPos=pos;

                itemClick(pos, view);
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "itemLongClick:"+pos);
                }
                selectedPos=pos;
                //showButtonBar(view);
                itemLongClick(pos, view);
                return true;
            }
        });

        /*mDataList=new ArrayList<UploadImage>();
        loadTestData();*/
        if (mAdapter==null) {
            mAdapter=new TimeLineAdapter();
        }
        if (null==mDataList||mDataList.size()<1) {
            NotifyUtils.showToast("您可以开始添加图片了.");
        }

        mGridView.setAdapter(mAdapter);
        loadBitmap(mDataList);
    }

    private void loadBitmap(ArrayList<UploadImage> arrayList) {
        if (null==mBitmapThread) {
            mBitmapThread=new BitmapThread();
        }
        Message msg=Message.obtain();
        msg.obj=new Object[]{arrayList, mHandler};
        msg.what=0;
        mBitmapThread.addMessage(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null!=mBitmapThread) {
            mBitmapThread.release();
        }
    }

    private void loadTestData() {
        UploadImage image;
        image=new UploadImage();
        image.path="/sdcard/.microblog/picture/027b3e60e001ade332fdf50089d752f4.jpg";
        mDataList.add(image);

        image=new UploadImage();
        image.path="/sdcard/.microblog/picture/030d3ce6bfe710ded1c6820f995bf61b.jpg";
        mDataList.add(image);

        image=new UploadImage();
        image.path="/sdcard/.microblog/picture/0adaecb64f50c3cce0945de952d21bf2.jpg";
        mDataList.add(image);

        image=new UploadImage();
        image.path="/sdcard/.microblog/picture/19931e4f886ed292c6d4978848222153.jpg";
        mDataList.add(image);

        image=new UploadImage();
        image.path="/sdcard/.microblog/picture/f9c30698454c5b330896808215d39935.jpg";
        mDataList.add(image);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemView itemView=null;

        if (convertView==null) {
            itemView=new ItemView(getActivity());
        } else {
            itemView=(ItemView) convertView;
        }
        UploadImage image=mDataList.get(position);
        itemView.update(image);

        return itemView;
    }

    public View getView(SimpleViewHolder holder, final int position, int itemType) {
        return null;
    }

    public View newView(ViewGroup parent, int viewType) {
        return null;
    }

    public void _onActivityCreated(Bundle savedInstanceState) {
        WeiboLog.v(TAG, "onActivityCreated");

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                int position=pos;
                if (position==-1) {
                    WeiboLog.v("选中的是头部，不可点击");
                    return;
                }

                selectedPos=position;
                WeiboLog.v(TAG, "itemClick:"+pos+" selectedPos:"+selectedPos);

                if (view==footerView) {
                    return;
                }
                itemClick(position, view);
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                WeiboLog.v(TAG, "itemLongClick:"+pos);
                int position=pos;
                selectedPos=position;

                if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
                    WeiboLog.v(TAG, "footerView.click.");
                    return true;
                }

                if (view!=footerView) {
                    return itemLongClick(position, view);
                }
                return true;
            }
        });
        //mGridView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (mAdapter==null) {
            mAdapter=new TimeLineAdapter();
        }
        mGridView.setAdapter(mAdapter);

        WeiboLog.v(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        loadData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        MenuItem actionItem=menu.add(0, MENU_ADD, 0, "Add");

        // Items that show as actions should favor the "if room" setting, which will
        // prevent too many buttons from crowding the bar. Extra items will show in the
        // overflow area.
        MenuItemCompat.setShowAsAction(actionItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

        // Items that show as actions are strongly encouraged to use an icon.
        // These icons are shown without a text description, and therefore should
        // be sufficiently descriptive on their own.

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int resId=R.drawable.content_new_dark;
        if ("2".equals(themeId)) {
            //resId=R.drawable.content_new_dark;
        }
        actionItem.setIcon(resId);

        actionItem=menu.add(0, MENU_SAVE, 0, "SAVE");

        // Items that show as actions should favor the "if room" setting, which will
        // prevent too many buttons from crowding the bar. Extra items will show in the
        // overflow area.
        MenuItemCompat.setShowAsAction(actionItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

        // Items that show as actions are strongly encouraged to use an icon.
        // These icons are shown without a text description, and therefore should
        // be sufficiently descriptive on their own.

        resId=R.drawable.ic_cab_done_holo_light;
        if ("2".equals(themeId)) {
            resId=R.drawable.ic_cab_done_holo_light;
        }
        actionItem.setIcon(resId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==MENU_ADD) {
            addNewPicture();
        } else if (item.getItemId()==MENU_SAVE) {
            completePick();
        }
        return true;
    }

    private void addNewPicture() {
        int count=mAdapter.getCount();
        if (count>=9) {
            NotifyUtils.showToast("最多只能添加9张图片.");
            return;
        }

        selectedPos=-1; //设置-1,则在选择照片后,进行添加,否则视为编辑.
        if (null==takePictureUtil) {
            takePictureUtil=new TakePictureUtil();
            //takePictureUtil.setActivity(getActivity());
            takePictureUtil.setContext(getActivity());
            takePictureUtil.setFragment(this);
        }
        takePictureUtil.doPickPhotoAction();
    }

    private void completePick() {
        if (null!=mPickPhotoListener) {
            String path=null;
            if (null!=mDataList&&mDataList.size()>0) {
                path=mDataList.get(0).path;
            }
            //WeiboLog.v(TAG, "completePick:"+path);
            mPickPhotoListener.onPickOne(path);
        }
    }

    @Override
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {

    }

    @Override
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
        prepareMenu(achor);
    }

    @Override
    protected boolean itemLongClick(int pos, View achor) {
        return false;
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, "修改");
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, "删除");
        menuBuilder.getMenu().add(0, Constants.OP_ID_ORITEXT, index++, "查看");
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "onMenuItemClick:"+menuId);
        }
        switch (menuId) {
            case Constants.OP_ID_QUICK_REPOST: {
                quickRepostStatus();
                break;
            }
            case Constants.OP_ID_COMMENT: {
                commentStatus();
                break;
            }
            case Constants.OP_ID_ORITEXT: {
                viewOriginalStatus(null);
                break;
            }
        }
        return true;
    }

    /**
     * 修改
     */
    public void quickRepostStatus() {
        UploadImage image=mDataList.get(selectedPos);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("修改:"+image);
        }
        if (null==takePictureUtil) {
            takePictureUtil=new TakePictureUtil();
            //takePictureUtil.setActivity(getActivity());
            takePictureUtil.setContext(getActivity());
            takePictureUtil.setFragment(this);
        }
        takePictureUtil.doPickPhotoAction();
    }

    /**
     * 删除
     */
    public void commentStatus() {
        UploadImage image=mDataList.get(selectedPos);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("删除:"+image);
        }
        mDataList.remove(selectedPos);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 查看图片
     *
     * @param achor
     */
    public void viewOriginalStatus(View achor) {
        Intent intent=new Intent(getActivity(), ImageViewerActivity.class);
        UploadImage image=mDataList.get(selectedPos);
        String[] imageUrls=new String[]{image.path};
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "view :"+image);
        }
        intent.putExtra("thumbs", imageUrls);
        intent.putExtra("pos", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==Activity.RESULT_OK) {    //TODO 需要处理返回的视频的情况.
            if (requestCode==TakePictureUtil.CAMERA_WITH_DATA_TO_THUMB) {
                processGalleryData(data.getData());
            } else if (requestCode==TakePictureUtil.PHOTO_PICKED_WITH_DATA) {
                processGalleryData(data.getData());
            } else if (requestCode==TakePictureUtil.CAMERA_WITH_DATA) {
                // 照相机程序返回的,再次调用图 片剪辑程序去修剪图片
                //doCropPhoto();
                String path=takePictureUtil.getCurrentPhotoFile().getAbsolutePath();
                WeiboLog.i(TAG, "path:"+path);
                if (!TextUtils.isEmpty(path)) {
                    /*String imgUrl=path;
                    //showPhoto(imgUrl);
                    try {
                      Uri mPhotoUri=Uri.fromFile(new File(path));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    updateImageList(path);
                }
            } /*else if (requestCode==REQUEST_DRAFT) {
                Draft draft=(Draft) data.getSerializableExtra("draft");
                if (null!=draft) {
                    mDraft=draft;
                    initDraft(draft);
                }
            }*/ else if (requestCode==TakePictureUtil.EDIT_PHOTO_PICKED_WITH_DATA) {
                processGalleryData(data.getData());
            }
        } else {
            //clearImagePreview();
        }
    }

    private void processGalleryData(Uri imageFileUri) {
        String[] proj={MediaStore.Images.Media.DATA};
        Cursor cur=null;

        try {
            WeiboLog.i(TAG, "imageFileUri:"+imageFileUri);
            cur=getActivity().getContentResolver().query(imageFileUri, proj, null, null, null);
            int imageIdx=cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cur.moveToFirst();
            String imagePath=cur.getString(imageIdx);
            WeiboLog.i(TAG, "imagePath:"+imagePath);

            File file=new File(imagePath);
            if (file.exists()) {
                if (file.length()>TakePictureUtil.MAX_IMAGE_SIZE) {
                    NotifyUtils.showToast("上传的图片超过了5m，新浪不支持！");
                    //clearImagePreview();
                    return;
                }
                /*mPhotoUri=imageFileUri;
                showPhoto(imageFileUri);*/
                updateImageList(imagePath);
            }

        } catch (Exception e) {
            WeiboLog.e(e.toString());
        } finally {
            if (null!=cur) {
                cur.close();
            }
        }
    }

    private void updateImageList(String imagePath) {
        if (selectedPos>-1) {
            UploadImage image=mDataList.get(selectedPos);
            image.path=imagePath;
            image.pic_id="";
        } else {
            UploadImage image=new UploadImage();
            image.path=imagePath;
            mDataList.add(image);
        }
        mAdapter.notifyDataSetChanged();
        loadBitmap(mDataList);
    }

    //---------------------------------------
    private class ItemView extends LinearLayout implements Checkable {

        private TextView mTitle;
        private ImageView mIcon;    //

        private ItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ak_pick_image_item, this);
            mIcon=(ImageView) findViewById(R.id.iv_portrait);
        }

        public void update(UploadImage image) {
            Bitmap bitmap=bitmapLruCache.get(image.path);
            if (null!=bitmap) {
                mIcon.setImageBitmap(bitmap);
            }/* else {
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inSampleSize=BitmapUtils.computeSampleSizeLarger(options.outWidth, options.outHeight, width);
                options.inPreferredConfig=Bitmap.Config.RGB_565;
                bitmap=BitmapFactory.decodeFile(image.path, options);
                if (null!=bitmap) {
                    bitmapLruCache.put(image.path, bitmap);
                    mIcon.setImageBitmap(bitmap);
                }
            }*/
        }

        private boolean checked=false;

        @Override
        public boolean isChecked() {
            return checked;
        }

        @Override
        public void setChecked(boolean aChecked) {
            if (checked==aChecked) {
                return;
            }
            checked=aChecked;
            setIndicatorVisible(checked);
        }

        public void setIndicatorVisible(boolean checked) {
            setBackgroundResource(checked ? R.drawable.abs__list_focused_holo : android.R.color.transparent);
        }

        @Override
        public void toggle() {
            setChecked(!checked);
        }
    }

    //--------------------- adapter ---------------------
    public class TimeLineAdapter extends BaseAdapter {

        public TimeLineAdapter() {
            WeiboLog.v(TAG, "TimeLineAdapter:");
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return mDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return PickImageFragment.this.getView(position, convertView, parent);
        }
    }
}
