package org.datavaultplatform.common.io;

import java.io.IOException;
import java.io.OutputStream;

// TODO might need changing for java17+
public class OutputStreamAdapter extends OutputStream {

  private final OutputStream os;

  public OutputStreamAdapter(OutputStream os) {
    this.os = os;
  }

  @Override
  public void write(int b) throws IOException {
      this.os.write(b);
  }

  @Override
  public void write(byte[] data) throws IOException {
    this.os.write(data);
  }

  @Override
  public void write(byte[] data, int start, int len) throws IOException {
    this.os.write(data, start, len);
  }

  @Override
  public void flush() throws IOException {
    this.os.flush();
  }

  @Override
  public void close() throws IOException {
    this.os.close();
  }

  @Override
  public String toString() {
    return this.os.toString();
  }

  @SuppressWarnings("com.haulmont.jpb.EqualsDoesntCheckParameterClass")
  @Override
  public boolean equals(Object value) {
    return this.os.equals(value);
  }

  @Override
  public int hashCode() {
    return this.os.hashCode();
  }

}
