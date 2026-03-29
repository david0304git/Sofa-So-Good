package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level5Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_5; }
    @Override protected String getNextLevelClass() { return Level6Activity.class.getName(); }
    @Override protected String[] getDialogs() {
        return new String[]{ "The balcony is a tight squeeze…" };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_thinking };
    }
}
