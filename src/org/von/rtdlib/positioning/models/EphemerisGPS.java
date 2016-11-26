package org.von.rtdlib.positioning.models;

import org.ejml.simple.SimpleMatrix;
import org.von.rtdlib.positioning.Constants;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.EphemerisProvider;

/**
 * Created by diner on 16-10-21.
 */
public class EphemerisGPS extends EphemerisProvider {

    protected int week; /* GPS week number */

    protected int L2Code; /* Code on L2 */
    protected int L2Flag; /* L2 P data flag */

    protected int svAccur; /* SV accuracy (URA index) */
    protected int svHealth; /* SV health */

    protected int iode; /* Issue of data (ephemeris) */
    protected int iodc; /* Issue of data (clock) */

    protected double toc; /* clock data reference time */
    protected double toe; /* ephemeris reference time */
    protected double tom; /* transmission time of message */

    /* satellite clock parameters */
    protected double af0;
    protected double af1;
    protected double af2;
    protected double tgd;

    /* satellite orbital parameters */
    protected double rootA; /* Square root of the semimajor axis */
    protected double e; /* Eccentricity */
    protected double i0; /* Inclination angle at reference time */
    protected double iDot; /* Rate of inclination angle */
    protected double omega; /* Argument of perigee */
    protected double omega0; /*
					 * Longitude of ascending node of orbit plane at beginning
					 * of week
					 */
    protected double omegaDot; /* Rate of right ascension */
    protected double M0; /* Mean anomaly at reference time */
    protected double deltaN; /* Mean motion difference from computed value */
    protected double crc, crs, cuc, cus, cic, cis; /*
										 * Amplitude of second-order harmonic
										 * perturbations
										 */
    protected double fitInt; /* Fit interval */


    public EphemerisGPS()
    {

    }

