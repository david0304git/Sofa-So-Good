package com.speed.sofasogood.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.UserInfoHelper;
import com.speed.sofasogood.utils.LocaleHelper;

public class ExtraContextsActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private float soundVolume = 1.0f;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_contexts);
        ImmersiveHelper.enable(getWindow());
        new UserInfoHelper().setup(this);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);

        // Title image based on language
        ImageView extraTitle = findViewById(R.id.extraTitle);
        String lang = LocaleHelper.getLanguage(this);
        switch (lang) {
            case "zh-TW": extraTitle.setImageResource(R.drawable.ui_title_extra_cn); break;
            case "ja":    extraTitle.setImageResource(R.drawable.ui_title_extra_jp); break;
            default:      extraTitle.setImageResource(R.drawable.ui_title_extra_eng); break;
        }

        View cardCamera = findViewById(R.id.frameCameraMode);
        setupButtonAnimation(cardCamera);
        cardCamera.setOnClickListener(v ->
                startActivity(new Intent(this, CameraModeActivity.class)));

        View cardBathroom = findViewById(R.id.frameBathroomMode);
        setupButtonAnimation(cardBathroom);
        cardBathroom.setOnClickListener(v ->
                startActivity(new Intent(this, BathroomSelectActivity.class)));

        View cardCat = findViewById(R.id.frameCatMode);
        setupButtonAnimation(cardCat);
        cardCat.setOnClickListener(v ->
                startActivity(new Intent(this, CatSelectActivity.class)));

        // Back button image based on language
        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        switch (lang) {
            case "zh-TW": btnBack.setImageResource(R.drawable.ic_btn_extra_cn); break;
            case "ja":    btnBack.setImageResource(R.drawable.ic_btn_extra_jp); break;
            default:      btnBack.setImageResource(R.drawable.ic_btn_extra_eng); break;
        }
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setHapticFeedbackEnabled(false);
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
        if (soundPool != null) { soundPool.release(); soundPool = null; }
        super.onDestroy();
    }
}
