package jmagine.chessclock;

import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;

import android.media.MediaPlayer;

import android.view.MotionEvent;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.preference.PreferenceManager;

import java.util.TimerTask;
import java.util.Timer;

public class MainActivity extends AppCompatActivity
                          implements View.OnTouchListener, OnSharedPreferenceChangeListener{

  MediaPlayer mp1;
  MediaPlayer mp2;

  ImageButton[] editTimeButtons;
  Button topButton;
  Button bottomButton;
  ImageButton controlButton1;
  ImageButton controlButton2;
  ImageButton soundButton;
  ImageButton settingsButton;
  ImageButton topEditTimeModeUp;
  ImageButton topEditTimeModeDown;
  ImageButton bottomEditTimeModeUp;
  ImageButton bottomEditTimeModeDown;
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
  final int MODE_RESET_CNF = 4;

  String timeFormat;
  String timeRotation;
  long[] time_default;
  long[] comp_default;

  //TODO turn these into arrays too
  long time_top;
  long time_bot;
  long time_top_temp;
  long time_bot_temp;
  long top_comp;
  long bot_comp;
  long bronsteinTopTime;
  long bronsteinBottomTime;

  int turn;               //turn to move
  int currMode;           //current clock mode
  int displayTimeModeTop;    //HOUR_MIN or MIN_SEC mode
  int displayTimeModeBottom; //HOUR_MIN or MIN_SEC mode
  int incrementType;
  boolean playSounds;     //whether to play sounds
  boolean leadingZero;    //whether to display leading 0
  boolean blinkingColon;  //whether to blink colon
  boolean timeUnits;      //whether to display s or m after time

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Create 2 ticking MediaPlayers, one for each button
    mp1 = MediaPlayer.create(this, R.raw.tick);
    mp2 = MediaPlayer.create(this, R.raw.tick);

    //Initialize all the ui elements and set listeners
    editTimeButtons = new ImageButton[8];
    topTimeTV = (TextView) findViewById(R.id.topTime);
    bottomTimeTV = (TextView) findViewById(R.id.bottomTime);
    topFirstFlag = (ImageView) findViewById(R.id.topFirstFlag);
    bottomFirstFlag = (ImageView) findViewById(R.id.bottomFirstFlag);
    topButton = (Button) findViewById(R.id.topButton);
    bottomButton = (Button) findViewById(R.id.bottomButton);
    controlButton1 = (ImageButton) findViewById(R.id.controlButton1);
    controlButton2 = (ImageButton) findViewById(R.id.controlButton2);
    soundButton = (ImageButton) findViewById(R.id.soundButton);
    settingsButton = (ImageButton) findViewById(R.id.settingsButton);
    topEditTimeModeUp = (ImageButton) findViewById(R.id.topEditTimeModeUp);
    topEditTimeModeDown = (ImageButton) findViewById(R.id.topEditTimeModeDown);
    bottomEditTimeModeUp = (ImageButton) findViewById(R.id.bottomEditTimeModeUp);
    bottomEditTimeModeDown = (ImageButton) findViewById(R.id.bottomEditTimeModeDown);
    editTimeButtons[0] = (ImageButton) findViewById(R.id.editTimeTop1Up);
    editTimeButtons[1] = (ImageButton) findViewById(R.id.editTimeTop2Up);
    editTimeButtons[2] = (ImageButton) findViewById(R.id.editTimeBottom1Up);
    editTimeButtons[3] = (ImageButton) findViewById(R.id.editTimeBottom2Up);
    editTimeButtons[4] = (ImageButton) findViewById(R.id.editTimeTop1Down);
    editTimeButtons[5] = (ImageButton) findViewById(R.id.editTimeTop2Down);
    editTimeButtons[6] = (ImageButton) findViewById(R.id.editTimeBottom1Down);
    editTimeButtons[7] = (ImageButton) findViewById(R.id.editTimeBottom2Down);

    //Technically works, just find a better way to do it and put it in all entry points in app
    topTimeTV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


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

    //Initialize time control and comp_default arrays in preparation for shared preferences update
    time_default = new long[2];
    comp_default = new long[2];

    //Get preferences and set default values if not set
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    getPreferences();
    sharedPref.registerOnSharedPreferenceChangeListener(this);
    displayTimeModeTop = DISP_AUTO;
    displayTimeModeBottom = DISP_AUTO;
    setMode(MODE_INIT);

    //TODO look into real-time alternatives like SystemClock
    //Update times each 10 ms
    Timer t = new Timer();
    t.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if(turn == PLAYER_TOP) {
          if(top_comp > 0) top_comp -= 10;
          else             time_top -= 10;
        }
        else if(turn == PLAYER_BOT) {
          if(bot_comp > 0) bot_comp -= 10;
          else             time_bot -= 10;
        }

        updateUI();
      }
    }, 10, 10);
  }

  @Override
  public boolean onTouch(View v, MotionEvent m) {
    topTimeTV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    if(m.getAction() != MotionEvent.ACTION_DOWN)
      return false;

    switch (v.getId()) {
      case R.id.topButton:
        if(mp1 != null && playSounds && turn != PLAYER_BOT)
          mp1.start();

        if(time_bot > 0) bottomButton.setBackgroundColor(getResources().getColor(R.color.button_active));
        else               bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if(time_top > 0)    topButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

        if(currMode == MODE_INIT || turn == PLAYER_TOP) {
          switch (incrementType) {
            case Constants.DELAY:
              bot_comp = comp_default[1];
              break;
            case Constants.FISCHER:
              time_bot += comp_default[1];
              break;
            case Constants.BRONSTEIN:
              time_top += comp_default[0];

              if (time_top > bronsteinTopTime)
                time_top = bronsteinTopTime;

              bronsteinBottomTime = time_bot;
              break;
          }
        }

        turn = PLAYER_BOT;
        setMode(MODE_PLAY);
        break;
      case R.id.bottomButton:
        if(mp2 != null && playSounds && turn != PLAYER_TOP)
          mp2.start();

        if(time_bot > 0) bottomButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if(time_top > 0)    topButton.setBackgroundColor(getResources().getColor(R.color.button_active));
        else               topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

        if(currMode == MODE_INIT || turn == PLAYER_BOT) {
          switch (incrementType) {
            case Constants.DELAY:
              top_comp = comp_default[0];
              break;
            case Constants.FISCHER:
              time_top += comp_default[0];
              break;
            case Constants.BRONSTEIN:
              time_bot += comp_default[1];

              if (time_bot > bronsteinBottomTime)
                time_bot = bronsteinBottomTime;

              bronsteinTopTime = time_top;
              break;
          }
        }
        turn = PLAYER_TOP;
        setMode(MODE_PLAY);
        break;
      case R.id.controlButton1:
        if(currMode == MODE_INIT) {
          Intent intent = new Intent(controlButton1.getContext(), TimeSelectorActivity.class);
          intent.putExtra("times", time_default);
          intent.putExtra("comp_default", comp_default);
          intent.putExtra("increment_type", incrementType);
          startActivityForResult(intent, 0);
        }
        else if(currMode == MODE_RESET_CNF) {
          setMode(MODE_INIT);
        }
        else if(currMode == MODE_PLAY || currMode == MODE_PAUSE)
          setMode(MODE_RESET_CNF);
        else if(currMode == MODE_EDIT_TIME)
          if(time_top == time_default[0] && time_bot == time_default[1])
            setMode(MODE_INIT);
          else
            setMode(MODE_PAUSE);
        break;
      case R.id.controlButton2:
        if(currMode == MODE_INIT || currMode == MODE_PAUSE)
          setMode(MODE_EDIT_TIME);
        else if(currMode == MODE_EDIT_TIME) {
           revertChanges();

          if(time_top == time_default[0] && time_bot == time_default[1])
            setMode(MODE_INIT);
          else
            setMode(MODE_PAUSE);
        }
        else if(currMode == MODE_RESET_CNF) {
          setMode(MODE_PAUSE);
        }
        else
          setMode(MODE_PAUSE);
        break;
      case R.id.soundButton:
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        if(playSounds) {
          playSounds = false;
          editor.putBoolean("sound", false);
          soundButton.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.sound_off));
        }
        else {
          playSounds = true;
          editor.putBoolean("sound", true);
          soundButton.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.sound_on));
        }
        editor.apply();
        break;
      case R.id.settingsButton:
        Intent intent = new Intent( this, SettingsActivity.class );
        intent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
        intent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );

        //old settings activity intent code
        //Intent myIntent = new Intent(settingsButton.getContext(), SettingsActivity.class);
        //startActivityForResult(myIntent, 0);
        startActivity(intent);
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
        if(displayTimeModeTop == DISP_MIN_SEC) time_top += SECOND;
        else                           time_top += MINUTE;
        break;
      case R.id.editTimeTop2Up:
        if(displayTimeModeTop == DISP_MIN_SEC) time_top += MINUTE;
        else                           time_top += HOUR;
        break;
      case R.id.editTimeBottom1Up:
        if(displayTimeModeBottom == DISP_MIN_SEC) time_bot += SECOND;
        else                              time_bot += MINUTE;
        break;
      case R.id.editTimeBottom2Up:
        if(displayTimeModeBottom == DISP_MIN_SEC) time_bot += MINUTE;
        else                              time_bot += HOUR;
        break;
      case R.id.editTimeTop1Down:
        if(displayTimeModeTop == DISP_MIN_SEC) time_top -= SECOND;
        else                           time_top -= MINUTE;
        if(time_top < 0)                time_top = 0;
        break;
      case R.id.editTimeTop2Down:
        if(displayTimeModeTop == DISP_MIN_SEC) time_top -= MINUTE;
        else                           time_top -= HOUR;
        if(time_top < 0)                time_top = 0;
        break;
      case R.id.editTimeBottom1Down:
        if(displayTimeModeBottom == DISP_MIN_SEC) time_bot -= SECOND;
        else                              time_bot -= MINUTE;
        if(time_bot < 0)                time_bot = 0;
        break;
      case R.id.editTimeBottom2Down:
        if(displayTimeModeBottom == DISP_MIN_SEC) time_bot -= MINUTE;
        else                              time_bot -= HOUR;
        if(time_bot < 0)                time_bot = 0;
        break;
      default:
        break;
    }
    return false;
  }

  @Override
  protected void onResume() {
    super.onResume();

    topTimeTV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    mp1 = MediaPlayer.create(this, R.raw.tick);
    mp2 = MediaPlayer.create(this, R.raw.tick);
    displayTimeModeTop = DISP_AUTO;
    displayTimeModeBottom = DISP_AUTO;
    getPreferences();

    if(time_top == time_default[0] && time_bot == time_default[1])
      setMode(MODE_INIT);
    else
      setMode(MODE_PAUSE);
  }

  @Override
  protected void onRestart() {
    super.onRestart();

    topTimeTV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    if(mp1 == null)
      mp1 = MediaPlayer.create(this, R.raw.tick);
    if(mp2 == null)
      mp2 = MediaPlayer.create(this, R.raw.tick);
    displayTimeModeTop = DISP_AUTO;
    displayTimeModeBottom = DISP_AUTO;
    getPreferences();

    setMode(MODE_INIT);
  }

  @Override
  protected void onPause() {
    super.onPause();

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putLong("curr_time_p1", time_top);
    editor.putLong("curr_time_p2", time_bot);

    editor.apply();

    if(mp1 != null)
      mp1.release();

    if(mp2 != null)
      mp2.release();
  }

  @Override
  protected void onStop() {
    super.onStop();

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putLong("time_p1", time_top);
    editor.putLong("time_p2", time_bot);

    editor.apply();

    if(mp1 != null)
      mp1.release();

    if(mp2 != null)
      mp2.release();
  }

  public void updateUI() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        /*
        switch(timeRotation) {
          case "-90":
            topTimeTV.setRotation(-90);
            bottomTimeTV.setRotation(-90);

            break;
          case "90":
            topTimeTV.setRotation(90);
            bottomTimeTV.setRotation(90);
            break;
          case "180 and 0":
            topTimeTV.setRotation(180);
            bottomTimeTV.setRotation(0);
            break;
        }
        */

        //TODO clean this

        //TODO testing the time string creation optimizations
        if(turn == PLAYER_TOP && top_comp > 0)
          topTimeTV.setText(createTimeString(top_comp, displayTimeModeTop, blinkingColon, leadingZero, timeUnits));
        else if(turn != PLAYER_TOP || time_top / 1000 % 2 == 1)
          topTimeTV.setText(createTimeString(time_top, displayTimeModeTop, true, leadingZero, timeUnits));
        else
          topTimeTV.setText(createTimeString(time_top, displayTimeModeTop, !blinkingColon, leadingZero, timeUnits));

        if(time_top <= 0) {
          time_top = 0;
          topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

          if(time_bot > 0 && currMode != MODE_EDIT_TIME)
            topFirstFlag.setVisibility(View.VISIBLE);
        }

        if(turn == PLAYER_BOT && bot_comp > 0)
          bottomTimeTV.setText(createTimeString(bot_comp, displayTimeModeBottom, blinkingColon, leadingZero, timeUnits));
        else if(turn != PLAYER_BOT || time_bot / 1000 % 2 == 1)
          bottomTimeTV.setText(createTimeString(time_bot, displayTimeModeBottom, true, leadingZero, timeUnits));
        else
          bottomTimeTV.setText(createTimeString(time_bot, displayTimeModeBottom, !blinkingColon, leadingZero, timeUnits));

        if(time_bot <= 0) {
          time_bot = 0;
          bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

          if(time_top > 0 && currMode != MODE_EDIT_TIME)
            bottomFirstFlag.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  //TODO clean or move this
  public String createTimeString(long time, int displayMode, boolean colon, boolean leadingZero, boolean timeUnits) {
    String timeString = "";
    //Process HH:MM format times
    if((time >= HOUR || displayMode == DISP_HOUR_MIN) && (displayMode != DISP_MIN_SEC)) {
      if(time / HOUR < 10 && leadingZero) timeString = timeString.concat("0" + time / HOUR);
      else                                timeString = timeString.concat("" + time / HOUR);

      if(colon) timeString = timeString.concat(":");
      else      timeString = timeString.concat(" ");

      if((time / MINUTE) % MIN_PER_HOUR < 10) timeString = timeString.concat("0" + (time / MINUTE) % MIN_PER_HOUR);
      else                                    timeString = timeString.concat("" + (time / MINUTE) % MIN_PER_HOUR);

      if(timeUnits)
        timeString = timeString.concat("m");
    }

    //Process MM:SS format times
    else if(time >= MINUTE || displayMode == DISP_MIN_SEC) {
      if((time / MINUTE) % MIN_PER_HOUR < 10 && leadingZero) timeString = timeString.concat("0" + (time / MINUTE) % MIN_PER_HOUR);
      else                                                   timeString = timeString.concat("" + (time / MINUTE) % MIN_PER_HOUR);

      if(colon) timeString = timeString.concat(":");
      else      timeString = timeString.concat(" ");

      if((time / SECOND) % SEC_PER_MIN < 10) timeString = timeString.concat("0" + (time / SECOND) % SEC_PER_MIN);
      else                                   timeString = timeString.concat("" + (time / SECOND) % SEC_PER_MIN);

      if(timeUnits) timeString = timeString.concat("s");
    }

    //Process lower than 1 minute times
    else if(time > 0) {
      switch(timeFormat) {
        case "SS.d":
          if((time / SECOND) % SEC_PER_MIN < 10) timeString = timeString.concat("0" + (time / SECOND) % SEC_PER_MIN);
          else                                   timeString = timeString.concat("" + (time / SECOND) % SEC_PER_MIN);

          timeString = timeString.concat("." + (time / 100) % 10);
          break;
        case "0:SS":
          timeString = "0";

          if(colon) timeString = timeString.concat(":");
          else      timeString = timeString.concat(" ");

          if((time / SECOND) % SEC_PER_MIN < 10) timeString = timeString.concat("0" + (time / SECOND) % SEC_PER_MIN);
          else                                   timeString = timeString.concat("" + (time / SECOND) % SEC_PER_MIN);
          break;
        case "SS":
          if((time / SECOND) % SEC_PER_MIN < 10) timeString = timeString.concat("0" + (time / SECOND) % SEC_PER_MIN);
          else                                   timeString = timeString.concat("" + (time / SECOND) % SEC_PER_MIN);
          break;
      }

      if(timeUnits) timeString = timeString.concat("s");
    }

    //Process 0 or negative times
    else {
      switch(timeFormat) {
        case "SS.d":
          timeString = "00.0";
          break;
        case "0:SS":
          if(leadingZero) timeString = "0";
          else            timeString = "";

          timeString = timeString.concat("0:00");
          break;
        case "SS":
          timeString = "00";
          break;
      }

      if(timeUnits) timeString = timeString.concat("s");
    }
    return timeString;
  }

  public void resetTimes() {
    time_top = time_default[0];
    time_bot = time_default[1];
  }

  public void revertChanges() {
    time_top = time_top_temp;
    time_bot = time_bot_temp;
  }

  public void setMode(int mode) {

    switch(mode) {
      case MODE_INIT:

        //Update UI
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);
        topFirstFlag.setVisibility(View.INVISIBLE);
        bottomFirstFlag.setVisibility(View.INVISIBLE);

        topEditTimeModeUp.setVisibility(View.INVISIBLE);
        topEditTimeModeDown.setVisibility(View.INVISIBLE);
        bottomEditTimeModeUp.setVisibility(View.INVISIBLE);
        bottomEditTimeModeDown.setVisibility(View.INVISIBLE);

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.plus));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.edit_time));

        for(int i = 0; i < 8; i++) editTimeButtons[i].setVisibility(View.INVISIBLE);

        //Update Model
        turn = PAUSE;
        resetTimes();

        bronsteinTopTime = time_top;
        bronsteinBottomTime = time_bot;

        //if out of time, button is still red. otherwise unpressed color
        if(time_top > 0)    topButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if(time_bot > 0) bottomButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        break;
      case MODE_PLAY:
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.reset));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.pause));

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
        if(time_top > 0)    topButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if(time_bot > 0) bottomButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.reset));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.edit_time));
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

        if(time_top < HOUR) {
          topEditTimeModeUp.setVisibility(View.VISIBLE);
          topEditTimeModeDown.setVisibility(View.INVISIBLE);
          displayTimeModeTop = DISP_MIN_SEC;
        }
        else {
          topEditTimeModeUp.setVisibility(View.INVISIBLE);
          topEditTimeModeDown.setVisibility(View.VISIBLE);
          displayTimeModeTop = DISP_HOUR_MIN;
        }

        if(time_bot < HOUR) {
          bottomEditTimeModeUp.setVisibility(View.VISIBLE);
          bottomEditTimeModeDown.setVisibility(View.INVISIBLE);
          displayTimeModeBottom = DISP_MIN_SEC;
        }
        else {
          bottomEditTimeModeUp.setVisibility(View.INVISIBLE);
          bottomEditTimeModeDown.setVisibility(View.VISIBLE);
          displayTimeModeBottom = DISP_HOUR_MIN;
        }

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.apply));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.cancel));

        time_top_temp = time_top;
        time_bot_temp = time_bot;

        for(int i = 0; i < 8; i++) editTimeButtons[i].setVisibility(View.VISIBLE);

        turn = PAUSE;
        break;
      case MODE_RESET_CNF:
        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.apply));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.cancel));
        break;
    }

    currMode = mode;
  }

  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    switch(key) {
      case "time_format":
        timeFormat = prefs.getString("time_format", "SS.d");
        break;
      case "time_rotation":
        timeRotation = prefs.getString("time_rotation", "-90");
        break;
      case "leading_zero":
        leadingZero = prefs.getBoolean("leading_zero", false);
        break;
      case "blinking_colon":
        blinkingColon = prefs.getBoolean("blinking_colon", true);
        break;
      case "time_units":
        timeUnits = prefs.getBoolean("time_units", false);
        break;
      case "sound":
        playSounds = prefs.getBoolean("sound", true);
    }

  }

  public void getPreferences() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    timeFormat = sharedPref.getString("time_format", "SS.d");
    timeRotation = sharedPref.getString("time_rotation", "-90");
    leadingZero = sharedPref.getBoolean("leading_zero", false);
    blinkingColon = sharedPref.getBoolean("blinking_colon", true);
    timeUnits = sharedPref.getBoolean("time_units", true);
    playSounds = sharedPref.getBoolean("sound", true);
    time_default[0] = sharedPref.getLong("time_control_p1", 5 * MINUTE);
    time_default[1] = sharedPref.getLong("time_control_p2", 5 * MINUTE);
    comp_default[0] = sharedPref.getLong("time_inc_p1", 0);
    comp_default[1] = sharedPref.getLong("time_inc_p2", 0);
    time_top = sharedPref.getLong("curr_time_p1", 5 * MINUTE);
    time_bot = sharedPref.getLong("curr_time_p2", 5 * MINUTE);
    incrementType = sharedPref.getInt("inc_type", Constants.DELAY);

    if(playSounds)
      soundButton.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.sound_on));
    else
      soundButton.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.sound_off));

    switch(timeRotation) {
      case "-90":
        topTimeTV.setRotation(-90);
        bottomTimeTV.setRotation(-90);

        break;
      case "90":
        topTimeTV.setRotation(90);
        bottomTimeTV.setRotation(90);
        break;
      case "180 and 0":
        topTimeTV.setRotation(180);
        bottomTimeTV.setRotation(0);
        break;
    }
  }

}
