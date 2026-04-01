package com.speed.sofasogood.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.FurnitureStore;
import com.speed.sofasogood.utils.ImmersiveHelper;

public class CustomFurnitureActivity extends AppCompatActivity {

    private String pendingKey;

    private ImageView imgPlant, imgTv, imgSofa;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null && pendingKey != null) {
                        FurnitureStore.get().set(pendingKey, photo);
                        refreshPreviews();
                        Toast.makeText(this, "Furniture updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_furniture);
        ImmersiveHelper.enable(getWindow());

        imgPlant = findViewById(R.id.slotPlant).findViewById(R.id.imgPreview);
        imgTv    = findViewById(R.id.slotTv).findViewById(R.id.imgPreview);
        imgSofa  = findViewById(R.id.slotSofa).findViewById(R.id.imgPreview);

        setupSlot(R.id.slotPlant, "Plant",  FurnitureStore.KEY_PLANT,  R.drawable.asset_plant);
        setupSlot(R.id.slotTv,    "TV",     FurnitureStore.KEY_TV,     R.drawable.asset_tv);
        setupSlot(R.id.slotSofa,  "Sofa",   FurnitureStore.KEY_SOFA_L, R.drawable.asset_sofa_left);

        findViewById(R.id.btnResetAll).setOnClickListener(v -> {
            FurnitureStore.get().resetAll();
            refreshPreviews();
            Toast.makeText(this, "All furniture reset", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> finish());
    }

    @SuppressLint("SetTextI18n")
    private void setupSlot(int slotId, String label, String key, int defaultRes) {
        View slot = findViewById(slotId);

        ((TextView) slot.findViewById(R.id.tvFurnitureName)).setText(label);

        ImageView preview = slot.findViewById(R.id.imgPreview);
        if (FurnitureStore.get().has(key)) {
            preview.setImageBitmap(FurnitureStore.get().get(key));
        } else {
            preview.setImageResource(defaultRes);
        }

        slot.findViewById(R.id.btnCamera).setOnClickListener(v -> {
            pendingKey = key;
            requestCameraAndShoot();
        });

        slot.findViewById(R.id.btnReset).setOnClickListener(v -> {
            FurnitureStore.get().reset(key);
            preview.setImageResource(defaultRes);
            Toast.makeText(this, label + " reset", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshPreviews() {
        updatePreview(R.id.slotPlant, FurnitureStore.KEY_PLANT,  R.drawable.asset_plant);
        updatePreview(R.id.slotTv,    FurnitureStore.KEY_TV,     R.drawable.asset_tv);
        updatePreview(R.id.slotSofa,  FurnitureStore.KEY_SOFA_L, R.drawable.asset_sofa_left);
    }

    private void updatePreview(int slotId, String key, int defaultRes) {
        ImageView preview = findViewById(slotId).findViewById(R.id.imgPreview);
        if (FurnitureStore.get().has(key)) {
            preview.setImageBitmap(FurnitureStore.get().get(key));
        } else {
            preview.setImageResource(defaultRes);
        }
    }

    private void requestCameraAndShoot() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }
}
