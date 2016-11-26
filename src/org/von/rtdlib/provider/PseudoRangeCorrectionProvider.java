package org.von.rtdlib.provider;

import org.von.rtdlib.StreamResource;
import org.von.rtdlib.positioning.PseudoRangeCorrectionSet;

/**
 * Created by diner on 16-10-16.
 */
public interface PseudoRangeCorrectionProvider extends StreamResource {
    public PseudoRangeCorrectionSet getPRC(long unixTime, int satID, char satType);
}
