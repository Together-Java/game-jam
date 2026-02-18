package com.solutiongameofficial.swing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@AllArgsConstructor
public class Frame {

    protected static final int WIDTH = 1920;
    protected static final int HEIGHT = 1080;

    private final JFrame FRAME = new JFrame(System.getenv("APP_NAME"));
    private final JPanel CONTENT_PANE = new JPanel() {

        {
            setBackground(Color.BLACK);
            setDoubleBuffered(true);

            setLayout(null);

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (graphicScaleFunction == null) {
                return;
            }

            Graphics2D graphics2D = (Graphics2D) graphics;
            try {
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Dimension currentSize = getSize();
                double scaleX = currentSize.getWidth() / Frame.WIDTH;
                double scaleY = currentSize.getHeight() / Frame.HEIGHT;

                graphicScaleFunction.apply(graphics2D, scaleX, scaleY);
            } finally {
                graphics2D.dispose();
            }
        }
    };

    @Setter(AccessLevel.PROTECTED)
    private GraphicScaleFunction graphicScaleFunction;

    public JRootPane getRootPane() {
        return FRAME.getRootPane();
    }

    protected void repaint() {
        SwingUtilities.invokeLater(() -> {
            CONTENT_PANE.repaint();
            CONTENT_PANE.revalidate();
        });
    }

    {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        FRAME.setSize((int) dimension.getWidth() / 2, (int) dimension.getHeight() / 2);

        FRAME.setExtendedState(JFrame.MAXIMIZED_BOTH);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FRAME.setLocationRelativeTo(null);

        FRAME.setContentPane(CONTENT_PANE);
        FRAME.getContentPane().revalidate();

        FRAME.setVisible(true);
    }
}
