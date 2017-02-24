package com.example.android.chessclock;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
  TextView topTimeTV;
  TextView bottomTimeTV;
  Button topButton;
  Button bottomButton;
  Button pauseButton;
  Button editButton;
  Button settingsButton;

  Button[] editTimeButtons;
  /*
  Button editTimeTop1Up;
  Button editTimeTop2Up;
  Button editTimeBottom1Up;
  Button editTimeBottom2Up;

  Button editTimeTop1Down;
  Button editTimeTop2Down;
  Button editTimeBottom1Down;
  Button editTimeBottom2Down;
  */
  final long HOUR = 3600000;
  final long MINUTE = 60000;
  final long SECOND = 1000;

  final int PAUSE = 0;
  final int PLAYER_TOP = 1;
  final int PLAYER_BOT = 2;

  final int MIN_PER_HOUR = 60;
  final int SEC_PER_MIN = 60;

  final int MODE_PLAY = 0;
  final int MODE_EDIT_TIME = 1;

  long[] initTimes;
  long topTime;
  long bottomTime;
  int turn = PAUSE;

  boolean editMode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    topTimeTV = (TextView) findViewById(R.id.topTime);
    bottomTimeTV = (TextView) findViewById(R.id.bottomTime);
    topButton = (Button) findViewById(R.id.topButton);
    bottomButton = (Button) findViewById(R.id.bottomButton);

    pauseButton = (Button) findViewById(R.id.pauseButton);
    editButton = (Button) findViewById(R.id.editButton);
    settingsButton = (Button) findViewById(R.id.settingsButton);
    /*
    editTimeTop1Up = (Button) findViewById(R.id.editTimeTop1Up);
    editTimeTop2Up = (Button) findViewById(R.id.editTimeTop2Up);
    editTimeBottom1Up = (Button) findViewById(R.id.editTimeBottom1Up);
    editTimeBottom2Up = (Button) findViewById(R.id.editTimeBottom2Up);

    editTimeTop1Down = (Button) findViewById(R.id.editTimeTop1Down);
    editTimeTop2Down = (Button) findViewById(R.id.editTimeTop2Down);
    editTimeBottom1Down = (Button) findViewById(R.id.editTimeBottom1Down);
    editTimeBottom2Down = (Button) findViewById(R.id.editTimeBottom2Down);

    editTimeTop1Up.setVisibility(View.INVISIBLE);
    */
    editTimeButtons = new Button[8];

    editTimeButtons[0] = (Button) findViewById(R.id.editTimeTop1Up);
    editTimeButtons[1] = (Button) findViewById(R.id.editTimeTop2Up);
    editTimeButtons[2] = (Button) findViewById(R.id.editTimeBottom1Up);
    editTimeButtons[3] = (Button) findViewById(R.id.editTimeBottom2Up);

    editTimeButtons[4] = (Button) findViewById(R.id.editTimeTop1Down);
    editTimeButtons[5] = (Button) findViewById(R.id.editTimeTop2Down);
    editTimeButtons[6] = (Button) findViewById(R.id.editTimeBottom1Down);
    editTimeButtons[7] = (Button) findViewById(R.id.editTimeBottom2Down);

    editMode = false;

    for(int i = 0; i < 8; i++) {
      editTimeButtons[i].setVisibility(View.INVISIBLE);
    }

    setMode(MODE_PLAY);

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
      topTime = 0 * HOUR + 1 * MINUTE;
      bottomTime = 0 * HOUR + 1 * SECOND;
    }

    topButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0) {
              bottomButton.setBackgroundColor(0xFF00CCCC);
            }
            else
              bottomButton.setBackgroundColor(0xFFFF4444);

            if(topTime > 0) {
              topButton.setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.topTime)).setText(createTimeString(topTime, 0, true, false));
            }
            else
              topButton.setBackgroundColor(0xFFFF4444);

            pauseButton.setVisibility(View.VISIBLE);
            turn = PLAYER_BOT;
            break;
        }
        return false;
      }
    });

    bottomButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0) {
              bottomButton.setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.bottomTime)).setText(createTimeString(bottomTime, 0, true, false));
            }
            else
              bottomButton.setBackgroundColor(0xFFFF4444);

            if(topTime > 0) {
              topButton.setBackgroundColor(0xFF00CCCC);
            }
            else
              topButton.setBackgroundColor(0xFFFF4444);
            pauseButton.setVisibility(View.VISIBLE);
            turn = PLAYER_TOP;
            break;
        }
        return false;
      }
    });

    pauseButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0) {
              bottomButton.setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.bottomTime)).setText(createTimeString(bottomTime, 0, true, false));
            }
            else
              bottomButton.setBackgroundColor(0xFFFF4444);

            if(topTime > 0) {
              topButton.setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.topTime)).setText(createTimeString(topTime, 0, true, false));
            }
            else
              topButton.setBackgroundColor(0xFFFF4444);

            pauseButton.setVisibility(View.INVISIBLE);
            turn = PAUSE;
            break;
        }
        return false;
      }
    });

    editButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(editMode) {
              editMode = false;
              setMode(MODE_PLAY);
            }
            else {
              editMode = true;
              setMode(MODE_EDIT_TIME);
            }
            break;
        }
        return false;
      }
    });

    settingsButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            Intent myIntent = new Intent(settingsButton.getContext(), SettingsActivity.class);
            startActivityForResult(myIntent, 0);
            break;
        }
        return false;
      }
    });



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

  public void updateTimes() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if(turn != PLAYER_TOP || topTime / 1000 % 2 == 1)
          topTimeTV.setText(createTimeString(topTime, 0, true, false));
        else
          topTimeTV.setText(createTimeString(topTime, 0, false, false));

        if(topTime <= 0)
          topButton.setBackgroundColor(0xFFFF4444);

        if(turn != PLAYER_BOT || bottomTime / 1000 % 2 == 1)
          bottomTimeTV.setText(createTimeString(bottomTime, 0, true, false));
        else
          bottomTimeTV.setText(createTimeString(bottomTime, 0, false, false));

        if(bottomTime <= 0)
          bottomButton.setBackgroundColor(0xFFFF4444);
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

  public void setMode(int mode) {
    if(mode == MODE_PLAY) {

    }

    if(mode == MODE_EDIT_TIME) {
      for(int i = 0; i < 8; i++) {
        editTimeButtons[i].setVisibility(View.VISIBLE);
      }
    }
    else {
      for(int i = 0; i < 8; i++) {
        editTimeButtons[i].setVisibility(View.INVISIBLE);
      }
    }
  }
}
