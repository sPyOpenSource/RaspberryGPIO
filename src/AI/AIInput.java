package AI;
/**
 * This is the input class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */

import AI.Models.Info;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.core.Core;

public class AIInput implements Runnable
{
    /**
     * This is the initialization of AIInput class 
     */
    private final AIMemory mem;
    private final VideoCapture capLeft = new VideoCapture(); 
    private final VideoCapture capRight = new VideoCapture();
    private final int bufferSize;
    private BufferedReader in;
    
    /**
     * Constructor for objects of class AIInput
     * @param mem
     */
    public AIInput(AIMemory mem)
    {
    	this.mem = mem;
        bufferSize = 1024;
        try {
            in = new BufferedReader(new InputStreamReader(mem.getSerialPort().getInputStream()));
        } catch (IOException |NullPointerException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        capLeft.open(0);
        capRight.open(1);
        capRight.set(Videoio.CAP_PROP_MODE,Videoio.CAP_MODE_GRAY);
        //capRight.set(Videoio.CAP_PROP_CONVERT_RGB,0);
    }
    
    private void getImageFromWebcam(VideoCapture cap, String images){
        Mat image = new Mat();
        cap.read(image);
        //System.out.println(images+image.channels());
        if(!image.empty()){
        	/*List<Mat> test = new ArrayList<Mat>();
            List<Mat> test2 = new ArrayList<Mat>();
            Core.split(image,test);
            test2.add(test.get(0));
            test2.add(test.get(0));
            test2.add(test.get(0));
            Core.merge(test2, image);*/
            Imgcodecs.imwrite("/home/spy/"+images+".jpg", image);
            mem.addInfo(new Info(image),images);
        }
    }
    
    private void ReadMessageFromArduino(){
        try {
            mem.addInfo(new Info(in.readLine()),"incomingMessages");         
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void ImportMemory(){
        try {
            BufferedReader memory = null;
            try (BufferedReader log = new BufferedReader(new FileReader(mem.getLogPath()))) {
                String filename;
                while((filename=log.readLine())!=null){
                    memory = new BufferedReader(new FileReader(filename));               
                }
            }
            if(memory!=null){
                String line;
                while((line=memory.readLine())!=null){
                    String[] pair = line.split(",");
                    mem.addInfo(new Info(pair[1]), pair[0]);
                } 
            }        
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        ImportMemory();
        Thread t1 = new Thread(){
            @Override
            public void run(){
                while(true)
                    ReadMessageFromArduino();
            }
        };
        t1.start();
        Thread t2 = new Thread(){
            @Override
            public void run(){
            	int i = 0;
                while(i<10){
                    getImageFromWebcam(capLeft,"leftImages");
                    i++;
                }
            }
        };
        t2.start();
        Thread t3 = new Thread(){
            @Override
            public void run(){
                while(true)
                    mem.ReceiveFromNetwork(bufferSize);
            }
        };
        t3.start();
        Thread t4 = new Thread(){
            @Override
            public void run(){
            	int i = 0;
                while(i<10){
                    getImageFromWebcam(capRight,"rightImages");
                    i++;
                }
            }
        };
        t4.start();
        Thread t5 = new Thread(){
            @Override
            public void run(){
                while(true)
                    mem.ReceiveFromWebsocket();
            }
        };
        t5.start();
    }
}