package cn.archko.microblog.recycler;

import android.view.View;
import android.widget.AbsListView.RecyclerListener;
import cn.archko.microblog.view.TagsViewGroup;
import cn.archko.microblog.view.ThreadBeanItemView;

/**
 * @author archko
 */
public class RecycleHolder implements RecyclerListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMovedToScrapHeap(final View view) {
        if (view instanceof ThreadBeanItemView) {
            ThreadBeanItemView itemView = (ThreadBeanItemView) view;

            TagsViewGroup tagsViewGroup = itemView.mTagsViewGroup;
            if (null != tagsViewGroup) {
                tagsViewGroup.setAdapter(null);
                tagsViewGroup.removeAllViews();
            }
        }

        /*MusicHolder holder=(MusicHolder) view.getTag();
        if (holder==null) {
            holder=new MusicHolder(view);
            view.setTag(holder);
        }

        // Release mBackground's reference
        if (holder.mBackground.get()!=null) {
            holder.mBackground.get().setImageDrawable(null);
            holder.mBackground.get().setImageBitmap(null);
        }

        // Release mImage's reference
        if (holder.mImage.get()!=null) {
            holder.mImage.get().setImageDrawable(null);
            holder.mImage.get().setImageBitmap(null);
        }

        // Release mLineOne's reference
        if (holder.mLineOne.get()!=null) {
            holder.mLineOne.get().setText(null);
        }

        // Release mLineTwo's reference
        if (holder.mLineTwo.get()!=null) {
            holder.mLineTwo.get().setText(null);
        }

        // Release mLineThree's reference
        if (holder.mLineThree.get()!=null) {
            holder.mLineThree.get().setText(null);
        }*/
    }

}
