package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level3Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_3; }
    @Override protected String getNextLevelClass() { return Level4Activity.class.getName(); }
    @Override protected int[] getDialogResIds() { return new int[]{ R.string.l3_d1 }; }
    @Override protected int[] getExpressions() { return new int[]{ R.drawable.character_thinking }; }
    @Override protected int getLevelNumber() {
        return 3;
    }
}
