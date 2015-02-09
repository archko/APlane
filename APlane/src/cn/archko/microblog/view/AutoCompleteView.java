package cn.archko.microblog.view;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Filter;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko
 */
public class AutoCompleteView extends android.widget.AutoCompleteTextView implements Filter.FilterListener {

    static final boolean DEBUG=false;
    static final String TAG="AutoCompleteView";

    public AutoCompleteView(Context context) {
        this(context, null);
    }

    public AutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * <p>Performs the text completion by converting the selected item from
     * the drop down list into a string, replacing the text box's content with
     * this string and finally dismissing the drop down menu.</p>
     */
    public void performCompletion() {
        performCompletion(null, -1, -1);
    }

    private void performCompletion(View selectedView, int position, long id) {
        /*if (isPopupShowing()) {
            Object selectedItem;
            if (position<0) {
                selectedItem=mPopup.getSelectedItem();
            } else {
                selectedItem=mAdapter.getItem(position);
            }
            if (selectedItem==null) {
                Log.w(TAG, "performCompletion: no selected item");
                return;
            }

            mBlockCompletion=true;
            replaceText(convertSelectionToString(selectedItem));
            mBlockCompletion=false;

            if (mItemClickListener!=null) {
                final ListPopupWindow list=mPopup;

                if (selectedView==null||position<0) {
                    selectedView=list.getSelectedView();
                    position=list.getSelectedItemPosition();
                    id=list.getSelectedItemId();
                }
                mItemClickListener.itemClick(list.getListView(), selectedView, position, id);
            }
        }

        if (mDropDownDismissedOnCompletion) {
            dismissDropDown();
        }*/
    }

    /**
     * <p>Performs the text completion by replacing the current text by the
     * selected item. Subclasses should override this method to avoid replacing
     * the whole content of the edit box.</p>
     *
     * @param text the selected suggestion in the drop down list
     */
    protected void replaceText(CharSequence text) {
        clearComposingText();

        Editable editable=getText();
        String txt=editable.toString();
        int index=getSelectionStart();
        int end=getSelectionEnd();
        WeiboLog.d(TAG, "index:"+index+" txt:"+txt+" end:"+end);
        String startTxt=txt.substring(0, index);
        String endTxt=txt.substring(end);
        WeiboLog.d(TAG, "startTxt:"+startTxt+" endTxt:"+endTxt);

        /*08-03 15:59:32.714: D/AutoCompleteView(6606): index:11 txt:ygfsbnhgv @@ end:11
        08-03 15:59:32.714: D/AutoCompleteView(6606): startTxt:ygfsbnhgv @ endTxt:@*/

        String result=startTxt+text+endTxt;
        setText(result);
        // make sure we keep the caret at the end of the text view
        Editable spannable=getText();
        Selection.setSelection(spannable, spannable.length());
    }

    /**
     * <p>Displays the drop down on screen.</p>
     */
    public void showDropDown() {
        super.showDropDown();
    }
}
