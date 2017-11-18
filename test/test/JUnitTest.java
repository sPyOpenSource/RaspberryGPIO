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
        VectorFilter filter = new VectorFilter(10, 0.0001, 0.001, 0.1, 0.02);
        filter.init(new Vector3D(0.0,0.0,0.0));
        VectorMat X = filter.Filter(new Vector3D(0.0,0.0,0.0),new Vector3D(0.0,0.0,0.0));
        Mat Y = Mat.zeros(3,1,CvType.CV_64F);
        assertEquals(X.getX(0).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(1).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(2).Compare(new Vector3D(0.0,0.0,0.0)),true);
    }
    
    @Test
    public void testKalmanFilter(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        VectorFilter filter = new VectorFilter(10, 0.0001, 0.001, 0.1, 0.02);
        filter.init(new Vector3D(1.0,100.0,0.001));
        VectorMat X = filter.Filter(new Vector3D(1.0,1.0,1.0), new Vector3D(1.0,1.0,1.0));
        assertEquals(X.getX(0).Compare(new Vector3D(0.00999950003699695,0.999950003699695,9.99950003699695E-6)),true);
        assertEquals(X.getX(1).Compare(new Vector3D(0.0,0.0,0.0)),true);
        assertEquals(X.getX(2).Compare(new Vector3D(0.0,0.0,0.0)),true);
    }
    
    @Test
    public void testKalmanFilterTwoTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.1, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        kalmanFilter.Filter(Z);
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.014509990485252137);
        assertEquals(X.get(1,0)[0], 1.0509990485252145);
        assertEquals(X.get(2,0)[0], 0.0);
    }
    
    @Test
    public void testKalmanFilterThreeTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.1, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        kalmanFilter.Filter(Z);
        kalmanFilter.Filter(Z);
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.05474938699080596);
        assertEquals(X.get(1,0)[0], 1.173153520358747);
        assertEquals(X.get(2,0)[0], -1.5896602243088833);
    }
    
    @Test
    public void testKalmanFilterManyTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.1, 0.02);
        kalmanFilter.init(0.001,0.1);
        Mat Z = Mat.ones(2,1,CvType.CV_64F);
        for(int i = 0; i < 100; i++){
            kalmanFilter.Filter(Z);
        }
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 1.1193010750067705);
        assertEquals(X.get(1,0)[0], 0.9744390702501257);
        assertEquals(X.get(2,0)[0], 0.2554685909342975);
    }
    
    @Test
    public void testKalmanFilterXManyTimes(){
        System.out.println("* JUnitTest: testKalmanFilter()");
        KalmanFilter kalmanFilter = new KalmanFilter(10, 0.0001, 0.001, 0.1, 0.02);
        kalmanFilter.init(0.7,0.0);
        Mat Z = new Mat(2,1,CvType.CV_64F);
        Z.put(0, 0, 1d, 0);
        for(int i = 0; i < 100000; i++){
            kalmanFilter.Filter(Z);
        }
        Mat X = kalmanFilter.Filter(Z);
        assertEquals(X.get(0,0)[0], 0.9999999999999996);
        assertEquals(X.get(1,0)[0], 8.491396016125807E-17);
        assertEquals(X.get(2,0)[0], -8.486721710181393E-16);
    }
}
