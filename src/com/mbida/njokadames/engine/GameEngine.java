package com.mbida.njokadames.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Moteur autonome des dames internationales 10x10.
 * Blanc : 1 (pion), 2 (dame).
 * Noir  : -1 (pion), -2 (dame).
 * 0 : case vide.
 */
public final class GameEngine {
    public static final int SIZE = 10;
    public static final int EMPTY = 0;
    public static final int WHITE = 1;
    public static final int BLACK = -1;

    private static final int[] DR = {-1, -1, 1, 1};
    private static final int[] DC = {-1, 1, -1, 1};

    private final int[][] board = new int[SIZE][SIZE];
    private int currentPlayer;

    public static final class Move {
        private final List<Integer> path;
        private final List<Integer> captured;

        private Move(List<Integer> path, List<Integer> captured) {
            this.path = Collections.unmodifiableList(
                    new ArrayList<>(path));
            this.captured = Collections.unmodifiableList(
                    new ArrayList<>(captured));
        }

        public List<Integer> getPath() {
            return path;
        }

        public List<Integer> getCaptured() {
            return captured;
        }

        public int getFrom() {
            return path.get(0);
        }

        public int getTo() {
            return path.get(path.size() - 1);
        }

        public int getCaptureCount() {
            return captured.size();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Move)) return false;
            Move other = (Move) obj;
            return path.equals(other.path)
                    && captured.equals(other.captured);
        }

        @Override
        public int hashCode() {
            return 31 * path.hashCode() + captured.hashCode();
        }

        @Override
        public String toString() {
            return "Move{path=" + path
                    + ", captures=" + captured + "}";
        }
    }

    /** Crée une nouvelle partie standard. */
    public GameEngine() {
        currentPlayer = WHITE;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (!isPlayable(r, c)) continue;
                if (r < 4) board[r][c] = BLACK;
                else if (r >= 6) board[r][c] = WHITE;
            }
        }
    }

    /** Constructeur utile pour restaurer une position ou la tester. */
    public GameEngine(int[][] position, int player) {
        if (position == null || position.length != SIZE)
            throw new IllegalArgumentException("Plateau invalide");
        if (player != WHITE && player != BLACK)
            throw new IllegalArgumentException("Joueur invalide");

        for (int r = 0; r < SIZE; r++) {
            if (position[r] == null || position[r].length != SIZE)
                throw new IllegalArgumentException("Ligne invalide");
            for (int c = 0; c < SIZE; c++) {
                int v = position[r][c];
                if (v < -2 || v > 2)
                    throw new IllegalArgumentException("Pièce invalide");
                if (v != 0 && !isPlayable(r, c))
                    throw new IllegalArgumentException("Case interdite");
                board[r][c] = v;
            }
        }
        currentPlayer = player;
    }

    public static boolean isPlayable(int r, int c) {
        return r >= 0 && r < SIZE
                && c >= 0 && c < SIZE
                && (r + c) % 2 == 1;
    }

    private static boolean inside(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    private static int row(int square) {
        return square / SIZE;
    }

    private static int col(int square) {
        return square % SIZE;
    }

    private static int square(int r, int c) {
        return r * SIZE + c;
    }

    private static boolean isKing(int piece) {
        return Math.abs(piece) == 2;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPiece(int r, int c) {
        if (!inside(r, c))
            throw new IndexOutOfBoundsException();
        return board[r][c];
    }

    public int[][] getBoard() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++)
            System.arraycopy(board[r], 0, copy[r], 0, SIZE);
        return copy;
    }

    public int countPieces(int player) {
        int count = 0;
        for (int[] line : board)
            for (int p : line)
                if (p != 0 && Integer.signum(p) == player)
                    count++;
        return count;
    }

    /**
     * Renvoie uniquement les coups légaux.
     * Une prise est obligatoire ; parmi les prises, il faut
     * capturer le plus grand nombre de pièces possible.
     */
    public List<Move> getLegalMoves() {
        List<Move> captures = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int piece = board[r][c];
                if (piece != 0
                        && Integer.signum(piece) == currentPlayer) {
                    List<Integer> path = new ArrayList<>();
                    List<Integer> taken = new ArrayList<>();
                    path.add(square(r, c));
                    collectCaptures(r, c, path, taken, captures);
                }
            }
        }

        if (!captures.isEmpty()) {
            int max = 0;
            for (Move m : captures)
                max = Math.max(max, m.getCaptureCount());

            List<Move> legal = new ArrayList<>();
            for (Move m : captures)
                if (m.getCaptureCount() == max) legal.add(m);
            return legal;
        }

        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int piece = board[r][c];
                if (piece == 0
                        || Integer.signum(piece) != currentPlayer)
                    continue;

                if (isKing(piece)) {
                    for (int d = 0; d < 4; d++) {
                        int nr = r + DR[d], nc = c + DC[d];
                        while (inside(nr, nc)
                                && board[nr][nc] == EMPTY) {
                            moves.add(new Move(
                                    Arrays.asList(square(r, c),
                                            square(nr, nc)),
                                    Collections.emptyList()));
                            nr += DR[d];
                            nc += DC[d];
                        }
                    }
                } else {
                    int forward = piece > 0 ? -1 : 1;
                    for (int dc : new int[]{-1, 1}) {
                        int nr = r + forward, nc = c + dc;
                        if (inside(nr, nc)
                                && isPlayable(nr, nc)
                                && board[nr][nc] == EMPTY) {
                            moves.add(new Move(
                                    Arrays.asList(square(r, c),
                                            square(nr, nc)),
                                    Collections.emptyList()));
                        }
                    }
                }
            }
        }
        return moves;
    }

    private void collectCaptures(
            int r, int c,
            List<Integer> path,
            List<Integer> taken,
            List<Move> output) {

        int piece = board[r][c];
        boolean extended = false;

        if (isKing(piece)) {
            for (int d = 0; d < 4; d++) {
                int nr = r + DR[d], nc = c + DC[d];

                // La première pièce rencontrée reste un obstacle.
                while (inside(nr, nc)
                        && board[nr][nc] == EMPTY) {
                    nr += DR[d];
                    nc += DC[d];
                }

                if (!inside(nr, nc)
                        || board[nr][nc] == EMPTY
                        || Integer.signum(board[nr][nc])
                           == Integer.signum(piece)
                        || taken.contains(square(nr, nc))) {
                    continue;
                }

                int er = nr, ec = nc;
                int lr = er + DR[d], lc = ec + DC[d];

                // La dame peut atterrir sur chaque case libre
                // située après la pièce adverse.
                while (inside(lr, lc)
                        && board[lr][lc] == EMPTY) {

                    extended = true;

                    board[r][c] = EMPTY;
                    board[lr][lc] = piece;

                    path.add(square(lr, lc));
                    taken.add(square(er, ec));

                    collectCaptures(lr, lc, path, taken, output);

                    taken.remove(taken.size() - 1);
                    path.remove(path.size() - 1);

                    board[lr][lc] = EMPTY;
                    board[r][c] = piece;

                    lr += DR[d];
                    lc += DC[d];
                }
            }
        } else {
            for (int d = 0; d < 4; d++) {
                int er = r + DR[d], ec = c + DC[d];
                int lr = r + 2 * DR[d], lc = c + 2 * DC[d];

                if (!inside(er, ec) || !inside(lr, lc)) {
                    continue;
                }

                int enemy = board[er][ec];

                if (enemy == EMPTY
                        || Integer.signum(enemy)
                           == Integer.signum(piece)
                        || taken.contains(square(er, ec))
                        || board[lr][lc] != EMPTY) {
                    continue;
                }

                extended = true;

                board[r][c] = EMPTY;
                board[lr][lc] = piece;

                path.add(square(lr, lc));
                taken.add(square(er, ec));

                collectCaptures(lr, lc, path, taken, output);

                taken.remove(taken.size() - 1);
                path.remove(path.size() - 1);

                board[lr][lc] = EMPTY;
                board[r][c] = piece;
            }
        }

        if (!extended && !taken.isEmpty()) {
            output.add(new Move(path, taken));
        }
    }

    /** Joue un coup uniquement s'il figure parmi les coups légaux. */
    public void applyMove(Move requested) {
        if (requested == null || !getLegalMoves().contains(requested))
            throw new IllegalArgumentException("Coup illégal");

        int from = requested.getFrom();
        int to = requested.getTo();
        int piece = board[row(from)][col(from)];

        board[row(from)][col(from)] = EMPTY;
        for (int captured : requested.getCaptured())
            board[row(captured)][col(captured)] = EMPTY;

        int endRow = row(to);
        if (piece == WHITE && endRow == 0) piece = 2;
        if (piece == BLACK && endRow == SIZE - 1) piece = -2;

        board[row(to)][col(to)] = piece;
        currentPlayer = -currentPlayer;
    }

    public boolean isGameOver() {
        return getLegalMoves().isEmpty();
    }

    /** Renvoie le gagnant, ou 0 si la partie n'est pas terminée. */
    public int getWinner() {
        return isGameOver() ? -currentPlayer : 0;
    }
}
