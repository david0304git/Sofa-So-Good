package com.speed.sofasogood.game.levels.extra;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.LevelScoreConfig;
import com.speed.sofasogood.game.levels.BaseLevelActivity;
import com.speed.sofasogood.game.model.ExtraLevelData;

public class BathModeExtraLevel2Activity extends BaseLevelActivity {
    @Override
    protected int[][] getLevelData() {
        return ExtraLevelData.EXTRA_LEVEL_2;
    }

    @Override
    protected int[] getDialogResIds() {
        return new int[]{ R.string.l1_d1, R.string.l1_d2, R.string.l1_d3 };
    }

    @Override
    protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy, R.drawable.character_thinking, R.drawable.character_idea };
    }

    @Override
    protected String getNextLevelClass() {
        return null;
    }

    @Override
    protected int getLevelNumber() { return 102; }

    @Override
    protected int[] getVoiceResIds() {
        return null;
    }

    @Override
    protected int getBackgroundResId() {
        return R.drawable.level3_background;
    }

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
