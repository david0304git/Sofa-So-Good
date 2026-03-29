package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level4Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_4; }
    @Override protected String getNextLevelClass() { return Level5Activity.class.getName(); }
    @Override protected String[] getDialogs() {
        return new String[]{ "Time to set up the bedroom!" };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy };
    }
}
