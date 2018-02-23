package com.calorieminer.minerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.calorieminer.minerapp.CustomClass.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.calorieminer.minerapp.model.GameParam;
import com.calorieminer.minerapp.model.Joiner;
import com.calorieminer.minerapp.model.Starter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import static com.calorieminer.minerapp.SelectionActivity.MESSAGES_CHILD;


public class GameActivity extends AppCompatActivity {

    public String mUsername, mUserEmail;
    public FirebaseAuth mFirebaseAuth;
    public FirebaseUser mFirebaseUser;
    static GameParam gameParam;
    private DatabaseReference dbFB, checkChatDataBaseRef;
    ChildEventListener chechChatDatabaseChildEventListener;

    Button btn_decrease, btn_increase, btn_start, btn_exit;
    TextView tv_blinksCounts, tv_myName, tv_theirName;
    CheckBox beepCheckBox;
    public static Integer totalRounds, myScore, otherScore;
    static String dBUID, whosName;
    static Starter selectedStarter;
    static Joiner selectedJoiner;
    String tag = "GameActivity - ";
    ValueEventListener gameParamValueEventListener;

    static boolean isGameActivity;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myScore = 0;
        otherScore = 0;
        isGameActivity = false;
        if (gameParamValueEventListener != null)
            dbFB.child("GameParam").child(dBUID).removeEventListener(gameParamValueEventListener);

