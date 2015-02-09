package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.DraftListFragment;
import cn.archko.microblog.fragment.PickImageFragment;
import cn.archko.microblog.fragment.SearchDialogFragment;
import cn.archko.microblog.fragment.abs.AtUserListener;
import cn.archko.microblog.listeners.OnPickPhotoListener;
import cn.archko.microblog.location.BaiduLocation;
import cn.archko.microblog.location.Command;
import cn.archko.microblog.location.LocationCommand;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.view.AutoCompleteView;
import cn.archko.microblog.view.EmojiPanelView;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.me.microblog.App;
import com.me.microblog.bean.AKLocation;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.Draft;
import com.me.microblog.bean.SendTask;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.util.Date;

/**
 * @author root
 */
public class NewStatusActivity extends SkinFragmentActivity implements ActionBar.OnNavigationListener, OnPickPhotoListener {

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

    AKLocation mLocation;

    private String imgUrl="";
    boolean isDone=false;
    EmojiPanelView mEmojiPanelView;
    InputMethodManager imm;

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

    public static final int MODE_NORMAL=0;
    public static final int MODE_PICK_PHOTO=1;
    int mode=MODE_NORMAL;
    //--------------------- 认证 ---------------------

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
                //doPickPhotoAction();  //TODO
                pickPhoto();
                break;
            }

            case R.id.btn_location: {
                getLocation();
                break;
            }

            case R.id.btn_trend: {
                mEmojiPanelView.setVisibility(View.GONE);
                autoCompleteTrends();
                break;
            }

            case R.id.btn_at: {
                mEmojiPanelView.setVisibility(View.GONE);
                autoCompleteAt();
                break;
            }

            case R.id.btn_emo: {
                int emoVisible=mEmojiPanelView.getVisibility();
                if (emoVisible==View.VISIBLE) {
                    mEmojiPanelView.setVisibility(View.GONE);
                } else {
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
                NotifyUtils.showToast("Not implemted!");
                break;
            }

            case R.id.rotate_btn: {
                NotifyUtils.showToast("Not implemted!");
                break;
            }

            case R.id.filter_btn: {
                NotifyUtils.showToast("Not implemted!");
                break;
            }

            case R.id.edit_btn: {
                //doEditPhoto();    //TODO
                break;
            }

            default:
                break;
        }
    }

    private void pickPhoto() {
        Fragment newFragment=new PickImageFragment();
        WeiboLog.v(TAG, "pickPhoto:"+imgUrl);
        if (!TextUtils.isEmpty(imgUrl)) {
            Bundle args=new Bundle();
            args.putString(PickImageFragment.KEY_PHOTO, imgUrl);
            newFragment.setArguments(args);
        }
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        ft.add(android.R.id.content, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

        mode=MODE_PICK_PHOTO;
        updateActionBar();
    }

    @Override
    public void onPickOne(String path) {
        imgUrl=path;
        WeiboLog.v(TAG, "pick:"+path);
        getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
            FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mode=MODE_NORMAL;
        updateActionBar();
    }

    /**
     * 对于不同的状态,处理不同的ActionBar.
     */
    private void updateActionBar() {
        if (mode==MODE_NORMAL) {
            mActionBar.setTitle(R.string.text_new_status);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        } else {
            mActionBar.setTitle(R.string.txt_pick_photo);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    /**
     * 发微博。
     */
    private void sendWeibo() {
        imm.hideSoftInputFromWindow(content.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        String contentString=content.getEditableText().toString();
        if (TextUtils.isEmpty(contentString)) {
            NotifyUtils.showToast(R.string.new_status_should_not_null);
            return;
        }

        int len=contentString.length();
        if (len>Constants.INPUT_STRING_COUNT) {
            NotifyUtils.showToast(R.string.text_exceed_max_num);
            return;
        }
        //send.setEnabled(false);

        addTask(contentString);
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

        startMap();

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
        int postId=R.drawable.send_light;
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
            WeiboLog.d(TAG, "onOptionsItemSelected:"+mode);
            if (mode==MODE_NORMAL) {
            } else {
                getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                mode=MODE_NORMAL;
                updateActionBar();
                return true;
            }
            showExistDialog();
        } else if (MENU_FIRST==itemId) {
            getDraft();
        } else if (MENU_SECOND==itemId) {
            sendWeibo();
        }

        return super.onOptionsItemSelected(item);
    }

    void clearLocation() {
        mLocation=null;
        mLocResultBtn.setText(null);
        mClearLocBtn.setVisibility(View.GONE);
        mLocResultBtn.setVisibility(View.GONE);
    }

    void getLocation() {
        clearLocation();
        mLocProgressBar.setVisibility(View.VISIBLE);
        startMap();
    }

    /**
     * 显示退出对话框
     */
    void showExistDialog() {
        String contentString=content.getEditableText().toString();
        if (TextUtils.isEmpty(contentString)&&TextUtils.isEmpty(imgUrl)) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {    //TODO 需要处理返回的视频的情况.
            /*if (requestCode==CAMERA_WITH_DATA_TO_THUMB) {
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
            } else*/
            if (requestCode==REQUEST_DRAFT) {
                Draft draft=(Draft) data.getSerializableExtra("draft");
                if (null!=draft) {
                    mDraft=draft;
                    initDraft(draft);
                }
            }/* else if (requestCode==EDIT_PHOTO_PICKED_WITH_DATA) {
                processGalleryData(data.getData());
            }*/
        } else {
            //clearImagePreview();
        }
    }

    private void addTask(String content) {
        if (content.length()>Constants.INPUT_STRING_COUNT) {
            content=content.substring(0, Constants.INPUT_STRING_COUNT);
            NotifyUtils.showToast(R.string.new_status_too_more_txt);
            return;
        }

        Intent taskService=new Intent(NewStatusActivity.this, SendTaskService.class);
        SendTask task=new SendTask();
        task.uid=currentUserId;
        task.userId=currentUserId;
        task.content=content;
        if (null!=mLocation) {
            task.data=mLocation.latitude+"-"+mLocation.longitude;
        }
        task.type=TwitterTable.SendQueueTbl.SEND_TYPE_STATUS;
        task.imgUrl=imgUrl;
        task.createAt=new Date().getTime();
        task.text=String.valueOf(selectedPos);
        taskService.putExtra("send_task", task);
        NewStatusActivity.this.startService(taskService);
        NotifyUtils.showToast("新微博任务添加到队列服务中了。");
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

        showCompleteFragment(2);
    }

    @Override
    public void onBackPressed() {
        WeiboLog.d(TAG, "onBackPressed:"+mode);
        if (mode==MODE_NORMAL) {
            if (!isDone) {
                showExistDialog();
            } else {
                if (TextUtils.isEmpty(imgUrl)) {
                    super.onBackPressed();
                } else {
                    showExistDialog();
                }
            }
        } else {
            getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mode=MODE_NORMAL;
            updateActionBar();
        }

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
        overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
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
                //showPhoto(imgUrl);    //TODO
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
                NotifyUtils.showToast("内容为空，不保存！");
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
                    NotifyUtils.showToast("成功插入新的草稿！");
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
                    NotifyUtils.showToast("成功更新草稿！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        NewStatusActivity.this.finish();
    }

    private void startMap() {
        WeiboLog.d(TAG, "startMap.");

        final BaiduLocation location=new BaiduLocation();
        //下面这个应该放在EmployeeBaiduLocation里面处理的.
        location.setMyListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation==null) {
                    return;
                }
                AKLocation akLocation=new AKLocation(bdLocation.getLongitude(), bdLocation.getLatitude());
                akLocation.mLocationTimestamp=System.currentTimeMillis();
                akLocation.addr=bdLocation.getAddrStr();
                ((App) App.getAppContext()).setLocation(akLocation);
                if (!isFinishing()) {
                    mLocation=akLocation;
                    String log=String.format(
                        "纬度:%f 经度:%f",
                        bdLocation.getLongitude(), bdLocation.getLatitude());
                    mLocResultBtn.setText(TextUtils.isEmpty(bdLocation.getAddrStr()) ? log : bdLocation.getAddrStr());
                    mClearLocBtn.setVisibility(View.VISIBLE);
                    mLocResultBtn.setVisibility(View.VISIBLE);
                    mLocProgressBar.setVisibility(View.GONE);
                }
            }
        });
        Command command=new LocationCommand(location);
        command.execute();
    }

    //--------------------- 图片 ---------------------
    private static final int MAX_IMAGE_SIZE=5000000;
    Uri mPhotoUri=null;

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
                    NotifyUtils.showToast("上传的图片超过了5m，新浪不支持！");
                    clearImagePreview();
                    return;
                }
                mPhotoUri=imageFileUri;
                //showPhoto(imageFileUri);
            }

        } catch (Exception e) {
            WeiboLog.e(e.toString());
        } finally {
            if (null!=cur) {
                cur.close();
            }
        }
    }
}
