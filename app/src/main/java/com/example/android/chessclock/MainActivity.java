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

  long topTime;
  long bottomTime;
  int turn = PAUSE;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    topTime = 60000;
    bottomTime = 60000;

    findViewById(R.id.topButton).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(bottomTime > 0)
              findViewById(R.id.bottomButton).setBackgroundColor(0xFF00FFFF);
            else
              findViewById(R.id.bottomButton).setBackgroundColor(0xFFFF4444);

            if(topTime > 0)
              findViewById(R.id.topButton).setBackgroundColor(0x222222CC);
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
            if(bottomTime > 0)
              findViewById(R.id.bottomButton).setBackgroundColor(0x222222CC);
            else
              findViewById(R.id.bottomButton).setBackgroundColor(0xFFFF4444);

            if(topTime > 0)
              findViewById(R.id.topButton).setBackgroundColor(0xFF00FFFF);
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
            if(bottomTime > 0)
              findViewById(R.id.bottomButton).setBackgroundColor(0x222222CC);
            else
              findViewById(R.id.bottomButton).setBackgroundColor(0xFFFF4444);

            if(topTime > 0)
              findViewById(R.id.topButton).setBackgroundColor(0x222222CC);
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
        String timeString = "";
        Button timeButton;
        TextView timeText;
        long time;
        if(turn == PLAYER_TOP) {
          timeButton = (Button) findViewById(R.id.topButton);
          timeText = (TextView) findViewById(R.id.topTime);
          time = topTime;
        }
        else if(turn == PLAYER_BOT) {
          timeButton = (Button) findViewById(R.id.bottomButton);
          timeText = (TextView) findViewById(R.id.bottomTime);
          time = bottomTime;
        }
        else
          return;

        //TODO append times to a string instead to make them more dynamic
        if(time > 0) {

          if(time / 60000 < 10)
            timeString = timeString.concat("0" + time / 60000);
          else
            timeString = timeString.concat("" + time / 60000);
          timeString = timeString.concat(":");

          if((time / 1000) % 60 < 10)
            timeString = timeString.concat("0" + (time / 1000) % 60);
          else
            timeString = timeString.concat("" + (time / 1000) % 60);

          timeText.setText(timeString);

          //timeText.setText("" + time);
        }
        else {
          timeText.setText("00:00");
          timeButton.setBackgroundColor(0xFFFF4444);
        }
      }
    });
  }
}
