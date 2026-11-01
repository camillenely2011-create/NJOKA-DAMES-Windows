package com.mbida.njokadames.desktop.ui;

import com.mbida.njokadames.engine.GameEngine;
import com.mbida.njokadames.engine.GameEngine.Move;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class BoardPanel extends JPanel {

    private GameEngine game;
    private int selectedSquare = -1;
    private List<Move> candidates = new ArrayList<>();
    private int pathIndex = 1;
    private boolean inputLocked;

    private final IntConsumer squareHandler;

    public BoardPanel(IntConsumer squareHandler) {
        this.squareHandler = squareHandler;

        setOpaque(false);
        setPreferredSize(new Dimension(620, 620));
        setMinimumSize(new Dimension(400, 400));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (inputLocked || game == null) {
                    return;
                }

                int size = boardSize();
                int cell = size / GameEngine.SIZE;
                int offsetX = (getWidth() - size) / 2;
                int offsetY = (getHeight() - size) / 2;

                int mouseX = e.getX() - offsetX;
                int mouseY = e.getY() - offsetY;

                if (mouseX < 0 || mouseY < 0
                        || mouseX >= size || mouseY >= size) {
                    return;
                }

                int col = mouseX / cell;
                int row = mouseY / cell;

                if (row >= 0 && row < GameEngine.SIZE
                        && col >= 0 && col < GameEngine.SIZE
                        && GameEngine.isPlayable(row, col)) {
                    squareHandler.accept(
                            row * GameEngine.SIZE + col
                    );
                }
            }
        });
    }

    public void setGameState(
            GameEngine game,
            int selectedSquare,
            List<Move> candidates,
            int pathIndex,
            boolean inputLocked) {

        this.game = game;
        this.selectedSquare = selectedSquare;
        this.candidates = candidates == null
                ? new ArrayList<>()
                : new ArrayList<>(candidates);
        this.pathIndex = pathIndex;
        this.inputLocked = inputLocked;

        repaint();
    }

    private int boardSize() {
        int available = Math.min(getWidth(), getHeight()) - 20;
        int cell = Math.max(1, available / GameEngine.SIZE);
        return cell * GameEngine.SIZE;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        int size = boardSize();
        int cell = size / GameEngine.SIZE;

        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        drawShadow(g, x, y, size);

        g.setColor(Theme.BOARD_LIGHT);
        g.fillRoundRect(x, y, size, size, 14, 14);

        for (int row = 0; row < GameEngine.SIZE; row++) {
            for (int col = 0; col < GameEngine.SIZE; col++) {

                int px = x + col * cell;
                int py = y + row * cell;

                boolean dark = GameEngine.isPlayable(row, col);

                g.setColor(dark
                        ? Theme.BOARD_DARK
                        : Theme.BOARD_LIGHT);

                g.fillRect(px, py, cell, cell);

                int square = row * GameEngine.SIZE + col;

                if (square == selectedSquare) {
                    g.setColor(Theme.SELECTION);
                    g.fillRect(px, py, cell, cell);

                    g.setColor(Theme.GOLD_LIGHT);
                    g.setStroke(new BasicStroke(2.5f));
                    g.drawRect(
                            px + 3, py + 3,
                            cell - 6, cell - 6
                    );
                }

                if (isDestination(square)) {
                    int diameter = Math.max(8, cell / 4);

                    g.setColor(Theme.TARGET);
                    g.fillOval(
                            px + (cell - diameter) / 2,
                            py + (cell - diameter) / 2,
                            diameter,
                            diameter
                    );

                    g.setColor(Theme.GOLD_LIGHT);
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawOval(
                            px + (cell - diameter) / 2,
                            py + (cell - diameter) / 2,
                            diameter,
                            diameter
                    );
                }

                if (game != null) {
                    int piece = game.getPiece(row, col);

                    if (piece != GameEngine.EMPTY) {
                        drawPiece(g, px, py, cell, piece);
                    }
                }
            }
        }

        drawBorder(g, x, y, size);
        g.dispose();
    }

    private void drawShadow(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(0, 0, 0, 115));
        g.fillRoundRect(
                x + 7, y + 9,
                size, size,
                18, 18
        );
    }

    private boolean isDestination(int square) {
        for (Move move : candidates) {
            List<Integer> path = move.getPath();

            if (path != null && pathIndex >= 1
                    && path.size() > pathIndex
                    && path.get(pathIndex) == square) {
                return true;
            }
        }

        return false;
    }

    private void drawPiece(
            Graphics2D g,
            int x,
            int y,
            int cell,
            int piece) {

        int margin = Math.max(4, cell / 7);
        int px = x + margin;
        int py = y + margin;
        int diameter = cell - margin * 2;

        // Ombre de la pièce
        g.setColor(new Color(0, 0, 0, 110));
        g.fillOval(
                px + 3, py + 5,
                diameter, diameter
        );

        Color top;
        Color bottom;

        if (piece > 0) {
            top = new Color(255, 255, 238);
            bottom = new Color(190, 183, 151);
        } else {
            top = new Color(76, 91, 75);
            bottom = new Color(12, 22, 16);
        }

        g.setPaint(new GradientPaint(
                px, py, top,
                px + diameter, py + diameter, bottom
        ));

        g.fillOval(px, py, diameter, diameter);

        g.setColor(piece > 0
                ? new Color(255, 255, 255, 190)
                : new Color(190, 155, 75, 210));

        g.setStroke(new BasicStroke(2f));
        g.drawOval(
                px + 2, py + 2,
                diameter - 4, diameter - 4
        );

        // Une dame vaut 2 ou -2.
        if (Math.abs(piece) == 2) {
            drawCrown(g, px, py, diameter, piece);
        }
    }

    private void drawCrown(
            Graphics2D g,
            int x,
            int y,
            int diameter,
            int piece) {

        int cx = x + diameter / 2;
        int cy = y + diameter / 2;

        g.setColor(piece > 0
                ? Theme.FOREST
                : Theme.GOLD_LIGHT);

        g.setFont(new Font(
                "Serif",
                Font.BOLD,
                Math.max(14, diameter / 2)
        ));

        String crown = "♛";
        int width = g.getFontMetrics().stringWidth(crown);

        g.drawString(
                crown,
                cx - width / 2,
                cy + g.getFontMetrics().getAscent() / 3
        );
    }

    private void drawBorder(
            Graphics2D g,
            int x,
            int y,
            int size) {

        g.setColor(Theme.GOLD_LIGHT);
        g.setStroke(new BasicStroke(4f));

        g.drawRoundRect(
                x, y, size, size,
                14, 14
        );
    }
}
