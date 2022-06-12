package com.leon.xplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;

public class TimerActivity extends AppCompatActivity implements View.OnClickListener {
    Button SetStop,CancelStop;
    Button SetStart,CancelStart;
    private AlarmManager StopAlarm;
    private PendingIntent StopPi;
    private AlarmManager StartAlarm;
    private PendingIntent StartPi;
    private Calendar curTime;
    private Calendar c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        SetStop = findViewById(R.id.setStopMusic);
        CancelStop = findViewById(R.id.CancelStopMusic);
        StopAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        SetStart = findViewById(R.id.setStartMusic);
        CancelStart = findViewById(R.id.CancelStartMusic);
        StartAlarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        /*registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("Alarm", "now");
                    }
                },
                new IntentFilter("Alarm")
        );*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            StopPi = PendingIntent.getBroadcast(
                    this,
                    0,
                    new Intent("StopMusic"),
                    PendingIntent.FLAG_IMMUTABLE
            );
            StartPi = PendingIntent.getBroadcast(
                    this,
                    0,
                    new Intent("StartMusic"),
                    PendingIntent.FLAG_IMMUTABLE
            );

        } else {
            StopPi = PendingIntent.getBroadcast(
                    this,
                    0,
                    new Intent("StopMusic"),
                    PendingIntent.FLAG_ONE_SHOT
            );
            StartPi = PendingIntent.getBroadcast(
                    this,
                    0,
                    new Intent("StartMusic"),
                    PendingIntent.FLAG_ONE_SHOT
            );
        }




        SetStop.setOnClickListener(this::onClick);
        CancelStop.setOnClickListener(this::onClick);
        SetStart.setOnClickListener(this::onClick);
        CancelStart.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.setStopMusic:
                curTime = Calendar.getInstance();
                new TimePickerDialog(TimerActivity.this, 0, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(System.currentTimeMillis());
                        c.set(Calendar.HOUR_OF_DAY,h);
                        c.set(Calendar.MINUTE,m);
                        c.set(Calendar.SECOND,0);
                        System.out.println(new Date(c.getTimeInMillis()));

                        StopAlarm.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),StopPi);
                        Log.d("timmer",c.getTimeInMillis() + "stop setting done" );
                        Toast.makeText(TimerActivity.this,"定时器设置完毕"+c.getTimeInMillis(),Toast.LENGTH_LONG).show();
                    }
                },curTime.get(Calendar.HOUR_OF_DAY),curTime.get(Calendar.MINUTE),false).show();
                break;
            case R.id.CancelStopMusic:
                StopAlarm.cancel(StopPi);
                Toast.makeText(TimerActivity.this, "定时停止已取消", Toast.LENGTH_LONG)
                        .show();
                Log.d("timmer","stop cancel" );
                break;
            case R.id.setStartMusic:
                curTime = Calendar.getInstance();
                new TimePickerDialog(TimerActivity.this, 0, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        c = Calendar.getInstance();
                        c.setTimeInMillis(System.currentTimeMillis());
                        c.set(Calendar.HOUR_OF_DAY,h);
                        c.set(Calendar.MINUTE,m);
                        c.set(Calendar.SECOND,0);
                        System.out.println(new Date(c.getTimeInMillis()));

                        StartAlarm.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),StartPi);
                        Log.d("timmer",c.getTimeInMillis() + "start setting done" );
                        Toast.makeText(TimerActivity.this,"定时器设置完毕"+c.getTimeInMillis(),Toast.LENGTH_LONG).show();
                    }
                },curTime.get(Calendar.HOUR_OF_DAY),curTime.get(Calendar.MINUTE),false).show();
                break;
            case R.id.CancelStartMusic:
                StartAlarm.cancel(StartPi);
                Toast.makeText(TimerActivity.this, "定时启动已取消", Toast.LENGTH_LONG)
                        .show();
                Log.d("timmer","start cancel" );
                break;
        }
    }
}