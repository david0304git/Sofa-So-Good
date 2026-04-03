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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;
import com.speed.sofasogood.views.OutlinedTextButton;

import java.util.HashMap;
import java.util.Map;

public class LevelSelectActivity extends AppCompatActivity {

    private static final int LEVELS_PER_PAGE = 4;
    private static final int GRID_COLUMNS = 2;

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;

    private ViewFlipper viewFlipper;
    private View btnPrev, btnNext;

    private float soundVolume = 1.0f;
    private float radiusPx;
    private float strokePx;
    private int tileMarginPx;

    private int currentPageIndex = 0;

    private final Map<String, Drawable.ConstantState> backgroundCache = new HashMap<>();

    private final LevelInfo[] levels = new LevelInfo[] {
            new LevelInfo(1, R.drawable.level1_background, com.speed.sofasogood.game.levels.Level1Activity.class),
            new LevelInfo(2, R.drawable.level2_background, com.speed.sofasogood.game.levels.Level2Activity.class),
            new LevelInfo(3, R.drawable.level3_background, com.speed.sofasogood.game.levels.Level3Activity.class),
            new LevelInfo(4, R.drawable.level4_background, com.speed.sofasogood.game.levels.Level4Activity.class),
            new LevelInfo(5, R.drawable.level5_background, com.speed.sofasogood.game.levels.Level5Activity.class),
            new LevelInfo(6, R.drawable.level6_background, com.speed.sofasogood.game.levels.Level6Activity.class),
            new LevelInfo(7, R.drawable.level7_background, com.speed.sofasogood.game.levels.Level7Activity.class),
            new LevelInfo(8, R.drawable.level8_background, com.speed.sofasogood.game.levels.Level8Activity.class)
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);
        ImmersiveHelper.enable(getWindow());

        initSound();
        initDimensions();
        initViews();
        buildLevelPages();
        bindStaticButtons();

