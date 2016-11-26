package org.von.rtdlib.positioning.models;

import org.ejml.data.Matrix;
import org.von.rtdlib.positioning.Constants;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.EphemerisProvider;

/**
 * Created by diner on 16-10-21.
 * 星历参数 及 卫星位置计算
 * 北斗使用的星历参数和GPS完全一致
 */
public class EphemerisBDS extends EphemerisGPS{


    @Override
    public SatellitePosition computeSatPosition(Observations obs) {

        //注意传入的这个unixTime是UTC时间
        //GPST，而后转换为BDT，因为北斗导航电文中Toe等时间都是在BDT下的，
        //要计算当前历元相对于Toe的时间间隔，必须将UTC转化为BDT
        long unixTime = obs.getRefTime().getMsec() - 14000;

        double obsPseudorange = obs.getSatByIDType(satID, satType).getPseudorange(0);

//		char satType2 = eph.getSatType() ;
//					System.out.println("### other than GLONASS data");

        // Compute satellite clock error
        double satelliteClockError = computeSatelliteClockError(unixTime, obsPseudorange);

        // Compute clock corrected transmission(emit the signal) time
        double tBDS = computeClockCorrectedTransmissionTime(unixTime, satelliteClockError, obsPseudorange);

        // Compute eccentric anomaly
        double Ek = computeEccentricAnomaly(tBDS);

        // Semi-major axis
        double A = this.getRootA() * this.getRootA();

        // Time from the ephemerides reference epoch
        double tk = checkGpsTime(tBDS - this.getToe());

        // Position computation
        double fk = Math.atan2(Math.sqrt(1 - Math.pow(this.getE(), 2))
                * Math.sin(Ek), Math.cos(Ek) - this.getE());
        double phi = fk + this.getOmega();
        phi = Math.IEEEremainder(phi, 2 * Math.PI);
        double u = phi + this.getCuc() * Math.cos(2 * phi) + this.getCus()
                * Math.sin(2 * phi);
        double r = A * (1 - this.getE() * Math.cos(Ek)) + this.getCrc()
                * Math.cos(2 * phi) + this.getCrs() * Math.sin(2 * phi);
        double ik = this.getI0() + this.getiDot() * tk + this.getCic() * Math.cos(2 * phi)
                + this.getCis() * Math.sin(2 * phi);
        double Omega = this.getOmega0()
                + (this.getOmegaDot() - Constants.OMEGAE_DOT_BDS) * tk
                - Constants.OMEGAE_DOT_BDS * this.getToe();
        Omega = Math.IEEEremainder(Omega + 2 * Math.PI, 2 * Math.PI);
        double x1 = Math.cos(u) * r;
        double y1 = Math.sin(u) * r;

        /* beidou geo satellite (ref [9]) */
        if (satID <= 5)
        {
             Omega = this.getOmega0()
                    + this.getOmegaDot() * tk
                    - Constants.OMEGAE_DOT_BDS * this.getToe();
            //Omega = Math.IEEEremainder(Omega + 2 * Math.PI, 2 * Math.PI);
            double xg = x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega);
            double yg = x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega);
            double zg = y1 * Math.sin(ik);
            SatellitePosition sp = new SatellitePosition(unixTime,satID, satType,
                    xg * Math.cos(Constants.OMEGAE_DOT_BDS * tk) + yg * Math.sin(Constants.OMEGAE_DOT_BDS * tk) * Math.cos(-5 * Math.PI / 180) + zg * Math.sin(Constants.OMEGAE_DOT_BDS * tk) * Math.sin(-5 * Math.PI / 180),
                    -xg* Math.sin(Constants.OMEGAE_DOT_BDS * tk) + yg * Math.cos(Constants.OMEGAE_DOT_BDS * tk) * Math.cos(-5 * Math.PI / 180) + zg * Math.cos(Constants.OMEGAE_DOT_BDS * tk) * Math.sin(-5 * Math.PI / 180),
                    -yg * Math.sin(-5 * Math.PI / 180) + zg * Math.cos(-5 * Math.PI / 180));
            sp.setSatelliteClockError(satelliteClockError);
            return sp;

        }
        else
        {
            // Coordinates
            //			double[][] data = new double[3][1];
            //			data[0][0] = x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega);
            //			data[1][0] = x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega);
            //			data[2][0] = y1 * Math.sin(ik);

            // Fill in the satellite position matrix
            //this.coord.ecef = new SimpleMatrix(data);
            //this.coord = Coordinates.globalXYZInstance(new SimpleMatrix(data));
            SatellitePosition sp = new SatellitePosition(unixTime,satID, satType,
                    x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
                    x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
                    y1 * Math.sin(ik));
            sp.setSatelliteClockError(satelliteClockError);
            return sp;
        }


    }


}
