package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;
import org.von.rtdlib.positioning.models.IonoBDS;
import org.von.rtdlib.positioning.models.IonoGLONASS;
import org.von.rtdlib.positioning.models.IonoGPS;
import org.von.rtdlib.provider.EphemerisProvider;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by diner on 16-10-20.
 */
public class RinexNavigationParser implements NavigationProvider{


    protected File fileNav;
    protected FileInputStream streamNav;
    protected InputStreamReader inStreamNav;
    protected BufferedReader buffStreamNav;

    protected IonoBDS ionoBDS = null;
    protected IonoGPS ionoGPS = null;
    protected IonoGLONASS ionoGLONASS = null;

    protected ArrayList<EphemerisProvider> ephemerisProviders = new ArrayList<EphemerisProvider>(); /* GPS broadcast ephemerides */
    protected IonosphereCorrectionProvider iono = null; /* Ionosphere model parameters */

    public RinexNavigationParser(File fileNav)
    {
        this.fileNav = fileNav;
    }

    public RinexNavigationParser() {


    }

    //选择时间范围与函数输入时间最接近的星历参数提供者(EphemerisProvider)
    @Override
    public EphemerisProvider getEphemeris(long unixTime,int satID, char satType) {
        long dt = 0;
        long dtMin = 0;
        EphemerisProvider refEph = null;

        //long gpsTime = (new Time(unixTime)).getGpsTime();

        for (int i = 0; i < ephemerisProviders.size(); i++) {
            // Find ephemeris sets for given satellite
            if (ephemerisProviders.get(i).getSatID() == satID && ephemerisProviders.get(i).getSatType() == satType) {
                // Compare current time and ephemeris reference time
                dt = Math.abs(ephemerisProviders.get(i).getRefTime().getMsec() - unixTime /*getGpsTime() - gpsTime*/);
                // If it's the first round, set the minimum time difference and
                // select the first ephemeris set candidate
                if (refEph == null) {
                    dtMin = dt;
                    refEph = ephemerisProviders.get(i);
                    // Check if the current ephemeris set is closer in time than
                    // the previous candidate; if yes, select new candidate
                } else if (dt < dtMin) {
                    dtMin = dt;
                    refEph = ephemerisProviders.get(i);
                }
            }
        }

//check satellite health
//		temporary comment out by Yoshida, since NVS does not include health value

        if (refEph != null && refEph.getSvHealth() != 0) {
            refEph = null;
        }

        return refEph;
    }

    @Override
    public IonosphereCorrectionProvider getIono(long unixTime) {
        return iono;
    }

    @Override
    public void init() throws Exception {
        open();
        parseHeader();
        parseData();

    }

    @Override
    public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
        close();
    }

    protected void parseHeader() {

    }
    protected void parseData() {

    }
    public void open() {
        try {

            if(fileNav!=null) streamNav = new FileInputStream(fileNav);
            if(streamNav!=null) inStreamNav = new InputStreamReader(streamNav);
            if(inStreamNav!=null) buffStreamNav = new BufferedReader(inStreamNav);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }
    public void close() {
        try {

            if(buffStreamNav!=null) buffStreamNav.close();
            if(inStreamNav!=null) inStreamNav.close();
            if(streamNav!=null) streamNav.close();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    public void addEph(EphemerisProvider eph){
        this.ephemerisProviders.add(eph);
    }


}
