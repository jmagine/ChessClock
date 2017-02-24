package com.example.android.chessclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
  Button[] editTimeButtons;
  Button topButton;
  Button bottomButton;
  Button controlButton1;
  Button controlButton2;
  Button soundButton;
  Button settingsButton;
  ImageView topFirstFlag;
  ImageView bottomFirstFlag;
  TextView topTimeTV;
  TextView bottomTimeTV;

  final long HOUR = 3600000;
  final long MINUTE = 60000;
  final long SECOND = 1000;

  final int PAUSE = 0;
  final int PLAYER_TOP = 1;
  final int PLAYER_BOT = 2;

  final int MIN_PER_HOUR = 60;
  final int SEC_PER_MIN = 60;

  final int MODE_INIT      = 0;
  final int MODE_PLAY      = 1;
  final int MODE_PAUSE     = 2;
  final int MODE_EDIT_TIME = 3;

  long[] initTimes;
  long topTime;
  long bottomTime;
  long tempTopTime;
  long tempBottomTime;
  long initTopTime;
  long initBottomTime;
  int turn = PAUSE;
  int currMode;

  boolean editMode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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

    for(int i = 0; i < 8; i++)
      editTimeButtons[i].setOnTouchListener(this);

    //TODO figure out all the intent stuff
    //Need intent for setting time control

    Bundle bundle = getIntent().getExtras();

    if(bundle != null) {
      initTimes = bundle.getLongArray("times");
      if(initTimes != null) {
        topTime = initTimes[0];
        bottomTime = initTimes[1];
      }
    }
    else {
      initTopTime    = 0 * HOUR + 0 * MINUTE + 1 * SECOND;
      initBottomTime = 0 * HOUR + 0 * MINUTE + 1 * SECOND;
    }

    editMode = false;
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
        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF00CCCC);
        else               bottomButton.setBackgroundColor(0xFFFF4444);
        if(topTime > 0)    topButton.setBackgroundColor(0xFF222222);
        else               topButton.setBackgroundColor(0xFFFF4444);
        turn = PLAYER_BOT;
        setMode(MODE_PLAY);
        break;
      case R.id.bottomButton:
        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF222222);
        else               bottomButton.setBackgroundColor(0xFFFF4444);
        if(topTime > 0)    topButton.setBackgroundColor(0xFF00CCCC);
        else               topButton.setBackgroundColor(0xFFFF4444);
        turn = PLAYER_TOP;
        setMode(MODE_PLAY);
        break;
      case R.id.controlButton1:
        if(currMode == MODE_INIT) {

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
        //TODO sound
        break;
      case R.id.settingsButton:
        Intent myIntent = new Intent(settingsButton.getContext(), SettingsActivity.class);
        startActivityForResult(myIntent, 0);
        break;
      //TODO implement the HH:MM vs MM:SS edit time modes
      case R.id.editTimeTop1Up:      topTime    += SECOND; break;
      case R.id.editTimeTop2Up:      topTime    += MINUTE; break;
      case R.id.editTimeBottom1Up:   bottomTime += SECOND; break;
      case R.id.editTimeBottom2Up:   bottomTime += MINUTE; break;
      case R.id.editTimeTop1Down:    topTime    -= SECOND; if(topTime < 0)    topTime = 0;    break;
      case R.id.editTimeTop2Down:    topTime    -= MINUTE; if(topTime < 0)    topTime = 0;    break;
      case R.id.editTimeBottom1Down: bottomTime -= SECOND; if(bottomTime < 0) bottomTime = 0; break;
      case R.id.editTimeBottom2Down: bottomTime -= MINUTE; if(bottomTime < 0) bottomTime = 0; break;
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
          topTimeTV.setText(createTimeString(topTime, 0, true, false));
        else
          topTimeTV.setText(createTimeString(topTime, 0, false, false));

        if(topTime <= 0) {
          topTime = 0;
          topButton.setBackgroundColor(0xFFFF4444);

          if(bottomTime > 0)
            topFirstFlag.setVisibility(View.VISIBLE);
        }

        if(turn != PLAYER_BOT || bottomTime / 1000 % 2 == 1)
          bottomTimeTV.setText(createTimeString(bottomTime, 0, true, false));
        else
          bottomTimeTV.setText(createTimeString(bottomTime, 0, false, false));

        if(bottomTime <= 0) {
          bottomTime = 0;
          bottomButton.setBackgroundColor(0xFFFF4444);

          if(topTime > 0)
            bottomFirstFlag.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  public String createTimeString(long time, int displayMode, boolean colon, boolean leadingZero) {
    String timeString = "";
    //Process HH:MM format times
    if(time >= HOUR) {
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
    }

    //Process MM:SS format times
    else if(time >= MINUTE) {
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
    }

    //Process 0 or negative times
    else {
      if(leadingZero)
        timeString = "00:00";
      else
        timeString = "0:00";
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

        controlButton1.setText("Time Control");
        controlButton2.setText("Edit Time");

        for(int i = 0; i < 8; i++)
          editTimeButtons[i].setVisibility(View.INVISIBLE);

        turn = 0;
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

        for(int i = 0; i < 8; i++)
          editTimeButtons[i].setVisibility(View.INVISIBLE);
        break;
      case MODE_PAUSE:
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);

        //if out of time, button is still red. otherwise unpressed color
        if(topTime > 0)    topButton.setBackgroundColor(0xFF222222);
        else               topButton.setBackgroundColor(0xFFFF4444);
        if(bottomTime > 0) bottomButton.setBackgroundColor(0xFF222222);
        else               bottomButton.setBackgroundColor(0xFFFF4444);


        controlButton1.setText("Reset");
        controlButton2.setText("Edit Time");

        for(int i = 0; i < 8; i++)
          editTimeButtons[i].setVisibility(View.INVISIBLE);

        turn = 0;
        break;
      case MODE_EDIT_TIME:
        topButton.setVisibility(View.INVISIBLE);
        bottomButton.setVisibility(View.INVISIBLE);
        topFirstFlag.setVisibility(View.INVISIBLE);
        bottomFirstFlag.setVisibility(View.INVISIBLE);

        controlButton1.setText("Apply");
        controlButton2.setText("Cancel");

        tempTopTime = topTime;
        tempBottomTime = bottomTime;

        for(int i = 0; i < 8; i++)
          editTimeButtons[i].setVisibility(View.VISIBLE);

        turn = 0;
        break;
    }

    currMode = mode;
  }
}
