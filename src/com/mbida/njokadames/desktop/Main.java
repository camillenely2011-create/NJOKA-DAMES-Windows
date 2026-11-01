package com.mbida.njokadames.desktop;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;
import com.mbida.njokadames.ai.DraughtsAI;
import com.mbida.njokadames.desktop.ui.Theme;
import com.mbida.njokadames.desktop.ui.BoardPanel;
import com.mbida.njokadames.desktop.ui.PremiumButton;
import com.mbida.njokadames.desktop.ui.PremiumPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Main extends JFrame {

    private GameEngine game = new GameEngine();

    private final DraughtsAI ai = new DraughtsAI();

    private final BoardPanel boardPanel;

    private final JLabel status = new JLabel("À vous de jouer — Blancs");
    private final JLabel pieces = new JLabel();
    private final JLabel turnIndicator = new JLabel("BLANCS");

    private final JComboBox<String> mode = new JComboBox<>(
            new String[]{"Deux joueurs", "Contre l'IA"}
    );

    private final JComboBox<String> difficulty = new JComboBox<>(
            new String[]{
                "Facile", "Moyen", "Difficile", "Expert", "Maître"
            }
    );

    private List<Move> candidates = new ArrayList<>();

    private int selectedSquare = -1;
    private int pathIndex = 1;
    private boolean aiThinking = false;

    public Main() {
        super("NJOKA DAMES — Édition Premium");

        boardPanel = new BoardPanel(this::handleSquare);

        configureWindow();
        buildInterface();
        updateInformation();
        refreshBoard();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 760));
        setSize(1180, 880);

        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(14, 14));
    }

    private void buildInterface() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        PremiumPanel header = new PremiumPanel(true);
        header.setLayout(new BorderLayout(15, 5));
        header.setBorder(new EmptyBorder(12, 22, 12, 22));

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("NJOKA DAMES");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(Theme.GOLD_LIGHT);

        JLabel subtitle = new JLabel(
                "DAMES INTERNATIONALES  •  PLATEAU 10 × 10"
        );
        subtitle.setFont(Theme.SMALL);
        subtitle.setForeground(Theme.MUTED);

        identity.add(title);
        identity.add(Box.createVerticalStrut(4));
        identity.add(subtitle);

        JLabel edition = new JLabel("ÉDITION PREMIUM");
        edition.setFont(Theme.BUTTON);
        edition.setForeground(Theme.GOLD);

        header.add(identity, BorderLayout.WEST);
        header.add(edition, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 16, 0, 16));
        wrapper.add(header, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 16, 0, 16));

        PremiumPanel boardCard = new PremiumPanel(true);
        boardCard.setLayout(new BorderLayout());
        boardCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        boardPanel.setPreferredSize(new Dimension(640, 640));
        boardCard.add(boardPanel, BorderLayout.CENTER);

        content.add(boardCard, BorderLayout.CENTER);
        content.add(buildSidebar(), BorderLayout.EAST);

        return content;
    }

    private JPanel buildSidebar() {
        PremiumPanel sidebar = new PremiumPanel(false);
        sidebar.setPreferredSize(new Dimension(275, 640));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 18, 20, 18));

        JLabel section = makeLabel("CENTRE DE CONTRÔLE", Theme.GOLD);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(18));

        sidebar.add(makeLabel("TOUR DE JEU", Theme.MUTED));
        sidebar.add(Box.createVerticalStrut(7));

        turnIndicator.setFont(new Font("SansSerif", Font.BOLD, 23));
        turnIndicator.setForeground(Theme.GOLD_LIGHT);
        turnIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(turnIndicator);

        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(makeLabel("MODE DE JEU", Theme.MUTED));
        sidebar.add(Box.createVerticalStrut(7));

        styleCombo(mode);
        sidebar.add(mode);

        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(makeLabel("DIFFICULTÉ DE L'IA", Theme.MUTED));
        sidebar.add(Box.createVerticalStrut(7));

        styleCombo(difficulty);
        difficulty.setEnabled(false);
        sidebar.add(difficulty);

        sidebar.add(Box.createVerticalStrut(22));

        PremiumButton newGameButton =
                new PremiumButton("NOUVELLE PARTIE");

        newGameButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        newGameButton.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 48
        ));

        newGameButton.addActionListener(e -> newGame());
        sidebar.add(newGameButton);

        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(makeLabel("STATISTIQUES", Theme.GOLD));
        sidebar.add(Box.createVerticalStrut(10));

        pieces.setFont(Theme.BODY);
        pieces.setForeground(Theme.TEXT);
        pieces.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(pieces);

        sidebar.add(Box.createVerticalStrut(20));

        PremiumPanel rules = new PremiumPanel(false);
        rules.setLayout(new BoxLayout(rules, BoxLayout.Y_AXIS));
        rules.setAlignmentX(Component.LEFT_ALIGNMENT);
        rules.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 150
        ));

        JLabel rulesTitle = makeLabel("RÈGLES", Theme.GOLD);
        JLabel rulesText = new JLabel(
                "<html><div style='width:190px'>"
                + "• Prises obligatoires<br>"
                + "• Prise du maximum de pièces<br>"
                + "• Dames volantes<br>"
                + "• Prises multiples"
                + "</div></html>"
        );

        rulesText.setForeground(Theme.MUTED);
        rulesText.setFont(Theme.SMALL);

        rules.add(rulesTitle);
        rules.add(Box.createVerticalStrut(9));
        rules.add(rulesText);

        sidebar.add(rules);

        sidebar.add(Box.createVerticalGlue());

        JLabel signature = makeLabel(
                "NJOKA  •  STRATÉGIE  •  EXCELLENCE",
                Theme.GOLD_DARK
        );
        signature.setFont(new Font("SansSerif", Font.BOLD, 9));
        sidebar.add(signature);

        mode.addActionListener(e -> {
            difficulty.setEnabled(mode.getSelectedIndex() == 1);
            newGame();
        });

        return sidebar;
    }

    private JPanel buildFooter() {
        PremiumPanel footer = new PremiumPanel(false);
        footer.setLayout(new BorderLayout(12, 5));
        footer.setBorder(new EmptyBorder(10, 18, 10, 18));

        status.setFont(Theme.BODY);
        status.setForeground(Theme.GOLD_LIGHT);

        JLabel help = new JLabel(
                "Sélectionnez une pièce puis sa destination."
        );
        help.setFont(Theme.SMALL);
        help.setForeground(Theme.MUTED);

        footer.add(status, BorderLayout.WEST);
        footer.add(help, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 16, 12, 16));
        wrapper.add(footer, BorderLayout.CENTER);

        return wrapper;
    }

    private JLabel makeLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(Theme.BUTTON);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(Theme.BODY);
        combo.setForeground(Theme.TEXT);
        combo.setBackground(Theme.SURFACE_LIGHT);
        combo.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 42
        ));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void refreshBoard() {
        boolean locked = aiThinking
                || game.isGameOver()
                || (mode.getSelectedIndex() == 1
                && game.getCurrentPlayer() == GameEngine.BLACK);

        boardPanel.setGameState(
                game,
                selectedSquare,
                candidates,
                pathIndex,
                locked
        );
    }

    private void newGame() {
        game = new GameEngine();

        candidates.clear();
        selectedSquare = -1;
        pathIndex = 1;
        aiThinking = false;

        status.setText("Nouvelle partie — Les Blancs commencent.");

        updateInformation();
        refreshBoard();
    }

    private void updateInformation() {
        int white = game.countPieces(GameEngine.WHITE);
        int black = game.countPieces(GameEngine.BLACK);

        pieces.setText(
                "<html><div style='line-height:1.8'>"
                + "Blancs : <b>" + white + "</b><br>"
                + "Noirs : <b>" + black + "</b>"
                + "</div></html>"
        );

        turnIndicator.setText(
                game.getCurrentPlayer() == GameEngine.WHITE
                ? "BLANCS"
                : "NOIRS"
        );

        turnIndicator.setForeground(
                game.getCurrentPlayer() == GameEngine.WHITE
                ? Theme.GOLD_LIGHT
                : Theme.MUTED
        );
    }

    private int selectedDifficulty() {
        switch (difficulty.getSelectedIndex()) {
            case 0:
                return DraughtsAI.DIFFICULTY_EASY;
            case 1:
                return DraughtsAI.DIFFICULTY_MEDIUM;
            case 2:
                return DraughtsAI.DIFFICULTY_HARD;
            case 3:
                return DraughtsAI.DIFFICULTY_EXPERT;
            case 4:
                return DraughtsAI.DIFFICULTY_MASTER;
            default:
                return DraughtsAI.DIFFICULTY_MEDIUM;
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
                    status.setText("Continuez votre prise multiple...");
                    refreshBoard();
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
            refreshBoard();
            return;
        }

        candidates = new ArrayList<>();

        for (Move move : game.getLegalMoves()) {
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
            status.setText("Choisissez une destination.");
        }

        refreshBoard();
    }

    private void applySelectedMove(Move move) {
        game.applyMove(move);

        selectedSquare = -1;
        candidates.clear();
        pathIndex = 1;

        updateInformation();
        refreshBoard();

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
                    ? "Au tour des Blancs."
                    : "Au tour des Noirs."
            );
        }
    }

    private void startAI() {
        aiThinking = true;
        status.setText("L'intelligence artificielle réfléchit...");
        refreshBoard();

        final GameEngine thinkingGame = game;
        final int difficultyLevel = selectedDifficulty();

        SwingWorker<Move, Void> worker =
                new SwingWorker<Move, Void>() {

            @Override
            protected Move doInBackground() {
                return ai.chooseMove(
                        thinkingGame,
                        difficultyLevel
                );
            }

            @Override
            protected void done() {
                // Une nouvelle partie a pu être lancée entre-temps.
                if (game != thinkingGame) {
                    return;
                }

                try {
                    Move move = get();

                    if (move != null && !game.isGameOver()) {
                        game.applyMove(move);
                    }

                    aiThinking = false;
                    updateInformation();
                    refreshBoard();

                    if (game.isGameOver()) {
                        showWinner();
                    } else {
                        status.setText("À vous de jouer — Blancs.");
                    }

                } catch (Exception ex) {
                    aiThinking = false;
                    status.setText(
                            "Erreur pendant le calcul de l'IA."
                    );
                    refreshBoard();
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
        refreshBoard();

        JOptionPane.showMessageDialog(
                this,
                message,
                "Fin de partie",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
