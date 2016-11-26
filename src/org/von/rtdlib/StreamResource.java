package org.von.rtdlib;

/**
 * Created by diner on 16-10-16.
 */
public interface StreamResource {

    public void init() throws Exception;
    //	public void initNVS() throws Exception;
    public void release(boolean waitForThread, long timeoutMs) throws InterruptedException;
}
