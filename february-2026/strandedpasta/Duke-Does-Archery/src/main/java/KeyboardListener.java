import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {
    private final Player player;
    private final Mainpanel panel;

    public KeyboardListener(Player player, Mainpanel panel){
        this.player = player;
        this.panel = panel;
    }

    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                if(player.height + 5 <= player.maximum && !panel.shot){
                    player.height = player.height + 5;
                }
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S ->{
                if(player.height - 5 >= 0 && !panel.shot){
                    player.height = player.height - 5;
                }
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A ->{
                if(player.power - 5 >= 0 && !panel.shot){
                    player.power = player.power - 5;
                }
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D ->{
                if(player.power + 5 <= player.maximum && !panel.shot){
                    player.power = player.power + 5;
                }
            }
            case KeyEvent.VK_ENTER -> {
                if(!panel.shot) {
                    panel.shot = true;
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
