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

import android.widget.ImageView;
import android.widget.LinearLayout;
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
  static long playerTopTime;
  static long playerBottomTime;
  static long playerTopIncrement;
  static long playerBottomIncrement;

  int incrementType;

  static boolean enablePlayer2;

  LinearLayout player2TimeSelection;

  ImageView separator;

  TextView player1Text;
  TextView player2Text;

  Button applyButton;
  Button cancelButton;

  NumberPicker hourPicker1;
  NumberPicker minPicker1;
  NumberPicker secPicker1;
  NumberPicker incPicker1;
  NumberPicker hourPicker2;
  NumberPicker minPicker2;
  NumberPicker secPicker2;
  NumberPicker incPicker2;

  CheckBox sameTimes;

  RadioButton incFischer;
  RadioButton incDelay;
  RadioButton incBronstein;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_time_selector);

    player2TimeSelection = (LinearLayout) findViewById(R.id.player2TimeSelection);
    player1Text = (TextView) findViewById(R.id.player1Text);
    player2Text = (TextView) findViewById(R.id.player2Text);

    separator = (ImageView) findViewById(R.id.separator);

    applyButton = (Button) findViewById(R.id.applyButton);
    cancelButton = (Button) findViewById(R.id.cancelButton);
    sameTimes = (CheckBox) findViewById(R.id.sameTimes);
    incFischer = (RadioButton) findViewById(R.id.incFischer);
    incDelay = (RadioButton) findViewById(R.id.incDelay);
    incBronstein = (RadioButton) findViewById(R.id.incBronstein);

    hourPicker1 = (NumberPicker) findViewById(R.id.hourPicker1);
    minPicker1 = (NumberPicker) findViewById(R.id.minPicker1);
    secPicker1 = (NumberPicker) findViewById(R.id.secPicker1);
    incPicker1 = (NumberPicker) findViewById(R.id.incPicker1);

    hourPicker2 = (NumberPicker) findViewById(R.id.hourPicker2);
    minPicker2 = (NumberPicker) findViewById(R.id.minPicker2);
    secPicker2 = (NumberPicker) findViewById(R.id.secPicker2);
    incPicker2 = (NumberPicker) findViewById(R.id.incPicker2);

    applyButton.setOnClickListener(this);
    cancelButton.setOnClickListener(this);
    sameTimes.setOnClickListener(this);
    incFischer.setOnClickListener(this);
    incDelay.setOnClickListener(this);
    incBronstein.setOnClickListener(this);

    hourPicker1.setMaxValue(99);
    hourPicker1.setMinValue(0);
    minPicker1.setMaxValue(59);
    minPicker1.setMinValue(0);
    secPicker1.setMaxValue(59);
    secPicker1.setMinValue(0);
    incPicker1.setMaxValue(60);
    incPicker1.setMinValue(0);

    hourPicker2.setMaxValue(99);
    hourPicker2.setMinValue(0);
    minPicker2.setMaxValue(59);
    minPicker2.setMinValue(0);
    secPicker2.setMaxValue(59);
    secPicker2.setMinValue(0);
    incPicker2.setMaxValue(60);
    incPicker2.setMinValue(0);

    hourPicker1.setValue(0);

    //SharedPreferences
    getPreferences();

    if (playerTopTime == playerBottomTime && playerTopIncrement == playerBottomIncrement) {
      setSameTime(true);
      sameTimes.setChecked(true);
    }
    else {
      setSameTime(false);
      sameTimes.setChecked(false);
    }
  }

  @Override
  public void onClick(View v) {
    Intent intent;

    switch(v.getId()) {
      case R.id.applyButton:

        playerTopTime = 0;
        playerTopTime += hourPicker1.getValue() * Constants.HOUR;
        playerTopTime += minPicker1.getValue() * Constants.MINUTE;
        playerTopTime += secPicker1.getValue() * Constants.SECOND;
        playerTopIncrement = incPicker1.getValue() * Constants.SECOND;

        if(enablePlayer2) {
          playerBottomTime = 0;
          playerBottomTime += hourPicker2.getValue() * Constants.HOUR;
          playerBottomTime += minPicker2.getValue() * Constants.MINUTE;
          playerBottomTime += secPicker2.getValue() * Constants.SECOND;
          playerBottomIncrement = incPicker2.getValue() * Constants.SECOND;
        }
        else {
          playerBottomTime = playerTopTime;
          playerBottomIncrement = playerTopIncrement;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("curr_time_p1", playerTopTime);
        editor.putLong("curr_time_p2", playerBottomTime);
        editor.putLong("time_control_p1", playerTopTime);
        editor.putLong("time_control_p2", playerBottomTime);
        editor.putLong("time_inc_p1", playerTopIncrement);
        editor.putLong("time_inc_p2", playerBottomIncrement);
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
    hourPicker2.setEnabled(!same);
    minPicker2.setEnabled(!same);
    secPicker2.setEnabled(!same);
    incPicker2.setEnabled(!same);

    if(same) {
      player1Text.setText("Both Times");
      player2TimeSelection.setVisibility(View.GONE);
      player2Text.setVisibility(View.GONE);
      separator.setVisibility(View.GONE);

    }
    else {
      player1Text.setText("Top Time");
      player2TimeSelection.setVisibility(View.VISIBLE);
      player2Text.setVisibility(View.VISIBLE);
      separator.setVisibility(View.VISIBLE);
    }
  }

  public void getPreferences() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    playerTopTime = sharedPref.getLong("time_control_p1", 5 * Constants.MINUTE);
    playerBottomTime = sharedPref.getLong("time_control_p2", 5 * Constants.MINUTE);
    playerTopIncrement = sharedPref.getLong("time_inc_p1", 0);
    playerBottomIncrement = sharedPref.getLong("time_inc_p2", 0);
    incrementType = sharedPref.getInt("inc_type", Constants.DELAY);

    hourPicker1.setValue((int) (playerTopTime / Constants.HOUR));
    minPicker1.setValue((int) ((playerTopTime % Constants.HOUR) / Constants.MINUTE));
    secPicker1.setValue((int) ((playerTopTime % Constants.MINUTE) / Constants.SECOND));
    incPicker1.setValue((int) (playerTopIncrement / Constants.SECOND));

    hourPicker2.setValue((int) (playerBottomTime / Constants.HOUR));
    minPicker2.setValue((int) ((playerBottomTime % Constants.HOUR) / Constants.MINUTE));
    secPicker2.setValue((int) ((playerBottomTime % Constants.MINUTE) / Constants.SECOND));
    incPicker2.setValue((int) (playerBottomIncrement / Constants.SECOND));
  }
}