        if (chechChatDatabaseChildEventListener != null)
            checkChatDataBaseRef.removeEventListener(chechChatDatabaseChildEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isGameActivity)
            init();
        else finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        isGameActivity = true;
        dbFB = FirebaseDatabase.getInstance().getReference();


    }

    private void getEveryAssignmentForPlayers() {


        SelectionActivity.user1Assignement = new ArrayList<>();
        SelectionActivity.user2Assignement = new ArrayList<>();

        for (int i = 0; i < totalRounds; i++)
        {
            Random rand = new Random();
            int n1 = rand.nextInt(2);
            int n2 = rand.nextInt(2);

            SelectionActivity.user1Assignement.add(n1);
            SelectionActivity.user2Assignement.add(n2);

        }



        if (!ObtainContactActivity.isAvatar)
        {
            gameParam.setWaitFlag(true);
            gameParam.setStarterAssignment(SelectionActivity.user1Assignement);
            gameParam.setJoinerAssignment(SelectionActivity.user2Assignement);

            dbFB.child("GameParam").child(dBUID).setValue(gameParam);
        }

    }

    private void init() {

        tv_myName = findViewById(R.id.textView2);
        tv_theirName = findViewById(R.id.textView3);

        btn_start = findViewById(R.id.button1);
        btn_decrease = findViewById(R.id.decreaseRoundBtn);
        btn_increase = findViewById(R.id.increaseRoundBtn);
        btn_exit = findViewById(R.id.exit_btn);

        tv_blinksCounts = findViewById(R.id.round);
        beepCheckBox = findViewById(R.id.soundcheckbox);

        beepCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ObtainContactActivity.isAvatar)
                {
                    SelectionActivity.soundsOn = beepCheckBox.isChecked();
                } else
                {
                    if (MainActivity.myState.equals("s"))
                    {
                        gameParam.setStarterSound(beepCheckBox.isChecked());
                        dbFB.child("GameParam").child(dBUID).setValue(gameParam);
                    } else
                    {
                        gameParam.setJoinerSound(beepCheckBox.isChecked());
                        dbFB.child("GameParam").child(dBUID).setValue(gameParam);
                    }
                }
            }
        });

        if (ObtainContactActivity.isAvatar) // avatar
        {
            dbFB = null;
            SelectionActivity.soundsOn = true;
            totalRounds = 10;
            beepCheckBox.setChecked(true);
            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if (mFirebaseUser == null)
                return;

            dBUID = mFirebaseUser.getUid();
            SelectionActivity.dBUID = dBUID;

            mUsername = mFirebaseUser.getDisplayName();
            mUserEmail = mFirebaseUser.getEmail();

            tv_myName.setText(mUsername);

            SessionIdentifierGenerator sID = new SessionIdentifierGenerator();
            String avatarName = sID.nextSessionId();

            tv_theirName.setText(avatarName);

            tv_blinksCounts.setText(String.valueOf(totalRounds));
        } else if (MainActivity.myState.equals("s") && selectedStarter != null) // server
        {
            dBUID = selectedStarter.getUID() + selectedJoiner.getUid();
            SelectionActivity.dBUID = dBUID;
            whosName = selectedStarter.getUserName() + "_" + selectedJoiner.getUserName();
            // initialize
            gameParam = new GameParam(whosName, 10, null, null, null, false, false, 0, 0, null, null, true, true, true, false, false);
            dbFB.child("GameParam").child(dBUID).setValue(gameParam);

            updateDB();

            tv_myName.setText(selectedStarter.getUserName());
            tv_theirName.setText(selectedJoiner.getUserName());

            tv_blinksCounts.setText(String.valueOf(10));
            btn_start.setText(R.string.wait); // wait them...
            btn_start.setClickable(false);

        } else if (MainActivity.myState.equals("j") && selectedJoiner != null) // customer
        {

            dBUID = selectedStarter.getUID() + selectedJoiner.getUid();
            SelectionActivity.dBUID = dBUID;
            whosName = selectedStarter.getUserName() + "_" + selectedJoiner.getUserName();

            updateDB();
            tv_myName.setText(selectedJoiner.getUserName());
            tv_theirName.setText(selectedStarter.getUserName());

            // hide customizing button

            btn_decrease.setVisibility(View.INVISIBLE);
            btn_increase.setVisibility(View.INVISIBLE);

        }
    }

    private void updateDB() {
        gameParamValueEventListener = dbFB.child("GameParam").child(dBUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gameParam = dataSnapshot.getValue(GameParam.class);
                if (gameParam == null)
                    return;
                totalRounds = gameParam.getTotal_rounds();
                tv_blinksCounts.setText(String.valueOf(totalRounds));

                tv_blinksCounts.setText(String.valueOf(gameParam.getTotal_rounds()));

                if (MainActivity.myState.equals("j") && isGameActivity)
                {
                    beepCheckBox.setChecked(gameParam.isJoinerSound());
                    btn_start.setText(gameParam.isStarted() ? getResources().getString(R.string.wait_for_starter) : getResources().getString(R.string.start_button));
                    btn_start.setAlpha(gameParam.isStarted() ? 0.3f : 1.0f);

                    if (gameParam.isStarted() && gameParam.getStarterAssignment() != null)
                    {
                        SelectionActivity.gameParam = gameParam;
                        SelectionActivity.user1Assignement = gameParam.getJoinerAssignment();
                        SelectionActivity.user2Assignement = gameParam.getStarterAssignment();

                        Intent intent = new Intent(GameActivity.this, SelectionActivity.class);
                        startActivity(intent);
                        finish();

//                        checkChatDataBaseRef = FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD).child(dBUID);
//                        chechChatDatabaseChildEventListener = checkChatDataBaseRef.addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                Intent intent = new Intent(GameActivity.this, SelectionActivity.class);
//                                startActivity(intent);
//                                finish();
//                            }
//
//                            @Override
//                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                            }
//
//                            @Override
//                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                        Intent intent = new Intent(GameActivity.this, SelectionActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                        startActivity(intent);
//                        finish();
                    }

                } else if (MainActivity.myState.equals("s") && isGameActivity)
                {
                    SelectionActivity.gameParam = gameParam;
                    beepCheckBox.setChecked(gameParam.isStarterSound());
                    btn_start.setClickable(gameParam.isStarted());
                    btn_start.setText(gameParam.isStarted() ? getResources().getString(R.string.start_button) : getResources().getString(R.string.wait));
                    btn_start.setAlpha(gameParam.isStarted() ? 1.0f : 0.3f);

                    if (gameParam.getStarterAssignment() != null)
                    {
                        SelectionActivity.user1Assignement = gameParam.getStarterAssignment();
                        SelectionActivity.user2Assignement = gameParam.getJoinerAssignment();

                        Intent intent = new Intent(GameActivity.this, SelectionActivity.class);
                        startActivity(intent);
                        finish();


                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onClick(View v) {
         switch (v.getId()) {

             case R.id.button1: // start
                 showSelections();
                 break;

             case R.id.exit_btn:
                 exitApp();
                 break;

             case R.id.decreaseRoundBtn:

                 totalRounds --;

                 if (totalRounds < 1) {
                     Toast.makeText(GameActivity.this, R.string.min_blinks, Toast.LENGTH_LONG).show();
                     totalRounds ++;
                 }
                 
                 if (ObtainContactActivity.isAvatar) // avatar
                 {
                     tv_blinksCounts.setText(String.valueOf(totalRounds));

                 } else
                 {
                    if (!gameParam.isStarted())
                    {
                        gameParam.setTotal_rounds(totalRounds);
                        dbFB.child("GameParam").child(dBUID).setValue(gameParam);
//                        updateDB();
                    } else totalRounds ++;

                 }
                 
                 break;

             case R.id.increaseRoundBtn:

                 totalRounds ++;
                 if (totalRounds > AppConstants.MAX_BLINK_NUMBER){
                     Toast.makeText(GameActivity.this, "Limit reached.", Toast.LENGTH_LONG).show();
                     totalRounds --;
                 }

                 if (ObtainContactActivity.isAvatar) // avatar
                 {
                     tv_blinksCounts.setText(String.valueOf(totalRounds));

                 } else
                 {
                     if (!gameParam.isStarted())
                     {
                         gameParam.setTotal_rounds(totalRounds);
                         dbFB.child("GameParam").child(dBUID).setValue(gameParam);
//                         updateDB();
                     } else totalRounds --;

                 }

                 break;

         }
     }

    private void exitApp() {

        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("EXIT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    public void showSelections(){

        if (ObtainContactActivity.isAvatar)
        {
            getEveryAssignmentForPlayers();
            Intent intent = new Intent(GameActivity.this, SelectionActivity.class);
            startActivity(intent);
            finish();
        } else if (MainActivity.myState.equals("j"))
        {
            if (!gameParam.isStarted())
            {
                gameParam.setStarted(true);
                dbFB.child("GameParam").child(dBUID).setValue(gameParam);
            } else
            {
                Toast.makeText(GameActivity.this, R.string.please_wait, Toast.LENGTH_LONG).show();
            }

        } else if (MainActivity.myState.equals("s"))
        {
            if (gameParam.isStarted())
            {
                getEveryAssignmentForPlayers();
            }

            // random generation
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
                        exitApp();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })

                .show();

    }

}