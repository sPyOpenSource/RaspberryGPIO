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

import java.util.Objects;

/**
 *
 * @author X. Wang
 */
public class Vector3D extends Vector<Double>{
    private double l;
    public Vector3D(Double x, Double y, Double z){
        super(x,y,z);
        if (x!=0||y!=0||z!=0)
            l = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        else
            l = 1;
    }
    public Vector3D getUnitVector(){
        return new Vector3D(x/l,y/l,z/l);
    }
    public Vector3D cross(Vector3D vector){
        return new Vector3D(y*vector.z-z*vector.y,z*vector.x-x*vector.z,x*vector.y-y*vector.x);
    }
    public void Normalize(){
        x/=l;
        y/=l;
        z/=l;
        l=1;
    }
    public void Sub(Vector<Double> vector){
        x-=vector.x;
        y-=vector.y;
        z-=vector.z;
    }
    public double Dot(Vector3D vector){
        return x*vector.x+y*vector.y+z*vector.z;
    }
    public String Display(){
        return x+","+y+","+z;
    }
    public boolean Compare(Vector3D vector){
        if (Objects.equals(vector.x, x) && Objects.equals(vector.y, y) && Objects.equals(vector.z, z)){
            return true;
        }
        return false;
    }
}
