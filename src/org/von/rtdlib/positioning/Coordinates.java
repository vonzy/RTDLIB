package org.von.rtdlib.positioning;

import org.ejml.simple.SimpleMatrix;
import org.von.rtdlib.Streamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by diner on 16-10-16.
 */
public class Coordinates implements Streamable{
    private final static int STREAM_V = 1;

    // Global systems
    private SimpleMatrix ecef = null; /* Earth-Centered, Earth-Fixed (X, Y, Z) */
    private SimpleMatrix geod = null; /* Longitude (lam), latitude (phi), height (h) */

    // Local systems (require to specify an origin)
    private SimpleMatrix enu; /* Local coordinates (East, North, Up) */

    private Time refTime = null;

    protected Coordinates(){
        ecef = new SimpleMatrix(3, 1);
        geod = new SimpleMatrix(3, 1);
        enu = new SimpleMatrix(3, 1);
    }
    public static Coordinates readFromStream(DataInputStream dai, boolean oldVersion) throws IOException {
        Coordinates c = new Coordinates();
        c.read(dai, oldVersion);
        return c;
    }

    public static Coordinates globalXYZInstance(double x, double y, double z){
        Coordinates c = new Coordinates();
        //c.ecef = new SimpleMatrix(3, 1);
        c.setXYZ(x, y, z);
        return c;
    }
    //	public static Coordinates globalXYZInstance(SimpleMatrix ecef){
//		Coordinates c = new Coordinates();
//		c.ecef = ecef.copy();
//		return c;
//	}
    public static Coordinates globalENUInstance(SimpleMatrix ecef){
        Coordinates c = new Coordinates();
        c.enu = ecef.copy();
        return c;
    }

    public static Coordinates globalGeodInstance( double lat, double lon, double alt ){
        Coordinates c = new Coordinates();
        //c.ecef = new SimpleMatrix(3, 1);
        c.setGeod( lat, lon, alt);
        c.computeECEF();

        if( !c.isValidXYZ() )
            throw new RuntimeException("Invalid ECEF: " + c);
        return c;
    }

