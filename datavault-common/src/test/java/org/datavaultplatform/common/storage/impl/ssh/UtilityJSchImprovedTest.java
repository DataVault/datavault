package org.datavaultplatform.common.storage.impl.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This is a test of
 * 1) the Comparator used in UtilityJSchImproved
 * 2) the removal of items from an underlying Vector using Iterator.remove()
 */
@ExtendWith(MockitoExtension.class)
public class UtilityJSchImprovedTest {

  @Mock
  ChannelSftp.LsEntry entry1;
  @Mock
  ChannelSftp.LsEntry entry2;
  @Mock
  ChannelSftp.LsEntry entry3;
  @Mock
  ChannelSftp.LsEntry entry4;

  @Mock
  SftpATTRS attrs1;

  @Mock
  SftpATTRS attrs2;
  @Mock
  SftpATTRS attrs3;
  @Mock
  SftpATTRS attrs4;

  @Test
  void testComparator() {

    when(entry1.getAttrs()).thenReturn(attrs1);
    when(entry2.getAttrs()).thenReturn(attrs2);
    when(entry3.getAttrs()).thenReturn(attrs3);
    when(entry4.getAttrs()).thenReturn(attrs4);

    when(attrs1.isDir()).thenReturn(true);
    when(attrs2.isDir()).thenReturn(true);
    when(attrs3.isDir()).thenReturn( false);
    when(attrs4.isDir()).thenReturn(false);
    when(entry1.getFilename()).thenReturn("D-BBBB");
    when(entry2.getFilename()).thenReturn("D-AAAA");
    when(entry3.getFilename()).thenReturn("F-BBBB");
    when(entry4.getFilename()).thenReturn("F-AAAA");

    List<LsEntry> items = Arrays.asList(this.entry1, this.entry2, this.entry3, this.entry4);
    Collections.shuffle(items);

    items.forEach(item -> System.out.printf("BEFORE[%s]%n",item.getFilename()));
    items.sort(UtilityJSchImproved.COMPARATOR);

    items.forEach(item -> System.out.printf("AFTER[%s]%n",item.getFilename()));

    assertEquals(entry4, items.get(0));
    assertEquals(entry3, items.get(1));
    assertEquals(entry2, items.get(2));
    assertEquals(entry1, items.get(3));
  }

  @Test
  void testRemoveFromVectorIterator(){
      Vector items = new Vector<>(Arrays.asList("1","2","3","4","5"));
      Iterator<String> iter = items.iterator();
      while(iter.hasNext()) {
        String item = iter.next();
        System.out.printf("item[%s]items count[%d]%n", item, items.size());
        iter.remove();
      }
      assertEquals(0, items.size());
  }
}
