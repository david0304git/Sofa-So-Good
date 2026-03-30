package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level1Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_1; }
    @Override protected String getNextLevelClass() { return Level2Activity.class.getName(); }
    @Override protected int[] getDialogResIds() {
        return new int[]{ R.string.l1_d1, R.string.l1_d2, R.string.l1_d3, R.string.l1_d4, R.string.l1_d5 };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_idea };
    }
    @Override protected int getLevelNumber() { return 1; }
}
