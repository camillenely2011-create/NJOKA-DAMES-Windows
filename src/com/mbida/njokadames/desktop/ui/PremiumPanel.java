package com.mbida.njokadames.desktop.ui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.BasicStroke;

public class PremiumPanel extends JPanel {

    private final boolean highlighted;

    public PremiumPanel() {
        this(false);
    }

    public PremiumPanel(boolean highlighted) {
        this.highlighted = highlighted;

        setOpaque(false);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                18, 18, 18, 18
        ));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        // Ombre extérieure
        g.setColor(new Color(0, 0, 0, 75));

        g.fillRoundRect(
                2, 5,
                width - 4, height - 7,
                Theme.CORNER_RADIUS,
                Theme.CORNER_RADIUS
        );

        // Fond principal
        g.setColor(Theme.SURFACE);

        g.fillRoundRect(
                1, 1,
                width - 3, height - 7,
                Theme.CORNER_RADIUS,
                Theme.CORNER_RADIUS
        );

        // Bordure
        g.setColor(
                highlighted
                ? Theme.GOLD
                : Theme.SURFACE_LIGHT
        );

        g.setStroke(new BasicStroke(
                highlighted ? 1.5f : 1f
        ));

        g.drawRoundRect(
                1, 1,
                width - 3, height - 7,
                Theme.CORNER_RADIUS,
                Theme.CORNER_RADIUS
        );

        g.dispose();

        super.paintComponent(graphics);
    }
}
