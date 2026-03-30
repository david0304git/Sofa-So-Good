package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level7Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_7; }
    @Override protected String getNextLevelClass() { return Level8Activity.class.getName(); }
    @Override protected int[] getDialogResIds() {
        return new int[]{ R.string.l7_d1, R.string.l7_d2, R.string.l7_d3, R.string.l7_d4, R.string.l7_d5 };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_idea };
    }
    @Override protected int getBackgroundResId() { return R.drawable.level7_background; }
    @Override protected int getLevelNumber() { return 7; }
}
