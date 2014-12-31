package cn.archko.microblog.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.archko.microblog.view.TagsViewGroup;
import cn.archko.microblog.view.ThreadBeanItemView;

/**
 * @author archko
 */
public class RecyclerViewHolder implements RecyclerView.RecyclerListener {

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof SimpleViewHolder) {
            SimpleViewHolder simpleViewHolder = (SimpleViewHolder) viewHolder;
            View view = simpleViewHolder.baseItemView;
            if (view instanceof ThreadBeanItemView) {
                ThreadBeanItemView itemView = (ThreadBeanItemView) view;

                TagsViewGroup tagsViewGroup = itemView.mTagsViewGroup;
                if (null != tagsViewGroup) {
                    tagsViewGroup.setAdapter(null);
                    tagsViewGroup.removeAllViews();
                }
            }
        }
    }
}
