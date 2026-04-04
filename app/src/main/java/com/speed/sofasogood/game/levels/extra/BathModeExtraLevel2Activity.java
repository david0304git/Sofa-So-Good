package com.speed.sofasogood.game.levels.extra;

import com.speed.sofasogood.R;
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
}
