package org.von.rtdlib.test;

import org.von.rtdlib.producer.TxtProducer;
import org.von.rtdlib.parser.ParserFactory;
import org.von.rtdlib.Rtdlib;
import org.von.rtdlib.provider.NavigationProvider;
import org.von.rtdlib.provider.ObservationsProvider;
import org.von.rtdlib.provider.PseudoRangeCorrectionProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by diner on 16-10-16.
 */
public class TestRTD {

    public static void main(String[] args) {
        //force dot as decimal separator
        Locale.setDefault(new Locale("en", "US"));

        int dynamicModel = Rtdlib.DYN_MODEL_CONST_SPEED;
//		int dynamicModel = Rtdlib.DYN_MODEL_STATIC;
        double goodDopThreshold = 2.5;
        int timeSampleDelaySec = 1;

        boolean gpsEnable = true;  // enable GPS data reading
        boolean qzsEnable = true;  // enable QZSS data reading
        boolean gloEnable = true;  // enable GLONASS data reading
        boolean galEnable = true;  // enable Galileo data reading
        boolean bdsEnable = true;  // enable BeiDou data reading

        Boolean[] multiConstellation = {gpsEnable, qzsEnable, gloEnable, galEnable, bdsEnable};

        try {
            // Get current time
            long start = System.currentTimeMillis();
            //ObservationsProvider roverIn = new RinexObservationParserV2(new File("./data/07590920.05o"));
            //NavigationProvider navigationIn = new RinexNavigationParserV2(new File("./data/07590920.05n"));
            ObservationsProvider roverIn =  ParserFactory.produceRinexObsProvider(new File("./data/K09.16o"));
            NavigationProvider navigationIn = ParserFactory.produceRinexNavProvider(new File("./data/K09.16p"));
            PseudoRangeCorrectionProvider prcIn= ParserFactory.producePseudoRangeCorrectionProvider(new File("./data/corrections.txt"));
            // 1st init
            navigationIn.init();
            roverIn.init();
            prcIn.init();

            // Name output files name using Timestamp
            Date date = new Date();
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            String date1 = sdf1.format(date);
            String outPathTxt = "./test/" + date1 + ".txt";
            TxtProducer txt = new TxtProducer(outPathTxt);
            Rtdlib rtdlib = new Rtdlib(navigationIn,roverIn,prcIn);
            rtdlib.addPositionConsumerListener(txt);

            rtdlib.setDynamicModel(dynamicModel);
            rtdlib.runDGNSS();
            try{
                roverIn.release(true,10000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }

            try{
                navigationIn.release(true,10000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }

            prcIn.release(true,10000);

            // To wait for other Thread to be finished
            System.out.println("waiting for finishing all the processes");
            while (Thread.activeCount() > 1){
            }
            System.out.println("Finished!");

            // Get and display elapsed time
            int elapsedTimeSec = (int) Math.floor((System.currentTimeMillis() - start) / 1000);
            int elapsedTimeMillisec = (int) ((System.currentTimeMillis() - start) - elapsedTimeSec * 1000);
            System.out.println("\nElapsed time (read + proc + display + write): "
                    + elapsedTimeSec + " seconds " + elapsedTimeMillisec
                    + " milliseconds.");
        }catch(Exception e){
            e.printStackTrace();
        }
        System.exit(0);

    }
}
