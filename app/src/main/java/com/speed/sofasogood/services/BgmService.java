package com.speed.sofasogood.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;

public class BgmService extends Service {

    private MediaPlayer mediaPlayer;
    private boolean pausedByLevel = false;
    private final LifecycleEventObserver lifecycleObserver = (source, event) -> {
        if (mediaPlayer == null || pausedByLevel) return;
        if (event == Lifecycle.Event.ON_STOP) {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        } else if (event == Lifecycle.Event.ON_START) {
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.background_soundtrack_loop);
        mediaPlayer.setLooping(true);
        
        // Load saved media volume and apply it immediately
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float mediaVolume = prefs.getFloat("media_volume", 1.0f);
        mediaPlayer.setVolume(mediaVolume, mediaVolume);
        
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) return START_STICKY;
        
        if (intent != null && "PAUSE".equals(intent.getAction())) {
            pausedByLevel = true;
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        } else if (intent != null && "RESUME".equals(intent.getAction())) {
            pausedByLevel = false;
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        } else if (intent != null && "SET_VOLUME".equals(intent.getAction())) {
            float volume = intent.getFloatExtra("volume", 1.0f);
            mediaPlayer.setVolume(volume, volume);
        } else {
            pausedByLevel = false;
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(lifecycleObserver);
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
