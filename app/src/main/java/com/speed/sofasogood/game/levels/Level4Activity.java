package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level4Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_4; }
    @Override protected String getNextLevelClass() { return Level5Activity.class.getName(); }
    @Override protected int[] getDialogResIds() {
        return new int[]{ R.string.l4_d1, R.string.l4_d2, R.string.l4_d3, R.string.l4_d4, R.string.l4_d5 };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy, R.drawable.character_thinking, R.drawable.character_tired, R.drawable.character_thinking, R.drawable.character_idea };
    }
    @Override protected int getBackgroundResId() { return R.drawable.level4_background; }
    @Override protected int getLevelNumber() { return 4; }
}
