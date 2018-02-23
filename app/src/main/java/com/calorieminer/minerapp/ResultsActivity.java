package com.calorieminer.minerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.calorieminer.minerapp.model.GameParam;


import static com.calorieminer.minerapp.GameActivity.myScore;
import static com.calorieminer.minerapp.GameActivity.otherScore;
import static com.calorieminer.minerapp.MainActivity.myState;

public class ResultsActivity extends AppCompatActivity {

    TextView tv_iScore, tv_otherScore, tv_result, tv_cummulativescore;
    GameParam gameParam;
    boolean openSite;
    static boolean isResultActivity;
    boolean isClicked;
    public String mUserEmail;

    @Override
    protected void onStart() {
        super.onStart();

        if (!isResultActivity)
        {
            tv_iScore = findViewById(R.id.textView2);
            tv_otherScore = findViewById(R.id.textView3);
            tv_result = findViewById(R.id.textView4);
            tv_cummulativescore = findViewById(R.id.cummulativescore);

            if (!ObtainContactActivity.isAvatar)
            {
                openSite = false;
                gameParam = SelectionActivity.gameParam;
                myScore = myState.equals("s") ? gameParam.getStarterScore() : gameParam.getJoinerScore();
                otherScore = myState.equals("s") ? gameParam.getJoinerScore() : gameParam.getStarterScore();
            }
            calcScore();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        isResultActivity = false;
        isClicked = false;
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null)
            return;
        mUserEmail = mFirebaseUser.getEmail();
    }

    private void calcScore() {

        String tempStr = "Your score: " + myScore + "/" + GameActivity.totalRounds + " - " + myScore*100/GameActivity.totalRounds + "%";

        tv_iScore.setText(tempStr);

        tempStr = "Their score: " + otherScore + "/" + GameActivity.totalRounds + " - " + otherScore*100/GameActivity.totalRounds + "%";
        tv_otherScore.setText(tempStr);

        tempStr = "Cummulative Score: " + (myScore+otherScore) + "/" + (GameActivity.totalRounds*2) + " - " +
                ((myScore+otherScore))*100/GameActivity.totalRounds/2 + "%";
        tv_cummulativescore.setText(tempStr);

        if (myScore > otherScore)
        {

            tv_result.setText(R.string.you_win);

        } else if (myScore < otherScore)
        {

            tv_result.setText(R.string.they_win);

        } else
        {

            tv_result.setText(R.string.tie);

        }
    }

    public void onClick(View v) {

        if (isClicked) return;

        isClicked = true;
        isResultActivity = true;
        switch (v.getId()) {
            case R.id.transcenderstarship:
                showResults(R.id.transcenderstarship);
                break;

            case R.id.transcender:
                showResults(R.id.transcender);
                break;

            case R.id.fadetoblack:
                showResults(R.id.fadetoblack);
                break;

            case R.id.playagain: // exit game
                isClicked = false;
                exitApp();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResultActivity = true;
    }

    public void showResults(int buttonID){

        Intent signUpIntent;

        if (buttonID == R.id.transcenderstarship)
        {
            signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.transcenderstarship.com"));

        } else if (buttonID == R.id.transcender)
        {
            signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.transcender.com/"));
        } else
        {
            signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jimmychurchradio.com"));
        }

        SelectionActivity.messagesRef.setValue(null);
        signUpIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(signUpIntent);
        finish();

    }

    private void exitApp()
    {
        SelectionActivity.messagesRef.setValue(null);
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("EXIT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
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

