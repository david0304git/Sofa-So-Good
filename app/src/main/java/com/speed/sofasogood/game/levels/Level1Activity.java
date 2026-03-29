package com.speed.sofasogood.game.levels;

import com.speed.sofasogood.R;
import com.speed.sofasogood.game.model.LevelData;

public class Level1Activity extends BaseLevelActivity {
    @Override protected int[][] getLevelData() { return LevelData.LEVEL_1; }
    @Override protected String getNextLevelClass() { return Level2Activity.class.getName(); }
    @Override protected String[] getDialogs() {
        return new String[]{
            "Finally, I've moved in… this is my new home.",
            "But the living room is such a mess, boxes and furniture everywhere.",
            "I should set up the sofa and table first, so it feels more like home.",
            "Hmm, those boxes in the corner need sorting too.",
            "Alright, let's start arranging—time to make the living room shine!"
        };
    }
    @Override protected int[] getExpressions() {
        return new int[]{ R.drawable.character_happy, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_thinking, R.drawable.character_idea };
    }
}
