/*
 * Copyright (C) 2016 X. Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package AI.Models;

import AI.util.KalmanFilter;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

/**
 *
 * @author X. Wang
 */
public class VectorFilter extends Vector<KalmanFilter>{
    private Vector3D vectorU;
    public VectorFilter(double processNoiseStdev, double Rx, double Rv, double dt){
    	super(
                new KalmanFilter(processNoiseStdev, Rx, Rv, dt),
                new KalmanFilter(processNoiseStdev, Rx, Rv, dt),
                new KalmanFilter(processNoiseStdev, Rx, Rv, dt)
        );
    }
    public VectorMat Filter(Vector3D vector, Vector<Double> gyr){
        vectorU = vector.getUnitVector();
        Vector<Double> vectorV = new Vector<>(
                vectorU.y*gyr.z-vectorU.z*gyr.y,
                vectorU.z*gyr.x-vectorU.x*gyr.z,
                vectorU.x*gyr.y-vectorU.y*gyr.x
        );
        Mat X = new Mat(2, 1, CvType.CV_64F);
		X.put(0, 0, vectorU.x, vectorV.x);
		Mat Y = new Mat(2, 1, CvType.CV_64F);
		Y.put(0, 0, vectorU.y, vectorV.y);
		Mat Z = new Mat(2, 1, CvType.CV_64F);
		Z.put(0, 0, vectorU.z, vectorV.z);
        return new VectorMat(
                x.Filter(X),
                y.Filter(Y),
                z.Filter(Z)
        );
    }
    public void init(Vector3D vector){
        vectorU = vector.getUnitVector();
        x.init(vectorU.x, 0);
        y.init(vectorU.y, 0);
        z.init(vectorU.z, 0);
    }
}
