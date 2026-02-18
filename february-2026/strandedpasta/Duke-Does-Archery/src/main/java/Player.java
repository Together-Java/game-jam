import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Player {
    int maximum = 120;
    int power = 120;
    int height = 0;
    private final BufferedImage character;
    private final BufferedImage[] bow = new BufferedImage[5];

    public Player(){
        try {
            this.character = ImageIO.read(new File("images/dukehood.png"));
            for(int i = 0; i < 5; i++){
                String selected = "images/bow_frames/dukehoodbow" + String.valueOf(i+1) + ".png";
                this.bow[i] = ImageIO.read(new File(selected));
            }
        } catch(IOException e){
            throw new RuntimeException("Image not found!");
        }
    }

    public void drawPlayer(Graphics2D graph2, int x, int y){
        graph2.drawImage(character,x,y,240,360,null);
    }

    public void drawBow(Graphics2D graph2, int x, int y, double disx, double disy){
        //Tried having the bow and hand be seperate with a draw method for string. Didn't work.
        //So have image frames for power instead!
        BufferedImage current = bow[(int) Math.floor((double) power /((double) maximum /4))];
        double rotation = Math.toRadians(-70*((double) this.height/maximum));
        double centerWidth = (double) current.getWidth()/2;
        double centerHeight = (double) current.getHeight()/2;
        AffineTransform transform = AffineTransform.getRotateInstance(rotation,centerWidth,centerHeight);
        AffineTransformOp transformOp = new AffineTransformOp(transform,AffineTransformOp.TYPE_BILINEAR);
        graph2.drawImage(transformOp.filter(current,null), (int) (160-(disx*30)), (int) (240+(disy*50)), x, y,null);
    }
}
