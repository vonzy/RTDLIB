package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.provider.ObservationsProvider;

import java.io.File;
import java.io.IOException;

/**
 * Created by diner on 16-10-16.
 */
public class RinexObservationParserV212 extends RinexObservationParserV2{

    public RinexObservationParserV212(File file) {
        super(file);
        this.ver = 212;
    }

//    @Override
//    protected void parseHeader() {
//
//        boolean foundTypeObs = false;
//
//        try {
//            while (buffStreamObs.ready()) {
//                String line = buffStreamObs.readLine();
//                String typeField = line.substring(60, line.length());
//                typeField = typeField.trim();
//
//                if (typeField.equals("# / TYPES OF OBSERV")) {
//                    parseTypes(line);
//                    foundTypeObs = true;
//                }
//                else if (typeField.equals("TIME OF FIRST OBS")) {
//                    parseTimeofFirstObs(line);
//                }
//                else if (typeField.equals("APPROX POSITION XYZ")) {
//                    parseApproxPos(line);
//                }
//                else if (typeField.equals("ANTENNA: DELTA H/E/N")) {
//                    parseAntDelta(line);
//                }
//                else if (typeField.equals("END OF HEADER")) {
//                    if (!foundTypeObs) {
//                        // Display an error if TIME OF FIRST OBS was not found
//                        System.err.println("Critical information"
//                                + "(TYPES OF OBSERV) is missing in file "
//                                + fileObs.toString() + " header");
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    protected void parseTypes(String line) throws IOException {

        // Extract number of available data types
        nTypes = Integer.parseInt(line.substring(1, 6).trim());

        // Allocate the array that stores data type order
        typeOrder = new int[nTypes];

        if(nTypes >= 19 ){  // In case of more than 18 Types, it will three lines

            int i = 0;
            for (int j = 0; j <= 8; j++) {
                String type = line.substring(6 * (j + 2) - 2, 6 * (j + 2));
                assignTypeOrder(type, i);
                i++;
            }

            line = buffStreamObs.readLine();   // read the second line, from type 10 - 18

            for (int j = 0; j <= 8  ; j++) {
                String type = line.substring(6 * (j + 2) - 2, 6 * (j + 2));
                assignTypeOrder(type, i);
                i++;
            }

            line = buffStreamObs.readLine();   // read the third line, from type 19 -

            for (int j = 0; j < nTypes-18 ; j++) {
                String type = line.substring(6 * (j + 2) - 2, 6 * (j + 2));
                assignTypeOrder(type, i);
                i++;
            }

        } else if (nTypes > 9 && nTypes < 19){  // In case of 10 - 18 Types, it will two lines

            int i = 0;
            for (int j = 0; j <= 8; j++) {
                String type = line.substring(6 * (j + 2) - 2, 6 * (j + 2));
                assignTypeOrder(type, i);
                i++;
            }

            line = buffStreamObs.readLine();   // read the second line, from type 10

            for (int j = 0; j < nTypes-9  ; j++) {
                String type = line.substring(6 * (j + 2) - 2, 6 * (j + 2));
                assignTypeOrder(type, i);
                i++ ;
            }

        } else {  // less than 10 types, it will be one line.

            for (int i = 0; i < nTypes; i++) {
                String type = line.substring(6 * (i + 2) - 2, 6 * (i + 2));
                assignTypeOrder(type, i);
            }
        }


    }

    @Override
    protected void assignTypeOrder(String type, int i) {
        if (type.equals("C1") || type.equals("CA")) {
            typeOrder[i] = 0;
        } else if (type.equals("C2")) {
            typeOrder[i] = 1;
        } else if (type.equals("P1")) {
            typeOrder[i] = 2;
        } else if (type.equals("P2") || type.equals("CC")) {
            typeOrder[i] = 3;
        } else if (type.equals("L1") || type.equals("LA")) {
            typeOrder[i] = 4;
        } else if (type.equals("L2") || type.equals("LC")) {
            typeOrder[i] = 5;
        } else if (type.equals("S1") || type.equals("SA")) {
            typeOrder[i] = 6;
            hasS1Field = true;
        } else if (type.equals("S2") || type.equals("SC")) {
            typeOrder[i] = 7;
            hasS2Field = true;
        } else if (type.equals("D1") || type.equals("DA")) {
            typeOrder[i] = 8;
        } else if (type.equals("D2") || type.equals("DC")) {
            typeOrder[i] = 9;
        }
    }
}
