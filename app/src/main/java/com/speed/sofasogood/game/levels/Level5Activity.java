package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level5Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_5; }
    @Override protected String getNextLevelClass() { return Level6Activity.class.getName(); }
    @Override protected int[] getDialogResIds() { return new int[]{ R.string.l5_d1 }; }
    @Override protected int[] getExpressions() { return new int[]{ R.drawable.character_thinking }; }
    @Override protected int getLevelNumber() {
        return 5;
    }
}
