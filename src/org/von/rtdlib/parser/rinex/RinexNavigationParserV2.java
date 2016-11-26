package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.positioning.models.*;
import org.von.rtdlib.provider.EphemerisProvider;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

import java.io.*;
import java.text.ParseException;

/**
 * Created by diner on 16-10-16.
 */
public class RinexNavigationParserV2 extends RinexNavigationParser{

    protected File[] fileNavs;  // use array for multi navigation files
    protected FileInputStream[] streamNavs;
    protected InputStreamReader[] inStreamNavs;
    protected BufferedReader[] buffStreamNavs;

    protected boolean multiFileInput;

    public RinexNavigationParserV2(File[] fileNavs)
    {
        this.fileNavs = fileNavs;
        multiFileInput = true;
    }

    public RinexNavigationParserV2(File file) {
        super(file);
        multiFileInput = false;
    }


    @Override
    public EphemerisProvider getEphemeris(long unixTime,int satID, char satType) {
        return super.getEphemeris(unixTime, satID, satType);
    }

    @Override
    public IonosphereCorrectionProvider getIono(long unixTime) {
        return super.getIono(unixTime);
    }

    @Override
    protected void parseHeader() {
        if (multiFileInput)
        {
            for (int i = 0; i < fileNavs.length; i++)
            {
                parseOneHeader(buffStreamNavs[i],getSystemTypeByFileName(fileNavs[i]));
            }
        }
        else
        {
            parseOneHeader(buffStreamNav,getSystemTypeByFileName(fileNav));
        }
    }

    @Override
    protected void parseData() {

        if (multiFileInput)
        {
            for (int i = 0; i < fileNavs.length; i++)
            {
                switch (getSystemTypeByFileName(fileNavs[i]))
                {
                    case RTDLIB_SYSTEMTYPE_BDS:
                        parseBDSNav(buffStreamNavs[i]);
                        break;
                    case RTDLIB_SYSTEMTYPE_GPS:
                        parseGPSNav(buffStreamNavs[i]);
                        break;
                    case RTDLIB_SYSTEMTYPE_GLONASS:
                        parseGLONASSNav(buffStreamNavs[i]);
                        break;
                }
            }
        }
        else
        {
            switch (getSystemTypeByFileName(fileNav))
            {
                case RTDLIB_SYSTEMTYPE_BDS:
                    parseBDSNav(buffStreamNav);
                    break;
                case RTDLIB_SYSTEMTYPE_GPS:
                    parseGPSNav(buffStreamNav);
                    break;
                case RTDLIB_SYSTEMTYPE_GLONASS:
                    parseGLONASSNav(buffStreamNav);
                    break;
            }
        }
    }

