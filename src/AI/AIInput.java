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
import AI.util.LSM303;
import AI.util.L3GD20;
import com.pi4j.io.serial.Serial;

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
    private final VideoCapture cap = new VideoCapture(); 
    private final LSM303 lsm303;
    private final L3GD20 l3gd20;
    private final VectorFilter accFilter;
    private Vector3D g;
    private final double dt = 0.02;
    private final Serial serial;

    /**
     * Constructor for objects of class AIInput
     * @param mem
     */
    public AIInput(AIMemory mem)
    {
    	super(mem);
        serial = mem.getSerial();
        cap.open(0);
        lsm303 = new LSM303();
        l3gd20 = new L3GD20();
        accFilter = new VectorFilter(10.0, 10.0, 0.0001, 0.1, 0.02);
        try {
            l3gd20.init();
        } catch (Exception ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            g = lsm303.readingAcc();
            Vector3D gyr = l3gd20.getRawOutValues();
            accFilter.init(g,gyr);
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void getImageFromWebcam(){
        Mat image = new Mat();
        cap.read(image);
        if(!image.empty()){
            mem.addInfo(new Info(image),"webcam");
        }
    }
    
    private void filter(){
        try{ 
            Vector3D acc = lsm303.readingAcc();
            g.setValues(
                g.x*0.9+acc.x*0.1,
                g.y*0.9+acc.y*0.1,
                g.z*0.9+acc.z*0.1
            );
            Vector3D gyr = l3gd20.getRawOutValues();
            VectorMat result = accFilter.Filter(g, gyr);
            //System.out.print(gyr.Display()+",");
            //System.out.print(acc.Display()+",");
            System.out.println(result.getX(1).Display());
        } catch (Exception ex){
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        AILogic.Wait(dt);
    }
    
    private void ReadMessageFromArduino(){
        System.out.println(serial.read());
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
        Thread getImageFromWebcam = new Thread(){
            @Override
            public void run(){
                while(true)
                    getImageFromWebcam();
            }
        };
        getImageFromWebcam.start();
        Thread filter = new Thread(){
            @Override
            public void run(){
                while(true)
                    filter();
            }
        };
        filter.start();
    }
}