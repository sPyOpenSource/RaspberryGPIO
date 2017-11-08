package test;

import AI.AIMemory;
import AI.Models.Vector3D;
import AI.Models.VectorFilter;
import AI.Models.VectorMat;
import AI.Models.Info;
import AI.Models.Computer;
import AI.util.KalmanFilter;
import AI.util.PID;
import junit.framework.TestCase;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.net.Socket;


/**
 *
 * @author X. Wang
 */
public class JUnitTest extends TestCase{
    private final AIMemory memory;
    private final Vector3D vector;
    private final PID pid;
    private final Info info;
    private final Computer computer;
    
    public JUnitTest(){
        System.load("/home/spy/Downloads/Source/C/opencv/build/lib/libopencv_java320.so");
        memory = new AIMemory();
        vector = new Vector3D(1.0,1.0,1.0);
        pid = new PID(1,1,1);
        info = new Info("test");
        computer = new Computer(new Socket());
    }

    @Test
    public void testIncomingMessages(){
        System.out.println("* JUnitTest: memoryTestIncomingMessages()");
        memory.addInfo(new Info("configure:test"),"incomingMessages");
        assertEquals("configure:test",memory.dequeFirst("incomingMessages").getPayload());
    }
    
    @Test
    public void testOutgoingMessages2ArduinoDequeLast(){
        System.out.println("* JUnitTest: memoryTestOutgoingMessages2ArduinoDequeLast()");
        memory.addInfo(new Info("test1"), "outgoingMessages2Arduino");
        assertEquals("test1",memory.dequeLast("outgoingMessages2Arduino").getPayload());
    }
    
    @Test
    public void testConfiguresGetLast(){
        System.out.println("* JUnitTest: memoryTestConfiguresGetLast()");
        memory.addInfo(new Info("test2"), "configures");
        assertEquals("test2",memory.dequeLast("configures").getPayload());
    }
    
    @Test
    public void testEmotions(){
        System.out.println("* JUnitTest: testEmotions()");
        memory.setEmotions(0, 0);
        assertEquals(memory.getEmotions(0),0.0);
    }
    
    @Test
    public void testLogpath(){
        System.out.println("* JUnitTest: testLogpath()");
        assertEquals(memory.getLogPath(),"/home/spy/AI/log.txt");
    }
    
    @Test
    public void testVector3D(){
        System.out.println("* JUnitTest: testVector3D()");
        assertEquals(vector.Dot(vector), 3.0);
        assertEquals(vector.cross(vector).Compare(new Vector3D(0.0,0.0,0.0)), true);
        vector.Normalize();
        assertEquals(vector.getUnitVector().Compare(vector), true);
        vector.Sub(vector);
        assertEquals(vector.Compare(new Vector3D(0.00,0.00,0.00)), true);
    }
    
    @Test
    public void testKalmanFilterZero(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        VectorFilter filter = new VectorFilter(10, 0.0001, 0.001, 0.02);
        filter.init(new Vector3D(0.0,0.0,0.0));
        VectorMat X = filter.Filter(new Vector3D(0.0,0.0,0.0),new Vector3D(0.0,0.0,0.0));
        assertEquals(X.getX(0).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(1).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(2).Compare(new Vector3D(0.0,0.0,0.0)),true);
    }
    
    @Test
    public void testKalmanFilter(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        VectorFilter filter = new VectorFilter(10, 0.0001, 0.001, 0.02);
        filter.init(new Vector3D(1.0,100.0,0.001));
        VectorMat X = filter.Filter(new Vector3D(1.0,1.0,1.0), new Vector3D(1.0,1.0,1.0));
        assertEquals(X.getX(0).Compare(new Vector3D(0.00999950003699695,0.999950003699695,9.99950003699695E-6)),true);
        assertEquals(X.getX(1).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(2).Compare(new Vector3D(0.0,0.0,0.0)),true);
    }
    
    @Test
    public void testKalmanFilterTwoTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        kalmanFilter.Filter(Z);
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.007854898600399885);
        assertEquals(X.get(1,0)[0], 0.38548986003998853);
        assertEquals(X.get(2,0)[0], 16.258783204798625);
    }
    
    @Test
    public void testKalmanFilterThreeTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        kalmanFilter.Filter(Z);
        kalmanFilter.Filter(Z);
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.023933585123087662);
        assertEquals(X.get(1,0)[0], 1.0046191990151088);
        assertEquals(X.get(2,0)[0], 27.464609948167812);
    }
    
    @Test
    public void testKalmanFilterManyTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        for(int i = 0; i < 100; i++){
            kalmanFilter.Filter(Z);
        }
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 1.3027591767322584);
        assertEquals(X.get(1,0)[0], 0.9370422506769516);
        assertEquals(X.get(2,0)[0], 1.0698110481008136);
    }
    
    @Test
    public void testKalmanFilterXManyTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.02);
        kalmanFilter.init(0.7,0.0);
        Mat Z = new Mat(2,1,CvType.CV_64F);
        Z.put(0, 0, 1d, 0);
        for(int i = 0; i < 100000; i++){
            kalmanFilter.Filter(Z);
        }
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.9999999999999991);
        assertEquals(X.get(1,0)[0], 1.7389024333743455E-16);
        assertEquals(X.get(2,0)[0], -2.9655025754001526E-15);
    }
    
    @Test
    public void testPID(){
        System.out.println("* JUnitTest: testPID()");
        double y = pid.Compute(1, 1, 1);
        assertEquals(y, -3.0);
        y = pid.Compute(10, 20, 1);
        assertEquals(y, -31.0);
        y = pid.Compute(20, 30, 1);
        assertEquals(y, -71.0);
        y = pid.Compute(50, 20, 1);
        assertEquals(y, -150.0);
        y = pid.Compute(-150, 20, 1);
        assertEquals(y, 150.0);
        y = pid.Compute(0, 20, 1);
        assertEquals(y, 69.0);
        y = pid.Compute(10, 10, 10);
        assertEquals(y, -51.0);
        y = pid.Compute(10, 10, 10);
        assertEquals(y, -150.0);
    }
    
    @Test
    public void testInfo(){
        System.out.println("* JUnitTest: testInfo()");
        assertEquals(info.getPayload(), "test");
        assertEquals(info.isOnline(), false);
    }
    
    @Test
    public void testComputer(){
        System.out.println("* JUnitTest: testComputer()");
        assertEquals(computer.isOnline(), false);
        assertEquals(computer.isStart(), false);
        String test = computer.getInput();
        assertEquals(test, null);
        assertEquals(computer.isStart(), true);
    }
}
