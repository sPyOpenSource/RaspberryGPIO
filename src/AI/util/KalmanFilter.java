package AI.util;

/**
 * This is a KalmanFilter class.
 * 
 * @author X. Wang
 * @version 1.0
 */

import static java.lang.Math.pow;
import static org.opencv.core.Core.gemm;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class KalmanFilter
{
    private final Mat H, R, F, X, K, P;
    private Mat Q;
    
    public KalmanFilter(double processNoiseStdev, double Rx, double Rv, double dt)
    {
        P = Mat.zeros(3,3,CvType.CV_64F);
        H = new Mat(2,3,CvType.CV_64F);
        H.put(0, 0, 1, 0, 0, 0, 1, 0);
        R = Mat.eye(2, 2, CvType.CV_64F);
        R.put(0, 0, Rx, 0, 0, Rv);
        F = new Mat(3,3,CvType.CV_64F);
        F.put(0, 0, 1, dt, pow(dt,2)/2, 0, 1, dt, 0, 0, 1);
        Q = new Mat(3,3,CvType.CV_64F);
        Q.put(0, 0 , 
            pow(dt, 5) / 4, pow(dt, 4) / 2, pow(dt, 3) / 2,
            pow(dt, 4) / 2, pow(dt, 3) / 1, pow(dt, 2) / 1,
            pow(dt, 3) / 1, pow(dt, 2) / 1, pow(dt, 1) / 1
        );
        Q = Q.mul(Mat.ones(3,3,CvType.CV_64F), pow(processNoiseStdev, 2)/2);
        K = new Mat();
        X = new Mat(3,1,CvType.CV_64F);
        X.put(0, 0, 0, 0, 0); //Initial position, velocity and accelation.
        
    }
    
    public void init(double x, double v){
        X.put(0, 0, x, v, 0);
    }
    
    public Mat Filter(Mat Z)
    {
        KalmanGain();
        ProjectX();
        UpdateEstimate(Z);        
        UpdateCovariance();
        ProjectP();        
        return X;
    }
    
    private void KalmanGain()
    {
    	Mat S0 = new Mat();
    	Mat S1 = new Mat();
    	Mat S2 = new Mat();
    	gemm(P, H.t(), 1, new Mat(), 0, S1);
    	gemm(H, S1, 1, R, 1, S0);
        gemm(H.t(), S0.inv(), 1, new Mat(), 0, S2);
        gemm(P, S2, 1, new Mat(), 0, K);
    }
    
    private void UpdateEstimate(Mat Z)
    {
    	Mat S = new Mat();
    	gemm(H, X, -1, Z, 1, S);
    	gemm(K, S, 1, X, 1, X);
    }
    
    private void UpdateCovariance()
    {
    	Mat S = new Mat();
    	gemm(H, P, 1, new Mat(), 0, S);
    	gemm(K, S, -1, P, 1, P);
    }
          
    private void ProjectX()
    {
        gemm(F, X, 1, new Mat(), 0, X);
    }
    
    private void ProjectP()
    {
    	Mat S = new Mat();
    	gemm(P, F.t(), 1, new Mat(), 0, S);
    	gemm(F, S, 1, Q, 1, P);
    }
}
