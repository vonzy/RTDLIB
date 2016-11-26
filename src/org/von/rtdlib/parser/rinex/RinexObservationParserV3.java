package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.ObservationSet;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.ObservationsProvider;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by diner on 16-10-16.
 */
public class RinexObservationParserV3 extends RinexObservationParser{




    public RinexObservationParserV3(File file) {
        super(file);
        this.ver = 3;
    }

    @Override
    protected void parseHeader() {
        boolean foundTypeObs = false;

        try {
            while (buffStreamObs.ready()) {
                String line = buffStreamObs.readLine();
                String typeField = line.substring(60, line.length());
                typeField = typeField.trim();
                if (typeField.equals("SYS / # / OBS TYPES")) {

                    String satType = line.substring(0, 1);
//							System.out.println("sys: " + sys);

//							if(satType.equals("G")){
                    parseTypes(line, satType);
                    foundTypeObs = true;
//							}

                } else if (typeField.equals("TIME OF FIRST OBS")) {
                    parseTimeofFirstObs(line);
                } else if (typeField.equals("APPROX POSITION XYZ")) {
                    parseApproxPos(line);
                } else if (typeField.equals("ANTENNA: DELTA H/E/N")) {
                    parseAntDelta(line);
                } else if (typeField.equals("END OF HEADER")) {
                    if (!foundTypeObs) {
                        // Display an error if TIME OF FIRST OBS was not found
                        System.err.println("Critical information"
                                + "(TYPES OF OBSERV) is missing in file "
                                + fileObs.toString() + " header");
                    }
                    break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void parseTimeofFirstObs(String line) {

        // Format date string according to DateStringToTime required format
//		String dateStr = line.substring(0, 43).trim().replace("    ", " ") .replace("   ", " ");
        String dateStr = line.substring(0, 43).trim().replace("    ", " ") .replace("   ", " ");
//		System.out.println(dateStr);


        // Create time object
        //timeFirstObs = new Time();

        // Convert date string to standard UNIX time in milliseconds
        try {
            timeFirstObs = new Time(dateStr); //Time.dateStringToTime(dateStr);
//			System.out.println("TIME OF FIRST OBS: " + timeFirstObs);


        } catch (ParseException e) {
            // Display an error if END OF HEADER was not reached
            System.err.println("TIME OF FIRST OBS parsing failed in file "
                    + fileObs.toString());
        }
    }
    /**
     * @param line
     * @param satType
     * @throws IOException
     */

    protected void parseTypes(String line, String satType) throws IOException {

        // Extract number of available data types
        nTypes = Integer.parseInt(line.substring(1, 6).trim());

        // Allocate the array that stores data type order
        typeOrder = new int[nTypes];

        if(nTypes > 13){  // In case of more than 13 Types, it will two lines

            for (int i = 0; i <= 12; i++) {
                String type = line.substring(4 * (i + 3) -5 , 4 * (i + 3) -2);
                assignTypeOrder(type, i);
            }

            line = buffStreamObs.readLine();   // read the second line

            int j = 0;
            for (int i = 13; i <= nTypes  ; i++) {
                String type = line.substring(4 * (j + 3) -5 , 4 * (j + 3) -2);
                assignTypeOrder(type, i);
                j++ ;
            }

        } else {  // less than 14 types, it will be one line.

            for (int i = 0; i < nTypes; i++) {
                String type = line.substring(4 * (i + 3) -5 , 4 * (i + 3) -2);
                assignTypeOrder(type, i);
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


    /**根据Rinex 3.03中（按照频率）对观测值类型的定义，
     * 某一系统的第二个频点上的观测值可能是另一系统的第一个频点的观测值
     * 比如 */
    @Override
    protected void assignTypeOrder(String type, int i) {

        if (type.equals("C1C")) {
            typeOrder[i] = 0;
        } else if (type.equals("C2C")) {
            typeOrder[i] = 1;
        } else if (type.equals("P1C")) {
            typeOrder[i] = 2;
        } else if (type.equals("P2C")) {
            typeOrder[i] = 3;
        } else if (type.equals("L1C")) {
            typeOrder[i] = 4;
        } else if (type.equals("L2C")) {
            typeOrder[i] = 5;
        } else if (type.equals("S1C")) {
            typeOrder[i] = 6;
            hasS1Field = true;
        } else if (type.equals("S2C")) {
            typeOrder[i] = 7;
            hasS2Field = true;
        } else if (type.equals("D1C")) {
            typeOrder[i] = 8;
        } else if (type.equals("D2C")) {
            typeOrder[i] = 9;

        } else if (type.equals("C1W")) {
            typeOrder[i] = 10;
        } else if (type.equals("L1W")) {
            typeOrder[i] = 11;
        } else if (type.equals("D1W")) {
            typeOrder[i] = 12;
        } else if (type.equals("S1W")) {
            typeOrder[i] = 13;

        } else if (type.equals("C2W")) {
            typeOrder[i] = 14;
        } else if (type.equals("L2W")) {
            typeOrder[i] = 15;
        } else if (type.equals("D2W")) {
            typeOrder[i] = 16;
        } else if (type.equals("S2W")) {
            typeOrder[i] = 17;

        } else if (type.equals("C1X")) {
            typeOrder[i] = 18;
        } else if (type.equals("L1X")) {
            typeOrder[i] = 19;
        } else if (type.equals("D1X")) {
            typeOrder[i] = 20;
        } else if (type.equals("S1X")) {
            typeOrder[i] = 21;

        } else if (type.equals("C2X")) {
            typeOrder[i] = 22;
        } else if (type.equals("L2X")) {
            typeOrder[i] = 23;
        } else if (type.equals("D2X")) {
            typeOrder[i] = 24;
        } else if (type.equals("S2X")) {
            typeOrder[i] = 25;

        } else if (type.equals("C5X")) {
            typeOrder[i] = 26;
        } else if (type.equals("L5X")) {
            typeOrder[i] = 27;
        } else if (type.equals("D5X")) {
            typeOrder[i] = 28;
        } else if (type.equals("S5X")) {
            typeOrder[i] = 29;

        } else if (type.equals("C6X")) {
            typeOrder[i] = 30;
        } else if (type.equals("L6X")) {
            typeOrder[i] = 31;
        } else if (type.equals("D6X")) {
            typeOrder[i] = 32;
        } else if (type.equals("S6X")) {
            typeOrder[i] = 33;

        } else if (type.equals("C7X")) {
            typeOrder[i] = 34;
        } else if (type.equals("L7X")) {
            typeOrder[i] = 35;
        } else if (type.equals("D7X")) {
            typeOrder[i] = 36;
        } else if (type.equals("S7X")) {
            typeOrder[i] = 37;

        } else if (type.equals("C8X")) {
            typeOrder[i] = 38;
        } else if (type.equals("L8X")) {
            typeOrder[i] = 39;
        } else if (type.equals("D8X")) {
            typeOrder[i] = 40;
        } else if (type.equals("S8X")) {
            typeOrder[i] = 41;

        } else if (type.equals("C1P")) {
            typeOrder[i] = 42;
        } else if (type.equals("L1P")) {
            typeOrder[i] = 43;
        } else if (type.equals("D1P")) {
            typeOrder[i] = 44;
        } else if (type.equals("S1P")) {
            typeOrder[i] = 45;

        } else if (type.equals("C2P")) {
            typeOrder[i] = 46;
        } else if (type.equals("L2P")) {
            typeOrder[i] = 47;
        } else if (type.equals("D2P")) {
            typeOrder[i] = 48;
        } else if (type.equals("S2P")) {
            typeOrder[i] = 49;

        } else if (type.equals("C2I")) {
            typeOrder[i] = 50;
        } else if (type.equals("L2I")) {
            typeOrder[i] = 51;
        } else if (type.equals("D2I")) {
            typeOrder[i] = 52;
        } else if (type.equals("S2I")) {
            typeOrder[i] = 53;

        } else if (type.equals("C6I")) {
            typeOrder[i] = 54;
        } else if (type.equals("L6I")) {
            typeOrder[i] = 55;
        } else if (type.equals("D6I")) {
            typeOrder[i] = 56;
        } else if (type.equals("S6I")) {
            typeOrder[i] = 57;

        } else if (type.equals("C7I")) {
            typeOrder[i] = 58;
        } else if (type.equals("L7I")) {
            typeOrder[i] = 59;
        } else if (type.equals("D7I")) {
            typeOrder[i] = 60;
        } else if (type.equals("S7I")) {
            typeOrder[i] = 61;

        }
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
//			System.out.println("nSat: " + nSat);

            for (int i = 0; i < nSat; i++) {

                // Read line of observations
                String line = buffStreamObs.readLine();

                String satType = line.substring(0, 1);
                String satNum = line.substring(1, 3);

                int satID = Integer.parseInt(satNum.trim());


                if (satType.equals("G") && gpsEnable) {

                    // Create observation object
                    ObservationSet os = new ObservationSet();
                    os.setSatType('G');
                    os.setSatID(satID);
                    obs.setObsSets(i, os);

                    line = line.substring(3);
                    // Parse observation data according to typeOrder
                    int j = 0;
                    for (int k = 0; k < nTypesG; k++) {
                        assignObsWithType(line, k, j, i, os.getSatType());
                        j = j + 16;
                    }

                } else if (satType.equals("R") && gloEnable){

                    // Create observation object
                    ObservationSet os = new ObservationSet();
                    os.setSatType('R');
                    os.setSatID(satID);
                    obs.setObsSets(i, os);

                    line = line.substring(3);
                    // Parse observation data according to typeOrder
                    int j = 0;
                    for (int k = 0; k < nTypesR; k++) {
                        assignObsWithType(line, k, j, i, os.getSatType());
                        j = j + 16;
                    }

                } else if (satType.equals("E") && galEnable){

                    // Create observation object
                    ObservationSet os = new ObservationSet();
                    os.setSatType('E');
                    os.setSatID(satID);
                    obs.setObsSets(i, os);

                    line = line.substring(3);
                    // Parse observation data according to typeOrder
                    int j = 0;
                    for (int k = 0; k < nTypesE; k++) {
                        assignObsWithType(line, k, j, i, os.getSatType());
                        j = j + 16;
                    }

                } else if (satType.equals("J") && qzsEnable){

                    // Create observation object
                    ObservationSet os = new ObservationSet();
                    os.setSatType('J');
                    os.setSatID(satID);
                    obs.setObsSets(i, os);

                    line = line.substring(3);
                    // Parse observation data according to typeOrder
                    int j = 0;
                    for (int k = 0; k < nTypesJ; k++) {
                        assignObsWithType(line, k, j, i, os.getSatType());
                        j = j + 16;
                    }

                } else if (satType.equals("C") && bdsEnable){

                    // Create observation object
                    ObservationSet os = new ObservationSet();
                    os.setSatType('C');
                    os.setSatID(satID);
                    obs.setObsSets(i, os);

                    line = line.substring(3);
                    // Parse observation data according to typeOrder
                    int j = 0;
                    for (int k = 0; k < nTypesC; k++) {
                        assignObsWithType(line, k, j, i, os.getSatType());
                        j = j + 16;
                    }
                } // End of if
            }  // End of for

        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            // Skip over blank lines
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Observations getNextObservations(){

        try {
            if(!hasMoreObservations()) return null;
            String line = buffStreamObs.readLine();

            // Parse date and time
            String dateStr = line.substring(2, 25);

            // Parse event flag
            String eFlag = line.substring(30, 32).trim();

            int eventFlag = Integer.parseInt(eFlag);

            // Parse available satellites string
            String satAvail = line.substring(33, 35).trim();

            // Parse number of available satellites
//				String numOfSat = satAvail.substring(0, 2).trim();
            nSat = Integer.parseInt(satAvail);

            // Arrays to store satellite order
//				satOrder = new int[nSat];
//				sysOrder = new char[nSat];

            nGps = 0;
            nGlo = 0;
            nSbs = 0;
            nQzs = 0;
            nBds = 0;

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
        }
        catch (ParseException e) {
            // Skip over unexpected observation lines
            e.printStackTrace();
        } catch (StringIndexOutOfBoundsException e) {
            // Skip over blank lines
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
