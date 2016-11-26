package org.von.rtdlib.parser.rinex;

import org.von.rtdlib.positioning.PseudoRangeCorrectionSet;
import org.von.rtdlib.positioning.PseudoRangeCorrections;
import org.von.rtdlib.positioning.Time;
import org.von.rtdlib.provider.PseudoRangeCorrectionProvider;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by diner on 16-10-28.
 */
public class RinexPseudoRangeCorrectionParser implements PseudoRangeCorrectionProvider {

    protected File filePrc;
    protected FileInputStream streamPrc;
    protected InputStreamReader inStreamPrc;
    protected BufferedReader buffStreamPrc;
    private List<PseudoRangeCorrections> ListOfPRCs = new ArrayList<PseudoRangeCorrections>();
    private int nSat;
    protected int nGps;
    protected int nGlo;
    protected int nQzs;
    protected int nSbs;
    protected int nBds;

    public RinexPseudoRangeCorrectionParser(File filePrc) {
        this.filePrc = filePrc;
    }

    @Override
    public PseudoRangeCorrectionSet getPRC(long unixTime, int satID, char satType) {

        long dt = 0;
        long dtMin = 0;
        PseudoRangeCorrectionSet prc = null;

        PseudoRangeCorrections prcs = null;

        //long gpsTime = (new Time(unixTime)).getGpsTime();

        for (int i = 0; i < ListOfPRCs.size(); i++) {

            dt = Math.abs(ListOfPRCs.get(i).getRefTime().getMsec() - unixTime);

            if(prcs == null)
            {
                dtMin = dt;
                prcs = ListOfPRCs.get(i);
            }
            else if(dt < dtMin)
            {
                dtMin = dt;
                prcs = ListOfPRCs.get(i);
            }
        }
        return prcs.getPRCByIDType(satID,satType);
            // Find ephemeris sets for given satellite

    }

    private void parseData() {

        try {
            while (buffStreamPrc.ready()) {
                String line = buffStreamPrc.readLine();
                int len = line.length();

                // Parse date and time
                String dateStr = line.substring(2, 25);

                // Parse available satellites string
                String satAvail = line.substring(30, len);

                // Parse number of available satellites
                String numOfSat = satAvail.substring(0, 2).trim();
                nSat = Integer.parseInt(numOfSat);

                nGps = 0;
                nGlo = 0;
                nSbs = 0;
                nQzs = 0;
                nBds = 0;

                PseudoRangeCorrections prcSets = new PseudoRangeCorrections(new Time(dateStr));

                for (int i = 0 ; i < nSat; i++)
                {
                    line = buffStreamPrc.readLine();

                    int satNum = Integer.parseInt(line.substring(0, 3).trim());

                    String satInfo = num2SatInfo(satNum);

                    PseudoRangeCorrectionSet prcSet = new PseudoRangeCorrectionSet();

                    prcSet.setRefTime(new Time(dateStr));

                    prcSet.setSatType(satInfo.charAt(0));

                    prcSet.setSatID(Integer.parseInt(satInfo.substring(1, satInfo.length())));

                    prcSet.setPRC(Double.parseDouble(line.substring(4,4+14)));

                    prcSet.setPRCRate(0);

                    prcSets.addPRCSets(prcSet); //add one satellite data

                    prcSets.cleanPRCs();
                }
                ListOfPRCs.add(prcSets);



            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }

    private String num2SatInfo(int num)
    {
        int satID=0; char satType=0;
        if (num >=1 && num <= 32)
        {
            satType = 'G';
            satID = num;
        }
        else if(num >= 65 && num <= 96)
        {
            satType = 'R';
            satID = num - 65 + 1;
        }
        else if(num >= 201 && num <= 235)
        {
            satType = 'C';
            satID = num - 201 + 1;
        }
        return Character.toString(satType) +Integer.toString(satID);

    }

    private boolean hasMorePrcs() throws IOException {
        return buffStreamPrc.ready();

    }

    @Override
    public void init() throws Exception {
        open();
        parseData();
    }


    protected void open() throws FileNotFoundException {
        streamPrc = new FileInputStream(filePrc);
        inStreamPrc = new InputStreamReader(streamPrc);
        buffStreamPrc = new BufferedReader(inStreamPrc);
    }
    
    

    @Override
    public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
        try {
            streamPrc.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            inStreamPrc.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            buffStreamPrc.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

}
