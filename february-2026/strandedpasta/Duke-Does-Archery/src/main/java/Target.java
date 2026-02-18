import java.awt.*;

public class Target {
    double targetLocation;
    public Target(double target){
        this.targetLocation = target;
    }
    public void targetFloor(Graphics2D graph, double length, double height){
        graph.setColor(Color.white);
        graph.fillRect((int) ((targetLocation+10)*30 + length - 50), (int) (510+ height),200,30);
        graph.setColor(Color.red);
        graph.fillRect((int) ((targetLocation+10)*30 + length), (int) (510+ height),100,30);
    }
}
