package AI;

import AI.Models.Info;
import AI.util.PID;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.CvType;
import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * This is the logic class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AILogic extends AIBaseLogic
{
    // instance variables
    private final PID pidx,pidy;
    private final int imax;
    private final Mat dx = new Mat();
    private final Mat temp = new Mat();
    private final Mat dy = new Mat();
    private final Mat dI = new Mat();
    private final List<Mat> channels;
    private final Mat A,B;
    private Mat Old = new Mat();
    private int index;
        
    /**
     * Constructor for objects of class AILogic
     * @param mem
     */
    public AILogic(AIMemory mem)
    {
        // Initialize instance variables
	super(mem);
        pidx = new PID(1,0,0);
        pidy = new PID(1,0,0);
        imax = 150;
        channels = new ArrayList<>();
        A = new Mat(2,2,CvType.CV_64F);
        B = new Mat(2,1,CvType.CV_64F);
        index = 1;
    }
    
    /*private void ProcessMessages(){
        Info info = mem.dequeLast("incomingMessages");
        if (info!=null)
        {
            mem.addInfo(info,"outgoingMessages2Network");
            while(true){
                if("start".equals(info)){
                    String configure = mem.getLastConfigure();
                    if (configure!=null){
                        mem.addInfo("0"+configure,"outgoingMessages2Arduino");
                    }
                } else {
                    String[] arrays = info.split(":");
                    if(arrays.length>1){
                        if("0".equals(arrays[0]))
                            mem.addInfo(arrays[1],"outgoingMessages2Web");
                        else if("configure".equals(arrays[0])){
                            mem.addInfo(arrays[1],"configures");
                            mem.addInfo("0"+arrays[1],"outgoingMessages2Arduino");
                        }  
                    }                         
                }
                info = mem.dequeFirst("incomingMessages");
                if(info==null)
                    break;
            }   
        }
    }*/

    private void ProcessImages() {      
        Info image = mem.dequeFirst("webcame");
        if (image!=null){
            try{  
                image.getImage().colRange(0,320).rowRange(0,240).assignTo(temp, CvType.CV_16SC3);
                Core.split(temp, channels);
                Mat sum = new Mat();
                Mat S0 = new Mat();
                Core.add(channels.get(0), channels.get(1), S0);
                Core.add(S0, channels.get(2), sum);
                int height = sum.height();
                int width = sum.width();
                Mat North = sum.colRange(0, width-1).rowRange(0, height-1);
                if (!Old.empty()){
                    Mat South = sum.colRange(0, width-1).rowRange(1, height);
                    Mat Eest = sum.colRange(1, width).rowRange(0, height-1);
                    Mat West = sum.colRange(0, width-1).rowRange(0, height-1);    
                    Core.subtract(North,Old,dI);
                    Core.subtract(North,South,dx);
                    Core.subtract(Eest,West,dy);
                    double xy = dx.dot(dy);
                    A.put(0, 0, dx.dot(dx), dy.dot(dy), xy, xy);
                    B.put(0, 0, -dx.dot(dI)/image.getTime(), -dx.dot(dI)/image.getTime());
                    Mat V = new Mat();
                    Core.multiply(A.inv(), B, V);
                    String info = String.format(
                        "%f,%f",
                    	pidx.Compute(V.get(0,0)[0],0,image.getTime()),
                    	pidy.Compute(V.get(1,0)[0],0,image.getTime())
                    );
                    System.out.println(info);
                    //mem.addOutgoingMessage2Arduino("1"+info);
                    //mem.addOutgoingMessage2Network(message+",");
                }
                Old = North;
                if(index%imax==0){
                    System.gc();
                }
                index++;
            } catch (Exception e){
                Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, e);
            }
        }  
    }

    @Override
    public void run() {
        while(true){
            ProcessImages();
            //ProcessMessages();
            Wait(dt);
        }
    }
}