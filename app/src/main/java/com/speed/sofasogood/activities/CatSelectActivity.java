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
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.UserInfoHelper;
import com.speed.sofasogood.utils.LocaleHelper;
import com.speed.sofasogood.views.OutlinedTextButton;

import java.util.HashSet;
import java.util.Set;

public class CatSelectActivity extends AppCompatActivity {

    private static final int TOTAL_LEVELS = 4;
    private static final int GRID_COLUMNS = 2;
        private final LevelInfo[] levels = new LevelInfo[] {
            new LevelInfo(1, R.drawable.level8_background, com.speed.sofasogood.game.levels.extra.CatModeExtraLevel1Activity.class),
            new LevelInfo(2, R.drawable.level8_background, com.speed.sofasogood.game.levels.extra.CatModeExtraLevel2Activity.class),
            new LevelInfo(3, R.drawable.level8_background, com.speed.sofasogood.game.levels.extra.CatModeExtraLevel3Activity.class),
            new LevelInfo(4, R.drawable.level8_background, com.speed.sofasogood.game.levels.extra.CatModeExtraLevel4Activity.class),
        };

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
        new UserInfoHelper().setup(this);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = status == 0);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);

        float radiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        float strokePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        int marginPx = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));

        Set<String> completedLevels = new HashSet<>(getSharedPreferences("game_progress", MODE_PRIVATE)
                .getStringSet("completed_levels", new HashSet<>()));

        GridLayout grid = findViewById(R.id.levelGrid);
        for (int idx = 0; idx < Math.min(TOTAL_LEVELS, levels.length); idx++) {
            final LevelInfo info = levels[idx];
            FrameLayout wrapper = new FrameLayout(this);
            wrapper.setClipChildren(false);
            wrapper.setClipToPadding(false);

            OutlinedTextButton btn = new OutlinedTextButton(this, null);
            btn.setText(String.valueOf(info.number));
            btn.setTextColor(0xFFFFFFFF);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            btn.setTypeface(btn.getTypeface(), Typeface.BOLD);
            btn.setGravity(Gravity.CENTER);
            btn.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            setupButtonAnimation(btn);
            final int idx2 = idx;
            btn.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, info.activityClass);
                if (idx2 + 1 < levels.length) {
                    intent.putExtra("nextLevel", levels[idx2 + 1].activityClass.getName());
                }
                startActivity(intent);
            });
            setLevelBackground(btn, info.backgroundRes, radiusPx, strokePx);
            wrapper.addView(btn);

            if (completedLevels.contains(String.valueOf(info.levelNumber))) {
                ImageView tick = new ImageView(this);
                int tickSize = Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()));
                FrameLayout.LayoutParams tickParams = new FrameLayout.LayoutParams(tickSize, tickSize);
                tickParams.gravity = Gravity.TOP | Gravity.END;
                tick.setLayoutParams(tickParams);
                tick.setImageResource(R.drawable.ic_level_complete);
                tick.setElevation(10f);
                wrapper.addView(tick);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(idx / GRID_COLUMNS, 1f),
                    GridLayout.spec(idx % GRID_COLUMNS, 1f)
            );
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            wrapper.setLayoutParams(params);
            grid.addView(wrapper);
        }

        grid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or2, int ob) {
                if (r - l <= 0 || b - t <= 0) return;
                grid.removeOnLayoutChangeListener(this);
                int firstW = 0;
                for (int i = 0; i < grid.getChildCount(); i++) {
                    View child = grid.getChildAt(i);
                    if (child instanceof FrameLayout && child.getWidth() > 0) { firstW = child.getWidth(); break; }
                }
                if (firstW <= 0) return;
                int targetH = Math.round(firstW * 2f / 3f);
                for (int i = 0; i < grid.getChildCount(); i++) {
                    View child = grid.getChildAt(i);
                    ViewGroup.LayoutParams p = child.getLayoutParams();
                    p.height = child instanceof FrameLayout ? targetH : 0;
                    child.setLayoutParams(p);
                }
            }
        });

        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Set back button image based on language
        String lang = LocaleHelper.getLanguage(this);
        int backImg;
        if ("zh-TW".equals(lang)) backImg = R.drawable.ui_btnback_levelselect_cn;
        else if ("ja".equals(lang)) backImg = R.drawable.ui_btnback_levelselect_jp;
        else backImg = R.drawable.ui_btnback_levelselect_eng;
        ((android.widget.ImageButton) btnBack).setImageResource(backImg);
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

        Bitmap frameSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ui_btnframe_levelselect);
        Bitmap frameScaled = Bitmap.createScaledBitmap(frameSrc, w, h, true);
        if (frameSrc != frameScaled && !frameSrc.isRecycled()) frameSrc.recycle();

        btn.setBackground(new LayerDrawable(new Drawable[]{
                new BitmapDrawable(getResources(), rounded),
                new BitmapDrawable(getResources(), frameScaled)
        }));
        if (scaled != src && !scaled.isRecycled()) scaled.recycle();
        if (!src.isRecycled()) src.recycle();
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
        super.onDestroy();
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }

    private static final class LevelInfo {
        final int number;
        final int backgroundRes;
        final int levelNumber;
        final Class<?> activityClass;

        LevelInfo(int number, int backgroundRes, Class<?> activityClass) {
            this.number = number;
            this.backgroundRes = backgroundRes;
            this.activityClass = activityClass;
            // Cat extra levels use 105-108
            this.levelNumber = 104 + number;
        }
    }
}
