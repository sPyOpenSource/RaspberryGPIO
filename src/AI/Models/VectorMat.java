package AI.Models;

import org.opencv.core.Mat;

/**
 *
 * @author X. Wang
 */
public class VectorMat extends Vector<Mat> {
    public VectorMat(Mat x, Mat y, Mat z){
        super(x,y,z);
    }
    public Vector3D getX(int i){
        return new Vector3D(x.get(i,0)[0], y.get(i,0)[0], z.get(i,0)[0]);
    }
}
