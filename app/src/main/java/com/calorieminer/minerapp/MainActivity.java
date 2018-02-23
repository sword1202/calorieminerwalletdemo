package com.calorieminer.minerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.lang.System.exit;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {


    @Override
    public void onBackPressed() {
        showAlert();
    }

    public static final String TAG = "MainActivity";
    private static final int REQUEST_INVITE = 1;

    public FirebaseAuth mFirebaseAuth;
    public FirebaseUser mFirebaseUser;
    public DatabaseReference mFirebaseDatabaseReference;
//    private AdView mAdView;
    static String myState;
    boolean isMainActivityActive;
    ImageView imgView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imageView);
        int width = MainActivity.this.getWindowManager().getDefaultDisplay().getWidth();
        android.view.ViewGroup.LayoutParams layoutParams = imgView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = width;
        imgView.setLayoutParams(layoutParams);
        isMainActivityActive = true;
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    public void onPause() {

        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.join_game:
                joinGame();
                return true;
            case R.id.readmefile:
                readmeFile();
                return true;
            case R.id.obtain_contact_menu: // create game
                showObtainContact();
                return true;
            case R.id.exit_menu:
                exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void joinGame() {
        // create Customer to play game

        ResultsActivity.isResultActivity = false;
        MainActivity.myState = "j";
        SignInActivity.mStateString = "j";
        Intent intent = new Intent(MainActivity.this, ObtainContactActivity.class);
        startActivity(intent);
        finish();
    }

    public void readmeFile(){
        Intent intent = new Intent(this, ReadmeFileActivity.class);
        startActivity(intent);

    }

    public void showObtainContact(){ // create game

        // create Server to play game

        ResultsActivity.isResultActivity = false;
        myState = "s";
        SignInActivity.mStateString = "s";
        Intent intent = new Intent(MainActivity.this, ObtainContactActivity.class);
        startActivity(intent);
        finish();

    }

    private void sendInvitation() {
        String message = "You are the Joiner, enter my Gmail <" + mFirebaseUser.getEmail() + " > as the Starter.";
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(message)
                .setDeepLink(Uri.parse(getString(R.string.deeplinkURL)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");

                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed.", Toast.LENGTH_LONG).show();
    }

    private void showAlert() {

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

    private void exitApp() {

        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("EXIT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}
