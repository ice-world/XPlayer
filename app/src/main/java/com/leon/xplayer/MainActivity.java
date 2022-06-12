package com.leon.xplayer;

import static com.leon.xplayer.Lib.config.ADD_MUSIC_EXTRA_ALL;
import static com.leon.xplayer.Lib.config.ADD_MUSIC_EXTRA_FAVORITE;
import static com.leon.xplayer.Lib.config.CATCHER_ASKING;
import static com.leon.xplayer.Lib.config.CATCHER_RESPONSE;
import static com.leon.xplayer.Lib.config.LOGIN_EXTRA_PASSWORD;
import static com.leon.xplayer.Lib.config.LOGIN_EXTRA_USERNAME;
import static com.leon.xplayer.Lib.config.MUSIC_SERVICE_MUSIC_FINISHED;
import static com.leon.xplayer.Lib.config.ModeEnum;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leon.xplayer.Lib.MusicAdapter;
import com.leon.xplayer.Lib.MusicInfo;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // User data
    boolean loggedIn = false;
    boolean onSeekBarTouch = false;

    // Music data
    int playPos = 0;
    int curTime = 0;
    int totalTime = 0;

    ModeEnum playMode = ModeEnum.order;

    // Instances
    ActivityResultLauncher<Intent> loginLauncher;
    ActivityResultLauncher<Intent> addMusicLauncher;
    ArrayList<MusicInfo> allMusicList;
    ArrayList<MusicInfo> favoriteList;
    MusicAdapter musicAdapter;
    Dialog moreDialog;
    MusicServiceConnection musicConnection;
    MusicService.Binder musicBinder;
    MusicFinishedReceiver finishedReceiver;

    // Widgets
    TextView bt_profile;
    ImageButton bt_control;
    ImageButton bt_last;
    ImageButton bt_next;
    ImageButton bt_mode_changer;
    ImageButton bt_more;
    ImageButton bt_relocate;
    SeekBar sb_progress;
    RecyclerView list_musics;
    TextView tv_timer;
    TextView tv_song_info;

    /*
     * Override functions
     * onCreate(), onStart(), onDestroy()...
     * */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prepare
        RequestPermissions();

        // Bind parts
        BindWidgets();

        // Create instances
        CreateInstances();
        LoadFavoriteMusic();
        LoadMusicData();

        // Set functions
        SetWidgets();

        bindService(
                new Intent(this, MusicService.class),
                musicConnection = new MusicServiceConnection(),
                Context.BIND_AUTO_CREATE
        );

        registerReceiver(
                finishedReceiver = new MusicFinishedReceiver(),
                new IntentFilter() {{
                    addAction(MUSIC_SERVICE_MUSIC_FINISHED);
                }}
        );

        Handler catcher = new MusicInfoCatcher(Looper.getMainLooper());
        catcher.sendEmptyMessage(0);
        new Thread(() -> {
            Looper.prepare();

            catcher.sendEmptyMessage(CATCHER_ASKING);

            Looper.loop();
        }).start();

        registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("Timmer", "stop");
                        pauseMusic();
                    }
                },
                new IntentFilter("StopMusic")
        );
        registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("Timmer", "start");
                        startMusic();
                    }
                },
                new IntentFilter("StartMusic")
        );
    }

    @Override
    protected void onDestroy() {
        unbindService(musicConnection);
        unregisterReceiver(finishedReceiver);

        super.onDestroy();
    }

    /*
     * Preparations
     * */

    void RequestPermissions() {
        // request for READ_EXTERNAL_STORAGE permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else
            Log.d("requestPermissions", "already have read permission");
    }

    void BindWidgets() {
        // Buttons
        bt_profile = findViewById(R.id.bt_profile);
        bt_control = findViewById(R.id.bt_control);
        bt_last = findViewById(R.id.bt_last);
        bt_next = findViewById(R.id.bt_next);
        bt_mode_changer = findViewById(R.id.bt_mode_changer);
        bt_more = findViewById(R.id.bt_more);
        bt_relocate = findViewById(R.id.bt_relocate);

        //SeekBars
        sb_progress = findViewById(R.id.sb_progress);

        // ListViews
        list_musics = findViewById(R.id.list_musics);

        // TextViews
        tv_timer = findViewById(R.id.tv_time_line);
        tv_song_info = findViewById(R.id.tv_song_info);
    }

    void SetWidgets() {
        // widgets settings
        bt_profile.setOnClickListener(view -> {
            if (!loggedIn)
                loginLauncher.launch(new Intent(this, LoginActivity.class));
            else
                startActivity(new Intent(this, ProfileActivity.class));
        });

        bt_control.setOnClickListener(view -> {
            if (musicBinder.isPlaying())
                pauseMusic();
            else
                startMusic();
        });

        bt_next.setOnClickListener(view -> moveNext());

        bt_last.setOnClickListener(view -> moveLast());

        bt_mode_changer.setOnClickListener(view -> {
            switch (playMode) {
                case order:
                    playMode = ModeEnum.random;
                    Toast.makeText(this, "切换到随机播放", Toast.LENGTH_SHORT).show();
                    break;
                case random:
                    playMode = ModeEnum.single;
                    Toast.makeText(this, "切换到单曲播放", Toast.LENGTH_SHORT).show();
                    break;
                case single:
                    playMode = ModeEnum.order;
                    Toast.makeText(this, "切换到顺序播放", Toast.LENGTH_SHORT).show();
            }
        });

        bt_more.setOnClickListener(view -> moreDialog.show());

        bt_relocate.setOnClickListener(view -> list_musics.smoothScrollToPosition(playPos));

        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (onSeekBarTouch) {
                    curTime = totalTime * seekBar.getProgress() / seekBar.getMax();
                    tv_timer.setText(String.format(
                            getString(R.string.mainA_TimeFmt),
                            timeFormat(curTime),
                            timeFormat(totalTime)
                    ));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onSeekBarTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicBinder.seekTo(seekBar.getProgress() * musicBinder.getDur() / seekBar.getMax());
                startMusic();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onSeekBarTouch = false;
            }
        });

        list_musics.setAdapter(musicAdapter = new MusicAdapter(
                favoriteList,
                // OnItemClickListener
                position -> {
                    bt_control.setImageResource(R.mipmap.icon_play);
                    playPos = position;
                    reloadMusic();
                    startMusic();
                }
        ));

        list_musics.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false)
        );
    }

    void CreateInstances() {
        // ActivityLauncher
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            switch (result.getResultCode()) {
                case Activity.RESULT_OK:
                    Intent userInfo = result.getData();
                    assert userInfo != null;
                    String username = userInfo.getStringExtra(LOGIN_EXTRA_USERNAME);
                    String password = userInfo.getStringExtra(LOGIN_EXTRA_PASSWORD);
                    Log.d("result", "user:" + username);
                    Log.d("result", "pass:" + password);
                    bt_profile.setText(username);
                    loggedIn = true;
                    Toast.makeText(this, "欢迎，" + username, Toast.LENGTH_SHORT).show();
                    break;

                default:
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "登录取消", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        addMusicLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            String TAG = "Add Music Result";
            switch (result.getResultCode()) {
                case Activity.RESULT_OK:
                    Log.d(TAG, "RESULT_OK");
                    Intent musicResult = result.getData();
                    assert musicResult != null;
                    ArrayList<MusicInfo> list = musicResult.getParcelableArrayListExtra(ADD_MUSIC_EXTRA_FAVORITE);
                    int itemRangeStart = favoriteList.size();
                    int itemRangeEnd = itemRangeStart + list.size();
                    favoriteList.addAll(list);
                    allMusicList.removeAll(list);
                    if (itemRangeStart == 0)
                        setDefaultMusic(); // set default music when list is empty before addition.
                    musicAdapter.notifyItemRangeInserted(itemRangeStart, itemRangeEnd);
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                    break;

                default:
                case Activity.RESULT_CANCELED:
                    Log.d(TAG, "RESULT_CANCELED");
                    Toast.makeText(this, "添加取消", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        moreDialog = new AlertDialog.Builder(this)
                .setTitle("更多")
                .setIcon(R.mipmap.temp)
                .setItems(new String[]{"添加音乐", "设置定时"}, (dialog, pos) -> {
                    if (pos == 0) {
                        addMusicLauncher.launch(
                                new Intent(this, AddMusicActivity.class) {{
                                    putExtra(ADD_MUSIC_EXTRA_ALL, allMusicList);
                                    putExtra(ADD_MUSIC_EXTRA_FAVORITE, favoriteList);
                                }}
                        );
                    } else if (pos == 1) {
                        //TODO: add code to enable timer
                        startActivity(new Intent(this, TimerActivity.class));
                    }
                }).create();
    }

    void LoadFavoriteMusic() {
        if (favoriteList == null) favoriteList = new ArrayList<>();
        // TODO: add code to load favorite musics to List
    }

    void LoadMusicData() {
        if (allMusicList == null) allMusicList = new ArrayList<>();
        Log.d("MusicLoad", "Load start");
        Cursor cursor = getContentResolver()
                .query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Audio.Media.TITLE
                );
        int pos = 0;
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String dataUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            if (duration == 0) continue;

            MusicInfo info = new MusicInfo(++pos, title, artist, album, dataUri, duration);
            allMusicList.add(info);
            Log.d("MusicLoad", "Music info: " + info);

        }
        cursor.close();
        allMusicList.removeAll(favoriteList);
        Log.d("MusicLoad", "Load finished");
    }

    /*
     * Functions
     * */

    void startMusic() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            Log.d("startMusic", "Music empty");
            return;
        }
        if (musicBinder == null) {
            Log.d("startMusic", "Service not started");
            return;
        }

        bt_control.setImageResource(R.mipmap.icon_pause);
        tv_song_info.setText(String.format(
                Locale.getDefault(),
                getString(R.string.mainA_playingInfoFmt),
                favoriteList.get(playPos).id + ": " + favoriteList.get(playPos).title));
        if (!musicBinder.isPrepared()) {
            musicBinder.setPlaying();
        } else {
            musicBinder.start();
        }
    }

    void pauseMusic() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            Log.d("startMusic", "Music empty");
            return;
        }
        if (musicBinder == null) {
            Log.d("startMusic", "Service not started");
            return;
        }
        musicBinder.pause();
        bt_control.setImageResource(R.mipmap.icon_play);
    }

    void reloadMusic() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            Log.d("startMusic", "Music empty");
            return;
        }
        if (musicBinder == null) {
            Log.d("startMusic", "Service not started");
            return;
        }
        musicBinder.load(favoriteList.get(playPos).dataUri);
    }

    void moveNext() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            Log.d("startMusic", "Music empty");
            return;
        }
        switch (playMode) {
            case order:
                playPos = (playPos + 1) % favoriteList.size();
                break;
            case random:
                playPos = new Random().nextInt(favoriteList.size());
                break;
            case single:
            default:
                break;
        }
        reloadMusic();
        startMusic();
    }

    void moveLast() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            Log.d("startMusic", "Music empty");
            return;
        }
        switch (playMode) {
            case order:
                playPos = (playPos + favoriteList.size() - 1) % favoriteList.size();
                break;
            case random:
                playPos = new Random().nextInt(favoriteList.size());
                break;
            case single:
            default:
                break;
        }
        reloadMusic();
        startMusic();
    }

    void setDefaultMusic() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            Log.d("startMusic", "Music empty");
            return;
        }
        musicBinder.load(favoriteList.get(playPos).dataUri);
    }

    String timeFormat(int time) {
        int ss = time % 60;
        int mm = time / 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mm, ss);
    }

    /*
     * Classes
     * */

    class MusicServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicBinder = (MusicService.Binder) iBinder;
            setDefaultMusic();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    class MusicFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            moveNext();
        }
    }

    class MusicInfoCatcher extends Handler {

        public MusicInfoCatcher(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CATCHER_ASKING:
                    msg = new Message();
                    msg.what = CATCHER_RESPONSE;
                    if (musicBinder != null) {
                        msg.arg1 = musicBinder.getPos() / 1000;
                        msg.arg2 = musicBinder.getDur() / 1000;
                    } else
                        msg.obj = 0;
                    sendMessage(msg);
                    break;
                case CATCHER_RESPONSE:
                    if (!onSeekBarTouch) {
                        curTime = msg.arg1;
                        totalTime = msg.arg2;
                        tv_timer.setText(String.format(
                                getString(R.string.mainA_TimeFmt),
                                timeFormat(curTime),
                                timeFormat(totalTime)
                        ));
                        sb_progress.setProgress(curTime * 100 / (totalTime + 1));
                    }
                    sendEmptyMessageDelayed(CATCHER_ASKING, 300);
                    break;
            }
        }
    }
}
























