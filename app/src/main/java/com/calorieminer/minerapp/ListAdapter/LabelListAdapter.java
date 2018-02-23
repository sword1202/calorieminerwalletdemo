package com.calorieminer.minerapp.ListAdapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calorieminer.minerapp.CustomClass.AppConstants;
import com.calorieminer.minerapp.R;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.ArrayList;

public class LabelListAdapter extends BaseAdapter {

    private ArrayList<EntityAnnotation> objects;
    private static LayoutInflater inflater = null;
    private String formatSt;

    public LabelListAdapter(AppCompatActivity activity, ArrayList<EntityAnnotation> objects, String formatSt) {
        this.objects = objects;
        this.formatSt = formatSt;
        inflater = (LayoutInflater) activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class ViewHolder {

        TextView tv_title;
        TextView tv_value;
        ProgressBar progressBar;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            if (formatSt.matches(AppConstants.FACE_ADAPTER))
            {
                rowView = inflater.inflate(R.layout.face_list_item, null);

                holder.tv_title = (TextView) rowView.findViewById(R.id.tv_title);
                holder.tv_value = (TextView) rowView.findViewById(R.id.tv_value);
                // add image
            } else if (formatSt.matches(AppConstants.LABEL_ADAPTER) || formatSt.matches(AppConstants.FOOD_ADAPTER))
            {
                rowView = inflater.inflate(R.layout.label_list_item, null);
                holder.tv_title = (TextView) rowView.findViewById(R.id.label_title);
                holder.tv_value = (TextView) rowView.findViewById(R.id.label_value);
                holder.progressBar = (ProgressBar) rowView.findViewById(R.id.label_progressbar);

            }
            rowView.setTag(holder);

        } else
            holder = (ViewHolder)rowView.getTag();

        String title = objects.get(position).getDescription();
        holder.tv_title.setText(title);

        if (formatSt.matches(AppConstants.FACE_ADAPTER))
        {
            String mValue = getFirstCapitalFromString(objects.get(position).getMid());
            holder.tv_value.setText(mValue);

        } else if (formatSt.matches(AppConstants.LABEL_ADAPTER) || formatSt.matches(AppConstants.FOOD_ADAPTER))
        {
            int mValue = (int) (objects.get(position).getScore()*100);
            String strValue = "" + mValue + "%";
            holder.tv_value.setText(strValue);
            holder.progressBar.setProgress(mValue);
            holder.progressBar.getProgressDrawable().setColorFilter(android.graphics.Color.parseColor("#2ea565"), PorterDuff.Mode.SRC_IN);

            if (position == objects.size() - 1 && formatSt.matches(AppConstants.LABEL_ADAPTER)) {
                holder.tv_title.setTextColor(android.graphics.Color.BLACK);
                holder.progressBar.getProgressDrawable().setColorFilter(android.graphics.Color.BLUE, PorterDuff.Mode.SRC_IN);
            }
        }


        rowView.setClickable(false);
        rowView.setEnabled(false);
        return rowView;
    }

    private String getFirstCapitalFromString(String mString)
    {
        String[] strArray = mString.split("_");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            builder.append(cap + " ");
        }
        return builder.toString();
    }
}
