package cn.archko.microblog.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.text.Selection;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import cn.archko.microblog.R;
import cn.archko.microblog.fragment.DraftListFragment;
import cn.archko.microblog.fragment.SearchDialogFragment;
import cn.archko.microblog.fragment.abs.AtUserListener;
import cn.archko.microblog.fragment.abs.OnRefreshListener;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.utils.AKUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import cn.archko.microblog.view.AutoCompleteView;
import cn.archko.microblog.view.EmojiPanelView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.me.microblog.App;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.Draft;
import com.me.microblog.bean.SendTask;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

/**
 * @author root
 */
public class NewStatusActivity extends BaseOauthFragmentActivity implements ActionBar.OnNavigationListener {

    private static final String TAG="NewStatusActivity";
    private ActionBar mActionBar;
    private AutoCompleteView content;
    private ImageView mPreview, mCloseImage;
    /**
     * 图像操作栏.提供不同的操作入口.
     */
    LinearLayout mImageOperaBar;
    /**
     * 字数文本
     */
    private TextView mCharNum;
    /**
     * 取图按钮
     */
    Button mPictureBtn, mLocBtn, mTrendBtn, mAtBtn;
    Button mEmoBtn;
    Button mLocResultBtn;
    ImageView mClearLocBtn;
    ProgressBar mLocProgressBar;
    Button mCrop, mRotate, mFilter;
    private Button mEditPhoto;

    private String imgUrl="";
    boolean isDone=false;
    /**
     * 自动完成的用户名
     */
    ArrayList<String> mAtNames=null;
    Handler mHandler=new Handler();
    /**
     * 话题列表
     */
    ArrayList<String> trendList=null;
    /**
     * 经度，为0时就是不发位置
     */
    double longitude=0.0;
    /**
     * 纬度，为0时就是不发位置
     */
    double latitude=0.0;
    /**
     * 地理位置是否启用，默认是启用的，可以取消。
     */
    //boolean isGeoEnabled=true;

    /*GridView mEmotionGridview;
    private GridAdapter mEmotionAdapter;*/
    EmojiPanelView mEmojiPanelView;
    InputMethodManager imm;
    /**
     * 监听器用于显示进度
     */
    OnRefreshListener mRefreshListener;

    /**
     * 草稿
     */
    Draft mDraft;
    Button mDraftBtn;
    public static final int REQUEST_DRAFT=1024;

    ArrayAdapter<CharSequence> mVisibleAdapter=null;
    /**
     * 选中的位置。
     */
    int selectedPos=0;
    //--------------------- 认证 ---------------------

    /**
     * 认证失败后的操作
     */
    void oauthFailed() {
        AKUtils.showToast(R.string.new_status_failed, Toast.LENGTH_LONG);
    }

