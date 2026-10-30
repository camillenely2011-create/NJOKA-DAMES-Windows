package com.mbida.njokadames.tests;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;
import java.util.List;

public final class PromotionCaptureTest {

    public static void main(String[] args) {
        int[][] board = new int[10][10];

        board[2][1] = GameEngine.WHITE;
        board[1][2] = GameEngine.BLACK;
        board[1][4] = GameEngine.BLACK;

        GameEngine game = new GameEngine(board, GameEngine.WHITE);
        List<Move> moves = game.getLegalMoves();

        boolean found = false;

        for (Move move : moves) {
            if (move.getFrom() == 21
                    && move.getTo() == 25
                    && move.getCaptureCount() == 2) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new AssertionError(
                "La prise multiple traversant la rangée de promotion est absente.");
        }

        System.out.println(
            "[OK] Le pion poursuit sa prise après avoir traversé la rangée de promotion.");
        System.out.println(
            "[OK] Deux pièces sont capturées dans la même séquence.");
    }
}
