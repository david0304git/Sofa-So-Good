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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.speed.sofasogood.R;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.UserInfoHelper;
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

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;

    private float soundVolume = 1.0f;
    private float radiusPx;
    private float strokePx;
    private int tileMarginPx;

    private int currentPageIndex = 0;
    private int pageCount;

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
        new UserInfoHelper().setup(this);

        initSound();
        initDimensions();
        initViews();
        buildLevelPages();
        bindStaticButtons();
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
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
    }

    private void bindStaticButtons() {
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

        // Set back button image based on language
        int backImg;
        if ("zh-TW".equals(lang)) {
            backImg = R.drawable.ui_btnback_levelselect_cn;
        } else if ("ja".equals(lang)) {
            backImg = R.drawable.ui_btnback_levelselect_jp;
        } else {
            backImg = R.drawable.ui_btnback_levelselect_eng;
        }
        ((android.widget.ImageButton) btnBack).setImageResource(backImg);

        View btnExtraContexts = findViewById(R.id.btnExtraContexts);
        setupButtonAnimation(btnExtraContexts);
        btnExtraContexts.setOnClickListener(v ->
                startActivity(new Intent(this, ExtraContextsActivity.class)));
    }

    private void buildLevelPages() {
        pageCount = (int) Math.ceil(levels.length / (float) LEVELS_PER_PAGE);
        viewPager.setAdapter(new LevelPageAdapter());
        viewPager.setOffscreenPageLimit(pageCount);
        setupDots();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void setupDots() {
        dotsLayout.removeAllViews();
        int sizePx = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
        int marginPx = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()));
        for (int i = 0; i < pageCount; i++) {
            View dot = new View(this);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(params);
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(i == 0 ? 0xFFFFD700 : 0x88FFFFFF);
            dot.setBackground(shape);
            dotsLayout.addView(dot);
        }
    }

    private void updateDots(int selected) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            android.graphics.drawable.GradientDrawable shape = (android.graphics.drawable.GradientDrawable) dotsLayout.getChildAt(i).getBackground();
            shape.setColor(i == selected ? 0xFFFFD700 : 0x88FFFFFF);
        }
    }

    private View createLevelPage(int startInclusive, int endExclusive) {
        GridLayout grid = new GridLayout(this);
        grid.setLayoutParams(new ViewGroup.LayoutParams(
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

    private class LevelPageAdapter extends RecyclerView.Adapter<LevelPageAdapter.PageHolder> {
        @NonNull
        @Override
        public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PageHolder(new android.widget.FrameLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull PageHolder holder, int position) {
            android.widget.FrameLayout container = (android.widget.FrameLayout) holder.itemView;
            container.removeAllViews();
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            int start = position * LEVELS_PER_PAGE;
            int end = Math.min(start + LEVELS_PER_PAGE, levels.length);
            container.addView(createLevelPage(start, end));
        }

        @Override
        public int getItemCount() { return pageCount; }

        class PageHolder extends RecyclerView.ViewHolder {
            PageHolder(@NonNull View itemView) { super(itemView); }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setHapticFeedbackEnabled(false);
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