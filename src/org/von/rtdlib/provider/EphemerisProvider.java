package org.von.rtdlib.provider;

import org.von.rtdlib.positioning.Constants;
import org.von.rtdlib.positioning.Observations;
import org.von.rtdlib.positioning.SatellitePosition;
import org.von.rtdlib.positioning.Time;

/**
 * Created by diner on 16-10-21.
 * 星历提供者 提供卫星位置计算
 * 由于接口中只能定义常量属性，而该对象需要有卫星ID号和卫星类型等变量，所以使用abstract class
 */
public abstract class EphemerisProvider {
    protected Time refTime; /* Reference time of the dataset */
    protected char satType; /* Satellite Type */
    protected int satID; /* Satellite ID number */
    private int svHealth; /* SV health */


    /**计算卫星在发射时刻，发射时刻下地固坐标系下的位置（不加地球自转效应改正）*/
    public abstract SatellitePosition computeSatPosition(Observations obs);
    public abstract double computeSatelliteClockError(long unixTime,  double obsPseudorange);


    /**
     * @return the refTime
     */
    public Time getRefTime() {
        return refTime;
    }
    /**
     * @param refTime the refTime to set
     */
    public void setRefTime(Time refTime) {
        this.refTime = refTime;
    }
    /**
     * @return the satType
     */
    public char getSatType() {
        return satType;
    }
    /**
     * @param satType the satType to set
     */
    public void setSatType(char satType) {
        this.satType = satType;
    }
    /**
     * @return the satID
     */
    public int getSatID() {
        return satID;
    }
    /**
     * @param satID the satID to set
     */
    public void setSatID(int satID) {
        this.satID = satID;
    }

    public int getSvHealth() {
        return svHealth;
    }
    /**
     * @param svHealth the svHealth to set
     */
    public void setSvHealth(int svHealth) {
        this.svHealth = svHealth;
    }

    protected double checkGpsTime(double time) {

        // Account for beginning or end of week crossover
        if (time > Constants.SEC_IN_HALF_WEEK) {
            time = time - 2 * Constants.SEC_IN_HALF_WEEK;
        } else if (time < -Constants.SEC_IN_HALF_WEEK) {
            time = time + 2 * Constants.SEC_IN_HALF_WEEK;
        }
        return time;
    }

    protected double computeClockCorrectedTransmissionTime(long unixTime, double satelliteClockError, double obsPseudorange) {

        double gpsTime = (new Time(unixTime)).getGpsTime();

        // Remove signal travel time from observation time
        double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

        return tRaw - satelliteClockError;
    }

}
