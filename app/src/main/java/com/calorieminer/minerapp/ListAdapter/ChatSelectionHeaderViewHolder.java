package com.calorieminer.minerapp.ListAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by p1 on 1/9/18.
 */

public class ChatSelectionHeaderViewHolder extends RecyclerView.ViewHolder {

    TextView textView;

    public ChatSelectionHeaderViewHolder(View itemView) {
        super(itemView);
    }

    public void render(String text){
        textView.setText(text);
    }
}
