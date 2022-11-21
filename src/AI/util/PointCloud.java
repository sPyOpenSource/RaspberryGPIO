
package AI.util;

import AI.Models.Info;
import AI.Models.Vector3D;
import org.opencv.core.Mat;

/**
 *
 * @author X. Wang
 */
public class PointCloud {
    final Vector3D factor, position;

    public PointCloud(){
        factor = new Vector3D(0.001, 0.001, 0.1 / 6);
        position = new Vector3D(0d, 0d, 0d);
    }
    
    public void Calculate(Info dequeFirst, double x, double y) {
        if (dequeFirst != null){
            Mat test = dequeFirst.getImage();
            if(test != null){
                position.z = test.get((int)x + 640 / 2, (int)y + 340 / 2)[0] * this.factor.z;
                position.x = x * position.z * this.factor.x;
                position.y = y * position.z * this.factor.y;
                position.z  = Math.sqrt(Math.pow(position.z, 2) - Math.pow(position.x, 2) - Math.pow(position.y, 2));
                System.out.println(String.format("%f.4,%f.4,%f.4", position.x, position.y, position.z));
                test.release();
            }
        }
    }
}
