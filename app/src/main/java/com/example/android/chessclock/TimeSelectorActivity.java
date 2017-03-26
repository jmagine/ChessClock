package com.example.android.chessclock;

import android.app.DialogFragment;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.widget.NumberPicker;
import android.widget.Button;
import android.widget.CheckBox;

import android.util.Log;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by JasonMa on 2/24/2017.
 */

public class TimeSelectorActivity extends AppCompatActivity implements View.OnClickListener{
  static int fragmentMode;
  static int playerTopTime;
  static int playerBottomTime;
  static int selectedTime;
  static long playerTopIncrement;
  static long playerBottomIncrement;

  long[] initTimes;
  long[] increment;
  int incrementType;

  static boolean enablePlayer2;

  TextView player1Text;
  TextView player2Text;

  Button topTime;
  Button bottomTime;
  Button topIncrement;
  Button bottomIncrement;
  Button applyButton;
  Button cancelButton;

  CheckBox sameTimes;

  RadioButton incFischer;
  RadioButton incDelay;
  RadioButton incBronstein;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_time_selector);

    Bundle bundle = getIntent().getExtras();

    if(bundle != null) {
      initTimes = bundle.getLongArray("times");
      increment = bundle.getLongArray("increment");
      incrementType = bundle.getInt("increment_type");

      if(initTimes == null) {
        initTimes = new long[2];
        initTimes[0] = 0;
        initTimes[1] = 0;
      }

      if(increment == null) {
        increment = new long[2];
        increment[0] = 0;
        increment[1] = 0;
      }
    }
    else {
      initTimes = new long[2];
      initTimes[0] = 0;
      initTimes[1] = 0;
      increment = new long[2];
      increment[0] = 0;
      increment[1] = 0;
      incrementType = Constants.DELAY;
    }

    //SharedPreferences

    player1Text = (TextView) findViewById(R.id.player1Text);
    player2Text = (TextView) findViewById(R.id.player2Text);

    topTime = (Button) findViewById(R.id.player1Time);
    bottomTime = (Button) findViewById(R.id.player2Time);
    topIncrement = (Button) findViewById(R.id.player1Increment);
    bottomIncrement = (Button) findViewById(R.id.player2Increment);
    applyButton = (Button) findViewById(R.id.applyButton);
    cancelButton = (Button) findViewById(R.id.cancelButton);
    sameTimes = (CheckBox) findViewById(R.id.sameTimes);
    incFischer = (RadioButton) findViewById(R.id.incFischer);
    incDelay = (RadioButton) findViewById(R.id.incDelay);
    incBronstein = (RadioButton) findViewById(R.id.incBronstein);
    topTime.setOnClickListener(this);
    bottomTime.setOnClickListener(this);
    topIncrement.setOnClickListener(this);
    bottomIncrement.setOnClickListener(this);
    applyButton.setOnClickListener(this);
    cancelButton.setOnClickListener(this);
    sameTimes.setOnClickListener(this);
    incFischer.setOnClickListener(this);
    incDelay.setOnClickListener(this);
    incBronstein.setOnClickListener(this);

    setSameTime(false);
  }

  public static class HourMinSecPickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      //return super.onCreateDialog(savedInstanceState);
      LayoutInflater inflater = getActivity().getLayoutInflater();

      View v = inflater.inflate(R.layout.time_selector_hms, null);
      final NumberPicker hourPicker = (NumberPicker) v.findViewById(R.id.hourPicker);
      final NumberPicker minPicker = (NumberPicker) v.findViewById(R.id.minPicker);
      final NumberPicker secPicker = (NumberPicker) v.findViewById(R.id.secPicker);
      final TextView hourText = (TextView) v.findViewById(R.id.hourText);
      final TextView minuteText = (TextView) v.findViewById(R.id.minuteText);

      hourPicker.setMaxValue(99);
      hourPicker.setMinValue(0);
      minPicker.setMaxValue(59);
      minPicker.setMinValue(0);
      secPicker.setMaxValue(59);
      secPicker.setMinValue(0);

      if(fragmentMode > 1) {
        hourPicker.setVisibility(View.INVISIBLE);
        minPicker.setVisibility(View.INVISIBLE);
        hourText.setVisibility(View.INVISIBLE);
        minuteText.setVisibility(View.INVISIBLE);
      }
      else {
        hourPicker.setVisibility(View.VISIBLE);
        minPicker.setVisibility(View.VISIBLE);
        hourText.setVisibility(View.VISIBLE);
        minuteText.setVisibility(View.VISIBLE);
      }

      return new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustomTheme)
            .setTitle("Set Player Time")
            .setView(v)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                selectedTime = 0;
                selectedTime += hourPicker.getValue() * Constants.HOUR;
                selectedTime += minPicker.getValue() * Constants.MINUTE;
                selectedTime += secPicker.getValue() * Constants.SECOND;

                switch(fragmentMode) {
                  case 0:
                    playerTopTime = selectedTime;

                    if(!enablePlayer2)
                      playerBottomTime = selectedTime;
                    break;
                  case 1:
                    playerBottomTime = selectedTime;
                    break;
                  case 2:
                    playerTopIncrement = selectedTime;

                    if(!enablePlayer2)
                      playerBottomIncrement = selectedTime;
                    break;
                  case 3:
                    playerBottomIncrement = selectedTime;
                    break;
                }
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

              }
            })
            .create();
    }
  }

  @Override
  public void onClick(View v) {
    Intent intent;

    switch(v.getId()) {
      case R.id.player1Time:
        fragmentMode = 0;
        HourMinSecPickerFragment topTimeFragment = new HourMinSecPickerFragment();
        topTimeFragment.show(getFragmentManager(), "Top Time");
        break;
      case R.id.player2Time:
        fragmentMode = 1;
        HourMinSecPickerFragment bottomTimeFragment = new HourMinSecPickerFragment();
        bottomTimeFragment.show(getFragmentManager(), "Bottom Time");
        break;
      case R.id.player1Increment:
        fragmentMode = 2;
        HourMinSecPickerFragment topIncrementFragment = new HourMinSecPickerFragment();
        topIncrementFragment.show(getFragmentManager(), "Top Increment");
        break;
      case R.id.player2Increment:
        fragmentMode = 3;
        HourMinSecPickerFragment bottomIncrementFragment = new HourMinSecPickerFragment();
        bottomIncrementFragment.show(getFragmentManager(), "Bottom Increment");
        break;
      case R.id.applyButton:
        initTimes[0] = playerTopTime;
        if(enablePlayer2) initTimes[1] = playerBottomTime;
        else              initTimes[1] = playerTopTime;

        increment[0] = playerTopIncrement;
        if(enablePlayer2) increment[1] = playerBottomIncrement;
        else              increment[1] = playerTopIncrement;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("curr_time_p1", initTimes[0]);
        editor.putLong("curr_time_p2", initTimes[1]);
        editor.putLong("time_control_p1", initTimes[0]);
        editor.putLong("time_control_p2", initTimes[1]);
        editor.putLong("time_inc_p1", increment[0]);
        editor.putLong("time_inc_p2", increment[1]);
        editor.putInt("inc_type", incrementType);
        editor.apply();

        intent = new Intent(applyButton.getContext(), MainActivity.class);
        startActivityForResult(intent, 0);
        break;
      case R.id.cancelButton:
        intent = new Intent(applyButton.getContext(), MainActivity.class);
        startActivityForResult(intent, 0);
        break;
      case R.id.sameTimes:
        setSameTime(sameTimes.isChecked());
        break;
      case R.id.incFischer:
        incrementType = Constants.FISCHER;
        break;
      case R.id.incDelay:
        incrementType = Constants.DELAY;
        break;
      case R.id.incBronstein:
        incrementType = Constants.BRONSTEIN;
        break;
    }
  }

  public void setSameTime(boolean same) {
    enablePlayer2 = !same;
    //bottomTime.setClickable(same);
    bottomTime.setEnabled(!same);
    //bottomIncrement.setClickable(same);
    bottomIncrement.setEnabled(!same);

    if(same) {
      player1Text.setText("Time Settings");
      player2Text.setAlpha(0.5f);
    }
    else {
      player1Text.setText("Player 1");
      player2Text.setAlpha(1);
    }
  }
}
