package AI;
/**
 * This is the input class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */

import AI.Models.Info;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.videoio.VideoCapture;

public class AIInput extends AIBaseInput
{
    /**
     * This is the initialization of AIInput class 
     */
    private final VideoCapture capLeft = new VideoCapture(), capRight = new VideoCapture();
    private BufferedReader in;
    
    /**
     * Constructor for objects of class AIInput
     * @param mem
     */
    public AIInput(AIMemory mem)
    {
        super(mem);
        try {
            in = new BufferedReader(new InputStreamReader(mem.getSerialPort().getInputStream()));
        } catch (IOException |NullPointerException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        capLeft.open(0);
        //capRight.open(1);
    }
    
    private void ReadMessageFromArduino(){
        try {
            mem.addInfo(new Info(in.readLine()),"incomingMessages");         
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void Thread() {
        Thread ReadMessageFromArduino = new Thread(){
            @Override
            public void run(){
                while(true)
                    ReadMessageFromArduino();
            }
        };
        ReadMessageFromArduino.start();
        Thread getImageFromWebcamLeft = new Thread(){
            @Override
            public void run(){
            	int i = 0;
                while(i<100){
                    getImageFromWebcam(capLeft,"leftImages");
                    i++;
                }
            }
        };
        //getImageFromWebcamLeft.start();
        Thread getImageFromWebcamRight = new Thread(){
            @Override
            public void run(){
            	int i = 0;
                while(i<10){
                    getImageFromWebcam(capRight,"rightImages");
                    i++;
                }
            }
        };
        //t4.start();
    }
}