    /**
     * @return the week
     */
    public int getWeek() {
        return week;
    }
    /**
     * @param week the week to set
     */
    public void setWeek(int week) {
        this.week = week;
    }
    /**
     * @return the l2Code
     */
    public int getL2Code() {
        return L2Code;
    }
    /**
     * @param l2Code the l2Code to set
     */
    public void setL2Code(int l2Code) {
        L2Code = l2Code;
    }
    /**
     * @return the l2Flag
     */
    public int getL2Flag() {
        return L2Flag;
    }
    /**
     * @param l2Flag the l2Flag to set
     */
    public void setL2Flag(int l2Flag) {
        L2Flag = l2Flag;
    }
    /**
     * @return the svAccur
     */
    public int getSvAccur() {
        return svAccur;
    }
    /**
     * @param svAccur the svAccur to set
     */
    public void setSvAccur(int svAccur) {
        this.svAccur = svAccur;
    }
    /**
     * @return the svHealth
     */
    public int getSvHealth() {
        return svHealth;
    }
    /**
     * @param svHealth the svHealth to set
     */
    public void setSvHealth(int svHealth) {
        this.svHealth = svHealth;
    }
    /**
     * @return the iode
     */
    public int getIode() {
        return iode;
    }
    /**
     * @param iode the iode to set
     */
    public void setIode(int iode) {
        this.iode = iode;
    }
    /**
     * @return the iodc
     */
    public int getIodc() {
        return iodc;
    }
    /**
     * @param iodc the iodc to set
     */
    public void setIodc(int iodc) {
        this.iodc = iodc;
    }
    /**
     * @return the toc
     */
    public double getToc() {
        return toc;
    }
    /**
     * @param toc the toc to set
     */
    public void setToc(double toc) {
        this.toc = toc;
    }
    /**
     * @return the toe
     */
    public double getToe() {
        return toe;
    }
    /**
     * @param toe the toe to set
     */
    public void setToe(double toe) {
        this.toe = toe;
    }
    /**
     * @return the tom
     */
    public double getTom() {
        return tom;
    }
    /**
     * @param tom the tom to set
     */
    public void setTom(double tom) {
        this.tom = tom;
    }
    /**
     * @return the af0
     */
    public double getAf0() {
        return af0;
    }
    /**
     * @param af0 the af0 to set
     */
    public void setAf0(double af0) {
        this.af0 = af0;
    }
    /**
     * @return the af1
     */
    public double getAf1() {
        return af1;
    }
    /**
     * @param af1 the af1 to set
     */
    public void setAf1(double af1) {
        this.af1 = af1;
    }
    /**
     * @return the af2
     */
    public double getAf2() {
        return af2;
    }
    /**
     * @param af2 the af2 to set
     */
    public void setAf2(double af2) {
        this.af2 = af2;
    }
    /**
     * @return the tgd
     */
    public double getTgd() {
        return tgd;
    }
    /**
     * @param tgd the tgd to set
     */
    public void setTgd(double tgd) {
        this.tgd = tgd;
    }
    /**
     * @return the rootA
     */
    public double getRootA() {
        return rootA;
    }
    /**
     * @param rootA the rootA to set
     */
    public void setRootA(double rootA) {
        this.rootA = rootA;
    }
    /**
     * @return the e
     */
    public double getE() {
        return e;
    }
    /**
     * @param e the e to set
     */
    public void setE(double e) {
        this.e = e;
    }
    /**
     * @return the i0
     */
    public double getI0() {
        return i0;
    }
    /**
     * @param i0 the i0 to set
     */
    public void setI0(double i0) {
        this.i0 = i0;
    }
    /**
     * @return the iDot
     */
    public double getiDot() {
        return iDot;
    }
    /**
     * @param iDot the iDot to set
     */
    public void setiDot(double iDot) {
        this.iDot = iDot;
    }
    /**
     * @return the omega
     */
    public double getOmega() {
        return omega;
    }
    /**
     * @param omega the omega to set
     */
    public void setOmega(double omega) {
        this.omega = omega;
    }
    /**
     * @return the omega0
     */
    public double getOmega0() {
        return omega0;
    }
    /**
     * @param omega0 the omega0 to set
     */
    public void setOmega0(double omega0) {
        this.omega0 = omega0;
    }
    /**
     * @return the omegaDot
     */
    public double getOmegaDot() {
        return omegaDot;
    }
    /**
     * @param omegaDot the omegaDot to set
     */
    public void setOmegaDot(double omegaDot) {
        this.omegaDot = omegaDot;
    }
    /**
     * @return the m0
     */
    public double getM0() {
        return M0;
    }
    /**
     * @param m0 the m0 to set
     */
    public void setM0(double m0) {
        M0 = m0;
    }
    /**
     * @return the deltaN
     */
    public double getDeltaN() {
        return deltaN;
    }
    /**
     * @param deltaN the deltaN to set
     */
    public void setDeltaN(double deltaN) {
        this.deltaN = deltaN;
    }
    /**
     * @return the crc
     */
    public double getCrc() {
        return crc;
    }
    /**
     * @param crc the crc to set
     */
    public void setCrc(double crc) {
        this.crc = crc;
    }
    /**
     * @return the crs
     */
    public double getCrs() {
        return crs;
    }
    /**
     * @param crs the crs to set
     */
    public void setCrs(double crs) {
        this.crs = crs;
    }
    /**
     * @return the cuc
     */
    public double getCuc() {
        return cuc;
    }
    /**
     * @param cuc the cuc to set
     */
    public void setCuc(double cuc) {
        this.cuc = cuc;
    }
    /**
     * @return the cus
     */
    public double getCus() {
        return cus;
    }
    /**
     * @param cus the cus to set
     */
    public void setCus(double cus) {
        this.cus = cus;
    }
    /**
     * @return the cic
     */
    public double getCic() {
        return cic;
    }
    /**
     * @param cic the cic to set
     */
    public void setCic(double cic) {
        this.cic = cic;
    }
    /**
     * @return the cis
     */
    public double getCis() {
        return cis;
    }
    /**
     * @param cis the cis to set
     */
    public void setCis(double cis) {
        this.cis = cis;
    }
    /**
     * @return the fitInt
     */
    public double getFitInt() {
        return fitInt;
    }
    /**
     * @param fitInt the fitInt to set
     */
    public void setFitInt(double fitInt) {
        this.fitInt = fitInt;
    }


