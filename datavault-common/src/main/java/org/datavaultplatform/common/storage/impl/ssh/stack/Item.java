package org.datavaultplatform.common.storage.impl.ssh.stack;

import java.util.Stack;

public interface Item<D,C> {

  void process(Stack<Item<D,C>> stack, ItemContext<C> context);
}
