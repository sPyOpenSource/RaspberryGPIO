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
package AI.util;

import java.util.ArrayList;
import java.util.List;

import AI.Model.Info;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author X. Wang
 */
public class MotionDetection {
    private Mat X,Y,Ones;
    private Mat temp = new Mat();
    private List<Mat> Background;
    private final List<Mat> tempImage;
    private double x,y;
    private final double filter;
    private final double threshold;
    public MotionDetection(double filter, double threshold){
        Background = null;
        tempImage = new ArrayList<Mat>();
        tempImage.add(new Mat());
        tempImage.add(new Mat());
        tempImage.add(new Mat());
        x = 0;
        y = 0;
        this.threshold = threshold;
        this.filter = filter;      
    }
    public void UpdatePosition(Info image){
        image.getImage().assignTo(temp, CvType.CV_32FC3);
        List<Mat> channels = new ArrayList<Mat>();
        Core.split(temp, channels);
        if(Background!=null){
            Core.absdiff(Background.get(0),channels.get(0),tempImage.get(0));
            Core.absdiff(Background.get(1),channels.get(1),tempImage.get(1));
            Core.absdiff(Background.get(2),channels.get(2),tempImage.get(2));
            double sum0 = Core.sumElems(tempImage.get(0)).val[0];
            double sum1 = Core.sumElems(tempImage.get(1)).val[0];
            double sum2 = Core.sumElems(tempImage.get(2)).val[0];
            if(sum0+sum1+sum2<threshold){
                UpdateBackground(channels);
            } else {
                x = (tempImage.get(0).dot(X)/sum0/3+tempImage.get(1).dot(X)/sum1/3+tempImage.get(2).dot(X)/sum2/3);
                y = (tempImage.get(0).dot(Y)/sum0/3+tempImage.get(1).dot(Y)/sum1/3+tempImage.get(2).dot(Y)/sum2/3);
            }
        } else {
            Background = channels;
            int height = temp.height();
            int width = temp.width();
            X = new Mat(height,width,CvType.CV_32F);
            Y = new Mat(height,width,CvType.CV_32F);
            Ones = Mat.ones(height, width, CvType.CV_32F);
            for(int i=0;i<height;i++){
                for(int j=0;j<width;j++){
                    Y.put(i,j, i-height/2);
                    X.put(i,j, j-width/2);
                }
            }  
        }
    }
    private void UpdateBackground(List<Mat> channels){
        Core.add(Background.get(0).mul(Ones,filter), channels.get(0).mul(Ones,1-filter), Background.get(0));
        Core.add(Background.get(1).mul(Ones,filter), channels.get(1).mul(Ones,1-filter), Background.get(1));
        Core.add(Background.get(2).mul(Ones,filter), channels.get(2).mul(Ones,1-filter), Background.get(2));
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
}
