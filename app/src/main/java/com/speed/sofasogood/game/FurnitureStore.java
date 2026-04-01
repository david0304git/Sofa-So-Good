package com.speed.sofasogood.game;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class FurnitureStore {

    public static final String KEY_PLANT   = "plant";
    public static final String KEY_TV      = "tv";
    public static final String KEY_SOFA_L  = "sofa_l";
    public static final String KEY_SOFA_R  = "sofa_r";

    private static final FurnitureStore INSTANCE = new FurnitureStore();
    private final Map<String, Bitmap> customs = new HashMap<>();

    private FurnitureStore() {}

    public static FurnitureStore get() { return INSTANCE; }

    public void set(String key, Bitmap bmp) { customs.put(key, bmp); }

    public Bitmap get(String key) { return customs.get(key); }

    public boolean has(String key) { return customs.containsKey(key) && customs.get(key) != null; }

    public void reset(String key) { customs.remove(key); }

    public void resetAll() { customs.clear(); }
}
