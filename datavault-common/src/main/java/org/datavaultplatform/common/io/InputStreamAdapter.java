package org.datavaultplatform.common.io;

import java.io.IOException;
import java.io.InputStream;

// TODO might need changing for java17+
public class InputStreamAdapter extends InputStream {

  private final InputStream is;

  public InputStreamAdapter(InputStream is) {
    this.is = is;
  }

  @Override
  public int read() throws IOException {
    return is.read();
  }

  @Override
  public int read(byte[] traget) throws IOException {
    return is.read(traget);
  }

  @Override
  public int read(byte[] traget, int start, int len) throws IOException {
    return is.read(traget, start, len);
  }

  @Override
  public void close() throws IOException {
    this.is.close();
  }

  @Override
  public boolean markSupported() {
    return this.is.markSupported();
  }

  @Override
  public void mark(int readLimit) {
    this.is.mark(readLimit);
  }

  @Override
  public int available() throws IOException {
    return this.is.available();
  }

  @Override
  public void reset() throws IOException {
    this.is.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    return this.is.skip(n);
  }

  @Override
  public String toString() {
    return this.is.toString();
  }

  @SuppressWarnings("com.haulmont.jpb.EqualsDoesntCheckParameterClass")
  @Override
  public boolean equals(Object value) {
    return this.is.equals(value);
  }

  @Override
  public int hashCode() {
    return this.is.hashCode();
  }

}
