package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.ObservationSet;

import java.io.File;
import java.io.IOException;

/**
 * Created by diner on 16-10-28.
 */
public class RinexObservationParserV302 extends RinexObservationParserV3 {

    protected SignalTypeConstants[] typeOrder, typeOrderG, typeOrderR, typeOrderE, typeOrderJ, typeOrderC; /* Order of observation data */

    public RinexObservationParserV302(File file) {
        super(file);
        this.ver = 302;

    }

    protected void assignTypeOrder(String type, int i,char satType) {

        switch (satType)
        {
            case 'G': assignTypeOrderGPS(type, i); break;
            case 'R': assignTypeOrderGLONASS(type, i); break;
            case 'C': assignTypeOrderBDS(type, i); break;
        }
    }

    private void assignTypeOrderBDS(String type, int i) {
        if(type.equals("C1I")){  typeOrder[i] = SignalTypeConstants.BDS_B1C;}
        if(type.equals("C7I")){  typeOrder[i] = SignalTypeConstants.BDS_B2C;}
        if(type.equals("C6I")){  typeOrder[i] = SignalTypeConstants.BDS_B3C;}
        if(type.equals("L1I")){  typeOrder[i] = SignalTypeConstants.BDS_B1L;}
        if(type.equals("L7I")){  typeOrder[i] = SignalTypeConstants.BDS_B2L;}
        if(type.equals("L6I")){  typeOrder[i] = SignalTypeConstants.BDS_B3L;}
        if(type.equals("S1I")){  typeOrder[i] = SignalTypeConstants.BDS_B1S;}
        if(type.equals("S7I")){  typeOrder[i] = SignalTypeConstants.BDS_B2S;}
        if(type.equals("S6I")){  typeOrder[i] = SignalTypeConstants.BDS_B3S;}
        if(type.equals("D1I")){  typeOrder[i] = SignalTypeConstants.BDS_B1D;}
        if(type.equals("D7I")){  typeOrder[i] = SignalTypeConstants.BDS_B2D;}
        if(type.equals("D6I")){  typeOrder[i] = SignalTypeConstants.BDS_B3D;}
    }

    private void assignTypeOrderGLONASS(String type, int i) {
        if(type.equals("C1C")){  typeOrder[i] = SignalTypeConstants.GLONASS_G1C;}
        if(type.equals("C2P")){  typeOrder[i] = SignalTypeConstants.GLONASS_G2C;}
        if(type.equals("L1C")){  typeOrder[i] = SignalTypeConstants.GLONASS_G1L;}
        if(type.equals("L2P")){  typeOrder[i] = SignalTypeConstants.GLONASS_G2L;}
        if(type.equals("S1C")){  typeOrder[i] = SignalTypeConstants.GLONASS_G1S;}
        if(type.equals("S2P")){  typeOrder[i] = SignalTypeConstants.GLONASS_G2S;}
        if(type.equals("D1C")){  typeOrder[i] = SignalTypeConstants.GLONASS_G1D;}
        if(type.equals("D2P")){  typeOrder[i] = SignalTypeConstants.GLONASS_G2D;}
    }

    private void assignTypeOrderGPS(String type, int i) {
        if(type.equals("C1C")){  typeOrder[i] = SignalTypeConstants.GPS_L1C;}
        if(type.equals("C2P")){  typeOrder[i] = SignalTypeConstants.GPS_L2C;}
        if(type.equals("L1C")){  typeOrder[i] = SignalTypeConstants.GPS_L1L;}
        if(type.equals("L2P")){  typeOrder[i] = SignalTypeConstants.GPS_L2L;}
        if(type.equals("S1C")){  typeOrder[i] = SignalTypeConstants.GPS_L1S;}
        if(type.equals("S2P")){  typeOrder[i] = SignalTypeConstants.GPS_L2S;}
        if(type.equals("D1C")){  typeOrder[i] = SignalTypeConstants.GPS_L1D;}
        if(type.equals("D2P")){  typeOrder[i] = SignalTypeConstants.GPS_L2D;}
    }

