package com.speed.sofasogood;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BgmService extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.background_soundtrack_loop);
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "PAUSE".equals(intent.getAction())) {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        } else if (intent != null && "RESUME".equals(intent.getAction())) {
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        } else {
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
