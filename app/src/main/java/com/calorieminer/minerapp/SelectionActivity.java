package com.calorieminer.minerapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;
import com.calorieminer.minerapp.FriendlyChat.FriendlyMessage;
import com.calorieminer.minerapp.ListAdapter.SelectionSectionedRecyclerViewAdapter;
import com.calorieminer.minerapp.ListAdapter.FeedbackSectionedRecyclerViewAdapter;
import com.calorieminer.minerapp.model.GameParam;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.hash.Hashing;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


public class SelectionActivity extends AppCompatActivity{

    static String dBUID;
    static GameParam gameParam;
    static ProgressBar progressBarForRounds;
    public static ArrayList<Integer> userlSelection, user2Selection, user1Assignement, user2Assignement;
    public static int[] mediaIds = {0, 0, 0, 0};
    public static SoundPool soundPool;
    static String waitTitle = "";
    static MediaPlayer mediaPlayer;
    static boolean soundsOn, isTriedToExit;
    static boolean isActiveSelectionActivity;
    public static DatabaseReference dbFB, mFirebaseDatabaseReference, messagesRef, updateDBRef;
    private static ValueEventListener messageEventListener, updateDBValueEventListener;

    // mesasging
    public FirebaseAuth mFirebaseAuth;
    public static FirebaseUser mFirebaseUser;
    public static String mUsername;
    public static String mPhotoUrl;

    private static RecyclerView mMessageRecyclerView_selection;
    private static RecyclerView mMessageRecyclerView_feedback;
    static RelativeLayout selectionRecylerLayout;
    private LinearLayoutManager mLinearLayoutManager_selection, mLinearLayoutManager_feedback;

    static Context context;
    static Activity activity;
    public static final String MESSAGES_CHILD = "private_messages";
    private static EditText mMessageEditText;
    private static FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter_selection, mFirebaseAdapter_feedback;
    static SelectionSectionedRecyclerViewAdapter mSectionedAdapter_selection;
    static FeedbackSectionedRecyclerViewAdapter mSectionedAdapter_feedback;

    static LinearLayout inputBoxLayout;
    static RelativeLayout rootLayout;

    public Action getIndexApiAction() {
        return Actions.newView("Chat", "http://friendlychat.firebase.google.com/message");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isActiveSelectionActivity = false;

        if (messageEventListener != null)
            messagesRef.removeEventListener(messageEventListener);
        if (updateDBValueEventListener != null)
            updateDBRef.removeEventListener(updateDBValueEventListener);
        super.onDestroy();

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        TextView shah256HashTextView;
        CircleImageView messengerImageView;
        RelativeLayout sha256Layout;
        LinearLayout mMessageLayout;

        MessageViewHolder(View v) {
            super(v);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerTextView = itemView.findViewById(R.id.messengerTextView);
            shah256HashTextView = itemView.findViewById(R.id.shah256hashTextView);

            shah256HashTextView.setTypeface(Typeface.createFromAsset(context.getAssets(), "DroidSansMono.ttf"));

            messengerImageView = itemView.findViewById(R.id.messengerImageView);
            sha256Layout = itemView.findViewById(R.id.shahLayout);
            mMessageLayout = itemView.findViewById(R.id.messagelayout);
        }
    }

