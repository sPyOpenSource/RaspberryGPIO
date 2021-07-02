package AI;
/**
 * This is the input class of AI.
 * Everthing including human interdaces
 * 
 * @author X. Wang 
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import AI.Models.Info;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

public class AIInput extends AIBaseInput
{
    /**
     * This is the initialization of AIInput class 
     */
    private final VideoCapture capColorCamera = new VideoCapture();
    private final VideoCapture capDepthCamera = new VideoCapture();
    private final VideoCapture ipCamera = new VideoCapture("http://192.168.1.4:8080/video");
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
        } catch (NullPointerException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        capColorCamera.open(0);
        capDepthCamera.open(2);
        ipCamera.open(0);
        int fourcc = VideoWriter.fourcc('Z', '1', '6', ' ');
        capDepthCamera.set(Videoio.CAP_PROP_FOURCC, fourcc);
        mem.addInfo(new Info(capColorCamera), "the webcam");
        mem.addInfo(new Info(capDepthCamera), "the webcam");
        mem.addInfo(new Info(ipCamera), "the webcam");
    }
    
    private void ReadMessageFromSerial(){
        try {
            mem.addInfo(new Info(in.readLine()), "incomingMessages");         
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void setup() {
        Thread ReadMessageFromSerial = new Thread(){
            @Override
            public void run(){
                while(true)
                    ReadMessageFromSerial();
            }
        };
        //ReadMessageFromSerial.start();
        Thread getImageFromWebcamColorCamera = new Thread(){
            @Override
            public void run(){
                while(true)
                    getImageFromWebcam(capColorCamera, "colorCameraImages");
            }
        };
        //getImageFromWebcamColorCamera.start();
        Thread getImageFromWebcamDepthCamera = new Thread(){
            @Override
            public void run(){
                while(true)
                    getImageFromWebcam(capDepthCamera, "depthCameraImages");
            }
        };
        //getImageFromWebcamDepthCamera.start();
        Thread getImageFromWebcamIPCamera = new Thread(){
            @Override
            public void run(){
                while(true)
                    getImageFromWebcam(ipCamera, "ipCameraImages");
            }
        };
        //getImageFromWebcamIPCamera.start();
    }
}