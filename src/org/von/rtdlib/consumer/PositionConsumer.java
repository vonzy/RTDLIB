package org.von.rtdlib.consumer;

import org.von.rtdlib.positioning.ReceiverPosition;

/**
 * Created by diner on 16-10-16.
 * PositionConsumer is a listener to monitor the process of positioning,
 * when position of a epoch is calculated , consumer should be notified by addCoordinate
 */
public interface PositionConsumer {
    public final static int EVENT_START_OF_TRACK = 0;
    public final static int EVENT_END_OF_TRACK = 1;
    public final static int EVENT_RTDLIB_THREAD_ENDED = 2;

    public void addCoordinate(ReceiverPosition coord);
    public void event(int event);
}
