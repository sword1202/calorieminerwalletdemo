package com.calorieminer.minerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.calorieminer.minerapp.CustomClass.AccessFactory;
import com.calorieminer.minerapp.CustomClass.PrefsUtil;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;
import com.calorieminer.minerapp.CustomClass.ScrambledPin;

public class PinEntryActivity extends AppCompatActivity{

    private Button ta = null;
    private Button tb = null;
    private Button tc = null;
    private Button td = null;
    private Button te = null;
    private Button tf = null;
    private Button tg = null;
    private Button th = null;
    private Button ti = null;
    private Button tj = null;
    private ImageButton tsend = null;

    private TextView tvUserInput = null;

    private StringBuilder userInput = null;

    private boolean create = false;             // create PIN
    private boolean confirm = false;            // confirm PIN
    private boolean change = false;
    private String strConfirm = null;
    private boolean isOpenCameraActivity = false;

    private ProgressDialog progress = null;

    private static int failures = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        userInput = new StringBuilder();
        ScrambledPin keypad = new ScrambledPin();

        tvUserInput = findViewById(R.id.userInput);
        tvUserInput.setText("");

        TextView tvPrompt = findViewById(R.id.prompt2);

        boolean scramble = PrefsUtil.getInstance(PinEntryActivity.this).getValue(PrefsUtil.SCRAMBLE_PIN, false);
        Bundle extras = getIntent().getExtras();

        if(extras != null && extras.containsKey("create") && extras.getBoolean("create"))	{
            tvPrompt.setText(R.string.create_pin);
            scramble = false;
            create = true;
            confirm = false;
//            Toast.makeText(PinEntryActivity.this, R.string.pin_1_8, Toast.LENGTH_LONG).show();
        }
        else if(extras != null && extras.containsKey("confirm") && extras.getBoolean("confirm"))	{
            tvPrompt.setText(R.string.confirm_pin);
            scramble = false;
            create = false;
            confirm = true;
            strConfirm = extras.getString("first");
//            Toast.makeText(PinEntryActivity.this, R.string.pin_1_8_confirm, Toast.LENGTH_LONG).show();
        }
        else if(extras != null && extras.containsKey("changepin") && extras.getBoolean("changepin"))	{
            tvPrompt.setText(R.string.change_pin);
            create = false;
            confirm = false;
            scramble = false;
            change = true;
            isOpenCameraActivity = false;
        } else {
            change = false;
            isOpenCameraActivity = true;
        }

        ta = findViewById(R.id.ta);
        ta.setText(scramble ? Integer.toString(keypad.getMatrix().get(0).getValue()) : "1");
        tb = findViewById(R.id.tb);
        tb.setText(scramble ? Integer.toString(keypad.getMatrix().get(1).getValue()) : "2");
        tc = findViewById(R.id.tc);
        tc.setText(scramble ? Integer.toString(keypad.getMatrix().get(2).getValue()) : "3");
        td = findViewById(R.id.td);
        td.setText(scramble ? Integer.toString(keypad.getMatrix().get(3).getValue()) : "4");
        te = findViewById(R.id.te);
        te.setText(scramble ? Integer.toString(keypad.getMatrix().get(4).getValue()) : "5");
        tf = findViewById(R.id.tf);
        tf.setText(scramble ? Integer.toString(keypad.getMatrix().get(5).getValue()) : "6");
        tg = findViewById(R.id.tg);
        tg.setText(scramble ? Integer.toString(keypad.getMatrix().get(6).getValue()) : "7");
        th = findViewById(R.id.th);
        th.setText(scramble ? Integer.toString(keypad.getMatrix().get(7).getValue()) : "8");
        ti = findViewById(R.id.ti);
        ti.setText(scramble ? Integer.toString(keypad.getMatrix().get(8).getValue()) : "9");
        tj = findViewById(R.id.tj);
        tj.setText(scramble ? Integer.toString(keypad.getMatrix().get(9).getValue()) : "0");
        tsend = findViewById(R.id.tsend);
        ImageButton tback = findViewById(R.id.tback);

