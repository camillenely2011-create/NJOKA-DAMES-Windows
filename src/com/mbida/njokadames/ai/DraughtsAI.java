package com.mbida.njokadames.ai;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Adversaire informatique hors ligne pour NJOKA DAMES.
 * Cinq niveaux, recherche progressive et table de transposition.
 */
public final class DraughtsAI {

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    public static final int DIFFICULTY_EXPERT = 3;
    public static final int DIFFICULTY_MASTER = 4;

    private static final int INF = 1_000_000;
    private static final int KING_VALUE = 300;
    private static final int MAN_VALUE = 100;

    private static final int EXACT = 0;
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 2;

    private static final int TABLE_SIZE = 32768;

    private final Random random = new Random();

    private final TranspositionEntry[] transpositionTable =
            new TranspositionEntry[TABLE_SIZE];

    private static final SearchTimeout SEARCH_TIMEOUT =
            new SearchTimeout();

    private static final class SearchTimeout
            extends RuntimeException {

        SearchTimeout() {
            super(null, null, false, false);
        }
    }

    private static final class TranspositionEntry {
        final long key;
        final int depth;
        final int score;
        final int bound;
        final Move bestMove;

        TranspositionEntry(
                long key,
                int depth,
                int score,
                int bound,
                Move bestMove) {

            this.key = key;
            this.depth = depth;
            this.score = score;
            this.bound = bound;
            this.bestMove = bestMove;
        }
    }

    public Move chooseMove(GameEngine game) {
        return chooseMove(game, DIFFICULTY_MEDIUM);
    }

    /**
     * Recherche progressive : le dernier coup entièrement calculé
     * reste disponible lorsque le temps de réflexion est écoulé.
     */
    public Move chooseMove(GameEngine game, int difficulty) {
        if (game == null) {
            return null;
        }

        List<Move> moves = game.getLegalMoves();

        if (moves.isEmpty()) {
            return null;
        }

        Arrays.fill(transpositionTable, null);

        Collections.shuffle(moves, random);
        moves = orderMoves(game, moves, null);

        Move bestMove = moves.get(0);

        int maxDepth = getSearchDepth(difficulty);
        long timeLimit = getTimeLimit(difficulty);
        long deadline = System.nanoTime()
                + timeLimit * 1_000_000L;

        int player = game.getCurrentPlayer();

        for (int depth = 1; depth <= maxDepth; depth++) {
            Move iterationBest = null;
            int iterationBestScore = -INF;
            int alpha = -INF;

            try {
                for (Move move : moves) {
                    checkTime(deadline);

                    GameEngine copy = copyGame(game);
                    copy.applyMove(move);

                    int score = minimax(
                            copy,
                            depth - 1,
                            alpha,
                            INF,
                            player,
                            deadline
                    );

                    if (score > iterationBestScore) {
                        iterationBestScore = score;
                        iterationBest = move;
                    }

                    alpha = Math.max(alpha, score);
                }

                if (iterationBest != null) {
                    bestMove = iterationBest;
                }

            } catch (SearchTimeout timeout) {
                break;
            }
        }

        return bestMove;
    }

