package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level2Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_2; }
    @Override protected String getNextLevelClass() { return Level3Activity.class.getName(); }
    @Override protected String[] getDialogs() {
        return new String[]{ "The kitchen is next… let's get it sorted!" };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy };
    }
}
