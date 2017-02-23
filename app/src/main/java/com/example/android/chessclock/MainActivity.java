package com.example.android.chessclock;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
  final int PAUSE = 0;
  final int PLAYER_TOP = 1;
  final int PLAYER_BOT = 2;

  final long HOUR = 3600000;
  final long MINUTE = 60000;
  final long SECOND = 1000;

  final int MIN_PER_HOUR = 60;
  final int SEC_PER_MIN = 60;

  long topTime;
  long bottomTime;
  int turn = PAUSE;

  TextView topTimeTV;
  TextView bottomTimeTV;
  Button topButton;
  Button bottomButton;
  Button pauseButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    topTimeTV = (TextView) findViewById(R.id.topTime);
    bottomTimeTV = (TextView) findViewById(R.id.bottomTime);
    topButton = (Button) findViewById(R.id.topButton);
    bottomButton = (Button) findViewById(R.id.bottomButton);
    pauseButton = (Button) findViewById(R.id.pauseButton);
    topTime = 0 * HOUR + 1 * MINUTE;
    bottomTime = 0 * HOUR + 1 * SECOND;

    //TODO figure out all the intent stuff
    //Need intent for setting time control
    //final Bundle bundle1 = getIntent().getExtras();

    findViewById(R.id.topButton).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0) {
              findViewById(R.id.bottomButton).setBackgroundColor(0xFF00CCCC);
            }
            else
              findViewById(R.id.bottomButton).setBackgroundColor(0xFFFF4444);

            if(topTime > 0) {
              findViewById(R.id.topButton).setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.topTime)).setText(createTimeString(topTime, 0, true, false));
            }
            else
              findViewById(R.id.topButton).setBackgroundColor(0xFFFF4444);

            findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
            turn = PLAYER_BOT;
            break;
        }
        return false;
      }
    });

    findViewById(R.id.bottomButton).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0) {
              findViewById(R.id.bottomButton).setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.bottomTime)).setText(createTimeString(bottomTime, 0, true, false));
            }
            else
              findViewById(R.id.bottomButton).setBackgroundColor(0xFFFF4444);

            if(topTime > 0) {
              findViewById(R.id.topButton).setBackgroundColor(0xFF00CCCC);
            }
            else
              findViewById(R.id.topButton).setBackgroundColor(0xFFFF4444);
            findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
            turn = PLAYER_TOP;
            break;
        }
        return false;
      }
    });

    findViewById(R.id.pauseButton).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0) {
              findViewById(R.id.bottomButton).setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.bottomTime)).setText(createTimeString(bottomTime, 0, true, false));
            }
            else
              findViewById(R.id.bottomButton).setBackgroundColor(0xFFFF4444);

            if(topTime > 0) {
              findViewById(R.id.topButton).setBackgroundColor(0xFF222222);
              //((TextView) findViewById(R.id.topTime)).setText(createTimeString(topTime, 0, true, false));
            }
            else
              findViewById(R.id.topButton).setBackgroundColor(0xFFFF4444);

            findViewById(R.id.pauseButton).setVisibility(View.INVISIBLE);
            turn = PAUSE;
            break;
        }
        return false;
      }
    });

    findViewById(R.id.editButton).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:

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
}
