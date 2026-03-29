package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level8Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_8; }
    @Override protected String getNextLevelClass() { return null; } // last level
    @Override protected String[] getDialogs() {
        return new String[]{ "The final room… let's finish this!" };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_idea };
    }
}
