package org.von.rtdlib.positioning;

import java.util.ArrayList;

/**
 * Created by diner on 16-10-29.
 */
public class PseudoRangeCorrections {

    private Time refTime; /* Reference time of the dataset */

    private ArrayList<PseudoRangeCorrectionSet> prcSets = new ArrayList<PseudoRangeCorrectionSet>(); /* sets of PRCs */

    public PseudoRangeCorrections(Time time){
        this.refTime = time;
    }


    /** 伪距为0或者本身为null的ObservationSet可以被删除掉 **/
    public void cleanPRCs(){
        if(prcSets != null)
            for (int i=prcSets.size()-1;i>=0;i--)
                if(prcSets.get(i)==null || Double.isNaN(prcSets.get(i).getPRC()))
                    prcSets.remove(i);
    }

    public void setPRCSets(int i, PseudoRangeCorrectionSet prcSet) {
        if(prcSets==null) prcSets = new ArrayList<PseudoRangeCorrectionSet>(i+1);
        if(i == prcSets.size()){
            prcSets.add(prcSet);
        }else{
            int c = prcSets.size();
            while(c++ <= i) prcSets.add(null);
            prcSets.set(i,prcSet);
        }
    }
    public void addPRCSets(PseudoRangeCorrectionSet prcSet)
    {
        if (prcSet != null) prcSets.add(prcSet);
    }
    public PseudoRangeCorrectionSet getSatByIndex(int idx){
        return prcSets.get(idx);
    }

    /**
     * @return the refTime
     */
    public Time getRefTime() {
        return refTime;
    }
    public PseudoRangeCorrectionSet getPRCByIDType(Integer satID, char satType){
        if(prcSets == null || satID==null) return null;
        for(int i=0;i<prcSets.size();i++)
            if(prcSets.get(i)!=null && prcSets.get(i).getSatID()==satID.intValue() && prcSets.get(i).getSatType()==satType) return prcSets.get(i);
        return null;
    }
    public int getNumSat(){
        if(prcSets == null) return 0;
        int nsat = 0;
        for(int i=0;i<prcSets.size();i++)
            if(prcSets.get(i)!=null) nsat++;
        return prcSets==null?-1:nsat;
    }
    public Integer getSatID(int idx){
        return getSatByIndex(idx).getSatID();
    }
    public char getSatType(int idx){
        return getSatByIndex(idx).getSatType();
    }


}
