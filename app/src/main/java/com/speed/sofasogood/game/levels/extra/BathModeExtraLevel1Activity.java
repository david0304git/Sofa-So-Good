package com.speed.sofasogood.game.levels.extra;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.levels.BaseLevelActivity;
import com.speed.sofasogood.game.model.ExtraLevelData;

public class BathModeExtraLevel1Activity extends BaseLevelActivity {
    @Override
    protected int[][] getLevelData() {
        return ExtraLevelData.EXTRA_LEVEL_1;
    }

    @Override
    protected int[] getDialogResIds() {
        return new int[]{ R.string.e1l1_d1, R.string.e1l1_d2, R.string.e1l1_d3, R.string.e1l1_d4, R.string.e1l1_d5 };
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
    protected int getLevelNumber() { return 101; }

    @Override
    protected int[] getVoiceResIds() {
        return new int[]{ R.raw.e1l1_d1, R.raw.e1l1_d2, R.raw.e1l1_d3, R.raw.e1l1_d4, R.raw.e1l1_d5 };
    }

    @Override
    protected int getBackgroundResId() {
        return R.drawable.level3_background;
    }

    @Override
    protected int[] getDialogBackgrounds() {
        return new int[]{ 0, R.drawable.extra1_level1_background, R.drawable.extra1_level1_background, R.drawable.extra1_level1_background, R.drawable.extra1_level1_background };
    }
}
