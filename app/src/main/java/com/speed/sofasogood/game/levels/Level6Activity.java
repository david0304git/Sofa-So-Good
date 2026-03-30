package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level6Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_6; }
    @Override protected String getNextLevelClass() { return Level7Activity.class.getName(); }
    @Override protected int[] getDialogResIds() {
        return new int[]{ R.string.l6_d1, R.string.l6_d2, R.string.l6_d3, R.string.l6_d4, R.string.l6_d5 };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_thinking, R.drawable.character_tired, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_idea };
    }
    @Override protected int getBackgroundResId() { return R.drawable.level6_background; }
    @Override protected int getLevelNumber() { return 6; }
}
