package com.speed.sofasogood.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.AssetSkinManager;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;

public class CameraModeActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;

    private int pendingAssetIndex = -1;
    private ImageView[] previews;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && pendingAssetIndex >= 0) {
                        Bitmap photo = (Bitmap) extras.get("data");
                        if (photo != null) {
                            String key = AssetSkinManager.ASSET_KEYS[pendingAssetIndex];
                            AssetSkinManager.saveCustomBitmap(this, key, photo);
                            // Clear GameView source cache so it reloads
                            refreshPreview(pendingAssetIndex);
                        }
                    }
                }
                pendingAssetIndex = -1;
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_mode);
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

        LinearLayout assetList = findViewById(R.id.assetList);
        previews = new ImageView[AssetSkinManager.ASSET_KEYS.length];

        for (int i = 0; i < AssetSkinManager.ASSET_KEYS.length; i++) {
            final int index = i;
            View row = LayoutInflater.from(this).inflate(R.layout.item_asset_row, assetList, false);

            ImageView preview = row.findViewById(R.id.assetPreview);
            TextView label = row.findViewById(R.id.assetLabel);
            ImageButton btnCamera = row.findViewById(R.id.btnCamera);
            ImageButton btnReset = row.findViewById(R.id.btnReset);

            label.setText(getString(AssetSkinManager.ASSET_LABEL_RES_IDS[i]));
            previews[i] = preview;
            refreshPreview(i);

            btnCamera.setOnClickListener(v -> {
                if (soundReady) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
                pendingAssetIndex = index;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(intent);
            });

            btnReset.setOnClickListener(v -> {
                if (soundReady) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
                AssetSkinManager.resetCustom(this, AssetSkinManager.ASSET_KEYS[index]);
                refreshPreview(index);
            });

            assetList.addView(row);
        }

        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void refreshPreview(int index) {
        Bitmap custom = AssetSkinManager.loadCustomBitmap(this, AssetSkinManager.ASSET_KEYS[index]);
        if (custom != null) {
            previews[index].setImageBitmap(custom);
        } else {
            previews[index].setImageResource(AssetSkinManager.ASSET_RES_IDS[index]);
        }
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
        if (soundPool != null) { soundPool.release(); soundPool = null; }
        super.onDestroy();
    }
}
