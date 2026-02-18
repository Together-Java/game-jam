import javax.swing.*;

public class Mainline {
    private static final int UPDATE_DELAY = 150;
    public static void main(String[] arg){

        Player player = new Player();
        Target target = new Target(10 + Math.random()*70);
        Mainpanel panel = new Mainpanel(player,target);
                ;

        //Resolution (960, 720 by default)
        int width = 960;
        int height = 720;

        //Jfarme settings
        JFrame frame = new JFrame("Duke the Archer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width,height);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.add(panel);
        frame.addKeyListener(new KeyboardListener(player, panel));
        frame.setVisible(true);

        //Thread
        new Thread(() -> {
            while (!panel.gameEnd) {
                panel.update();
                panel.repaint();
                try {
                    Thread.sleep(UPDATE_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.exit(0);
        }).start();
    }
}
