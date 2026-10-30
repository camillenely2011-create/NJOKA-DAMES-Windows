package com.mbida.njokadames.tests;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;
import com.mbida.njokadames.ai.DraughtsAI;

import java.util.List;

public final class AITest {

    public static void main(String[] args) {
        GameEngine game = new GameEngine();
        DraughtsAI ai = new DraughtsAI();

        int playerBefore = game.getCurrentPlayer();
        int[][] boardBefore = game.getBoard();

        System.out.println("=== TESTS DE L'IA NJOKA DAMES ===");

        Move chosen = ai.chooseMove(game);
        if (chosen == null) {
            throw new AssertionError("L'IA n'a choisi aucun coup.");
        }
        System.out.println("[OK] L'IA choisit un coup.");

        boolean legal = false;
        List<Move> moves = game.getLegalMoves();

        for (Move move : moves) {
            if (move.equals(chosen)) {
                legal = true;
                break;
            }
        }

        if (!legal) {
            throw new AssertionError("L'IA a choisi un coup illégal.");
        }
        System.out.println("[OK] Le coup choisi est légal.");

        if (game.getCurrentPlayer() != playerBefore) {
            throw new AssertionError(
                "L'IA a modifié le joueur courant de la partie.");
        }
        System.out.println("[OK] Le joueur courant reste inchangé.");

        int[][] boardAfter = game.getBoard();

        for (int r = 0; r < GameEngine.SIZE; r++) {
            for (int c = 0; c < GameEngine.SIZE; c++) {
                if (boardBefore[r][c] != boardAfter[r][c]) {
                    throw new AssertionError(
                        "L'IA a modifié le plateau original.");
                }
            }
        }
        System.out.println("[OK] Le plateau original reste inchangé.");

        System.out.println("COUP CHOISI : " +
            chosen.getFrom() + " -> " + chosen.getTo());
        System.out.println("TOTAL : 4 VERIFICATIONS REUSSIES");
    }
}
