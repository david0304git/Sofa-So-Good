package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.LevelScoreConfig;
import com.speed.sofasogood.game.model.LevelData;

public class Level5Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_5; }
    @Override protected String getNextLevelClass() { return Level6Activity.class.getName(); }
    @Override protected int[] getDialogResIds() {
        return new int[]{ R.string.l5_d1, R.string.l5_d2, R.string.l5_d3, R.string.l5_d4, R.string.l5_d5 };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_idea };
    }
    @Override protected int getBackgroundResId() { return R.drawable.level5_background; }
    @Override protected int getLevelNumber() { return 5; }
    @Override
    protected LevelScoreConfig getScoreConfig() {
        return new LevelScoreConfig(
                12_000,   // bestTimeMs
                90_000,   // worstTimeMs
                10,       // bestSteps
                40,       // worstSteps

                20_000,   // star3TimeMs
                35_000,   // star2TimeMs
                60_000,   // star1TimeMs

                12,       // star3Steps
                18,       // star2Steps
                28        // star1Steps
        );
    }
}
