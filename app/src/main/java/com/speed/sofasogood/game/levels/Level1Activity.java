package com.speed.sofasogood.game.levels;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.speed.sofasogood.BgmService;

public class Level1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent bgm = new Intent(this, BgmService.class);
        bgm.setAction("PAUSE");
        startService(bgm);
    }

    @Override
    protected void onDestroy() {
        Intent bgm = new Intent(this, BgmService.class);
        bgm.setAction("RESUME");
        startService(bgm);
        super.onDestroy();
    }
}
