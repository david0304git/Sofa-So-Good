package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level3Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_3; }
    @Override protected String getNextLevelClass() { return Level4Activity.class.getName(); }
    @Override protected String[] getDialogs() {
        return new String[]{ "The washroom needs some work too!" };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_thinking };
    }
}
