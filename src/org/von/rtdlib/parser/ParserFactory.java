package org.von.rtdlib.parser;

import org.von.rtdlib.parser.rinex.*;
import org.von.rtdlib.provider.NavigationProvider;
import org.von.rtdlib.provider.ObservationsProvider;
import org.von.rtdlib.provider.PseudoRangeCorrectionProvider;

import java.io.*;

/**
 * Created by diner on 16-10-20.
 * 简单工厂模式，根据输入的需要生产出相应的Provider
 */
public class ParserFactory {


    /**  variable for rinex parser */
    private static FileInputStream stream;
    private static InputStreamReader inStream;
    private static BufferedReader buffStream;



    public static ObservationsProvider produceRinexObsProvider(File file) throws FileNotFoundException {
       int ver = getRinexObsVer(file);
        switch (ver)
        {
            case 2: return new RinexObservationParserV2(file);
            case 212: return new RinexObservationParserV212(file);
            case 3: return new RinexObservationParserV3(file);
            case 302: return new RinexObservationParserV302(file);

        }
        return null;
   }

    public static NavigationProvider produceRinexNavProvider(File file) {
        int ver = getRinexNavVer(file);
        switch (ver)
        {
            case 2: return new RinexNavigationParserV2(file);
            case 3: return new RinexNavigationParserV3(file);
        }
        return null;
    }

    public static PseudoRangeCorrectionProvider producePseudoRangeCorrectionProvider(File file) {
        return new RinexPseudoRangeCorrectionParser(file) ;
    }
    /** Only for ver 2.*/
    public static NavigationProvider produceRinexNavProvider(File[] files) {
        return new  RinexNavigationParserV2(files);
    }



    private static void open(File fileObs) throws FileNotFoundException {
        stream = new FileInputStream(fileObs);
        inStream = new InputStreamReader(stream);
        buffStream = new BufferedReader(inStream);
    }
    private static int getRinexObsVer(File fileObs) {

        int ver = -1;
        try {
            open(fileObs);
            while (buffStream.ready()) {
                String line = buffStream.readLine();
                String typeField = line.substring(60, line.length());
                typeField = typeField.trim();
                if (typeField.equals("RINEX VERSION / TYPE")) {

                    if (!line.substring(20, 21).equals("O")) {

                        // Error if observation file identifier was not found
                        throw new Exception("Observation file identifier is missing in file "
                                + fileObs.toString() + " header");

                    }else if (line.substring(5, 9).equals("3.02")) {
                        ver = 302;
                    }
                    else if (line.substring(5, 7).equals("3.")) {
                        ver = 3;
                    } else if (line.substring(5, 9).equals("2.12")) {
                        ver = 212;
                    } else {
                        ver = 2;
                    }
                    break;
                }
            }
            release();

        }catch (IOException e) {
            e.printStackTrace();
        } catch (StringIndexOutOfBoundsException e) {
            // Skip over blank lines
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ver;
    }
    private static int getRinexNavVer(File fileNav) {
        int ver = -1;
        try {
            open(fileNav);
            while (buffStream.ready()) {
                String line = buffStream.readLine();
                String typeField = line.substring(60, line.length());
                typeField = typeField.trim();

                if (typeField.equals("RINEX VERSION / TYPE")) {

                    if (!line.substring(20, 21).equals("N")) {

                        // Error if navigation file identifier was not found
                        System.err.println("Navigation file identifier is missing in file " + fileNav.toString() + " header");
                        return ver = -1;

                    } else if (line.substring(5, 7).equals("3.")) {

//							System.out.println("Ver. 3.01");
                        ver = 3;

                    } else if (line.substring(5, 9).equals("2.12")) {

//							System.out.println("Ver. 2.12");
                        ver = 212;

                    } else {

//							System.out.println("Ver. 2.x");
                        ver = 2;
                    }
                    break;
                }
            }
            release();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ver;
    }
    public static void release() throws InterruptedException {
        try {
            stream.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            inStream.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            buffStream.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

}
