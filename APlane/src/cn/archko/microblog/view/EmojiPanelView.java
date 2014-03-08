package cn.archko.microblog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.smiley.AKSmiley;
import cn.archko.microblog.smiley.AKSmileyParser;
import cn.archko.microblog.smiley.AKTxtSmileyParser;

/**
 * 这是表情面板,包含所有的表情,可以扩展.作为独立的控件,可以在不同的地方使用.
 *
 * @author: archko 13-6-9
 */
public class EmojiPanelView extends LinearLayout {

    private static final String TAG="EmojiPanelView";

    TagsViewGroup mEmotionGridview;
    private GridAdapter mEmotionAdapter;
    Button btn_emoji_picture, btn_emoji_love, btn_emoji_pig, btn_emoji_wakeup, btn_emoji_kiss,
        btn_emoji_happy, btn_emoji_embarrased, btn_emoji_cry, btn_emoji_angry, btn_emoji_animal;
    private static final int MODE_PICTURE=0;
    private static final int MODE_TEXT=1;
    int mode=MODE_PICTURE;
    EditText mContent;

    public EmojiPanelView(Context context) {
        super(context);
        init(context);
    }

    public EmojiPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.emoji_panel, this);
        btn_emoji_picture=(Button) findViewById(R.id.btn_emoji_picture);
        btn_emoji_love=(Button) findViewById(R.id.btn_emoji_love);
        btn_emoji_pig=(Button) findViewById(R.id.btn_emoji_pig);
        btn_emoji_wakeup=(Button) findViewById(R.id.btn_emoji_wakeup);
        btn_emoji_kiss=(Button) findViewById(R.id.btn_emoji_kiss);
        btn_emoji_happy=(Button) findViewById(R.id.btn_emoji_happy);
        btn_emoji_embarrased=(Button) findViewById(R.id.btn_emoji_embarrased);
        btn_emoji_cry=(Button) findViewById(R.id.btn_emoji_cry);
        btn_emoji_angry=(Button) findViewById(R.id.btn_emoji_angry);
        btn_emoji_animal=(Button) findViewById(R.id.btn_emoji_animal);

        btn_emoji_picture.setOnClickListener(clickListener);
        btn_emoji_love.setOnClickListener(clickListener);
        btn_emoji_pig.setOnClickListener(clickListener);
        btn_emoji_wakeup.setOnClickListener(clickListener);
        btn_emoji_kiss.setOnClickListener(clickListener);
        btn_emoji_happy.setOnClickListener(clickListener);
        btn_emoji_embarrased.setOnClickListener(clickListener);
        btn_emoji_cry.setOnClickListener(clickListener);
        btn_emoji_angry.setOnClickListener(clickListener);
        btn_emoji_animal.setOnClickListener(clickListener);

        mEmotionGridview=(TagsViewGroup) findViewById(R.id.tags);
        mEmotionAdapter=new GridAdapter(getContext());

        getPictureEmoji();
    }

    OnClickListener clickListener=new OnClickListener() {
        @Override
        public void onClick(View view) {
            int id=view.getId();
            if (id==R.id.btn_emoji_picture) {
                mode=MODE_PICTURE;
                getPictureEmoji();
            } else {
                mode=MODE_TEXT;
                int resId=R.raw.emoji_angry;
                if (id==R.id.btn_emoji_angry) {
                } else if (id==R.id.btn_emoji_love) {
                    resId=R.raw.emoji_love;
                } else if (id==R.id.btn_emoji_pig) {
                    resId=R.raw.emoji_pig;
                } else if (id==R.id.btn_emoji_wakeup) {
                    resId=R.raw.emoji_wakeup;
                } else if (id==R.id.btn_emoji_kiss) {
                    resId=R.raw.emoji_kiss;
                } else if (id==R.id.btn_emoji_happy) {
                    resId=R.raw.emoji_happy;
                } else if (id==R.id.btn_emoji_embarrased) {
                    resId=R.raw.emoji_embarrassed;
                } else if (id==R.id.btn_emoji_cry) {
                    resId=R.raw.emoji_cry;
                } else if (id==R.id.btn_emoji_animal) {
                    resId=R.raw.emoji_animal;
                }
                getTextEmoji(resId);
            }
        }
    };

    private void getPictureEmoji() {
        mEmotionAdapter.setList(AKSmileyParser.getInstance(getContext()).mSmileyTexts);
        mEmotionGridview.setAdapter(mEmotionAdapter);
    }

    private void getTextEmoji(int resId) {
        String[] txts=AKTxtSmileyParser.getInstance().getSmileyTexts(getContext(), resId);
        if (null==txts) {
            Toast.makeText(getContext(), "解析表情出错.", Toast.LENGTH_LONG).show();
            return;
        }

        mEmotionAdapter.setList(txts);
        mEmotionGridview.setAdapter(mEmotionAdapter);
    }

    public void setContent(EditText content) {
        this.mContent=content;
    }

    public class GridAdapter extends BaseAdapter {

        private Context context;

        private LayoutInflater mInflater;

        private String[] list;

        public GridAdapter(Context c) {
            super();
            this.context=c;
        }

        public void setList(String[] list) {
            this.list=list;
            mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return list.length;
        }

        @Override
        public Object getItem(int index) {
            return list[index];
        }

        @Override
        public long getItemId(int index) {
            return 0;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            GridHolder holder;
            if (convertView==null) {
                convertView=mInflater.inflate(R.layout.emoji_item, null);
                holder=new GridHolder();
                holder.appImage=(ImageView) convertView.findViewById(R.id.itemImage);
                holder.textView=(TextView) convertView.findViewById(R.id.itemTxt);
                convertView.setTag(holder);
            } else {
                holder=(GridHolder) convertView.getTag();
            }

            if (mode==MODE_PICTURE) {
                holder.appImage.setVisibility(VISIBLE);
                holder.appImage.setImageDrawable(context.getResources().getDrawable(AKSmiley.mSmileyMap.get(list[index])));
                holder.textView.setText(null);
                holder.textView.setVisibility(GONE);
            } else {
                holder.appImage.setVisibility(GONE);
                holder.appImage.setImageDrawable(null);
                holder.textView.setVisibility(VISIBLE);
                holder.textView.setText(list[index]);
            }

            final int pos=index;
            holder.appImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(view, pos);
                }
            });
            holder.textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(view, pos);
                }
            });

            return convertView;
        }

        private class GridHolder {

            ImageView appImage;
            TextView textView;
        }

        private void onClickItem(View view, int pos) {
            if (null==mContent) {
                Log.e(VIEW_LOG_TAG, "没有编辑框,无法输入表情.");
                return;
            }

            String txt=list[pos];
            if (view instanceof ImageView) {
                refreshText(txt);
            } else if (view instanceof TextView) {
                refreshText2(txt);
            }
        }

    }

    private void refreshText(String charToBuild) {
        //String charToBuild=(String) mEmotionAdapter.getItem(position);
        String oldChar=mContent.getText().toString();
        //WeiboLog.d(TAG, "oldChar:"+oldChar);
        //WeiboLog.d(TAG, "charToBuild:"+charToBuild);

        int selection=mContent.getSelectionStart();
        CharSequence start=oldChar.subSequence(0, selection);
        CharSequence end=oldChar.subSequence(selection, oldChar.length());
        //WeiboLog.d(TAG, "start:"+start+"---end:"+end+" selection:"+selection);

        StringBuilder builder=new StringBuilder(start);
        builder.append(charToBuild).append(end);
        int newSel=selection+charToBuild.length();

        AKSmileyParser parser=AKSmileyParser.getInstance(getContext());
        CharSequence newChar=parser.addSmileySpans(builder);

        //WeiboLog.d(TAG, "newChar:"+newChar+" newSel:"+newSel);

        mContent.setText(newChar);
        mContent.setSelection(newSel);

        //09-27 11:05:07: D: oldChar:神马[神马] d兔子[兔子]ad嘻嘻[嘻嘻]aa
        //09-27 11:05:07: D: charToBuild:[哈哈]
        //09-27 11:05:07: D: start:神马[神马] d兔---end:子[兔子]ad嘻嘻[嘻嘻]aa selection:9
        //09-27 11:05:07: D: newChar:神马[神马] d兔[哈哈]子[兔子]ad嘻嘻[嘻嘻]aa newSel:13
    }

    /**
     * 加入文本表情
     *
     * @param charToBuild
     */
    private void refreshText2(String charToBuild) {
        String oldChar=mContent.getText().toString();

        int selection=mContent.getSelectionStart();
        CharSequence start=oldChar.subSequence(0, selection);
        CharSequence end=oldChar.subSequence(selection, oldChar.length());

        StringBuilder builder=new StringBuilder(start);
        int newSel=selection+charToBuild.length();
        builder.append(charToBuild).append(end);

        mContent.setText(builder.toString());
        mContent.setSelection(newSel);
    }
}