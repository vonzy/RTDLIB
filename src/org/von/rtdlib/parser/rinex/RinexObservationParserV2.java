package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.ObservationSet;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.ObservationsProvider;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;

/**
 * Created by diner on 16-10-16.
 */
public class RinexObservationParserV2 extends RinexObservationParser{

    public RinexObservationParserV2(File file) {
           super(file);
           this.ver = 2;
    }

    @Override
    protected void parseHeader() {

        boolean foundTypeObs = false;

        try {
            while (buffStreamObs.ready()) {
                String line = buffStreamObs.readLine();
                String typeField = line.substring(60, line.length());
                typeField = typeField.trim();

                if (typeField.equals("# / TYPES OF OBSERV")) {
                    parseTypes(line);
                    foundTypeObs = true;
                }
                else if (typeField.equals("TIME OF FIRST OBS")) {
                    parseTimeofFirstObs(line);
                }
                else if (typeField.equals("APPROX POSITION XYZ")) {
                    parseApproxPos(line);
                }
                else if (typeField.equals("ANTENNA: DELTA H/E/N")) {
                    parseAntDelta(line);
                }
                else if (typeField.equals("END OF HEADER")) {
                    if (!foundTypeObs) {
                        // Display an error if TIME OF FIRST OBS was not found
                        System.err.println("Critical information"
                                + "(TYPES OF OBSERV) is missing in file "
                                + fileObs.toString() + " header");
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Observations getNextObservations() {
        try {

            if(!hasMoreObservations()) return null;
            String line = buffStreamObs.readLine();
            int len = line.length();

            // Parse date and time
            String dateStr = "20" + line.substring(1, 22);

            // Parse event flag
            String eFlag = line.substring(28, 30).trim();
            int eventFlag = Integer.parseInt(eFlag);

            // Parse available satellites string
            String satAvail = line.substring(30, len);

            // Parse number of available satellites
            String numOfSat = satAvail.substring(0, 2).trim();
            nSat = Integer.parseInt(numOfSat);

            // Arrays to store satellite order
            satOrder = new int[nSat];
            sysOrder = new char[nSat];

            nGps = 0;
            nGlo = 0;
            nSbs = 0;

            // If number of satellites <= 12, read only one line...
            if (nSat <= 12) {

                // Parse satellite IDs
                int j = 2;
                for (int i = 0; i < nSat; i++) {

                    String satType = satAvail.substring(j, j + 1);
                   // System.out.println(satAvail);
                    //System.out.println(dateStr);
                    String satID = satAvail.substring(j + 1, j + 3);
                    if (satType.equals("G") || satType.equals(" ")) {
                        sysOrder[i] = 'G';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nGps++;
                    } else if (satType.equals("R")) {
                        sysOrder[i] = 'R';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nGlo++;
                    } else if (satType.equals("C")) {
                        sysOrder[i] = 'C';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nBds++;
                    } else if (satType.equals("S")) {
                        sysOrder[i] = 'S';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nSbs++;
                    }
                    j = j + 3;
                }
            } else { // ... otherwise, read two lines

                // Parse satellite IDs
                int j = 2;
                for (int i = 0; i < 12; i++) {

                    String satType = satAvail.substring(j, j + 1);
                    String satID = satAvail.substring(j + 1, j + 3);
                    if (satType.equals("G") || satType.equals(" ")) {
                        sysOrder[i] = 'G';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nGps++;
                    } else if (satType.equals("R")) {
                        sysOrder[i] = 'R';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nGlo++;
                    } else if (satType.equals("C")) {
                        sysOrder[i] = 'C';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nBds++;
                    }else if (satType.equals("S")) {
                        sysOrder[i] = 'S';
                        satOrder[i] = Integer.parseInt(satID.trim());
                        nSbs++;
                    }
                    j = j + 3;
                }
                // Get second line
                satAvail = buffStreamObs.readLine().trim();

                // Number of remaining satellites
                int num = nSat - 12;

                // Parse satellite IDs
                int k = 0;
                for (int i = 0; i < num; i++) {

                    String satType = satAvail.substring(k, k + 1);
                    String satID = satAvail.substring(k + 1, k + 3);
                    if (satType.equals("G") || satType.equals(" ")) {
                        sysOrder[i + 12] = 'G';
                        satOrder[i + 12] = Integer.parseInt(satID.trim());
                        nGps++;
                    } else if (satType.equals("R")) {
                        sysOrder[i + 12] = 'R';
                        satOrder[i + 12] = Integer.parseInt(satID.trim());
                        nGlo++;
                    } else if (satType.equals("C")) {
                        sysOrder[i + 12] = 'C';
                        satOrder[i + 12] = Integer.parseInt(satID.trim());
                        nBds++;
                    }else if (satType.equals("S")) {
                        sysOrder[i + 12] = 'S';
                        satOrder[i + 12] = Integer.parseInt(satID.trim());
                        nSbs++;
                    }
                    k = k + 3;
                }
            }

            obs = new Observations(new Time(dateStr), eventFlag);

            // Convert date string to standard UNIX time in milliseconds
            //long time = Time.dateStringToTime(dateStr);

            // Store time
            //obs.refTime = new Time(dateStr);
            //obs.refTime.msec = time;

            // Store event flag
            //obs.eventFlag = eventFlag;

            parseData();
            obs.cleanObservations();

            return obs;

        } catch (StringIndexOutOfBoundsException e) {
            // Skip over blank lines
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void parseData() {

        try {

            //obs.init(nGps, nGlo, nSbs);

            // Arrays to store satellite list for each system
//			obs.gpsSat = new ArrayList<Integer>(nGps);
//			obs.gloSat = new ArrayList<Integer>(nGlo);
//			obs.sbsSat = new ArrayList<Integer>(nSbs);
//
//			// Allocate array of observation objects
//			if (nGps > 0)
//				obs.gps = new ObservationSet[nGps];
//			if (nGlo > 0)
//				obs.glo = new ObservationSet[nGlo];
//			if (nSbs > 0)
//				obs.sbs = new ObservationSet[nSbs];

            // Loop through observation lines
            for (int i = 0; i < nSat; i++) {

                // Read line of observations
                String line = buffStreamObs.readLine();

                float nLinesToRead0 = (float) nTypes / 5;
                BigDecimal bd0 = new BigDecimal(nLinesToRead0);
                BigDecimal bd = bd0.setScale(0, BigDecimal.ROUND_UP);
                int nLinesToRead = (int) bd.doubleValue();

                // Create observation object
                ObservationSet os = new ObservationSet();
                if (sysOrder[i] == 'G' && gpsEnable) {
                    os.setSatType('G');
                    os.setSatID(satOrder[i]);
                    obs.setObsSets(i, os);
                }
                else if (sysOrder[i] == 'R' && gloEnable) {
                    os.setSatType('R');
                    os.setSatID(satOrder[i]);
                    obs.setObsSets(i, os);
                }
                else if (sysOrder[i] == 'C' && bdsEnable) {
                    os.setSatType('C');
                    os.setSatID(satOrder[i]);
                    obs.setObsSets(i, os);
                }
                else if (sysOrder[i] == 'J' && qzsEnable) {
                    os.setSatType('J');
                    os.setSatID(satOrder[i]);
                    obs.setObsSets(i, os);
                }
                else {  // skip unselected observations

                    if (nLinesToRead > 1) { // If the number of observation

                        for (int l = 0; l < nLinesToRead; l++){
                            int remTypes = nTypes -  5 * l ; // To calculate remaining Types

                            if (remTypes > 5){  // 5 types in one line
                                line = buffStreamObs.readLine();
                            }	// end of if
                        } // end of for
                    } // end of if
                    continue;
                } // end of if

                if (nLinesToRead == 1) {

                    // Parse observation data according to typeOrder
                    int j = 0;
                    for (int k = 0; k < nTypes; k++) {
                        assignObsWithType(line, k, j, i, os.getSatType());
                        j = j + 16;
                    }

                } else { // ... otherwise, they are more than one lines

                    int k = 0;
                    for (int l = 0; l < nLinesToRead; l++){

                        int remTypes = nTypes -  5 * l ; // To calculate remaining Types

                        if (remTypes > 5){  // 5 types is in one line
                            int j = 0;
                            for (int m = 0; m < 5; m++ ) {
                                assignObsWithType(line, k, j, i, os.getSatType());
                                j = j + 16;
                                k++;
                            }
                            line = buffStreamObs.readLine();

                        } else if (remTypes < 5 && remTypes > 0) {  // the number of types in the last line
                            int j = 0;
                            for (int m = 0; m < remTypes; m++ ) {
                                assignObsWithType(line, k, j, i, os.getSatType());
                                j = j + 16;
                                k++;
                            }
                        }	// end of if
                    } // end of for
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            // Skip over blank lines
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void parseTypes(String line) throws IOException {
        // Extract number of available data types
        nTypes = Integer.parseInt(line.substring(0, 6).trim());

        // Allocate the array that stores data type order
        typeOrder = new int[nTypes];
        /*
        * Parse data types and store order (internal order: C1 P1 P2 L1 L2 S1
        * S2 D1 D2)
        */
        for (int i = 0; i < nTypes; i++) {
            String type = line.substring(6 * (i + 2) - 2, 6 * (i + 2));
            assignTypeOrder(type, i);
        }

    }

    @Override
    protected final void parseTimeofFirstObs(String line) {
        // Format date string according to DateStringToTime required format
        String dateStr = line.substring(0, 42).trim().replace("    ", " ") .replace("   ", " ");

        // Create time object
        //timeFirstObs = new Time();

        // Convert date string to standard UNIX time in milliseconds
        try {
            timeFirstObs = new Time(dateStr); //Time.dateStringToTime(dateStr);

        } catch (ParseException e) {
            // Display an error if END OF HEADER was not reached
            System.err.println("TIME OF FIRST OBS parsing failed in file "
                    + fileObs.toString());
        }
    }

    @Override
    protected void assignTypeOrder(String type, int i) {
        if (type.equals("C1")) {
            typeOrder[i] = 0;
        } else if (type.equals("C2")) {
            typeOrder[i] = 1;
        } else if (type.equals("P1")) {
            typeOrder[i] = 2;
        } else if (type.equals("P2")) {
            typeOrder[i] = 3;
        } else if (type.equals("L1")) {
            typeOrder[i] = 4;
        } else if (type.equals("L2")) {
            typeOrder[i] = 5;
        } else if (type.equals("S1")) {
            typeOrder[i] = 6;
            hasS1Field = true;
        } else if (type.equals("S2")) {
            typeOrder[i] = 7;
            hasS2Field = true;
        } else if (type.equals("D1")) {
            typeOrder[i] = 8;
        } else if (type.equals("D2")) {
            typeOrder[i] = 9;
        }
    }
}
