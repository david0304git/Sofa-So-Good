package com.speed.sofasogood.game.levels.extra;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.LevelScoreConfig;
import com.speed.sofasogood.game.levels.BaseLevelActivity;
import com.speed.sofasogood.game.model.ExtraLevelData;

public class CatModeExtraLevel1Activity extends BaseLevelActivity {
    @Override
    protected int[][] getLevelData() {
        return ExtraLevelData.EXTRA_LEVEL_5;
    }

    @Override
    protected int[] getDialogResIds() {
        return new int[]{ R.string.e1l1_d1, R.string.e1l1_d2, R.string.e1l1_d3, R.string.e1l1_d4, R.string.e1l1_d5 };
    }

    @Override
    protected boolean shouldSkipDialog() {
        return true;
    }

    @Override
    protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy, R.drawable.character_scared2, R.drawable.character_scared, R.drawable.character_angry, R.drawable.character_angry };
    }

    @Override
    protected String getNextLevelClass() {
        return null;
    }

    @Override
    protected int getLevelNumber() { return 105; }

    @Override
    protected int[] getVoiceResIds() {
        return new int[]{ R.raw.e1l1_d1, R.raw.e1l1_d2, R.raw.e1l1_d3, R.raw.e1l1_d4, R.raw.e1l1_d5 };
    }

    @Override
    protected int getBackgroundResId() {
        return R.drawable.level8_background;
    }

    @Override
    protected int[] getDialogBackgrounds() {
        return new int[]{ 0, R.drawable.extra1_level1_background, R.drawable.extra1_level1_background, R.drawable.extra1_level1_background, R.drawable.extra1_level1_background };
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
