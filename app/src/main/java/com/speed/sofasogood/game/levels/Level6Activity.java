package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level6Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_6; }
    @Override protected String getNextLevelClass() { return Level7Activity.class.getName(); }
    @Override protected String[] getDialogs() {
        return new String[]{ "This room has a lot of furniture to move!" };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_thinking };
    }
}
