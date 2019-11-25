package AI;

import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This is a class initialize an artificial intelligence service.
 * 
 * @author X. Wang
 * @version 1.0
 */
public final class AI 
{
    // instance variables
    private final AIMemory mem = new AIMemory();
    private final AIInput  inp;
    private final AILogic  log;
    private final AIOutput oup;
    private final Thread   logThread, inpThread, oupThread;
    
    /**
     * Constructor for objects of class AI
     */
    public AI()
    {
        // Initialize instance variables
        mem.setLogPath("/home/spy/AI/");
        inp = new AIInput(mem);
        log = new AILogic(mem);
        oup = new AIOutput(mem);
	logThread = new Thread(log);
        inpThread = new Thread(inp);
        oupThread = new Thread(oup);
    }
    
    public static void main(String[] args) {
        System.load("/home/spy/Source/C/opencv/build/lib/libopencv_java412.so");
       SwingUtilities.invokeLater(() -> {
           VideoPanel panel = new VideoPanel();
           JFrame frame = new JFrame("Video");
           frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
           frame.getContentPane().add(panel);
           frame.setResizable(false);
           frame.pack();
           frame.setLocationRelativeTo(null);
           frame.setVisible(true);
        });
              
    }
    
    public void start()
    {
    	logThread.start();
        inpThread.start(); 
        oupThread.start();
    }
    
    public BufferedImage getImage(String camera){
        return oup.Mat2BufferedImage(camera);
    }
}