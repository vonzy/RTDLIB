package org.von.rtdlib.parser.rinex;

/**
 * Created by diner on 16-10-20.
 */

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.ObservationSet;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.ObservationsProvider;

import java.io.*;

/** 该类将由各个版本的RinexObservationParser所继承*/
public class RinexObservationParser implements ObservationsProvider {


    /**对信号各支路不加区分，每个频率只考虑一种观测值，因为同频率多支路的GNSS接收机非常少见，一般用不到*/
    public enum SignalTypeConstants
    {
        GPS_L1C,GPS_L1L,GPS_L1D,GPS_L1S,
        GPS_L2C,GPS_L2L,GPS_L2D,GPS_L2S,
        GPS_L5C,GPS_L5L,GPS_L5D,GPS_L5S,
        GLONASS_G1C,GLONASS_G1L,GLONASS_G1D,GLONASS_G1S,
        GLONASS_G2C,GLONASS_G2L,GLONASS_G2D,GLONASS_G2S,
        GLONASS_G3C,GLONASS_G3L,GLONASS_G3D,GLONASS_G3S,
        BDS_B1C,BDS_B1L,BDS_B1D,BDS_B1S,
        BDS_B2C,BDS_B2L,BDS_B2D,BDS_B2S,
        BDS_B3C,BDS_B3L,BDS_B3D,BDS_B3S
    }

    protected File fileObs;
    protected FileInputStream streamObs;
    protected InputStreamReader inStreamObs;
    protected BufferedReader buffStreamObs;

    protected int nTypes, nTypesG, nTypesR, nTypesE, nTypesJ, nTypesC; /* Number of observation types */
    protected int[] typeOrder, typeOrderG, typeOrderR, typeOrderE, typeOrderJ, typeOrderC; /* Order of observation data */
    protected boolean hasS1Field = false; /* S1 field (SNR) is present */
    protected boolean hasS2Field = false; /* S2 field (SNR) is present */
    protected Time timeFirstObs; /* Time of first observation set */

    protected Coordinates approxPos; /* Approximate position (X, Y, Z) [m] */
    protected double[] antDelta; /* Antenna delta (E, N, U) [m] */

    protected Observations obs = null; /* Current observation data sets */

    // Private fields useful to keep track of values between epoch parsing and
    // data parsing
    protected int nGps;
    protected int nGlo;
    protected int nQzs;
    protected int nSbs;
    protected int nBds;
    protected int nSat;
    protected char[] sysOrder;
    protected int[] satOrder;
    protected int ver ;

    protected boolean gpsEnable = true;  // enable GPS data reading
    protected boolean qzsEnable = true;  // enable QZSS data reading
    protected boolean gloEnable = true;  // enable GLONASS data reading
    protected boolean galEnable = true;  // enable Galileo data reading
    protected boolean bdsEnable = true;  // enable BeiDou data reading

    Boolean[] multiConstellation = {gpsEnable, qzsEnable, gloEnable, galEnable, bdsEnable};


    public RinexObservationParser(File fileObs) {
        this.fileObs = fileObs;
    }
    public RinexObservationParser(File fileObs, Boolean[] multiConstellation) {
        this.fileObs = fileObs;
        this.gpsEnable = multiConstellation[0];
        this.qzsEnable = multiConstellation[1];
        this.gloEnable = multiConstellation[2];
        this.galEnable = multiConstellation[3];
        this.bdsEnable = multiConstellation[4];
        this.multiConstellation = multiConstellation;
    }

