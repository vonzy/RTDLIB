package org.von.rtdlib.positioning;

/**
 * Created by diner on 16-10-28.
 * pseudo range correction
 */
public class PseudoRangeCorrectionSet {
    private double PRC;
    private double PRCRate;
    private Time refTime;
    private int satID;	/* Satellite number */
    private char satType;	/* Satellite Type */

    public double getPRC() {
        return PRC;
    }

    public void setPRC(double PRC){ this.PRC = PRC; }

    public int getSatID() {
        return satID;
    }

    public char getSatType() {
        return satType;
    }

    public Time getRefTime() {
        return refTime;
    }

    public void setRefTime(Time refTime) {
        this.refTime = refTime;
    }

    public double getPRCRate() {
        return PRCRate;
    }

    public void setPRCRate(double PRCRate) {
        this.PRCRate = PRCRate;
    }

    public void setSatID(int satID) {
        this.satID = satID;
    }

    public void setSatType(char satType) {
        this.satType = satType;
    }
}
