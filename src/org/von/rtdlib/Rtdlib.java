package org.von.rtdlib;

import org.ejml.simple.SimpleMatrix;
import org.von.rtdlib.consumer.PositionConsumer;
import org.von.rtdlib.positioning.*;
import org.von.rtdlib.producer.TxtProducer;
import org.von.rtdlib.provider.EphemerisProvider;
import org.von.rtdlib.provider.NavigationProvider;
import org.von.rtdlib.provider.ObservationsProvider;
import org.von.rtdlib.provider.PseudoRangeCorrectionProvider;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by diner on 16-10-16.
 */
public class Rtdlib {

    // Frequency selector
    /** The Constant FREQ_L1. */
    public final static int FREQ_L1 = ObservationSet.L1;

    /** The Constant FREQ_L2. */
    public final static int FREQ_L2 = ObservationSet.L2;

    /** The freq. */
    private int freq = FREQ_L1;

    /** The Constant DYN_MODEL_STATIC. */
    public final static int DYN_MODEL_STATIC = 1;

    /** The Constant DYN_MODEL_CONST_SPEED. */
    public final static int DYN_MODEL_CONST_SPEED = 2;

    private NavigationProvider navigation;

    /** The rover in. */
    private ObservationsProvider roverIn;

    private PseudoRangeCorrectionProvider prcIn;

    private ReceiverPosition roverPos = null;

    private boolean validPosition = false;

    private Vector<PositionConsumer> positionConsumers = new Vector<PositionConsumer>();

    private int dynamicModel;

    private boolean debug = true;

    char satType;

    private double cutoff = 15; // Elevation cutoff


    // Weighting strategy
    // 0 = same weight for all observations
    // 1 = weight based on satellite elevation
    // 2 = weight based on signal-to-noise ratio
    // 3 = weight based on combined elevation and signal-to-noise ratio
    /** The Constant WEIGHT_EQUAL. */
    public final static int WEIGHT_EQUAL = 0;

    /** The Constant WEIGHT_SAT_ELEVATION. */
    public final static int WEIGHT_SAT_ELEVATION = 1;

    /** The Constant WEIGHT_SIGNAL_TO_NOISE_RATIO. */
    public final static int WEIGHT_SIGNAL_TO_NOISE_RATIO = 2;

    /** The Constant WEIGHT_COMBINED_ELEVATION_SNR. */
    public final static int WEIGHT_COMBINED_ELEVATION_SNR = 3;

    /** The weights. */
    private int weights = WEIGHT_SAT_ELEVATION;

    private ArrayList<Integer> satAvail; /* List of satellites available for processing */
    private ArrayList<Character> satTypeAvail; /* List of satellite Types available for processing */
    private ArrayList<String> gnssAvail;  /* List of satellite Types & Id available for processing */

    private ArrayList<Integer> satAvailPhase; /* List of satellites available for processing */
    private ArrayList<Character> satTypeAvailPhase; /* List of satellite Type available for processing */
    private ArrayList<String> gnssAvailPhase;  /* List of satellite Types & Id available for processing */

    private SatellitePosition[] pos; /* Absolute position of all visible satellites (ECEF) */


    private TopocentricCoordinates[] roverTopo;
    // Fields related to receiver-satellite geometry
    private SimpleMatrix[] diffRoverSat; /* Rover-satellite vector */
    private double[] roverSatAppRange; /* Rover-satellite approximate range */
    private double[] roverSatTropoCorr; /* Rover-satellite troposphere correction */
    private double[] roverSatIonoCorr; /* Rover-satellite ionosphere correction */
    private double[] roverSatEarthCorr; /* Rover-satellite Earth Rotation correction */
    private double[] pseudoRangeCorr; /* Rover-satellite pseudorange  correction */


    public Rtdlib(NavigationProvider navigation, ObservationsProvider roverIn) {
//        stDevCodeP = new double[2];
//        stDevCodeP[0] = 0.6;
//        stDevCodeP[1] = 0.4;

        this.navigation = navigation;

        this.roverIn = roverIn;

        validPosition = false;
    }

    public Rtdlib(NavigationProvider navigation, ObservationsProvider roverIn,PseudoRangeCorrectionProvider prcIn) {
//        stDevCodeP = new double[2];
//        stDevCodeP[0] = 0.6;
//        stDevCodeP[1] = 0.4;

        this.navigation = navigation;

        this.roverIn = roverIn;

        this.prcIn = prcIn;

        validPosition = false;
    }

    public void addPositionConsumerListener(TxtProducer txt) {
    }

    public void setDynamicModel(int dynamicModel) {
        this.dynamicModel = dynamicModel;
    }

