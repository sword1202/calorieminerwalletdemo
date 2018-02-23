package com.calorieminer.minerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.calorieminer.minerapp.model.GameParam;
import com.calorieminer.minerapp.model.Joiner;
import com.calorieminer.minerapp.model.Starter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ObtainContactActivity extends AppCompatActivity {

    static boolean initFlag1, initFlag2, isStarterSelected, isJoinerClickedContinueButton;

    String userID, userGmail, selectedTheirGmail;
    Button btn_playHuman, btn_playAvatar, btn_exit;
    boolean existServerRemoveFlag; // exit serverEmail in customerUSer when select another serverUser
    int spinnerPosition;
    String gameParamID;
    static boolean isAvatar;
    Starter sel_Starter, realStarter;
    TextView tv_usergmail, tv_theirgmail;
    Joiner selJoiner, realJoiner;
    boolean initialFlag;
    public static Integer  counts;
    static boolean isActiveObtainContactActivity;
    EditText ed_inputStarterGmail;
    Button btn_findStarter;
    boolean isSelectedFindStarterButton, isSelectedPlayHuman;
    String tag = "ObtainAcivity - ";
    String sharedPreferenceKey = "old_gmail";
    FirebaseUser currentUser;
    ValueEventListener checkResultListener, checkRealtimeDBValueEventListener, checkGameParamValueEventListener;
    DatabaseReference checkGameParamDB;
    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (ResultsActivity.isResultActivity)
        {
            finish();
        } else
        {
            isActiveObtainContactActivity = true;
        }
    }

    @Override
    public void onPause() {

        initFlag1 = false;
        initFlag2 = false;
        isStarterSelected = false;
        isJoinerClickedContinueButton = false;
        counts = 0;
        super.onPause();
        isActiveObtainContactActivity = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obtain_contact);


        btn_playHuman = findViewById(R.id.button1);
        btn_playAvatar = findViewById(R.id.button2);
        btn_exit = findViewById(R.id.exit);
        btn_findStarter = findViewById(R.id.findstarter);
        isSelectedFindStarterButton = false;
        isSelectedPlayHuman = false;

        existServerRemoveFlag = false;
        spinnerPosition = 0;

        isAvatar = false;

        initialFlag = false;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        userID = currentUser.getUid();
        userGmail = currentUser.getEmail();
        ed_inputStarterGmail = findViewById(R.id.gmailForm);

        tv_usergmail = findViewById(R.id.userGmail);
        tv_theirgmail = findViewById(R.id.tv_theirgmail);


        if (MainActivity.myState.equals("s"))
        {
            tv_theirgmail.setText(R.string.their_gmail_address);
            ed_inputStarterGmail.setVisibility(View.GONE);
            tv_usergmail.setVisibility(View.VISIBLE);
            btn_playHuman.setText(R.string.play_human_wait);
            tv_usergmail.setText(R.string.you_starter_wait_joiner);
            btn_findStarter.setVisibility(View.GONE);
        } else
        {
            tv_theirgmail.setText(R.string.enter_starter_gmail);
            btn_playHuman.setText(R.string.wait_game);
            ed_inputStarterGmail.setVisibility(View.VISIBLE);
            tv_usergmail.setVisibility(View.GONE);

            btn_findStarter.setClickable(false);
            btn_findStarter.setAlpha(0.3f);

            // check saved Starter's gmail
            SharedPreferences preferences = ObtainContactActivity.this.getSharedPreferences(sharedPreferenceKey, Context.MODE_PRIVATE);
            String oldGmail = preferences.getString("value_gmail", null);

            if (oldGmail != null)
                ed_inputStarterGmail.setText(oldGmail);
        }

        btn_playHuman.setClickable(false);
        btn_playHuman.setAlpha(0.3f);

        createStarterJoiner();

    }


    @Override
    protected void onDestroy() {
        if (checkResultListener != null)
            FirebaseDatabase.getInstance().getReference().child("Starter").child(sel_Starter.uid).orderByChild("joinerEmail")
                .equalTo(selectedTheirGmail).removeEventListener(checkResultListener);

        if (checkRealtimeDBValueEventListener != null)
            FirebaseDatabase.getInstance().getReference().removeEventListener(checkRealtimeDBValueEventListener);

        if (checkGameParamValueEventListener != null)
            checkGameParamDB.removeEventListener(checkGameParamValueEventListener);

        super.onDestroy();
    }

    private void checkResult() {

        checkResultListener = FirebaseDatabase.getInstance().getReference().child("Starter").child(sel_Starter.uid).orderByChild("joinerEmail")
                .equalTo(selectedTheirGmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (MainActivity.myState.equals("s") && isActiveObtainContactActivity)
                {
                    btn_playHuman.setClickable(true);
                    btn_playHuman.setText(R.string.play_human);
                    btn_playHuman.setAlpha(1.0f);
                    tv_usergmail.setText(selJoiner.getUserEmail());
                    btn_playAvatar.setClickable(false);
                    btn_playAvatar.setAlpha(0.3f);
                } else if (MainActivity.myState.equals("j") && isActiveObtainContactActivity)
                {
                    btn_playHuman.setText(R.string.ready_start);
                    btn_playHuman.setClickable(true);
                    btn_playHuman.setAlpha(1.0f);

                    realStarter = sel_Starter;
                    realJoiner = selJoiner;
                    ed_inputStarterGmail.setText(sel_Starter.userEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void createStarterJoiner() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (currentUser == null)
            return;
        final String s_userID = currentUser.getUid();
        final String s_userName = currentUser.getDisplayName();
        final String s_userEmail = currentUser.getEmail();



        // --------------

        if (MainActivity.myState.equals("s"))
        {

            DatabaseReference dBRef = database.getReference().child("Starter").child(s_userID);
            Starter starter = new Starter(s_userID, s_userName, s_userEmail, "");
            sel_Starter = starter;
            database.getReference().child("Joiner").child(s_userID).setValue(null);
            dBRef.setValue(null);
            dBRef.setValue(starter).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(ObtainContactActivity.this, R.string.you_starter, Toast.LENGTH_LONG).show();
                    checkingRealtimeDatabase();
                }
            });

        } else
        {
            DatabaseReference dBRef = database.getReference().child("Joiner").child(s_userID);
            Joiner joiner = new Joiner(s_userID, s_userName, s_userEmail, "");
            selJoiner = joiner;
            database.getReference().child("Starter").child(s_userID).setValue(null);
            dBRef.setValue(null);

            dBRef.setValue(joiner).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    btn_findStarter.setClickable(true);
                    btn_findStarter.setAlpha(1.0f);
                    Toast.makeText(ObtainContactActivity.this, R.string.you_joiner, Toast.LENGTH_LONG).show();
                    checkingRealtimeDatabase();
                }
            });


