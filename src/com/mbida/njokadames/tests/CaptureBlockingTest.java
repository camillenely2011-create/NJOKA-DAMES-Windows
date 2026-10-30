package com.mbida.njokadames.tests;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;

public final class CaptureBlockingTest {

    public static void main(String[] args) {

        int[][] board = new int[10][10];

        // Dame blanche
        board[7][2] = 2;

        // Deux pions noirs placés sur la diagonale
        board[6][3] = -1;
        board[8][1] = -1;

        GameEngine game = new GameEngine(
            board,
            GameEngine.WHITE
        );

        boolean illegalSequenceFound = false;

        for (Move move : game.getLegalMoves()) {

            boolean capturesFirst = move.getCaptured().contains(63);
            boolean capturesSecond = move.getCaptured().contains(81);

            if (capturesFirst && capturesSecond) {
                illegalSequenceFound = true;
                System.out.println(
                    "[DEFAUT DETECTE] Séquence incorrecte : " + move
                );
            }
        }

        if (illegalSequenceFound) {
            throw new AssertionError(
                "Le moteur traverse une pièce déjà capturée."
            );
        }

        System.out.println(
            "[OK] Une pièce capturée reste un obstacle."
        );
    }
}
