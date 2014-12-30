package cn.archko.microblog.filter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.archko.microblog.R;

public class AccountFiltersListAdapter extends ResourceCursorAdapter {

//    private static final String THIS_FILE = "AccEditListAd";

    public AccountFiltersListAdapter(Context context, Cursor c) {
        super(context, R.layout.filters_list_item, c, 0);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Filter filter = new Filter();
        filter.createFromDb(cursor);
        String filterDesc = filter.getRepresentation(context);

        TextView tv = (TextView) view.findViewById(R.id.line1);
        ImageView icon = (ImageView) view.findViewById(R.id.action_icon);

        tv.setText(filterDesc);
        icon.setContentDescription(filterDesc);
        /*switch (filter.action) {
            case Filter.ACTION_CAN_CALL:
                icon.setImageResource(R.drawable.ic_menu_goto);
                break;
            case Filter.ACTION_CANT_CALL:
                icon.setImageResource(R.drawable.ic_menu_blocked_user);
                break;
            case Filter.ACTION_REPLACE:
                icon.setImageResource(android.R.drawable.ic_menu_edit);
                break;
            case Filter.ACTION_DIRECTLY_CALL:
                icon.setImageResource(R.drawable.ic_menu_answer_call);
                break;
            case Filter.ACTION_AUTO_ANSWER:
                icon.setImageResource(R.drawable.ic_menu_auto_answer);
                break;
            default:
                break;
        }*/
    }
}
