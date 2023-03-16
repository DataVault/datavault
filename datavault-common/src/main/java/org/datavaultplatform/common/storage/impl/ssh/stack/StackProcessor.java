package org.datavaultplatform.common.storage.impl.ssh.stack;

import java.util.Stack;

/*
An abstract stack processor which can be used to crawl a tree of directories/files
without using recursion.
@See org.datavaultplatform.common.storage.impl.ssh.UtilityJSchNonRecurse
@see
 */
public class StackProcessor<D,C> {

  private final ItemContext<C> context;
  private final Item<D, C> initialItem;

  public StackProcessor(ItemContext<C> context, Item<D, C> initialItem) {
    this.context = context;
    this.initialItem = initialItem;
  }

  /*
   when crawling directories, the Stack represents files/directories discovered but not processed
   and the context can represent the current directory and any calculated results
   */
  public ItemContext<C> process() {


    Stack<Item<D, C>> stack = new Stack();

    stack.push(initialItem);

    while (!stack.isEmpty()) {
      Item item = stack.pop();
      // when we process an item it may push more items on the stack, it may affect the context
      item.process(stack, context);
    }
    return context;
  }
}

