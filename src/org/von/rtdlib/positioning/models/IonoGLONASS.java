package org.von.rtdlib.positioning.models;

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

/**
 * Created by diner on 16-10-16.
 * GLONASS导航电文中不含有电离层改正模型参数
 */
public class IonoGLONASS extends IonosphereCorrectionProvider {
    @Override
    public double computeIonosphereCorrection(NavigationProvider navigation, Coordinates coord, double azimuth, double elevation, Time time) {
        return 0;
    }
}