    public void open() {
        if (multiFileInput)
        {
            try {
                streamNavs = new FileInputStream[fileNavs.length];
                inStreamNavs = new InputStreamReader[fileNavs.length];
                buffStreamNavs = new BufferedReader[fileNavs.length];
                for (int i = 0 ; i < fileNavs.length ; i++)
                {
                    if(fileNavs[i]!=null) streamNavs[i] = new FileInputStream(fileNavs[i]);
                    if(streamNavs[i]!=null) inStreamNavs[i] = new InputStreamReader(streamNavs[i]);
                    if(inStreamNavs[i]!=null) buffStreamNavs[i] = new BufferedReader(inStreamNavs[i]);
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        else super.open();
    }
    public void close() {

        if (multiFileInput)
        {
            try {
                for (int i = 0 ; i < fileNavs.length ; i++)
                {
                    if(buffStreamNavs[i]!=null) buffStreamNavs[i].close();
                    if(inStreamNavs[i]!=null) inStreamNavs[i].close();
                    if(streamNavs[i]!=null) streamNavs[i].close();
                }

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        else super.close();


    }
    private void parseOneHeader(BufferedReader buffStreamNav,int SystemType) {
        String sub;

        switch (SystemType)
        {
            case RTDLIB_SYSTEMTYPE_BDS:
                ionoBDS = new IonoBDS();
                iono = ionoBDS;
                break;
            case RTDLIB_SYSTEMTYPE_GPS:
                ionoGPS = new IonoGPS();
                iono = ionoGPS;
                break;
            case RTDLIB_SYSTEMTYPE_GLONASS:
                ionoGLONASS = new IonoGLONASS();
                iono = ionoGLONASS;
                break;
        }

        try {
            while (buffStreamNav.ready()) {
                String line = buffStreamNav.readLine();
                String typeField = line.substring(60, line.length());
                typeField = typeField.trim();
                if (typeField.equals("ION ALPHA")) {

                    float a[] = new float[4];
                    sub = line.substring(3, 14).replace('D', 'e');
                    //Navigation.iono[0] = Double.parseDouble(sub.trim());
                    a[0] = Float.parseFloat(sub.trim());

                    sub = line.substring(15, 26).replace('D', 'e');
                    //Navigation.iono[1] = Double.parseDouble(sub.trim());
                    a[1] = Float.parseFloat(sub.trim());

                    sub = line.substring(27, 38).replace('D', 'e');
                    //Navigation.iono[2] = Double.parseDouble(sub.trim());
                    a[2] = Float.parseFloat(sub.trim());

                    sub = line.substring(39, 50).replace('D', 'e');
                    //Navigation.iono[3] = Double.parseDouble(sub.trim());
                    a[3] = Float.parseFloat(sub.trim());

                    iono.setAlpha(a);

                } else if (typeField.equals("ION BETA")) {

                    float b[] = new float[4];

                    sub = line.substring(3, 14).replace('D', 'e');
                    //Navigation.iono[4] = Double.parseDouble(sub.trim());
                    //setIono(4, Double.parseDouble(sub.trim()));
                    b[0] = Float.parseFloat(sub.trim());


                    sub = line.substring(15, 26).replace('D', 'e');
                    //Navigation.iono[5] = Double.parseDouble(sub.trim());
                    //setIono(5, Double.parseDouble(sub.trim()));
                    b[1] = Float.parseFloat(sub.trim());

                    sub = line.substring(27, 38).replace('D', 'e');
                    //Navigation.iono[6] = Double.parseDouble(sub.trim());
                    //setIono(6, Double.parseDouble(sub.trim()));
                    b[2] = Float.parseFloat(sub.trim());

                    sub = line.substring(39, 50).replace('D', 'e');
                    //Navigation.iono[7] = Double.parseDouble(sub.trim());
                    //setIono(7, Double.parseDouble(sub.trim()));
                    b[3] = Float.parseFloat(sub.trim());

                    iono.setBeta(b);

                } else if (typeField.equals("DELTA-UTC: A0,A1,T,W")) {

                    sub = line.substring(3, 22).replace('D', 'e');
                    //setA0(Double.parseDouble(sub.trim()));
                    iono.setUtcA0(Double.parseDouble(sub.trim()));

                    sub = line.substring(22, 41).replace('D', 'e');
                    //setA1(Double.parseDouble(sub.trim()));
                    iono.setUtcA1(Double.parseDouble(sub.trim()));

                    sub = line.substring(41, 50).replace('D', 'e');
                    //setT(Integer.parseInt(sub.trim()));
                    // TODO need check
                    iono.setUtcWNT(Integer.parseInt(sub.trim()));

                    sub = line.substring(50, 59).replace('D', 'e');
                    //setW(Integer.parseInt(sub.trim()));
                    // TODO need check
                    iono.setUtcTOW(Integer.parseInt(sub.trim()));

                } else if (typeField.equals("LEAP SECONDS")) {
                    sub = line.substring(0, 6).trim().replace('D', 'e');
                    //setLeaps(Integer.parseInt(sub.trim()));
                    // TODO need check
                    iono.setUtcLS(Integer.parseInt(sub.trim()));

                } else if (typeField.equals("END OF HEADER")) {
                    return;
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getSystemTypeByFileName(File file) {
        switch (file.getName().toUpperCase().charAt(file.getName().length()-1))
        {
            case 'C':return RTDLIB_SYSTEMTYPE_BDS;
            case 'N':return RTDLIB_SYSTEMTYPE_GPS;
            case 'G':return RTDLIB_SYSTEMTYPE_GLONASS;
        }
        return -1;
    }
    private void parseBDSNav(BufferedReader buffStreamNav) {
        try {
        EphemerisBDS eph = null;

        while (buffStreamNav.ready()) {

            String sub;
            char satType = 'C';

            eph = new EphemerisBDS();
            addEph(eph);
            eph.setSatType(satType);

            // read 8 lines
            for (int i = 0; i < 8; i++) {

                String line = buffStreamNav.readLine();
                try {

                    int len = line.length();

                    if (len != 0) {

                        if (i == 0) { // LINE 1

                            //Navigation.eph.get(j).refTime = new Time();


                            //Navigation.eph.add(eph);
//								addEph(eph);

                            // Get satellite ID
                            sub = line.substring(0, 2).trim();
                            eph.setSatID(Integer.parseInt(sub));

                            // Get and format date and time string
                            String dT = line.substring(2, 22);
                            dT = dT.replace("  ", " 0").trim();
                            dT = "20" + dT;
//								System.out.println(dT);


                            try {
                                //Time timeEph = new Time(dT);
                                // Convert String to UNIX standard time in
                                // milliseconds
                                //timeEph.msec = Time.dateStringToTime(dT);
                                Time toc =  Time.BDT2GPST(new Time(dT));
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

                            sub = line.substring(3, 22).replace('D', 'e');
                            double iode = Double.parseDouble(sub.trim());
                            // TODO check double -> int conversion ?
                            eph.setIode((int) iode);

                            sub = line.substring(22, 41).replace('D', 'e');
                            eph.setCrs(Double.parseDouble(sub.trim()));

                            sub = line.substring(41, 60).replace('D', 'e');
                            eph.setDeltaN(Double.parseDouble(sub.trim()));

                            sub = line.substring(60, len).replace('D', 'e');
                            eph.setM0(Double.parseDouble(sub.trim()));

                        } else if (i == 2) { // LINE 3

                            sub = line.substring(0, 22).replace('D', 'e');
                            eph.setCuc(Double.parseDouble(sub.trim()));

                            sub = line.substring(22, 41).replace('D', 'e');
                            eph.setE(Double.parseDouble(sub.trim()));

                            sub = line.substring(41, 60).replace('D', 'e');
                            eph.setCus(Double.parseDouble(sub .trim()));

                            sub = line.substring(60, len).replace('D', 'e');
                            eph.setRootA(Double.parseDouble(sub.trim()));

                        } else if (i == 3) { // LINE 4

                            sub = line.substring(0, 22).replace('D', 'e');
                            eph.setToe(Double.parseDouble(sub.trim()) + 14);

                            sub = line.substring(22, 41).replace('D', 'e');
                            eph.setCic(Double.parseDouble(sub.trim()));

                            sub = line.substring(41, 60).replace('D', 'e');
                            eph.setOmega0(Double.parseDouble(sub.trim()));

                            sub = line.substring(60, len).replace('D', 'e');
                            eph.setCis(Double.parseDouble(sub.trim()));

                        } else if (i == 4) { // LINE 5

                            sub = line.substring(0, 22).replace('D', 'e');
                            eph.setI0(Double.parseDouble(sub.trim()));

                            sub = line.substring(22, 41).replace('D', 'e');
                            eph.setCrc(Double.parseDouble(sub.trim()));

                            sub = line.substring(41, 60).replace('D', 'e');
                            eph.setOmega(Double.parseDouble(sub.trim()));

                            sub = line.substring(60, len).replace('D', 'e');
                            eph.setOmegaDot(Double.parseDouble(sub.trim()));

                        } else if (i == 5) { // LINE 6

                            sub = line.substring(0, 22).replace('D', 'e');
                            eph.setiDot(Double.parseDouble(sub.trim()));

                            sub = line.substring(22, 41).replace('D', 'e');
                            double L2Code = Double.parseDouble(sub.trim());
                            eph.setL2Code((int) L2Code);

                            sub = line.substring(41, 60).replace('D', 'e');
                            double week = Double.parseDouble(sub.trim());
                            eph.setWeek((int) week);

                            sub = line.substring(60, len).replace('D', 'e');
                            double L2Flag = Double.parseDouble(sub.trim());
                            eph.setL2Flag((int) L2Flag);

                        } else if (i == 6) { // LINE 7

                            sub = line.substring(0, 22).replace('D', 'e');
                            double svAccur = Double.parseDouble(sub.trim());
                            eph.setSvAccur((int) svAccur);

                            sub = line.substring(22, 41).replace('D', 'e');
                            double svHealth = Double.parseDouble(sub.trim());
                            eph.setSvHealth((int) svHealth);

                            sub = line.substring(41, 60).replace('D', 'e');
                            eph.setTgd(Double.parseDouble(sub.trim()));

                            sub = line.substring(60, len).replace('D', 'e');
                            double iodc = Double.parseDouble(sub.trim());
                            eph.setIodc((int) iodc);

                        } else if (i == 7) { // LINE 8

                            sub = line.substring(0, 22).replace('D', 'e');
                            eph.setTom(Double.parseDouble(sub.trim()) + 14.0);

                            if (len > 22) {
                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setFitInt(Double.parseDouble(sub.trim()));

                            } else {
                                eph.setFitInt(0);
                            }
                        }
                    }
                    else {
                        i--;
                    }
                } catch (NullPointerException e) {
                    // Skip over blank lines
                }
            }

            // Increment array index
//				j++;
            // Store the number of ephemerides
            //Navigation.n = j;
        }

    } catch (IOException e) {
        e.printStackTrace();
    } catch (NullPointerException e) {
        e.printStackTrace();
    }
    }
    private void parseGLONASSNav(BufferedReader buffStreamNav) {
        try {
            EphemerisGLONASS eph = null;

            while (buffStreamNav.ready()) {

                String sub;
                char satType = 'R';

                eph = new EphemerisGLONASS();
                addEph(eph);
                eph.setSatType(satType);
                for (int i = 0; i < 4; i++) {
                    String line = buffStreamNav.readLine();

                    try {
                        int len = line.length();

                        if (len != 0) {
                            if (i == 0) { // LINE 1

                                //Navigation.eph.get(j).refTime = new Time();

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
                                sub = line.substring(3, 22).replace('D', 'e');
//										System.out.println(sub);
                                eph.setX(Double.parseDouble(sub.trim())*1e3);

										/* Xv: satellite velocity along X at ephemeris reference time [m/s] */
                                sub = line.substring(22, 41).replace('D', 'e');
//										System.out.println(sub);
                                eph.setXv(Double.parseDouble(sub.trim())*1e3);

										/* Xa: acceleration due to lunar-solar gravitational perturbation along X at ephemeris reference time [m/s^2] */
                                sub = line.substring(41, 60).replace('D', 'e');
//										System.out.println(sub);
                                eph.setXa(Double.parseDouble(sub.trim())*1e3);

										/* Bn */
                                sub = line.substring(60, len).replace('D', 'e');
//										System.out.println(sub);
                                eph.setBn(Double.parseDouble(sub.trim()));

                            } else if (i == 2) { // LINE 3

										/* Y: satellite Y coordinate at ephemeris reference time [m] */
                                sub = line.substring(3, 22).replace('D', 'e');
//										System.out.println(sub);
                                eph.setY(Double.parseDouble(sub.trim())*1e3);

										/* Yv: satellite velocity along Y at ephemeris reference time [m/s] */
                                sub = line.substring(22, 41).replace('D', 'e');
//										System.out.println(sub);
                                eph.setYv(Double.parseDouble(sub.trim())*1e3);

										/* Ya: acceleration due to lunar-solar gravitational perturbation along Y at ephemeris reference time [m/s^2] */
                                sub = line.substring(41, 60).replace('D', 'e');
//										System.out.println(sub);
                                eph.setYa(Double.parseDouble(sub.trim())*1e3);

										/* freq_num */
                                sub = line.substring(60, len).replace('D', 'e');
//										System.out.println(sub);
                                eph.setfreq_num((int) Double.parseDouble(sub.trim()));

                            } else if (i == 3) { // LINE 4

										/* Z: satellite Z coordinate at ephemeris reference time [m] */
                                sub = line.substring(3, 22).replace('D', 'e');
//										System.out.println(sub);
                                eph.setZ(Double.parseDouble(sub.trim())*1e3);

										/* Zv: satellite velocity along Z at ephemeris reference time [m/s] */
                                sub = line.substring(22, 41).replace('D', 'e');
//										System.out.println(sub);
                                eph.setZv(Double.parseDouble(sub.trim())*1e3);

										/* Za: acceleration due to lunar-solar gravitational perturbation along Z at ephemeris reference time [m/s^2]  */
                                sub = line.substring(41, 60).replace('D', 'e');
//										System.out.println(sub);
                                eph.setZa(Double.parseDouble(sub.trim())*1e3);

										/* En */
                                sub = line.substring(60, len).replace('D', 'e');
//										System.out.println(sub);
//										eph.setEn(Long.parseLong(sub.trim()));
                                eph.setEn(Double.parseDouble(sub.trim()));

                            } // End of if

                        } else {
                            i--;
                        }
                    } catch (NullPointerException e) {
                        // Skip over blank lines
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void parseGPSNav(BufferedReader buffStreamNav) {
        try {
            EphemerisGPS eph = null;

            while (buffStreamNav.ready()) {

                String sub;
                char satType = 'G';

                eph = new EphemerisGPS();
                addEph(eph);
                eph.setSatType(satType);



                // read 8 lines
                for (int i = 0; i < 8; i++) {

                    String line = buffStreamNav.readLine();
                    try {

                        int len = line.length();

                        if (len != 0) {

                            if (i == 0) { // LINE 1

                                //Navigation.eph.get(j).refTime = new Time();


                                //Navigation.eph.add(eph);
//								addEph(eph);

                                // Get satellite ID
                                sub = line.substring(0, 2).trim();
                                eph.setSatID(Integer.parseInt(sub));

                                // Get and format date and time string
                                String dT = line.substring(2, 22);
                                dT = dT.replace("  ", " 0").trim();
                                dT = "20" + dT;
//								System.out.println(dT);


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

                                sub = line.substring(3, 22).replace('D', 'e');
                                double iode = Double.parseDouble(sub.trim());
                                // TODO check double -> int conversion ?
                                eph.setIode((int) iode);

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setCrs(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setDeltaN(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setM0(Double.parseDouble(sub.trim()));

                            } else if (i == 2) { // LINE 3

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setCuc(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setE(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setCus(Double.parseDouble(sub .trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setRootA(Double.parseDouble(sub.trim()));

                            } else if (i == 3) { // LINE 4

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setToe(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setCic(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setOmega0(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setCis(Double.parseDouble(sub.trim()));

                            } else if (i == 4) { // LINE 5

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setI0(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setCrc(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setOmega(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setOmegaDot(Double.parseDouble(sub.trim()));

                            } else if (i == 5) { // LINE 6

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setiDot(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                double L2Code = Double.parseDouble(sub.trim());
                                eph.setL2Code((int) L2Code);

                                sub = line.substring(41, 60).replace('D', 'e');
                                double week = Double.parseDouble(sub.trim());
                                eph.setWeek((int) week);

                                sub = line.substring(60, len).replace('D', 'e');
                                double L2Flag = Double.parseDouble(sub.trim());
                                eph.setL2Flag((int) L2Flag);

                            } else if (i == 6) { // LINE 7

                                sub = line.substring(0, 22).replace('D', 'e');
                                double svAccur = Double.parseDouble(sub.trim());
                                eph.setSvAccur((int) svAccur);

                                sub = line.substring(22, 41).replace('D', 'e');
                                double svHealth = Double.parseDouble(sub.trim());
                                eph.setSvHealth((int) svHealth);

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setTgd(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                double iodc = Double.parseDouble(sub.trim());
                                eph.setIodc((int) iodc);

                            } else if (i == 7) { // LINE 8

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setTom(Double.parseDouble(sub.trim()));

                                if (len > 22) {
                                    sub = line.substring(22, 41).replace('D', 'e');
                                    eph.setFitInt(Double.parseDouble(sub.trim()));

                                } else {
                                    eph.setFitInt(0);
                                }
                            }
                        }
                        else {
                            i--;
                        }
                    } catch (NullPointerException e) {
                        // Skip over blank lines
                    }
                }

                // Increment array index
//				j++;
                // Store the number of ephemerides
                //Navigation.n = j;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }



}
