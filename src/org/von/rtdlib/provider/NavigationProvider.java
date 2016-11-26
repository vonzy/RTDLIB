package org.von.rtdlib.provider;

import org.von.rtdlib.StreamResource;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;

/**
 * Created by diner on 16-10-16.
 */
public interface NavigationProvider extends StreamResource{

    public EphemerisProvider getEphemeris(long unixTime,int satID, char satType);

    public IonosphereCorrectionProvider getIono(long unixTime);

    public static final int RTDLIB_SYSTEMTYPE_BDS = 0;
    public static final int RTDLIB_SYSTEMTYPE_GPS = 1;
    public static final int RTDLIB_SYSTEMTYPE_GLONASS = 2;

}