//            database.getReference().child("Joiner").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.hasChild(s_userID))
//                    {
//                        btn_findStarter.setClickable(true);
//                        btn_findStarter.setAlpha(1.0f);
//                        Toast.makeText(ObtainContactActivity.this, R.string.you_joiner, Toast.LENGTH_LONG).show();
//                        checkingRealtimeDatabase();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

        }
    }

    private void checkingRealtimeDatabase() {

        checkRealtimeDBValueEventListener = FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (MainActivity.myState.equals("s") && isActiveObtainContactActivity) {

                    // if customer selected me, replace customerEmail to curtomer's ID;
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (mUser == null)
                        return;
                    final String s_userID = mUser.getUid();

                    FirebaseDatabase.getInstance().getReference().child("Joiner").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            tv_usergmail.setText(R.string.you_starter_wait_joiner);
                            btn_playHuman.setClickable(false);
                            btn_playHuman.setAlpha(0.3f);
                            btn_playHuman.setText(R.string.play_human_wait);

                            btn_playAvatar.setAlpha(1.0f);
                            btn_playAvatar.setClickable(true);

                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(s_userID)) {
                                    FirebaseDatabase.getInstance().getReference().child("Joiner").child(s_userID).removeValue();
                                }

                                for (DataSnapshot singleSnaoshot : dataSnapshot.getChildren()) {
                                    Joiner tempJoiner = singleSnaoshot.getValue(Joiner.class);

                                    if (tempJoiner == null)
                                        return;
                                    String sss = tempJoiner.getstarterEmail();
                                    String aaa = sel_Starter.getUserEmail();
                                    if (sss != null) {
                                        if (sss.equals(aaa)) {
                                            selJoiner = tempJoiner;
                                            sel_Starter.setJoinerEmail(selJoiner.getUserEmail());
                                            DatabaseReference dBRef = FirebaseDatabase.getInstance().getReference().child("Starter").child(sel_Starter.getUID());
                                            dBRef.setValue(sel_Starter);

                                            checkResult();
                                        }


                                    }

                                }
                            } else {
                                tv_usergmail.setText(R.string.you_starter_wait_joiner);
                                btn_playHuman.setClickable(false);
                                btn_playHuman.setText(R.string.play_human_wait);
                                btn_playHuman.setAlpha(0.3f);

                                btn_playAvatar.setAlpha(1.0f);
                                btn_playAvatar.setClickable(true);
                                FirebaseDatabase.getInstance().getReference().child("Starter").child(s_userID).child("joinerEmail").setValue("");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button1: // play Human

                playHuman();

                break;

            case R.id.findstarter:

                didSelectFindStarterButton();

                break;

            case R.id.button2: // play avatar

                isAvatar = true; // play avatar
                showGameActivity();
                break;

            case R.id.exit: // reset

                exitApp();

                break;
        }
    }

    private void playHuman() {

        if (isSelectedPlayHuman) return;

        isSelectedPlayHuman = true;
        if (MainActivity.myState.equals("j"))
        {
            if (btn_playHuman.getText().equals(getResources().getString(R.string.ready_start)))
            {
                checkingGameParam();
                isSelectedPlayHuman = false;

            } else if (btn_playHuman.getText().equals(getResources().getString(R.string.join_game_select)) && sel_Starter.getjoinerEmail().matches(""))
            {
                DatabaseReference dBRef = FirebaseDatabase.getInstance().getReference().child("Joiner").child(selJoiner.getUid());
                selJoiner.setStarterEmail(selectedTheirGmail);
                sel_Starter.setJoinerEmail(selJoiner.getUserEmail());
                dBRef.setValue(selJoiner);
                gameParamID = sel_Starter.getUID() + selJoiner.getUid();
                isSelectedPlayHuman = false;
                checkResult();

            }

        } else if (MainActivity.myState.equals("s"))
        {
            GameActivity.selectedStarter = sel_Starter;
            GameActivity.selectedJoiner = selJoiner;
            showGameActivity();
            isSelectedPlayHuman = false;
        }
    }

    private void didSelectFindStarterButton() {

        if (!isSelectedFindStarterButton) {

            isSelectedFindStarterButton = true;

            selectedTheirGmail = ed_inputStarterGmail.getText().toString();
            if (selectedTheirGmail.matches("")) {
                Toast.makeText(this, R.string.please_enter_starter_gmail, Toast.LENGTH_SHORT).show();
                isSelectedFindStarterButton = false;

            } else {

                if (sel_Starter != null && sel_Starter.userEmail.equals(selectedTheirGmail)) {
                    // return if you are still selecting same email to JOIN GAME.
                    isSelectedFindStarterButton = false;
                    btn_findStarter.setAlpha(0.3f);
                    return;
                }

                checkStartersOnFirebaseDatabase();

            }
        } else
            Toast.makeText(this, R.string.pairing_error, Toast.LENGTH_SHORT).show();
    }

    private void checkStartersOnFirebaseDatabase() {

        FirebaseDatabase.getInstance().getReference().child("Starter").orderByChild("userEmail").equalTo(selectedTheirGmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (isActiveObtainContactActivity) {
                    boolean isValidate = false;
                    for (DataSnapshot everySnapshot : dataSnapshot.getChildren())
                    {
                        // it validates
                        Starter temStarter = everySnapshot.getValue(Starter.class);

                        if (temStarter == null)
                            return;
                        if (!temStarter.getjoinerEmail().equals(""))
                        {
                            Toast.makeText(ObtainContactActivity.this, R.string.playing_starter, Toast.LENGTH_LONG).show();
                            return;
                        }


                        sel_Starter = temStarter;
                        Toast.makeText(ObtainContactActivity.this, R.string.starter_found, Toast.LENGTH_LONG).show();
                        btn_playHuman.setText(R.string.join_game_select);
                        btn_playHuman.setClickable(true);
                        btn_playHuman.setAlpha(1.0f);

                        btn_playAvatar.setClickable(false);
                        btn_playAvatar.setAlpha(0.3f);
                        isValidate = true;

                        // save previous gmail of Starter.

                        SharedPreferences preferences = ObtainContactActivity.this.getSharedPreferences(sharedPreferenceKey, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("value_gmail", sel_Starter.getUserEmail());
                        editor.apply();
                    }

                    if (!isValidate)
                    {
                        // it invalidates
                        btn_findStarter.setAlpha(1.0f);
                        showAlert();
                    } else
                        btn_findStarter.setAlpha(0.3f);

                    isSelectedFindStarterButton = false;

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showAlert() {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(R.string.warning)
                .setMessage(R.string.starter_not_found)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_playHuman.setText(R.string.wait_game);
                        btn_playHuman.setClickable(false);
                        btn_playHuman.setAlpha(0.3f);

                        btn_playAvatar.setClickable(true);
                        btn_playAvatar.setAlpha(1.0f);

                    }
                })

                .show();

    }

    private void checkingGameParam() {

        final String newGameParamUID = gameParamID;
        checkGameParamDB = FirebaseDatabase.getInstance().getReference().child("GameParam").child(newGameParamUID);
        checkRealtimeDBValueEventListener = checkGameParamDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isActiveObtainContactActivity)
                {
                    if (dataSnapshot.exists())
                    {
                        GameParam exitGameParam = dataSnapshot.getValue(GameParam.class);
                        if (exitGameParam == null)
                            return;
                        String whoseGame = exitGameParam.getWhoseGame();
                        GameParam compareParam = new GameParam(whoseGame, 10, null, null, null, false, false, 0, 0, null, null, true, true, true, false, false);

                        if (compareParam.compareTo(exitGameParam) == 1)
                        {
                            GameActivity.selectedStarter = realStarter;
                            GameActivity.selectedJoiner = realJoiner;
                            counts = 0;
                            showGameActivity();

                        } else
                            Toast.makeText(ObtainContactActivity.this, R.string.wait_starter, Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(ObtainContactActivity.this, R.string.wait_starter, Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showGameActivity() {

        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        isActiveObtainContactActivity = false;
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(null)
                .setMessage(R.string.back_button_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResultsActivity.isResultActivity = true;
                        exitApp();

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })

                .show();
    }

    private void exitApp() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("EXIT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}

