package test;

import AI.Models.Vector3D;
import AI.Models.VectorFilter;
import AI.Models.VectorMat;
import AI.util.KalmanFilter;
import junit.framework.TestCase;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 *
 * @author X. Wang
 */
public class JUnitTest extends TestCase{
    public JUnitTest(){
        System.load("/home/spy/Downloads/Source/C/opencv/build/lib/libopencv_java320.so");
    }
    
    @Test
    public void testKalmanFilterZero(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        VectorFilter filter = new VectorFilter(10, 10, 0.0001, 0.1, 0.02);
        filter.init(new Vector3D(0.0,0.0,0.0),new Vector3D(0.0,0.0,0.0));
        VectorMat X = filter.Filter(new Vector3D(0.0,0.0,0.0),new Vector3D(0.0,0.0,0.0));
        assertEquals(X.getX(0).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(1).Compare(new Vector3D(0.0,0.0,0.0)),true);
    }
    
    @Test
    public void testKalmanFilter(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        VectorFilter filter = new VectorFilter(10, 10, 0.0001, 0.1, 0.02);
        filter.init(new Vector3D(1.0,100.0,0.001),new Vector3D(0.0,0.0,0.0));
        VectorMat X = filter.Filter(new Vector3D(1.0,1.0,1.0), new Vector3D(1.0,1.0,1.0));
        assertEquals(X.getX(0).Compare(new Vector3D(0.00999950003699695,0.999950003699695,9.99950003699695E-6)),true);
        assertEquals(X.getX(1).Compare(new Vector3D(0.0,0.0,0.0)),true);
    }
    
    @Test
    public void testKalmanFilterTwoTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 10, 0.0001, 0.1, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        kalmanFilter.Filter(Z);
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.9995027486256873);
        assertEquals(X.get(1,0)[0], 0.7);
    }
    
    @Test
    public void testKalmanFilterThreeTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 10, 0.0001, 0.1, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        kalmanFilter.Filter(Z);
        kalmanFilter.Filter(Z);
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 1.0000072887467968);
        assertEquals(X.get(1,0)[0], 0.9181553136480118);
    }
    
    @Test
    public void testKalmanFilterManyTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 10, 0.0001, 0.1, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        for(int i = 0; i < 100; i++){
            kalmanFilter.Filter(Z);
        }
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 1.0000099933988171);
        assertEquals(X.get(1,0)[0], 0.9999464388129045);
    }
    
    @Test
    public void testKalmanFilterXManyTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 10, 0.0001, 0.1, 0.02);
        kalmanFilter.init(0.7,0.0);
        Mat Z = new Mat(2,1,CvType.CV_64F);
        Z.put(0, 0, 1d, 0);
        for(int i = 0; i < 100000; i++){
            kalmanFilter.Filter(Z);
        }
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 1.0);
        assertEquals(X.get(1,0)[0], 0.0);
    }
}