    @Override
    public SatellitePosition computeSatPosition(Observations obs) {
        long unixTime = obs.getRefTime().getMsec();
        double obsPseudorange = obs.getSatByIDType(satID, satType).getPseudorange(0);

//		char satType2 = eph.getSatType() ;
//					System.out.println("### other than GLONASS data");

            // Compute satellite clock error
            double satelliteClockError = computeSatelliteClockError(unixTime, obsPseudorange);

            // Compute clock corrected transmission time
            double tGPS = computeClockCorrectedTransmissionTime(unixTime, satelliteClockError, obsPseudorange);

            // Compute eccentric anomaly
            double Ek = computeEccentricAnomaly(tGPS);

            // Semi-major axis
            double A = this.getRootA() * this.getRootA();

            // Time from the ephemerides reference epoch
            double tk = checkGpsTime(tGPS - this.getToe());

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
                    + (this.getOmegaDot() - Constants.EARTH_ANGULAR_VELOCITY) * tk
                    - Constants.EARTH_ANGULAR_VELOCITY * this.getToe();
            Omega = Math.IEEEremainder(Omega + 2 * Math.PI, 2 * Math.PI);
            double x1 = Math.cos(u) * r;
            double y1 = Math.sin(u) * r;

            // Coordinates
            //			double[][] data = new double[3][1];
            //			data[0][0] = x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega);
            //			data[1][0] = x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega);
            //			data[2][0] = y1 * Math.sin(ik);

            // Fill in the satellite position matrix
            //this.coord.ecef = new SimpleMatrix(data);
            //this.coord = Coordinates.globalXYZInstance(new SimpleMatrix(data));
            SatellitePosition sp = new SatellitePosition(unixTime,satID, satType, x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
                    x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
                    y1 * Math.sin(ik));
        sp.setSatelliteClockError(satelliteClockError);
            return sp;
    }

    @Override
    public double computeSatelliteClockError(long unixTime,double obsPseudorange) {
        double gpsTime = (new Time(unixTime)).getGpsTime();
        // Remove signal travel time from observation time
        double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

        // Compute eccentric anomaly
        double Ek = computeEccentricAnomaly(tRaw);

        // Relativistic correction term computation
        double dtr = Constants.RELATIVISTIC_ERROR_CONSTANT * this.getE() * this.getRootA() * Math.sin(Ek);

        // Clock error computation
        double dt = checkGpsTime(tRaw - this.getToc());
        double timeCorrection = (this.getAf2() * dt + this.getAf1()) * dt + this.getAf0() + dtr - this.getTgd();
        double tGPS = tRaw - timeCorrection;
        dt = checkGpsTime(tGPS - this.getToc());
        timeCorrection = (this.getAf2() * dt + this.getAf1()) * dt + this.getAf0() + dtr - this.getTgd();

        return timeCorrection;
    }


    /**
     * @param time
     *            (GPS time in seconds)
     * @return Eccentric anomaly 偏近点角
     */
    protected double computeEccentricAnomaly(double time) {

        // Semi-major axis
        double A = this.getRootA() * this.getRootA();

        // Time from the ephemerides reference epoch
        double tk = checkGpsTime(time - this.getToe());

        // Computed mean motion [rad/sec]
        double n0 = Math.sqrt(Constants.EARTH_GRAVITATIONAL_CONSTANT / Math.pow(A, 3));

        // Corrected mean motion [rad/sec]
        double n = n0 + this.getDeltaN();

        // Mean anomaly
        double Mk = this.getM0() + n * tk;

        // Eccentric anomaly starting value
        Mk = Math.IEEEremainder(Mk + 2 * Math.PI, 2 * Math.PI);
        double Ek = Mk;

        int i;
        double EkOld, dEk;

        // Eccentric anomaly iterative computation
        int maxNumIter = 12;
        for (i = 0; i < maxNumIter; i++) {
            EkOld = Ek;
            Ek = Mk + this.getE() * Math.sin(Ek);
            dEk = Math.IEEEremainder(Ek - EkOld, 2 * Math.PI);
            if (Math.abs(dEk) < 1e-12)
                break;
        }

        // TODO Display/log warning message
        if (i == maxNumIter)
            System.out.println("Warning: Eccentric anomaly does not converge.");

        return Ek;

    }

}