    /**
     * Assign observation data according to type order
     * String line: line text to be processed
     * int k: the index of typeOrder
     * int j: the start position of line text
     * int i: the index of Observations
     */
    protected void assignObsWithType(String line, int k, int j, int i, char satType) {

        if (ver == 3) {
            if (satType == 'G') {
                typeOrder = typeOrderG;
            } else if (satType == 'R') {
                typeOrder = typeOrderR;
            } else if (satType == 'E') {
                typeOrder = typeOrderE;
            } else if (satType == 'J') {
                typeOrder = typeOrderJ;
            } else if (satType == 'C') {
                typeOrder = typeOrderC;
            }
        }

        try {
            ObservationSet o = obs.getSatByIndex(i);

            if (typeOrder[k] == 0) { // ** C1 code

                String codeC = line.substring(j, j + 14).trim();
                if(codeC.trim().length()>0)o.setCodeC(0,Double.parseDouble(codeC));

            } else if (typeOrder[k] == 1) { // ** C2 code

                String codeC = line.substring(j, j + 14).trim();
                if(codeC.trim().length()>0)o.setCodeC(1,Double.parseDouble(codeC));

            } else if (typeOrder[k] == 2) { // ** P1 code

                String codeP = line.substring(j, j + 14).trim();
//				System.out.println("codeP: " + codeP);
                if (codeP.length() != 0) {
                    o.setCodeP(0,Double.parseDouble(codeP));
                }
            } else if (typeOrder[k] == 3) { // ** P2 code

                String codeP = line.substring(j, j + 14).trim();
                if (codeP.length() != 0) {
                    o.setCodeP(1,Double.parseDouble(codeP));
                }
            } else if (typeOrder[k] == 4) { // ** L1 phase

                String phaseL = line.substring(j, j + 14);
//				System.out.println("phaseL: " + phaseL);
                phaseL = phaseL.trim();
                try {
                    if (phaseL.length() != 0) {
                        o.setPhaseCycles(0,Double.parseDouble(phaseL));
                        try{
                            // Loss of Lock
                            int lli = Integer.parseInt(line.substring(j+14, j + 15));
                            o.setLossLockInd(0,lli);
                        }catch(Exception ignore){}
                        try{
                            // Signal Strength
                            int ss = Integer.parseInt(line.substring(j+15, j + 16));
                            o.setSignalStrengthInd(0, ss);
                            if (!hasS1Field)
                                o.setSignalStrength(0,ss * 6);
                        }catch(Exception ignore){}
                    }
                } catch (NumberFormatException e) {
                }
            } else if (typeOrder[k] == 5) { // ** L2 phase

                String phaseL = line.substring(j, j + 14).trim();
                try {
                    if (phaseL.length() != 0) {
                        o.setPhaseCycles(1,Double.parseDouble(phaseL));

                        try{
                            // Loss of Lock
                            int lli = Integer.parseInt(line.substring(j+14, j + 15));
                            o.setLossLockInd(1,lli);
                        }catch(Exception ignore){}
                        try{
                            // Signal Strength
                            int ss = Integer.parseInt(line.substring(j+15, j + 16));
                            o.setSignalStrengthInd(1, ss);
                            if (!hasS2Field)
                                o.setSignalStrength(1,ss * 6);
                        }catch(Exception ignore){}
                    }
                } catch (NumberFormatException e) {
                }
            } else if (typeOrder[k] == 6) { // S1 ** SNR on L1

                String snrS = line.substring(j, j + 14).trim();
//				System.out.println("snrS: " + snrS);

                if (snrS.length() != 0) {
                    o.setSignalStrength(0,Float.parseFloat(snrS));
                }
            } else if (typeOrder[k] == 7) { // S2 ** SNR on L2

                String snrS = line.substring(j, j + 14).trim();
                if (snrS.length() != 0) {
                    o.setSignalStrength(1,Float.parseFloat(snrS));
                }
            } else if (typeOrder[k] == 8) { // ** D1 doppler

                String dopplerD = line.substring(j, j + 14).trim();
//				System.out.println("dopplerD: " + dopplerD);

                if (dopplerD.length() != 0) {
                    o.setDoppler(0,Float.parseFloat(dopplerD));
                }
            } else if (typeOrder[k] == 9) { // ** D2 doppler

                String dopplerD = line.substring(j, j + 14).trim();
                if (dopplerD.length() != 0) {
                    o.setDoppler(1,Float.parseFloat(dopplerD));
                }

			/*  NEED to improve below codes  */


            } else if (typeOrder[k] == 10) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 11) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 12) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 13) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 14) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 15) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 16) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 17) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 18) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 19) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 20) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 21) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 22) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 23) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}

            } else if (typeOrder[k] == 24) { // ** D2 doppler

//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
            }



        } catch (StringIndexOutOfBoundsException e) {
            // Skip over blank slots
        }
    }

    /**
     * @param line
     */
    protected void parseApproxPos(String line) {

        // Allocate the vector that stores the approximate position (X, Y, Z)
        //approxPos = Coordinates.globalXYZInstance(new SimpleMatrix(3, 1));
//		approxPos.ecef = new SimpleMatrix(3, 1);

        // Read approximate position coordinates
//		approxPos.ecef.set(0, 0, Double.valueOf(line.substring(0, 14).trim())
//				.doubleValue());
//		approxPos.ecef.set(1, 0, Double.valueOf(line.substring(14, 28).trim())
//				.doubleValue());
//		approxPos.ecef.set(2, 0, Double.valueOf(line.substring(28, 42).trim())
//				.doubleValue());
//
        approxPos = Coordinates.globalXYZInstance(Double.valueOf(line.substring(0, 14).trim()), Double.valueOf(line.substring(14, 28).trim()), Double.valueOf(line.substring(28, 42).trim()) );

        // Convert the approximate position to geodetic coordinates
        approxPos.computeGeodetic();
    }

    /**
     * @param line
     */
    protected void parseAntDelta(String line) {

        // Allocate the array that stores the approximate position
        antDelta = new double[3];

        // Read approximate position coordinates (E, N, U)
        antDelta[2] = Double.valueOf(line.substring(0, 14).trim())
                .doubleValue();
        antDelta[0] = Double.valueOf(line.substring(14, 28).trim())
                .doubleValue();
        antDelta[1] = Double.valueOf(line.substring(28, 42).trim())
                .doubleValue();
    }

    /**
     * @return the approxPos
     */
    public Coordinates getDefinedPosition() {
        return approxPos;
    }


    /**
     * @return the obs
     */
    public Observations getCurrentObservations() {
        return obs;
    }


    public boolean hasMoreObservations() throws IOException {
        return buffStreamObs.ready();
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.Obser.vationsProducer#init()
     */
    @Override
    public void init() throws Exception {
        // Open file streams
        open();

        // Parse RINEX observation headers
        parseHeader(); /* Header */

    }

    @Override
    public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
        try {
            streamObs.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            inStreamObs.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            buffStreamObs.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    protected void open() throws FileNotFoundException {
        streamObs = new FileInputStream(fileObs);
        inStreamObs = new InputStreamReader(streamObs);
        buffStreamObs = new BufferedReader(inStreamObs);
    }

    /** override by subclass*/
    protected void parseHeader() {
    }

    /** override by subclass*/
    @Override
    public Observations getNextObservations() {
        return null;
    }

    /** override by subclass*/
    protected void parseTimeofFirstObs(String line) {

    }

    /** override by subclass*/
    protected void parseData() {

    }

    /** override by subclass*/
    protected void parseTypes(String line) throws IOException {

    }

    /** override by subclass*/
    protected void assignTypeOrder(String type, int i) {

    }

}
