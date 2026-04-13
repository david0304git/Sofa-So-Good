package com.speed.sofasogood.game.model;

public class ExtraLevelData {
    // TODO: design 8 extra levels with different layouts and challenges, using all game elements (walls, narrow spaces, multiple items/targets)
    // TODO: level select problem

    /*
     *  0 = Empty       1 = Wall        2 = Player
     *  3 = Water       4 = Drain       5 = Broken Pipe
     * 10 = Plant      12 = TV                        (single boxes)
     * 13 = Sofa Left  14 = Sofa Right                (2-tile box)
     * 15 = Tub Left   16 = Tub Right                 (2-tile box)
     * 20 = Plant pos  22 = TV pos                    (single targets)
     * 23 = Sofa L pos 24 = Sofa R pos                (2-tile target)
     * 25 = Tub L pos  26 = Tub R pos                 (2-tile target)
     */

    public static final int[][] EXTRA_LEVEL_1 = {
            { 1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  2,  0,  0,  0,  0,  0,  1},
            { 1,  0,  0,  3,  3,  0,  0,  1},
            { 1,  0,  3, 15, 16,  3,  0,  1},
            { 1,  0, 10,  3,  3,  0,  0,  1},
            { 1,  0,  0, 25, 26,  0,  0,  1},
            { 1,  0, 20,  0,  0,  0,  4,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1},
    };

    public static final int[][] EXTRA_LEVEL_2 = {
            { 1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  0,  0,  0,  0,  1,  0,  1},
            { 1,  0,  2, 15, 16,  0,  0,  1},
            { 1,  0,  1,  0,  5,  1,  0,  1},
            { 1,  0, 10,  0,  0,  0,  0,  1},
            { 1,  0,  1,  0,  0,  1,  0,  1},
            { 1, 20,  0, 25, 26,  4,  0,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1},
    };

    // L3 Washroom — wall obstacle, all items solvable
    public static final int[][] EXTRA_LEVEL_3 = {
            { 1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  0,  0,  0,  0,  0,  0,  1},
            { 1,  0,  2,  0,  0,  1,  0,  1},
            { 1,  0, 10,  0,  0,  0,  1,  1},
            { 1,  1,  0, 15, 16,  0,  1,  1},
            { 1, 20,  5,  0,  0,  4,  0,  1},
            { 1,  1,  0, 25, 26,  0,  0,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1},
    };

    // L4 Bedroom — two single items + sofa, tighter space
    public static final int[][] EXTRA_LEVEL_4 = {
            { 1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  0,  0,  0,  0,  1, 20,  1},
            { 1,  0,  0,  1, 12,  0,  0,  1},
            { 1,  0,  0,  4,  2,  1,  0,  1},
            { 1,  0,  0, 15, 16,  0,  0,  1},
            { 1,  0,  0,  1,  0, 10,  0,  1},
            { 1,  0,  5, 25, 26,  0,  0,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1},
    };

    // L5 Balcony — narrow corridor with wall blocks
    public static final int[][] EXTRA_LEVEL_5 = {
            { 1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  2,  0,  0,  4,  0, 20,  1},
            { 1,  0,  0,  1,  0, 10,  1,  1},
            { 1,  1,  0, 15, 16,  0,  1,  1},
            { 1,  0, 12,  0,  0,  1,  0,  1},
            { 1,  0,  0,  0, 25, 26,  0,  1},
            { 1,  0,  0,  5,  0,  1,  0,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1},
    };

    // L6 — two sofas to place
    public static final int[][] EXTRA_LEVEL_6 = {
            { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  0,  0,  0,  1,  0,  0,  0, 20,  1},
            { 1,  0,  2, 15, 16,  0,  0,  1,  0,  1},
            { 1,  0,  0,  0,  0,  0, 15, 16,  0,  1},
            { 1,  0,  0,  0,  1,  0,  0,  5,  0,  1},
            { 1,  0,  1, 25, 26,  1,  0, 12,  0,  1},
            { 1,  0, 10,  0,  0, 25, 26,  0,  0,  1},
            { 1,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            { 1,  1,  1,  4,  0,  0,  0,  0,  1,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
    };

    // L7 — two sofas + two singles, wall obstacles
    public static final int[][] EXTRA_LEVEL_7 = {
            { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  0,  2,  1,  0,  0,  1,  5,  0,  1},
            { 1,  0, 12,  0, 15, 16,  0, 10,  0,  1},
            { 1,  1,  0,  1,  1,  0,  0,  0,  0,  1},
            { 1,  0,  0,  0,  0, 15, 16,  0,  0,  1},
            { 1,  1, 25, 26,  1,  0,  0,  1,  0,  1},
            { 1, 20,  0,  0,  0, 25, 26,  0,  0,  1},
            { 1,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            { 1,  1,  4,  0,  0,  0,  0,  1,  0,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
    };

    // L8 — hardest: three singles + two sofas, multiple wall blocks
    public static final int[][] EXTRA_LEVEL_8 = {
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
            {1,  0,  0,  1, 15, 16,  0,  5,  1, 20,  0,  0,  1},
            {1,  0,  0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  1},
            {1,  0, 10,  0,  1,  1,  0,  0, 20,  1,  0,  1,  1},
            {1,  1,  0,  0,  0,  0, 15, 16,  0, 10,  0,  1,  1},
            {1,  1,  1,  0,  1,  1,  0,  0,  0,  0, 12,  0,  1},
            {1, 10,  0,  0,  0, 15, 16,  0,  0,  1,  1,  0,  1},
            {1, 10,  0,  0,  1,  1,  0,  0, 25, 26,  1,  0,  1},
            {1,  0,  0,  0,  1,  0,  0,  1, 10,  1,  1,  0,  1},
            {1,  0, 10,  0,  4, 25, 26, 10,  1,  1,  1,  0,  1},
            {1,  1, 12,  1,  0,  0, 10, 10,  1,  1,  1,  0,  1},
            {1,  1,  1,  1,  0,  0,  0,  0,  0,  2,  0,  0,  1},
            {1,  1,  1,  1,  0,  0, 10,  0,  1, 10,  0,  0,  1},
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
    };
}