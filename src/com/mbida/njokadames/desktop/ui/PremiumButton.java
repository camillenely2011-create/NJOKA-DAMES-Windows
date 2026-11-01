package com.mbida.njokadames.desktop.ui;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PremiumButton extends JButton {

    private final boolean primary;

    private float hoverProgress = 0f;
    private float targetProgress = 0f;

    private final Timer animation;

    public PremiumButton(String text) {
        this(text, true);
    }

    public PremiumButton(String text, boolean primary) {
        super(text);

        this.primary = primary;

        setFont(Theme.BUTTON);
        setForeground(primary ? Theme.BACKGROUND : Theme.GOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(false);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                12, 22, 12, 22
        ));

        animation = new Timer(16, e -> {
            float difference = targetProgress - hoverProgress;

            if (Math.abs(difference) < 0.015f) {
                hoverProgress = targetProgress;
                animationStop();
            } else {
                hoverProgress += difference * 0.24f;
            }

            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                targetProgress = 1f;
                if (!animation.isRunning()) {
                    animation.start();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                targetProgress = 0f;
                if (!animation.isRunning()) {
                    animation.start();
                }
            }
        });
    }

    private void animationStop() {
        animation.stop();
    }

    private Color blend(Color first, Color second, float amount) {
        float inverse = 1f - amount;

        return new Color(
                (int) (first.getRed() * inverse
                        + second.getRed() * amount),
                (int) (first.getGreen() * inverse
                        + second.getGreen() * amount),
                (int) (first.getBlue() * inverse
                        + second.getBlue() * amount)
        );
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

        boolean pressed = getModel().isPressed();

        Color start;
        Color end;

        if (primary) {
            start = blend(
                    Theme.GOLD,
                    Theme.GOLD_LIGHT,
                    hoverProgress
            );

            end = blend(
                    Theme.GOLD_DARK,
                    Theme.GOLD,
                    hoverProgress
            );
        } else {
            start = blend(
                    Theme.SURFACE_LIGHT,
                    Theme.FOREST,
                    hoverProgress
            );

            end = blend(
                    Theme.SURFACE,
                    Theme.SURFACE_LIGHT,
                    hoverProgress
            );
        }

        if (pressed) {
            start = blend(start, Theme.BACKGROUND, 0.15f);
            end = blend(end, Theme.BACKGROUND, 0.15f);
        }

        // Ombre
        g.setColor(new Color(0, 0, 0, 65));
        g.fillRoundRect(
                1, 4, width - 2, height - 3,
                Theme.CORNER_RADIUS,
                Theme.CORNER_RADIUS
        );

        // Dégradé
        g.setPaint(new GradientPaint(
                0, 0, start,
                width, height, end
        ));

        g.fillRoundRect(
                1, 1, width - 2, height - 5,
                Theme.CORNER_RADIUS,
                Theme.CORNER_RADIUS
        );

        // Bordure
        g.setColor(primary
                ? Theme.GOLD_LIGHT
                : Theme.GOLD_DARK);

        g.setStroke(new BasicStroke(1f));

        g.drawRoundRect(
                1, 1, width - 3, height - 6,
                Theme.CORNER_RADIUS,
                Theme.CORNER_RADIUS
        );

        g.dispose();

        super.paintComponent(graphics);
    }
}
