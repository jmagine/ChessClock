package jmagine.chessclock;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
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

import android.util.Log;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, OnSharedPreferenceChangeListener{

  MediaPlayer mp1;
  MediaPlayer mp2;

  Button topButton;
  Button bottomButton;
  ImageButton controlButton1;
  ImageButton controlButton2;
  ImageButton soundButton;
  ImageButton settingsButton;
  ImageView topFirstFlag;
  ImageView bottomFirstFlag;
  TextView topTimeTV;
  TextView bottomTimeTV;

  //turn
  final int PAUSE = -1;
  final int PLAYER_TOP = 0;
  final int PLAYER_BOT = 1;

  //clock modes
  final int MODE_UNINIT    = 0;
  final int MODE_INIT      = 1;
  final int MODE_PLAY      = 2;
  final int MODE_PAUSE     = 3;
  final int MODE_EDIT_TIME = 4;
  final int MODE_RESET_CNF = 5;

  String timeFormat;
  String timeRotation;

  //TODO turn these into arrays too
  Clock clock;
  boolean clockBound = false;

  Timer timer;

  int currMode = MODE_UNINIT; //current clock mode
  boolean playSounds;     //whether to play sounds
  boolean leadingZero;    //whether to display leading 0
  boolean blinkingColon;  //whether to blink colon
  boolean timeUnits;      //whether to display s or m after time

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d("main", "onCreate");

    setContentView(R.layout.activity_main);

    //Create 2 ticking MediaPlayers, one for each button
    mp1 = MediaPlayer.create(this, R.raw.tick);
    mp2 = MediaPlayer.create(this, R.raw.tick);

    //Initialize all the ui elements and set listeners
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

    //Technically works, just find a better way to do it and put it in all entry points in app
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    topButton.setOnTouchListener(this);
    bottomButton.setOnTouchListener(this);
    controlButton1.setOnTouchListener(this);
    controlButton2.setOnTouchListener(this);
    soundButton.setOnTouchListener(this);
    settingsButton.setOnTouchListener(this);

    //Get preferences and set default values if not set
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    getPreferences();
    sharedPref.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();

    Log.d("main", "onStart");

    Intent intent = new Intent(this, Clock.class);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);
  }

  private ServiceConnection connection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      Clock.LocalBinder binder = (Clock.LocalBinder) service;
      clock = binder.getService();
      clockBound = true;

      setClockFromPreferences();
      if(clock.moves[0] + clock.moves[1] == 0)
        setMode(MODE_INIT);
      else
        setMode(MODE_PAUSE);

      Log.d("main", "clock service connect");
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      clockBound = false;
      Log.d("main", "clock service disconnect");
    }
  };

  @Override
  protected void onResume() {
    super.onResume();

    Log.d("main", "onResume");

    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    if(mp1 == null)
      mp1 = MediaPlayer.create(this, R.raw.tick);
    if(mp2 == null)
      mp2 = MediaPlayer.create(this, R.raw.tick);

    getPreferences();

    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        updateUI();
      }
    }, 0, 50);
  }

  @Override
  protected void onRestart() {
    super.onRestart();

    Log.d("main", "onRestart");
  }

  @Override
  protected void onPause() {
    super.onPause();

    Log.d("main", "onPause");

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putLong("curr_time_p1", clock.time_curr[PLAYER_TOP]);
    editor.putLong("curr_time_p2", clock.time_curr[PLAYER_BOT]);
    editor.putLong("moves_p1", clock.moves[PLAYER_TOP]);
    editor.putLong("moves_p2", clock.moves[PLAYER_BOT]);
    editor.commit();

    if(mp1 != null)
      mp1.release();

    if(mp2 != null)
      mp2.release();
  }

  @Override
  protected void onStop() {
    super.onStop();

    Log.d("main", "onStop");

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putLong("curr_time_p1", clock.time_curr[PLAYER_TOP]);
    editor.putLong("curr_time_p2", clock.time_curr[PLAYER_BOT]);
    editor.putLong("moves_p1", clock.moves[PLAYER_TOP]);
    editor.putLong("moves_p2", clock.moves[PLAYER_BOT]);
    editor.commit();

    if(mp1 != null)
      mp1.release();

    if(mp2 != null)
      mp2.release();

    if(clockBound) {
      unbindService(connection);
      clockBound = false;
      Log.d("main", "clock service unbound");
    }
  }

  @Override
  public boolean onTouch(View v, MotionEvent m) {
    View decorView = getWindow().getDecorView();;
    if(m.getAction() != MotionEvent.ACTION_DOWN)
      return false;

    if(!clockBound) {
      Log.d("main", "onTouch clock service not bound yet");
      return false;
    }

    if(currMode == MODE_PLAY)
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    Intent intent;
    switch (v.getId()) {
      case R.id.topButton:
        if(mp1 != null && playSounds && clock.turn != PLAYER_BOT)
          mp1.start();

        intent = new Intent(this, Clock.class);
        startService(intent);

        if(clock.time_curr[PLAYER_BOT] > 0) bottomButton.setBackgroundColor(getResources().getColor(R.color.button_active));
        else               bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if(clock.time_curr[PLAYER_TOP] > 0)    topButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

        clock.setTurn(PLAYER_BOT);

        setMode(MODE_PLAY);
        break;
      case R.id.bottomButton:
        if(mp2 != null && playSounds && clock.turn != PLAYER_TOP)
          mp2.start();

        intent = new Intent(this, Clock.class);
        startService(intent);

        if(clock.time_curr[PLAYER_BOT] > 0) bottomButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else               bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if(clock.time_curr[PLAYER_TOP] > 0)    topButton.setBackgroundColor(getResources().getColor(R.color.button_active));
        else               topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

        clock.setTurn(PLAYER_TOP);

        setMode(MODE_PLAY);
        break;
      case R.id.controlButton1:
        if(currMode == MODE_INIT) {
          intent = new Intent(controlButton1.getContext(), TimeSelectorActivity.class);
          intent.putExtra("times", clock.time_default);
          intent.putExtra("comp_default", clock.comp_default);
          intent.putExtra("increment_type", clock.comp_type);
          startActivityForResult(intent, 0);
        }
        else if(currMode == MODE_RESET_CNF) {
          setMode(MODE_INIT);
        }
        else if(currMode == MODE_PLAY || currMode == MODE_PAUSE)
          setMode(MODE_RESET_CNF);
        else if(currMode == MODE_EDIT_TIME)
          if(clock.moves[0] + clock.moves[1] == 0)
            setMode(MODE_INIT);
          else
            setMode(MODE_PAUSE);
        break;
      case R.id.controlButton2:
        if(currMode == MODE_INIT || currMode == MODE_PAUSE)
          setMode(MODE_EDIT_TIME);
        else if(currMode == MODE_EDIT_TIME) {
          if(clock.moves[0] + clock.moves[1] == 0)
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
        editor.commit();
        break;
      case R.id.settingsButton:
        intent = new Intent( this, SettingsActivity.class );
        intent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
        intent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );
        startActivity(intent);
        break;
      default:
        break;
    }
    return false;
  }

  public void updateUI() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if(!clockBound) return;

        if(clock.turn == PLAYER_TOP && clock.comp_curr[PLAYER_TOP] > 0)
          topTimeTV.setText(createTimeString(clock.comp_curr[PLAYER_TOP], blinkingColon, leadingZero, timeUnits));
        else if(clock.turn != PLAYER_TOP || clock.time_curr[PLAYER_TOP] / Const.SECOND % 2 == 1)
          topTimeTV.setText(createTimeString(clock.time_curr[PLAYER_TOP], true, leadingZero, timeUnits));
        else
          topTimeTV.setText(createTimeString(clock.time_curr[PLAYER_TOP], !blinkingColon, leadingZero, timeUnits));

        if(clock.time_curr[PLAYER_TOP] <= 0) {
          clock.time_curr[PLAYER_TOP] = 0;
          topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

          if(clock.time_curr[PLAYER_BOT] > 0 && currMode != MODE_EDIT_TIME)
            topFirstFlag.setVisibility(View.VISIBLE);
        }

        if(clock.turn == PLAYER_BOT && clock.comp_curr[PLAYER_BOT] > 0)
          bottomTimeTV.setText(createTimeString(clock.comp_curr[PLAYER_BOT], blinkingColon, leadingZero, timeUnits));
        else if(clock.turn != PLAYER_BOT || clock.time_curr[PLAYER_BOT] / Const.SECOND % 2 == 1)
          bottomTimeTV.setText(createTimeString(clock.time_curr[PLAYER_BOT], true, leadingZero, timeUnits));
        else
          bottomTimeTV.setText(createTimeString(clock.time_curr[PLAYER_BOT], !blinkingColon, leadingZero, timeUnits));

        if(clock.time_curr[PLAYER_BOT] <= 0) {
          clock.time_curr[PLAYER_BOT] = 0;
          bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

          if(clock.time_curr[PLAYER_TOP] > 0 && currMode != MODE_EDIT_TIME)
            bottomFirstFlag.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  public String createTimeString(long ms, boolean colon, boolean leadingZero, boolean timeUnits) {
    String timeString = "";
    String colonString;
    String leadingZeroString;
    String highTimeString;
    String lowTimeString;
    String highUnitString = "";
    String lowUnitString = "";
    String format;

    //ms sanity check
    if (ms < 0) {
      ms = 0;
    }

    //parse ms into its components
    long h = ms / Const.HOUR;
    long m = (ms / Const.MINUTE) % Const.MIN_PER_HOUR;
    long s = (ms /Const.SECOND) % Const.SEC_PER_MIN;
    long d = (ms / 100) % 10;

    if(ms >= Const.HOUR) {
      highTimeString = Long.toString(h);
      lowTimeString = Long.toString(m);
      if(timeUnits){
        highUnitString = "h";
        lowUnitString = "m";
      }

    }
    else if(ms >= Const.MINUTE) {
      highTimeString = Long.toString(m);
      lowTimeString = Long.toString(s);
      if(timeUnits) {
        highUnitString = "m";
        lowUnitString = "s";
      }
    }
    else {
      highTimeString = Long.toString(s);
      lowTimeString = Long.toString(d);
      if(timeUnits) {
        highUnitString = "";
        lowUnitString = "s";
      }
    }

    //colon
    if(colon && ms >= Const.MINUTE) colonString = ":";
    else if(ms >= Const.MINUTE) colonString = " ";
    else colonString = ".";

    //leadingZero
    if(leadingZero && highTimeString.length() < 2) leadingZeroString = "0";
    else leadingZeroString = "";

    timeString = "" + leadingZeroString + highTimeString + highUnitString + colonString + lowTimeString + lowUnitString;
    return timeString;
  }

  public void setMode(int mode) {

    if (mode == currMode)
      return;

    if(!clockBound) {
      Log.d("main", "setMode clock service not bound yet");
      return;
    }

    switch (mode) {
      case MODE_INIT:

        //Update UI
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);
        topFirstFlag.setVisibility(View.INVISIBLE);
        bottomFirstFlag.setVisibility(View.INVISIBLE);

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.plus));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.edit_time));

        clock.turn = -1;
        clock.resetTimes();

        //if out of time, button is still red. otherwise unpressed color
        if (clock.time_curr[PLAYER_TOP] > 0)
          topButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if (clock.time_curr[PLAYER_BOT] > 0)
          bottomButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        break;
      case MODE_PLAY:
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.reset));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.pause));

        break;
      case MODE_PAUSE:
        topButton.setVisibility(View.VISIBLE);
        bottomButton.setVisibility(View.VISIBLE);

        //if out of time, button is still red. otherwise unpressed color
        if (clock.time_curr[PLAYER_TOP] > 0)
          topButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else topButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));
        if (clock.time_curr[PLAYER_BOT] > 0)
          bottomButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
        else bottomButton.setBackgroundColor(getResources().getColor(R.color.button_flagged));

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.reset));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.edit_time));

        clock.turn = PAUSE;
        break;
      case MODE_EDIT_TIME:
        topButton.setVisibility(View.INVISIBLE);
        bottomButton.setVisibility(View.INVISIBLE);
        topFirstFlag.setVisibility(View.INVISIBLE);
        bottomFirstFlag.setVisibility(View.INVISIBLE);

        controlButton1.setImageDrawable(ContextCompat.getDrawable(controlButton1.getContext(), R.drawable.apply));
        controlButton2.setImageDrawable(ContextCompat.getDrawable(controlButton2.getContext(), R.drawable.cancel));

        clock.turn = PAUSE;
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

  public void setClockFromPreferences() {
    if(!clockBound) {
      Log.d("main", "setClockFromPreferences clock not bound");
      return;
    }

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    clock.time_default[0] = sharedPref.getLong("time_control_p1", 5 * Const.MINUTE);
    clock.time_default[1] = sharedPref.getLong("time_control_p2", 5 * Const.MINUTE);
    clock.comp_default[0] = sharedPref.getLong("time_inc_p1", 0);
    clock.comp_default[1] = sharedPref.getLong("time_inc_p2", 0);
    clock.time_curr[0] = sharedPref.getLong("curr_time_p1", 5 * Const.MINUTE);
    clock.time_curr[1] = sharedPref.getLong("curr_time_p2", 5 * Const.MINUTE);
    clock.comp_type = sharedPref.getInt("inc_type", Const.DELAY);
    clock.moves[0] = sharedPref.getLong("moves_p1", 0);
    clock.moves[1] = sharedPref.getLong("moves_p2", 0);
  }

  public void getPreferences() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    timeFormat = sharedPref.getString("time_format", "SS.d");
    timeRotation = sharedPref.getString("time_rotation", "-90");
    leadingZero = sharedPref.getBoolean("leading_zero", false);
    blinkingColon = sharedPref.getBoolean("blinking_colon", true);
    timeUnits = sharedPref.getBoolean("time_units", true);
    playSounds = sharedPref.getBoolean("sound", true);

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
