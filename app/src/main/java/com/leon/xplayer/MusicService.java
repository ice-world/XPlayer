package com.leon.xplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.leon.xplayer.Lib.config;

import java.io.IOException;

public class MusicService extends Service {
    private final String TAG = "Music Service";

    private MediaPlayer mediaPlayer;

    //status
    private boolean prepared = false;
    private boolean playing = false;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service on bind");
        return new Binder();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service on create");

        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(player -> {
            prepared = true;
            if (playing) player.start();
            Log.d(TAG, "Music prepared");
        });
        mediaPlayer.setOnCompletionListener(player -> {
            sendBroadcast(new Intent(config.MUSIC_SERVICE_MUSIC_FINISHED));
            Log.d(TAG, "Music completed");
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service on destroy");

        if (mediaPlayer != null)
            mediaPlayer.release();
        super.onDestroy();
    }

    /*
     * Binder
     * */

    public class Binder extends android.os.Binder {
        public void load(String path) {
            prepared = false;
            try {
                Log.d(TAG, "Start load music");
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            if (prepared) {
                Log.d(TAG, "Music start");
                mediaPlayer.start();
                playing = true;
            } else Log.d(TAG, "Music not prepared");
        }

        public void pause() {
            if (prepared) {
                Log.d(TAG, "Music pause");
                mediaPlayer.pause();
                playing = false;
            } else Log.d(TAG, "Music not prepared");
        }

        public int getPos() {
            if (prepared) {
                return mediaPlayer.getCurrentPosition();
            } else return 0;
        }

        public int getDur() {
            if (prepared) {
                return mediaPlayer.getDuration();
            } else return 1;
        }

        public void seekTo(int pos) {
            if (prepared) {
                Log.d(TAG, "Music seek to " + pos);
                mediaPlayer.seekTo(pos);
            } else Log.d(TAG, "Music not prepared");
        }

        public void setPlaying() {
            setPlaying(true);
        }

        public void setPlaying(boolean f) {
            playing = f;
        }

        public boolean isPlaying() {
            return playing;
        }

        public boolean isPrepared() {
            return prepared;
        }
    }
}
