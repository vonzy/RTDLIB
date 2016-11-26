package org.von.rtdlib.positioning;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by diner on 16-10-16.
 * All satellite Observations in One Epoch.
 */

public class Observations {

    SimpleDateFormat sdfHeader = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
    DecimalFormat dfX4 = new DecimalFormat("0.0000");


    private final static int STREAM_V = 1;

    private Time refTime; /* Reference time of the dataset */
    private int eventFlag; /* Event flag */

    private ArrayList<ObservationSet> obsSets; /* sets of observations */
    private int issueOfData = -1;


    public Observations(Time time, int flag){
        this.refTime = time;
        this.eventFlag = flag;
    }


    /** 伪距为0或者本身为null的ObservationSet可以被删除掉 **/
    public void cleanObservations(){
        if(obsSets != null)
            for (int i=obsSets.size()-1;i>=0;i--)
                if(obsSets.get(i)==null || Double.isNaN(obsSets.get(i).getPseudorange(0)))
                    obsSets.remove(i);
    }

    public void setObsSets(int i, ObservationSet os) {
        if(obsSets==null) obsSets = new ArrayList<ObservationSet>(i+1);
        if(i == obsSets.size()){
            obsSets.add(os);
        }else{
            int c = obsSets.size();
            while(c++ <= i) obsSets.add(null);
            obsSets.set(i,os);
        }
    }
    public ObservationSet getSatByIndex(int idx){
        return obsSets.get(idx);
    }

    /**
     * @return the refTime
     */
    public Time getRefTime() {
        return refTime;
    }
    public ObservationSet getSatByIDType(Integer satID, char satType){
        if(obsSets == null || satID==null) return null;
        for(int i=0;i<obsSets.size();i++)
            if(obsSets.get(i)!=null && obsSets.get(i).getSatID()==satID.intValue() && obsSets.get(i).getSatType()==satType) return obsSets.get(i);
        return null;
    }
    public int getNumSat(){
        if(obsSets == null) return 0;
        int nsat = 0;
        for(int i=0;i<obsSets.size();i++)
            if(obsSets.get(i)!=null) nsat++;
        return obsSets==null?-1:nsat;
    }
    public Integer getSatID(int idx){
        return getSatByIndex(idx).getSatID();
    }
    public char getSatType(int idx){
        return getSatByIndex(idx).getSatType();
    }

}
