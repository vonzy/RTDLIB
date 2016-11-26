package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.models.IonoBDS;
import org.von.rtdlib.positioning.models.IonoGLONASS;
import org.von.rtdlib.positioning.models.IonoGPS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by diner on 16-10-26.
 */
public class RinexNavigationParserV212 extends RinexNavigationParserV2{


    public RinexNavigationParserV212(File[] fileNavs) {
        super(fileNavs);
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

    private void parseOneHeader(BufferedReader buffStreamNav, int SystemType) {
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
                if (typeField.equals("GPSA")) {

                    float a[] = new float[4];
                    sub = line.substring(6, 17).replace('D', 'e');
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

                    iono.setAlpha(a);

                } else if (typeField.equals("GPSB")) {

                    float b[] = new float[4];

                    sub = line.substring(6, 17).replace('D', 'e');
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

                    iono.setBeta(b);

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


}
