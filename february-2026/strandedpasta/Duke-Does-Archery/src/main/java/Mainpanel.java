import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Mainpanel extends JPanel {

    private final Player player;
    private final Target target;
    private int tries = 4;
    double shootRate = 0.4;
    double trejectory = 0;
    boolean fallCheck = false;
    boolean gameEnd = false;
    boolean shot = false;
    BufferedImage background;

    public Mainpanel(Player player, Target target) {
        this.player = player;
        this.target = target;
        this.setBackground(Color.BLACK);
        try {
            this.background = ImageIO.read(new File("images/field.png"));
        } catch(IOException e){
            throw new RuntimeException("Image not found!");
        }
    }

    public void checkHit(){
        double incrament = 0.05;
        double formula = 0.05;
        double savedIncrament = 0;
        double shootRate = 0.4;
        while (formula > 0) {
            if (formula > .55 || incrament == 0.05) {
                formula = (Math.sin(incrament / ((double) (player.power + (player.height/6) + 1) / 6))) * ((double) (player.power + 1) / 6) + .6;
                incrament = incrament + 0.05;
            }else{
                if (savedIncrament == 0) {
                    savedIncrament = incrament;
                }
                formula = ((Math.sin(savedIncrament / ((double) (player.power + (player.height/6) + 1) / 6))) * ((double) (player.power + 1) / 6) + .6) - shootRate;
                incrament = incrament + 0.05;
                shootRate = shootRate + 0.4;
            }
        }
        System.out.println(target.targetLocation);
        System.out.println(incrament+10);
        if (incrament+10 < target.targetLocation-3 || incrament+10 > target.targetLocation+3){
            tries--;
            if (tries == 0){
                gameEnd = true;
            }
        }else{
            gameEnd = true;
        }
    }

    public double shot(double inc){
        if(Math.sin(inc / ((double) (player.power + (player.height/6) + 1) / 6)) * ((double) (player.height + 1) / 6) + .6 > .551 && !fallCheck) {
            return (Math.sin(inc / ((double) (player.power + (player.height/6) + 1) / 6)) * ((double) (player.height + 1) / 6) + .6);
        }else{
            fallCheck = true;
            shootRate = shootRate + 0.4;
            return ((Math.sin(.55 / ((double) (player.power + (player.height/6) + 1) / 6))) * ((double) (player.height + 1) / 6) + .6) - shootRate;
        }
    }

    public void update(){
    }

    @Override
    public void paintComponent(Graphics graph){
        super.paintComponent(graph);
        Graphics2D graph2 = (Graphics2D) graph;

        if(!this.shot){
            graph2.drawImage(background, 0, -getHeight()*2 + 180, null);

            player.drawPlayer(graph2, getWidth() / 10, getHeight() / 4 + 12);
            player.drawBow(graph2, getWidth() / 6, getHeight() / 3 + 12,0,0);

            target.targetFloor(graph2,0,0);

            graph2.setColor(Color.black);
            graph2.fillRect(0, (getHeight()-(getHeight()/7)), getWidth(),getHeight());

            graph.setColor(Color.red);
            for(int i = 0; i < this.tries; i++){
                graph2.fillRect(getWidth()-getWidth()/6-i*getWidth()/10,getHeight()-(getHeight()/13)-20, 50,50);
            }

            graph2.scale(3, 3);
            graph2.setColor(Color.lightGray);

            graph2.drawString("Height : " + String.valueOf(player.height),(getWidth()/10)/3,(getHeight()-(getHeight()/13))/3);
            graph2.drawString("Power : " + String.valueOf(player.power),(getWidth()/10)/3,(getHeight()-(getHeight()/13)+40)/3);
        }

        if(this.shot){
            double formula = shot(trejectory);
            graph2.drawImage(background,(int) -(trejectory*30), (int) (-getHeight()*2 + 180 + (formula*50)), null);

            player.drawPlayer(graph2, (int) ((double) getWidth() / 10 - (trejectory*30)), (int) (((double) getHeight() / 4) + 12 + (formula*50)));
            player.drawBow(graph2, getWidth() / 6, (getHeight() / 3) + 12,trejectory,formula);

            graph2.setColor(Color.orange);
            graph2.fillRect(195, 300, 50, 50);

            target.targetFloor(graph2,-(trejectory*30),formula*50);

            graph2.setColor(Color.black);
            graph2.fillRect(0, (getHeight()-(getHeight()/7)), getWidth(),getHeight());

            graph.setColor(Color.red);
            for(int i = 0; i < this.tries; i++){
                graph2.fillRect(getWidth()-getWidth()/6-i*getWidth()/10,getHeight()-(getHeight()/13)-20, 50,50);
            }

            graph2.scale(3, 3);
            graph2.setColor(Color.lightGray);

            graph2.drawString("Height : " + String.valueOf(player.height),(getWidth()/10)/3,(getHeight()-(getHeight()/13))/3);
            graph2.drawString("Power : " + String.valueOf(player.power),(getWidth()/10)/3,(getHeight()-(getHeight()/13)+40)/3);

            trejectory = trejectory + 1;

            if((((double) getHeight() / 4) + 12 + (formula*50)) < -10){
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                shootRate = 0;
                trejectory = 0;
                fallCheck = false;
                this.checkHit();
                this.shot = !this.shot;
            }
        }
    }
}