    /**
     * Assign observation data according to type order
     * String line: line text to be processed
     * int k: the index of typeOrder
     * int j: the start position of line text
     * int i: the index of Observations
     */
    protected void assignObsWithType(String line, int k, int j, int i, char satType) {

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

        try {
            ObservationSet o = obs.getSatByIndex(i);

            if (typeOrder[k] == SignalTypeConstants.GPS_L1C ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G1C ||
                    typeOrder[k] == SignalTypeConstants.BDS_B1C) { // ** C1 code
                String codeC = line.substring(j, j + 14).trim();
                if (codeC.trim().length() > 0) o.setCodeC(0, Double.parseDouble(codeC));

            } else if (typeOrder[k] == SignalTypeConstants.GPS_L2C ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G2C ||
                    typeOrder[k] == SignalTypeConstants.BDS_B2C) { // ** C2 code

                String codeC = line.substring(j, j + 14).trim();
                if (codeC.trim().length() > 0) o.setCodeC(1, Double.parseDouble(codeC));

            } else if (typeOrder[k] == SignalTypeConstants.GPS_L1L ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G1L ||
                    typeOrder[k] == SignalTypeConstants.BDS_B1L) { // ** L1 phase

                String phaseL = line.substring(j, j + 14);
//				System.out.println("phaseL: " + phaseL);
                phaseL = phaseL.trim();
                try {
                    if (phaseL.length() != 0) {
                        o.setPhaseCycles(0, Double.parseDouble(phaseL));
                        try {
                            // Loss of Lock
                            int lli = Integer.parseInt(line.substring(j + 14, j + 15));
                            o.setLossLockInd(0, lli);
                        } catch (Exception ignore) {
                        }
                        try {
                            // Signal Strength
                            int ss = Integer.parseInt(line.substring(j + 15, j + 16));
                            o.setSignalStrengthInd(0, ss);
                            if (!hasS1Field)
                                o.setSignalStrength(0, ss * 6);
                        } catch (Exception ignore) {
                        }
                    }
                } catch (NumberFormatException e) {
                }
            } else if (typeOrder[k] == SignalTypeConstants.GPS_L2L ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G2L ||
                    typeOrder[k] == SignalTypeConstants.BDS_B2L) { // ** L2 phase

                String phaseL = line.substring(j, j + 14).trim();
                try {
                    if (phaseL.length() != 0) {
                        o.setPhaseCycles(1, Double.parseDouble(phaseL));

                        try {
                            // Loss of Lock
                            int lli = Integer.parseInt(line.substring(j + 14, j + 15));
                            o.setLossLockInd(1, lli);
                        } catch (Exception ignore) {
                        }
                        try {
                            // Signal Strength
                            int ss = Integer.parseInt(line.substring(j + 15, j + 16));
                            o.setSignalStrengthInd(1, ss);
                            if (!hasS2Field)
                                o.setSignalStrength(1, ss * 6);
                        } catch (Exception ignore) {
                        }
                    }
                } catch (NumberFormatException e) {
                }
            } else if (typeOrder[k] == SignalTypeConstants.GPS_L1S ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G1S ||
                    typeOrder[k] == SignalTypeConstants.BDS_B1S) { // S1 ** SNR on L1

                String snrS = line.substring(j, j + 14).trim();
//				System.out.println("snrS: " + snrS);

                if (snrS.length() != 0) {
                    o.setSignalStrength(0, Float.parseFloat(snrS));
                }
            } else if (typeOrder[k] == SignalTypeConstants.GPS_L2S ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G2S ||
                    typeOrder[k] == SignalTypeConstants.BDS_B2S) { // S2 ** SNR on L2

                String snrS = line.substring(j, j + 14).trim();
                if (snrS.length() != 0) {
                    o.setSignalStrength(1, Float.parseFloat(snrS));
                }
            } else if (typeOrder[k] == SignalTypeConstants.GPS_L1D ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G1D ||
                    typeOrder[k] == SignalTypeConstants.BDS_B1D) { // ** D1 doppler

                String dopplerD = line.substring(j, j + 14).trim();
//				System.out.println("dopplerD: " + dopplerD);

                if (dopplerD.length() != 0) {
                    o.setDoppler(0, Float.parseFloat(dopplerD));
                }
            } else if (typeOrder[k] == SignalTypeConstants.GPS_L2D ||
                    typeOrder[k] == SignalTypeConstants.GLONASS_G2D ||
                    typeOrder[k] == SignalTypeConstants.BDS_B2D) { // ** D2 doppler

                String dopplerD = line.substring(j, j + 14).trim();
                if (dopplerD.length() != 0) {
                    o.setDoppler(1, Float.parseFloat(dopplerD));
                }

            }
        }catch (StringIndexOutOfBoundsException e) {
            // Skip over blank slots
        }
    }


    protected void parseTypes(String line, String satType) throws IOException {

        // Extract number of available data types
        nTypes = Integer.parseInt(line.substring(1, 6).trim());

        // Allocate the array that stores data type order
        typeOrder = new SignalTypeConstants[nTypes];

        if(nTypes > 13){  // In case of more than 13 Types, it will two lines

            for (int i = 0; i <= 12; i++) {
                String type = line.substring(4 * (i + 3) -5 , 4 * (i + 3) -2);
                assignTypeOrder(type, i ,satType.charAt(0));
            }

            line = buffStreamObs.readLine();   // read the second line

            int j = 0;
            for (int i = 13; i <= nTypes  ; i++) {
                String type = line.substring(4 * (j + 3) -5 , 4 * (j + 3) -2);
                assignTypeOrder(type, i,satType.charAt(0));
                j++ ;
            }

        } else {  // less than 14 types, it will be one line.

            for (int i = 0; i < nTypes; i++) {
                String type = line.substring(4 * (i + 3) -5 , 4 * (i + 3) -2);
                assignTypeOrder(type, i,satType.charAt(0));
            }
        }

        if (satType.equals("G")) {
            typeOrderG = typeOrder;
            nTypesG = nTypes;
        } else if (satType.equals("R")) {
            typeOrderR = typeOrder;
            nTypesR = nTypes;
        } else if (satType.equals("E")) {
            typeOrderE = typeOrder;
            nTypesE = nTypes;
        } else if (satType.equals("J")) {
            typeOrderJ = typeOrder;
            nTypesJ = nTypes;
        } else if (satType.equals("C")) {
            typeOrderC = typeOrder;
            nTypesC = nTypes;
        }
    }


}
