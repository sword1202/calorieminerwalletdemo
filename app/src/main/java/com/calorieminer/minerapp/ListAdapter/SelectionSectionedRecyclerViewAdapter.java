package com.calorieminer.minerapp.ListAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.calorieminer.minerapp.R;
import com.calorieminer.minerapp.SelectionActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import java.util.Arrays;
import java.util.Comparator;

public class SelectionSectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private int mSectionResourceId;
    private LayoutInflater mLayoutInflater;
    private FirebaseRecyclerAdapter mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<Section>();


    @SuppressLint("StaticFieldLeak")
    public static TextView iHave, progressOfRound;
    public static Button sameButton, differentButton, randomButton;
    public static String tag = "SelectionActivity - ";

    public static TextView tv_waitingWhoText;
    public static TextView messageTextStr;


    public SelectionSectionedRecyclerViewAdapter(Context context, int sectionResourceId) {

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSectionResourceId = sectionResourceId;
        mContext = context;

    }


    public static class SectionViewHolder extends RecyclerView.ViewHolder {


        public SectionViewHolder(View view) {
            super(view);

            tv_waitingWhoText = view.findViewById(R.id.watingwhotext);
//            selectionLayout = view.findViewById(R.id.selectionlayout);
//            feedbackLayout = view.findViewById(R.id.feedbackLayout);
            messageTextStr = view.findViewById(R.id.message_title);
            sameButton = view.findViewById(R.id.s_button1);
            differentButton = view.findViewById(R.id.s_button2);
            randomButton = view.findViewById(R.id.s_button3);

            iHave = view.findViewById(R.id.s_textView2);
            progressOfRound = view.findViewById(R.id.s_textView4);

        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
            return new SectionViewHolder(view);
        }else{
            return mBaseAdapter.onCreateViewHolder(parent, typeView -1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
//            SelectionActivity.init();
        }else{
            mBaseAdapter.onBindViewHolder(sectionViewHolder,sectionedPositionToPosition(position));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) +1 ;
    }


    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }
    }

    public void setmBaseAdapter(FirebaseRecyclerAdapter firebaseRecyclerAdapter) {
        mBaseAdapter = firebaseRecyclerAdapter;
        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }


    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }


    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }

}
