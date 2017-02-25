package com.example.android.chessclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.media.MediaPlayer;

import android.util.Log;

import android.view.MotionEvent;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.preference.PreferenceManager;

import java.util.TimerTask;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, OnSharedPreferenceChangeListener{

  MediaPlayer mp1;
  MediaPlayer mp2;

  Button[] editTimeButtons;
  Button topButton;
  Button bottomButton;
  Button controlButton1;
  Button controlButton2;
  Button soundButton;
  Button settingsButton;
  Button topEditTimeModeUp;
  Button topEditTimeModeDown;
  Button bottomEditTimeModeUp;
  Button bottomEditTimeModeDown;
  ImageView topFirstFlag;
  ImageView bottomFirstFlag;
  TextView topTimeTV;
  TextView bottomTimeTV;

  //millis factors
  final long HOUR = 3600000;
  final long MINUTE = 60000;
  final long SECOND = 1000;

  //conversion factors
  final int MIN_PER_HOUR = 60;
  final int SEC_PER_MIN = 60;

  //display mode
  final int DISP_MIN_SEC = 0;
  final int DISP_HOUR_MIN = 1;
  final int DISP_AUTO = 2;

  //turn
  final int PAUSE = 0;
  final int PLAYER_TOP = 1;
  final int PLAYER_BOT = 2;

  //clock modes
  final int MODE_INIT      = 0;
  final int MODE_PLAY      = 1;
  final int MODE_PAUSE     = 2;
  final int MODE_EDIT_TIME = 3;

  String timeFormat;
  long[] initTimes;
  long topTime;
  long bottomTime;
  long tempTopTime;
  long tempBottomTime;
  long initTopTime;
  long initBottomTime;
  int turn;               //turn to move
  int currMode;           //current clock mode
  int displayTimeModeTop;    //HOUR_MIN or MIN_SEC mode
  int displayTimeModeBottom; //HOUR_MIN or MIN_SEC mode
  boolean playSounds;     //whether to play sounds
  boolean leadingZero;    //whether to display leading 0
  boolean blinkingColon;  //whether to blink colon
  boolean timeUnits;      //whether to display s or m after time

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mp1 = MediaPlayer.create(this, R.raw.tick);
    mp2 = MediaPlayer.create(this, R.raw.tick);

    //initialize all the ui elements
    editTimeButtons = new Button[8];
    topTimeTV = (TextView) findViewById(R.id.topTime);
    bottomTimeTV = (TextView) findViewById(R.id.bottomTime);
    topFirstFlag = (ImageView) findViewById(R.id.topFirstFlag);
    bottomFirstFlag = (ImageView) findViewById(R.id.bottomFirstFlag);
    topButton = (Button) findViewById(R.id.topButton);
    bottomButton = (Button) findViewById(R.id.bottomButton);
    controlButton1 = (Button) findViewById(R.id.controlButton1);
    controlButton2 = (Button) findViewById(R.id.controlButton2);
    soundButton = (Button) findViewById(R.id.soundButton);
    settingsButton = (Button) findViewById(R.id.settingsButton);
    topEditTimeModeUp = (Button) findViewById(R.id.topEditTimeModeUp);
    topEditTimeModeDown = (Button) findViewById(R.id.topEditTimeModeDown);
    bottomEditTimeModeUp = (Button) findViewById(R.id.bottomEditTimeModeUp);
    bottomEditTimeModeDown = (Button) findViewById(R.id.bottomEditTimeModeDown);
    editTimeButtons[0] = (Button) findViewById(R.id.editTimeTop1Up);
    editTimeButtons[1] = (Button) findViewById(R.id.editTimeTop2Up);
    editTimeButtons[2] = (Button) findViewById(R.id.editTimeBottom1Up);
    editTimeButtons[3] = (Button) findViewById(R.id.editTimeBottom2Up);
    editTimeButtons[4] = (Button) findViewById(R.id.editTimeTop1Down);
    editTimeButtons[5] = (Button) findViewById(R.id.editTimeTop2Down);
    editTimeButtons[6] = (Button) findViewById(R.id.editTimeBottom1Down);
    editTimeButtons[7] = (Button) findViewById(R.id.editTimeBottom2Down);

    topButton.setOnTouchListener(this);
    bottomButton.setOnTouchListener(this);
    controlButton1.setOnTouchListener(this);
    controlButton2.setOnTouchListener(this);
    soundButton.setOnTouchListener(this);
    settingsButton.setOnTouchListener(this);
    topEditTimeModeUp.setOnTouchListener(this);
    topEditTimeModeDown.setOnTouchListener(this);
    bottomEditTimeModeUp.setOnTouchListener(this);
    bottomEditTimeModeDown.setOnTouchListener(this);

    for(int i = 0; i < 8; i++)
      editTimeButtons[i].setOnTouchListener(this);

    //TODO figure out all the intent stuff
    //Need intent for setting time control

    Bundle bundle = getIntent().getExtras();

    if(bundle != null) {
      initTimes = bundle.getLongArray("times");
      if(initTimes != null) {
        initTopTime = initTimes[0];
        initBottomTime = initTimes[1];
      }
    }
    else {
      initTopTime    = 0 * HOUR + 0 * MINUTE + 1 * SECOND;
      initBottomTime = 0 * HOUR + 0 * MINUTE + 1 * SECOND;
    }

    playSounds = true;

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

    leadingZero = sharedPref.getBoolean("leading_zero", leadingZero);
    blinkingColon = sharedPref.getBoolean("blinking_colon", blinkingColon);
    timeUnits = sharedPref.getBoolean("time_units", timeUnits);
    timeFormat = sharedPref.getString("time_format", timeFormat);

    sharedPref.registerOnSharedPreferenceChangeListener(this);

    setMode(MODE_INIT);

    //update times each 10 ms
    Timer t = new Timer();
    t.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if(turn == PLAYER_TOP) {
          topTime -= 10;
        }
        else if(turn == PLAYER_BOT) {
          bottomTime -= 10;
        }

        updateTimes();
      }
    }, 10, 10);
  }

  @Override
  public boolean onTouch(View v, MotionEvent m) {
    if(m.getAction() != MotionEvent.ACTION_DOWN)
      return false;

    switch (v.getId()) {
      case R.id.topButton:
        if(mp1 != null && playSounds && turn != PLAYER_BOT)
          mp1.start();

        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF00CCCC);
        else               bottomButton.setBackgroundColor(0xFFFF4444);
        if(topTime > 0)    topButton.setBackgroundColor(0xFF222222);
        else               topButton.setBackgroundColor(0xFFFF4444);
        turn = PLAYER_BOT;
        setMode(MODE_PLAY);
        break;
      case R.id.bottomButton:
        if(mp2 != null && playSounds && turn != PLAYER_TOP)
          mp2.start();

        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF222222);
        else               bottomButton.setBackgroundColor(0xFFFF4444);
        if(topTime > 0)    topButton.setBackgroundColor(0xFF00CCCC);
        else               topButton.setBackgroundColor(0xFFFF4444);
        turn = PLAYER_TOP;
        setMode(MODE_PLAY);
        break;
      case R.id.controlButton1:
        if(currMode == MODE_INIT) {
          Intent myIntent = new Intent(controlButton1.getContext(), TimeSelectorActivity.class);
          startActivityForResult(myIntent, 0);
        }
        else if(currMode == MODE_PLAY || currMode == MODE_PAUSE)
          setMode(MODE_INIT);
        else if(currMode == MODE_EDIT_TIME)
          setMode(MODE_PAUSE);
        break;
      case R.id.controlButton2:
        if(currMode == MODE_INIT || currMode == MODE_PAUSE)
          setMode(MODE_EDIT_TIME);
        else if(currMode == MODE_EDIT_TIME) {
          revertChanges();
          setMode(MODE_PAUSE);
        }
        else
          setMode(MODE_PAUSE);
        break;
      case R.id.soundButton:
        if(playSounds)
          playSounds = false;
        else
          playSounds = true;
        break;
      case R.id.settingsButton:
        Intent myIntent = new Intent(settingsButton.getContext(), SettingsActivity.class);
        startActivityForResult(myIntent, 0);
        break;
      //editTimeMode buttons, when 1 is pressed it gets hidden and other gets shown, then editTimeMode is set. don't forget to put in xml
      case R.id.topEditTimeModeUp:
        topEditTimeModeUp.setVisibility(View.INVISIBLE);
        topEditTimeModeDown.setVisibility(View.VISIBLE);
        if(displayTimeModeTop != DISP_HOUR_MIN)
          displayTimeModeTop = DISP_HOUR_MIN;
        break;
      case R.id.topEditTimeModeDown:
        topEditTimeModeUp.setVisibility(View.VISIBLE);
        topEditTimeModeDown.setVisibility(View.INVISIBLE);
        if(displayTimeModeTop != DISP_MIN_SEC)
          displayTimeModeTop = DISP_MIN_SEC;
        break;
      case R.id.bottomEditTimeModeUp:
        bottomEditTimeModeUp.setVisibility(View.INVISIBLE);
        bottomEditTimeModeDown.setVisibility(View.VISIBLE);
        if(displayTimeModeBottom != DISP_HOUR_MIN)
          displayTimeModeBottom = DISP_HOUR_MIN;
        break;
      case R.id.bottomEditTimeModeDown:
        bottomEditTimeModeUp.setVisibility(View.VISIBLE);
        bottomEditTimeModeDown.setVisibility(View.INVISIBLE);
        if(displayTimeModeBottom != DISP_MIN_SEC)
          displayTimeModeBottom = DISP_MIN_SEC;
        break;
      case R.id.editTimeTop1Up:
        if(displayTimeModeTop == DISP_MIN_SEC) topTime += SECOND;
        else                           topTime += MINUTE;
        break;
      case R.id.editTimeTop2Up:
        if(displayTimeModeTop == DISP_MIN_SEC) topTime += MINUTE;
        else                           topTime += HOUR;
        break;
      case R.id.editTimeBottom1Up:
        if(displayTimeModeBottom == DISP_MIN_SEC) bottomTime += SECOND;
        else                              bottomTime += MINUTE;
        break;
      case R.id.editTimeBottom2Up:
        if(displayTimeModeBottom == DISP_MIN_SEC) bottomTime += MINUTE;
        else                              bottomTime += HOUR;
        break;
      case R.id.editTimeTop1Down:
        if(displayTimeModeTop == DISP_MIN_SEC) topTime -= SECOND;
        else                           topTime -= MINUTE;
        if(topTime < 0)                topTime = 0;
        break;
      case R.id.editTimeTop2Down:
        if(displayTimeModeTop == DISP_MIN_SEC) topTime -= MINUTE;
        else                           topTime -= HOUR;
        if(topTime < 0)                topTime = 0;
        break;
      case R.id.editTimeBottom1Down:
        if(displayTimeModeBottom == DISP_MIN_SEC) bottomTime -= SECOND;
        else                              bottomTime -= MINUTE;
        if(bottomTime < 0)                bottomTime = 0;
        break;
      case R.id.editTimeBottom2Down:
        if(displayTimeModeBottom == DISP_MIN_SEC) bottomTime -= MINUTE;
        else                              bottomTime -= HOUR;
        if(bottomTime < 0)                bottomTime = 0;
        break;
      default:
        break;
    }
    return false;
  }

  public void updateTimes() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if(turn != PLAYER_TOP || topTime / 1000 % 2 == 1)
          topTimeTV.setText(createTimeString(topTime, displayTimeModeTop, true, leadingZero, timeUnits));
        else
          topTimeTV.setText(createTimeString(topTime, displayTimeModeTop, !blinkingColon, leadingZero, timeUnits));

        if(topTime <= 0) {
          topTime = 0;
          topButton.setBackgroundColor(0xFFFF4444);

          if(bottomTime > 0 && currMode != MODE_EDIT_TIME)
            topFirstFlag.setVisibility(View.VISIBLE);
        }

        if(turn != PLAYER_BOT || bottomTime / 1000 % 2 == 1)
          bottomTimeTV.setText(createTimeString(bottomTime, displayTimeModeBottom, true, leadingZero, timeUnits));
        else
          bottomTimeTV.setText(createTimeString(bottomTime, displayTimeModeBottom, !blinkingColon, leadingZero, timeUnits));

        if(bottomTime <= 0) {
          bottomTime = 0;
          bottomButton.setBackgroundColor(0xFFFF4444);

          if(topTime > 0 && currMode != MODE_EDIT_TIME)
            bottomFirstFlag.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  public String createTimeString(long time, int displayMode, boolean colon, boolean leadingZero, boolean timeUnits) {
    String timeString = "";
    //Process HH:MM format times
    if((time >= HOUR || displayMode == DISP_HOUR_MIN) && (displayMode != DISP_MIN_SEC)) {
      if(time / HOUR < 10 && leadingZero)
        timeString = timeString.concat("0" + time / HOUR);
      else
        timeString = timeString.concat("" + time / HOUR);

      if(colon)
        timeString = timeString.concat(":");
      else
        timeString = timeString.concat(" ");

      if((time / MINUTE) % MIN_PER_HOUR < 10)
        timeString = timeString.concat("0" + (time / MINUTE) % MIN_PER_HOUR);
      else
        timeString = timeString.concat("" + (time / MINUTE) % MIN_PER_HOUR);

      if(timeUnits)
        timeString = timeString.concat("m");
    }

    //Process MM:SS format times
    else if(time >= MINUTE || displayMode == DISP_MIN_SEC) {
      if((time / MINUTE) % MIN_PER_HOUR < 10 && leadingZero)
        timeString = timeString.concat("0" + (time / MINUTE) % MIN_PER_HOUR);
      else
        timeString = timeString.concat("" + (time / MINUTE) % MIN_PER_HOUR);

      if(colon)
        timeString = timeString.concat(":");
      else
        timeString = timeString.concat(" ");

      if((time / SECOND) % SEC_PER_MIN < 10)
        timeString = timeString.concat("0" + (time / SECOND) % SEC_PER_MIN);
      else
        timeString = timeString.concat("" + (time / SECOND) % SEC_PER_MIN);

      if(timeUnits)
        timeString = timeString.concat("s");
    }

    //Process < 1 minute times
    else if(time > 0) {
      if(time / MINUTE < 10 && leadingZero)
        timeString = timeString.concat("0" + time / MINUTE);
      else
        timeString = timeString.concat("" + time / MINUTE);

      if(colon)
        timeString = timeString.concat(":");
      else
        timeString = timeString.concat(" ");

      if((time / SECOND) % SEC_PER_MIN < 10)
        timeString = timeString.concat("0" + (time / SECOND) % SEC_PER_MIN);
      else
        timeString = timeString.concat("" + (time / SECOND) % SEC_PER_MIN);

      if(timeUnits)
        timeString = timeString.concat("s");
    }

    //Process 0 or negative times
    else {
      if(leadingZero)
        timeString = "00:00";
      else
        timeString = "0:00";

      if(timeUnits)
        timeString = timeString.concat("s");
    }
    return timeString;
  }

  public void resetTimes() {
    topTime = initTopTime;
    bottomTime = initBottomTime;
  }

  public void revertChanges() {
    topTime = tempTopTime;
    bottomTime = tempBottomTime;
  }

  public void setMode(int mode) {

    switch(mode) {
      case MODE_INIT:

        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);
        topFirstFlag.setVisibility(View.INVISIBLE);
        bottomFirstFlag.setVisibility(View.INVISIBLE);

        topEditTimeModeUp.setVisibility(View.INVISIBLE);
        topEditTimeModeDown.setVisibility(View.INVISIBLE);
        bottomEditTimeModeUp.setVisibility(View.INVISIBLE);
        bottomEditTimeModeDown.setVisibility(View.INVISIBLE);

        controlButton1.setText("Time Control");
        controlButton2.setText("Edit Time");

        for(int i = 0; i < 8; i++) editTimeButtons[i].setVisibility(View.INVISIBLE);

        turn = PAUSE;
        resetTimes();

        //if out of time, button is still red. otherwise unpressed color
        if(topTime > 0)    topButton.setBackgroundColor(0xFF222222);
        else               topButton.setBackgroundColor(0xFFFF4444);
        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF222222);
        else               bottomButton.setBackgroundColor(0xFFFF4444);
        break;
      case MODE_PLAY:
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);

        controlButton1.setText("Reset");
        controlButton2.setText("Pause");

        for(int i = 0; i < 8; i++) editTimeButtons[i].setVisibility(View.INVISIBLE);
        break;
      case MODE_PAUSE:
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);

        topEditTimeModeUp.setVisibility(View.INVISIBLE);
        topEditTimeModeDown.setVisibility(View.INVISIBLE);
        bottomEditTimeModeUp.setVisibility(View.INVISIBLE);
        bottomEditTimeModeDown.setVisibility(View.INVISIBLE);

        //if out of time, button is still red. otherwise unpressed color
        if(topTime > 0)    topButton.setBackgroundColor(0xFF222222);
        else               topButton.setBackgroundColor(0xFFFF4444);
        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF222222);
        else               bottomButton.setBackgroundColor(0xFFFF4444);


        controlButton1.setText("Reset");
        controlButton2.setText("Edit Time");
        displayTimeModeTop = DISP_AUTO;
        displayTimeModeBottom = DISP_AUTO;

        for(int i = 0; i < 8; i++) editTimeButtons[i].setVisibility(View.INVISIBLE);

        turn = PAUSE;
        break;
      case MODE_EDIT_TIME:
        topButton.setVisibility(View.INVISIBLE);
        bottomButton.setVisibility(View.INVISIBLE);
        topFirstFlag.setVisibility(View.INVISIBLE);
        bottomFirstFlag.setVisibility(View.INVISIBLE);

        if(topTime < HOUR) {
          topEditTimeModeUp.setVisibility(View.VISIBLE);
          topEditTimeModeDown.setVisibility(View.INVISIBLE);
          displayTimeModeTop = DISP_MIN_SEC;
        }
        else {
          topEditTimeModeUp.setVisibility(View.INVISIBLE);
          topEditTimeModeDown.setVisibility(View.VISIBLE);
          displayTimeModeTop = DISP_HOUR_MIN;
        }

        if(bottomTime < HOUR) {
          bottomEditTimeModeUp.setVisibility(View.VISIBLE);
          bottomEditTimeModeDown.setVisibility(View.INVISIBLE);
          displayTimeModeBottom = DISP_MIN_SEC;
        }
        else {
          bottomEditTimeModeUp.setVisibility(View.INVISIBLE);
          bottomEditTimeModeDown.setVisibility(View.VISIBLE);
          displayTimeModeBottom = DISP_HOUR_MIN;
        }

        controlButton1.setText("Apply");
        controlButton2.setText("Cancel");

        tempTopTime = topTime;
        tempBottomTime = bottomTime;

        for(int i = 0; i < 8; i++) editTimeButtons[i].setVisibility(View.VISIBLE);

        turn = PAUSE;
        break;
    }

    currMode = mode;
  }

  public void onSharedPreferenceChanged(SharedPreferences prefs,
                                        String key) {
    switch(key) {
      case "leading_zero":
        leadingZero = prefs.getBoolean("leading_zero", false);
        break;
      case "blinking_colon":
        blinkingColon = prefs.getBoolean("blinking_colon", true);
        break;
      case "time_units":
        timeUnits = prefs.getBoolean("time_units", false);
        break;
      case "time_format":
        timeFormat = prefs.getString("time_format", "SS:d");
        break;
    }

  }

  @Override
  protected void onResume() {
    super.onResume();

    mp1 = MediaPlayer.create(this, R.raw.tick);
    mp2 = MediaPlayer.create(this, R.raw.tick);

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    sharedPref.getBoolean("leading_zero", leadingZero);
    sharedPref.getBoolean("blinking_colon", blinkingColon);
    sharedPref.getBoolean("time_units", timeUnits);
    sharedPref.getString("time_format", timeFormat);
  }

  @Override
  protected void onRestart() {
    super.onRestart();

    if(mp1 == null)
      mp1 = MediaPlayer.create(this, R.raw.tick);
    if(mp2 == null)
      mp2 = MediaPlayer.create(this, R.raw.tick);

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    sharedPref.getBoolean("leading_zero", leadingZero);
    sharedPref.getBoolean("blinking_colon", blinkingColon);
    sharedPref.getBoolean("time_units", timeUnits);
    sharedPref.getString("time_format", timeFormat);

    setMode(MODE_INIT);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if(mp1 != null)
      mp1.release();

    if(mp2 != null)
      mp2.release();
  }

  @Override
  protected void onStop() {
    super.onStop();

    if(mp1 != null)
      mp1.release();

    if(mp2 != null)
      mp2.release();
  }
}