        ta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(ta.getText().toString());
                displayUserInput();
            }
        });

        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(tb.getText().toString());
                displayUserInput();
            }
        });

        tc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(tc.getText().toString());
                displayUserInput();
            }
        });

        td.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(td.getText().toString());
                displayUserInput();
            }
        });

        te.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(te.getText().toString());
                displayUserInput();
            }
        });

        tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(tf.getText().toString());
                displayUserInput();
            }
        });

        tg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(tg.getText().toString());
                displayUserInput();
            }
        });

        th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(th.getText().toString());
                displayUserInput();
            }
        });

        ti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(ti.getText().toString());
                displayUserInput();
            }
        });

        tj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput.append(tj.getText().toString());
                displayUserInput();
            }
        });

        tsend.setVisibility(View.INVISIBLE);
        tsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(create && userInput.toString().length() >= AccessFactory.MIN_PIN_LENGTH && userInput.toString().length() <= AccessFactory.MAX_PIN_LENGTH) {
                    Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("confirm", true);
                    intent.putExtra("create", false);
                    intent.putExtra("first", userInput.toString());
                    startActivity(intent);
                }
                else if(!isOpenCameraActivity && confirm && userInput.toString().length() >= AccessFactory.MIN_PIN_LENGTH && userInput.toString().length() <= AccessFactory.MAX_PIN_LENGTH) {

                    if(userInput.toString().equals(strConfirm)) {

//                        progress = new ProgressDialog(PinEntryActivity.this);
//                        progress.setCancelable(false);
//                        progress.setTitle(R.string.app_name);
//                        progress.setMessage(strSeed == null ? getString(R.string.creating_wallet) :  getString(R.string.restoring_wallet));
//                        progress.show();

                        storePin(userInput.toString());

                    }
                    else {
                        Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("create", true);
                        startActivity(intent);
                    }

                } else if (change && userInput.toString().length() >= AccessFactory.MIN_PIN_LENGTH && userInput.toString().length() <= AccessFactory.MAX_PIN_LENGTH)
                {
                    validatePin(userInput.toString());
                }
                else {
                    if(userInput.toString().length() >= AccessFactory.MIN_PIN_LENGTH && userInput.toString().length() <= AccessFactory.MAX_PIN_LENGTH) {
                        validatePin(userInput.toString());
                    }
                }

            }
        });

        tback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(userInput.toString().length() > 0) {
                    userInput.deleteCharAt(userInput.length() - 1);
                }
                displayUserInput();

            }
        });

    }

    private void displayUserInput() {

        tvUserInput.setText("");

        for(int i = 0; i < userInput.toString().length(); i++) {
            tvUserInput.append("*");
        }

        if(userInput.toString().length() >= AccessFactory.MIN_PIN_LENGTH && userInput.toString().length() <= AccessFactory.MAX_PIN_LENGTH) {
            tsend.setVisibility(View.VISIBLE);
        }
        else {
            tsend.setVisibility(View.INVISIBLE);
        }

    }

    private void validatePin(final String pin)	{

        String storedPin = PrefsUtil.getInstance(PinEntryActivity.this).getValue(PrefsUtil.PIN, "");
        if (pin.length() < AccessFactory.MIN_PIN_LENGTH || pin.length() > AccessFactory.MAX_PIN_LENGTH) {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            Toast.makeText(PinEntryActivity.this, R.string.pin_error, Toast.LENGTH_SHORT).show();
        } else if (!storedPin.matches(pin))
        {
            failures++;
            Intent intent;
            userInput = new StringBuilder();
            tvUserInput.setText("");

            Toast.makeText(PinEntryActivity.this, PinEntryActivity.this.getText(R.string.pincode_error) + ":" + failures + "/3", Toast.LENGTH_SHORT).show();

            if(failures == 3)    {
                failures = 0;
                intent = new Intent(PinEntryActivity.this, SignInActivity.class);
                intent.putExtra("EXIT", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();
            }

            if (isOpenCameraActivity)
            {
                // restart
                intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (change)
            {
                intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("changepin", true);
                startActivity(intent);
            }

        } else
        {
            Intent intent;
            if (isOpenCameraActivity)
            {
                if (RemoteConfigParam.getInstance(this).getMap_location_enabled())
                {
                    intent = new Intent(PinEntryActivity.this, Hallfinder.class);
                    intent.putExtra("confirmPIN", true);
                    startActivity(intent);
                } else
                {
                    intent = new Intent(PinEntryActivity.this, SignInActivity.class);
                    intent.putExtra("confirmPIN", true);
                    startActivity(intent);
                }

                finish();
            } else if (change)
            {
                change = false;
                intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("create", true);
                startActivity(intent);
            }

        }

    }

    private void storePin(final String pin) {

        PrefsUtil.getInstance(PinEntryActivity.this).setValue(PrefsUtil.PIN, pin);
        PrefsUtil.getInstance(PinEntryActivity.this).setValue(PrefsUtil.SCRAMBLE_PIN, true);

        Intent intent = new Intent(PinEntryActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.join_game).setVisible(false);
        menu.findItem(R.id.readmefile).setVisible(false);
        menu.findItem(R.id.obtain_contact_menu).setVisible(false);
        menu.findItem(R.id.exit_menu).setVisible(false);
        menu.findItem(R.id.invite_menu).setTitle("Help");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                showAlert(R.string.pin_help_button_message);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showAlert(R.string.back_button_message);
    }

    private void showAlert(int message) {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(null)
                .setMessage(message)
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