        currentPageIndex = 0;
        viewFlipper.setDisplayedChild(currentPageIndex);
        syncArrowState();
    }

    private void initSound() {
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
    }

    private void initDimensions() {
        radiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        strokePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()
        );
        tileMarginPx = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
        ));
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        btnPrev = findViewById(R.id.btnPrevPage);
        btnNext = findViewById(R.id.btnNextPage);
    }

    private void bindStaticButtons() {
        setupButtonAnimation(btnPrev);
        setupButtonAnimation(btnNext);

        btnNext.setOnClickListener(v -> {
            int lastPageIndex = Math.max(0, viewFlipper.getChildCount() - 1);
            if (currentPageIndex < lastPageIndex) {
                currentPageIndex++;
                viewFlipper.setDisplayedChild(currentPageIndex);
                syncArrowState();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPageIndex > 0) {
                currentPageIndex--;
                viewFlipper.setDisplayedChild(currentPageIndex);
                syncArrowState();
            }
        });

        View btnLeaderboard = findViewById(R.id.btnLeaderboard);
        setupButtonAnimation(btnLeaderboard);
        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));

        // Set leaderboard button image based on language
        String lang = LocaleHelper.getLanguage(this);
        int leaderboardImg;
        if ("zh-TW".equals(lang)) {
            leaderboardImg = R.drawable.ui_btnleaderboard_levelselect_cn;
        } else if ("ja".equals(lang)) {
            leaderboardImg = R.drawable.ui_btnleaderboard_levelselect_jp;
        } else {
            leaderboardImg = R.drawable.ui_btnleaderboard_levelselect_eng;
        }
        ((android.widget.ImageButton) btnLeaderboard).setImageResource(leaderboardImg);

        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());

        View btnExtraContexts = findViewById(R.id.btnExtraContexts);
        setupButtonAnimation(btnExtraContexts);
        btnExtraContexts.setOnClickListener(v ->
                startActivity(new Intent(this, ExtraContextsActivity.class)));
    }

    private void buildLevelPages() {
        viewFlipper.removeAllViews();

        int pageCount = (int) Math.ceil(levels.length / (float) LEVELS_PER_PAGE);
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int start = pageIndex * LEVELS_PER_PAGE;
            int endExclusive = Math.min(start + LEVELS_PER_PAGE, levels.length);
            viewFlipper.addView(createLevelPage(start, endExclusive));
        }

        currentPageIndex = 0;
    }

    private View createLevelPage(int startInclusive, int endExclusive) {
        GridLayout grid = new GridLayout(this);
        grid.setLayoutParams(new ViewFlipper.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        grid.setColumnCount(GRID_COLUMNS);
        grid.setRowCount(2);
        grid.setUseDefaultMargins(false);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setPadding(tileMarginPx, tileMarginPx, tileMarginPx, tileMarginPx);

        for (int levelIndex = startInclusive; levelIndex < endExclusive; levelIndex++) {
            int slotIndex = levelIndex - startInclusive;
            grid.addView(createLevelButton(levels[levelIndex], slotIndex));
        }

        fillRemainingSlots(grid, endExclusive - startInclusive);
        sizeGridButtonsToRatio(grid);
        return grid;
    }

    private View createLevelButton(LevelInfo levelInfo, int slotIndex) {
        OutlinedTextButton button = new OutlinedTextButton(this, null);
        button.setId(View.generateViewId());
        button.setText(String.valueOf(levelInfo.number));
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        button.setTypeface(button.getTypeface(), android.graphics.Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(slotIndex / GRID_COLUMNS, 1f),
                GridLayout.spec(slotIndex % GRID_COLUMNS, 1f)
        );
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.setMargins(tileMarginPx, tileMarginPx, tileMarginPx, tileMarginPx);
        params.setGravity(Gravity.FILL_HORIZONTAL);
        button.setLayoutParams(params);

        setupButtonAnimation(button);
        button.setOnClickListener(v ->
                startActivity(new Intent(this, levelInfo.activityClass)));
        setLevelBackground(button, levelInfo.backgroundRes, radiusPx, strokePx);

        return button;
    }

    private void fillRemainingSlots(GridLayout grid, int usedSlots) {
        for (int slotIndex = usedSlots; slotIndex < LEVELS_PER_PAGE; slotIndex++) {
            View spacer = new View(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(slotIndex / GRID_COLUMNS, 1f),
                    GridLayout.spec(slotIndex % GRID_COLUMNS, 1f)
            );
            params.width = 0;
            params.height = 0;
            params.setMargins(tileMarginPx, tileMarginPx, tileMarginPx, tileMarginPx);

            spacer.setLayoutParams(params);
            spacer.setVisibility(View.INVISIBLE);
            grid.addView(spacer);
        }
    }

    private void sizeGridButtonsToRatio(GridLayout grid) {
        grid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (right - left <= 0 || bottom - top <= 0) {
                    return;
                }

                grid.removeOnLayoutChangeListener(this);

                int firstMeasuredWidth = 0;
                for (int i = 0; i < grid.getChildCount(); i++) {
                    View child = grid.getChildAt(i);
                    if (child instanceof OutlinedTextButton && child.getWidth() > 0) {
                        firstMeasuredWidth = child.getWidth();
                        break;
                    }
                }

                if (firstMeasuredWidth <= 0) {
                    return;
                }

                int targetHeight = Math.round(firstMeasuredWidth * 2f / 3f);

                for (int i = 0; i < grid.getChildCount(); i++) {
                    View child = grid.getChildAt(i);
                    ViewGroup.LayoutParams params = child.getLayoutParams();
                    if (params == null) {
                        continue;
                    }
                    params.height = child instanceof OutlinedTextButton ? targetHeight : 0;
                    child.setLayoutParams(params);
                }
            }
        });
    }

    //TODO: switching visibility of arrows not working
    private void syncArrowState() {
        int totalPages = viewFlipper.getChildCount();

        boolean hasPrev = currentPageIndex > 0;
        boolean hasNext = currentPageIndex < totalPages - 1;

        btnPrev.setVisibility(hasPrev ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);

        btnPrev.setEnabled(hasPrev);
        btnNext.setEnabled(hasNext);

        btnPrev.setClickable(hasPrev);
        btnNext.setClickable(hasNext);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (v.isEnabled()) {
                        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                        playClickSound();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_release));
                    break;

                default:
                    break;
            }
            return false;
        });
    }

    private void playClickSound() {
        if (soundReady && soundPool != null) {
            soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
        }
    }

    private void setLevelBackground(View btn, int bgResId, float radius, float stroke) {
        btn.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r, int b,
                                       int ol, int ot, int orr, int ob) {
                int w = v.getWidth();
                int h = v.getHeight();
                if (w == 0 || h == 0) {
                    return;
                }
                v.removeOnLayoutChangeListener(this);
                applyLevelBackground(v, bgResId, radius, stroke, w, h);
            }
        });

        if (btn.getWidth() > 0 && btn.getHeight() > 0) {
            applyLevelBackground(btn, bgResId, radius, stroke, btn.getWidth(), btn.getHeight());
        }
    }

    private void applyLevelBackground(View btn, int bgResId, float radius, float stroke, int w, int h) {
        String cacheKey = bgResId + ":" + w + "x" + h;
        Drawable.ConstantState cachedState = backgroundCache.get(cacheKey);
        if (cachedState != null) {
            btn.setBackground(cachedState.newDrawable(getResources()).mutate());
            return;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), bgResId, opts);

        int widthSample = Math.max(1, opts.outWidth / Math.max(1, w));
        int heightSample = Math.max(1, opts.outHeight / Math.max(1, h));
        int sampleSize = Math.max(1, Math.min(widthSample, heightSample));

        opts.inJustDecodeBounds = false;
        opts.inSampleSize = sampleSize;

        Bitmap src = BitmapFactory.decodeResource(getResources(), bgResId, opts);
        if (src == null) {
            return;
        }

        Bitmap rounded = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(rounded);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        float scale = Math.max((float) w / src.getWidth(), (float) h / src.getHeight());
        int scaledWidth = Math.max(1, Math.round(src.getWidth() * scale));
        int scaledHeight = Math.max(1, Math.round(src.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(src, scaledWidth, scaledHeight, true);

        int dx = (scaled.getWidth() - w) / 2;
        int dy = (scaled.getHeight() - h) / 2;

        BitmapShader shader = new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.setTranslate(-dx, -dy);
        shader.setLocalMatrix(matrix);

        paint.setShader(shader);
        canvas.drawRoundRect(new RectF(0, 0, w, h), radius, radius, paint);

        Bitmap frameSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ui_btnframe_levelselect);
        Bitmap frameScaled = Bitmap.createScaledBitmap(frameSrc, w, h, true);
        if (frameSrc != frameScaled && !frameSrc.isRecycled()) frameSrc.recycle();

        LayerDrawable result = new LayerDrawable(new Drawable[]{
                new BitmapDrawable(getResources(), rounded),
                new BitmapDrawable(getResources(), frameScaled)
        });

        btn.setBackground(result);

        Drawable.ConstantState state = result.getConstantState();
        if (state != null) {
            backgroundCache.put(cacheKey, state);
        }

        if (scaled != src && !scaled.isRecycled()) {
            scaled.recycle();
        }
        if (!src.isRecycled()) {
            src.recycle();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundCache.clear();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private static final class LevelInfo {
        final int number;
        final int backgroundRes;
        final Class<?> activityClass;

        LevelInfo(int number, int backgroundRes, Class<?> activityClass) {
            this.number = number;
            this.backgroundRes = backgroundRes;
            this.activityClass = activityClass;
        }
    }
}