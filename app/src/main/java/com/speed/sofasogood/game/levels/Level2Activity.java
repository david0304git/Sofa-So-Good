package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level2Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_2; }
    @Override protected String getNextLevelClass() { return Level3Activity.class.getName(); }
    @Override protected int[] getDialogResIds() { return new int[]{ R.string.l2_d1 }; }
    @Override protected int[] getExpressions() { return new int[]{ R.drawable.character_happy }; }
}
