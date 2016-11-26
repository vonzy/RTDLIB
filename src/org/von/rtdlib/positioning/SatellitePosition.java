package org.von.rtdlib.positioning;

/**
 * Created by diner on 16-10-16.
 */
public class SatellitePosition extends Coordinates{
    private int satID; /* Satellite ID number */
    private char satType;
    private long unixTime;
    private double satelliteClockError; /* Correction due to satellite clock error in seconds*/

    public SatellitePosition(long unixTime, int satID, char satType, double x, double y, double z) {
        super();

        this.unixTime = unixTime;
        this.satID = satID;
        this.satType = satType;

        this.setXYZ(x, y, z);
    }

    /**
     * @return the timeCorrection
     */
    public double getSatelliteClockError() {
        return satelliteClockError;
    }

    /**
     * @param timeCorrection the timeCorrection to set
     */
    public void setSatelliteClockError(double timeCorrection) {
        this.satelliteClockError = timeCorrection;
    }


}
