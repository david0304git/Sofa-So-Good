package com.speed.sofasogood;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private float soundVolume = 1.0f;  // Default to full volume
    private float mediaVolume = 1.0f;  // Default to full volume

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImmersiveHelper.enable(getWindow());

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Media Volume (controls music volume)
        mediaVolume = prefs.getFloat("media_volume", 1.0f);
        SeekBar seekBarVolume = findViewById(R.id.seekBarVolume);
        seekBarVolume.setMax(100);
        seekBarVolume.setProgress((int) (mediaVolume * 100));
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaVolume = progress / 100.0f;
                    prefs.edit().putFloat("media_volume", mediaVolume).commit();
                    Intent intent = new Intent(SettingsActivity.this, BgmService.class);
                    intent.setAction("SET_VOLUME");
                    intent.putExtra("volume", mediaVolume);
                    startService(intent);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set initial music volume
        Intent volumeIntent = new Intent(this, BgmService.class);
        volumeIntent.setAction("SET_VOLUME");
        volumeIntent.putExtra("volume", mediaVolume);
        startService(volumeIntent);

        // Sound Effects Volume (new SeekBar)
        SeekBar seekBarSoundVolume = findViewById(R.id.seekBarSoundVolume);
        seekBarSoundVolume.setMax(100);  // 0-100 for finer control
        soundVolume = prefs.getFloat("sound_volume", 1.0f);
        seekBarSoundVolume.setProgress((int) (soundVolume * 100));
        seekBarSoundVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    soundVolume = progress / 100.0f;
                    prefs.edit().putFloat("sound_volume", soundVolume).commit();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Language Spinner
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        String[] languages = {"English", "繁體中文", "日本語"};
        String[] langCodes = {"en", "zh-TW", "ja"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, languages);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Set current selection
        String currentLang = LocaleHelper.getLanguage(this);
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean init = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (init) { init = false; return; }
                String selected = langCodes[position];
                if (!selected.equals(LocaleHelper.getLanguage(SettingsActivity.this))) {
                    LocaleHelper.setLanguage(SettingsActivity.this, selected);
                    recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Buttons
        View btnLogout = findViewById(R.id.btnLogout);
        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnLogout);
        setupButtonAnimation(btnBack);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    if (soundReady) soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_release));
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }
}
