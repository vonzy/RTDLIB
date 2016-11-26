package org.von.rtdlib.positioning;

import org.ejml.simple.SimpleMatrix;

/**
 * Created by diner on 16-10-16.
 * 站心坐标系(TopocentricCoordinates.java)
 * 用于计算卫星的方位角(Azimuth)和高度角(Elevation)
 * Copyright (C) 2007-2015 by Von Z.Y, All rights reserved.
 */

public class TopocentricCoordinates {

    private SimpleMatrix topocentric = new SimpleMatrix(3, 1); /* Azimuth (az), elevation (el), distance (d) */

    public void computeTopocentric(Coordinates origin, Coordinates target) {

//		// Build rotation matrix from global to local reference systems
//		SimpleMatrix R = globalToLocalMatrix(origin);
//
//		// Compute local vector from origin to this object coordinates
//		//SimpleMatrix enu = R.mult(target.ecef.minus(origin.ecef));
//		SimpleMatrix enu = R.mult(target.minusXYZ(origin));

        origin.computeLocal(target);

        double E = origin.getE();//enu.get(0);
        double N = origin.getN();//enu.get(1);
        double U = origin.getU();//enu.get(2);

        // Compute horizontal distance from origin to this object
        double hDist = Math.sqrt(Math.pow(E, 2) + Math.pow(N, 2));

        // If this object is at zenith ...
        if (hDist < 1e-20) {
            // ... set azimuth = 0 and elevation = 90, ...
            this.topocentric.set(0, 0, 0);
            this.topocentric.set(1, 0, 90);

        } else {

            // ... otherwise compute azimuth ...
            this.topocentric.set(0, 0, Math.toDegrees(Math.atan2(E, N)));

            // ... and elevation
            this.topocentric.set(1, 0, Math.toDegrees(Math.atan2(U, hDist)));

            if (this.topocentric.get(0) < 0)
                this.topocentric.set(0, 0, this.topocentric.get(0) + 360);
        }

        // Compute distance
        this.topocentric.set(2, 0, Math.sqrt(Math.pow(E, 2) + Math.pow(N, 2)
                + Math.pow(U, 2)));
    }

    public double getAzimuth(){
        return topocentric.get(0);
    }
    public double getElevation(){
        return topocentric.get(1);
    }
    public double getDistance(){
        return topocentric.get(2);
    }


}