    private int getSearchDepth(int difficulty) {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return 2;
            case DIFFICULTY_MEDIUM:
                return 4;
            case DIFFICULTY_HARD:
                return 5;
            case DIFFICULTY_EXPERT:
                return 6;
            case DIFFICULTY_MASTER:
                return 7;
            default:
                return 4;
        }
    }

    private long getTimeLimit(int difficulty) {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return 250;
            case DIFFICULTY_MEDIUM:
                return 650;
            case DIFFICULTY_HARD:
                return 1200;
            case DIFFICULTY_EXPERT:
                return 1800;
            case DIFFICULTY_MASTER:
                return 2500;
            default:
                return 650;
        }
    }

    private void checkTime(long deadline) {
        if (System.nanoTime() >= deadline) {
            throw SEARCH_TIMEOUT;
        }
    }

    /**
     * Empreinte 64 bits du plateau et du joueur actif.
     */
    private long positionKey(GameEngine game) {
        int[][] board = game.getBoard();

        long hash = 0xcbf29ce484222325L;

        for (int r = 0; r < GameEngine.SIZE; r++) {
            for (int c = 0; c < GameEngine.SIZE; c++) {
                hash ^= board[r][c] + 3;
                hash *= 0x100000001b3L;
            }
        }

        hash ^= game.getCurrentPlayer() + 3;
        hash *= 0x100000001b3L;

        return hash;
    }

    private int tableIndex(long key) {
        return (int) (key ^ (key >>> 32))
                & (TABLE_SIZE - 1);
    }

    private TranspositionEntry findEntry(long key) {
        TranspositionEntry entry =
                transpositionTable[tableIndex(key)];

        if (entry != null && entry.key == key) {
            return entry;
        }

        return null;
    }

    private void storeEntry(
            long key,
            int depth,
            int score,
            int bound,
            Move bestMove) {

        int index = tableIndex(key);
        TranspositionEntry old = transpositionTable[index];

        if (old == null
                || old.key != key
                || depth >= old.depth) {

            transpositionTable[index] =
                    new TranspositionEntry(
                            key,
                            depth,
                            score,
                            bound,
                            bestMove
                    );
        }
    }

    /**
     * Place le meilleur coup mémorisé en première position.
     */
    private List<Move> orderMoves(
            GameEngine game,
            List<Move> moves,
            Move preferredMove) {

        List<Move> ordered = new ArrayList<>(moves);
        int[][] board = game.getBoard();

        for (int i = 1; i < ordered.size(); i++) {
            Move current = ordered.get(i);
            int currentScore = moveOrderScore(
                    board, current, preferredMove);

            int j = i - 1;

            while (j >= 0) {
                int previousScore = moveOrderScore(
                        board, ordered.get(j), preferredMove);

                if (previousScore >= currentScore) {
                    break;
                }

                ordered.set(j + 1, ordered.get(j));
                j--;
            }

            ordered.set(j + 1, current);
        }

        return ordered;
    }

    private int moveOrderScore(
            int[][] board,
            Move move,
            Move preferredMove) {

        int score = move.getCaptureCount() * 10000;

        if (preferredMove != null && move.equals(preferredMove)) {
            score += 1000000;
        }

        int from = move.getFrom();
        int to = move.getTo();

        int fromRow = from / GameEngine.SIZE;
        int fromCol = from % GameEngine.SIZE;
        int toRow = to / GameEngine.SIZE;
        int toCol = to % GameEngine.SIZE;

        int piece = board[fromRow][fromCol];

        if (Math.abs(piece) == 1) {
            boolean promotion =
                    (piece == GameEngine.WHITE && toRow == 0)
                    || (piece == GameEngine.BLACK
                        && toRow == GameEngine.SIZE - 1);

            if (promotion) {
                score += 2000;
            }
        }

        int centerDistance =
                Math.abs(4 - toRow) + Math.abs(4 - toCol);

        score += Math.max(0, 8 - centerDistance);

        return score;
    }

    private int minimax(
            GameEngine game,
            int depth,
            int alpha,
            int beta,
            int aiPlayer,
            long deadline) {

        checkTime(deadline);

        List<Move> moves = game.getLegalMoves();

        if (moves.isEmpty()) {
            return game.getCurrentPlayer() == aiPlayer
                    ? -100_000 - depth
                    : 100_000 + depth;
        }

        if (depth <= 0) {
            return evaluate(game, aiPlayer);
        }

        int originalAlpha = alpha;
        int originalBeta = beta;

        long key = positionKey(game);
        TranspositionEntry entry = findEntry(key);

        Move preferredMove = null;

        if (entry != null) {
            preferredMove = entry.bestMove;

            if (entry.depth >= depth) {
                if (entry.bound == EXACT) {
                    return entry.score;
                }

                if (entry.bound == LOWER_BOUND) {
                    alpha = Math.max(alpha, entry.score);
                } else if (entry.bound == UPPER_BOUND) {
                    beta = Math.min(beta, entry.score);
                }

                if (alpha >= beta) {
                    return entry.score;
                }
            }
        }

        moves = orderMoves(game, moves, preferredMove);

        boolean maximizing =
                game.getCurrentPlayer() == aiPlayer;

        int best = maximizing ? -INF : INF;
        Move bestMove = null;

        for (Move move : moves) {
            checkTime(deadline);

            GameEngine copy = copyGame(game);
            copy.applyMove(move);

            int score = minimax(
                    copy,
                    depth - 1,
                    alpha,
                    beta,
                    aiPlayer,
                    deadline
            );

            if (maximizing) {
                if (score > best) {
                    best = score;
                    bestMove = move;
                }

                alpha = Math.max(alpha, best);

            } else {
                if (score < best) {
                    best = score;
                    bestMove = move;
                }

                beta = Math.min(beta, best);
            }

            if (beta <= alpha) {
                break;
            }
        }

        int bound;

        if (best <= originalAlpha) {
            bound = UPPER_BOUND;
        } else if (best >= originalBeta) {
            bound = LOWER_BOUND;
        } else {
            bound = EXACT;
        }

        storeEntry(
                key,
                depth,
                best,
                bound,
                bestMove
        );

        return best;
    }

    private int evaluate(GameEngine game, int aiPlayer) {
        int[][] board = game.getBoard();
        int score = 0;

        for (int r = 0; r < GameEngine.SIZE; r++) {
            for (int c = 0; c < GameEngine.SIZE; c++) {
                int piece = board[r][c];

                if (piece == GameEngine.EMPTY) {
                    continue;
                }

                int sign = Integer.signum(piece);

                int value = Math.abs(piece) == 2
                        ? KING_VALUE
                        : MAN_VALUE;

                if (Math.abs(piece) == 1) {
                    int advancement = sign == GameEngine.WHITE
                            ? GameEngine.SIZE - 1 - r
                            : r;

                    value += advancement * 4;

                } else {
                    int centerDistance =
                            Math.abs(4 - r) + Math.abs(4 - c);

                    value += Math.max(
                            0, 8 - centerDistance
                    ) * 2;
                }

                score += sign == aiPlayer
                        ? value
                        : -value;
            }
        }

        return score;
    }

    private GameEngine copyGame(GameEngine source) {
        return new GameEngine(
                source.getBoard(),
                source.getCurrentPlayer()
        );
    }
}
