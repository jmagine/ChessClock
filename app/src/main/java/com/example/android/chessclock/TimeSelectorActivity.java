package com.example.android.chessclock;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.app.DialogFragment;
import android.view.LayoutInflater;
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

public class TimeSelectorActivity extends AppCompatActivity {

  static int playerTopTime;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_time_selector);

    Button topTime = (Button) findViewById(R.id.player1Time);
    topTime.setOnClickListener(onclick);

  }

  View.OnClickListener onclick =
        new View.OnClickListener(){
          @Override
          public void onClick(View view){
            NumberPickerFragment topTimeFragment = new NumberPickerFragment();
            topTimeFragment.show(getFragmentManager(), "Top Time");
          }
        };

  public static class NumberPickerFragment extends DialogFragment implements NumberPicker.OnValueChangeListener {

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
            .setTitle("Set Player 1 Time")
            .setView(v)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                playerTopTime = 0;
                playerTopTime += hourPicker.getValue() * Constants.HOUR;
                playerTopTime += minPicker.getValue() * Constants.MINUTE;
                playerTopTime += secPicker.getValue() * Constants.SECOND;
                Log.d("jaslog", "PlayerTopTime: " + playerTopTime);
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

              }
            })
            .create();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
  }
}
