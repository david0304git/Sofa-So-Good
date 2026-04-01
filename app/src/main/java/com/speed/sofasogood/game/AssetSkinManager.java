package com.speed.sofasogood.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AssetSkinManager {

    private static final String DIR = "custom_skins";

    public static final String[] ASSET_KEYS = {
            "asset_wall", "asset_floor", "asset_player",
            "asset_plant", "asset_sofa_left", "asset_sofa_right",
            "asset_tv", "asset_tub_left", "asset_tub_right"
    };

    public static final int[] ASSET_LABEL_RES_IDS = {
            com.speed.sofasogood.R.string.label_asset_wall,
            com.speed.sofasogood.R.string.label_asset_floor,
            com.speed.sofasogood.R.string.label_asset_player,
            com.speed.sofasogood.R.string.label_asset_plant,
            com.speed.sofasogood.R.string.label_asset_sofa_left,
            com.speed.sofasogood.R.string.label_asset_sofa_right,
            com.speed.sofasogood.R.string.label_asset_tv,
            com.speed.sofasogood.R.string.label_asset_tub_left,
            com.speed.sofasogood.R.string.label_asset_tub_right
    };

    public static final int[] ASSET_RES_IDS = {
            com.speed.sofasogood.R.drawable.asset_wall,
            com.speed.sofasogood.R.drawable.asset_floor,
            com.speed.sofasogood.R.drawable.asset_player,
            com.speed.sofasogood.R.drawable.asset_plant,
            com.speed.sofasogood.R.drawable.asset_sofa_left,
            com.speed.sofasogood.R.drawable.asset_sofa_right,
            com.speed.sofasogood.R.drawable.asset_tv,
            com.speed.sofasogood.R.drawable.asset_tub_left,
            com.speed.sofasogood.R.drawable.asset_tub_right
    };

    private static File getDir(Context ctx) {
        File dir = new File(ctx.getFilesDir(), DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void saveCustomBitmap(Context ctx, String key, Bitmap bmp) {
        File file = new File(getDir(ctx), key + ".png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException ignored) {}
    }

    public static Bitmap loadCustomBitmap(Context ctx, String key) {
        File file = new File(getDir(ctx), key + ".png");
        if (!file.exists()) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public static boolean hasCustom(Context ctx, String key) {
        return new File(getDir(ctx), key + ".png").exists();
    }

    public static void resetCustom(Context ctx, String key) {
        File file = new File(getDir(ctx), key + ".png");
        if (file.exists()) file.delete();
    }

    public static void resetAll(Context ctx) {
        for (String key : ASSET_KEYS) resetCustom(ctx, key);
    }
}
