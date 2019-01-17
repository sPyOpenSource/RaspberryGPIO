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
    private final double threshold = 10000, distand = 0.185, xmincor = 1.12, factor = 833.33, focuspoint, zmincor;
    private final MotionDetection left, right;
    private final VectorFilter accFilter, magFilter;
    private int index = 1;

    /**
     * Constructor for objects of class AILogic
     * @param mem
     */
    public AILogic(AIMemory mem)
    {
        // Initialize instance variables        
	super(mem);
        left  = new MotionDetection(0.2, threshold);
        right = new MotionDetection(0.2, threshold);
        zmincor    = 5.73; // 1m
        focuspoint = 1.90 / zmincor;
        accFilter = new VectorFilter(10, 10, 0.0001, 0.1, 0.1 / 3);
        magFilter = new VectorFilter(10, 10, 0.0001, 0.1, 0.1 / 3);
        accFilter.init(new Vector3D(0d, 0d, 0d), new Vector3D(0d, 0d, 0d));
        magFilter.init(new Vector3D(0d, 0d, 0d), new Vector3D(0d, 0d, 0d));
    }

    private void ProcessImages() {    
        if (index%60==0){
            left.saveBack("/home/spy/left.jpg");
            right.saveBack("/home/spy/right.jpg");
        }
        index++;
        left.UpdatePosition(mem.dequeFirst("leftImages"));
        right.UpdatePosition(mem.dequeFirst("rightImages"));
        int s = mem.search("leftImages").size();
        double xLeft  = left.getX() / factor;
        double yLeft  = left.getY() / factor;
        double length = Math.sqrt(Math.pow(xLeft - right.getX() / factor, 2) + Math.pow(yLeft - right.getY() / factor, 2));
        if (length > 0){
            double z = (distand * focuspoint) / length;
            double x = - xLeft * z / focuspoint / xmincor;
            double y = - yLeft * z / focuspoint / xmincor;
            System.out.println(String.format("%.4f, %.4f, %.4f, %d", x, y, z, s));
        }
    }

    @Override
    protected void Thread() {
        ProcessImages();
    }

    @Override
    protected void Messages(Info info) {
        String[] result = info.getPayload().split(",");
        if (result.length == 9){
            Vector3D gyr = new Vector3D(Double.parseDouble(result[3]), Double.parseDouble(result[4]), Double.parseDouble(result[5]));
            VectorMat answerAcc = accFilter.Filter(new Vector3D(Double.parseDouble(result[0]), Double.parseDouble(result[1]), Double.parseDouble(result[2])), gyr);
            //VectorMat answerMag = accFilter.Filter(new Vector3D(Double.parseDouble(result[6]), Double.parseDouble(result[7]), Double.parseDouble(result[8])), gyr);
            //System.out.println(answerAcc.getX(0).Display());
            //System.out.println(answerMag.getX(0).Display());
            mem.addInfo(new Info(answerAcc.getX(0).Display()), "outgoingMessages");
        }
    }
}