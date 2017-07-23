package AI.Models;

import org.opencv.core.Mat;

/**
 *
 * @author X. Wang
 */
public class MatEx {
    private final Mat mat;
    private final double dt;
    public MatEx(Mat mat, double dt){
        this.mat = mat;
        this.dt = dt;
    }
    public Mat getMat(){
        return mat;
    }
    public double getTime(){
        return dt;
    }
}
