package AI;

import AI.Models.Info;
import AI.Models.Vector3D;
import AI.Models.VectorFilter;
import AI.Models.VectorMat;
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
    private final VectorFilter accFilter, magFilter;

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
        accFilter = new VectorFilter(10.0, 10.0, 0.0001, 0.1, 0.03333333333);
        accFilter.init(new Vector3D(0.0,0.0,0.0), new Vector3D(0.0,0.0,0.0));
        magFilter = new VectorFilter(10.0, 10.0, 0.0001, 0.1, 0.03333333333);
        magFilter.init(new Vector3D(0.0,0.0,0.0), new Vector3D(0.0,0.0,0.0));
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
        double l = Math.sqrt(Math.pow(xLeft - xRight, 2)+Math.pow(yLeft - yRight, 2));
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
        //ProcessImages();
    }

    @Override
    protected void Messages(Info info) {
        String[] result = info.getPayload().split(",");
        Vector3D gyr = new Vector3D(Double.parseDouble(result[3]),Double.parseDouble(result[4]),Double.parseDouble(result[5]));
        VectorMat answerAcc = accFilter.Filter(new Vector3D(Double.parseDouble(result[0]),Double.parseDouble(result[1]),Double.parseDouble(result[2])), gyr);
        //VectorMat answerMag = accFilter.Filter(new Vector3D(Double.parseDouble(result[6]),Double.parseDouble(result[7]),Double.parseDouble(result[8])), gyr);
        System.out.println(answerAcc.getX(0).Display());
        //System.out.println(answerMag.getX(0).Display());
    }
}