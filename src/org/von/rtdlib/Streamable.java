package org.von.rtdlib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by diner on 16-10-19.
 */

public interface Streamable {

    public final static String MESSAGE_OBSERVATIONS = "obs";
    public final static String MESSAGE_IONO = "ion";
    public final static String MESSAGE_EPHEMERIS = "eph";
    public final static String MESSAGE_OBSERVATIONS_SET = "eps";
    public final static String MESSAGE_COORDINATES = "coo";

    public int write(DataOutputStream dos) throws IOException;
    public void read(DataInputStream dai, boolean oldVersion) throws IOException;
}

