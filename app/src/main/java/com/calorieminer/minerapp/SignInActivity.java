package com.calorieminer.minerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.calorieminer.minerapp.CustomClass.PrefsUtil;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;
import com.calorieminer.minerapp.model.Users;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.calorieminer.minerapp.CustomClass.CustomDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private SignInButton mSignInButton;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    static boolean isSigninActivity, isPassword, isEmail;
    static String mStateString;
    static GoogleSignInAccount account;
    EditText edEmail, edPassword;
    Button signupButton, signInButton;
    TextView tvForgotPassword;
    public static boolean isConfirmedPIN;

    private CustomDialog pd;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false))
        {

            if (Build.VERSION.SDK_INT >= 21)
            {
                finishAndRemoveTask();

            } else
            {

                finish();
            }
        }

        isConfirmedPIN();

        isSigninActivity = true;

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Assign fields
        mSignInButton = findViewById(R.id.sign_in_button);
        //mSignUpButton = (SignUpButtonB) findViewById(R.id.sign_up_button);


        // Set click listeners
        mSignInButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // signup by passing email and password
        signupButton = findViewById(R.id.sign_up_button);
        signInButton = findViewById(R.id.signInButton);

        setDisable(signInButton);
        setDisable(signupButton);

        tvForgotPassword = findViewById(R.id.forgotPassword);

        edEmail = findViewById(R.id.eT_email);
        edPassword = findViewById(R.id.eT_password);

        signupButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);

        edEmail.setOnEditorActionListener(mEditorActionListener);
        edPassword.setOnEditorActionListener(mEditorActionListener);
        edEmail.requestFocus();

        isPassword = false;
        isEmail = false;
    }

    private void isConfirmedPIN() {

        if (RemoteConfigParam.getInstance(this).getMap_location_enabled())
            return;
        Bundle extras = getIntent().getExtras();
        isConfirmedPIN = false;
        if (extras != null && extras.containsKey("confirmPIN") && extras.getBoolean("confirmPIN")) {
            SignInActivity.isConfirmedPIN = true;
        }
    }

    TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
            switch (textView.getId())
            {
                case R.id.eT_email:
                    String email = edEmail.getText().toString();
                    if (actionID == EditorInfo.IME_ACTION_NEXT)
                    {
                        if (email.matches(""))
                        {
                            Toast.makeText(SignInActivity.this, R.string.no_email, Toast.LENGTH_SHORT).show();
                            return false;
                        } else if (!isEmailValid(email))
                        {
                            Toast.makeText(SignInActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                            return false;
                        } else if (isPassword) {
                            isEmail = true;
                            setEnable(signupButton);
                            setEnable(signInButton);
                        } else
                        {
                            isEmail = true;
                        }
                    }
                    break;
                case R.id.eT_password:
                    String password = edPassword.getText().toString();
                    if (actionID == EditorInfo.IME_ACTION_DONE)
                    {
                        if (password.matches(""))
                        {
                            Toast.makeText(SignInActivity.this, R.string.no_password, Toast.LENGTH_SHORT).show();
                            return false;
                        } else if (isEmail)
                        {
                            isPassword = true;
                            setEnable(signupButton);
                            setEnable(signInButton);
                        } else
                        {
                            isPassword = true;
                        }

                    }

                    break;
            }

            return false;
        }
    };

    public void setDisable(Button button)
    {
        button.setAlpha(0.3f);
        button.setClickable(false);
    }

    public void setEnable(Button button)
    {
        button.setAlpha(1.0f);
        button.setClickable(true);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case  R.id.sign_up_button:
                signUp();
                break;
            case R.id.signInButton:
                emailSignIn();
                break;
            case R.id.forgotPassword:
                forgotPassword();
                break;
        }

        isEmail = false;
        isPassword = false;
    }

    private void forgotPassword() {

    }

    private void emailSignIn() {

        setAllDisableButton();

        pd = new CustomDialog(this);
        pd.setText(getResources().getString(R.string.sign_loading));
        pd.show();
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        pd.dismiss();
                        setAllEnableButton();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, R.string.faile_auth,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void setAllDisableButton()
    {
        setDisable(signInButton);
        setDisable(signupButton);
        mSignInButton.setClickable(false);
        mSignInButton.setAlpha(0.3f);
    }

    public void setAllEnableButton()
    {
        setEnable(signupButton);
        setEnable(signInButton);
        mSignInButton.setClickable(true);
        mSignInButton.setAlpha(1.0f);
    }

    private void signUp() {

        setAllDisableButton();

        pd = new CustomDialog(this);
        pd.setText(getResources().getString(R.string.signup_loading));
        pd.show();
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                pd.dismiss();

                setAllEnableButton();
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(SignInActivity.this, R.string.faile_auth,
                            Toast.LENGTH_SHORT).show();
                }

                // ...
            }
        });
    }

    public static boolean isEmailValid(String email)
    {
        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void signIn() {

        getSize();
        setAllDisableButton();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void getSize()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowmanager == null)
            return;
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;
        VideoRecorder.width = deviceWidth;
        VideoRecorder.height = deviceHeight;

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            pd = new CustomDialog(this);
            pd.setText(getResources().getString(R.string.sign_loading));
            pd.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                if (pd!=null)
                    pd.dismiss();

                setAllEnableButton();
                Toast.makeText(SignInActivity.this, R.string.fail_googlesign,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {

                            pd.dismiss();
                            setAllEnableButton();
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, R.string.faile_auth,
                                    Toast.LENGTH_SHORT).show();
                        } else {


                            if (mFirebaseAuth.getCurrentUser() == null)
                                return;

                            final String uid = mFirebaseAuth.getCurrentUser().getUid();
                            final Users users = new Users(mFirebaseAuth.getCurrentUser().getUid(), mFirebaseAuth.getCurrentUser().getDisplayName(),
                                    mFirebaseAuth.getCurrentUser().getEmail(), phoneFormatFromString(CameraActivity.phoneNumber), CameraActivity.latitude, CameraActivity.longitude,
                                    CameraActivity.timeStamp, 0, 0, CameraActivity.timeStamp, CameraActivity.timeStamp);

                            FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists())
                                    {
                                        Users existUser = dataSnapshot.getValue(Users.class);

                                        if (existUser == null)
                                            return;

                                        if (CameraActivity.phoneNumber.matches(""))
                                        {
                                            // can not enter phone number, but there exits it in database

                                            String phoneNumber = existUser.getphoneNumber();
                                            users.setPhoneNumber(phoneNumber);
                                        }

                                        int width = existUser.getWidth();
                                        int height = existUser.getHeight();
                                        if (width != 0 && height != 0)
                                        {
                                            // if there exits width and heigh, but not 1
                                            users.setWidth(width);
                                            users.setHeight(height);
                                        }

                                    }

                                    storeDBinUsers(users, uid);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                        }
                    }
                });

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    mGoogleApiClient.disconnect();
                }
            });
    }

    public String phoneFormatFromString(String phoneStr)
    {
        String resultPhoneStr;

        switch (phoneStr.length()) {
            case 7:
                resultPhoneStr = String.format("%s-%s", phoneStr.substring(0,3), phoneStr.substring(3,7));
                break;
            case 10:
                resultPhoneStr = String.format("(%s) %s-%s", phoneStr.substring(0,3), phoneStr.substring(3,6), phoneStr.substring(6,10));
                break;
            case 11:
                resultPhoneStr = String.format("%s (%s) %s-%s", phoneStr.substring(0,1) ,phoneStr.substring(1,4), phoneStr.substring(4,7), phoneStr.substring(7,11));
                break;
            case 12:
                resultPhoneStr = String.format("+%s (%s) %s-%s", phoneStr.substring(0,2) ,phoneStr.substring(2,5), phoneStr.substring(5,8), phoneStr.substring(8,12));
                break;
            default:
                return null;
        }

        return resultPhoneStr;
    }

    public void storeDBinUsers(Users mUsers, String uid)
    {
        DatabaseReference dBRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        dBRef.setValue(mUsers, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                pd.dismiss();
                setAllEnableButton();
                if (databaseError == null)
                {
                    Intent intent = new Intent(new Intent(SignInActivity.this, CameraActivity.class));
                    boolean isStoredPIN = PrefsUtil.getInstance(SignInActivity.this).getValue(PrefsUtil.SCRAMBLE_PIN, false);
                    if (!isConfirmedPIN && RemoteConfigParam.getInstance(SignInActivity.this).getPinEnabled() && !isStoredPIN)
                    {
                        intent = new Intent(SignInActivity.this, PinEntryActivity.class);
                        intent.putExtra("create", true);

                    }

                    startActivity(intent);
                    finish();

                } else
                {
                    Toast.makeText(SignInActivity.this, R.string.normal_error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, R.string.gps_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isSigninActivity = false;
    }

    @Override
    public void onBackPressed() {
        showAlert();
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

        System.exit(0);
    }

}
