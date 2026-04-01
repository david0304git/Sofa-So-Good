package com.speed.sofasogood.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;
import com.speed.sofasogood.views.OutlinedTextButton;

public class CatSelectActivity extends AppCompatActivity {

    private static final int TOTAL_LEVELS = 4;
    private static final int GRID_COLUMNS = 2;

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
        setContentView(R.layout.activity_cat_select);
        ImmersiveHelper.enable(getWindow());

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = status == 0);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);

        float radiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        float strokePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        int marginPx = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));

        GridLayout grid = findViewById(R.id.levelGrid);
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            final int levelNum = i + 1;
            OutlinedTextButton btn = new OutlinedTextButton(this, null);
            btn.setText(String.valueOf(levelNum));
            btn.setTextColor(0xFFFFFFFF);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            btn.setTypeface(btn.getTypeface(), Typeface.BOLD);
            btn.setGravity(Gravity.CENTER);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / GRID_COLUMNS, 1f),
                    GridLayout.spec(i % GRID_COLUMNS, 1f)
            );
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            btn.setLayoutParams(params);

            setupButtonAnimation(btn);
            btn.setOnClickListener(v ->
                    Toast.makeText(this, "Cat Level " + levelNum, Toast.LENGTH_SHORT).show());
            setLevelBackground(btn, R.drawable.level8_background, radiusPx, strokePx);
            grid.addView(btn);
        }

        grid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or2, int ob) {
                if (r - l <= 0 || b - t <= 0) return;
                grid.removeOnLayoutChangeListener(this);
                int firstW = 0;
                for (int i = 0; i < grid.getChildCount(); i++) {
                    View child = grid.getChildAt(i);
                    if (child.getWidth() > 0) { firstW = child.getWidth(); break; }
                }
                if (firstW <= 0) return;
                int targetH = Math.round(firstW * 2f / 3f);
                for (int i = 0; i < grid.getChildCount(); i++) {
                    ViewGroup.LayoutParams p = grid.getChildAt(i).getLayoutParams();
                    p.height = targetH;
                    grid.getChildAt(i).setLayoutParams(p);
                }
            }
        });

        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setLevelBackground(View btn, int bgResId, float radius, float stroke) {
        btn.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or2, int ob) {
                int w = v.getWidth(), h = v.getHeight();
                if (w == 0 || h == 0) return;
                v.removeOnLayoutChangeListener(this);
                applyBackground(v, bgResId, radius, stroke, w, h);
            }
        });
    }

    private void applyBackground(View btn, int bgResId, float radius, float stroke, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), bgResId, opts);
        opts.inSampleSize = Math.max(1, Math.min(opts.outWidth / Math.max(1, w), opts.outHeight / Math.max(1, h)));
        opts.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(getResources(), bgResId, opts);
        if (src == null) return;

        Bitmap rounded = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(rounded);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float scale = Math.max((float) w / src.getWidth(), (float) h / src.getHeight());
        Bitmap scaled = Bitmap.createScaledBitmap(src, Math.max(1, Math.round(src.getWidth() * scale)),
                Math.max(1, Math.round(src.getHeight() * scale)), true);
        BitmapShader shader = new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setTranslate(-(scaled.getWidth() - w) / 2f, -(scaled.getHeight() - h) / 2f);
        shader.setLocalMatrix(m);
        p.setShader(shader);
        c.drawRoundRect(new RectF(0, 0, w, h), radius, radius, p);

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(radius);
        border.setColor(0x00000000);
        border.setStroke((int) stroke, 0xFFFFD700);

        btn.setBackground(new LayerDrawable(new Drawable[]{
                new BitmapDrawable(getResources(), rounded), border
        }));
        if (scaled != src && !scaled.isRecycled()) scaled.recycle();
        if (!src.isRecycled()) src.recycle();
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
