/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AI.util;

import AI.AIBaseLogic;
import AI.Models.Computer;
import AI.Models.Info;
import AI.Models.WebsocketServer;
import Factorzations.GUI;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Game extends Thread{
    private final int sleep = 50;
    private final int Y = 9;
    private final int X = 16;
    private static int xf = 0, yf = 0, width, height;
    
    public static Robot robot;
    public static WebsocketServer server;
    
    private final Info client;
    private Mat image, old, df;
    //private final VideoCapture file;
    //private final VideoWriter write = new VideoWriter();
    private Rectangle rectangle;
    private static Component component;
    
    public Game(int size, int n){
        client = new Info(new Computer(server.WaitOnConnection()));
        String key = client.Receive();
        //file = new VideoCapture("/home/spy/视频/media/Robots.avi");
        //write.open("/home/spy/视频/media/Robots.mp4", VideoWriter.fourcc('M', 'P', '4','V'), 30d, new Size(512,288));
        //int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        //Rectangle rectangle = new Rectangle(20*16, 20*9);
        rectangle = new Rectangle(size * X, size * Y);
        width = rectangle.width;
        height = rectangle.height;
        image = new Mat(rectangle.height, rectangle.width, CvType.CV_32SC3);
        switch(n){
            case 3:
                rectangle.translate(size * X, 0);
                Thread threadOS = new Thread(){
                    @Override
                    public void run(){
                        while(true){
                            System.out.println(client.Receive());
                        }
                    }
                };
                threadOS.start();
                break;
            case 4:
                rectangle.translate(0, size * Y);
                break;
            case 2:
                rectangle.translate(size * X, 0);
                Thread threadKey = new Thread(){
                    @Override
                    public void run(){
                        System.out.println("Key Thread is Running");
                        while(true){
                            String key = client.Receive();
                            int keyCode;
                            if(key.length() > 1){
                                System.out.println(key);
                                keyCode = Integer.parseInt(client.Receive());
                            } else {
                                keyCode = KeyEvent.getExtendedKeyCodeForChar(key.charAt(0));    
                                client.Receive();
                            }
                            //client.send(new Info(AIBaseLogic.Image2Base64(getScreenShot(component))));

                            robot.keyPress(keyCode);
                            robot.keyRelease(keyCode);
                        }
                    }
                };
                threadKey.start();
                break;
            default:
                Thread threadMouse = new Thread(){
                    @Override
                    public void run(){
                        System.out.println("Mouse Thread is Running");
                        while(true){
                            String mouse = client.Receive();
                            int x = (int)(Double.parseDouble(mouse.split(",")[0]) * width);
                            int y = (int)(Double.parseDouble(mouse.split(",")[1]) * height);
                            robot.mouseMove(x + xf, y + yf);
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                            //client.send(new Info(AIBaseLogic.Image2Base64(getScreenShot(component))));
                        }
                    }
                };
                threadMouse.start();
                break;
        }
    }
    
    public static BufferedImage getScreenShot(
    Component component) {
        BufferedImage image = new BufferedImage(
            component.getWidth(),
            component.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        // call the Component's paint method, using
        // the Graphics object of the image.
        component.paint( image.getGraphics() ); // alternately use .printAll(..)
        return image;
    }
    
    public void Analyse(){
        //file.read(image);
        //System.out.println(image.size());
        //write.write(image);
        old = image.clone();
        image = new Mat(rectangle.height, rectangle.width, CvType.CV_32SC3);
        
        //BufferedImage bufferedImage = AIBaseLogic.Mat2BufferedImage(image);
        BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
        
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        image.put(0, 0, pixels);
        df = new Mat(rectangle.height, rectangle.width, CvType.CV_32SC3);
        Core.subtract(old, image, df);
        double d = df.dot(df);
        
        if(d > 100000)
            client.send(new Info(AIBaseLogic.Image2Base64(bufferedImage)));
        else
            System.out.println(d);
    }
    
    @Override
    public void run(){
        while (true) {
            try {
                Analyse();
                Thread.sleep(sleep);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String... args){
        Game.server = new WebsocketServer(9080);
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /*java.awt.EventQueue.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
            component = gui.getContentPane();
            xf = component.getLocationOnScreen().x;
            yf = component.getLocationOnScreen().y;
            width = gui.getWidth();
            height = gui.getHeight();
        });*/
        try {
            Game.robot = new Robot();
            //Game game1 = new Game(35, 1);
            //game1.start();
            //Game game2 = new Game(35, 2);
            //game2.start();
            Game game3 = new Game(35, 3);
            //game3.start();
            //Game game4 = new Game(35, 4);
            //game4.start();
        } catch (AWTException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
