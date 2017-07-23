package AI;
/**
 * This is the input class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */

import AI.Models.MatEx;
import AI.Models.Vector3D;
import AI.Models.VectorFilter;
import AI.Models.VectorMat;
import AI.util.LSM303;
import AI.util.L3GD20;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;


public class AIInput implements Runnable
{
    /**
     * This is the initialization of AIInput class 
     */
    private final AIMemory mem;
    private final VideoCapture cap = new VideoCapture(); 
    private final int bufferSize;
    private BufferedReader log;
    private long start;
    private final LSM303 lsm303;
    private final L3GD20 l3gd20;
    private final VectorFilter accFilter;
    
    /**
     * Constructor for objects of class AIInput
     * @param mem
     */
    public AIInput(AIMemory mem)
    {
    	this.mem = mem;
        bufferSize = 1024;
        try {
            log = new BufferedReader(new FileReader(mem.getLogPath()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        cap.open(0);
        start = System.currentTimeMillis();
        lsm303 = new LSM303();
        l3gd20 = new L3GD20();
        accFilter = new VectorFilter(10, 0.0001, 0.001, 0.1, 0.02);
    }
    
    private void getImageFromWebcam(){
        Mat image = new Mat();
        cap.read(image);
        if(!image.empty()){
            long end = System.currentTimeMillis();
            MatEx temp = new MatEx(image,(end-start)/1000d);
            mem.addInpImage(temp);
            start = end;
        }
    }
    
    /*private void ReadMessageFromArduino(){
        System.out.println(mem.getSerial().read());
    }*/
    
    public void ReceiveFromNetwork(){
        byte[] buffer = new byte[bufferSize];
        DatagramPacket inPacket = new DatagramPacket(buffer, bufferSize);
        try {
            mem.getSocket().receive(inPacket);
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] info = new String(inPacket.getData()).split(";");
        mem.addInfo(info[0],"incomingMessages");
    }
    
    private void ImportMemory(){
        try {
            String filename;
            BufferedReader memory = null;
            while((filename=log.readLine())!=null){
                memory = new BufferedReader(new FileReader(filename));               
            }
            if(memory==null)
                return;
            String pair;
            while((pair=memory.readLine())!=null){
                String[] pairs = pair.split(",");
                mem.addInfo(pairs[1], pairs[0]);
            }
        } catch (IOException ex) {
            Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        ImportMemory();
        /*Thread t1 = new Thread(){
            @Override
            public void run(){
                while(true)
                    ReadMessageFromArduino();
            }
        };
        t1.start();*/
        Thread t2 = new Thread(){
            @Override
            public void run(){
                while(true)
                    getImageFromWebcam();
            }
        };
        t2.start();
        Thread t3 = new Thread(){
            @Override
            public void run(){
                while(true)
                    ReceiveFromNetwork();
            }
        };
        //t3.start();
        Thread t4 = new Thread(){
            @Override
            public void run(){
                try {
                    l3gd20.init();
                } catch (Exception ex) {
                    Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
                }
                Vector3D g = null;
                try {
                	g = lsm303.readingAcc();
                    accFilter.init(g);
                } catch (IOException ex) {
                    Logger.getLogger(AIInput.class.getName()).log(Level.SEVERE, null, ex);
                }
                while(true){
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
                    } catch (Exception e){
                        System.out.println(e);
                    }
                    AILogic.Wait(0.02);
                }
            }
        };
        //t4.start();
        Thread t5 = new Thread(){
            @Override
            public void run(){
                while(true)
                    mem.ReceiveFromWebsocket();
            }
        };
        //t5.start();
    }
}