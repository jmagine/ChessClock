package com.example.android.chessclock;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.app.DialogFragment;
import android.widget.TimePicker;

/**
 * Created by JasonMa on 2/24/2017.
 */

public class TimeSelectorActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_time_selector);



  }

  public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

    }
  }
}
