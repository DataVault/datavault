package org.datavaultplatform.worker.operations;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This implementation of OutputStream simply discards any data written to it.
 * Useful for testing tar operations where otherwise the final tar file would be very large.
 */
public class BlackHoleOutputStream extends OutputStream {

    @Override
    public void write(byte b[]) throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
    }

}
