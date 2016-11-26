package org.von.rtdlib.positioning.models;

import org.von.rtdlib.positioning.Constants;
import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

/**
 * Created by diner on 16-10-16.
 */
public class IonoGPS extends IonosphereCorrectionProvider{


    // klobuchar Model
    @Override
    public double computeIonosphereCorrection(NavigationProvider navigation, Coordinates coord, double azimuth, double elevation, Time time) {
        double ionoCorr = 0;
//		double a0 = navigation.getIono(time.getMsec(),0);
//		double a1 = navigation.getIono(time.getMsec(),1);
//		double a2 = navigation.getIono(time.getMsec(),2);
//		double a3 = navigation.getIono(time.getMsec(),3);
//		double b0 = navigation.getIono(time.getMsec(),4);
//		double b1 = navigation.getIono(time.getMsec(),5);
//		double b2 = navigation.getIono(time.getMsec(),6);
//		double b3 = navigation.getIono(time.getMsec(),7);

        elevation = Math.abs(elevation);

        // Parameter conversion to semicircles
        double lon = coord.getGeodeticLongitude() / 180; // geod.get(0)
        double lat = coord.getGeodeticLatitude() / 180; //geod.get(1)
        azimuth = azimuth / 180;
        elevation = elevation / 180;

        // Klobuchar algorithm
        double f = 1 + 16 * Math.pow((0.53 - elevation), 3);
        double psi = 0.0137 / (elevation + 0.11) - 0.022;
        double phi = lat + psi * Math.cos(azimuth * Math.PI);
        if (phi > 0.416){
            phi = 0.416;
        }
        if (phi < -0.416){
            phi = -0.416;
        }
        double lambda = lon + (psi * Math.sin(azimuth * Math.PI))
                / Math.cos(phi * Math.PI);
        double ro = phi + 0.064 * Math.cos((lambda - 1.617) * Math.PI);
        double t = lambda * 43200 + time.getGpsTime();
        while (t >= 86400)
            t = t - 86400;
        while (t < 0)
            t = t + 86400;
        double p = this.getBeta(0) + this.getBeta(1) * ro + this.getBeta(2) * Math.pow(ro, 2) + this.getBeta(3) * Math.pow(ro, 3);

        if (p < 72000)
            p = 72000;
        double a = this.getAlpha(0) + this.getAlpha(1) * ro + this.getAlpha(2) * Math.pow(ro, 2) + this.getAlpha(3) * Math.pow(ro, 3);
        if (a < 0)
            a = 0;
        double x = (2 * Math.PI * (t - 50400)) / p;
        if (Math.abs(x) < 1.57){
            ionoCorr = Constants.SPEED_OF_LIGHT
                    * f
                    * (5e-9 + a
                    * (1 - (Math.pow(x, 2)) / 2 + (Math.pow(x, 4)) / 24));
        }else{
            ionoCorr = Constants.SPEED_OF_LIGHT * f * 5e-9;
        }
        return ionoCorr;
    }
}
