package AI;

/**
 * This is the input class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */

import AI.Models.Info;
import AI.Models.Vector3D;
import AI.Models.VectorFilter;
import AI.Models.VectorMat;
import AI.util.AII2CBus;

import gnu.io.SerialPort;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class AIInput extends AIBaseInput
{
    /**
     * This is the initialization of AIInput class 
     */
    private Vector3D g;
    private AII2CBus i2cbus;
    private final double dt = 0.02;
    private final VideoCapture cap = new VideoCapture("http://192.168.1.4:8080/video"); 
    private final VectorFilter accFilter;
    private final SerialPort serial;

    /**
     * Constructor for objects of class AIInput
     * @param mem
     */
    public AIInput(AIMemory mem)
    {
        super(mem);
        serial = mem.getSerial();
        accFilter = new VectorFilter(10.0, 10.0, 0.0001, 0.1, 0.02);
    }
    
    private void getImageFromWebcam(){
        Mat image = new Mat();
        cap.read(image);
        if(!image.empty()){
            mem.addInfo(new Info(image), "webcam");
        }
    }
    
    private void filter(double filter){
        try{
            Vector3D acc = i2cbus.readingAcc();
            g.setValues(
                g.x * filter + acc.x * (1 - filter),
                g.y * filter + acc.y * (1 - filter),
                g.z * filter + acc.z * (1 - filter)
            );
            Vector3D gyr = i2cbus.readingGyr();
            VectorMat result = accFilter.Filter(g, gyr);
            System.out.println(result.getX(1).Display());
        } catch (IOException ex){
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        AILogic.Wait(dt);
    }
    
    private void ReadMessageFromSerial(){
        try {
            System.out.println(serial.getInputStream().read());
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void Thread() {
        Thread ReadMessageFromSerial = new Thread(){
            @Override
            public void run(){
                while(true)
                    ReadMessageFromSerial();
            }
        };
        ReadMessageFromSerial.start();
        Thread getImageFromWebcam = new Thread(){
            @Override
            public void run(){
                while(true)
                    getImageFromWebcam();
            }
        };
        //getImageFromWebcam.start();
        Thread filter = new Thread(){
            @Override
            public void run(){
                while(true)
                    filter(0.9);
            }
        };
        //filter.start();
    }
}