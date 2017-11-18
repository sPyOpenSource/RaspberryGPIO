package AI;

import AI.Models.Info;
import AI.util.MotionDetection;

/**
 * This is the logic class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AILogic extends AIBaseLogic
{
    // instance variables
    private final int imax = 150;
    private final double threshold = 10000,factor,zmincor,d = 0.185,f,xmincor;
    private int index = 1;
    private final MotionDetection left,right;
        
    /**
     * Constructor for objects of class AILogic
     * @param mem
     */
    public AILogic(AIMemory mem)
    {
        // Initialize instance variables        
	super(mem);
        left = new MotionDetection(0.5, threshold);
        right = new MotionDetection(0.5, threshold);
        factor = 500/0.6;
        zmincor = 4.5*3.82/3; // 1m
        xmincor = 0.14/0.62*5; //-0.14 ipv 0.62, 
        f = 1.90/zmincor;
    }

    private void ProcessImages() {    
        Info imageLeft = mem.getLast("leftImages");
        if (imageLeft!=null){
            left.UpdatePosition(imageLeft);
            mem.removeAll("leftImages");
        }  
        /*Info imageRight = mem.getLast("rightImages");
        if (imageRight!=null){
            mem.removeAll("rightImages");
            right.UpdatePosition(imageRight);
        }*/
        double xLeft = left.getX()/factor;
        double yLeft = left.getY()/factor;
        double xRight = right.getX()/factor;
        double yRight = right.getY()/factor;
        double l = Math.sqrt(Math.pow(xLeft - xRight,2)+Math.pow(yLeft - yRight,2));
        if (l>0){
            double z = (d*f)/l;
            double x = -xLeft*z/f/xmincor;
            double y = -yLeft*z/f/xmincor ;
            System.out.println(String.format("%.4f, %.4f, %.4f",x,y,z));
        }
        if(index%imax==0)
            System.gc();
        index++;
    }

    @Override
    protected void Thread() {
        ProcessImages();
    }

    @Override
    protected void Messages(Info info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}