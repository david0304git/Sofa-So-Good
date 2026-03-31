package com.speed.sofasogood.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;
import com.speed.sofasogood.R;

public class LevelSelectActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private ViewFlipper viewFlipper;
    private View btnPrev, btnNext;
    private float soundVolume = 1.0f;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);
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
        soundVolume = prefs.getFloat("sound_volume", 1.0f);

        viewFlipper = findViewById(R.id.viewFlipper);
        btnPrev = findViewById(R.id.btnPrevPage);
        btnNext = findViewById(R.id.btnNextPage);

        // 翻頁
        btnNext.setOnClickListener(v -> {
            if (soundReady) soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
            viewFlipper.showNext();
            updateArrows();
        });
        btnPrev.setOnClickListener(v -> {
            if (soundReady) soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
            viewFlipper.showPrevious();
            updateArrows();
        });

        // 關卡按鈕動畫 + 背景
        int[] levelBtnIds = {
                R.id.btnLevel1, R.id.btnLevel2, R.id.btnLevel3, R.id.btnLevel4,
                R.id.btnLevel5, R.id.btnLevel6, R.id.btnLevel7, R.id.btnLevel8
        };
        int[] levelBgRes = {
                R.drawable.level1_background, R.drawable.level2_background,
                R.drawable.level3_background, R.drawable.level4_background,
                R.drawable.level5_background, R.drawable.level6_background,
                R.drawable.level7_background, R.drawable.level8_background
        };
        float radiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        float strokePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        for (int i = 0; i < levelBtnIds.length; i++) {
            View btn = findViewById(levelBtnIds[i]);
            setupButtonAnimation(btn);
            setLevelBackground(btn, levelBgRes[i], radiusPx, strokePx);
        }

        // 返回
        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 第一關
        findViewById(R.id.btnLevel1).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level1Activity.class)));
        findViewById(R.id.btnLevel2).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level2Activity.class)));
        findViewById(R.id.btnLevel3).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level3Activity.class)));
        findViewById(R.id.btnLevel4).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level4Activity.class)));
        findViewById(R.id.btnLevel5).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level5Activity.class)));
        findViewById(R.id.btnLevel6).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level6Activity.class)));
        findViewById(R.id.btnLevel7).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level7Activity.class)));
        findViewById(R.id.btnLevel8).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level8Activity.class)));
    }

    private void updateArrows() {
        int current = viewFlipper.getDisplayedChild();
        int total = viewFlipper.getChildCount();
        btnPrev.setVisibility(current == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(current == total - 1 ? View.INVISIBLE : View.VISIBLE);
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

    private void setLevelBackground(View btn, int bgResId, float radius, float stroke) {
        btn.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r2, int b, int ol, int ot, int or2, int ob) {
                int w = v.getWidth();
                int h = v.getHeight();
                if (w == 0 || h == 0) return;
                v.removeOnLayoutChangeListener(this);
                applyLevelBackground(v, bgResId, radius, stroke, w, h);
            }
        });
        // Also try immediately if already laid out
        if (btn.getWidth() > 0 && btn.getHeight() > 0) {
            applyLevelBackground(btn, bgResId, radius, stroke, btn.getWidth(), btn.getHeight());
        }
    }

    private void applyLevelBackground(View btn, int bgResId, float radius, float stroke, int w, int h) {

            // Decode & center-crop
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), bgResId, opts);
            int sampleSize = Math.min(opts.outWidth / w, opts.outHeight / h);
            if (sampleSize < 1) sampleSize = 1;
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = sampleSize;
            Bitmap src = BitmapFactory.decodeResource(getResources(), bgResId, opts);

            // Draw rounded bitmap
            Bitmap rounded = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(rounded);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            float scale = Math.max((float) w / src.getWidth(), (float) h / src.getHeight());
            Bitmap scaled = Bitmap.createScaledBitmap(src, (int)(src.getWidth() * scale), (int)(src.getHeight() * scale), true);
            int dx = (scaled.getWidth() - w) / 2;
            int dy = (scaled.getHeight() - h) / 2;
            BitmapShader shader = new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            android.graphics.Matrix m = new android.graphics.Matrix();
            m.setTranslate(-dx, -dy);
            shader.setLocalMatrix(m);
            p.setShader(shader);
            c.drawRoundRect(new RectF(0, 0, w, h), radius, radius, p);
            src.recycle();
            scaled.recycle();

            // Border
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.RECTANGLE);
            border.setCornerRadius(radius);
            border.setColor(0x00000000);
            border.setStroke((int) stroke, 0xFFFFD700);

            btn.setBackground(new LayerDrawable(new Drawable[]{
                    new BitmapDrawable(getResources(), rounded), border
            }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
