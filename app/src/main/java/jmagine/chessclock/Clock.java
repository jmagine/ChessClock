package jmagine.chessclock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Binder;
import android.support.v4.app.NotificationCompat;

import android.app.Service;
import android.content.Intent;
import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Build;

import java.util.Timer;
import java.util.TimerTask;


import android.util.Log;

//Handles logic for clock state
public class Clock extends Service {
    private IBinder binder = new LocalBinder();
    int turn = -1;
    int control;
    int comp_type;

    long[] time_default;
    long[] comp_default;
    long[] time_curr;
    long[] comp_curr;

    long[] moves;
    Timer timer;

    public static final String CHANNEL_ID = "ClockForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("clock", "onCreate");

        this.time_default = new long[2];
        this.comp_default = new long[2];
        this.time_curr = new long[2];
        this.comp_curr = new long[2];

        this.moves = new long[2];
        this.control = Const.SUDDEN_DEATH;
        this.comp_type = Const.DELAY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("clock", "onStartCommand");

        if(this.timer != null) {
            Log.d("clock", "stopped timer");
            this.timer.cancel();
        }

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new ClockPeriod(), 0, Const.TICK_INTERVAL);
        Log.d("clock", "created timer");
        return START_NOT_STICKY;
    }

    public void startInForeground() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("content title")
                .setContentText("content text")
                .setSmallIcon(R.drawable.plus)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("clock", "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("clock", "onDestroy");

    }

    public void tick() {
        //don't do anything if mode is not PLAY
        if(this.turn != 0 && this.turn != 1) {
            return;
        }

        //modify clock values based on clock and increment mode
        switch(this.control) {
            case Const.SUDDEN_DEATH:
                switch(this.comp_type) {
                    case Const.FISCHER: case Const.DELAY:
                        if(comp_curr[this.turn] >= 0)
                            comp_curr[this.turn] -= Const.TICK_INTERVAL;
                        else
                            time_curr[this.turn] -= Const.TICK_INTERVAL;
                        break;
                    case Const.BRONSTEIN:
                        time_curr[this.turn] -= Const.TICK_INTERVAL;
                        break;
                }
                break;
            case Const.BYO_YOMI:
                break;
            case Const.HOURGLASS:
                break;
        }
    }

    public void setComp(int next_turn) {
        switch(this.comp_type) {
            //add comp to clock at beginning of new turn
            case Const.FISCHER:
                this.time_curr[next_turn] += this.comp_default[next_turn];
                break;
            //set delay for next turn
            case Const.DELAY:
                this.comp_curr[next_turn] = this.comp_default[next_turn];
                break;
            //time is capped at what it was at beginning of turn
            case Const.BRONSTEIN:
                this.time_curr[this.turn] = Math.min(this.comp_default[this.turn], this.comp_curr[this.turn]);
                this.comp_curr[next_turn] = this.time_curr[next_turn];
                break;
        }
    }

    //flips turn
    public void setTurn(int turn) {
        Log.d("clock", "setTurn");
        //don't do anything if turn does not flip
        if(turn == this.turn) return;

        if(turn == -1) {
            if(this.timer != null) this.timer.cancel();
            stopForeground(true); //TODO don't know which ones to keep
        }

        this.setComp(turn);
        if(this.turn >= 0) this.moves[this.turn] += 1;

        this.turn = turn;
    }

    public void resetTimes() {
        Log.d("clock", "resetTimes");
        this.time_curr[0] = this.time_default[0];
        this.time_curr[1] = this.time_default[1];
        this.moves[0] = 0;
        this.moves[1] = 0;
        stopForeground(true); //TODO don't know which ones to keep
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private class ClockPeriod extends TimerTask {
        public void run() {
            tick();
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        Clock getService() {
            // Return this instance of LocalService so clients can call public methods
            return Clock.this;
        }
    }
}
