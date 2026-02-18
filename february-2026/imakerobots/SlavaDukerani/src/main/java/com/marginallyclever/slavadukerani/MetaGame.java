package com.marginallyclever.slavadukerani;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/// MetaGame runs multiple instances of [SlavaDukerani] and provides a UX to control them.
public class MetaGame extends JPanel implements FlagChangeListener, GameOverListener {
    private SlavaDukerani game = null;
    private final MetaGameSettingsPanel settingsPanel = new MetaGameSettingsPanel(20,10,30,(int)(Math.random()*1000000));
    static final JFrame frame = new JFrame("Slava Dukerani");
    private final JMenuBar menuBar = new JMenuBar();
    private final JFormattedTextField numMinesLeft = PanelHelper.addNumberFieldInt("Number of mines left", 0);
    private final JTextField timeDisplay = new JTextField("0");
    private final JMenuItem settingsButton = new JMenuItem("Settings");
    private final JMenuItem newGame = new JMenuItem("New Game");
    private final JMenuItem resetGame = new JMenuItem("Restart");
    // Add copy/paste menu items (stubs) for future implementation
    private final JMenuItem pasteBoard = new JMenuItem("Paste Board");
    private Timer timer;
    private long seconds;


    public static void main( String[] args ) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MetaGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public MetaGame() {
        setLayout(new BorderLayout());
        initMenuBar();

        newGame.addActionListener(e -> startNewGame());
        resetGame.addActionListener(e -> resetGame());
        settingsButton.addActionListener(e->showSettingsDialog());
        // wire copy/paste actions to empty stubs
        pasteBoard.addActionListener(e -> pasteBoardAction());

        startNewGame();
    }

    private void showSettingsDialog() {
        int result = JOptionPane.showConfirmDialog(frame, settingsPanel, "Game Settings", JOptionPane.OK_CANCEL_OPTION);
        if(result==JOptionPane.OK_OPTION) {
            startNewGame();
        }
    }

    private void startNewGame() {
        settingsPanel.newSeed();
        resetGame();
    }

    private void resetGame() {
        int totalMines = settingsPanel.getMines();
        numMinesLeft.setValue(totalMines);
        if (game != null) {
            game.removeFlagChangeListener(this);
            game.setRequestFocusEnabled(false);
        }
        game = new SlavaDukerani(
                settingsPanel.getBoardWidth(),
                settingsPanel.getBoardHeight(),
                settingsPanel.getSeed(),
                settingsPanel.getMines());
        startGame(game);
    }

    private void startGame(SlavaDukerani game) {
        removeAll();
        var pane = new JScrollPane(game);
        Dimension max = getSingleScreenSize(0.9f);
        pane.setMaximumSize(max);
        add(pane, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        revalidate();

        game.addFlagChangeListener(this);
        game.addGameOverListener(this);
        startNewTimer();

        game.setFocusable(true);
        // set focus on the game.
        game.setRequestFocusEnabled(true);
        // Request focus on the EDT to ensure keyboard events go to the game component.
        SwingUtilities.invokeLater(() -> {
            // prefer requestFocusInWindow; if that fails, fall back to requestFocus
            if (!game.requestFocusInWindow()) {
                game.requestFocus();
            }
        });

        timer.start();
    }

    private Dimension getSingleScreenSize(float scale) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        if (devices.length > 0) {
            DisplayMode dm = devices[0].getDisplayMode();
            return new Dimension(
                    (int)(dm.getWidth()*scale),
                    (int)(dm.getHeight()*scale));
        }
        // fallback to a default size if no screens are detected
        return new Dimension(800, 600);
    }

    private void startNewTimer() {
        // stop any existing timer to avoid multiple timers running concurrently
        if (timer != null) {
            timer.stop();
        }
        seconds = 0;
        timer = new Timer(1000, e -> {
            //System.out.println(seconds%2==0?"tick":"tock");
            seconds++;
            long h = seconds / 3600;
            long m = (seconds % 3600) / 60;
            long s = seconds % 60;
            StringBuilder sb = new StringBuilder();
            if(h>0) sb.append(String.format("%02d",h)).append(":");
            if(h>0||m>0) sb.append(String.format("%02d",m)).append(":");
            sb.append(String.format("%02d",s));
            timeDisplay.setText(sb.toString());
        });
    }

    private void initMenuBar() {
        frame.setJMenuBar(menuBar);
        JMenu menu = new JMenu("Game");
        menuBar.add(menu);
        menu.add(settingsButton);
        menu.add(newGame);
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        menu.add(resetGame);
        resetGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        // add Copy/Paste menu items (stubs)
        menu.add(pasteBoard);
        pasteBoard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

        menu.add(new JSeparator());
        var exitItem = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(exitItem);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

        menu = new JMenu("Help");
        menuBar.add(menu);
        menu.add(new AbstractAction("Website") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("https://github.com/i-make-robots/SlavaDukerani/"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        menu.add(new AbstractAction("About") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame,
                        "Slava Dukerani\n" +
                                "A Minesweeper/Sokoban crossover event\n"+
                                "by Marginally Clever Software\n" +
                                "https://github.com/i-make-robots/SlavaDukerani/\n\n"+
                                "WASD/Arrow keys to move and push your sensor equipment.\n" +
                                "Q/Mouse left click to reveal hidden tiles.\n" +
                                "E/Mouse right click to flag/unflag mines.\n" +
                                "Reach the bottom right corner to win."
                        );

            }
        });

        menuBar.add(numMinesLeft);
        menuBar.add(timeDisplay);
        numMinesLeft.setEditable(false);
        numMinesLeft.setFocusable(false);
        timeDisplay.setEditable(false);
        timeDisplay.setFocusable(false);

        // right justified
        numMinesLeft.setHorizontalAlignment(JTextField.RIGHT);
        timeDisplay.setHorizontalAlignment(JTextField.RIGHT);
    }

    @Override
    public void flagCountChanged(int flagCount) {
        numMinesLeft.setValue(settingsPanel.getMines()- flagCount);
    }

    @Override
    public void gameOver(boolean won) {
        timer.stop();
    }

    // Stub for paste action; to be implemented later.
    private void pasteBoardAction() {
        // read clipboard to String
        try {
            String clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
            // attempt to parse clipboard as a board and start a new game with it
            startGame(new SlavaDukerani(clipboard));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to parse board from clipboard. Please ensure the clipboard contains a valid board string.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
