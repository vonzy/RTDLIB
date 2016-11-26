package org.von.rtdlib.provider;

import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.Time;

/**
 * Created by diner on 16-10-16.
 */
public abstract class IonosphereCorrectionProvider {

    /** Bitmask, every bit represenst a GPS SV (1-32). If the bit is set the SV is healthy. */
    protected long health = 0;

    /** UTC - parameter A1. */
    protected double utcA1;

    /** UTC - parameter A0. */
    protected double utcA0;

    /** UTC - reference time of week. */
    protected long utcTOW;

    /** UTC - reference week number. */
    protected int utcWNT;

    /** UTC - time difference due to leap seconds before event. */
    protected int utcLS;

    /** UTC - week number when next leap second event occurs. */
    protected int utcWNF;

    /** UTC - day of week when next leap second event occurs. */
    protected int utcDN;

    /** UTC - time difference due to leap seconds after event. */
    protected int utcLSF;

    /** Klobuchar - alpha. */
    protected float alpha[] = new float[4];

    /** Klobuchar - beta. */
    protected float beta[] = new float[4];

    /** Healthmask field in this message is valid. */
    protected boolean validHealth;

    /** UTC parameter fields in this message are valid. */
    protected boolean validUTC;

    /** Klobuchar parameter fields in this message are valid. */
    protected boolean validKlobuchar;

    /** Reference time. */
    private Time refTime;

    public IonosphereCorrectionProvider()
    {

    }
    /**
     * Instantiates a new iono gps.
     */
    public IonosphereCorrectionProvider(Time refTime) {
        this.refTime = refTime;
    }
    /**
     * Gets the reference time.
     *
     * @return the refTime
     */
    public Time getRefTime() {
        return refTime;
    }

    /**
     * Sets the reference time.
     *
     * @param refTime the refTime to set
     */
    public void setRefTime(Time refTime) {
        this.refTime = refTime;
    }

    public abstract double computeIonosphereCorrection(NavigationProvider navigation,
                                Coordinates coord, double azimuth, double elevation, Time time);

    /**
     * Gets the bitmask, every bit represenst a GPS SV (1-32).
     *
     * @return the health
     */
    public long getHealth() {
        return health;
    }

    /**
     * Sets the bitmask, every bit represenst a GPS SV (1-32).
     *
     * @param health the health to set
     */
    public void setHealth(long health) {
        this.health = health;
    }

    /**
     * Gets the UTC - parameter A1.
     *
     * @return the utcA1
     */
    public double getUtcA1() {
        return utcA1;
    }

    /**
     * Sets the UTC - parameter A1.
     *
     * @param utcA1 the utcA1 to set
     */
    public void setUtcA1(double utcA1) {
        this.utcA1 = utcA1;
    }

    /**
     * Gets the UTC - parameter A0.
     *
     * @return the utcA0
     */
    public double getUtcA0() {
        return utcA0;
    }

    /**
     * Sets the UTC - parameter A0.
     *
     * @param utcA0 the utcA0 to set
     */
    public void setUtcA0(double utcA0) {
        this.utcA0 = utcA0;
    }

    /**
     * Gets the UTC - reference time of week.
     *
     * @return the utcTOW
     */
    public long getUtcTOW() {
        return utcTOW;
    }

    /**
     * Sets the UTC - reference time of week.
     *
     * @param utcTOW the utcTOW to set
     */
    public void setUtcTOW(long utcTOW) {
        this.utcTOW = utcTOW;
    }

    /**
     * Gets the UTC - reference week number.
     *
     * @return the utcWNT
     */
    public int getUtcWNT() {
        return utcWNT;
    }

    /**
     * Sets the UTC - reference week number.
     *
     * @param utcWNT the utcWNT to set
     */
    public void setUtcWNT(int utcWNT) {
        this.utcWNT = utcWNT;
    }

    /**
     * Gets the UTC - time difference due to leap seconds before event.
     *
     * @return the utcLS
     */
    public int getUtcLS() {
        return utcLS;
    }

    /**
     * Sets the UTC - time difference due to leap seconds before event.
     *
     * @param utcLS the utcLS to set
     */
    public void setUtcLS(int utcLS) {
        this.utcLS = utcLS;
    }

    /**
     * Gets the UTC - week number when next leap second event occurs.
     *
     * @return the utcWNF
     */
    public int getUtcWNF() {
        return utcWNF;
    }

    /**
     * Sets the UTC - week number when next leap second event occurs.
     *
     * @param utcWNF the utcWNF to set
     */
    public void setUtcWNF(int utcWNF) {
        this.utcWNF = utcWNF;
    }

    /**
     * Gets the UTC - day of week when next leap second event occurs.
     *
     * @return the utcDN
     */
    public int getUtcDN() {
        return utcDN;
    }

    /**
     * Sets the UTC - day of week when next leap second event occurs.
     *
     * @param utcDN the utcDN to set
     */
    public void setUtcDN(int utcDN) {
        this.utcDN = utcDN;
    }

    /**
     * Gets the UTC - time difference due to leap seconds after event.
     *
     * @return the utcLSF
     */
    public int getUtcLSF() {
        return utcLSF;
    }

    /**
     * Sets the UTC - time difference due to leap seconds after event.
     *
     * @param utcLSF the utcLSF to set
     */
    public void setUtcLSF(int utcLSF) {
        this.utcLSF = utcLSF;
    }

    /**
     * Gets the klobuchar - alpha.
     *
     * @param i the i<sup>th<sup> value in the range 0-3
     * @return the alpha
     */
    public float getAlpha(int i) {
        return alpha[i];
    }

    /**
     * Sets the klobuchar - alpha.
     *
     * @param alpha the alpha to set
     */
    public void setAlpha(float[] alpha) {
        this.alpha = alpha;
    }

    /**
     * Gets the klobuchar - beta.
     *
     * @param i the i<sup>th<sup> value in the range 0-3
     * @return the beta
     */
    public float getBeta(int i) {
        return beta[i];
    }

    /**
     * Sets the klobuchar - beta.
     *
     * @param beta the beta to set
     */
    public void setBeta(float[] beta) {
        this.beta = beta;
    }

    /**
     * Checks if is healthmask field in this message is valid.
     *
     * @return the validHealth
     */
    public boolean isValidHealth() {
        return validHealth;
    }

    /**
     * Sets the healthmask field in this message is valid.
     *
     * @param validHealth the validHealth to set
     */
    public void setValidHealth(boolean validHealth) {
        this.validHealth = validHealth;
    }

    /**
     * Checks if is UTC parameter fields in this message are valid.
     *
     * @return the validUTC
     */
    public boolean isValidUTC() {
        return validUTC;
    }

    /**
     * Sets the UTC parameter fields in this message are valid.
     *
     * @param validUTC the validUTC to set
     */
    public void setValidUTC(boolean validUTC) {
        this.validUTC = validUTC;
    }

    /**
     * Checks if is klobuchar parameter fields in this message are valid.
     *
     * @return the validKlobuchar
     */
    public boolean isValidKlobuchar() {
        return validKlobuchar;
    }

    /**
     * Sets the klobuchar parameter fields in this message are valid.
     *
     * @param validKlobuchar the validKlobuchar to set
     */
    public void setValidKlobuchar(boolean validKlobuchar) {
        this.validKlobuchar = validKlobuchar;
    }
	/* (non-Javadoc)
	 * @see org.gogpsproject.Streamable#write(java.io.DataOutputStream)
	 */
}
