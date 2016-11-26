package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.positioning.models.EphemerisBDS;
import org.von.rtdlib.positioning.models.EphemerisGLONASS;
import org.von.rtdlib.positioning.models.EphemerisGPS;
import org.von.rtdlib.positioning.models.IonoGPS;
import org.von.rtdlib.provider.EphemerisProvider;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.EmptyStackException;

/**
 * Created by diner on 16-10-16.
 */
public class RinexNavigationParserV3 extends RinexNavigationParser{
    public RinexNavigationParserV3(File file) {
        super(file);
    }


    @Override
    public IonosphereCorrectionProvider getIono(long unixTime) {
        return super.getIono(unixTime);
    }
    @Override
    protected void parseHeader(){
        String sub;
//						String typeField3 = line.substring(60, line.length());
//						typeField3 = typeField3.trim();

//						System.out.println(typeField2);
        try {
            while (buffStreamNav.ready()) {
                String line = buffStreamNav.readLine();
                String typeField = line.substring(60, line.length());
                typeField = typeField.trim();

                if (typeField.equals("GPSA")) {

//							System.out.println("GPSA");

                    float a[] = new float[4];
                    sub = line.substring(7, 17).replace('D', 'e');
                    //Navigation.iono[0] = Double.parseDouble(sub.trim());
                    a[0] = Float.parseFloat(sub.trim());

                    sub = line.substring(18, 29).replace('D', 'e');
                    //Navigation.iono[1] = Double.parseDouble(sub.trim());
                    a[1] = Float.parseFloat(sub.trim());

                    sub = line.substring(30, 41).replace('D', 'e');
                    //Navigation.iono[2] = Double.parseDouble(sub.trim());
                    a[2] = Float.parseFloat(sub.trim());

                    sub = line.substring(42, 53).replace('D', 'e');
                    //Navigation.iono[3] = Double.parseDouble(sub.trim());
                    a[3] = Float.parseFloat(sub.trim());

                    if (iono == null) iono = new IonoGPS();
                    iono.setAlpha(a);
//
//							System.out.println(a[0]);
//							System.out.println(a[1]);
//							System.out.println(a[2]);
//							System.out.println(a[3]);
                } else if (typeField.equals("GPSB")) {

//							System.out.println("GPSB");

                    float b[] = new float[4];

                    sub = line.substring(7, 17).replace('D', 'e');
                    //Navigation.iono[4] = Double.parseDouble(sub.trim());
                    //setIono(4, Double.parseDouble(sub.trim()));
                    b[0] = Float.parseFloat(sub.trim());


                    sub = line.substring(18, 29).replace('D', 'e');
                    //Navigation.iono[5] = Double.parseDouble(sub.trim());
                    //setIono(5, Double.parseDouble(sub.trim()));
                    b[1] = Float.parseFloat(sub.trim());

                    sub = line.substring(30, 41).replace('D', 'e');
                    //Navigation.iono[6] = Double.parseDouble(sub.trim());
                    //setIono(6, Double.parseDouble(sub.trim()));
                    b[2] = Float.parseFloat(sub.trim());

                    sub = line.substring(42, 53).replace('D', 'e');
                    //Navigation.iono[7] = Double.parseDouble(sub.trim());
                    //setIono(7, Double.parseDouble(sub.trim()));
                    b[3] = Float.parseFloat(sub.trim());

                    if (iono == null) iono = new IonoGPS();
                    iono.setBeta(b);
                } else if (typeField.equals("END OF HEADER")) {
//							System.out.println("END OF HEADER");
                    return;
                }
            }
        }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    @Override
    protected void parseData() {
        try {

            while (buffStreamNav.ready()) {
                String sub;
                char satType;

                satType = (char)buffStreamNav.read();

//				System.out.println(s);
                if (satType == 'G'){
//						System.out.println(satType);
                    // read 8 lines
                    EphemerisGPS eph = new EphemerisGPS();
                    parseGPSNav(buffStreamNav,eph,satType);
                    addEph(eph);

                }
                else if (satType == 'C')
                {
                    EphemerisBDS eph = new EphemerisBDS();
                    parseBDSNav(buffStreamNav,eph,satType);
                    addEph(eph);

                }
                else if (satType == 'R') {   // In case of GLONASS data
//						System.out.println("satType: " + satType);
                    EphemerisGLONASS eph = new EphemerisGLONASS();
                    parseGLONASSNav(buffStreamNav,eph,satType);
                    addEph(eph);

                } else { //SBAS data

                    for (int i = 0; i < 4; i++) {
                    }

                }  // End of GLO if


                // Increment array index
//				j++;
                // Store the number of ephemerides
                //Navigation.n = j;
            } // End of while

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }


    protected void parseBDSNav(BufferedReader buffStreamNav, EphemerisBDS eph,char satType) throws IOException {

        parseGPSNav(buffStreamNav,eph,satType);

    }

    protected void parseGPSNav(BufferedReader buffStreamNav, EphemerisGPS eph,char satType) throws IOException {

        String sub;
        for (int i = 0; i < 8; i++) {
            String line = buffStreamNav.readLine();
            try {
                int len = line.length();

                if (len != 0) {
                    if (i == 0) { // LINE 1

                        //Navigation.eph.get(j).refTime = new Time();

                        //Navigation.eph.add(eph);

                        eph.setSatType(satType);

                        // Get satellite ID
                        sub = line.substring(0, 2).trim();
//										System.out.println(sub);
                        eph.setSatID(Integer.parseInt(sub));

                        // Get and format date and time string
                        String dT = line.substring(3, 22);
                        //								dT = dT.replace("  ", " 0").trim();
                        dT = dT + ".0";
//										System.out.println(dT);

                        try {
                            //Time timeEph = new Time(dT);
                            // Convert String to UNIX standard time in
                            // milliseconds
                            //timeEph.msec = Time.dateStringToTime(dT);
                            Time toc = new Time(dT);
                            eph.setRefTime(toc);
                            eph.setToc(toc.getGpsWeekSec());

                            // sets Iono reference time
                            if(iono!=null && iono.getRefTime()==null) iono.setRefTime(new Time(dT));

                        } catch (ParseException e) {
                            System.err.println("Time parsing failed");
                        }

                        sub = line.substring(22, 41).replace('D', 'e');
                        eph.setAf0(Double.parseDouble(sub.trim()));

                        sub = line.substring(41, 60).replace('D', 'e');
                        eph.setAf1(Double.parseDouble(sub.trim()));

                        sub = line.substring(60, len).replace('D', 'e');
                        eph.setAf2(Double.parseDouble(sub.trim()));

                    } else if (i == 1) { // LINE 2

                        sub = line.substring(4, 23).replace('D', 'e');
                        double iode = Double.parseDouble(sub.trim());
                        // TODO check double -> int conversion ?
                        eph.setIode((int) iode);

                        sub = line.substring(23, 42).replace('D', 'e');
                        eph.setCrs(Double.parseDouble(sub.trim()));

                        sub = line.substring(42, 61).replace('D', 'e');
                        eph.setDeltaN(Double.parseDouble(sub.trim()));

                        sub = line.substring(61, len).replace('D', 'e');
                        eph.setM0(Double.parseDouble(sub.trim()));

                    } else if (i == 2) { // LINE 3

                        sub = line.substring(4, 23).replace('D', 'e');
                        eph.setCuc(Double.parseDouble(sub.trim()));

                        sub = line.substring(23, 42).replace('D', 'e');
                        eph.setE(Double.parseDouble(sub.trim()));

                        sub = line.substring(42, 61).replace('D', 'e');
                        eph.setCus(Double.parseDouble(sub .trim()));

                        sub = line.substring(61, len).replace('D', 'e');
                        eph.setRootA(Double.parseDouble(sub.trim()));

                    } else if (i == 3) { // LINE 4

                        sub = line.substring(4, 23).replace('D', 'e');
                        eph.setToe(Double.parseDouble(sub.trim()));

                        sub = line.substring(23, 42).replace('D', 'e');
                        eph.setCic(Double.parseDouble(sub.trim()));

                        sub = line.substring(42, 61).replace('D', 'e');
                        eph.setOmega0(Double.parseDouble(sub.trim()));

                        sub = line.substring(61, len).replace('D', 'e');
                        eph.setCis(Double.parseDouble(sub.trim()));

                    } else if (i == 4) { // LINE 5

                        sub = line.substring(4, 23).replace('D', 'e');
                        eph.setI0(Double.parseDouble(sub.trim()));

                        sub = line.substring(23, 42).replace('D', 'e');
                        eph.setCrc(Double.parseDouble(sub.trim()));

                        sub = line.substring(42, 61).replace('D', 'e');
                        eph.setOmega(Double.parseDouble(sub.trim()));

                        sub = line.substring(61, len).replace('D', 'e');
                        eph.setOmegaDot(Double.parseDouble(sub.trim()));

                    } else if (i == 5) { // LINE 6

                        sub = line.substring(4, 23).replace('D', 'e');
                        eph.setiDot(Double.parseDouble(sub.trim()));

                        sub = line.substring(23, 42).replace('D', 'e');
                        double L2Code = Double.parseDouble(sub.trim());
                        eph.setL2Code((int) L2Code);

                        sub = line.substring(42, 61).replace('D', 'e');
                        double week = Double.parseDouble(sub.trim());
                        eph.setWeek((int) week);

                        sub = line.substring(61, len).replace('D', 'e');
                        if (!sub.trim().isEmpty()) {
                            double L2Flag = Double.parseDouble(sub.trim());
                            eph.setL2Flag((int) L2Flag);
                        } else {
                            eph.setL2Flag(0);
                        }

                    } else if (i == 6) { // LINE 7

                        sub = line.substring(4, 23).replace('D', 'e');
                        double svAccur = Double.parseDouble(sub.trim());
                        eph.setSvAccur((int) svAccur);

                        sub = line.substring(23, 42).replace('D', 'e');
                        double svHealth = Double.parseDouble(sub.trim());
                        eph.setSvHealth((int) svHealth);

                        sub = line.substring(42, 61).replace('D', 'e');
                        eph.setTgd(Double.parseDouble(sub.trim()));

                        sub = line.substring(61, len).replace('D', 'e');
                        double iodc = Double.parseDouble(sub.trim());
                        eph.setIodc((int) iodc);

                    } else if (i == 7) { // LINE 8

                        sub = line.substring(4, 23).replace('D', 'e');
                        eph.setTom(Double.parseDouble(sub.trim()));

                        if (line.trim().length() > 22) {
                            sub = line.substring(23, 42).replace('D', 'e');
                            eph.setFitInt(Double.parseDouble(sub.trim()));

                        } else {
                            eph.setFitInt(0);
                        }
                    }
                } else {
                    i--;
                }


            } catch (NullPointerException e) {
                // Skip over blank lines
            }

        }  // End of for

    }

    protected void parseGLONASSNav(BufferedReader buffStreamNav, EphemerisGLONASS eph,char satType) throws IOException {
        String sub;
        for (int i = 0; i < 4; i++) {
            String line = buffStreamNav.readLine();
            try {
                int len = line.length();

                if (len != 0) {
                    if (i == 0) { // LINE 1

                        //Navigation.eph.get(j).refTime = new Time();

                        eph.setSatType(satType);

                        // Get satellite ID
                        sub = line.substring(0, 2).trim();
//										System.out.println("ID: "+sub);
                        eph.setSatID(Integer.parseInt(sub));

                        // Get and format date and time string
                        String dT = line.substring(3, 22);
                        //								dT = dT.replace("  ", " 0").trim();
                        dT = dT + ".0";

//										System.out.println("dT: " + dT);

                        try {
                            //Time timeEph = new Time(dT);
                            // Convert String to UNIX standard time in
                            // milliseconds
                            //timeEph.msec = Time.dateStringToTime(dT);


                            Time dtoc = new Time(dT);
                            eph.setRefTime(dtoc);
                            int toc = dtoc.getGpsWeekSec();
//												System.out.println("toc: " + toc);
                            eph.setToc(toc);

                            int week = dtoc.getGpsWeek();
//												System.out.println("week: " + week);
                            eph.setWeek(week);

                            double toe = toc;
//												System.out.printf("%.3f\n", gTime);
//												System.out.println("timeEph: " + toe);
                            eph.setToe(toe);

                            // sets Iono reference time
                            if(iono!=null && iono.getRefTime()==null) iono.setRefTime(new Time(dT));

                        } catch (ParseException e) {
                            System.err.println("Time parsing failed");
                        }

										/* TauN */
                        sub = line.substring(22, 41).replace('D', 'e');
//										System.out.println(sub);
                        eph.setTauN(Float.parseFloat(sub.trim()));

										/* GammaN */
                        sub = line.substring(41, 60).replace('D', 'e');
//										System.out.println(sub);
                        eph.setGammaN(Float.parseFloat(sub.trim()));

										/* tb */
                        sub = line.substring(60, len).replace('D', 'e');
//										System.out.println("tb: " + sub);

										/* tb is a time interval within the current day (UTC + 3 hours)*/
                        double tb = Double.parseDouble(sub.trim());
                        double tk = tb - 10800;
//										System.out.println("tk: " + tk);
                        eph.settk(tk);


//										eph.settb(Double.parseDouble(sub.trim()));

                    } else if (i == 1) { // LINE 2

										/* X: satellite X coordinate at ephemeris reference time [m] */
                        sub = line.substring(4, 23).replace('D', 'e');
//										System.out.println(sub);
                        eph.setX(Double.parseDouble(sub.trim())*1e3);

										/* Xv: satellite velocity along X at ephemeris reference time [m/s] */
                        sub = line.substring(23, 42).replace('D', 'e');
//										System.out.println(sub);
                        eph.setXv(Double.parseDouble(sub.trim())*1e3);

										/* Xa: acceleration due to lunar-solar gravitational perturbation along X at ephemeris reference time [m/s^2] */
                        sub = line.substring(42, 61).replace('D', 'e');
//										System.out.println(sub);
                        eph.setXa(Double.parseDouble(sub.trim())*1e3);

										/* Bn */
                        sub = line.substring(61, len).replace('D', 'e');
//										System.out.println(sub);
                        eph.setBn(Double.parseDouble(sub.trim()));

                    } else if (i == 2) { // LINE 3

										/* Y: satellite Y coordinate at ephemeris reference time [m] */
                        sub = line.substring(4, 23).replace('D', 'e');
//										System.out.println(sub);
                        eph.setY(Double.parseDouble(sub.trim())*1e3);

										/* Yv: satellite velocity along Y at ephemeris reference time [m/s] */
                        sub = line.substring(23, 42).replace('D', 'e');
//										System.out.println(sub);
                        eph.setYv(Double.parseDouble(sub.trim())*1e3);

										/* Ya: acceleration due to lunar-solar gravitational perturbation along Y at ephemeris reference time [m/s^2] */
                        sub = line.substring(42, 61).replace('D', 'e');
//										System.out.println(sub);
                        eph.setYa(Double.parseDouble(sub.trim())*1e3);

										/* freq_num */
                        sub = line.substring(61, len).replace('D', 'e');
//										System.out.println(sub);
                        eph.setfreq_num((int) Double.parseDouble(sub.trim()));

                    } else if (i == 3) { // LINE 4

										/* Z: satellite Z coordinate at ephemeris reference time [m] */
                        sub = line.substring(4, 23).replace('D', 'e');
//										System.out.println(sub);
                        eph.setZ(Double.parseDouble(sub.trim())*1e3);

										/* Zv: satellite velocity along Z at ephemeris reference time [m/s] */
                        sub = line.substring(23, 42).replace('D', 'e');
//										System.out.println(sub);
                        eph.setZv(Double.parseDouble(sub.trim())*1e3);

										/* Za: acceleration due to lunar-solar gravitational perturbation along Z at ephemeris reference time [m/s^2]  */
                        sub = line.substring(42, 61).replace('D', 'e');
//										System.out.println(sub);
                        eph.setZa(Double.parseDouble(sub.trim())*1e3);

										/* En */
                        sub = line.substring(61, len).replace('D', 'e');
//										System.out.println(sub);
//										eph.setEn(Long.parseLong(sub.trim()));
                        eph.setEn(Double.parseDouble(sub.trim()));


                    } // End of if

                } else {
                    i--;
                }
                //		}  // End of if


            } catch (NullPointerException e) {
                // Skip over blank lines
            }

        } // End of for
    }
}
