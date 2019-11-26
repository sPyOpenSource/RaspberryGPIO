package AI;

import AI.Models.Info;
import AI.util.PID;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.CvType;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
    private final Mat temp = new Mat(), dx = new Mat(), dy = new Mat(), dI = new Mat(), sum = new Mat(), V = new Mat(), Old = new Mat();
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
        switch (info.getPayload()){
            case "configures":
                Info configure = mem.getLast("configures");
                if (configure != null)
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
        }
    }

    private void Configure(String info){
        mem.addInfo(new Info(info + ";"), "outgoingMessages2Serial");
    }
    
    private void ProcessImages() {      
        Info image = mem.dequeFirst("webcam");
        if (image != null){
            double dt = (image.getTime() - start) / 1000d;
            try{  
                image.getImage().assignTo(temp, CvType.CV_16SC3);
                Imgproc.cvtColor(temp, sum, Imgproc.COLOR_RGB2GRAY);
                temp.release();
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