
package AI;

import AI.Models.Info;
import AI.Models.Vector3D;
import AI.Models.VectorMat;
import AI.util.MotionDetection;
import AI.util.PointCloud;
import AI.util.VectorFilter;

import ecm.PrimeTest.LucasLehmer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the logic class of AI.
 * Everything including processing
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
        colorCamera = new MotionDetection(filter, threshold);
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
    protected void loop() {
        ProcessImages();
    }

    @Override
    protected void MessagesLogic(Info info) {
        switch (info.getPayload()){
            case "news":
                ((AIBaseMemory)mem).search("news").parallelStream().forEach((news) -> {
                    mem.add(news, "outgoingMessages");
                }); 
                break;
            case "topics":
                ((AIBaseMemory)mem).search("topics").parallelStream().forEach((topic) -> {
                    mem.add(topic, "outgoingMessages");
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
            Vector3D gyr = new Vector3D(
                    Double.valueOf(result[3]), 
                    Double.valueOf(result[4]), 
                    Double.valueOf(result[5]));
            VectorMat answerAcc = accFilter.Filter(new Vector3D(
                    Double.valueOf(result[0]), 
                    Double.valueOf(result[1]), 
                    Double.valueOf(result[2])), gyr);
            mem.add(new Info(answerAcc.getVector(0).Display()), "outgoingMessages");
        }
    }
}