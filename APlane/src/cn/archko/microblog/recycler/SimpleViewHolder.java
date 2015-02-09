package cn.archko.microblog.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author: archko 2014/12/30 :16:24
 */
public class SimpleViewHolder extends RecyclerView.ViewHolder {

    public View baseItemView;

    public SimpleViewHolder(View view) {
        super(view);
        baseItemView=view;
    }
}
