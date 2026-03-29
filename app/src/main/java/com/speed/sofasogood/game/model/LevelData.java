package com.speed.sofasogood.game.model;

public class LevelData {

    /*
     *  0 = Empty       1 = Wall        2 = Player
     * 10 = Plant      12 = TV                          (single boxes)
     * 13 = Sofa Left  14 = Sofa Right                  (2-tile box)
     * 20 = Plant pos  22 = TV pos                      (single targets)
     * 23 = Sofa L pos 24 = Sofa R pos                  (2-tile target)
     */

    public static final int[][] LEVEL_1 = {
            { 1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  0,  0,  0,  0,  0,  0,  1},
            { 1,  0,  2,  0,  0,  0,  0,  1},
            { 1,  0,  0, 13, 14,  0,  0,  1},
            { 1,  0, 10,  0,  0, 12,  0,  1},
            { 1,  0,  0, 23, 24,  0,  0,  1},
            { 1,  0, 20,  0,  0, 22,  0,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1},
    };
}
