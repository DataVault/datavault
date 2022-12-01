package org.datavaultplatform.common.storage.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.impl.ssh.UtilitySSHD.SFTPMonitorSSHD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UtilitySSHDTest {

  @Nested
  class SFTPMonitorSSHDTests {

    final Clock clock = Clock.systemDefaultZone();

    @Nested
    class NonMonitoring {

      Progress progress;
      SFTPMonitorSSHD monitor;

      long last;

      @BeforeEach
      void setup() {
        progress = new Progress();
        monitor = new SFTPMonitorSSHD(progress, clock, false);
        last = clock.millis();
      }

      @Test
      @SneakyThrows
      void testMonitorOutputStream() {
        assertFalse(monitor.isMonitoring());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream wrapped = monitor.monitorOutputStream(baos);
        assertEquals(baos, wrapped);
      }

      @Test
      @SneakyThrows
      void testMonitorInputStream() {
        assertFalse(monitor.isMonitoring());
        ByteArrayInputStream bais = new ByteArrayInputStream("TheCatSatOnTheVeryBigMat".getBytes(StandardCharsets.UTF_8));
        InputStream wrapped = monitor.monitorInputStream(bais);
        assertEquals(bais, wrapped);
      }
    }

    @Nested
    class Monitoring {

      Progress progress;
      SFTPMonitorSSHD monitor;

      long last;

      @BeforeEach
      void setup() {
        progress = new Progress();
        monitor = new SFTPMonitorSSHD(progress, clock, true);
        last = clock.millis();
      }

      @Test
      @SneakyThrows
      void testMonitorOutputStream() {
        assertTrue(monitor.isMonitoring());
        ByteArrayOutputStream baos = Mockito.spy(new ByteArrayOutputStream());
        OutputStream wrapped = monitor.monitorOutputStream(baos);

        //test write(byte)
        wrapped.write(0xFF);
        assertEquals(1, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        last = progress.getTimestamp();
        wrapped.write(Integer.MAX_VALUE);
        assertEquals(2, progress.getByteCount());
        byte byte2 = (byte) (Integer.MAX_VALUE & 0xFF);
        wrapped.flush();
        byte[] snapshot1 = baos.toByteArray();
        assertEquals(2, snapshot1.length);
        assertEquals(-1, snapshot1[0]);
        assertEquals(byte2, snapshot1[1]);

        //test write(byte[])
        byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        wrapped.write(hello);
        assertTrue(progress.getTimestamp() >= last);
        last = progress.getTimestamp();
        assertEquals(7, progress.getByteCount());
        wrapped.flush();
        byte[] snapshot2 = baos.toByteArray();
        assertEquals(7, snapshot2.length);
        assertEquals("Hello",
            new String(Arrays.copyOfRange(snapshot2, 2, 7), StandardCharsets.UTF_8));

        //test write(byte[])
        byte[] $123 = "OneTwoThree".getBytes(StandardCharsets.UTF_8);
        wrapped.write($123, 3, 3);
        assertTrue(progress.getTimestamp() >= last);
        last = progress.getTimestamp();
        assertEquals(10, progress.getByteCount());
        wrapped.flush();
        byte[] snapshot3 = baos.toByteArray();
        assertEquals(10, snapshot3.length);
        assertEquals("Two",
            new String(Arrays.copyOfRange(snapshot3, 7, 10), StandardCharsets.UTF_8));

        //test close
        Mockito.verify(baos, never()).close();
        wrapped.close();
        assertTrue(progress.getTimestamp() >= last);
        Mockito.verify(baos, times(1)).close();
      }

      @Test
      @SneakyThrows
      void testMonitorInputStream() {
        long read;
        assertTrue(monitor.isMonitoring());
        ByteArrayInputStream bais = Mockito.spy(
            new ByteArrayInputStream("TheCatSatOnTheVeryBigMat".getBytes(StandardCharsets.UTF_8)));
        InputStream wrapped = monitor.monitorInputStream(bais);

        //test 'int read()'
        int first = wrapped.read();
        assertEquals(1, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        last = progress.getTimestamp();

        int second = wrapped.read();
        assertEquals(2, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        last = progress.getTimestamp();

        int third = wrapped.read();
        assertEquals(3, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        last = progress.getTimestamp();
        assertEquals(Character.valueOf('T').charValue(), first);
        assertEquals(Character.valueOf('h').charValue(), second);
        assertEquals(Character.valueOf('e').charValue(), third);

        //test 'void read(byte[])'
        byte[] target1 = new byte[3];
        read = wrapped.read(target1);
        assertEquals(3, read);
        assertEquals(6, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        assertEquals("Cat", new String(target1, StandardCharsets.UTF_8));

        //test 'void read(byte[], int offset, int len)'
        byte[] target2 = new byte[100];
        read = wrapped.read(target2, 10, 8);
        assertEquals(8, read);
        assertEquals(14, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        assertEquals("SatOnThe",
            new String(Arrays.copyOfRange(target2, 10, 18), StandardCharsets.UTF_8));

        //skip over 'VeryBig'
        assertEquals(7,wrapped.skip(7));

        //test 'void read(byte[])'
        byte[] target3 = new byte[10];
        read = wrapped.read(target3);
        assertEquals(3, read);
        assertEquals(17, progress.getByteCount());
        assertTrue(progress.getTimestamp() >= last);
        assertEquals("Mat", new String(Arrays.copyOfRange(target3, 0, 3), StandardCharsets.UTF_8));

        assertEquals(-1, wrapped.read());
        assertEquals(17, progress.getByteCount());

        assertEquals(-1, wrapped.read(new byte[3]));
        assertEquals(17, progress.getByteCount());

        assertEquals(-1, wrapped.read(new byte[3], 0, 3));
        assertEquals(17, progress.getByteCount());

        //test close
        Mockito.verify(bais, never()).close();
        wrapped.close();
        assertTrue(progress.getTimestamp() >= last);
        Mockito.verify(bais, times(1)).close();

      }
    }
  }
}

