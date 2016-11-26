package org.von.rtdlib.positioning.models;

import org.ejml.simple.SimpleMatrix;
import org.von.rtdlib.positioning.*;
import org.von.rtdlib.provider.EphemerisProvider;

import java.util.Arrays;

/**
 * Created by diner on 16-10-21.
 */
public class EphemerisGLONASS extends EphemerisProvider {


    private int week; /* GPS week number */
    private double toc; /* clock data reference time */
    private double tow;
    private double toe; /* ephemeris reference time */

    /* for GLONASS data */
    private float tauN;
    private float gammaN;
    private double tk;

    private double X;
    private double Xv;
    private double Xa;
    private double Bn;

    private double Y;
    private double Yv;
    private double Ya;
    private int freq_num;
    private double tb;

    private double Z;
    private double Zv;
    private double Za;
    private double En;

    /* for GLONASS data */


    public EphemerisGLONASS()
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
     * @return the tow
     */
    public double getTow() {
        return tow;
    }
    /**
     * @param tow the tow to set
     */
    public void setTow(double tow) {
        this.tow = tow;
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

    public float getTauN() {
        return tauN;
    }
    public void setTauN(float tauN) {
        this.tauN = tauN;
    }

    public float getGammaN() {
        return gammaN;
    }
    public void setGammaN(float gammaN) {
        this.gammaN = gammaN;
    }

    public double gettk() {
        return tk;
    }
    public void settk(double tk) {
        this.tk = tk;
    }

    public double getX() {
        return X;
    }
    public void setX(double X) {
        this.X = X;
    }

    public double getXv() {
        return Xv;
    }
    public void setXv(double Xv) {
        this.Xv = Xv;
    }

    public double getXa() {
        return Xa;
    }
    public void setXa(double Xa) {
        this.Xa = Xa;
    }

    public double getBn() {
        return Bn;
    }
    public void setBn(double Bn) {
        this.Bn = Bn;
    }

    public double getY() {
        return Y;
    }
    public void setY(double Y) {
        this.Y = Y;
    }

    public double getYv() {
        return Yv;
    }
    public void setYv(double Yv) {
        this.Yv = Yv;
    }

    public double getYa() {
        return Ya;
    }
    public void setYa(double Ya) {
        this.Ya = Ya;
    }

    public int getfreq_num() {
        return freq_num;
    }
    public void setfreq_num(int freq_num) {
        this.freq_num = freq_num;
    }

    public double gettb() {
        return tb;
    }
    public void settb(double tb) {
        this.tb = tb;
    }

    public double getZ() {
        return Z;
    }
    public void setZ(double Z) {
        this.Z = Z;
    }

    public double getZv() {
        return Zv;
    }
    public void setZv(double Zv) {
        this.Zv = Zv;
    }

    public double getZa() {
        return Za;
    }
    public void setZa(double Za) {
        this.Za = Za;
    }

    public double getEn() {
        return En;
    }
    public void setEn(double En) {
        this.En = En;
    }

    @Override
    public SatellitePosition computeSatPosition(Observations obs) {
        long unixTime = obs.getRefTime().getMsec();
        double obsPseudorange = obs.getSatByIDType(satID, satType).getPseudorange(0);


        satID = this.getSatID();
        double X = this.getX();  // satellite X coordinate at ephemeris reference time
        double Y = this.getY();  // satellite Y coordinate at ephemeris reference time
        double Z = this.getZ();  // satellite Z coordinate at ephemeris reference time
        double Xv = this.getXv();  // satellite velocity along X at ephemeris reference time
        double Yv = this.getYv();  // satellite velocity along Y at ephemeris reference time
        double Zv = this.getZv();  // satellite velocity along Z at ephemeris reference time
        double Xa = this.getXa();  // acceleration due to lunar-solar gravitational perturbation along X at ephemeris reference time
        double Ya = this.getYa();  // acceleration due to lunar-solar gravitational perturbation along Y at ephemeris reference time
        double Za = this.getZa();  // acceleration due to lunar-solar gravitational perturbation along Z at ephemeris reference time
					/* NOTE:  Xa,Ya,Za are considered constant within the integration interval (i.e. toe ?}15 minutes) */

        double tn = this.getTauN();
        float gammaN = this.getGammaN();
        double tk = this.gettk();
        double En = this.getEn();
        double toc = this.getToc();
        double toe = this.getToe();
        int freqNum = this.getfreq_num();

        obs.getSatByIDType(satID, satType).setFreqNum(freqNum);

					/*
					String refTime = eph.getRefTime().toString();
//					refTime = refTime.substring(0,10);
					refTime = refTime.substring(0,19);
//					refTime = refTime + " 00 00 00";
					System.out.println("refTime: " + refTime);

					try {
							// Set GMT time zone
							TimeZone zone = TimeZone.getTimeZone("GMT Time");
//							TimeZone zone = TimeZone.getTimeZone("UTC+4");
							DateFormat df = new java.text.SimpleDateFormat("yyyy MM dd HH mm ss");
							df.setTimeZone(zone);

							long ut = df.parse(refTime).getTime() ;
							System.out.println("ut: " + ut);
							Time tm = new Time(ut);
							double gpsTime = tm.getGpsTime();
	//						double gpsTime = tm.getRoundedGpsTime();
							System.out.println("gpsT: " + gpsTime);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/


//					System.out.println("refTime: " + refTime);
//					System.out.println("toc: " + toc);
//					System.out.println("toe: " + toe);
//					System.out.println("unixTime: " + unixTime);
//					System.out.println("satID: " + satID);
//					System.out.println("X: " + X);
//					System.out.println("Y: " + Y);
//					System.out.println("Z: " + Z);
//					System.out.println("Xv: " + Xv);
//					System.out.println("Yv: " + Yv);
//					System.out.println("Zv: " + Zv);
//					System.out.println("Xa: " + Xa);
//					System.out.println("Ya: " + Ya);
//					System.out.println("Za: " + Za);
//					System.out.println("tn: " + tn);
//					System.out.println("gammaN: " + gammaN);
////					System.out.println("tb: " + tb);
//					System.out.println("tk: " + tk);
//					System.out.println("En: " + En);
//					System.out.println("					");

					/* integration step */
        int int_step = 60 ; // [s]

					/* Compute satellite clock error */
        double satelliteClockError = computeSatelliteClockError(unixTime, obsPseudorange);
//				    System.out.println("satelliteClockError: " + satelliteClockError);

					/* Compute clock corrected transmission time */
        double tGPS = computeClockCorrectedTransmissionTime(unixTime, satelliteClockError, obsPseudorange);
//				    System.out.println("tGPS: " + tGPS);

				    /* Time from the ephemerides reference epoch */
        Time reftime = new Time(this.getWeek(), tGPS);
        double tk2 = checkGpsTime(tGPS - toe - reftime.getLeapSeconds());
//					System.out.println("tk2: " + tk2);

				    /* number of iterations on "full" steps */
        int n = (int) Math.floor(Math.abs(tk2 / int_step));
//					System.out.println("Number of iterations: " + n);

					/* array containing integration steps (same sign as tk) */
        double[] array = new double[n];
        Arrays.fill(array, 1);
        SimpleMatrix tkArray = new SimpleMatrix(n, 1, true, array);

//					SimpleMatrix tkArray2  = tkArray.scale(2);
        tkArray = tkArray.scale(int_step);
        tkArray = tkArray.scale(tk2/Math.abs(tk2));
//					tkArray.print();
        //double ii = tkArray * int_step * (tk2/Math.abs(tk2));

					/* check residual iteration step (i.e. remaining fraction of int_step) */
        double int_step_res = tk2 % int_step;
//				    System.out.println("int_step_res: " + int_step_res);

        double[] intStepRes = new double[]{int_step_res};
        SimpleMatrix int_stepArray = new SimpleMatrix(1, 1, false, intStepRes);
//					int_stepArray.print();

					/* adjust the total number of iterations and the array of iteration steps */
        if (int_step_res != 0){
            tkArray = tkArray.combine(n, 0, int_stepArray);
//				        tkArray.print();
            n = n + 1;
            // tkArray = [ii; int_step_res];
        }
//				    System.out.println("n: " + n);

        // numerical integration steps (i.e. re-calculation of satellite positions from toe to tk)
        double[] pos = {X, Y, Z};
        double[] vel = {Xv, Yv, Zv};
        double[] acc = {Xa, Ya, Za};
        double[] pos1;
        double[] vel1;

        SimpleMatrix posArray = new SimpleMatrix(1, 3, true, pos);
        SimpleMatrix velArray = new SimpleMatrix(1, 3, true, vel);
        SimpleMatrix accArray = new SimpleMatrix(1, 3, true, acc);
        SimpleMatrix pos1Array;
        SimpleMatrix vel1Array;
        SimpleMatrix pos2Array;
        SimpleMatrix vel2Array;
        SimpleMatrix pos3Array;
        SimpleMatrix vel3Array;
        SimpleMatrix pos4Array;
        SimpleMatrix vel4Array;
        SimpleMatrix pos1dotArray;
        SimpleMatrix vel1dotArray;
        SimpleMatrix pos2dotArray;
        SimpleMatrix vel2dotArray;
        SimpleMatrix pos3dotArray;
        SimpleMatrix vel3dotArray;
        SimpleMatrix pos4dotArray;
        SimpleMatrix vel4dotArray;
        SimpleMatrix subPosArray;
        SimpleMatrix subVelArray;

        for (int i = 0 ; i < n ; i++ ){

							/* Runge-Kutta numerical integration algorithm */
            // step 1
            pos1Array = posArray;
            //pos1 = pos;
            vel1Array = velArray;
            //vel1 = vel;

            // differential position
            pos1dotArray = velArray;
            //double[] pos1_dot = vel;
            vel1dotArray = satellite_motion_diff_eq(pos1Array, vel1Array, accArray, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
            //double[] vel1_dot = satellite_motion_diff_eq(pos1, vel1, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							vel1dotArray.print();

            // step 2
            pos2Array = pos1dotArray.scale(tkArray.get(i)).divide(2);
            pos2Array = posArray.plus(pos2Array);
            //double[] pos2 = pos + pos1_dot*ii(i)/2;
//							System.out.println("## pos2Array: " ); pos2Array.print();

            vel2Array = vel1dotArray.scale(tkArray.get(i)).divide(2);
            vel2Array = velArray.plus(vel2Array);
            //double[] vel2 = vel + vel1_dot * tkArray.get(i)/2;
//							System.out.println("## vel2Array: " ); vel2Array.print();

            pos2dotArray = vel2Array;
            //double[] pos2_dot = vel2;
            vel2dotArray = satellite_motion_diff_eq(pos2Array, vel2Array, accArray, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
            //double[] vel2_dot = satellite_motion_diff_eq(pos2, vel2, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							System.out.println("## vel2dotArray: " ); vel2dotArray.print();

            // step 3
            pos3Array = pos2dotArray.scale(tkArray.get(i)).divide(2);
            pos3Array = posArray.plus(pos3Array);
//							double[] pos3 = pos + pos2_dot * tkArray.get(i)/2;
//							System.out.println("## pos3Array: " ); pos3Array.print();

            vel3Array = vel2dotArray.scale(tkArray.get(i)).divide(2);
            vel3Array = velArray.plus(vel3Array);
//					        double[] vel3 = vel + vel2_dot * tkArray.get(i)/2;
//							System.out.println("## vel3Array: " ); vel3Array.print();

            pos3dotArray = vel3Array;
            //double[] pos3_dot = vel3;
            vel3dotArray = satellite_motion_diff_eq(pos3Array, vel3Array, accArray, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
            //double[] vel3_dot = satellite_motion_diff_eq(pos3, vel3, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							System.out.println("## vel3dotArray: " ); vel3dotArray.print();

            // step 4
            pos4Array = pos3dotArray.scale(tkArray.get(i));
            pos4Array = posArray.plus(pos4Array);
            //double[] pos4 = pos + pos3_dot * tkArray.get(i);
//							System.out.println("## pos4Array: " ); pos4Array.print();

            vel4Array = vel3dotArray.scale(tkArray.get(i));
            vel4Array = velArray.plus(vel4Array);
            //double[] vel4 = vel + vel3_dot * tkArray.get(i);
//							System.out.println("## vel4Array: " ); vel4Array.print();

            pos4dotArray = vel4Array;
            //double[] pos4_dot = vel4;
            vel4dotArray = satellite_motion_diff_eq(pos4Array, vel4Array, accArray, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
            //double[] vel4_dot = satellite_motion_diff_eq(pos4, vel4, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							System.out.println("## vel4dotArray: " ); vel4dotArray.print();

            // final position and velocity
            subPosArray = pos1dotArray.plus(pos2dotArray.scale(2)).plus(pos3dotArray.scale(2)).plus(pos4dotArray);
            subPosArray = subPosArray.scale(tkArray.get(i)).divide(6);
            posArray = posArray.plus(subPosArray) ;
            //pos = pos + (pos1_dot + 2*pos2_dot + 2*pos3_dot + pos4_dot)*ii(s)/6;
//							System.out.println("## posArray: " ); posArray.print();

            subVelArray = vel1dotArray.plus(vel2dotArray.scale(2)).plus(vel3dotArray.scale(2)).plus(vel4dotArray);
            subVelArray = subVelArray.scale(tkArray.get(i)).divide(6);
            velArray = velArray.plus(subVelArray) ;
            //vel = vel + (vel1_dot + 2*vel2_dot + 2*vel3_dot + vel4_dot)*ii(s)/6;
//							System.out.println("## velArray: " ); velArray.print();
//							System.out.println(" " );


        }

					/* transformation from PZ-90.02 to WGS-84 (G1150) */
        double x1 = posArray.get(0) - 0.36;
        double y1 = posArray.get(1) + 0.08;
        double z1 = posArray.get(2) + 0.18;

					/* satellite velocity */
        double Xv1 = velArray.get(0);
        double Yv1 = velArray.get(1);
        double Zv1 = velArray.get(2);

					/* Fill in the satellite position matrix */
        SatellitePosition sp = new SatellitePosition(unixTime,satID, satType, x1, y1, z1);
        sp.setSatelliteClockError(satelliteClockError);
//
//					/* Apply the correction due to the Earth rotation during signal travel time */
        //SimpleMatrix R = computeEarthRotationCorrection(unixTime, receiverClockError, tGPS);
        //sp.setSMMultXYZ(R);

        return sp ;
    }


    private SimpleMatrix satellite_motion_diff_eq(SimpleMatrix pos1Array,
                                                  SimpleMatrix vel1Array, SimpleMatrix accArray, long ellAGlo,
                                                  double gmGlo, double j2Glo, double omegaeDotGlo) {
        // TODO Auto-generated method stub

		/* renaming variables for better readability position */
        double X = pos1Array.get(0);
        double Y = pos1Array.get(1);
        double Z = pos1Array.get(2);

//		System.out.println("X: " + X);
//		System.out.println("Y: " + Y);
//		System.out.println("Z: " + Z);

		/* velocity */
        double Xv = vel1Array.get(0);
        double Yv = vel1Array.get(1);

//		System.out.println("Xv: " + Xv);
//		System.out.println("Yv: " + Yv);

		/* acceleration (i.e. perturbation) */
        double Xa = accArray.get(0);
        double Ya = accArray.get(1);
        double Za = accArray.get(2);

//		System.out.println("Xa: " + Xa);
//		System.out.println("Ya: " + Ya);
//		System.out.println("Za: " + Za);

		/* parameters */
        double r = Math.sqrt(Math.pow(X,2) + Math.pow(Y,2) + Math.pow(Z,2));
        double g = -gmGlo/Math.pow(r,3);
        double h = j2Glo*1.5*Math.pow((ellAGlo/r),2);
        double k = 5*Math.pow(Z,2)/Math.pow(r,2);

//		System.out.println("r: " + r);
//		System.out.println("g: " + g);
//		System.out.println("h: " + h);
//		System.out.println("k: " + k);

		/* differential velocity */
        double[] vel_dot = new double[3] ;
        vel_dot[0] = g*X*(1 - h*(k - 1)) + Xa + Math.pow(omegaeDotGlo,2)*X + 2*omegaeDotGlo*Yv;
//		System.out.println("vel1: " + vel_dot[0]);

        vel_dot[1] = g*Y*(1 - h*(k - 1)) + Ya + Math.pow(omegaeDotGlo,2)*Y - 2*omegaeDotGlo*Xv;
//		System.out.println("vel2: " + vel_dot[1]);

        vel_dot[2] = g*Z*(1 - h*(k - 3)) + Za;
//		System.out.println("vel3: " + vel_dot[2]);

        SimpleMatrix velDotArray = new SimpleMatrix(1, 3, true, vel_dot);
//		velDotArray.print();

        return velDotArray;
    }

    @Override
    public double computeSatelliteClockError(long unixTime, double obsPseudorange) {

        double gpsTime = (new Time(unixTime)).getGpsTime();
//				System.out.println("gpsTime: " + gpsTime);
//				System.out.println("obsPseudorange: " + obsPseudorange);

        // Remove signal travel time from observation time
        double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);
//				System.out.println("tRaw: " + tRaw);

        // Clock error computation
        double dt = checkGpsTime(tRaw - this.getToe());
//				System.out.println("dt: " + dt);

        double timeCorrection =  this.getTauN() + this.getGammaN() * dt ;
//				double timeCorrection =  - eph.getTauN() + eph.getGammaN() * dt ;

        return timeCorrection;
    }


   }
