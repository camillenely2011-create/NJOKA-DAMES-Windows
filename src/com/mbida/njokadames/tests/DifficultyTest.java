package com.mbida.njokadames.tests;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;
import com.mbida.njokadames.ai.DraughtsAI;

import java.util.List;

public final class DifficultyTest {

    public static void main(String[] args) {
        GameEngine game = new GameEngine();
        DraughtsAI ai = new DraughtsAI();

        int[][] originalBoard = game.getBoard();
        int originalPlayer = game.getCurrentPlayer();

        int[] levels = {
            DraughtsAI.DIFFICULTY_EASY,
            DraughtsAI.DIFFICULTY_MEDIUM,
            DraughtsAI.DIFFICULTY_HARD,
            DraughtsAI.DIFFICULTY_EXPERT,
            DraughtsAI.DIFFICULTY_MASTER
        };

        String[] names = {
            "Facile", "Moyen", "Difficile", "Expert", "Maître"
        };

        int total = 0;

        System.out.println("=== TEST DES CINQ NIVEAUX IA ===");

        for (int i = 0; i < levels.length; i++) {
            long start = System.nanoTime();

            Move chosen = ai.chooseMove(game, levels[i]);

            long elapsed = (System.nanoTime() - start) / 1_000_000L;

            if (chosen == null) {
                throw new AssertionError(
                    names[i] + " : aucun coup choisi.");
            }

            List<Move> legalMoves = game.getLegalMoves();

            if (!legalMoves.contains(chosen)) {
                throw new AssertionError(
                    names[i] + " : coup illégal.");
            }

            if (game.getCurrentPlayer() != originalPlayer) {
                throw new AssertionError(
                    names[i] + " : joueur courant modifié.");
            }

            int[][] currentBoard = game.getBoard();

            for (int r = 0; r < GameEngine.SIZE; r++) {
                for (int c = 0; c < GameEngine.SIZE; c++) {
                    if (originalBoard[r][c] != currentBoard[r][c]) {
                        throw new AssertionError(
                            names[i] + " : plateau modifié.");
                    }
                }
            }

            System.out.println(
                "[OK] " + names[i]
                + " : coup légal, plateau intact, "
                + elapsed + " ms."
            );

            total++;
        }

        System.out.println(
            "TOTAL : " + total + " NIVEAUX TESTÉS AVEC SUCCÈS."
        );
    }
}