    public static float dpToPx(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 100, metrics);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_feedback);
        context = SelectionActivity.this;
        activity = SelectionActivity.this;
        mMessageRecyclerView_selection = findViewById(R.id.messageRecyclerView_selection);
        mMessageRecyclerView_feedback = findViewById(R.id.messageRecyclerView_feedback);

        inputBoxLayout = findViewById(R.id.inputBoxLayout);
        rootLayout = findViewById(R.id.root_layout);

        mMessageEditText = findViewById(R.id.messageEditText);


        dbFB = FirebaseDatabase.getInstance().getReference("GameParam");
        if (dbFB == null)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        progressBarForRounds = findViewById(R.id.s_progressBarForRounds);
        initializeSoundPool();
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null)
            return;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        isTriedToExit = false;
        isActiveSelectionActivity = true;
        if (isActiveSelectionActivity && !ResultsActivity.isResultActivity){

            // SELECTION ***

            mLinearLayoutManager_selection = new LinearLayoutManager(this);
            mMessageRecyclerView_selection.setLayoutManager(mLinearLayoutManager_selection);

            List<SelectionSectionedRecyclerViewAdapter.Section> sections_selection =
                    new ArrayList<>();

            //Sections of Selection part
            sections_selection.add(new SelectionSectionedRecyclerViewAdapter.Section(0,"Section 1"));
            SelectionSectionedRecyclerViewAdapter.Section[] dummy_selection = new SelectionSectionedRecyclerViewAdapter.Section[sections_selection.size()];
            mSectionedAdapter_selection = new
                    SelectionSectionedRecyclerViewAdapter(this,R.layout.chat_header_selection);
            mSectionedAdapter_selection.setSections(sections_selection.toArray(dummy_selection));
            mMessageRecyclerView_selection.setAdapter(mSectionedAdapter_selection);

            selectionRecylerLayout = findViewById(R.id.selection_recycler_layout);

            selectionRecylerLayout.setVisibility(View.VISIBLE);

            // FEEDBACK ***

            mLinearLayoutManager_feedback = new LinearLayoutManager(this);
            mMessageRecyclerView_feedback.setLayoutManager(mLinearLayoutManager_feedback);
            List<FeedbackSectionedRecyclerViewAdapter.Section> sections_feedback =
                    new ArrayList<>();

            //Sections of Feedback part
            sections_feedback.add(new FeedbackSectionedRecyclerViewAdapter.Section(0,"Section 2"));
            FeedbackSectionedRecyclerViewAdapter.Section[] dummy_feedback = new FeedbackSectionedRecyclerViewAdapter.Section[sections_feedback.size()];
            mSectionedAdapter_feedback = new
                    FeedbackSectionedRecyclerViewAdapter(this,R.layout.chat_header_feedback);
            mSectionedAdapter_feedback.setSections(sections_feedback.toArray(dummy_feedback));
            mMessageRecyclerView_feedback.setAdapter(mSectionedAdapter_feedback);

            mMessageRecyclerView_feedback.setVisibility(View.INVISIBLE);

            initMessaging();

        }

    }

    private void initMessaging() {

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {

            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            Uri mUri = mFirebaseUser.getPhotoUrl();
            if (mUri == null)
                return;
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mLinearLayoutManager_selection.setStackFromEnd(false);
        mLinearLayoutManager_selection.setReverseLayout(false);

        mLinearLayoutManager_feedback.setStackFromEnd(false);
        mLinearLayoutManager_feedback.setReverseLayout(false);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(dBUID);

        SnapshotParser<FriendlyMessage> parser_selection = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot snapshot) {
                FriendlyMessage friendlyMessage = snapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(snapshot.getKey());
                }
                return friendlyMessage;
            }
        };



        FirebaseRecyclerOptions<FriendlyMessage> options_selection =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser_selection)
                        .build();


        // ** Selection ViewHolder
        mFirebaseAdapter_selection = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options_selection) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }
            @Override
            public FriendlyMessage getItem(int position) {
                return getSnapshots().get(getItemCount() - 1 - position);
            }
            @Override
            public void onDataChanged() {
                mFirebaseAdapter_selection.notifyDataSetChanged();
//                mSectionedAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder,
                                            int position,
                                            FriendlyMessage friendlyMessage) {

//                String senderID = friendlyMessage.getImageUrl();

                ViewGroup.LayoutParams lp = viewHolder.mMessageLayout.getLayoutParams();
                lp.height = 0;

//                if (senderID != null && senderID.equals(mFirebaseUser.getUid()))
//                {
//                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//
//                    if (friendlyMessage.getText() != null) {
//                        viewHolder.messageTextView.setText(friendlyMessage.getText());
//                        viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
//                        viewHolder.messageImageView.setVisibility(ImageView.GONE);
//                        String shahText = getSha256Hash(friendlyMessage.getText());
//                        viewHolder.shah256HashTextView.setText(shahText);
//                        viewHolder.sha256Layout.setVisibility(View.VISIBLE);
//
//                    }
//
//                    viewHolder.messengerTextView.setText(friendlyMessage.getName());
//
//                    if (friendlyMessage.getPhotoUrl() == null) {
//                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this,
//                                R.drawable.ic_account_circle_black_36dp));
//                    } else if (friendlyMessage.getPhotoUrl().equals("dummy")) {
//                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this,
//                                R.drawable.ic_backspace_white_24dp));
//                    } else if (friendlyMessage.getPhotoUrl().equals("avatar")) {
//                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this,
//                                R.drawable.avatar_profile));
//                    }
//                    else {
//                        Glide.with(SelectionActivity.this)
//                                .load(friendlyMessage.getPhotoUrl())
//                                .into(viewHolder.messengerImageView);
//                    }
//                } else
//                {
//                    lp.height = 0;
//                }

                viewHolder.mMessageLayout.setLayoutParams(lp);

            }
        };

        mFirebaseAdapter_selection.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mMessageRecyclerView_selection.smoothScrollToPosition(0);

            }
        });

        mSectionedAdapter_selection.setmBaseAdapter(mFirebaseAdapter_selection);

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter((int) RemoteConfigParam.getInstance(SelectionActivity.this).getMax_winbit_message())});

        mMessageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                {
                    mMessageRecyclerView_selection.setAlpha(0.3f);
                }
            }
        });

        // ** Feedback ViewHolder

        SnapshotParser<FriendlyMessage> parser_feedback = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot snapshot) {
                FriendlyMessage friendlyMessage = snapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(snapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        FirebaseRecyclerOptions<FriendlyMessage> options_feedback =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser_feedback)
                        .build();

        mFirebaseAdapter_feedback = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options_feedback) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }
            @Override
            public FriendlyMessage getItem(int position) {
                return getSnapshots().get(getItemCount() - 1 - position);
            }
            @Override
            public void onDataChanged() {
                mFirebaseAdapter_feedback.notifyDataSetChanged();
//                mSectionedAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder,
                                            int position,
                                            FriendlyMessage friendlyMessage) {

                String senderID = friendlyMessage.getImageUrl();
                ViewGroup.LayoutParams lp = viewHolder.mMessageLayout.getLayoutParams();

                if (senderID != null && senderID.equals("dummy"))
                {
                    lp.height = 0;
                } else
                {
                    if (friendlyMessage.getText() != null) {
                        viewHolder.messageTextView.setText(friendlyMessage.getText());
                        viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                        viewHolder.messageImageView.setVisibility(ImageView.GONE);

                        String shahText = getSha256Hash(friendlyMessage.getText());
                        viewHolder.shah256HashTextView.setText(shahText);
                        viewHolder.sha256Layout.setVisibility(View.VISIBLE);

                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    }

                    viewHolder.messengerTextView.setText(friendlyMessage.getName());

                    if (friendlyMessage.getPhotoUrl() == null) {
                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this,
                                R.drawable.ic_account_circle_black_36dp));
                    } else if (friendlyMessage.getPhotoUrl().equals("dummy")) {
                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this,
                                R.drawable.ic_backspace_white_24dp));
                    } else if (friendlyMessage.getPhotoUrl().equals("avatar")) {
                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this,
                                R.drawable.avatar_profile));
                    }
                    else {
                        Glide.with(SelectionActivity.this)
                                .load(friendlyMessage.getPhotoUrl())
                                .into(viewHolder.messengerImageView);
                    }
                }

                viewHolder.mMessageLayout.setLayoutParams(lp);

            }
        };

        mFirebaseAdapter_feedback.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mMessageRecyclerView_feedback.smoothScrollToPosition(0);

            }
        });

        mSectionedAdapter_feedback.setmBaseAdapter(mFirebaseAdapter_feedback);




        if (ObtainContactActivity.isAvatar || MainActivity.myState.equals("s")){
            messagesRef.setValue(null);
            dummyMessage();
        } else
        {
            dummyMessage();
//            init();
//            checkAvailabilityChatRoom();
        }

        Button mDoneButton = findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputBoxLayout.setVisibility(View.GONE);
                mMessageRecyclerView_selection.setAlpha(1.0f);
                SelectionSectionedRecyclerViewAdapter.messageTextStr.setVisibility(View.GONE);
                mMessageEditText.clearFocus();
                hideKeyboard();

            }
        });

    }

    private void dummyMessage()
    {
        FriendlyMessage friendlyMessage = new FriendlyMessage("", "",
                "dummy", "dummy");
        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(dBUID).push().setValue(friendlyMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                init();
            }
        });

        checkAvailabilityChatRoom();
    }

    static void sendMessage()
    {
        String message = mMessageEditText.getText().toString();

        FriendlyMessage friendlyMessage;

        if (!message.matches(""))
        {
            friendlyMessage = new FriendlyMessage(message, mUsername,
                    mPhotoUrl, mFirebaseUser.getUid());
            mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(dBUID).push().setValue(friendlyMessage);
            mMessageEditText.setText("");
        }

        if (ObtainContactActivity.isAvatar)
        {

            String mAvatarName = "Calorie Miner Avatar";
            String mAvatarPhoto = "avatar";
            friendlyMessage = new FriendlyMessage("This message is from the Avatar.", mAvatarName,
                    mAvatarPhoto, "avatar_id");
            mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(dBUID).push().setValue(friendlyMessage);

        }
    }

    private static void checkAvailabilityChatRoom() {

        messageEventListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    if (isTriedToExit)
                    {
                        ResultsActivity.isResultActivity = true;
                        exitApp();
                    } else
                        exitApp("You are exiting the app.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void exitApp(String message) {

        new AlertDialog.Builder(context,R.style.DateTImePicker)
                .setTitle("Warning")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, SignInActivity.class);
                        intent.putExtra("EXIT", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        activity.startActivity(intent);
                        activity.finish();

                    }
                })

                .show();
    }

    private void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

    }

    public void initializeSoundPool() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(1)
                    .build();
        } else
        {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }

        mediaIds[0] = soundPool.load(this, R.raw.sound_2_correct, 1);
        mediaIds[1] = soundPool.load(this, R.raw.sound_both_incorrect, 1);
        mediaIds[2] = soundPool.load(this, R.raw.sound_only_i_correct, 1);
        mediaIds[3] = soundPool.load(this, R.raw.sound_they_correct, 1);


    }

    public static void init() {

        if (ObtainContactActivity.isAvatar)
        {
            playWithAvatar();

        } else {

            playWithHuman();
        }

        SelectionSectionedRecyclerViewAdapter.sameButton.setOnClickListener(mClickListner);
        SelectionSectionedRecyclerViewAdapter.differentButton.setOnClickListener(mClickListner);
        SelectionSectionedRecyclerViewAdapter.randomButton.setOnClickListener(mClickListner);
        FeedbackSectionedRecyclerViewAdapter.continueButton.setOnClickListener(mClickListner);

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                if (heightDiff < dpToPx(context) && mMessageEditText.hasFocus()) {
                    inputBoxLayout.setVisibility(View.GONE);
                    mMessageRecyclerView_selection.setAlpha(1.0f);
                    SelectionSectionedRecyclerViewAdapter.messageTextStr.setVisibility(View.GONE);
                    mMessageEditText.clearFocus();
                }

            }
        });

    }

    private static void playWithAvatar() {

        progressBarForRounds.setProgress(ObtainContactActivity.counts);
        userlSelection = new ArrayList<>();
        user2Selection = new ArrayList<>();
        progressBarForRounds.setMax(GameActivity.totalRounds);

        mMessageRecyclerView_feedback.setVisibility(View.INVISIBLE);
        selectionRecylerLayout.setVisibility(View.VISIBLE);
        FeedbackSectionedRecyclerViewAdapter.continueButton.setText(R.string.btn_continue);
        SelectionSectionedRecyclerViewAdapter.iHave.setText(String.valueOf(user1Assignement.get(ObtainContactActivity.counts)));
        SelectionSectionedRecyclerViewAdapter.sameButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? context.getResources().getString(R.string.same_1) : context.getResources().getString(R.string.same_0));
        SelectionSectionedRecyclerViewAdapter.differentButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? context.getResources().getString(R.string.different_0) : context.getResources().getString(R.string.different_1));

        String displayRound = "Block " + (ObtainContactActivity.counts+1) + " of " + GameActivity.totalRounds + ".";
        SelectionSectionedRecyclerViewAdapter.progressOfRound.setText(displayRound);

    }

    private static void playWithHuman() {

        new updateDB(dbFB).execute();

    }

    @SuppressLint("StaticFieldLeak")
    public static class updateDB extends AsyncTask<Void, Void, Void> {

        DatabaseReference mReferenece;

        private updateDB(DatabaseReference reference) {
            mReferenece = reference;
        }

        @Override
        protected Void doInBackground(Void... params) {

            updateDBRef = mReferenece.child(dBUID);
            updateDBValueEventListener = updateDBRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (isActiveSelectionActivity)
                    {
                        if (dataSnapshot.exists())
                        {
                            gameParam = dataSnapshot.getValue(GameParam.class);

                            processingGame();
                        } else
                            Toast.makeText(context, R.string.normal_error, Toast.LENGTH_LONG).show();
                    }
                }

                private void processingGame() {

                    GameActivity.totalRounds = gameParam.getTotal_rounds();

                    userlSelection = new ArrayList<>();
                    user2Selection = new ArrayList<>();


                    if (MainActivity.myState.equals("s")) {

                        user1Assignement = gameParam.getStarterAssignment();
                        user2Assignement = gameParam.getJoinerAssignment();

                        SelectionSectionedRecyclerViewAdapter.tv_waitingWhoText.setText(gameParam.isWaitFlag() ? context.getResources().getString(R.string.waiting_you) : context.getResources().getString(R.string.waiting_them));

                        if (gameParam.getUser1Selection() != null)
                            userlSelection = gameParam.getUser1Selection();

                        if (gameParam.getUser2Selection() != null)
                            user2Selection = gameParam.getUser2Selection();

                        GameActivity.myScore = gameParam.getStarterScore();
                        GameActivity.otherScore = gameParam.getJoinerScore();

                        setClickableButtons(gameParam.isWaitFlag());

                        FeedbackSectionedRecyclerViewAdapter.continueButton.setText(R.string.btn_continue);

                        mMessageRecyclerView_feedback.setVisibility(View.INVISIBLE);
                        selectionRecylerLayout.setVisibility(View.VISIBLE);

                        if (gameParam.getUser2Selection() != null) {
                            if (user2Selection.size() == ObtainContactActivity.counts + 1) {
                                selectionRecylerLayout.setVisibility(View.GONE);
                                mMessageRecyclerView_feedback.setVisibility(View.VISIBLE);
                                updateScore();
                            }
                        }

                        if (gameParam.isWaitFlag()) // clickable button
                        {
                            // wait with disabled buttons if joiner didnt select "continue"
                            if (gameParam.isReadyJoinerToStart())
                            {
                                SelectionSectionedRecyclerViewAdapter.sameButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.same_1) + waitTitle) : (context.getResources().getString(R.string.same_0) + waitTitle));
                                SelectionSectionedRecyclerViewAdapter.differentButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.different_0) + waitTitle) : (context.getResources().getString(R.string.different_1) + waitTitle));

                                SelectionSectionedRecyclerViewAdapter.sameButton.setAlpha(waitTitle.matches("") ? 1.0f : 0.3f);
                                SelectionSectionedRecyclerViewAdapter.differentButton.setAlpha(waitTitle.matches("") ? 1.0f : 0.3f);
                            } else
                            {
                                SelectionSectionedRecyclerViewAdapter.sameButton.setClickable(false);
                                SelectionSectionedRecyclerViewAdapter.differentButton.setClickable(false);
                                SelectionSectionedRecyclerViewAdapter.sameButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? context.getResources().getString(R.string.same_1_wait) : context.getResources().getString(R.string.same_0_wait));
                                SelectionSectionedRecyclerViewAdapter.differentButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? context.getResources().getString(R.string.different_0_wait) : context.getResources().getString(R.string.different_1_wait));

                                SelectionSectionedRecyclerViewAdapter.sameButton.setAlpha(0.3f);
                                SelectionSectionedRecyclerViewAdapter.differentButton.setAlpha(0.3f);
                            }

                        } else // generate " --- SELECTED" when i click
                        {

                            changeButtonTitleToSelected();

                        }


                    } else {

                        user1Assignement = gameParam.getJoinerAssignment();
                        user2Assignement = gameParam.getStarterAssignment();

                        SelectionSectionedRecyclerViewAdapter.tv_waitingWhoText.setText(gameParam.isWaitFlag() ? context.getResources().getString(R.string.waiting_them) : context.getResources().getString(R.string.waiting_you));

                        if (gameParam.getUser2Selection() != null)
                            userlSelection = gameParam.getUser2Selection();
                        if (gameParam.getUser1Selection() != null)
                            user2Selection = gameParam.getUser1Selection();

                        GameActivity.myScore = gameParam.getJoinerScore();
                        GameActivity.otherScore = gameParam.getStarterScore();

                        setClickableButtons(!gameParam.isWaitFlag());
                        FeedbackSectionedRecyclerViewAdapter.continueButton.setText(R.string.btn_continue);
                        mMessageRecyclerView_feedback.setVisibility(View.INVISIBLE);
                        selectionRecylerLayout.setVisibility(View.VISIBLE);


                        if (gameParam.getUser2Selection() != null) {

                            if (userlSelection.size() == ObtainContactActivity.counts + 1) {
                                selectionRecylerLayout.setVisibility(View.GONE);
                                mMessageRecyclerView_feedback.setVisibility(View.VISIBLE);
                                updateScore();
                            }

                        }

                        SelectionSectionedRecyclerViewAdapter.sameButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.same_1) + waitTitle) : (context.getResources().getString(R.string.same_0) + waitTitle));
                        SelectionSectionedRecyclerViewAdapter.differentButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.different_0) + waitTitle) : (context.getResources().getString(R.string.different_1) + waitTitle));

                        SelectionSectionedRecyclerViewAdapter.sameButton.setAlpha(waitTitle.matches("") ? 1.0f : 0.3f);
                        SelectionSectionedRecyclerViewAdapter.differentButton.setAlpha(waitTitle.matches("") ? 1.0f : 0.3f);
                    }

                    progressBarForRounds.setMax(GameActivity.totalRounds);
                    progressBarForRounds.setProgress(ObtainContactActivity.counts);
                    progressBarForRounds.setProgressTintList(ColorStateList.valueOf(Color.RED));

                    SelectionSectionedRecyclerViewAdapter.iHave.setText(String.valueOf(user1Assignement.get(ObtainContactActivity.counts)));


                    String displayRound = "Block " + (ObtainContactActivity.counts + 1) + " of " + GameActivity.totalRounds + ".";
                    SelectionSectionedRecyclerViewAdapter.progressOfRound.setText(displayRound);
                }

                private void changeButtonTitleToSelected() { // for only Starter's case

                    String title_wait;
                    if (user1Assignement.get(ObtainContactActivity.counts) == 1)
                    {
                        if (userlSelection.get(ObtainContactActivity.counts) == 1)
                        {
                            title_wait = context.getResources().getString(R.string.same_1) + waitTitle;
                            SelectionSectionedRecyclerViewAdapter.sameButton.setText(title_wait);
                            SelectionSectionedRecyclerViewAdapter.differentButton.setText(context.getResources().getString(R.string.different_0));
                            SelectionSectionedRecyclerViewAdapter.differentButton.setAlpha(0.3f);
                        } else
                        {
                            title_wait = context.getResources().getString(R.string.different_0) + waitTitle;
                            SelectionSectionedRecyclerViewAdapter.sameButton.setText(context.getResources().getString(R.string.same_1));
                            SelectionSectionedRecyclerViewAdapter.differentButton.setText(title_wait);
                            SelectionSectionedRecyclerViewAdapter.sameButton.setAlpha(0.3f);
                        }
                    } else
                    {
                        if (userlSelection.get(ObtainContactActivity.counts) == 0)
                        {
                            title_wait = context.getResources().getString(R.string.same_0) + waitTitle;
                            SelectionSectionedRecyclerViewAdapter.sameButton.setText(title_wait);
                            SelectionSectionedRecyclerViewAdapter.differentButton.setText(context.getResources().getString(R.string.different_1));
                            SelectionSectionedRecyclerViewAdapter.differentButton.setAlpha(0.3f);
                        } else
                        {
                            title_wait = context.getResources().getString(R.string.different_1) + waitTitle;
                            SelectionSectionedRecyclerViewAdapter.sameButton.setText(context.getResources().getString(R.string.same_0));
                            SelectionSectionedRecyclerViewAdapter.differentButton.setText(title_wait);
                            SelectionSectionedRecyclerViewAdapter.sameButton.setAlpha(0.3f);
                        }
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return null;
        }
    }


    private static void updateScore() {

        // udpate UI

        String tempstr;
        tempstr = "Result for Block " + (ObtainContactActivity.counts + 1) + " of " + GameActivity.totalRounds + ".";
        FeedbackSectionedRecyclerViewAdapter.roundsNum.setText(tempstr);

        tempstr = "You had: " + String.valueOf(user1Assignement.get(ObtainContactActivity.counts));
        FeedbackSectionedRecyclerViewAdapter.iHad.setText(tempstr);

        tempstr = "They had: " + String.valueOf(user2Assignement.get(ObtainContactActivity.counts));
        FeedbackSectionedRecyclerViewAdapter.theyHad.setText(tempstr);

        String str_i_correctORnot = (Objects.equals(gameParam.getJoinerAssignment().get(ObtainContactActivity.counts), gameParam.getUser1Selection().get(ObtainContactActivity.counts)) ? "CORRECT" : "INCORRECT");
        String str_they_correctORnot = (Objects.equals(gameParam.getStarterAssignment().get(ObtainContactActivity.counts), gameParam.getUser2Selection().get(ObtainContactActivity.counts)) ? "CORRECT" : "INCORRECT");

        if (MainActivity.myState.equals("s"))
        {

            if (gameParam.getStarterAssignment().get(ObtainContactActivity.counts) == 1)
            {
                tempstr = "You selected: " + (gameParam.getUser1Selection().get(ObtainContactActivity.counts) == 0 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.iSelected.setText(tempstr);
            } else
            {
                tempstr = "You selected: " + (gameParam.getUser1Selection().get(ObtainContactActivity.counts) == 1 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.iSelected.setText(tempstr);
            }

            if (gameParam.getJoinerAssignment().get(ObtainContactActivity.counts) == 1)
            {
                tempstr = "They selected: " + (gameParam.getUser2Selection().get(ObtainContactActivity.counts) == 0 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.theySelected.setText(tempstr);
            } else
            {
                tempstr = "They selected: " + (gameParam.getUser2Selection().get(ObtainContactActivity.counts) == 1 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.theySelected.setText(tempstr);
            }

            if (gameParam.isStarterSound() && !ObtainContactActivity.initFlag1)
            {
                // play music
                ObtainContactActivity.initFlag1 = true;

                new playSoundWithMediaPlayer(str_i_correctORnot, str_they_correctORnot).execute();
            }


            tempstr = "You were : " + str_i_correctORnot;
            FeedbackSectionedRecyclerViewAdapter.iCorrectOrNot.setText(tempstr);

            tempstr = "They were : " + str_they_correctORnot;
            FeedbackSectionedRecyclerViewAdapter.theyCorrectOrNot.setText(tempstr);

            tempstr = "Your score : " + gameParam.getStarterScore() + "/" + (ObtainContactActivity.counts+1);
            FeedbackSectionedRecyclerViewAdapter.iScore.setText(tempstr);
            GameActivity.otherScore = gameParam.getJoinerScore();
            tempstr = "Their score : " + GameActivity.otherScore + "/" + (ObtainContactActivity.counts+1);
            FeedbackSectionedRecyclerViewAdapter.theirScore.setText(tempstr);
        } else
        {

            if (gameParam.getJoinerAssignment().get(ObtainContactActivity.counts) == 1)
            {
                tempstr = "You selected: " + (gameParam.getUser2Selection().get(ObtainContactActivity.counts) == 0 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.iSelected.setText(tempstr);
            } else
            {
                tempstr = "You selected: " + (gameParam.getUser2Selection().get(ObtainContactActivity.counts) == 1 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.iSelected.setText(tempstr);
            }

            if (gameParam.getStarterAssignment().get(ObtainContactActivity.counts) == 1)
            {
                tempstr = "They selected: " + (gameParam.getUser1Selection().get(ObtainContactActivity.counts) == 0 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.theySelected.setText(tempstr);
            } else
            {
                tempstr = "They selected: " + (gameParam.getUser1Selection().get(ObtainContactActivity.counts) == 1 ? "DIFFERENT" : "SAME");
                FeedbackSectionedRecyclerViewAdapter.theySelected.setText(tempstr);
            }


            if (gameParam.isJoinerSound() && !ObtainContactActivity.initFlag2)
            {
                new playSoundWithMediaPlayer(str_i_correctORnot, str_they_correctORnot).execute();
            }

            tempstr = "You were : " + str_they_correctORnot;
            FeedbackSectionedRecyclerViewAdapter.iCorrectOrNot.setText(tempstr);

            tempstr = "They were : " + str_i_correctORnot;
            FeedbackSectionedRecyclerViewAdapter.theyCorrectOrNot.setText(tempstr);

            tempstr = "Your score : " + gameParam.getJoinerScore() + "/" + (ObtainContactActivity.counts+1);
            FeedbackSectionedRecyclerViewAdapter.iScore.setText(tempstr);
            GameActivity.otherScore = gameParam.getStarterScore();
            tempstr = "Their score : " + GameActivity.otherScore + "/" + (ObtainContactActivity.counts+1);
            FeedbackSectionedRecyclerViewAdapter.theirScore.setText(tempstr);
        }



        if (ObtainContactActivity.counts == GameActivity.totalRounds - 1)
        {
            ObtainContactActivity.counts ++;
            progressBarForRounds.setProgress(ObtainContactActivity.counts);
            progressBarForRounds.setProgressTintList(ColorStateList.valueOf(Color.RED));
            FeedbackSectionedRecyclerViewAdapter.continueButton.setText(R.string.view_result);

        }
        if (ObtainContactActivity.counts >= GameActivity.totalRounds) ObtainContactActivity.counts--;
    }

    private static void setClickableButtons(boolean clickable)
    {
        SelectionSectionedRecyclerViewAdapter.sameButton.setClickable(clickable);
        SelectionSectionedRecyclerViewAdapter.differentButton.setClickable(clickable);

        if (MainActivity.myState.equals("s"))
        {
            waitTitle = (clickable ? "" : " - SELECTED");
        } else
        {
            waitTitle = (clickable ? "" : " - WAIT");
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showFeedback(){

        Random rand = new Random();
        int n1 = rand.nextInt(2);
        user2Selection.add(n1);
        updateUI();
        selectionRecylerLayout.setVisibility(View.GONE);
        mMessageRecyclerView_feedback.setVisibility(View.VISIBLE);

    }

    public static void showResults(){

        ObtainContactActivity.counts ++;

        if (ObtainContactActivity.initFlag1) ObtainContactActivity.initFlag1 = false;
        if (ObtainContactActivity.initFlag2) ObtainContactActivity.initFlag2 = false;

        if (ObtainContactActivity.isAvatar)
        {
            SelectionSectionedRecyclerViewAdapter.sameButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.same_1) + waitTitle) : (context.getResources().getString(R.string.same_0) + waitTitle));
            SelectionSectionedRecyclerViewAdapter.differentButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.different_0) + waitTitle) : (context.getResources().getString(R.string.different_1) + waitTitle));

        } else
        {
            if (MainActivity.myState.equals("s"))
            {

                SelectionSectionedRecyclerViewAdapter.tv_waitingWhoText.setText(gameParam.isWaitFlag() ? "Waiting on: YOU" : "Waiting on: THEM");

                setClickableButtons(gameParam.isWaitFlag());
                new updateDB(dbFB).execute();
            } else
            {
                SelectionSectionedRecyclerViewAdapter.tv_waitingWhoText.setText(gameParam.isWaitFlag() ? "Waiting on: THEM" : "Waiting on: YOU");
                setClickableButtons(!gameParam.isWaitFlag());
//                gameParam.setReadyJoinerToStart(true);
//                FirebaseDatabase.getInstance().getReference().child("GameParam").child(dBUID).setValue(gameParam);
//                new updateDB(dbFB).execute();
                SelectionSectionedRecyclerViewAdapter.sameButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.same_1) + waitTitle) : (context.getResources().getString(R.string.same_0) + waitTitle));
                SelectionSectionedRecyclerViewAdapter.differentButton.setText(user1Assignement.get(ObtainContactActivity.counts) == 1 ? (context.getResources().getString(R.string.different_0) + waitTitle) : (context.getResources().getString(R.string.different_1) + waitTitle));

                SelectionSectionedRecyclerViewAdapter.sameButton.setAlpha(waitTitle.matches("") ? 1.0f : 0.3f);
                SelectionSectionedRecyclerViewAdapter.differentButton.setAlpha(waitTitle.matches("") ? 1.0f : 0.3f);

//                if (!gameParam.isWaitFlag())
//                {
//                    sameButton.setText(user1Assignement.get(counts) == 1 ? ("1 - SAME") : ("0 - SAME"));
//                    differentButton.setText(user1Assignement.get(counts) == 1 ? ("0 - DIFFERENT") : ("1 - DIFFERENT"));
//                }
            }

        }

        mMessageRecyclerView_feedback.setVisibility(View.INVISIBLE);
        selectionRecylerLayout.setVisibility(View.VISIBLE);

        progressBarForRounds.setMax(GameActivity.totalRounds);
        progressBarForRounds.setProgress(ObtainContactActivity.counts);
        progressBarForRounds.setProgressTintList(ColorStateList.valueOf(Color.RED));

        SelectionSectionedRecyclerViewAdapter.iHave.setText(String.valueOf(user1Assignement.get(ObtainContactActivity.counts)));

        String displayRound = "Block " + (ObtainContactActivity.counts+1) + " of " + GameActivity.totalRounds + ".";
        SelectionSectionedRecyclerViewAdapter.progressOfRound.setText(displayRound);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void updateUI() {

        String tempStr;
        tempStr = "Result for Block " + (ObtainContactActivity.counts + 1) + " of " + GameActivity.totalRounds + ".";
        FeedbackSectionedRecyclerViewAdapter.roundsNum.setText(tempStr);

        tempStr = "You had: " + user1Assignement.get(ObtainContactActivity.counts);
        FeedbackSectionedRecyclerViewAdapter.iHad.setText(tempStr);

        tempStr = "They had: " + String.valueOf(user2Assignement.get(ObtainContactActivity.counts));
        FeedbackSectionedRecyclerViewAdapter.theyHad.setText(tempStr);

        if (user1Assignement.get(ObtainContactActivity.counts) == 1)
        {
            tempStr = "You selected: " + (userlSelection.get(ObtainContactActivity.counts) == 0 ? "DIFFERENT" : "SAME");
            FeedbackSectionedRecyclerViewAdapter.iSelected.setText(tempStr);
        } else
        {
            tempStr = "You selected: " + (userlSelection.get(ObtainContactActivity.counts) == 0 ? "SAME" : "DIFFERENT");
            FeedbackSectionedRecyclerViewAdapter.iSelected.setText(tempStr);
        }

        if (user2Assignement.get(ObtainContactActivity.counts) == 1)
        {
            tempStr = "They selected: " + (user2Selection.get(ObtainContactActivity.counts) == 0 ? "DIFFERENT" : "SAME");
            FeedbackSectionedRecyclerViewAdapter.theySelected.setText(tempStr);
        } else
        {
            tempStr = "They selected: " + (user2Selection.get(ObtainContactActivity.counts) == 0 ? "SAME" : "DIFFERENT");
            FeedbackSectionedRecyclerViewAdapter.theySelected.setText(tempStr);
        }


        // play sounds

        String str_i_correctORnot = (Objects.equals(user2Assignement.get(ObtainContactActivity.counts), userlSelection.get(ObtainContactActivity.counts)) ? "CORRECT" : "INCORRECT");
        String str_they_correctORnot = (Objects.equals(user1Assignement.get(ObtainContactActivity.counts), user2Selection.get(ObtainContactActivity.counts)) ? "CORRECT" : "INCORRECT");

        if (soundsOn) {

            new playSoundWithMediaPlayer(str_i_correctORnot, str_they_correctORnot).execute();
        }

        tempStr = "You were : " + str_i_correctORnot;
        FeedbackSectionedRecyclerViewAdapter.iCorrectOrNot.setText(tempStr);

        tempStr = "They were : " + str_they_correctORnot;
        FeedbackSectionedRecyclerViewAdapter.theyCorrectOrNot.setText(tempStr);

        GameActivity.myScore += (Objects.equals(user2Assignement.get(ObtainContactActivity.counts), userlSelection.get(ObtainContactActivity.counts)) ? 1 : 0);

        tempStr = "Your score : " + String.valueOf(GameActivity.myScore) + "/" + (ObtainContactActivity.counts+1);
        FeedbackSectionedRecyclerViewAdapter.iScore.setText(tempStr);

        GameActivity.otherScore += (Objects.equals(user1Assignement.get(ObtainContactActivity.counts), user2Selection.get(ObtainContactActivity.counts)) ? 1 : 0);

        tempStr = "Their score : " + String.valueOf(GameActivity.otherScore) + "/" + (ObtainContactActivity.counts+1);
        FeedbackSectionedRecyclerViewAdapter.theirScore.setText(tempStr);

        if (ObtainContactActivity.counts == GameActivity.totalRounds - 1)
        {
            ObtainContactActivity.counts ++;
            progressBarForRounds.setProgress(ObtainContactActivity.counts);
            progressBarForRounds.setProgressTintList(ColorStateList.valueOf(Color.RED));
            FeedbackSectionedRecyclerViewAdapter.continueButton.setText(R.string.view_result);

        }
    }

    static Button.OnClickListener mClickListner = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.s_button1: // same
                {

//                    playBeep();
                    sendMessage();
                    if (ObtainContactActivity.isAvatar)
                    {
                        if (SelectionSectionedRecyclerViewAdapter.sameButton.getText().equals(context.getResources().getString(R.string.same_1)))
                        {
                            userlSelection.add(1);
                        } else
                        {
                            userlSelection.add(0);
                        }
                        showFeedback();

                    } else if (gameParam.isWaitFlag() && MainActivity.myState.equals("s"))
                    {
                        if (SelectionSectionedRecyclerViewAdapter.sameButton.getText().equals(context.getResources().getString(R.string.same_1)))
                        {
                            userlSelection.add(1);
                        } else
                        {
                            userlSelection.add(0);
                        }

                        updateStarter();

                    } else if (!gameParam.isWaitFlag() && MainActivity.myState.equals("j")){

                        if (SelectionSectionedRecyclerViewAdapter.sameButton.getText().equals(context.getResources().getString(R.string.same_1)))
                        {
                            userlSelection.add(1);
                        } else
                        {
                            userlSelection.add(0);
                        }

                        updateJoiner();
                    }

                }
                break;

                case R.id.s_button2: // different
                {
                    sendMessage();
                    if (ObtainContactActivity.isAvatar)
                    {
                        if (SelectionSectionedRecyclerViewAdapter.differentButton.getText().equals(context.getResources().getString(R.string.different_0)))
                        {
                            userlSelection.add(0);
                        } else
                        {
                            userlSelection.add(1);
                        }
                        showFeedback();

                    } else if (gameParam.isWaitFlag() && MainActivity.myState.equals("s"))
                    {
                        if (SelectionSectionedRecyclerViewAdapter.differentButton.getText().equals(context.getResources().getString(R.string.different_0)))
                        {
                            userlSelection.add(0);
                        } else
                        {
                            userlSelection.add(1);
                        }

                        updateStarter();
                    } else if (!gameParam.isWaitFlag() && MainActivity.myState.equals("j")){

                        if (SelectionSectionedRecyclerViewAdapter.differentButton.getText().equals(context.getResources().getString(R.string.different_0)))
                        {
                            userlSelection.add(0);
                        } else
                        {
                            userlSelection.add(1);
                        }

                        updateJoiner();
                    }

                }

                break;

                case R.id.s_button3: // random

                {
//                    playBeep();

                    if (ObtainContactActivity.isAvatar)
                    {
                        Random rand = new Random();
                        int n1 = rand.nextInt(2);
                        userlSelection.add(n1);
                        showFeedback();

                    } else if (gameParam.isWaitFlag() && MainActivity.myState.equals("s"))
                    {
                        Random rand = new Random();
                        int n1 = rand.nextInt(2);
                        userlSelection.add(n1);

                        updateStarter();
                    } else if (!gameParam.isWaitFlag() && MainActivity.myState.equals("j")){

                        Random rand = new Random();
                        int n1 = rand.nextInt(2);
                        userlSelection.add(n1);

                        updateJoiner();
                    }
                }

                break;

                case R.id.button1: // continue

                    if (ObtainContactActivity.isAvatar)
                    {
                        if (ObtainContactActivity.counts < GameActivity.totalRounds)
                        {
                            showResults();
                        } else
                        {
                            Intent intent = new Intent(context, ResultsActivity.class);
                            context.startActivity(intent);
                            activity.finish();
                        }
                    } else
                    {
                        if (ObtainContactActivity.counts < GameActivity.totalRounds-1)
                        {
                            showResults();
                        } else
                        {
                            notifyFinishedState();

                        }
                    }

                    inputBoxLayout.setVisibility(View.VISIBLE);
                    SelectionSectionedRecyclerViewAdapter.messageTextStr.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private static void notifyFinishedState() {

        Intent intent = new Intent(context, ResultsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
        activity.finish();
    }

    private static void updateJoiner() {

        GameActivity.myScore = gameParam.getJoinerScore() + (Objects.equals(gameParam.getStarterAssignment().get(ObtainContactActivity.counts), userlSelection.get(ObtainContactActivity.counts)) ? 1 : 0);
        gameParam.setJoinerScore(GameActivity.myScore);
        gameParam.setUser2Selection(userlSelection);
        gameParam.setWaitFlag(true);
//        gameParam.setReadyJoinerToStart(false);
        FirebaseDatabase.getInstance().getReference().child("GameParam").child(dBUID).setValue(gameParam);
        new updateDB(dbFB).execute();
    }

    private static void updateStarter() {

        GameActivity.myScore = gameParam.getStarterScore() + (Objects.equals(gameParam.getJoinerAssignment().get(ObtainContactActivity.counts), userlSelection.get(ObtainContactActivity.counts)) ? 1 : 0);
        gameParam.setStarterScore(GameActivity.myScore);
        gameParam.setUser1Selection(userlSelection);
        gameParam.setWaitFlag(false);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("GameParam").child(dBUID);
        dbRef.setValue(gameParam);

        new updateDB(dbFB).execute();
    }

    private static void playSoundWithSoundPool(String iSelectedString, String theySelectedString)
    {
        ObtainContactActivity.initFlag1 = true;
        if (iSelectedString.equals("CORRECT") && theySelectedString.equals("CORRECT"))
        {
            soundPool.play(mediaIds[0], 25f, .25f, 1, 0, 1);
        }
        if (iSelectedString.equals("CORRECT") && theySelectedString.equals("INCORRECT"))
        {
            soundPool.play(mediaIds[2], 25f, .25f, 1, 0, 1);
        }
        if (iSelectedString.equals("INCORRECT") && theySelectedString.equals("CORRECT"))
        {
            soundPool.play(mediaIds[3], 25f, .25f, 1, 0, 1);
        }
        if (iSelectedString.equals("INCORRECT") && theySelectedString.equals("INCORRECT"))
        {
            soundPool.play(mediaIds[1], 25f, .25f, 1, 0, 1);
        }
    }



    @SuppressLint("StaticFieldLeak")
    private static class playSoundWithMediaPlayer extends AsyncTask<Void, Void, Void> {

        String iSelectedString, theySelectedString;

        private playSoundWithMediaPlayer(String iSelectedString, String theySelectedString)
        {
            this.iSelectedString = iSelectedString;
            this.theySelectedString = theySelectedString;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            if (MainActivity.myState.equals("s"))
            {
                ObtainContactActivity.initFlag1 = true;
                if (iSelectedString.equals("CORRECT") && theySelectedString.equals("CORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_2_correct);
                    mediaPlayer.start();
                }
                if (iSelectedString.equals("CORRECT") && theySelectedString.equals("INCORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_only_i_correct);
                    mediaPlayer.start();
                }
                if (iSelectedString.equals("INCORRECT") && theySelectedString.equals("CORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_they_correct);
                    mediaPlayer.start();
                }
                if (iSelectedString.equals("INCORRECT") && theySelectedString.equals("INCORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_both_incorrect);
                    mediaPlayer.start();
                }
            } else
            {
                ObtainContactActivity.initFlag2 = true;

                if (iSelectedString.equals("CORRECT") && theySelectedString.equals("CORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_2_correct);
                    mediaPlayer.start();
                }
                if (theySelectedString.equals("CORRECT") && iSelectedString.equals("INCORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_only_i_correct);
                    mediaPlayer.start();
                }
                if (theySelectedString.equals("INCORRECT") && iSelectedString.equals("CORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_they_correct);
                    mediaPlayer.start();
                }
                if (iSelectedString.equals("INCORRECT") && theySelectedString.equals("INCORRECT"))
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.sound_both_incorrect);
                    mediaPlayer.start();
                }
            }

            return null;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(null)
                .setMessage(R.string.back_button_message)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mFirebaseAdapter_feedback.stopListening();
                        mFirebaseAdapter_selection.stopListening();
                        messagesRef.setValue(null);
                        isTriedToExit = true;
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })

                .show();
    }

    private String getSha256Hash(String str) {

        String shahStr = "";
        if (str.matches(""))
            return "";

        shahStr = Hashing.sha256()
                .hashString(str, StandardCharsets.UTF_8)
                .toString();

        // split it every 4 characters

        List<String> strings = new ArrayList<String>();
        int index = 0;
        int enterNextLine = 1;
        while (index < shahStr.length()) {
            if (enterNextLine%4 == 0 && enterNextLine < 16)
                strings.add(shahStr.substring(index, Math.min(index + 4, shahStr.length())) + ":\n");
            else
                strings.add(shahStr.substring(index, Math.min(index + 4, shahStr.length())) + ":");

            enterNextLine ++;
            index += 4;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); i ++) {
            builder.append(strings.get(i));
        }
        return builder.toString();

    }

    private static void exitApp() {

        Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra("EXIT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void onPause() {
        mFirebaseAdapter_selection.stopListening();
        mFirebaseAdapter_feedback.stopListening();
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter_selection.startListening();
        mFirebaseAdapter_feedback.startListening();
    }
}
