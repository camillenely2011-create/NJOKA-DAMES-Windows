package com.mbida.njokadames.tests;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;

import java.util.List;

public final class EngineTest {

    private static int passed = 0;

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("ECHEC : " + message);
        }
        passed++;
        System.out.println("[OK] " + message);
    }

    private static GameEngine position(int[][] pieces, int player) {
        return new GameEngine(pieces, player);
    }

    public static void main(String[] args) {
        System.out.println("=== TESTS NJOKA DAMES ===");

        // 1. Position de départ
        GameEngine game = new GameEngine();

        check(game.countPieces(1) == 20,
                "20 pions blancs au départ");
        check(game.countPieces(-1) == 20,
                "20 pions noirs au départ");
        check(game.getCurrentPlayer() == 1,
                "Les blancs commencent");
        check(game.getLegalMoves().size() == 9,
                "9 déplacements initiaux");

        // 2. Prise obligatoire
        int[][] p = new int[10][10];
        p[6][1] = 1;
        p[5][2] = -1;

        GameEngine capture = position(p, 1);
        List<Move> moves = capture.getLegalMoves();

        check(moves.size() == 1,
                "La prise obligatoire élimine les déplacements simples");
        check(moves.get(0).getCaptureCount() == 1,
                "Une pièce adverse est capturée");

        capture.applyMove(moves.get(0));

        check(capture.getPiece(4, 3) == 1,
                "Le pion arrive à destination");
        check(capture.getPiece(5, 2) == 0,
                "La pièce capturée disparaît");
        check(capture.getCurrentPlayer() == -1,
                "Le tour passe aux noirs");

        // 3. Prise multiple
        p = new int[10][10];
        p[6][1] = 1;
        p[5][2] = -1;
        p[3][4] = -1;

        GameEngine multiple = position(p, 1);
        moves = multiple.getLegalMoves();

        check(moves.size() == 1,
                "La séquence de prises est obligatoire");
        check(moves.get(0).getCaptureCount() == 2,
                "Le moteur trouve une prise de deux pièces");

        multiple.applyMove(moves.get(0));

        check(multiple.getPiece(2, 5) == 1,
                "Le pion termine la prise multiple");
        check(multiple.countPieces(-1) == 0,
                "Les deux pièces adverses sont retirées");

        // 4. Obligation de choisir la prise maximale
        p = new int[10][10];
        p[6][1] = 1;
        p[5][2] = -1;
        p[3][4] = -1;
        p[6][7] = 1;
        p[5][8] = -1;

        GameEngine maximum = position(p, 1);
        moves = maximum.getLegalMoves();

        check(!moves.isEmpty(),
                "Des prises légales sont disponibles");

        for (Move m : moves) {
            check(m.getCaptureCount() == 2,
                    "Seules les prises maximales sont conservées");
        }

        // 5. Dame volante
        p = new int[10][10];
        p[5][2] = 2;
        p[3][4] = -1;

        GameEngine king = position(p, 1);
        moves = king.getLegalMoves();

        check(!moves.isEmpty(),
                "La dame dispose d'une capture");
        check(moves.stream().allMatch(
                        m -> m.getCaptureCount() == 1),
                "La dame capture une pièce à distance");
        check(moves.stream().anyMatch(
                        m -> m.getTo() == 25),
                "La dame peut atterrir au-delà de la pièce");

        // 6. Promotion en dame
        p = new int[10][10];
        p[1][2] = 1;

        GameEngine promotion = position(p, 1);
        moves = promotion.getLegalMoves();

        Move promotionMove = null;
        for (Move m : moves) {
            if (m.getTo() == 1) {
                promotionMove = m;
                break;
            }
        }

        check(promotionMove != null,
                "Un coup de promotion est disponible");

        promotion.applyMove(promotionMove);

        check(promotion.getPiece(0, 1) == 2,
                "Le pion blanc devient une dame");

        // 7. Copie du plateau
        int[][] copy = promotion.getBoard();
        copy[0][1] = 0;

        check(promotion.getPiece(0, 1) == 2,
                "La copie du plateau ne modifie pas le moteur");

        System.out.println();
        System.out.println("TOTAL : " + passed + " VERIFICATIONS REUSSIES");
        System.out.println("FIN DES TESTS.");
    }
}
