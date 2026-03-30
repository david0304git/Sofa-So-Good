package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level3Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_3; }
    @Override protected String getNextLevelClass() { return Level4Activity.class.getName(); }
    @Override protected int[] getDialogResIds() {
        return new int[]{ R.string.l3_d1, R.string.l3_d2, R.string.l3_d3, R.string.l3_d4, R.string.l3_d5 };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_tired, R.drawable.character_thinking, R.drawable.character_idea };
    }
    @Override protected int getBackgroundResId() { return R.drawable.level3_background; }
    @Override protected int getLevelNumber() { return 3; }
    @Override protected int[] getVoiceResIds() {
        return new int[]{ R.raw.l3_d1, R.raw.l3_d2, R.raw.l3_d3, R.raw.l3_d4, R.raw.l3_d5 };
    }
}