    public void runDGNSS() {
        try {
            runDGNSS(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void runDGNSS(double stopAtDopThreshold) throws Exception {

        roverPos = new ReceiverPosition();

        try {
            Observations obsR = roverIn.getNextObservations();
            while (obsR!=null) { // buffStreamObs.ready()
//				if(debug) System.out.println("OK ");

                //try{
                // If there are at least four satellites
                if (obsR.getNumSat() >= 4) { // gps.length
                    if(debug) System.out.println("Total number of satellites: "+obsR.getNumSat());

                    // Compute approximate positioning by iterative least-squares
                    for (int iter = 0; iter < 3; iter++) {
                        // Select all satellites
                        selectSatellitesDGNSS(obsR, -100);
                        if (getSatAvailNumber() >= 4) {
                            SinglePointPositioningWithPRC(obsR, false, true);
                        }
                    }

                    // If an approximate position was computed
                    if(debug) System.out.println("Valid approximate position? "+roverPos.isValidXYZ()+" X:"+roverPos.getX()+" Y:"+roverPos.getY()+" Z:"+roverPos.getZ());

                    if (roverPos.isValidXYZ()) {
                        // Select available satellites
                        selectSatellitesDGNSS(obsR);

                        if (getSatAvailNumber() >= 4){
                            if(debug) System.out.println("Number of selected satellites: " + getSatAvailNumber());
                            // Compute code stand-alone positioning (epoch-by-epoch solution)
                            SinglePointPositioningWithPRC(obsR, false, true);
                        }
                        else
                            // Discard approximate positioning
                            roverPos.setXYZ(0, 0, 0);
                    }

                    if(debug)System.out.println("Valid LS position? "+roverPos.isValidXYZ()+" X:"+roverPos.getX()+" Y:"+roverPos.getY()+" Z:"+roverPos.getZ());
                    if (roverPos.isValidXYZ()) {
                        if(!validPosition){
                            notifyPositionConsumerEvent(PositionConsumer.EVENT_START_OF_TRACK);
                            validPosition = true;
                        }else{
                            ReceiverPosition coord = new ReceiverPosition(roverPos, ReceiverPosition.DOP_TYPE_STANDARD, roverPos.getpDop(), roverPos.gethDop(), roverPos.getvDop());

                            if(positionConsumers.size()>0){
                                coord.setRefTime(new Time(obsR.getRefTime().getMsec()));
                                notifyPositionConsumerAddCoordinate(coord);
                            }
                            if(debug)System.out.println("PDOP: "+roverPos.getpDop());
                            if(debug)System.out.println("------------------------------------------------------------");
                            if(stopAtDopThreshold>0.0 && roverPos.getpDop()<stopAtDopThreshold){
                                return;
                            }
                        }
                    }
                }
//				}catch(Exception e){
//					System.out.println("Could not complete due to "+e);
//					e.printStackTrace();
//				}
                obsR = roverIn.getNextObservations();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            notifyPositionConsumerEvent(PositionConsumer.EVENT_END_OF_TRACK);
        }
        return;
    }

    public void runSPP() {
        try {
            runSPP(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void runSPP(double stopAtDopThreshold) throws Exception {

        roverPos = new ReceiverPosition();

        try {
            Observations obsR = roverIn.getNextObservations();
            while (obsR!=null) { // buffStreamObs.ready()
//				if(debug) System.out.println("OK ");

                //try{
                // If there are at least four satellites
                if (obsR.getNumSat() >= 4) { // gps.length
                    if(debug) System.out.println("Total number of satellites: "+obsR.getNumSat());

                    // Compute approximate positioning by iterative least-squares
                    for (int iter = 0; iter < 3; iter++) {
                        // Select all satellites
                        selectSatellitesStandalone(obsR, -100);
                        if (getSatAvailNumber() >= 4) {
                            SinglePointPositioning(obsR, false, true);
                        }
                    }

                    // If an approximate position was computed
                    if(debug) System.out.println("Valid approximate position? "+roverPos.isValidXYZ()+" X:"+roverPos.getX()+" Y:"+roverPos.getY()+" Z:"+roverPos.getZ());

                    if (roverPos.isValidXYZ()) {
                        // Select available satellites
                        selectSatellitesStandalone(obsR);

                        if (getSatAvailNumber() >= 4){
                            if(debug) System.out.println("Number of selected satellites: " + getSatAvailNumber());
                            // Compute code stand-alone positioning (epoch-by-epoch solution)
                            SinglePointPositioning(obsR, false, false);
                        }
                        else
                            // Discard approximate positioning
                            roverPos.setXYZ(0, 0, 0);
                    }

                    if(debug)System.out.println("Valid LS position? "+roverPos.isValidXYZ()+" X:"+roverPos.getX()+" Y:"+roverPos.getY()+" Z:"+roverPos.getZ());
                    if (roverPos.isValidXYZ()) {
                        if(!validPosition){
                            notifyPositionConsumerEvent(PositionConsumer.EVENT_START_OF_TRACK);
                            validPosition = true;
                        }else{
                            ReceiverPosition coord = new ReceiverPosition(roverPos, ReceiverPosition.DOP_TYPE_STANDARD, roverPos.getpDop(), roverPos.gethDop(), roverPos.getvDop());

                            if(positionConsumers.size()>0){
                                coord.setRefTime(new Time(obsR.getRefTime().getMsec()));
                                notifyPositionConsumerAddCoordinate(coord);
                            }
                            if(debug)System.out.println("PDOP: "+roverPos.getpDop());
                            if(debug)System.out.println("------------------------------------------------------------");
                            if(stopAtDopThreshold>0.0 && roverPos.getpDop()<stopAtDopThreshold){
                                return;
                            }
                        }
                    }
                }
//				}catch(Exception e){
//					System.out.println("Could not complete due to "+e);
//					e.printStackTrace();
//				}
                obsR = roverIn.getNextObservations();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            notifyPositionConsumerEvent(PositionConsumer.EVENT_END_OF_TRACK);
        }
        return;
    }

    public String getAvailGnssSystems(){
        if(satTypeAvail.isEmpty()) return "";
        String GnssSys = "";
        for(int i=0;i<satTypeAvail.size();i++) {
            if (GnssSys.indexOf((satTypeAvail.get(i))) < 0)
                GnssSys = GnssSys + satTypeAvail.get(i);
        }
        return GnssSys;
    }

    public void SinglePointPositioning(Observations roverObs, boolean estimateOnlyClock, boolean ignoreTopocentricParameters) {

        // Number of GNSS observations without cutoff
        int nObs = roverObs.getNumSat();

        // Number of unknown parameters
        int nUnknowns = 4;

        // Add one unknown for each constellation in addition to the first (to estimate Inter-System Biases - ISBs)
        String sys = getAvailGnssSystems();
        if (sys.length()>0) {
            sys = sys.substring(1);
            nUnknowns = nUnknowns + sys.length();
        }

        // Define least squares matrices
        SimpleMatrix A;
        SimpleMatrix b;
        SimpleMatrix y0;
        SimpleMatrix Q;
        SimpleMatrix x;
        SimpleMatrix vEstim;
        SimpleMatrix tropoCorr;
        SimpleMatrix ionoCorr;
        SimpleMatrix earthCorr;

        // Covariance matrix obtained from matrix A (satellite geometry) [ECEF coordinates]
        SimpleMatrix covXYZ;
        covXYZ = new SimpleMatrix(3, 3);

        // Covariance matrix obtained from matrix A (satellite geometry) [local coordinates]
        SimpleMatrix covENU;
        covENU = new SimpleMatrix(3, 3);

        // Number of available satellites (i.e. observations)
        int nObsAvail = satAvail.size();

        // Least squares design matrix
        A = new SimpleMatrix(nObsAvail, nUnknowns);

        // Vector for approximate pseudoranges
        b = new SimpleMatrix(nObsAvail, 1);

        // Vector for observed pseudoranges
        y0 = new SimpleMatrix(nObsAvail, 1);

        // Cofactor matrix (initialized to identity)
        Q = SimpleMatrix.identity(nObsAvail);

        // Solution vector
        x = new SimpleMatrix(nUnknowns, 1);

        // Vector for observation error
        vEstim = new SimpleMatrix(nObsAvail, 1);

        // Vectors for troposphere and ionosphere corrections
        tropoCorr = new SimpleMatrix(nObsAvail, 1);

        ionoCorr = new SimpleMatrix(nObsAvail, 1);

        earthCorr = new SimpleMatrix(nObsAvail, 1);


        // Counter for available satellites
        int k = 0;

        // Satellite ID
        int id = 0;

        // Set up the least squares matrices
        for (int i = 0; i < nObs; i++) {

            id = roverObs.getSatID(i);
            char satType = roverObs.getSatType(i);
            String checkAvailGnss = String.valueOf(satType) + String.valueOf(id);

            if (pos[i]!=null && gnssAvail.contains(checkAvailGnss)) {
//			if (pos[i]!=null && satAvail.contains(id)  && satTypeAvail.contains(satType)) {
//				System.out.println("####" + checkAvailGnss  + "####");

                // Fill in one row in the design matrix
                A.set(k, 0, diffRoverSat[i].get(0) / roverSatAppRange[i]); /* X */
                A.set(k, 1, diffRoverSat[i].get(1) / roverSatAppRange[i]); /* Y */
                A.set(k, 2, diffRoverSat[i].get(2) / roverSatAppRange[i]); /* Z */
                A.set(k, 3, 1); /* clock error */
                for (int c = 0; c < sys.length(); c++) {
                    A.set(k, 4+c, sys.indexOf(satType)==c?1:0); /* inter-system bias */
                }

                // Add the approximate pseudorange value to b
                b.set(k, 0, roverSatAppRange[i] - pos[i].getSatelliteClockError() * Constants.SPEED_OF_LIGHT);

                // Add the clock-corrected observed pseudorange value to y0
                y0.set(k, 0, roverObs.getSatByIDType(id, satType).getPseudorange(this.getFreq()));

                if (!ignoreTopocentricParameters) {
                    // Fill in troposphere and ionosphere double differenced
                    // corrections
                    tropoCorr.set(k, 0, roverSatTropoCorr[i]);
                    ionoCorr.set(k, 0, roverSatIonoCorr[i]);
                    earthCorr.set(k,0,roverSatEarthCorr[i]);

                    // Fill in the cofactor matrix
                    double weight = Q.get(k, k)
                            + computeWeight(roverTopo[i].getElevation(),
                            roverObs.getSatByIDType(id, satType).getSignalStrength(getFreq()));
                    Q.set(k, k, weight);
                }

                // Increment available satellites counter
                k++;
            }

        }

        if (!ignoreTopocentricParameters) {
            // Apply troposphere and ionosphere correction
            b = b.plus(tropoCorr);
            b = b.plus(ionoCorr);
            b = b.plus(earthCorr);
        }

        // Least squares solution x = ((A'*Q^-1*A)^-1)*A'*Q^-1*(y0-b);
        x = A.transpose().mult(Q.invert()).mult(A).invert().mult(A.transpose())
                .mult(Q.invert()).mult(y0.minus(b));

        // Receiver clock error
        roverPos.setReceiverClockError(x.get(3) / Constants.SPEED_OF_LIGHT);

        if(estimateOnlyClock)
            return;

        // Receiver position
        //this.coord.ecef.set(this.coord.ecef.plus(x.extractMatrix(0, 3, 0, 1)));
        roverPos.setPlusXYZ(x.extractMatrix(0, 3, 0, 1));

        // Estimation of the variance of the observation error
        vEstim = y0.minus(A.mult(x).plus(b));
        double varianceEstim = (vEstim.transpose().mult(Q.invert())
                .mult(vEstim)).get(0)
                / (nObsAvail - nUnknowns);

        // Covariance matrix of the estimation error
        if (nObsAvail > nUnknowns) {
            SimpleMatrix covariance = A.transpose().mult(Q.invert()).mult(A).invert()
                    .scale(varianceEstim);
            roverPos.setPositionCovariance(covariance.extractMatrix(0, 3, 0, 3));
        }else{
            roverPos.setPositionCovariance(null);
        }

        // Compute covariance matrix from A matrix [ECEF reference system]
        covXYZ = A.transpose().mult(A).invert();
        covXYZ = covXYZ.extractMatrix(0, 3, 0, 3);

        // Allocate and build rotation matrix
        SimpleMatrix R = new SimpleMatrix(3, 3);
        R = Coordinates.rotationMatrix(roverPos);

        // Propagate covariance from global system to local system
        covENU = R.mult(covXYZ).mult(R.transpose());

        //Compute DOP values
        roverPos.setpDop(Math.sqrt(covXYZ.get(0, 0) + covXYZ.get(1, 1) + covXYZ.get(2, 2)));
        roverPos.sethDop(Math.sqrt(covENU.get(0, 0) + covENU.get(1, 1)));
        roverPos.setvDop(Math.sqrt(covENU.get(2, 2)));

        // Compute positioning in geodetic coordinates
        roverPos.computeGeodetic();
    }

    public void SinglePointPositioningWithPRC(Observations roverObs ,boolean estimateOnlyClock, boolean ignoreTopocentricParameters) {

        // Number of GNSS observations without cutoff
        int nObs = roverObs.getNumSat();

        // Number of unknown parameters
        int nUnknowns = 4;

        // Add one unknown for each constellation in addition to the first (to estimate Inter-System Biases - ISBs)
        String sys = getAvailGnssSystems();
        if (sys.length()>0) {
            sys = sys.substring(1);
            nUnknowns = nUnknowns + sys.length();
        }

        // Define least squares matrices
        SimpleMatrix A;
        SimpleMatrix b;
        SimpleMatrix y0;
        SimpleMatrix Q;
        SimpleMatrix x;
        SimpleMatrix vEstim;
        SimpleMatrix tropoCorr;
        SimpleMatrix ionoCorr;
        SimpleMatrix earthCorr;
        SimpleMatrix prcCorr;

        // Covariance matrix obtained from matrix A (satellite geometry) [ECEF coordinates]
        SimpleMatrix covXYZ;
        covXYZ = new SimpleMatrix(3, 3);

        // Covariance matrix obtained from matrix A (satellite geometry) [local coordinates]
        SimpleMatrix covENU;
        covENU = new SimpleMatrix(3, 3);

        // Number of available satellites (i.e. observations)
        int nObsAvail = satAvail.size();

        // Least squares design matrix
        A = new SimpleMatrix(nObsAvail, nUnknowns);

        // Vector for approximate pseudoranges
        b = new SimpleMatrix(nObsAvail, 1);

        // Vector for observed pseudoranges
        y0 = new SimpleMatrix(nObsAvail, 1);

        // Cofactor matrix (initialized to identity)
        Q = SimpleMatrix.identity(nObsAvail);

        // Solution vector
        x = new SimpleMatrix(nUnknowns, 1);

        // Vector for observation error
        vEstim = new SimpleMatrix(nObsAvail, 1);

        // Vectors for troposphere and ionosphere corrections
        tropoCorr = new SimpleMatrix(nObsAvail, 1);

        ionoCorr = new SimpleMatrix(nObsAvail, 1);

        earthCorr = new SimpleMatrix(nObsAvail, 1);

        prcCorr = new SimpleMatrix(nObsAvail,1);

        // Counter for available satellites
        int k = 0;

        // Satellite ID
        int id = 0;

        // Set up the least squares matrices
        for (int i = 0; i < nObs; i++) {

            id = roverObs.getSatID(i);
            char satType = roverObs.getSatType(i);
            String checkAvailGnss = String.valueOf(satType) + String.valueOf(id);

            if (pos[i]!=null && gnssAvail.contains(checkAvailGnss)) {
//			if (pos[i]!=null && satAvail.contains(id)  && satTypeAvail.contains(satType)) {
//				System.out.println("####" + checkAvailGnss  + "####");

                // Fill in one row in the design matrix
                A.set(k, 0, diffRoverSat[i].get(0) / roverSatAppRange[i]); /* X */
                A.set(k, 1, diffRoverSat[i].get(1) / roverSatAppRange[i]); /* Y */
                A.set(k, 2, diffRoverSat[i].get(2) / roverSatAppRange[i]); /* Z */
                A.set(k, 3, 1); /* clock error */
                for (int c = 0; c < sys.length(); c++) {
                    A.set(k, 4+c, sys.indexOf(satType)==c?1:0); /* inter-system bias */
                }

                // Add the approximate pseudorange value to b
                b.set(k, 0, roverSatAppRange[i] - pos[i].getSatelliteClockError() * Constants.SPEED_OF_LIGHT);

                // Add the clock-corrected observed pseudorange value to y0
                y0.set(k, 0, roverObs.getSatByIDType(id, satType).getPseudorange(this.getFreq()));

                prcCorr.set(k,0,pseudoRangeCorr[i]);

                if (!ignoreTopocentricParameters) {
                    // Fill in troposphere and ionosphere double differenced
                    // corrections
                    tropoCorr.set(k, 0, roverSatTropoCorr[i]);
                    ionoCorr.set(k, 0, roverSatIonoCorr[i]);
                    earthCorr.set(k,0,roverSatEarthCorr[i]);

                    // Fill in the cofactor matrix
                    double weight = Q.get(k, k)
                            + computeWeight(roverTopo[i].getElevation(),
                            roverObs.getSatByIDType(id, satType).getSignalStrength(getFreq()));
                    Q.set(k, k, weight);
                }

                // Increment available satellites counter
                k++;
            }

        }

        if (!ignoreTopocentricParameters) {
            // Apply troposphere and ionosphere correction
            b = b.plus(tropoCorr);
            b = b.plus(ionoCorr);
            b = b.plus(earthCorr);
        }
        b = b.plus(prcCorr);

        // Least squares solution x = ((A'*Q^-1*A)^-1)*A'*Q^-1*(y0-b);
        x = A.transpose().mult(Q.invert()).mult(A).invert().mult(A.transpose())
                .mult(Q.invert()).mult(y0.minus(b));

        // Receiver clock error
        roverPos.setReceiverClockError(x.get(3) / Constants.SPEED_OF_LIGHT);

        if(estimateOnlyClock)
            return;

        // Receiver position
        //this.coord.ecef.set(this.coord.ecef.plus(x.extractMatrix(0, 3, 0, 1)));
        roverPos.setPlusXYZ(x.extractMatrix(0, 3, 0, 1));

        // Estimation of the variance of the observation error
        vEstim = y0.minus(A.mult(x).plus(b));
        double varianceEstim = (vEstim.transpose().mult(Q.invert())
                .mult(vEstim)).get(0)
                / (nObsAvail - nUnknowns);

        // Covariance matrix of the estimation error
        if (nObsAvail > nUnknowns) {
            SimpleMatrix covariance = A.transpose().mult(Q.invert()).mult(A).invert()
                    .scale(varianceEstim);
            roverPos.setPositionCovariance(covariance.extractMatrix(0, 3, 0, 3));
        }else{
            roverPos.setPositionCovariance(null);
        }

        // Compute covariance matrix from A matrix [ECEF reference system]
        covXYZ = A.transpose().mult(A).invert();
        covXYZ = covXYZ.extractMatrix(0, 3, 0, 3);

        // Allocate and build rotation matrix
        SimpleMatrix R = new SimpleMatrix(3, 3);
        R = Coordinates.rotationMatrix(roverPos);

        // Propagate covariance from global system to local system
        covENU = R.mult(covXYZ).mult(R.transpose());

        //Compute DOP values
        roverPos.setpDop(Math.sqrt(covXYZ.get(0, 0) + covXYZ.get(1, 1) + covXYZ.get(2, 2)));
        roverPos.sethDop(Math.sqrt(covENU.get(0, 0) + covENU.get(1, 1)));
        roverPos.setvDop(Math.sqrt(covENU.get(2, 2)));

        // Compute positioning in geodetic coordinates
        roverPos.computeGeodetic();
    }



    private int getSatAvailNumber()
    {
        return satAvail.size();
    }
    /**在Rtdlib实现卫星钟差计算，通过调用EphemerisProvider中自带的方法实现了多态**/
    private double computeSatelliteClockError(long unixTime, EphemerisProvider eph, double obsPseudorange) {
        return eph.computeSatelliteClockError(unixTime,obsPseudorange);
    }

    /**在Rtdlib实现卫星位置计算，通过调用EphemerisProvider中自带的方法实现了多态**/
    private SatellitePosition computeSatPosition(Observations obs, EphemerisProvider eph) {
        return eph.computeSatPosition(obs);
    }

    /**return dr   r should be corrected by plus dr*/
    private double computeEarthRotationCorrection(SatellitePosition satPos, ReceiverPosition revPos) {
        SimpleMatrix e = new SimpleMatrix(3,1);
        return Constants.OMEGAE_DOT_GPS*(satPos.getECEF().get(0,0)*revPos.getECEF().get(1,0)-satPos.getECEF().get(1,0)*revPos.getECEF().get(0,0))/Constants.SPEED_OF_LIGHT;
    }

    // Saastamoinen Model
    private double computeTroposphereCorrection(double elevation, double height) {
        double tropoCorr = 0;

        if (height < 5000) {

            elevation = Math.toRadians(Math.abs(elevation));
            if (elevation == 0){
                elevation = elevation + 0.01;
            }

            // Numerical constants and tables for Saastamoinen algorithm
            // (troposphere correction)
            double hr = 50.0;
            int[] ha = new int[9];
            double[] ba = new double[9];

            ha[0] = 0;
            ha[1] = 500;
            ha[2] = 1000;
            ha[3] = 1500;
            ha[4] = 2000;
            ha[5] = 2500;
            ha[6] = 3000;
            ha[7] = 4000;
            ha[8] = 5000;

            ba[0] = 1.156;
            ba[1] = 1.079;
            ba[2] = 1.006;
            ba[3] = 0.938;
            ba[4] = 0.874;
            ba[5] = 0.813;
            ba[6] = 0.757;
            ba[7] = 0.654;
            ba[8] = 0.563;

            // Saastamoinen algorithm
            double P = Constants.STANDARD_PRESSURE * Math.pow((1 - 0.0000226 * height), 5.225);
            double T = Constants.STANDARD_TEMPERATURE - 0.0065 * height;
            double H = hr * Math.exp(-0.0006396 * height);

            // If height is below zero, keep the maximum correction value
            double B = ba[0];
            // Otherwise, interpolate the tables
            if (height >= 0) {
                int i = 1;
                while (height > ha[i]) {
                    i++;
                }
                double m = (ba[i] - ba[i - 1]) / (ha[i] - ha[i - 1]);
                B = ba[i - 1] + m * (height - ha[i - 1]);
            }

            double e = 0.01
                    * H
                    * Math.exp(-37.2465 + 0.213166 * T - 0.000256908
                    * Math.pow(T, 2));

            tropoCorr = ((0.002277 / Math.sin(elevation))
                    * (P - (B / Math.pow(Math.tan(elevation), 2))) + (0.002277 / Math.sin(elevation))
                    * (1255 / T + 0.05) * e);
        }

        return tropoCorr;
    }

    /**
     * @param roverObs
     * @param cutoff
     * this function used to compute approximate diffRoverSat(vector between Rover and Sat) ,
     *                                           ionosphere and troposphere error.
     * And the effect of earth rotation has been corrected while NavigationProducer.getGpsSatPosition()
     * 	RinexNavigation.getGpsSatPosition->RinexNavigationParser.getGpsSatPosition->
     * 	        ReceiverPosition.computePositionGps->ReceiverPosition.computeEarthRotationCorrection
     */
    public void selectSatellitesStandalone(Observations roverObs, double cutoff) {

        // Number of GPS observations
        int nObs = roverObs.getNumSat();

        // Allocate an array to store GPS satellite positions
        pos = new SatellitePosition[nObs];

        // Allocate an array to store receiver-satellite vectors
        diffRoverSat = new SimpleMatrix[nObs];

        // Allocate an array to store receiver-satellite approximate range
        roverSatAppRange = new double[nObs];

        // Allocate arrays to store receiver-satellite atmospheric corrections
        roverSatTropoCorr = new double[nObs];
        roverSatIonoCorr = new double[nObs];
        roverSatEarthCorr = new double[nObs];
        
        // Create a list for available satellites after cutoff
        satAvail = new ArrayList<Integer>(0);
        satTypeAvail = new ArrayList<Character>(0);
        gnssAvail = new ArrayList<String>(0);

        // Create a list for available satellites with phase
        satAvailPhase = new ArrayList<Integer>(0);
        satTypeAvailPhase = new ArrayList<Character>(0);
        gnssAvailPhase = new ArrayList<String>(0);

        // Allocate array of topocentric coordinates
        roverTopo = new TopocentricCoordinates[nObs];

        // Satellite ID
        int id = 0;

        // Compute topocentric coordinates and
        // select satellites above the cutoff level
        for (int i = 0; i < nObs; i++) {

            id = roverObs.getSatID(i);
            satType = roverObs.getSatType(i);

            // Compute GPS satellite positions getGpsByIdx(idx).getSatType()
            EphemerisProvider eph = navigation.getEphemeris(roverObs.getRefTime().getMsec(), id, satType);
            if(eph != null)
            {
                pos[i] = eph.computeSatPosition(roverObs);
            }
            else continue;


            if(pos[i]!=null){

                // Compute rover-satellite approximate pseudorange
                diffRoverSat[i] = roverPos.minusXYZ(pos[i]);
                roverSatAppRange[i] = Math.sqrt(Math.pow(diffRoverSat[i].get(0), 2)
                        + Math.pow(diffRoverSat[i].get(1), 2)
                        + Math.pow(diffRoverSat[i].get(2), 2));

                // Compute azimuth, elevation and distance for each satellite
                roverTopo[i] = new TopocentricCoordinates();
                roverTopo[i].computeTopocentric(roverPos, pos[i]);

                // Correct approximate pseudorange for troposphere
                roverSatTropoCorr[i] = computeTroposphereCorrection(roverTopo[i].getElevation(), roverPos.getGeodeticHeight());

                // Correct approximate pseudorange for ionosphere
                roverSatIonoCorr[i] = computeIonosphereCorrection(roverPos, roverTopo[i].getAzimuth(), roverTopo[i].getElevation(), roverObs.getRefTime());

                roverSatEarthCorr[i] = computeEarthRotationCorrection(pos[i],roverPos);
//				System.out.println("getElevation: " + id + "::" + roverTopo[i].getElevation() );
                // Check if satellite elevation is higher than cutoff
                if (roverTopo[i].getElevation() > cutoff) {

                    satAvail.add(id);
                    satTypeAvail.add(satType);
                    gnssAvail.add(String.valueOf(satType) + String.valueOf(id));

                    // Check if also phase is available
                    if (!Double.isNaN(roverObs.getSatByIDType(id, satType).getPhaseCycles(this.getFreq()))) {
                        satAvailPhase.add(id);
                        satTypeAvailPhase.add(satType);
                        gnssAvailPhase.add(String.valueOf(satType) + String.valueOf(id));
                    }
                }else{
                    if(debug) System.out.println("Not useful sat "+roverObs.getSatType(i) + roverObs.getSatID(i)+" for too low elevation "+roverTopo[i].getElevation()+" < "+cutoff);
                }
            }

        }
    }

    public void selectSatellitesStandalone(Observations roverObs) {
        // Retrieve options from goGPS class
        double cutoff = this.getCutoff();

        selectSatellitesStandalone(roverObs, cutoff);
    }

    public void selectSatellitesDGNSS(Observations roverObs, double cutoff) {

        // Number of GPS observations
        int nObs = roverObs.getNumSat();

        // Allocate an array to store GPS satellite positions
        pos = new SatellitePosition[nObs];

        // Allocate an array to store receiver-satellite vectors
        diffRoverSat = new SimpleMatrix[nObs];

        // Allocate an array to store receiver-satellite approximate range
        roverSatAppRange = new double[nObs];

        // Allocate arrays to store receiver-satellite atmospheric corrections
//        roverSatTropoCorr = new double[nObs];
//        roverSatIonoCorr = new double[nObs];
//        roverSatEarthCorr = new double[nObs];
        pseudoRangeCorr = new double[nObs];

        // Create a list for available satellites after cutoff
        satAvail = new ArrayList<Integer>(0);
        satTypeAvail = new ArrayList<Character>(0);
        gnssAvail = new ArrayList<String>(0);

        // Create a list for available satellites with phase
        satAvailPhase = new ArrayList<Integer>(0);
        satTypeAvailPhase = new ArrayList<Character>(0);
        gnssAvailPhase = new ArrayList<String>(0);

        // Allocate array of topocentric coordinates
        roverTopo = new TopocentricCoordinates[nObs];

        // Satellite ID
        int id = 0;

        // Compute topocentric coordinates and
        // select satellites above the cutoff level
        for (int i = 0; i < nObs; i++) {

            id = roverObs.getSatID(i);
            satType = roverObs.getSatType(i);

            // Compute GPS satellite positions getGpsByIdx(idx).getSatType()
            EphemerisProvider eph = navigation.getEphemeris(roverObs.getRefTime().getMsec(), id, satType);
            if(eph != null)
            {
                pos[i] = eph.computeSatPosition(roverObs);
                pseudoRangeCorr[i] = prcIn.getPRC(roverObs.getRefTime().getMsec(), id, satType).getPRC();
            }
            else continue;


            if(pos[i]!=null){

                // Compute rover-satellite approximate pseudorange
                diffRoverSat[i] = roverPos.minusXYZ(pos[i]);
                roverSatAppRange[i] = Math.sqrt(Math.pow(diffRoverSat[i].get(0), 2)
                        + Math.pow(diffRoverSat[i].get(1), 2)
                        + Math.pow(diffRoverSat[i].get(2), 2));

                // Compute azimuth, elevation and distance for each satellite
                roverTopo[i] = new TopocentricCoordinates();
                roverTopo[i].computeTopocentric(roverPos, pos[i]);

                // Correct approximate pseudorange for troposphere
//                roverSatTropoCorr[i] = computeTroposphereCorrection(roverTopo[i].getElevation(), roverPos.getGeodeticHeight());
//
//                // Correct approximate pseudorange for ionosphere
//                roverSatIonoCorr[i] = computeIonosphereCorrection(roverPos, roverTopo[i].getAzimuth(), roverTopo[i].getElevation(), roverObs.getRefTime());
//
//                roverSatEarthCorr[i] = computeEarthRotationCorrection(pos[i],roverPos);
//				System.out.println("getElevation: " + id + "::" + roverTopo[i].getElevation() );
                // Check if satellite elevation is higher than cutoff
                if (roverTopo[i].getElevation() > cutoff) {

                    satAvail.add(id);
                    satTypeAvail.add(satType);
                    gnssAvail.add(String.valueOf(satType) + String.valueOf(id));

                    // Check if also phase is available
                    if (!Double.isNaN(roverObs.getSatByIDType(id, satType).getPhaseCycles(this.getFreq()))) {
                        satAvailPhase.add(id);
                        satTypeAvailPhase.add(satType);
                        gnssAvailPhase.add(String.valueOf(satType) + String.valueOf(id));
                    }
                }else{
                    if(debug) System.out.println("Not useful sat "+roverObs.getSatType(i) + roverObs.getSatID(i)+" for too low elevation "+roverTopo[i].getElevation()+" < "+cutoff);
                }
            }

        }
    }

    public void selectSatellitesDGNSS(Observations roverObs) {
        // Retrieve options from goGPS class
        double cutoff = this.getCutoff();

        selectSatellitesDGNSS(roverObs, cutoff);
    }

    private double computeIonosphereCorrection(Coordinates revPos,double elevation, double height,Time time) {

        if (navigation.getIono(time.getMsec())!=null)
        return navigation.getIono(time.getMsec()).computeIonosphereCorrection(navigation,revPos,elevation,height,time);
        else return 0;
    }

    /**
     * Gets the cutoff.
     *
     * @return the cutoff
     */
    public double getCutoff() {
        return cutoff;
    }

    /**
     * Sets the cutoff.
     *
     * @param cutoff the cutoff to set
     */
    public void setCutoff(double cutoff) {
        this.cutoff = cutoff;
    }

    private void notifyPositionConsumerEvent(int event){
        for(PositionConsumer pc:positionConsumers){
            try{
                pc.event(event);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    private void notifyPositionConsumerAddCoordinate(ReceiverPosition coord){
        for(PositionConsumer pc:positionConsumers){
            try{
                pc.addCoordinate(coord);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the freq.
     *
     * @return the freq
     */
    public int getFreq() {
        return freq;
    }

    /**
     * Sets the freq.
     *
     * @param freq the freq to set
     */
    public void setFreq(int freq) {
        this.freq = freq;
    }

    private double computeWeight(double elevation, float snr) {

        double weight = 1;
        float Sa = Constants.SNR_a;
        float SA = Constants.SNR_A;
        float S0 = Constants.SNR_0;
        float S1 = Constants.SNR_1;

        if (Float.isNaN(snr) && (getWeights() == Rtdlib.WEIGHT_SIGNAL_TO_NOISE_RATIO ||
                getWeights() == Rtdlib.WEIGHT_COMBINED_ELEVATION_SNR)) {
            if(debug) System.out.println("SNR not available: forcing satellite elevation-based weights...");
            setWeights(Rtdlib.WEIGHT_SAT_ELEVATION);
        }

        switch (getWeights()) {

            // Weight based on satellite elevation
            case Rtdlib.WEIGHT_SAT_ELEVATION:
                weight = 1 / Math.pow(Math.sin(elevation * Math.PI / 180), 2);
                break;

            // Weight based on signal-to-noise ratio
            case Rtdlib.WEIGHT_SIGNAL_TO_NOISE_RATIO:
                if (snr >= S1) {
                    weight = 1;
                } else {
                    weight = Math.pow(10, -(snr - S1) / Sa)
                            * ((SA / Math.pow(10, -(S0 - S1) / Sa) - 1) / (S0 - S1)
                            * (snr - S1) + 1);
                }
                break;

            // Weight based on combined elevation and signal-to-noise ratio
            case Rtdlib.WEIGHT_COMBINED_ELEVATION_SNR:
                if (snr >= S1) {
                    weight = 1;
                } else {
                    double weightEl = 1 / Math.pow(Math.sin(elevation * Math.PI / 180), 2);
                    double weightSnr = Math.pow(10, -(snr - S1) / Sa)
                            * ((SA / Math.pow(10, -(S0 - S1) / Sa) - 1) / (S0 - S1) * (snr - S1) + 1);
                    weight = weightEl * weightSnr;
                }
                break;

            // Same weight for all observations or default
            case Rtdlib.WEIGHT_EQUAL:
            default:
                weight = 1;
        }
        return weight;
    }

    /**
     * Gets the weights.
     *
     * @return the weights
     */
    public int getWeights() {
        return weights;
    }

    /**
     * Sets the weights.
     *
     * @param weights the weights to set
     */
    public void setWeights(int weights) {
        this.weights = weights;
    }


}
