package test;

import AI.AIMemory;
import AI.Models.Vector3D;
import AI.Models.Info;
import AI.Models.Computer;
import AI.util.PID;
import junit.framework.TestCase;
import org.junit.Test;
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
        assertEquals(memory.getLogPath(),"log.txt");
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
