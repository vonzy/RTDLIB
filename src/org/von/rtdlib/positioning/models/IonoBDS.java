package org.von.rtdlib.positioning.models;

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

/**
 * Created by diner on 16-10-16.
 */
public class IonoBDS extends IonosphereCorrectionProvider {
    @Override
    public double computeIonosphereCorrection(NavigationProvider navigation, Coordinates coord, double azimuth, double elevation, Time time) {
        return 0;
    }
}
