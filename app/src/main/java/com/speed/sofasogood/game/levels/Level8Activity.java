package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level8Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_8; }
    @Override protected String getNextLevelClass() { return null; }
    @Override protected int[] getDialogResIds() { return new int[]{ R.string.l8_d1 }; }
    @Override protected int[] getExpressions() { return new int[]{ R.drawable.character_idea }; }
    @Override protected int getLevelNumber() {
        return 8;
    }
}
