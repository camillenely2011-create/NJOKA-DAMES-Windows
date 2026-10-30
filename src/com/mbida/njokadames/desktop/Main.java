package com.mbida.njokadames.desktop;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;
import com.mbida.njokadames.ai.DraughtsAI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Main extends JFrame {

    private GameEngine game = new GameEngine();
    private final DraughtsAI ai = new DraughtsAI();

    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel status = new JLabel("À vous de jouer — Blancs");
    private final JLabel pieces = new JLabel();

    private final JComboBox<String> mode = new JComboBox<>(
            new String[]{"Deux joueurs", "Contre l'IA"}
    );

    private final JComboBox<String> difficulty = new JComboBox<>(
            new String[]{"Facile", "Moyen", "Difficile", "Expert", "Maître"}
    );

    private List<Move> candidates = new ArrayList<>();
    private int selectedSquare = -1;
    private int pathIndex = 1;
    private boolean aiThinking = false;

    private static final Color FOREST = new Color(24, 75, 53);
    private static final Color GOLD = new Color(205, 164, 75);
    private static final Color IVORY = new Color(244, 235, 211);
    private static final Color DARK = new Color(105, 75, 54);

    public Main() {
        super("NJOKA DAMES — Dames internationales");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 760));
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(IVORY);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FOREST);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("NJOKA DAMES");
        title.setForeground(GOLD);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitle = new JLabel("DAMES INTERNATIONALES • 10 × 10");
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel heading = new JPanel(new GridLayout(2, 1));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);
        header.add(heading, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        center.add(boardPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(IVORY);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 18, 18, 18));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controls.setOpaque(false);

        JButton newGame = new JButton("Nouvelle partie");
        newGame.addActionListener(e -> newGame());

        mode.addActionListener(e -> {
            difficulty.setEnabled(mode.getSelectedIndex() == 1);
            newGame();
        });

        difficulty.setEnabled(false);

        controls.add(new JLabel("Mode :"));
        controls.add(mode);
        controls.add(new JLabel("Niveau IA :"));
        controls.add(difficulty);
        controls.add(newGame);

        status.setFont(new Font("SansSerif", Font.BOLD, 16));
        status.setForeground(FOREST);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        pieces.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pieces.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottom.add(controls);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(status);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(pieces);

        add(bottom, BorderLayout.SOUTH);

        updateInformation();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void newGame() {
        game = new GameEngine();
        candidates.clear();
        selectedSquare = -1;
        pathIndex = 1;
        aiThinking = false;

        status.setText("Nouvelle partie — Blancs commencent");
        updateInformation();
        boardPanel.repaint();
    }

    private void updateInformation() {
        pieces.setText(
                "Pièces blanches : " + game.countPieces(GameEngine.WHITE)
                + "     |     Pièces noires : "
                + game.countPieces(GameEngine.BLACK)
        );
    }

    private int selectedDifficulty() {
        switch (difficulty.getSelectedIndex()) {
            case 0: return DraughtsAI.DIFFICULTY_EASY;
            case 1: return DraughtsAI.DIFFICULTY_MEDIUM;
            case 2: return DraughtsAI.DIFFICULTY_HARD;
            case 3: return DraughtsAI.DIFFICULTY_EXPERT;
            case 4: return DraughtsAI.DIFFICULTY_MASTER;
            default: return DraughtsAI.DIFFICULTY_MEDIUM;
        }
    }

    private void handleSquare(int square) {
        if (aiThinking || game.isGameOver()) {
            return;
        }

        if (mode.getSelectedIndex() == 1
                && game.getCurrentPlayer() == GameEngine.BLACK) {
            return;
        }

        if (selectedSquare >= 0) {
            List<Move> matching = new ArrayList<>();

            for (Move move : candidates) {
                List<Integer> path = move.getPath();

                if (path.size() > pathIndex
                        && path.get(pathIndex) == square) {
                    matching.add(move);
                }
            }

            if (!matching.isEmpty()) {
                candidates = matching;
                pathIndex++;

                boolean hasContinuation = false;

                for (Move move : candidates) {
                    if (move.getPath().size() > pathIndex) {
                        hasContinuation = true;
                        break;
                    }
                }

                if (!hasContinuation) {
                    applySelectedMove(candidates.get(0));
                } else {
                    status.setText("Continuez votre prise...");
                    boardPanel.repaint();
                }
                return;
            }
        }

        selectPiece(square);
    }

    private void selectPiece(int square) {
        int row = square / GameEngine.SIZE;
        int col = square % GameEngine.SIZE;
        int piece = game.getPiece(row, col);

        if (piece == GameEngine.EMPTY
                || Integer.signum(piece) != game.getCurrentPlayer()) {
            selectedSquare = -1;
            candidates.clear();
            boardPanel.repaint();
            return;
        }

        List<Move> legal = game.getLegalMoves();
        candidates = new ArrayList<>();

        for (Move move : legal) {
            if (move.getFrom() == square) {
                candidates.add(move);
            }
        }

        if (candidates.isEmpty()) {
            selectedSquare = -1;
            status.setText("Aucun coup légal pour cette pièce.");
        } else {
            selectedSquare = square;
            pathIndex = 1;
            status.setText("Sélectionnez une case de destination.");
        }

        boardPanel.repaint();
    }

    private void applySelectedMove(Move move) {
        game.applyMove(move);

        selectedSquare = -1;
        candidates.clear();
        pathIndex = 1;

        updateInformation();
        boardPanel.repaint();

        if (game.isGameOver()) {
            showWinner();
            return;
        }

        if (mode.getSelectedIndex() == 1
                && game.getCurrentPlayer() == GameEngine.BLACK) {
            startAI();
        } else {
            status.setText(
                    game.getCurrentPlayer() == GameEngine.WHITE
                    ? "Au tour des Blancs"
                    : "Au tour des Noirs"
            );
        }
    }

    private void startAI() {
        aiThinking = true;
        status.setText("L'IA réfléchit...");
        boardPanel.repaint();

        SwingWorker<Move, Void> worker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                return ai.chooseMove(game, selectedDifficulty());
            }

            @Override
            protected void done() {
                try {
                    Move move = get();

                    if (move != null && !game.isGameOver()) {
                        game.applyMove(move);
                    }

                    aiThinking = false;
                    updateInformation();
                    boardPanel.repaint();

                    if (game.isGameOver()) {
                        showWinner();
                    } else {
                        status.setText("À vous de jouer — Blancs");
                    }

                } catch (Exception ex) {
                    aiThinking = false;
                    status.setText("Erreur pendant le calcul de l'IA.");
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void showWinner() {
        int winner = game.getWinner();

        String message = winner == GameEngine.WHITE
                ? "Les Blancs remportent la partie !"
                : "Les Noirs remportent la partie !";

        status.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Fin de partie",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private final class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(620, 620));
            setBackground(IVORY);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int size = Math.min(getWidth(), getHeight());
                    int cell = size / GameEngine.SIZE;

                    int offsetX = (getWidth() - size) / 2;
                    int offsetY = (getHeight() - size) / 2;

                    int col = (e.getX() - offsetX) / cell;
                    int row = (e.getY() - offsetY) / cell;

                    if (row >= 0 && row < 10 && col >= 0 && col < 10) {
                        handleSquare(row * 10 + col);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int size = Math.min(getWidth(), getHeight());
            int cell = size / GameEngine.SIZE;
            int offsetX = (getWidth() - size) / 2;
            int offsetY = (getHeight() - size) / 2;

            Set<Integer> targets = new HashSet<>();

            for (Move move : candidates) {
                if (move.getPath().size() > pathIndex) {
                    targets.add(move.getPath().get(pathIndex));
                }
            }

            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    int x = offsetX + c * cell;
                    int y = offsetY + r * cell;

                    boolean playable = GameEngine.isPlayable(r, c);
                    g.setColor(playable ? DARK : IVORY);
                    g.fillRect(x, y, cell, cell);

                    int square = r * 10 + c;

                    if (square == selectedSquare) {
                        g.setColor(GOLD);
                        g.setStroke(new BasicStroke(4));
                        g.drawRect(x + 3, y + 3, cell - 6, cell - 6);
                    }

                    if (targets.contains(square)) {
                        g.setColor(new Color(110, 190, 110, 190));
                        g.fillOval(
                                x + cell / 3,
                                y + cell / 3,
                                cell / 3,
                                cell / 3
                        );
                    }

                    int piece = game.getPiece(r, c);

                    if (piece != GameEngine.EMPTY) {
                        int margin = Math.max(4, cell / 9);
                        int px = x + margin;
                        int py = y + margin;
                        int diameter = cell - 2 * margin;

                        g.setColor(
                                piece > 0
                                ? new Color(245, 242, 226)
                                : new Color(35, 38, 36)
                        );

                        g.fillOval(px, py, diameter, diameter);

                        g.setColor(piece > 0 ? GOLD : new Color(150, 150, 150));
                        g.setStroke(new BasicStroke(2));
                        g.drawOval(px + 2, py + 2,
                                diameter - 4, diameter - 4);

                        if (Math.abs(piece) == 2) {
                            g.setColor(piece > 0 ? FOREST : GOLD);
                            g.setFont(new Font("SansSerif", Font.BOLD,
                                    Math.max(12, cell / 3)));
                            FontMetrics fm = g.getFontMetrics();
                            String crown = "♛";
                            g.drawString(
                                    crown,
                                    x + (cell - fm.stringWidth(crown)) / 2,
                                    y + (cell + fm.getAscent() - fm.getDescent()) / 2
                            );
                        }
                    }
                }
            }

            g.setColor(FOREST);
            g.setStroke(new BasicStroke(3));
            g.drawRect(offsetX, offsetY, size, size);

            g.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
