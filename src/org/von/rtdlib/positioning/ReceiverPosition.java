package org.von.rtdlib.positioning;

import org.ejml.simple.SimpleMatrix;
import org.von.rtdlib.provider.IonosphereCorrectionProvider;
import org.von.rtdlib.provider.NavigationProvider;

/**
 * Created by diner on 16-10-16.
 */
public class ReceiverPosition extends Coordinates{

    private int dopType = DOP_TYPE_NONE;
    public final static int DOP_TYPE_NONE = 0;
    public final static int DOP_TYPE_STANDARD = 1; /* Standard DOP values (satellite geometry only) */
    public final static int DOP_TYPE_KALMAN = 2; /* Kalman DOP values (KDOP), based on the Kalman filter error covariance matrix */

    /* Position dilution of precision (PDOP) */
    private double pDop;

    /* Horizontal dilution of precision (HDOP) */
    private double hDop;

    /* Vertical dilution of precision (VDOP) */
    private double vDop;

    private double receiverClockError;

    private SimpleMatrix positionCovariance; /* Covariance matrix of the position estimation error */

    private double computeIonosphereCorrection(IonosphereCorrectionProvider ionoModel, NavigationProvider navigation ,Coordinates coord, double azimuth, double elevation, Time time) {
         return ionoModel.computeIonosphereCorrection(navigation,coord,azimuth,elevation,time);
    }

    public ReceiverPosition(Coordinates c, int dopType, double pDop, double hDop, double vDop) {
        super();
        c.cloneInto(this);
        this.dopType = dopType;
        this.pDop = pDop;
        this.hDop = hDop;
        this.vDop = vDop;
    }

    /**
     * @return the receiver clock error
     */
    public double getReceiverClockError() {
        return receiverClockError;
    }

    /**
     * @param receiverClockError the receiver clock error to set
     */
    public void setReceiverClockError(double receiverClockError) {
        this.receiverClockError = receiverClockError;
    }

    public ReceiverPosition()
    {
        super();
        this.setXYZ(0.0, 0.0, 0.0);
        this.receiverClockError = 0.0;
    }

    /**
     * @return the pDop
     */
    public double getpDop() {
        return pDop;
    }

    /**
     * @param pDop the pDop to set
     */
    public void setpDop(double pDop) {
        this.pDop = pDop;
    }

    /**
     * @return the hDop
     */
    public double gethDop() {
        return hDop;
    }

    /**
     * @param hDop the hDop to set
     */
    public void sethDop(double hDop) {
        this.hDop = hDop;
    }

    /**
     * @return the vDop
     */
    public double getvDop() {
        return vDop;
    }

    /**
     * @param vDop the vDop to set
     */
    public void setvDop(double vDop) {
        this.vDop = vDop;
    }

    /**
     * @return the dopType
     */
    public int getDopType() {
        return dopType;
    }

    /**
     * @param dopType the dopType to set
     */
    public void setDopType(int dopType) {
        this.dopType = dopType;
    }

    public SimpleMatrix getPositionCovariance() {
        return positionCovariance;
    }

    public void setPositionCovariance(SimpleMatrix positionCovariance) {
        this.positionCovariance = positionCovariance;
    }
}
