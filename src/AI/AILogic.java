package AI;

import AI.Models.Info;
import AI.Models.Vector3D;
import AI.Models.VectorFilter;
import AI.Models.VectorMat;
import AI.util.MotionDetection;
import AI.util.PointCloud;

import ecm.PrimeTest.LucasLehmer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the logic class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AILogic extends AIBaseLogic
{
    // instance variables
    private final double threshold = 1000000, filter = 0.99;
    private final MotionDetection colorCamera;
    private final PointCloud depthCamera;
    private final VectorFilter accFilter, magFilter;
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    /**
     * Constructor for objects of class AILogic
     * @param mem
     */
    public AILogic(AIMemory mem)
    {
        // Initialize instance variables        
        super(mem);
        colorCamera  = new MotionDetection(filter, threshold);
        depthCamera = new PointCloud();
        accFilter = new VectorFilter(10, 10, 0.0001, 0.1, 0.1 / 3);
        magFilter = new VectorFilter(10, 10, 0.0001, 0.1, 0.1 / 3);
        accFilter.init(new Vector3D(0d, 0d, 0d), new Vector3D(0d, 0d, 0d));
        magFilter.init(new Vector3D(0d, 0d, 0d), new Vector3D(0d, 0d, 0d));
    }

    private void ProcessImages() {
        colorCamera.UpdatePosition((Info)mem.dequeFirst("colorCameraImages"));
        depthCamera.Calculate((Info)mem.dequeFirst("depthCameraImages"), colorCamera.getX(), colorCamera.getY());
    }

    @Override
    protected void Thread() {
        ProcessImages();
    }

    @Override
    protected void Messages(Info info) {
        switch (info.getPayload()){
            case "news":
                ((AIBaseMemory)mem).search("news").parallelStream().forEach((news) -> {
                    mem.addInfo(news, "outgoingMessages");
                }); 
                break;
            case "topics":
                ((AIBaseMemory)mem).search("topics").parallelStream().forEach((topic) -> {
                    mem.addInfo(topic, "outgoingMessages");
                });
                break;
            case "feedback":
                mem.addEmotion();
                break;
            case "prime":
                ((AIBaseMemory)mem).search("prime").parallelStream().forEach(
                        (prime) -> {
                            LucasLehmer ll = new LucasLehmer(Integer.parseInt(prime.getPayload()));
                            pool.execute(ll);
                        });
                break;
        }
        String[] result = info.getPayload().split(",");
        if (result.length == 9){
            Vector3D gyr = new Vector3D(Double.parseDouble(result[3]), Double.parseDouble(result[4]), Double.parseDouble(result[5]));
            VectorMat answerAcc = accFilter.Filter(new Vector3D(Double.parseDouble(result[0]), Double.parseDouble(result[1]), Double.parseDouble(result[2])), gyr);
            mem.addInfo(new Info(answerAcc.getX(0).Display()), "outgoingMessages");
        }
    }
}