package org.von.rtdlib.provider;

import org.von.rtdlib.StreamResource;
import org.von.rtdlib.positioning.Coordinates;
import org.von.rtdlib.positioning.Observations;

/**
 * Created by diner on 16-10-16.
 */
public interface ObservationsProvider extends StreamResource {
    public Observations getCurrentObservations();
    public Observations getNextObservations();
  //  public Coordinates getDefinedPosition();
}
