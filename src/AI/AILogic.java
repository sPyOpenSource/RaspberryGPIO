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
    private final PID pidx, pidy;
    private final Mat temp = new Mat(), dx = new Mat(), dy = new Mat(), dI = new Mat(), sum = new Mat(), S0 = new Mat(), V = new Mat(), Old = new Mat();
    private final List<Mat> channels = new ArrayList<>();
    private final Mat A, B;
    private long start;
        
    /**
     * Constructor for objects of class AILogic
     * @param mem
     */
    public AILogic(AIMemory mem)
    {
        // Initialize instance variables
	super(mem);
        pidx = new PID(1, 0, 0);
        pidy = new PID(1, 0, 0);
        A = new Mat(2, 2, CvType.CV_64F);
        B = new Mat(2, 1, CvType.CV_64F);
        start = System.currentTimeMillis();
    }
    
    @Override
    protected void Messages(Info info){
        /*switch (info.getPayload()){
            case "configures":
                Info configure = mem.getLast("configures");
                if (configure!=null)
                    Configure(configure.getPayload());
                break;
            case "news":
                mem.search("news").parallelStream().forEach((news) -> {
                    mem.addInfo(news, "outgoingMessages");
                }); 
                break;
            case "topics":
                mem.search("topics").parallelStream().forEach((topic) -> {
                    mem.addInfo(topic, "outgoingMessages");
                });
                break;
            default:
                String[] array = info.getPayload().split(":");
                if(array.length>1){
                    if("configure".equals(array[0])){
                        mem.addInfo(new Info(array[1]),"configures");
                        Configure(array[1]);                    
                    } else if("messages".equals(array[0])){
                        mem.search(array[1]).parallelStream().forEach((message) -> {
                            mem.addInfo(message, "outgoingMessges");
                        });
                    }
                }   
                break;
        }*/
    }

    private void Configure(String info){
        mem.addInfo(new Info("0" + info), "outgoingMessages2Arduino");
    }
    
    private void ProcessImages() {      
        Info image = mem.dequeFirst("webcame");
        if (image!=null){
            double dt = (image.getTime() - start) / 1000d;
            try{  
                image.getImage().colRange(0, 320).rowRange(0, 240).assignTo(temp, CvType.CV_16SC3);
                Core.split(temp, channels);
                Core.add(channels.get(0), channels.get(1), S0);
                Core.add(S0, channels.get(2), sum);
                channels.parallelStream().forEach((channel) -> {
                    channel.release();
                });
                int height = sum.height();
                int width  = sum.width();
                Mat North = sum.colRange(0, width-1).rowRange(0, height - 1);
                if (!Old.empty()){
                    Mat South = sum.colRange(0, width - 1).rowRange(1, height    );
                    Mat Eest  = sum.colRange(1, width    ).rowRange(0, height - 1);
                    Mat West  = sum.colRange(0, width - 1).rowRange(0, height - 1);    
                    Core.subtract(North,   Old,  dI);
                    Core.subtract(North, South,  dx);
                    Core.subtract( Eest,  West,  dy);
                    //Old.release();
                    double xy = dx.dot(dy);
                    A.put(0, 0, dx.dot(dx), dy.dot(dy), xy, xy);
                    B.put(0, 0, -dx.dot(dI) / dt, -dx.dot(dI) / dt);
                    Core.multiply(A.inv(), B, V);
                    String info = String.format(
                        "%f,%f",
                    	pidx.Compute(V.get(0, 0)[0], 0, dt),
                    	pidy.Compute(V.get(1, 0)[0], 0, dt)
                    );
                    start = image.getTime();
                    System.out.println(info);
                }
                North.copyTo(Old);
            } catch (Exception e){
                Logger.getLogger(AILogic.class.getName()).log(Level.SEVERE, null, e);
            }
            image.getImage().release();
        }  
    }

    @Override
    protected void Thread() {
        ProcessImages();
    }
}