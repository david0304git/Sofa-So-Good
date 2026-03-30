package com.speed.sofasogood;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        // Volume
        SeekBar seekBarVolume = findViewById(R.id.seekBarVolume);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        seekBarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
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
                    if (soundReady) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
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