    public SimpleMatrix minusXYZ(Coordinates coord){
        return this.ecef.minus(coord.ecef);
    }
    /**
     *
     */
    public void computeGeodetic() {
        double X = this.ecef.get(0);
        double Y = this.ecef.get(1);
        double Z = this.ecef.get(2);

        //this.geod = new SimpleMatrix(3, 1);

        double a = Constants.WGS84_SEMI_MAJOR_AXIS;
        double e = Constants.WGS84_ECCENTRICITY;

        // Radius computation
        double r = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2) + Math.pow(Z, 2));

        // Geocentric longitude
        double lamGeoc = Math.atan2(Y, X);

        // Geocentric latitude
        double phiGeoc = Math.atan(Z / Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2)));

        // Computation of geodetic coordinates
        double psi = Math.atan(Math.tan(phiGeoc) / Math.sqrt(1 - Math.pow(e, 2)));
        double phiGeod = Math.atan((r * Math.sin(phiGeoc) + Math.pow(e, 2) * a
                / Math.sqrt(1 - Math.pow(e, 2)) * Math.pow(Math.sin(psi), 3))
                / (r * Math.cos(phiGeoc) - Math.pow(e, 2) * a * Math.pow(Math.cos(psi), 3)));
        double lamGeod = lamGeoc;
        double N = a / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(phiGeod), 2));
        double h = r * Math.cos(phiGeoc) / Math.cos(phiGeod) - N;

        this.geod.set(0, 0, Math.toDegrees(lamGeod));
        this.geod.set(1, 0, Math.toDegrees(phiGeod));
        this.geod.set(2, 0, h);
    }

    /*
     function [X,Y,Z] = frgeod( a, finv, dphi, dlambda, h )
         %FRGEOD  Subroutine to calculate Cartesian coordinates X,Y,Z
         %       given geodetic coordinates latitude, longitude (east),
         %       and height above reference ellipsoid along with
         %       reference ellipsoid values semi-major axis (a) and
         %       the inverse of flattening (finv)

         % The units of linear parameters h,a must agree (m,km,mi,..etc).
         % The input units of angular quantities must be in decimal degrees.
         % The output units of X,Y,Z will be the same as the units of h and a.
         % Copyright (C) 1987 C. Goad, Columbus, Ohio
         % Reprinted with permission of author, 1996
         % Original Fortran code rewritten into MATLAB
         % Kai Borre 03-03-96
     */
    public void computeECEF() {
        final long a = 6378137;
        final double finv = 298.257223563d;

        double dphi = this.geod.get(1);
        double dlambda = this.geod.get(0);
        double h = this.geod.get(2);

        // compute degree-to-radian factor
        double dtr = Math.PI/180;

        // compute square of eccentricity
        double esq = (2-1/finv)/finv;
        double sinphi = Math.sin(dphi*dtr);
        // compute radius of curvature in prime vertical
        double N_phi = a/Math.sqrt(1-esq*sinphi*sinphi);

        // compute P and Z
        // P is distance from Z axis
        double P = (N_phi + h)*Math.cos(dphi*dtr);
        double Z = (N_phi*(1-esq) + h) * sinphi;
        double X = P*Math.cos(dlambda*dtr);
        double Y = P*Math.sin(dlambda*dtr);

        this.ecef.set(0, 0, X );
        this.ecef.set(1, 0, Y );
        this.ecef.set(2, 0, Z );
    }
    public SimpleMatrix getECEF()
    {
        return this.ecef;
    }

    /**
     * @param origin
     * @return Local (ENU) coordinates
     */
    public void computeLocal(Coordinates target) {
        if(this.geod==null) computeGeodetic();

        SimpleMatrix R = rotationMatrix(this);

        enu = R.mult(target.minusXYZ(this));

    }

    /** BLH  **/
    public double getGeodeticLongitude(){
        if(this.geod==null) computeGeodetic();
        return this.geod.get(0);
    }
    public double getGeodeticLatitude(){
        if(this.geod==null) computeGeodetic();
        return this.geod.get(1);
    }
    public double getGeodeticHeight(){
        if(this.geod==null) computeGeodetic();
        return this.geod.get(2);
    }

    /** XYZ  **/
    public double getX(){
        return ecef.get(0);
    }
    public double getY(){
        return ecef.get(1);
    }
    public double getZ(){
        return ecef.get(2);
    }

    /** ENU  **/
    public void setENU(double e, double n, double u){
        this.enu.set(0, 0, e);
        this.enu.set(1, 0, n);
        this.enu.set(2, 0, u);
    }
    public double getE(){
        return enu.get(0);
    }
    public double getN(){
        return enu.get(1);
    }
    public double getU(){
        return enu.get(2);
    }


    public void setXYZ(double x, double y, double z){
        //if(this.ecef==null) this.ecef = new SimpleMatrix(3, 1);
        this.ecef.set(0, 0, x);
        this.ecef.set(1, 0, y);
        this.ecef.set(2, 0, z);
    }
    public void setGeod( double lat, double lon, double alt ){
        //if(this.ecef==null) this.ecef = new SimpleMatrix(3, 1);
        this.geod.set(1, 0, lat);
        this.geod.set(0, 0, lon);
        this.geod.set(2, 0, alt);
    }
    public void setPlusXYZ(SimpleMatrix sm){
        this.ecef.set(ecef.plus(sm));
    }
    public void setSMMultXYZ(SimpleMatrix sm){
        this.ecef = sm.mult(this.ecef);
    }

    public boolean isValidXYZ(){
        return (this.ecef != null && this.ecef.elementSum() != 0 && !Double.isNaN(this.ecef.get(0)) && !Double.isNaN(this.ecef.get(1)) && !Double.isNaN(this.ecef.get(2)));
    }

    public Object clone(){
        Coordinates c = new Coordinates();
        cloneInto(c);
        return c;
    }

    public void cloneInto(Coordinates c){
        c.ecef = this.ecef.copy();
        c.enu = this.enu.copy();
        c.geod = this.geod.copy();

        if(refTime!=null) c.refTime = (Time)refTime.clone();
    }
    /**
     * @param origin
     * @return Rotation matrix used to switch from global to local reference systems (and vice-versa)
     */
    public static SimpleMatrix rotationMatrix(Coordinates origin) {

        double lam = Math.toRadians(origin.getGeodeticLongitude());
        double phi = Math.toRadians(origin.getGeodeticLatitude());

        double cosLam = Math.cos(lam);
        double cosPhi = Math.cos(phi);
        double sinLam = Math.sin(lam);
        double sinPhi = Math.sin(phi);

        double[][] data = new double[3][3];
        data[0][0] = -sinLam;
        data[0][1] = cosLam;
        data[0][2] = 0;
        data[1][0] = -sinPhi * cosLam;
        data[1][1] = -sinPhi * sinLam;
        data[1][2] = cosPhi;
        data[2][0] = cosPhi * cosLam;
        data[2][1] = cosPhi * sinLam;
        data[2][2] = sinPhi;

        SimpleMatrix R = new SimpleMatrix(data);

        return R;
    }

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

    public int write(DataOutputStream dos) throws IOException{
        int size=0;
        dos.writeUTF(MESSAGE_COORDINATES); size+=5;// 5
        dos.writeInt(STREAM_V); size+=4; // 4

        dos.writeLong(refTime==null?-1:refTime.getMsec()); size+=8; // 8

        for(int i=0;i<3;i++){
            dos.writeDouble(ecef.get(i));  size+=8;
        }
        for(int i=0;i<3;i++){
            dos.writeDouble(enu.get(i));  size+=8;
        }
        for(int i=0;i<3;i++){
            dos.writeDouble(geod.get(i));  size+=8;
        }

        return size;
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.Streamable#read(java.io.DataInputStream)
     */
    @Override
    public void read(DataInputStream dai, boolean oldVersion) throws IOException {
        int v = dai.readInt();

        if(v == 1){
            long l = dai.readLong();
            refTime = l==-1?null:new Time(l);
            for(int i=0;i<3;i++){
                ecef.set(i, dai.readDouble());
            }
            for(int i=0;i<3;i++){
                enu.set(i, dai.readDouble());
            }
            for(int i=0;i<3;i++){
                geod.set(i, dai.readDouble());
            }
        }else{
            throw new IOException("Unknown format version:"+v);
        }



    }

    public String toString(){
        String lineBreak = System.getProperty("line.separator");

        String out= "Coord ECEF: X:"+getX()+" Y:"+getY()+" Z:"+getZ()+lineBreak;
        out +=      "       ENU: E:"+getE()+" N:"+getN()+" U:"+getU()+lineBreak;
        out +=      "      GEOD: Lon:"+getGeodeticLongitude()+" Lat:"+getGeodeticLatitude()+" H:"+getGeodeticHeight()+lineBreak;

        return out;
    }
}