    private View.OnClickListener clickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickMethod(v);
        }
    };

    /**
     * 处理点击事件
     *
     * @param v
     */
    private void onClickMethod(View v) {
        switch (v.getId()) {
            case R.id.status_img_close:
                clearImagePreview();
                break;

            case R.id.btn_picture: {
                //mEmotionGridview.setVisibility(View.GONE);
                mEmojiPanelView.setVisibility(View.GONE);
                doPickPhotoAction();
                break;
            }

            case R.id.btn_location: {
                //Toast.makeText(this, "不好意思,暂时不能发位置.稍后加入!", Toast.LENGTH_LONG).show();
                //isGeoEnabled=false;
                getLocation();
                break;
            }

            case R.id.btn_trend: {
                //mEmotionGridview.setVisibility(View.GONE);
                mEmojiPanelView.setVisibility(View.GONE);
                autoCompleteTrends();
                break;
            }

            case R.id.btn_at: {
                //mEmotionGridview.setVisibility(View.GONE);
                mEmojiPanelView.setVisibility(View.GONE);
                autoCompleteAt();
                break;
            }

            case R.id.btn_emo: {
                int emoVisible=mEmojiPanelView.getVisibility();
                if (emoVisible==View.VISIBLE) {
                    //mEmotionGridview.setVisibility(View.GONE);
                    mEmojiPanelView.setVisibility(View.GONE);
                } else {
                    //mEmotionGridview.setVisibility(View.VISIBLE);
                    mEmojiPanelView.setVisibility(View.VISIBLE);
                    imm.hideSoftInputFromWindow(content.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
                break;
            }

            case R.id.btn_draft: {
                getDraft();
                break;
            }

            case R.id.status_content: {
                int emoVisible=mEmojiPanelView.getVisibility();
                if (emoVisible==View.VISIBLE) {
                    imm.hideSoftInputFromWindow(content.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
                break;
            }

            case R.id.search_close_btn: {
                clearLocation();
                break;
            }

            case R.id.crop_btn: {
                AKUtils.showToast("Not implemted!");
                break;
            }

            case R.id.rotate_btn: {
                AKUtils.showToast("Not implemted!");
                break;
            }

            case R.id.filter_btn: {
                AKUtils.showToast("Not implemted!");
                break;
            }

            case R.id.edit_btn: {
                doEditPhoto();
                break;
            }

            default:
                break;
        }
    }

    /**
     * 发微博。
     */
    private void sendWeibo() {
        imm.hideSoftInputFromWindow(content.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        String contentString=content.getEditableText().toString();
        if (TextUtils.isEmpty(contentString)) {
            AKUtils.showToast(R.string.new_status_should_not_null);
            return;
        }

        int len=contentString.length();
        if (len>Constants.INPUT_STRING_COUNT) {
            AKUtils.showToast(R.string.text_exceed_max_num);
            return;
        }
        //send.setEnabled(false);

        addTask(contentString);
        /*if (App.OAUTH_MODE.equalsIgnoreCase(Constants.SOAUTH_TYPE_CLIENT)) {
            SendStatusTask sendStatusTask=new SendStatusTask();
            sendStatusTask.execute(contentString);
        } else {
            if (System.currentTimeMillis()>=App.oauth2_timestampe&&App.oauth2_timestampe!=0) {
                WeiboLog.w(TAG, "web认证，token过期了.");
                mOauth2Handler.oauth2(null);
                showToast(R.string.new_status_token_invalid, Toast.LENGTH_SHORT);
            } else {
                WeiboLog.d(TAG, "web认证，但token有效。");
                SendStatusTask sendStatusTask=new SendStatusTask();
                sendStatusTask.execute(contentString);
            }
        }*/
    }

    private TextWatcher watcher=new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable e) {
            WeiboLog.i(TAG, "");
            String string=e.toString();
            int len=string.length();

            mCharNum.setText(String.valueOf(Constants.INPUT_STRING_COUNT-len));
        }
    };

    private void clearImagePreview() {
        mPreview.setVisibility(View.GONE);
        mCloseImage.setVisibility(View.GONE);
        mImageOperaBar.setVisibility(View.GONE);
        mLocProgressBar.setVisibility(View.GONE);
        imgUrl="";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        final ActionBar bar=getActionBar();
        mActionBar=bar;
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);  //隐藏主面的标题
        mActionBar.setDisplayShowHomeEnabled(true);   //整个标题栏
        mActionBar.setTitle(R.string.text_new_status);
        setContentView(R.layout.status_new);

        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initViews();

        initData();

        initLocation();

        mVisibleAdapter=ArrayAdapter.createFromResource(this, R.array.status_visible_arr, android.R.layout.simple_spinner_item);
        mVisibleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mActionBar.setListNavigationCallbacks(mVisibleAdapter, this);

        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), NewStatusActivity.this);
    }

    /**
     * 初始化微博的数据，有内部调用与外部分享调用。
     */
    private void initData() {
        Intent intent=getIntent();
        WeiboLog.d(TAG, "initData:"+intent);
        if (null!=intent) {
            String action=intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                Uri uri=(Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                WeiboLog.d(TAG, "uri: ->"+uri);
                if (null!=uri) {
                    processGalleryData(uri);
                    imm.hideSoftInputFromWindow(content.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }

                String type=intent.getType();
                if (type.startsWith("text/")) {
                    String txt=intent.getExtras().getString("android.intent.extra.TEXT");
                    WeiboLog.d(TAG, "txt: ->"+txt);
                    if (!TextUtils.isEmpty(txt)) {
                        content.setText(txt);
                        Selection.setSelection(content.getText(), txt.length());
                    }
                } else {
                }
            } else if (Constants.INTENT_NEW_BLOG.equals(action)) {
                String at_some=intent.getStringExtra("at_some");
                WeiboLog.d("处理@："+at_some);
                if (!TextUtils.isEmpty(at_some)) {
                    at_some+=" ";
                    content.setText(at_some);
                    content.setSelection(at_some.length());
                } else {
                    at_some=intent.getStringExtra("trend");
                    WeiboLog.d("处理话题："+at_some);
                    if (!TextUtils.isEmpty(at_some)) {
                        at_some+=" ";
                        content.setText(at_some);
                        content.setSelection(at_some.length());
                    }
                }

                Draft draft=(Draft) intent.getSerializableExtra("draft");
                if (null!=draft) {
                    mDraft=draft;
                    initDraft(draft);
                }
            }
        }
    }

    void initViews() {
        content=(AutoCompleteView) findViewById(R.id.status_content);

        content.addTextChangedListener(watcher);
        content.setOnClickListener(clickListener);

        mPreview=(ImageView) findViewById(R.id.iv_status_img);
        mCloseImage=(ImageView) findViewById(R.id.status_img_close);
        mCloseImage.setOnClickListener(clickListener);
        mImageOperaBar=(LinearLayout) findViewById(R.id.image_opera_bar);
        mCharNum=(TextView) findViewById(R.id.char_num);
        mPictureBtn=(Button) findViewById(R.id.btn_picture);
        mLocBtn=(Button) findViewById(R.id.btn_location);
        mTrendBtn=(Button) findViewById(R.id.btn_trend);
        mAtBtn=(Button) findViewById(R.id.btn_at);
        mLocResultBtn=(Button) findViewById(R.id.location);
        mEmoBtn=(Button) findViewById(R.id.btn_emo);
        mDraftBtn=(Button) findViewById(R.id.btn_draft);
        mClearLocBtn=(ImageView) findViewById(R.id.search_close_btn);
        mLocProgressBar=(ProgressBar) findViewById(R.id.loc_progress_bar);

        mCrop=(Button) findViewById(R.id.crop_btn);
        mRotate=(Button) findViewById(R.id.rotate_btn);
        mFilter=(Button) findViewById(R.id.filter_btn);
        mEditPhoto=(Button) findViewById(R.id.edit_btn);

        //mEmotionGridview=(GridView) findViewById(R.id.faces);
        mEmojiPanelView=(EmojiPanelView) findViewById(R.id.emoji_panel);

        mPictureBtn.setOnClickListener(clickListener);
        mLocBtn.setOnClickListener(clickListener);
        mTrendBtn.setOnClickListener(clickListener);
        mAtBtn.setOnClickListener(clickListener);
        mLocResultBtn.setOnClickListener(clickListener);
        mEmoBtn.setOnClickListener(clickListener);
        mDraftBtn.setOnClickListener(clickListener);
        mClearLocBtn.setOnClickListener(clickListener);

        mCrop.setOnClickListener(clickListener);
        mRotate.setOnClickListener(clickListener);
        mFilter.setOnClickListener(clickListener);
        mEditPhoto.setOnClickListener(clickListener);

        /*mEmotionAdapter=new EmojiPanelView.GridAdapter(this);
        mEmotionAdapter.setList(AKSmileyParser.getInstance(this).mSmileyTexts);
        mEmotionGridview.setAdapter(mEmotionAdapter);
        mEmotionGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                refreshText(position);
            }
        });*/
        mEmojiPanelView.setContent(content);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        WeiboLog.d(TAG, "onNavigationItemSelected:"+itemPosition);
        selectedPos=itemPosition;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_FIRST, 0, R.string.new_status_drafts).
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SECOND, 0, R.string.text_new_status).
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        /*SubMenu sub=menu.addSubMenu("Theme");
        sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);*/

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        //int overFlowId=R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark;
        int postId=R.drawable.send_dark;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            postId=R.drawable.send_light;
            //overFlowId=R.drawable.abs__ic_menu_moreoverflow_normal_holo_light;
        }
        menu.findItem(MENU_SECOND).setIcon(postId);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            showExistDialog();
        } else if (MENU_FIRST==itemId) {
            getDraft();
        } else if (MENU_SECOND==itemId) {
            sendWeibo();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取地理位置，由地图api获取
     */
    private void initLocation() {
        mLocClient=new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(myListener);
    }

    void clearLocation() {
        longitude=0.0d;
        latitude=0.0d;
        mLocResultBtn.setText(null);
        mClearLocBtn.setVisibility(View.GONE);
        mLocResultBtn.setVisibility(View.GONE);
    }

    void getLocation() {
        clearLocation();
        mLocProgressBar.setVisibility(View.VISIBLE);
        setLocationOption();
        startMap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mLocClient.unRegisterLocationListener(myListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示退出对话框
     */
    void showExistDialog() {
        String contentString=content.getEditableText().toString();
        if (TextUtils.isEmpty(contentString)) {
            finish();
            return;
        }

        LayoutInflater inflater=LayoutInflater.from(NewStatusActivity.this);
        View view=inflater.inflate(R.layout.home_dialog_view, null);
        ThemeUtils.getsInstance().themeBackground(view, NewStatusActivity.this);

        Button cancelButton=(Button) view.findViewById(R.id.cancel);
        Button updateButton=(Button) view.findViewById(R.id.ok);
        Button installButton=(Button) view.findViewById(R.id.install);
        TextView msgView=(TextView) view.findViewById(R.id.update_msg);

        installButton.setVisibility(View.VISIBLE);
        cancelButton.setText(R.string.new_status_cancel);
        updateButton.setText(R.string.new_status_save_draft);
        //updateButton.setVisibility(View.GONE);
        installButton.setText(R.string.new_status_exit);
        msgView.setText(R.string.new_status_exit_msg);

        AlertDialog.Builder builder=new AlertDialog.Builder(NewStatusActivity.this)
            .setTitle(R.string.app_name)
            .setView(view);

        final AlertDialog dialog=builder.create();
        dialog.show();

        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
                saveDraft();
            }
        });

        installButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
                NewStatusActivity.this.finish();
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        WeiboLog.i(TAG, "dispatchKeyEvent.code:"+event.getKeyCode());
        if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
            if (event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0) {
                if (!isDone) {
                    showExistDialog();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {    //TODO 需要处理返回的视频的情况.
            if (requestCode==CAMERA_WITH_DATA_TO_THUMB) {
                processGalleryData(data.getData());
            } else if (requestCode==PHOTO_PICKED_WITH_DATA) {
                processGalleryData(data.getData());
            } else if (requestCode==CAMERA_WITH_DATA) {
                // 照相机程序返回的,再次调用图 片剪辑程序去修剪图片
                //doCropPhoto();
                String path=mCurrentPhotoFile.getAbsolutePath();
                WeiboLog.i(TAG, "path:"+path);
                if (!TextUtils.isEmpty(path)) {
                    imgUrl=path;
                    showPhoto(imgUrl);
                    try {
                        mPhotoUri=Uri.fromFile(new File(path));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode==REQUEST_DRAFT) {
                Draft draft=(Draft) data.getSerializableExtra("draft");
                if (null!=draft) {
                    mDraft=draft;
                    initDraft(draft);
                }
            } else if (requestCode==EDIT_PHOTO_PICKED_WITH_DATA) {
                processGalleryData(data.getData());
            }
        } else {
            //clearImagePreview();
        }
    }

    private void addTask(String content) {
        if (content.length()>Constants.INPUT_STRING_COUNT) {
            content=content.substring(0, Constants.INPUT_STRING_COUNT);
            AKUtils.showToast(R.string.new_status_too_more_txt);
        }

        Intent taskService=new Intent(NewStatusActivity.this, SendTaskService.class);
        SendTask task=new SendTask();
        task.uid=currentUserId;
        task.userId=currentUserId;
        task.content=content;
        task.data=latitude+"-"+longitude;
        task.type=TwitterTable.SendQueueTbl.SEND_TYPE_STATUS;
        task.imgUrl=imgUrl;
        task.createAt=new Date().getTime();
        task.text=String.valueOf(selectedPos);
        taskService.putExtra("send_task", task);
        NewStatusActivity.this.startService(taskService);
        AKUtils.showToast("新微博任务添加到队列服务中了。");
        NewStatusActivity.this.finish();
    }

    //--------------------- autocomplete listview ---------------------
    AtUserListener mAtUserListener=new AtUserListener() {
        @Override
        public void getAtUser(AtUser atUser) {
            if (null!=atUser) {
                completeText(atUser.name);
            }
        }
    };

    /**
     * 显示自动完成的Fragment
     *
     * @param type 类型,是搜索用户还是话题
     */
    private void showCompleteFragment(int type) {
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        Fragment prev=getFragmentManager().findFragmentByTag("dialog");
        if (prev!=null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args=new Bundle();
        args.putInt("type", type);
        SearchDialogFragment searchDialogFragment=new SearchDialogFragment();
        searchDialogFragment.setArguments(args);
        searchDialogFragment.setAtUserListener(mAtUserListener);
        searchDialogFragment.show(ft, "dialog");
    }

    private void autoCompleteAt() {
        Editable editable=content.getText();
        String txt=editable.toString();
        int start=content.getSelectionStart();
        int end=content.getSelectionEnd();
        //WeiboLog.v(TAG, "start:"+start+" txt:"+txt+" end:"+end);

        String startTxt=txt.substring(0, start);
        String endTxt=txt.substring(end);
        //WeiboLog.v(TAG, "startTxt:"+startTxt+"->endTxt:"+endTxt);

        /*NewStatusActivity(11798): start:5 txt:gdgjmngddgn end:5
        D/NewStatusActivity(11798): startTxt:gdgjm->endTxt:ngddgn*/

        txt=startTxt+" @ "+endTxt;
        content.setText(txt);
        content.setSelection(start+2);

        /*queryNames();
        if (mAtNames.size()>0) {
            updateAdapter(mAtNames);
        }*/
        showCompleteFragment(0);
    }

    private void autoCompleteTrends() {
        Editable editable=content.getText();
        String txt=editable.toString();
        int start=content.getSelectionStart();
        int end=content.getSelectionEnd();
        //WeiboLog.v(TAG, "start:"+start+" txt:"+txt+" end:"+end);

        String startTxt=txt.substring(0, start);
        String endTxt=txt.substring(end);

        txt=startTxt+" ## "+endTxt;
        content.setText(txt);
        content.setSelection(start+2);

        //getTrends();
        showCompleteFragment(2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mEmojiPanelView.getVisibility()==View.VISIBLE) {
            mEmojiPanelView.setVisibility(View.GONE);
        }
    }

    private void completeText(String item) {
        Editable editable=content.getText();
        String txt=editable.toString();
        int start=content.getSelectionStart();
        int end=content.getSelectionEnd();
        //WeiboLog.d(TAG, "start:"+start+" txt:"+txt+" end:"+end);

        String startTxt=txt.substring(0, start);
        String endTxt=txt.substring(end);
        //WeiboLog.d(TAG, "startTxt:"+startTxt+"->endTxt:"+endTxt);

        /*NewStatusActivity(11798): start:7 txt:gdgjm @ ngddgn end:7
        NewStatusActivity(11798): startTxt:gdgjm @->endTxt: ngddgn*/

        String result=startTxt+item+endTxt;
        content.setText(result);
        // make sure we keep the caret at the end of the text view
        /*Editable spannable=content.getText();
        Selection.setSelection(spannable, spannable.length());*/
        content.setSelection(start+item.length()+1);
    }

    //--------------------- 草稿 ---------------------

    /**
     * 多草稿列表中获取一个草稿
     */
    private void getDraft() {
        Intent intent=new Intent(NewStatusActivity.this, AccountUserActivity.class);
        intent.putExtra("type", AccountUserActivity.TYPE_DRAFT_ONLY);
        intent.putExtra("mode", DraftListFragment.GET_DRAFT);
        startActivityForResult(intent, REQUEST_DRAFT);
    }

    /**
     * 初始化草稿
     */
    private void initDraft(Draft draft) {
        if (draft.uid==currentUserId) {
            content.setText(draft.content);
            final String url=draft.imgUrl;
            if (!TextUtils.isEmpty(url)) {
                imgUrl=url;
                showPhoto(imgUrl);
            } else {
                imgUrl=null;
                mPreview.setImageBitmap(null);

                mPreview.setVisibility(View.VISIBLE);
                mCloseImage.setVisibility(View.VISIBLE);
                mImageOperaBar.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 存储草稿，如果原来是编辑的，就修改，如果是新建就保存。
     */
    private void saveDraft() {
        String newContent=content.getEditableText().toString();
        try {
            if (currentUserId==-1) {
                WeiboLog.e(TAG, "用户id不存在，系统出错！");
            } else if (TextUtils.isEmpty(newContent)&&TextUtils.isEmpty(imgUrl)) {
                WeiboLog.w(TAG, "内容为空，不保存！");
                AKUtils.showToast("内容为空，不保存！");
            } else {
                ContentResolver resolver=getContentResolver();
                if (null==mDraft) {   //create
                    ContentValues cv=new ContentValues();
                    if (newContent.length()>Constants.INPUT_STRING_COUNT) {
                        newContent=newContent.substring(0, Constants.INPUT_STRING_COUNT);
                    }

                    cv.put(TwitterTable.DraftTbl.UID, currentUserId);
                    cv.put(TwitterTable.DraftTbl.USER_ID, currentUserId);
                    cv.put(TwitterTable.DraftTbl.CONTENT, newContent);
                    cv.put(TwitterTable.DraftTbl.IMG_URL, imgUrl);
                    cv.put(TwitterTable.DraftTbl.CREATED_AT, new Date().getTime());
                    cv.put(TwitterTable.DraftTbl.TYPE, TwitterTable.DraftTbl.STATUS_TYPE);

                    resolver.insert(TwitterTable.DraftTbl.CONTENT_URI, cv);
                    AKUtils.showToast("成功插入新的草稿！");
                } else {    //update
                    ContentValues cv=new ContentValues();
                    if (newContent.length()>Constants.INPUT_STRING_COUNT) {
                        newContent=newContent.substring(0, Constants.INPUT_STRING_COUNT);
                    }

                    cv.put(TwitterTable.DraftTbl.UID, currentUserId);
                    cv.put(TwitterTable.DraftTbl.USER_ID, currentUserId);
                    cv.put(TwitterTable.DraftTbl.CONTENT, newContent);
                    cv.put(TwitterTable.DraftTbl.IMG_URL, imgUrl);
                    cv.put(TwitterTable.DraftTbl.CREATED_AT, new Date().getTime());

                    resolver.update(Uri.withAppendedPath(TwitterTable.DraftTbl.CONTENT_URI, String.valueOf(mDraft.id)), cv, null, null);
                    AKUtils.showToast("成功更新草稿！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        NewStatusActivity.this.finish();
    }

    //--------------------- geo ---------------------
    private LocationClient mLocClient;
    public MyLocationListenner myListener=new MyLocationListenner();
    private boolean mIsStart;

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void stopMap() {
        WeiboLog.d(TAG, "stopMap.");
        mLocClient.stop();
        mIsStart=false;
    }

    private void startMap() {
        WeiboLog.d(TAG, "startMap.");
        mLocClient.start();
        mIsStart=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setLocationOption() {
        LocationClientOption option=new LocationClientOption();
        //option.setOpenGps();                //打开gps
        //option.setCoorType("");        //设置坐标类型
        option.setAddrType("all");        //设置地址信息，仅设置为“all”时有地址信息，默认无地址信息
        option.setScanSpan(1);    //设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
        mLocClient.setLocOption(option);
    }

    /**
     * 监听函数，又新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location==null) {
                return;
            }

            StringBuilder sb=new StringBuilder(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType()==BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType()==BDLocation.TypeNetWorkLocation) {
                sb.append("\n省：");
                sb.append(location.getProvince());
                sb.append("\n市：");
                sb.append(location.getCity());
                sb.append("\n区/县：");
                sb.append(location.getDistrict());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }
            sb.append("\nsdk version : ");
            sb.append(mLocClient.getVersion());
            //logMsg(sb.toString());
            WeiboLog.v(TAG, " sb:"+sb.toString());

            sb.setLength(0);
            sb.append(location.getCity()).append(location.getDistrict()).append(location.getAddrStr());
            longitude=location.getLongitude();
            latitude=location.getLatitude();
            String log=String.format(
                "纬度:%f 经度:%f",
                location.getLongitude(), location.getLatitude());
            mLocResultBtn.setText(TextUtils.isEmpty(location.getAddrStr()) ? log : location.getAddrStr());
            mClearLocBtn.setVisibility(View.VISIBLE);
            mLocResultBtn.setVisibility(View.VISIBLE);
            mLocProgressBar.setVisibility(View.GONE);

            stopMap();
            WeiboLog.v(TAG, " geo:"+sb.toString());
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation==null) {
                return;
            }
            StringBuffer sb=new StringBuffer(256);
            sb.append("Poi time : ");
            sb.append(poiLocation.getTime());
            sb.append("\nerror code : ");
            sb.append(poiLocation.getLocType());
            sb.append("\nlatitude : ");
            sb.append(poiLocation.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(poiLocation.getLongitude());
            sb.append("\nradius : ");
            sb.append(poiLocation.getRadius());
            if (poiLocation.getLocType()==BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(poiLocation.getAddrStr());
            }
            if (poiLocation.hasPoi()) {
                sb.append("\nPoi:");
                sb.append(poiLocation.getPoi());
            } else {
                sb.append("noPoi information");
            }
            //logMsg(sb.toString());
        }
    }

    //--------------------- 图片 ---------------------
    /**
     * 编辑
     */
    public static final int EDIT_PHOTO_PICKED_WITH_DATA=3029;

    public static final int CAMERA_WITH_DATA_TO_THUMB=3025;
    /*用来标识请求照相功能的 activity*/
    public static final int CAMERA_WITH_DATA=3023;
    /*用来标识请求 gallery 的 activity*/
    public static final int PHOTO_PICKED_WITH_DATA=3021;
    /*拍照的照片存储位置*/
    private static final File PHOTO_DIR=new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera");
    private File mCurrentPhotoFile;//照相机拍照得到的图片
    private static final int MAX_IMAGE_SIZE=5000000;
    Uri mPhotoUri=null;

    /**
     * 编辑照片
     */
    private void doEditPhoto() {
        try { // 启动 gallery 去剪辑这个照片
            Intent intent=new Intent("android.intent.action.EDIT");
            intent.setDataAndType(mPhotoUri, "image/*");
            startActivityForResult(intent, EDIT_PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到相关的编辑程序，现在裁剪。", Toast.LENGTH_LONG).show();
            startCropPhoto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理从相册中选择图片
     *
     * @param data
     */
    private void processGalleryData(Uri imageFileUri) {
        String[] proj={MediaStore.Images.Media.DATA};
        Cursor cur=null;

        try {
            WeiboLog.i(TAG, "imageFileUri:"+imageFileUri);
            cur=getContentResolver().query(imageFileUri, proj, null, null, null);
            int imageIdx=cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cur.moveToFirst();
            imgUrl=cur.getString(imageIdx);
            WeiboLog.i(TAG, "imgUrl:"+imgUrl);

            File file=new File(imgUrl);
            if (file.exists()) {
                if (file.length()>MAX_IMAGE_SIZE) {
                    AKUtils.showToast("上传的图片超过了5m，新浪不支持！");
                    clearImagePreview();
                    return;
                }
                mPhotoUri=imageFileUri;
                showPhoto(imageFileUri);
            }

        } catch (Exception e) {
            WeiboLog.e(e.toString());
        } finally {
            if (null!=cur) {
                cur.close();
            }
        }
    }

    private void doPickPhotoAction() {
        // Wrap our context to inflate list items using correct theme
        final Context dialogContext=new ContextThemeWrapper(NewStatusActivity.this, android.R.style.Theme_Light);
        String cancel=getString(R.string.new_back);
        String[] choices;
        choices=new String[2];
        choices[0]=getString(R.string.new_take_photo);            //拍照 
        choices[1]=getString(R.string.new_pick_photo);        //从相册中选择 
        final ListAdapter adapter=new ArrayAdapter<String>(dialogContext, android.R.layout.simple_list_item_1, choices);
        final AlertDialog.Builder builder=new AlertDialog.Builder(dialogContext);
        builder.setTitle(R.string.app_name);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0: {
                        String status=Environment.getExternalStorageState();
                        if (status.equals(Environment.MEDIA_MOUNTED)) {//判断是否有 SD 卡
                            doTakePhoto();
                            // 用户点击了从照相机 获取
                        } else {
                            AKUtils.showToast(R.string.new_no_sdcard, Toast.LENGTH_SHORT);
                        }
                        break;
                    }

                    case 1:
                        doPickPhotoFromGallery();// 从相册中去获 取
                        break;
                }
            }
        });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 拍照获取图片
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            // 创建照片的存储目录 
            mCurrentPhotoFile=new File(PHOTO_DIR, getPhotoFileName());
            // 给新照的照片文件命名

            final Intent intent=getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            AKUtils.showToast(R.string.new_photo_picker_not_found, Toast.LENGTH_LONG);
        }
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * 用当前时间给取得的图片命名,需要注意，如果文件名有空格，这货还取不到返回值
     */
    private String getPhotoFileName() {
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat=new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date)+".jpg";
    }

    // 请求 Gallery 程序
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            //final Intent intent=getPhotoPickIntent();
            //startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            Intent choosePictureIntent=new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePictureIntent, CAMERA_WITH_DATA_TO_THUMB);
        } catch (ActivityNotFoundException e) {
            AKUtils.showToast(R.string.new_photo_picker_not_found, Toast.LENGTH_LONG);
        }
    }

    // 封装请求 Gallery 的 intent 
    public static Intent getPhotoPickIntent() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", true);
        return intent;
    }

    protected void doCropPhoto() {
        doCropPhoto(mCurrentPhotoFile);
    }

    protected void doCropPhoto(File f) {
        try { // 启动 gallery 去剪辑这个照片
            final Intent intent=getCropImageIntent(Uri.fromFile(f));
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            //Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 裁剪，直接使用uri来处理。在没有图片编辑软件后调用这个裁剪功能。
     */
    protected void startCropPhoto() {
        try { // 启动 gallery 去剪辑这个照片
            final Intent intent=getCropImageIntent(mPhotoUri);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            Toast.makeText(this, "系统没有裁剪的程序！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Constructs an intent for image cropping. 调用图片剪辑程序
     */
    public static Intent getCropImageIntent(Uri photoUri) {
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        /*intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);*/
        intent.putExtra("return-data", true);
        return intent;
    }

    void showPhoto(Uri imageFileUri) throws FileNotFoundException {
        Display currentDisplay=getWindowManager().getDefaultDisplay();
        int dw=currentDisplay.getWidth();
        int dh=currentDisplay.getHeight()/2-100;

        // Load up the image's dimensions not the image itself
        BitmapFactory.Options bmpFactoryOptions=new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds=true;
        Bitmap bmp=BitmapFactory.decodeStream(
            getContentResolver().openInputStream(imageFileUri), null,
            bmpFactoryOptions);

        int heightRatio=(int) Math.ceil(bmpFactoryOptions.outHeight/(float) dh);
        int widthRatio=(int) Math.ceil(bmpFactoryOptions.outWidth/(float) dw);

        if (heightRatio>1&&widthRatio>1) {
            if (heightRatio>widthRatio) {
                bmpFactoryOptions.inSampleSize=heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize=widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds=false;
        bmp=BitmapFactory.decodeStream(
            getContentResolver().openInputStream(imageFileUri), null,
            bmpFactoryOptions);

        mPreview.setImageBitmap(bmp);

        mPreview.setVisibility(View.VISIBLE);
        mCloseImage.setVisibility(View.VISIBLE);
        mImageOperaBar.setVisibility(View.VISIBLE);
    }

    void showPhoto(String filename) {
        Display currentDisplay=getWindowManager().getDefaultDisplay();
        int dw=currentDisplay.getWidth();
        int dh=currentDisplay.getHeight()/2-100;

        // Load up the image's dimensions not the image itself
        BitmapFactory.Options bmpFactoryOptions=new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds=true;
        Bitmap bmp=BitmapFactory.decodeFile(filename, bmpFactoryOptions);

        int heightRatio=(int) Math.ceil(bmpFactoryOptions.outHeight/(float) dh);
        int widthRatio=(int) Math.ceil(bmpFactoryOptions.outWidth/(float) dw);

        if (heightRatio>1&&widthRatio>1) {
            if (heightRatio>widthRatio) {
                bmpFactoryOptions.inSampleSize=heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize=widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds=false;
        bmp=BitmapFactory.decodeFile(filename, bmpFactoryOptions);

        mPreview.setImageBitmap(bmp);

        mPreview.setVisibility(View.VISIBLE);
        mCloseImage.setVisibility(View.VISIBLE);
        mImageOperaBar.setVisibility(View.VISIBLE);
    }
}
