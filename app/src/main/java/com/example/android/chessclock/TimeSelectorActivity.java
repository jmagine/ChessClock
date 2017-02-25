package com.example.android.chessclock;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import android.widget.Button;

import android.view.View;

import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.LinearLayout;


/**
 * Created by JasonMa on 2/24/2017.
 */

public class TimeSelectorActivity extends AppCompatActivity implements View.OnTouchListener{
  static int player;
  static int playerTopTime;
  static int playerBottomTime;
  static int selectedTime;

  long[] initTimes;

  Button topTime;
  Button bottomTime;
  Button applyButton;
  Button cancelButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_time_selector);

    Bundle bundle = getIntent().getExtras();

    if(bundle != null) {
      initTimes = bundle.getLongArray("times");
      if(initTimes != null) {
        initTimes = new long[2];

        initTimes[0] = 0;
        initTimes[1] = 0;
      }
    }
    else {
      initTimes = new long[2];

      initTimes[0] = 0;
      initTimes[1] = 0;
    }

    topTime = (Button) findViewById(R.id.player1Time);
    bottomTime = (Button) findViewById(R.id.player2Time);
    applyButton = (Button) findViewById(R.id.applyButton);
    cancelButton = (Button) findViewById(R.id.cancelButton);
    topTime.setOnTouchListener(this);
    bottomTime.setOnTouchListener(this);
    applyButton.setOnTouchListener(this);
    cancelButton.setOnTouchListener(this);

  }

  public static class NumberPickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      //return super.onCreateDialog(savedInstanceState);
      LayoutInflater inflater = getActivity().getLayoutInflater();

      View v = inflater.inflate(R.layout.time_selector_hms, null);
      final NumberPicker hourPicker = (NumberPicker) v.findViewById(R.id.hourPicker);
      final NumberPicker minPicker = (NumberPicker) v.findViewById(R.id.minPicker);
      final NumberPicker secPicker = (NumberPicker) v.findViewById(R.id.secPicker);
      hourPicker.setMaxValue(99);
      hourPicker.setMinValue(0);
      minPicker.setMaxValue(59);
      minPicker.setMinValue(0);
      secPicker.setMaxValue(59);
      secPicker.setMinValue(0);

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

                if(player == 0)
                  playerTopTime = selectedTime;
                else
                  playerBottomTime = selectedTime;
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
  public boolean onTouch(View v, MotionEvent m) {
    if(m.getAction() != MotionEvent.ACTION_DOWN)
      return false;

    Intent intent;

    switch (v.getId()) {
      case R.id.player1Time:
        player = 0;
        NumberPickerFragment topTimeFragment = new NumberPickerFragment();
        topTimeFragment.show(getFragmentManager(), "Top Time");
        break;
      case R.id.player2Time:
        player = 1;
        NumberPickerFragment bottomTimeFragment = new NumberPickerFragment();
        bottomTimeFragment.show(getFragmentManager(), "Bottom Time");
        break;
      case R.id.applyButton:
        initTimes[0] = playerTopTime;
        initTimes[1] = playerBottomTime;

        intent = new Intent(applyButton.getContext(), MainActivity.class);
        intent.putExtra("times", initTimes);
        Log.d("jaslogs", initTimes[0] + " " + initTimes[1]);
        startActivityForResult(intent, 0);
        break;
      case R.id.cancelButton:
        initTimes[0] = playerTopTime;
        initTimes[1] = playerBottomTime;

        intent = new Intent(applyButton.getContext(), MainActivity.class);
        intent.putExtra("times", initTimes);
        Log.d("jaslogs", initTimes[0] + " " + initTimes[1]);
        startActivityForResult(intent, 0);
        break;
    }

    return false;
  }
}
