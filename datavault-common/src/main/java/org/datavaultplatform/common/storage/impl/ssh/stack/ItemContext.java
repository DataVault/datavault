package org.datavaultplatform.common.storage.impl.ssh.stack;

public abstract class ItemContext<T> {

  private T ctx;
  private long size;
  private long count;

  public long getSize(){
    return size;
  }
  public T getContext() {
    return ctx;
  }

  public void setContext(T ctx) {
    this.ctx = ctx;
  }

  public void increment(long inc){
    size += inc;
  }
  public long getCount(){
    return count;
  }
  public void incrementCount(){
    count+=1;
  }
}
